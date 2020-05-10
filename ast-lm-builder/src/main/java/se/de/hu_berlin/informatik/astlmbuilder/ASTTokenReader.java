package se.de.hu_berlin.informatik.astlmbuilder;

import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import edu.berkeley.nlp.lm.StringWordIndexer;
import edu.berkeley.nlp.lm.io.LmReaderCallback;
import edu.berkeley.nlp.lm.util.LongRef;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.IBasicNodeMapper;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractConsumingProcessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * This token reader parses each file in a given set and sends the read token
 * sequences to the language model.
 * <p>
 * TODO: This Processor can not be used in parallel, since e.g. the token mapper
 * is global and gets accessed by all instanced Processes... But each submitted
 * file has its own private methods (for example) that need to be treated
 * differently for each file... We could use the same mapper if we do not change
 * the mapper by setting the list "globally", but add a new argument to the
 * methods that generate the tokens... Alternatively, we could generate separate
 * instances of the token mapper for each instance of this Processor. (Which
 * would probably be easier, too.) TODO: Further, access to the word indexer
 * should only be granted synchronized...
 *
 * @param <T> the type of the token objects
 */
public class ASTTokenReader<T> extends AbstractConsumingProcessor<Path> {

    private final StringWordIndexer wordIndexer;

    private int startId = 0;
    private int endId = 0;

    private final LmReaderCallback<LongRef> callback;
    // this defines the entry point for the AST
    private final boolean onlyMethodNodes;
    // this enables the black list for unimportant node types
    private final boolean filterNodes;

    // this could be made configurable
    private final IBasicNodeMapper<T> t_mapper;

    // token abstraction depth
    final private int depth;

    final private boolean includeParent;

    private Node lastNode;

    private Node lastParent;

    // this is not accurate because of threads but it does not have to be
    public static final int stats_files_processed = 0;
    public static int stats_files_successfully_parsed = 0;
    public static int stats_fnf_e = 0; // file not found exceptions
    public static final List<String> fnf_list = new ArrayList<>();
    public static int stats_parse_e = 0; // parse exceptions
    public static final int stats_runtime_e = 0; // runtime exceptions
    public static final int stats_general_e = 0; // remaining exceptions
    public static int stats_token_err = 0; // token manager errors
    public static int stats_general_err = 0; // remaining errors

    /**
     * Constructor
     *
     * @param tokenMapper      a token mapper object
     * @param aWordIndexer     the word indexer stores the different ids for the language model
     * @param aCallback        this is the actual language model
     * @param aOnlyMethodNodes if set to true only method nodes will be used to train the language
     *                         model. If set to false the compilation unit will be the root of the
     *                         abstract syntax tree.
     * @param aFilterNodes     if set to true unimportant node types will not be included into the
     *                         language model
     * @param depth            the maximum depth of constructing the tokens, where 0 equals total
     *                         abstraction and -1 means unlimited depth
     * @param includeParent    whether to include information about the parent node
     */
    public ASTTokenReader(IBasicNodeMapper<T> tokenMapper, StringWordIndexer aWordIndexer,
                          LmReaderCallback<LongRef> aCallback, boolean aOnlyMethodNodes, boolean aFilterNodes, int depth, boolean includeParent) {
        super();
        t_mapper = tokenMapper;
        wordIndexer = aWordIndexer;
        callback = aCallback;
        onlyMethodNodes = aOnlyMethodNodes;
        filterNodes = aFilterNodes;
        this.depth = depth;
        this.includeParent = includeParent;

        if (wordIndexer != null) {
            startId = wordIndexer.getOrAddIndex(wordIndexer.getStartSymbol());
            endId = wordIndexer.getOrAddIndex(wordIndexer.getEndSymbol());
        }
    }

    /**
     * Triggers the collection of all token sequences from the given file and
     * adds them to the token language model.
     *
     * @param aSingleFile the path to the file
     */
    private void parseNGramsFromFile(Path aSingleFile) {
        List<List<T>> allSequences;
        try {
            allSequences = getAllTokenSequences(aSingleFile.toFile());
        } catch (ParseProblemException e) {
            Log.err(this, "Parse exception. " + e.getMessage());
            ++stats_parse_e;
            allSequences = Collections.emptyList();
        }

        for (List<T> seq : allSequences) {
            addSequenceToLM(seq);
        }
    }

    /**
     * Parses the file and creates sequences for the language model.
     *
     * @param aFilePath the path to the file that should be parsed
     * @return a list of token sequences
     * @throws ParseProblemException if the file can not be parsed
     */
    public List<List<T>> getAllTokenSequences(String aFilePath) throws ParseProblemException {
        return getAllTokenSequences(new File(aFilePath));
    }

