package se.de.hu_berlin.informatik.astlmbuilder.parsing.creator;

import java.util.EnumSet;
import java.util.List;

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
import se.de.hu_berlin.informatik.astlmbuilder.parsing.parser.ITokenParser;

public interface INodeCreator {

	public ITokenParser getParser();

	default public <T extends Node> InformationWrapper updateGeneralInfo(Class<T> lastSeenNodeClass,
			InformationWrapper info, boolean useCopy) {
		if (useCopy) {
			info = info.getCopy();
		}
		info.addNodeClassToHistory(lastSeenNodeClass);

		return info;
	}

	// TODO: (maybe there exists a more elegant way?)
	// Attention: Parsing of Modifiers, types, booleans and operators is already
	// implemented in the respective Handler-interfaces!

	// expects the count of member data strings to be correct (verified
	// beforehand)

	public default ConstructorDeclaration createConstructorDeclaration(List<String> memberData,
			InformationWrapper info) {
		info = updateGeneralInfo(ConstructorDeclaration.class, info, true);

		EnumSet<Modifier> modifiers = getParser().createModifiersFromToken(memberData.get(0));
		NodeList<AnnotationExpr> annotations = getParser()
				.parseListFromToken(AnnotationExpr.class, memberData.get(1), info);
		NodeList<TypeParameter> typeParameters = getParser()
				.parseListFromToken(TypeParameter.class, memberData.get(2), info);
		SimpleName name = getParser().createSimpleName(memberData.get(3), info);
		NodeList<Parameter> parameters = getParser().createListFromToken(Parameter.class, memberData.get(4), info);
		@SuppressWarnings("rawtypes")
		NodeList<ReferenceType> thrownExceptions = getParser()
				.parseListFromToken(ReferenceType.class, memberData.get(5), info);
		BlockStmt body = getParser().createBlockStmt(memberData.get(6), info);

		return new ConstructorDeclaration(modifiers, annotations, typeParameters, name, parameters, thrownExceptions,
				body);
	}

	public default InitializerDeclaration createInitializerDeclaration(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(InitializerDeclaration.class, info, false);

		boolean isStatic = getParser().createBooleanFromToken(memberData.get(0));
		BlockStmt body = getParser().createBlockStmt(memberData.get(1), info);

		return new InitializerDeclaration(isStatic, body);
	}

	public default EnumConstantDeclaration createEnumConstantDeclaration(List<String> memberData,
			InformationWrapper info) throws IllegalArgumentException {
		info = updateGeneralInfo(EnumConstantDeclaration.class, info, false);

		NodeList<AnnotationExpr> annotations = getParser()
				.parseListFromToken(AnnotationExpr.class, memberData.get(0), info);
		SimpleName name = getParser().createSimpleName(memberData.get(1), info);
		NodeList<Expression> arguments = getParser().createListFromToken(Expression.class, memberData.get(2), info);
		NodeList<BodyDeclaration<?>> classBody = getParser().createBodyDeclarationListFromToken(memberData.get(3), info);

		return new EnumConstantDeclaration(annotations, name, arguments, classBody);
	};

	public default VariableDeclarator createVariableDeclarator(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(VariableDeclarator.class, info, false);

		Type type = getParser().createNodeFromToken(Type.class, memberData.get(0), info);
		SimpleName name = getParser().createSimpleName(memberData.get(1), info);
		Expression initializer = getParser().createNodeFromToken(Expression.class, memberData.get(2), info);

		return new VariableDeclarator(type, name, initializer);
	}

	public default EnumDeclaration createEnumDeclaration(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(EnumDeclaration.class, info, false);

		EnumSet<Modifier> modifiers = getParser().createModifiersFromToken(memberData.get(0));
		NodeList<AnnotationExpr> annotations = getParser()
				.parseListFromToken(AnnotationExpr.class, memberData.get(1), info);
		SimpleName name = getParser().createSimpleName(memberData.get(2), info);
		NodeList<ClassOrInterfaceType> implementedTypes = getParser()
				.parseListFromToken(ClassOrInterfaceType.class, memberData.get(3), info);
		NodeList<EnumConstantDeclaration> entries = getParser()
				.parseListFromToken(EnumConstantDeclaration.class, memberData.get(4), info);
		NodeList<BodyDeclaration<?>> members = getParser().createBodyDeclarationListFromToken(memberData.get(5), info);

		return new EnumDeclaration(modifiers, annotations, name, implementedTypes, entries, members);
	}

