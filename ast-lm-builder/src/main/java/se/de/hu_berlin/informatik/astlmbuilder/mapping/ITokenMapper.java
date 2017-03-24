package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import java.util.Collection;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.TypeParameter;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EmptyMemberDeclaration;
import com.github.javaparser.ast.body.EmptyTypeDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.MultiTypeParameter;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralMinValueExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralMinValueExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.QualifiedNameExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.TypeDeclarationStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.BodyStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ElseStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ExtendsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ImplementsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ThrowsStmt;

public interface ITokenMapper<T> extends IBasicMapper<T>, INodeMapper<T>, IKeyWordProvider<T> {

	/**
	 * Passes a black list of method names to the mapper.
	 * 
	 * @param aBL
	 *            a collection of method names that should be handled
	 *            differently
	 */
	default public void setPrivMethodBlackList(Collection<String> aBL) { throw new UnsupportedOperationException(); }
	
	/**
	 * @return
	 * the black list of method names; null if not set
	 */
	default public Collection<String> getPrivMethodBlackList() { throw new UnsupportedOperationException(); }

	/**
	 * Clears the black list of method names from this mapper
	 */
	default public void clearPrivMethodBlackList() { throw new UnsupportedOperationException(); }
	

	/**
	 * Returns a closing token for some block nodes
	 * 
	 * @param aNode
	 *            an AST node for which the closing token shall be generated
	 * @return closing token or null if the node has none
	 */
	public default T getClosingToken(Node aNode) {
		if (aNode == null) {
			return null;
		}

		if (aNode instanceof MethodDeclaration) {
			return getClosingMdec();
		} else if (aNode instanceof ConstructorDeclaration) {
			return getClosingCnstr();
		} else if (aNode instanceof IfStmt) {
			return getClosingIf();
		} else if (aNode instanceof WhileStmt) {
			return getClosingWhile();
		} else if (aNode instanceof ForStmt) {
			return getClosingFor();
		} else if (aNode instanceof TryStmt) {
			return getClosingTry();
		} else if (aNode instanceof CatchClause) {
			return getClosingCatch();
		} else if (aNode instanceof ForeachStmt) {
			return getClosingForEach();
		} else if (aNode instanceof DoStmt) {
			return getClosingDo();
		} else if (aNode instanceof SwitchStmt) {
			return getClosingSwitch();
		} else if (aNode instanceof EnclosedExpr) {
			return getClosingEnclosed();
		} else if (aNode instanceof BlockStmt) {
			return getClosingBlockStmt();
		} else if (aNode instanceof ExpressionStmt) {
			return getClosingExpressionStmt();
		} else if (aNode instanceof CompilationUnit) {
			return getClosingCompilationUnit();
		}

		return null;
	}

	@Override
	public default T getMappingForExtendsStmt(ExtendsStmt aNode, int aDepth) {
		return getExtendsStatement();
	}

	@Override
	public default T getMappingForImplementsStmt(ImplementsStmt aNode, int aDepth) {
		return getImplementsStatement();
	}

	@Override
	public default T getMappingForUnknownNode(Node aNode, int aDepth) {
		return getUnknown(aNode);
	}

	@Override
	public default T getMappingForCompilationUnit(CompilationUnit aNode, int aDepth) {
		return getCompilationUnit();
	}

	@Override
	public default T getMappingForMethodBodyStmt(BodyStmt aNode, int aDepth) {
		return getBodyStmt();
	}

	@Override
	public default T getMappingForThrowsStmt(ThrowsStmt aNode, int aDepth) {
		return getThrowsStatement();
	}

	@Override
	public default T getMappingForMemberValuePair(MemberValuePair aNode, int aDepth) {
		return getMemberValuePair();
	}

	@Override
	public default T getMappingForVariableDeclaratorId(VariableDeclaratorId aNode, int aDepth) {
		return getVariableDeclarationId();
	}

	@Override
	public default T getMappingForVariableDeclarator(VariableDeclarator aNode, int aDepth) {
		return getVariableDeclaration();
	}

