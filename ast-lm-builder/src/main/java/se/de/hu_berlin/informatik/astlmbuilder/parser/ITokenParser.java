package se.de.hu_berlin.informatik.astlmbuilder.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.modules.ModuleDeclaration;
import com.github.javaparser.ast.modules.ModuleStmt;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.IModifierHandler;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.dispatcher.IKeyWordDispatcher;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IBasicKeyWords;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.nodes.BodyStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.nodes.ElseStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.nodes.ExtendsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.nodes.ImplementsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.nodes.ThrowsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.nodes.UnknownNode;


/**
 * Interface that provides functionality to create AST nodes from String representations.
 * <p> General format for elements with 
 * <br> maximum abstraction: {@code $node_id}, and
 * <br> other abstraction level: {@code ($node_id,[member_1],[member_2],...,[member_n])},
 * <br> where each {@code member_k} is again an element itself.
 */
public interface ITokenParser extends OperatorParser, TypeParser, IModifierHandler {
	
	public IKeyWordProvider<String> getKeyWordProvider();
	
	public IKeyWordDispatcher getDispatcher();
	
	/**
	 * Removes existing outer brackets from the given token. Checks if the first
	 * char is an opening bracket and looks for a respective closing bracket at the
	 * end of the token. If both exist, the String without the brackets is returned.
	 * Calls itself recursively to further remove possibly existing enclosing brackets.
	 * @param token
	 * the token to remove outer brackets from
	 * @return
	 * the token without outer brackets
	 * @throws IllegalArgumentException
	 * if the given token is of the wrong format, i.e., if there doesn't exist a matching
	 * closing bracket for an existing opening bracket
	 */
	public static String removeOuterBrackets(String token) throws IllegalArgumentException {
		char start = token.charAt(0);
		if (start == IBasicKeyWords.BIG_GROUP_START) { //shape of big group brackets and parse remaining String
			if (token.charAt(token.length()-1) == IBasicKeyWords.BIG_GROUP_END) {
				return removeOuterBrackets(token.substring(1, token.length()-1));
			} else {
				throw new IllegalArgumentException("Illegal end: '" + token + "'.");
			}
		} else if (start == IBasicKeyWords.GROUP_START) { //shape of group brackets and parse remaining String
			if (token.charAt(token.length()-1) == IBasicKeyWords.GROUP_END) {
				return removeOuterBrackets(token.substring(1, token.length()-1));
			} else {
				throw new IllegalArgumentException("Illegal end: '" + token + "'.");
			}
		}
		return token;
	}
	
	/**
	 * Parses the given token and extracts the member data.
	 * <p> Expected token format: {@code [member_1],...,[member_n]}.
	 * @param token
	 * the token to parse
	 * @return
	 * the list of members (may be empty, but will not be null)
	 * @throws IllegalArgumentException
	 * if the given token is of the wrong format
	 */
	public static List<String> getMembers(String token) throws IllegalArgumentException {
		if (token.charAt(0) != IBasicKeyWords.GROUP_START 
				|| token.charAt(token.length()-1) != IBasicKeyWords.GROUP_END) {
			throw new IllegalArgumentException("Illegal start or end: '" + token + "'.");
		}
		List<String> allMembers = new ArrayList<String>();
		int depth = 0;
		int startIdx = 0;
		
		//this will fail if any actual values inside of the tokens contain '[' or ']'
		for( int idx = 0; idx < token.length(); ++idx ) {
			switch( token.charAt( idx ) ) {
			case IBasicKeyWords.GROUP_START : 
				if( ++depth == 1 ) { // mark this only if it starts a group at depth 1
					startIdx = idx+1; 
				}; 
				break; 
			case IBasicKeyWords.GROUP_END : 
				if ( --depth == 0 ) { // this may add empty strings to the result set which is fine
					allMembers.add( token.substring( startIdx, idx ) );
					startIdx = idx +1; 
				};
				if (depth < 0) {
					throw new IllegalArgumentException("Illegal format: '" + token + "'.");
				}
				break;
			default : break;
			}
		}
		if (depth != 0) { //should always have an equal number of opening and closing brackets
			throw new IllegalArgumentException("Illegal format: '" + token + "'.");
		}
		return allMembers;
	}
	
