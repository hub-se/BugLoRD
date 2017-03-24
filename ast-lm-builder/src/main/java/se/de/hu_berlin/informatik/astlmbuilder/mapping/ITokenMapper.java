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

public interface ITokenMapper extends IBasicMapper, INodeMapper, IKeyWordProvider {

	/**
	 * Returns a closing token for some block nodes
	 * 
	 * @param aNode
	 *            an AST node for which the closing token shall be generated
	 * @return closing token or null if the node has none
	 */
	public default String getClosingToken(Node aNode) {
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

	// this is only relevant for the creation of the abstraction tokens
	// TODO add reasonable description here
	public void setPrivMethodBlackList(Collection<String> aBL);

	public void clearPrivMethodBlackList();

	@Override
	public default String getMappingForExtendsStmt(ExtendsStmt aNode, int aDepth) {
		return getExtendsStatement();
	}

	@Override
	public default String getMappingForImplementsStmt(ImplementsStmt aNode, int aDepth) {
		return getImplementsStatement();
	}

	@Override
	public default String getMappingForUnknownNode(Node aNode, int aDepth) {
		if (aNode != null) {
			return getUnknown() + GROUP_START + aNode.getClass() + GROUP_END;
		} else {
			return getUnknown();
		}
	}

	@Override
	public default String getMappingForCompilationUnit(CompilationUnit aNode, int aDepth) {
		return getCompilationUnit();
	}

	@Override
	public default String getMappingForMethodBodyStmt(BodyStmt aNode, int aDepth) {
		return getBodyStmt();
	}

	@Override
	public default String getMappingForThrowsStmt(ThrowsStmt aNode, int aDepth) {
		return getThrowsStatement();
	}

	@Override
	public default String getMappingForMemberValuePair(MemberValuePair aNode, int aDepth) {
		return getMemberValuePair();
	}

	@Override
	public default String getMappingForVariableDeclaratorId(VariableDeclaratorId aNode, int aDepth) {
		return getVariableDeclarationId();
	}

	@Override
	public default String getMappingForVariableDeclarator(VariableDeclarator aNode, int aDepth) {
		return getVariableDeclaration();
	}

	@Override
	public default String getMappingForCatchClause(CatchClause aNode, int aDepth) {
		return getCatchClauseStatement();
	}

	@Override
	public default String getMappingForTypeParameter(TypeParameter aNode, int aDepth) {
		return getTypePar();
	}

	@Override
	public default String getMappingForImportDeclaration(ImportDeclaration aNode, int aDepth) {
		return getImportDeclaration();
	}

	@Override
	public default String getMappingForPackageDeclaration(PackageDeclaration aNode, int aDepth) {
		return getPackageDeclaration();
	}

	@Override
	public default String getMappingForMultiTypeParameter(MultiTypeParameter aNode, int aDepth) {
		return getMultiTypeParameter();
	}

	@Override
	public default String getMappingForParameter(Parameter aNode, int aDepth) {
		return getParameter();
	}

	@Override
	public default String getMappingForJavadocComment(JavadocComment aNode, int aDepth) {
		return getJavadocComment();
	}

	@Override
	public default String getMappingForBlockComment(BlockComment aNode, int aDepth) {
		return getBlockComment();
	}

	@Override
	public default String getMappingForLineComment(LineComment aNode, int aDepth) {
		return getLineComment();
	}

	@Override
	public default String getMappingForEnumDeclaration(EnumDeclaration aNode, int aDepth) {
		return getEnumDeclaration();
	}

	@Override
	public default String getMappingForEmptyTypeDeclaration(EmptyTypeDeclaration aNode, int aDepth) {
		return getEmptyTypeDeclaration();
	}

	@Override
	public default String getMappingForClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration aNode, int aDepth) {
		return getClassOrInterfaceDeclaration();
	}

	@Override
	public default String getMappingForAnnotationDeclaration(AnnotationDeclaration aNode, int aDepth) {
		return getAnnotationDeclaration();
	}

	@Override
	public default String getMappingForEmptyMemberDeclaration(EmptyMemberDeclaration aNode, int aDepth) {
		return getEmptyMemberDeclaration();
	}

	@Override
	public default String getMappingForAnnotationMemberDeclaration(AnnotationMemberDeclaration aNode, int aDepth) {
		return getAnnotationMemberDeclaration();
	}

