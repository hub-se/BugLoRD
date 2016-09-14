package se.de.hu_berlin.informatik.astlmbuilder.reader;

import com.github.javaparser.ast.ImportDeclaration;
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
import com.github.javaparser.ast.expr.IntegerLiteralMinValueExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralMinValueExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
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
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;

import se.de.hu_berlin.informatik.astlmbuilder.BodyStmt;
import se.de.hu_berlin.informatik.astlmbuilder.ElseStmt;
import se.de.hu_berlin.informatik.astlmbuilder.ExtendsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.ImplementsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.ThrowsStmt;

public interface IASTLMDesirializer {

	public ConstructorDeclaration createConstructorDeclaration(String aSerializedNode);

	public InitializerDeclaration createInitializerDeclaration(String aSerializedNode);

	public EnumConstantDeclaration createEnumConstantDeclaration(String aSerializedNode);

	public VariableDeclarator createVariableDeclarator(String aSerializedNode);

	public EnumDeclaration createEnumDeclaration(String aSerializedNode);

	public AnnotationDeclaration createAnnotationDeclaration(String aSerializedNode);

	public AnnotationMemberDeclaration createAnnotationMemberDeclaration(String aSerializedNode);

	public EmptyMemberDeclaration createEmptyMemberDeclaration(String aSerializedNode);

	public EmptyTypeDeclaration createEmptyTypeDeclaration(String aSerializedNode);

	public WhileStmt createWhileStmt(String aSerializedNode);

	public TryStmt createTryStmt(String aSerializedNode);
	
	public ThrowStmt createThrowStmt(String aSerializedNode);

	// This may never be used
	public ThrowsStmt createThrowsStmt(String aSerializedNode);

	public SynchronizedStmt createSynchronizedStmt(String aSerializedNode);

	public SwitchStmt createSwitchStmt(String aSerializedNode);

	public SwitchEntryStmt createSwitchEntryStmt(String aSerializedNode);

	public ReturnStmt createReturnStmt(String aSerializedNode);

	public LabeledStmt createLabeledStmt(String aSerializedNode);

	public IfStmt createIfStmt(String aSerializedNode);

	// this may never be used
	public ElseStmt createElseStmt(String aSerializedNode);

	public ForStmt createForStmt(String aSerializedNode);
	public ForeachStmt createForeachStmt(String aSerializedNode);

	public ExpressionStmt createExpressionStmt(String aSerializedNode);

	public ExplicitConstructorInvocationStmt createExplicitConstructorInvocationStmt(String aSerializedNode);

	public EmptyStmt createEmptyStmt(String aSerializedNode);

	public DoStmt createDoStmt(String aSerializedNode);

	public ContinueStmt createContinueStmt(String aSerializedNode);

	public CatchClause createCatchClause(String aSerializedNode);

	public BlockStmt createBlockStmt(String aSerializedNode);

	public VariableDeclaratorId createVariableDeclaratorId(String aSerializedNode);

	public VariableDeclarationExpr createVariableDeclarationExpr(String aSerializedNode);

	public TypeExpr createTypeExpr(String aSerializedNode);

	public SuperExpr createSuperExpr(String aSerializedNode);

	public QualifiedNameExpr createQualifiedNameExpr(String aSerializedNode);

	public NullLiteralExpr createNullLiteralExpr(String aSerializedNode);

	public MethodReferenceExpr createMethodReferenceExpr(String aSerializedNode);

	// this may never be used
	public BodyStmt createBodyStmt(String aSerializedNode);

	public LongLiteralMinValueExpr createLongLiteralMinValueExpr(String aSerializedNode);

	public LambdaExpr createLambdaExpr(String aSerializedNode);

	public IntegerLiteralMinValueExpr createIntegerLiteralMinValueExpr(String aSerializedNode);

	public InstanceOfExpr createInstanceOfExpr(String aSerializedNode);

	public FieldAccessExpr createFieldAccessExpr(String aSerializedNode);

	public ConditionalExpr createConditionalExpr(String aSerializedNode);

	public ClassExpr createClassExpr(String aSerializedNode);

	public CastExpr createCastExpr(String aSerializedNode);
	
	public AssignExpr createAssignExpr(String aSerializedNode);

	public ArrayInitializerExpr createArrayInitializerExpr(String aSerializedNode);

	public ArrayCreationExpr createArrayCreationExpr(String aSerializedNode);

	public ArrayAccessExpr createArrayAccessExpr(String aSerializedNode);

	public PackageDeclaration createPackageDeclaration(String aSerializedNode);

	// this may never be used
	public ImportDeclaration createImportDeclaration(String aSerializedNode);

	public FieldDeclaration createFieldDeclaration(String aSerializedNode);

	public ClassOrInterfaceType createClassOrInterfaceType(String aSerializedNode);

	public ClassOrInterfaceDeclaration createClassOrInterfaceDeclaration(String aSerializedNode);

	public MethodDeclaration createMethodDeclaration(String aSerializedNode);

	public BinaryExpr createBinaryExpr(String aSerializedNode);

	public UnaryExpr createUnaryExpr(String aSerializedNode);

	public MethodCallExpr createMethodCallExpr(String aSerializedNode);

	public MethodCallExpr createPrivMethodCallExpr(String aSerializedNode);

	public NameExpr createNameExpr(String aSerializedNode);

	public ConstructorDeclaration createIntegerLiteralExpr(String aSerializedNode);

	public DoubleLiteralExpr createDoubleLiteralExpr(String aSerializedNode);

	public StringLiteralExpr createStringLiteralExpr(String aSerializedNode);

	public BooleanLiteralExpr createBooleanLiteralExpr(String aSerializedNode);

	public CharLiteralExpr createCharLiteralExpr(String aSerializedNode);

	public LongLiteralExpr createLongLiteralExpr(String aSerializedNode);

	public ThisExpr createThisExpr(String aSerializedNode);

	public BreakStmt createBreakStmt(String aSerializedNode);

	public ObjectCreationExpr createObjectCreationExpr(String aSerializedNode);

	public MarkerAnnotationExpr createMarkerAnnotationExpr(String aSerializedNode);

	public NormalAnnotationExpr createNormalAnnotationExpr(String aSerializedNode);

	public SingleMemberAnnotationExpr createSingleMemberAnnotationExpr(String aSerializedNode);

	public Parameter createParameter(String aSerializedNode);

	public MultiTypeParameter createMultiTypeParameter(String aSerializedNode);

	public EnclosedExpr createEnclosedExpr(String aSerializedNode);

	public AssertStmt createAssertStmt(String aSerializedNode);

	public ConstructorDeclaration createMemberValuePair(String aSerializedNode);

	public TypeDeclarationStmt createTypeDeclarationStmt(String aSerializedNode);

	public ReferenceType createReferenceType(String aSerializedNode);

	public PrimitiveType createPrimitiveType(String aSerializedNode);

	// this may never be used
	public UnionType createUnionType(String aSerializedNode);

	public IntersectionType createIntersectionType(String aSerializedNode);

	public TypeParameter createTypeParameter(String aSerializedNode);

	public WildcardType createWildcardType(String aSerializedNode);

	public VoidType createVoidType(String aSerializedNode);

	// this may never be used
	public ExtendsStmt createExtendsStmt(String aSerializedNode);

	// this may never be used
	public ImplementsStmt createImplementsStmt(String aSerializedNode);
	
	public void createUnknown();
	
}