    /**
     * Parses the file and creates sequences for the language model.
     *
     * @param aFilePath the path to the file that should be parsed
     * @return a list of token sequences
     * @throws ParseProblemException if the file can not be parsed
     */
    public List<List<T>> getAllTokenSequences(Path aFilePath) throws ParseProblemException {
        return getAllTokenSequences(aFilePath.toFile());
    }

    /**
     * Parses the file and creates sequences for the language model.
     *
     * @param aSourceFile the file that should be parsed
     * @return a list of token sequences
     * @throws ParseProblemException if the file can not be parsed
     */
    public List<List<T>> getAllTokenSequences(File aSourceFile) throws ParseProblemException {
        List<List<T>> result = new ArrayList<>();

        lastNode = null;
        lastParent = null;
        CompilationUnit cu;
        try (FileInputStream fis = new FileInputStream(aSourceFile)) {
            cu = parseInputStream(fis);
            // search the compilation unit for all method names that we want to
            // ignore
            initBlacklist(cu);

            if (onlyMethodNodes) {
                // this creates string arrays with token sequences starting at
                // method nodes
                getMethodTokenSequences(cu, result);
            } else {
                // just add everything in a single string array
                result.add(getTokenSequenceStartingFromNode(cu));
            }

            ++stats_files_successfully_parsed;

        } catch (IOException e) {
            Log.err(this, "IOException. " + e.getMessage());
            fnf_list.add(aSourceFile.getAbsolutePath());
            ++stats_fnf_e;
//		} catch (ParseException e) {
//			Log.err(this, "Parse exception. " + e.getMessage());
//			++stats_parse_e;
        } catch (TokenMgrException tme) { // this was a token mgr error in the
            // previous version of the java
            // parser
//			Log.err(this, "token manager error: %s", tme);
            ++stats_token_err;
//		} catch (RuntimeException re) {
//			Log.err(this, re, "runtime exception");
//			++stats_runtime_e;
//		} catch (Exception e) {
//			Log.err(this, e, "other exception");
//			++stats_general_e;
        } catch (Error err) {
            String string = err.toString();
            Log.err(this, "general error: %s", string.substring(0, string.length() <= 5000 ? string.length() - 1 : 5000));
            ++stats_general_err;
            Log.err(this, "source file: %s", aSourceFile.getAbsolutePath());
            if (lastParent != null) {
                Position position = lastParent.getBegin().get();
                Log.err(this, "parent node: line %d, content: %s", position == null ? -1 : position.line, String.valueOf(lastParent));
            }
            if (lastNode != null) {
                Position position = lastNode.getBegin().get();
                Log.err(this, "last node: line %d, content: %s", position == null ? -1 : position.line, String.valueOf(lastNode));
            }
        }

//		for (List<T> list : result) {
//			System.out.println(Misc.listToString(list));
//		}

        return result;
    }

    public static <T> List<List<T>> getAllTokenSequencesAndFixCertainErrors(ASTTokenReader<T> reader, Path inputFile) {
        try {
            return reader.getAllTokenSequences(inputFile.toFile());
        } catch (ParseProblemException e) {
            // this may be due to using the keyword "enum" in the package name...
            try {
                String fileContent = FileUtils.readFile2String(inputFile);
                if (fileContent.contains(".enum")) {
                    fileContent = fileContent.replace(".enum", ".enumfix");
                    File fixed = new File(inputFile.toString() + "_fixed.java_");
                    FileUtils.writeString2File(fileContent, fixed);
                    try {
                        return reader.getAllTokenSequences(fixed);
                    } catch (ParseProblemException e1) {
                        // fix did not work... try harder!? ("enum" may be used as a var name, too...) 
                        fileContent = FileUtils.readFile2String(inputFile);
                        if (fileContent.contains("enum")) {
                            fileContent = fileContent.replace("enum", "enumfix");
                            File fixed2 = new File(inputFile.toString() + "_fixed2.java_");
                            FileUtils.writeString2File(fileContent, fixed2);
                            try {
                                return reader.getAllTokenSequences(fixed2);
                            } catch (ParseProblemException e2) {
                                // fix did not work...
                                Log.err(ASTTokenReader.class, e2, "Parsing 'fixed' file '%s' did not succeed.", fixed2);
                            } finally {
                                FileUtils.delete(fixed2);
                            }
                        }
                    } finally {
                        FileUtils.delete(fixed);
                    }
                }
            } catch (IOException e1) {
                Log.err(ASTTokenReader.class, e1, "Fixing parsing error of file '%s' did not succeed.", inputFile);
            }
        }
        return Collections.emptyList();
    }

