package se.de.hu_berlin.informatik.astlmbuilder.parsing.dispatcher;

import com.github.javaparser.ast.Node;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider.KeyWords;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.InformationWrapper;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.parser.ITokenParser;

public interface IKeyWordDispatcher {

	public ITokenParser getParser();
	
	/**
	 * Creates a new node object for a given token
	 * @param keyWord
	 * the keyword for choosing the node to create
	 * @param token
	 * the complete token
	 * @param info
	 * an object that holds relevant information about current variable scopes, etc.
	 * @param getParser()
	 * the getParser() to use
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
			return (T) getParser().createConstructorDeclaration(token, info);

		case INITIALIZER_DECLARATION:
			return (T) getParser().createInitializerDeclaration(token, info);

		case ENUM_CONSTANT_DECLARATION:
			return (T) getParser().createEnumConstantDeclaration(token, info);

		case VARIABLE_DECLARATION:
			return (T) getParser().createVariableDeclarator(token, info);

		case ENUM_DECLARATION:
			return (T) getParser().createEnumDeclaration(token, info);

		case ANNOTATION_DECLARATION:
			return (T) getParser().createAnnotationDeclaration(token, info);

		case ANNOTATION_MEMBER_DECLARATION:
			return (T) getParser().createAnnotationMemberDeclaration(token, info);

		case WHILE_STMT:
			return (T) getParser().createWhileStmt(token, info);

		case TRY_STMT:
			return (T) getParser().createTryStmt(token, info);

		case THROW_STMT:
			return (T) getParser().createThrowStmt(token, info);

		case SYNCHRONIZED_STMT:
			return (T) getParser().createSynchronizedStmt(token, info);

		case SWITCH_STMT:
			return (T) getParser().createSwitchStmt(token, info);

		case SWITCH_ENTRY_STMT:
			return (T) getParser().createSwitchEntryStmt(token, info);

		case RETURN_STMT:
			return (T) getParser().createReturnStmt(token, info);

		case LABELED_STMT:
			return (T) getParser().createLabeledStmt(token, info);

		case IF_STMT:
			return (T) getParser().createIfStmt(token, info);

		case FOR_STMT:
			return (T) getParser().createForStmt(token, info);

		case FOR_EACH_STMT:
			return (T) getParser().createForeachStmt(token, info);

		case EXPRESSION_STMT:
			return (T) getParser().createExpressionStmt(token, info);

		case EXPLICIT_CONSTRUCTOR_STMT:
			return (T) getParser().createExplicitConstructorInvocationStmt(token, info);

		case DO_STMT:
			return (T) getParser().createDoStmt(token, info);

		case CONTINUE_STMT:
			return (T) getParser().createContinueStmt(token, info);

		case CATCH_CLAUSE_STMT:
			return (T) getParser().createCatchClause(token, info);

		case BLOCK_STMT:
			return (T) getParser().createBlockStmt(token, info);

		case VARIABLE_DECLARATION_EXPRESSION:
			return (T) getParser().createVariableDeclarationExpr(token, info);

		case TYPE_EXPRESSION:
			return (T) getParser().createTypeExpr(token, info);

		case SUPER_EXPRESSION:
			return (T) getParser().createSuperExpr(token, info);

		case NULL_LITERAL_EXPRESSION:
			return (T) getParser().createNullLiteralExpr(token, info);

		case METHOD_REFERENCE_EXPRESSION:
			return (T) getParser().createMethodReferenceExpr(token, info);

		case LAMBDA_EXPRESSION:
			return (T) getParser().createLambdaExpr(token, info);

		case INSTANCEOF_EXPRESSION:
			return (T) getParser().createInstanceOfExpr(token, info);

		case FIELD_ACCESS_EXPRESSION:
			return (T) getParser().createFieldAccessExpr(token, info);

		case CONDITIONAL_EXPRESSION:
			return (T) getParser().createConditionalExpr(token, info);

		case CLASS_EXPRESSION:
			return (T) getParser().createClassExpr(token, info);

		case CAST_EXPRESSION:
			return (T) getParser().createCastExpr(token, info);

		case ASSIGN_EXPRESSION:
			return (T) getParser().createAssignExpr(token, info);

		case ARRAY_INIT_EXPRESSION:
			return (T) getParser().createArrayInitializerExpr(token, info);

		case ARRAY_CREATE_EXPRESSION:
			return (T) getParser().createArrayCreationExpr(token, info);

		case ARRAY_ACCESS_EXPRESSION:
			return (T) getParser().createArrayAccessExpr(token, info);

		case PACKAGE_DECLARATION:
			return (T) getParser().createPackageDeclaration(token, info);

		case IMPORT_DECLARATION:
			return (T) getParser().createImportDeclaration(token, info);

		case FIELD_DECLARATION:
			return (T) getParser().createFieldDeclaration(token, info);

		case CLASS_OR_INTERFACE_TYPE:
			return (T) getParser().createClassOrInterfaceType(token, info);

		case CLASS_OR_INTERFACE_DECLARATION:
			return (T) getParser().createClassOrInterfaceDeclaration(token, info);

		case METHOD_DECLARATION:
			return (T) getParser().createMethodDeclaration(token, info);

		case BINARY_EXPRESSION:
			return (T) getParser().createBinaryExpr(token, info);

		case UNARY_EXPRESSION:
			return (T) getParser().createUnaryExpr(token, info);

		case METHOD_CALL_EXPRESSION:
			return (T) getParser().createMethodCallExpr(token, info);

		case NAME_EXPRESSION:
			return (T) getParser().createNameExpr(token, info);

		case INTEGER_LITERAL_EXPRESSION:
			return (T) getParser().createIntegerLiteralExpr(token, info);

		case DOUBLE_LITERAL_EXPRESSION:
			return (T) getParser().createDoubleLiteralExpr(token, info);

		case STRING_LITERAL_EXPRESSION:
			return (T) getParser().createStringLiteralExpr(token, info);

		case BOOLEAN_LITERAL_EXPRESSION:
			return (T) getParser().createBooleanLiteralExpr(token, info);

		case CHAR_LITERAL_EXPRESSION:
			return (T) getParser().createCharLiteralExpr(token, info);

		case LONG_LITERAL_EXPRESSION:
			return (T) getParser().createLongLiteralExpr(token, info);

		case THIS_EXPRESSION:
			return (T) getParser().createThisExpr(token, info);

		case BREAK:
			return (T) getParser().createBreakStmt(token, info);

		case OBJ_CREATE_EXPRESSION:
			return (T) getParser().createObjectCreationExpr(token, info);

		case MARKER_ANNOTATION_EXPRESSION:
			return (T) getParser().createMarkerAnnotationExpr(token, info);

		case NORMAL_ANNOTATION_EXPRESSION:
			return (T) getParser().createNormalAnnotationExpr(token, info);

		case SINGLE_MEMBER_ANNOTATION_EXPRESSION:
			return (T) getParser().createSingleMemberAnnotationExpr(token, info);

		case PARAMETER:
			return (T) getParser().createParameter(token, info);

		case ENCLOSED_EXPRESSION:
			return (T) getParser().createEnclosedExpr(token, info);

		case ASSERT_STMT:
			return (T) getParser().createAssertStmt(token, info);

		case MEMBER_VALUE_PAIR:
			return (T) getParser().createMemberValuePair(token, info);

		case TYPE_PRIMITIVE:
			return (T) getParser().createPrimitiveType(token, info);

		case TYPE_UNION:
			return (T) getParser().createUnionType(token, info);

		case TYPE_INTERSECTION:
			return (T) getParser().createIntersectionType(token, info);

		case TYPE_WILDCARD:
			return (T) getParser().createWildcardType(token, info);

		case TYPE_VOID:
			return (T) getParser().createVoidType(token, info);
		
		case NAME:
			return (T) getParser().createName(token, info);
		
		case SIMPLE_NAME:
			return (T) getParser().createSimpleName(token, info);
		
		case LOCAL_CLASS_DECLARATION_STMT:
			return (T) getParser().createLocalClassDeclarationStmt(token, info);
		
		case ARRAY_TYPE:
			return (T) getParser().createArrayType(token, info);
		
		case ARRAY_CREATION_LEVEL:
			return (T) getParser().createArrayCreationLevel(token, info);

		case TYPE_UNKNOWN:
			return (T) getParser().createUnknownType(token, info);
			
		case TYPE_PAR:
			return (T) getParser().createTypeParameter(token, info);
		
		case UNKNOWN:
			return (T) getParser().createUnknown(token, info);
			
		case MODULE_DECLARATION:
			return (T) getParser().createModuleDeclaration(token, info);
			
		case MODULE_EXPORTS_STMT:
			return (T) getParser().createModuleExportsStmt(token, info);
			
		case MODULE_OPENS_STMT:
			return (T) getParser().createModuleOpensStmt(token, info);
			
		case MODULE_PROVIDES_STMT:
			return (T) getParser().createModuleProvidesStmt(token, info);
			
		case MODULE_REQUIRES_STMT:
			return (T) getParser().createModuleRequiresStmt(token, info);
			
		case MODULE_USES_STMT:
			return (T) getParser().createModuleUsesStmt(token, info);
			
		case COMPILATION_UNIT:
			return (T) getParser().createCompilationUnit(token, info);
			
		//can not create nodes for the following keywords
		case JAVADOC_COMMENT:
//			return (T) getParser().createJavaDocComment(token, info);
		case LINE_COMMENT:
//			return (T) getParser().createLineComment(token, info);
		case BLOCK_COMMENT:
//			return (T) getParser().createBlockComment(token, info);
			
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

}
