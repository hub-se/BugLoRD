package se.de.hu_berlin.informatik.astlmbuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.TokenMgrError;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.TypeParameter;
import com.github.javaparser.ast.body.AnnotableNode;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ModifierSet;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ReferenceType;

import edu.berkeley.nlp.lm.StringWordIndexer;
import edu.berkeley.nlp.lm.io.LmReaderCallback;
import edu.berkeley.nlp.lm.util.LongRef;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.ITokenMapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithPaths;

/**
 * This token reader parses each file in a given set and sends the read token
 * sequences to the language model.
 * @param <T>
 * The type of the token objects
 */
public class ASTTokenReader<T> extends CallableWithPaths<Path, Boolean> {
	
	private StringWordIndexer wordIndexer;

	private int startId = 0;
	private int endId = 0;

	private LmReaderCallback<LongRef> callback;
	// this defines the entry point for the AST
	private boolean onlyMethodNodes = true;
	// this enables the black list for unimportant node types
	private boolean filterNodes = true;

	// one logger especially for the errors
	private Logger errLog = Logger.getLogger(ASTTokenReader.class);

	// this could be made configurable
	private ITokenMapper<T> t_mapper;
	
	// private method names blacklist (HashMap<String> would be a bit much for low entry counts)
	private Collection<String> privMethodsBL = new ArrayList<String>();

	// this is not accurate because of threads but it does not have to be
	public static int stats_files_processed = 0;
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
	 * 			  a token mapper object
	 * @param aWordIndexer
	 *            The word indexer stores the different ids for the language
	 *            model
	 * @param aCallback
	 *            This is the actual language model
	 * @param aOnlyMethodNodes
	 *            If set to true only method nodes will be used to train the
	 *            language model. If set to false the compilation unit will be
	 *            the root of the abstract syntax tree.
	 * @param aFilterNodes
	 *            If set to true unimportant node types will not be included
	 *            into the language model
	 */
	public ASTTokenReader(ITokenMapper<T> tokenMapper, StringWordIndexer aWordIndexer, LmReaderCallback<LongRef> aCallback, boolean aOnlyMethodNodes,
			boolean aFilterNodes) {
		super();
		t_mapper = tokenMapper;
		wordIndexer = aWordIndexer;
		callback = aCallback;
		onlyMethodNodes = aOnlyMethodNodes;
		filterNodes = aFilterNodes;

		if (wordIndexer != null) {
			startId = wordIndexer.getOrAddIndex(wordIndexer.getStartSymbol());
			endId = wordIndexer.getOrAddIndex(wordIndexer.getEndSymbol());
		}
		// currently there are too many errors in the
		// corpus to see anything
		errLog.setLevel(Level.FATAL);
	}

	/**
	 * Triggers the collection of all token sequences from the given file and
	 * adds them to the token language model
	 * 
	 * @param aSingleFile
	 *            The path to the file
	 */
	private void parseNGramsFromFile(Path aSingleFile) {
		List<List<T>> allSequences = getAllTokenSequences(aSingleFile.toFile());
		// iterate over each sequence
		
//		File outputFile = new File( "C:\tmp\tmpOPF.txt" );
		
		for (List<T> seq : allSequences) {
			addSequenceToLM(seq);
		}
	}

	/**
	 * Parses the file and creates sequences for the language model
	 * 
	 * @param aFilePath
	 *            The path to the file that should be parsed
	 * @return A list of token sequences
	 */
	public List<List<T>> getAllTokenSequences(String aFilePath) {
		return getAllTokenSequences(new File(aFilePath));
	}

	/**
	 * Parses the file and creates sequences for the language model
	 * 
	 * @param aFilePath
	 *            The path to the file that should be parsed
	 * @return A list of token sequences
	 */
	public List<List<T>> getAllTokenSequences(Path aFilePath) {
		return getAllTokenSequences(aFilePath.toFile());
	}

	/**
	 * Parses the file and creates sequences for the language model
	 * 
	 * @param aSourceFile
	 *            The file that should be parsed
	 * @return A list of token sequences
	 */
	public List<List<T>> getAllTokenSequences(File aSourceFile) {
		List<List<T>> result = new ArrayList<List<T>>();

		CompilationUnit cu;
		try (FileInputStream fis = new FileInputStream(aSourceFile)) {
			cu = JavaParser.parse(fis);
			// search the compilation unit for all method names that we want to ignore
			initBlacklist( cu );
			
			if (onlyMethodNodes) {
				// this creates string arrays with token sequences starting at
				// method nodes
				getMethodTokenSequences(cu, result);
			} else {
				// just add everything in a single string array
				result.add(getAllTokensFromNode(cu));
			}

		} catch (FileNotFoundException e) {
			Log.err(this, e, "not found");
			fnf_list.add(aSourceFile.getAbsolutePath());
			++stats_fnf_e;
			errLog.error(e);
		} catch (ParseException e) {
			Log.err(this, e, "parse exception");
			++stats_parse_e;
			errLog.error(e);
		} catch (RuntimeException re) {
			Log.err(this, re, "runtime exception");
			++stats_runtime_e;
			errLog.error(re);
		} catch (Exception e) {
			Log.err(this, e, "other exception");
			++stats_general_e;
			errLog.error(e);
		} catch (TokenMgrError tme) {
			Log.err(this, "token manager error: %s", tme);
			++stats_token_err;
			errLog.error(tme);
		} catch (Error err) {
			Log.err(this, "general error: %s", err);
			++stats_general_err;
			errLog.error(err);
		}

		return result;
	}


