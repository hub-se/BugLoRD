package se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords;

import com.github.javaparser.ast.Node;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.IModifierHandler;

/**
 * Interface to get keywords for token generation
 */
public interface IKeyWordProvider<T> extends IModifierHandler {

	default public T getTypeParametersStart() { throw new UnsupportedOperationException(); }

	default public T getCompilationUnit() { throw new UnsupportedOperationException(); }

	default public T getLineComment() { throw new UnsupportedOperationException(); }

	default public T getBlockComment() { throw new UnsupportedOperationException(); }

	default public T getJavadocComment() { throw new UnsupportedOperationException(); }

	default public T getConstructorDeclaration() { throw new UnsupportedOperationException(); }

	default public T getInitializerDeclaration() { throw new UnsupportedOperationException(); }

	default public T getEnumConstantDeclaration() { throw new UnsupportedOperationException(); }

	default public T getVariableDeclaration() { throw new UnsupportedOperationException(); }

	default public T getEnumDeclaration() { throw new UnsupportedOperationException(); }

	default public T getAnnotationDeclaration() { throw new UnsupportedOperationException(); }

	default public T getAnnotationMemberDeclaration() { throw new UnsupportedOperationException(); }

//	default public T getEmptyMemberDeclaration() { throw new UnsupportedOperationException(); }

//	default public T getEmptyTypeDeclaration() { throw new UnsupportedOperationException(); }

	default public T getWhileStatement() { throw new UnsupportedOperationException(); }

	default public T getTryStatement() { throw new UnsupportedOperationException(); }

	default public T getThrowStatement() { throw new UnsupportedOperationException(); }

	default public T getThrowsStatement() { throw new UnsupportedOperationException(); }

	default public T getSynchronizedStatement() { throw new UnsupportedOperationException(); }

	default public T getSwitchStatement() { throw new UnsupportedOperationException(); }

	default public T getSwitchEntryStatement() { throw new UnsupportedOperationException(); }

	default public T getReturnStatement() { throw new UnsupportedOperationException(); }

	default public T getLabeledStatement() { throw new UnsupportedOperationException(); }

	default public T getIfStatement() { throw new UnsupportedOperationException(); }

	default public T getElseStatement() { throw new UnsupportedOperationException(); }

	default public T getForStatement() { throw new UnsupportedOperationException(); }

	default public T getForEachStatement() { throw new UnsupportedOperationException(); }

	default public T getExpressionStatement() { throw new UnsupportedOperationException(); }

	default public T getExplicitConstructorStatement() { throw new UnsupportedOperationException(); }

//	default public T getEmptyStatement() { throw new UnsupportedOperationException(); }

	default public T getDoStatement() { throw new UnsupportedOperationException(); }

	default public T getContinueStatement() { throw new UnsupportedOperationException(); }

	default public T getCatchClauseStatement() { throw new UnsupportedOperationException(); }

	default public T getBlockStatement() { throw new UnsupportedOperationException(); }

	//default public T getVariableDeclarationId() { throw new UnsupportedOperationException(); }

	default public T getVariableDeclarationExpression() { throw new UnsupportedOperationException(); }

	default public T getTypeExpression() { throw new UnsupportedOperationException(); }

	default public T getSuperExpression() { throw new UnsupportedOperationException(); }

//	default public T getQualifiedNameExpression() { throw new UnsupportedOperationException(); }

	default public T getNullLiteralExpression() { throw new UnsupportedOperationException(); }

	default public T getMethodReferenceExpression() { throw new UnsupportedOperationException(); }

	default public T getBodyStmt() { throw new UnsupportedOperationException(); }

//	default public T getLongLiteralMinValueExpression() { throw new UnsupportedOperationException(); }

	default public T getLambdaExpression() { throw new UnsupportedOperationException(); }

//	default public T getIntegerLiteralMinValueExpression() { throw new UnsupportedOperationException(); }

	default public T getInstanceofExpression() { throw new UnsupportedOperationException(); }

	default public T getFieldAccessExpression() { throw new UnsupportedOperationException(); }

	default public T getConditionalExpression() { throw new UnsupportedOperationException(); }

	default public T getClassExpression() { throw new UnsupportedOperationException(); }

	default public T getCastExpression() { throw new UnsupportedOperationException(); }

	default public T getAssignExpression() { throw new UnsupportedOperationException(); }

	default public T getArrayInitExpression() { throw new UnsupportedOperationException(); }

	default public T getArrayCreateExpression() { throw new UnsupportedOperationException(); }

	default public T getArrayAccessExpression() { throw new UnsupportedOperationException(); }

	default public T getPackageDeclaration() { throw new UnsupportedOperationException(); }

	default public T getImportDeclaration() { throw new UnsupportedOperationException(); }

	default public T getFieldDeclaration() { throw new UnsupportedOperationException(); }

