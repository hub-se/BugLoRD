package se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
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
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.modules.ModuleDeclaration;
import com.github.javaparser.ast.modules.ModuleStmt;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider;
import se.de.hu_berlin.informatik.astlmbuilder.nodes.ElseStmt;

public interface IMapper<T> extends IDetailedNodeMapper<T> {

	/**
	 * @return
	 * a keyword provider
	 */
	public IKeyWordProvider<T> getKeyWordProvider();
	
	@Override
	public default T getClosingToken(Node aNode) {
		if (aNode == null) {
			return null;
		}

		if (aNode instanceof MethodDeclaration) {
			return getKeyWordProvider().getClosingMdec();
		} else if (aNode instanceof ConstructorDeclaration) {
			return getKeyWordProvider().getClosingCnstr();
		} else if (aNode instanceof IfStmt) {
			return getKeyWordProvider().getClosingIf();
		} else if (aNode instanceof WhileStmt) {
			return getKeyWordProvider().getClosingWhile();
		} else if (aNode instanceof ForStmt) {
			return getKeyWordProvider().getClosingFor();
		} else if (aNode instanceof TryStmt) {
			return getKeyWordProvider().getClosingTry();
		} else if (aNode instanceof CatchClause) {
			return getKeyWordProvider().getClosingCatch();
		} else if (aNode instanceof ForeachStmt) {
			return getKeyWordProvider().getClosingForEach();
		} else if (aNode instanceof DoStmt) {
			return getKeyWordProvider().getClosingDo();
		} else if (aNode instanceof SwitchStmt) {
			return getKeyWordProvider().getClosingSwitch();
		} else if (aNode instanceof EnclosedExpr) {
			return getKeyWordProvider().getClosingEnclosed();
		} else if (aNode instanceof BlockStmt) {
			return getKeyWordProvider().getClosingBlockStmt();
		} else if (aNode instanceof ExpressionStmt) {
			return getKeyWordProvider().getClosingExpressionStmt();
		} else if (aNode instanceof CompilationUnit) {
			return getKeyWordProvider().getClosingCompilationUnit();
		}

		return null;
	}

	@Override
	public default T getMappingForUnknownNode(Node aNode, int aDepth) {
		return getKeyWordProvider().getUnknown(aNode);
	}

	@Override
	public default T getMappingForCompilationUnit(CompilationUnit aNode, int aDepth) {
		return getKeyWordProvider().getCompilationUnit();
	}

	@Override
	public default T getMappingForMemberValuePair(MemberValuePair aNode, int aDepth) {
		return getKeyWordProvider().getMemberValuePair();
	}

	@Override
	public default T getMappingForVariableDeclarator(VariableDeclarator aNode, int aDepth) {
		return getKeyWordProvider().getVariableDeclaration();
	}

	@Override
	public default T getMappingForCatchClause(CatchClause aNode, int aDepth) {
		return getKeyWordProvider().getCatchClauseStatement();
	}

	@Override
	public default T getMappingForTypeParameter(TypeParameter aNode, int aDepth) {
		return getKeyWordProvider().getTypePar();
	}

	@Override
	public default T getMappingForImportDeclaration(ImportDeclaration aNode, int aDepth) {
		return getKeyWordProvider().getImportDeclaration();
	}

	@Override
	public default T getMappingForPackageDeclaration(PackageDeclaration aNode, int aDepth) {
		return getKeyWordProvider().getPackageDeclaration();
	}

	@Override
	public default T getMappingForParameter(Parameter aNode, int aDepth) {
		return getKeyWordProvider().getParameter();
	}

	@Override
	public default T getMappingForJavadocComment(JavadocComment aNode, int aDepth) {
		return getKeyWordProvider().getJavadocComment();
	}

	@Override
	public default T getMappingForBlockComment(BlockComment aNode, int aDepth) {
		return getKeyWordProvider().getBlockComment();
	}

	@Override
	public default T getMappingForLineComment(LineComment aNode, int aDepth) {
		return getKeyWordProvider().getLineComment();
	}

