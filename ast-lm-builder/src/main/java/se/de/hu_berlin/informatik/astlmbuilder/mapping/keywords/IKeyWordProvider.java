package se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords;

import com.github.javaparser.ast.Node;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.IModifierHandler;

/**
 * Interface to get keywords for token generation
 */
public interface IKeyWordProvider<T> extends IModifierHandler, IBasicKeyWords {

	
	
	public T getMethodIdentifier();

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

//	public T getEmptyMemberDeclaration();

//	public T getEmptyTypeDeclaration();

	public T getWhileStatement();

	public T getTryStatement();

	public T getThrowStatement();

	public T getThrowsStatement();

	public T getSynchronizedStatement();

	public T getSwitchStatement();

	public T getSwitchEntryStatement();

	public T getReturnStatement();

	public T getLabeledStatement();

	public T getIfStatement();

	public T getElseStatement();

	public T getForStatement();

	public T getForEachStatement();

	public T getExpressionStatement();

	public T getExplicitConstructorStatement();

//	public T getEmptyStatement();

	public T getDoStatement();

	public T getContinueStatement();

	public T getCatchClauseStatement();

	public T getBlockStatement();

	//public T getVariableDeclarationId();

	public T getVariableDeclarationExpression();

	public T getTypeExpression();

	public T getSuperExpression();

//	public T getQualifiedNameExpression();

	public T getNullLiteralExpression();

	public T getMethodReferenceExpression();

	public T getBodyStmt();

//	public T getLongLiteralMinValueExpression();

	public T getLambdaExpression();

//	public T getIntegerLiteralMinValueExpression();

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

//	public T getClassDeclaration();
//
//	public T getInterfaceDeclaration();

	public T getExtendsStatement();

	public T getImplementsStatement();

	public T getMethodDeclaration();

	public T getBinaryExpression();

	public T getUnaryExpression();

	public T getMethodCallExpression();

	public T getLocalMethodCallExpression();

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

	//public T getMultiTypeParameter();

	public T getEnclosedExpression();

	public T getAssertStmt();

	public T getMemberValuePair();

//	public T getTypeDeclarationStatement();

//	public T getTypeReference();

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
	public T getModuleStmt();
	
	public T getUnknown(Node aNode);
	

	public T getEndSuffix();

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