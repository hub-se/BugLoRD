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
	public default String getMappingForMemberValuePair(MemberValuePair aNode, int aAbsDepth, boolean includeParent) {
		// final SimpleName name, final Expression value
		return applyCombination(
				aNode, includeParent,KeyWords.MEMBER_VALUE_PAIR, aAbsDepth,
				() -> getMappingForSimpleName(
						aNode.getName(), usesVariableNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false),
				() -> getMappingForExpression(aNode.getValue(), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForSwitchEntryStmt(SwitchEntryStmt aNode, int aAbsDepth, boolean includeParent) {
		// final Expression label, final NodeList<Statement> statements
		return applyCombination(
				aNode, includeParent,KeyWords.SWITCH_ENTRY_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getLabel().orElse(null), minusTwoLevels(aAbsDepth), false),
				() -> getMappingForStatementList(aNode.getStatements(), false, minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForUnionType(UnionType aNode, int aAbsDepth, boolean includeParent) {
		// NodeList<ReferenceType> elements
		return applyCombination(
				aNode, includeParent,KeyWords.TYPE_UNION, aAbsDepth,
				() -> getMappingForTypeList(aNode.getElements(), true, minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForIntersectionType(IntersectionType aNode, int aAbsDepth, boolean includeParent) {
		// NodeList<ReferenceType> elements
		return applyCombination(
				aNode, includeParent,KeyWords.TYPE_INTERSECTION, aAbsDepth,
				() -> getMappingForTypeList(aNode.getElements(), true, minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForLambdaExpr(LambdaExpr aNode, int aAbsDepth, boolean includeParent) {
		// NodeList<Parameter> parameters, Statement body, boolean
		// isEnclosingParameters
		return applyCombination(
				aNode, includeParent,KeyWords.LAMBDA_EXPRESSION, aAbsDepth,
				() -> getMappingForParameterList(aNode.getParameters(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForStatement(aNode.getBody(), minusOneLevel(aAbsDepth), false),
				() -> getMappingForBoolean(aNode.isEnclosingParameters()));
	}

	@Override
	public default String getMappingForInstanceOfExpr(InstanceOfExpr aNode, int aAbsDepth, boolean includeParent) {
		// final Expression expression, final ReferenceType<?> type
		return applyCombination(
				aNode, includeParent,KeyWords.INSTANCEOF_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression(), minusTwoLevels(aAbsDepth), false),
				() -> getMappingForType(aNode.getType(), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForConditionalExpr(ConditionalExpr aNode, int aAbsDepth, boolean includeParent) {
		// Expression condition, Expression thenExpr, Expression elseExpr
		return applyCombination(
				aNode, includeParent,KeyWords.CONDITIONAL_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getCondition(), minusTwoLevels(aAbsDepth), false),
				() -> getMappingForExpression(aNode.getThenExpr(), minusOneLevel(aAbsDepth), false),
				() -> getMappingForExpression(aNode.getElseExpr(), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForObjectCreationExpr(ObjectCreationExpr aNode, int aAbsDepth, boolean includeParent) {
		// final Expression scope, final ClassOrInterfaceType type, final
		// NodeList<Type> typeArguments,
		// final NodeList<Expression> arguments, final
		// NodeList<BodyDeclaration<?>> anonymousClassBody
		return applyCombination(
				aNode, includeParent,KeyWords.OBJ_CREATE_EXPRESSION, aAbsDepth,
				// get full scope if depth > 0
				() -> getMappingForExpression(aNode.getScope().orElse(null), noAbstraction(), false),
				() -> getMappingForClassOrInterfaceType(aNode.getType(), minusOneLevel(aAbsDepth), false),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForExpressionList(aNode.getArguments(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForBodyDeclarationList(
						aNode.getAnonymousClassBody().orElse(null), false, minusTwoLevels(aAbsDepth)));
	}

	@Override
	public default String getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode, int aAbsDepth, boolean includeParent) {
		// final ClassOrInterfaceType scope, final SimpleName name, final
		// NodeList<Type> typeArguments
		return applyCombination(
				aNode, includeParent,KeyWords.CLASS_OR_INTERFACE_TYPE, aAbsDepth,
				// get full scope if depth > 0
				() -> getMappingForType(aNode.getScope().orElse(null), noAbstraction(), false),
				() -> getMappingForSimpleName(
						aNode.getName(), usesClassNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForEnclosedExpr(EnclosedExpr aNode, int aAbsDepth, boolean includeParent) {
		// final Expression inner
		return applyCombination(
				aNode, includeParent,KeyWords.ENCLOSED_EXPRESSION, aAbsDepth,
				// skip parentheses
				() -> getMappingForExpression(aNode.getInner().orElse(null), aAbsDepth, false));
	}

	@Override
	public default String getMappingForArrayInitializerExpr(ArrayInitializerExpr aNode, int aAbsDepth, boolean includeParent) {
		// NodeList<Expression> values
		return applyCombination(
				aNode, includeParent,KeyWords.ARRAY_INIT_EXPRESSION, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getValues(), true, minusTwoLevels(aAbsDepth)));
	}

	@Override
	public default String getMappingForArrayCreationExpr(ArrayCreationExpr aNode, int aAbsDepth, boolean includeParent) {
		// Type elementType, NodeList<ArrayCreationLevel> levels,
		// ArrayInitializerExpr initializer
		return applyCombination(
				aNode, includeParent,KeyWords.ARRAY_CREATE_EXPRESSION, aAbsDepth,
				() -> getMappingForType(aNode.getElementType(), minusOneLevel(aAbsDepth), false),
				() -> getMappingForArrayCreationLevelList(aNode.getLevels(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForExpression(aNode.getInitializer().orElse(null), minusTwoLevels(aAbsDepth), false));
	}

	@Override
	public default String getMappingForArrayAccessExpr(ArrayAccessExpr aNode, int aAbsDepth, boolean includeParent) {
		// Expression name, Expression index
		return applyCombination(
				aNode, includeParent,KeyWords.ARRAY_ACCESS_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getName(), minusTwoLevels(aAbsDepth), false),
				() -> getMappingForExpression(aNode.getIndex(), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForTypeParameter(TypeParameter aNode, int aAbsDepth, boolean includeParent) {
		// SimpleName name, NodeList<ClassOrInterfaceType> typeBound,
		// NodeList<AnnotationExpr> annotations
		return applyCombination(
				aNode, includeParent,KeyWords.TYPE_PAR, aAbsDepth,
				() -> getMappingForSimpleName(
						aNode.getName(), usesGenericTypeNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false),
				() -> getMappingForClassOrInterfaceTypeList(aNode.getTypeBound(), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)));
	}

	@Override
	public default String getMappingForVariableDeclarator(VariableDeclarator aNode, int aAbsDepth, boolean includeParent) {
		// Type type, SimpleName name, Expression initializer
		return applyCombination(
				aNode, includeParent,KeyWords.VARIABLE_DECLARATOR, aAbsDepth,
				() -> getMappingForType(aNode.getType(), minusOneLevel(aAbsDepth), false),
				() -> getMappingForSimpleName(
						aNode.getName(), usesVariableNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false),
				() -> getMappingForExpression(aNode.getInitializer().orElse(null), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForImportDeclaration(ImportDeclaration aNode, int aAbsDepth, boolean includeParent) {
		// Name name, boolean isStatic, boolean isAsterisk
		return applyCombination(
				aNode, includeParent,KeyWords.IMPORT_DECLARATION, aAbsDepth,
				() -> getMappingForName(
						aNode.getName(), usesPackageAndImportAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false),
				() -> getMappingForBoolean(aNode.isStatic()), () -> getMappingForBoolean(aNode.isAsterisk()));
	}

	@Override
	public default String getMappingForPackageDeclaration(PackageDeclaration aNode, int aAbsDepth, boolean includeParent) {
		// NodeList<AnnotationExpr> annotations, Name name
		return applyCombination(
						aNode, includeParent,KeyWords.PACKAGE_DECLARATION, aAbsDepth,
						() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
						() -> getMappingForName(
								aNode.getName(),
								usesPackageAndImportAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false));
	}

	@SuppressWarnings("unchecked")
	@Override
	public default String getMappingForParameter(Parameter aNode, int aAbsDepth, boolean includeParent) {
		// EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations,
		// Type type,
		// boolean isVarArgs, NodeList<AnnotationExpr> varArgsAnnotations,
		// SimpleName name
		return applyCombination(
				aNode, includeParent,KeyWords.PARAMETER, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForType(aNode.getType(), minusOneLevel(aAbsDepth), false),
				() -> getMappingForBoolean(aNode.isVarArgs()),
				() -> getMappingForExpressionList(aNode.getVarArgsAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForSimpleName(
						aNode.getName(), usesVariableNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false));
	}

	@SuppressWarnings("unchecked")
	@Override
	public default String getMappingForEnumDeclaration(EnumDeclaration aNode, int aAbsDepth, boolean includeParent) {
		// EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations,
		// SimpleName name,
		// NodeList<ClassOrInterfaceType> implementedTypes,
		// NodeList<EnumConstantDeclaration> entries,
		// NodeList<BodyDeclaration<?>> members
		return applyCombination(
				aNode, includeParent,KeyWords.ENUM_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForSimpleName(
						aNode.getName(), usesVariableNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false),
				() -> getMappingForClassOrInterfaceTypeList(
						aNode.getImplementedTypes(), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForBodyDeclarationList(aNode.getEntries(), false, minusTwoLevels(aAbsDepth)),
				() -> getMappingForBodyDeclarationList(aNode.getMembers(), false, minusTwoLevels(aAbsDepth)));
	}

	@SuppressWarnings("unchecked")
	@Override
	public default String getMappingForClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration aNode, int aAbsDepth, boolean includeParent) {
		// final EnumSet<Modifier> modifiers, final NodeList<AnnotationExpr>
		// annotations, final boolean isInterface,
		// final SimpleName name, final NodeList<TypeParameter> typeParameters,
		// final NodeList<ClassOrInterfaceType> extendedTypes,
		// final NodeList<ClassOrInterfaceType> implementedTypes, final
		// NodeList<BodyDeclaration<?>> members
		return applyCombination(
				aNode, includeParent,KeyWords.CLASS_OR_INTERFACE_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForBoolean(aNode.isInterface()),
				() -> getMappingForSimpleName(
						aNode.getName(), usesClassNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false),
				() -> getMappingsForTypeParameterList(aNode.getTypeParameters(), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForClassOrInterfaceTypeList(aNode.getExtendedTypes(), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForClassOrInterfaceTypeList(
						aNode.getImplementedTypes(), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForBodyDeclarationList(aNode.getMembers(), false, minusTwoLevels(aAbsDepth)));
	}

	@Override
	public default String getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode, int aAbsDepth, boolean includeParent) {
		// NodeList<AnnotationExpr> annotations, SimpleName name,
		// NodeList<Expression> arguments, NodeList<BodyDeclaration<?>>
		// classBody
		return applyCombination(
				aNode, includeParent,KeyWords.ENUM_CONSTANT_DECLARATION, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForSimpleName(
						aNode.getName(), usesVariableNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false),
				() -> getMappingForExpressionList(aNode.getArguments(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForBodyDeclarationList(aNode.getClassBody(), false, minusTwoLevels(aAbsDepth)));
	}

	@SuppressWarnings("unchecked")
	@Override
	public default String getMappingForMethodDeclaration(MethodDeclaration aNode, int aAbsDepth, boolean includeParent) {
		// final EnumSet<Modifier> modifiers, final NodeList<AnnotationExpr>
		// annotations, final NodeList<TypeParameter> typeParameters,
		// final Type type, final SimpleName name, final boolean isDefault,
		// final NodeList<Parameter> parameters,
		// final NodeList<ReferenceType> thrownExceptions, final BlockStmt body
		return applyCombination(
				aNode, includeParent,KeyWords.METHOD_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingsForTypeParameterList(aNode.getTypeParameters(), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForType(aNode.getType(), minusOneLevel(aAbsDepth), false),
				() -> getMappingForSimpleName(
						aNode.getName(), usesMethodNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false),
				() -> getMappingForBoolean(aNode.isDefault()),
				() -> getMappingForParameterList(aNode.getParameters(), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForTypeList(aNode.getThrownExceptions(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForStatement(aNode.getBody().orElse(null), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForFieldDeclaration(FieldDeclaration aNode, int aAbsDepth, boolean includeParent) {
		// EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations,
		// NodeList<VariableDeclarator> variables
		return applyCombination(
				aNode, includeParent,KeyWords.FIELD_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForVariableDeclaratorList(aNode.getVariables(), true, minusTwoLevels(aAbsDepth)));
	}

	@SuppressWarnings("unchecked")
	@Override
	public default String getMappingForConstructorDeclaration(ConstructorDeclaration aNode, int aAbsDepth, boolean includeParent) {
		// EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations,
		// NodeList<TypeParameter> typeParameters,
		// SimpleName name, NodeList<Parameter> parameters,
		// NodeList<ReferenceType> thrownExceptions, BlockStmt body
		return applyCombination(
				aNode, includeParent,KeyWords.CONSTRUCTOR_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingsForTypeParameterList(aNode.getTypeParameters(), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForSimpleName(
						aNode.getName(), usesClassNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false),
				() -> getMappingForParameterList(aNode.getParameters(), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForTypeList(aNode.getThrownExceptions(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForStatement(aNode.getBody(), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForWhileStmt(WhileStmt aNode, int aAbsDepth, boolean includeParent) {
		// final Expression condition, final Statement body
		return applyCombination(
				aNode, includeParent,KeyWords.WHILE_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getCondition(), minusTwoLevels(aAbsDepth), false),
				() -> getMappingForStatement(aNode.getBody(), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForSwitchStmt(SwitchStmt aNode, int aAbsDepth, boolean includeParent) {
		// final Expression selector, final NodeList<SwitchEntryStmt> entries
		return applyCombination(
				aNode, includeParent,KeyWords.SWITCH_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getSelector(), minusTwoLevels(aAbsDepth), false),
				() -> getMappingForStatementList(aNode.getEntries(), false, minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForForStmt(ForStmt aNode, int aAbsDepth, boolean includeParent) {
		// final NodeList<Expression> initialization, final Expression compare,
		// final NodeList<Expression> update, final Statement body
		return applyCombination(
				aNode, includeParent,KeyWords.FOR_STMT, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getInitialization(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForExpression(aNode.getCompare().orElse(null), minusTwoLevels(aAbsDepth), false),
				() -> getMappingForExpressionList(aNode.getUpdate(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForStatement(aNode.getBody(), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForForeachStmt(ForeachStmt aNode, int aAbsDepth, boolean includeParent) {
		// final VariableDeclarationExpr variable, final Expression iterable,
		// final Statement body
		return applyCombination(
				aNode, includeParent,KeyWords.FOR_EACH_STMT, aAbsDepth,
				() -> getMappingForVariableDeclarationExpr(aNode.getVariable(), minusTwoLevels(aAbsDepth), false),
				() -> getMappingForExpression(aNode.getIterable(), minusTwoLevels(aAbsDepth), false),
				() -> getMappingForStatement(aNode.getBody(), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForExplicitConstructorInvocationStmt(ExplicitConstructorInvocationStmt aNode,
			int aAbsDepth, boolean includeParent) {
		// final NodeList<Type> typeArguments, final boolean isThis, final
		// Expression expression, final NodeList<Expression> arguments
		return applyCombination(
				aNode, includeParent,KeyWords.EXPL_CONSTR_INVOC_STMT, aAbsDepth,
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForBoolean(aNode.isThis()),
				() -> getMappingForExpression(aNode.getExpression().orElse(null), minusOneLevel(aAbsDepth), false),
				() -> getMappingForExpressionList(aNode.getArguments(), true, minusTwoLevels(aAbsDepth)));
	}

	@Override
	public default String getMappingForDoStmt(DoStmt aNode, int aAbsDepth, boolean includeParent) {
		// final Statement body, final Expression condition
		return applyCombination(
				aNode, includeParent,KeyWords.DO_STMT, aAbsDepth,
				() -> getMappingForStatement(aNode.getBody(), minusOneLevel(aAbsDepth), false),
				() -> getMappingForExpression(aNode.getCondition(), minusTwoLevels(aAbsDepth), false));
	}

	@Override
	public default String getMappingForAssertStmt(AssertStmt aNode, int aAbsDepth, boolean includeParent) {
		// final Expression check, final Expression message
		return applyCombination(
				aNode, includeParent,KeyWords.ASSERT_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getCheck(), minusOneLevel(aAbsDepth), false),
				() -> getMappingForExpression(aNode.getMessage().orElse(null), minusTwoLevels(aAbsDepth), false));
	}

	@Override
	public default String getMappingForPrimitiveType(PrimitiveType aNode, int aAbsDepth, boolean includeParent) {
		// final Primitive type
		return applyCombination(
				aNode, includeParent,KeyWords.TYPE_PRIMITIVE, aAbsDepth,
				() -> getMappingForPrimitive(aNode.getType()));
	}

	@Override
	public default String getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode, int aAbsDepth, boolean includeParent) {
		// final EnumSet<Modifier> modifiers, final NodeList<AnnotationExpr>
		// annotations, final NodeList<VariableDeclarator> variables
		return applyCombination(
				aNode, includeParent,KeyWords.VARIABLE_DECLARATION_EXPRESSION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForVariableDeclaratorList(aNode.getVariables(), true, minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForMethodReferenceExpr(MethodReferenceExpr aNode, int aAbsDepth, boolean includeParent) {
		// Expression scope, NodeList<Type> typeArguments, String identifier
		boolean isPrivate = aNode == null ? false : getPrivateMethodBlackList().contains(aNode.getIdentifier());
		return applyCombination(
				aNode, includeParent,KeyWords.METHOD_REFERENCE_EXPRESSION, aAbsDepth,
				// full scope
				() -> getMappingForExpression(aNode.getScope(), noAbstraction(), false),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, minusOneLevel(aAbsDepth)),
				() -> isPrivate || usesMethodNameAbstraction() ? String.valueOf(IBasicKeyWords.KEYWORD_ABSTRACT)
						: getMappingForString(aNode.getIdentifier()));
	}

	@Override
	public default String getMappingForMethodCallExpr(MethodCallExpr aNode, int aAbsDepth, boolean includeParent) {
		// final Expression scope, final NodeList<Type> typeArguments, final
		// SimpleName name, final NodeList<Expression> arguments
		boolean isPrivate = aNode == null ? false
				: getPrivateMethodBlackList().contains(aNode.getName().getIdentifier());
		return applyCombination(
				aNode, includeParent,KeyWords.METHOD_CALL_EXPRESSION, aAbsDepth,
				// full scope if not private
				() -> getMappingForExpression(aNode.getScope().orElse(null), isPrivate ? depthZero() : noAbstraction(), false),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForSimpleName(
						aNode.getName(),
						isPrivate || usesMethodNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false),
				() -> getMappingForExpressionList(aNode.getArguments(), true, minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForFieldAccessExpr(FieldAccessExpr aNode, int aAbsDepth, boolean includeParent) {
		// final Expression scope, final NodeList<Type> typeArguments, final
		// SimpleName name
		return applyCombination(
				aNode, includeParent,KeyWords.FIELD_ACCESS_EXPRESSION, aAbsDepth,
				// full scope
				() -> getMappingForExpression(aNode.getScope().orElse(null), noAbstraction(), false),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForSimpleName(
						aNode.getName(), usesVariableNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false));
	}

	@Override
	public default String getMappingForTypeExpr(TypeExpr aNode, int aAbsDepth, boolean includeParent) {
		// Type type
		return applyCombination(
				aNode, includeParent,KeyWords.TYPE_EXPRESSION, aAbsDepth,
				() -> getMappingForType(aNode.getType(), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForClassExpr(ClassExpr aNode, int aAbsDepth, boolean includeParent) {
		// Type type
		return applyCombination(
				aNode, includeParent,KeyWords.CLASS_EXPRESSION, aAbsDepth,
				() -> getMappingForType(aNode.getType(), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForCastExpr(CastExpr aNode, int aAbsDepth, boolean includeParent) {
		// Type type, Expression expression
		return applyCombination(
				aNode, includeParent,KeyWords.CAST_EXPRESSION, aAbsDepth,
				() -> getMappingForType(aNode.getType(), minusTwoLevels(aAbsDepth), false),
				() -> getMappingForExpression(aNode.getExpression(), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForUnaryExpr(UnaryExpr aNode, int aAbsDepth, boolean includeParent) {
		// final Expression expression, final Operator operator
		return applyCombination(
				aNode, includeParent,KeyWords.UNARY_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression(), minusOneLevel(aAbsDepth), false),
				() -> getMappingForUnaryOperator(aNode.getOperator()));
	}

	@Override
	public default String getMappingForBinaryExpr(BinaryExpr aNode, int aAbsDepth, boolean includeParent) {
		// Expression left, Expression right, Operator operator
		return applyCombination(
				aNode, includeParent,KeyWords.BINARY_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getLeft(), minusOneLevel(aAbsDepth), false),
				() -> getMappingForExpression(aNode.getRight(), minusOneLevel(aAbsDepth), false),
				() -> getMappingForBinaryOperator(aNode.getOperator()));
	}

	@Override
	public default String getMappingForAssignExpr(AssignExpr aNode, int aAbsDepth, boolean includeParent) {
		// Expression target, Expression value, Operator operator
		return applyCombination(
				aNode, includeParent,KeyWords.ASSIGN_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getTarget(), minusTwoLevels(aAbsDepth), false),
				() -> getMappingForExpression(aNode.getValue(), minusOneLevel(aAbsDepth), false),
				() -> getMappingForAssignOperator(aNode.getOperator()));
	}

	@Override
	public default String getMappingForIfStmt(IfStmt aNode, int aAbsDepth, boolean includeParent) {
		// final Expression condition, final Statement thenStmt, final Statement
		// elseStmt
		return applyCombination(
				aNode, includeParent,KeyWords.IF_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getCondition(), minusTwoLevels(aAbsDepth), false),
				() -> getMappingForStatement(aNode.getThenStmt(), minusOneLevel(aAbsDepth), false),
				() -> getMappingForStatement(aNode.getElseStmt().orElse(null), minusOneLevel(aAbsDepth), false));
	}

	@Override
	default String getMappingForLocalClassDeclarationStmt(LocalClassDeclarationStmt aNode, int aAbsDepth, boolean includeParent) {
		// final ClassOrInterfaceDeclaration classDeclaration
		return applyCombination(
				aNode, includeParent,KeyWords.LOCAL_CLASS_DECLARATION_STMT, aAbsDepth,
				() -> getMappingForClassOrInterfaceDeclaration(aNode.getClassDeclaration(), minusOneLevel(aAbsDepth), false));
	}

	@Override
	default String getMappingForArrayType(ArrayType aNode, int aAbsDepth, boolean includeParent) {
		// Type componentType, NodeList<AnnotationExpr> annotations
		return applyCombination(
				aNode, includeParent,KeyWords.ARRAY_TYPE, aAbsDepth,
				() -> getMappingForType(aNode.getComponentType(), minusOneLevel(aAbsDepth), false),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)));
	}

	@Override
	default String getMappingForArrayCreationLevel(ArrayCreationLevel aNode, int aAbsDepth, boolean includeParent) {
		// Expression dimension, NodeList<AnnotationExpr> annotations
		return applyCombination(
				aNode, includeParent,KeyWords.ARRAY_CREATION_LEVEL, aAbsDepth,
				() -> getMappingForExpression(aNode.getDimension().orElse(null), minusOneLevel(aAbsDepth), false),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)));
	}

	@Override
	public default String getMappingForInitializerDeclaration(InitializerDeclaration aNode, int aAbsDepth, boolean includeParent) {
		// boolean isStatic, BlockStmt body
		return applyCombination(
				aNode, includeParent,KeyWords.INITIALIZER_DECLARATION, aAbsDepth,
				() -> getMappingForBoolean(aNode.isStatic()),
				() -> getMappingForStatement(aNode.getBody(), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForThrowStmt(ThrowStmt aNode, int aAbsDepth, boolean includeParent) {
		// final Expression expression
		return applyCombination(
				aNode, includeParent,KeyWords.THROW_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression(), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForNameExpr(NameExpr aNode, int aAbsDepth, boolean includeParent) {
		// final SimpleName name
		return applyCombination(
				aNode, includeParent,KeyWords.NAME_EXPRESSION, aAbsDepth, () -> getMappingForSimpleName(
						aNode.getName(), usesVariableNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false));
	}

	@Override
	public default String getMappingForTryStmt(TryStmt aNode, int aAbsDepth, boolean includeParent) {
		// NodeList<VariableDeclarationExpr> resources, final BlockStmt
		// tryBlock, final NodeList<CatchClause> catchClauses, final BlockStmt
		// finallyBlock
		return applyCombination(
				aNode, includeParent,KeyWords.TRY_STMT, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getResources(), true, minusOneLevel(aAbsDepth)),
				() -> getMappingForStatement(aNode.getTryBlock().orElse(null), minusOneLevel(aAbsDepth), false),
				() -> getMappingForNodeList(aNode.getCatchClauses(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForStatement(aNode.getFinallyBlock().orElse(null), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForThisExpr(ThisExpr aNode, int aAbsDepth, boolean includeParent) {
		// final Expression classExpr
		return applyCombination(
				aNode, includeParent,KeyWords.THIS_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getClassExpr().orElse(null), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForExpressionStmt(ExpressionStmt aNode, int aAbsDepth, boolean includeParent) {
		// final Expression expression
		return applyCombination(
				aNode, includeParent,KeyWords.EXPRESSION_STMT, aAbsDepth,
				// skip the wrapper
				() -> getMappingForExpression(aNode.getExpression(), aAbsDepth, false));
	}

	@Override
	public default String getMappingForSuperExpr(SuperExpr aNode, int aAbsDepth, boolean includeParent) {
		// final Expression classExpr
		return applyCombination(
				aNode, includeParent,KeyWords.SUPER_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getClassExpr().orElse(null), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForReturnStmt(ReturnStmt aNode, int aAbsDepth, boolean includeParent) {
		// final Expression expression
		return applyCombination(
				aNode, includeParent,KeyWords.RETURN_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression().orElse(null), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForLabeledStmt(LabeledStmt aNode, int aAbsDepth, boolean includeParent) {
		// final SimpleName label, final Statement statement
		return applyCombination(
				aNode, includeParent,KeyWords.LABELED_STMT, aAbsDepth,
				() -> getMappingForSimpleName(
						aNode.getLabel(), usesVariableNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false),
				() -> getMappingForStatement(aNode.getStatement(), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForBreakStmt(BreakStmt aNode, int aAbsDepth, boolean includeParent) {
		// final SimpleName label
		return applyCombination(
				aNode, includeParent,KeyWords.BREAK, aAbsDepth,
				() -> getMappingForSimpleName(
						aNode.getLabel().orElse(null),
						usesVariableNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false));
	}

	@Override
	public default String getMappingForSingleMemberAnnotationExpr(SingleMemberAnnotationExpr aNode, int aAbsDepth, boolean includeParent) {
		// final Name name, final Expression memberValue
		return applyCombination(
				aNode, includeParent,KeyWords.SINGLE_MEMBER_ANNOTATION_EXPRESSION, aAbsDepth,
				() -> getMappingForName(
						aNode.getName(), usesAnnotationAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false),
				() -> getMappingForExpression(aNode.getMemberValue(), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForNormalAnnotationExpr(NormalAnnotationExpr aNode, int aAbsDepth, boolean includeParent) {
		// final Name name, final NodeList<MemberValuePair> pairs
		return applyCombination(
				aNode, includeParent,KeyWords.NORMAL_ANNOTATION_EXPRESSION, aAbsDepth,
				() -> getMappingForName(
						aNode.getName(), usesAnnotationAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false),
				() -> getMappingForNodeList(aNode.getPairs(), true, minusTwoLevels(aAbsDepth)));
	}

	@Override
	public default String getMappingForMarkerAnnotationExpr(MarkerAnnotationExpr aNode, int aAbsDepth, boolean includeParent) {
		// final Name name
		return applyCombination(
				aNode, includeParent,KeyWords.MARKER_ANNOTATION_EXPRESSION, aAbsDepth, () -> getMappingForName(
						aNode.getName(), usesAnnotationAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false));
	}

	@Override
	public default String getMappingForWildcardType(WildcardType aNode, int aAbsDepth, boolean includeParent) {
		// final ReferenceType extendedType, final ReferenceType superType
		return applyCombination(
				aNode, includeParent,KeyWords.TYPE_WILDCARD, aAbsDepth,
				() -> getMappingForType(aNode.getExtendedType().orElse(null), minusOneLevel(aAbsDepth), false),
				() -> getMappingForType(aNode.getSuperType().orElse(null), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForBlockStmt(BlockStmt aNode, int aAbsDepth, boolean includeParent) {
		// final NodeList<Statement> statements
		return applyCombination(aNode, includeParent,KeyWords.BLOCK_STMT, aAbsDepth,
				// skip parentheses
				() -> getMappingForStatementList(aNode.getStatements(), false, aAbsDepth));
	}

	@Override
	public default String getMappingForContinueStmt(ContinueStmt aNode, int aAbsDepth, boolean includeParent) {
		// final SimpleName label
		return applyCombination(
				aNode, includeParent,KeyWords.CONTINUE_STMT, aAbsDepth,
				() -> getMappingForSimpleName(
						aNode.getLabel().orElse(null),
						usesVariableNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false));
	}

	@Override
	public default String getMappingForSynchronizedStmt(SynchronizedStmt aNode, int aAbsDepth, boolean includeParent) {
		// final Expression expression, final BlockStmt body
		return applyCombination(
				aNode, includeParent,KeyWords.SYNCHRONIZED_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression(), minusTwoLevels(aAbsDepth), false),
				() -> getMappingForStatement(aNode.getBody(), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForCatchClause(CatchClause aNode, int aAbsDepth, boolean includeParent) {
		// final Parameter parameter, final BlockStmt body
		return applyCombination(
				aNode, includeParent,KeyWords.CATCH_CLAUSE_STMT, aAbsDepth,
				() -> getMappingForParameter(aNode.getParameter(), minusTwoLevels(aAbsDepth), false),
				() -> getMappingForStatement(aNode.getBody(), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForCompilationUnit(CompilationUnit aNode, int aAbsDepth, boolean includeParent) {
		// PackageDeclaration packageDeclaration, NodeList<ImportDeclaration>
		// imports, NodeList<TypeDeclaration<?>> types, ModuleDeclaration module
		return applyCombination(
				aNode, includeParent,KeyWords.COMPILATION_UNIT, aAbsDepth,
				() -> getMappingForNode(aNode.getPackageDeclaration().orElse(null), minusTwoLevels(aAbsDepth), false),
				() -> getMappingForNodeList(aNode.getImports(), false, minusTwoLevels(aAbsDepth)),
				() -> getMappingForBodyDeclarationList(aNode.getTypes(), false, minusOneLevel(aAbsDepth)),
				() -> getMappingForNode(aNode.getModule().orElse(null), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForAnnotationDeclaration(AnnotationDeclaration aNode, int aAbsDepth, boolean includeParent) {
		// EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations,
		// SimpleName name, NodeList<BodyDeclaration<?>> members
		return applyCombination(
				aNode, includeParent,KeyWords.ANNOTATION_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForSimpleName(
						aNode.getName(), usesAnnotationAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false),
				() -> getMappingForBodyDeclarationList(aNode.getMembers(), false, minusOneLevel(aAbsDepth)));
	}

	@Override
	public default String getMappingForAnnotationMemberDeclaration(AnnotationMemberDeclaration aNode, int aAbsDepth, boolean includeParent) {
		// EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations,
		// Type type, SimpleName name, Expression defaultValue
		return applyCombination(
				aNode, includeParent,KeyWords.ANNOTATION_MEMBER_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForType(aNode.getType(), minusOneLevel(aAbsDepth), false),
				() -> getMappingForSimpleName(
						aNode.getName(), usesAnnotationAbstraction() ? depthZero() : minusOneLevel(aAbsDepth), false),
				() -> getMappingForExpression(aNode.getDefaultValue().orElse(null), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForBlockComment(BlockComment aNode, int aAbsDepth, boolean includeParent) {
		// String content
		return applyCombination(
				aNode, includeParent,KeyWords.BLOCK_COMMENT, aAbsDepth, () -> usesCommentAbstraction()
						? String.valueOf(IBasicKeyWords.KEYWORD_ABSTRACT) : getMappingForString(aNode.getContent()));
	}

	@Override
	public default String getMappingForJavadocComment(JavadocComment aNode, int aAbsDepth, boolean includeParent) {
		// String content
		return applyCombination(
				aNode, includeParent,KeyWords.JAVADOC_COMMENT, aAbsDepth, () -> usesCommentAbstraction()
						? String.valueOf(IBasicKeyWords.KEYWORD_ABSTRACT) : getMappingForString(aNode.getContent()));
	}

	@Override
	public default String getMappingForLineComment(LineComment aNode, int aAbsDepth, boolean includeParent) {
		// String content
		return applyCombination(
				aNode, includeParent,KeyWords.LINE_COMMENT, aAbsDepth, () -> usesCommentAbstraction()
						? String.valueOf(IBasicKeyWords.KEYWORD_ABSTRACT) : getMappingForString(aNode.getContent()));
	}

	@Override
	default String getMappingForName(Name aNode, int aAbsDepth, boolean includeParent) {
		// Name qualifier, final String identifier, NodeList<AnnotationExpr>
		// annotations
		return applyCombination(aNode, includeParent,KeyWords.NAME, aAbsDepth,
				// get full qualifier if depth > 0
				() -> getMappingForName(aNode.getQualifier().orElse(null), noAbstraction(), false),
				() -> getMappingForString(aNode.getIdentifier()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)));
	}

	@Override
	default String getMappingForSimpleName(SimpleName aNode, int aAbsDepth, boolean includeParent) {
		// final String identifier
		return applyCombination(
				aNode, includeParent,KeyWords.SIMPLE_NAME, aAbsDepth,
				() -> getMappingForString(aNode.getIdentifier()));
	}

	@Override
	default String getMappingForModuleDeclaration(ModuleDeclaration aNode, int aAbsDepth, boolean includeParent) {
		// NodeList<AnnotationExpr> annotations, Name name, boolean isOpen,
		// NodeList<ModuleStmt> moduleStmts
		return applyCombination(
				aNode, includeParent,KeyWords.MODULE_DECLARATION, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusTwoLevels(aAbsDepth)),
				() -> getMappingForName(
						aNode.getName(), usesClassNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false),
				() -> getMappingForBoolean(aNode.isOpen()),
				() -> getMappingForNodeList(aNode.getModuleStmts(), false, minusOneLevel(aAbsDepth)));
	}

	@Override
	default String getMappingForModuleOpensStmt(ModuleOpensStmt aNode, int aAbsDepth, boolean includeParent) {
		// Name name, NodeList<Name> moduleNames
		return applyCombination(
				aNode, includeParent,KeyWords.MODULE_OPENS_STMT, aAbsDepth,
				() -> getMappingForName(
						aNode.getName(), usesClassNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false),
				() -> getMappingForNodeList(aNode.getModuleNames(), false, minusTwoLevels(aAbsDepth)));
	}

	@Override
	default String getMappingForModuleExportsStmt(ModuleExportsStmt aNode, int aAbsDepth, boolean includeParent) {
		// Name name, NodeList<Name> moduleNames
		return applyCombination(
				aNode, includeParent,KeyWords.MODULE_EXPORTS_STMT, aAbsDepth,
				() -> getMappingForName(
						aNode.getName(), usesClassNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false),
				() -> getMappingForNodeList(aNode.getModuleNames(), false, minusTwoLevels(aAbsDepth)));
	}

	@Override
	default String getMappingForModuleProvidesStmt(ModuleProvidesStmt aNode, int aAbsDepth, boolean includeParent) {
		// Type type, NodeList<Type> withTypes
		return applyCombination(
				aNode, includeParent,KeyWords.MODULE_PROVIDES_STMT, aAbsDepth,
				() -> getMappingForType(aNode.getType(), minusOneLevel(aAbsDepth), false),
				() -> getMappingForTypeList(aNode.getWithTypes(), false, minusOneLevel(aAbsDepth)));
	}

	@Override
	default String getMappingForModuleRequiresStmt(ModuleRequiresStmt aNode, int aAbsDepth, boolean includeParent) {
		// EnumSet<Modifier> modifiers, Name name
		return applyCombination(
				aNode, includeParent,KeyWords.MODULE_REQUIRES_STMT, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()), () -> getMappingForName(
						aNode.getName(), usesClassNameAbstraction() ? depthZero() : minusTwoLevels(aAbsDepth), false));
	}

	@Override
	default String getMappingForModuleUsesStmt(ModuleUsesStmt aNode, int aAbsDepth, boolean includeParent) {
		// Type type
		return applyCombination(
				aNode, includeParent,KeyWords.MODULE_USES_STMT, aAbsDepth,
				() -> getMappingForType(aNode.getType(), minusOneLevel(aAbsDepth), false));
	}

	@Override
	public default String getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode, boolean includeParent) {
		// final String value
		return applyCombination(
				aNode, includeParent,KeyWords.DOUBLE_LITERAL_EXPRESSION,
				usesNumberAbstraction() ? depthZero() : noAbstraction(), () -> aNode.getValue());
	}

	@Override
	public default String getMappingForLongLiteralExpr(LongLiteralExpr aNode, boolean includeParent) {
		// final String value
		return applyCombination(
				aNode, includeParent,KeyWords.LONG_LITERAL_EXPRESSION,
				usesNumberAbstraction() ? depthZero() : noAbstraction(), () -> aNode.getValue());
	}

	@Override
	public default String getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode, boolean includeParent) {
		// final String value
		return applyCombination(
				aNode, includeParent,KeyWords.INTEGER_LITERAL_EXPRESSION,
				usesNumberAbstraction() ? depthZero() : noAbstraction(), () -> aNode.getValue());
	}

	@Override
	public default String getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode, boolean includeParent) {
		// boolean value
		return applyCombination(
				aNode, includeParent,KeyWords.BOOLEAN_LITERAL_EXPRESSION,
				usesBooleanAbstraction() ? depthZero() : noAbstraction(), () -> getMappingForBoolean(aNode.getValue()));
	}

	// should not differentiate between different String values...
	@Override
	public default String getMappingForStringLiteralExpr(StringLiteralExpr aNode, boolean includeParent) {
		// final String value
		return applyCombination(
				aNode, includeParent,KeyWords.STRING_LITERAL_EXPRESSION,
				usesStringAbstraction() ? depthZero() : noAbstraction(), () -> getMappingForString(aNode.getValue()));
	}

	// char values may be important...
	@Override
	public default String getMappingForCharLiteralExpr(CharLiteralExpr aNode, boolean includeParent) {
		// String value
		return applyCombination(
				aNode, includeParent,KeyWords.CHAR_LITERAL_EXPRESSION,
				usesCharAbstraction() ? depthZero() : noAbstraction(), () -> getMappingForChar(aNode.getValue()));
	}

	// Here are some special cases that will always only consist of their
	// keyword but we need to overwrite the simple mapper anyway to get the
	// group brackets

	@Override
	public default String getMappingForNullLiteralExpr(NullLiteralExpr aNode, boolean includeParent) {
		return applyCombination(aNode, includeParent,KeyWords.NULL_LITERAL_EXPRESSION, depthZero());
	}

	@Override
	public default String getMappingForVoidType(VoidType aNode, boolean includeParent) {
		return applyCombination(aNode, includeParent,KeyWords.TYPE_VOID, depthZero());
	}

	@Override
	public default String getMappingForUnknownType(UnknownType aNode, boolean includeParent) {
		return applyCombination(aNode, includeParent,KeyWords.TYPE_UNKNOWN, depthZero());
	}

	@Override
	default String getMappingForEmptyStmt(EmptyStmt aNode, boolean includeParent) {
		return applyCombination(aNode, includeParent,KeyWords.EMPTY_STMT, depthZero());
	}

	@Override
	default String getMappingForUnknownNode(Node aNode, int aAbsDepth, boolean includeParent) {
		return applyCombination(
				aNode, includeParent,KeyWords.UNKNOWN, depthZero(),
				() -> getMappingForString(aNode.getClass().getCanonicalName()));
	}

}