	@Override
	public default T getMappingForCatchClause(CatchClause aNode, int aDepth) {
		return getCatchClauseStatement();
	}

	@Override
	public default T getMappingForTypeParameter(TypeParameter aNode, int aDepth) {
		return getTypePar();
	}

	@Override
	public default T getMappingForImportDeclaration(ImportDeclaration aNode, int aDepth) {
		return getImportDeclaration();
	}

	@Override
	public default T getMappingForPackageDeclaration(PackageDeclaration aNode, int aDepth) {
		return getPackageDeclaration();
	}

	@Override
	public default T getMappingForMultiTypeParameter(MultiTypeParameter aNode, int aDepth) {
		return getMultiTypeParameter();
	}

	@Override
	public default T getMappingForParameter(Parameter aNode, int aDepth) {
		return getParameter();
	}

	@Override
	public default T getMappingForJavadocComment(JavadocComment aNode, int aDepth) {
		return getJavadocComment();
	}

	@Override
	public default T getMappingForBlockComment(BlockComment aNode, int aDepth) {
		return getBlockComment();
	}

	@Override
	public default T getMappingForLineComment(LineComment aNode, int aDepth) {
		return getLineComment();
	}

	@Override
	public default T getMappingForEnumDeclaration(EnumDeclaration aNode, int aDepth) {
		return getEnumDeclaration();
	}

	@Override
	public default T getMappingForEmptyTypeDeclaration(EmptyTypeDeclaration aNode, int aDepth) {
		return getEmptyTypeDeclaration();
	}

