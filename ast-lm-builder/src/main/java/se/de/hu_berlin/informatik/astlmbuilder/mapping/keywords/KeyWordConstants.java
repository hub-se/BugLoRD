package se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords;

import com.github.javaparser.ast.Node;

public class KeyWordConstants implements IKeyWordProvider<String> {

	@Override
	public KeyWords StringToKeyWord(String token) throws IllegalArgumentException{
		return KeyWords.valueOf(token);
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getCompilationUnit()
	 */
	@Override
	public String getCompilationUnit() {
		return KeyWords.COMPILATION_UNIT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getLineComment()
	 */
	@Override
	public String getLineComment() {
		return KeyWords.LINE_COMMENT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getBlockComment()
	 */
	@Override
	public String getBlockComment() {
		return KeyWords.BLOCK_COMMENT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getJavadocComment()
	 */
	@Override
	public String getJavadocComment() {
		return KeyWords.JAVADOC_COMMENT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getConstructorDeclaration()
	 */
	@Override
	public String getConstructorDeclaration() {
		return KeyWords.CONSTRUCTOR_DECLARATION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getInitializerDeclaration()
	 */
	@Override
	public String getInitializerDeclaration() {
		return KeyWords.INITIALIZER_DECLARATION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getEnumConstantDeclaration()
	 */
	@Override
	public String getEnumConstantDeclaration() {
		return KeyWords.ENUM_CONSTANT_DECLARATION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getVariableDeclaration()
	 */
	@Override
	public String getVariableDeclaration() {
		return KeyWords.VARIABLE_DECLARATION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getEnumDeclaration()
	 */
	@Override
	public String getEnumDeclaration() {
		return KeyWords.ENUM_DECLARATION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getAnnotationDeclaration()
	 */
	@Override
	public String getAnnotationDeclaration() {
		return KeyWords.ANNOTATION_DECLARATION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getAnnotationMemberDeclaration()
	 */
	@Override
	public String getAnnotationMemberDeclaration() {
		return KeyWords.ANNOTATION_MEMBER_DECLARATION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getWhileStatement()
	 */
	@Override
	public String getWhileStatement() {
		return KeyWords.WHILE_STMT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTryStatement()
	 */
	@Override
	public String getTryStatement() {
		return KeyWords.TRY_STMT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getThrowStatement()
	 */
	@Override
	public String getThrowStatement() {
		return KeyWords.THROW_STMT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getSynchronizedStatement()
	 */
	@Override
	public String getSynchronizedStatement() {
		return KeyWords.SYNCHRONIZED_STMT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getSwitchStatement()
	 */
	@Override
	public String getSwitchStatement() {
		return KeyWords.SWITCH_STMT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getSwitchEntryStatement()
	 */
	@Override
	public String getSwitchEntryStatement() {
		return KeyWords.SWITCH_ENTRY_STMT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getReturnStatement()
	 */
	@Override
	public String getReturnStatement() {
		return KeyWords.RETURN_STMT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getLabeledStatement()
	 */
	@Override
	public String getLabeledStatement() {
		return KeyWords.LABELED_STMT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getIfStatement()
	 */
	@Override
	public String getIfStatement() {
		return KeyWords.IF_STMT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getForStatement()
	 */
	@Override
	public String getForStatement() {
		return KeyWords.FOR_STMT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getForEachStatement()
	 */
	@Override
	public String getForEachStatement() {
		return KeyWords.FOR_EACH_STMT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getExpressionStatement()
	 */
	@Override
	public String getExpressionStatement() {
		return KeyWords.EXPRESSION_STMT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getExplicitConstructorStatement()
	 */
	@Override
	public String getExplicitConstructorStatement() {
		return KeyWords.EXPLICIT_CONSTRUCTOR_STMT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getDoStatement()
	 */
	@Override
	public String getDoStatement() {
		return KeyWords.DO_STMT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getContinueStatement()
	 */
	@Override
	public String getContinueStatement() {
		return KeyWords.CONTINUE_STMT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getCatchClauseStatement()
	 */
	@Override
	public String getCatchClauseStatement() {
		return KeyWords.CATCH_CLAUSE_STMT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getBlockStatement()
	 */
	@Override
	public String getBlockStatement() {
		return KeyWords.BLOCK_STMT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getVariableDeclarationExpression()
	 */
	@Override
	public String getVariableDeclarationExpression() {
		return KeyWords.VARIABLE_DECLARATION_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypeExpression()
	 */
	@Override
	public String getTypeExpression() {
		return KeyWords.TYPE_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getSuperExpression()
	 */
	@Override
	public String getSuperExpression() {
		return KeyWords.SUPER_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getNullLiteralExpression()
	 */
	@Override
	public String getNullLiteralExpression() {
		return KeyWords.NULL_LITERAL_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getMethodReferenceExpression()
	 */
	@Override
	public String getMethodReferenceExpression() {
		return KeyWords.METHOD_REFERENCE_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getLambdaExpression()
	 */
	@Override
	public String getLambdaExpression() {
		return KeyWords.LAMBDA_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getInstanceofExpression()
	 */
	@Override
	public String getInstanceofExpression() {
		return KeyWords.INSTANCEOF_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getFieldAccessExpression()
	 */
	@Override
	public String getFieldAccessExpression() {
		return KeyWords.FIELD_ACCESS_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getConditionalExpression()
	 */
	@Override
	public String getConditionalExpression() {
		return KeyWords.CONDITIONAL_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClassExpression()
	 */
	@Override
	public String getClassExpression() {
		return KeyWords.CLASS_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getCastExpression()
	 */
	@Override
	public String getCastExpression() {
		return KeyWords.CAST_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getAssignExpression()
	 */
	@Override
	public String getAssignExpression() {
		return KeyWords.ASSIGN_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getArrayInitExpression()
	 */
	@Override
	public String getArrayInitExpression() {
		return KeyWords.ARRAY_INIT_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getArrayCreateExpression()
	 */
	@Override
	public String getArrayCreateExpression() {
		return KeyWords.ARRAY_CREATE_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getArrayAccessExpression()
	 */
	@Override
	public String getArrayAccessExpression() {
		return KeyWords.ARRAY_ACCESS_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getPackageDeclaration()
	 */
	@Override
	public String getPackageDeclaration() {
		return KeyWords.PACKAGE_DECLARATION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getImportDeclaration()
	 */
	@Override
	public String getImportDeclaration() {
		return KeyWords.IMPORT_DECLARATION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getFieldDeclaration()
	 */
	@Override
	public String getFieldDeclaration() {
		return KeyWords.FIELD_DECLARATION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClassOrInterfaceType()
	 */
	@Override
	public String getClassOrInterfaceType() {
		return KeyWords.CLASS_OR_INTERFACE_TYPE.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClassOrInterfaceDeclaration()
	 */
	@Override
	public String getClassOrInterfaceDeclaration() {
		return KeyWords.CLASS_OR_INTERFACE_DECLARATION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getMethodDeclaration()
	 */
	@Override
	public String getMethodDeclaration() {
		return KeyWords.METHOD_DECLARATION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getBinaryExpression()
	 */
	@Override
	public String getBinaryExpression() {
		return KeyWords.BINARY_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getUnaryExpression()
	 */
	@Override
	public String getUnaryExpression() {
		return KeyWords.UNARY_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getMethodCallExpression()
	 */
	@Override
	public String getMethodCallExpression() {
		return KeyWords.METHOD_CALL_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getNameExpression()
	 */
	@Override
	public String getNameExpression() {
		return KeyWords.NAME_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getIntegerLiteralExpression()
	 */
	@Override
	public String getIntegerLiteralExpression() {
		return KeyWords.INTEGER_LITERAL_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getDoubleLiteralExpression()
	 */
	@Override
	public String getDoubleLiteralExpression() {
		return KeyWords.DOUBLE_LITERAL_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getStringLiteralExpression()
	 */
	@Override
	public String getStringLiteralExpression() {
		return KeyWords.STRING_LITERAL_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getBooleanLiteralExpression()
	 */
	@Override
	public String getBooleanLiteralExpression() {
		return KeyWords.BOOLEAN_LITERAL_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getCharLiteralExpression()
	 */
	@Override
	public String getCharLiteralExpression() {
		return KeyWords.CHAR_LITERAL_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getLongLiteralExpression()
	 */
	@Override
	public String getLongLiteralExpression() {
		return KeyWords.LONG_LITERAL_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getThisExpression()
	 */
	@Override
	public String getThisExpression() {
		return KeyWords.THIS_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getBreak()
	 */
	@Override
	public String getBreak() {
		return KeyWords.BREAK.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getObjCreateExpression()
	 */
	@Override
	public String getObjCreateExpression() {
		return KeyWords.OBJ_CREATE_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getMarkerAnnotationExpression()
	 */
	@Override
	public String getMarkerAnnotationExpression() {
		return KeyWords.MARKER_ANNOTATION_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getNormalAnnotationExpression()
	 */
	@Override
	public String getNormalAnnotationExpression() {
		return KeyWords.NORMAL_ANNOTATION_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getSingleMemberAnnotationExpression()
	 */
	@Override
	public String getSingleMemberAnnotationExpression() {
		return KeyWords.SINGLE_MEMBER_ANNOTATION_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getParameter()
	 */
	@Override
	public String getParameter() {
		return KeyWords.PARAMETER.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getEnclosedExpression()
	 */
	@Override
	public String getEnclosedExpression() {
		return KeyWords.ENCLOSED_EXPRESSION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getAssertStmt()
	 */
	@Override
	public String getAssertStmt() {
		return KeyWords.ASSERT_STMT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getMemberValuePair()
	 */
	@Override
	public String getMemberValuePair() {
		return KeyWords.MEMBER_VALUE_PAIR.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypePrimitive()
	 */
	@Override
	public String getTypePrimitive() {
		return KeyWords.TYPE_PRIMITIVE.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypeUnion()
	 */
	@Override
	public String getTypeUnion() {
		return KeyWords.TYPE_UNION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypeIntersection()
	 */
	@Override
	public String getTypeIntersection() {
		return KeyWords.TYPE_INTERSECTION.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypePar()
	 */
	@Override
	public String getTypePar() {
		return KeyWords.TYPE_PAR.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypeWildcard()
	 */
	@Override
	public String getTypeWildcard() {
		return KeyWords.TYPE_WILDCARD.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypeVoid()
	 */
	@Override
	public String getTypeVoid() {
		return KeyWords.TYPE_VOID.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getTypeUnknown()
	 */
	@Override
	public String getTypeUnknown() {
		return KeyWords.TYPE_UNKNOWN.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getUnknown()
	 */
	@Override
	public String getUnknown(Node aNode) {
		if (aNode != null) {
			return KeyWords.UNKNOWN.toString() + GROUP_START + aNode.getClass() + GROUP_END;
		} else {
			return KeyWords.UNKNOWN.toString();
		}
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingMdec()
	 */
	@Override
	public String getClosingMdec() {
		return KeyWords.CLOSING_MDEC.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingCnstr()
	 */
	@Override
	public String getClosingCnstr() {
		return KeyWords.CLOSING_CNSTR.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingIf()
	 */
	@Override
	public String getClosingIf() {
		return KeyWords.CLOSING_IF.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingWhile()
	 */
	@Override
	public String getClosingWhile() {
		return KeyWords.CLOSING_WHILE.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingFor()
	 */
	@Override
	public String getClosingFor() {
		return KeyWords.CLOSING_FOR.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingTry()
	 */
	@Override
	public String getClosingTry() {
		return KeyWords.CLOSING_TRY.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingCatch()
	 */
	@Override
	public String getClosingCatch() {
		return KeyWords.CLOSING_CATCH.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingForEach()
	 */
	@Override
	public String getClosingForEach() {
		return KeyWords.CLOSING_FOR_EACH.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingDo()
	 */
	@Override
	public String getClosingDo() {
		return KeyWords.CLOSING_DO.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingSwitch()
	 */
	@Override
	public String getClosingSwitch() {
		return KeyWords.CLOSING_SWITCH.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingEnclosed()
	 */
	@Override
	public String getClosingEnclosed() {
		return KeyWords.CLOSING_ENCLOSED.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingBlockStmt()
	 */
	@Override
	public String getClosingBlockStmt() {
		return KeyWords.CLOSING_BLOCK_STMT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingExpressionStmt()
	 */
	@Override
	public String getClosingExpressionStmt() {
		return KeyWords.CLOSING_EXPRESSION_STMT.toString();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw.IKeyWordProvider#getClosingCompilationUnit()
	 */
	@Override
	public String getClosingCompilationUnit() {
		return KeyWords.CLOSING_COMPILATION_UNIT.toString();
	}

	@Override
	public String getName() {
		return KeyWords.NAME.toString();
	}

	@Override
	public String getSimpleName() {
		return KeyWords.SIMPLE_NAME.toString();
	}

	@Override
	public String getLocalClassDeclarationStmt() {
		return KeyWords.LOCAL_CLASS_DECLARATION_STMT.toString();
	}

	@Override
	public String getArrayType() {
		return KeyWords.ARRAY_TYPE.toString();
	}

	@Override
	public String getArrayCreationLevel() {
		return KeyWords.ARRAY_CREATION_LEVEL.toString();
	}
	
	@Override
	public String getModuleDeclaration() {
		return KeyWords.MODULE_DECLARATION.toString();
	}

	@Override
	public String getModuleExportsStmt() {
		return KeyWords.MODULE_EXPORTS_STMT.toString();
	}

	@Override
	public String getModuleOpensStmt() {
		return KeyWords.MODULE_OPENS_STMT.toString();
	}

	@Override
	public String getModuleProvidesStmt() {
		return KeyWords.MODULE_PROVIDES_STMT.toString();
	}

	@Override
	public String getModuleRequiresStmt() {
		return KeyWords.MODULE_REQUIRES_STMT.toString();
	}

	@Override
	public String getModuleUsesStmt() {
		return KeyWords.MODULE_USES_STMT.toString();
	}

}
