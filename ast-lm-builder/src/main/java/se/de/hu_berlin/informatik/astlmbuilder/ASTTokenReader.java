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
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ModifierSet;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;

import edu.berkeley.nlp.lm.StringWordIndexer;
import edu.berkeley.nlp.lm.io.LmReaderCallback;
import edu.berkeley.nlp.lm.util.LongRef;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.ITokenMapperShort;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.AbstractDisruptorEventHandlerFactory;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.DisruptorFCFSEventHandler;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInput;

/**
 * This token reader parses each file in a given set and sends the read token
 * sequences to the language model.
 * @param <T>
 * the type of the token objects
 */
public class ASTTokenReader<T> extends EHWithInput<Path> {
	
	public static class Factory<T> extends AbstractDisruptorEventHandlerFactory<Path> {

		private final StringWordIndexer wordIndexer;

		private final LmReaderCallback<LongRef> callback;
		// this defines the entry point for the AST
		private boolean onlyMethodNodes = true;
		// this enables the black list for unimportant node types
		private boolean filterNodes = true;

		// this could be made configurable
		private ITokenMapperShort<T,Integer> t_mapper;
		
		// token abstraction depth
		final private int depth;
		final private int seriDepth;
		
		/**
		 * Constructor
		 * @param tokenMapper
		 * a token mapper object
		 * @param aWordIndexer
		 * the word indexer stores the different ids for the language model
		 * @param aCallback
		 * this is the actual language model
		 * @param aOnlyMethodNodes
		 * if set to true only method nodes will be used to train the
		 * language model. If set to false the compilation unit will be
		 * the root of the abstract syntax tree.
		 * @param aFilterNodes
		 * if set to true unimportant node types will not be included
		 * into the language model
		 * @param depth
		 * the maximum depth of constructing the tokens, where 0 equals
		 * total abstraction and -1 means unlimited depth
		 * @param aSeriDepth
		 * the serialization depth
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Factory(ITokenMapperShort<T,Integer> tokenMapper, StringWordIndexer aWordIndexer, 
				LmReaderCallback<LongRef> aCallback, boolean aOnlyMethodNodes,
				boolean aFilterNodes, int depth, int aSeriDepth) {
			super((Class)ASTTokenReader.class);
			t_mapper = tokenMapper;
			wordIndexer = aWordIndexer;
			callback = aCallback;
			onlyMethodNodes = aOnlyMethodNodes;
			filterNodes = aFilterNodes;
			this.depth = depth;
			seriDepth = aSeriDepth;
		}

		@Override
		public DisruptorFCFSEventHandler<Path> newFreshInstance() {
			return new ASTTokenReader<>(t_mapper, wordIndexer, callback, onlyMethodNodes, filterNodes, depth, seriDepth);
		}
	}
	
	private final StringWordIndexer wordIndexer;

	private int startId = 0;
	private int endId = 0;

	private final LmReaderCallback<LongRef> callback;
	// this defines the entry point for the AST
	private final boolean onlyMethodNodes;
	// this enables the black list for unimportant node types
	private final boolean filterNodes;

	// one logger especially for the errors
	private final Logger errLog = Logger.getLogger(ASTTokenReader.class);

	// this could be made configurable
	private final ITokenMapperShort<T,Integer> t_mapper;
	
	// private method names blacklist (needs to be reset for every new file)
	private Collection<String> privMethodsBL = null;

	// token abstraction depth
	final private int depth;
	final private int seriDepth;

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
	 * if set to true only method nodes will be used to train the
	 * language model. If set to false the compilation unit will be
	 * the root of the abstract syntax tree.
	 * @param aFilterNodes
	 * if set to true unimportant node types will not be included
	 * into the language model
	 * @param depth
	 * the maximum depth of constructing the tokens, where 0 equals
	 * total abstraction and -1 means unlimited depth
	 * @param aSeriDepth
	 * the serialization depth
	 */
	public ASTTokenReader(ITokenMapperShort<T,Integer> tokenMapper, StringWordIndexer aWordIndexer, 
			LmReaderCallback<LongRef> aCallback, boolean aOnlyMethodNodes,
			boolean aFilterNodes, int depth, int aSeriDepth) {
		super();
		t_mapper = tokenMapper;
		wordIndexer = aWordIndexer;
		callback = aCallback;
		onlyMethodNodes = aOnlyMethodNodes;
		filterNodes = aFilterNodes;
		this.depth = depth;
		seriDepth = aSeriDepth;

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
	 * @return 
	 * a list of token sequences
	 */
	public List<List<T>> getAllTokenSequences(String aFilePath) {
		return getAllTokenSequences(new File(aFilePath));
	}

	/**
	 * Parses the file and creates sequences for the language model.
	 * @param aFilePath
	 * the path to the file that should be parsed
	 * @return 
	 * a list of token sequences
	 */
	public List<List<T>> getAllTokenSequences(Path aFilePath) {
		return getAllTokenSequences(aFilePath.toFile());
	}

	/**
	 * Parses the file and creates sequences for the language model.
	 * @param aSourceFile
	 * the file that should be parsed
	 * @return 
	 * a list of token sequences
	 */
	public List<List<T>> getAllTokenSequences(File aSourceFile) {
		List<List<T>> result = new ArrayList<List<T>>();

		CompilationUnit cu;
		try (FileInputStream fis = new FileInputStream(aSourceFile)) {
			cu = parseInputStream(fis);
			// search the compilation unit for all method names that we want to ignore
			initBlacklist( cu );
			
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
		} else if (aNode instanceof MethodDeclaration 
				|| aNode instanceof ConstructorDeclaration) {
			// collect all tokens from this method and add them to the result
			// collection
			aResult.add(getTokenSequenceStartingFromNode(aNode));
		} else {
			// search for sub nodes of type method
			for (Node n : aNode.getChildrenNodes()) {
				getMethodTokenSequences(n, aResult);
			}
		}
	}


	/**
	 * Searches the node for all relevant tokens and adds them to the sequence
	 * which will be added to the language model.
	 * @param aNode
	 * an AST node
	 * @return 
	 * a list of mapped token that were found under this node
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
	 * the maximum depth of constructing the tokens, where 0 equals
	 * total abstraction and -1 means unlimited depth
	 */
	private void collectAllTokensRec(Node aNode, List<T> aTokenCol) {
		if (filterNodes) {
			// ignore some nodes we do not care about
			if (isNodeTypeIgnored(aNode)) {
				return;
			}

//			if (isNodeImportant(aChildNode)) {
			aTokenCol.addAll(t_mapper.getMappingForNode(aNode, depth, seriDepth).getMappings());
//			}
		} else {
			// add this token regardless of importance
			aTokenCol.addAll(t_mapper.getMappingForNode(aNode, depth, seriDepth).getMappings());
		}

		//proceed recursively in a distinct way
		proceedFromNode(aNode, aTokenCol);

		// some nodes have a closing tag
		T closingTag = t_mapper.getClosingToken(aNode, depth, seriDepth);
		if (closingTag != null) {
			aTokenCol.add(closingTag);
		}
	}
	
	/**
	 * How to proceed from the distinct nodes. From certain nodes, it makes no real
	 * sense to just take the child nodes.
	 * @param aNode
	 * this node will be inspected
	 * @param aTokenCol
	 * the current collection of all found tokens in this part of the AST
	 * @param depth
	 * the maximum depth of constructing the tokens, where 0 equals
	 * total abstraction and -1 means unlimited depth
	 */
	private void proceedFromNode(Node aNode, List<T> aTokenCol) {
		if (aNode instanceof MethodDeclaration) {
			List<ReferenceType> exceptionList = ((MethodDeclaration) aNode).getThrows();
			if (exceptionList != null && exceptionList.size() > 0) {
				aTokenCol.addAll(t_mapper.getMappingForNode(
						new ThrowsStmt(exceptionList.get(0).getBeginLine(), exceptionList.get(0).getBeginColumn(), 
								exceptionList.get(0).getBeginLine(), exceptionList.get(0).getBeginColumn()), depth, seriDepth)
						.getMappings());
				// iterate over all children in the exception list
				for (Node n : exceptionList) {
					collectAllTokensRec(n, aTokenCol);
				}
			}
			BlockStmt body = ((MethodDeclaration) aNode).getBody();
			if (body != null) {
				aTokenCol.addAll(t_mapper.getMappingForNode(						
						new BodyStmt(body.getBeginLine(), body.getBeginColumn(), 
								body.getBeginLine(), body.getBeginColumn()), depth, seriDepth)
						.getMappings());
				// iterate over all children in the method body
				collectAllTokensRec(body, aTokenCol);
			}
		} else if (aNode instanceof ConstructorDeclaration) {
			List<NameExpr> exceptionList = ((ConstructorDeclaration) aNode).getThrows();
			if (exceptionList != null && exceptionList.size() > 0) {
				aTokenCol.addAll(t_mapper.getMappingForNode(
						new ThrowsStmt(exceptionList.get(0).getBeginLine(), exceptionList.get(0).getBeginColumn(), 
								exceptionList.get(0).getBeginLine(), exceptionList.get(0).getBeginColumn()), depth, seriDepth)
						.getMappings());
				// iterate over all children in the exception list
				for (Node n : exceptionList) {
					collectAllTokensRec(n, aTokenCol);
				}
			}
			BlockStmt body = ((ConstructorDeclaration) aNode).getBlock();
			if (body != null) {
				aTokenCol.addAll(t_mapper.getMappingForNode(						
						new BodyStmt(body.getBeginLine(), body.getBeginColumn(), 
								body.getBeginLine(), body.getBeginColumn()), depth, seriDepth)
						.getMappings());
				// iterate over all children in the method body
				collectAllTokensRec(body, aTokenCol);
			}
		} else if (aNode instanceof IfStmt) {
			// iterate over all children in the 'then' block
			for (Node n : ((IfStmt) aNode).getThenStmt().getChildrenNodes()) {
				collectAllTokensRec(n, aTokenCol);
			}
			Statement elseStmt = ((IfStmt) aNode).getElseStmt();
			if (elseStmt != null) {
				aTokenCol.addAll(t_mapper.getMappingForNode(
						new ElseStmt(elseStmt.getBeginLine(), elseStmt.getBeginColumn(), 
								elseStmt.getBeginLine(), elseStmt.getBeginColumn()), depth, seriDepth)
						.getMappings());
				// iterate over all children in the 'else' block
				for (Node n : elseStmt.getChildrenNodes()) {
					collectAllTokensRec(n, aTokenCol);
				}
			}
		} else if (aNode instanceof ClassOrInterfaceDeclaration) {
			List<ClassOrInterfaceType> extendsList = ((ClassOrInterfaceDeclaration) aNode).getExtends();
			if (extendsList != null && extendsList.size() > 0) {
				aTokenCol.addAll(t_mapper.getMappingForNode(
						new ExtendsStmt(extendsList, 
								extendsList.get(0).getBeginLine(), extendsList.get(0).getBeginColumn(), 
								extendsList.get(0).getBeginLine(), extendsList.get(0).getBeginColumn()), depth, seriDepth)
						.getMappings());
				// iterate over all children in the extends list
				for (Node n : extendsList) {
					collectAllTokensRec(n, aTokenCol);
				}
			}
			List<ClassOrInterfaceType> implementsList = ((ClassOrInterfaceDeclaration) aNode).getImplements();
			if (implementsList != null && implementsList.size() > 0) {
				aTokenCol.addAll(t_mapper.getMappingForNode(
						new ImplementsStmt(implementsList, 
								implementsList.get(0).getBeginLine(), implementsList.get(0).getBeginColumn(), 
								implementsList.get(0).getBeginLine(), implementsList.get(0).getBeginColumn()), depth, seriDepth)
						.getMappings());
				// iterate over all children in the implements list
				for (Node n : implementsList) {
					collectAllTokensRec(n, aTokenCol);
				}
			}
			// call this method for all children
			for (Node n : ((ClassOrInterfaceDeclaration) aNode).getMembers()) {
				collectAllTokensRec(n, aTokenCol);
			}
		} else if (aNode instanceof EnumDeclaration) {
			List<ClassOrInterfaceType> implementsList = ((EnumDeclaration) aNode).getImplements();
			if (implementsList != null && implementsList.size() > 0) {
				aTokenCol.addAll(t_mapper.getMappingForNode(
						new ImplementsStmt(implementsList, 
								implementsList.get(0).getBeginLine(), implementsList.get(0).getBeginColumn(), 
								implementsList.get(0).getBeginLine(), implementsList.get(0).getBeginColumn()), depth, seriDepth)
						.getMappings());
			}
			// iterate over all children in the body
			for (Node n : ((EnumDeclaration) aNode).getEntries()) {
				collectAllTokensRec(n, aTokenCol);
			}
		} else if (aNode instanceof WhileStmt) {
			// iterate over all children in the body
			collectAllTokensRec(((WhileStmt) aNode).getBody(), aTokenCol);
		} else if (aNode instanceof DoStmt) {
			// iterate over all children in the body
			collectAllTokensRec(((DoStmt) aNode).getBody(), aTokenCol);
		} else if (aNode instanceof ForStmt) {
			// iterate over all children in the body
			collectAllTokensRec(((ForStmt) aNode).getBody(), aTokenCol);
		} else if (aNode instanceof ForeachStmt) {
			// iterate over all children in the body
			collectAllTokensRec(((ForeachStmt) aNode).getBody(), aTokenCol);
		} else if (aNode instanceof SwitchStmt) {
			// iterate over all children in the body
			for (Node n : ((SwitchStmt) aNode).getEntries()) {
				collectAllTokensRec(n, aTokenCol);
			}
		} else if (aNode instanceof PackageDeclaration) {
			// iterate over all annotations
			for (Node n : ((PackageDeclaration) aNode).getAnnotations()) {
				collectAllTokensRec(n, aTokenCol);
			}
		} else  //for certain nodes, don't proceed to the children nodes 
			if (!(aNode instanceof ImportDeclaration)) {
			// call this method for all children
			for (Node n : aNode.getChildrenNodes()) {
				collectAllTokensRec(n, aTokenCol);
			}
		}
	}

	/**
	 * Maps the sequences to the indices and sends it to the language model object.
	 * @param aTokenSequence
	 * the sequences that were extracted from the abstract syntax tree
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

	/**
	 * We ignore a couple of node types if we use the normal mode. This could be
	 * done with a black list in the options but the entries are always the same.
	 * @param aNode
	 * a node
	 * @return 
	 * true if the node should be ignored, false otherwise
	 */
	private boolean isNodeTypeIgnored(Node aNode) {

		if (aNode == null) {
			return true;
		}

		if (aNode instanceof Comment
		// || aNode instanceof MarkerAnnotationExpr || aNode instanceof NormalAnnotationExpr
		// || aNode instanceof SingleMemberAnnotationExpr || aNode instanceof MemberValuePair
		) {
			return true;
		}

		return false;
	}

//	/**
//	 * Some nodes just have no informational value in itself but its children
//	 * should be checked anyway.
//	 * 
//	 * @param aNode
//	 *            The node that should be checked
//	 * @return false if the node should not be put into the language model but
//	 *         its children should be checked regardless
//	 */
//	private boolean isNodeImportant(Node aNode) {
//
//		if (aNode == null) {
//			return false;
//		}
//
//		if (aNode instanceof BlockStmt || aNode instanceof ExpressionStmt || aNode instanceof EnclosedExpr) {
//			return false;
//		}
//
//		return true;
//	}
	
	/**
	 * Initializes the black list for private method names.
	 * @param aCu 
	 * the compilation unit
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
	 * @param aCu 
	 * the compilation unit of a source file
	 * @return 
	 * a list storing all private method names
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

	@Override
	public void resetAndInit() {
		// private method names blacklist (HashMap<String> would be a bit much for low entry counts)
		privMethodsBL = new ArrayList<String>();
	}

	@Override
	public boolean processInput(Path input) {
		parseNGramsFromFile(input);
		return true;
	}

}
