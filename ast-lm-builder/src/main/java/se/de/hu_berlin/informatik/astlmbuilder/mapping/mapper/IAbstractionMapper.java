package se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
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
import com.github.javaparser.ast.nodeTypes.NodeWithTypeArguments;
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
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IBasicKeyWords;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.BodyStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ElseStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ExtendsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ImplementsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ThrowsStmt;

public interface IAbstractionMapper extends IMapper<String> {
	
	/**
	 * All tokens will be put together into one string that can be recreated
	 * very easy
	 * 
	 * @param aIdentifier
	 *            The keyword of the node
	 * @param aTokens
	 *            All its data blocks
	 * @return A finished string for the language model
	 */
	@SafeVarargs
	public static String combineData2String(IBasicKeyWords provider, Supplier<String> aIdentifier, Supplier<String>... aTokens) {
		// in contrast to the other methods i decided to use a StringBuilder
		// here because we will have more tokens
		StringBuilder result = new StringBuilder();
		result.append( provider.getBigGroupStart() );
		result.append(aIdentifier.get());

		if (aTokens == null || aTokens.length == 0) {
			result.append(provider.getBigGroupEnd());
			return result.toString();
		}

		// fix the tokens that did not get the child group brackets
		String[] fixedTokens = new String[aTokens.length];

		for (int i = 0; i < aTokens.length; ++i) {
			String fixedT = aTokens[i].get();
			// startsWith with chars
			if( fixedT == null || fixedT.length() == 0 ) {
				fixedT = "" + provider.getGroupStart() + provider.getGroupEnd();
			} else if (!(fixedT.charAt(0) == provider.getGroupStart())) {
				fixedT = provider.getGroupStart() + fixedT + provider.getGroupEnd();
			}

			fixedTokens[i] = fixedT;
		}

		// there are some data to be put into the string
		result.append(provider.getIdMarker());

		// String.join does not work for chars :(
		for (int i = 0; i < fixedTokens.length; ++i) {
			result.append(fixedTokens[i]);
			if (i < fixedTokens.length - 1) {
				// there is more to come
				result.append(provider.getSplit());
			}
		}

		result.append(provider.getBigGroupEnd());

		return result.toString();
	}
	
