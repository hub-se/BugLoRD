package se.de.hu_berlin.informatik.astlmbuilder.mapping.shortKW;

import java.util.Collection;

import com.github.javaparser.ast.Node;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.IKeyWordDispatcher;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.ITokenMapper;
import se.de.hu_berlin.informatik.astlmbuilder.reader.IASTLMDesirializer;

public class KeyWordDispatcherShort extends KeyWordConstantsShort implements IKeyWordDispatcher, ITokenMapper<String, Integer> {

	/**
	 * Creates a new node object for a given serialized string
	 * 
	 * @param aChildData
	 *            The keyword that the mapper used for the original node
	 * @return a node of the same type as the original one that got serialized
	 */
	public Node dispatchAndDesi( String aKeyWord, String aChildData, IASTLMDesirializer aDesi ) {
		
		if( aKeyWord == null ) {
			return null;
		}

		switch (aKeyWord) {
		case ( CONSTRUCTOR_DECLARATION):
			return aDesi.createConstructorDeclaration(aChildData);
			
		case ( INITIALIZER_DECLARATION):
			return aDesi.createInitializerDeclaration(aChildData);
			
		case ( ENUM_CONSTANT_DECLARATION):
			return aDesi.createEnumConstantDeclaration(aChildData);
			
		case ( VARIABLE_DECLARATION):
			return aDesi.createVariableDeclarator(aChildData);
			
		case ( ENUM_DECLARATION):
			return aDesi.createEnumDeclaration(aChildData);
			
		case ( ANNOTATION_DECLARATION):
			return aDesi.createAnnotationDeclaration(aChildData);
			
		case ( ANNOTATION_MEMBER_DECLARATION):
			return aDesi.createAnnotationMemberDeclaration(aChildData);
			
		case ( EMPTY_MEMBER_DECLARATION):
			return aDesi.createEmptyMemberDeclaration(aChildData);
			
		case ( EMPTY_TYPE_DECLARATION):
			return aDesi.createEmptyTypeDeclaration(aChildData);
			
		case ( WHILE_STATEMENT):
			return aDesi.createWhileStmt(aChildData);
			
		case ( TRY_STATEMENT):
			return aDesi.createTryStmt(aChildData);
			
		case ( THROW_STATEMENT):
			return aDesi.createThrowStmt(aChildData);
			
		case ( THROWS_STATEMENT):
			return aDesi.createThrowsStmt(aChildData);
			
		case ( SYNCHRONIZED_STATEMENT):
			return aDesi.createSynchronizedStmt(aChildData);
			
		case ( SWITCH_STATEMENT):
			return aDesi.createSwitchStmt(aChildData);
			
		case ( SWITCH_ENTRY_STATEMENT):
			return aDesi.createSwitchEntryStmt(aChildData);
			
		case ( RETURN_STATEMENT):
			return aDesi.createReturnStmt(aChildData);
			
		case ( LABELED_STATEMENT):
			return aDesi.createLabeledStmt(aChildData);
			
		case ( IF_STATEMENT):
			return aDesi.createIfStmt(aChildData);
			
		case ( ELSE_STATEMENT):
			return aDesi.createElseStmt(aChildData);
			
		case ( FOR_STATEMENT):
			return aDesi.createForStmt(aChildData);
			
		case ( FOR_EACH_STATEMENT):
			return aDesi.createForeachStmt(aChildData);
			
		case ( EXPRESSION_STATEMENT):
			return aDesi.createExpressionStmt(aChildData);
			
		case ( EXPLICIT_CONSTRUCTOR_STATEMENT):
			return aDesi.createExplicitConstructorInvocationStmt(aChildData);
			
		case ( EMPTY_STATEMENT):
			return aDesi.createEmptyStmt(aChildData);
			
		case ( DO_STATEMENT):
			return aDesi.createDoStmt(aChildData);
			
		case ( CONTINUE_STATEMENT):
			return aDesi.createContinueStmt(aChildData);
			
		case ( CATCH_CLAUSE_STATEMENT):
			return aDesi.createCatchClause(aChildData);
			
		case ( BLOCK_STATEMENT):
			return aDesi.createBlockStmt(aChildData);
			
		case ( VARIABLE_DECLARATION_ID):
			return aDesi.createVariableDeclaratorId(aChildData);
			
		case ( VARIABLE_DECLARATION_EXPRESSION):
			return aDesi.createVariableDeclarationExpr(aChildData);
			
		case ( TYPE_EXPRESSION):
			return aDesi.createTypeExpr(aChildData);
			
		case ( SUPER_EXPRESSION):
			return aDesi.createSuperExpr(aChildData);
			
		case ( QUALIFIED_NAME_EXPRESSION):
			return aDesi.createQualifiedNameExpr(aChildData);
			
		case ( NULL_LITERAL_EXPRESSION):
			return aDesi.createNullLiteralExpr(aChildData);
			
		case ( METHOD_REFERENCE_EXPRESSION):
			return aDesi.createMethodReferenceExpr(aChildData);
			
		case ( BODY_STMT):
			return aDesi.createBodyStmt(aChildData);
			
		case ( LONG_LITERAL_MIN_VALUE_EXPRESSION):
			return aDesi.createLongLiteralMinValueExpr(aChildData);
			
		case ( LAMBDA_EXPRESSION):
			return aDesi.createLambdaExpr(aChildData);
			
		case ( INTEGER_LITERAL_MIN_VALUE_EXPRESSION):
			return aDesi.createIntegerLiteralMinValueExpr(aChildData);
			
		case ( INSTANCEOF_EXPRESSION):
			return aDesi.createInstanceOfExpr(aChildData);
			
		case ( FIELD_ACCESS_EXPRESSION):
			return aDesi.createFieldAccessExpr(aChildData);
			
		case ( CONDITIONAL_EXPRESSION):
			return aDesi.createConditionalExpr(aChildData);
			
		case ( CLASS_EXPRESSION):
			return aDesi.createClassExpr(aChildData);
			
		case ( CAST_EXPRESSION):
			return aDesi.createCastExpr(aChildData);
			
		case ( ASSIGN_EXPRESSION):
			return aDesi.createAssignExpr(aChildData);
			
		case ( ARRAY_INIT_EXPRESSION):
			return aDesi.createArrayInitializerExpr(aChildData);
			
		case ( ARRAY_CREATE_EXPRESSION):
			return aDesi.createArrayCreationExpr(aChildData);
			
		case ( ARRAY_ACCESS_EXPRESSION):
			return aDesi.createArrayAccessExpr(aChildData);
			
		case ( PACKAGE_DECLARATION):
			return aDesi.createPackageDeclaration(aChildData);
			
		case ( IMPORT_DECLARATION):
			return aDesi.createImportDeclaration(aChildData);
			
		case ( FIELD_DECLARATION):
			return aDesi.createFieldDeclaration(aChildData);
			
		case ( CLASS_OR_INTERFACE_TYPE):
			return aDesi.createClassOrInterfaceType(aChildData);
			
		case ( CLASS_OR_INTERFACE_DECLARATION):
			return aDesi.createClassOrInterfaceDeclaration(aChildData);
			
		case ( CLASS_DECLARATION):
			return aDesi.createClassOrInterfaceDeclaration(aChildData);
			
		case ( INTERFACE_DECLARATION):
			return aDesi.createClassOrInterfaceDeclaration(aChildData);
			
		case ( EXTENDS_STATEMENT):
			return aDesi.createExtendsStmt(aChildData);
			
		case ( IMPLEMENTS_STATEMENT):
			return aDesi.createImplementsStmt(aChildData);
			
		case ( METHOD_DECLARATION):
			return aDesi.createMethodDeclaration(aChildData);
			
		case ( BINARY_EXPRESSION):
			return aDesi.createBinaryExpr(aChildData);
			
		case ( UNARY_EXPRESSION):
			return aDesi.createUnaryExpr(aChildData);
			
		case ( METHOD_CALL_EXPRESSION):
			return aDesi.createMethodCallExpr(aChildData);
			
		// if a private method is called we handle it differently
		case ( PRIVATE_METHOD_CALL_EXPRESSION):
			return aDesi.createPrivMethodCallExpr(aChildData);
			
		case ( NAME_EXPRESSION):
			return aDesi.createNameExpr(aChildData);
			
		case ( INTEGER_LITERAL_EXPRESSION):
			return aDesi.createIntegerLiteralExpr(aChildData);
			
		case ( DOUBLE_LITERAL_EXPRESSION):
			return aDesi.createDoubleLiteralExpr(aChildData);
			
		case ( STRING_LITERAL_EXPRESSION):
			return aDesi.createStringLiteralExpr(aChildData);
			
		case ( BOOLEAN_LITERAL_EXPRESSION):
			return aDesi.createBooleanLiteralExpr(aChildData);
			
		case ( CHAR_LITERAL_EXPRESSION):
			return aDesi.createCharLiteralExpr(aChildData);
			
		case ( LONG_LITERAL_EXPRESSION):
			return aDesi.createLongLiteralExpr(aChildData);
			
		case ( THIS_EXPRESSION):
			return aDesi.createThisExpr(aChildData);
			
		case ( BREAK):
			return aDesi.createBreakStmt(aChildData);
			
		case ( OBJ_CREATE_EXPRESSION):
			return aDesi.createObjectCreationExpr(aChildData);
			
		case ( MARKER_ANNOTATION_EXPRESSION):
			return aDesi.createMarkerAnnotationExpr(aChildData);
			
		case ( NORMAL_ANNOTATION_EXPRESSION):
			return aDesi.createNormalAnnotationExpr(aChildData);
			
		case ( SINGLE_MEMBER_ANNOTATION_EXPRESSION):
			return aDesi.createSingleMemberAnnotationExpr(aChildData);
			

		case ( PARAMETER):
			return aDesi.createParameter(aChildData);
			
		case ( MULTI_TYPE_PARAMETER):
			return aDesi.createMultiTypeParameter(aChildData);
			
		case ( ENCLOSED_EXPRESSION):
			return aDesi.createEnclosedExpr(aChildData);
			
		case ( ASSERT_STMT):
			return aDesi.createAssertStmt(aChildData);
			
		case ( MEMBER_VALUE_PAIR):
			return aDesi.createMemberValuePair(aChildData);
			

		case ( TYPE_DECLARATION_STATEMENT):
			return aDesi.createTypeDeclarationStmt(aChildData);
			
		case ( TYPE_REFERENCE):
			return aDesi.createReferenceType(aChildData);
			
		case ( TYPE_PRIMITIVE):
			return aDesi.createPrimitiveType(aChildData);
			
		case ( TYPE_UNION):
			return aDesi.createUnionType(aChildData);
			
		case ( TYPE_INTERSECTION):
			return aDesi.createIntersectionType(aChildData);
			
		case ( TYPE_PAR):
			return aDesi.createTypeParameter(aChildData);
			
		case ( TYPE_WILDCARD):
			return aDesi.createWildcardType(aChildData);
			
		case ( TYPE_VOID):
			return aDesi.createVoidType(aChildData);
			

		default:
			return null; // the unknown keyword will result in an unknown
							// token
		}
	
	}
	
	@Override
	public void setPrivMethodBlackList(Collection aBL) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearPrivMethodBlackList() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getClosingToken(Node aNode, Integer... values) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public char getKeyWordBigGroupStart() {
		return BIG_GROUP_START.charAt( 0 );
	}

	@Override
	public char getKeyWordBigGroupEnd() {
		return BIG_GROUP_END.charAt( 0 );
	}
	
	public char getKeyWordSerialize() {
		return KEYWORD_SERIALIZE.charAt( 0 );
	}
	
	public char getKeyWordAbstraction() {
		return KEYWORD_MARKER.charAt( 0 );
	}

	@Override
	public char getKeyWordSeparator() {
		return ID_MARKER.charAt( 0 );
	}
	
}
