package se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords;

import com.github.javaparser.ast.Node;

public class KeyWordConstantsShort implements IKeyWordProvider<String> {

	public static final String WHILE_STATEMENT = KEYWORD_MARKER + "A";
	public static final String TRY_STATEMENT = KEYWORD_MARKER + "B";
	public static final String THROW_STATEMENT = KEYWORD_MARKER + "C";
	public static final String SYNCHRONIZED_STATEMENT = KEYWORD_MARKER + "E";
	public static final String SWITCH_STATEMENT = KEYWORD_MARKER + "F";
	public static final String SWITCH_ENTRY_STATEMENT = KEYWORD_MARKER + "G";
	public static final String RETURN_STATEMENT = KEYWORD_MARKER + "H";
	public static final String LABELED_STATEMENT = KEYWORD_MARKER + "I";
	public static final String IF_STATEMENT = KEYWORD_MARKER + "J";
	public static final String ELSE_STATEMENT = KEYWORD_MARKER + "K";
	public static final String FOR_STATEMENT = KEYWORD_MARKER + "L";
	public static final String FOR_EACH_STATEMENT = KEYWORD_MARKER + "M";
	public static final String EXPRESSION_STATEMENT = KEYWORD_MARKER + "N";
	public static final String EXPLICIT_CONSTRUCTOR_STATEMENT = KEYWORD_MARKER + "O";
	public static final String DO_STATEMENT = KEYWORD_MARKER + "Q";
	public static final String CONTINUE_STATEMENT = KEYWORD_MARKER + "R";
	public static final String CATCH_CLAUSE_STATEMENT = KEYWORD_MARKER + "S";
	public static final String BLOCK_STATEMENT = KEYWORD_MARKER + "T";
	public static final String VARIABLE_DECLARATION_EXPRESSION = KEYWORD_MARKER + "V";
	public static final String TYPE_EXPRESSION = KEYWORD_MARKER + "W";
	public static final String SUPER_EXPRESSION = KEYWORD_MARKER + "X";
	public static final String NULL_LITERAL_EXPRESSION = KEYWORD_MARKER + "Z";
	public static final String METHOD_REFERENCE_EXPRESSION = KEYWORD_MARKER + "a";
	public static final String THIS_EXPRESSION = KEYWORD_MARKER + "c";
	public static final String LAMBDA_EXPRESSION = KEYWORD_MARKER + "d";
	public static final String BREAK = KEYWORD_MARKER + "e";
	public static final String INSTANCEOF_EXPRESSION = KEYWORD_MARKER + "f";
	public static final String FIELD_ACCESS_EXPRESSION = KEYWORD_MARKER + "g";
	public static final String CONDITIONAL_EXPRESSION = KEYWORD_MARKER + "h";
	public static final String CLASS_EXPRESSION = KEYWORD_MARKER + "i";
	public static final String CAST_EXPRESSION = KEYWORD_MARKER + "j";
	public static final String ASSIGN_EXPRESSION = KEYWORD_MARKER + "k";
	public static final String ARRAY_INIT_EXPRESSION = KEYWORD_MARKER + "l";
	public static final String ARRAY_CREATE_EXPRESSION = KEYWORD_MARKER + "m";
	public static final String ARRAY_ACCESS_EXPRESSION = KEYWORD_MARKER + "n";
	public static final String CLASS_OR_INTERFACE_TYPE = KEYWORD_MARKER + "o";
	public static final String METHOD_DECLARATION = KEYWORD_MARKER + "r";
	public static final String BINARY_EXPRESSION = KEYWORD_MARKER + "s";
	public static final String UNARY_EXPRESSION = KEYWORD_MARKER + "t";
	public static final String METHOD_CALL_EXPRESSION = KEYWORD_MARKER + "u";
	// if a private method is called we handle it differently
	public static final String PRIVATE_METHOD_CALL_EXPRESSION = KEYWORD_MARKER + "v";
	public static final String NAME_EXPRESSION = KEYWORD_MARKER + "w";
	public static final String OBJ_CREATE_EXPRESSION = KEYWORD_MARKER + "x";
	public static final String PARAMETER = KEYWORD_MARKER + "y";
	public static final String ENCLOSED_EXPRESSION = KEYWORD_MARKER + "z";

