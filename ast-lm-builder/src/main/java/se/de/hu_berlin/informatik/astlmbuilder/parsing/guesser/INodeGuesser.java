package se.de.hu_berlin.informatik.astlmbuilder.parsing.guesser;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
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
import com.github.javaparser.ast.expr.AnnotationExpr;
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
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
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
import com.github.javaparser.ast.modules.ModuleStmt;
import com.github.javaparser.ast.modules.ModuleUsesStmt;
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
import com.github.javaparser.ast.stmt.Statement;
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
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;

import se.de.hu_berlin.informatik.astlmbuilder.nodes.UnknownNode;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.InformationWrapper;

public interface INodeGuesser extends INodeGuesserBasics {

	default public ConstructorDeclaration guessConstructorDeclaration(InformationWrapper info) {
		// EnumSet<Modifier> modifiers
		// NodeList<AnnotationExpr> annotations
		// NodeList<TypeParameter> typeParameters 
		// SimpleName name
		// NodeList<Parameter> parameters
		// NodeList<ReferenceType> thrownExceptions
		// BlockStmt body
		return new ConstructorDeclaration(
				guessModifiers(info.getCopy()), 
				guessList(AnnotationExpr.class, info.getCopy()), 
				guessList(TypeParameter.class, info.getCopy()), 
				guessSimpleName(info.getCopy()), 
				guessList(Parameter.class, info.getCopy()), 
				guessList(ReferenceType.class, info.getCopy()), 
				guessBlockStmt(info.getCopy()));
	}

	public default InitializerDeclaration guessInitializerDeclaration(InformationWrapper info) {
		// boolean isStatic
		// BlockStmt body
		return new InitializerDeclaration(
				guessBoolean(info.getCopy()), 
				guessBlockStmt(info.getCopy()));
	}

	public default EnumConstantDeclaration guessEnumConstantDeclaration(InformationWrapper info) {
		// NodeList<AnnotationExpr> annotations,
		// SimpleName name
		// NodeList<Expression> arguments
		// NodeList<BodyDeclaration<?>> classBody
		return new EnumConstantDeclaration(
				guessList( AnnotationExpr.class, info.getCopy()),
				guessSimpleName(info.getCopy()),
				guessList( Expression.class, info.getCopy()),
				guessBodyDeclarationList( info.getCopy()));
	};

	public default VariableDeclarator guessVariableDeclarator(InformationWrapper info) {
		// Type type
		// SimpleName name
		// Expression initializer
		return new VariableDeclarator(
				guessNode( Type.class, info.getCopy() ),
				guessSimpleName(info.getCopy()), 
				guessNode( Expression.class, info.getCopy()));
	}

	public default EnumDeclaration guessEnumDeclaration(InformationWrapper info) {
		// EnumSet<Modifier> modifiers
		// NodeList<AnnotationExpr> annotations
		// SimpleName name
		// NodeList<ClassOrInterfaceType> implementedTypes
		// NodeList<EnumConstantDeclaration> entries
		// NodeList<BodyDeclaration<?>> members
		return new EnumDeclaration(
				guessModifiers(info.getCopy()), 
				guessList(AnnotationExpr.class, info.getCopy()), 
				guessSimpleName(info.getCopy()), 
				guessList(ClassOrInterfaceType.class, info.getCopy()), 
				guessList(EnumConstantDeclaration.class, info.getCopy()), 
				guessBodyDeclarationList( info.getCopy()));
	}

	public default AnnotationDeclaration guessAnnotationDeclaration(InformationWrapper info)throws IllegalArgumentException {
		// EnumSet<Modifier> modifiers
		// NodeList<AnnotationExpr> annotations
		// SimpleName name
		// NodeList<BodyDeclaration<?>> members
		return new AnnotationDeclaration(
				guessModifiers(info.getCopy()), 
				guessList(AnnotationExpr.class, info.getCopy()),
				guessSimpleName(info.getCopy()), 
				guessBodyDeclarationList( info.getCopy()));
	}

	public default AnnotationMemberDeclaration guessAnnotationMemberDeclaration(InformationWrapper info)throws IllegalArgumentException {
		// EnumSet<Modifier> modifiers
		// NodeList<AnnotationExpr> annotations
		// Type type
		// SimpleName name
		// Expression defaultValue
		return new AnnotationMemberDeclaration(
				guessModifiers(info.getCopy()), 
				guessList(AnnotationExpr.class, info.getCopy()),
				guessNode( Type.class, info.getCopy() ), 
				guessSimpleName(info.getCopy()),
				guessNode( Expression.class,  info.getCopy() ));
	}