	/**
	 * Checks if the expected keyword matches the token and returns existing member 
	 * data tokens, if any. Returns an empty list, if only the keyword exists, and 
	 * returns null if the parsed keyword is (only) the null keyword.
	 * <p> Expected token format: {@code $id} or {@code ($id,[member_1],...,[member_n])} 
	 * or {@code ~} for null.
	 * @param expectedKeyWord
	 * the expected keyword
	 * @param token
	 * the token to parse
	 * @return
	 * the list of member data tokens or null
	 * @throws IllegalArgumentException
	 * if the given token is of the wrong format
	 */
	default public List<String> parseExpectedNodeMembersFromToken(String expectedKeyWord, 
			String token) throws IllegalArgumentException {
		token = removeOuterBrackets(token);
		char start = token.charAt(0);
		if (start == IBasicKeyWords.KEYWORD_MARKER) { //create node from entire String
			int firstSplitIndex = token.indexOf(IBasicKeyWords.SPLIT);
			if (firstSplitIndex > 0) { //found split char
				if (token.substring(0, firstSplitIndex).equals(expectedKeyWord)) { //format: $id,[member_1],...,[member_n]
					return getMembers(token.substring(firstSplitIndex+1, token.length()));
				} else {
					throw new IllegalArgumentException("Unexpected keyword: '" + 
							token.substring(0, firstSplitIndex) + "', expected: '" + expectedKeyWord + "'.");
				}
			} else { //found no split char
				if (token.equals(expectedKeyWord)) { //format: $id
					return Collections.emptyList();
				} else {
					throw new IllegalArgumentException("Unexpected keyword: '" + 
							token + "', expected: '" + expectedKeyWord + "'.");
				}
			}
		} else if (start == IBasicKeyWords.KEYWORD_NULL) { //return null if this is the only char in the given String
			if (token.length() == 1) {
				return null;
			} else {
				throw new IllegalArgumentException("Illegal null token: '" + token + "'.");
			}
		} else { //this should not happen ever and should throw an exception
			throw new IllegalArgumentException("Illegal token: '" + token + "'.");
		}
	}
	
	
	
	/**
	 * Parses a list with nodes of unknown class (only the superclass may be known) and returns 
	 * a list with nodes with the type of the given superclass.
	 * <p> Expected token format: {@code #} or {@code (#,[member_1],...,[member_n])} 
	 * or {@code ~} for null.
	 * @param expectedSuperClazz
	 * the type of nodes in the list that should be returned
	 * @param token
	 * the token to parse
	 * @param info
	 * the currently available information
	 * @return
	 * the parsed node list
	 * @throws IllegalArgumentException
	 * if the given token is of the wrong format
	 * @throws ClassCastException
	 * if a node of the wrong type is returned for one of the list members
	 * @param <T>
	 * the type of nodes in the returned list
	 */
	default public <T extends Node> NodeList<T> parseListFromToken(Class<T> expectedSuperClazz, 
			String token, InformationWrapper info) throws IllegalArgumentException, ClassCastException {
		token = removeOuterBrackets(token);
		char start = token.charAt(0);
		if (start == IBasicKeyWords.KEYWORD_LIST) { //create list from entire String
			int firstSplitIndex = token.indexOf(IBasicKeyWords.SPLIT);
			if (firstSplitIndex == 1) { //found split char
				List<String> listMembers = getMembers(token.substring(2));
				NodeList<T> result = new NodeList<>();
				for (String member : listMembers) {
					//we only know the superclass of the list members here...
					result.add(createNodeFromToken(expectedSuperClazz, member, info));
				}
				//TODO: what about shortened lists?
				return result;
			} else if (token.length() == 1) { //only the list keyword
				return guessList(info);
			} else {
				throw new IllegalArgumentException("Illegal token: '" + token + "'.");
			}
		} else if (start == IBasicKeyWords.KEYWORD_NULL) { //return null if this is the only char in the given String
			if (token.length() == 1) {
				return null;
			} else {
				throw new IllegalArgumentException("Illegal null token: '" + token + "'.");
			}
		} else { //this should not happen ever and should throw an exception
			throw new IllegalArgumentException("Illegal token: '" + token + "'.");
		}
	}
	