	public static final String INTEGER_LITERAL_EXPRESSION = KEYWORD_MARKER + "0";
	public static final String DOUBLE_LITERAL_EXPRESSION = KEYWORD_MARKER + "1";
	public static final String STRING_LITERAL_EXPRESSION = KEYWORD_MARKER + "2";
	public static final String BOOLEAN_LITERAL_EXPRESSION = KEYWORD_MARKER + "3";
	public static final String CHAR_LITERAL_EXPRESSION = KEYWORD_MARKER + "4";
	public static final String LONG_LITERAL_EXPRESSION = KEYWORD_MARKER + "5";

	public static final String TYPE_PAR = KEYWORD_MARKER + "7";
	public static final String TYPE_VOID = KEYWORD_MARKER + "8";
	public static final String TYPE_PRIMITIVE = KEYWORD_MARKER + "9";

	public static final String COMPILATION_UNIT = KEYWORD_MARKER + "CA";
	public static final String LINE_COMMENT = KEYWORD_MARKER + "CB";
	public static final String BLOCK_COMMENT = KEYWORD_MARKER + "CD";
	public static final String JAVADOC_COMMENT = KEYWORD_MARKER + "CE";

	public static final String MARKER_ANNOTATION_EXPRESSION = KEYWORD_MARKER + "b";
	public static final String NORMAL_ANNOTATION_EXPRESSION = KEYWORD_MARKER + "p";
	public static final String SINGLE_MEMBER_ANNOTATION_EXPRESSION = KEYWORD_MARKER + "q";

	public static final String ASSERT_STMT = KEYWORD_MARKER + "BB";
	public static final String MEMBER_VALUE_PAIR = KEYWORD_MARKER + "BC";
	public static final String TYPE_UNION = KEYWORD_MARKER + "BE";
	public static final String TYPE_INTERSECTION = KEYWORD_MARKER + "BF";
	public static final String TYPE_WILDCARD = KEYWORD_MARKER + "BG";

	public static final String TYPE_UNKNOWN = KEYWORD_MARKER + "BU";
	
	public static final String NAME = KEYWORD_MARKER + "P";
	public static final String SIMPLE_NAME = KEYWORD_MARKER + "U";
	public static final String LOCAL_CLASS_DECLARATION_STMT = KEYWORD_MARKER + "D";
	public static final String ARRAY_TYPE = KEYWORD_MARKER + "6";
	public static final String ARRAY_CREATION_LEVEL = KEYWORD_MARKER + "Y";
	public static final String MODULE_DECLARATION = KEYWORD_MARKER + "MD";
	public static final String MODULE_STATEMENT = KEYWORD_MARKER + "MS";

	public static final String CONSTRUCTOR_DECLARATION = KEYWORD_MARKER + "AA";
	public static final String INITIALIZER_DECLARATION = KEYWORD_MARKER + "AB";
	public static final String ENUM_CONSTANT_DECLARATION = KEYWORD_MARKER + "AC";
	public static final String VARIABLE_DECLARATION = KEYWORD_MARKER + "AD";
	public static final String ENUM_DECLARATION = KEYWORD_MARKER + "AE";
	public static final String ANNOTATION_DECLARATION = KEYWORD_MARKER + "AF";
	public static final String ANNOTATION_MEMBER_DECLARATION = KEYWORD_MARKER + "AG";
	public static final String PACKAGE_DECLARATION = KEYWORD_MARKER + "AJ";
	public static final String IMPORT_DECLARATION = KEYWORD_MARKER + "AK";
	public static final String FIELD_DECLARATION = KEYWORD_MARKER + "AL";
	public static final String CLASS_OR_INTERFACE_DECLARATION = KEYWORD_MARKER + "AM";
	
	public static final String METHOD_IDENTIFIER = KEYWORD_MARKER + "MI";

	public static final String UNKNOWN = KEYWORD_MARKER + "UU";