	/**
	 * Searches for all nodes under the root node for methods (including
	 * constructors) and adds all token sequences to the result collection.
	 * 
	 * @param rootNode
	 *            The root node
	 * @param result
	 *            all token sequences found so far
	 */
	private void getMethodTokenSequences(Node aRootNode, List<List<T>> aResult) {
		if (aRootNode == null) {
			return;
		} else if (aRootNode instanceof MethodDeclaration || aRootNode instanceof ConstructorDeclaration) {
			// collect all tokens from this method and add them to the result
			// collection
			aResult.add(getAllTokensFromNode(aRootNode));
		} else {
			// search for sub nodes of type method
			for (Node n : aRootNode.getChildrenNodes()) {
				getMethodTokenSequences(n, aResult);
			}
		}
	}


	/**
	 * Searches the node for all relevant tokens and adds them to the sequence
	 * which will be added to the language model
	 * 
	 * @param aNode
	 * an AST node
	 * @return a list of mapped token that were found under this node
	 */
	private List<T> getAllTokensFromNode(Node aNode) {
		// TODO in case entry tokens should not be merged change here
		List<T> result = new ArrayList<T>();
		if (aNode instanceof MethodDeclaration) {
			result.addAll(t_mapper.getMappingForNode(aNode).getMappings());
			collectAllTokensRec(((MethodDeclaration) aNode).getBody(), result);
		} else if (aNode instanceof ConstructorDeclaration) {
			result.addAll(t_mapper.getMappingForNode(aNode).getMappings());
			collectAllTokensRec(((ConstructorDeclaration) aNode).getBlock(), result);
		} else {
			collectAllTokensRec(aNode, result);
		}
		
		// some nodes have a closing tag
		T closingTag = t_mapper.getClosingToken(aNode);
		if (closingTag != null) {
			result.add(closingTag);
		}

		return result;
	}

	/**
	 * Collects all tokens found in a node
	 * 
	 * @param aChildNode
	 *            This node will be inspected
	 * @param aTokenCol
	 *            The current collection of all found tokens in this part of the
	 *            AST
	 */
	private void collectAllTokensRec(Node aChildNode, List<T> aTokenCol) {
		if (filterNodes) {
			// ignore some nodes we do not care about
			if (isNodeTypeIgnored(aChildNode)) {
				return;
			}

			if (isNodeImportant(aChildNode)) {
				collectAnnotations(aChildNode, aTokenCol);
				aTokenCol.addAll(t_mapper.getMappingForNode(aChildNode).getMappings());
			}
		} else {
			// add this token regardless of importance
			collectAnnotations(aChildNode, aTokenCol);
			aTokenCol.addAll(t_mapper.getMappingForNode(aChildNode).getMappings());
		}

		//proceed recursively in a distinct way
		proceedFromNode(aChildNode, aTokenCol);

		// some nodes have a closing tag
		T closingTag = t_mapper.getClosingToken(aChildNode);
		if (closingTag != null) {
			aTokenCol.add(closingTag);
		}
	}
	
