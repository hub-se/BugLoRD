package se.de.hu_berlin.informatik.astlmbuilder.parsing.guesser;

import java.util.EnumSet;
import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
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
import com.github.javaparser.ast.modules.ModuleStmt;
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
import com.github.javaparser.ast.type.PrimitiveType.Primitive;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;

import se.de.hu_berlin.informatik.astlmbuilder.parsing.InformationWrapper;

public interface INodeGuesser extends INodeGuesserBasics {

	public final static String DEFAULT_STRING_LITERAL_VALUE = "default string value";
	public final static String DEFAULT_SIMPLE_NAME_VALUE = "default simple name value";
	
	/**
	 * This value will be used if no other strings are available but the node needs
	 * one to exist.
	 * @return A default string that indicates that no real information were given
	 */
	public default String getDefaultStringLiteralValue() {
		return DEFAULT_STRING_LITERAL_VALUE;
	}
	
	/**
	 * This value will be used if no other strings are available but the node needs
	 * one to exist.
	 * @return A default string that indicates that no real information were given
	 */
	public default String getDefaultSimpleNameValue() {
		return DEFAULT_SIMPLE_NAME_VALUE;
	}
	
	default public <T extends Node> InformationWrapper updateGeneralInfo(Class<T> lastSeenNodeClass,
			InformationWrapper info, boolean useCopy) {
		if (useCopy) {
			info = info.getCopy();
		}
		info.addNodeClassToHistory(lastSeenNodeClass);

		return info;
	}

	// TODO: fill information wrapper with useful information on the way...
	// (e.g. last seen nodes, etc.)

