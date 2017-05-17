package se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords;

/**
 * Interface to get keywords for token generation
 */
public interface IKeyWordProvider<T> extends IBasicKeyWords {

	public KeyWords StringToKeyWord(String token) throws IllegalArgumentException;
	
	public enum KeyWords {
		BLOCK_STMT,
		WHILE_STMT,
		TRY_STMT,
		THROW_STMT,
		SYNCHRONIZED_STMT,
		SWITCH_STMT,
		SWITCH_ENTRY_STMT,
		RETURN_STMT,
		LABELED_STMT,
		IF_STMT,
		ELSE_STMT,
		FOR_STMT,
		FOR_EACH_STMT,
		EXPRESSION_STMT,
		EXPLICIT_CONSTRUCTOR_STMT,
		DO_STMT,
		CONTINUE_STMT,
		CATCH_CLAUSE_STMT,
		
		VARIABLE_DECLARATION_EXPRESSION,
		TYPE_EXPRESSION,
		SUPER_EXPRESSION,
		NULL_LITERAL_EXPRESSION,
		METHOD_REFERENCE_EXPRESSION,
		LAMBDA_EXPRESSION,
		INSTANCEOF_EXPRESSION,
		FIELD_ACCESS_EXPRESSION,
		CONDITIONAL_EXPRESSION,
		CLASS_EXPRESSION,
		CAST_EXPRESSION,
		ASSIGN_EXPRESSION,
		ARRAY_INIT_EXPRESSION,
		ARRAY_CREATE_EXPRESSION,
		ARRAY_ACCESS_EXPRESSION,
		
		NAME,
		SIMPLE_NAME,
		LOCAL_CLASS_DECLARATION_STMT,
		ARRAY_TYPE,
		ARRAY_CREATION_LEVEL,
		
		BREAK,
		PARAMETER,
		ENCLOSED_EXPRESSION,
		ASSERT_STMT,
		MEMBER_VALUE_PAIR,

		TYPE_PRIMITIVE,
		TYPE_UNION,
		TYPE_INTERSECTION,
		TYPE_PAR,
		TYPE_WILDCARD,
		TYPE_VOID,
		TYPE_UNKNOWN,
		CLASS_OR_INTERFACE_TYPE,
		
		BINARY_EXPRESSION,
		UNARY_EXPRESSION,
		METHOD_CALL_EXPRESSION,
		NAME_EXPRESSION,
		INTEGER_LITERAL_EXPRESSION,
		DOUBLE_LITERAL_EXPRESSION,
		STRING_LITERAL_EXPRESSION,
		BOOLEAN_LITERAL_EXPRESSION,
		CHAR_LITERAL_EXPRESSION,
		LONG_LITERAL_EXPRESSION,
		THIS_EXPRESSION,
		OBJ_CREATE_EXPRESSION,
		MARKER_ANNOTATION_EXPRESSION,
		NORMAL_ANNOTATION_EXPRESSION,
		SINGLE_MEMBER_ANNOTATION_EXPRESSION,

		// closing tags for some special nodes
		CLOSING_MDEC,
		CLOSING_CNSTR,
		CLOSING_IF,
		CLOSING_WHILE,
		CLOSING_FOR,
		CLOSING_TRY,
		CLOSING_CATCH,
		CLOSING_FOR_EACH,
		CLOSING_DO,
		CLOSING_SWITCH,
		CLOSING_ENCLOSED,
		CLOSING_BLOCK_STMT,
		CLOSING_EXPRESSION_STMT,
		CLOSING_COMPILATION_UNIT,
		
		EMPTY_STMT,

		COMPILATION_UNIT,
		CONSTRUCTOR_DECLARATION,
		INITIALIZER_DECLARATION,
		ENUM_CONSTANT_DECLARATION,
		VARIABLE_DECLARATION,
		ENUM_DECLARATION,
		ANNOTATION_DECLARATION,
		ANNOTATION_MEMBER_DECLARATION,
		CLASS_OR_INTERFACE_DECLARATION,
		METHOD_DECLARATION,
		PACKAGE_DECLARATION,
		IMPORT_DECLARATION,
		FIELD_DECLARATION,