	@SafeVarargs
	public static String applyCombination(IBasicKeyWords provider, Supplier<String> getKeyWord, int aAbsDepth, Supplier<String>... mappings) {
		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(provider, getKeyWord);
		} else { // still at a higher level of abstraction (either negative or greater than 0)
			--aAbsDepth;
			return combineData2String(provider, getKeyWord, mappings);
		}
	}
	

	/**
	 * Creates a mapping for type arguments
	 * 
	 * @param typeArguments
	 *            The type arguments
	 * @param aAbsDepth
	 *            The depth of the mapping
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForTypeArguments(NodeWithTypeArguments<?> typeArguments, int aAbsDepth) {
		if (typeArguments != null && typeArguments.getTypeArguments().isPresent()
				&& !typeArguments.getTypeArguments().get().isEmpty() && !typeArguments.isUsingDiamondOperator()) {

			NodeList<Type> tArgs = typeArguments.getTypeArguments().get();
			String result = getKeyWordProvider().getTypeArgStart() + getMappingForType(tArgs.get(0), aAbsDepth);

			for (int i = 1; i < tArgs.size(); ++i) {
				result += getKeyWordProvider().getSplit() + getMappingForType(tArgs.get(i), aAbsDepth);
			}

			result += getKeyWordProvider().getTypeArgEnd();

			return result;
		} else {
			return getKeyWordProvider().getTypeArgStart() + "" + getKeyWordProvider().getTypeArgEnd();
		}
	}
	
	/**
	 * Creates a mapping for a list of variable declarators
	 * 
	 * @param vars
	 *            The list of variable declarators that should be mapped
	 * @param aAbsDepth
	 *            The depth of the mapping
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForVariableDeclaratorList(List<VariableDeclarator> vars, int aAbsDepth) {
		return getMappingForList(vars, aAbsDepth, this::getMappingForVariableDeclarator);
	}

	/**
	 * Creates a mapping for a list of types
	 * 
	 * @param types
	 *            The list of types that should be mapped
	 * @param aAbsDepth
	 *            The depth of the mapping
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForTypeList(List<? extends Type> types, int aAbsDepth) {
		return getMappingForList(types, aAbsDepth, this::getMappingForType);
	}

	/**
	 * Creates a mapping for a list of arguments
	 * 
	 * @param parameters
	 *            The list of parameters
	 * @param aAbsDepth
	 *            The depth of the mapping
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForParameterList(List<Parameter> parameters, int aAbsDepth) {
		return getMappingForList(parameters, aAbsDepth, this::getMappingForParameter);
	}

	/**
	 * Creates a mapping for a list of expressions
	 * 
	 * @param expressions
	 *            The list of expressions
	 * @param aAbsDepth
	 *            The depth of the mapping
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForExpressionList(List<Expression> expressions, int aAbsDepth) {
		return getMappingForList(expressions, aAbsDepth, this::getMappingForExpression);
	}
	
	/**
	 * Creates a mapping for a list of array creation levels
	 * 
	 * @param levels
	 *            The list of array creation levels
	 * @param aAbsDepth
	 *            The depth of the mapping
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForArrayCreationLevelList(List<ArrayCreationLevel> levels, int aAbsDepth) {
		return getMappingForList(levels, aAbsDepth, this::getMappingForArrayCreationLevel);
	}

	/**
	 * Creates a mapping for a list of body declarations
	 * 
	 * @param bodyDeclarations
	 *            The list of body declarations
	 * @param aAbsDepth
	 *            The depth of the mapping
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForBodyDeclarationList(List<BodyDeclaration<?>> bodyDeclarations, int aAbsDepth) {
		return getMappingForList(bodyDeclarations, aAbsDepth, this::getMappingForBodyDeclaration);
	}

	/**
	 * Creates a mapping for a list of body declarations
	 * 
	 * @param types
	 *            The list of body declarations
	 * @param aAbsDepth
	 *            The depth of the mapping
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForClassOrInterfaceTypeList(List<ClassOrInterfaceType> types, int aAbsDepth) {
		return getMappingForList(types, aAbsDepth, this::getMappingForType);
	}

	/**
	 * Creates a mapping for a list of type parameters
	 * 
	 * @param typeParameters
	 *            The list of type parameters
	 * @param aAbsDepth
	 *            The depth of the mapping
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingsForTypeParameterList(List<TypeParameter> typeParameters, int aAbsDepth) {
		return getMappingForList(typeParameters, aAbsDepth, this::getMappingForTypeParameter);
	}

	/**
	 * Creates a mapping for a list of type parameters
	 * @param list
	 * The list of type parameters
	 * @param aAbsDepth
	 * The depth of the mapping
	 * @param getMappingForT
	 * a function that gets a mapping for an object of type T
	 * @return
	 * A token that represents the mapping with the given depth
	 */
	public default <T> String getMappingForList(List<T> list, int aAbsDepth, 
			BiFunction<T, Integer, String> getMappingForT) {
		String result = "" + getKeyWordProvider().getGroupStart();

		if (list != null && !list.isEmpty()) {
			result += getMappingForT.apply(list.get(0), aAbsDepth);

			for (int i = 1; i < list.size(); ++i) {
				result += getKeyWordProvider().getSplit() + getMappingForT.apply(list.get(i), aAbsDepth);
			}

		}

		return result + getKeyWordProvider().getGroupEnd();
	}
	
	/**
	 * Returns the mapping for the scope of a class or interface type node
	 * 
	 * @param aNode
	 *            The class or interface type node
	 * @return The mapping for the scope
	 */
	public default String getMappingForScope(ClassOrInterfaceType aNode) {
		if (aNode == null) {
			return getKeyWordProvider().getGroupStart() + "" + getKeyWordProvider().getGroupEnd();
		}

		return getKeyWordProvider().getGroupStart() + getFullScope(aNode) + getKeyWordProvider().getGroupEnd();
	}

	/**
	 * Creates a mapping for a scope
	 * 
	 * @param scope
	 *            The scope expression
	 * @param aAbsDepth
	 *            The depth of the mapping
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForScope(Expression scope, int aAbsDepth) {
		if (scope != null) {
			return getKeyWordProvider().getGroupStart() + getMappingForExpression(scope, aAbsDepth) + getKeyWordProvider().getGroupEnd();
		} else {
			return getKeyWordProvider().getGroupStart() + "" + getKeyWordProvider().getGroupEnd();
		}
	}
	
	/**
	 * Returns the full scope for a given class or interface type
	 * 
	 * @param aNode
	 *            The class or interface type node
	 * @return The scope as part of a token
	 */
	public default String getFullScope(ClassOrInterfaceType aNode) {
		if (!aNode.getScope().isPresent()) {
			return aNode.getNameAsString();
		} else {
			return getFullScope(aNode.getScope().get()) + "." + aNode.getNameAsString();
		}
	}
		

	@Override
	public default String getMappingForMemberValuePair(MemberValuePair aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getMemberValuePair, aAbsDepth, 
				() -> aNode.getNameAsString(), 
				() -> getMappingForExpression(aNode.getValue(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForSwitchEntryStmt(SwitchEntryStmt aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getSwitchEntryStatement, aAbsDepth, 
				() -> getMappingForExpression(aNode.getLabel().orElse(null), aAbsDepth-1));
	}

	@Override
	public default String getMappingForUnionType(UnionType aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getTypeUnion, aAbsDepth,
				() -> getMappingForTypeList(aNode.getElements(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForIntersectionType(IntersectionType aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getTypeIntersection, aAbsDepth,
				() -> getMappingForTypeList(aNode.getElements(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForLambdaExpr(LambdaExpr aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getLambdaExpression, aAbsDepth,
				() -> (aNode.isEnclosingParameters() ? "true" : "false"),
				() -> getMappingForParameterList(aNode.getParameters(), aAbsDepth-1),
				() -> getMappingForStatement(aNode.getBody(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForInstanceOfExpr(InstanceOfExpr aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getInstanceofExpression, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression(), aAbsDepth-1),
				() -> getMappingForType(aNode.getType(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForConditionalExpr(ConditionalExpr aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getConditionalExpression, aAbsDepth,
				() -> getMappingForExpression(aNode.getCondition(), aAbsDepth-1),
				() -> getMappingForExpression(aNode.getThenExpr(), aAbsDepth-1),
				() -> getMappingForExpression(aNode.getElseExpr(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForObjectCreationExpr(ObjectCreationExpr aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getObjCreateExpression, aAbsDepth,
				() -> getMappingForScope(aNode.getScope().orElse(null), aAbsDepth-1),
				() -> getMappingForClassOrInterfaceType(aNode.getType(), aAbsDepth-1),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), aAbsDepth-1),
				() -> getMappingForExpressionList(aNode.getArguments(), aAbsDepth-1),
				() -> getMappingForBodyDeclarationList(aNode.getAnonymousClassBody().orElse(null), aAbsDepth-1));
	}

	@Override
	public default String getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getClassOrInterfaceType, aAbsDepth,
				() -> getMappingForScope(aNode),
				() -> getMappingForTypeArguments(aNode, aAbsDepth-1));
	}

	@Override
	public default String getMappingForEnclosedExpr(EnclosedExpr aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getEnclosedExpression, aAbsDepth,
				() -> getMappingForExpression(aNode.getInner().orElse(null), aAbsDepth-1));
	}

	@Override
	public default String getMappingForArrayInitializerExpr(ArrayInitializerExpr aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getArrayInitExpression, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getValues(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForArrayCreationExpr(ArrayCreationExpr aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getArrayCreateExpression, aAbsDepth,
				() -> getMappingForType(aNode.getElementType(), aAbsDepth-1),
				() -> getMappingForArrayCreationLevelList(aNode.getLevels(), aAbsDepth-1),
				() -> String.valueOf(aNode.getLevels().size()),
				() -> aNode.getInitializer() != null ? getMappingForExpression(aNode.getInitializer().orElse(null), aAbsDepth-1) : "");
	}

	@Override
	public default String getMappingForArrayAccessExpr(ArrayAccessExpr aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getArrayAccessExpression, aAbsDepth,
				() -> getMappingForExpression(aNode.getIndex(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForTypeParameter(TypeParameter aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getTypePar, aAbsDepth,
				() -> getMappingForClassOrInterfaceTypeList(aNode.getTypeBound(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForVariableDeclarator(VariableDeclarator aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getVariableDeclaration, aAbsDepth,
				() -> aNode.getInitializer().isPresent() ? getMappingForExpression(aNode.getInitializer().get(), aAbsDepth-1) : "");
	}

	@Override
	public default String getMappingForImportDeclaration(ImportDeclaration aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getImportDeclaration, aAbsDepth,
				() -> aNode.getNameAsString());
	}

	@Override
	public default String getMappingForPackageDeclaration(PackageDeclaration aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getPackageDeclaration, aAbsDepth,
				() -> aNode.getName().asString());
	}

	@Override
	public default String getMappingForParameter(Parameter aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getParameter, aAbsDepth,
				() -> getMappingForNode(aNode.getType(), aAbsDepth-1),
				() -> getKeyWordProvider().getModifierEnclosed(aNode.getModifiers()));
	}

	@Override
	public default String getMappingForEnumDeclaration(EnumDeclaration aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getEnumDeclaration, aAbsDepth,
				() -> getKeyWordProvider().getModifierEnclosed(aNode.getModifiers()));
	}

	@Override
	public default String getMappingForClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), (aNode.isInterface() ? getKeyWordProvider()::getInterfaceDeclaration : getKeyWordProvider()::getClassDeclaration), aAbsDepth,
				() -> getMappingsForTypeParameterList(aNode.getTypeParameters(), aAbsDepth-1),
				() -> getMappingForClassOrInterfaceTypeList(aNode.getExtendedTypes(), aAbsDepth-1),
				() -> getMappingForClassOrInterfaceTypeList(aNode.getImplementedTypes(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getEnumConstantDeclaration, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getArguments(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForMethodDeclaration(MethodDeclaration aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getMethodDeclaration, aAbsDepth,
				() -> getKeyWordProvider().getModifierEnclosed(aNode.getModifiers()),
				() -> getMappingForType(aNode.getType(), aAbsDepth-1),
				() -> getMappingForParameterList(aNode.getParameters(), aAbsDepth-1),
				() -> getMappingsForTypeParameterList(aNode.getTypeParameters(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForFieldDeclaration(FieldDeclaration aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getFieldDeclaration, aAbsDepth,
				() -> getKeyWordProvider().getModifierEnclosed(aNode.getModifiers()),
				() -> getMappingForType(aNode.getElementType(), aAbsDepth-1),
				() -> getMappingForVariableDeclaratorList(aNode.getVariables(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForConstructorDeclaration(ConstructorDeclaration aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getConstructorDeclaration, aAbsDepth,
				() -> getKeyWordProvider().getModifierEnclosed(aNode.getModifiers()),
				() -> getMappingForParameterList(aNode.getParameters(), aAbsDepth-1),
				() -> getMappingsForTypeParameterList(aNode.getTypeParameters(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForWhileStmt(WhileStmt aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getWhileStatement, aAbsDepth,
				() -> getMappingForExpression(aNode.getCondition(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForSwitchStmt(SwitchStmt aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getSwitchStatement, aAbsDepth,
				() -> getMappingForExpression(aNode.getSelector(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForForStmt(ForStmt aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getForStatement, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getInitialization(), aAbsDepth-1),
				() -> getMappingForExpression(aNode.getCompare().orElse(null), aAbsDepth-1),
				() -> getMappingForExpressionList(aNode.getUpdate(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForForeachStmt(ForeachStmt aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getForEachStatement, aAbsDepth,
				() -> getMappingForVariableDeclarationExpr(aNode.getVariable(), aAbsDepth-1),
				() -> getMappingForExpression(aNode.getIterable(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForExplicitConstructorInvocationStmt(ExplicitConstructorInvocationStmt aNode,
			int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getExplicitConstructorStatement, aAbsDepth,
				() -> (aNode.isThis() ? "this" : "super"),
				() -> getMappingForExpressionList(aNode.getArguments(), aAbsDepth-1),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), aAbsDepth-1));
	}

	@Override
	public default String getMappingForDoStmt(DoStmt aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getDoStatement, aAbsDepth,
				() -> getMappingForExpression(aNode.getCondition(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForAssertStmt(AssertStmt aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getAssertStmt, aAbsDepth,
				() -> getMappingForExpression(aNode.getCheck(), aAbsDepth-1),
				() -> getMappingForExpression(aNode.getMessage().orElse(null), aAbsDepth-1));
	}

	@Override
	public default String getMappingForPrimitiveType(PrimitiveType aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getTypePrimitive, aAbsDepth,
				() -> aNode.getType().asString());
	}

	@Override
	public default String getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getVariableDeclarationExpression, aAbsDepth,
				() -> getKeyWordProvider().getModifierEnclosed(aNode.getModifiers()),
				() -> getMappingForType(aNode.getElementType(), aAbsDepth-1),
				() -> getMappingForVariableDeclaratorList(aNode.getVariables(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForMethodReferenceExpr(MethodReferenceExpr aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getMethodReferenceExpression, aAbsDepth,
				() -> ((aNode.getScope() != null ? getMappingForExpression(aNode.getScope(), aAbsDepth-1) + "::" : "") + aNode.getIdentifier()),
				() -> getMappingForTypeArguments(aNode, aAbsDepth-1));
	}

	@Override
	public default String getMappingForMethodCallExpr(MethodCallExpr aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getMethodCallExpression, aAbsDepth,
				() -> (getPrivMethodBlackList().contains(aNode.getName()) ? getKeyWordProvider().getPrivateMethodCallExpression()
				: (aNode.getScope() != null ? getMappingForExpression(aNode.getScope().orElse(null), aAbsDepth-1) : "")),
				() -> aNode.getNameAsString(),
				() -> getMappingForExpressionList(aNode.getArguments(), aAbsDepth-1),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), aAbsDepth-1));
	}

	@Override
	public default String getMappingForFieldAccessExpr(FieldAccessExpr aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getFieldAccessExpression, aAbsDepth,
				() -> aNode.getNameAsString(),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), aAbsDepth-1));
		// if ( aFieldAccessExpr.getScope() != null ) {
		// result2 += getMappingForNode(aFieldAccessExpr.getScope()) + ".";
		// }
	}

	// TODO: rework implements and extends -> add as simple tokens and push the
	// list traversal in the ASTTokenReader class
	@Override
	public default String getMappingForExtendsStmt(ExtendsStmt aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getExtendsStatement, aAbsDepth,
				() -> getMappingForClassOrInterfaceTypeList(aNode.getExtends(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForImplementsStmt(ImplementsStmt aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getImplementsStatement, aAbsDepth,
				() -> getMappingForClassOrInterfaceTypeList(aNode.getImplements(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForTypeExpr(TypeExpr aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getTypeExpression, aAbsDepth,
				() -> getMappingForType(aNode.getType(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForUnaryExpr(UnaryExpr aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getUnaryExpression, aAbsDepth,
				() -> aNode.getOperator().asString(),
				() -> getMappingForExpression(aNode.getExpression(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForClassExpr(ClassExpr aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getClassExpression, aAbsDepth,
				() -> getMappingForType(aNode.getType(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForCastExpr(CastExpr aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getCastExpression, aAbsDepth,
				() -> getMappingForType(aNode.getType(), aAbsDepth-1),
				() -> getMappingForExpression(aNode.getExpression(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForBinaryExpr(BinaryExpr aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getBinaryExpression, aAbsDepth,
				() -> getMappingForExpression(aNode.getLeft(), aAbsDepth-1),
				() -> aNode.getOperator().asString(),
				() -> getMappingForExpression(aNode.getRight(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForAssignExpr(AssignExpr aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getAssignExpression, aAbsDepth,
				() -> getMappingForExpression(aNode.getTarget(), aAbsDepth-1),
				() -> aNode.getOperator().asString(),
				() -> getMappingForExpression(aNode.getValue(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getDoubleLiteralExpression, aAbsDepth,
				() -> aNode.getValue());
	}

	@Override
	public default String getMappingForLongLiteralExpr(LongLiteralExpr aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getLongLiteralExpression, aAbsDepth,
				() -> aNode.getValue());
	}

	@Override
	public default String getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getIntegerLiteralExpression, aAbsDepth,
				() -> aNode.getValue());
	}

	@Override
	public default String getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getBooleanLiteralExpression, aAbsDepth,
				() -> (aNode.getValue() ? "true" : "false"));
	}

	@Override
	public default String getMappingForIfStmt(IfStmt aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getIfStatement, aAbsDepth,
				() -> getMappingForExpression(aNode.getCondition(), aAbsDepth-1));
	}
	
	@Override
	default String getMappingForLocalClassDeclarationStmt(LocalClassDeclarationStmt aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getLocalClassDeclarationStmt, aAbsDepth,
				() -> getMappingForClassOrInterfaceDeclaration(aNode.getClassDeclaration(), aAbsDepth-1));
	}

	@Override
	default String getMappingForArrayType(ArrayType aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getArrayType, aAbsDepth,
				() -> getMappingForType(aNode.getComponentType(), aAbsDepth-1));
	}

	@Override
	default String getMappingForArrayCreationLevel(ArrayCreationLevel aNode, int aAbsDepth) {
		return applyCombination(getKeyWordProvider(), getKeyWordProvider()::getArrayCreationLevel, aAbsDepth,
				() -> getMappingForExpression(aNode.getDimension().orElse(null), aAbsDepth-1));
	}
	

	// Here are some special cases that will always only consist of their
	// keyword but I need to overwrite the simple mapper anyway to get the group
	// brackets
	@Override
	public default String getMappingForInitializerDeclaration(InitializerDeclaration aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getInitializerDeclaration);
	}

	@Override
	public default String getMappingForThrowStmt(ThrowStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getThrowStatement);
	}

	@Override
	public default String getMappingForNameExpr(NameExpr aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getNameExpression);
	}

	@Override
	public default String getMappingForTryStmt(TryStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getTryStatement);
	}

	@Override
	public default String getMappingForMethodBodyStmt(BodyStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getBodyStmt);
	}

	@Override
	public default String getMappingForStringLiteralExpr(StringLiteralExpr aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getStringLiteralExpression);
	}

	@Override
	public default String getMappingForCharLiteralExpr(CharLiteralExpr aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getCharLiteralExpression);
	}

	@Override
	public default String getMappingForNullLiteralExpr(NullLiteralExpr aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getNullLiteralExpression);
	}

	@Override
	public default String getMappingForThisExpr(ThisExpr aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getThisExpression);
	}

	@Override
	public default String getMappingForBlockComment(BlockComment aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getBlockComment);
	}

	@Override
	public default String getMappingForExpressionStmt(ExpressionStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getExpressionStatement);
	}

	@Override
	public default String getMappingForSuperExpr(SuperExpr aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getSuperExpression);
	}

	@Override
	public default String getMappingForReturnStmt(ReturnStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getReturnStatement);
	}

	@Override
	public default String getMappingForLabeledStmt(LabeledStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getLabeledStatement);
	}

	@Override
	public default String getMappingForThrowsStmt(ThrowsStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getThrowsStatement);
	}

	@Override
	public default String getMappingForElseStmt(ElseStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getElseStatement);
	}

	@Override
	public default String getMappingForBreakStmt(BreakStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getBreak);
	}

	@Override
	public default String getMappingForSingleMemberAnnotationExpr(SingleMemberAnnotationExpr aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getSingleMemberAnnotationExpression);
	}

	@Override
	public default String getMappingForNormalAnnotationExpr(NormalAnnotationExpr aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getNormalAnnotationExpression);
	}

	@Override
	public default String getMappingForMarkerAnnotationExpr(MarkerAnnotationExpr aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getMarkerAnnotationExpression);
	}

	@Override
	public default String getMappingForWildcardType(WildcardType aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getTypeWildcard);
	}

	@Override
	public default String getMappingForVoidType(VoidType aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getTypeVoid);
	}

	@Override
	public default String getMappingForUnknownType(UnknownType aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getTypeUnknown);
	}

	@Override
	public default String getMappingForBlockStmt(BlockStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getBlockStatement);
	}

	@Override
	public default String getMappingForContinueStmt(ContinueStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getContinueStatement);
	}

	@Override
	public default String getMappingForSynchronizedStmt(SynchronizedStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getSynchronizedStatement);
	}

	@Override
	public default String getMappingForCatchClause(CatchClause aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getCatchClauseStatement);
	}
	
	@Override
	public default String getMappingForCompilationUnit(CompilationUnit aNode, int aDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getCompilationUnit);
	}
	
	@Override
	public default String getMappingForAnnotationDeclaration(AnnotationDeclaration aNode, int aDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getAnnotationDeclaration);
	}

	@Override
	public default String getMappingForAnnotationMemberDeclaration(AnnotationMemberDeclaration aNode, int aDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getAnnotationMemberDeclaration);
	}
	
	@Override
	public default String getMappingForJavadocComment(JavadocComment aNode, int aDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getJavadocComment);
	}
	
	@Override
	public default String getMappingForLineComment(LineComment aNode, int aDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getLineComment);
	}
	
	@Override
	default String getMappingForName(Name aNode, int aDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getName);
	}

	@Override
	default String getMappingForSimpleName(SimpleName aNode, int aDepth) {
		return combineData2String(getKeyWordProvider(), getKeyWordProvider()::getSimpleName);
	}

	@Override
	default String getMappingForUnknownNode(Node aNode, int aDepth) {
		return combineData2String(getKeyWordProvider(), () -> getKeyWordProvider().getUnknown(aNode));
	}
	
}
