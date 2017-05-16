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
	 * an object that holds relevant information about current variable scopes,
	 * etc.
	 * @return the parsed node
	 * @param <T>
	 * the type of returned nodes
	 * @throws ClassCastException
	 * if the returned node can not be cast to the type T
	 */
	@SuppressWarnings("unchecked")
	default public <T extends Node> T dispatch(KeyWords keyWord, String token, InformationWrapper info)
			throws ClassCastException {
		if (keyWord == null) {
			throw new IllegalArgumentException("Can not create node for null keyword.");
		}

		switch (keyWord) {
		case CONSTRUCTOR_DECLARATION:
			return (T) getParser().parseConstructorDeclaration(token, info);

		case INITIALIZER_DECLARATION:
			return (T) getParser().parseInitializerDeclaration(token, info);

		case ENUM_CONSTANT_DECLARATION:
			return (T) getParser().parseEnumConstantDeclaration(token, info);

		case VARIABLE_DECLARATION:
			return (T) getParser().parseVariableDeclarator(token, info);

		case ENUM_DECLARATION:
			return (T) getParser().parseEnumDeclaration(token, info);

		case ANNOTATION_DECLARATION:
			return (T) getParser().parseAnnotationDeclaration(token, info);

		case ANNOTATION_MEMBER_DECLARATION:
			return (T) getParser().parseAnnotationMemberDeclaration(token, info);

		case WHILE_STMT:
			return (T) getParser().parseWhileStmt(token, info);

		case TRY_STMT:
			return (T) getParser().parseTryStmt(token, info);

		case THROW_STMT:
			return (T) getParser().parseThrowStmt(token, info);

		case SYNCHRONIZED_STMT:
			return (T) getParser().parseSynchronizedStmt(token, info);

		case SWITCH_STMT:
			return (T) getParser().parseSwitchStmt(token, info);

		case SWITCH_ENTRY_STMT:
			return (T) getParser().parseSwitchEntryStmt(token, info);

		case RETURN_STMT:
			return (T) getParser().parseReturnStmt(token, info);

		case LABELED_STMT:
			return (T) getParser().parseLabeledStmt(token, info);

		case IF_STMT:
			return (T) getParser().parseIfStmt(token, info);

		case FOR_STMT:
			return (T) getParser().parseForStmt(token, info);

		case FOR_EACH_STMT:
			return (T) getParser().parseForeachStmt(token, info);

		case EXPRESSION_STMT:
			return (T) getParser().parseExpressionStmt(token, info);

		case EXPLICIT_CONSTRUCTOR_STMT:
			return (T) getParser().parseExplicitConstructorInvocationStmt(token, info);

		case DO_STMT:
			return (T) getParser().parseDoStmt(token, info);

		case CONTINUE_STMT:
			return (T) getParser().parseContinueStmt(token, info);

		case CATCH_CLAUSE_STMT:
			return (T) getParser().parseCatchClause(token, info);

		case BLOCK_STMT:
			return (T) getParser().parseBlockStmt(token, info);

		case VARIABLE_DECLARATION_EXPRESSION:
			return (T) getParser().parseVariableDeclarationExpr(token, info);

		case TYPE_EXPRESSION:
			return (T) getParser().parseTypeExpr(token, info);

		case SUPER_EXPRESSION:
			return (T) getParser().parseSuperExpr(token, info);

		case NULL_LITERAL_EXPRESSION:
			return (T) getParser().parseNullLiteralExpr(token, info);

		case METHOD_REFERENCE_EXPRESSION:
			return (T) getParser().parseMethodReferenceExpr(token, info);

		case LAMBDA_EXPRESSION:
			return (T) getParser().parseLambdaExpr(token, info);

		case INSTANCEOF_EXPRESSION:
			return (T) getParser().parseInstanceOfExpr(token, info);

		case FIELD_ACCESS_EXPRESSION:
			return (T) getParser().parseFieldAccessExpr(token, info);

		case CONDITIONAL_EXPRESSION:
			return (T) getParser().parseConditionalExpr(token, info);

		case CLASS_EXPRESSION:
			return (T) getParser().parseClassExpr(token, info);

		case CAST_EXPRESSION:
			return (T) getParser().parseCastExpr(token, info);

		case ASSIGN_EXPRESSION:
			return (T) getParser().parseAssignExpr(token, info);

		case ARRAY_INIT_EXPRESSION:
			return (T) getParser().parseArrayInitializerExpr(token, info);

		case ARRAY_CREATE_EXPRESSION:
			return (T) getParser().parseArrayCreationExpr(token, info);

		case ARRAY_ACCESS_EXPRESSION:
			return (T) getParser().parseArrayAccessExpr(token, info);

		case PACKAGE_DECLARATION:
			return (T) getParser().parsePackageDeclaration(token, info);

		case IMPORT_DECLARATION:
			return (T) getParser().parseImportDeclaration(token, info);

		case FIELD_DECLARATION:
			return (T) getParser().parseFieldDeclaration(token, info);

		case CLASS_OR_INTERFACE_TYPE:
			return (T) getParser().parseClassOrInterfaceType(token, info);

		case CLASS_OR_INTERFACE_DECLARATION:
			return (T) getParser().parseClassOrInterfaceDeclaration(token, info);

		case METHOD_DECLARATION:
			return (T) getParser().parseMethodDeclaration(token, info);

		case BINARY_EXPRESSION:
			return (T) getParser().parseBinaryExpr(token, info);

		case UNARY_EXPRESSION:
			return (T) getParser().parseUnaryExpr(token, info);

		case METHOD_CALL_EXPRESSION:
			return (T) getParser().parseMethodCallExpr(token, info);

		case NAME_EXPRESSION:
			return (T) getParser().parseNameExpr(token, info);

		case INTEGER_LITERAL_EXPRESSION:
			return (T) getParser().parseIntegerLiteralExpr(token, info);

		case DOUBLE_LITERAL_EXPRESSION:
			return (T) getParser().parseDoubleLiteralExpr(token, info);

		case STRING_LITERAL_EXPRESSION:
			return (T) getParser().parseStringLiteralExpr(token, info);

		case BOOLEAN_LITERAL_EXPRESSION:
			return (T) getParser().parseBooleanLiteralExpr(token, info);

		case CHAR_LITERAL_EXPRESSION:
			return (T) getParser().parseCharLiteralExpr(token, info);

		case LONG_LITERAL_EXPRESSION:
			return (T) getParser().parseLongLiteralExpr(token, info);

		case THIS_EXPRESSION:
			return (T) getParser().parseThisExpr(token, info);

		case BREAK:
			return (T) getParser().parseBreakStmt(token, info);

		case OBJ_CREATE_EXPRESSION:
			return (T) getParser().parseObjectCreationExpr(token, info);

		case MARKER_ANNOTATION_EXPRESSION:
			return (T) getParser().parseMarkerAnnotationExpr(token, info);

		case NORMAL_ANNOTATION_EXPRESSION:
			return (T) getParser().parseNormalAnnotationExpr(token, info);

		case SINGLE_MEMBER_ANNOTATION_EXPRESSION:
			return (T) getParser().parseSingleMemberAnnotationExpr(token, info);

		case PARAMETER:
			return (T) getParser().parseParameter(token, info);

		case ENCLOSED_EXPRESSION:
			return (T) getParser().parseEnclosedExpr(token, info);

		case ASSERT_STMT:
			return (T) getParser().parseAssertStmt(token, info);

		case MEMBER_VALUE_PAIR:
			return (T) getParser().parseMemberValuePair(token, info);

		case TYPE_PRIMITIVE:
			return (T) getParser().parsePrimitiveType(token, info);

		case TYPE_UNION:
			return (T) getParser().parseUnionType(token, info);

		case TYPE_INTERSECTION:
			return (T) getParser().parseIntersectionType(token, info);

		case TYPE_WILDCARD:
			return (T) getParser().parseWildcardType(token, info);

		case TYPE_VOID:
			return (T) getParser().parseVoidType(token, info);

		case NAME:
			return (T) getParser().parseName(token, info);

		case SIMPLE_NAME:
			return (T) getParser().parseSimpleName(token, info);

		case LOCAL_CLASS_DECLARATION_STMT:
			return (T) getParser().parseLocalClassDeclarationStmt(token, info);

		case ARRAY_TYPE:
			return (T) getParser().parseArrayType(token, info);

		case ARRAY_CREATION_LEVEL:
			return (T) getParser().parseArrayCreationLevel(token, info);

		case TYPE_UNKNOWN:
			return (T) getParser().parseUnknownType(token, info);

		case TYPE_PAR:
			return (T) getParser().parseTypeParameter(token, info);

		case UNKNOWN:
			return (T) getParser().parseUnknown(token, info);

		case MODULE_DECLARATION:
			return (T) getParser().parseModuleDeclaration(token, info);

		case MODULE_EXPORTS_STMT:
			return (T) getParser().parseModuleExportsStmt(token, info);

		case MODULE_OPENS_STMT:
			return (T) getParser().parseModuleOpensStmt(token, info);

		case MODULE_PROVIDES_STMT:
			return (T) getParser().parseModuleProvidesStmt(token, info);

		case MODULE_REQUIRES_STMT:
			return (T) getParser().parseModuleRequiresStmt(token, info);

		case MODULE_USES_STMT:
			return (T) getParser().parseModuleUsesStmt(token, info);

		case COMPILATION_UNIT:
			return (T) getParser().parseCompilationUnit(token, info);

		// can not create nodes for the following keywords
		case JAVADOC_COMMENT:
			// return (T) getParser().parseJavaDocComment(token, info);
		case LINE_COMMENT:
			// return (T) getParser().parseLineComment(token, info);
		case BLOCK_COMMENT:
			// return (T) getParser().parseBlockComment(token, info);

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

	// /**
	// * Creates a new node object for a given token
	// * @param keyWord
	// * the keyword for choosing the node to create
	// * @param token
	// * the complete token
	// * @param info
	// * an object that holds relevant information about current variable
	// scopes, etc.
	// * @param getParser()
	// * the getParser() to use
	// * @return
	// * the parsed node
	// * @param <T>
	// * the type of returned nodes
	// * @throws ClassCastException
	// * if the returned node can not be cast to the type T
	// */
	// @SuppressWarnings("unchecked")
	// default public <T extends Node> T dispatchAndGuess( KeyWords keyWord,
	// InformationWrapper info ) throws ClassCastException {
	// if (keyWord == null) {
	// throw new IllegalArgumentException("Can not create node for null
	// keyword.");
	// }
	//
	// switch (keyWord) {
	// case CONSTRUCTOR_DECLARATION:
	// return (T) getGuesser().guessConstructorDeclaration(info);
	//
	// case INITIALIZER_DECLARATION:
	// return (T) getGuesser().guessInitializerDeclaration(info);
	//
	// case ENUM_CONSTANT_DECLARATION:
	// return (T) getGuesser().guessEnumConstantDeclaration(info);
	//
	// case VARIABLE_DECLARATION:
	// return (T) getGuesser().guessVariableDeclarator(info);
	//
	// case ENUM_DECLARATION:
	// return (T) getGuesser().guessEnumDeclaration(info);
	//
	// case ANNOTATION_DECLARATION:
	// return (T) getGuesser().guessAnnotationDeclaration(info);
	//
	// case ANNOTATION_MEMBER_DECLARATION:
	// return (T) getGuesser().guessAnnotationMemberDeclaration(info);
	//
	// case WHILE_STMT:
	// return (T) getGuesser().guessWhileStmt(info);
	//
	// case TRY_STMT:
	// return (T) getGuesser().guessTryStmt(info);
	//
	// case THROW_STMT:
	// return (T) getGuesser().guessThrowStmt(info);
	//
	// case SYNCHRONIZED_STMT:
	// return (T) getGuesser().guessSynchronizedStmt(info);
	//
	// case SWITCH_STMT:
	// return (T) getGuesser().guessSwitchStmt(info);
	//
	// case SWITCH_ENTRY_STMT:
	// return (T) getGuesser().guessSwitchEntryStmt(info);
	//
	// case RETURN_STMT:
	// return (T) getGuesser().guessReturnStmt(info);
	//
	// case LABELED_STMT:
	// return (T) getGuesser().guessLabeledStmt(info);
	//
	// case IF_STMT:
	// return (T) getGuesser().guessIfStmt(info);
	//
	// case FOR_STMT:
	// return (T) getGuesser().guessForStmt(info);
	//
	// case FOR_EACH_STMT:
	// return (T) getGuesser().guessForeachStmt(info);
	//
	// case EXPRESSION_STMT:
	// return (T) getGuesser().guessExpressionStmt(info);
	//
	// case EXPLICIT_CONSTRUCTOR_STMT:
	// return (T) getGuesser().guessExplicitConstructorInvocationStmt(info);
	//
	// case DO_STMT:
	// return (T) getGuesser().guessDoStmt(info);
	//
	// case CONTINUE_STMT:
	// return (T) getGuesser().guessContinueStmt(info);
	//
	// case CATCH_CLAUSE_STMT:
	// return (T) getGuesser().guessCatchClause(info);
	//
	// case BLOCK_STMT:
	// return (T) getGuesser().guessBlockStmt(info);
	//
	// case VARIABLE_DECLARATION_EXPRESSION:
	// return (T) getGuesser().guessVariableDeclarationExpr(info);
	//
	// case TYPE_EXPRESSION:
	// return (T) getGuesser().guessTypeExpr(info);
	//
	// case SUPER_EXPRESSION:
	// return (T) getGuesser().guessSuperExpr(info);
	//
	// case NULL_LITERAL_EXPRESSION:
	// return (T) getGuesser().guessNullLiteralExpr(info);
	//
	// case METHOD_REFERENCE_EXPRESSION:
	// return (T) getGuesser().guessMethodReferenceExpr(info);
	//
	// case LAMBDA_EXPRESSION:
	// return (T) getGuesser().guessLambdaExpr(info);
	//
	// case INSTANCEOF_EXPRESSION:
	// return (T) getGuesser().guessInstanceOfExpr(info);
	//
	// case FIELD_ACCESS_EXPRESSION:
	// return (T) getGuesser().guessFieldAccessExpr(info);
	//
	// case CONDITIONAL_EXPRESSION:
	// return (T) getGuesser().guessConditionalExpr(info);
	//
	// case CLASS_EXPRESSION:
	// return (T) getGuesser().guessClassExpr(info);
	//
	// case CAST_EXPRESSION:
	// return (T) getGuesser().guessCastExpr(info);
	//
	// case ASSIGN_EXPRESSION:
	// return (T) getGuesser().guessAssignExpr(info);
	//
	// case ARRAY_INIT_EXPRESSION:
	// return (T) getGuesser().guessArrayInitializerExpr(info);
	//
	// case ARRAY_CREATE_EXPRESSION:
	// return (T) getGuesser().guessArrayCreationExpr(info);
	//
	// case ARRAY_ACCESS_EXPRESSION:
	// return (T) getGuesser().guessArrayAccessExpr(info);
	//
	// case PACKAGE_DECLARATION:
	// return (T) getGuesser().guessPackageDeclaration(info);
	//
	// case IMPORT_DECLARATION:
	// return (T) getGuesser().guessImportDeclaration(info);
	//
	// case FIELD_DECLARATION:
	// return (T) getGuesser().guessFieldDeclaration(info);
	//
	// case CLASS_OR_INTERFACE_TYPE:
	// return (T) getGuesser().guessClassOrInterfaceType(info);
	//
	// case CLASS_OR_INTERFACE_DECLARATION:
	// return (T) getGuesser().guessClassOrInterfaceDeclaration(info);
	//
	// case METHOD_DECLARATION:
	// return (T) getGuesser().guessMethodDeclaration(info);
	//
	// case BINARY_EXPRESSION:
	// return (T) getGuesser().guessBinaryExpr(info);
	//
	// case UNARY_EXPRESSION:
	// return (T) getGuesser().guessUnaryExpr(info);
	//
	// case METHOD_CALL_EXPRESSION:
	// return (T) getGuesser().guessMethodCallExpr(info);
	//
	// case NAME_EXPRESSION:
	// return (T) getGuesser().guessNameExpr(info);
	//
	// case INTEGER_LITERAL_EXPRESSION:
	// return (T) getGuesser().guessIntegerLiteralExpr(info);
	//
	// case DOUBLE_LITERAL_EXPRESSION:
	// return (T) getGuesser().guessDoubleLiteralExpr(info);
	//
	// case STRING_LITERAL_EXPRESSION:
	// return (T) getGuesser().guessStringLiteralExpr(info);
	//
	// case BOOLEAN_LITERAL_EXPRESSION:
	// return (T) getGuesser().guessBooleanLiteralExpr(info);
	//
	// case CHAR_LITERAL_EXPRESSION:
	// return (T) getGuesser().guessCharLiteralExpr(info);
	//
	// case LONG_LITERAL_EXPRESSION:
	// return (T) getGuesser().guessLongLiteralExpr(info);
	//
	// case THIS_EXPRESSION:
	// return (T) getGuesser().guessThisExpr(info);
	//
	// case BREAK:
	// return (T) getGuesser().guessBreakStmt(info);
	//
	// case OBJ_CREATE_EXPRESSION:
	// return (T) getGuesser().guessObjectCreationExpr(info);
	//
	// case MARKER_ANNOTATION_EXPRESSION:
	// return (T) getGuesser().guessMarkerAnnotationExpr(info);
	//
	// case NORMAL_ANNOTATION_EXPRESSION:
	// return (T) getGuesser().guessNormalAnnotationExpr(info);
	//
	// case SINGLE_MEMBER_ANNOTATION_EXPRESSION:
	// return (T) getGuesser().guessSingleMemberAnnotationExpr(info);
	//
	// case PARAMETER:
	// return (T) getGuesser().guessParameter(info);
	//
	// case ENCLOSED_EXPRESSION:
	// return (T) getGuesser().guessEnclosedExpr(info);
	//
	// case ASSERT_STMT:
	// return (T) getGuesser().guessAssertStmt(info);
	//
	// case MEMBER_VALUE_PAIR:
	// return (T) getGuesser().guessMemberValuePair(info);
	//
	// case TYPE_PRIMITIVE:
	// return (T) getGuesser().guessPrimitiveType(info);
	//
	// case TYPE_UNION:
	// return (T) getGuesser().guessUnionType(info);
	//
	// case TYPE_INTERSECTION:
	// return (T) getGuesser().guessIntersectionType(info);
	//
	// case TYPE_WILDCARD:
	// return (T) getGuesser().guessWildcardType(info);
	//
	// case TYPE_VOID:
	// return (T) getGuesser().guessVoidType(info);
	//
	// case NAME:
	// return (T) getGuesser().guessName(info);
	//
	// case SIMPLE_NAME:
	// return (T) getGuesser().guessSimpleName(info);
	//
	// case LOCAL_CLASS_DECLARATION_STMT:
	// return (T) getGuesser().guessLocalClassDeclarationStmt(info);
	//
	// case ARRAY_TYPE:
	// return (T) getGuesser().guessArrayType(info);
	//
	// case ARRAY_CREATION_LEVEL:
	// return (T) getGuesser().guessArrayCreationLevel(info);
	//
	// case TYPE_UNKNOWN:
	// return (T) getGuesser().guessUnknownType(info);
	//
	// case TYPE_PAR:
	// return (T) getGuesser().guessTypeParameter(info);
	//
	// case UNKNOWN:
	// return (T) getGuesser().guessUnknown(info);
	//
	// case MODULE_DECLARATION:
	// return (T) getGuesser().guessModuleDeclaration(info);
	//
	// case MODULE_EXPORTS_STMT:
	// return (T) getGuesser().guessModuleExportsStmt(info);
	//
	// case MODULE_OPENS_STMT:
	// return (T) getGuesser().guessModuleOpensStmt(info);
	//
	// case MODULE_PROVIDES_STMT:
	// return (T) getGuesser().guessModuleProvidesStmt(info);
	//
	// case MODULE_REQUIRES_STMT:
	// return (T) getGuesser().guessModuleRequiresStmt(info);
	//
	// case MODULE_USES_STMT:
	// return (T) getGuesser().guessModuleUsesStmt(info);
	//
	// case COMPILATION_UNIT:
	// return (T) getGuesser().guessCompilationUnit(info);
	//
	// //can not guess nodes for the following keywords
	// case JAVADOC_COMMENT:
	//// return (T) getGuesser().guessJavaDocComment(info);
	// case LINE_COMMENT:
	//// return (T) getGuesser().guessLineComment(info);
	// case BLOCK_COMMENT:
	//// return (T) getGuesser().guessBlockComment(info);
	//
	// case ELSE_STMT:
	//
	// case CLOSING_BLOCK_STMT:
	// case CLOSING_CATCH:
	// case CLOSING_CNSTR:
	// case CLOSING_COMPILATION_UNIT:
	// case CLOSING_DO:
	// case CLOSING_ENCLOSED:
	// case CLOSING_EXPRESSION_STMT:
	// case CLOSING_FOR:
	// case CLOSING_FOR_EACH:
	// case CLOSING_IF:
	// case CLOSING_MDEC:
	// case CLOSING_SWITCH:
	// case CLOSING_TRY:
	// case CLOSING_WHILE:
	// throw new IllegalArgumentException("Can not guess node for " + keyWord);
	// }
	//
	// throw new UnsupportedOperationException("Can not guess node for " +
	// keyWord);
	// }

}