	/**
	 * Parses a boolean value from the given token.
	 * <p> Expected token format: {@code T} or {@code F}.
	 * @param token
	 * the token to parse
	 * @return
	 * the boolean value
	 * @throws IllegalArgumentException
	 * if the given token is of the wrong format
	 */
	default public boolean parseBooleanFromToken(String token) throws IllegalArgumentException {
		token = removeOuterBrackets(token);
		char start = token.charAt(0);
		if (start == IBasicKeyWords.KEYWORD_TRUE) { //found boolean true
			int firstSplitIndex = token.indexOf(IBasicKeyWords.SPLIT);
			if (firstSplitIndex > 0) { //found split char
				throw new IllegalArgumentException("Illegal token: '" + token + "'.");
			} else {
				return true;
			}
		} else if (start == IBasicKeyWords.KEYWORD_FALSE) { //found boolean false
			int firstSplitIndex = token.indexOf(IBasicKeyWords.SPLIT);
			if (firstSplitIndex > 0) { //found split char
				throw new IllegalArgumentException("Illegal token: '" + token + "'.");
			} else {
				return false;
			}
		} else { //this should not happen ever and should throw an exception
			throw new IllegalArgumentException("Illegal token: '" + token + "'.");
		}
	}
	
	/**
	 * Parses a String value from the given token.
	 * <p> Expected token format: {@code ~} (null), {@code "s"} or {@code s} , but not {@code "s}.
	 * @param token
	 * the token to parse
	 * @return
	 * the String value
	 * @throws IllegalArgumentException
	 * if the given token is of the wrong format
	 */
	default public String parseStringValueFromToken(String token) throws IllegalArgumentException {
		token = removeOuterBrackets(token);
		char start = token.charAt(0);
		if (start == IBasicKeyWords.KEYWORD_NULL) { //return null if this is the only char in the given String
			if (token.length() == 1) {
				return null;
			} else {
				throw new IllegalArgumentException("Illegal null token: '" + token + "'.");
			}
		} else if (start == '"') { //found starting "
			if (token.charAt(token.length()-1) == '"') {
				return token.substring(1, token.length()-1);
			} else {
				throw new IllegalArgumentException("Illegal end: '" + token + "'.");
			}
		} else {
			return token;
		}
	}
	
	/**
	 * Parses a char value from the given token.
	 * <p> Expected token format: {@code 'c'} or {@code c}.
	 * @param token
	 * the token to parse
	 * @return
	 * the char value
	 * @throws IllegalArgumentException
	 * if the given token is of the wrong format
	 */
	default public char parseCharValueFromToken(String token) throws IllegalArgumentException {
		token = removeOuterBrackets(token);
		char start = token.charAt(0);
		if (start == '\'') { //found starting '
			if (token.length() == 3 && token.charAt(2) == '\'') {
				return token.charAt(1);
			} else {
				throw new IllegalArgumentException("Illegal token: '" + token + "'.");
			}
		} else if (token.length() == 1) {
			return start;
		} else { //this should not happen ever and should throw an exception
			throw new IllegalArgumentException("Illegal token: '" + token + "'.");
		}
	}
	
	
	
	
	//TODO: implement these...
	//"guess" nodes based only on keywords and available information
	public List<String> guessNodeMembersFromKeyWord(String keyWord, InformationWrapper info);
	public <T extends Node> NodeList<T> guessList(InformationWrapper info);
	
	
	
	
	