	@Override
	public default String getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode, int aDepth) {
		return getEnumConstantDeclaration();
	}

	@Override
	public default String getMappingForMethodDeclaration(MethodDeclaration aNode, int aDepth) {
		return getMethodDeclaration();
	}

	@Override
	public default String getMappingForFieldDeclaration(FieldDeclaration aNode, int aDepth) {
		return getFieldDeclaration();
	}

	@Override
	public default String getMappingForInitializerDeclaration(InitializerDeclaration aNode, int aDepth) {
		return getInitializerDeclaration();
	}

	@Override
	public default String getMappingForConstructorDeclaration(ConstructorDeclaration aNode, int aDepth) {
		return getConstructorDeclaration();
	}

	@Override
	public default String getMappingForWhileStmt(WhileStmt aNode, int aDepth) {
		return getWhileStatement();
	}

	@Override
	public default String getMappingForTypeDeclarationStmt(TypeDeclarationStmt aNode, int aDepth) {
		return getTypeDeclarationStatement();
	}

	@Override
	public default String getMappingForTryStmt(TryStmt aNode, int aDepth) {
		return getTryStatement();
	}

	@Override
	public default String getMappingForThrowStmt(ThrowStmt aNode, int aDepth) {
		return getThrowStatement();
	}

	@Override
	public default String getMappingForSynchronizedStmt(SynchronizedStmt aNode, int aDepth) {
		return getSynchronizedStatement();
	}

	@Override
	public default String getMappingForSwitchStmt(SwitchStmt aNode, int aDepth) {
		return getSwitchStatement();
	}

	@Override
	public default String getMappingForSwitchEntryStmt(SwitchEntryStmt aNode, int aDepth) {
		return getSwitchEntryStatement();
	}

	@Override
	public default String getMappingForReturnStmt(ReturnStmt aNode, int aDepth) {
		return getReturnStatement();
	}

	@Override
	public default String getMappingForLabeledStmt(LabeledStmt aNode, int aDepth) {
		return getLabeledStatement();
	}

	@Override
	public default String getMappingForElseStmt(ElseStmt aNode, int aDepth) {
		return getElseStatement();
	}

	@Override
	public default String getMappingForIfStmt(IfStmt aNode, int aDepth) {
		return getIfStatement();
	}

	@Override
	public default String getMappingForForStmt(ForStmt aNode, int aDepth) {
		return getForStatement();
	}

	@Override
	public default String getMappingForForeachStmt(ForeachStmt aNode, int aDepth) {
		return getForEachStatement();
	}

	@Override
	public default String getMappingForExpressionStmt(ExpressionStmt aNode, int aDepth) {
		return getExpressionStatement();
	}

	@Override
	public default String getMappingForExplicitConstructorInvocationStmt(ExplicitConstructorInvocationStmt aNode,
			int aDepth) {
		// this is not the explicit constructor invocation statement?
		return getExplicitConstructorStatement();
	}

	@Override
	public default String getMappingForEmptyStmt(EmptyStmt aNode, int aDepth) {
		return getEmptyStatement();
	}

	@Override
	public default String getMappingForDoStmt(DoStmt aNode, int aDepth) {
		return getDoStatement();
	}

	@Override
	public default String getMappingForContinueStmt(ContinueStmt aNode, int aDepth) {
		return getContinueStatement();
	}

	@Override
	public default String getMappingForBreakStmt(BreakStmt aNode, int aDepth) {
		return getBreak();
	}

	@Override
	public default String getMappingForBlockStmt(BlockStmt aNode, int aDepth) {
		return getBlockStatement();
	}

	@Override
	public default String getMappingForAssertStmt(AssertStmt aNode, int aDepth) {
		return getAssertStmt();
	}

	@Override
	public default String getMappingForWildcardType(WildcardType aNode, int aDepth) {
		return getTypeWildcard();
	}

	@Override
	public default String getMappingForVoidType(VoidType aNode, int aDepth) {
		return getTypeVoid();
	}

	@Override
	public default String getMappingForUnknownType(UnknownType aNode, int aDepth) {
		return getTypeUnknown();
	}

	@Override
	public default String getMappingForUnionType(UnionType aNode, int aDepth) {
		return getTypeUnion();
	}

	@Override
	public default String getMappingForReferenceType(ReferenceType aNode, int aDepth) {
		return getTypeReference();
	}

	@Override
	public default String getMappingForPrimitiveType(PrimitiveType aNode, int aDepth) {
		return getTypePrimitive();
	}

	@Override
	public default String getMappingForIntersectionType(IntersectionType aNode, int aDepth) {
		return getTypeIntersection();
	}

	@Override
	public default String getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode, int aDepth) {
		return getClassOrInterfaceType();
	}

	@Override
	public default String getMappingForSingleMemberAnnotationExpr(SingleMemberAnnotationExpr aNode, int aDepth) {
		return getSingleMemberAnnotationExpression();
	}

	@Override
	public default String getMappingForNormalAnnotationExpr(NormalAnnotationExpr aNode, int aDepth) {
		return getNormalAnnotationExpression();
	}

	@Override
	public default String getMappingForMarkerAnnotationExpr(MarkerAnnotationExpr aNode, int aDepth) {
		return getMarkerAnnotationExpression();
	}

	@Override
	public default String getMappingForNameExpr(NameExpr aNode, int aDepth) {
		return getNameExpression();
	}

	@Override
	public default String getMappingForQualifiedNameExpr(QualifiedNameExpr aNode, int aDepth) {
		return getQualifiedNameExpression();
	}

	@Override
	public default String getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode, int aDepth) {
		return getVariableDeclarationExpression();
	}

	@Override
	public default String getMappingForTypeExpr(TypeExpr aNode, int aDepth) {
		return getTypeExpression();
	}

	@Override
	public default String getMappingForSuperExpr(SuperExpr aNode, int aDepth) {
		return getSuperExpression();
	}

	@Override
	public default String getMappingForUnaryExpr(UnaryExpr aNode, int aDepth) {
		return getUnaryExpression();
	}

	@Override
	public default String getMappingForObjectCreationExpr(ObjectCreationExpr aNode, int aDepth) {
		return getObjCreateExpression();
	}

	@Override
	public default String getMappingForEnclosedExpr(EnclosedExpr aNode, int aDepth) {
		return getEnclosedExpression();
	}

	@Override
	public default String getMappingForThisExpr(ThisExpr aNode, int aDepth) {
		return getThisExpression();
	}

	@Override
	public default String getMappingForMethodReferenceExpr(MethodReferenceExpr aNode, int aDepth) {
		return getMethodReferenceExpression();
	}

	@Override
	public default String getMappingForMethodCallExpr(MethodCallExpr aNode, int aDepth) {
		return getMethodCallExpression();
	}

	@Override
	public default String getMappingForLambdaExpr(LambdaExpr aNode, int aDepth) {
		return getLambdaExpression();
	}

	@Override
	public default String getMappingForInstanceOfExpr(InstanceOfExpr aNode, int aDepth) {
		return getInstanceofExpression();
	}

	@Override
	public default String getMappingForFieldAccessExpr(FieldAccessExpr aNode, int aDepth) {
		return getFieldAccessExpression();
	}

	@Override
	public default String getMappingForConditionalExpr(ConditionalExpr aNode, int aDepth) {
		return getConditionalExpression();
	}

	@Override
	public default String getMappingForClassExpr(ClassExpr aNode, int aDepth) {
		return getClassExpression();
	}

	@Override
	public default String getMappingForCastExpr(CastExpr aNode, int aDepth) {
		return getCastExpression();
	}

	@Override
	public default String getMappingForBinaryExpr(BinaryExpr aNode, int aDepth) {
		return getBinaryExpression();
	}

	@Override
	public default String getMappingForAssignExpr(AssignExpr aNode, int aDepth) {
		return getAssignExpression();
	}

	@Override
	public default String getMappingForArrayInitializerExpr(ArrayInitializerExpr aNode, int aDepth) {
		return getArrayInitExpression();
	}

	@Override
	public default String getMappingForArrayCreationExpr(ArrayCreationExpr aNode, int aDepth) {
		return getArrayCreateExpression();
	}

	@Override
	public default String getMappingForArrayAccessExpr(ArrayAccessExpr aNode, int aDepth) {
		return getArrayAccessExpression();
	}

	@Override
	public default String getMappingForStringLiteralExpr(StringLiteralExpr aNode, int aDepth) {
		return getStringLiteralExpression();
	}

	@Override
	public default String getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode, int aDepth) {
		return getDoubleLiteralExpression();
	}

	@Override
	public default String getMappingForLongLiteralExpr(LongLiteralExpr aNode, int aDepth) {
		return getLongLiteralExpression();
	}

	@Override
	public default String getMappingForLongLiteralMinValueExpr(LongLiteralMinValueExpr aNode, int aDepth) {
		return getLongLiteralMinValueExpression();
	}

	@Override
	public default String getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode, int aDepth) {
		return getIntegerLiteralExpression();
	}

	@Override
	public default String getMappingForIntegerLiteralMinValueExpr(IntegerLiteralMinValueExpr aNode, int aDepth) {
		return getIntegerLiteralMinValueExpression();
	}

	@Override
	public default String getMappingForCharLiteralExpr(CharLiteralExpr aNode, int aDepth) {
		return getCharLiteralExpression();
	}

	@Override
	public default String getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode, int aDepth) {
		return getBooleanLiteralExpression();
	}

	@Override
	public default String getMappingForNullLiteralExpr(NullLiteralExpr aNode, int aDepth) {
		return getNullLiteralExpression();
	}

}