	default public ConstructorDeclaration guessConstructorDeclaration(InformationWrapper info) {
		info = updateGeneralInfo(ConstructorDeclaration.class, info, false);

		EnumSet<Modifier> modifiers = guessModifiers(info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		NodeList<TypeParameter> typeParameters = guessList(TypeParameter.class, info);
		SimpleName name = guessSimpleName(info);
		NodeList<Parameter> parameters = guessList(Parameter.class, info);
		NodeList<ReferenceType> thrownExceptions = guessList(ReferenceType.class, info);
		BlockStmt body = guessBlockStmt(info);

		return new ConstructorDeclaration(modifiers, annotations, typeParameters, name, parameters, thrownExceptions,
				body);
	}

	public default InitializerDeclaration guessInitializerDeclaration(InformationWrapper info) {
		info = updateGeneralInfo(InitializerDeclaration.class, info, false);

		boolean isStatic = guessBoolean(info);
		BlockStmt body = guessBlockStmt(info);

		return new InitializerDeclaration(isStatic, body);
	}

	public default EnumConstantDeclaration guessEnumConstantDeclaration(InformationWrapper info) {
		info = updateGeneralInfo(EnumConstantDeclaration.class, info, false);

		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		SimpleName name = guessSimpleName(info);
		NodeList<Expression> arguments = guessList(Expression.class, info);
		NodeList<BodyDeclaration<?>> classBody = guessBodyDeclarationList(info);

		return new EnumConstantDeclaration(annotations, name, arguments, classBody);
	}

    public default VariableDeclarator guessVariableDeclarator(InformationWrapper info) {
		info = updateGeneralInfo(VariableDeclarator.class, info, false);

		Type type = guessNode(Type.class, info);
		SimpleName name = guessSimpleName(info);
		Expression initializer = guessNode(Expression.class, info);

		return new VariableDeclarator(type, name, initializer);
	}

	public default EnumDeclaration guessEnumDeclaration(InformationWrapper info) {
		info = updateGeneralInfo(EnumDeclaration.class, info, false);

		EnumSet<Modifier> modifiers = guessModifiers(info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		SimpleName name = guessSimpleName(info);
		NodeList<ClassOrInterfaceType> implementedTypes = guessList(ClassOrInterfaceType.class, info);
		NodeList<EnumConstantDeclaration> entries = guessList(EnumConstantDeclaration.class, info);
		NodeList<BodyDeclaration<?>> members = guessBodyDeclarationList(info);

		return new EnumDeclaration(modifiers, annotations, name, implementedTypes, entries, members);
	}

	public default AnnotationDeclaration guessAnnotationDeclaration(InformationWrapper info) {
		info = updateGeneralInfo(AnnotationDeclaration.class, info, false);

		EnumSet<Modifier> modifiers = guessModifiers(info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		SimpleName name = guessSimpleName(info);
		NodeList<BodyDeclaration<?>> members = guessBodyDeclarationList(info);

		return new AnnotationDeclaration(modifiers, annotations, name, members);
	}

	public default AnnotationMemberDeclaration guessAnnotationMemberDeclaration(InformationWrapper info) {
		info = updateGeneralInfo(AnnotationMemberDeclaration.class, info, false);

		EnumSet<Modifier> modifiers = guessModifiers(info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		Type type = guessNode(Type.class, info);
		SimpleName name = guessSimpleName(info);
		Expression defaultValue = guessNode(Expression.class, info);

		return new AnnotationMemberDeclaration(modifiers, annotations, type, name, defaultValue);
	}

	public default WhileStmt guessWhileStmt(InformationWrapper info) {
		info = updateGeneralInfo(WhileStmt.class, info, false);

		Expression condition = guessNode(Expression.class, info);
		Statement body = guessBlockStmt(info);

		return new WhileStmt(condition, body);
	}

	public default TryStmt guessTryStmt(InformationWrapper info) {
		info = updateGeneralInfo(TryStmt.class, info, false);

		NodeList<VariableDeclarationExpr> resources = guessList(VariableDeclarationExpr.class, info);
		BlockStmt tryBlock = guessBlockStmt(info);
		NodeList<CatchClause> catchClauses = guessList(CatchClause.class, info);
		BlockStmt finallyBlock = guessBlockStmt(info);

		return new TryStmt(resources, tryBlock, catchClauses, finallyBlock);
	}

	public default ThrowStmt guessThrowStmt(InformationWrapper info) {
		info = updateGeneralInfo(ThrowStmt.class, info, false);

		Expression expression = guessNode(Expression.class, info);

		return new ThrowStmt(expression);
	}

	public default SynchronizedStmt guessSynchronizedStmt(InformationWrapper info) {
		info = updateGeneralInfo(SynchronizedStmt.class, info, false);

		Expression expression = guessNode(Expression.class, info);
		BlockStmt body = guessBlockStmt(info);

		return new SynchronizedStmt(expression, body);
	}

	public default SwitchStmt guessSwitchStmt(InformationWrapper info) {
		info = updateGeneralInfo(SwitchStmt.class, info, false);

		Expression selector = guessNode(Expression.class, info);
		NodeList<SwitchEntryStmt> entries = guessList(SwitchEntryStmt.class, info);

		return new SwitchStmt(selector, entries);
	}

	public default SwitchEntryStmt guessSwitchEntryStmt(InformationWrapper info) {
		info = updateGeneralInfo(SwitchEntryStmt.class, info, false);

		Expression label = guessNode(Expression.class, info);
		NodeList<Statement> statements = guessList(Statement.class, info);

		return new SwitchEntryStmt(label, statements);
	}

	public default ReturnStmt guessReturnStmt(InformationWrapper info) {
		info = updateGeneralInfo(ReturnStmt.class, info, false);

		Expression expression = guessNode(Expression.class, info);

		return new ReturnStmt(expression);
	}

	public default LabeledStmt guessLabeledStmt(InformationWrapper info) {
		info = updateGeneralInfo(LabeledStmt.class, info, false);

		SimpleName label = guessSimpleName(info);
		Statement statement = guessNode(Statement.class, info);

		return new LabeledStmt(label, statement);
	}

	public default IfStmt guessIfStmt(InformationWrapper info) {
		info = updateGeneralInfo(IfStmt.class, info, false);

		Expression condition = guessNode(Expression.class, info);
		Statement thenStmt = guessNode(Statement.class, info);
		Statement elseStmt = guessNode(Statement.class, info);

		return new IfStmt(condition, thenStmt, elseStmt);
	}

	public default ForStmt guessForStmt(InformationWrapper info) {
		info = updateGeneralInfo(ForStmt.class, info, false);

		NodeList<Expression> initialization = guessList(Expression.class, info);
		Expression compare = guessNode(Expression.class, info);
		NodeList<Expression> update = guessList(Expression.class, info);
		Statement body = guessNode(Statement.class, info);

		return new ForStmt(initialization, compare, update, body);
	}

	public default ForeachStmt guessForeachStmt(InformationWrapper info) {
		info = updateGeneralInfo(ForeachStmt.class, info, false);

		VariableDeclarationExpr variable = guessVariableDeclarationExpr(info);
		Expression iterable = guessNode(Expression.class, info);
		Statement body = guessNode(Statement.class, info);

		return new ForeachStmt(variable, iterable, body);
	}

	public default ExpressionStmt guessExpressionStmt(InformationWrapper info) {
		info = updateGeneralInfo(ExpressionStmt.class, info, false);

		Expression expression = guessNode(Expression.class, info);

		return new ExpressionStmt(expression);
	}

	public default ExplicitConstructorInvocationStmt guessExplicitConstructorInvocationStmt(InformationWrapper info) {
		info = updateGeneralInfo(ExplicitConstructorInvocationStmt.class, info, false);

		NodeList<Type> typeArguments = guessList(Type.class, info);
		boolean isThis = guessBoolean(info);
		Expression expression = guessNode(Expression.class, info);
		NodeList<Expression> arguments = guessList(Expression.class, info);

		return new ExplicitConstructorInvocationStmt(typeArguments, isThis, expression, arguments);
	}

	public default DoStmt guessDoStmt(InformationWrapper info) {
		info = updateGeneralInfo(DoStmt.class, info, false);

		Statement body = guessNode(Statement.class, info);
		Expression condition = guessNode(Expression.class, info);

		return new DoStmt(body, condition);
	}

	public default ContinueStmt guessContinueStmt(InformationWrapper info) {
		info = updateGeneralInfo(ContinueStmt.class, info, false);

		SimpleName label = guessSimpleName(info);

		return new ContinueStmt(label);
	}

	public default CatchClause guessCatchClause(InformationWrapper info) {
		info = updateGeneralInfo(CatchClause.class, info, false);

		Parameter parameter = guessParameter(info);
		BlockStmt body = guessBlockStmt(info);

		return new CatchClause(parameter, body);
	}

	public default BlockStmt guessBlockStmt(InformationWrapper info) {
		info = updateGeneralInfo(BlockStmt.class, info, false);

		NodeList<Statement> statements = guessList(Statement.class, info.getCopy());

		return new BlockStmt(statements);
	}

	public default VariableDeclarationExpr guessVariableDeclarationExpr(InformationWrapper info) {
		info = updateGeneralInfo(VariableDeclarationExpr.class, info, false);

		EnumSet<Modifier> modifiers = guessModifiers(info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		NodeList<VariableDeclarator> variables = guessList(VariableDeclarator.class, info);

		return new VariableDeclarationExpr(modifiers, annotations, variables);
	}

	public default TypeExpr guessTypeExpr(InformationWrapper info) {
		info = updateGeneralInfo(TypeExpr.class, info, false);

		Type type = guessNode(Type.class, info);

		return new TypeExpr(type);
	}

	public default SuperExpr guessSuperExpr(InformationWrapper info) {
		info = updateGeneralInfo(SuperExpr.class, info, false);

		Expression classExpr = guessNode(Expression.class, info);

		return new SuperExpr(classExpr);
	}

	public default NullLiteralExpr guessNullLiteralExpr(InformationWrapper info) {
		return new NullLiteralExpr();
	}

	public default MethodReferenceExpr guessMethodReferenceExpr(InformationWrapper info) {
		info = updateGeneralInfo(MethodReferenceExpr.class, info, false);

		Expression scope = guessNode(Expression.class, info);
		NodeList<Type> typeArguments = guessList(Type.class, info);
		String identifier = guessMethodIdentifier(info);

		return new MethodReferenceExpr(scope, typeArguments, identifier);
	}

	public default LambdaExpr guessLambdaExpr(InformationWrapper info) {
		info = updateGeneralInfo(LambdaExpr.class, info, false);

		NodeList<Parameter> parameters = guessList(Parameter.class, info);
		Statement body = guessNode(Statement.class, info);
		boolean isEnclosingParameters = guessBoolean(info);

		return new LambdaExpr(parameters, body, isEnclosingParameters);
	}

	public default InstanceOfExpr guessInstanceOfExpr(InformationWrapper info) {
		info = updateGeneralInfo(InstanceOfExpr.class, info, false);

		Expression expression = guessNode(Expression.class, info);
		ReferenceType type = guessNode(ReferenceType.class, info);

		return new InstanceOfExpr(expression, type);
	}

	public default FieldAccessExpr guessFieldAccessExpr(InformationWrapper info) {
		info = updateGeneralInfo(FieldAccessExpr.class, info, false);

		Expression scope = guessNode(Expression.class, info);
		NodeList<Type> typeArguments = guessList(Type.class, info);
		SimpleName name = guessSimpleName(info);

		return new FieldAccessExpr(scope, typeArguments, name);
	}

	public default ConditionalExpr guessConditionalExpr(InformationWrapper info) {
		info = updateGeneralInfo(ConditionalExpr.class, info, false);

		Expression condition = guessNode(Expression.class, info);
		Expression thenExpr = guessNode(Expression.class, info);
		Expression elseExpr = guessNode(Expression.class, info);

		return new ConditionalExpr(condition, thenExpr, elseExpr);
	}

	public default ClassExpr guessClassExpr(InformationWrapper info) {
		info = updateGeneralInfo(ClassExpr.class, info, false);

		Type type = guessNode(Type.class, info);

		return new ClassExpr(type);
	}

	public default CastExpr guessCastExpr(InformationWrapper info) {
		info = updateGeneralInfo(CastExpr.class, info, false);

		Type type = guessNode(Type.class, info);
		Expression expression = guessNode(Expression.class, info);

		return new CastExpr(type, expression);
	}

	public default AssignExpr guessAssignExpr(InformationWrapper info) {
		info = updateGeneralInfo(AssignExpr.class, info, false);

		Expression target = guessNode(Expression.class, info);
		Expression value = guessNode(Expression.class, info);
		AssignExpr.Operator operator = guessAssignOperator(info);

		return new AssignExpr(target, value, operator);
	}

	public default ArrayInitializerExpr guessArrayInitializerExpr(InformationWrapper info) {
		info = updateGeneralInfo(ArrayInitializerExpr.class, info, false);

		NodeList<Expression> values = guessList(Expression.class, info);

		return new ArrayInitializerExpr(values);
	}

	public default ArrayCreationExpr guessArrayCreationExpr(InformationWrapper info) {
		info = updateGeneralInfo(ArrayCreationExpr.class, info, false);

		Type elementType = guessNode(Type.class, info);
		NodeList<ArrayCreationLevel> levels = guessList(ArrayCreationLevel.class, info);
		ArrayInitializerExpr initializer = guessArrayInitializerExpr(info);

		return new ArrayCreationExpr(elementType, levels, initializer);
	}

	public default ArrayAccessExpr guessArrayAccessExpr(InformationWrapper info) {
		info = updateGeneralInfo(ArrayAccessExpr.class, info, false);

		Expression name = guessNode(Expression.class, info);
		Expression index = guessNode(Expression.class, info);

		return new ArrayAccessExpr(name, index);
	}

	public default PackageDeclaration guessPackageDeclaration(InformationWrapper info) {
		info = updateGeneralInfo(PackageDeclaration.class, info, false);

		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		Name name = guessName(info);

		return new PackageDeclaration(annotations, name);
	}

	public default ImportDeclaration guessImportDeclaration(InformationWrapper info) {
		info = updateGeneralInfo(ImportDeclaration.class, info, false);

		Name name = guessName(info);
		boolean isStatic = guessBoolean(info);
		boolean isAsterisk = guessBoolean(info);

		return new ImportDeclaration(name, isStatic, isAsterisk);
	}

	public default FieldDeclaration guessFieldDeclaration(InformationWrapper info) {
		info = updateGeneralInfo(FieldDeclaration.class, info, false);

		EnumSet<Modifier> modifiers = guessModifiers(info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		NodeList<VariableDeclarator> variables = guessList(VariableDeclarator.class, info);

		return new FieldDeclaration(modifiers, annotations, variables);
	}

	public default ClassOrInterfaceType guessClassOrInterfaceType(InformationWrapper info) {
		info = updateGeneralInfo(ClassOrInterfaceType.class, info, false);

		ClassOrInterfaceType scope = guessClassOrInterfaceType(info);
		SimpleName name = guessSimpleName(info);
		NodeList<Type> typeArguments = guessList(Type.class, info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);

		return new ClassOrInterfaceType(scope, name, typeArguments, annotations);
	}

	public default ClassOrInterfaceDeclaration guessClassOrInterfaceDeclaration(InformationWrapper info) {
		info = updateGeneralInfo(ClassOrInterfaceDeclaration.class, info, false);

		EnumSet<Modifier> modifiers = guessModifiers(info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		boolean isInterface = guessBoolean(info);
		SimpleName name = guessSimpleName(info);
		NodeList<TypeParameter> typeParameters = guessList(TypeParameter.class, info);
		NodeList<ClassOrInterfaceType> extendedTypes = guessList(ClassOrInterfaceType.class, info);
		NodeList<ClassOrInterfaceType> implementedTypes = guessList(ClassOrInterfaceType.class, info);
		NodeList<BodyDeclaration<?>> members = guessBodyDeclarationList(info);

		return new ClassOrInterfaceDeclaration(modifiers, annotations, isInterface, name, typeParameters, extendedTypes,
				implementedTypes, members);
	}

	public default MethodDeclaration guessMethodDeclaration(InformationWrapper info) {
		info = updateGeneralInfo(MethodDeclaration.class, info, false);

		EnumSet<Modifier> modifiers = guessModifiers(info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		NodeList<TypeParameter> typeParameters = guessList(TypeParameter.class, info);
		Type type = guessNode(Type.class, info);
		SimpleName name = guessSimpleName(info);
		NodeList<Parameter> parameters = guessList(Parameter.class, info);
		NodeList<ReferenceType> thrownExceptions = guessList(ReferenceType.class, info);
		BlockStmt body = guessBlockStmt(info);

		return new MethodDeclaration(modifiers, annotations, typeParameters, type, name, parameters,
				thrownExceptions, body);
	}

	public default BinaryExpr guessBinaryExpr(InformationWrapper info) {
		info = updateGeneralInfo(BinaryExpr.class, info, false);

		Expression left = guessNode(Expression.class, info);
		Expression right = guessNode(Expression.class, info);
		BinaryExpr.Operator operator = guessBinaryOperator(info);

		return new BinaryExpr(left, right, operator);
	}

	public default UnaryExpr guessUnaryExpr(InformationWrapper info) {
		info = updateGeneralInfo(UnaryExpr.class, info, false);

		Expression expression = guessNode(Expression.class, info);
		UnaryExpr.Operator operator = guessUnaryOperator(info);

		return new UnaryExpr(expression, operator);
	}

	public default MethodCallExpr guessMethodCallExpr(InformationWrapper info) {
		info = updateGeneralInfo(MethodCallExpr.class, info, false);

		Expression scope = guessNode(Expression.class, info);
		NodeList<Type> typeArguments = guessList(Type.class, info);
		SimpleName name = guessSimpleName(info);
		NodeList<Expression> arguments = guessList(Expression.class, info);

		return new MethodCallExpr(scope, typeArguments, name, arguments);
	}

	public default NameExpr guessNameExpr(InformationWrapper info) {
		info = updateGeneralInfo(NameExpr.class, info, false);

		SimpleName name = guessSimpleName(info);

		return new NameExpr(name);
	}

	public default IntegerLiteralExpr guessIntegerLiteralExpr(InformationWrapper info) throws IllegalArgumentException {
		info = updateGeneralInfo(IntegerLiteralExpr.class, info, false);

		String value = guessStringValue(info);

		return new IntegerLiteralExpr(value);
	}

	public default DoubleLiteralExpr guessDoubleLiteralExpr(InformationWrapper info) {
		info = updateGeneralInfo(DoubleLiteralExpr.class, info, false);

		String value = guessStringValue(info);

		return new DoubleLiteralExpr(value);
	}

	public default StringLiteralExpr guessStringLiteralExpr(InformationWrapper info) {
		info = updateGeneralInfo(StringLiteralExpr.class, info, false);

		String value = guessStringValue(info);

		// this could also be part of the guessStringValue method
		if( value == null ) {
			// creating a StringLiteralExpr with a null value is not allowed
			// but we do not store the values in the tokens so we need some alternative value
			// we may need more of those default values and a good place for them
			value = DEFAULT_STRING_LITERAL_VALUE;
		}
		
		return new StringLiteralExpr(value);
	}

	public default BooleanLiteralExpr guessBooleanLiteralExpr(InformationWrapper info) {
		info = updateGeneralInfo(BooleanLiteralExpr.class, info, false);

		boolean value = guessBoolean(info);

		return new BooleanLiteralExpr(value);
	}

	public default CharLiteralExpr guessCharLiteralExpr(InformationWrapper info) {
		info = updateGeneralInfo(CharLiteralExpr.class, info, false);

		String value = guessStringValue(info);

		return new CharLiteralExpr(value);
	}

	public default LongLiteralExpr guessLongLiteralExpr(InformationWrapper info) {
		info = updateGeneralInfo(LongLiteralExpr.class, info, false);

		String value = guessStringValue(info);

		return new LongLiteralExpr(value);
	}

	public default ThisExpr guessThisExpr(InformationWrapper info) {
		info = updateGeneralInfo(ThisExpr.class, info, false);

		Expression classExpr = guessNode(Expression.class, info);

		return new ThisExpr(classExpr);
	}

	public default BreakStmt guessBreakStmt(InformationWrapper info) {
		info = updateGeneralInfo(BreakStmt.class, info, false);

		SimpleName label = guessSimpleName(info);

		return new BreakStmt(label);
	}

	public default ObjectCreationExpr guessObjectCreationExpr(InformationWrapper info) {
		info = updateGeneralInfo(ObjectCreationExpr.class, info, false);

		Expression scope = guessNode(Expression.class, info);
		ClassOrInterfaceType type = guessClassOrInterfaceType(info);
		NodeList<Type> typeArguments = guessList(Type.class, info);
		NodeList<Expression> arguments = guessList(Expression.class, info);
		NodeList<BodyDeclaration<?>> anonymousClassBody = guessBodyDeclarationList(info);

		return new ObjectCreationExpr(scope, type, typeArguments, arguments, anonymousClassBody);
	}

	public default MarkerAnnotationExpr guessMarkerAnnotationExpr(InformationWrapper info) {
		info = updateGeneralInfo(MarkerAnnotationExpr.class, info, false);

		Name name = guessName(info);

		return new MarkerAnnotationExpr(name);
	}

	public default NormalAnnotationExpr guessNormalAnnotationExpr(InformationWrapper info) {
		info = updateGeneralInfo(NormalAnnotationExpr.class, info, false);

		Name name = guessName(info);
		NodeList<MemberValuePair> pairs = guessList(MemberValuePair.class, info);

		return new NormalAnnotationExpr(name, pairs);
	}

	public default SingleMemberAnnotationExpr guessSingleMemberAnnotationExpr(InformationWrapper info) {
		info = updateGeneralInfo(SingleMemberAnnotationExpr.class, info, false);

		Name name = guessName(info);
		Expression memberValue = guessNode(Expression.class, info);

		return new SingleMemberAnnotationExpr(name, memberValue);
	}

	public default Parameter guessParameter(InformationWrapper info) {
		info = updateGeneralInfo(Parameter.class, info, false);

		EnumSet<Modifier> modifiers = guessModifiers(info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		Type type = guessNode(Type.class, info);
		boolean isVarArgs = guessBoolean(info);
		NodeList<AnnotationExpr> varArgsAnnotations = guessList(AnnotationExpr.class, info);
		SimpleName name = guessSimpleName(info);

		return new Parameter(modifiers, annotations, type, isVarArgs, varArgsAnnotations, name);
	}

	public default EnclosedExpr guessEnclosedExpr(InformationWrapper info) {
		info = updateGeneralInfo(EnclosedExpr.class, info, false);

		Expression inner = guessNode(Expression.class, info);

		return new EnclosedExpr(inner);
	}

	public default AssertStmt guessAssertStmt(InformationWrapper info) {
		info = updateGeneralInfo(AssertStmt.class, info, false);

		Expression check = guessNode(Expression.class, info);
		Expression message = guessNode(Expression.class, info);

		return new AssertStmt(check, message);
	}

	public default MemberValuePair guessMemberValuePair(InformationWrapper info) throws IllegalArgumentException {
		info = updateGeneralInfo(MemberValuePair.class, info, false);

		SimpleName name = guessSimpleName(info);
		Expression value = guessNode(Expression.class, info);

		return new MemberValuePair(name, value);
	}

	public default PrimitiveType guessPrimitiveType(InformationWrapper info) {
		info = updateGeneralInfo(PrimitiveType.class, info, false);

		Primitive type = guessPrimitive(info);

		return new PrimitiveType(type);
	}

	public default UnionType guessUnionType(InformationWrapper info) {
		info = updateGeneralInfo(UnionType.class, info, false);

		NodeList<ReferenceType> elements = guessList(ReferenceType.class, info);

		return new UnionType(elements);
	}

	public default IntersectionType guessIntersectionType(InformationWrapper info) {
		info = updateGeneralInfo(IntersectionType.class, info, false);

		NodeList<ReferenceType> elements = guessList(ReferenceType.class, info);

		return new IntersectionType(elements);
	}

	public default TypeParameter guessTypeParameter(InformationWrapper info) {
		info = updateGeneralInfo(TypeParameter.class, info, false);

		SimpleName name = guessSimpleName(info);
		NodeList<ClassOrInterfaceType> typeBound = guessList(ClassOrInterfaceType.class, info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);

		return new TypeParameter(name, typeBound, annotations);
	}

	public default WildcardType guessWildcardType(InformationWrapper info) {
		info = updateGeneralInfo(WildcardType.class, info, false);

		ReferenceType extendedType = guessNode(ReferenceType.class, info);
		ReferenceType superType = guessNode(ReferenceType.class, info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);

		return new WildcardType(extendedType, superType, annotations);
	}

	public default VoidType guessVoidType(InformationWrapper info) {
		info = updateGeneralInfo(VoidType.class, info, false);

		return new VoidType();
	}

	public default UnknownType guessUnknownType(InformationWrapper info) {
		info = updateGeneralInfo(UnknownType.class, info, false);

		return new UnknownType();
	}

	public default Name guessName(InformationWrapper info) {
		info = updateGeneralInfo(Name.class, info, false);

		// TODO: this is a recursive call that will possibly call itself indefinitely...
		Name qualifier = guessName(info);
		String identifier = guessStringValue(info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);

		return new Name(qualifier, identifier, annotations);
	}

	public default SimpleName guessSimpleName(InformationWrapper info) {
		info = updateGeneralInfo(SimpleName.class, info, false);

		String identifier = guessStringValue(info);

		// this could also be part of the guessStringValue method
		if( identifier == null ) {
			// creating a SimpleName with a null value is not allowed
			// but we do not store the values in the tokens so we need some alternative value
			// we may need more of those default values and a good place for them
			identifier = DEFAULT_SIMPLE_NAME_VALUE;
		}
		
		return new SimpleName(identifier);
	}

	public default LocalClassDeclarationStmt guessLocalClassDeclarationStmt(InformationWrapper info) {
		info = updateGeneralInfo(LocalClassDeclarationStmt.class, info, false);

		ClassOrInterfaceDeclaration classDeclaration = guessClassOrInterfaceDeclaration(info);

		return new LocalClassDeclarationStmt(classDeclaration);
	}

	public default ArrayType guessArrayType(InformationWrapper info) {
		info = updateGeneralInfo(ArrayType.class, info, false);

		Type componentType = guessNode(Type.class, info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);

		return new ArrayType(componentType, annotations);
	}

	public default ArrayCreationLevel guessArrayCreationLevel(InformationWrapper info) {
		info = updateGeneralInfo(ArrayCreationLevel.class, info, false);

		Expression dimension = guessNode(Expression.class, info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);

		return new ArrayCreationLevel(dimension, annotations);
	}

	public default ModuleDeclaration guessModuleDeclaration(InformationWrapper info) {
		info = updateGeneralInfo(ModuleDeclaration.class, info, false);

		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		Name name = guessName(info);
		boolean isOpen = guessBoolean(info);
		NodeList<ModuleStmt> moduleStmts = guessList(ModuleStmt.class, info);

		return new ModuleDeclaration(annotations, name, isOpen, moduleStmts);
	}

	public default ModuleExportsStmt guessModuleExportsStmt(InformationWrapper info) {
		info = updateGeneralInfo(ModuleExportsStmt.class, info, false);

		Name name = guessName(info);
		NodeList<Name> moduleNames = guessList(Name.class, info);

		return new ModuleExportsStmt(name, moduleNames);
	}

	public default ModuleOpensStmt guessModuleOpensStmt(InformationWrapper info) {
		info = updateGeneralInfo(ModuleOpensStmt.class, info, false);

		Name name = guessName(info);
		NodeList<Name> moduleNames = guessList(Name.class, info);

		return new ModuleOpensStmt(name, moduleNames);
	}

	public default ModuleProvidesStmt guessModuleProvidesStmt(InformationWrapper info) {
		info = updateGeneralInfo(ModuleProvidesStmt.class, info, false);

		Type type = guessNode(Type.class, info);
		NodeList<Type> withTypes = guessList(Type.class, info);

		return new ModuleProvidesStmt(type, withTypes);
	}

	public default ModuleRequiresStmt guessModuleRequiresStmt(InformationWrapper info) {
		info = updateGeneralInfo(ModuleRequiresStmt.class, info, false);

		EnumSet<Modifier> modifiers = guessModifiers(info);
		Name name = guessName(info);

		return new ModuleRequiresStmt(modifiers, name);
	}

	public default ModuleUsesStmt guessModuleUsesStmt(InformationWrapper info) {
		info = updateGeneralInfo(ModuleUsesStmt.class, info, false);

		Type type = guessNode(Type.class, info);

		return new ModuleUsesStmt(type);
	}

	public default CompilationUnit guessCompilationUnit(InformationWrapper info) {
		info = updateGeneralInfo(CompilationUnit.class, info, false);

		PackageDeclaration packageDeclaration = guessNode(PackageDeclaration.class, info);
		NodeList<ImportDeclaration> imports = guessList(ImportDeclaration.class, info);
		NodeList<TypeDeclaration<?>> types = guessTypeDeclarationList(info);
		ModuleDeclaration module = guessNode(ModuleDeclaration.class, info);

		return new CompilationUnit(packageDeclaration, imports, types, module);
	}
	
	public default EmptyStmt guessEmptyStmt(InformationWrapper info) {
		info = updateGeneralInfo(EmptyStmt.class, info, false);

		return new EmptyStmt();
	}

}