    private CompilationUnit parseInputStream(FileInputStream fis) throws ParseProblemException {
        CompilationUnit cu;
        cu = JavaParser.parse(fis);

        return cu;
    }

    /**
     * Searches for all nodes under the root node for methods (including
     * constructors) and adds all token sequences to the result collection.
     *
     * @param aNode   the root node
     * @param aResult all token sequences found so far
     */
    private void getMethodTokenSequences(Node aNode, List<List<T>> aResult) {
        if (aNode == null) {
            return;
        } else if (aNode instanceof MethodDeclaration) {
            // collect all tokens from this method and add them to the result
            // collection
            if (((MethodDeclaration) aNode).getBody().isPresent()) {
                aResult.add(getTokenSequenceStartingFromNode(aNode));
            }
        } else if (aNode instanceof ConstructorDeclaration) {
            // collect all tokens from this method and add them to the result
            // collection
            aResult.add(getTokenSequenceStartingFromNode(aNode));
        } else {
            // search for sub nodes of type method
            for (Node n : aNode.getChildNodes()) {
                getMethodTokenSequences(n, aResult);
            }
        }
    }

    /**
     * Searches the node for all relevant tokens and adds them to the sequence
     * which will be added to the language model.
     *
     * @param aNode an AST node
     * @return a list of mapped token that were found under this node
     */
    private List<T> getTokenSequenceStartingFromNode(Node aNode) {
        List<T> result = new ArrayList<>();

        collectAllTokensRec(aNode, null, result);

        return result;
    }

    /**
     * Collects all tokens found in a node.
     *
     * @param aNode     this node will be inspected
     * @param parent    the parent node
     * @param aTokenCol the current collection of all found tokens in this part of the AST
     * @return whether some node was added to the list
     */
    private boolean collectAllTokensRec(Node aNode, Node parent, List<T> aTokenCol) {
        if (filterNodes) {
            // ignore some nodes we do not care about
            if (isNodeTypeIgnored(aNode)) {
                return false;
            }
        }

        lastNode = aNode;
        lastParent = parent;

//		System.out.println(String.valueOf(parent) + " -> " + String.valueOf(aNode));

        // create tokens for the simplest nodes with total abstraction depth...
        if (//aNode.getChildNodes().isEmpty() || 
                aNode instanceof Name || aNode instanceof SimpleName
        ) {
            T token = t_mapper.getMappingForNode(aNode, parent, 0, includeParent, null);
            aTokenCol.add(token);
//			System.out.println(" - token: " + String.valueOf(token));
//			System.out.println(" - next: ( )");
            return true;
//			return false;
        }

        List<Node> nextNodes = new ArrayList<>();
        T token = t_mapper.getMappingForNode(aNode, parent, depth, includeParent, nextNodes);
        if (token != null) {
            aTokenCol.add(token);
        }

//		System.out.println(" - token: " + String.valueOf(token));
//		System.out.println(" - next: " + Misc.listToString(nextNodes));

//		if (token == null || getMaxChildDepth(aNode) > depth) {
        // proceed recursively in a distinctive way
        boolean addedSomeNodes = proceedFromNode(aNode, aTokenCol, nextNodes);

        // add a closing abstract token to mark the ending of a node 
        // (in case any child nodes were added)
        if (addedSomeNodes) {
            T abstractToken = t_mapper.getMappingForNode(aNode, parent, 0, includeParent, null);
            aTokenCol.add(t_mapper.getClosingMapping(abstractToken));
        }
//		}

        return true;
    }

//	private int getMaxChildDepth(Node aNode) {
//		int maxDepth = 0;
//		for (Node node : aNode.getChildNodes()) {
//			int childDepth = getMaxChildDepth(node) + 1;
//			maxDepth = childDepth > maxDepth ? childDepth : maxDepth;
//		}
//		return maxDepth;
//	}