	public default WhileStmt guessWhileStmt(InformationWrapper info)throws IllegalArgumentException {
		// final Expression condition
		// final Statement body
		return new WhileStmt(
				guessNode( Expression.class, info.getCopy() ), 
				guessBlockStmt(info.getCopy()));
	}

	public default TryStmt guessTryStmt(InformationWrapper info)throws IllegalArgumentException {
		// NodeList<VariableDeclarationExpr> resources
		// final BlockStmt tryBlock
		// final NodeList<CatchClause> catchClauses
		// final BlockStmt finallyBlock
		return new TryStmt(
				guessList(VariableDeclarationExpr.class, info.getCopy()), 
				guessBlockStmt(info.getCopy()),
				guessList(CatchClause.class, info.getCopy()), 
				guessBlockStmt(info.getCopy()));
	}

	public default ThrowStmt guessThrowStmt(InformationWrapper info)throws IllegalArgumentException {
		// final Expression expression
		return new ThrowStmt(
				guessNode( Expression.class,  info.getCopy() ));
	}

	public default SynchronizedStmt guessSynchronizedStmt(InformationWrapper info)throws IllegalArgumentException {
		// final Expression expression
		// final BlockStmt body
		return new SynchronizedStmt(
				guessNode( Expression.class,  info.getCopy() ),
				guessBlockStmt(info.getCopy()));
	}

	public default SwitchStmt guessSwitchStmt(InformationWrapper info)throws IllegalArgumentException {
		// final Expression selector
		// final NodeList<SwitchEntryStmt> entries
		return new SwitchStmt(
				guessNode( Expression.class,  info.getCopy() ), 
				guessList(SwitchEntryStmt.class, info.getCopy()));
	}

	public default SwitchEntryStmt guessSwitchEntryStmt(InformationWrapper info)throws IllegalArgumentException {
		// final Expression label
		// final NodeList<Statement> statements
		return new SwitchEntryStmt(
				guessNode( Expression.class,  info.getCopy() ), 
				guessList(Statement.class, info.getCopy()));
	}

	public default ReturnStmt guessReturnStmt(InformationWrapper info)throws IllegalArgumentException {
		// final Expression expression
		return new ReturnStmt(
				guessNode( Expression.class,  info.getCopy()));
	}

	public default LabeledStmt guessLabeledStmt(InformationWrapper info)throws IllegalArgumentException {
		// final String label
		// final Statement statement
		return new LabeledStmt(
				guessStringValue(info.getCopy()), 
				guessNode( Statement.class, info.getCopy()));
	}

	public default IfStmt guessIfStmt(InformationWrapper info)throws IllegalArgumentException {
		// final Expression condition
		// final Statement thenStmt
		// final Statement elseStmt
		return new IfStmt(
				guessNode( Expression.class,  info.getCopy()),
				guessNode( Statement.class, info.getCopy()),
				guessNode( Statement.class, info.getCopy()));
	}


	public default ForStmt guessForStmt(InformationWrapper info)throws IllegalArgumentException {
		// final NodeList<Expression> initialization
		// final Expression compare
		// final NodeList<Expression> update
		// final Statement body
		return new ForStmt(
				guessList(Expression.class, info.getCopy()),
				guessNode( Expression.class,  info.getCopy()),
				guessList(Expression.class, info.getCopy()),
				guessNode( Statement.class, info.getCopy()));
	}

	public default ForeachStmt guessForeachStmt(InformationWrapper info) {
		// final VariableDeclarationExpr variable
		// final Expression iterable
		// final Statement body
		return new ForeachStmt(
				guessVariableDeclarationExpr(info.getCopy()), 
				guessNode( Expression.class,  info.getCopy()), 
				guessNode( Statement.class, info.getCopy()));
	}

	public default ExpressionStmt guessExpressionStmt(InformationWrapper info) {
		// final Expression expression
		return new ExpressionStmt(
				guessNode( Expression.class,  info.getCopy()));
	}

