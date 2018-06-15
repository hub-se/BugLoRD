package se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper;

import java.util.Collections;
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

import se.de.hu_berlin.informatik.astlmbuilder.mapping.IModifierHandler;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.IOperatorHandler;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.ITypeHandler;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IBasicKeyWords;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider.KeyWords;

public interface IBasicAbstractionMapper
		extends IAbstractionMapperBasics, IModifierHandler, IOperatorHandler, ITypeHandler {

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

	public boolean ignoresWrappers();

	public static int minusOneLevel(int absDepth) {
		if (absDepth < 0) {
			return noAbstraction();
		} else {
			return absDepth - 1 < 0 ? 0 : absDepth - 1;
		}
	}

	public static int plusOneLevel(int absDepth) {
		if (absDepth < 0) {
			return noAbstraction();
		} else {
			return absDepth + 1;
		}
	}

	public static int noAbstraction() {
		return -1;
	}

	public static int depthZero() {
		return 0;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Node> List<T> convertToNullListIfNull(List<T> list) {
		if (list == null) {
			return (List<T>) Collections.singletonList(new NullListNode(null));
		} else if (list.isEmpty()) {
			return (List<T>) Collections.singletonList(new EmptyListNode(null));
		} else {
			return list;
		}
	}
	
	public static <T extends Node> void insertList(List<Node> nextNodes, List<T> list) {
		if (list == null) {
			nextNodes.add(new NullListNode(null));
		} else if (list.isEmpty()) {
			nextNodes.add(new EmptyListNode(null));
		} else {
			nextNodes.addAll(list);
		}
	}
	
	public static void insertNode(List<Node> nextNodes, Node node) {
		if (node == null) {
			nextNodes.add(new NullNode(null));
		} else {
			nextNodes.add(node);
		}
	}

	// public static int getMaxChildDepth(Node aNode) {
	// int maxDepth = 0;
	// for (Node node : aNode.getChildNodes()) {
	// int childDepth = getMaxChildDepth(node) + 1;
	// maxDepth = childDepth > maxDepth ? childDepth : maxDepth;
	// }
	// return maxDepth;
	// }

	// all tokens (if not abstract) are stored with all respective constructor
	// arguments (@allFieldsConstructor)

	@Override
	default String getMappingForNull(NullNode aNode, Node parent, boolean includeParent) {
		return applyCombination(aNode, parent, includeParent, KeyWords.NULL, 0);
//		return IAbstractionMapperBasics.super.getMappingForNull(includeParent);
	}
	
	@Override
	default String getMappingForNullList(NullListNode aNode, Node parent, boolean includeParent) {
		return applyCombination(aNode, parent, includeParent, KeyWords.NULL_LIST, 0);
//		return IAbstractionMapperBasics.super.getMappingForNull(includeParent);
	}
	
	@Override
	default String getMappingForEmptyList(EmptyListNode aNode, Node parent, boolean includeParent) {
		return applyCombination(aNode, parent, includeParent, KeyWords.EMPTY_LIST, 0);
//		return IAbstractionMapperBasics.super.getMappingForNull(includeParent);
	}

	@Override
	public default String getMappingForMemberValuePair(MemberValuePair aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getName());
			insertNode(nextNodes, aNode.getValue());
		}
		// final SimpleName name, final Expression value
		return applyCombination(
				aNode, parent, includeParent, KeyWords.MEMBER_VALUE_PAIR, aAbsDepth,
				() -> getMappingForSimpleName(
						aNode.getName(), aNode, usesVariableNameAbstraction() ? depthZero() : minusOneLevel(aAbsDepth),
						false, null),
				() -> getMappingForExpression(aNode.getValue(), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForSwitchEntryStmt(SwitchEntryStmt aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getLabel().orElse(null));
			insertList(nextNodes, aNode.getStatements());
		}
		// final Expression label, final NodeList<Statement> statements
		return applyCombination(
				aNode, parent, includeParent, KeyWords.SWITCH_ENTRY_STMT, aAbsDepth,
				() -> getMappingForExpression(
						aNode.getLabel().orElse(null), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForStatementList(aNode.getStatements(), false, minusOneLevel(aAbsDepth), null));
	}

	@Override
	public default String getMappingForUnionType(UnionType aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getElements());
		}
		// NodeList<ReferenceType> elements
		return applyCombination(
				aNode, parent, includeParent, KeyWords.TYPE_UNION, aAbsDepth,
				() -> getMappingForTypeList(aNode.getElements(), true, minusOneLevel(aAbsDepth), null));
	}

	@Override
	public default String getMappingForIntersectionType(IntersectionType aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getElements());
		}
		// NodeList<ReferenceType> elements
		return applyCombination(
				aNode, parent, includeParent, KeyWords.TYPE_INTERSECTION, aAbsDepth,
				() -> getMappingForTypeList(aNode.getElements(), true, minusOneLevel(aAbsDepth), null));
	}

	@Override
	public default String getMappingForLambdaExpr(LambdaExpr aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getParameters());
			insertNode(nextNodes, aNode.getBody());
		}
		// NodeList<Parameter> parameters, Statement body, boolean
		// isEnclosingParameters
		return applyCombination(
				aNode, parent, includeParent, KeyWords.LAMBDA_EXPRESSION, aAbsDepth,
				() -> getMappingForParameterList(aNode.getParameters(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForStatement(aNode.getBody(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForBoolean(aNode.isEnclosingParameters()));
	}

	@Override
	public default String getMappingForInstanceOfExpr(InstanceOfExpr aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getExpression());
			insertNode(nextNodes, aNode.getType());
		}
		// final Expression expression, final ReferenceType<?> type
		return applyCombination(
				aNode, parent, includeParent, KeyWords.INSTANCEOF_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForType(aNode.getType(), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForConditionalExpr(ConditionalExpr aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getCondition());
			insertNode(nextNodes, aNode.getThenExpr());
			insertNode(nextNodes, aNode.getElseExpr());
		}
		// Expression condition, Expression thenExpr, Expression elseExpr
		return applyCombination(
				aNode, parent, includeParent, KeyWords.CONDITIONAL_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getCondition(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForExpression(aNode.getThenExpr(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForExpression(aNode.getElseExpr(), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForObjectCreationExpr(ObjectCreationExpr aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getScope().orElse(null));
			insertNode(nextNodes, aNode.getType());
			insertList(nextNodes, aNode.getTypeArguments().orElse(null));
			insertList(nextNodes, aNode.getArguments());
			insertList(nextNodes, aNode.getAnonymousClassBody().orElse(null));
		}
		// final Expression scope, final ClassOrInterfaceType type, final
		// NodeList<Type> typeArguments,
		// final NodeList<Expression> arguments, final
		// NodeList<BodyDeclaration<?>> anonymousClassBody
		return applyCombination(aNode, parent, includeParent, KeyWords.OBJ_CREATE_EXPRESSION, aAbsDepth,
				// TODO: get full scope if depth > 0?
				() -> getMappingForExpression(
						aNode.getScope().orElse(null), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForClassOrInterfaceType(aNode.getType(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForTypeList(
						aNode.getTypeArguments().orElse(null), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForExpressionList(aNode.getArguments(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForBodyDeclarationList(
						aNode.getAnonymousClassBody().orElse(null), false, minusOneLevel(aAbsDepth), null));
	}

	@Override
	public default String getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getAnnotations());
			insertNode(nextNodes, aNode.getScope().orElse(null));
			insertNode(nextNodes, aNode.getName());
			insertList(nextNodes, aNode.getTypeArguments().orElse(null));
		}
		// final ClassOrInterfaceType scope, final SimpleName name, final
		// NodeList<Type> typeArguments, final NodeList<AnnotationExpr>
		// annotations
		return applyCombination(aNode, parent, includeParent, KeyWords.CLASS_OR_INTERFACE_TYPE, aAbsDepth,
				// get full scope if depth > 0
				() -> getMappingForType(aNode.getScope().orElse(null), aNode, noAbstraction(), false, null),
				() -> getMappingForSimpleName(
						aNode.getName(), aNode, usesClassNameAbstraction() ? depthZero() : minusOneLevel(aAbsDepth),
						false, null),
				() -> getMappingForTypeList(
						aNode.getTypeArguments().orElse(null), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusOneLevel(aAbsDepth), null));
	}

	@Override
	public default String getMappingForEnclosedExpr(EnclosedExpr aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (ignoresWrappers()) {
			// return null;
			return getMappingForExpression(aNode.getInner(), aNode, aAbsDepth, includeParent, nextNodes);
		} else {
			if (aNode != null && nextNodes != null) {
				insertNode(nextNodes, aNode.getInner());
			}
			// final Expression inner
			return applyCombination(aNode, parent, includeParent, KeyWords.ENCLOSED_EXPRESSION, aAbsDepth,
					// skip parentheses
					() -> getMappingForExpression(aNode.getInner(), aNode, minusOneLevel(aAbsDepth), false, null));
		}
	}

	@Override
	public default String getMappingForArrayInitializerExpr(ArrayInitializerExpr aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getValues());
		}
		// NodeList<Expression> values
		return applyCombination(
				aNode, parent, includeParent, KeyWords.ARRAY_INIT_EXPRESSION, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getValues(), true, minusOneLevel(aAbsDepth), null));
	}

	@Override
	public default String getMappingForArrayCreationExpr(ArrayCreationExpr aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getElementType());
			insertList(nextNodes, aNode.getLevels());
			insertNode(nextNodes, aNode.getInitializer().orElse(null));
		}
		// Type elementType, NodeList<ArrayCreationLevel> levels,
		// ArrayInitializerExpr initializer
		return applyCombination(
				aNode, parent, includeParent, KeyWords.ARRAY_CREATE_EXPRESSION, aAbsDepth,
				() -> getMappingForType(aNode.getElementType(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForArrayCreationLevelList(aNode.getLevels(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForExpression(
						aNode.getInitializer().orElse(null), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForArrayAccessExpr(ArrayAccessExpr aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getName());
			insertNode(nextNodes, aNode.getIndex());
		}
		// Expression name, Expression index
		return applyCombination(
				aNode, parent, includeParent, KeyWords.ARRAY_ACCESS_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getName(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForExpression(aNode.getIndex(), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForTypeParameter(TypeParameter aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getAnnotations());
			insertNode(nextNodes, aNode.getName());
			insertList(nextNodes, aNode.getTypeBound());
		}
		// SimpleName name, NodeList<ClassOrInterfaceType> typeBound,
		// NodeList<AnnotationExpr> annotations
		return applyCombination(
				aNode, parent, includeParent, KeyWords.TYPE_PAR, aAbsDepth,
				() -> getMappingForSimpleName(
						aNode.getName(), aNode,
						usesGenericTypeNameAbstraction() ? depthZero() : minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForClassOrInterfaceTypeList(aNode.getTypeBound(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusOneLevel(aAbsDepth), null));
	}

	@Override
	public default String getMappingForVariableDeclarator(VariableDeclarator aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getType());
			insertNode(nextNodes, aNode.getName());
			insertNode(nextNodes, aNode.getInitializer().orElse(null));
		}
		// Type type, SimpleName name, Expression initializer
		return applyCombination(
				aNode, parent, includeParent, KeyWords.VARIABLE_DECLARATOR, aAbsDepth,
				() -> getMappingForType(aNode.getType(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForSimpleName(
						aNode.getName(), aNode, usesVariableNameAbstraction() ? depthZero() : minusOneLevel(aAbsDepth),
						false, null),
				() -> getMappingForExpression(
						aNode.getInitializer().orElse(null), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForImportDeclaration(ImportDeclaration aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getName());
		}
		// Name name, boolean isStatic, boolean isAsterisk
		return applyCombination(
				aNode, parent, includeParent, KeyWords.IMPORT_DECLARATION, aAbsDepth,
				() -> getMappingForName(
						aNode.getName(), aNode,
						usesPackageAndImportAbstraction() ? depthZero() : minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForBoolean(aNode.isStatic()), () -> getMappingForBoolean(aNode.isAsterisk()));
	}

	@Override
	public default String getMappingForPackageDeclaration(PackageDeclaration aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getAnnotations());
			insertNode(nextNodes, aNode.getName());
		}
		// NodeList<AnnotationExpr> annotations, Name name
		return applyCombination(
				aNode, parent, includeParent, KeyWords.PACKAGE_DECLARATION, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForName(
						aNode.getName(), aNode,
						usesPackageAndImportAbstraction() ? depthZero() : minusOneLevel(aAbsDepth), false, null));
	}

	@SuppressWarnings("unchecked")
	@Override
	public default String getMappingForParameter(Parameter aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getAnnotations());
			insertNode(nextNodes, aNode.getType());
			insertList(nextNodes, aNode.getVarArgsAnnotations());
			insertNode(nextNodes, aNode.getName());
		}
		// EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations,
		// Type type,
		// boolean isVarArgs, NodeList<AnnotationExpr> varArgsAnnotations,
		// SimpleName name
		return applyCombination(
				aNode, parent, includeParent, KeyWords.PARAMETER, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForType(aNode.getType(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForBoolean(aNode.isVarArgs()),
				() -> getMappingForExpressionList(aNode.getVarArgsAnnotations(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForSimpleName(
						aNode.getName(), aNode, usesVariableNameAbstraction() ? depthZero() : minusOneLevel(aAbsDepth),
						false, null));
	}

	@SuppressWarnings("unchecked")
	@Override
	public default String getMappingForEnumDeclaration(EnumDeclaration aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getAnnotations());
			insertNode(nextNodes, aNode.getName());
			insertList(nextNodes, aNode.getImplementedTypes());
			insertList(nextNodes, aNode.getEntries());
			insertList(nextNodes, aNode.getMembers());
		}
		// EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations,
		// SimpleName name,
		// NodeList<ClassOrInterfaceType> implementedTypes,
		// NodeList<EnumConstantDeclaration> entries,
		// NodeList<BodyDeclaration<?>> members
		return applyCombination(
				aNode, parent, includeParent, KeyWords.ENUM_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForSimpleName(
						aNode.getName(), aNode, usesVariableNameAbstraction() ? depthZero() : minusOneLevel(aAbsDepth),
						false, null),
				() -> getMappingForClassOrInterfaceTypeList(
						aNode.getImplementedTypes(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForBodyDeclarationList(aNode.getEntries(), false, minusOneLevel(aAbsDepth), null),
				() -> getMappingForBodyDeclarationList(aNode.getMembers(), false, minusOneLevel(aAbsDepth), null));
	}

	@SuppressWarnings("unchecked")
	@Override
	public default String getMappingForClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration aNode, Node parent,
			int aAbsDepth, boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getAnnotations());
			insertNode(nextNodes, aNode.getName());
			insertList(nextNodes, aNode.getTypeParameters());
			insertList(nextNodes, aNode.getExtendedTypes());
			insertList(nextNodes, aNode.getImplementedTypes());
			insertList(nextNodes, aNode.getMembers());
		}
		// final EnumSet<Modifier> modifiers, final NodeList<AnnotationExpr>
		// annotations, final boolean isInterface,
		// final SimpleName name, final NodeList<TypeParameter> typeParameters,
		// final NodeList<ClassOrInterfaceType> extendedTypes,
		// final NodeList<ClassOrInterfaceType> implementedTypes, final
		// NodeList<BodyDeclaration<?>> members
		return applyCombination(
				aNode, parent, includeParent, KeyWords.CLASS_OR_INTERFACE_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForBoolean(aNode.isInterface()),
				() -> getMappingForSimpleName(
						aNode.getName(), aNode, usesClassNameAbstraction() ? depthZero() : minusOneLevel(aAbsDepth),
						false, null),
				() -> getMappingsForTypeParameterList(aNode.getTypeParameters(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForClassOrInterfaceTypeList(
						aNode.getExtendedTypes(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForClassOrInterfaceTypeList(
						aNode.getImplementedTypes(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForBodyDeclarationList(aNode.getMembers(), false, minusOneLevel(aAbsDepth), null));
	}

	@Override
	public default String getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode, Node parent,
			int aAbsDepth, boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getAnnotations());
			insertNode(nextNodes, aNode.getName());
			insertList(nextNodes, aNode.getArguments());
			insertList(nextNodes, aNode.getClassBody());
		}
		// NodeList<AnnotationExpr> annotations, SimpleName name,
		// NodeList<Expression> arguments, NodeList<BodyDeclaration<?>>
		// classBody
		return applyCombination(
				aNode, parent, includeParent, KeyWords.ENUM_CONSTANT_DECLARATION, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForSimpleName(
						aNode.getName(), aNode, usesVariableNameAbstraction() ? depthZero() : minusOneLevel(aAbsDepth),
						false, null),
				() -> getMappingForExpressionList(aNode.getArguments(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForBodyDeclarationList(aNode.getClassBody(), false, minusOneLevel(aAbsDepth), null));
	}

	@SuppressWarnings("unchecked")
	@Override
	public default String getMappingForMethodDeclaration(MethodDeclaration aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getAnnotations());
			insertList(nextNodes, aNode.getTypeParameters());
			insertNode(nextNodes, aNode.getType());
			insertNode(nextNodes, aNode.getName());
			insertList(nextNodes, aNode.getParameters());
			insertList(nextNodes, aNode.getThrownExceptions());
			insertNode(nextNodes, aNode.getBody().orElse(null));
		}
		// final EnumSet<Modifier> modifiers, final NodeList<AnnotationExpr>
		// annotations,
		// final NodeList<TypeParameter> typeParameters, final Type type, final
		// SimpleName name,
		// final NodeList<Parameter> parameters, final NodeList<ReferenceType>
		// thrownExceptions, final BlockStmt body
		return applyCombination(
				aNode, parent, includeParent, KeyWords.METHOD_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingsForTypeParameterList(aNode.getTypeParameters(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForType(aNode.getType(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForSimpleName(
						aNode.getName(), aNode, usesMethodNameAbstraction() ? depthZero() : minusOneLevel(aAbsDepth),
						false, null),
				() -> getMappingForParameterList(aNode.getParameters(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForTypeList(aNode.getThrownExceptions(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForStatement(
						aNode.getBody().orElse(null), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForFieldDeclaration(FieldDeclaration aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getAnnotations());
			insertList(nextNodes, aNode.getVariables());
		}
		// EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations,
		// NodeList<VariableDeclarator> variables
		return applyCombination(
				aNode, parent, includeParent, KeyWords.FIELD_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForVariableDeclaratorList(aNode.getVariables(), true, minusOneLevel(aAbsDepth), null));
	}

	@SuppressWarnings("unchecked")
	@Override
	public default String getMappingForConstructorDeclaration(ConstructorDeclaration aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getAnnotations());
			insertList(nextNodes, aNode.getTypeParameters());
			insertNode(nextNodes, aNode.getName());
			insertList(nextNodes, aNode.getParameters());
			insertList(nextNodes, aNode.getThrownExceptions());
			insertNode(nextNodes, aNode.getBody());
		}
		// EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations,
		// NodeList<TypeParameter> typeParameters,
		// SimpleName name, NodeList<Parameter> parameters,
		// NodeList<ReferenceType> thrownExceptions, BlockStmt body
		return applyCombination(
				aNode, parent, includeParent, KeyWords.CONSTRUCTOR_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingsForTypeParameterList(aNode.getTypeParameters(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForSimpleName(
						aNode.getName(), aNode, usesClassNameAbstraction() ? depthZero() : minusOneLevel(aAbsDepth),
						false, null),
				() -> getMappingForParameterList(aNode.getParameters(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForTypeList(aNode.getThrownExceptions(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForStatement(aNode.getBody(), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForWhileStmt(WhileStmt aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getCondition());
			insertNode(nextNodes, aNode.getBody());
		}
		// final Expression condition, final Statement body
		return applyCombination(
				aNode, parent, includeParent, KeyWords.WHILE_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getCondition(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForStatement(aNode.getBody(), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForSwitchStmt(SwitchStmt aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getSelector());
			insertList(nextNodes, aNode.getEntries());
		}
		// final Expression selector, final NodeList<SwitchEntryStmt> entries
		return applyCombination(
				aNode, parent, includeParent, KeyWords.SWITCH_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getSelector(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForStatementList(aNode.getEntries(), false, minusOneLevel(aAbsDepth), null));
	}

	@Override
	public default String getMappingForForStmt(ForStmt aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getInitialization());
			insertNode(nextNodes, aNode.getCompare().orElse(null));
			insertList(nextNodes, aNode.getUpdate());
			insertNode(nextNodes, aNode.getBody());
		}
		// final NodeList<Expression> initialization, final Expression compare,
		// final NodeList<Expression> update, final Statement body
		return applyCombination(
				aNode, parent, includeParent, KeyWords.FOR_STMT, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getInitialization(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForExpression(
						aNode.getCompare().orElse(null), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForExpressionList(aNode.getUpdate(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForStatement(aNode.getBody(), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForForeachStmt(ForeachStmt aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getVariable());
			insertNode(nextNodes, aNode.getIterable());
			insertNode(nextNodes, aNode.getBody());
		}
		// final VariableDeclarationExpr variable, final Expression iterable,
		// final Statement body
		return applyCombination(
				aNode, parent, includeParent, KeyWords.FOR_EACH_STMT, aAbsDepth,
				() -> getMappingForVariableDeclarationExpr(
						aNode.getVariable(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForExpression(aNode.getIterable(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForStatement(aNode.getBody(), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForExplicitConstructorInvocationStmt(ExplicitConstructorInvocationStmt aNode,
			Node parent, int aAbsDepth, boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getTypeArguments().orElse(null));
			insertNode(nextNodes, aNode.getExpression().orElse(null));
			insertList(nextNodes, aNode.getArguments());
		}
		// final NodeList<Type> typeArguments, final boolean isThis, final
		// Expression expression, final NodeList<Expression> arguments
		return applyCombination(
				aNode, parent, includeParent, KeyWords.EXPL_CONSTR_INVOC_STMT, aAbsDepth,
				() -> getMappingForTypeList(
						aNode.getTypeArguments().orElse(null), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForBoolean(aNode.isThis()),
				() -> getMappingForExpression(
						aNode.getExpression().orElse(null), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForExpressionList(aNode.getArguments(), true, minusOneLevel(aAbsDepth), null));
	}

	@Override
	public default String getMappingForDoStmt(DoStmt aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getBody());
			insertNode(nextNodes, aNode.getCondition());
		}
		// final Statement body, final Expression condition
		return applyCombination(
				aNode, parent, includeParent, KeyWords.DO_STMT, aAbsDepth,
				() -> getMappingForStatement(aNode.getBody(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForExpression(aNode.getCondition(), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForAssertStmt(AssertStmt aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getCheck());
			insertNode(nextNodes, aNode.getMessage().orElse(null));
		}
		// final Expression check, final Expression message
		return applyCombination(
				aNode, parent, includeParent, KeyWords.ASSERT_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getCheck(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForExpression(
						aNode.getMessage().orElse(null), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForPrimitiveType(PrimitiveType aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		// final Primitive type
		return applyCombination(
				aNode, parent, includeParent, KeyWords.TYPE_PRIMITIVE, aAbsDepth,
				() -> getMappingForPrimitive(aNode.getType()));
	}

	@Override
	public default String getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode, Node parent,
			int aAbsDepth, boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getAnnotations());
			insertList(nextNodes, aNode.getVariables());
		}
		// final EnumSet<Modifier> modifiers, final NodeList<AnnotationExpr>
		// annotations, final NodeList<VariableDeclarator> variables
		return applyCombination(
				aNode, parent, includeParent, KeyWords.VARIABLE_DECLARATION_EXPRESSION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForVariableDeclaratorList(aNode.getVariables(), true, minusOneLevel(aAbsDepth), null));
	}

	@Override
	public default String getMappingForMethodReferenceExpr(MethodReferenceExpr aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getScope());
			insertList(nextNodes, aNode.getTypeArguments().orElse(null));
		}
		// Expression scope, NodeList<Type> typeArguments, String identifier
		boolean isPrivate = aNode == null ? false : getPrivateMethodBlackList().contains(aNode.getIdentifier());
		return applyCombination(aNode, parent, includeParent, KeyWords.METHOD_REFERENCE_EXPRESSION, aAbsDepth,
				// TODO: full scope?
				() -> getMappingForExpression(aNode.getScope(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForTypeList(
						aNode.getTypeArguments().orElse(null), true, minusOneLevel(aAbsDepth), null),
				() -> isPrivate || usesMethodNameAbstraction() ? String.valueOf(IBasicKeyWords.KEYWORD_ABSTRACT)
						: getMappingForString(aNode.getIdentifier()));
	}

	@Override
	public default String getMappingForMethodCallExpr(MethodCallExpr aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getScope().orElse(null));
			insertList(nextNodes, aNode.getTypeArguments().orElse(null));
			insertNode(nextNodes, aNode.getName());
			insertList(nextNodes, aNode.getArguments());
		}
		// final Expression scope, final NodeList<Type> typeArguments, final
		// SimpleName name, final NodeList<Expression> arguments
		boolean isPrivate = aNode == null ? false
				: getPrivateMethodBlackList().contains(aNode.getName().getIdentifier());
		return applyCombination(aNode, parent, includeParent, KeyWords.METHOD_CALL_EXPRESSION, aAbsDepth,
				// TODO: full scope if not private
				() -> getMappingForExpression(
						aNode.getScope().orElse(null), aNode, isPrivate ? depthZero() : minusOneLevel(aAbsDepth), false,
						null),
				() -> getMappingForTypeList(
						aNode.getTypeArguments().orElse(null), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForSimpleName(
						aNode.getName(), aNode,
						isPrivate || usesMethodNameAbstraction() ? depthZero() : minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForExpressionList(aNode.getArguments(), true, minusOneLevel(aAbsDepth), null));
	}

	@Override
	public default String getMappingForFieldAccessExpr(FieldAccessExpr aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getScope());
			insertList(nextNodes, aNode.getTypeArguments().orElse(null));
			insertNode(nextNodes, aNode.getName());
		}
		// final Expression scope, final NodeList<Type> typeArguments, final
		// SimpleName name
		return applyCombination(aNode, parent, includeParent, KeyWords.FIELD_ACCESS_EXPRESSION, aAbsDepth,
				// TODO: full scope?
				() -> getMappingForExpression(aNode.getScope(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForTypeList(
						aNode.getTypeArguments().orElse(null), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForSimpleName(
						aNode.getName(), aNode, usesVariableNameAbstraction() ? depthZero() : minusOneLevel(aAbsDepth),
						false, null));
	}

	@Override
	public default String getMappingForTypeExpr(TypeExpr aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getType());
		}
		// Type type
		return applyCombination(
				aNode, parent, includeParent, KeyWords.TYPE_EXPRESSION, aAbsDepth,
				() -> getMappingForType(aNode.getType(), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForClassExpr(ClassExpr aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getType());
		}
		// Type type
		return applyCombination(
				aNode, parent, includeParent, KeyWords.CLASS_EXPRESSION, aAbsDepth,
				() -> getMappingForType(aNode.getType(), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForCastExpr(CastExpr aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getType());
			insertNode(nextNodes, aNode.getExpression());
		}
		// Type type, Expression expression
		return applyCombination(
				aNode, parent, includeParent, KeyWords.CAST_EXPRESSION, aAbsDepth,
				() -> getMappingForType(aNode.getType(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForExpression(aNode.getExpression(), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForUnaryExpr(UnaryExpr aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getExpression());
		}
		// final Expression expression, final Operator operator
		return applyCombination(
				aNode, parent, includeParent, KeyWords.UNARY_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForUnaryOperator(aNode.getOperator()));
	}

	@Override
	public default String getMappingForBinaryExpr(BinaryExpr aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getLeft());
			insertNode(nextNodes, aNode.getRight());
		}
		// Expression left, Expression right, Operator operator
		return applyCombination(
				aNode, parent, includeParent, KeyWords.BINARY_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getLeft(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForExpression(aNode.getRight(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForBinaryOperator(aNode.getOperator()));
	}

	@Override
	public default String getMappingForAssignExpr(AssignExpr aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getTarget());
			insertNode(nextNodes, aNode.getValue());
		}
		// Expression target, Expression value, Operator operator
		return applyCombination(
				aNode, parent, includeParent, KeyWords.ASSIGN_EXPRESSION, aAbsDepth,
				() -> getMappingForExpression(aNode.getTarget(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForExpression(aNode.getValue(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForAssignOperator(aNode.getOperator()));
	}

	@Override
	public default String getMappingForIfStmt(IfStmt aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getCondition());
			insertNode(nextNodes, aNode.getThenStmt());
			insertNode(nextNodes, aNode.getElseStmt().orElse(null));
		}
		// final Expression condition, final Statement thenStmt, final Statement
		// elseStmt
		return applyCombination(
				aNode, parent, includeParent, KeyWords.IF_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getCondition(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForStatement(aNode.getThenStmt(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForStatement(
						aNode.getElseStmt().orElse(null), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	default String getMappingForLocalClassDeclarationStmt(LocalClassDeclarationStmt aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getClassDeclaration());
		}
		// final ClassOrInterfaceDeclaration classDeclaration
		return applyCombination(
				aNode, parent, includeParent, KeyWords.LOCAL_CLASS_DECLARATION_STMT, aAbsDepth,
				() -> getMappingForClassOrInterfaceDeclaration(
						aNode.getClassDeclaration(), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	default String getMappingForArrayType(ArrayType aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getAnnotations());
			insertNode(nextNodes, aNode.getComponentType());
		}
		// Type componentType, NodeList<AnnotationExpr> annotations
		return applyCombination(
				aNode, parent, includeParent, KeyWords.ARRAY_TYPE, aAbsDepth,
				() -> getMappingForType(aNode.getComponentType(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusOneLevel(aAbsDepth), null));
	}

	@Override
	default String getMappingForArrayCreationLevel(ArrayCreationLevel aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getAnnotations());
			insertNode(nextNodes, aNode.getDimension().orElse(null));
		}
		// Expression dimension, NodeList<AnnotationExpr> annotations
		return applyCombination(
				aNode, parent, includeParent, KeyWords.ARRAY_CREATION_LEVEL, aAbsDepth,
				() -> getMappingForExpression(
						aNode.getDimension().orElse(null), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusOneLevel(aAbsDepth), null));
	}

	@Override
	public default String getMappingForInitializerDeclaration(InitializerDeclaration aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getBody());
		}
		// boolean isStatic, BlockStmt body
		return applyCombination(
				aNode, parent, includeParent, KeyWords.INITIALIZER_DECLARATION, aAbsDepth,
				() -> getMappingForBoolean(aNode.isStatic()),
				() -> getMappingForStatement(aNode.getBody(), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForThrowStmt(ThrowStmt aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getExpression());
		}
		// final Expression expression
		return applyCombination(
				aNode, parent, includeParent, KeyWords.THROW_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression(), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForNameExpr(NameExpr aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getName());
		}
		// final SimpleName name
		return applyCombination(
				aNode, parent, includeParent, KeyWords.NAME_EXPRESSION, aAbsDepth,
				() -> getMappingForSimpleName(
						aNode.getName(), aNode, usesVariableNameAbstraction() ? depthZero() : minusOneLevel(aAbsDepth),
						false, null));
	}

	@Override
	public default String getMappingForTryStmt(TryStmt aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getResources());
			insertNode(nextNodes, aNode.getTryBlock());
			insertList(nextNodes, aNode.getCatchClauses());
			insertNode(nextNodes, aNode.getFinallyBlock().orElse(null));
		}
		// NodeList<VariableDeclarationExpr> resources, final BlockStmt
		// tryBlock, final NodeList<CatchClause> catchClauses, final BlockStmt
		// finallyBlock
		return applyCombination(
				aNode, parent, includeParent, KeyWords.TRY_STMT, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getResources(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForStatement(aNode.getTryBlock(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForNodeList(aNode.getCatchClauses(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForStatement(
						aNode.getFinallyBlock().orElse(null), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForThisExpr(ThisExpr aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getClassExpr().orElse(null));
		}
		// final Expression classExpr
		return applyCombination(
				aNode, parent, includeParent, KeyWords.THIS_EXPRESSION, aAbsDepth, () -> getMappingForExpression(
						aNode.getClassExpr().orElse(null), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForExpressionStmt(ExpressionStmt aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (ignoresWrappers()) {
			// return null;
			return getMappingForExpression(aNode.getExpression(), aNode, aAbsDepth, includeParent, nextNodes);
		} else {
			if (aNode != null && nextNodes != null) {
				insertNode(nextNodes, aNode.getExpression());
			}
			// final Expression expression
			return applyCombination(aNode, parent, includeParent, KeyWords.EXPRESSION_STMT, aAbsDepth,
					// skip the wrapper
					() -> getMappingForExpression(aNode.getExpression(), aNode, minusOneLevel(aAbsDepth), false, null));
		}
	}

	@Override
	public default String getMappingForSuperExpr(SuperExpr aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getClassExpr().orElse(null));
		}
		// final Expression classExpr
		return applyCombination(
				aNode, parent, includeParent, KeyWords.SUPER_EXPRESSION, aAbsDepth, () -> getMappingForExpression(
						aNode.getClassExpr().orElse(null), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForReturnStmt(ReturnStmt aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getExpression().orElse(null));
		}
		// final Expression expression
		return applyCombination(
				aNode, parent, includeParent, KeyWords.RETURN_STMT, aAbsDepth, () -> getMappingForExpression(
						aNode.getExpression().orElse(null), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForLabeledStmt(LabeledStmt aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getLabel());
			insertNode(nextNodes, aNode.getStatement());
		}
		// final SimpleName label, final Statement statement
		return applyCombination(
				aNode, parent, includeParent, KeyWords.LABELED_STMT, aAbsDepth,
				() -> getMappingForSimpleName(
						aNode.getLabel(), aNode, usesVariableNameAbstraction() ? depthZero() : minusOneLevel(aAbsDepth),
						false, null),
				() -> getMappingForStatement(aNode.getStatement(), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForBreakStmt(BreakStmt aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getLabel().orElse(null));
		}
		// final SimpleName label
		return applyCombination(
				aNode, parent, includeParent, KeyWords.BREAK, aAbsDepth,
				() -> getMappingForSimpleName(
						aNode.getLabel().orElse(null), aNode,
						usesVariableNameAbstraction() ? depthZero() : minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForSingleMemberAnnotationExpr(SingleMemberAnnotationExpr aNode, Node parent,
			int aAbsDepth, boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getName());
			insertNode(nextNodes, aNode.getMemberValue());
		}
		// final Name name, final Expression memberValue
		return applyCombination(
				aNode, parent, includeParent, KeyWords.SINGLE_MEMBER_ANNOTATION_EXPRESSION, aAbsDepth,
				() -> getMappingForName(
						aNode.getName(), aNode, usesAnnotationAbstraction() ? depthZero() : minusOneLevel(aAbsDepth),
						false, null),
				() -> getMappingForExpression(aNode.getMemberValue(), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForNormalAnnotationExpr(NormalAnnotationExpr aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getName());
			insertList(nextNodes, aNode.getPairs());
		}
		// final Name name, final NodeList<MemberValuePair> pairs
		return applyCombination(
				aNode, parent, includeParent, KeyWords.NORMAL_ANNOTATION_EXPRESSION, aAbsDepth,
				() -> getMappingForName(
						aNode.getName(), aNode, usesAnnotationAbstraction() ? depthZero() : minusOneLevel(aAbsDepth),
						false, null),
				() -> getMappingForNodeList(aNode.getPairs(), true, minusOneLevel(aAbsDepth), null));
	}

	@Override
	public default String getMappingForMarkerAnnotationExpr(MarkerAnnotationExpr aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getName());
		}
		// final Name name
		return applyCombination(
				aNode, parent, includeParent, KeyWords.MARKER_ANNOTATION_EXPRESSION, aAbsDepth,
				() -> getMappingForName(
						aNode.getName(), aNode, usesAnnotationAbstraction() ? depthZero() : minusOneLevel(aAbsDepth),
						false, nextNodes));
	}

	@Override
	public default String getMappingForWildcardType(WildcardType aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getAnnotations());
			insertNode(nextNodes, aNode.getExtendedType().orElse(null));
			insertNode(nextNodes, aNode.getSuperType().orElse(null));
		}
		// final ReferenceType extendedType, final ReferenceType superType
		return applyCombination(
				aNode, parent, includeParent, KeyWords.TYPE_WILDCARD, aAbsDepth,
				() -> getMappingForType(
						aNode.getExtendedType().orElse(null), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForType(
						aNode.getSuperType().orElse(null), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusOneLevel(aAbsDepth), null));
	}

	@Override
	public default String getMappingForBlockStmt(BlockStmt aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getStatements());
		}
		// final NodeList<Statement> statements
		return applyCombination(aNode, parent, includeParent, KeyWords.BLOCK_STMT, aAbsDepth,
				// skip parentheses
				() -> getMappingForStatementList(aNode.getStatements(), false, minusOneLevel(aAbsDepth), null));
	}

	@Override
	public default String getMappingForContinueStmt(ContinueStmt aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getLabel().orElse(null));
		}
		// final SimpleName label
		return applyCombination(
				aNode, parent, includeParent, KeyWords.CONTINUE_STMT, aAbsDepth,
				() -> getMappingForSimpleName(
						aNode.getLabel().orElse(null), aNode,
						usesVariableNameAbstraction() ? depthZero() : minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForSynchronizedStmt(SynchronizedStmt aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getExpression());
			insertNode(nextNodes, aNode.getBody());
		}
		// final Expression expression, final BlockStmt body
		return applyCombination(
				aNode, parent, includeParent, KeyWords.SYNCHRONIZED_STMT, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForStatement(aNode.getBody(), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForCatchClause(CatchClause aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getParameter());
			insertNode(nextNodes, aNode.getBody());
		}
		// final Parameter parameter, final BlockStmt body
		return applyCombination(
				aNode, parent, includeParent, KeyWords.CATCH_CLAUSE_STMT, aAbsDepth,
				() -> getMappingForParameter(aNode.getParameter(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForStatement(aNode.getBody(), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForCompilationUnit(CompilationUnit aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getPackageDeclaration().orElse(null));
			insertList(nextNodes, aNode.getImports());
			insertList(nextNodes, aNode.getTypes());
			insertNode(nextNodes, aNode.getModule().orElse(null));
		}
		// PackageDeclaration packageDeclaration, NodeList<ImportDeclaration>
		// imports, NodeList<TypeDeclaration<?>> types, ModuleDeclaration module
		return applyCombination(
				aNode, parent, includeParent, KeyWords.COMPILATION_UNIT, aAbsDepth,
				() -> getMappingForPackageDeclaration(
						aNode.getPackageDeclaration().orElse(null), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForNodeList(aNode.getImports(), false, minusOneLevel(aAbsDepth), null),
				() -> getMappingForBodyDeclarationList(aNode.getTypes(), false, minusOneLevel(aAbsDepth), null),
				() -> getMappingForModuleDeclaration(
						aNode.getModule().orElse(null), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForAnnotationDeclaration(AnnotationDeclaration aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getAnnotations());
			insertNode(nextNodes, aNode.getName());
			insertList(nextNodes, aNode.getMembers());
		}
		// EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations,
		// SimpleName name, NodeList<BodyDeclaration<?>> members
		return applyCombination(
				aNode, parent, includeParent, KeyWords.ANNOTATION_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForSimpleName(
						aNode.getName(), aNode, usesAnnotationAbstraction() ? depthZero() : minusOneLevel(aAbsDepth),
						false, null),
				() -> getMappingForBodyDeclarationList(aNode.getMembers(), false, minusOneLevel(aAbsDepth), null));
	}

	@Override
	public default String getMappingForAnnotationMemberDeclaration(AnnotationMemberDeclaration aNode, Node parent,
			int aAbsDepth, boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getAnnotations());
			insertNode(nextNodes, aNode.getType());
			insertNode(nextNodes, aNode.getName());
			insertNode(nextNodes, aNode.getDefaultValue().orElse(null));
		}
		// EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations,
		// Type type, SimpleName name, Expression defaultValue
		return applyCombination(
				aNode, parent, includeParent, KeyWords.ANNOTATION_MEMBER_DECLARATION, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForType(aNode.getType(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForSimpleName(
						aNode.getName(), aNode, usesAnnotationAbstraction() ? depthZero() : minusOneLevel(aAbsDepth),
						false, null),
				() -> getMappingForExpression(
						aNode.getDefaultValue().orElse(null), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForBlockComment(BlockComment aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		// String content
		return applyCombination(
				aNode, parent, includeParent, KeyWords.BLOCK_COMMENT, aAbsDepth, () -> usesCommentAbstraction()
						? String.valueOf(IBasicKeyWords.KEYWORD_ABSTRACT) : getMappingForString(aNode.getContent()));
	}

	@Override
	public default String getMappingForJavadocComment(JavadocComment aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		// String content
		return applyCombination(
				aNode, parent, includeParent, KeyWords.JAVADOC_COMMENT, aAbsDepth, () -> usesCommentAbstraction()
						? String.valueOf(IBasicKeyWords.KEYWORD_ABSTRACT) : getMappingForString(aNode.getContent()));
	}

	@Override
	public default String getMappingForLineComment(LineComment aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		// String content
		return applyCombination(
				aNode, parent, includeParent, KeyWords.LINE_COMMENT, aAbsDepth, () -> usesCommentAbstraction()
						? String.valueOf(IBasicKeyWords.KEYWORD_ABSTRACT) : getMappingForString(aNode.getContent()));
	}

	@Override
	default String getMappingForName(Name aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getAnnotations());
			insertNode(nextNodes, aNode.getQualifier().orElse(null));
		}
		// Name qualifier, final String identifier, NodeList<AnnotationExpr>
		// annotations
		return applyCombination(aNode, parent, includeParent, KeyWords.NAME, aAbsDepth,
				// get full qualifier if depth > 0
				() -> getMappingForName(aNode.getQualifier().orElse(null), aNode, noAbstraction(), false, null),
				() -> getMappingForString(aNode.getIdentifier()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusOneLevel(aAbsDepth), null));
	}

	@Override
	default String getMappingForSimpleName(SimpleName aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		// final String identifier
		return applyCombination(
				aNode, parent, includeParent, KeyWords.SIMPLE_NAME, aAbsDepth,
				() -> getMappingForString(aNode.getIdentifier()));
	}

	@Override
	default String getMappingForModuleDeclaration(ModuleDeclaration aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertList(nextNodes, aNode.getAnnotations());
			insertNode(nextNodes, aNode.getName());
			insertList(nextNodes, aNode.getModuleStmts());
		}
		// NodeList<AnnotationExpr> annotations, Name name, boolean isOpen,
		// NodeList<ModuleStmt> moduleStmts
		return applyCombination(
				aNode, parent, includeParent, KeyWords.MODULE_DECLARATION, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, minusOneLevel(aAbsDepth), null),
				() -> getMappingForName(
						aNode.getName(), aNode, usesClassNameAbstraction() ? depthZero() : minusOneLevel(aAbsDepth),
						false, null),
				() -> getMappingForBoolean(aNode.isOpen()),
				() -> getMappingForNodeList(aNode.getModuleStmts(), false, minusOneLevel(aAbsDepth), null));
	}

	@Override
	default String getMappingForModuleOpensStmt(ModuleOpensStmt aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getName());
			insertList(nextNodes, aNode.getModuleNames());
		}
		// Name name, NodeList<Name> moduleNames
		return applyCombination(
				aNode, parent, includeParent, KeyWords.MODULE_OPENS_STMT, aAbsDepth,
				() -> getMappingForName(
						aNode.getName(), aNode, usesClassNameAbstraction() ? depthZero() : minusOneLevel(aAbsDepth),
						false, null),
				() -> getMappingForNodeList(aNode.getModuleNames(), false, minusOneLevel(aAbsDepth), null));
	}

	@Override
	default String getMappingForModuleExportsStmt(ModuleExportsStmt aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getName());
			insertList(nextNodes, aNode.getModuleNames());
		}
		// Name name, NodeList<Name> moduleNames
		return applyCombination(
				aNode, parent, includeParent, KeyWords.MODULE_EXPORTS_STMT, aAbsDepth,
				() -> getMappingForName(
						aNode.getName(), aNode, usesClassNameAbstraction() ? depthZero() : minusOneLevel(aAbsDepth),
						false, null),
				() -> getMappingForNodeList(aNode.getModuleNames(), false, minusOneLevel(aAbsDepth), null));
	}

	@Override
	default String getMappingForModuleProvidesStmt(ModuleProvidesStmt aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getType());
			insertList(nextNodes, aNode.getWithTypes());
		}
		// Type type, NodeList<Type> withTypes
		return applyCombination(
				aNode, parent, includeParent, KeyWords.MODULE_PROVIDES_STMT, aAbsDepth,
				() -> getMappingForType(aNode.getType(), aNode, minusOneLevel(aAbsDepth), false, null),
				() -> getMappingForTypeList(aNode.getWithTypes(), false, minusOneLevel(aAbsDepth), null));
	}

	@Override
	default String getMappingForModuleRequiresStmt(ModuleRequiresStmt aNode, Node parent, int aAbsDepth,
			boolean includeParent, List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getName());
		}
		// EnumSet<Modifier> modifiers, Name name
		return applyCombination(
				aNode, parent, includeParent, KeyWords.MODULE_REQUIRES_STMT, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForName(
						aNode.getName(), aNode, usesClassNameAbstraction() ? depthZero() : minusOneLevel(aAbsDepth),
						false, nextNodes));
	}

	@Override
	default String getMappingForModuleUsesStmt(ModuleUsesStmt aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		if (aNode != null && nextNodes != null) {
			insertNode(nextNodes, aNode.getType());
		}
		// Type type
		return applyCombination(
				aNode, parent, includeParent, KeyWords.MODULE_USES_STMT, aAbsDepth,
				() -> getMappingForType(aNode.getType(), aNode, minusOneLevel(aAbsDepth), false, null));
	}

	@Override
	public default String getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode, Node parent, boolean includeParent,
			List<Node> nextNodes) {
		// final String value
		return applyCombination(
				aNode, parent, includeParent, KeyWords.DOUBLE_LITERAL_EXPRESSION,
				usesNumberAbstraction() ? depthZero() : noAbstraction(), () -> aNode.getValue());
	}

	@Override
	public default String getMappingForLongLiteralExpr(LongLiteralExpr aNode, Node parent, boolean includeParent,
			List<Node> nextNodes) {
		// final String value
		return applyCombination(
				aNode, parent, includeParent, KeyWords.LONG_LITERAL_EXPRESSION,
				usesNumberAbstraction() ? depthZero() : noAbstraction(), () -> aNode.getValue());
	}

	@Override
	public default String getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode, Node parent, boolean includeParent,
			List<Node> nextNodes) {
		// final String value
		return applyCombination(
				aNode, parent, includeParent, KeyWords.INTEGER_LITERAL_EXPRESSION,
				usesNumberAbstraction() ? depthZero() : noAbstraction(), () -> aNode.getValue());
	}

	@Override
	public default String getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode, Node parent, boolean includeParent,
			List<Node> nextNodes) {
		// boolean value
		return applyCombination(
				aNode, parent, includeParent, KeyWords.BOOLEAN_LITERAL_EXPRESSION,
				usesBooleanAbstraction() ? depthZero() : noAbstraction(), () -> getMappingForBoolean(aNode.getValue()));
	}

	// should not differentiate between different String values...
	@Override
	public default String getMappingForStringLiteralExpr(StringLiteralExpr aNode, Node parent, boolean includeParent,
			List<Node> nextNodes) {
		// final String value
		return applyCombination(
				aNode, parent, includeParent, KeyWords.STRING_LITERAL_EXPRESSION,
				usesStringAbstraction() ? depthZero() : noAbstraction(), () -> getMappingForString(aNode.getValue()));
	}

	// char values may be important...
	@Override
	public default String getMappingForCharLiteralExpr(CharLiteralExpr aNode, Node parent, boolean includeParent,
			List<Node> nextNodes) {
		// String value
		return applyCombination(
				aNode, parent, includeParent, KeyWords.CHAR_LITERAL_EXPRESSION,
				usesCharAbstraction() ? depthZero() : noAbstraction(), () -> getMappingForChar(aNode.getValue()));
	}

	// Here are some special cases that will always only consist of their
	// keyword but we need to overwrite the simple mapper anyway to get the
	// group brackets

	@Override
	public default String getMappingForNullLiteralExpr(NullLiteralExpr aNode, Node parent, boolean includeParent,
			List<Node> nextNodes) {
		return applyCombination(aNode, parent, includeParent, KeyWords.NULL_LITERAL_EXPRESSION, depthZero());
	}

	@Override
	public default String getMappingForVoidType(VoidType aNode, Node parent, boolean includeParent,
			List<Node> nextNodes) {
		return applyCombination(aNode, parent, includeParent, KeyWords.TYPE_VOID, depthZero());
	}

	@Override
	public default String getMappingForUnknownType(UnknownType aNode, Node parent, boolean includeParent,
			List<Node> nextNodes) {
		return applyCombination(aNode, parent, includeParent, KeyWords.TYPE_UNKNOWN, depthZero());
	}

	@Override
	default String getMappingForEmptyStmt(EmptyStmt aNode, Node parent, boolean includeParent, List<Node> nextNodes) {
		return applyCombination(aNode, parent, includeParent, KeyWords.EMPTY_STMT, depthZero());
	}

	@Override
	default String getMappingForUnknownNode(Node aNode, Node parent, int aAbsDepth, boolean includeParent,
			List<Node> nextNodes) {
		return applyCombination(
				aNode, parent, includeParent, KeyWords.UNKNOWN, depthZero(),
				() -> getMappingForString(aNode.getClass().getCanonicalName()));
	}

}