	// closing tags for some special nodes
	public static final String END_SUFFIX = "_";
	public static final String CLOSING_MDEC = METHOD_DECLARATION + END_SUFFIX;
	public static final String CLOSING_CNSTR = CONSTRUCTOR_DECLARATION + END_SUFFIX;
	public static final String CLOSING_IF = IF_STATEMENT + END_SUFFIX;
	public static final String CLOSING_WHILE = WHILE_STATEMENT + END_SUFFIX;
	public static final String CLOSING_FOR = FOR_STATEMENT + END_SUFFIX;
	public static final String CLOSING_TRY = TRY_STATEMENT + END_SUFFIX;
	public static final String CLOSING_CATCH = CATCH_CLAUSE_STATEMENT + END_SUFFIX;
	public static final String CLOSING_FOR_EACH = FOR_EACH_STATEMENT + END_SUFFIX;
	public static final String CLOSING_DO = DO_STATEMENT + END_SUFFIX;
	public static final String CLOSING_SWITCH = SWITCH_STATEMENT + END_SUFFIX;
	public static final String CLOSING_ENCLOSED = ENCLOSED_EXPRESSION + END_SUFFIX;
	public static final String CLOSING_BLOCK_STMT = BLOCK_STATEMENT + END_SUFFIX;
	public static final String CLOSING_EXPRESSION_STMT = EXPRESSION_STATEMENT + END_SUFFIX;
	public static final String CLOSING_COMPILATION_UNIT = COMPILATION_UNIT + END_SUFFIX;


	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider#getMethodIdentifier()
	 */
	@Override
	public String getMethodIdentifier() {
		return METHOD_IDENTIFIER;
	}
	
	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getCompilationUnit()
	 */
	@Override
	public String getCompilationUnit() {
		return COMPILATION_UNIT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getLineComment()
	 */
	@Override
	public String getLineComment() {
		return LINE_COMMENT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getBlockComment()
	 */
	@Override
	public String getBlockComment() {
		return BLOCK_COMMENT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getJavadocComment()
	 */
	@Override
	public String getJavadocComment() {
		return JAVADOC_COMMENT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getConstructorDeclaration()
	 */
	@Override
	public String getConstructorDeclaration() {
		return CONSTRUCTOR_DECLARATION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getInitializerDeclaration()
	 */
	@Override
	public String getInitializerDeclaration() {
		return INITIALIZER_DECLARATION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getEnumConstantDeclaration()
	 */
	@Override
	public String getEnumConstantDeclaration() {
		return ENUM_CONSTANT_DECLARATION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getVariableDeclaration()
	 */
	@Override
	public String getVariableDeclaration() {
		return VARIABLE_DECLARATION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getEnumDeclaration()
	 */
	@Override
	public String getEnumDeclaration() {
		return ENUM_DECLARATION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getAnnotationDeclaration()
	 */
	@Override
	public String getAnnotationDeclaration() {
		return ANNOTATION_DECLARATION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getAnnotationMemberDeclaration()
	 */
	@Override
	public String getAnnotationMemberDeclaration() {
		return ANNOTATION_MEMBER_DECLARATION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getWhileStatement()
	 */
	@Override
	public String getWhileStatement() {
		return WHILE_STATEMENT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTryStatement()
	 */
	@Override
	public String getTryStatement() {
		return TRY_STATEMENT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getThrowStatement()
	 */
	@Override
	public String getThrowStatement() {
		return THROW_STATEMENT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getSynchronizedStatement()
	 */
	@Override
	public String getSynchronizedStatement() {
		return SYNCHRONIZED_STATEMENT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getSwitchStatement()
	 */
	@Override
	public String getSwitchStatement() {
		return SWITCH_STATEMENT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getSwitchEntryStatement()
	 */
	@Override
	public String getSwitchEntryStatement() {
		return SWITCH_ENTRY_STATEMENT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getReturnStatement()
	 */
	@Override
	public String getReturnStatement() {
		return RETURN_STATEMENT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getLabeledStatement()
	 */
	@Override
	public String getLabeledStatement() {
		return LABELED_STATEMENT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getIfStatement()
	 */
	@Override
	public String getIfStatement() {
		return IF_STATEMENT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getElseStatement()
	 */
	@Override
	public String getElseStatement() {
		return ELSE_STATEMENT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getForStatement()
	 */
	@Override
	public String getForStatement() {
		return FOR_STATEMENT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getForEachStatement()
	 */
	@Override
	public String getForEachStatement() {
		return FOR_EACH_STATEMENT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getExpressionStatement()
	 */
	@Override
	public String getExpressionStatement() {
		return EXPRESSION_STATEMENT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getExplicitConstructorStatement()
	 */
	@Override
	public String getExplicitConstructorStatement() {
		return EXPLICIT_CONSTRUCTOR_STATEMENT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getDoStatement()
	 */
	@Override
	public String getDoStatement() {
		return DO_STATEMENT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getContinueStatement()
	 */
	@Override
	public String getContinueStatement() {
		return CONTINUE_STATEMENT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getCatchClauseStatement()
	 */
	@Override
	public String getCatchClauseStatement() {
		return CATCH_CLAUSE_STATEMENT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getBlockStatement()
	 */
	@Override
	public String getBlockStatement() {
		return BLOCK_STATEMENT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getVariableDeclarationExpression()
	 */
	@Override
	public String getVariableDeclarationExpression() {
		return VARIABLE_DECLARATION_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypeExpression()
	 */
	@Override
	public String getTypeExpression() {
		return TYPE_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getSuperExpression()
	 */
	@Override
	public String getSuperExpression() {
		return SUPER_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getNullLiteralExpression()
	 */
	@Override
	public String getNullLiteralExpression() {
		return NULL_LITERAL_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getMethodReferenceExpression()
	 */
	@Override
	public String getMethodReferenceExpression() {
		return METHOD_REFERENCE_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getLambdaExpression()
	 */
	@Override
	public String getLambdaExpression() {
		return LAMBDA_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getInstanceofExpression()
	 */
	@Override
	public String getInstanceofExpression() {
		return INSTANCEOF_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getFieldAccessExpression()
	 */
	@Override
	public String getFieldAccessExpression() {
		return FIELD_ACCESS_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getConditionalExpression()
	 */
	@Override
	public String getConditionalExpression() {
		return CONDITIONAL_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClassExpression()
	 */
	@Override
	public String getClassExpression() {
		return CLASS_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getCastExpression()
	 */
	@Override
	public String getCastExpression() {
		return CAST_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getAssignExpression()
	 */
	@Override
	public String getAssignExpression() {
		return ASSIGN_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getArrayInitExpression()
	 */
	@Override
	public String getArrayInitExpression() {
		return ARRAY_INIT_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getArrayCreateExpression()
	 */
	@Override
	public String getArrayCreateExpression() {
		return ARRAY_CREATE_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getArrayAccessExpression()
	 */
	@Override
	public String getArrayAccessExpression() {
		return ARRAY_ACCESS_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getPackageDeclaration()
	 */
	@Override
	public String getPackageDeclaration() {
		return PACKAGE_DECLARATION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getImportDeclaration()
	 */
	@Override
	public String getImportDeclaration() {
		return IMPORT_DECLARATION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getFieldDeclaration()
	 */
	@Override
	public String getFieldDeclaration() {
		return FIELD_DECLARATION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClassOrInterfaceType()
	 */
	@Override
	public String getClassOrInterfaceType() {
		return CLASS_OR_INTERFACE_TYPE;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClassOrInterfaceDeclaration()
	 */
	@Override
	public String getClassOrInterfaceDeclaration() {
		return CLASS_OR_INTERFACE_DECLARATION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getMethodDeclaration()
	 */
	@Override
	public String getMethodDeclaration() {
		return METHOD_DECLARATION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getBinaryExpression()
	 */
	@Override
	public String getBinaryExpression() {
		return BINARY_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getUnaryExpression()
	 */
	@Override
	public String getUnaryExpression() {
		return UNARY_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getMethodCallExpression()
	 */
	@Override
	public String getMethodCallExpression() {
		return METHOD_CALL_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getPrivateMethodCallExpression()
	 */
	@Override
	public String getLocalMethodCallExpression() {
		return PRIVATE_METHOD_CALL_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getNameExpression()
	 */
	@Override
	public String getNameExpression() {
		return NAME_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getIntegerLiteralExpression()
	 */
	@Override
	public String getIntegerLiteralExpression() {
		return INTEGER_LITERAL_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getDoubleLiteralExpression()
	 */
	@Override
	public String getDoubleLiteralExpression() {
		return DOUBLE_LITERAL_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getStringLiteralExpression()
	 */
	@Override
	public String getStringLiteralExpression() {
		return STRING_LITERAL_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getBooleanLiteralExpression()
	 */
	@Override
	public String getBooleanLiteralExpression() {
		return BOOLEAN_LITERAL_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getCharLiteralExpression()
	 */
	@Override
	public String getCharLiteralExpression() {
		return CHAR_LITERAL_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getLongLiteralExpression()
	 */
	@Override
	public String getLongLiteralExpression() {
		return LONG_LITERAL_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getThisExpression()
	 */
	@Override
	public String getThisExpression() {
		return THIS_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getBreak()
	 */
	@Override
	public String getBreak() {
		return BREAK;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getObjCreateExpression()
	 */
	@Override
	public String getObjCreateExpression() {
		return OBJ_CREATE_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getMarkerAnnotationExpression()
	 */
	@Override
	public String getMarkerAnnotationExpression() {
		return MARKER_ANNOTATION_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getNormalAnnotationExpression()
	 */
	@Override
	public String getNormalAnnotationExpression() {
		return NORMAL_ANNOTATION_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getSingleMemberAnnotationExpression()
	 */
	@Override
	public String getSingleMemberAnnotationExpression() {
		return SINGLE_MEMBER_ANNOTATION_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getParameter()
	 */
	@Override
	public String getParameter() {
		return PARAMETER;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getEnclosedExpression()
	 */
	@Override
	public String getEnclosedExpression() {
		return ENCLOSED_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getAssertStmt()
	 */
	@Override
	public String getAssertStmt() {
		return ASSERT_STMT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getMemberValuePair()
	 */
	@Override
	public String getMemberValuePair() {
		return MEMBER_VALUE_PAIR;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypePrimitive()
	 */
	@Override
	public String getTypePrimitive() {
		return TYPE_PRIMITIVE;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypeUnion()
	 */
	@Override
	public String getTypeUnion() {
		return TYPE_UNION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypeIntersection()
	 */
	@Override
	public String getTypeIntersection() {
		return TYPE_INTERSECTION;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypePar()
	 */
	@Override
	public String getTypePar() {
		return TYPE_PAR;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypeWildcard()
	 */
	@Override
	public String getTypeWildcard() {
		return TYPE_WILDCARD;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypeVoid()
	 */
	@Override
	public String getTypeVoid() {
		return TYPE_VOID;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypeUnknown()
	 */
	@Override
	public String getTypeUnknown() {
		return TYPE_UNKNOWN;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getUnknown()
	 */
	@Override
	public String getUnknown(Node aNode) {
		if (aNode != null) {
			return UNKNOWN + GROUP_START + aNode.getClass() + GROUP_END;
		} else {
			return UNKNOWN;
		}
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getEndSuffix()
	 */
	@Override
	public String getEndSuffix() {
		return END_SUFFIX;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingMdec()
	 */
	@Override
	public String getClosingMdec() {
		return CLOSING_MDEC;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingCnstr()
	 */
	@Override
	public String getClosingCnstr() {
		return CLOSING_CNSTR;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingIf()
	 */
	@Override
	public String getClosingIf() {
		return CLOSING_IF;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingWhile()
	 */
	@Override
	public String getClosingWhile() {
		return CLOSING_WHILE;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingFor()
	 */
	@Override
	public String getClosingFor() {
		return CLOSING_FOR;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingTry()
	 */
	@Override
	public String getClosingTry() {
		return CLOSING_TRY;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingCatch()
	 */
	@Override
	public String getClosingCatch() {
		return CLOSING_CATCH;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingForEach()
	 */
	@Override
	public String getClosingForEach() {
		return CLOSING_FOR_EACH;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingDo()
	 */
	@Override
	public String getClosingDo() {
		return CLOSING_DO;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingSwitch()
	 */
	@Override
	public String getClosingSwitch() {
		return CLOSING_SWITCH;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingEnclosed()
	 */
	@Override
	public String getClosingEnclosed() {
		return CLOSING_ENCLOSED;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingBlockStmt()
	 */
	@Override
	public String getClosingBlockStmt() {
		return CLOSING_BLOCK_STMT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingExpressionStmt()
	 */
	@Override
	public String getClosingExpressionStmt() {
		return CLOSING_EXPRESSION_STMT;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingCompilationUnit()
	 */
	@Override
	public String getClosingCompilationUnit() {
		return CLOSING_COMPILATION_UNIT;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getSimpleName() {
		return SIMPLE_NAME;
	}

	@Override
	public String getLocalClassDeclarationStmt() {
		return LOCAL_CLASS_DECLARATION_STMT;
	}

	@Override
	public String getArrayType() {
		return ARRAY_TYPE;
	}

	@Override
	public String getArrayCreationLevel() {
		return ARRAY_CREATION_LEVEL;
	}

	@Override
	public String getModuleDeclaration() {
		return MODULE_DECLARATION;
	}
	
	@Override
	public String getModuleStmt() {
		return MODULE_STATEMENT;
	}
	
}