	default public T getClassOrInterfaceType() { throw new UnsupportedOperationException(); }

	default public T getClassOrInterfaceDeclaration() { throw new UnsupportedOperationException(); }

	default public T getClassDeclaration() { throw new UnsupportedOperationException(); }

	default public T getInterfaceDeclaration() { throw new UnsupportedOperationException(); }

	default public T getExtendsStatement() { throw new UnsupportedOperationException(); }

	default public T getImplementsStatement() { throw new UnsupportedOperationException(); }

	default public T getMethodDeclaration() { throw new UnsupportedOperationException(); }

	default public T getBinaryExpression() { throw new UnsupportedOperationException(); }

	default public T getUnaryExpression() { throw new UnsupportedOperationException(); }

	default public T getMethodCallExpression() { throw new UnsupportedOperationException(); }

	default public T getPrivateMethodCallExpression() { throw new UnsupportedOperationException(); }

	default public T getNameExpression() { throw new UnsupportedOperationException(); }

	default public T getIntegerLiteralExpression() { throw new UnsupportedOperationException(); }

	default public T getDoubleLiteralExpression() { throw new UnsupportedOperationException(); }

	default public T getStringLiteralExpression() { throw new UnsupportedOperationException(); }

	default public T getBooleanLiteralExpression() { throw new UnsupportedOperationException(); }

	default public T getCharLiteralExpression() { throw new UnsupportedOperationException(); }

	default public T getLongLiteralExpression() { throw new UnsupportedOperationException(); }

	default public T getThisExpression() { throw new UnsupportedOperationException(); }

	default public T getBreak() { throw new UnsupportedOperationException(); }

	default public T getObjCreateExpression() { throw new UnsupportedOperationException(); }

	default public T getMarkerAnnotationExpression() { throw new UnsupportedOperationException(); }

	default public T getNormalAnnotationExpression() { throw new UnsupportedOperationException(); }

	default public T getSingleMemberAnnotationExpression() { throw new UnsupportedOperationException(); }

	default public T getParameter() { throw new UnsupportedOperationException(); }

	//default public T getMultiTypeParameter() { throw new UnsupportedOperationException(); }

	default public T getEnclosedExpression() { throw new UnsupportedOperationException(); }

	default public T getAssertStmt() { throw new UnsupportedOperationException(); }

	default public T getMemberValuePair() { throw new UnsupportedOperationException(); }

//	default public T getTypeDeclarationStatement() { throw new UnsupportedOperationException(); }

//	default public T getTypeReference() { throw new UnsupportedOperationException(); }

	default public T getTypePrimitive() { throw new UnsupportedOperationException(); }

	default public T getTypeUnion() { throw new UnsupportedOperationException(); }
	
	default public T getTypePar() { throw new UnsupportedOperationException(); }

	default public T getTypeIntersection() { throw new UnsupportedOperationException(); }

	default public T getTypeWildcard() { throw new UnsupportedOperationException(); }

	default public T getTypeVoid() { throw new UnsupportedOperationException(); }

	default public T getTypeUnknown() { throw new UnsupportedOperationException(); }

	default public T getName() { throw new UnsupportedOperationException(); }
	default public T getSimpleName() { throw new UnsupportedOperationException(); }
	default public T getLocalClassDeclarationStmt() { throw new UnsupportedOperationException(); }
	default public T getArrayType() { throw new UnsupportedOperationException(); }
	default public T getArrayCreationLevel() { throw new UnsupportedOperationException(); }
	default public T getModuleDeclaration() { throw new UnsupportedOperationException(); }
	default public T getModuleStmt() { throw new UnsupportedOperationException(); }
	
	default public T getUnknown(Node aNode) { throw new UnsupportedOperationException(); }
	

	default public T getEndSuffix() { throw new UnsupportedOperationException(); }

	default public T getClosingMdec() { throw new UnsupportedOperationException(); }

	default public T getClosingCnstr() { throw new UnsupportedOperationException(); }

	default public T getClosingIf() { throw new UnsupportedOperationException(); }

	default public T getClosingWhile() { throw new UnsupportedOperationException(); }

	default public T getClosingFor() { throw new UnsupportedOperationException(); }

	default public T getClosingTry() { throw new UnsupportedOperationException(); }

	default public T getClosingCatch() { throw new UnsupportedOperationException(); }

	default public T getClosingForEach() { throw new UnsupportedOperationException(); }

	default public T getClosingDo() { throw new UnsupportedOperationException(); }

	default public T getClosingSwitch() { throw new UnsupportedOperationException(); }

	default public T getClosingEnclosed() { throw new UnsupportedOperationException(); }

	default public T getClosingBlockStmt() { throw new UnsupportedOperationException(); }

	default public T getClosingExpressionStmt() { throw new UnsupportedOperationException(); }

	default public T getClosingCompilationUnit() { throw new UnsupportedOperationException(); }

}