	public default AnnotationDeclaration createAnnotationDeclaration(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(AnnotationDeclaration.class, info, false);

		EnumSet<Modifier> modifiers = getParser().createModifiersFromToken(memberData.get(0));
		NodeList<AnnotationExpr> annotations = getParser()
				.parseListFromToken(AnnotationExpr.class, memberData.get(1), info);
		SimpleName name = getParser().createSimpleName(memberData.get(2), info);
		NodeList<BodyDeclaration<?>> members = getParser().createBodyDeclarationListFromToken(memberData.get(3), info);

		return new AnnotationDeclaration(modifiers, annotations, name, members);
	}

	public default AnnotationMemberDeclaration createAnnotationMemberDeclaration(List<String> memberData,
			InformationWrapper info) throws IllegalArgumentException {
		info = updateGeneralInfo(AnnotationMemberDeclaration.class, info, false);

		EnumSet<Modifier> modifiers = getParser().createModifiersFromToken(memberData.get(0));
		NodeList<AnnotationExpr> annotations = getParser()
				.parseListFromToken(AnnotationExpr.class, memberData.get(1), info);
		Type type = getParser().createNodeFromToken(Type.class, memberData.get(2), info);
		SimpleName name = getParser().createSimpleName(memberData.get(3), info);
		Expression defaultValue = getParser().createNodeFromToken(Expression.class, memberData.get(4), info);

		return new AnnotationMemberDeclaration(modifiers, annotations, type, name, defaultValue);
	}

	public default WhileStmt createWhileStmt(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(WhileStmt.class, info, false);

		Expression condition = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);
		Statement body = getParser().createNodeFromToken(Statement.class, memberData.get(1), info);