	/**
	 * How to proceed from the distinct nodes. From certain nodes, it makes no real
	 * sense to just take the child nodes.
	 * 
	 * @param aChildNode
	 *            This node will be inspected
	 * @param aTokenCol
	 *            The current collection of all found tokens in this part of the
	 *            AST
	 */
	private void proceedFromNode(Node aChildNode, List<T> aTokenCol) {
		if (aChildNode instanceof MethodDeclaration) {
			if (((MethodDeclaration) aChildNode).getBody() != null) {
				// iterate over all children in the method body
				for (Node n : ((MethodDeclaration) aChildNode).getBody().getChildrenNodes()) {
					collectAllTokensRec(n, aTokenCol);
				}
			}
		} else if (aChildNode instanceof IfStmt) {
			// iterate over all children in the 'then' block
			for (Node n : ((IfStmt) aChildNode).getThenStmt().getChildrenNodes()) {
				collectAllTokensRec(n, aTokenCol);
			}
			if (((IfStmt) aChildNode).getElseStmt() != null) {
				aTokenCol.addAll(t_mapper.getMappingForNode(new ElseStmt()).getMappings());
				// iterate over all children in the 'else' block
				for (Node n : ((IfStmt) aChildNode).getElseStmt().getChildrenNodes()) {
					collectAllTokensRec(n, aTokenCol);
				}
			}
		} else if (aChildNode instanceof ClassOrInterfaceDeclaration) {
			if (((ClassOrInterfaceDeclaration) aChildNode).getExtends() != null
					&& ((ClassOrInterfaceDeclaration) aChildNode).getExtends().size() > 0) {
				aTokenCol.addAll(t_mapper.getMappingForNode(
						new ExtendsStmt(((ClassOrInterfaceDeclaration) aChildNode).getExtends())).getMappings());
			}
			if (((ClassOrInterfaceDeclaration) aChildNode).getImplements() != null
					&& ((ClassOrInterfaceDeclaration) aChildNode).getImplements().size() > 0) {
				aTokenCol.addAll(t_mapper.getMappingForNode(
						new ImplementsStmt(((ClassOrInterfaceDeclaration) aChildNode).getImplements())).getMappings());
			}
			// call this method for all children
			for (Node n : ((ClassOrInterfaceDeclaration) aChildNode).getMembers()) {
				collectAllTokensRec(n, aTokenCol);
			}
		} else if (aChildNode instanceof EnumDeclaration) {
			if (((EnumDeclaration) aChildNode).getImplements() != null) {
				aTokenCol.addAll(t_mapper.getMappingForNode(
						new ImplementsStmt(((EnumDeclaration) aChildNode).getImplements())).getMappings());
			}
			// iterate over all children in the body
			for (Node n : ((EnumDeclaration) aChildNode).getEntries()) {
				collectAllTokensRec(n, aTokenCol);
			}
		} else if (aChildNode instanceof WhileStmt) {
			// iterate over all children in the body
			for (Node n : ((WhileStmt) aChildNode).getBody().getChildrenNodes()) {
				collectAllTokensRec(n, aTokenCol);
			}
		} else if (aChildNode instanceof DoStmt) {
			// iterate over all children in the body
			for (Node n : ((DoStmt) aChildNode).getBody().getChildrenNodes()) {
				collectAllTokensRec(n, aTokenCol);
			}
		} else if (aChildNode instanceof ForStmt) {
			// iterate over all children in the body
			for (Node n : ((ForStmt) aChildNode).getBody().getChildrenNodes()) {
				collectAllTokensRec(n, aTokenCol);
			}
		} else if (aChildNode instanceof ForeachStmt) {
			// iterate over all children in the body
			for (Node n : ((ForeachStmt) aChildNode).getBody().getChildrenNodes()) {
				collectAllTokensRec(n, aTokenCol);
			}
		} else if (aChildNode instanceof SwitchStmt) {
			// iterate over all children in the body
			for (Node n : ((SwitchStmt) aChildNode).getEntries()) {
				collectAllTokensRec(n, aTokenCol);
			}
		} else if (aChildNode instanceof PackageDeclaration) {
			// iterate over all children in the body
			for (Node n : ((PackageDeclaration) aChildNode).getAnnotations()) {
				collectAllTokensRec(n, aTokenCol);
			}
		} else  //for certain nodes, don't proceed to the children nodes 
			if (//!(aChildNode instanceof Expression) &&
				!(aChildNode instanceof ImportDeclaration)
//				&& !(aChildNode instanceof VariableDeclarator)
//				&& !(aChildNode instanceof EnumConstantDeclaration)
//				&& !(aChildNode instanceof ExplicitConstructorInvocationStmt)
//				&& !(aChildNode instanceof PackageDeclaration)
//				&& !(aChildNode instanceof AssertStmt)
				) {
			// call this method for all children
			for (Node n : aChildNode.getChildrenNodes()) {
				collectAllTokensRec(n, aTokenCol);
			}
		}
	}

