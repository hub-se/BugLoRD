package se.de.hu_berlin.informatik.astlmbuilder.parser.dispatcher;

import com.github.javaparser.ast.Node;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstantsShort;
import se.de.hu_berlin.informatik.astlmbuilder.parser.ITokenParser;
import se.de.hu_berlin.informatik.astlmbuilder.parser.InformationWrapper;

public class KeyWordDispatcherShort extends KeyWordConstantsShort implements IKeyWordDispatcher {

	//TODO: not sure if this works like that...
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Node> T dispatch( String keyWord, String token, InformationWrapper info, ITokenParser parser ) {

		if (keyWord == null) {
			return null;
		}

		switch (keyWord) {
		case (CONSTRUCTOR_DECLARATION):
			return (T) parser.createConstructorDeclaration(token, info);

		case (INITIALIZER_DECLARATION):
			return (T) parser.createInitializerDeclaration(token, info);

		case (ENUM_CONSTANT_DECLARATION):
			return (T) parser.createEnumConstantDeclaration(token, info);

		case (VARIABLE_DECLARATION):
			return (T) parser.createVariableDeclarator(token, info);

		case (ENUM_DECLARATION):
			return (T) parser.createEnumDeclaration(token, info);

		case (ANNOTATION_DECLARATION):
			return (T) parser.createAnnotationDeclaration(token, info);

		case (ANNOTATION_MEMBER_DECLARATION):
			return (T) parser.createAnnotationMemberDeclaration(token, info);

		case (WHILE_STATEMENT):
			return (T) parser.createWhileStmt(token, info);

		case (TRY_STATEMENT):
			return (T) parser.createTryStmt(token, info);

		case (THROW_STATEMENT):
			return (T) parser.createThrowStmt(token, info);

		case (SYNCHRONIZED_STATEMENT):
			return (T) parser.createSynchronizedStmt(token, info);

		case (SWITCH_STATEMENT):
			return (T) parser.createSwitchStmt(token, info);

		case (SWITCH_ENTRY_STATEMENT):
			return (T) parser.createSwitchEntryStmt(token, info);

		case (RETURN_STATEMENT):
			return (T) parser.createReturnStmt(token, info);

		case (LABELED_STATEMENT):
			return (T) parser.createLabeledStmt(token, info);

		case (IF_STATEMENT):
			return (T) parser.createIfStmt(token, info);

		case (ELSE_STATEMENT):
			return (T) parser.createElseStmt(token, info);

		case (FOR_STATEMENT):
			return (T) parser.createForStmt(token, info);

		case (FOR_EACH_STATEMENT):
			return (T) parser.createForeachStmt(token, info);

		case (EXPRESSION_STATEMENT):
			return (T) parser.createExpressionStmt(token, info);

		case (EXPLICIT_CONSTRUCTOR_STATEMENT):
			return (T) parser.createExplicitConstructorInvocationStmt(token, info);

		case (DO_STATEMENT):
			return (T) parser.createDoStmt(token, info);

		case (CONTINUE_STATEMENT):
			return (T) parser.createContinueStmt(token, info);

		case (CATCH_CLAUSE_STATEMENT):
			return (T) parser.createCatchClause(token, info);

		case (BLOCK_STATEMENT):
			return (T) parser.createBlockStmt(token, info);

		case (VARIABLE_DECLARATION_EXPRESSION):
			return (T) parser.createVariableDeclarationExpr(token, info);

		case (TYPE_EXPRESSION):
			return (T) parser.createTypeExpr(token, info);

		case (SUPER_EXPRESSION):
			return (T) parser.createSuperExpr(token, info);

		case (NULL_LITERAL_EXPRESSION):
			return (T) parser.createNullLiteralExpr(token, info);

		case (METHOD_REFERENCE_EXPRESSION):
			return (T) parser.createMethodReferenceExpr(token, info);

		case (LAMBDA_EXPRESSION):
			return (T) parser.createLambdaExpr(token, info);

		case (INSTANCEOF_EXPRESSION):
			return (T) parser.createInstanceOfExpr(token, info);

		case (FIELD_ACCESS_EXPRESSION):
			return (T) parser.createFieldAccessExpr(token, info);

		case (CONDITIONAL_EXPRESSION):
			return (T) parser.createConditionalExpr(token, info);

		case (CLASS_EXPRESSION):
			return (T) parser.createClassExpr(token, info);

		case (CAST_EXPRESSION):
			return (T) parser.createCastExpr(token, info);

		case (ASSIGN_EXPRESSION):
			return (T) parser.createAssignExpr(token, info);

		case (ARRAY_INIT_EXPRESSION):
			return (T) parser.createArrayInitializerExpr(token, info);

		case (ARRAY_CREATE_EXPRESSION):
			return (T) parser.createArrayCreationExpr(token, info);

		case (ARRAY_ACCESS_EXPRESSION):
			return (T) parser.createArrayAccessExpr(token, info);

		case (PACKAGE_DECLARATION):
			return (T) parser.createPackageDeclaration(token, info);

		case (IMPORT_DECLARATION):
			return (T) parser.createImportDeclaration(token, info);

		case (FIELD_DECLARATION):
			return (T) parser.createFieldDeclaration(token, info);

		case (CLASS_OR_INTERFACE_TYPE):
			return (T) parser.createClassOrInterfaceType(token, info);

		case (CLASS_OR_INTERFACE_DECLARATION):
			return (T) parser.createClassOrInterfaceDeclaration(token, info);

		case (METHOD_DECLARATION):
			return (T) parser.createMethodDeclaration(token, info);

		case (BINARY_EXPRESSION):
			return (T) parser.createBinaryExpr(token, info);

		case (UNARY_EXPRESSION):
			return (T) parser.createUnaryExpr(token, info);

		case (METHOD_CALL_EXPRESSION):
			return (T) parser.createMethodCallExpr(token, info);

		// if a private method is called we handle it differently
		case (PRIVATE_METHOD_CALL_EXPRESSION):
			return (T) parser.createPrivMethodCallExpr(token, info);

		case (NAME_EXPRESSION):
			return (T) parser.createNameExpr(token, info);

		case (INTEGER_LITERAL_EXPRESSION):
			return (T) parser.createIntegerLiteralExpr(token, info);

		case (DOUBLE_LITERAL_EXPRESSION):
			return (T) parser.createDoubleLiteralExpr(token, info);

		case (STRING_LITERAL_EXPRESSION):
			return (T) parser.createStringLiteralExpr(token, info);

		case (BOOLEAN_LITERAL_EXPRESSION):
			return (T) parser.createBooleanLiteralExpr(token, info);

		case (CHAR_LITERAL_EXPRESSION):
			return (T) parser.createCharLiteralExpr(token, info);

		case (LONG_LITERAL_EXPRESSION):
			return (T) parser.createLongLiteralExpr(token, info);

		case (THIS_EXPRESSION):
			return (T) parser.createThisExpr(token, info);

		case (BREAK):
			return (T) parser.createBreakStmt(token, info);

		case (OBJ_CREATE_EXPRESSION):
			return (T) parser.createObjectCreationExpr(token, info);

		case (MARKER_ANNOTATION_EXPRESSION):
			return (T) parser.createMarkerAnnotationExpr(token, info);

		case (NORMAL_ANNOTATION_EXPRESSION):
			return (T) parser.createNormalAnnotationExpr(token, info);

		case (SINGLE_MEMBER_ANNOTATION_EXPRESSION):
			return (T) parser.createSingleMemberAnnotationExpr(token, info);

		case (PARAMETER):
			return (T) parser.createParameter(token, info);

		case (ENCLOSED_EXPRESSION):
			return (T) parser.createEnclosedExpr(token, info);

		case (ASSERT_STMT):
			return (T) parser.createAssertStmt(token, info);

		case (MEMBER_VALUE_PAIR):
			return (T) parser.createMemberValuePair(token, info);

		case (TYPE_PRIMITIVE):
			return (T) parser.createPrimitiveType(token, info);

		case (TYPE_UNION):
			return (T) parser.createUnionType(token, info);

		case (TYPE_INTERSECTION):
			return (T) parser.createIntersectionType(token, info);

		case (TYPE_WILDCARD):
			return (T) parser.createWildcardType(token, info);

		case (TYPE_VOID):
			return (T) parser.createVoidType(token, info);
		
		case (NAME):
			return (T) parser.createName(token, info);
		
		case (SIMPLE_NAME):
			return (T) parser.createSimpleName(token, info);
		
		case (LOCAL_CLASS_DECLARATION_STMT):
			return (T) parser.createLocalClassDeclarationStmt(token, info);
		
		case (ARRAY_TYPE):
			return (T) parser.createArrayType(token, info);
		
		case (ARRAY_CREATION_LEVEL):
			return (T) parser.createArrayCreationLevel(token, info);

		case (TYPE_UNKNOWN):
			return (T) parser.createUnknownType(token, info);
		
		case (UNKNOWN):
			return (T) parser.createUnknown(token, info);

		default:
			throw new UnsupportedOperationException();
		}

	}
	
}
