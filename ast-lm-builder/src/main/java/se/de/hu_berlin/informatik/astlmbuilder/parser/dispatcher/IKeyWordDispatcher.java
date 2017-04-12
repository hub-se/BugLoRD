package se.de.hu_berlin.informatik.astlmbuilder.parser.dispatcher;

import com.github.javaparser.ast.Node;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider.KeyWords;
import se.de.hu_berlin.informatik.astlmbuilder.parser.InformationWrapper;

public interface IKeyWordDispatcher extends IIndividualNodeCreator {

	/**
	 * Creates a new node object for a given token
	 * @param keyWord
	 * the keyword for choosing the node to create
	 * @param token
	 * the complete token
	 * @param info
	 * an object that holds relevant information about current variable scopes, etc.
	 * @return
	 * the parsed node
	 * @param <T>
	 * the type of returned nodes
	 */
	@SuppressWarnings("unchecked")
	default public <T extends Node> T dispatch( KeyWords keyWord, String token, InformationWrapper info ) {
		if (keyWord == null) {
			throw new IllegalArgumentException("Can not create node for null keyword.");
		}

		switch (keyWord) {
		case CONSTRUCTOR_DECLARATION:
			return (T) createConstructorDeclaration(token, info);

		case INITIALIZER_DECLARATION:
			return (T) createInitializerDeclaration(token, info);

		case ENUM_CONSTANT_DECLARATION:
			return (T) createEnumConstantDeclaration(token, info);

		case VARIABLE_DECLARATION:
			return (T) createVariableDeclarator(token, info);

		case ENUM_DECLARATION:
			return (T) createEnumDeclaration(token, info);

		case ANNOTATION_DECLARATION:
			return (T) createAnnotationDeclaration(token, info);

		case ANNOTATION_MEMBER_DECLARATION:
			return (T) createAnnotationMemberDeclaration(token, info);

		case WHILE_STMT:
			return (T) createWhileStmt(token, info);

		case TRY_STMT:
			return (T) createTryStmt(token, info);

		case THROW_STMT:
			return (T) createThrowStmt(token, info);

		case SYNCHRONIZED_STMT:
			return (T) createSynchronizedStmt(token, info);

		case SWITCH_STMT:
			return (T) createSwitchStmt(token, info);

		case SWITCH_ENTRY_STMT:
			return (T) createSwitchEntryStmt(token, info);

		case RETURN_STMT:
			return (T) createReturnStmt(token, info);

		case LABELED_STMT:
			return (T) createLabeledStmt(token, info);

		case IF_STMT:
			return (T) createIfStmt(token, info);

		case FOR_STMT:
			return (T) createForStmt(token, info);

		case FOR_EACH_STMT:
			return (T) createForeachStmt(token, info);

		case EXPRESSION_STMT:
			return (T) createExpressionStmt(token, info);

		case EXPLICIT_CONSTRUCTOR_STMT:
			return (T) createExplicitConstructorInvocationStmt(token, info);

		case DO_STMT:
			return (T) createDoStmt(token, info);

		case CONTINUE_STMT:
			return (T) createContinueStmt(token, info);

		case CATCH_CLAUSE_STMT:
			return (T) createCatchClause(token, info);

		case BLOCK_STMT:
			return (T) createBlockStmt(token, info);

		case VARIABLE_DECLARATION_EXPRESSION:
			return (T) createVariableDeclarationExpr(token, info);

		case TYPE_EXPRESSION:
			return (T) createTypeExpr(token, info);

		case SUPER_EXPRESSION:
			return (T) createSuperExpr(token, info);

		case NULL_LITERAL_EXPRESSION:
			return (T) createNullLiteralExpr(token, info);

		case METHOD_REFERENCE_EXPRESSION:
			return (T) createMethodReferenceExpr(token, info);

		case LAMBDA_EXPRESSION:
			return (T) createLambdaExpr(token, info);

		case INSTANCEOF_EXPRESSION:
			return (T) createInstanceOfExpr(token, info);

		case FIELD_ACCESS_EXPRESSION:
			return (T) createFieldAccessExpr(token, info);

		case CONDITIONAL_EXPRESSION:
			return (T) createConditionalExpr(token, info);

		case CLASS_EXPRESSION:
			return (T) createClassExpr(token, info);

		case CAST_EXPRESSION:
			return (T) createCastExpr(token, info);

		case ASSIGN_EXPRESSION:
			return (T) createAssignExpr(token, info);

		case ARRAY_INIT_EXPRESSION:
			return (T) createArrayInitializerExpr(token, info);

		case ARRAY_CREATE_EXPRESSION:
			return (T) createArrayCreationExpr(token, info);

		case ARRAY_ACCESS_EXPRESSION:
			return (T) createArrayAccessExpr(token, info);

		case PACKAGE_DECLARATION:
			return (T) createPackageDeclaration(token, info);

		case IMPORT_DECLARATION:
			return (T) createImportDeclaration(token, info);

		case FIELD_DECLARATION:
			return (T) createFieldDeclaration(token, info);

		case CLASS_OR_INTERFACE_TYPE:
			return (T) createClassOrInterfaceType(token, info);

		case CLASS_OR_INTERFACE_DECLARATION:
			return (T) createClassOrInterfaceDeclaration(token, info);

		case METHOD_DECLARATION:
			return (T) createMethodDeclaration(token, info);

		case BINARY_EXPRESSION:
			return (T) createBinaryExpr(token, info);

		case UNARY_EXPRESSION:
			return (T) createUnaryExpr(token, info);

		case METHOD_CALL_EXPRESSION:
			return (T) createMethodCallExpr(token, info);

		case NAME_EXPRESSION:
			return (T) createNameExpr(token, info);

		case INTEGER_LITERAL_EXPRESSION:
			return (T) createIntegerLiteralExpr(token, info);

		case DOUBLE_LITERAL_EXPRESSION:
			return (T) createDoubleLiteralExpr(token, info);

		case STRING_LITERAL_EXPRESSION:
			return (T) createStringLiteralExpr(token, info);

		case BOOLEAN_LITERAL_EXPRESSION:
			return (T) createBooleanLiteralExpr(token, info);

		case CHAR_LITERAL_EXPRESSION:
			return (T) createCharLiteralExpr(token, info);

		case LONG_LITERAL_EXPRESSION:
			return (T) createLongLiteralExpr(token, info);

		case THIS_EXPRESSION:
			return (T) createThisExpr(token, info);

		case BREAK:
			return (T) createBreakStmt(token, info);

		case OBJ_CREATE_EXPRESSION:
			return (T) createObjectCreationExpr(token, info);

		case MARKER_ANNOTATION_EXPRESSION:
			return (T) createMarkerAnnotationExpr(token, info);

		case NORMAL_ANNOTATION_EXPRESSION:
			return (T) createNormalAnnotationExpr(token, info);

		case SINGLE_MEMBER_ANNOTATION_EXPRESSION:
			return (T) createSingleMemberAnnotationExpr(token, info);

		case PARAMETER:
			return (T) createParameter(token, info);

		case ENCLOSED_EXPRESSION:
			return (T) createEnclosedExpr(token, info);

		case ASSERT_STMT:
			return (T) createAssertStmt(token, info);

		case MEMBER_VALUE_PAIR:
			return (T) createMemberValuePair(token, info);

		case TYPE_PRIMITIVE:
			return (T) createPrimitiveType(token, info);

		case TYPE_UNION:
			return (T) createUnionType(token, info);

		case TYPE_INTERSECTION:
			return (T) createIntersectionType(token, info);

		case TYPE_WILDCARD:
			return (T) createWildcardType(token, info);

		case TYPE_VOID:
			return (T) createVoidType(token, info);
		
		case NAME:
			return (T) createName(token, info);
		
		case SIMPLE_NAME:
			return (T) createSimpleName(token, info);
		
		case LOCAL_CLASS_DECLARATION_STMT:
			return (T) createLocalClassDeclarationStmt(token, info);
		
		case ARRAY_TYPE:
			return (T) createArrayType(token, info);
		
		case ARRAY_CREATION_LEVEL:
			return (T) createArrayCreationLevel(token, info);

		case TYPE_UNKNOWN:
			return (T) createUnknownType(token, info);
			
		case TYPE_PAR:
			return (T) createTypeParameter(token, info);
		
		case UNKNOWN:
			return (T) createUnknown(token, info);
			
		case MODULE_DECLARATION:
			return (T) createModuleDeclaration(token, info);
			
		case MODULE_EXPORTS_STMT:
			return (T) createModuleExportsStmt(token, info);
			
		case MODULE_OPENS_STMT:
			return (T) createModuleOpensStmt(token, info);
			
		case MODULE_PROVIDES_STMT:
			return (T) createModuleProvidesStmt(token, info);
			
		case MODULE_REQUIRES_STMT:
			return (T) createModuleRequiresStmt(token, info);
			
		case MODULE_USES_STMT:
			return (T) createModuleUsesStmt(token, info);
			
		case COMPILATION_UNIT:
			return (T) createCompilationUnit(token, info);
			
		case JAVADOC_COMMENT:
//			return (T) createJavaDocComment(token, info);
		case LINE_COMMENT:
//			return (T) createLineComment(token, info);
		case BLOCK_COMMENT:
//			return (T) createBlockComment(token, info);
			
		case ELSE_STMT:
		case CLOSING_BLOCK_STMT:
		case CLOSING_CATCH:
		case CLOSING_CNSTR:
		case CLOSING_COMPILATION_UNIT:
		case CLOSING_DO:
		case CLOSING_ENCLOSED:
		case CLOSING_EXPRESSION_STMT:
		case CLOSING_FOR:
		case CLOSING_FOR_EACH:
		case CLOSING_IF:
		case CLOSING_MDEC:
		case CLOSING_SWITCH:
		case CLOSING_TRY:
		case CLOSING_WHILE:
			throw new IllegalArgumentException("Can not create node for " + keyWord);
		}
		
		throw new UnsupportedOperationException("Can not create node for " + keyWord);
	}

	@Override
	default IKeyWordDispatcher getDispatcher() {
		return this;
	}

}
