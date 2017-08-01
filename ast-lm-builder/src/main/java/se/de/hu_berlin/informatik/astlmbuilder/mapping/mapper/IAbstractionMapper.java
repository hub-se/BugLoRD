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

	public boolean usesBooleanAbstraction();

	public boolean usesNumberAbstraction();

	public boolean usesPrivateMethodAbstraction();

	public boolean usesMethodNameAbstraction();

	public boolean usesVariableNameAbstraction();

	public boolean usesGenericTypeNameAbstraction();

	public boolean usesClassNameAbstraction();

	public boolean usesPackageAndImportAbstraction();

	public boolean usesAnnotationAbstraction();

	public boolean usesCommentAbstraction();

	public static int minusTwoLevels(int absDepth) {
		if (absDepth < 0) {
			return noAbstraction();
		} else {
			return minusDepthOrZero(absDepth, 2);
		}
	}

	public static int minusOneLevel(int absDepth) {
		if (absDepth < 0) {
			return noAbstraction();
		} else {
			return minusDepthOrZero(absDepth, 1);
		}
	}

	public static int minusDepthOrZero(int absDepth, int minus) {
		return absDepth - minus < 0 ? 0 : absDepth - minus;
	}

	public static int noAbstraction() {
		return -1;
	}

	public static int depthZero() {
		return 0;
	}

	// all tokens (if not abstract) are stored with all respective constructor
	// arguments (@allFieldsConstructor)

	@Override
	public default String getMappingForMemberValuePair(MemberValuePair aNode, int aAbsDepth) {
		// final SimpleName name, final Expression value
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.MEMBER_VALUE_PAIR, aAbsDepth,
				() -> getMappingForSimpleName(
						aNode.getName(), usesVariableNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)),
				() -> getMappingForExpression(aNode.getValue(), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForSwitchEntryStmt(SwitchEntryStmt aNode, int aAbsDepth) {
		// final Expression label, final NodeList<Statement> statements
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.SWITCH_ENTRY_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getLabel().orElse(null), minusTwoLevels(aAbsDepth)),
				() -> getMappingForStatementList(aNode.getStatements(), false, minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForUnionType(UnionType aNode, int aAbsDepth) {
		// NodeList<ReferenceType> elements
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.TYPE_UNION, aAbsDepth,
				() -> getMappingForTypeList(aNode.getElements(), true, minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForIntersectionType(IntersectionType aNode, int aAbsDepth) {
		// NodeList<ReferenceType> elements
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.TYPE_INTERSECTION, aAbsDepth,
				() -> getMappingForTypeList(aNode.getElements(), true, minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForLambdaExpr(LambdaExpr aNode, int aAbsDepth) {
		// NodeList<Parameter> parameters, Statement body, boolean
		// isEnclosingParameters
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.LAMBDA_EXPRESSION, aAbsDepth,
				() -> getMappingForParameterList(aNode.getParameters(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForStatement(aNode.getBody(), minusOneLevel(aAbsDepth)),
				() -> getMappingForBoolean(aNode.isEnclosingParameters()));
	}

	@Override
	public default String getMappingForInstanceOfExpr(InstanceOfExpr aNode, int aAbsDepth) {
		// final Expression expression, final ReferenceType<?> type
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.INSTANCEOF_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression(), minusTwoLevels(aAbsDepth)),
				() -> getMappingForType(aNode.getType(), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForConditionalExpr(ConditionalExpr aNode, int aAbsDepth) {
		// Expression condition, Expression thenExpr, Expression elseExpr
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.CONDITIONAL_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getCondition(), minusTwoLevels(aAbsDepth)),
				() -> getMappingForExpression(aNode.getThenExpr(), minusOneLevel(aAbsDepth)),
				() -> getMappingForExpression(aNode.getElseExpr(), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForObjectCreationExpr(ObjectCreationExpr aNode, int aAbsDepth) {
		// final Expression scope, final ClassOrInterfaceType type, final
		// NodeList<Type> typeArguments,
		// final NodeList<Expression> arguments, final
		// NodeList<BodyDeclaration<?>> anonymousClassBody
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.OBJ_CREATE_EXPRESSION, aAbsDepth,
				// get full scope if depth > 0
				() -> getMappingForExpression(aNode.getScope().orElse(null), noAbstraction()),
				() -> getMappingForClassOrInterfaceType(aNode.getType(), minusOneLevel(aAbsDepth)),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForExpressionList(aNode.getArguments(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForBodyDeclarationList(
						aNode.getAnonymousClassBody().orElse(null), false, minusTwoLevels(aAbsDepth)));
	}

	@Override
	public default String getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode, int aAbsDepth) {
		// final ClassOrInterfaceType scope, final SimpleName name, final
		// NodeList<Type> typeArguments
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.CLASS_OR_INTERFACE_TYPE, aAbsDepth,
				// get full scope if depth > 0
				() -> getMappingForType(aNode.getScope().orElse(null), noAbstraction()),
				() -> getMappingForSimpleName(
						aNode.getName(), usesClassNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForEnclosedExpr(EnclosedExpr aNode, int aAbsDepth) {
		// final Expression inner
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.ENCLOSED_EXPRESSION, aAbsDepth,
				// skip parentheses
				() -> getMappingForExpression(aNode.getInner().orElse(null), aAbsDepth));
	}

	@Override
	public default String getMappingForArrayInitializerExpr(ArrayInitializerExpr aNode, int aAbsDepth) {
		// NodeList<Expression> values
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.ARRAY_INIT_EXPRESSION, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getValues(), true, minusTwoLevels(aAbsDepth)));
	}

	@Override
	public default String getMappingForArrayCreationExpr(ArrayCreationExpr aNode, int aAbsDepth) {
		// Type elementType, NodeList<ArrayCreationLevel> levels,
		// ArrayInitializerExpr initializer
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.ARRAY_CREATE_EXPRESSION, aAbsDepth,
				() -> getMappingForType(aNode.getElementType(), minusOneLevel(aAbsDepth)),
				() -> getMappingForArrayCreationLevelList(aNode.getLevels(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForExpression(aNode.getInitializer().orElse(null), minusTwoLevels(aAbsDepth)));
	}

	@Override
	public default String getMappingForArrayAccessExpr(ArrayAccessExpr aNode, int aAbsDepth) {
		// Expression name, Expression index
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.ARRAY_ACCESS_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getName(), minusTwoLevels(aAbsDepth)),
				() -> getMappingForExpression(aNode.getIndex(), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForTypeParameter(TypeParameter aNode, int aAbsDepth) {
		// SimpleName name, NodeList<ClassOrInterfaceType> typeBound,
		// NodeList<AnnotationExpr> annotations
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.TYPE_PAR, aAbsDepth,
				() -> getMappingForSimpleName(
						aNode.getName(), usesGenericTypeNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)),
				() -> getMappingForClassOrInterfaceTypeList(aNode.getTypeBound(), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)));
	}

	@Override
	public default String getMappingForVariableDeclarator(VariableDeclarator aNode, int aAbsDepth) {
		// Type type, SimpleName name, Expression initializer
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.VARIABLE_DECLARATOR, aAbsDepth,
				() -> getMappingForType(aNode.getType(), minusOneLevel(aAbsDepth)),
				() -> getMappingForSimpleName(
						aNode.getName(), usesVariableNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)),
				() -> getMappingForExpression(aNode.getInitializer().orElse(null), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForImportDeclaration(ImportDeclaration aNode, int aAbsDepth) {
		// Name name, boolean isStatic, boolean isAsterisk
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.IMPORT_DECLARATION, aAbsDepth,
				() -> getMappingForName(
						aNode.getName(), usesPackageAndImportAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)),
				() -> getMappingForBoolean(aNode.isStatic()), () -> getMappingForBoolean(aNode.isAsterisk()));
	}

	@Override
	public default String getMappingForPackageDeclaration(PackageDeclaration aNode, int aAbsDepth) {
		// NodeList<AnnotationExpr> annotations, Name name
		return IAbstractionMapperBasics
				.applyCombination(
						aNode, getKeyWordProvider(), KeyWords.PACKAGE_DECLARATION, aAbsDepth,
						() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
						() -> getMappingForName(
								aNode.getName(),
								usesPackageAndImportAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)));
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
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForType(aNode.getType(), minusOneLevel(aAbsDepth)),
				() -> getMappingForBoolean(aNode.isVarArgs()),
				() -> getMappingForExpressionList(aNode.getVarArgsAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForSimpleName(
						aNode.getName(), usesVariableNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)));
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
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForSimpleName(
						aNode.getName(), usesVariableNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)),
				() -> getMappingForClassOrInterfaceTypeList(
						aNode.getImplementedTypes(), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForBodyDeclarationList(aNode.getEntries(), false, minusTwoLevels(aAbsDepth)),
				() -> getMappingForBodyDeclarationList(aNode.getMembers(), false, minusTwoLevels(aAbsDepth)));
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
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForBoolean(aNode.isInterface()),
				() -> getMappingForSimpleName(
						aNode.getName(), usesClassNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)),
				() -> getMappingsForTypeParameterList(aNode.getTypeParameters(), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForClassOrInterfaceTypeList(aNode.getExtendedTypes(), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForClassOrInterfaceTypeList(
						aNode.getImplementedTypes(), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForBodyDeclarationList(aNode.getMembers(), false, minusTwoLevels(aAbsDepth)));
	}

	@Override
	public default String getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode, int aAbsDepth) {
		// NodeList<AnnotationExpr> annotations, SimpleName name,
		// NodeList<Expression> arguments, NodeList<BodyDeclaration<?>>
		// classBody
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.ENUM_CONSTANT_DECLARATION, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForSimpleName(
						aNode.getName(), usesVariableNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)),
				() -> getMappingForExpressionList(aNode.getArguments(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForBodyDeclarationList(aNode.getClassBody(), false, minusTwoLevels(aAbsDepth)));
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
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingsForTypeParameterList(aNode.getTypeParameters(), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForType(aNode.getType(), minusOneLevel(aAbsDepth)),
				() -> getMappingForSimpleName(
						aNode.getName(), usesMethodNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)),
				() -> getMappingForBoolean(aNode.isDefault()),
				() -> getMappingForParameterList(aNode.getParameters(), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForTypeList(aNode.getThrownExceptions(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForStatement(aNode.getBody().orElse(null), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForFieldDeclaration(FieldDeclaration aNode, int aAbsDepth) {
		// EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations,
		// NodeList<VariableDeclarator> variables
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.FIELD_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForVariableDeclaratorList(aNode.getVariables(), true, minusTwoLevels(aAbsDepth)));
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
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingsForTypeParameterList(aNode.getTypeParameters(), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForSimpleName(
						aNode.getName(), usesClassNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)),
				() -> getMappingForParameterList(aNode.getParameters(), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForTypeList(aNode.getThrownExceptions(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForStatement(aNode.getBody(), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForWhileStmt(WhileStmt aNode, int aAbsDepth) {
		// final Expression condition, final Statement body
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.WHILE_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getCondition(), minusTwoLevels(aAbsDepth)),
				() -> getMappingForStatement(aNode.getBody(), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForSwitchStmt(SwitchStmt aNode, int aAbsDepth) {
		// final Expression selector, final NodeList<SwitchEntryStmt> entries
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.SWITCH_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getSelector(), minusTwoLevels(aAbsDepth)),
				() -> getMappingForStatementList(aNode.getEntries(), false, minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForForStmt(ForStmt aNode, int aAbsDepth) {
		// final NodeList<Expression> initialization, final Expression compare,
		// final NodeList<Expression> update, final Statement body
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.FOR_STMT, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getInitialization(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForExpression(aNode.getCompare().orElse(null), minusTwoLevels(aAbsDepth)),
				() -> getMappingForExpressionList(aNode.getUpdate(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForStatement(aNode.getBody(), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForForeachStmt(ForeachStmt aNode, int aAbsDepth) {
		// final VariableDeclarationExpr variable, final Expression iterable,
		// final Statement body
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.FOR_EACH_STMT, aAbsDepth,
				() -> getMappingForVariableDeclarationExpr(aNode.getVariable(), minusTwoLevels(aAbsDepth)),
				() -> getMappingForExpression(aNode.getIterable(), minusTwoLevels(aAbsDepth)),
				() -> getMappingForStatement(aNode.getBody(), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForExplicitConstructorInvocationStmt(ExplicitConstructorInvocationStmt aNode,
			int aAbsDepth) {
		// final NodeList<Type> typeArguments, final boolean isThis, final
		// Expression expression, final NodeList<Expression> arguments
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.EXPL_CONSTR_INVOC_STMT, aAbsDepth,
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForBoolean(aNode.isThis()),
				() -> getMappingForExpression(aNode.getExpression().orElse(null), minusOneLevel(aAbsDepth)),
				() -> getMappingForExpressionList(aNode.getArguments(), true, minusTwoLevels(aAbsDepth)));
	}

	@Override
	public default String getMappingForDoStmt(DoStmt aNode, int aAbsDepth) {
		// final Statement body, final Expression condition
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.DO_STMT, aAbsDepth,
				() -> getMappingForStatement(aNode.getBody(), minusOneLevel(aAbsDepth)),
				() -> getMappingForExpression(aNode.getCondition(), minusTwoLevels(aAbsDepth)));
	}

	@Override
	public default String getMappingForAssertStmt(AssertStmt aNode, int aAbsDepth) {
		// final Expression check, final Expression message
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.ASSERT_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getCheck(), minusOneLevel(aAbsDepth)),
				() -> getMappingForExpression(aNode.getMessage().orElse(null), minusTwoLevels(aAbsDepth)));
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
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForVariableDeclaratorList(aNode.getVariables(), true, minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForMethodReferenceExpr(MethodReferenceExpr aNode, int aAbsDepth) {
		// Expression scope, NodeList<Type> typeArguments, String identifier
		boolean isPrivate = aNode == null ? false : getPrivateMethodBlackList().contains(aNode.getIdentifier());
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.METHOD_REFERENCE_EXPRESSION, aAbsDepth,
				// full scope
				() -> getMappingForExpression(aNode.getScope(), noAbstraction()),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, minusOneLevel(aAbsDepth)),
				() -> isPrivate || usesMethodNameAbstraction() ? String.valueOf(IBasicKeyWords.KEYWORD_ABSTRACT)
						: getMappingForString(aNode.getIdentifier()));
	}

	@Override
	public default String getMappingForMethodCallExpr(MethodCallExpr aNode, int aAbsDepth) {
		// final Expression scope, final NodeList<Type> typeArguments, final
		// SimpleName name, final NodeList<Expression> arguments
		boolean isPrivate = aNode == null ? false
				: getPrivateMethodBlackList().contains(aNode.getName().getIdentifier());
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.METHOD_CALL_EXPRESSION, aAbsDepth,
				// full scope if not private
				() -> getMappingForExpression(aNode.getScope().orElse(null), isPrivate ? depthZero() : noAbstraction()),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForSimpleName(
						aNode.getName(),
						isPrivate || usesMethodNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)),
				() -> getMappingForExpressionList(aNode.getArguments(), true, minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForFieldAccessExpr(FieldAccessExpr aNode, int aAbsDepth) {
		// final Expression scope, final NodeList<Type> typeArguments, final
		// SimpleName name
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.FIELD_ACCESS_EXPRESSION, aAbsDepth,
				// full scope
				() -> getMappingForExpression(aNode.getScope().orElse(null), noAbstraction()),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForSimpleName(
						aNode.getName(), usesVariableNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)));
	}

	@Override
	public default String getMappingForTypeExpr(TypeExpr aNode, int aAbsDepth) {
		// Type type
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.TYPE_EXPRESSION, aAbsDepth,
				() -> getMappingForType(aNode.getType(), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForClassExpr(ClassExpr aNode, int aAbsDepth) {
		// Type type
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.CLASS_EXPRESSION, aAbsDepth,
				() -> getMappingForType(aNode.getType(), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForCastExpr(CastExpr aNode, int aAbsDepth) {
		// Type type, Expression expression
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.CAST_EXPRESSION, aAbsDepth,
				() -> getMappingForType(aNode.getType(), minusTwoLevels(aAbsDepth)),
				() -> getMappingForExpression(aNode.getExpression(), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForUnaryExpr(UnaryExpr aNode, int aAbsDepth) {
		// final Expression expression, final Operator operator
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.UNARY_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression(), minusOneLevel(aAbsDepth)),
				() -> getMappingForUnaryOperator(aNode.getOperator()));
	}

	@Override
	public default String getMappingForBinaryExpr(BinaryExpr aNode, int aAbsDepth) {
		// Expression left, Expression right, Operator operator
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.BINARY_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getLeft(), minusOneLevel(aAbsDepth)),
				() -> getMappingForExpression(aNode.getRight(), minusOneLevel(aAbsDepth)),
				() -> getMappingForBinaryOperator(aNode.getOperator()));
	}

	@Override
	public default String getMappingForAssignExpr(AssignExpr aNode, int aAbsDepth) {
		// Expression target, Expression value, Operator operator
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.ASSIGN_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getTarget(), minusTwoLevels(aAbsDepth)),
				() -> getMappingForExpression(aNode.getValue(), minusOneLevel(aAbsDepth)),
				() -> getMappingForAssignOperator(aNode.getOperator()));
	}

	@Override
	public default String getMappingForIfStmt(IfStmt aNode, int aAbsDepth) {
		// final Expression condition, final Statement thenStmt, final Statement
		// elseStmt
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.IF_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getCondition(), minusTwoLevels(aAbsDepth)),
				() -> getMappingForStatement(aNode.getThenStmt(), minusOneLevel(aAbsDepth)),
				() -> getMappingForStatement(aNode.getElseStmt().orElse(null), minusOneLevel(aAbsDepth)));
	}

	@Override
	default String getMappingForLocalClassDeclarationStmt(LocalClassDeclarationStmt aNode, int aAbsDepth) {
		// final ClassOrInterfaceDeclaration classDeclaration
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.LOCAL_CLASS_DECLARATION_STMT, aAbsDepth,
				() -> getMappingForClassOrInterfaceDeclaration(aNode.getClassDeclaration(), minusOneLevel(aAbsDepth)));
	}

	@Override
	default String getMappingForArrayType(ArrayType aNode, int aAbsDepth) {
		// Type componentType, NodeList<AnnotationExpr> annotations
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.ARRAY_TYPE, aAbsDepth,
				() -> getMappingForType(aNode.getComponentType(), minusOneLevel(aAbsDepth)),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)));
	}

	@Override
	default String getMappingForArrayCreationLevel(ArrayCreationLevel aNode, int aAbsDepth) {
		// Expression dimension, NodeList<AnnotationExpr> annotations
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.ARRAY_CREATION_LEVEL, aAbsDepth,
				() -> getMappingForExpression(aNode.getDimension().orElse(null), minusOneLevel(aAbsDepth)),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)));
	}

	@Override
	public default String getMappingForInitializerDeclaration(InitializerDeclaration aNode, int aAbsDepth) {
		// boolean isStatic, BlockStmt body
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.INITIALIZER_DECLARATION, aAbsDepth,
				() -> getMappingForBoolean(aNode.isStatic()),
				() -> getMappingForStatement(aNode.getBody(), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForThrowStmt(ThrowStmt aNode, int aAbsDepth) {
		// final Expression expression
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.THROW_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression(), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForNameExpr(NameExpr aNode, int aAbsDepth) {
		// final SimpleName name
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.NAME_EXPRESSION, aAbsDepth, () -> getMappingForSimpleName(
						aNode.getName(), usesVariableNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)));
	}

	@Override
	public default String getMappingForTryStmt(TryStmt aNode, int aAbsDepth) {
		// NodeList<VariableDeclarationExpr> resources, final BlockStmt
		// tryBlock, final NodeList<CatchClause> catchClauses, final BlockStmt
		// finallyBlock
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.TRY_STMT, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getResources(), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForStatement(aNode.getTryBlock().orElse(null), minusOneLevel(aAbsDepth)),
				() -> getMappingForNodeList(aNode.getCatchClauses(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForStatement(aNode.getFinallyBlock().orElse(null), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForThisExpr(ThisExpr aNode, int aAbsDepth) {
		// final Expression classExpr
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.THIS_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getClassExpr().orElse(null), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForExpressionStmt(ExpressionStmt aNode, int aAbsDepth) {
		// final Expression expression
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.EXPRESSION_STMT, aAbsDepth,
				// skip the wrapper
				() -> getMappingForExpression(aNode.getExpression(), aAbsDepth));
	}

	@Override
	public default String getMappingForSuperExpr(SuperExpr aNode, int aAbsDepth) {
		// final Expression classExpr
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.SUPER_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getClassExpr().orElse(null), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForReturnStmt(ReturnStmt aNode, int aAbsDepth) {
		// final Expression expression
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.RETURN_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression().orElse(null), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForLabeledStmt(LabeledStmt aNode, int aAbsDepth) {
		// final SimpleName label, final Statement statement
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.LABELED_STMT, aAbsDepth,
				() -> getMappingForSimpleName(
						aNode.getLabel(), usesVariableNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)),
				() -> getMappingForStatement(aNode.getStatement(), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForBreakStmt(BreakStmt aNode, int aAbsDepth) {
		// final SimpleName label
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.BREAK, aAbsDepth,
				() -> getMappingForSimpleName(
						aNode.getLabel().orElse(null),
						usesVariableNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)));
	}

	@Override
	public default String getMappingForSingleMemberAnnotationExpr(SingleMemberAnnotationExpr aNode, int aAbsDepth) {
		// final Name name, final Expression memberValue
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.SINGLE_MEMBER_ANNOTATION_EXPRESSION, aAbsDepth,
				() -> getMappingForName(
						aNode.getName(), usesAnnotationAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)),
				() -> getMappingForExpression(aNode.getMemberValue(), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForNormalAnnotationExpr(NormalAnnotationExpr aNode, int aAbsDepth) {
		// final Name name, final NodeList<MemberValuePair> pairs
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.NORMAL_ANNOTATION_EXPRESSION, aAbsDepth,
				() -> getMappingForName(
						aNode.getName(), usesAnnotationAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)),
				() -> getMappingForNodeList(aNode.getPairs(), true, minusTwoLevels(aAbsDepth)));
	}

	@Override
	public default String getMappingForMarkerAnnotationExpr(MarkerAnnotationExpr aNode, int aAbsDepth) {
		// final Name name
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.MARKER_ANNOTATION_EXPRESSION, aAbsDepth, () -> getMappingForName(
						aNode.getName(), usesAnnotationAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)));
	}

	@Override
	public default String getMappingForWildcardType(WildcardType aNode, int aAbsDepth) {
		// final ReferenceType extendedType, final ReferenceType superType
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.TYPE_WILDCARD, aAbsDepth,
				() -> getMappingForType(aNode.getExtendedType().orElse(null), minusOneLevel(aAbsDepth)),
				() -> getMappingForType(aNode.getSuperType().orElse(null), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForBlockStmt(BlockStmt aNode, int aAbsDepth) {
		// final NodeList<Statement> statements
		return IAbstractionMapperBasics.applyCombination(aNode, getKeyWordProvider(), KeyWords.BLOCK_STMT, aAbsDepth,
				// skip parentheses
				() -> getMappingForStatementList(aNode.getStatements(), false, aAbsDepth));
	}

	@Override
	public default String getMappingForContinueStmt(ContinueStmt aNode, int aAbsDepth) {
		// final SimpleName label
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.CONTINUE_STMT, aAbsDepth,
				() -> getMappingForSimpleName(
						aNode.getLabel().orElse(null),
						usesVariableNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)));
	}

	@Override
	public default String getMappingForSynchronizedStmt(SynchronizedStmt aNode, int aAbsDepth) {
		// final Expression expression, final BlockStmt body
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.SYNCHRONIZED_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression(), minusTwoLevels(aAbsDepth)),
				() -> getMappingForStatement(aNode.getBody(), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForCatchClause(CatchClause aNode, int aAbsDepth) {
		// final Parameter parameter, final BlockStmt body
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.CATCH_CLAUSE_STMT, aAbsDepth,
				() -> getMappingForParameter(aNode.getParameter(), minusTwoLevels(aAbsDepth)),
				() -> getMappingForStatement(aNode.getBody(), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForCompilationUnit(CompilationUnit aNode, int aAbsDepth) {
		// PackageDeclaration packageDeclaration, NodeList<ImportDeclaration>
		// imports, NodeList<TypeDeclaration<?>> types, ModuleDeclaration module
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.COMPILATION_UNIT, aAbsDepth,
				() -> getMappingForNode(aNode.getPackageDeclaration().orElse(null), minusTwoLevels(aAbsDepth)),
				() -> getMappingForNodeList(aNode.getImports(), false, minusTwoLevels(aAbsDepth)),
				() -> getMappingForBodyDeclarationList(aNode.getTypes(), false, minusOneLevel(aAbsDepth)),
				() -> getMappingForNode(aNode.getModule().orElse(null), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForAnnotationDeclaration(AnnotationDeclaration aNode, int aAbsDepth) {
		// EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations,
		// SimpleName name, NodeList<BodyDeclaration<?>> members
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.ANNOTATION_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForSimpleName(
						aNode.getName(), usesAnnotationAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)),
				() -> getMappingForBodyDeclarationList(aNode.getMembers(), false, minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForAnnotationMemberDeclaration(AnnotationMemberDeclaration aNode, int aAbsDepth) {
		// EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations,
		// Type type, SimpleName name, Expression defaultValue
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.ANNOTATION_MEMBER_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForType(aNode.getType(), minusOneLevel(aAbsDepth)),
				() -> getMappingForSimpleName(
						aNode.getName(), usesAnnotationAbstraction() ? depthZero() : minusOneLevel(aAbsDepth)),
				() -> getMappingForExpression(aNode.getDefaultValue().orElse(null), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForBlockComment(BlockComment aNode, int aAbsDepth) {
		// String content
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.BLOCK_COMMENT, aAbsDepth, () -> usesCommentAbstraction()
						? String.valueOf(IBasicKeyWords.KEYWORD_ABSTRACT) : getMappingForString(aNode.getContent()));
	}

	@Override
	public default String getMappingForJavadocComment(JavadocComment aNode, int aAbsDepth) {
		// String content
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.JAVADOC_COMMENT, aAbsDepth, () -> usesCommentAbstraction()
						? String.valueOf(IBasicKeyWords.KEYWORD_ABSTRACT) : getMappingForString(aNode.getContent()));
	}

	@Override
	public default String getMappingForLineComment(LineComment aNode, int aAbsDepth) {
		// String content
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.LINE_COMMENT, aAbsDepth, () -> usesCommentAbstraction()
						? String.valueOf(IBasicKeyWords.KEYWORD_ABSTRACT) : getMappingForString(aNode.getContent()));
	}

	@Override
	default String getMappingForName(Name aNode, int aAbsDepth) {
		// Name qualifier, final String identifier, NodeList<AnnotationExpr>
		// annotations
		return IAbstractionMapperBasics.applyCombination(aNode, getKeyWordProvider(), KeyWords.NAME, aAbsDepth,
				// get full qualifier if depth > 0
				() -> getMappingForName(aNode.getQualifier().orElse(null), noAbstraction()),
				() -> getMappingForString(aNode.getIdentifier()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)));
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
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForName(
						aNode.getName(), usesClassNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)),
				() -> getMappingForBoolean(aNode.isOpen()),
				() -> getMappingForNodeList(aNode.getModuleStmts(), false, minusOneLevel(aAbsDepth)));
	}

	@Override
	default String getMappingForModuleOpensStmt(ModuleOpensStmt aNode, int aAbsDepth) {
		// Name name, NodeList<Name> moduleNames
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.MODULE_OPENS_STMT, aAbsDepth,
				() -> getMappingForName(
						aNode.getName(), usesClassNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)),
				() -> getMappingForNodeList(aNode.getModuleNames(), false, minusTwoLevels(aAbsDepth)));
	}

	@Override
	default String getMappingForModuleExportsStmt(ModuleExportsStmt aNode, int aAbsDepth) {
		// Name name, NodeList<Name> moduleNames
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.MODULE_EXPORTS_STMT, aAbsDepth,
				() -> getMappingForName(
						aNode.getName(), usesClassNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)),
				() -> getMappingForNodeList(aNode.getModuleNames(), false, minusTwoLevels(aAbsDepth)));
	}

	@Override
	default String getMappingForModuleProvidesStmt(ModuleProvidesStmt aNode, int aAbsDepth) {
		// Type type, NodeList<Type> withTypes
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.MODULE_PROVIDES_STMT, aAbsDepth,
				() -> getMappingForType(aNode.getType(), minusOneLevel(aAbsDepth)),
				() -> getMappingForTypeList(aNode.getWithTypes(), false, minusOneLevel(aAbsDepth)));
	}

	@Override
	default String getMappingForModuleRequiresStmt(ModuleRequiresStmt aNode, int aAbsDepth) {
		// EnumSet<Modifier> modifiers, Name name
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.MODULE_REQUIRES_STMT, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()), () -> getMappingForName(
						aNode.getName(), usesClassNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth)));
	}

	@Override
	default String getMappingForModuleUsesStmt(ModuleUsesStmt aNode, int aAbsDepth) {
		// Type type
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.MODULE_USES_STMT, aAbsDepth,
				() -> getMappingForType(aNode.getType(), minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode) {
		// final String value
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.DOUBLE_LITERAL_EXPRESSION,
				usesNumberAbstraction() ? depthZero() : noAbstraction(), () -> aNode.getValue());
	}

	@Override
	public default String getMappingForLongLiteralExpr(LongLiteralExpr aNode) {
		// final String value
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.LONG_LITERAL_EXPRESSION,
				usesNumberAbstraction() ? depthZero() : noAbstraction(), () -> aNode.getValue());
	}

	@Override
	public default String getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode) {
		// final String value
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.INTEGER_LITERAL_EXPRESSION,
				usesNumberAbstraction() ? depthZero() : noAbstraction(), () -> aNode.getValue());
	}

	@Override
	public default String getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode) {
		// boolean value
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.BOOLEAN_LITERAL_EXPRESSION,
				usesBooleanAbstraction() ? depthZero() : noAbstraction(), () -> getMappingForBoolean(aNode.getValue()));
	}

	// should not differentiate between different String values...
	@Override
	public default String getMappingForStringLiteralExpr(StringLiteralExpr aNode) {
		// final String value
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.STRING_LITERAL_EXPRESSION,
				usesStringAbstraction() ? depthZero() : noAbstraction(), () -> getMappingForString(aNode.getValue()));
	}

	// char values may be important...
	@Override
	public default String getMappingForCharLiteralExpr(CharLiteralExpr aNode) {
		// String value
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.CHAR_LITERAL_EXPRESSION,
				usesCharAbstraction() ? depthZero() : noAbstraction(), () -> getMappingForChar(aNode.getValue()));
	}

	// Here are some special cases that will always only consist of their
	// keyword but we need to overwrite the simple mapper anyway to get the
	// group brackets

	@Override
	public default String getMappingForNullLiteralExpr(NullLiteralExpr aNode) {
		return IAbstractionMapperBasics
				.applyCombination(aNode, getKeyWordProvider(), KeyWords.NULL_LITERAL_EXPRESSION, depthZero());
	}

	@Override
	public default String getMappingForVoidType(VoidType aNode) {
		return IAbstractionMapperBasics.applyCombination(aNode, getKeyWordProvider(), KeyWords.TYPE_VOID, depthZero());
	}

	@Override
	public default String getMappingForUnknownType(UnknownType aNode) {
		return IAbstractionMapperBasics
				.applyCombination(aNode, getKeyWordProvider(), KeyWords.TYPE_UNKNOWN, depthZero());
	}

	@Override
	default String getMappingForEmptyStmt(EmptyStmt aNode) {
		return IAbstractionMapperBasics.applyCombination(aNode, getKeyWordProvider(), KeyWords.EMPTY_STMT, depthZero());
	}

	@Override
	default String getMappingForUnknownNode(Node aNode, int aAbsDepth) {
		return IAbstractionMapperBasics.applyCombination(
				aNode, getKeyWordProvider(), KeyWords.UNKNOWN, depthZero(),
				() -> getMappingForString(aNode.getClass().getCanonicalName()));
	}

}