	/**
	 * Collect the annotations from distinct nodes.
	 * 
	 * @param aChildNode
	 *            This node will be inspected
	 * @param aTokenCol
	 *            The current collection of all found tokens in this part of the
	 *            AST
	 */
	private void collectAnnotations(Node aChildNode, List<T> aTokenCol) {
		if (aChildNode instanceof MethodDeclaration) {
			// iterate over all annotations
			for (Node n : ((MethodDeclaration) aChildNode).getAnnotations()) {
				collectAllTokensRec(n, aTokenCol);
			}
		} else if (aChildNode instanceof PackageDeclaration) {
			// iterate over all annotations
			for (Node n : ((PackageDeclaration) aChildNode).getAnnotations()) {
				collectAllTokensRec(n, aTokenCol);
			}
		} else if (aChildNode instanceof TypeParameter) {
			// iterate over all annotations
			for (Node n : ((TypeParameter) aChildNode).getAnnotations()) {
				collectAllTokensRec(n, aTokenCol);
			}
		} else if (aChildNode instanceof AnnotableNode) {
			// iterate over all annotations
			for (Node n : ((AnnotableNode) aChildNode).getAnnotations()) {
				collectAllTokensRec(n, aTokenCol);
			}
		} else if (aChildNode instanceof ReferenceType) {
			// iterate over all annotations
			for (Node n : ((ReferenceType) aChildNode).getAnnotations()) {
				collectAllTokensRec(n, aTokenCol);
			}
		} else if (aChildNode instanceof ArrayCreationExpr) {
			// iterate over all annotations
			for (List<AnnotationExpr> list : ((ArrayCreationExpr) aChildNode).getArraysAnnotations()) {
				if (list != null) {
					for (Node n : list) {
						collectAllTokensRec(n, aTokenCol);
					}
				}
			}
		}
	}

	/**
	 * Maps the sequences to the indices and sends it to the language model
	 * object
	 * 
	 * @param aTokenSequence
	 *            The sequences that were extracted from the abstract syntax
	 *            tree
	 */
	private void addSequenceToLM(List<T> aTokenSequence) {
		final int[] sent = new int[aTokenSequence.size() + 2];
		sent[0] = startId;
		sent[sent.length - 1] = endId;
		
		for (int i = 0; i < aTokenSequence.size(); ++i) {
			//the word indexer needs a string here... This works if T is a String itself
			//or if the method toString() is overridden for T to return the token as a string
			sent[i + 1] = wordIndexer.getOrAddIndexFromString(aTokenSequence.get(i).toString());
		}

		callback.call(sent, 0, sent.length, new LongRef(1L), null);
	}

	@Override
	public Boolean call() {
		// TODO remove after testing?
		// TODO why 1024 and not 1000 or sth like that?
		if (++stats_files_processed % 1024 == 0) {
			// not using the usual logger because of fatal level
			Log.out(this, stats_files_processed + " files processed");
		}

		parseNGramsFromFile(getInput());

		return true;
	}

	/**
	 * We ignore a couple of node types if we use the normal mode. This could be
	 * done with a black list in the options but the entries are always the same
	 * so i guess a configuration is not needed.
	 * 
	 * @param aNode
	 * @return true if the node should be ignored false otherwise
	 */
	private boolean isNodeTypeIgnored(Node aNode) {

		if (aNode == null) {
			return true;
		}

		if (aNode instanceof Comment
		// || aNode instanceof MarkerAnnotationExpr || aNode instanceof
		// NormalAnnotationExpr
		// || aNode instanceof SingleMemberAnnotationExpr
		// // I dont even know what this is supposed to be
		// //TODO and that makes it safe to throw away? :)
		// || aNode instanceof MemberValuePair
		) {
			return true;
		}

		return false;
	}

	/**
	 * Some nodes just have no informational value in itself but its children
	 * should be checked anyway.
	 * 
	 * @param aNode
	 *            The node that should be checked
	 * @return false if the node should not be put into the language model but
	 *         its children should be checked regardless
	 */
	private boolean isNodeImportant(Node aNode) {

		if (aNode == null) {
			return false;
		}

		if (aNode instanceof BlockStmt || aNode instanceof ExpressionStmt || aNode instanceof EnclosedExpr) {
			return false;
		}

		return true;
	}
	
	/**
	 * Initializes the black list for private method names
	 * @param aCu The compilation unit
	 */
	private void initBlacklist( CompilationUnit aCu ) {
		privMethodsBL = new ArrayList<String>();
		collectAllPrivateMethodNames( aCu );
		if( privMethodsBL != null ) {
			t_mapper.setPrivMethodBlackList( privMethodsBL );
		}
	}
	
	/**
	 * Searches the compilation unit and all class declarations for method declarations
	 * of private methods and returns a list of the method names.
	 * @param aCu The compilation unit of a source file
	 * @return A list storing all private method names
	 */
	private void collectAllPrivateMethodNames( Node aCu ) {
		
		for ( Node singleNode : aCu.getChildrenNodes() ) {
			if ( singleNode instanceof MethodDeclaration ) {
				MethodDeclaration mdec = (MethodDeclaration) singleNode;
				if ( ModifierSet.isPrivate( mdec.getModifiers() ) ) {
					privMethodsBL.add( mdec.getName() );
				}
			} else if ( singleNode instanceof ClassOrInterfaceDeclaration ) {
				collectAllPrivateMethodNames( singleNode );
			}
		}

	}

}
