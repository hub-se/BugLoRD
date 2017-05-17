package se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords;

public class KeyWordConstantsShort implements IKeyWordProvider<String> {

	@Override
	public KeyWords StringToKeyWord(String token) throws IllegalArgumentException{
		return KeyWords.values()[Integer.parseUnsignedInt(token, 16)];
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getCompilationUnit()
	 */
	@Override
	public String getCompilationUnit() {
		return Integer.toHexString(KeyWords.COMPILATION_UNIT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getLineComment()
	 */
	@Override
	public String getLineComment() {
		return Integer.toHexString(KeyWords.LINE_COMMENT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getBlockComment()
	 */
	@Override
	public String getBlockComment() {
		return Integer.toHexString(KeyWords.BLOCK_COMMENT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getJavadocComment()
	 */
	@Override
	public String getJavadocComment() {
		return Integer.toHexString(KeyWords.JAVADOC_COMMENT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getConstructorDeclaration()
	 */
	@Override
	public String getConstructorDeclaration() {
		return Integer.toHexString(KeyWords.CONSTRUCTOR_DECLARATION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getInitializerDeclaration()
	 */
	@Override
	public String getInitializerDeclaration() {
		return Integer.toHexString(KeyWords.INITIALIZER_DECLARATION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getEnumConstantDeclaration()
	 */
	@Override
	public String getEnumConstantDeclaration() {
		return Integer.toHexString(KeyWords.ENUM_CONSTANT_DECLARATION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getVariableDeclaration()
	 */
	@Override
	public String getVariableDeclaration() {
		return Integer.toHexString(KeyWords.VARIABLE_DECLARATION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getEnumDeclaration()
	 */
	@Override
	public String getEnumDeclaration() {
		return Integer.toHexString(KeyWords.ENUM_DECLARATION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getAnnotationDeclaration()
	 */
	@Override
	public String getAnnotationDeclaration() {
		return Integer.toHexString(KeyWords.ANNOTATION_DECLARATION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getAnnotationMemberDeclaration()
	 */
	@Override
	public String getAnnotationMemberDeclaration() {
		return Integer.toHexString(KeyWords.ANNOTATION_MEMBER_DECLARATION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getWhileStatement()
	 */
	@Override
	public String getWhileStatement() {
		return Integer.toHexString(KeyWords.WHILE_STMT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTryStatement()
	 */
	@Override
	public String getTryStatement() {
		return Integer.toHexString(KeyWords.TRY_STMT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getThrowStatement()
	 */
	@Override
	public String getThrowStatement() {
		return Integer.toHexString(KeyWords.THROW_STMT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getSynchronizedStatement()
	 */
	@Override
	public String getSynchronizedStatement() {
		return Integer.toHexString(KeyWords.SYNCHRONIZED_STMT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getSwitchStatement()
	 */
	@Override
	public String getSwitchStatement() {
		return Integer.toHexString(KeyWords.SWITCH_STMT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getSwitchEntryStatement()
	 */
	@Override
	public String getSwitchEntryStatement() {
		return Integer.toHexString(KeyWords.SWITCH_ENTRY_STMT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getReturnStatement()
	 */
	@Override
	public String getReturnStatement() {
		return Integer.toHexString(KeyWords.RETURN_STMT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getLabeledStatement()
	 */
	@Override
	public String getLabeledStatement() {
		return Integer.toHexString(KeyWords.LABELED_STMT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getIfStatement()
	 */
	@Override
	public String getIfStatement() {
		return Integer.toHexString(KeyWords.IF_STMT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getForStatement()
	 */
	@Override
	public String getForStatement() {
		return Integer.toHexString(KeyWords.FOR_STMT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getForEachStatement()
	 */
	@Override
	public String getForEachStatement() {
		return Integer.toHexString(KeyWords.FOR_EACH_STMT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getExpressionStatement()
	 */
	@Override
	public String getExpressionStatement() {
		return Integer.toHexString(KeyWords.EXPRESSION_STMT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getExplicitConstructorStatement()
	 */
	@Override
	public String getExplicitConstructorStatement() {
		return Integer.toHexString(KeyWords.EXPLICIT_CONSTRUCTOR_STMT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getDoStatement()
	 */
	@Override
	public String getDoStatement() {
		return Integer.toHexString(KeyWords.DO_STMT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getContinueStatement()
	 */
	@Override
	public String getContinueStatement() {
		return Integer.toHexString(KeyWords.CONTINUE_STMT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getCatchClauseStatement()
	 */
	@Override
	public String getCatchClauseStatement() {
		return Integer.toHexString(KeyWords.CATCH_CLAUSE_STMT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getBlockStatement()
	 */
	@Override
	public String getBlockStatement() {
		return Integer.toHexString(KeyWords.BLOCK_STMT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getVariableDeclarationExpression()
	 */
	@Override
	public String getVariableDeclarationExpression() {
		return Integer.toHexString(KeyWords.VARIABLE_DECLARATION_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypeExpression()
	 */
	@Override
	public String getTypeExpression() {
		return Integer.toHexString(KeyWords.TYPE_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getSuperExpression()
	 */
	@Override
	public String getSuperExpression() {
		return Integer.toHexString(KeyWords.SUPER_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getNullLiteralExpression()
	 */
	@Override
	public String getNullLiteralExpression() {
		return Integer.toHexString(KeyWords.NULL_LITERAL_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getMethodReferenceExpression()
	 */
	@Override
	public String getMethodReferenceExpression() {
		return Integer.toHexString(KeyWords.METHOD_REFERENCE_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getLambdaExpression()
	 */
	@Override
	public String getLambdaExpression() {
		return Integer.toHexString(KeyWords.LAMBDA_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getInstanceofExpression()
	 */
	@Override
	public String getInstanceofExpression() {
		return Integer.toHexString(KeyWords.INSTANCEOF_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getFieldAccessExpression()
	 */
	@Override
	public String getFieldAccessExpression() {
		return Integer.toHexString(KeyWords.FIELD_ACCESS_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getConditionalExpression()
	 */
	@Override
	public String getConditionalExpression() {
		return Integer.toHexString(KeyWords.CONDITIONAL_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClassExpression()
	 */
	@Override
	public String getClassExpression() {
		return Integer.toHexString(KeyWords.CLASS_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getCastExpression()
	 */
	@Override
	public String getCastExpression() {
		return Integer.toHexString(KeyWords.CAST_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getAssignExpression()
	 */
	@Override
	public String getAssignExpression() {
		return Integer.toHexString(KeyWords.ASSIGN_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getArrayInitExpression()
	 */
	@Override
	public String getArrayInitExpression() {
		return Integer.toHexString(KeyWords.ARRAY_INIT_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getArrayCreateExpression()
	 */
	@Override
	public String getArrayCreateExpression() {
		return Integer.toHexString(KeyWords.ARRAY_CREATE_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getArrayAccessExpression()
	 */
	@Override
	public String getArrayAccessExpression() {
		return Integer.toHexString(KeyWords.ARRAY_ACCESS_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getPackageDeclaration()
	 */
	@Override
	public String getPackageDeclaration() {
		return Integer.toHexString(KeyWords.PACKAGE_DECLARATION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getImportDeclaration()
	 */
	@Override
	public String getImportDeclaration() {
		return Integer.toHexString(KeyWords.IMPORT_DECLARATION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getFieldDeclaration()
	 */
	@Override
	public String getFieldDeclaration() {
		return Integer.toHexString(KeyWords.FIELD_DECLARATION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClassOrInterfaceType()
	 */
	@Override
	public String getClassOrInterfaceType() {
		return Integer.toHexString(KeyWords.CLASS_OR_INTERFACE_TYPE.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClassOrInterfaceDeclaration()
	 */
	@Override
	public String getClassOrInterfaceDeclaration() {
		return Integer.toHexString(KeyWords.CLASS_OR_INTERFACE_DECLARATION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getMethodDeclaration()
	 */
	@Override
	public String getMethodDeclaration() {
		return Integer.toHexString(KeyWords.METHOD_DECLARATION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getBinaryExpression()
	 */
	@Override
	public String getBinaryExpression() {
		return Integer.toHexString(KeyWords.BINARY_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getUnaryExpression()
	 */
	@Override
	public String getUnaryExpression() {
		return Integer.toHexString(KeyWords.UNARY_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getMethodCallExpression()
	 */
	@Override
	public String getMethodCallExpression() {
		return Integer.toHexString(KeyWords.METHOD_CALL_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getNameExpression()
	 */
	@Override
	public String getNameExpression() {
		return Integer.toHexString(KeyWords.NAME_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getIntegerLiteralExpression()
	 */
	@Override
	public String getIntegerLiteralExpression() {
		return Integer.toHexString(KeyWords.INTEGER_LITERAL_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getDoubleLiteralExpression()
	 */
	@Override
	public String getDoubleLiteralExpression() {
		return Integer.toHexString(KeyWords.DOUBLE_LITERAL_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getStringLiteralExpression()
	 */
	@Override
	public String getStringLiteralExpression() {
		return Integer.toHexString(KeyWords.STRING_LITERAL_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getBooleanLiteralExpression()
	 */
	@Override
	public String getBooleanLiteralExpression() {
		return Integer.toHexString(KeyWords.BOOLEAN_LITERAL_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getCharLiteralExpression()
	 */
	@Override
	public String getCharLiteralExpression() {
		return Integer.toHexString(KeyWords.CHAR_LITERAL_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getLongLiteralExpression()
	 */
	@Override
	public String getLongLiteralExpression() {
		return Integer.toHexString(KeyWords.LONG_LITERAL_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getThisExpression()
	 */
	@Override
	public String getThisExpression() {
		return Integer.toHexString(KeyWords.THIS_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getBreak()
	 */
	@Override
	public String getBreak() {
		return Integer.toHexString(KeyWords.BREAK.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getObjCreateExpression()
	 */
	@Override
	public String getObjCreateExpression() {
		return Integer.toHexString(KeyWords.OBJ_CREATE_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getMarkerAnnotationExpression()
	 */
	@Override
	public String getMarkerAnnotationExpression() {
		return Integer.toHexString(KeyWords.MARKER_ANNOTATION_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getNormalAnnotationExpression()
	 */
	@Override
	public String getNormalAnnotationExpression() {
		return Integer.toHexString(KeyWords.NORMAL_ANNOTATION_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getSingleMemberAnnotationExpression()
	 */
	@Override
	public String getSingleMemberAnnotationExpression() {
		return Integer.toHexString(KeyWords.SINGLE_MEMBER_ANNOTATION_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getParameter()
	 */
	@Override
	public String getParameter() {
		return Integer.toHexString(KeyWords.PARAMETER.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getEnclosedExpression()
	 */
	@Override
	public String getEnclosedExpression() {
		return Integer.toHexString(KeyWords.ENCLOSED_EXPRESSION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getAssertStmt()
	 */
	@Override
	public String getAssertStmt() {
		return Integer.toHexString(KeyWords.ASSERT_STMT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getMemberValuePair()
	 */
	@Override
	public String getMemberValuePair() {
		return Integer.toHexString(KeyWords.MEMBER_VALUE_PAIR.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypePrimitive()
	 */
	@Override
	public String getTypePrimitive() {
		return Integer.toHexString(KeyWords.TYPE_PRIMITIVE.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypeUnion()
	 */
	@Override
	public String getTypeUnion() {
		return Integer.toHexString(KeyWords.TYPE_UNION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypeIntersection()
	 */
	@Override
	public String getTypeIntersection() {
		return Integer.toHexString(KeyWords.TYPE_INTERSECTION.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypePar()
	 */
	@Override
	public String getTypePar() {
		return Integer.toHexString(KeyWords.TYPE_PAR.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypeWildcard()
	 */
	@Override
	public String getTypeWildcard() {
		return Integer.toHexString(KeyWords.TYPE_WILDCARD.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypeVoid()
	 */
	@Override
	public String getTypeVoid() {
		return Integer.toHexString(KeyWords.TYPE_VOID.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypeUnknown()
	 */
	@Override
	public String getTypeUnknown() {
		return Integer.toHexString(KeyWords.TYPE_UNKNOWN.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getUnknown()
	 */
	@Override
	public String getUnknown() {
		return Integer.toHexString(KeyWords.UNKNOWN.ordinal());
	}
	
	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider#getEmptyStmt()
	 */
	@Override
	public String getEmptyStmt() {
		return Integer.toHexString(KeyWords.EMPTY_STMT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingMdec()
	 */
	@Override
	public String getClosingMdec() {
		return Integer.toHexString(KeyWords.CLOSING_MDEC.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingCnstr()
	 */
	@Override
	public String getClosingCnstr() {
		return Integer.toHexString(KeyWords.CLOSING_CNSTR.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingIf()
	 */
	@Override
	public String getClosingIf() {
		return Integer.toHexString(KeyWords.CLOSING_IF.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingWhile()
	 */
	@Override
	public String getClosingWhile() {
		return Integer.toHexString(KeyWords.CLOSING_WHILE.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingFor()
	 */
	@Override
	public String getClosingFor() {
		return Integer.toHexString(KeyWords.CLOSING_FOR.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingTry()
	 */
	@Override
	public String getClosingTry() {
		return Integer.toHexString(KeyWords.CLOSING_TRY.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingCatch()
	 */
	@Override
	public String getClosingCatch() {
		return Integer.toHexString(KeyWords.CLOSING_CATCH.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingForEach()
	 */
	@Override
	public String getClosingForEach() {
		return Integer.toHexString(KeyWords.CLOSING_FOR_EACH.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingDo()
	 */
	@Override
	public String getClosingDo() {
		return Integer.toHexString(KeyWords.CLOSING_DO.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingSwitch()
	 */
	@Override
	public String getClosingSwitch() {
		return Integer.toHexString(KeyWords.CLOSING_SWITCH.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingEnclosed()
	 */
	@Override
	public String getClosingEnclosed() {
		return Integer.toHexString(KeyWords.CLOSING_ENCLOSED.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingBlockStmt()
	 */
	@Override
	public String getClosingBlockStmt() {
		return Integer.toHexString(KeyWords.CLOSING_BLOCK_STMT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingExpressionStmt()
	 */
	@Override
	public String getClosingExpressionStmt() {
		return Integer.toHexString(KeyWords.CLOSING_EXPRESSION_STMT.ordinal());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingCompilationUnit()
	 */
	@Override
	public String getClosingCompilationUnit() {
		return Integer.toHexString(KeyWords.CLOSING_COMPILATION_UNIT.ordinal());
	}

	@Override
	public String getName() {
		return Integer.toHexString(KeyWords.NAME.ordinal());
	}

	@Override
	public String getSimpleName() {
		return Integer.toHexString(KeyWords.SIMPLE_NAME.ordinal());
	}

	@Override
	public String getLocalClassDeclarationStmt() {
		return Integer.toHexString(KeyWords.LOCAL_CLASS_DECLARATION_STMT.ordinal());
	}

	@Override
	public String getArrayType() {
		return Integer.toHexString(KeyWords.ARRAY_TYPE.ordinal());
	}

	@Override
	public String getArrayCreationLevel() {
		return Integer.toHexString(KeyWords.ARRAY_CREATION_LEVEL.ordinal());
	}
	
	@Override
	public String getModuleDeclaration() {
		return Integer.toHexString(KeyWords.MODULE_DECLARATION.ordinal());
	}
	
	@Override
	public String getModuleExportsStmt() {
		return Integer.toHexString(KeyWords.MODULE_EXPORTS_STMT.ordinal());
	}

	@Override
	public String getModuleOpensStmt() {
		return Integer.toHexString(KeyWords.MODULE_OPENS_STMT.ordinal());
	}

	@Override
	public String getModuleProvidesStmt() {
		return Integer.toHexString(KeyWords.MODULE_PROVIDES_STMT.ordinal());
	}

	@Override
	public String getModuleRequiresStmt() {
		return Integer.toHexString(KeyWords.MODULE_REQUIRES_STMT.ordinal());
	}

	@Override
	public String getModuleUsesStmt() {
		return Integer.toHexString(KeyWords.MODULE_USES_STMT.ordinal());
	}
	
}