	public default ExplicitConstructorInvocationStmt guessExplicitConstructorInvocationStmt(InformationWrapper info) {
		// final boolean isThis
		// final Expression expression
		// final NodeList<Expression> arguments
		return new ExplicitConstructorInvocationStmt(
				guessBoolean(info.getCopy()), 
				guessNode( Expression.class,  info.getCopy()),
				guessList(Expression.class, info.getCopy()));
	}

	public default DoStmt guessDoStmt(InformationWrapper info) {
		// final Statement body
		// final Expression condition
		return new DoStmt(
				guessNode( Statement.class, info.getCopy()),
				guessNode( Expression.class,  info.getCopy()));
	}

	public default ContinueStmt guessContinueStmt(InformationWrapper info) {
		// final SimpleName label
		return new ContinueStmt(
				guessSimpleName(info.getCopy()));
	}

	public default CatchClause guessCatchClause(InformationWrapper info) {
		// final EnumSet<Modifier> exceptModifier
		// final NodeList<AnnotationExpr> exceptAnnotations
		// final ClassOrInterfaceType exceptType
		// final SimpleName exceptName
		// final BlockStmt body
		return new CatchClause(
				guessModifiers(info.getCopy()), 
				guessList(AnnotationExpr.class, info.getCopy()), 
				guessClassOrInterfaceType( info.getCopy()), 
				guessSimpleName(info.getCopy()), 
				guessBlockStmt(info.getCopy()));
	}

	public default BlockStmt guessBlockStmt(InformationWrapper info) {
		// final NodeList<Statement> statements
		return new BlockStmt( 
				guessList(Statement.class, info.getCopy()));
	}

	public default VariableDeclarationExpr guessVariableDeclarationExpr(InformationWrapper info) {
		// final EnumSet<Modifier> modifiers
		// final NodeList<AnnotationExpr> annotations
		// final NodeList<VariableDeclarator> variables
		return new VariableDeclarationExpr(
				guessModifiers(info.getCopy()), 
				guessList(AnnotationExpr.class, info.getCopy()), 
				guessList(VariableDeclarator.class, info.getCopy()));
	}

	public default TypeExpr guessTypeExpr(InformationWrapper info) {
		// Type type
		return new TypeExpr(
				guessNode( Type.class,info.getCopy() ));
	}

	public default SuperExpr guessSuperExpr(InformationWrapper info) {
		// final Expression classExpr
		return new SuperExpr(
				guessNode( Expression.class,  info.getCopy() ));
	}

	public default NullLiteralExpr guessNullLiteralExpr(InformationWrapper info) {
		// funny :D
		return new NullLiteralExpr();
	}

	public default MethodReferenceExpr guessMethodReferenceExpr(InformationWrapper info) {
		// Expression scope
		// NodeList<Type> typeArguments
		// String identifier
		return new MethodReferenceExpr(
				guessNode( Expression.class, info.getCopy()), 
				guessList(Type.class, info.getCopy()),
				guessMethodIdentifier( info.getCopy()));
	}

	public default LambdaExpr guessLambdaExpr(InformationWrapper info) {
		// NodeList<Parameter> parameters
		// Statement body
		// boolean isEnclosingParameters
		return new LambdaExpr(
				guessList(Parameter.class, info.getCopy()), 
				guessNode( Statement.class, info.getCopy() ),
				guessBoolean(info.getCopy()));
	}

	public default InstanceOfExpr guessInstanceOfExpr(InformationWrapper info) {
		// final Expression expression
		// final ReferenceType<?> type
		return new InstanceOfExpr(
				guessNode( Expression.class, info.getCopy() ), 
				guessNode( ReferenceType.class, info.getCopy()));
	}

	public default FieldAccessExpr guessFieldAccessExpr(InformationWrapper info) {
		// final Expression scope
		// final NodeList<Type> typeArguments
		// final SimpleName name
		return new FieldAccessExpr(
				guessNode( Expression.class, info.getCopy()), 
				guessList(Type.class, info.getCopy()), 
				guessSimpleName(info.getCopy()));
	}

	public default ConditionalExpr guessConditionalExpr(InformationWrapper info) {
		// Expression condition
		// Expression thenExpr
		// Expression elseExpr
		return new ConditionalExpr(
				guessNode( Expression.class, info.getCopy()), 
				guessNode( Expression.class, info.getCopy()), 
				guessNode( Expression.class, info.getCopy()));
	}

