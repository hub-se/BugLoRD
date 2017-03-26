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
	public default String combineData2String(String aIdentifier, String... aTokens) {
		// in contrast to the other methods i decided to use a StringBuilder
		// here because we will have more tokens
		StringBuilder result = new StringBuilder();
		result.append( getKeyWordProvider().getBigGroupStart() );
		result.append(aIdentifier);

		if (aTokens == null || aTokens.length == 0) {
			result.append(getKeyWordProvider().getBigGroupEnd());
			return result.toString();
		}

		// fix the tokens that did not get the child group brackets
		String[] fixedTokens = new String[aTokens.length];

		for (int i = 0; i < aTokens.length; ++i) {
			String fixedT = aTokens[i];
			// startsWith with chars
			if( fixedT == null || fixedT.length() == 0 ) {
				fixedT = "" + getKeyWordProvider().getGroupStart() + getKeyWordProvider().getGroupEnd();
			} else if (!(fixedT.charAt(0) == getKeyWordProvider().getGroupStart())) {
				fixedT = getKeyWordProvider().getGroupStart() + fixedT + getKeyWordProvider().getGroupEnd();
			}

			fixedTokens[i] = fixedT;
		}

		// there are some data to be put into the string
		result.append(getKeyWordProvider().getIdMarker());

		// String.join does not work for chars :(
		for (int i = 0; i < fixedTokens.length; ++i) {
			result.append(fixedTokens[i]);
			if (i < fixedTokens.length - 1) {
				// there is more to come
				result.append(getKeyWordProvider().getSplit());
			}
		}

		result.append(getKeyWordProvider().getBigGroupEnd());

		return result.toString();
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
	
//	public default String applyCombination(Supplier<String> getKeyWord, int aAbsDepth, String... mappings) {
//		if (aAbsDepth == 0) { // maximum abstraction
//			return combineData2String(getKeyWord.get());
//		} else { // still at a higher level of abstraction (either negative or greater than 0)
//			--aAbsDepth;
//			return combineData2String(getKeyWord.get(), mappings);
//		}
//	}

	@Override
	public default String getMappingForMemberValuePair(MemberValuePair aNode, int aAbsDepth) {
//		applyCombination(getKeyWordProvider()::getMemberValuePair, aAbsDepth, 
//				aNode.getNameAsString(), 
//				getMappingForExpression(aNode.getValue(), aAbsDepth-1));
		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getMemberValuePair());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String name = aNode.getNameAsString();
		String expr = getMappingForExpression(aNode.getValue(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getMemberValuePair(), name, expr);
	}

	@Override
	public default String getMappingForSwitchEntryStmt(SwitchEntryStmt aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getSwitchEntryStatement());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String label = getMappingForExpression(aNode.getLabel().orElse(null), aAbsDepth);

		return combineData2String(getKeyWordProvider().getSwitchEntryStatement(), label);
	}

	@Override
	public default String getMappingForUnionType(UnionType aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getTypeUnion());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}
		return combineData2String(getKeyWordProvider().getTypeUnion(), getMappingForTypeList(aNode.getElements(), aAbsDepth));
	}

	@Override
	public default String getMappingForIntersectionType(IntersectionType aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getTypeIntersection());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}
		return combineData2String(getKeyWordProvider().getTypeIntersection(), getMappingForTypeList(aNode.getElements(), aAbsDepth));
	}

	@Override
	public default String getMappingForLambdaExpr(LambdaExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getLambdaExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String parEnclosed = (aNode.isEnclosingParameters() ? "true" : "false");
		String parList = getMappingForParameterList(aNode.getParameters(), aAbsDepth);
		String stmt = getMappingForStatement(aNode.getBody(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getLambdaExpression(), parEnclosed, parList, stmt);
	}

	@Override
	public default String getMappingForInstanceOfExpr(InstanceOfExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getInstanceofExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String expr = getMappingForExpression(aNode.getExpression(), aAbsDepth);
		String type = getMappingForType(aNode.getType(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getInstanceofExpression(), expr, type);
	}

	@Override
	public default String getMappingForConditionalExpr(ConditionalExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getConditionalExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String condExpr = getMappingForExpression(aNode.getCondition(), aAbsDepth);
		String thenExpr = getMappingForExpression(aNode.getThenExpr(), aAbsDepth);
		String elseExpr = getMappingForExpression(aNode.getElseExpr(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getConditionalExpression(), condExpr, thenExpr, elseExpr);
	}

	@Override
	public default String getMappingForObjectCreationExpr(ObjectCreationExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getObjCreateExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String scope = getMappingForScope(aNode.getScope().orElse(null), aAbsDepth);
		String ci_type = getMappingForClassOrInterfaceType(aNode.getType(), aAbsDepth);
		String type_list = getMappingForTypeList(aNode.getTypeArguments().orElse(null), aAbsDepth);
		String expr_list = getMappingForExpressionList(aNode.getArguments(), aAbsDepth);
		String body_dec_list = getMappingForBodyDeclarationList(aNode.getAnonymousClassBody().orElse(null), aAbsDepth);

		return combineData2String(getKeyWordProvider().getObjCreateExpression(), scope, ci_type, type_list, expr_list, body_dec_list);
	}

	@Override
	public default String getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getClassOrInterfaceType());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String scope = getMappingForScope(aNode);
		String tArgs = getMappingForTypeArguments(aNode, aAbsDepth);

		return combineData2String(getKeyWordProvider().getClassOrInterfaceType(), scope, tArgs);
	}

	@Override
	public default String getMappingForEnclosedExpr(EnclosedExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getEnclosedExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}
		return combineData2String(getKeyWordProvider().getEnclosedExpression(), getMappingForExpression(aNode.getInner().orElse(null), aAbsDepth));
	}

	@Override
	public default String getMappingForArrayInitializerExpr(ArrayInitializerExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getArrayInitExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}
		return combineData2String(getKeyWordProvider().getArrayInitExpression(),
				getMappingForExpressionList(aNode.getValues(), aAbsDepth));
	}

	@Override
	public default String getMappingForArrayCreationExpr(ArrayCreationExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getArrayCreateExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String type = getMappingForType(aNode.getElementType(), aAbsDepth);
		String levels = getMappingForArrayCreationLevelList(aNode.getLevels(), aAbsDepth);
		String levelCount = String.valueOf(aNode.getLevels().size());
		String init = aNode.getInitializer() != null
				? getMappingForExpression(aNode.getInitializer().orElse(null), aAbsDepth) : "";

		return combineData2String(getKeyWordProvider().getArrayCreateExpression(), type, levels, levelCount, init);
	}

	@Override
	public default String getMappingForArrayAccessExpr(ArrayAccessExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getArrayAccessExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}
		return combineData2String(getKeyWordProvider().getArrayAccessExpression(),
				getMappingForExpression(aNode.getIndex(), aAbsDepth));
	}

	@Override
	public default String getMappingForTypeParameter(TypeParameter aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getTypePar());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String ci_typeList = getMappingForClassOrInterfaceTypeList(aNode.getTypeBound(), aAbsDepth);

		return combineData2String(combineData2String(getKeyWordProvider().getTypePar(), ci_typeList));
	}

	@Override
	public default String getMappingForVariableDeclarator(VariableDeclarator aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getVariableDeclaration());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String init = aNode.getInitializer().isPresent() ? 
				getMappingForExpression(aNode.getInitializer().get(), aAbsDepth) : "";

		return combineData2String(getKeyWordProvider().getVariableDeclaration(), init);
	}

	@Override
	public default String getMappingForImportDeclaration(ImportDeclaration aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getImportDeclaration());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}
		return combineData2String(getKeyWordProvider().getImportDeclaration(), aNode.getNameAsString());
	}

	@Override
	public default String getMappingForPackageDeclaration(PackageDeclaration aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getPackageDeclaration());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}
		return combineData2String(getKeyWordProvider().getPackageDeclaration(), aNode.getName().asString());
	}

	@Override
	public default String getMappingForParameter(Parameter aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getParameter());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String type = getMappingForNode(aNode.getType(), aAbsDepth);
		String mods = getKeyWordProvider().getModifierEnclosed(aNode.getModifiers());

		return combineData2String(getKeyWordProvider().getParameter(), type, mods);
	}

	@Override
	public default String getMappingForEnumDeclaration(EnumDeclaration aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getEnumDeclaration());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String mods = getKeyWordProvider().getModifierEnclosed(aNode.getModifiers());

		return combineData2String(getKeyWordProvider().getEnumDeclaration(), mods);
	}

	@Override
	public default String getMappingForClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			if (aNode.isInterface()) {
				return combineData2String(getKeyWordProvider().getInterfaceDeclaration());
			} else {
				return combineData2String(getKeyWordProvider().getClassDeclaration());
			}
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String tPars = getMappingsForTypeParameterList(aNode.getTypeParameters(), aAbsDepth);
		String extendsList = getMappingForClassOrInterfaceTypeList(aNode.getExtendedTypes(), aAbsDepth);
		String implementsList = getMappingForClassOrInterfaceTypeList(aNode.getImplementedTypes(), aAbsDepth);

		if (aNode.isInterface()) {
			return combineData2String(getKeyWordProvider().getInterfaceDeclaration(), tPars, extendsList, implementsList);
		} else {
			return combineData2String(getKeyWordProvider().getClassDeclaration(), tPars, extendsList, implementsList);
		}
	}

	@Override
	public default String getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getEnumConstantDeclaration());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String exprList = getMappingForExpressionList(aNode.getArguments(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getEnumConstantDeclaration(), exprList);
	}

	@Override
	public default String getMappingForMethodDeclaration(MethodDeclaration aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getMethodDeclaration());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String mods = getKeyWordProvider().getModifierEnclosed(aNode.getModifiers());
		String type = getMappingForType(aNode.getType(), aAbsDepth);
		String pars = getMappingForParameterList(aNode.getParameters(), aAbsDepth);
		String tPars = getMappingsForTypeParameterList(aNode.getTypeParameters(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getMethodDeclaration(), mods, type, pars, tPars);
	}

	@Override
	public default String getMappingForFieldDeclaration(FieldDeclaration aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getFieldDeclaration());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String mods = getKeyWordProvider().getModifierEnclosed(aNode.getModifiers());
		String type = getMappingForType(aNode.getElementType(), aAbsDepth);
		String varDecList = getMappingForVariableDeclaratorList(aNode.getVariables(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getFieldDeclaration(), mods, type, varDecList);
	}

	@Override
	public default String getMappingForConstructorDeclaration(ConstructorDeclaration aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getConstructorDeclaration());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String mods = getKeyWordProvider().getModifierEnclosed(aNode.getModifiers());
		String pars = getMappingForParameterList(aNode.getParameters(), aAbsDepth);
		String typePars = getMappingsForTypeParameterList(aNode.getTypeParameters(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getConstructorDeclaration(), mods, pars, typePars);
	}

	@Override
	public default String getMappingForWhileStmt(WhileStmt aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getWhileStatement());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String expr = getMappingForExpression(aNode.getCondition(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getWhileStatement(), expr);
	}

	@Override
	public default String getMappingForSwitchStmt(SwitchStmt aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getSwitchStatement());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String expr = getMappingForExpression(aNode.getSelector(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getSwitchStatement(), expr);
	}

	@Override
	public default String getMappingForForStmt(ForStmt aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getForStatement());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String init = getMappingForExpressionList(aNode.getInitialization(), aAbsDepth);
		String compare = getMappingForExpression(aNode.getCompare().orElse(null), aAbsDepth);
		String update = getMappingForExpressionList(aNode.getUpdate(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getForStatement(), init, compare, update);
	}

	@Override
	public default String getMappingForForeachStmt(ForeachStmt aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getForEachStatement());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String var = getMappingForVariableDeclarationExpr(aNode.getVariable(), aAbsDepth);
		String iterable = getMappingForExpression(aNode.getIterable(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getForEachStatement(), var, iterable);
	}

	@Override
	public default String getMappingForExplicitConstructorInvocationStmt(ExplicitConstructorInvocationStmt aNode,
			int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getExplicitConstructorStatement());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String thisOrSuper = aNode.isThis() ? "this" : "super";
		String exprList = getMappingForExpressionList(aNode.getArguments(), aAbsDepth);
		String typeList = getMappingForTypeList(aNode.getTypeArguments().orElse(null), aAbsDepth);

		return combineData2String(getKeyWordProvider().getExplicitConstructorStatement(), thisOrSuper, exprList, typeList);
	}

	@Override
	public default String getMappingForDoStmt(DoStmt aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getDoStatement());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String expr = getMappingForExpression(aNode.getCondition(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getDoStatement(), expr);
	}

	@Override
	public default String getMappingForAssertStmt(AssertStmt aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getAssertStmt());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String expr = getMappingForExpression(aNode.getCheck(), aAbsDepth);
		// TODO should the message really be part of the assert token? I really
		// doubt it
		String msg = getMappingForExpression(aNode.getMessage().orElse(null), aAbsDepth);

		return combineData2String(getKeyWordProvider().getAssertStmt(), expr, msg);
	}

	@Override
	public default String getMappingForPrimitiveType(PrimitiveType aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getTypePrimitive());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String type = aNode.getType().asString();

		return combineData2String(getKeyWordProvider().getTypePrimitive(), type);
	}

	@Override
	public default String getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getVariableDeclarationExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String mods = getKeyWordProvider().getModifierEnclosed(aNode.getModifiers());
		String types = getMappingForType(aNode.getElementType(), aAbsDepth);
		String varDecList = getMappingForVariableDeclaratorList(aNode.getVariables(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getVariableDeclarationExpression(), mods, types, varDecList);
	}

	@Override
	public default String getMappingForMethodReferenceExpr(MethodReferenceExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getMethodReferenceExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String scope = ((aNode.getScope() != null ? getMappingForExpression(aNode.getScope(), aAbsDepth) + "::" : "")
				+ aNode.getIdentifier());

		String tArgs = getMappingForTypeArguments(aNode, aAbsDepth);

		return combineData2String(getKeyWordProvider().getMethodReferenceExpression(), scope, tArgs);
	}

	@Override
	public default String getMappingForMethodCallExpr(MethodCallExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getMethodCallExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}
		String method = "";
		if (getPrivMethodBlackList().contains(aNode.getName())) {
			method += getKeyWordProvider().getPrivateMethodCallExpression();
		} else {
			if (aNode.getScope() != null) {
				method += getMappingForExpression(aNode.getScope().orElse(null), aAbsDepth);
			}
		}

		String name = aNode.getNameAsString();
		String exprList = getMappingForExpressionList(aNode.getArguments(), aAbsDepth);
		String typeList = getMappingForTypeList(aNode.getTypeArguments().orElse(null), aAbsDepth);

		return combineData2String(getKeyWordProvider().getMethodCallExpression(), method, name, exprList, typeList);
	}

	@Override
	public default String getMappingForFieldAccessExpr(FieldAccessExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getFieldAccessExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String nameExpr = aNode.getNameAsString();
		String typeList = getMappingForTypeList(aNode.getTypeArguments().orElse(null), aAbsDepth);

		return combineData2String(getKeyWordProvider().getFieldAccessExpression(), nameExpr, typeList);

		// if ( aFieldAccessExpr.getScope() != null ) {
		// result2 += getMappingForNode(aFieldAccessExpr.getScope()) + ".";
		// }
	}

	// TODO: rework implements and extends -> add as simple tokens and push the
	// list traversal in the ASTTokenReader class
	@Override
	public default String getMappingForExtendsStmt(ExtendsStmt aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getExtendsStatement());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		// TODO what is this?
		// mappings.addAll(getMappingsForClassOrInterfaceTypeList(aNode.getExtends(),
		// aAbsDepth).getMappings());
		String extendsStr = getMappingForClassOrInterfaceTypeList(aNode.getExtends(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getExtendsStatement(), extendsStr);
	}

	@Override
	public default String getMappingForImplementsStmt(ImplementsStmt aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getImplementsStatement());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		// mappings.addAll(getMappingsForClassOrInterfaceTypeList(aNode.getImplements(),
		// aAbsDepth).getMappings());
		String implementsStr = getMappingForClassOrInterfaceTypeList(aNode.getImplements(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getImplementsStatement(), implementsStr);
	}

	@Override
	public default String getMappingForTypeExpr(TypeExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getTypeExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String type = getMappingForType(aNode.getType(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getTypeExpression(), type);
	}

	@Override
	public default String getMappingForUnaryExpr(UnaryExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getUnaryExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String operator = aNode.getOperator().asString();
		String expr = getMappingForExpression(aNode.getExpression(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getUnaryExpression(), operator, expr);
	}

	@Override
	public default String getMappingForClassExpr(ClassExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getClassExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String type = getMappingForType(aNode.getType(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getClassExpression(), type);
	}

	@Override
	public default String getMappingForCastExpr(CastExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getCastExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String type = getMappingForType(aNode.getType(), aAbsDepth);
		String expr = getMappingForExpression(aNode.getExpression(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getCastExpression(), type, expr);
	}

	@Override
	public default String getMappingForBinaryExpr(BinaryExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getBinaryExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String exprLeft = getMappingForExpression(aNode.getLeft(), aAbsDepth);
		String operator = aNode.getOperator().asString();
		String exprRight = getMappingForExpression(aNode.getRight(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getBinaryExpression(), exprLeft, operator, exprRight);
	}

	@Override
	public default String getMappingForAssignExpr(AssignExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getAssignExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String exprTar = getMappingForExpression(aNode.getTarget(), aAbsDepth);
		String operator = aNode.getOperator().asString();
		String exprValue = getMappingForExpression(aNode.getValue(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getAssignExpression(), exprTar, operator, exprValue);
	}

	@Override
	public default String getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getDoubleLiteralExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String value = aNode.getValue();

		return combineData2String(getKeyWordProvider().getDoubleLiteralExpression(), value);
	}

	@Override
	public default String getMappingForLongLiteralExpr(LongLiteralExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getLongLiteralExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String value = aNode.getValue();

		return combineData2String(getKeyWordProvider().getLongLiteralExpression(), value);
	}

	@Override
	public default String getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getIntegerLiteralExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String value = aNode.getValue();

		return combineData2String(getKeyWordProvider().getIntegerLiteralExpression(), value);
	}

	@Override
	public default String getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getBooleanLiteralExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String value = String.valueOf(aNode.getValue());

		return combineData2String(getKeyWordProvider().getBooleanLiteralExpression(), value);
	}

	@Override
	public default String getMappingForIfStmt(IfStmt aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getIfStatement());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String cond = getMappingForExpression(aNode.getCondition(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getIfStatement(), cond);
	}
	
	@Override
	default String getMappingForLocalClassDeclarationStmt(LocalClassDeclarationStmt aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getLocalClassDeclarationStmt());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String classDec = getMappingForClassOrInterfaceDeclaration(aNode.getClassDeclaration(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getLocalClassDeclarationStmt(), classDec);
	}

	@Override
	default String getMappingForArrayType(ArrayType aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getArrayType());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String componentType = getMappingForType(aNode.getComponentType(), aAbsDepth);

		return combineData2String(getKeyWordProvider().getArrayType(), componentType);
	}

	@Override
	default String getMappingForArrayCreationLevel(ArrayCreationLevel aNode, int aAbsDepth) {
		
		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getKeyWordProvider().getArrayCreationLevel());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String dimension = getMappingForExpression(aNode.getDimension().orElse(null), aAbsDepth);

		return combineData2String(getKeyWordProvider().getArrayCreationLevel(), dimension);
	}
	

	// Here are some special cases that will always only consist of their
	// keyword but I need to overwrite the simple mapper anyway to get the group
	// brackets
	@Override
	public default String getMappingForInitializerDeclaration(InitializerDeclaration aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getInitializerDeclaration());
	}

	@Override
	public default String getMappingForThrowStmt(ThrowStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getThrowStatement());
	}

	@Override
	public default String getMappingForNameExpr(NameExpr aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getNameExpression());
	}

	@Override
	public default String getMappingForTryStmt(TryStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getTryStatement());
	}

	@Override
	public default String getMappingForMethodBodyStmt(BodyStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getBodyStmt());
	}

	@Override
	public default String getMappingForStringLiteralExpr(StringLiteralExpr aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getStringLiteralExpression());
	}

	@Override
	public default String getMappingForCharLiteralExpr(CharLiteralExpr aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getCharLiteralExpression());
	}

	@Override
	public default String getMappingForNullLiteralExpr(NullLiteralExpr aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getNullLiteralExpression());
	}

	@Override
	public default String getMappingForThisExpr(ThisExpr aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getThisExpression());
	}

	@Override
	public default String getMappingForBlockComment(BlockComment aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getBlockComment());
	}

	@Override
	public default String getMappingForExpressionStmt(ExpressionStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getExpressionStatement());
	}

	@Override
	public default String getMappingForSuperExpr(SuperExpr aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getSuperExpression());
	}

	@Override
	public default String getMappingForReturnStmt(ReturnStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getReturnStatement());
	}

	@Override
	public default String getMappingForLabeledStmt(LabeledStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getLabeledStatement());
	}

	@Override
	public default String getMappingForThrowsStmt(ThrowsStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getThrowsStatement());
	}

	@Override
	public default String getMappingForElseStmt(ElseStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getElseStatement());
	}

	@Override
	public default String getMappingForBreakStmt(BreakStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getBreak());
	}

	@Override
	public default String getMappingForSingleMemberAnnotationExpr(SingleMemberAnnotationExpr aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getSingleMemberAnnotationExpression());
	}

	@Override
	public default String getMappingForNormalAnnotationExpr(NormalAnnotationExpr aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getNormalAnnotationExpression());
	}

	@Override
	public default String getMappingForMarkerAnnotationExpr(MarkerAnnotationExpr aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getMarkerAnnotationExpression());
	}

	@Override
	public default String getMappingForWildcardType(WildcardType aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getTypeWildcard());
	}

	@Override
	public default String getMappingForVoidType(VoidType aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getTypeVoid());
	}

	@Override
	public default String getMappingForUnknownType(UnknownType aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getTypeUnknown());
	}

	@Override
	public default String getMappingForBlockStmt(BlockStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getBlockStatement());
	}

	@Override
	public default String getMappingForContinueStmt(ContinueStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getContinueStatement());
	}

	@Override
	public default String getMappingForSynchronizedStmt(SynchronizedStmt aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getSynchronizedStatement());
	}

	@Override
	public default String getMappingForCatchClause(CatchClause aNode, int aAbsDepth) {
		return combineData2String(getKeyWordProvider().getCatchClauseStatement());
	}
	
	@Override
	public default String getMappingForCompilationUnit(CompilationUnit aNode, int aDepth) {
		return combineData2String(getKeyWordProvider().getCompilationUnit());
	}
	
	@Override
	public default String getMappingForAnnotationDeclaration(AnnotationDeclaration aNode, int aDepth) {
		return combineData2String(getKeyWordProvider().getAnnotationDeclaration());
	}

	@Override
	public default String getMappingForAnnotationMemberDeclaration(AnnotationMemberDeclaration aNode, int aDepth) {
		return combineData2String(getKeyWordProvider().getAnnotationMemberDeclaration());
	}
	
	@Override
	public default String getMappingForJavadocComment(JavadocComment aNode, int aDepth) {
		return combineData2String(getKeyWordProvider().getJavadocComment());
	}
	
	@Override
	public default String getMappingForLineComment(LineComment aNode, int aDepth) {
		return combineData2String(getKeyWordProvider().getLineComment());
	}
	
	@Override
	default String getMappingForName(Name aNode, int aDepth) {
		return combineData2String(getKeyWordProvider().getName());
	}

	@Override
	default String getMappingForSimpleName(SimpleName aNode, int aDepth) {
		return combineData2String(getKeyWordProvider().getSimpleName());
	}

	@Override
	default String getMappingForUnknownNode(Node aNode, int aDepth) {
		return combineData2String(getKeyWordProvider().getUnknown(aNode));
	}
	
}
