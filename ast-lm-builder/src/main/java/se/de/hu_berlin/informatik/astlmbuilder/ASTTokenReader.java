package se.de.hu_berlin.informatik.astlmbuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.TokenMgrException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import edu.berkeley.nlp.lm.StringWordIndexer;
import edu.berkeley.nlp.lm.io.LmReaderCallback;
import edu.berkeley.nlp.lm.util.LongRef;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.IBasicNodeMapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractConsumingProcessor;

/**
 * This token reader parses each file in a given set and sends the read token
 * sequences to the language model.
 * 
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
 * @param <T>
 * the type of the token objects
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

	// this is not accurate because of threads but it does not have to be
	public static int stats_files_processed = 0;
	public static int stats_files_successfully_parsed = 0;
	public static int stats_fnf_e = 0; // file not found exceptions
	public static List<String> fnf_list = new ArrayList<String>();
	public static int stats_parse_e = 0; // parse exceptions
	public static int stats_runtime_e = 0; // runtime exceptions
	public static int stats_general_e = 0; // remaining exceptions
	public static int stats_token_err = 0; // token manager errors
	public static int stats_general_err = 0; // remaining errors

	/**
	 * Constructor
	 * @param tokenMapper
	 * a token mapper object
	 * @param aWordIndexer
	 * the word indexer stores the different ids for the language model
	 * @param aCallback
	 * this is the actual language model
	 * @param aOnlyMethodNodes
	 * if set to true only method nodes will be used to train the language
	 * model. If set to false the compilation unit will be the root of the
	 * abstract syntax tree.
	 * @param aFilterNodes
	 * if set to true unimportant node types will not be included into the
	 * language model
	 * @param depth
	 * the maximum depth of constructing the tokens, where 0 equals total
	 * abstraction and -1 means unlimited depth
	 */
	public ASTTokenReader(IBasicNodeMapper<T> tokenMapper, StringWordIndexer aWordIndexer,
			LmReaderCallback<LongRef> aCallback, boolean aOnlyMethodNodes, boolean aFilterNodes, int depth) {
		super();
		t_mapper = tokenMapper;
		wordIndexer = aWordIndexer;
		callback = aCallback;
		onlyMethodNodes = aOnlyMethodNodes;
		filterNodes = aFilterNodes;
		this.depth = depth;

		if (wordIndexer != null) {
			startId = wordIndexer.getOrAddIndex(wordIndexer.getStartSymbol());
			endId = wordIndexer.getOrAddIndex(wordIndexer.getEndSymbol());
		}
	}

	/**
	 * Triggers the collection of all token sequences from the given file and
	 * adds them to the token language model.
	 * @param aSingleFile
	 * the path to the file
	 */
	private void parseNGramsFromFile(Path aSingleFile) {
		List<List<T>> allSequences = getAllTokenSequences(aSingleFile.toFile());

		for (List<T> seq : allSequences) {
			addSequenceToLM(seq);
		}
	}

	/**
	 * Parses the file and creates sequences for the language model.
	 * @param aFilePath
	 * the path to the file that should be parsed
	 * @return a list of token sequences
	 */
	public List<List<T>> getAllTokenSequences(String aFilePath) {
		return getAllTokenSequences(new File(aFilePath));
	}

	/**
	 * Parses the file and creates sequences for the language model.
	 * @param aFilePath
	 * the path to the file that should be parsed
	 * @return a list of token sequences
	 */
	public List<List<T>> getAllTokenSequences(Path aFilePath) {
		return getAllTokenSequences(aFilePath.toFile());
	}

	/**
	 * Parses the file and creates sequences for the language model.
	 * @param aSourceFile
	 * the file that should be parsed
	 * @return a list of token sequences
	 */
	public List<List<T>> getAllTokenSequences(File aSourceFile) {
		List<List<T>> result = new ArrayList<List<T>>();

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

		} catch (FileNotFoundException e) {
			Log.err(this, e, "not found");
			fnf_list.add(aSourceFile.getAbsolutePath());
			++stats_fnf_e;
		} catch (ParseException e) {
			Log.err(this, e, "parse exception");
			++stats_parse_e;
		} catch (TokenMgrException tme) { // this was a token mgr error in the
											// previous version of the java
											// parser
			Log.err(this, "token manager error: %s", tme);
			++stats_token_err;
		} catch (RuntimeException re) {
			Log.err(this, re, "runtime exception");
			++stats_runtime_e;
		} catch (Exception e) {
			Log.err(this, e, "other exception");
			++stats_general_e;
		} catch (Error err) {
			Log.err(this, "general error: %s", err);
			++stats_general_err;
		}

		return result;
	}

	private CompilationUnit parseInputStream(FileInputStream fis) throws ParseException {
		CompilationUnit cu;
		cu = JavaParser.parse(fis);

		return cu;
	}

	/**
	 * Searches for all nodes under the root node for methods (including
	 * constructors) and adds all token sequences to the result collection.
	 * @param aNode
	 * the root node
	 * @param aResult
	 * all token sequences found so far
	 */
	private void getMethodTokenSequences(Node aNode, List<List<T>> aResult) {
		if (aNode == null) {
			return;
		} else if (aNode instanceof MethodDeclaration || aNode instanceof ConstructorDeclaration) {
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
	 * @param aNode
	 * an AST node
	 * @return a list of mapped token that were found under this node
	 */
	private List<T> getTokenSequenceStartingFromNode(Node aNode) {
		List<T> result = new ArrayList<T>();

		collectAllTokensRec(aNode, result);

		return result;
	}

	/**
	 * Collects all tokens found in a node.
	 * @param aNode
	 * this node will be inspected
	 * @param aTokenCol
	 * the current collection of all found tokens in this part of the AST
	 * @param depth
	 * the maximum depth of constructing the tokens, where 0 equals total
	 * abstraction and -1 means unlimited depth
	 */
	private void collectAllTokensRec(Node aNode, List<T> aTokenCol) {
		// don't create tokens for the simplest leaf nodes...
		if (aNode.getChildNodes().isEmpty() || aNode instanceof Name) {
			return;
		}

		if (filterNodes) {
			// ignore some nodes we do not care about
			if (isNodeTypeIgnored(aNode)) {
				return;
			}
		}

		// add this token to the token list
		aTokenCol.add(t_mapper.getMappingForNode(aNode, depth));
		// proceed recursively in a distinct way
		proceedFromNode(aNode, aTokenCol);

		// some nodes have a closing tag
		T closingTag = t_mapper.getClosingToken(aNode);
		if (closingTag != null) {
			aTokenCol.add(closingTag);
		}
	}

	/**
	 * How to proceed from the distinct nodes. From certain nodes, it makes no
	 * real sense to just take the child nodes.
	 * @param aNode
	 * this node will be inspected
	 * @param aTokenCol
	 * the current collection of all found tokens in this part of the AST
	 * @param depth
	 * the maximum depth of constructing the tokens, where 0 equals total
	 * abstraction and -1 means unlimited depth
	 */
	private void proceedFromNode(Node aNode, List<T> aTokenCol) {
		List<? extends Node> childNodes = getRelevantChildNodes(aNode);
		// proceed with all relevant child nodes
		for (Node n : childNodes) {
			collectAllTokensRec(n, aTokenCol);
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
		} else if (parent instanceof IfStmt) {
			Statement elseStatement = ((IfStmt) parent).getElseStmt().orElse(null);
			if (elseStatement == null) {
				return Collections.singletonList(((IfStmt) parent).getThenStmt());
			} else {
				List<Statement> temp = new ArrayList<>(2);
				temp.add(((IfStmt) parent).getThenStmt());
				temp.add(elseStatement);
				return temp;
			}
		} else if (parent instanceof ClassOrInterfaceDeclaration) {
			return ((ClassOrInterfaceDeclaration) parent).getMembers();
		} else if (parent instanceof EnumDeclaration) {
			return ((EnumDeclaration) parent).getEntries();
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
		} else {
			return parent.getChildNodes();
		}
	}

	/**
	 * Maps the sequences to the indices and sends it to the language model
	 * object.
	 * @param aTokenSequence
	 * the sequences that were extracted from the abstract syntax tree
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
	 * @param aNode
	 * a node
	 * @return true if the node should be ignored, false otherwise
	 */
	private boolean isNodeTypeIgnored(Node aNode) {

		if (aNode == null) {
			return true;
		}

		if (aNode instanceof Comment
		// || aNode instanceof MarkerAnnotationExpr || aNode instanceof
		// NormalAnnotationExpr
		// || aNode instanceof SingleMemberAnnotationExpr || aNode instanceof
		// MemberValuePair
		) {
			return true;
		}

		return false;
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
	 * @param aCu
	 * the compilation unit
	 */
	private void initBlacklist(CompilationUnit aCu) {
		// private method names blacklist (HashMap<String> would be a bit much
		// for low entry counts)
		List<String> privMethodsBL = new ArrayList<>();
		collectAllPrivateMethodNames(aCu, privMethodsBL);
		if (privMethodsBL != null) {
			t_mapper.setPrivateMethodBlackList(privMethodsBL);
		}
	}

	/**
	 * Searches the compilation unit and all class declarations for method
	 * declarations of private methods and fills the given list with the method
	 * names.
	 * @param aCu
	 * the compilation unit of a source file
	 * @param privMethodsBL
	 * private method names blacklist to fill
	 * @return a list storing all private method names
	 */
	private void collectAllPrivateMethodNames(Node aCu, List<String> privMethodsBL) {
		for (Node singleNode : aCu.getChildNodes()) {
			if (singleNode instanceof MethodDeclaration) {
				MethodDeclaration mdec = (MethodDeclaration) singleNode;
				if (mdec.getModifiers().contains(Modifier.PRIVATE)) {
					privMethodsBL.add(mdec.getNameAsString());
				}
			} else if (singleNode instanceof ClassOrInterfaceDeclaration) {
				collectAllPrivateMethodNames(singleNode, privMethodsBL);
			}
		}
	}

	@Override
	public void consumeItem(Path item) {
		parseNGramsFromFile(item);
	}

}