	public default ClassExpr guessClassExpr(InformationWrapper info) {
		// Type type
		return new ClassExpr(
				guessNode( Type.class,info.getCopy()));
	}

	public default CastExpr guessCastExpr(InformationWrapper info) {
		// Type type
		// Expression expression
		return new CastExpr(
				guessNode( Type.class,info.getCopy()),
				guessNode( Expression.class,  info.getCopy()));
	}

	public default AssignExpr guessAssignExpr(InformationWrapper info) {
		// Expression target
		// Expression value
		// Operator operator
		return new AssignExpr(
				guessNode( Expression.class,  info.getCopy()),
				guessNode( Expression.class,  info.getCopy()),
				guessAssignOperator(info.getCopy()));
	}

	public default ArrayInitializerExpr guessArrayInitializerExpr(InformationWrapper info) {
		// NodeList<Expression> values
		return new ArrayInitializerExpr(
				guessList(Expression.class, info.getCopy()));
	}

	public default ArrayCreationExpr guessArrayCreationExpr(InformationWrapper info) {
		// Type elementType
		// NodeList<ArrayCreationLevel> levels
		// ArrayInitializerExpr initializer
		return new ArrayCreationExpr(
				guessNode( Type.class, info.getCopy()), 
				guessList(ArrayCreationLevel.class, info.getCopy()), 
				guessArrayInitializerExpr( info.getCopy()));
	}

	public default ArrayAccessExpr guessArrayAccessExpr(InformationWrapper info) {
		// Expression name
		// Expression index
		return new ArrayAccessExpr(
				guessNode( Expression.class,  info.getCopy()),
				guessNode( Expression.class,  info.getCopy()));
	}

	public default PackageDeclaration guessPackageDeclaration(InformationWrapper info) {
		// NodeList<AnnotationExpr> annotations
		// Name name
		return new PackageDeclaration(
				guessList(AnnotationExpr.class, info.getCopy()), 
				guessName(info.getCopy()));
	}

	public default ImportDeclaration guessImportDeclaration(InformationWrapper info) {
		// Name name
		// boolean isStatic
		// boolean isAsterisk
		return new ImportDeclaration(
				guessName(info.getCopy()),
				guessBoolean(info.getCopy()),
				guessBoolean(info.getCopy()));
	}

	public default FieldDeclaration guessFieldDeclaration(InformationWrapper info) {
		// EnumSet<Modifier> modifiers
		// NodeList<AnnotationExpr> annotations
		// NodeList<VariableDeclarator> variables
		return new FieldDeclaration(
				guessModifiers(info.getCopy()), 
				guessList(AnnotationExpr.class, info.getCopy()), 
				guessList(VariableDeclarator.class, info.getCopy()));
	}

	public default ClassOrInterfaceType guessClassOrInterfaceType(InformationWrapper info) {
		// final ClassOrInterfaceType scope
		// final SimpleName name
		// final NodeList<Type> typeArguments
		return new ClassOrInterfaceType(
				guessClassOrInterfaceType(info.getCopy()),  
				guessSimpleName(info.getCopy()), 
				guessList(Type.class, info.getCopy()));
	}

	public default ClassOrInterfaceDeclaration guessClassOrInterfaceDeclaration(InformationWrapper info) {
		// final EnumSet<Modifier> modifiers
		// final NodeList<AnnotationExpr> annotations
		// final boolean isInterface
		// final SimpleName name
		// final NodeList<TypeParameter> typeParameters
		// final NodeList<ClassOrInterfaceType> extendedTypes
		// final NodeList<ClassOrInterfaceType> implementedTypes
		// final NodeList<BodyDeclaration<?>> members
		return new ClassOrInterfaceDeclaration(
				guessModifiers(info.getCopy()), 
				guessList(AnnotationExpr.class, info.getCopy()), 
				guessBoolean(info.getCopy()), 
				guessSimpleName(info.getCopy()), 
				guessList(TypeParameter.class, info.getCopy()), 
				guessList(ClassOrInterfaceType.class, info.getCopy()),
				guessList(ClassOrInterfaceType.class, info.getCopy()),
				guessBodyDeclarationList(info.getCopy()));
	}