		return new WhileStmt(condition, body);
	}

	public default TryStmt createTryStmt(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(TryStmt.class, info, false);

		NodeList<VariableDeclarationExpr> resources = getParser()
				.parseListFromToken(VariableDeclarationExpr.class, memberData.get(0), info);
		BlockStmt tryBlock = getParser().createBlockStmt(memberData.get(1), info);
		NodeList<CatchClause> catchClauses = getParser().createListFromToken(CatchClause.class, memberData.get(2), info);
		BlockStmt finallyBlock = getParser().createBlockStmt(memberData.get(3), info);

		return new TryStmt(resources, tryBlock, catchClauses, finallyBlock);
	}

	public default ThrowStmt createThrowStmt(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(ThrowStmt.class, info, false);

		Expression expression = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);

		return new ThrowStmt(expression);
	}

	public default SynchronizedStmt createSynchronizedStmt(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(SynchronizedStmt.class, info, false);

		Expression expression = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);
		BlockStmt body = getParser().createBlockStmt(memberData.get(1), info);

		return new SynchronizedStmt(expression, body);
	}

	public default SwitchStmt createSwitchStmt(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(SwitchStmt.class, info, false);

		Expression selector = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);
		NodeList<SwitchEntryStmt> entries = getParser()
				.parseListFromToken(SwitchEntryStmt.class, memberData.get(1), info);

		return new SwitchStmt(selector, entries);
	}

	public default SwitchEntryStmt createSwitchEntryStmt(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(SwitchEntryStmt.class, info, false);

		Expression label = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);
		NodeList<Statement> statements = getParser().createListFromToken(Statement.class, memberData.get(1), info);

		return new SwitchEntryStmt(label, statements);
	}

	public default ReturnStmt createReturnStmt(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(ReturnStmt.class, info, false);

		Expression expression = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);

		return new ReturnStmt(expression);
	}

	public default LabeledStmt createLabeledStmt(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(LabeledStmt.class, info, false);

		String label = getParser().createStringValueFromToken(memberData.get(0));
		Statement statement = getParser().createNodeFromToken(Statement.class, memberData.get(1), info);

		return new LabeledStmt(label, statement);
	}

	public default IfStmt createIfStmt(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(IfStmt.class, info, false);

		Expression condition = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);
		Statement thenStmt = getParser().createNodeFromToken(Statement.class, memberData.get(1), info);
		Statement elseStmt = getParser().createNodeFromToken(Statement.class, memberData.get(2), info);

		return new IfStmt(condition, thenStmt, elseStmt);
	}

	public default ForStmt createForStmt(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(ForStmt.class, info, false);

		NodeList<Expression> initialization = getParser().createListFromToken(Expression.class, memberData.get(0), info);
		Expression compare = getParser().createNodeFromToken(Expression.class, memberData.get(1), info);
		NodeList<Expression> update = getParser().createListFromToken(Expression.class, memberData.get(2), info);
		Statement body = getParser().createNodeFromToken(Statement.class, memberData.get(3), info);

		return new ForStmt(initialization, compare, update, body);
	}

	public default ForeachStmt createForeachStmt(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(ForeachStmt.class, info, false);

		VariableDeclarationExpr variable = getParser().createVariableDeclarationExpr(memberData.get(0), info);
		Expression iterable = getParser().createNodeFromToken(Expression.class, memberData.get(1), info);
		Statement body = getParser().createNodeFromToken(Statement.class, memberData.get(2), info);

		return new ForeachStmt(variable, iterable, body);
	}

	public default ExpressionStmt createExpressionStmt(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(ExpressionStmt.class, info, false);

		Expression expression = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);

		return new ExpressionStmt(expression);
	}

	public default ExplicitConstructorInvocationStmt createExplicitConstructorInvocationStmt(List<String> memberData,
			InformationWrapper info) throws IllegalArgumentException {
		info = updateGeneralInfo(ExplicitConstructorInvocationStmt.class, info, false);

		boolean isThis = getParser().createBooleanFromToken(memberData.get(0));
		Expression expression = getParser().createNodeFromToken(Expression.class, memberData.get(1), info);
		NodeList<Expression> arguments = getParser().createListFromToken(Expression.class, memberData.get(2), info);

		return new ExplicitConstructorInvocationStmt(isThis, expression, arguments);
	}

	public default DoStmt createDoStmt(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(DoStmt.class, info, false);

		Statement body = getParser().createNodeFromToken(Statement.class, memberData.get(0), info);
		Expression condition = getParser().createNodeFromToken(Expression.class, memberData.get(1), info);

		return new DoStmt(body, condition);
	}

	public default ContinueStmt createContinueStmt(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(ContinueStmt.class, info, false);

		SimpleName label = getParser().createSimpleName(memberData.get(0), info);

		return new ContinueStmt(label);
	}

	public default CatchClause createCatchClause(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(CatchClause.class, info, false);

		EnumSet<Modifier> exceptModifier = getParser().createModifiersFromToken(memberData.get(0));
		NodeList<AnnotationExpr> exceptAnnotations = getParser()
				.parseListFromToken(AnnotationExpr.class, memberData.get(1), info);
		ClassOrInterfaceType exceptType = getParser().createClassOrInterfaceType(memberData.get(2), info);
		SimpleName exceptName = getParser().createSimpleName(memberData.get(3), info);
		BlockStmt body = getParser().createBlockStmt(memberData.get(4), info);

		return new CatchClause(exceptModifier, exceptAnnotations, exceptType, exceptName, body);
	}

	public default BlockStmt createBlockStmt(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(BlockStmt.class, info, false);

		NodeList<Statement> statements = getParser()
				.parseListFromToken(Statement.class, memberData.get(0), info.getCopy());

		return new BlockStmt(statements);
	}

	public default VariableDeclarationExpr createVariableDeclarationExpr(List<String> memberData,
			InformationWrapper info) {
		info = updateGeneralInfo(VariableDeclarationExpr.class, info, false);

		EnumSet<Modifier> modifiers = getParser().createModifiersFromToken(memberData.get(0));
		NodeList<AnnotationExpr> annotations = getParser()
				.parseListFromToken(AnnotationExpr.class, memberData.get(1), info);
		NodeList<VariableDeclarator> variables = getParser()
				.parseListFromToken(VariableDeclarator.class, memberData.get(2), info);

		return new VariableDeclarationExpr(modifiers, annotations, variables);
	}

	public default TypeExpr createTypeExpr(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(TypeExpr.class, info, false);

		Type type = getParser().createNodeFromToken(Type.class, memberData.get(0), info);

		return new TypeExpr(type);
	}

	public default SuperExpr createSuperExpr(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(SuperExpr.class, info, false);

		Expression classExpr = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);

		return new SuperExpr(classExpr);
	}

	public default NullLiteralExpr createNullLiteralExpr(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(NullLiteralExpr.class, info, false);

		return new NullLiteralExpr();
	}

	public default MethodReferenceExpr createMethodReferenceExpr(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(MethodReferenceExpr.class, info, false);

		Expression scope = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);
		NodeList<Type> typeArguments = getParser().createListFromToken(Type.class, memberData.get(1), info);
		String identifier = getParser().createMethodIdentifierFromToken(memberData.get(2), info);

		return new MethodReferenceExpr(scope, typeArguments, identifier);
	}

	public default LambdaExpr createLambdaExpr(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(LambdaExpr.class, info, false);

		NodeList<Parameter> parameters = getParser().createListFromToken(Parameter.class, memberData.get(0), info);
		Statement body = getParser().createNodeFromToken(Statement.class, memberData.get(1), info);
		boolean isEnclosingParameters = getParser().createBooleanFromToken(memberData.get(2));

		return new LambdaExpr(parameters, body, isEnclosingParameters);
	}

	public default InstanceOfExpr createInstanceOfExpr(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(InstanceOfExpr.class, info, false);

		Expression expression = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);
		ReferenceType<?> type = getParser().createNodeFromToken(ReferenceType.class, memberData.get(1), info);

		return new InstanceOfExpr(expression, type);
	}

	public default FieldAccessExpr createFieldAccessExpr(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(FieldAccessExpr.class, info, false);

		Expression scope = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);
		NodeList<Type> typeArguments = getParser().createListFromToken(Type.class, memberData.get(1), info);
		SimpleName name = getParser().createSimpleName(memberData.get(2), info);

		return new FieldAccessExpr(scope, typeArguments, name);
	}

	public default ConditionalExpr createConditionalExpr(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(ConditionalExpr.class, info, false);

		Expression condition = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);
		Expression thenExpr = getParser().createNodeFromToken(Expression.class, memberData.get(1), info);
		Expression elseExpr = getParser().createNodeFromToken(Expression.class, memberData.get(2), info);

		return new ConditionalExpr(condition, thenExpr, elseExpr);
	}

	public default ClassExpr createClassExpr(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(ClassExpr.class, info, false);

		Type type = getParser().createNodeFromToken(Type.class, memberData.get(0), info);

		return new ClassExpr(type);
	}

	public default CastExpr createCastExpr(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(CastExpr.class, info, false);

		Type type = getParser().createNodeFromToken(Type.class, memberData.get(0), info);
		Expression expression = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);

		return new CastExpr(type, expression);
	}

	public default AssignExpr createAssignExpr(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(AssignExpr.class, info, false);

		Expression target = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);
		Expression value = getParser().createNodeFromToken(Expression.class, memberData.get(1), info);
		AssignExpr.Operator operator = getParser().createAssignOperatorFromToken(memberData.get(2));

		return new AssignExpr(target, value, operator);
	}

	public default ArrayInitializerExpr createArrayInitializerExpr(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(ArrayInitializerExpr.class, info, false);

		NodeList<Expression> values = getParser().createListFromToken(Expression.class, memberData.get(0), info);

		return new ArrayInitializerExpr(values);
	}

	public default ArrayCreationExpr createArrayCreationExpr(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(ArrayCreationExpr.class, info, false);

		Type elementType = getParser().createNodeFromToken(Type.class, memberData.get(0), info);
		NodeList<ArrayCreationLevel> levels = getParser()
				.parseListFromToken(ArrayCreationLevel.class, memberData.get(1), info);
		ArrayInitializerExpr initializer = getParser().createArrayInitializerExpr(memberData.get(2), info);

		return new ArrayCreationExpr(elementType, levels, initializer);
	}

	public default ArrayAccessExpr createArrayAccessExpr(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(ArrayAccessExpr.class, info, false);

		Expression name = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);
		Expression index = getParser().createNodeFromToken(Expression.class, memberData.get(1), info);

		return new ArrayAccessExpr(name, index);
	}

	public default PackageDeclaration createPackageDeclaration(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(PackageDeclaration.class, info, false);

		NodeList<AnnotationExpr> annotations = getParser()
				.parseListFromToken(AnnotationExpr.class, memberData.get(0), info);
		Name name = getParser().createName(memberData.get(1), info);

		return new PackageDeclaration(annotations, name);
	}

	public default ImportDeclaration createImportDeclaration(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(ImportDeclaration.class, info, false);

		Name name = getParser().createName(memberData.get(0), info);
		boolean isStatic = getParser().createBooleanFromToken(memberData.get(1));
		boolean isAsterisk = getParser().createBooleanFromToken(memberData.get(2));

		return new ImportDeclaration(name, isStatic, isAsterisk);
	}

	public default FieldDeclaration createFieldDeclaration(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(FieldDeclaration.class, info, false);

		EnumSet<Modifier> modifiers = getParser().createModifiersFromToken(memberData.get(0));
		NodeList<AnnotationExpr> annotations = getParser()
				.parseListFromToken(AnnotationExpr.class, memberData.get(1), info);
		NodeList<VariableDeclarator> variables = getParser()
				.parseListFromToken(VariableDeclarator.class, memberData.get(2), info);

		return new FieldDeclaration(modifiers, annotations, variables);
	}

	public default ClassOrInterfaceType createClassOrInterfaceType(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(ClassOrInterfaceType.class, info, false);

		ClassOrInterfaceType scope = getParser().createClassOrInterfaceType(memberData.get(0), info);
		SimpleName name = getParser().createSimpleName(memberData.get(1), info);
		NodeList<Type> typeArguments = getParser().createListFromToken(Type.class, memberData.get(2), info);

		return new ClassOrInterfaceType(scope, name, typeArguments);
	}

	public default ClassOrInterfaceDeclaration createClassOrInterfaceDeclaration(List<String> memberData,
			InformationWrapper info) throws IllegalArgumentException {
		info = updateGeneralInfo(ClassOrInterfaceDeclaration.class, info, false);

		EnumSet<Modifier> modifiers = getParser().createModifiersFromToken(memberData.get(0));
		NodeList<AnnotationExpr> annotations = getParser()
				.parseListFromToken(AnnotationExpr.class, memberData.get(1), info);
		boolean isInterface = getParser().createBooleanFromToken(memberData.get(2));
		SimpleName name = getParser().createSimpleName(memberData.get(3), info);
		NodeList<TypeParameter> typeParameters = getParser()
				.parseListFromToken(TypeParameter.class, memberData.get(4), info);
		NodeList<ClassOrInterfaceType> extendedTypes = getParser()
				.parseListFromToken(ClassOrInterfaceType.class, memberData.get(5), info);
		NodeList<ClassOrInterfaceType> implementedTypes = getParser()
				.parseListFromToken(ClassOrInterfaceType.class, memberData.get(6), info);
		NodeList<BodyDeclaration<?>> members = getParser().createBodyDeclarationListFromToken(memberData.get(7), info);

		return new ClassOrInterfaceDeclaration(modifiers, annotations, isInterface, name, typeParameters, extendedTypes,
				implementedTypes, members);
	}

	public default MethodDeclaration createMethodDeclaration(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(MethodDeclaration.class, info, false);

		EnumSet<Modifier> modifiers = getParser().createModifiersFromToken(memberData.get(0));
		NodeList<AnnotationExpr> annotations = getParser()
				.parseListFromToken(AnnotationExpr.class, memberData.get(1), info);
		NodeList<TypeParameter> typeParameters = getParser()
				.parseListFromToken(TypeParameter.class, memberData.get(2), info);
		Type type = getParser().createNodeFromToken(Type.class, memberData.get(3), info);
		SimpleName name = getParser().createSimpleName(memberData.get(4), info);
		boolean isDefault = getParser().createBooleanFromToken(memberData.get(5));
		NodeList<Parameter> parameters = getParser().createListFromToken(Parameter.class, memberData.get(6), info);
		@SuppressWarnings("rawtypes")
		NodeList<ReferenceType> thrownExceptions = getParser()
				.parseListFromToken(ReferenceType.class, memberData.get(7), info);
		BlockStmt body = getParser().createBlockStmt(memberData.get(8), info);

		return new MethodDeclaration(modifiers, annotations, typeParameters, type, name, isDefault, parameters,
				thrownExceptions, body);
	}

	public default BinaryExpr createBinaryExpr(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(BinaryExpr.class, info, false);

		Expression left = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);
		Expression right = getParser().createNodeFromToken(Expression.class, memberData.get(1), info);
		BinaryExpr.Operator operator = getParser().createBinaryOperatorFromToken(memberData.get(2));

		return new BinaryExpr(left, right, operator);
	}

	public default UnaryExpr createUnaryExpr(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(UnaryExpr.class, info, false);

		Expression expression = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);
		UnaryExpr.Operator operator = getParser().createUnaryOperatorFromToken(memberData.get(1));

		return new UnaryExpr(expression, operator);
	}

	public default MethodCallExpr createMethodCallExpr(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(MethodCallExpr.class, info, false);

		Expression scope = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);
		NodeList<Type> typeArguments = getParser().createListFromToken(Type.class, memberData.get(1), info);
		SimpleName name = getParser().createSimpleName(memberData.get(2), info);
		NodeList<Expression> arguments = getParser().createListFromToken(Expression.class, memberData.get(3), info);

		return new MethodCallExpr(scope, typeArguments, name, arguments);
	}

	public default NameExpr createNameExpr(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(NameExpr.class, info, false);

		SimpleName name = getParser().createSimpleName(memberData.get(0), info);

		return new NameExpr(name);
	}

	public default IntegerLiteralExpr createIntegerLiteralExpr(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(IntegerLiteralExpr.class, info, false);

		String value = getParser().createStringValueFromToken(memberData.get(0));

		return new IntegerLiteralExpr(value);
	}

	public default DoubleLiteralExpr createDoubleLiteralExpr(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(DoubleLiteralExpr.class, info, false);

		String value = getParser().createStringValueFromToken(memberData.get(0));

		return new DoubleLiteralExpr(value);
	}

	public default StringLiteralExpr createStringLiteralExpr(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(StringLiteralExpr.class, info, false);

		String value = getParser().createStringValueFromToken(memberData.get(0));

		return new StringLiteralExpr(value);
	}

	public default BooleanLiteralExpr createBooleanLiteralExpr(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(BooleanLiteralExpr.class, info, false);

		boolean value = getParser().createBooleanFromToken(memberData.get(0));

		return new BooleanLiteralExpr(value);
	}

	public default CharLiteralExpr createCharLiteralExpr(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(CharLiteralExpr.class, info, false);

		String value = getParser().createStringValueFromToken(memberData.get(0));

		return new CharLiteralExpr(value);
	}

	public default LongLiteralExpr createLongLiteralExpr(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(LongLiteralExpr.class, info, false);

		String value = getParser().createStringValueFromToken(memberData.get(0));

		return new LongLiteralExpr(value);
	}

	public default ThisExpr createThisExpr(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(ThisExpr.class, info, false);

		Expression classExpr = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);

		return new ThisExpr(classExpr);
	}

	public default BreakStmt createBreakStmt(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(BreakStmt.class, info, false);

		SimpleName label = getParser().createSimpleName(memberData.get(0), info);

		return new BreakStmt(label);
	}

	public default ObjectCreationExpr createObjectCreationExpr(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(ObjectCreationExpr.class, info, false);

		Expression scope = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);
		ClassOrInterfaceType type = getParser().createClassOrInterfaceType(memberData.get(1), info);
		NodeList<Type> typeArguments = getParser().createListFromToken(Type.class, memberData.get(2), info);
		NodeList<Expression> arguments = getParser().createListFromToken(Expression.class, memberData.get(3), info);
		NodeList<BodyDeclaration<?>> anonymousClassBody = getParser()
				.parseBodyDeclarationListFromToken(memberData.get(4), info);

		return new ObjectCreationExpr(scope, type, typeArguments, arguments, anonymousClassBody);
	}

	public default MarkerAnnotationExpr createMarkerAnnotationExpr(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(MarkerAnnotationExpr.class, info, false);

		Name name = getParser().createName(memberData.get(0), info);

		return new MarkerAnnotationExpr(name);
	}

	public default NormalAnnotationExpr createNormalAnnotationExpr(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(NormalAnnotationExpr.class, info, false);

		Name name = getParser().createName(memberData.get(0), info);
		NodeList<MemberValuePair> pairs = getParser()
				.parseListFromToken(MemberValuePair.class, memberData.get(1), info);

		return new NormalAnnotationExpr(name, pairs);
	}

	public default SingleMemberAnnotationExpr createSingleMemberAnnotationExpr(List<String> memberData,
			InformationWrapper info) {
		info = updateGeneralInfo(SingleMemberAnnotationExpr.class, info, false);

		Name name = getParser().createName(memberData.get(0), info);
		Expression memberValue = getParser().createNodeFromToken(Expression.class, memberData.get(1), info);

		return new SingleMemberAnnotationExpr(name, memberValue);
	}

	public default Parameter createParameter(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(Parameter.class, info, false);

		EnumSet<Modifier> modifiers = getParser().createModifiersFromToken(memberData.get(0));
		NodeList<AnnotationExpr> annotations = getParser()
				.parseListFromToken(AnnotationExpr.class, memberData.get(1), info);
		Type type = getParser().createNodeFromToken(Type.class, memberData.get(2), info);
		boolean isVarArgs = getParser().createBooleanFromToken(memberData.get(3));
		NodeList<AnnotationExpr> varArgsAnnotations = getParser()
				.parseListFromToken(AnnotationExpr.class, memberData.get(4), info);
		SimpleName name = getParser().createSimpleName(memberData.get(5), info);

		return new Parameter(modifiers, annotations, type, isVarArgs, varArgsAnnotations, name);
	}

	public default EnclosedExpr createEnclosedExpr(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(EnclosedExpr.class, info, false);

		Expression inner = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);

		return new EnclosedExpr(inner);
	}

	public default AssertStmt createAssertStmt(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(AssertStmt.class, info, false);

		Expression check = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);
		Expression message = getParser().createNodeFromToken(Expression.class, memberData.get(1), info);

		return new AssertStmt(check, message);
	}

	public default MemberValuePair createMemberValuePair(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(MemberValuePair.class, info, false);

		SimpleName name = getParser().createSimpleName(memberData.get(0), info);
		Expression value = getParser().createNodeFromToken(Expression.class, memberData.get(1), info);

		return new MemberValuePair(name, value);
	}

	public default PrimitiveType createPrimitiveType(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(PrimitiveType.class, info, false);

		Primitive type = getParser().createPrimitiveFromToken(memberData.get(0));

		return new PrimitiveType(type);
	}

	public default UnionType createUnionType(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(UnionType.class, info, false);

		@SuppressWarnings("rawtypes")
		NodeList<ReferenceType> elements = getParser().createListFromToken(ReferenceType.class, memberData.get(0), info);

		return new UnionType(elements);
	}

	public default IntersectionType createIntersectionType(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(IntersectionType.class, info, false);

		@SuppressWarnings("rawtypes")
		NodeList<ReferenceType> elements = getParser().createListFromToken(ReferenceType.class, memberData.get(0), info);

		return new IntersectionType(elements);
	}

	public default TypeParameter createTypeParameter(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(TypeParameter.class, info, false);

		SimpleName name = getParser().createSimpleName(memberData.get(0), info);
		NodeList<ClassOrInterfaceType> typeBound = getParser()
				.parseListFromToken(ClassOrInterfaceType.class, memberData.get(1), info);
		NodeList<AnnotationExpr> annotations = getParser()
				.parseListFromToken(AnnotationExpr.class, memberData.get(2), info);

		return new TypeParameter(name, typeBound, annotations);
	}

	public default WildcardType createWildcardType(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(WildcardType.class, info, false);

		@SuppressWarnings("rawtypes")
		ReferenceType extendedType = getParser().createNodeFromToken(ReferenceType.class, memberData.get(0), info);
		@SuppressWarnings("rawtypes")
		ReferenceType superType = getParser().createNodeFromToken(ReferenceType.class, memberData.get(1), info);

		return new WildcardType(extendedType, superType);
	}

	public default UnknownType createUnknownType(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(UnknownType.class, info, false);

		return new UnknownType();
	}

	public default VoidType createVoidType(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(VoidType.class, info, false);

		return new VoidType();
	}

	public default Name createName(List<String> memberData, InformationWrapper info) throws IllegalArgumentException {
		info = updateGeneralInfo(Name.class, info, false);

		Name qualifier = getParser().createName(memberData.get(0), info);
		String identifier = getParser().createStringValueFromToken(memberData.get(1));
		NodeList<AnnotationExpr> annotations = getParser()
				.parseListFromToken(AnnotationExpr.class, memberData.get(2), info);

		return new Name(qualifier, identifier, annotations);
	}

	public default SimpleName createSimpleName(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(SimpleName.class, info, false);

		String identifier = getParser().createStringValueFromToken(memberData.get(0));

		return new SimpleName(identifier);
	}

	public default LocalClassDeclarationStmt createLocalClassDeclarationStmt(List<String> memberData,
			InformationWrapper info) throws IllegalArgumentException {
		info = updateGeneralInfo(LocalClassDeclarationStmt.class, info, false);

		ClassOrInterfaceDeclaration classDeclaration = getParser()
				.parseClassOrInterfaceDeclaration(memberData.get(0), info);

		return new LocalClassDeclarationStmt(classDeclaration);
	}

	public default ArrayType createArrayType(List<String> memberData, InformationWrapper info) {
		info = updateGeneralInfo(ArrayType.class, info, false);

		Type componentType = getParser().createNodeFromToken(Type.class, memberData.get(0), info);
		NodeList<AnnotationExpr> annotations = getParser()
				.parseListFromToken(AnnotationExpr.class, memberData.get(1), info);

		return new ArrayType(componentType, annotations);
	}

	public default ArrayCreationLevel createArrayCreationLevel(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(ArrayCreationLevel.class, info, false);

		Expression dimension = getParser().createNodeFromToken(Expression.class, memberData.get(0), info);
		NodeList<AnnotationExpr> annotations = getParser()
				.parseListFromToken(AnnotationExpr.class, memberData.get(1), info);

		return new ArrayCreationLevel(dimension, annotations);
	}

	public default ModuleDeclaration createModuleDeclaration(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(ModuleDeclaration.class, info, false);

		NodeList<AnnotationExpr> annotations = getParser()
				.parseListFromToken(AnnotationExpr.class, memberData.get(0), info);
		Name name = getParser().createName(memberData.get(1), info);
		boolean isOpen = getParser().createBooleanFromToken(memberData.get(2));
		NodeList<ModuleStmt> moduleStmts = getParser().createListFromToken(ModuleStmt.class, memberData.get(3), info);

		return new ModuleDeclaration(annotations, name, isOpen, moduleStmts);
	}

	public default ModuleExportsStmt createModuleExportsStmt(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(ModuleExportsStmt.class, info, false);

		Name name = getParser().createName(memberData.get(0), info);
		NodeList<Name> moduleNames = getParser().createListFromToken(Name.class, memberData.get(1), info);

		return new ModuleExportsStmt(name, moduleNames);
	}

	public default ModuleOpensStmt createModuleOpensStmt(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(ModuleOpensStmt.class, info, false);

		Name name = getParser().createName(memberData.get(0), info);
		NodeList<Name> moduleNames = getParser().createListFromToken(Name.class, memberData.get(1), info);

		return new ModuleOpensStmt(name, moduleNames);
	}

	public default ModuleProvidesStmt createModuleProvidesStmt(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(ModuleProvidesStmt.class, info, false);

		Type type = getParser().createNodeFromToken(Type.class, memberData.get(0), info);
		NodeList<Type> withTypes = getParser().createListFromToken(Type.class, memberData.get(1), info);

		return new ModuleProvidesStmt(type, withTypes);
	}

	public default ModuleRequiresStmt createModuleRequiresStmt(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(ModuleRequiresStmt.class, info, false);

		EnumSet<Modifier> modifiers = getParser().createModifiersFromToken(memberData.get(0));
		Name name = getParser().createName(memberData.get(1), info);

		return new ModuleRequiresStmt(modifiers, name);
	}

	public default ModuleUsesStmt createModuleUsesStmt(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(ModuleUsesStmt.class, info, false);

		Type type = getParser().createNodeFromToken(Type.class, memberData.get(0), info);

		return new ModuleUsesStmt(type);
	}

	public default CompilationUnit createCompilationUnit(List<String> memberData, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(CompilationUnit.class, info, false);

		PackageDeclaration packageDeclaration = getParser()
				.parseNodeFromToken(PackageDeclaration.class, memberData.get(0), info);
		NodeList<ImportDeclaration> imports = getParser()
				.parseListFromToken(ImportDeclaration.class, memberData.get(1), info);
		NodeList<TypeDeclaration<?>> types = getParser().createTypeDeclarationListFromToken(memberData.get(2), info);
		ModuleDeclaration module = getParser().createNodeFromToken(ModuleDeclaration.class, memberData.get(3), info);

		return new CompilationUnit(packageDeclaration, imports, types, module);
	}

}