	/**
	 * Parses a node of unknown class (only the superclass may be known) and returns 
	 * a node with the type of the given superclass.
	 * <p> Expected token format: {@code $id} or {@code ($id,[member_1],...,[member_n])} 
	 * or {@code ~} for null.
	 * @param expectedSuperClazz
	 * the type of nodes that should be returned
	 * @param token
	 * the token to parse
	 * @param info
	 * the currently available information
	 * @return
	 * the parsed node
	 * @throws IllegalArgumentException
	 * if the given token is of the wrong format
	 * @throws ClassCastException
	 * if a node of the wrong type is returned
	 * @param <T>
	 * the type of returned nodes
	 */
	default public <T extends Node> T createNodeFromToken(Class<T> expectedSuperClazz, 
			String token, InformationWrapper info) throws IllegalArgumentException, ClassCastException {
		token = removeOuterBrackets(token);
		char start = token.charAt(0);
		if (start == IBasicKeyWords.KEYWORD_MARKER) { //create node from entire String
			int firstSplitIndex = token.indexOf(IBasicKeyWords.SPLIT);
			try {
				if (firstSplitIndex > 0) { //found split char
					return expectedSuperClazz.cast(
							getDispatcher().dispatch(
									token.substring(0, firstSplitIndex), token,
									info.getCopy(), this));
				} else { //no split char, probably just the keyword
					return expectedSuperClazz.cast(
							getDispatcher().dispatch(
									token, token, 
									info.getCopy(), this));
				}
			} catch (ClassCastException e) {
				throw e;
			}
		} else if (start == IBasicKeyWords.KEYWORD_NULL) { //return null if this is the only char in the given String
			if (token.length() == 1) {
				return null;
			} else {
				throw new IllegalArgumentException("Illegal null token: '" + token + "'.");
			}
		} else { //this should not happen ever and should throw an exception
			throw new IllegalArgumentException("Illegal token: '" + token + "'.");
		}
	}

	
	
	
	/**
	 * Splits the given token into its members, if any, or "guesses" members based on 
	 * available information. May return null, if the given token is the null keyword. 
	 * Checks if the returned member data list has the expected length.
	 * <p> Expected token format: {@code $id} or {@code ($id,[member_1],...,[member_n])} 
	 * or {@code ~} for null.
	 * @param token
	 * the token to parse
	 * @param expectedKeyWord
	 * the expected keyword
	 * @param expectedMemberCount
	 * the expected member data count
	 * @param info
	 * the currently available information
	 * @return
	 * a list of member data tokens or null
	 * @throws IllegalArgumentException
	 * if the given token is of the wrong format
	 */
	public default List<String> parseAndCheckMembers(String token, String expectedKeyWord, 
			int expectedMemberCount, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseExpectedNodeMembersFromToken(expectedKeyWord, token);
		if (memberData == null) { //token: ~
			return null;
		}if (memberData.isEmpty()) { //token: $id
			memberData = guessNodeMembersFromKeyWord(expectedKeyWord, info);
		}
		
		if (memberData.size() != expectedMemberCount) { //token: ($id,[member_1],...,[member_n])
			throw new IllegalArgumentException("Member token count does not match node constructor arguments.");
		}
		return memberData;
	}
	
	//TODO: these following methods need to be implemented in the manner of the next method (maybe there exists a more elegant way?)
	//expected token format: $id or ($id,[member_1],...,[member_n]) or ~ for null
	
	default public ConstructorDeclaration createConstructorDeclaration(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getConstructorDeclaration(), 7, info); 