	@Override
	public default T getMappingForClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration aNode, int aDepth) {
		return getClassOrInterfaceDeclaration();
	}

	@Override
	public default T getMappingForAnnotationDeclaration(AnnotationDeclaration aNode, int aDepth) {
		return getAnnotationDeclaration();
	}

	@Override
	public default T getMappingForEmptyMemberDeclaration(EmptyMemberDeclaration aNode, int aDepth) {
		return getEmptyMemberDeclaration();
	}

	@Override
	public default T getMappingForAnnotationMemberDeclaration(AnnotationMemberDeclaration aNode, int aDepth) {
		return getAnnotationMemberDeclaration();
	}

	@Override
	public default T getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode, int aDepth) {
		return getEnumConstantDeclaration();
	}

	@Override
	public default T getMappingForMethodDeclaration(MethodDeclaration aNode, int aDepth) {
		return getMethodDeclaration();
	}

	@Override
	public default T getMappingForFieldDeclaration(FieldDeclaration aNode, int aDepth) {
		return getFieldDeclaration();
	}

	@Override
	public default T getMappingForInitializerDeclaration(InitializerDeclaration aNode, int aDepth) {
		return getInitializerDeclaration();
	}

	@Override
	public default T getMappingForConstructorDeclaration(ConstructorDeclaration aNode, int aDepth) {
		return getConstructorDeclaration();
	}

	@Override
	public default T getMappingForWhileStmt(WhileStmt aNode, int aDepth) {
		return getWhileStatement();
	}

	@Override
	public default T getMappingForTypeDeclarationStmt(TypeDeclarationStmt aNode, int aDepth) {
		return getTypeDeclarationStatement();
	}

	@Override
	public default T getMappingForTryStmt(TryStmt aNode, int aDepth) {
		return getTryStatement();
	}

	@Override
	public default T getMappingForThrowStmt(ThrowStmt aNode, int aDepth) {
		return getThrowStatement();
	}

	@Override
	public default T getMappingForSynchronizedStmt(SynchronizedStmt aNode, int aDepth) {
		return getSynchronizedStatement();
	}

	@Override
	public default T getMappingForSwitchStmt(SwitchStmt aNode, int aDepth) {
		return getSwitchStatement();
	}

	@Override
	public default T getMappingForSwitchEntryStmt(SwitchEntryStmt aNode, int aDepth) {
		return getSwitchEntryStatement();
	}

	@Override
	public default T getMappingForReturnStmt(ReturnStmt aNode, int aDepth) {
		return getReturnStatement();
	}

	@Override
	public default T getMappingForLabeledStmt(LabeledStmt aNode, int aDepth) {
		return getLabeledStatement();
	}

	@Override
	public default T getMappingForElseStmt(ElseStmt aNode, int aDepth) {
		return getElseStatement();
	}

	@Override
	public default T getMappingForIfStmt(IfStmt aNode, int aDepth) {
		return getIfStatement();
	}

	@Override
	public default T getMappingForForStmt(ForStmt aNode, int aDepth) {
		return getForStatement();
	}

	@Override
	public default T getMappingForForeachStmt(ForeachStmt aNode, int aDepth) {
		return getForEachStatement();
	}

	@Override
	public default T getMappingForExpressionStmt(ExpressionStmt aNode, int aDepth) {
		return getExpressionStatement();
	}

	@Override
	public default T getMappingForExplicitConstructorInvocationStmt(ExplicitConstructorInvocationStmt aNode,
			int aDepth) {
		// this is not the explicit constructor invocation statement?
		return getExplicitConstructorStatement();
	}

	@Override
	public default T getMappingForEmptyStmt(EmptyStmt aNode, int aDepth) {
		return getEmptyStatement();
	}

	@Override
	public default T getMappingForDoStmt(DoStmt aNode, int aDepth) {
		return getDoStatement();
	}

	@Override
	public default T getMappingForContinueStmt(ContinueStmt aNode, int aDepth) {
		return getContinueStatement();
	}

	@Override
	public default T getMappingForBreakStmt(BreakStmt aNode, int aDepth) {
		return getBreak();
	}

	@Override
	public default T getMappingForBlockStmt(BlockStmt aNode, int aDepth) {
		return getBlockStatement();
	}

	@Override
	public default T getMappingForAssertStmt(AssertStmt aNode, int aDepth) {
		return getAssertStmt();
	}

	@Override
	public default T getMappingForWildcardType(WildcardType aNode, int aDepth) {
		return getTypeWildcard();
	}

	@Override
	public default T getMappingForVoidType(VoidType aNode, int aDepth) {
		return getTypeVoid();
	}

	@Override
	public default T getMappingForUnknownType(UnknownType aNode, int aDepth) {
		return getTypeUnknown();
	}

	@Override
	public default T getMappingForUnionType(UnionType aNode, int aDepth) {
		return getTypeUnion();
	}

	@Override
	public default T getMappingForReferenceType(ReferenceType aNode, int aDepth) {
		return getTypeReference();
	}

	@Override
	public default T getMappingForPrimitiveType(PrimitiveType aNode, int aDepth) {
		return getTypePrimitive();
	}

	@Override
	public default T getMappingForIntersectionType(IntersectionType aNode, int aDepth) {
		return getTypeIntersection();
	}

	@Override
	public default T getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode, int aDepth) {
		return getClassOrInterfaceType();
	}

	@Override
	public default T getMappingForSingleMemberAnnotationExpr(SingleMemberAnnotationExpr aNode, int aDepth) {
		return getSingleMemberAnnotationExpression();
	}

	@Override
	public default T getMappingForNormalAnnotationExpr(NormalAnnotationExpr aNode, int aDepth) {
		return getNormalAnnotationExpression();
	}

	@Override
	public default T getMappingForMarkerAnnotationExpr(MarkerAnnotationExpr aNode, int aDepth) {
		return getMarkerAnnotationExpression();
	}

	@Override
	public default T getMappingForNameExpr(NameExpr aNode, int aDepth) {
		return getNameExpression();
	}

	@Override
	public default T getMappingForQualifiedNameExpr(QualifiedNameExpr aNode, int aDepth) {
		return getQualifiedNameExpression();
	}

	@Override
	public default T getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode, int aDepth) {
		return getVariableDeclarationExpression();
	}

	@Override
	public default T getMappingForTypeExpr(TypeExpr aNode, int aDepth) {
		return getTypeExpression();
	}

	@Override
	public default T getMappingForSuperExpr(SuperExpr aNode, int aDepth) {
		return getSuperExpression();
	}

	@Override
	public default T getMappingForUnaryExpr(UnaryExpr aNode, int aDepth) {
		return getUnaryExpression();
	}

	@Override
	public default T getMappingForObjectCreationExpr(ObjectCreationExpr aNode, int aDepth) {
		return getObjCreateExpression();
	}

	@Override
	public default T getMappingForEnclosedExpr(EnclosedExpr aNode, int aDepth) {
		return getEnclosedExpression();
	}

	@Override
	public default T getMappingForThisExpr(ThisExpr aNode, int aDepth) {
		return getThisExpression();
	}

	@Override
	public default T getMappingForMethodReferenceExpr(MethodReferenceExpr aNode, int aDepth) {
		return getMethodReferenceExpression();
	}

	@Override
	public default T getMappingForMethodCallExpr(MethodCallExpr aNode, int aDepth) {
		return getMethodCallExpression();
	}

	@Override
	public default T getMappingForLambdaExpr(LambdaExpr aNode, int aDepth) {
		return getLambdaExpression();
	}

	@Override
	public default T getMappingForInstanceOfExpr(InstanceOfExpr aNode, int aDepth) {
		return getInstanceofExpression();
	}

	@Override
	public default T getMappingForFieldAccessExpr(FieldAccessExpr aNode, int aDepth) {
		return getFieldAccessExpression();
	}

	@Override
	public default T getMappingForConditionalExpr(ConditionalExpr aNode, int aDepth) {
		return getConditionalExpression();
	}

	@Override
	public default T getMappingForClassExpr(ClassExpr aNode, int aDepth) {
		return getClassExpression();
	}

	@Override
	public default T getMappingForCastExpr(CastExpr aNode, int aDepth) {
		return getCastExpression();
	}

	@Override
	public default T getMappingForBinaryExpr(BinaryExpr aNode, int aDepth) {
		return getBinaryExpression();
	}

	@Override
	public default T getMappingForAssignExpr(AssignExpr aNode, int aDepth) {
		return getAssignExpression();
	}

	@Override
	public default T getMappingForArrayInitializerExpr(ArrayInitializerExpr aNode, int aDepth) {
		return getArrayInitExpression();
	}

	@Override
	public default T getMappingForArrayCreationExpr(ArrayCreationExpr aNode, int aDepth) {
		return getArrayCreateExpression();
	}

	@Override
	public default T getMappingForArrayAccessExpr(ArrayAccessExpr aNode, int aDepth) {
		return getArrayAccessExpression();
	}

	@Override
	public default T getMappingForStringLiteralExpr(StringLiteralExpr aNode, int aDepth) {
		return getStringLiteralExpression();
	}

	@Override
	public default T getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode, int aDepth) {
		return getDoubleLiteralExpression();
	}

	@Override
	public default T getMappingForLongLiteralExpr(LongLiteralExpr aNode, int aDepth) {
		return getLongLiteralExpression();
	}

	@Override
	public default T getMappingForLongLiteralMinValueExpr(LongLiteralMinValueExpr aNode, int aDepth) {
		return getLongLiteralMinValueExpression();
	}

	@Override
	public default T getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode, int aDepth) {
		return getIntegerLiteralExpression();
	}

	@Override
	public default T getMappingForIntegerLiteralMinValueExpr(IntegerLiteralMinValueExpr aNode, int aDepth) {
		return getIntegerLiteralMinValueExpression();
	}

	@Override
	public default T getMappingForCharLiteralExpr(CharLiteralExpr aNode, int aDepth) {
		return getCharLiteralExpression();
	}

	@Override
	public default T getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode, int aDepth) {
		return getBooleanLiteralExpression();
	}

	@Override
	public default T getMappingForNullLiteralExpr(NullLiteralExpr aNode, int aDepth) {
		return getNullLiteralExpression();
	}

}