	public default MethodDeclaration guessMethodDeclaration(InformationWrapper info) {
		// final EnumSet<Modifier> modifiers
		// final NodeList<AnnotationExpr> annotations
		// final NodeList<TypeParameter> typeParameters
		// final Type type
		// final SimpleName name
		// final boolean isDefault
		// final NodeList<Parameter> parameters
		// final NodeList<ReferenceType> thrownExceptions
		// final BlockStmt body
		return new MethodDeclaration(
				guessModifiers(info.getCopy()), 
				guessList(AnnotationExpr.class, info.getCopy()), 
				guessList(TypeParameter.class, info.getCopy()),
				guessNode( Type.class, info.getCopy()),
				guessSimpleName(info.getCopy()),
				guessBoolean(info.getCopy()),
				guessList(Parameter.class, info.getCopy()), 
				guessList(ReferenceType.class, info.getCopy()), 
				guessBlockStmt(info.getCopy()));
	}

	public default BinaryExpr guessBinaryExpr(InformationWrapper info) {
		// Expression left
		// Expression right
		// Operator operator
		return new BinaryExpr(
				guessNode( Expression.class,  info.getCopy()),
				guessNode( Expression.class,  info.getCopy()),
				guessBinaryOperator(info.getCopy()));
	}

	public default UnaryExpr guessUnaryExpr(InformationWrapper info) {
		// final Expression expression
		// final Operator operator
		return new UnaryExpr(
				guessNode( Expression.class,  info.getCopy()),
				guessUnaryOperator(info.getCopy()));
	}

	public default MethodCallExpr guessMethodCallExpr(InformationWrapper info) {
		// final Expression scope
		// final NodeList<Type> typeArguments
		// final SimpleName name
		// final NodeList<Expression> arguments
		return new MethodCallExpr(
				guessNode( Expression.class,  info.getCopy() ), 
				guessList(Type.class, info.getCopy()), 
				guessSimpleName(info.getCopy()), 
				guessList(Expression.class, info.getCopy()));
	}

	public default NameExpr guessNameExpr(InformationWrapper info) {
		// final SimpleName name
		return new NameExpr(
				guessSimpleName(info.getCopy()));
	}

	public default ConstructorDeclaration guessIntegerLiteralExpr(InformationWrapper info) {
		// EnumSet<Modifier> modifiers
		// NodeList<AnnotationExpr> annotations
		// NodeList<TypeParameter> typeParameters
		// SimpleName name
		// NodeList<Parameter> parameters
		// NodeList<ReferenceType> thrownExceptions
		// BlockStmt body
		return new ConstructorDeclaration(
				guessModifiers(info.getCopy()), 
				guessList(AnnotationExpr.class, info.getCopy()), 
				guessList(TypeParameter.class, info.getCopy()), 
				guessSimpleName(info.getCopy()), 
				guessList(Parameter.class, info.getCopy()), 
				guessList(ReferenceType.class, info.getCopy()), 
				guessBlockStmt(info.getCopy()));
	}

	public default DoubleLiteralExpr guessDoubleLiteralExpr(InformationWrapper info) {
		// final String value
		return new DoubleLiteralExpr(
				guessStringValue(info.getCopy()));
	}

	public default StringLiteralExpr guessStringLiteralExpr(InformationWrapper info) {
		// final String value
		return new StringLiteralExpr(
				guessStringValue(info.getCopy()));
	}

	public default BooleanLiteralExpr guessBooleanLiteralExpr(InformationWrapper info) {
		// boolean value
		return new BooleanLiteralExpr(
				guessBoolean(info.getCopy()));
	}

	public default CharLiteralExpr guessCharLiteralExpr(InformationWrapper info) {
		// String value
		return new CharLiteralExpr(
				guessStringValue(info.getCopy()));
	}

	public default LongLiteralExpr guessLongLiteralExpr(InformationWrapper info) {
		// final String value
		return new LongLiteralExpr(
				guessStringValue(info.getCopy()));
	}

	public default ThisExpr guessThisExpr(InformationWrapper info) {
		// final Expression classExpr
		return new ThisExpr(
				guessNode( Expression.class,  info.getCopy() ));
	}

	public default BreakStmt guessBreakStmt(InformationWrapper info)  {
		// final SimpleName label
		return new BreakStmt(
				guessSimpleName(info.getCopy()));
	}