		//EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations, NodeList<TypeParameter> typeParameters, 
		//SimpleName name, NodeList<Parameter> parameters, NodeList<ReferenceType> thrownExceptions, BlockStmt body
		return new ConstructorDeclaration(
				parseEnumSetFromToken(memberData.get(0)), 
				parseListFromToken(AnnotationExpr.class, memberData.get(1), info.getCopy()), 
				parseListFromToken(TypeParameter.class, memberData.get(2), info.getCopy()), 
				createSimpleName(memberData.get(3), info.getCopy()), 
				parseListFromToken(Parameter.class, memberData.get(4), info.getCopy()), 
				parseListFromToken(ReferenceType.class, memberData.get(5), info.getCopy()), 
				createBlockStmt(memberData.get(6), info.getCopy()));
	}

	public InitializerDeclaration createInitializerDeclaration(String token, InformationWrapper info) throws IllegalArgumentException;

	public EnumConstantDeclaration createEnumConstantDeclaration(String token, InformationWrapper info) throws IllegalArgumentException;

	public VariableDeclarator createVariableDeclarator(String token, InformationWrapper info) throws IllegalArgumentException;

	public EnumDeclaration createEnumDeclaration(String token, InformationWrapper info) throws IllegalArgumentException;

	public AnnotationDeclaration createAnnotationDeclaration(String token, InformationWrapper info) throws IllegalArgumentException;

	public AnnotationMemberDeclaration createAnnotationMemberDeclaration(String token, InformationWrapper info) throws IllegalArgumentException;

	public WhileStmt createWhileStmt(String token, InformationWrapper info) throws IllegalArgumentException;

	public TryStmt createTryStmt(String token, InformationWrapper info) throws IllegalArgumentException;
	
	public ThrowStmt createThrowStmt(String token, InformationWrapper info) throws IllegalArgumentException;

	// This may never be used
	public ThrowsStmt createThrowsStmt(String token, InformationWrapper info) throws IllegalArgumentException;

	public SynchronizedStmt createSynchronizedStmt(String token, InformationWrapper info) throws IllegalArgumentException;

	public SwitchStmt createSwitchStmt(String token, InformationWrapper info) throws IllegalArgumentException;

	public SwitchEntryStmt createSwitchEntryStmt(String token, InformationWrapper info) throws IllegalArgumentException;

	public ReturnStmt createReturnStmt(String token, InformationWrapper info) throws IllegalArgumentException;

	public LabeledStmt createLabeledStmt(String token, InformationWrapper info) throws IllegalArgumentException;

	public IfStmt createIfStmt(String token, InformationWrapper info) throws IllegalArgumentException;

	// this may never be used
	public ElseStmt createElseStmt(String token, InformationWrapper info) throws IllegalArgumentException;

	public ForStmt createForStmt(String token, InformationWrapper info) throws IllegalArgumentException;
	public ForeachStmt createForeachStmt(String token, InformationWrapper info) throws IllegalArgumentException;

	public ExpressionStmt createExpressionStmt(String token, InformationWrapper info) throws IllegalArgumentException;

	public ExplicitConstructorInvocationStmt createExplicitConstructorInvocationStmt(String token, InformationWrapper info) throws IllegalArgumentException;

	public DoStmt createDoStmt(String token, InformationWrapper info) throws IllegalArgumentException;

	public ContinueStmt createContinueStmt(String token, InformationWrapper info) throws IllegalArgumentException;

	public CatchClause createCatchClause(String token, InformationWrapper info) throws IllegalArgumentException;

	public BlockStmt createBlockStmt(String token, InformationWrapper info) throws IllegalArgumentException;

	public VariableDeclarationExpr createVariableDeclarationExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public TypeExpr createTypeExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public SuperExpr createSuperExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public NullLiteralExpr createNullLiteralExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public MethodReferenceExpr createMethodReferenceExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	// this may never be used
	public BodyStmt createBodyStmt(String token, InformationWrapper info) throws IllegalArgumentException;

	public LambdaExpr createLambdaExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public InstanceOfExpr createInstanceOfExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public FieldAccessExpr createFieldAccessExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public ConditionalExpr createConditionalExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public ClassExpr createClassExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public CastExpr createCastExpr(String token, InformationWrapper info) throws IllegalArgumentException;
	
	public AssignExpr createAssignExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public ArrayInitializerExpr createArrayInitializerExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public ArrayCreationExpr createArrayCreationExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public ArrayAccessExpr createArrayAccessExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public PackageDeclaration createPackageDeclaration(String token, InformationWrapper info) throws IllegalArgumentException;

	// this may never be used
	public ImportDeclaration createImportDeclaration(String token, InformationWrapper info) throws IllegalArgumentException;

	public FieldDeclaration createFieldDeclaration(String token, InformationWrapper info) throws IllegalArgumentException;

	public ClassOrInterfaceType createClassOrInterfaceType(String token, InformationWrapper info) throws IllegalArgumentException;

	public ClassOrInterfaceDeclaration createClassOrInterfaceDeclaration(String token, InformationWrapper info) throws IllegalArgumentException;
	
	public ClassOrInterfaceDeclaration createClassDeclaration(String token, InformationWrapper info) throws IllegalArgumentException;
	
	public ClassOrInterfaceDeclaration createInterfaceDeclaration(String token, InformationWrapper info) throws IllegalArgumentException;

	public MethodDeclaration createMethodDeclaration(String token, InformationWrapper info) throws IllegalArgumentException;

	public BinaryExpr createBinaryExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public UnaryExpr createUnaryExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public MethodCallExpr createMethodCallExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public MethodCallExpr createPrivMethodCallExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public NameExpr createNameExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public ConstructorDeclaration createIntegerLiteralExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public DoubleLiteralExpr createDoubleLiteralExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public StringLiteralExpr createStringLiteralExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public BooleanLiteralExpr createBooleanLiteralExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public CharLiteralExpr createCharLiteralExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public LongLiteralExpr createLongLiteralExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public ThisExpr createThisExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public BreakStmt createBreakStmt(String token, InformationWrapper info) throws IllegalArgumentException;

	public ObjectCreationExpr createObjectCreationExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public MarkerAnnotationExpr createMarkerAnnotationExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public NormalAnnotationExpr createNormalAnnotationExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public SingleMemberAnnotationExpr createSingleMemberAnnotationExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public Parameter createParameter(String token, InformationWrapper info) throws IllegalArgumentException;

	public EnclosedExpr createEnclosedExpr(String token, InformationWrapper info) throws IllegalArgumentException;

	public AssertStmt createAssertStmt(String token, InformationWrapper info) throws IllegalArgumentException;

	public ConstructorDeclaration createMemberValuePair(String token, InformationWrapper info) throws IllegalArgumentException;

	public PrimitiveType createPrimitiveType(String token, InformationWrapper info) throws IllegalArgumentException;

	// this may never be used
	public UnionType createUnionType(String token, InformationWrapper info) throws IllegalArgumentException;

	public IntersectionType createIntersectionType(String token, InformationWrapper info) throws IllegalArgumentException;
	
	public TypeParameter createTypeParameter(String token, InformationWrapper info) throws IllegalArgumentException;

	public WildcardType createWildcardType(String token, InformationWrapper info) throws IllegalArgumentException;

	public VoidType createVoidType(String token, InformationWrapper info) throws IllegalArgumentException;

	// this may never be used
	public ExtendsStmt createExtendsStmt(String token, InformationWrapper info) throws IllegalArgumentException;

	// this may never be used
	public ImplementsStmt createImplementsStmt(String token, InformationWrapper info) throws IllegalArgumentException;
	
	public UnknownType createUnknownType(String token, InformationWrapper info) throws IllegalArgumentException;
	public UnknownNode createUnknown(String token, InformationWrapper info) throws IllegalArgumentException;
	
	public Name createName(String token, InformationWrapper info) throws IllegalArgumentException;
	public SimpleName createSimpleName(String token, InformationWrapper info) throws IllegalArgumentException;
	public LocalClassDeclarationStmt createLocalClassDeclarationStmt(String token, InformationWrapper info) throws IllegalArgumentException;
	public ArrayType createArrayType(String token, InformationWrapper info) throws IllegalArgumentException;
	public ArrayCreationLevel createArrayCreationLevel(String token, InformationWrapper info) throws IllegalArgumentException;
	public ModuleDeclaration createModuleDeclaration(String token, InformationWrapper info) throws IllegalArgumentException;
	public ModuleStmt createModuleStmt(String token, InformationWrapper info) throws IllegalArgumentException;
	
}
