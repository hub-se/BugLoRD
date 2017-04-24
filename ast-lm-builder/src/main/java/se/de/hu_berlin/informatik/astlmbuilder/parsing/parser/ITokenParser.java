package se.de.hu_berlin.informatik.astlmbuilder.parsing.parser;

import java.util.EnumSet;
import java.util.List;

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

public interface ITokenParser extends ITokenParserBasics {

	// TODO: (maybe there exists a more elegant way?)
	// Attention: Parsing of Modifiers, types, booleans and operators is already
	// implemented in the respective Handler-interfaces!

	// expected token format: id, or (id,[member_1],...,[member_n]), or ~ for
	// null

	public default ConstructorDeclaration createConstructorDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		InformationWrapper infoCopy = info.getCopy();
		infoCopy.addNodeClassToHistory(ConstructorDeclaration.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getConstructorDeclaration(), 7);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ConstructorDeclaration.class, token, info);
		}

		EnumSet<Modifier> modifiers = parseModifiersFromToken(memberData.get(0));
		NodeList<AnnotationExpr> annotations = parseListFromToken(AnnotationExpr.class, memberData.get(1), infoCopy);
		NodeList<TypeParameter> typeParameters = parseListFromToken(TypeParameter.class, memberData.get(2), infoCopy);
		SimpleName name = createSimpleName(memberData.get(3), infoCopy);
		NodeList<Parameter> parameters = parseListFromToken(Parameter.class, memberData.get(4), infoCopy);
		@SuppressWarnings("rawtypes")
		NodeList<ReferenceType> thrownExceptions = parseListFromToken(ReferenceType.class, memberData.get(5), infoCopy);
		BlockStmt body = createBlockStmt(memberData.get(6), infoCopy);

		return new ConstructorDeclaration(modifiers, annotations, typeParameters, name, parameters, thrownExceptions,
				body);
	}

	public default InitializerDeclaration createInitializerDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(InitializerDeclaration.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getInitializerDeclaration(), 2);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(InitializerDeclaration.class, token, info);
		}

		boolean isStatic = parseBooleanFromToken(memberData.get(0));
		BlockStmt body = createBlockStmt(memberData.get(1), info);

		return new InitializerDeclaration(isStatic, body);
	}

	public default EnumConstantDeclaration createEnumConstantDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(EnumConstantDeclaration.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getEnumConstantDeclaration(), 4);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(EnumConstantDeclaration.class, token, info);
		}

		NodeList<AnnotationExpr> annotations = parseListFromToken(AnnotationExpr.class, memberData.get(0), info);
		SimpleName name = createSimpleName(memberData.get(1), info);
		NodeList<Expression> arguments = parseListFromToken(Expression.class, memberData.get(2), info);
		NodeList<BodyDeclaration<?>> classBody = parseBodyDeclarationListFromToken(memberData.get(3), info);

		return new EnumConstantDeclaration(annotations, name, arguments, classBody);
	};

	public default VariableDeclarator createVariableDeclarator(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(VariableDeclarator.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getVariableDeclaration(), 3);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(VariableDeclarator.class, token, info);
		}

		Type type = createNodeFromToken(Type.class, memberData.get(0), info);
		SimpleName name = createSimpleName(memberData.get(1), info);
		Expression initializer = createNodeFromToken(Expression.class, memberData.get(2), info);

		return new VariableDeclarator(type, name, initializer);
	}

	public default EnumDeclaration createEnumDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(EnumDeclaration.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getEnumDeclaration(), 6);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(EnumDeclaration.class, token, info);
		}

		EnumSet<Modifier> modifiers = parseModifiersFromToken(memberData.get(0));
		NodeList<AnnotationExpr> annotations = parseListFromToken(AnnotationExpr.class, memberData.get(1), info);
		SimpleName name = createSimpleName(memberData.get(2), info);
		NodeList<ClassOrInterfaceType> implementedTypes = parseListFromToken(ClassOrInterfaceType.class,
				memberData.get(3), info);
		NodeList<EnumConstantDeclaration> entries = parseListFromToken(EnumConstantDeclaration.class, memberData.get(4),
				info);
		NodeList<BodyDeclaration<?>> members = parseBodyDeclarationListFromToken(memberData.get(5), info);

		return new EnumDeclaration(modifiers, annotations, name, implementedTypes, entries, members);
	}

	public default AnnotationDeclaration createAnnotationDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(AnnotationDeclaration.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getAnnotationDeclaration(), 4);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(AnnotationDeclaration.class, token, info);
		}

		EnumSet<Modifier> modifiers = parseModifiersFromToken(memberData.get(0));
		NodeList<AnnotationExpr> annotations = parseListFromToken(AnnotationExpr.class, memberData.get(1), info);
		SimpleName name = createSimpleName(memberData.get(2), info);
		NodeList<BodyDeclaration<?>> members = parseBodyDeclarationListFromToken(memberData.get(3), info);

		return new AnnotationDeclaration(modifiers, annotations, name, members);
	}

	public default AnnotationMemberDeclaration createAnnotationMemberDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(AnnotationMemberDeclaration.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getAnnotationMemberDeclaration(), 5);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(AnnotationMemberDeclaration.class, token, info);
		}

		EnumSet<Modifier> modifiers = parseModifiersFromToken(memberData.get(0));
		NodeList<AnnotationExpr> annotations = parseListFromToken(AnnotationExpr.class, memberData.get(1), info);
		Type type = createNodeFromToken(Type.class, memberData.get(2), info);
		SimpleName name = createSimpleName(memberData.get(3), info);
		Expression defaultValue = createNodeFromToken(Expression.class, memberData.get(4), info);

		return new AnnotationMemberDeclaration(modifiers, annotations, type, name, defaultValue);
	}

	public default WhileStmt createWhileStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(WhileStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getWhileStatement(), 2);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(WhileStmt.class, token, info);
		}

		Expression condition = createNodeFromToken(Expression.class, memberData.get(0), info);
		Statement body = createBlockStmt(memberData.get(1), info);

		return new WhileStmt(condition, body);
	}

	public default TryStmt createTryStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(TryStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getTryStatement(), 4);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(TryStmt.class, token, info);
		}

		NodeList<VariableDeclarationExpr> resources = parseListFromToken(VariableDeclarationExpr.class,
				memberData.get(0), info);
		BlockStmt tryBlock = createBlockStmt(memberData.get(1), info);
		NodeList<CatchClause> catchClauses = parseListFromToken(CatchClause.class, memberData.get(2), info);
		BlockStmt finallyBlock = createBlockStmt(memberData.get(3), info);

		return new TryStmt(resources, tryBlock, catchClauses, finallyBlock);
	}

	public default ThrowStmt createThrowStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(ThrowStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getThrowStatement(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ThrowStmt.class, token, info);
		}

		Expression expression = createNodeFromToken(Expression.class, memberData.get(0), info);

		return new ThrowStmt(expression);
	}

	public default SynchronizedStmt createSynchronizedStmt(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(SynchronizedStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getSynchronizedStatement(), 2);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(SynchronizedStmt.class, token, info);
		}

		Expression expression = createNodeFromToken(Expression.class, memberData.get(0), info);
		BlockStmt body = createBlockStmt(memberData.get(1), info);

		return new SynchronizedStmt(expression, body);
	}

	public default SwitchStmt createSwitchStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(SwitchStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getSwitchStatement(), 2);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(SwitchStmt.class, token, info);
		}

		Expression selector = createNodeFromToken(Expression.class, memberData.get(0), info);
		NodeList<SwitchEntryStmt> entries = parseListFromToken(SwitchEntryStmt.class, memberData.get(1), info);

		return new SwitchStmt(selector, entries);
	}

	public default SwitchEntryStmt createSwitchEntryStmt(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(SwitchEntryStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getSwitchEntryStatement(), 2);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(SwitchEntryStmt.class, token, info);
		}

		Expression label = createNodeFromToken(Expression.class, memberData.get(0), info);
		NodeList<Statement> statements = parseListFromToken(Statement.class, memberData.get(1), info);

		return new SwitchEntryStmt(label, statements);
	}

	public default ReturnStmt createReturnStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(ReturnStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getReturnStatement(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ReturnStmt.class, token, info);
		}

		Expression expression = createNodeFromToken(Expression.class, memberData.get(0), info);

		return new ReturnStmt(expression);
	}

	public default LabeledStmt createLabeledStmt(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(LabeledStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getLabeledStatement(), 2);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(LabeledStmt.class, token, info);
		}

		String label = parseStringValueFromToken(memberData.get(0));
		Statement statement = createNodeFromToken(Statement.class, memberData.get(1), info);

		return new LabeledStmt(label, statement);
	}

	public default IfStmt createIfStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(IfStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getIfStatement(), 3);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(IfStmt.class, token, info);
		}

		Expression condition = createNodeFromToken(Expression.class, memberData.get(0), info);
		Statement thenStmt = createNodeFromToken(Statement.class, memberData.get(1), info);
		Statement elseStmt = createNodeFromToken(Statement.class, memberData.get(2), info);

		return new IfStmt(condition, thenStmt, elseStmt);
	}

	public default ForStmt createForStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(ForStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getForStatement(), 4);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ForStmt.class, token, info);
		}

		NodeList<Expression> initialization = parseListFromToken(Expression.class, memberData.get(0), info);
		Expression compare = createNodeFromToken(Expression.class, memberData.get(1), info);
		NodeList<Expression> update = parseListFromToken(Expression.class, memberData.get(2), info);
		Statement body = createNodeFromToken(Statement.class, memberData.get(3), info);

		return new ForStmt(initialization, compare, update, body);
	}

	public default ForeachStmt createForeachStmt(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(ForeachStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getForEachStatement(), 3);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ForeachStmt.class, token, info);
		}

		VariableDeclarationExpr variable = createVariableDeclarationExpr(memberData.get(0), info);
		Expression iterable = createNodeFromToken(Expression.class, memberData.get(1), info);
		Statement body = createNodeFromToken(Statement.class, memberData.get(2), info);

		return new ForeachStmt(variable, iterable, body);
	}

	public default ExpressionStmt createExpressionStmt(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(ExpressionStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getExpressionStatement(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ExpressionStmt.class, token, info);
		}

		Expression expression = createNodeFromToken(Expression.class, memberData.get(0), info);

		return new ExpressionStmt(expression);
	}

	public default ExplicitConstructorInvocationStmt createExplicitConstructorInvocationStmt(String token,
			InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(ExplicitConstructorInvocationStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getExplicitConstructorStatement(),
				3);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ExplicitConstructorInvocationStmt.class, token, info);
		}

		boolean isThis = parseBooleanFromToken(memberData.get(0));
		Expression expression = createNodeFromToken(Expression.class, memberData.get(1), info);
		NodeList<Expression> arguments = parseListFromToken(Expression.class, memberData.get(2), info);

		return new ExplicitConstructorInvocationStmt(isThis, expression, arguments);
	}

	public default DoStmt createDoStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(DoStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getDoStatement(), 2);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(DoStmt.class, token, info);
		}

		Statement body = createNodeFromToken(Statement.class, memberData.get(0), info);
		Expression condition = createNodeFromToken(Expression.class, memberData.get(1), info);

		return new DoStmt(body, condition);
	}

	public default ContinueStmt createContinueStmt(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(ContinueStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getContinueStatement(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ContinueStmt.class, token, info);
		}

		SimpleName label = createSimpleName(memberData.get(0), info);

		return new ContinueStmt(label);
	}

	public default CatchClause createCatchClause(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(CatchClause.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getCatchClauseStatement(), 5);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(CatchClause.class, token, info);
		}

		EnumSet<Modifier> exceptModifier = parseModifiersFromToken(memberData.get(0));
		NodeList<AnnotationExpr> exceptAnnotations = parseListFromToken(AnnotationExpr.class, memberData.get(1), info);
		ClassOrInterfaceType exceptType = createClassOrInterfaceType(memberData.get(2), info);
		SimpleName exceptName = createSimpleName(memberData.get(3), info);
		BlockStmt body = createBlockStmt(memberData.get(4), info);

		return new CatchClause(exceptModifier, exceptAnnotations, exceptType, exceptName, body);
	}

	public default BlockStmt createBlockStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(BlockStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getBlockStatement(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(BlockStmt.class, token, info);
		}

		NodeList<Statement> statements = parseListFromToken(Statement.class, memberData.get(0), info.getCopy());

		return new BlockStmt(statements);
	}

	public default VariableDeclarationExpr createVariableDeclarationExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(VariableDeclarationExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getVariableDeclarationExpression(),
				3);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(VariableDeclarationExpr.class, token, info);
		}

		EnumSet<Modifier> modifiers = parseModifiersFromToken(memberData.get(0));
		NodeList<AnnotationExpr> annotations = parseListFromToken(AnnotationExpr.class, memberData.get(1), info);
		NodeList<VariableDeclarator> variables = parseListFromToken(VariableDeclarator.class, memberData.get(2), info);

		return new VariableDeclarationExpr(modifiers, annotations, variables);
	}

	public default TypeExpr createTypeExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(TypeExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getTypeExpression(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(TypeExpr.class, token, info);
		}

		Type type = createNodeFromToken(Type.class, memberData.get(0), info);

		return new TypeExpr(type);
	}

	public default SuperExpr createSuperExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(SuperExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getSuperExpression(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(SuperExpr.class, token, info);
		}

		Expression classExpr = createNodeFromToken(Expression.class, memberData.get(0), info);

		return new SuperExpr(classExpr);
	}

	public default NullLiteralExpr createNullLiteralExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(NullLiteralExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getNullLiteralExpression(), 0);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(NullLiteralExpr.class, token, info);
		}

		return new NullLiteralExpr();
	}

	public default MethodReferenceExpr createMethodReferenceExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(MethodReferenceExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getMethodReferenceExpression(), 3);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(MethodReferenceExpr.class, token, info);
		}

		Expression scope = createNodeFromToken(Expression.class, memberData.get(0), info);
		NodeList<Type> typeArguments = parseListFromToken(Type.class, memberData.get(1), info);
		String identifier = parseMethodIdentifierFromToken(memberData.get(2), info);

		return new MethodReferenceExpr(scope, typeArguments, identifier);
	}

	public default LambdaExpr createLambdaExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(LambdaExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getLambdaExpression(), 3);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(LambdaExpr.class, token, info);
		}

		NodeList<Parameter> parameters = parseListFromToken(Parameter.class, memberData.get(0), info);
		Statement body = createNodeFromToken(Statement.class, memberData.get(1), info);
		boolean isEnclosingParameters = parseBooleanFromToken(memberData.get(2));

		return new LambdaExpr(parameters, body, isEnclosingParameters);
	}

	public default InstanceOfExpr createInstanceOfExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(InstanceOfExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getInstanceofExpression(), 2);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(InstanceOfExpr.class, token, info);
		}

		Expression expression = createNodeFromToken(Expression.class, memberData.get(0), info);
		ReferenceType<?> type = createNodeFromToken(ReferenceType.class, memberData.get(1), info);

		return new InstanceOfExpr(expression, type);
	}

	public default FieldAccessExpr createFieldAccessExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(FieldAccessExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getFieldAccessExpression(), 3);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(FieldAccessExpr.class, token, info);
		}

		Expression scope = createNodeFromToken(Expression.class, memberData.get(0), info);
		NodeList<Type> typeArguments = parseListFromToken(Type.class, memberData.get(1), info);
		SimpleName name = createSimpleName(memberData.get(2), info);

		return new FieldAccessExpr(scope, typeArguments, name);
	}

	public default ConditionalExpr createConditionalExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(ConditionalExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getConditionalExpression(), 3);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ConditionalExpr.class, token, info);
		}

		Expression condition = createNodeFromToken(Expression.class, memberData.get(0), info);
		Expression thenExpr = createNodeFromToken(Expression.class, memberData.get(1), info);
		Expression elseExpr = createNodeFromToken(Expression.class, memberData.get(2), info);

		return new ConditionalExpr(condition, thenExpr, elseExpr);
	}

	public default ClassExpr createClassExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(ClassExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getClassExpression(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ClassExpr.class, token, info);
		}

		Type type = createNodeFromToken(Type.class, memberData.get(0), info);

		return new ClassExpr(type);
	}

	public default CastExpr createCastExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(CastExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getCastExpression(), 2);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(CastExpr.class, token, info);
		}

		Type type = createNodeFromToken(Type.class, memberData.get(0), info);
		Expression expression = createNodeFromToken(Expression.class, memberData.get(0), info);

		return new CastExpr(type, expression);
	}

	public default AssignExpr createAssignExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(AssignExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getAssignExpression(), 3);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(AssignExpr.class, token, info);
		}

		Expression target = createNodeFromToken(Expression.class, memberData.get(0), info);
		Expression value = createNodeFromToken(Expression.class, memberData.get(1), info);
		AssignExpr.Operator operator = parseAssignOperatorFromToken(memberData.get(2));

		return new AssignExpr(target, value, operator);
	}

	public default ArrayInitializerExpr createArrayInitializerExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(ArrayInitializerExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getArrayInitExpression(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ArrayInitializerExpr.class, token, info);
		}

		NodeList<Expression> values = parseListFromToken(Expression.class, memberData.get(0), info);

		return new ArrayInitializerExpr(values);
	}

	public default ArrayCreationExpr createArrayCreationExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(ArrayCreationExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getArrayCreateExpression(), 3);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ArrayCreationExpr.class, token, info);
		}

		Type elementType = createNodeFromToken(Type.class, memberData.get(0), info);
		NodeList<ArrayCreationLevel> levels = parseListFromToken(ArrayCreationLevel.class, memberData.get(1), info);
		ArrayInitializerExpr initializer = createArrayInitializerExpr(memberData.get(2), info);

		return new ArrayCreationExpr(elementType, levels, initializer);
	}

	public default ArrayAccessExpr createArrayAccessExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(ArrayAccessExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getArrayAccessExpression(), 2);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ArrayAccessExpr.class, token, info);
		}

		Expression name = createNodeFromToken(Expression.class, memberData.get(0), info);
		Expression index = createNodeFromToken(Expression.class, memberData.get(1), info);

		return new ArrayAccessExpr(name, index);
	}

	public default PackageDeclaration createPackageDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(PackageDeclaration.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getPackageDeclaration(), 2);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(PackageDeclaration.class, token, info);
		}

		NodeList<AnnotationExpr> annotations = parseListFromToken(AnnotationExpr.class, memberData.get(0), info);
		Name name = createName(memberData.get(1), info);

		return new PackageDeclaration(annotations, name);
	}

	public default ImportDeclaration createImportDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(ImportDeclaration.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getImportDeclaration(), 3);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ImportDeclaration.class, token, info);
		}

		Name name = createName(memberData.get(0), info);
		boolean isStatic = parseBooleanFromToken(memberData.get(1));
		boolean isAsterisk = parseBooleanFromToken(memberData.get(2));

		return new ImportDeclaration(name, isStatic, isAsterisk);
	}

	public default FieldDeclaration createFieldDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(FieldDeclaration.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getFieldDeclaration(), 3);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(FieldDeclaration.class, token, info);
		}

		EnumSet<Modifier> modifiers = parseModifiersFromToken(memberData.get(0));
		NodeList<AnnotationExpr> annotations = parseListFromToken(AnnotationExpr.class, memberData.get(1), info);
		NodeList<VariableDeclarator> variables = parseListFromToken(VariableDeclarator.class, memberData.get(2), info);

		return new FieldDeclaration(modifiers, annotations, variables);
	}

	public default ClassOrInterfaceType createClassOrInterfaceType(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(ClassOrInterfaceType.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getClassOrInterfaceType(), 3);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ClassOrInterfaceType.class, token, info);
		}

		ClassOrInterfaceType scope = createClassOrInterfaceType(memberData.get(0), info);
		SimpleName name = createSimpleName(memberData.get(1), info);
		NodeList<Type> typeArguments = parseListFromToken(Type.class, memberData.get(2), info);

		return new ClassOrInterfaceType(scope, name, typeArguments);
	}

	public default ClassOrInterfaceDeclaration createClassOrInterfaceDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(ClassOrInterfaceDeclaration.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getClassOrInterfaceDeclaration(), 8);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ClassOrInterfaceDeclaration.class, token, info);
		}

		EnumSet<Modifier> modifiers = parseModifiersFromToken(memberData.get(0));
		NodeList<AnnotationExpr> annotations = parseListFromToken(AnnotationExpr.class, memberData.get(1), info);
		boolean isInterface = parseBooleanFromToken(memberData.get(2));
		SimpleName name = createSimpleName(memberData.get(3), info);
		NodeList<TypeParameter> typeParameters = parseListFromToken(TypeParameter.class, memberData.get(4), info);
		NodeList<ClassOrInterfaceType> extendedTypes = parseListFromToken(ClassOrInterfaceType.class, memberData.get(5),
				info);
		NodeList<ClassOrInterfaceType> implementedTypes = parseListFromToken(ClassOrInterfaceType.class,
				memberData.get(6), info);
		NodeList<BodyDeclaration<?>> members = parseBodyDeclarationListFromToken(memberData.get(7), info);

		return new ClassOrInterfaceDeclaration(modifiers, annotations, isInterface, name, typeParameters, extendedTypes,
				implementedTypes, members);
	}

	public default MethodDeclaration createMethodDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(MethodDeclaration.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getMethodDeclaration(), 9);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(MethodDeclaration.class, token, info);
		}

		EnumSet<Modifier> modifiers = parseModifiersFromToken(memberData.get(0));
		NodeList<AnnotationExpr> annotations = parseListFromToken(AnnotationExpr.class, memberData.get(1), info);
		NodeList<TypeParameter> typeParameters = parseListFromToken(TypeParameter.class, memberData.get(2), info);
		Type type = createNodeFromToken(Type.class, memberData.get(3), info);
		SimpleName name = createSimpleName(memberData.get(4), info);
		boolean isDefault = parseBooleanFromToken(memberData.get(5));
		NodeList<Parameter> parameters = parseListFromToken(Parameter.class, memberData.get(6), info);
		@SuppressWarnings("rawtypes")
		NodeList<ReferenceType> thrownExceptions = parseListFromToken(ReferenceType.class, memberData.get(7), info);
		BlockStmt body = createBlockStmt(memberData.get(8), info);

		return new MethodDeclaration(modifiers, annotations, typeParameters, type, name, isDefault, parameters,
				thrownExceptions, body);
	}

	public default BinaryExpr createBinaryExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(BinaryExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getBinaryExpression(), 3);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(BinaryExpr.class, token, info);
		}

		Expression left = createNodeFromToken(Expression.class, memberData.get(0), info);
		Expression right = createNodeFromToken(Expression.class, memberData.get(1), info);
		BinaryExpr.Operator operator = parseBinaryOperatorFromToken(memberData.get(2));

		return new BinaryExpr(left, right, operator);
	}

	public default UnaryExpr createUnaryExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(UnaryExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getUnaryExpression(), 2);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(UnaryExpr.class, token, info);
		}

		Expression expression = createNodeFromToken(Expression.class, memberData.get(0), info);
		UnaryExpr.Operator operator = parseUnaryOperatorFromToken(memberData.get(1));

		return new UnaryExpr(expression, operator);
	}

	public default MethodCallExpr createMethodCallExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(MethodCallExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getMethodCallExpression(), 4);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(MethodCallExpr.class, token, info);
		}

		Expression scope = createNodeFromToken(Expression.class, memberData.get(0), info);
		NodeList<Type> typeArguments = parseListFromToken(Type.class, memberData.get(1), info);
		SimpleName name = createSimpleName(memberData.get(2), info);
		NodeList<Expression> arguments = parseListFromToken(Expression.class, memberData.get(3), info);

		return new MethodCallExpr(scope, typeArguments, name, arguments);
	}

	public default NameExpr createNameExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(NameExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getNameExpression(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(NameExpr.class, token, info);
		}

		SimpleName name = createSimpleName(memberData.get(2), info);

		return new NameExpr(name);
	}

	public default ConstructorDeclaration createIntegerLiteralExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(ConstructorDeclaration.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getConstructorDeclaration(), 7);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ConstructorDeclaration.class, token, info);
		}

		EnumSet<Modifier> modifiers = parseModifiersFromToken(memberData.get(0));
		NodeList<AnnotationExpr> annotations = parseListFromToken(AnnotationExpr.class, memberData.get(1), info);
		NodeList<TypeParameter> typeParameters = parseListFromToken(TypeParameter.class, memberData.get(2), info);
		SimpleName name = createSimpleName(memberData.get(3), info);
		NodeList<Parameter> parameters = parseListFromToken(Parameter.class, memberData.get(4), info);
		@SuppressWarnings("rawtypes")
		NodeList<ReferenceType> thrownExceptions = parseListFromToken(ReferenceType.class, memberData.get(5), info);
		BlockStmt body = createBlockStmt(memberData.get(6), info);

		return new ConstructorDeclaration(modifiers, annotations, typeParameters, name, parameters, thrownExceptions,
				body);
	}

	public default DoubleLiteralExpr createDoubleLiteralExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(DoubleLiteralExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getDoubleLiteralExpression(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(DoubleLiteralExpr.class, token, info);
		}

		String value = parseStringValueFromToken(memberData.get(0));

		return new DoubleLiteralExpr(value);
	}

	public default StringLiteralExpr createStringLiteralExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(StringLiteralExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getStringLiteralExpression(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(StringLiteralExpr.class, token, info);
		}

		String value = parseStringValueFromToken(memberData.get(0));

		return new StringLiteralExpr(value);
	}

	public default BooleanLiteralExpr createBooleanLiteralExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(BooleanLiteralExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getBooleanLiteralExpression(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(BooleanLiteralExpr.class, token, info);
		}

		boolean value = parseBooleanFromToken(memberData.get(0));

		return new BooleanLiteralExpr(value);
	}

	public default CharLiteralExpr createCharLiteralExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(CharLiteralExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getCharLiteralExpression(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(CharLiteralExpr.class, token, info);
		}

		String value = parseStringValueFromToken(memberData.get(0));

		return new CharLiteralExpr(value);
	}

	public default LongLiteralExpr createLongLiteralExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(LongLiteralExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getLongLiteralExpression(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(LongLiteralExpr.class, token, info);
		}

		String value = parseStringValueFromToken(memberData.get(0));

		return new LongLiteralExpr(value);
	}

	public default ThisExpr createThisExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(ThisExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getThisExpression(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ThisExpr.class, token, info);
		}

		Expression classExpr = createNodeFromToken(Expression.class, memberData.get(0), info);

		return new ThisExpr(classExpr);
	}

	public default BreakStmt createBreakStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(BreakStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getBreak(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(BreakStmt.class, token, info);
		}

		SimpleName label = createSimpleName(memberData.get(0), info);

		return new BreakStmt(label);
	}

	public default ObjectCreationExpr createObjectCreationExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(ObjectCreationExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getObjCreateExpression(), 5);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ObjectCreationExpr.class, token, info);
		}

		Expression scope = createNodeFromToken(Expression.class, memberData.get(0), info);
		ClassOrInterfaceType type = createClassOrInterfaceType(memberData.get(1), info);
		NodeList<Type> typeArguments = parseListFromToken(Type.class, memberData.get(2), info);
		NodeList<Expression> arguments = parseListFromToken(Expression.class, memberData.get(3), info);
		NodeList<BodyDeclaration<?>> anonymousClassBody = parseBodyDeclarationListFromToken(memberData.get(4), info);

		return new ObjectCreationExpr(scope, type, typeArguments, arguments, anonymousClassBody);
	}

	public default MarkerAnnotationExpr createMarkerAnnotationExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(MarkerAnnotationExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getMarkerAnnotationExpression(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(MarkerAnnotationExpr.class, token, info);
		}

		Name name = createName(memberData.get(0), info);

		return new MarkerAnnotationExpr(name);
	}

	public default NormalAnnotationExpr createNormalAnnotationExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(NormalAnnotationExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getNormalAnnotationExpression(), 2);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(NormalAnnotationExpr.class, token, info);
		}

		Name name = createName(memberData.get(0), info);
		NodeList<MemberValuePair> pairs = parseListFromToken(MemberValuePair.class, memberData.get(1), info);

		return new NormalAnnotationExpr(name, pairs);
	}

	public default SingleMemberAnnotationExpr createSingleMemberAnnotationExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(SingleMemberAnnotationExpr.class);
		List<String> memberData = parseAndCheckMembers(token,
				getKeyWordProvider().getSingleMemberAnnotationExpression(), 2);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(SingleMemberAnnotationExpr.class, token, info);
		}

		Name name = createName(memberData.get(0), info);
		Expression memberValue = createNodeFromToken(Expression.class, memberData.get(1), info);

		return new SingleMemberAnnotationExpr(name, memberValue);
	}

	public default Parameter createParameter(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(Parameter.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getParameter(), 6);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(Parameter.class, token, info);
		}

		EnumSet<Modifier> modifiers = parseModifiersFromToken(memberData.get(0));
		NodeList<AnnotationExpr> annotations = parseListFromToken(AnnotationExpr.class, memberData.get(1), info);
		Type type = createNodeFromToken(Type.class, memberData.get(2), info);
		boolean isVarArgs = parseBooleanFromToken(memberData.get(3));
		NodeList<AnnotationExpr> varArgsAnnotations = parseListFromToken(AnnotationExpr.class, memberData.get(4), info);
		SimpleName name = createSimpleName(memberData.get(5), info);

		return new Parameter(modifiers, annotations, type, isVarArgs, varArgsAnnotations, name);
	}

	public default EnclosedExpr createEnclosedExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(EnclosedExpr.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getEnclosedExpression(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(EnclosedExpr.class, token, info);
		}

		Expression inner = createNodeFromToken(Expression.class, memberData.get(0), info);

		return new EnclosedExpr(inner);
	}

	public default AssertStmt createAssertStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(AssertStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getAssertStmt(), 2);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(AssertStmt.class, token, info);
		}

		Expression check = createNodeFromToken(Expression.class, memberData.get(0), info);
		Expression message = createNodeFromToken(Expression.class, memberData.get(1), info);

		return new AssertStmt(check, message);
	}

	public default ConstructorDeclaration createMemberValuePair(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(ConstructorDeclaration.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getConstructorDeclaration(), 7);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ConstructorDeclaration.class, token, info);
		}

		EnumSet<Modifier> modifiers = parseModifiersFromToken(memberData.get(0));
		NodeList<AnnotationExpr> annotations = parseListFromToken(AnnotationExpr.class, memberData.get(1), info);
		NodeList<TypeParameter> typeParameters = parseListFromToken(TypeParameter.class, memberData.get(2), info);
		SimpleName name = createSimpleName(memberData.get(3), info);
		NodeList<Parameter> parameters = parseListFromToken(Parameter.class, memberData.get(4), info);
		@SuppressWarnings("rawtypes")
		NodeList<ReferenceType> thrownExceptions = parseListFromToken(ReferenceType.class, memberData.get(5), info);
		BlockStmt body = createBlockStmt(memberData.get(6), info);

		return new ConstructorDeclaration(modifiers, annotations, typeParameters, name, parameters, thrownExceptions,
				body);
	}

	public default PrimitiveType createPrimitiveType(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(PrimitiveType.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getTypePrimitive(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(PrimitiveType.class, token, info);
		}

		Primitive type = parsePrimitiveFromToken(memberData.get(0));

		return new PrimitiveType(type);
	}

	public default UnionType createUnionType(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(UnionType.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getTypeUnion(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(UnionType.class, token, info);
		}

		@SuppressWarnings("rawtypes")
		NodeList<ReferenceType> elements = parseListFromToken(ReferenceType.class, memberData.get(0), info);

		return new UnionType(elements);
	}

	public default IntersectionType createIntersectionType(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(IntersectionType.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getTypeIntersection(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(IntersectionType.class, token, info);
		}

		@SuppressWarnings("rawtypes")
		NodeList<ReferenceType> elements = parseListFromToken(ReferenceType.class, memberData.get(0), info);

		return new IntersectionType(elements);
	}

	public default TypeParameter createTypeParameter(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(TypeParameter.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getTypePar(), 3);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(TypeParameter.class, token, info);
		}

		SimpleName name = createSimpleName(memberData.get(0), info);
		NodeList<ClassOrInterfaceType> typeBound = parseListFromToken(ClassOrInterfaceType.class, memberData.get(1),
				info);
		NodeList<AnnotationExpr> annotations = parseListFromToken(AnnotationExpr.class, memberData.get(2), info);

		return new TypeParameter(name, typeBound, annotations);
	}

	public default WildcardType createWildcardType(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(WildcardType.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getTypeWildcard(), 2);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(WildcardType.class, token, info);
		}

		@SuppressWarnings("rawtypes")
		ReferenceType extendedType = createNodeFromToken(ReferenceType.class, memberData.get(0), info);
		@SuppressWarnings("rawtypes")
		ReferenceType superType = createNodeFromToken(ReferenceType.class, memberData.get(0), info);

		return new WildcardType(extendedType, superType);
	}

	public default VoidType createVoidType(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(VoidType.class);

		return new VoidType();
	}

	public default UnknownType createUnknownType(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(UnknownType.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getTypeUnknown(), 0);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(UnknownType.class, token, info);
		}

		return new UnknownType();
	}

	public default UnknownNode createUnknown(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(UnknownNode.class);

		return new UnknownNode();
	}

	public default Name createName(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(Name.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getName(), 3);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(Name.class, token, info);
		}

		Name qualifier = createName(memberData.get(0), info);
		String identifier = parseStringValueFromToken(memberData.get(1));
		NodeList<AnnotationExpr> annotations = parseListFromToken(AnnotationExpr.class, memberData.get(2), info);

		return new Name(qualifier, identifier, annotations);
	}

	public default SimpleName createSimpleName(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(SimpleName.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getSimpleName(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(SimpleName.class, token, info);
		}

		String identifier = parseStringValueFromToken(memberData.get(0));

		return new SimpleName(identifier);
	}

	public default LocalClassDeclarationStmt createLocalClassDeclarationStmt(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(LocalClassDeclarationStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getLocalClassDeclarationStmt(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(LocalClassDeclarationStmt.class, token, info);
		}

		ClassOrInterfaceDeclaration classDeclaration = createClassOrInterfaceDeclaration(memberData.get(0), info);

		return new LocalClassDeclarationStmt(classDeclaration);
	}

	public default ArrayType createArrayType(String token, InformationWrapper info) throws IllegalArgumentException {
		info.addNodeClassToHistory(ArrayType.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getArrayType(), 2);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ArrayType.class, token, info);
		}

		Type componentType = createNodeFromToken(Type.class, memberData.get(0), info);
		NodeList<AnnotationExpr> annotations = parseListFromToken(AnnotationExpr.class, memberData.get(1), info);

		return new ArrayType(componentType, annotations);
	}

	public default ArrayCreationLevel createArrayCreationLevel(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(ArrayCreationLevel.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getArrayCreationLevel(), 2);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ArrayCreationLevel.class, token, info);
		}

		Expression dimension = createNodeFromToken(Expression.class, memberData.get(0), info);
		NodeList<AnnotationExpr> annotations = parseListFromToken(AnnotationExpr.class, memberData.get(1), info);

		return new ArrayCreationLevel(dimension, annotations);
	}

	public default ModuleDeclaration createModuleDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(ModuleDeclaration.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getModuleDeclaration(), 4);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ModuleDeclaration.class, token, info);
		}

		NodeList<AnnotationExpr> annotations = parseListFromToken(AnnotationExpr.class, memberData.get(0), info);
		Name name = createName(memberData.get(1), info);
		boolean isOpen = parseBooleanFromToken(memberData.get(2));
		NodeList<ModuleStmt> moduleStmts = parseListFromToken(ModuleStmt.class, memberData.get(3), info);

		return new ModuleDeclaration(annotations, name, isOpen, moduleStmts);
	}

	public default ModuleExportsStmt createModuleExportsStmt(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(ModuleExportsStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getModuleExportsStmt(), 2);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ModuleExportsStmt.class, token, info);
		}

		Name name = createName(memberData.get(0), info);
		NodeList<Name> moduleNames = parseListFromToken(Name.class, memberData.get(1), info);

		return new ModuleExportsStmt(name, moduleNames);
	}

	public default ModuleOpensStmt createModuleOpensStmt(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(ModuleOpensStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getModuleOpensStmt(), 2);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ModuleOpensStmt.class, token, info);
		}

		Name name = createName(memberData.get(0), info);
		NodeList<Name> moduleNames = parseListFromToken(Name.class, memberData.get(1), info);

		return new ModuleOpensStmt(name, moduleNames);
	}

	public default ModuleProvidesStmt createModuleProvidesStmt(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(ModuleProvidesStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getModuleProvidesStmt(), 2);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ModuleProvidesStmt.class, token, info);
		}

		Type type = createNodeFromToken(Type.class, memberData.get(0), info);
		NodeList<Type> withTypes = parseListFromToken(Type.class, memberData.get(1), info);

		return new ModuleProvidesStmt(type, withTypes);
	}

	public default ModuleRequiresStmt createModuleRequiresStmt(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(ModuleRequiresStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getModuleRequiresStmt(), 2);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ModuleRequiresStmt.class, token, info);
		}

		EnumSet<Modifier> modifiers = parseModifiersFromToken(memberData.get(0));
		Name name = createName(memberData.get(1), info);

		return new ModuleRequiresStmt(modifiers, name);
	}

	public default ModuleUsesStmt createModuleUsesStmt(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(ModuleUsesStmt.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getModuleUsesStmt(), 1);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(ModuleUsesStmt.class, token, info);
		}

		Type type = createNodeFromToken(Type.class, memberData.get(0), info);

		return new ModuleUsesStmt(type);
	}

	public default CompilationUnit createCompilationUnit(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info.addNodeClassToHistory(CompilationUnit.class);
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getCompilationUnit(), 4);
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { // token: id
			return guessNodeFromKeyWord(CompilationUnit.class, token, info);
		}

		PackageDeclaration packageDeclaration = createNodeFromToken(PackageDeclaration.class, memberData.get(0), info);
		NodeList<ImportDeclaration> imports = parseListFromToken(ImportDeclaration.class, memberData.get(1), info);
		NodeList<TypeDeclaration<?>> types = parseTypeDeclarationListFromToken(memberData.get(2), info);
		ModuleDeclaration module = createNodeFromToken(ModuleDeclaration.class, memberData.get(3), info);

		return new CompilationUnit(packageDeclaration, imports, types, module);
	}

}
