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

import se.de.hu_berlin.informatik.astlmbuilder.mapping.IModifierHandler;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.IOperatorHandler;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.ITypeHandler;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IBasicKeyWords;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider.KeyWords;

@SuppressWarnings("deprecation")
public interface IAbstractionMapper extends IAbstractionMapperBasics, IModifierHandler, IOperatorHandler, ITypeHandler {

	public boolean usesStringAbstraction();
	public boolean usesCharAbstraction();
	public boolean usesNumberAbstraction();
	public boolean usesPrivateMethodAbstraction();
	public boolean usesMethodNameAbstraction();
	public boolean usesVariableNameAbstraction();
	public boolean usesGenericTypeNameAbstraction();
	public boolean usesClassNameAbstraction();
	public boolean usesPackageAndImportAbstraction();
	public boolean usesAnnotationAbstraction();

	// all tokens (if not abstract) are stored with all respective constructor
	// arguments (@allFieldsConstructor)

	@Override
	public default String getMappingForMemberValuePair(MemberValuePair aNode, int aAbsDepth) {
		// final SimpleName name, final Expression value
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.MEMBER_VALUE_PAIR, aAbsDepth,
				() -> getMappingForSimpleName(aNode.getName(), usesVariableNameAbstraction() ? 0 : aAbsDepth - 1),
				() -> getMappingForExpression(aNode.getValue(), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForSwitchEntryStmt(SwitchEntryStmt aNode, int aAbsDepth) {
		// final Expression label, final NodeList<Statement> statements
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.SWITCH_ENTRY_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getLabel().orElse(null), aAbsDepth - 1),
				() -> getMappingForStatementList(aNode.getStatements(), false, aAbsDepth - 1));
	}