		MODULE_DECLARATION,
		MODULE_EXPORTS_STMT,
		MODULE_OPENS_STMT,
		MODULE_USES_STMT,
		MODULE_PROVIDES_STMT,
		MODULE_REQUIRES_STMT,

		LINE_COMMENT,
		BLOCK_COMMENT,
		JAVADOC_COMMENT,
		
		UNKNOWN,
	}
	
	public T getCompilationUnit();

	public T getLineComment();

	public T getBlockComment();

	public T getJavadocComment();

	public T getConstructorDeclaration();

	public T getInitializerDeclaration();

	public T getEnumConstantDeclaration();

	public T getVariableDeclaration();

	public T getEnumDeclaration();

	public T getAnnotationDeclaration();

	public T getAnnotationMemberDeclaration();

	public T getWhileStatement();

	public T getTryStatement();

	public T getThrowStatement();

	public T getSynchronizedStatement();

	public T getSwitchStatement();

	public T getSwitchEntryStatement();

	public T getReturnStatement();

	public T getLabeledStatement();

	public T getIfStatement();

	public T getForStatement();

	public T getForEachStatement();

	public T getExpressionStatement();

	public T getExplicitConstructorStatement();

	public T getDoStatement();

	public T getContinueStatement();

	public T getCatchClauseStatement();

	public T getBlockStatement();

	public T getVariableDeclarationExpression();

	public T getTypeExpression();

	public T getSuperExpression();

	public T getNullLiteralExpression();

	public T getMethodReferenceExpression();

	public T getLambdaExpression();

	public T getInstanceofExpression();

	public T getFieldAccessExpression();

	public T getConditionalExpression();

	public T getClassExpression();

	public T getCastExpression();

	public T getAssignExpression();

	public T getArrayInitExpression();

	public T getArrayCreateExpression();

	public T getArrayAccessExpression();

	public T getPackageDeclaration();

	public T getImportDeclaration();

	public T getFieldDeclaration();

	public T getClassOrInterfaceType();

	public T getClassOrInterfaceDeclaration();

	public T getMethodDeclaration();

	public T getBinaryExpression();

	public T getUnaryExpression();

	public T getMethodCallExpression();

	public T getNameExpression();

	public T getIntegerLiteralExpression();

	public T getDoubleLiteralExpression();

	public T getStringLiteralExpression();

	public T getBooleanLiteralExpression();

	public T getCharLiteralExpression();

	public T getLongLiteralExpression();

	public T getThisExpression();

	public T getBreak();

	public T getObjCreateExpression();

	public T getMarkerAnnotationExpression();

	public T getNormalAnnotationExpression();

	public T getSingleMemberAnnotationExpression();

	public T getParameter();

	public T getEnclosedExpression();

	public T getAssertStmt();

	public T getMemberValuePair();

	public T getTypePrimitive();

	public T getTypeUnion();
	
	public T getTypePar();

	public T getTypeIntersection();

	public T getTypeWildcard();

	public T getTypeVoid();

	public T getTypeUnknown();

	public T getName();
	public T getSimpleName();
	public T getLocalClassDeclarationStmt();
	public T getArrayType();
	public T getArrayCreationLevel();
	public T getModuleDeclaration();
	
	public T getModuleExportsStmt();
	public T getModuleOpensStmt();
	public T getModuleProvidesStmt();
	public T getModuleRequiresStmt();
	public T getModuleUsesStmt();
	
	public T getUnknown();
	
	public T getEmptyStmt();

	public T getClosingMdec();

	public T getClosingCnstr();

	public T getClosingIf();

	public T getClosingWhile();

	public T getClosingFor();

	public T getClosingTry();

	public T getClosingCatch();

	public T getClosingForEach();

	public T getClosingDo();

	public T getClosingSwitch();

	public T getClosingEnclosed();

	public T getClosingBlockStmt();

	public T getClosingExpressionStmt();

	public T getClosingCompilationUnit();

}