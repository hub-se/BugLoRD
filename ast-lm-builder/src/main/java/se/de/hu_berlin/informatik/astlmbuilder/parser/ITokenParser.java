package se.de.hu_berlin.informatik.astlmbuilder.parser;

import java.util.ArrayList;
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
import se.de.hu_berlin.informatik.astlmbuilder.mapping.UnknownNode;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.dispatcher.IKeyWordDispatcher;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IBasicKeyWords;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.BodyStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ElseStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ExtendsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ImplementsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ThrowsStmt;


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
	
	//remove outer brackets from a token
	public static String removeOuterBrackets(String token) {
		char start = token.charAt(0);
		if (start == IBasicKeyWords.BIG_GROUP_START) { //shape of big group brackets and parse remaining String
			if (token.charAt(token.length()-1) == IBasicKeyWords.BIG_GROUP_END) {
				return removeOuterBrackets(token.substring(1, token.length()-1));
			} else {
				throw new IllegalArgumentException("Illegal end char: '" + token + "'.");
			}
		} else if (start == IBasicKeyWords.GROUP_START) { //shape of group brackets and parse remaining String
			if (token.charAt(token.length()-1) == IBasicKeyWords.GROUP_END) {
				return removeOuterBrackets(token.substring(1, token.length()-1));
			} else {
				throw new IllegalArgumentException("Illegal end char: '" + token + "'.");
			}
		}
		return token;
	}
	
	//expected token format: [member_1],...,[member_n]
	public static List<String> getMembers(String token) {
		if (token.charAt(0) != IBasicKeyWords.GROUP_START || token.charAt(token.length()-1) != IBasicKeyWords.GROUP_END) {
			throw new IllegalArgumentException("Illegal start or end char: '" + token + "'.");
		}
		List<String> allMembers = new ArrayList<String>();
		int depth = 0;
		int startIdx = 0;
		
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
				break;
			default : break;
			}
		}
		return allMembers;
	}
	
	//expected token format: $id or ($id,[member_1],...,[member_n]) or ~ for null
	default public <T extends Node> T parseNodeFromToken(String token, InformationWrapper info) {
		token = removeOuterBrackets(token);
		char start = token.charAt(0);
		if (start == IBasicKeyWords.KEYWORD_MARKER) { //create node from entire String
			int firstSplitIndex = token.indexOf(IBasicKeyWords.SPLIT);
			if (firstSplitIndex > 0) { //found split char
				return getDispatcher().dispatchAndDesi(
						token.substring(0, firstSplitIndex), 
						getMembers(token.substring(firstSplitIndex+1, token.length())), 
						this);
			} else {
				return guessNodeFromKeyWord(token, info);
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
	
	//expected token format: # or (#,[member_1],...,[member_n]) or ~ for null
	default public NodeList<? extends Node> parseListFromToken(String token, InformationWrapper info) {
		token = removeOuterBrackets(token);
		char start = token.charAt(0);
		if (start == IBasicKeyWords.KEYWORD_LIST) { //create list from entire String
			int firstSplitIndex = token.indexOf(IBasicKeyWords.SPLIT);
			if (firstSplitIndex == 1) { //found split char
				List<String> listMembers = getMembers(token.substring(2));
				NodeList<? extends Node> result = new NodeList<>();
				for (String member : listMembers) {
					result.add(parseNodeFromToken(member, info));
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
	
	//expected token format: T or F
	default public boolean parseBooleanFromToken(String token) {
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
	
	//expected token format: "value" or value , but not value" or "value
	default public String parseStringValueFromToken(String token) {
		token = removeOuterBrackets(token);
		char start = token.charAt(0);
		if (start == '"') { //found starting "
			if (token.charAt(token.length()-1) == '"') {
				return token.substring(1, token.length()-1);
			} else {
				throw new IllegalArgumentException("Illegal end char: '" + token + "'.");
			}
		} else {
			if (token.charAt(token.length()-1) == '"') {
				throw new IllegalArgumentException("Illegal start char: '" + token + "'.");
			} else {
				return token;
			}
		}
	}
	
	//expected token format: 'c' or c
	default public char parseCharValueFromToken(String token) {
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
	
	//"guess" nodes based only on keywords and available information
	public <T extends Node> T guessNodeFromKeyWord(String keyWord, InformationWrapper info);
	public <T extends Node> NodeList<T> guessList(InformationWrapper info);

	//TODO: these following methods need to be implemented in the manner of the next method
	@SuppressWarnings({ "unchecked", "rawtypes" })
	default public ConstructorDeclaration createConstructorDeclaration(List<String> memberData, InformationWrapper info) {
		if (memberData.size() != 7) {
			throw new IllegalArgumentException("Member token count does not match node constructor arguments.");
		}
		try {
			//EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations, NodeList<TypeParameter> typeParameters, 
			//SimpleName name, NodeList<Parameter> parameters, NodeList<ReferenceType> thrownExceptions, BlockStmt body
			return new ConstructorDeclaration(
					getAllModsAsSet(memberData.get(0)), 
					(NodeList<AnnotationExpr>) parseListFromToken(memberData.get(1), info.getCopy()), 
					(NodeList<TypeParameter>) parseListFromToken(memberData.get(2), info.getCopy()), 
					(SimpleName) parseNodeFromToken(memberData.get(3), info.getCopy()), 
					(NodeList<Parameter>) parseListFromToken(memberData.get(4), info.getCopy()), 
					(NodeList<ReferenceType>) parseListFromToken(memberData.get(5), info.getCopy()), 
					(BlockStmt) parseNodeFromToken(memberData.get(6), info.getCopy()));
		} catch (ClassCastException e) {
			throw e;
		}
	}

	public InitializerDeclaration createInitializerDeclaration(List<String> memberData, InformationWrapper info);

	public EnumConstantDeclaration createEnumConstantDeclaration(List<String> memberData, InformationWrapper info);

	public VariableDeclarator createVariableDeclarator(List<String> memberData, InformationWrapper info);

	public EnumDeclaration createEnumDeclaration(List<String> memberData, InformationWrapper info);

	public AnnotationDeclaration createAnnotationDeclaration(List<String> memberData, InformationWrapper info);

	public AnnotationMemberDeclaration createAnnotationMemberDeclaration(List<String> memberData, InformationWrapper info);

	public WhileStmt createWhileStmt(List<String> memberData, InformationWrapper info);

	public TryStmt createTryStmt(List<String> memberData, InformationWrapper info);
	
	public ThrowStmt createThrowStmt(List<String> memberData, InformationWrapper info);

	// This may never be used
	public ThrowsStmt createThrowsStmt(List<String> memberData, InformationWrapper info);

	public SynchronizedStmt createSynchronizedStmt(List<String> memberData, InformationWrapper info);

	public SwitchStmt createSwitchStmt(List<String> memberData, InformationWrapper info);

	public SwitchEntryStmt createSwitchEntryStmt(List<String> memberData, InformationWrapper info);

	public ReturnStmt createReturnStmt(List<String> memberData, InformationWrapper info);

	public LabeledStmt createLabeledStmt(List<String> memberData, InformationWrapper info);

	public IfStmt createIfStmt(List<String> memberData, InformationWrapper info);

	// this may never be used
	public ElseStmt createElseStmt(List<String> memberData, InformationWrapper info);

	public ForStmt createForStmt(List<String> memberData, InformationWrapper info);
	public ForeachStmt createForeachStmt(List<String> memberData, InformationWrapper info);

	public ExpressionStmt createExpressionStmt(List<String> memberData, InformationWrapper info);

	public ExplicitConstructorInvocationStmt createExplicitConstructorInvocationStmt(List<String> memberData, InformationWrapper info);

	public DoStmt createDoStmt(List<String> memberData, InformationWrapper info);

	public ContinueStmt createContinueStmt(List<String> memberData, InformationWrapper info);

	public CatchClause createCatchClause(List<String> memberData, InformationWrapper info);

	public BlockStmt createBlockStmt(List<String> memberData, InformationWrapper info);

	public VariableDeclarationExpr createVariableDeclarationExpr(List<String> memberData, InformationWrapper info);

	public TypeExpr createTypeExpr(List<String> memberData, InformationWrapper info);

	public SuperExpr createSuperExpr(List<String> memberData, InformationWrapper info);

	public NullLiteralExpr createNullLiteralExpr(List<String> memberData, InformationWrapper info);

	public MethodReferenceExpr createMethodReferenceExpr(List<String> memberData, InformationWrapper info);

	// this may never be used
	public BodyStmt createBodyStmt(List<String> memberData, InformationWrapper info);

	public LambdaExpr createLambdaExpr(List<String> memberData, InformationWrapper info);

	public InstanceOfExpr createInstanceOfExpr(List<String> memberData, InformationWrapper info);

	public FieldAccessExpr createFieldAccessExpr(List<String> memberData, InformationWrapper info);

	public ConditionalExpr createConditionalExpr(List<String> memberData, InformationWrapper info);

	public ClassExpr createClassExpr(List<String> memberData, InformationWrapper info);

	public CastExpr createCastExpr(List<String> memberData, InformationWrapper info);
	
	public AssignExpr createAssignExpr(List<String> memberData, InformationWrapper info);

	public ArrayInitializerExpr createArrayInitializerExpr(List<String> memberData, InformationWrapper info);

	public ArrayCreationExpr createArrayCreationExpr(List<String> memberData, InformationWrapper info);

	public ArrayAccessExpr createArrayAccessExpr(List<String> memberData, InformationWrapper info);

	public PackageDeclaration createPackageDeclaration(List<String> memberData, InformationWrapper info);

	// this may never be used
	public ImportDeclaration createImportDeclaration(List<String> memberData, InformationWrapper info);

	public FieldDeclaration createFieldDeclaration(List<String> memberData, InformationWrapper info);

	public ClassOrInterfaceType createClassOrInterfaceType(List<String> memberData, InformationWrapper info);

	public ClassOrInterfaceDeclaration createClassOrInterfaceDeclaration(List<String> memberData, InformationWrapper info);
	
	public ClassOrInterfaceDeclaration createClassDeclaration(List<String> memberData, InformationWrapper info);
	
	public ClassOrInterfaceDeclaration createInterfaceDeclaration(List<String> memberData, InformationWrapper info);

	public MethodDeclaration createMethodDeclaration(List<String> memberData, InformationWrapper info);

	public BinaryExpr createBinaryExpr(List<String> memberData, InformationWrapper info);

	public UnaryExpr createUnaryExpr(List<String> memberData, InformationWrapper info);

	public MethodCallExpr createMethodCallExpr(List<String> memberData, InformationWrapper info);

	public MethodCallExpr createPrivMethodCallExpr(List<String> memberData, InformationWrapper info);

	public NameExpr createNameExpr(List<String> memberData, InformationWrapper info);

	public ConstructorDeclaration createIntegerLiteralExpr(List<String> memberData, InformationWrapper info);

	public DoubleLiteralExpr createDoubleLiteralExpr(List<String> memberData, InformationWrapper info);

	public StringLiteralExpr createStringLiteralExpr(List<String> memberData, InformationWrapper info);

	public BooleanLiteralExpr createBooleanLiteralExpr(List<String> memberData, InformationWrapper info);

	public CharLiteralExpr createCharLiteralExpr(List<String> memberData, InformationWrapper info);

	public LongLiteralExpr createLongLiteralExpr(List<String> memberData, InformationWrapper info);

	public ThisExpr createThisExpr(List<String> memberData, InformationWrapper info);

	public BreakStmt createBreakStmt(List<String> memberData, InformationWrapper info);

	public ObjectCreationExpr createObjectCreationExpr(List<String> memberData, InformationWrapper info);

	public MarkerAnnotationExpr createMarkerAnnotationExpr(List<String> memberData, InformationWrapper info);

	public NormalAnnotationExpr createNormalAnnotationExpr(List<String> memberData, InformationWrapper info);

	public SingleMemberAnnotationExpr createSingleMemberAnnotationExpr(List<String> memberData, InformationWrapper info);

	public Parameter createParameter(List<String> memberData, InformationWrapper info);

	public EnclosedExpr createEnclosedExpr(List<String> memberData, InformationWrapper info);

	public AssertStmt createAssertStmt(List<String> memberData, InformationWrapper info);

	public ConstructorDeclaration createMemberValuePair(List<String> memberData, InformationWrapper info);

	public PrimitiveType createPrimitiveType(List<String> memberData, InformationWrapper info);

	// this may never be used
	public UnionType createUnionType(List<String> memberData, InformationWrapper info);

	public IntersectionType createIntersectionType(List<String> memberData, InformationWrapper info);
	
	public TypeParameter createTypeParameter(List<String> memberData, InformationWrapper info);

	public WildcardType createWildcardType(List<String> memberData, InformationWrapper info);

	public VoidType createVoidType(List<String> memberData, InformationWrapper info);

	// this may never be used
	public ExtendsStmt createExtendsStmt(List<String> memberData, InformationWrapper info);

	// this may never be used
	public ImplementsStmt createImplementsStmt(List<String> memberData, InformationWrapper info);
	
	public UnknownType createUnknownType(List<String> memberData, InformationWrapper info);
	public UnknownNode createUnknown(List<String> memberData, InformationWrapper info);
	
	public Name createName(List<String> memberData, InformationWrapper info);
	public SimpleName createSimpleName(List<String> memberData, InformationWrapper info);
	public LocalClassDeclarationStmt createLocalClassDeclarationStmt(List<String> memberData, InformationWrapper info);
	public ArrayType createArrayType(List<String> memberData, InformationWrapper info);
	public ArrayCreationLevel createArrayCreationLevel(List<String> memberData, InformationWrapper info);
	
}