    private List<? extends Node> getOrderedNodeList(Node parent, List<Node> nodes) {
        if (nodes == null) {
            return null;
        }
        nodes.replaceAll(k -> {
            if (k == null) {
                return new IBasicNodeMapper.NullNode(null);
            } else {
                return k;
            }
        });
//		nodes.removeAll(Collections.singleton(null));
        Range lastRange = null;
        boolean isParentRange = true;
        if (parent.getRange().isPresent()) {
            lastRange = parent.getRange().get();
        }
        // set ranges for artificially inserted nodes
        for (Node node : nodes) {
            if (node instanceof IBasicNodeMapper.NullNode ||
                    node instanceof IBasicNodeMapper.NullListNode ||
                    node instanceof IBasicNodeMapper.EmptyListNode) {
                if (isParentRange) {
                    node.setRange(new Range(lastRange.begin, lastRange.begin));
                } else {
                    node.setRange(new Range(lastRange.end, lastRange.end));
                }
            } else {
                if (node.getRange().isPresent()) {
                    lastRange = node.getRange().get();
                    isParentRange = false;
                } else {
                    node.setRange(lastRange);
                }
            }
        }
        List<Node> list = new ArrayList<>(nodes);
        list.sort(Node.NODE_BY_BEGIN_POSITION);
        return list;
//		return nodes;
    }

//	/**
//     * This can be used to sort nodes on position.
//     */
//    public static Comparator<N odeWithRange<?>> NODE_BY_BEGIN_POSITION = (a, b) -> {
//        if (a.getRange().isPresent() && b.getRange().isPresent()) {
//            return a.getRange().get().begin.compareTo(b.getRange().get().begin);
//        }
//        if (a.getRange().isPresent() || b.getRange().isPresent()) {
//            if (a.getRange().isPresent()) {
//                return 1;
//            }
//            return -1;
//        }
//        return 0;
//    };

    /**
     * How to proceed from the distinct nodes. From certain nodes, it makes no
     * real sense to just take the child nodes.
     *
     * @param aNode     this node will be inspected
     * @param aTokenCol the current collection of all found tokens in this part of the AST
     * @param nextNodes a list with child nodes that have to be traversed
     * @return whether some node was added to the list
     */
    @SuppressWarnings("unchecked")
    private boolean proceedFromNode(Node aNode, List<T> aTokenCol, List<Node> nextNodes) {
        if (nextNodes != null) {
            boolean addedSomeNodes = false;
            for (Node n : getOrderedNodeList(aNode, nextNodes)) {
                addedSomeNodes |= collectAllTokensRec(n, aNode, aTokenCol);
            }
            return addedSomeNodes;
        } else {
            List<? extends Node> childNodes = getRelevantChildNodes(aNode);
            //		List<? extends Node> childNodes = getOrderedNodeList(aNode.getChildNodes());
            // proceed with all relevant child nodes
            boolean addedSomeNodes = false;
            for (Node n : getOrderedNodeList(aNode, (List<Node>) childNodes)) {
                addedSomeNodes |= collectAllTokensRec(n, aNode, aTokenCol);
            }
            return addedSomeNodes;
        }
    }

    private List<? extends Node> getRelevantChildNodes(Node parent) {
        if (parent instanceof MethodDeclaration) {
            BlockStmt body = ((MethodDeclaration) parent).getBody().orElse(null);
            if (body != null) {
                return Collections.singletonList(body);
            } else {
                return Collections.emptyList();
            }
        } else if (parent instanceof ConstructorDeclaration) {
            BlockStmt body = ((ConstructorDeclaration) parent).getBody();
            if (body != null) {
                return Collections.singletonList(body);
            } else {
                return Collections.emptyList();
            }
        } else if (parent instanceof ClassOrInterfaceDeclaration) {
            return ((ClassOrInterfaceDeclaration) parent).getMembers();
        } else if (parent instanceof EnumDeclaration) {
            return ((EnumDeclaration) parent).getEntries();
        } else if (parent instanceof CatchClause) {
            return Collections.singletonList(((CatchClause) parent).getBody());
        } else if (parent instanceof Expression) {
            if (parent instanceof EnclosedExpr) {
                return Collections.singletonList(((EnclosedExpr) parent).getInner());
            } else if (parent instanceof ObjectCreationExpr) {
                if (((ObjectCreationExpr) parent).getAnonymousClassBody().isPresent()) {
                    return ((ObjectCreationExpr) parent).getAnonymousClassBody().get();
                } else {
                    return Collections.emptyList();
                }
            } else {
                return Collections.emptyList();
            }
        } else if (parent instanceof Statement) {
            if (parent instanceof IfStmt) {
                Statement elseStatement = ((IfStmt) parent).getElseStmt().orElse(null);
                if (elseStatement == null) {
                    return Collections.singletonList(((IfStmt) parent).getThenStmt());
                } else {
                    List<Statement> temp = new ArrayList<>(2);
                    temp.add(((IfStmt) parent).getThenStmt());
                    temp.add(elseStatement);
                    return temp;
                }
            } else if (parent instanceof BlockStmt) {
                return ((BlockStmt) parent).getStatements();
            } else if (parent instanceof LocalClassDeclarationStmt) {
                return Collections.singletonList(((LocalClassDeclarationStmt) parent).getClassDeclaration());
            } else if (parent instanceof ExpressionStmt) {
                return Collections.singletonList(((ExpressionStmt) parent).getExpression());
            } else if (parent instanceof TryStmt) {
                Statement finallyBlock = ((TryStmt) parent).getFinallyBlock().orElse(null);
                List<Node> temp = new ArrayList<>();
                temp.add(((TryStmt) parent).getTryBlock());
                temp.addAll(((TryStmt) parent).getCatchClauses());
                if (finallyBlock != null) {
                    temp.add(finallyBlock);
                }
                return temp;
            } else if (parent instanceof WhileStmt) {
                return Collections.singletonList(((WhileStmt) parent).getBody());
            } else if (parent instanceof DoStmt) {
                return Collections.singletonList(((DoStmt) parent).getBody());
            } else if (parent instanceof ForStmt) {
                return Collections.singletonList(((ForStmt) parent).getBody());
            } else if (parent instanceof ForeachStmt) {
                return Collections.singletonList(((ForeachStmt) parent).getBody());
            } else if (parent instanceof SwitchStmt) {
                return ((SwitchStmt) parent).getEntries();
            } else if (parent instanceof SwitchEntryStmt) {
                return ((SwitchEntryStmt) parent).getStatements();
            } else if (parent instanceof SynchronizedStmt) {
                return Collections.singletonList(((SynchronizedStmt) parent).getBody());
            } else {
                return Collections.emptyList();
            }
        } else {
            return parent.getChildNodes();
        }
    }

