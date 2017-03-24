package se.de.hu_berlin.informatik.astlmbuilder.mapping;

/**
 * Interface to get keywords for token generation
 */
public interface IKeyWordProvider {

	public String getTypeParametersStart();

	public String getCompilationUnit();

	public String getLineComment();

	public String getBlockComment();

	public String getJavadocComment();

	public String getConstructorDeclaration();

	public String getInitializerDeclaration();

	public String getEnumConstantDeclaration();

	public String getVariableDeclaration();

	public String getEnumDeclaration();

	public String getAnnotationDeclaration();

	public String getAnnotationMemberDeclaration();

	public String getEmptyMemberDeclaration();

	public String getEmptyTypeDeclaration();

	public String getWhileStatement();

	public String getTryStatement();

	public String getThrowStatement();

	public String getThrowsStatement();

	public String getSynchronizedStatement();

	public String getSwitchStatement();

	public String getSwitchEntryStatement();

	public String getReturnStatement();

	public String getLabeledStatement();

	public String getIfStatement();

	public String getElseStatement();

	public String getForStatement();

	public String getForEachStatement();

	public String getExpressionStatement();

	public String getExplicitConstructorStatement();

	public String getEmptyStatement();

	public String getDoStatement();

	public String getContinueStatement();

	public String getCatchClauseStatement();

	public String getBlockStatement();

	public String getVariableDeclarationId();

	public String getVariableDeclarationExpression();

	public String getTypeExpression();

	public String getSuperExpression();

	public String getQualifiedNameExpression();

	public String getNullLiteralExpression();

	public String getMethodReferenceExpression();

	public String getBodyStmt();

	public String getLongLiteralMinValueExpression();

	public String getLambdaExpression();

	public String getIntegerLiteralMinValueExpression();

	public String getInstanceofExpression();

	public String getFieldAccessExpression();

	public String getConditionalExpression();

	public String getClassExpression();

	public String getCastExpression();

	public String getAssignExpression();

	public String getArrayInitExpression();

	public String getArrayCreateExpression();

	public String getArrayAccessExpression();

	public String getPackageDeclaration();

	public String getImportDeclaration();

	public String getFieldDeclaration();

	public String getClassOrInterfaceType();

	public String getClassOrInterfaceDeclaration();

	public String getClassDeclaration();

	public String getInterfaceDeclaration();

	public String getExtendsStatement();

	public String getImplementsStatement();

	public String getMethodDeclaration();

	public String getBinaryExpression();

	public String getUnaryExpression();

	public String getMethodCallExpression();

	public String getPrivateMethodCallExpression();

	public String getNameExpression();

	public String getIntegerLiteralExpression();

	public String getDoubleLiteralExpression();

	public String getStringLiteralExpression();

	public String getBooleanLiteralExpression();

	public String getCharLiteralExpression();

	public String getLongLiteralExpression();

	public String getThisExpression();

	public String getBreak();

	public String getObjCreateExpression();

	public String getMarkerAnnotationExpression();

	public String getNormalAnnotationExpression();

	public String getSingleMemberAnnotationExpression();

	public String getParameter();

	public String getMultiTypeParameter();

	public String getEnclosedExpression();

	public String getAssertStmt();

	public String getMemberValuePair();

	public String getTypeDeclarationStatement();

	public String getTypeReference();

	public String getTypePrimitive();

	public String getTypeUnion();

	public String getTypeIntersection();

	public String getTypePar();

	public String getTypeWildcard();

	public String getTypeVoid();

	public String getTypeUnknown();

	public String getUnknown();

	public String getEndSuffix();

	public String getClosingMdec();

	public String getClosingCnstr();

	public String getClosingIf();

	public String getClosingWhile();

	public String getClosingFor();

	public String getClosingTry();

	public String getClosingCatch();

	public String getClosingForEach();

	public String getClosingDo();

	public String getClosingSwitch();

	public String getClosingEnclosed();

	public String getClosingBlockStmt();

	public String getClosingExpressionStmt();

	public String getClosingCompilationUnit();

	public String getPriv();

	public String getPub();

	public String getProt();

	public String getAbs();

	public String getStatic();

	public String getFinal();

	public String getNative();

	public String getStrictfp();

	public String getSync();

	public String getTrans();

	public String getVolatile();

}