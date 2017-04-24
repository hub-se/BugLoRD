package se.de.hu_berlin.informatik.astlmbuilder.parsing.guesser;

import java.util.EnumSet;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
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
import com.github.javaparser.ast.type.PrimitiveType.Primitive;
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

	// TODO: fill information wrapper with useful information on the way...
	// (e.g. last seen nodes, etc.)

	default public ConstructorDeclaration guessConstructorDeclaration(InformationWrapper info) {
		info.addNodeClassToHistory(ConstructorDeclaration.class);

		EnumSet<Modifier> modifiers = guessModifiers(info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		NodeList<TypeParameter> typeParameters = guessList(TypeParameter.class, info);
		SimpleName name = guessSimpleName(info);
		NodeList<Parameter> parameters = guessList(Parameter.class, info);
		@SuppressWarnings("rawtypes")
		NodeList<ReferenceType> thrownExceptions = guessList(ReferenceType.class, info);
		BlockStmt body = guessBlockStmt(info);

		return new ConstructorDeclaration(modifiers, annotations, typeParameters, name, parameters, thrownExceptions,
				body);
	}

	public default InitializerDeclaration guessInitializerDeclaration(InformationWrapper info) {
		info.addNodeClassToHistory(InitializerDeclaration.class);

		boolean isStatic = guessBoolean(info);
		BlockStmt body = guessBlockStmt(info);

		return new InitializerDeclaration(isStatic, body);
	}

	public default EnumConstantDeclaration guessEnumConstantDeclaration(InformationWrapper info) {
		info.addNodeClassToHistory(EnumConstantDeclaration.class);

		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		SimpleName name = guessSimpleName(info);
		NodeList<Expression> arguments = guessList(Expression.class, info);
		NodeList<BodyDeclaration<?>> classBody = guessBodyDeclarationList(info);

		return new EnumConstantDeclaration(annotations, name, arguments, classBody);
	};

	public default VariableDeclarator guessVariableDeclarator(InformationWrapper info) {
		info.addNodeClassToHistory(VariableDeclarator.class);

		Type type = guessNode(Type.class, info);
		SimpleName name = guessSimpleName(info);
		Expression initializer = guessNode(Expression.class, info);

		return new VariableDeclarator(type, name, initializer);
	}

	public default EnumDeclaration guessEnumDeclaration(InformationWrapper info) {
		info.addNodeClassToHistory(EnumDeclaration.class);

		EnumSet<Modifier> modifiers = guessModifiers(info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		SimpleName name = guessSimpleName(info);
		NodeList<ClassOrInterfaceType> implementedTypes = guessList(ClassOrInterfaceType.class, info);
		NodeList<EnumConstantDeclaration> entries = guessList(EnumConstantDeclaration.class, info);
		NodeList<BodyDeclaration<?>> members = guessBodyDeclarationList(info);

		return new EnumDeclaration(modifiers, annotations, name, implementedTypes, entries, members);
	}

	public default AnnotationDeclaration guessAnnotationDeclaration(InformationWrapper info) {
		info.addNodeClassToHistory(AnnotationDeclaration.class);

		EnumSet<Modifier> modifiers = guessModifiers(info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		SimpleName name = guessSimpleName(info);
		NodeList<BodyDeclaration<?>> members = guessBodyDeclarationList(info);

		return new AnnotationDeclaration(modifiers, annotations, name, members);
	}

	public default AnnotationMemberDeclaration guessAnnotationMemberDeclaration(InformationWrapper info) {
		info.addNodeClassToHistory(AnnotationMemberDeclaration.class);

		EnumSet<Modifier> modifiers = guessModifiers(info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		Type type = guessNode(Type.class, info);
		SimpleName name = guessSimpleName(info);
		Expression defaultValue = guessNode(Expression.class, info);

		return new AnnotationMemberDeclaration(modifiers, annotations, type, name, defaultValue);
	}

	public default WhileStmt guessWhileStmt(InformationWrapper info) {
		info.addNodeClassToHistory(WhileStmt.class);

		Expression condition = guessNode(Expression.class, info);
		Statement body = guessBlockStmt(info);

		return new WhileStmt(condition, body);
	}

	public default TryStmt guessTryStmt(InformationWrapper info) {
		info.addNodeClassToHistory(TryStmt.class);

		NodeList<VariableDeclarationExpr> resources = guessList(VariableDeclarationExpr.class, info);
		BlockStmt tryBlock = guessBlockStmt(info);
		NodeList<CatchClause> catchClauses = guessList(CatchClause.class, info);
		BlockStmt finallyBlock = guessBlockStmt(info);

		return new TryStmt(resources, tryBlock, catchClauses, finallyBlock);
	}

	public default ThrowStmt guessThrowStmt(InformationWrapper info) {
		info.addNodeClassToHistory(ThrowStmt.class);

		Expression expression = guessNode(Expression.class, info);

		return new ThrowStmt(expression);
	}

	public default SynchronizedStmt guessSynchronizedStmt(InformationWrapper info) {
		info.addNodeClassToHistory(SynchronizedStmt.class);

		Expression expression = guessNode(Expression.class, info);
		BlockStmt body = guessBlockStmt(info);

		return new SynchronizedStmt(expression, body);
	}

	public default SwitchStmt guessSwitchStmt(InformationWrapper info) {
		info.addNodeClassToHistory(SwitchStmt.class);

		Expression selector = guessNode(Expression.class, info);
		NodeList<SwitchEntryStmt> entries = guessList(SwitchEntryStmt.class, info);

		return new SwitchStmt(selector, entries);
	}

	public default SwitchEntryStmt guessSwitchEntryStmt(InformationWrapper info) {
		info.addNodeClassToHistory(SwitchEntryStmt.class);

		Expression label = guessNode(Expression.class, info);
		NodeList<Statement> statements = guessList(Statement.class, info);

		return new SwitchEntryStmt(label, statements);
	}

	public default ReturnStmt guessReturnStmt(InformationWrapper info) {
		info.addNodeClassToHistory(ReturnStmt.class);

		Expression expression = guessNode(Expression.class, info);

		return new ReturnStmt(expression);
	}

	public default LabeledStmt guessLabeledStmt(InformationWrapper info) {
		info.addNodeClassToHistory(LabeledStmt.class);

		String label = guessStringValue(info);
		Statement statement = guessNode(Statement.class, info);

		return new LabeledStmt(label, statement);
	}

	public default IfStmt guessIfStmt(InformationWrapper info) {
		info.addNodeClassToHistory(IfStmt.class);

		Expression condition = guessNode(Expression.class, info);
		Statement thenStmt = guessNode(Statement.class, info);
		Statement elseStmt = guessNode(Statement.class, info);

		return new IfStmt(condition, thenStmt, elseStmt);
	}

	public default ForStmt guessForStmt(InformationWrapper info) {
		info.addNodeClassToHistory(ForStmt.class);

		NodeList<Expression> initialization = guessList(Expression.class, info);
		Expression compare = guessNode(Expression.class, info);
		NodeList<Expression> update = guessList(Expression.class, info);
		Statement body = guessNode(Statement.class, info);

		return new ForStmt(initialization, compare, update, body);
	}

	public default ForeachStmt guessForeachStmt(InformationWrapper info) {
		info.addNodeClassToHistory(ForeachStmt.class);

		VariableDeclarationExpr variable = guessVariableDeclarationExpr(info);
		Expression iterable = guessNode(Expression.class, info);
		Statement body = guessNode(Statement.class, info);

		return new ForeachStmt(variable, iterable, body);
	}

	public default ExpressionStmt guessExpressionStmt(InformationWrapper info) {
		info.addNodeClassToHistory(ExpressionStmt.class);

		Expression expression = guessNode(Expression.class, info);

		return new ExpressionStmt(expression);
	}

	public default ExplicitConstructorInvocationStmt guessExplicitConstructorInvocationStmt(InformationWrapper info) {
		info.addNodeClassToHistory(ExplicitConstructorInvocationStmt.class);

		boolean isThis = guessBoolean(info);
		Expression expression = guessNode(Expression.class, info);
		NodeList<Expression> arguments = guessList(Expression.class, info);

		return new ExplicitConstructorInvocationStmt(isThis, expression, arguments);
	}

	public default DoStmt guessDoStmt(InformationWrapper info) {
		info.addNodeClassToHistory(DoStmt.class);

		Statement body = guessNode(Statement.class, info);
		Expression condition = guessNode(Expression.class, info);

		return new DoStmt(body, condition);
	}

	public default ContinueStmt guessContinueStmt(InformationWrapper info) {
		info.addNodeClassToHistory(ContinueStmt.class);

		SimpleName label = guessSimpleName(info);

		return new ContinueStmt(label);
	}

	public default CatchClause guessCatchClause(InformationWrapper info) {
		info.addNodeClassToHistory(CatchClause.class);

		EnumSet<Modifier> exceptModifier = guessModifiers(info);
		NodeList<AnnotationExpr> exceptAnnotations = guessList(AnnotationExpr.class, info);
		ClassOrInterfaceType exceptType = guessClassOrInterfaceType(info);
		SimpleName exceptName = guessSimpleName(info);
		BlockStmt body = guessBlockStmt(info);

		return new CatchClause(exceptModifier, exceptAnnotations, exceptType, exceptName, body);
	}

	public default BlockStmt guessBlockStmt(InformationWrapper info) {
		info.addNodeClassToHistory(BlockStmt.class);

		NodeList<Statement> statements = guessList(Statement.class, info.getCopy());

		return new BlockStmt(statements);
	}

	public default VariableDeclarationExpr guessVariableDeclarationExpr(InformationWrapper info) {
		info.addNodeClassToHistory(VariableDeclarationExpr.class);

		EnumSet<Modifier> modifiers = guessModifiers(info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		NodeList<VariableDeclarator> variables = guessList(VariableDeclarator.class, info);

		return new VariableDeclarationExpr(modifiers, annotations, variables);
	}

	public default TypeExpr guessTypeExpr(InformationWrapper info) {
		info.addNodeClassToHistory(TypeExpr.class);

		Type type = guessNode(Type.class, info);

		return new TypeExpr(type);
	}

	public default SuperExpr guessSuperExpr(InformationWrapper info) {
		info.addNodeClassToHistory(SuperExpr.class);

		Expression classExpr = guessNode(Expression.class, info);

		return new SuperExpr(classExpr);
	}

	public default NullLiteralExpr guessNullLiteralExpr(InformationWrapper info) {
		return new NullLiteralExpr();
	}

	public default MethodReferenceExpr guessMethodReferenceExpr(InformationWrapper info) {
		info.addNodeClassToHistory(MethodReferenceExpr.class);

		Expression scope = guessNode(Expression.class, info);
		NodeList<Type> typeArguments = guessList(Type.class, info);
		String identifier = guessMethodIdentifier(info);

		return new MethodReferenceExpr(scope, typeArguments, identifier);
	}

	public default LambdaExpr guessLambdaExpr(InformationWrapper info) {
		info.addNodeClassToHistory(LambdaExpr.class);

		NodeList<Parameter> parameters = guessList(Parameter.class, info);
		Statement body = guessNode(Statement.class, info);
		boolean isEnclosingParameters = guessBoolean(info);

		return new LambdaExpr(parameters, body, isEnclosingParameters);
	}

	public default InstanceOfExpr guessInstanceOfExpr(InformationWrapper info) {
		info.addNodeClassToHistory(InstanceOfExpr.class);

		Expression expression = guessNode(Expression.class, info);
		ReferenceType<?> type = guessNode(ReferenceType.class, info);

		return new InstanceOfExpr(expression, type);
	}

	public default FieldAccessExpr guessFieldAccessExpr(InformationWrapper info) {
		info.addNodeClassToHistory(FieldAccessExpr.class);

		Expression scope = guessNode(Expression.class, info);
		NodeList<Type> typeArguments = guessList(Type.class, info);
		SimpleName name = guessSimpleName(info);

		return new FieldAccessExpr(scope, typeArguments, name);
	}

	public default ConditionalExpr guessConditionalExpr(InformationWrapper info) {
		info.addNodeClassToHistory(ConditionalExpr.class);

		Expression condition = guessNode(Expression.class, info);
		Expression thenExpr = guessNode(Expression.class, info);
		Expression elseExpr = guessNode(Expression.class, info);

		return new ConditionalExpr(condition, thenExpr, elseExpr);
	}

	public default ClassExpr guessClassExpr(InformationWrapper info) {
		info.addNodeClassToHistory(ClassExpr.class);

		Type type = guessNode(Type.class, info);

		return new ClassExpr(type);
	}

	public default CastExpr guessCastExpr(InformationWrapper info) {
		info.addNodeClassToHistory(CastExpr.class);

		Type type = guessNode(Type.class, info);
		Expression expression = guessNode(Expression.class, info);

		return new CastExpr(type, expression);
	}

	public default AssignExpr guessAssignExpr(InformationWrapper info) {
		info.addNodeClassToHistory(AssignExpr.class);

		Expression target = guessNode(Expression.class, info);
		Expression value = guessNode(Expression.class, info);
		AssignExpr.Operator operator = guessAssignOperator(info);

		return new AssignExpr(target, value, operator);
	}

	public default ArrayInitializerExpr guessArrayInitializerExpr(InformationWrapper info) {
		info.addNodeClassToHistory(ArrayInitializerExpr.class);

		NodeList<Expression> values = guessList(Expression.class, info);

		return new ArrayInitializerExpr(values);
	}

	public default ArrayCreationExpr guessArrayCreationExpr(InformationWrapper info) {
		info.addNodeClassToHistory(ArrayCreationExpr.class);

		Type elementType = guessNode(Type.class, info);
		NodeList<ArrayCreationLevel> levels = guessList(ArrayCreationLevel.class, info);
		ArrayInitializerExpr initializer = guessArrayInitializerExpr(info);

		return new ArrayCreationExpr(elementType, levels, initializer);
	}

	public default ArrayAccessExpr guessArrayAccessExpr(InformationWrapper info) {
		info.addNodeClassToHistory(ArrayAccessExpr.class);

		Expression name = guessNode(Expression.class, info);
		Expression index = guessNode(Expression.class, info);

		return new ArrayAccessExpr(name, index);
	}

	public default PackageDeclaration guessPackageDeclaration(InformationWrapper info) {
		info.addNodeClassToHistory(PackageDeclaration.class);

		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		Name name = guessName(info);

		return new PackageDeclaration(annotations, name);
	}

	public default ImportDeclaration guessImportDeclaration(InformationWrapper info) {
		info.addNodeClassToHistory(ImportDeclaration.class);

		Name name = guessName(info);
		boolean isStatic = guessBoolean(info);
		boolean isAsterisk = guessBoolean(info);

		return new ImportDeclaration(name, isStatic, isAsterisk);
	}

	public default FieldDeclaration guessFieldDeclaration(InformationWrapper info) {
		info.addNodeClassToHistory(FieldDeclaration.class);

		EnumSet<Modifier> modifiers = guessModifiers(info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		NodeList<VariableDeclarator> variables = guessList(VariableDeclarator.class, info);

		return new FieldDeclaration(modifiers, annotations, variables);
	}

	public default ClassOrInterfaceType guessClassOrInterfaceType(InformationWrapper info) {
		info.addNodeClassToHistory(ClassOrInterfaceType.class);

		ClassOrInterfaceType scope = guessClassOrInterfaceType(info);
		SimpleName name = guessSimpleName(info);
		NodeList<Type> typeArguments = guessList(Type.class, info);

		return new ClassOrInterfaceType(scope, name, typeArguments);
	}

	public default ClassOrInterfaceDeclaration guessClassOrInterfaceDeclaration(InformationWrapper info) {
		info.addNodeClassToHistory(ClassOrInterfaceDeclaration.class);

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
		info.addNodeClassToHistory(MethodDeclaration.class);

		EnumSet<Modifier> modifiers = guessModifiers(info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		NodeList<TypeParameter> typeParameters = guessList(TypeParameter.class, info);
		Type type = guessNode(Type.class, info);
		SimpleName name = guessSimpleName(info);
		boolean isDefault = guessBoolean(info);
		NodeList<Parameter> parameters = guessList(Parameter.class, info);
		@SuppressWarnings("rawtypes")
		NodeList<ReferenceType> thrownExceptions = guessList(ReferenceType.class, info);
		BlockStmt body = guessBlockStmt(info);
		return new MethodDeclaration(modifiers, annotations, typeParameters, type, name, isDefault, parameters,
				thrownExceptions, body);
	}

	public default BinaryExpr guessBinaryExpr(InformationWrapper info) {
		info.addNodeClassToHistory(BinaryExpr.class);

		Expression left = guessNode(Expression.class, info);
		Expression right = guessNode(Expression.class, info);
		BinaryExpr.Operator operator = guessBinaryOperator(info);

		return new BinaryExpr(left, right, operator);
	}

	public default UnaryExpr guessUnaryExpr(InformationWrapper info) {
		info.addNodeClassToHistory(UnaryExpr.class);

		Expression expression = guessNode(Expression.class, info);
		UnaryExpr.Operator operator = guessUnaryOperator(info);

		return new UnaryExpr(expression, operator);
	}

	public default MethodCallExpr guessMethodCallExpr(InformationWrapper info) {
		info.addNodeClassToHistory(MethodCallExpr.class);

		Expression scope = guessNode(Expression.class, info);
		NodeList<Type> typeArguments = guessList(Type.class, info);
		SimpleName name = guessSimpleName(info);
		NodeList<Expression> arguments = guessList(Expression.class, info);

		return new MethodCallExpr(scope, typeArguments, name, arguments);
	}

	public default NameExpr guessNameExpr(InformationWrapper info) {
		info.addNodeClassToHistory(NameExpr.class);

		SimpleName name = guessSimpleName(info);

		return new NameExpr(name);
	}

	public default ConstructorDeclaration guessIntegerLiteralExpr(InformationWrapper info) {
		info.addNodeClassToHistory(ConstructorDeclaration.class);

		EnumSet<Modifier> modifiers = guessModifiers(info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		NodeList<TypeParameter> typeParameters = guessList(TypeParameter.class, info);
		SimpleName name = guessSimpleName(info);
		NodeList<Parameter> parameters = guessList(Parameter.class, info);
		@SuppressWarnings("rawtypes")
		NodeList<ReferenceType> thrownExceptions = guessList(ReferenceType.class, info);
		BlockStmt body = guessBlockStmt(info);

		return new ConstructorDeclaration(modifiers, annotations, typeParameters, name, parameters, thrownExceptions,
				body);
	}

	public default DoubleLiteralExpr guessDoubleLiteralExpr(InformationWrapper info) {
		info.addNodeClassToHistory(DoubleLiteralExpr.class);

		String value = guessStringValue(info);

		return new DoubleLiteralExpr(value);
	}

	public default StringLiteralExpr guessStringLiteralExpr(InformationWrapper info) {
		info.addNodeClassToHistory(StringLiteralExpr.class);

		String value = guessStringValue(info);

		return new StringLiteralExpr(value);
	}

	public default BooleanLiteralExpr guessBooleanLiteralExpr(InformationWrapper info) {
		info.addNodeClassToHistory(BooleanLiteralExpr.class);

		boolean value = guessBoolean(info);

		return new BooleanLiteralExpr(value);
	}

	public default CharLiteralExpr guessCharLiteralExpr(InformationWrapper info) {
		info.addNodeClassToHistory(CharLiteralExpr.class);

		String value = guessStringValue(info);

		return new CharLiteralExpr(value);
	}

	public default LongLiteralExpr guessLongLiteralExpr(InformationWrapper info) {
		info.addNodeClassToHistory(LongLiteralExpr.class);

		String value = guessStringValue(info);

		return new LongLiteralExpr(value);
	}

	public default ThisExpr guessThisExpr(InformationWrapper info) {
		info.addNodeClassToHistory(ThisExpr.class);

		Expression classExpr = guessNode(Expression.class, info);

		return new ThisExpr(classExpr);
	}

	public default BreakStmt guessBreakStmt(InformationWrapper info) {
		info.addNodeClassToHistory(BreakStmt.class);

		SimpleName label = guessSimpleName(info);

		return new BreakStmt(label);
	}

	public default ObjectCreationExpr guessObjectCreationExpr(InformationWrapper info) {
		info.addNodeClassToHistory(ObjectCreationExpr.class);

		Expression scope = guessNode(Expression.class, info);
		ClassOrInterfaceType type = guessClassOrInterfaceType(info);
		NodeList<Type> typeArguments = guessList(Type.class, info);
		NodeList<Expression> arguments = guessList(Expression.class, info);
		NodeList<BodyDeclaration<?>> anonymousClassBody = guessBodyDeclarationList(info);

		return new ObjectCreationExpr(scope, type, typeArguments, arguments, anonymousClassBody);
	}

	public default MarkerAnnotationExpr guessMarkerAnnotationExpr(InformationWrapper info) {
		info.addNodeClassToHistory(MarkerAnnotationExpr.class);

		Name name = guessName(info);

		return new MarkerAnnotationExpr(name);
	}

	public default NormalAnnotationExpr guessNormalAnnotationExpr(InformationWrapper info) {
		info.addNodeClassToHistory(NormalAnnotationExpr.class);

		Name name = guessName(info);
		NodeList<MemberValuePair> pairs = guessList(MemberValuePair.class, info);

		return new NormalAnnotationExpr(name, pairs);
	}

	public default SingleMemberAnnotationExpr guessSingleMemberAnnotationExpr(InformationWrapper info) {
		info.addNodeClassToHistory(SingleMemberAnnotationExpr.class);

		Name name = guessName(info);
		Expression memberValue = guessNode(Expression.class, info);

		return new SingleMemberAnnotationExpr(name, memberValue);
	}

	public default Parameter guessParameter(InformationWrapper info) {
		info.addNodeClassToHistory(Parameter.class);

		EnumSet<Modifier> modifiers = guessModifiers(info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		Type type = guessNode(Type.class, info);
		boolean isVarArgs = guessBoolean(info);
		NodeList<AnnotationExpr> varArgsAnnotations = guessList(AnnotationExpr.class, info);
		SimpleName name = guessSimpleName(info);

		return new Parameter(modifiers, annotations, type, isVarArgs, varArgsAnnotations, name);
	}

	public default EnclosedExpr guessEnclosedExpr(InformationWrapper info) {
		info.addNodeClassToHistory(EnclosedExpr.class);

		Expression inner = guessNode(Expression.class, info);

		return new EnclosedExpr(inner);
	}

	public default AssertStmt guessAssertStmt(InformationWrapper info) {
		info.addNodeClassToHistory(AssertStmt.class);

		Expression check = guessNode(Expression.class, info);
		Expression message = guessNode(Expression.class, info);

		return new AssertStmt(check, message);
	}

	public default ConstructorDeclaration guessMemberValuePair(InformationWrapper info) {
		info.addNodeClassToHistory(ConstructorDeclaration.class);

		EnumSet<Modifier> modifiers = guessModifiers(info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		NodeList<TypeParameter> typeParameters = guessList(TypeParameter.class, info);
		SimpleName name = guessSimpleName(info);
		NodeList<Parameter> parameters = guessList(Parameter.class, info);
		@SuppressWarnings("rawtypes")
		NodeList<ReferenceType> thrownExceptions = guessList(ReferenceType.class, info);
		BlockStmt body = guessBlockStmt(info);

		return new ConstructorDeclaration(modifiers, annotations, typeParameters, name, parameters, thrownExceptions,
				body);
	}

	public default PrimitiveType guessPrimitiveType(InformationWrapper info) {
		info.addNodeClassToHistory(PrimitiveType.class);

		Primitive type = guessPrimitive(info);

		return new PrimitiveType(type);
	}

	public default UnionType guessUnionType(InformationWrapper info) {
		info.addNodeClassToHistory(UnionType.class);

		@SuppressWarnings("rawtypes")
		NodeList<ReferenceType> elements = guessList(ReferenceType.class, info);

		return new UnionType(elements);
	}

	public default IntersectionType guessIntersectionType(InformationWrapper info) {
		info.addNodeClassToHistory(IntersectionType.class);

		@SuppressWarnings("rawtypes")
		NodeList<ReferenceType> elements = guessList(ReferenceType.class, info);

		return new IntersectionType(elements);
	}

	public default TypeParameter guessTypeParameter(InformationWrapper info) {
		info.addNodeClassToHistory(TypeParameter.class);

		SimpleName name = guessSimpleName(info);
		NodeList<ClassOrInterfaceType> typeBound = guessList(ClassOrInterfaceType.class, info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);

		return new TypeParameter(name, typeBound, annotations);
	}

	public default WildcardType guessWildcardType(InformationWrapper info) {
		info.addNodeClassToHistory(WildcardType.class);

		@SuppressWarnings("rawtypes")
		ReferenceType extendedType = guessNode(ReferenceType.class, info);
		@SuppressWarnings("rawtypes")
		ReferenceType superType = guessNode(ReferenceType.class, info);

		return new WildcardType(extendedType, superType);
	}

	public default VoidType guessVoidType(InformationWrapper info) {
		return new VoidType();
	}

	public default UnknownType guessUnknownType(InformationWrapper info) {
		return new UnknownType();
	}

	public default UnknownNode guessUnknown(InformationWrapper info) {
		return new UnknownNode();
	}

	public default Name guessName(InformationWrapper info) {
		info.addNodeClassToHistory(Name.class);

		Name qualifier = guessName(info);
		String identifier = guessStringValue(info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		return new Name(qualifier, identifier, annotations);
	}

	public default SimpleName guessSimpleName(InformationWrapper info) {
		info.addNodeClassToHistory(SimpleName.class);

		String identifier = guessStringValue(info);

		return new SimpleName(identifier);
	}

	public default LocalClassDeclarationStmt guessLocalClassDeclarationStmt(InformationWrapper info) {
		info.addNodeClassToHistory(LocalClassDeclarationStmt.class);

		ClassOrInterfaceDeclaration classDeclaration = guessClassOrInterfaceDeclaration(info);

		return new LocalClassDeclarationStmt(classDeclaration);
	}

	public default ArrayType guessArrayType(InformationWrapper info) {
		info.addNodeClassToHistory(ArrayType.class);

		Type componentType = guessNode(Type.class, info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);

		return new ArrayType(componentType, annotations);
	}

	public default ArrayCreationLevel guessArrayCreationLevel(InformationWrapper info) {
		info.addNodeClassToHistory(ArrayCreationLevel.class);

		Expression dimension = guessNode(Expression.class, info);
		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);

		return new ArrayCreationLevel(dimension, annotations);
	}

	public default ModuleDeclaration guessModuleDeclaration(InformationWrapper info) {
		info.addNodeClassToHistory(ModuleDeclaration.class);

		NodeList<AnnotationExpr> annotations = guessList(AnnotationExpr.class, info);
		Name name = guessName(info);
		boolean isOpen = guessBoolean(info);
		NodeList<ModuleStmt> moduleStmts = guessList(ModuleStmt.class, info);

		return new ModuleDeclaration(annotations, name, isOpen, moduleStmts);
	}

	public default ModuleExportsStmt guessModuleExportsStmt(InformationWrapper info) {
		info.addNodeClassToHistory(ModuleExportsStmt.class);

		Name name = guessName(info);
		NodeList<Name> moduleNames = guessList(Name.class, info);

		return new ModuleExportsStmt(name, moduleNames);
	}

	public default ModuleOpensStmt guessModuleOpensStmt(InformationWrapper info) {
		info.addNodeClassToHistory(ModuleOpensStmt.class);

		Name name = guessName(info);
		NodeList<Name> moduleNames = guessList(Name.class, info);

		return new ModuleOpensStmt(name, moduleNames);
	}

	public default ModuleProvidesStmt guessModuleProvidesStmt(InformationWrapper info) {
		info.addNodeClassToHistory(ModuleProvidesStmt.class);

		Type type = guessNode(Type.class, info);
		NodeList<Type> withTypes = guessList(Type.class, info);

		return new ModuleProvidesStmt(type, withTypes);
	}

	public default ModuleRequiresStmt guessModuleRequiresStmt(InformationWrapper info) {
		info.addNodeClassToHistory(ModuleRequiresStmt.class);

		EnumSet<Modifier> modifiers = guessModifiers(info);
		Name name = guessName(info);

		return new ModuleRequiresStmt(modifiers, name);
	}

	public default ModuleUsesStmt guessModuleUsesStmt(InformationWrapper info) {
		info.addNodeClassToHistory(ModuleUsesStmt.class);

		Type type = guessNode(Type.class, info);

		return new ModuleUsesStmt(type);
	}

	public default CompilationUnit guessCompilationUnit(InformationWrapper info) {
		info.addNodeClassToHistory(CompilationUnit.class);

		PackageDeclaration packageDeclaration = guessNode(PackageDeclaration.class, info);
		NodeList<ImportDeclaration> imports = guessList(ImportDeclaration.class, info);
		NodeList<TypeDeclaration<?>> types = guessTypeDeclarationList(info);
		ModuleDeclaration module = guessNode(ModuleDeclaration.class, info);

		return new CompilationUnit(packageDeclaration, imports, types, module);
	}

}