    /**
     * Maps the sequences to the indices and sends it to the language model
     * object.
     *
     * @param aTokenSequence the sequences that were extracted from the abstract syntax tree
     */
    private void addSequenceToLM(List<T> aTokenSequence) {
        final int[] sent = new int[aTokenSequence.size() + 2];
        sent[0] = startId;
        sent[sent.length - 1] = endId;

        for (int i = 0; i < aTokenSequence.size(); ++i) {
            // the word indexer needs a string here... This works if T is a
            // String itself
            // or if the method toString() is overridden for T to return the
            // token as a string
            sent[i + 1] = wordIndexer.getOrAddIndexFromString(aTokenSequence.get(i).toString());
        }

        callback.call(sent, 0, sent.length, new LongRef(1L), null);
    }

    /**
     * We ignore a couple of node types if we use the normal mode. This could be
     * done with a black list in the options but the entries are always the
     * same.
     *
     * @param aNode a node
     * @return true if the node should be ignored, false otherwise
     */
    private boolean isNodeTypeIgnored(Node aNode) {

//		if (aNode == null) {
//			return true;
//		}

        return aNode instanceof Comment;

    }

    // /**
    // * Some nodes just have no informational value in itself but its children
    // * should be checked anyway.
    // *
    // * @param aNode
    // * The node that should be checked
    // * @return false if the node should not be put into the language model but
    // * its children should be checked regardless
    // */
    // private boolean isNodeImportant(Node aNode) {
    //
    // if (aNode == null) {
    // return false;
    // }
    //
    // if (aNode instanceof BlockStmt || aNode instanceof ExpressionStmt ||
    // aNode instanceof EnclosedExpr) {
    // return false;
    // }
    //
    // return true;
    // }

    /**
     * Initializes the black list for private method names.
     *
     * @param aCu the compilation unit
     */
    private void initBlacklist(CompilationUnit aCu) {
        // private method names blacklist
        Set<String> privMethodsBL = new HashSet<>();
        collectAllPrivateMethodNames(aCu, privMethodsBL);
        t_mapper.setPrivateMethodBlackList(privMethodsBL);
    }

    /**
     * Searches the compilation unit and all class declarations for method
     * declarations of private methods and fills the given list with the method
     * names.
     *
     * @param aCu           the compilation unit of a source file
     * @param privMethodsBL private method names blacklist to fill
     */
    private void collectAllPrivateMethodNames(Node aCu, Collection<String> privMethodsBL) {
        for (Node singleNode : aCu.getChildNodes()) {
            if (singleNode instanceof MethodDeclaration) {
                MethodDeclaration mdec = (MethodDeclaration) singleNode;
                if (mdec.getModifiers().contains(Modifier.PRIVATE)) {
                    privMethodsBL.add(mdec.getName().getIdentifier());
                }
            } else {
                collectAllPrivateMethodNames(singleNode, privMethodsBL);
            }
        }
    }

    @Override
    public void consumeItem(Path item) {
        parseNGramsFromFile(item);
    }

}
