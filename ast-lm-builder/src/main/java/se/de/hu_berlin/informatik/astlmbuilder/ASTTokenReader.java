package se.de.hu_berlin.informatik.astlmbuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.TokenMgrError;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

import edu.berkeley.nlp.lm.StringWordIndexer;
import edu.berkeley.nlp.lm.io.LmReaderCallback;
import edu.berkeley.nlp.lm.util.LongRef;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithPaths;
import se.de.hu_berlin.informatik.astlmbuilder.TokenMapperIf;

/**
 * This token reader parses each file in a given set and sends the read token
 * sequences to the language model.
 */
public class ASTTokenReader extends CallableWithPaths<Path, Boolean> {

	private StringWordIndexer wordIndexer;

	private int startId = 0;
	private int endId = 0;

	private LmReaderCallback<LongRef> callback;
	// this defines the entry point for the AST
	private boolean onlyMethodNodes = true;
	// this enables the black list for unimportant node types
	private boolean filterNodes = true;

	private Path rootDir;

	// one logger especially for the errors
	private Logger errLog = Logger.getLogger(ASTTokenReader.class);

	// this could be made configurable
	TokenMapperIf t_mapper = new AdvancedNode2LMMapping();

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
	 * 
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
	public ASTTokenReader(StringWordIndexer aWordIndexer, LmReaderCallback<LongRef> aCallback, boolean aOnlyMethodNodes,
			boolean aFilterNodes) {
		super();
		wordIndexer = aWordIndexer;
		callback = aCallback;
		onlyMethodNodes = aOnlyMethodNodes;
		filterNodes = aFilterNodes;

		startId = wordIndexer.getOrAddIndex(wordIndexer.getStartSymbol());
		endId = wordIndexer.getOrAddIndex(wordIndexer.getEndSymbol());
		// currently there are to many errors in the
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
	private void countNgrams(Path aSingleFile) {
		List<List<String>> allSequences = getAllTokenSequences(aSingleFile.toFile());
		// iterate over each sequence
		for (List<String> seq : allSequences) {
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
	public List<List<String>> getAllTokenSequences(String aFilePath) {
		return getAllTokenSequences(new File(aFilePath));
	}

	/**
	 * Parses the file and creates sequences for the language model
	 * 
	 * @param aFilePath
	 *            The path to the file that should be parsed
	 * @return A list of token sequences
	 */
	public List<List<String>> getAllTokenSequences(Path aFilePath) {
		return getAllTokenSequences(aFilePath.toFile());
	}

	/**
	 * Parses the file and creates sequences for the language model
	 * 
	 * @param aSourceFile
	 *            The file that should be parsed
	 * @return A list of token sequences
	 */
	public List<List<String>> getAllTokenSequences(File aSourceFile) {
		List<List<String>> result = new ArrayList<List<String>>();

		CompilationUnit cu;
		try (FileInputStream fis = new FileInputStream(aSourceFile)) {
			cu = JavaParser.parse(fis);

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
	 * Parses the file and creates sequences for the language model
	 * 
	 * @param aSourceFile
	 *            The file that should be parsed
	 * @return A list of token sequences
	 */
	public List<List<TokenWrapper>> getAllTokenSequencesWithLineNumbers(File aSourceFile) {
		List<List<TokenWrapper>> result = new ArrayList<List<TokenWrapper>>();

		FileInputStream fis = null;
		CompilationUnit cu;
		try {
			fis = new FileInputStream(aSourceFile);
			cu = JavaParser.parse(fis);

			if (onlyMethodNodes) {
				// this creates string arrays with token sequences starting at
				// method nodes
				getMethodTokenSequencesWithLineNumbers(cu, result);
			} else {
				// just add everything in a single string array
				result.add(getAllTokensWithLineNumbersFromNode(cu));
			}

		} catch (FileNotFoundException e) {
			++stats_fnf_e;
			errLog.error(e);
		} catch (ParseException e) {
			++stats_parse_e;
			errLog.error(e);
		} catch (RuntimeException re) {
			++stats_runtime_e;
			errLog.error(re);
		} catch (Exception e) {
			++stats_general_e;
			errLog.error(e);
		} catch (TokenMgrError tme) {
			++stats_token_err;
			errLog.error(tme);
		} catch (Error err) {
			++stats_general_err;
			errLog.error(err);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// nothing to do
				}
			}
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
	private void getMethodTokenSequences(Node aRootNode, List<List<String>> aResult) {
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
	 * Searches for all nodes under the root node for methods and adds all token
	 * sequences to the result collection.
	 * 
	 * @param rootNode
	 *            The root node
	 * @param result
	 *            all token sequences found so far
	 */
	private void getMethodTokenSequencesWithLineNumbers(Node aRootNode, List<List<TokenWrapper>> aResult) {
		if (aRootNode == null) {
			return;
		} else if (aRootNode instanceof MethodDeclaration || aRootNode instanceof ConstructorDeclaration) {
			// collect all tokens from this method and add them to the result
			// collection
			aResult.add(getAllTokensWithLineNumbersFromNode(aRootNode));
		} else {
			// search for sub nodes of type method
			List<Node> children = aRootNode.getChildrenNodes();
			for (Node n : children) {
				getMethodTokenSequencesWithLineNumbers(n, aResult);
			}
		}
	}

	/**
	 * Searches the node for all relevant tokens and adds them to the sequence
	 * which will be added to the language model
	 * 
	 * @param aNode
	 * @return a list of mapped token that were found under this node
	 */
	private List<String> getAllTokensFromNode(Node aNode) {
		// TODO in case entry tokens should not be merged change here
		List<String> result = new ArrayList<String>();
		if (aNode instanceof MethodDeclaration) {
			result.add(t_mapper.getMappingForNode(aNode));
			collectAllTokensRec(((MethodDeclaration) aNode).getBody(), result);
		} else if (aNode instanceof ConstructorDeclaration) {
			result.add(t_mapper.getMappingForNode(aNode));
			collectAllTokensRec(((ConstructorDeclaration) aNode).getBlock(), result);
		} else {
			collectAllTokensRec(aNode, result);
		}

		return result;
	}

	/**
	 * Searches the node for all relevant tokens and adds them to the sequence
	 * which will be added to the language model
	 * 
	 * @param aNode
	 * @return a list of mapped token that were found under this node
	 */
	private List<TokenWrapper> getAllTokensWithLineNumbersFromNode(Node aNode) {
		List<TokenWrapper> result = new ArrayList<>();

		collectAllTokensWithLineNumbersRec(aNode, result);

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
	private void collectAllTokensRec(Node aChildNode, List<String> aTokenCol) {
		if (filterNodes) {
			// ignore some nodes we do not care about
			if (isNodeTypeIgnored(aChildNode)) {
				return;
			}

			if (isNodeImportant(aChildNode)) {
				aTokenCol.add(t_mapper.getMappingForNode(aChildNode));
			}
		} else {
			// add this token regardless of importance
			aTokenCol.add(t_mapper.getMappingForNode(aChildNode));
		}

		// call this method for all children
		for (Node n : aChildNode.getChildrenNodes()) {
			collectAllTokensRec(n, aTokenCol);
		}

		// some nodes have a closing tag
		String closingTag = getClosingToken(aChildNode);
		if (closingTag != null) {
			aTokenCol.add(closingTag);
		}
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
	private void collectAllTokensWithLineNumbersRec(Node aChildNode, List<TokenWrapper> aTokenCol) {

		if (filterNodes) {
			// ignore some nodes we do not care about
			if (isNodeTypeIgnored(aChildNode)) {
				return;
			}

			if (isNodeImportant(aChildNode)) {
				aTokenCol.add(new TokenWrapper(t_mapper.getMappingForNode(aChildNode), aChildNode.getBeginLine(),
						aChildNode.getEndLine()));
			}
		} else {
			// add this token regardless of importance
			aTokenCol.add(new TokenWrapper(t_mapper.getMappingForNode(aChildNode), aChildNode.getBeginLine(),
					aChildNode.getEndLine()));
		}

		// call this method for all children
		for (Node n : aChildNode.getChildrenNodes()) {
			collectAllTokensWithLineNumbersRec(n, aTokenCol);
		}

		// add the closing tag of block expressions like method declarations or
		// loops here
		// some nodes have a closing tag
		String closingTag = getClosingToken(aChildNode);
		if (closingTag != null) {
			aTokenCol.add(new TokenWrapper(closingTag, aChildNode.getBeginLine(), aChildNode.getEndLine()));
		}
	}

	/**
	 * Maps the sequences to the indices and sends it to the language model
	 * object
	 * 
	 * @param aTokenSequence
	 *            The sequences that were extracted from the abstract syntax
	 *            tree
	 * @param callback
	 *            The language model where the sequences should be stored
	 */
	private void addSequenceToLM(List<String> aTokenSequence) {
		final int[] sent = new int[aTokenSequence.size() + 2];
		sent[0] = startId;
		sent[sent.length - 1] = endId;

		for (int i = 0; i < aTokenSequence.size(); ++i) {
			sent[i + 1] = wordIndexer.getOrAddIndexFromString(aTokenSequence.get(i));
		}

		callback.call(sent, 0, sent.length, new LongRef(1L), null);
	}

	@Override
	public Boolean call() {
		rootDir = getInput();

		// TODO remove after testing?
		if (++stats_files_processed % 1024 == 0) {
			// not using the usual logger because of fatal level
			Log.out(this, stats_files_processed + " files processed");
		}

		countNgrams(rootDir);

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
	 * Returns a closing token for some block nodes
	 * 
	 * @param aNode
	 * @return Closing token or null if the node has none
	 */
	private String getClosingToken(Node aNode) {
		if (aNode == null) {
			return null;
		}

		if (aNode instanceof MethodDeclaration) {
			return TokenMapperIf.CLOSING_MDEC;
		} else if (aNode instanceof ConstructorDeclaration) {
			return TokenMapperIf.CLOSING_CNSTR;
		} else if (aNode instanceof IfStmt) {
			return TokenMapperIf.CLOSING_IF;
		} else if (aNode instanceof WhileStmt) {
			return TokenMapperIf.CLOSING_WHILE;
		} else if (aNode instanceof ForStmt) {
			return TokenMapperIf.CLOSING_FOR;
		} else if (aNode instanceof TryStmt) {
			return TokenMapperIf.CLOSING_TRY;
		} else if (aNode instanceof CatchClause) {
			return TokenMapperIf.CLOSING_CATCH;
		} else if (aNode instanceof ForeachStmt) {
			return TokenMapperIf.CLOSING_FOR_EACH;
		} else if (aNode instanceof DoStmt) {
			return TokenMapperIf.CLOSING_DO;
		}

		return null;
	}

}
