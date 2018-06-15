package se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper;

import java.util.List;

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
import com.github.javaparser.ast.modules.ModuleExportsStmt;
import com.github.javaparser.ast.modules.ModuleOpensStmt;
import com.github.javaparser.ast.modules.ModuleProvidesStmt;
import com.github.javaparser.ast.modules.ModuleRequiresStmt;
import com.github.javaparser.ast.modules.ModuleUsesStmt;
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
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider.KeyWords;

public interface IMapper<T> extends IDetailedNodeMapper<T> {

	/**
	 * @return a keyword provider
	 */
	public IKeyWordProvider<T> getKeyWordProvider();

	// @Override
	// public default T getClosingToken(Node aNode) {
	// if (aNode == null) {
	// return null;
	// }
	//
	// if (aNode instanceof MethodDeclaration) {
	// return getKeyWordProvider().getKeyWord(KeyWords.CLOSING_MDEC);
	// } else if (aNode instanceof ConstructorDeclaration) {
	// return getKeyWordProvider().getKeyWord(KeyWords.CLOSING_CNSTR);
	// } else if (aNode instanceof IfStmt) {
	// return getKeyWordProvider().getKeyWord(KeyWords.CLOSING_IF);
	// } else if (aNode instanceof WhileStmt) {
	// return getKeyWordProvider().getKeyWord(KeyWords.CLOSING_WHILE);
	// } else if (aNode instanceof ForStmt) {
	// return getKeyWordProvider().getKeyWord(KeyWords.CLOSING_FOR);
	// } else if (aNode instanceof TryStmt) {
	// return getKeyWordProvider().getKeyWord(KeyWords.CLOSING_TRY);
	// } else if (aNode instanceof CatchClause) {
	// return getKeyWordProvider().getKeyWord(KeyWords.CLOSING_CATCH);
	// } else if (aNode instanceof ForeachStmt) {
	// return getKeyWordProvider().getKeyWord(KeyWords.CLOSING_FOR_EACH);
	// } else if (aNode instanceof DoStmt) {
	// return getKeyWordProvider().getKeyWord(KeyWords.CLOSING_DO);
	// } else if (aNode instanceof SwitchStmt) {
	// return getKeyWordProvider().getKeyWord(KeyWords.CLOSING_SWITCH);
	// } else if (aNode instanceof EnclosedExpr) {
	// return getKeyWordProvider().getKeyWord(KeyWords.CLOSING_ENCLOSED);
	// } else if (aNode instanceof BlockStmt) {
	// return getKeyWordProvider().getKeyWord(KeyWords.CLOSING_BLOCK_STMT);
	// } else if (aNode instanceof ExpressionStmt) {
	// return getKeyWordProvider().getKeyWord(KeyWords.CLOSING_EXPRESSION_STMT);
	// } else if (aNode instanceof CompilationUnit) {
	// return
	// getKeyWordProvider().getKeyWord(KeyWords.CLOSING_COMPILATION_UNIT);
	// }
	//
	// return null;
	// }
	
	@Override
	default T getMappingForNull(NullNode aNode, Node parent, boolean includeParent) {
		return getKeyWordProvider().getKeyWord(KeyWords.NULL);
	}
	
	@Override
	default T getMappingForNullList(NullListNode aNode, Node parent, boolean includeParent) {
		return getKeyWordProvider().getKeyWord(KeyWords.NULL_LIST);
	}
	
	@Override
	default T getMappingForEmptyList(EmptyListNode aNode, Node parent, boolean includeParent) {
		return getKeyWordProvider().getKeyWord(KeyWords.EMPTY_LIST);
	}