	@Override
	public default T getMappingForEnumDeclaration(EnumDeclaration aNode, int aDepth) {
		return getKeyWordProvider().getEnumDeclaration();
	}

	@Override
	public default T getMappingForClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration aNode, int aDepth) {
		return getKeyWordProvider().getClassOrInterfaceDeclaration();
	}

	@Override
	public default T getMappingForAnnotationDeclaration(AnnotationDeclaration aNode, int aDepth) {
		return getKeyWordProvider().getAnnotationDeclaration();
	}

	@Override
	public default T getMappingForAnnotationMemberDeclaration(AnnotationMemberDeclaration aNode, int aDepth) {
		return getKeyWordProvider().getAnnotationMemberDeclaration();
	}

	@Override
	public default T getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode, int aDepth) {
		return getKeyWordProvider().getEnumConstantDeclaration();
	}

	@Override
	public default T getMappingForMethodDeclaration(MethodDeclaration aNode, int aDepth) {
		return getKeyWordProvider().getMethodDeclaration();
	}

	@Override
	public default T getMappingForFieldDeclaration(FieldDeclaration aNode, int aDepth) {
		return getKeyWordProvider().getFieldDeclaration();
	}

	@Override
	public default T getMappingForInitializerDeclaration(InitializerDeclaration aNode, int aDepth) {
		return getKeyWordProvider().getInitializerDeclaration();
	}

	@Override
	public default T getMappingForConstructorDeclaration(ConstructorDeclaration aNode, int aDepth) {
		return getKeyWordProvider().getConstructorDeclaration();
	}

	@Override
	public default T getMappingForWhileStmt(WhileStmt aNode, int aDepth) {
		return getKeyWordProvider().getWhileStatement();
	}

	@Override
	public default T getMappingForTryStmt(TryStmt aNode, int aDepth) {
		return getKeyWordProvider().getTryStatement();
	}

	@Override
	public default T getMappingForThrowStmt(ThrowStmt aNode, int aDepth) {
		return getKeyWordProvider().getThrowStatement();
	}

	@Override
	public default T getMappingForSynchronizedStmt(SynchronizedStmt aNode, int aDepth) {
		return getKeyWordProvider().getSynchronizedStatement();
	}

	@Override
	public default T getMappingForSwitchStmt(SwitchStmt aNode, int aDepth) {
		return getKeyWordProvider().getSwitchStatement();
	}

	@Override
	public default T getMappingForSwitchEntryStmt(SwitchEntryStmt aNode, int aDepth) {
		return getKeyWordProvider().getSwitchEntryStatement();
	}

	@Override
	public default T getMappingForReturnStmt(ReturnStmt aNode, int aDepth) {
		return getKeyWordProvider().getReturnStatement();
	}

	@Override
	public default T getMappingForLabeledStmt(LabeledStmt aNode, int aDepth) {
		return getKeyWordProvider().getLabeledStatement();
	}

	@Override
	public default T getMappingForElseStmt(ElseStmt aNode, int aDepth) {
		return getKeyWordProvider().getElseStatement();
	}

	@Override
	public default T getMappingForIfStmt(IfStmt aNode, int aDepth) {
		return getKeyWordProvider().getIfStatement();
	}

	@Override
	public default T getMappingForForStmt(ForStmt aNode, int aDepth) {
		return getKeyWordProvider().getForStatement();
	}

	@Override
	public default T getMappingForForeachStmt(ForeachStmt aNode, int aDepth) {
		return getKeyWordProvider().getForEachStatement();
	}

	@Override
	public default T getMappingForExpressionStmt(ExpressionStmt aNode, int aDepth) {
		return getKeyWordProvider().getExpressionStatement();
	}

	@Override
	public default T getMappingForExplicitConstructorInvocationStmt(ExplicitConstructorInvocationStmt aNode,
			int aDepth) {
		// this is not the explicit constructor invocation statement?
		return getKeyWordProvider().getExplicitConstructorStatement();
	}

	@Override
	public default T getMappingForDoStmt(DoStmt aNode, int aDepth) {
		return getKeyWordProvider().getDoStatement();
	}

	@Override
	public default T getMappingForContinueStmt(ContinueStmt aNode, int aDepth) {
		return getKeyWordProvider().getContinueStatement();
	}

	@Override
	public default T getMappingForBreakStmt(BreakStmt aNode, int aDepth) {
		return getKeyWordProvider().getBreak();
	}

	@Override
	public default T getMappingForBlockStmt(BlockStmt aNode, int aDepth) {
		return getKeyWordProvider().getBlockStatement();
	}

	@Override
	public default T getMappingForAssertStmt(AssertStmt aNode, int aDepth) {
		return getKeyWordProvider().getAssertStmt();
	}

	@Override
	public default T getMappingForWildcardType(WildcardType aNode, int aDepth) {
		return getKeyWordProvider().getTypeWildcard();
	}

	@Override
	public default T getMappingForVoidType(VoidType aNode, int aDepth) {
		return getKeyWordProvider().getTypeVoid();
	}

	@Override
	public default T getMappingForUnknownType(UnknownType aNode, int aDepth) {
		return getKeyWordProvider().getTypeUnknown();
	}

	@Override
	public default T getMappingForUnionType(UnionType aNode, int aDepth) {
		return getKeyWordProvider().getTypeUnion();
	}

	@Override
	public default T getMappingForPrimitiveType(PrimitiveType aNode, int aDepth) {
		return getKeyWordProvider().getTypePrimitive();
	}

	@Override
	public default T getMappingForIntersectionType(IntersectionType aNode, int aDepth) {
		return getKeyWordProvider().getTypeIntersection();
	}

	@Override
	public default T getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode, int aDepth) {
		return getKeyWordProvider().getClassOrInterfaceType();
	}

	@Override
	public default T getMappingForSingleMemberAnnotationExpr(SingleMemberAnnotationExpr aNode, int aDepth) {
		return getKeyWordProvider().getSingleMemberAnnotationExpression();
	}

	@Override
	public default T getMappingForNormalAnnotationExpr(NormalAnnotationExpr aNode, int aDepth) {
		return getKeyWordProvider().getNormalAnnotationExpression();
	}

	@Override
	public default T getMappingForMarkerAnnotationExpr(MarkerAnnotationExpr aNode, int aDepth) {
		return getKeyWordProvider().getMarkerAnnotationExpression();
	}

	@Override
	public default T getMappingForNameExpr(NameExpr aNode, int aDepth) {
		return getKeyWordProvider().getNameExpression();
	}

	@Override
	public default T getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode, int aDepth) {
		return getKeyWordProvider().getVariableDeclarationExpression();
	}

	@Override
	public default T getMappingForTypeExpr(TypeExpr aNode, int aDepth) {
		return getKeyWordProvider().getTypeExpression();
	}

	@Override
	public default T getMappingForSuperExpr(SuperExpr aNode, int aDepth) {
		return getKeyWordProvider().getSuperExpression();
	}

	@Override
	public default T getMappingForUnaryExpr(UnaryExpr aNode, int aDepth) {
		return getKeyWordProvider().getUnaryExpression();
	}

	@Override
	public default T getMappingForObjectCreationExpr(ObjectCreationExpr aNode, int aDepth) {
		return getKeyWordProvider().getObjCreateExpression();
	}

	@Override
	public default T getMappingForEnclosedExpr(EnclosedExpr aNode, int aDepth) {
		return getKeyWordProvider().getEnclosedExpression();
	}

	@Override
	public default T getMappingForThisExpr(ThisExpr aNode, int aDepth) {
		return getKeyWordProvider().getThisExpression();
	}

	@Override
	public default T getMappingForMethodReferenceExpr(MethodReferenceExpr aNode, int aDepth) {
		return getKeyWordProvider().getMethodReferenceExpression();
	}

	@Override
	public default T getMappingForMethodCallExpr(MethodCallExpr aNode, int aDepth) {
		return getKeyWordProvider().getMethodCallExpression();
	}

	@Override
	public default T getMappingForLambdaExpr(LambdaExpr aNode, int aDepth) {
		return getKeyWordProvider().getLambdaExpression();
	}

	@Override
	public default T getMappingForInstanceOfExpr(InstanceOfExpr aNode, int aDepth) {
		return getKeyWordProvider().getInstanceofExpression();
	}

	@Override
	public default T getMappingForFieldAccessExpr(FieldAccessExpr aNode, int aDepth) {
		return getKeyWordProvider().getFieldAccessExpression();
	}

	@Override
	public default T getMappingForConditionalExpr(ConditionalExpr aNode, int aDepth) {
		return getKeyWordProvider().getConditionalExpression();
	}

	@Override
	public default T getMappingForClassExpr(ClassExpr aNode, int aDepth) {
		return getKeyWordProvider().getClassExpression();
	}

	@Override
	public default T getMappingForCastExpr(CastExpr aNode, int aDepth) {
		return getKeyWordProvider().getCastExpression();
	}

	@Override
	public default T getMappingForBinaryExpr(BinaryExpr aNode, int aDepth) {
		return getKeyWordProvider().getBinaryExpression();
	}

	@Override
	public default T getMappingForAssignExpr(AssignExpr aNode, int aDepth) {
		return getKeyWordProvider().getAssignExpression();
	}

	@Override
	public default T getMappingForArrayInitializerExpr(ArrayInitializerExpr aNode, int aDepth) {
		return getKeyWordProvider().getArrayInitExpression();
	}

	@Override
	public default T getMappingForArrayCreationExpr(ArrayCreationExpr aNode, int aDepth) {
		return getKeyWordProvider().getArrayCreateExpression();
	}

	@Override
	public default T getMappingForArrayAccessExpr(ArrayAccessExpr aNode, int aDepth) {
		return getKeyWordProvider().getArrayAccessExpression();
	}

	@Override
	public default T getMappingForStringLiteralExpr(StringLiteralExpr aNode, int aDepth) {
		return getKeyWordProvider().getStringLiteralExpression();
	}

	@Override
	public default T getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode, int aDepth) {
		return getKeyWordProvider().getDoubleLiteralExpression();
	}

	@Override
	public default T getMappingForLongLiteralExpr(LongLiteralExpr aNode, int aDepth) {
		return getKeyWordProvider().getLongLiteralExpression();
	}

	@Override
	public default T getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode, int aDepth) {
		return getKeyWordProvider().getIntegerLiteralExpression();
	}

	@Override
	public default T getMappingForCharLiteralExpr(CharLiteralExpr aNode, int aDepth) {
		return getKeyWordProvider().getCharLiteralExpression();
	}

	@Override
	public default T getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode, int aDepth) {
		return getKeyWordProvider().getBooleanLiteralExpression();
	}

	@Override
	public default T getMappingForNullLiteralExpr(NullLiteralExpr aNode, int aDepth) {
		return getKeyWordProvider().getNullLiteralExpression();
	}

	@Override
	default T getMappingForName(Name aNode, int aDepth) {
		return getKeyWordProvider().getName();
	}

	@Override
	default T getMappingForSimpleName(SimpleName aNode, int aDepth) {
		return getKeyWordProvider().getSimpleName();
	}

	@Override
	default T getMappingForLocalClassDeclarationStmt(LocalClassDeclarationStmt aNode, int aDepth) {
		return getKeyWordProvider().getLocalClassDeclarationStmt();
	}

	@Override
	default T getMappingForArrayType(ArrayType aNode, int aDepth) {
		return getKeyWordProvider().getArrayType();
	}

	@Override
	default T getMappingForArrayCreationLevel(ArrayCreationLevel aNode, int aDepth) {
		return getKeyWordProvider().getArrayCreationLevel();
	}
	
	@Override
	default T getMappingForModuleDeclaration(ModuleDeclaration aNode, int aDepth) {
		return getKeyWordProvider().getModuleDeclaration();
	}
	
	@Override
	default T getMappingForModuleStmt(ModuleStmt aNode, int aDepth) {
		return getKeyWordProvider().getModuleStmt();
	}

}