	@Override
	public default String getMappingForUnionType(UnionType aNode, int aAbsDepth) {
		// NodeList<ReferenceType> elements
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.TYPE_UNION, aAbsDepth,
				() -> getMappingForTypeList(aNode.getElements(), true, aAbsDepth - 1));
	}

	@Override
	public default String getMappingForIntersectionType(IntersectionType aNode, int aAbsDepth) {
		// NodeList<ReferenceType> elements
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.TYPE_INTERSECTION, aAbsDepth,
				() -> getMappingForTypeList(aNode.getElements(), true, aAbsDepth - 1));
	}

	@Override
	public default String getMappingForLambdaExpr(LambdaExpr aNode, int aAbsDepth) {
		// NodeList<Parameter> parameters, Statement body, boolean
		// isEnclosingParameters
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.LAMBDA_EXPRESSION, aAbsDepth,
				() -> getMappingForParameterList(aNode.getParameters(), true, aAbsDepth - 1),
				() -> getMappingForStatement(aNode.getBody(), aAbsDepth - 1),
				() -> getMappingForBoolean(aNode.isEnclosingParameters()));
	}

	@Override
	public default String getMappingForInstanceOfExpr(InstanceOfExpr aNode, int aAbsDepth) {
		// final Expression expression, final ReferenceType<?> type
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.INSTANCEOF_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression(), aAbsDepth - 1),
				() -> getMappingForType(aNode.getType(), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForConditionalExpr(ConditionalExpr aNode, int aAbsDepth) {
		// Expression condition, Expression thenExpr, Expression elseExpr
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.CONDITIONAL_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getCondition(), aAbsDepth - 1),
				() -> getMappingForExpression(aNode.getThenExpr(), aAbsDepth - 1),
				() -> getMappingForExpression(aNode.getElseExpr(), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForObjectCreationExpr(ObjectCreationExpr aNode, int aAbsDepth) {
		// final Expression scope, final ClassOrInterfaceType type, final
		// NodeList<Type> typeArguments,
		// final NodeList<Expression> arguments, final
		// NodeList<BodyDeclaration<?>> anonymousClassBody
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.OBJ_CREATE_EXPRESSION, aAbsDepth,
				// TODO: get full scope if depth > 0
				() -> getMappingForExpression(aNode.getScope().orElse(null), aAbsDepth - 1),
				() -> getMappingForClassOrInterfaceType(aNode.getType(), aAbsDepth - 1),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, aAbsDepth - 1),
				() -> getMappingForExpressionList(aNode.getArguments(), true, aAbsDepth - 1),
				() -> getMappingForBodyDeclarationList(
						aNode.getAnonymousClassBody().orElse(null), false, aAbsDepth - 1));
	}

	@Override
	public default String getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode, int aAbsDepth) {
		// final ClassOrInterfaceType scope, final SimpleName name, final
		// NodeList<Type> typeArguments
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.CLASS_OR_INTERFACE_TYPE, aAbsDepth,
				// get full scope if depth > 0
				() -> getMappingForType(aNode.getScope().orElse(null), aAbsDepth), 
				() -> getMappingForSimpleName(aNode.getName(), usesClassNameAbstraction() ? 0 : aAbsDepth - 1),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, aAbsDepth - 1));
	}

	@Override
	public default String getMappingForEnclosedExpr(EnclosedExpr aNode, int aAbsDepth) {
		// final Expression inner
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.ENCLOSED_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getInner().orElse(null), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForArrayInitializerExpr(ArrayInitializerExpr aNode, int aAbsDepth) {
		// NodeList<Expression> values
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.ARRAY_INIT_EXPRESSION, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getValues(), true, aAbsDepth - 1));
	}

	@Override
	public default String getMappingForArrayCreationExpr(ArrayCreationExpr aNode, int aAbsDepth) {
		// Type elementType, NodeList<ArrayCreationLevel> levels,
		// ArrayInitializerExpr initializer
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.ARRAY_CREATE_EXPRESSION, aAbsDepth,
				() -> getMappingForType(aNode.getElementType(), aAbsDepth - 1),
				() -> getMappingForArrayCreationLevelList(aNode.getLevels(), true, aAbsDepth - 1),
				() -> getMappingForExpression(aNode.getInitializer().orElse(null), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForArrayAccessExpr(ArrayAccessExpr aNode, int aAbsDepth) {
		// Expression name, Expression index
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.ARRAY_ACCESS_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getName(), aAbsDepth - 1),
				() -> getMappingForExpression(aNode.getIndex(), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForTypeParameter(TypeParameter aNode, int aAbsDepth) {
		// SimpleName name, NodeList<ClassOrInterfaceType> typeBound,
		// NodeList<AnnotationExpr> annotations
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.TYPE_PAR, aAbsDepth,
				() -> getMappingForSimpleName(aNode.getName(), usesGenericTypeNameAbstraction() ? 0 : aAbsDepth - 1),
				() -> getMappingForClassOrInterfaceTypeList(aNode.getTypeBound(), true, aAbsDepth - 1),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth - 1));
	}

	@Override
	public default String getMappingForVariableDeclarator(VariableDeclarator aNode, int aAbsDepth) {
		// Type type, SimpleName name, Expression initializer
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.VARIABLE_DECLARATOR, aAbsDepth,
				() -> getMappingForType(aNode.getType(), aAbsDepth - 1),
				() -> getMappingForSimpleName(aNode.getName(), usesVariableNameAbstraction() ? 0 : aAbsDepth - 1),
				() -> getMappingForExpression(aNode.getInitializer().orElse(null), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForImportDeclaration(ImportDeclaration aNode, int aAbsDepth) {
		// Name name, boolean isStatic, boolean isAsterisk
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.IMPORT_DECLARATION, aAbsDepth,
				() -> getMappingForName(aNode.getName(), usesPackageAndImportAbstraction() ? 0 : aAbsDepth - 1),
				() -> getMappingForBoolean(aNode.isStatic()), () -> getMappingForBoolean(aNode.isAsterisk()));
	}

	@Override
	public default String getMappingForPackageDeclaration(PackageDeclaration aNode, int aAbsDepth) {
		// NodeList<AnnotationExpr> annotations, Name name
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.PACKAGE_DECLARATION, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth - 1),
				() -> getMappingForName(aNode.getName(), usesPackageAndImportAbstraction() ? 0 : aAbsDepth - 1));
	}

	@Override
	public default String getMappingForParameter(Parameter aNode, int aAbsDepth) {
		// EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations,
		// Type type,
		// boolean isVarArgs, NodeList<AnnotationExpr> varArgsAnnotations,
		// SimpleName name
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.PARAMETER, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth - 1),
				() -> getMappingForType(aNode.getType(), aAbsDepth - 1), () -> getMappingForBoolean(aNode.isVarArgs()),
				() -> getMappingForExpressionList(aNode.getVarArgsAnnotations(), true, aAbsDepth - 1),
				() -> getMappingForSimpleName(aNode.getName(), usesVariableNameAbstraction() ? 0 : aAbsDepth - 1));
	}

	@Override
	public default String getMappingForEnumDeclaration(EnumDeclaration aNode, int aAbsDepth) {
		// EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations,
		// SimpleName name,
		// NodeList<ClassOrInterfaceType> implementedTypes,
		// NodeList<EnumConstantDeclaration> entries,
		// NodeList<BodyDeclaration<?>> members
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.ENUM_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth - 1),
				() -> getMappingForSimpleName(aNode.getName(), usesVariableNameAbstraction() ? 0 : aAbsDepth - 1),
				() -> getMappingForClassOrInterfaceTypeList(aNode.getImplementedTypes(), true, aAbsDepth - 1),
				() -> getMappingForBodyDeclarationList(aNode.getEntries(), true, aAbsDepth - 1),
				() -> getMappingForBodyDeclarationList(aNode.getMembers(), false, aAbsDepth - 1));
	}

	@Override
	public default String getMappingForClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration aNode, int aAbsDepth) {
		// final EnumSet<Modifier> modifiers, final NodeList<AnnotationExpr>
		// annotations, final boolean isInterface,
		// final SimpleName name, final NodeList<TypeParameter> typeParameters,
		// final NodeList<ClassOrInterfaceType> extendedTypes,
		// final NodeList<ClassOrInterfaceType> implementedTypes, final
		// NodeList<BodyDeclaration<?>> members
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.CLASS_OR_INTERFACE_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth - 1),
				() -> getMappingForBoolean(aNode.isInterface()),
				() -> getMappingForSimpleName(aNode.getName(), usesClassNameAbstraction() ? 0 : aAbsDepth - 1),
				() -> getMappingsForTypeParameterList(aNode.getTypeParameters(), true, aAbsDepth - 1),
				() -> getMappingForClassOrInterfaceTypeList(aNode.getExtendedTypes(), true, aAbsDepth - 1),
				() -> getMappingForClassOrInterfaceTypeList(aNode.getImplementedTypes(), true, aAbsDepth - 1),
				() -> getMappingForBodyDeclarationList(aNode.getMembers(), false, aAbsDepth - 1));
	}

	@Override
	public default String getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode, int aAbsDepth) {
		// NodeList<AnnotationExpr> annotations, SimpleName name,
		// NodeList<Expression> arguments, NodeList<BodyDeclaration<?>>
		// classBody
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.ENUM_CONSTANT_DECLARATION, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth - 1),
				() -> getMappingForSimpleName(aNode.getName(), usesVariableNameAbstraction() ? 0 : aAbsDepth - 1),
				() -> getMappingForExpressionList(aNode.getArguments(), true, aAbsDepth - 1),
				() -> getMappingForBodyDeclarationList(aNode.getClassBody(), false, aAbsDepth - 1));
	}

	@Override
	public default String getMappingForMethodDeclaration(MethodDeclaration aNode, int aAbsDepth) {
		// final EnumSet<Modifier> modifiers, final NodeList<AnnotationExpr>
		// annotations, final NodeList<TypeParameter> typeParameters,
		// final Type type, final SimpleName name, final boolean isDefault,
		// final NodeList<Parameter> parameters,
		// final NodeList<ReferenceType> thrownExceptions, final BlockStmt body
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.METHOD_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth - 1),
				() -> getMappingsForTypeParameterList(aNode.getTypeParameters(), true, aAbsDepth - 1),
				() -> getMappingForType(aNode.getType(), aAbsDepth - 1),
				() -> getMappingForSimpleName(aNode.getName(), usesMethodNameAbstraction() ? 0 : aAbsDepth - 1),
				() -> getMappingForBoolean(aNode.isDefault()),
				() -> getMappingForParameterList(aNode.getParameters(), true, aAbsDepth - 1),
				() -> getMappingForTypeList(aNode.getThrownExceptions(), true, aAbsDepth - 1),
				() -> getMappingForStatement(aNode.getBody().orElse(null), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForFieldDeclaration(FieldDeclaration aNode, int aAbsDepth) {
		// EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations,
		// NodeList<VariableDeclarator> variables
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.FIELD_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth - 1),
				() -> getMappingForVariableDeclaratorList(aNode.getVariables(), true, aAbsDepth - 1));
	}

	@Override
	public default String getMappingForConstructorDeclaration(ConstructorDeclaration aNode, int aAbsDepth) {
		// EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations,
		// NodeList<TypeParameter> typeParameters,
		// SimpleName name, NodeList<Parameter> parameters,
		// NodeList<ReferenceType> thrownExceptions, BlockStmt body
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.CONSTRUCTOR_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth - 1),
				() -> getMappingsForTypeParameterList(aNode.getTypeParameters(), true, aAbsDepth - 1),
				() -> getMappingForSimpleName(aNode.getName(), usesClassNameAbstraction() ? 0 : aAbsDepth - 1),
				() -> getMappingForParameterList(aNode.getParameters(), true, aAbsDepth - 1),
				() -> getMappingForTypeList(aNode.getThrownExceptions(), true, aAbsDepth - 1),
				() -> getMappingForStatement(aNode.getBody(), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForWhileStmt(WhileStmt aNode, int aAbsDepth) {
		// final Expression condition, final Statement body
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.WHILE_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getCondition(), aAbsDepth - 1),
				() -> getMappingForStatement(aNode.getBody(), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForSwitchStmt(SwitchStmt aNode, int aAbsDepth) {
		// final Expression selector, final NodeList<SwitchEntryStmt> entries
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.SWITCH_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getSelector(), aAbsDepth - 1),
				() -> getMappingForStatementList(aNode.getEntries(), false, aAbsDepth - 1));
	}

	@Override
	public default String getMappingForForStmt(ForStmt aNode, int aAbsDepth) {
		// final NodeList<Expression> initialization, final Expression compare,
		// final NodeList<Expression> update, final Statement body
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.FOR_STMT, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getInitialization(), true, aAbsDepth - 1),
				() -> getMappingForExpression(aNode.getCompare().orElse(null), aAbsDepth - 1),
				() -> getMappingForExpressionList(aNode.getUpdate(), true, aAbsDepth - 1),
				() -> getMappingForStatement(aNode.getBody(), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForForeachStmt(ForeachStmt aNode, int aAbsDepth) {
		// final VariableDeclarationExpr variable, final Expression iterable,
		// final Statement body
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.FOR_EACH_STMT, aAbsDepth,
				() -> getMappingForVariableDeclarationExpr(aNode.getVariable(), aAbsDepth - 1),
				() -> getMappingForExpression(aNode.getIterable(), aAbsDepth - 1),
				() -> getMappingForStatement(aNode.getBody(), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForExplicitConstructorInvocationStmt(ExplicitConstructorInvocationStmt aNode,
			int aAbsDepth) {
		// final NodeList<Type> typeArguments, final boolean isThis, final
		// Expression expression, final NodeList<Expression> arguments
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.EXPL_CONSTR_INVOC_STMT, aAbsDepth,
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, aAbsDepth - 1),
				() -> getMappingForBoolean(aNode.isThis()),
				() -> getMappingForExpression(aNode.getExpression().orElse(null), aAbsDepth - 1),
				() -> getMappingForExpressionList(aNode.getArguments(), true, aAbsDepth - 1));
	}

	@Override
	public default String getMappingForDoStmt(DoStmt aNode, int aAbsDepth) {
		// final Statement body, final Expression condition
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.DO_STMT, aAbsDepth,
				() -> getMappingForStatement(aNode.getBody(), aAbsDepth - 1),
				() -> getMappingForExpression(aNode.getCondition(), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForAssertStmt(AssertStmt aNode, int aAbsDepth) {
		// final Expression check, final Expression message
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.ASSERT_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getCheck(), aAbsDepth - 1),
				() -> getMappingForExpression(aNode.getMessage().orElse(null), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForPrimitiveType(PrimitiveType aNode, int aAbsDepth) {
		// final Primitive type
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.TYPE_PRIMITIVE, aAbsDepth,
				() -> getMappingForPrimitive(aNode.getType()));
	}

	@Override
	public default String getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode, int aAbsDepth) {
		// final EnumSet<Modifier> modifiers, final NodeList<AnnotationExpr>
		// annotations, final NodeList<VariableDeclarator> variables
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.VARIABLE_DECLARATION_EXPRESSION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth - 1),
				() -> getMappingForVariableDeclaratorList(aNode.getVariables(), true, aAbsDepth - 1));
	}

	@Override
	public default String getMappingForMethodReferenceExpr(MethodReferenceExpr aNode, int aAbsDepth) {
		// Expression scope, NodeList<Type> typeArguments, String identifier
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.METHOD_REFERENCE_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getScope(), aAbsDepth - 1),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, aAbsDepth - 1),
				() -> usesMethodNameAbstraction() ? String.valueOf(IBasicKeyWords.KEYWORD_ABSTRACT)
						: getMappingForString(aNode.getIdentifier()));
	}

	@Override
	public default String getMappingForMethodCallExpr(MethodCallExpr aNode, int aAbsDepth) {
		// final Expression scope, final NodeList<Type> typeArguments, final
		// SimpleName name, final NodeList<Expression> arguments
		boolean isPrivate = aNode == null ? false
				: getPrivateMethodBlackList().contains(getMappingForSimpleName(aNode.getName(), 1));
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.METHOD_CALL_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getScope().orElse(null), isPrivate ? 0 : aAbsDepth - 1),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, aAbsDepth - 1),
				() -> getMappingForSimpleName(
						aNode.getName(), isPrivate || usesMethodNameAbstraction() ? 0 : aAbsDepth - 1),
				() -> getMappingForExpressionList(aNode.getArguments(), true, aAbsDepth - 1));
	}

	@Override
	public default String getMappingForFieldAccessExpr(FieldAccessExpr aNode, int aAbsDepth) {
		// final Expression scope, final NodeList<Type> typeArguments, final
		// SimpleName name
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.FIELD_ACCESS_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getScope().orElse(null), aAbsDepth - 1),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, aAbsDepth - 1),
				() -> getMappingForSimpleName(aNode.getName(), usesVariableNameAbstraction() ? 0 : aAbsDepth - 1));
	}

	@Override
	public default String getMappingForTypeExpr(TypeExpr aNode, int aAbsDepth) {
		// Type type
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.TYPE_EXPRESSION, aAbsDepth,
				() -> getMappingForType(aNode.getType(), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForClassExpr(ClassExpr aNode, int aAbsDepth) {
		// Type type
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.CLASS_EXPRESSION, aAbsDepth,
				() -> getMappingForType(aNode.getType(), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForCastExpr(CastExpr aNode, int aAbsDepth) {
		// Type type, Expression expression
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.CAST_EXPRESSION, aAbsDepth,
				() -> getMappingForType(aNode.getType(), aAbsDepth - 1),
				() -> getMappingForExpression(aNode.getExpression(), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForUnaryExpr(UnaryExpr aNode, int aAbsDepth) {
		// final Expression expression, final Operator operator
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.UNARY_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression(), aAbsDepth - 1),
				() -> getMappingForUnaryOperator(aNode.getOperator()));
	}

	@Override
	public default String getMappingForBinaryExpr(BinaryExpr aNode, int aAbsDepth) {
		// Expression left, Expression right, Operator operator
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.BINARY_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getLeft(), aAbsDepth - 1),
				() -> getMappingForExpression(aNode.getRight(), aAbsDepth - 1),
				() -> getMappingForBinaryOperator(aNode.getOperator()));
	}

	@Override
	public default String getMappingForAssignExpr(AssignExpr aNode, int aAbsDepth) {
		// Expression target, Expression value, Operator operator
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.ASSIGN_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getTarget(), aAbsDepth - 1),
				() -> getMappingForExpression(aNode.getValue(), aAbsDepth - 1),
				() -> getMappingForAssignOperator(aNode.getOperator()));
	}

	@Override
	public default String getMappingForIfStmt(IfStmt aNode, int aAbsDepth) {
		// final Expression condition, final Statement thenStmt, final Statement
		// elseStmt
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.IF_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getCondition(), aAbsDepth - 1),
				() -> getMappingForStatement(aNode.getThenStmt(), aAbsDepth - 1),
				() -> getMappingForStatement(aNode.getElseStmt().orElse(null), aAbsDepth - 1));
	}

	@Override
	default String getMappingForLocalClassDeclarationStmt(LocalClassDeclarationStmt aNode, int aAbsDepth) {
		// final ClassOrInterfaceDeclaration classDeclaration
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.LOCAL_CLASS_DECLARATION_STMT, aAbsDepth,
				() -> getMappingForClassOrInterfaceDeclaration(aNode.getClassDeclaration(), aAbsDepth - 1));
	}

	@Override
	default String getMappingForArrayType(ArrayType aNode, int aAbsDepth) {
		// Type componentType, NodeList<AnnotationExpr> annotations
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.ARRAY_TYPE, aAbsDepth,
				() -> getMappingForType(aNode.getComponentType(), aAbsDepth - 1),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth - 1));
	}

	@Override
	default String getMappingForArrayCreationLevel(ArrayCreationLevel aNode, int aAbsDepth) {
		// Expression dimension, NodeList<AnnotationExpr> annotations
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.ARRAY_CREATION_LEVEL, aAbsDepth,
				() -> getMappingForExpression(aNode.getDimension().orElse(null), aAbsDepth - 1),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth - 1));
	}

	@Override
	public default String getMappingForInitializerDeclaration(InitializerDeclaration aNode, int aAbsDepth) {
		// boolean isStatic, BlockStmt body
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.INITIALIZER_DECLARATION, aAbsDepth,
				() -> getMappingForBoolean(aNode.isStatic()),
				() -> getMappingForStatement(aNode.getBody(), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForThrowStmt(ThrowStmt aNode, int aAbsDepth) {
		// final Expression expression
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.THROW_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression(), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForNameExpr(NameExpr aNode, int aAbsDepth) {
		// final SimpleName name
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.NAME_EXPRESSION, aAbsDepth,
				() -> getMappingForSimpleName(aNode.getName(), usesVariableNameAbstraction() ? 0 : aAbsDepth - 1));
	}

	@Override
	public default String getMappingForTryStmt(TryStmt aNode, int aAbsDepth) {
		// NodeList<VariableDeclarationExpr> resources, final BlockStmt
		// tryBlock, final NodeList<CatchClause> catchClauses, final BlockStmt
		// finallyBlock
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.TRY_STMT, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getResources(), true, aAbsDepth - 1),
				() -> getMappingForStatement(aNode.getTryBlock().orElse(null), aAbsDepth - 1),
				() -> getMappingForNodeList(aNode.getCatchClauses(), true, aAbsDepth - 1),
				() -> getMappingForStatement(aNode.getFinallyBlock().orElse(null), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForThisExpr(ThisExpr aNode, int aAbsDepth) {
		// final Expression classExpr
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.THIS_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getClassExpr().orElse(null), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForBlockComment(BlockComment aNode, int aAbsDepth) {
		// String content
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.BLOCK_COMMENT, aAbsDepth,
				() -> getMappingForString(aNode.getContent()));
	}

	@Override
	public default String getMappingForExpressionStmt(ExpressionStmt aNode, int aAbsDepth) {
		// final Expression expression
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.EXPRESSION_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression(), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForSuperExpr(SuperExpr aNode, int aAbsDepth) {
		// final Expression classExpr
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.SUPER_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getClassExpr().orElse(null), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForReturnStmt(ReturnStmt aNode, int aAbsDepth) {
		// final Expression expression
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.RETURN_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression().orElse(null), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForLabeledStmt(LabeledStmt aNode, int aAbsDepth) {
		// final SimpleName label, final Statement statement
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.LABELED_STMT, aAbsDepth,
				() -> getMappingForSimpleName(aNode.getLabel(), usesVariableNameAbstraction() ? 0 : aAbsDepth - 1),
				() -> getMappingForStatement(aNode.getStatement(), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForBreakStmt(BreakStmt aNode, int aAbsDepth) {
		// final SimpleName label
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.BREAK, aAbsDepth, () -> getMappingForSimpleName(
						aNode.getLabel().orElse(null), usesVariableNameAbstraction() ? 0 : aAbsDepth - 1));
	}

	@Override
	public default String getMappingForSingleMemberAnnotationExpr(SingleMemberAnnotationExpr aNode, int aAbsDepth) {
		// final Name name, final Expression memberValue
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.SINGLE_MEMBER_ANNOTATION_EXPRESSION, aAbsDepth,
				() -> getMappingForName(aNode.getName(), usesAnnotationAbstraction() ? 0 : aAbsDepth - 1),
				() -> getMappingForExpression(aNode.getMemberValue(), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForNormalAnnotationExpr(NormalAnnotationExpr aNode, int aAbsDepth) {
		// final Name name, final NodeList<MemberValuePair> pairs
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.NORMAL_ANNOTATION_EXPRESSION, aAbsDepth,
				() -> getMappingForName(aNode.getName(), usesAnnotationAbstraction() ? 0 : aAbsDepth - 1),
				() -> getMappingForNodeList(aNode.getPairs(), true, aAbsDepth - 1));
	}

	@Override
	public default String getMappingForMarkerAnnotationExpr(MarkerAnnotationExpr aNode, int aAbsDepth) {
		// final Name name
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.MARKER_ANNOTATION_EXPRESSION, aAbsDepth,
				() -> getMappingForName(aNode.getName(), usesAnnotationAbstraction() ? 0 : aAbsDepth - 1));
	}

	@Override
	public default String getMappingForWildcardType(WildcardType aNode, int aAbsDepth) {
		// final ReferenceType extendedType, final ReferenceType superType
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.TYPE_WILDCARD, aAbsDepth,
				() -> getMappingForType(aNode.getExtendedType().orElse(null), aAbsDepth - 1),
				() -> getMappingForType(aNode.getSuperType().orElse(null), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForBlockStmt(BlockStmt aNode, int aAbsDepth) {
		// final NodeList<Statement> statements
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.BLOCK_STMT, aAbsDepth,
				() -> getMappingForStatementList(aNode.getStatements(), false, aAbsDepth - 1));
	}

	@Override
	public default String getMappingForContinueStmt(ContinueStmt aNode, int aAbsDepth) {
		// final SimpleName label
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.CONTINUE_STMT, aAbsDepth, () -> getMappingForSimpleName(
						aNode.getLabel().orElse(null), usesVariableNameAbstraction() ? 0 : aAbsDepth - 1));
	}

	@Override
	public default String getMappingForSynchronizedStmt(SynchronizedStmt aNode, int aAbsDepth) {
		// final Expression expression, final BlockStmt body
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.SYNCHRONIZED_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression(), aAbsDepth - 1),
				() -> getMappingForStatement(aNode.getBody(), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForCatchClause(CatchClause aNode, int aAbsDepth) {
		// final Parameter parameter, final BlockStmt body
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.CATCH_CLAUSE_STMT, aAbsDepth,
				() -> getMappingForParameter(aNode.getParameter(), aAbsDepth - 1),
				() -> getMappingForStatement(aNode.getBody(), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForCompilationUnit(CompilationUnit aNode, int aAbsDepth) {
		// PackageDeclaration packageDeclaration, NodeList<ImportDeclaration>
		// imports, NodeList<TypeDeclaration<?>> types, ModuleDeclaration module
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.COMPILATION_UNIT, aAbsDepth,
				() -> getMappingForNode(aNode.getPackageDeclaration().orElse(null), aAbsDepth - 1),
				() -> getMappingForNodeList(aNode.getImports(), true, aAbsDepth - 1),
				() -> getMappingForBodyDeclarationList(aNode.getTypes(), true, aAbsDepth - 1),
				() -> getMappingForNode(aNode.getModule().orElse(null), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForAnnotationDeclaration(AnnotationDeclaration aNode, int aAbsDepth) {
		// EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations,
		// SimpleName name, NodeList<BodyDeclaration<?>> members
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.ANNOTATION_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth - 1),
				() -> getMappingForSimpleName(aNode.getName(), usesAnnotationAbstraction() ? 0 : aAbsDepth - 1),
				() -> getMappingForBodyDeclarationList(aNode.getMembers(), false, aAbsDepth - 1));
	}

	@Override
	public default String getMappingForAnnotationMemberDeclaration(AnnotationMemberDeclaration aNode, int aAbsDepth) {
		// EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations,
		// Type type, SimpleName name, Expression defaultValue
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.ANNOTATION_MEMBER_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth - 1),
				() -> getMappingForType(aNode.getType(), aAbsDepth - 1),
				() -> getMappingForSimpleName(aNode.getName(), usesAnnotationAbstraction() ? 0 : aAbsDepth - 1),
				() -> getMappingForExpression(aNode.getDefaultValue().orElse(null), aAbsDepth - 1));
	}

	@Override
	public default String getMappingForJavadocComment(JavadocComment aNode, int aAbsDepth) {
		// String content
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.JAVADOC_COMMENT, aAbsDepth,
				() -> getMappingForString(aNode.getContent()));
	}

	@Override
	public default String getMappingForLineComment(LineComment aNode, int aAbsDepth) {
		// String content
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.LINE_COMMENT, aAbsDepth,
				() -> getMappingForString(aNode.getContent()));
	}

	@Override
	default String getMappingForName(Name aNode, int aAbsDepth) {
		// Name qualifier, final String identifier, NodeList<AnnotationExpr>
		// annotations
		return IAbstractionMapperBasics.applyCombination(aNode, getKeyWordProvider(), KeyWords.NAME, aAbsDepth,
				// get full qualifier if depth > 0
				() -> getMappingForName(aNode.getQualifier().orElse(null), aAbsDepth),
				() -> getMappingForString(aNode.getIdentifier()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth - 1));
	}

	@Override
	default String getMappingForSimpleName(SimpleName aNode, int aAbsDepth) {
		// final String identifier
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.SIMPLE_NAME, aAbsDepth,
				() -> getMappingForString(aNode.getIdentifier()));
	}

	@Override
	default String getMappingForModuleDeclaration(ModuleDeclaration aNode, int aAbsDepth) {
		// NodeList<AnnotationExpr> annotations, Name name, boolean isOpen,
		// NodeList<ModuleStmt> moduleStmts
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.MODULE_DECLARATION, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth - 1),
				() -> getMappingForName(aNode.getName(), usesClassNameAbstraction() ? 0 : aAbsDepth - 1),
				() -> getMappingForBoolean(aNode.isOpen()),
				() -> getMappingForNodeList(aNode.getModuleStmts(), false, aAbsDepth - 1));
	}

	@Override
	default String getMappingForModuleOpensStmt(ModuleOpensStmt aNode, int aAbsDepth) {
		// Name name, NodeList<Name> moduleNames
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.MODULE_OPENS_STMT, aAbsDepth,
				() -> getMappingForName(aNode.getName(), usesClassNameAbstraction() ? 0 : aAbsDepth - 1),
				() -> getMappingForNodeList(aNode.getModuleNames(), false, aAbsDepth - 1));
	}

	@Override
	default String getMappingForModuleExportsStmt(ModuleExportsStmt aNode, int aAbsDepth) {
		// Name name, NodeList<Name> moduleNames
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.MODULE_EXPORTS_STMT, aAbsDepth,
				() -> getMappingForName(aNode.getName(), usesClassNameAbstraction() ? 0 : aAbsDepth - 1),
				() -> getMappingForNodeList(aNode.getModuleNames(), false, aAbsDepth - 1));
	}

	@Override
	default String getMappingForModuleProvidesStmt(ModuleProvidesStmt aNode, int aAbsDepth) {
		// Type type, NodeList<Type> withTypes
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.MODULE_PROVIDES_STMT, aAbsDepth,
				() -> getMappingForType(aNode.getType(), usesClassNameAbstraction() ? 0 : aAbsDepth - 1),
				() -> getMappingForTypeList(aNode.getWithTypes(), false, aAbsDepth - 1));
	}

	@Override
	default String getMappingForModuleRequiresStmt(ModuleRequiresStmt aNode, int aAbsDepth) {
		// EnumSet<Modifier> modifiers, Name name
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.MODULE_REQUIRES_STMT, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForName(aNode.getName(), usesClassNameAbstraction() ? 0 : aAbsDepth - 1));
	}

	@Override
	default String getMappingForModuleUsesStmt(ModuleUsesStmt aNode, int aAbsDepth) {
		// Type type
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.MODULE_USES_STMT, aAbsDepth,
				() -> getMappingForType(aNode.getType(), usesClassNameAbstraction() ? 0 : aAbsDepth - 1));
	}

	@Override
	public default String getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode, int aAbsDepth) {
		// final String value
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.DOUBLE_LITERAL_EXPRESSION,
				usesNumberAbstraction() ? 0 : aAbsDepth, () -> aNode.getValue());
	}

	@Override
	public default String getMappingForLongLiteralExpr(LongLiteralExpr aNode, int aAbsDepth) {
		// final String value
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.LONG_LITERAL_EXPRESSION, usesNumberAbstraction() ? 0 : aAbsDepth,
				() -> aNode.getValue());
	}

	@Override
	public default String getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode, int aAbsDepth) {
		// final String value
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.INTEGER_LITERAL_EXPRESSION,
				usesNumberAbstraction() ? 0 : aAbsDepth, () -> aNode.getValue());
	}

	@Override
	public default String getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode, int aAbsDepth) {
		// boolean value
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.BOOLEAN_LITERAL_EXPRESSION, aAbsDepth,
				() -> getMappingForBoolean(aNode.getValue()));
	}

	// should not differentiate between different String values...
	@Override
	public default String getMappingForStringLiteralExpr(StringLiteralExpr aNode, int aAbsDepth) {
		// final String value
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.STRING_LITERAL_EXPRESSION,
				usesStringAbstraction() ? 0 : aAbsDepth, () -> getMappingForString(aNode.getValue()));
	}

	// char values may be important...
	@Override
	public default String getMappingForCharLiteralExpr(CharLiteralExpr aNode, int aAbsDepth) {
		// String value
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.CHAR_LITERAL_EXPRESSION, usesCharAbstraction() ? 0 : aAbsDepth,
				() -> getMappingForChar(aNode.getValue()));
	}

	// Here are some special cases that will always only consist of their
	// keyword but we need to overwrite the simple mapper anyway to get the
	// group
	// brackets

	@Override
	public default String getMappingForNullLiteralExpr(NullLiteralExpr aNode, int aAbsDepth) {
		return IAbstractionMapperBasics
				.applyCombination(aNode, getKeyWordProvider(), KeyWords.NULL_LITERAL_EXPRESSION, 0);
	}

	@Override
	public default String getMappingForVoidType(VoidType aNode, int aAbsDepth) {
		return IAbstractionMapperBasics.applyCombination(aNode, getKeyWordProvider(), KeyWords.TYPE_VOID, 0);
	}

	@Override
	public default String getMappingForUnknownType(UnknownType aNode, int aAbsDepth) {
		return IAbstractionMapperBasics.applyCombination(aNode, getKeyWordProvider(), KeyWords.TYPE_UNKNOWN, 0);
	}

	@Override
	default String getMappingForEmptyStmt(EmptyStmt aNode, int aAbsDepth) {
		return IAbstractionMapperBasics.applyCombination(aNode, getKeyWordProvider(), KeyWords.EMPTY_STMT, 0);
	}

	@Override
	default String getMappingForUnknownNode(Node aNode, int aAbsDepth) {
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.UNKNOWN, aAbsDepth,
				() -> getMappingForString(aNode.getClass().getCanonicalName()));
	}

}