	@Override
	public default T getMappingForUnknownNode(Node aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.UNKNOWN);
	}

	@Override
	public default T getMappingForCompilationUnit(CompilationUnit aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.COMPILATION_UNIT);
	}

	@Override
	public default T getMappingForMemberValuePair(MemberValuePair aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.MEMBER_VALUE_PAIR);
	}

	@Override
	public default T getMappingForVariableDeclarator(VariableDeclarator aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.VARIABLE_DECLARATOR);
	}

	@Override
	public default T getMappingForCatchClause(CatchClause aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.CATCH_CLAUSE_STMT);
	}

	@Override
	public default T getMappingForTypeParameter(TypeParameter aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.TYPE_PAR);
	}

	@Override
	public default T getMappingForImportDeclaration(ImportDeclaration aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.IMPORT_DECLARATION);
	}

	@Override
	public default T getMappingForPackageDeclaration(PackageDeclaration aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.PACKAGE_DECLARATION);
	}

	@Override
	public default T getMappingForParameter(Parameter aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.PARAMETER);
	}

	@Override
	public default T getMappingForJavadocComment(JavadocComment aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.JAVADOC_COMMENT);
	}

	@Override
	public default T getMappingForBlockComment(BlockComment aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.BLOCK_COMMENT);
	}

	@Override
	public default T getMappingForLineComment(LineComment aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.LINE_COMMENT);
	}

	@Override
	public default T getMappingForEnumDeclaration(EnumDeclaration aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.ENUM_DECLARATION);
	}

	@Override
	public default T getMappingForClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration aNode, Node parent, int aDepth,
			boolean includeParent, List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.CLASS_OR_INTERFACE_DECLARATION);
	}

	@Override
	public default T getMappingForAnnotationDeclaration(AnnotationDeclaration aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.ANNOTATION_DECLARATION);
	}

	@Override
	public default T getMappingForAnnotationMemberDeclaration(AnnotationMemberDeclaration aNode, Node parent, int aDepth,
			boolean includeParent, List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.ANNOTATION_MEMBER_DECLARATION);
	}

	@Override
	public default T getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode, Node parent, int aDepth,
			boolean includeParent, List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.ENUM_CONSTANT_DECLARATION);
	}

	@Override
	public default T getMappingForMethodDeclaration(MethodDeclaration aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.METHOD_DECLARATION);
	}

	@Override
	public default T getMappingForFieldDeclaration(FieldDeclaration aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.FIELD_DECLARATION);
	}

	@Override
	public default T getMappingForInitializerDeclaration(InitializerDeclaration aNode, Node parent, int aDepth,
			boolean includeParent, List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.INITIALIZER_DECLARATION);
	}

	@Override
	public default T getMappingForConstructorDeclaration(ConstructorDeclaration aNode, Node parent, int aDepth,
			boolean includeParent, List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.CONSTRUCTOR_DECLARATION);
	}

	@Override
	public default T getMappingForWhileStmt(WhileStmt aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.WHILE_STMT);
	}

	@Override
	public default T getMappingForTryStmt(TryStmt aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.TRY_STMT);
	}

	@Override
	public default T getMappingForThrowStmt(ThrowStmt aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.THROW_STMT);
	}

	@Override
	public default T getMappingForSynchronizedStmt(SynchronizedStmt aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.SYNCHRONIZED_STMT);
	}

	@Override
	public default T getMappingForSwitchStmt(SwitchStmt aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.SWITCH_STMT);
	}

	@Override
	public default T getMappingForSwitchEntryStmt(SwitchEntryStmt aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.SWITCH_ENTRY_STMT);
	}

	@Override
	public default T getMappingForReturnStmt(ReturnStmt aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.RETURN_STMT);
	}

	@Override
	public default T getMappingForLabeledStmt(LabeledStmt aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.LABELED_STMT);
	}

	@Override
	public default T getMappingForIfStmt(IfStmt aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.IF_STMT);
	}

	@Override
	public default T getMappingForForStmt(ForStmt aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.FOR_STMT);
	}

	@Override
	public default T getMappingForForeachStmt(ForeachStmt aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.FOR_EACH_STMT);
	}

	@Override
	public default T getMappingForExpressionStmt(ExpressionStmt aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.EXPRESSION_STMT);
	}

	@Override
	public default T getMappingForExplicitConstructorInvocationStmt(ExplicitConstructorInvocationStmt aNode, Node parent, int aDepth,
			boolean includeParent, List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.EXPL_CONSTR_INVOC_STMT);
	}

	@Override
	public default T getMappingForDoStmt(DoStmt aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.DO_STMT);
	}

	@Override
	public default T getMappingForContinueStmt(ContinueStmt aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.CONTINUE_STMT);
	}

	@Override
	public default T getMappingForBreakStmt(BreakStmt aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.BREAK);
	}

	@Override
	public default T getMappingForBlockStmt(BlockStmt aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.BLOCK_STMT);
	}

	@Override
	public default T getMappingForAssertStmt(AssertStmt aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.ASSERT_STMT);
	}

	@Override
	public default T getMappingForWildcardType(WildcardType aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.TYPE_WILDCARD);
	}

	@Override
	public default T getMappingForVoidType(VoidType aNode, Node parent, boolean includeParent, List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.TYPE_VOID);
	}

	@Override
	public default T getMappingForUnknownType(UnknownType aNode, Node parent, boolean includeParent, List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.TYPE_UNKNOWN);
	}

	@Override
	public default T getMappingForUnionType(UnionType aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.TYPE_UNION);
	}

	@Override
	public default T getMappingForPrimitiveType(PrimitiveType aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.TYPE_PRIMITIVE);
	}

	@Override
	public default T getMappingForIntersectionType(IntersectionType aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.TYPE_INTERSECTION);
	}

	@Override
	public default T getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.CLASS_OR_INTERFACE_TYPE);
	}

	@Override
	public default T getMappingForSingleMemberAnnotationExpr(SingleMemberAnnotationExpr aNode, Node parent, int aDepth,
			boolean includeParent, List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.SINGLE_MEMBER_ANNOTATION_EXPRESSION);
	}

	@Override
	public default T getMappingForNormalAnnotationExpr(NormalAnnotationExpr aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.NORMAL_ANNOTATION_EXPRESSION);
	}

	@Override
	public default T getMappingForMarkerAnnotationExpr(MarkerAnnotationExpr aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.MARKER_ANNOTATION_EXPRESSION);
	}

	@Override
	public default T getMappingForNameExpr(NameExpr aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.NAME_EXPRESSION);
	}

	@Override
	public default T getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode, Node parent, int aDepth,
			boolean includeParent, List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.VARIABLE_DECLARATION_EXPRESSION);
	}

	@Override
	public default T getMappingForTypeExpr(TypeExpr aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.TYPE_EXPRESSION);
	}

	@Override
	public default T getMappingForSuperExpr(SuperExpr aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.SUPER_EXPRESSION);
	}

	@Override
	public default T getMappingForUnaryExpr(UnaryExpr aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.UNARY_EXPRESSION);
	}

	@Override
	public default T getMappingForObjectCreationExpr(ObjectCreationExpr aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.OBJ_CREATE_EXPRESSION);
	}

	@Override
	public default T getMappingForEnclosedExpr(EnclosedExpr aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.ENCLOSED_EXPRESSION);
	}

	@Override
	public default T getMappingForThisExpr(ThisExpr aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.THIS_EXPRESSION);
	}

	@Override
	public default T getMappingForMethodReferenceExpr(MethodReferenceExpr aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.METHOD_REFERENCE_EXPRESSION);
	}

	@Override
	public default T getMappingForMethodCallExpr(MethodCallExpr aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.METHOD_CALL_EXPRESSION);
	}

	@Override
	public default T getMappingForLambdaExpr(LambdaExpr aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.LAMBDA_EXPRESSION);
	}

	@Override
	public default T getMappingForInstanceOfExpr(InstanceOfExpr aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.INSTANCEOF_EXPRESSION);
	}

	@Override
	public default T getMappingForFieldAccessExpr(FieldAccessExpr aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.FIELD_ACCESS_EXPRESSION);
	}

	@Override
	public default T getMappingForConditionalExpr(ConditionalExpr aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.CONDITIONAL_EXPRESSION);
	}

	@Override
	public default T getMappingForClassExpr(ClassExpr aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.CLASS_EXPRESSION);
	}

	@Override
	public default T getMappingForCastExpr(CastExpr aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.CAST_EXPRESSION);
	}

	@Override
	public default T getMappingForBinaryExpr(BinaryExpr aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.BINARY_EXPRESSION);
	}

	@Override
	public default T getMappingForAssignExpr(AssignExpr aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.ASSIGN_EXPRESSION);
	}

	@Override
	public default T getMappingForArrayInitializerExpr(ArrayInitializerExpr aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.ARRAY_INIT_EXPRESSION);
	}

	@Override
	public default T getMappingForArrayCreationExpr(ArrayCreationExpr aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.ARRAY_CREATE_EXPRESSION);
	}

	@Override
	public default T getMappingForArrayAccessExpr(ArrayAccessExpr aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.ARRAY_ACCESS_EXPRESSION);
	}

	@Override
	public default T getMappingForStringLiteralExpr(StringLiteralExpr aNode, Node parent, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.STRING_LITERAL_EXPRESSION);
	}

	@Override
	public default T getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode, Node parent, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.DOUBLE_LITERAL_EXPRESSION);
	}

	@Override
	public default T getMappingForLongLiteralExpr(LongLiteralExpr aNode, Node parent, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.LONG_LITERAL_EXPRESSION);
	}

	@Override
	public default T getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode, Node parent, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.INTEGER_LITERAL_EXPRESSION);
	}

	@Override
	public default T getMappingForCharLiteralExpr(CharLiteralExpr aNode, Node parent, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.CHAR_LITERAL_EXPRESSION);
	}

	@Override
	public default T getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode, Node parent, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.BOOLEAN_LITERAL_EXPRESSION);
	}

	@Override
	public default T getMappingForNullLiteralExpr(NullLiteralExpr aNode, Node parent, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.NULL_LITERAL_EXPRESSION);
	}

	@Override
	default T getMappingForName(Name aNode, Node parent, int aDepth, boolean includeParent, List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.NAME);
	}

	@Override
	default T getMappingForSimpleName(SimpleName aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.SIMPLE_NAME);
	}

	@Override
	default T getMappingForLocalClassDeclarationStmt(LocalClassDeclarationStmt aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.LOCAL_CLASS_DECLARATION_STMT);
	}

	@Override
	default T getMappingForArrayType(ArrayType aNode, Node parent, int aDepth, boolean includeParent, List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.ARRAY_TYPE);
	}

	@Override
	default T getMappingForArrayCreationLevel(ArrayCreationLevel aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.ARRAY_CREATION_LEVEL);
	}

	@Override
	default T getMappingForModuleDeclaration(ModuleDeclaration aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.MODULE_DECLARATION);
	}

	@Override
	default T getMappingForModuleExportsStmt(ModuleExportsStmt aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.MODULE_EXPORTS_STMT);
	}

	@Override
	default T getMappingForModuleOpensStmt(ModuleOpensStmt aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.MODULE_OPENS_STMT);
	}

	@Override
	default T getMappingForModuleProvidesStmt(ModuleProvidesStmt aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.MODULE_PROVIDES_STMT);
	}

	@Override
	default T getMappingForModuleRequiresStmt(ModuleRequiresStmt aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.MODULE_REQUIRES_STMT);
	}

	@Override
	default T getMappingForModuleUsesStmt(ModuleUsesStmt aNode, Node parent, int aDepth, boolean includeParent,
			List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.MODULE_USES_STMT);
	}

	@Override
	default T getMappingForEmptyStmt(EmptyStmt aNode, Node parent, boolean includeParent, List<Node> nextNodes) {
		return getKeyWordProvider().getKeyWord(KeyWords.EMPTY_STMT);
	}

}