	public default ObjectCreationExpr guessObjectCreationExpr(InformationWrapper info)  {
		// final Expression scope
		// final ClassOrInterfaceType type
		// final NodeList<Type> typeArguments
		// final NodeList<Expression> arguments
		// final NodeList<BodyDeclaration<?>> anonymousClassBody
		return new ObjectCreationExpr(
				guessNode( Expression.class, info.getCopy()), 
				guessClassOrInterfaceType( info.getCopy()), 
				guessList(Type.class, info.getCopy()), 
				guessList(Expression.class, info.getCopy()), 
				guessBodyDeclarationList(info.getCopy()));
	}

	public default MarkerAnnotationExpr guessMarkerAnnotationExpr(InformationWrapper info)  {
		// final Name name
		return new MarkerAnnotationExpr( 
				guessName(info.getCopy())); 
	}

	public default NormalAnnotationExpr guessNormalAnnotationExpr(InformationWrapper info)  {
		// final Name name
		// final NodeList<MemberValuePair> pairs
		return new NormalAnnotationExpr(
				guessName(info.getCopy()),
				guessList(MemberValuePair.class, info.getCopy()));
	}

	public default SingleMemberAnnotationExpr guessSingleMemberAnnotationExpr(InformationWrapper info) {
		// final Name name
		// final Expression memberValue
		return new SingleMemberAnnotationExpr(
				guessName(info.getCopy()),
				guessNode( Expression.class,  info.getCopy()));
	}

	public default Parameter guessParameter(InformationWrapper info)  {
		// EnumSet<Modifier> modifiers
		// NodeList<AnnotationExpr> annotations
		// Type type
		// boolean isVarArgs
		// NodeList<AnnotationExpr> varArgsAnnotations
		// SimpleName name
		return new Parameter(
				guessModifiers(info.getCopy()), 
				guessList(AnnotationExpr.class, info.getCopy()), 
				guessNode( Type.class, info.getCopy()),
				guessBoolean(info.getCopy()),
				guessList(AnnotationExpr.class, info.getCopy()), 
				guessSimpleName(info.getCopy()));
	}

	public default EnclosedExpr guessEnclosedExpr(InformationWrapper info)  {
		// final Expression inner
		return new EnclosedExpr(
				guessNode( Expression.class,  info.getCopy()));
	}

	public default AssertStmt guessAssertStmt(InformationWrapper info)  {
		// final Expression check
		// final Expression message
		return new AssertStmt(
				guessNode( Expression.class,  info.getCopy()),
				guessNode( Expression.class,  info.getCopy()));
	}

	public default ConstructorDeclaration guessMemberValuePair(InformationWrapper info)  {
		// EnumSet<Modifier> modifiers
		// NodeList<AnnotationExpr> annotations
		// NodeList<TypeParameter> typeParameters
		// SimpleName name
		// NodeList<Parameter> parameters
		// NodeList<ReferenceType> thrownExceptions
		// BlockStmt body
		return new ConstructorDeclaration(
				guessModifiers(info.getCopy()), 
				guessList(AnnotationExpr.class, info.getCopy()), 
				guessList(TypeParameter.class, info.getCopy()), 
				guessSimpleName(info.getCopy()), 
				guessList(Parameter.class, info.getCopy()), 
				guessList(ReferenceType.class, info.getCopy()), 
				guessBlockStmt(info.getCopy()));
	}

	public default PrimitiveType guessPrimitiveType(InformationWrapper info)  {
		// final Primitive type
		return new PrimitiveType(
				guessPrimitive(info.getCopy()));
	}

	// this may never be used
	public default UnionType guessUnionType(InformationWrapper info)  {
		// NodeList<ReferenceType> elements
		return new UnionType(
				guessList(ReferenceType.class, info.getCopy()));
	}

	public default IntersectionType guessIntersectionType(InformationWrapper info)  {
		// NodeList<ReferenceType> elements
		return new IntersectionType( 
				guessList(ReferenceType.class, info.getCopy()));
	}

	public default TypeParameter guessTypeParameter(InformationWrapper info)  {
		// SimpleName name
		// NodeList<ClassOrInterfaceType> typeBound
		// NodeList<AnnotationExpr> annotations
		return new TypeParameter( 
				guessSimpleName(info.getCopy()), 
				guessList(ClassOrInterfaceType.class, info.getCopy()), 
				guessList(AnnotationExpr.class, info.getCopy()));
	}

	public default WildcardType guessWildcardType(InformationWrapper info)  {
		// final ReferenceType extendedType
		// final ReferenceType superType
		return new WildcardType(
				guessNode( ReferenceType.class, info.getCopy() ),
				guessNode( ReferenceType.class, info.getCopy() ));
	}

	public default VoidType guessVoidType(InformationWrapper info)  {return new VoidType();
	}

	public default UnknownType guessUnknownType(InformationWrapper info)  {
		// none
		return new UnknownType();
	}

	// only needed for debugging
	public default UnknownNode guessUnknown(InformationWrapper info)  {	
		// none
		return new UnknownNode();
	}

	public default Name guessName(InformationWrapper info)  {
		// final String identifier
		// NodeList<AnnotationExpr> annotations
		return new Name(
				guessName( info.getCopy() ), // this will return null eventually but is this a bug or a feature?
				guessStringValue(info.getCopy()),
				guessList(AnnotationExpr.class, info.getCopy()));
	}

	public default SimpleName guessSimpleName(InformationWrapper info) {
		// final String identifier
		return new SimpleName(
				guessStringValue(info.getCopy()));
	}

	public default LocalClassDeclarationStmt guessLocalClassDeclarationStmt(InformationWrapper info) {
		// final ClassOrInterfaceDeclaration classDeclaration
		return new LocalClassDeclarationStmt(
				guessClassOrInterfaceDeclaration( info.getCopy()));
	}
	public default ArrayType guessArrayType(InformationWrapper info)  {
		// Type componentType
		// NodeList<AnnotationExpr> annotations
		return new ArrayType(
				guessNode( Type.class, info.getCopy()), 
				guessList(AnnotationExpr.class, info.getCopy()));
	}

	public default ArrayCreationLevel guessArrayCreationLevel(InformationWrapper info)  {
		// Expression dimension
		// NodeList<AnnotationExpr> annotations
		return new ArrayCreationLevel(
				guessNode( Expression.class, info.getCopy()), 
				guessList(AnnotationExpr.class, info.getCopy()));
	}

	public default ModuleDeclaration guessModuleDeclaration(InformationWrapper info) {
		// NodeList<AnnotationExpr> annotations
		// Name name
		// boolean isOpen
		// NodeList<ModuleStmt> moduleStmts
		return new ModuleDeclaration( 
				guessList(AnnotationExpr.class, info.getCopy()), 
				guessName(info.getCopy()),
				guessBoolean(info.getCopy()),
				guessList(ModuleStmt.class, info.getCopy()));
	}

	public default ModuleExportsStmt guessModuleExportsStmt(InformationWrapper info) {
		// Name name
		// NodeList<Name> moduleNames
		return new ModuleExportsStmt( 
				guessName(info.getCopy()),
				guessList(Name.class, info.getCopy()));
	}

	public default ModuleOpensStmt guessModuleOpensStmt(InformationWrapper info) {
		// Name name
		// NodeList<Name> moduleNames
		return new ModuleOpensStmt( 
				guessName(info.getCopy()),
				guessList(Name.class, info.getCopy()));
	}

	public default ModuleProvidesStmt guessModuleProvidesStmt(InformationWrapper info) {
		// Type type
		// NodeList<Type> withTypes
		return new ModuleProvidesStmt( 
				guessNode( Type.class, info.getCopy()),
				guessList(Type.class, info.getCopy()));
	}

	public default ModuleRequiresStmt guessModuleRequiresStmt(InformationWrapper info) {
		// EnumSet<Modifier> modifiers
		// Name name
		return new ModuleRequiresStmt( 
				guessModifiers(info.getCopy()), 
				guessName(info.getCopy()));
	}

	public default ModuleUsesStmt guessModuleUsesStmt(InformationWrapper info) {
		// Type type
		return new ModuleUsesStmt( 
				guessNode( Type.class, info.getCopy()));
	}

	public default CompilationUnit guessCompilationUnit(InformationWrapper info) {
		// PackageDeclaration packageDeclaration
		// NodeList<ImportDeclaration> imports
		// NodeList<TypeDeclaration<?>> types
		// ModuleDeclaration module
		return new CompilationUnit( 
				guessNode( PackageDeclaration.class, info.getCopy()),
				guessList(ImportDeclaration.class, info.getCopy()),
				guessTypeDeclarationList(info.getCopy()),
				guessNode( ModuleDeclaration.class, info.getCopy()));
	}


}
