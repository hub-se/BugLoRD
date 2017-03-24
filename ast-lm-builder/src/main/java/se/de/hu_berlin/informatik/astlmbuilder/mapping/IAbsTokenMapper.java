package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import java.util.Collection;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.TypeArguments;
import com.github.javaparser.ast.TypeParameter;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EmptyMemberDeclaration;
import com.github.javaparser.ast.body.EmptyTypeDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.MultiTypeParameter;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
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
import com.github.javaparser.ast.expr.IntegerLiteralMinValueExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralMinValueExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.QualifiedNameExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
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
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.TypeDeclarationStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.BodyStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ElseStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ExtendsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ImplementsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ThrowsStmt;

public interface IAbsTokenMapper extends ITokenMapper {
	
	/**
	 * Passes a black list of method names to the mapper.
	 * 
	 * @param aBL
	 *            a collection of method names that should be handled
	 *            differently
	 */
	public void setPrivMethodBlackList(Collection<String> aBL);

	/**
	 * Clears the black list of method names from this mapper
	 */
	public void clearPrivMethodBlackList();

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
		result.append( BIG_GROUP_START );
		result.append(aIdentifier);

		if (aTokens == null || aTokens.length == 0) {
			result.append(BIG_GROUP_END);
			return result.toString();
		}

		// fix the tokens that did not get the child group brackets
		String[] fixedTokens = new String[aTokens.length];

		for (int i = 0; i < aTokens.length; ++i) {
			String fixedT = aTokens[i];
			// startsWith with chars
			if( fixedT.length() == 0 ) {
				fixedT = "" + GROUP_START + GROUP_END;
			} else if (!(fixedT.charAt(0) == GROUP_START)) {
				fixedT = GROUP_START + fixedT + GROUP_END;
			}

			fixedTokens[i] = fixedT;
		}

		// there are some data to be put into the string
		result.append(ID_MARKER);

		// String.join does not work for chars :(
		for (int i = 0; i < fixedTokens.length; ++i) {
			result.append(fixedTokens[i]);
			if (i < fixedTokens.length - 1) {
				// there is more to come
				result.append(SPLIT);
			}
		}

		result.append(BIG_GROUP_END);

		return result.toString();
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
		String result = "" + GROUP_START;

		if (vars != null && !vars.isEmpty()) {

			// first element has no leading split
			result += getMappingForVariableDeclarator(vars.get(0), aAbsDepth);

			for (int i = 1; i < vars.size(); ++i) {
				result += SPLIT + getMappingForVariableDeclarator(vars.get(i), aAbsDepth);
			}

		}

		return result + GROUP_END;
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
		String result = "" + GROUP_START;

		if (types != null && !types.isEmpty()) {

			// first element again
			result += getMappingForType(types.get(0), aAbsDepth);

			for (int i = 1; i < types.size(); ++i) {
				result += SPLIT + getMappingForType(types.get(i), aAbsDepth);
			}

		}

		return result + GROUP_END;
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
			return GROUP_START + getMappingForExpression(scope, aAbsDepth) + GROUP_END;
		} else {
			return GROUP_START + "" + GROUP_END;
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
	public default String getMappingForTypeArguments(TypeArguments typeArguments, int aAbsDepth) {
		if (typeArguments != null && typeArguments.getTypeArguments() != null
				&& !typeArguments.getTypeArguments().isEmpty() && !typeArguments.isUsingDiamondOperator()) {

			List<Type> tArgs = typeArguments.getTypeArguments();
			String result = TYPEARG_START + getMappingForType(typeArguments.getTypeArguments().get(0), aAbsDepth);

			for (int i = 1; i < tArgs.size(); ++i) {
				result += SPLIT + getMappingForType(tArgs.get(i), aAbsDepth);
			}

			result += TYPEARG_END;

			return result;
		} else {
			return TYPEARG_START + "" + TYPEARG_END;
		}
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
		String result = "" + GROUP_START;

		if (parameters != null && !parameters.isEmpty()) {
			result += getMappingForParameter(parameters.get(0), aAbsDepth);

			for (int i = 1; i < parameters.size(); ++i) {
				result += SPLIT + getMappingForParameter(parameters.get(i), aAbsDepth);
			}

		}

		return result + GROUP_END;
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
		String result = "" + GROUP_START;

		if (expressions != null && !expressions.isEmpty()) {
			result += getMappingForExpression(expressions.get(0), aAbsDepth);
			;

			for (int i = 1; i < expressions.size(); ++i) {
				result += SPLIT + getMappingForExpression(expressions.get(i), aAbsDepth);
			}

		}

		return result + GROUP_END;
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
	public default String getMappingForBodyDeclarationList(List<BodyDeclaration> bodyDeclarations, int aAbsDepth) {
		String result = "" + GROUP_START;

		if (bodyDeclarations != null && !bodyDeclarations.isEmpty()) {
			result += getMappingForBodyDeclaration(bodyDeclarations.get(0), aAbsDepth);

			for (int i = 1; i < bodyDeclarations.size(); ++i) {
				result += SPLIT + getMappingForBodyDeclaration(bodyDeclarations.get(i), aAbsDepth);
			}

		}

		return result + GROUP_END;
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
		String result = "" + GROUP_START;

		if (types != null && !types.isEmpty()) {
			result += getMappingForType(types.get(0), aAbsDepth);

			for (int i = 1; i < types.size(); ++i) {
				result += SPLIT + getMappingForType(types.get(i), aAbsDepth);
			}

		}

		return result + GROUP_END;
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
		String result = "" + GROUP_START;

		if (typeParameters != null && !typeParameters.isEmpty()) {
			result += getMappingForTypeParameter(typeParameters.get(0), aAbsDepth);

			for (int i = 1; i < typeParameters.size(); ++i) {
				result += SPLIT + getMappingForTypeParameter(typeParameters.get(i), aAbsDepth);
			}

		}

		return result + GROUP_END;
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
			return GROUP_START + "" + GROUP_END;
		}

		return GROUP_START + getFullScope(aNode) + GROUP_END;
	}

	/**
	 * Returns the full scope for a given class or interface type
	 * 
	 * @param aNode
	 *            The class or interface type node
	 * @return The scope as part of a token
	 */
	public default String getFullScope(ClassOrInterfaceType aNode) {
		if (aNode.getScope() == null) {
			return aNode.getName();
		} else {
			return getFullScope(aNode.getScope()) + "." + aNode.getName();
		}
	}

	@Override
	public default String getMappingForMemberValuePair(MemberValuePair aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getMemberValuePair());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String name = aNode.getName();
		String expr = getMappingForExpression(aNode.getValue(), aAbsDepth).toString();

		return combineData2String(getMemberValuePair(), name, expr);
	}

	@Override
	public default String getMappingForTypeDeclarationStmt(TypeDeclarationStmt aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getTypeDeclarationStatement());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}
		return combineData2String(getTypeDeclarationStatement(),
				getMappingForTypeDeclaration(aNode.getTypeDeclaration(), aAbsDepth).toString());
	}

	@Override
	public default String getMappingForSwitchEntryStmt(SwitchEntryStmt aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getSwitchEntryStatement());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String label = getMappingForExpression(aNode.getLabel(), aAbsDepth).toString();

		return combineData2String(getSwitchEntryStatement(), label);
	}

	@Override
	public default String getMappingForUnionType(UnionType aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getTypeUnion());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}
		return combineData2String(getTypeUnion(), getMappingForTypeList(aNode.getElements(), aAbsDepth));
	}

	@Override
	public default String getMappingForIntersectionType(IntersectionType aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getTypeIntersection());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}
		return combineData2String(getTypeIntersection(), getMappingForTypeList(aNode.getElements(), aAbsDepth));
	}

	@Override
	public default String getMappingForLambdaExpr(LambdaExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getLambdaExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String parEnclosed = (aNode.isParametersEnclosed() ? "true" : "false");
		String parList = getMappingForParameterList(aNode.getParameters(), aAbsDepth);
		String stmt = getMappingForStatement(aNode.getBody(), aAbsDepth).toString();

		return combineData2String(getLambdaExpression(), parEnclosed, parList, stmt);
	}

	@Override
	public default String getMappingForInstanceOfExpr(InstanceOfExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getInstanceofExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String expr = getMappingForExpression(aNode.getExpr(), aAbsDepth).toString();
		String type = getMappingForType(aNode.getType(), aAbsDepth).toString();

		return combineData2String(getInstanceofExpression(), expr, type);
	}

	@Override
	public default String getMappingForConditionalExpr(ConditionalExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getConditionalExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String condExpr = getMappingForExpression(aNode.getCondition(), aAbsDepth).toString();
		String thenExpr = getMappingForExpression(aNode.getThenExpr(), aAbsDepth).toString();
		String elseExpr = getMappingForExpression(aNode.getElseExpr(), aAbsDepth).toString();

		return combineData2String(getConditionalExpression(), condExpr, thenExpr, elseExpr);
	}

	@Override
	public default String getMappingForObjectCreationExpr(ObjectCreationExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getObjCreateExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String scope = getMappingForScope(aNode.getScope(), aAbsDepth);
		String ci_type = getMappingForClassOrInterfaceType(aNode.getType(), aAbsDepth).toString();
		String type_list = getMappingForTypeList(aNode.getTypeArgs(), aAbsDepth);
		String expr_list = getMappingForExpressionList(aNode.getArgs(), aAbsDepth);
		String body_dec_list = getMappingForBodyDeclarationList(aNode.getAnonymousClassBody(), aAbsDepth);

		return combineData2String(getObjCreateExpression(), scope, ci_type, type_list, expr_list, body_dec_list);
	}

	@Override
	public default String getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getClassOrInterfaceType());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String scope = getMappingForScope(aNode);
		String tArgs = getMappingForTypeArguments(aNode.getTypeArguments(), aAbsDepth);

		return combineData2String(getClassOrInterfaceType(), scope, tArgs);
	}

	@Override
	public default String getMappingForEnclosedExpr(EnclosedExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getEnclosedExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}
		return combineData2String(getEnclosedExpression(), getMappingForExpression(aNode.getInner(), aAbsDepth).toString());
	}

	@Override
	public default String getMappingForArrayInitializerExpr(ArrayInitializerExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getArrayInitExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}
		return combineData2String(getArrayInitExpression(),
				getMappingForExpressionList(aNode.getValues(), aAbsDepth).toString());
	}

	@Override
	public default String getMappingForArrayCreationExpr(ArrayCreationExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getArrayCreateExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String type = getMappingForType(aNode.getType(), aAbsDepth).toString();
		String exprList = getMappingForExpressionList(aNode.getDimensions(), aAbsDepth);
		String arrCount = String.valueOf(aNode.getArrayCount());
		String init = aNode.getInitializer() != null
				? getMappingForArrayInitializerExpr(aNode.getInitializer(), aAbsDepth).toString() : "";

		return combineData2String(getArrayCreateExpression(), type, exprList, arrCount, init);
	}

	@Override
	public default String getMappingForArrayAccessExpr(ArrayAccessExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getArrayAccessExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}
		return combineData2String(getArrayAccessExpression(),
				getMappingForExpression(aNode.getIndex(), aAbsDepth).toString());
	}

	@Override
	public default String getMappingForTypeParameter(TypeParameter aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getTypePar());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String ci_typeList = getMappingForClassOrInterfaceTypeList(aNode.getTypeBound(), aAbsDepth).toString();

		return combineData2String(combineData2String(getTypePar(), ci_typeList));
	}

	@Override
	public default String getMappingForVariableDeclaratorId(VariableDeclaratorId aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getVariableDeclarationId());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String arrayCount = String.valueOf(aNode.getArrayCount());

		return combineData2String(getVariableDeclarationId(), arrayCount);
	}

	@Override
	public default String getMappingForVariableDeclarator(VariableDeclarator aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getVariableDeclaration());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String init = aNode.getInit() != null ? getMappingForExpression(aNode.getInit(), aAbsDepth).toString() : "";

		return combineData2String(getVariableDeclaration(), init);
	}

	@Override
	public default String getMappingForImportDeclaration(ImportDeclaration aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getImportDeclaration());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}
		return combineData2String(getImportDeclaration(), aNode.getName().toString());
	}

	@Override
	public default String getMappingForPackageDeclaration(PackageDeclaration aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getPackageDeclaration());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}
		return combineData2String(getPackageDeclaration(), aNode.getPackageName());
	}

	@Override
	public default String getMappingForMultiTypeParameter(MultiTypeParameter aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getMultiTypeParameter());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String type = getMappingForNode(aNode.getType(), aAbsDepth).toString();
		String mods = getModifierEnclosed(aNode.getModifiers());

		return combineData2String(getMultiTypeParameter(), type, mods);
	}

	@Override
	public default String getMappingForParameter(Parameter aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getParameter());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String type = getMappingForNode(aNode.getType(), aAbsDepth).toString();
		String mods = getModifierEnclosed(aNode.getModifiers());

		return combineData2String(getParameter(), type, mods);
	}

	@Override
	public default String getMappingForEnumDeclaration(EnumDeclaration aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getEnumDeclaration());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String mods = getModifierEnclosed(aNode.getModifiers());

		return combineData2String(getEnumDeclaration(), mods);
	}

	@Override
	public default String getMappingForClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			if (aNode.isInterface()) {
				return combineData2String(getInterfaceDeclaration());
			} else {
				return combineData2String(getClassDeclaration());
			}
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String tPars = getMappingsForTypeParameterList(aNode.getTypeParameters(), aAbsDepth);
		String extendsList = getMappingForClassOrInterfaceTypeList(aNode.getExtends(), aAbsDepth);
		String implementsList = getMappingForClassOrInterfaceTypeList(aNode.getImplements(), aAbsDepth);

		if (aNode.isInterface()) {
			return combineData2String(getInterfaceDeclaration(), tPars, extendsList, implementsList);
		} else {
			return combineData2String(getClassDeclaration(), tPars, extendsList, implementsList);
		}
	}

	@Override
	public default String getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getEnumConstantDeclaration());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String exprList = getMappingForExpressionList(aNode.getArgs(), aAbsDepth);

		return combineData2String(getEnumConstantDeclaration(), exprList);
	}

	@Override
	public default String getMappingForMethodDeclaration(MethodDeclaration aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getMethodDeclaration());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String mods = getModifierEnclosed(aNode.getModifiers());
		String type = getMappingForType(aNode.getType(), aAbsDepth).toString();
		String pars = getMappingForParameterList(aNode.getParameters(), aAbsDepth);
		String tPars = getMappingsForTypeParameterList(aNode.getTypeParameters(), aAbsDepth);

		return combineData2String(getMethodDeclaration(), mods, type, pars, tPars);
	}

	@Override
	public default String getMappingForFieldDeclaration(FieldDeclaration aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getFieldDeclaration());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String mods = getModifierEnclosed(aNode.getModifiers());
		String type = getMappingForType(aNode.getType(), aAbsDepth).toString();
		String varDecList = getMappingForVariableDeclaratorList(aNode.getVariables(), aAbsDepth);

		return combineData2String(getFieldDeclaration(), mods, type, varDecList);
	}

	@Override
	public default String getMappingForConstructorDeclaration(ConstructorDeclaration aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getConstructorDeclaration());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String mods = getModifierEnclosed(aNode.getModifiers());
		String pars = getMappingForParameterList(aNode.getParameters(), aAbsDepth);
		String typePars = getMappingsForTypeParameterList(aNode.getTypeParameters(), aAbsDepth);

		return combineData2String(getConstructorDeclaration(), mods, pars, typePars);
	}

	@Override
	public default String getMappingForWhileStmt(WhileStmt aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getWhileStatement());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String expr = getMappingForExpression(aNode.getCondition(), aAbsDepth).toString();

		return combineData2String(getWhileStatement(), expr);
	}

	@Override
	public default String getMappingForSwitchStmt(SwitchStmt aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getSwitchStatement());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String expr = getMappingForExpression(aNode.getSelector(), aAbsDepth).toString();

		return combineData2String(getSwitchStatement(), expr);
	}

	@Override
	public default String getMappingForForStmt(ForStmt aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getForStatement());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String init = getMappingForExpressionList(aNode.getInit(), aAbsDepth);
		String compare = getMappingForExpression(aNode.getCompare(), aAbsDepth).toString();
		String update = getMappingForExpressionList(aNode.getUpdate(), aAbsDepth);

		return combineData2String(getForStatement(), init, compare, update);
	}

	@Override
	public default String getMappingForForeachStmt(ForeachStmt aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getForEachStatement());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String var = getMappingForVariableDeclarationExpr(aNode.getVariable(), aAbsDepth).toString();
		String iterable = getMappingForExpression(aNode.getIterable(), aAbsDepth).toString();

		return combineData2String(getForEachStatement(), var, iterable);
	}

	@Override
	public default String getMappingForExplicitConstructorInvocationStmt(ExplicitConstructorInvocationStmt aNode,
			int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getExplicitConstructorStatement());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String thisOrSuper = aNode.isThis() ? "this" : "super";
		String exprList = getMappingForExpressionList(aNode.getArgs(), aAbsDepth);
		String typeList = getMappingForTypeList(aNode.getTypeArgs(), aAbsDepth);

		return combineData2String(getExplicitConstructorStatement(), thisOrSuper, exprList, typeList);
	}

	@Override
	public default String getMappingForDoStmt(DoStmt aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getDoStatement());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String expr = getMappingForExpression(aNode.getCondition(), aAbsDepth).toString();

		return combineData2String(getDoStatement(), expr);
	}

	@Override
	public default String getMappingForAssertStmt(AssertStmt aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getAssertStmt());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String expr = getMappingForExpression(aNode.getCheck(), aAbsDepth).toString();
		// TODO should the message really be part of the assert token? I really
		// doubt it
		String msg = getMappingForExpression(aNode.getMessage(), aAbsDepth).toString();

		return combineData2String(getAssertStmt(), expr, msg);
	}

	@Override
	public default String getMappingForReferenceType(ReferenceType aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getTypeReference());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String type = getMappingForType(aNode.getType(), aAbsDepth).toString();

		return combineData2String(getTypeReference(), type);
	}

	@Override
	public default String getMappingForPrimitiveType(PrimitiveType aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getTypePrimitive());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String type = aNode.getType().toString();

		return combineData2String(getTypePrimitive(), type);
	}

	@Override
	public default String getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getVariableDeclarationExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String mods = getModifierEnclosed(aNode.getModifiers());
		String types = getMappingForType(aNode.getType(), aAbsDepth).toString();
		String varDecList = getMappingForVariableDeclaratorList(aNode.getVars(), aAbsDepth);

		return combineData2String(getVariableDeclarationExpression(), mods, types, varDecList);
	}

	@Override
	public default String getMappingForMethodReferenceExpr(MethodReferenceExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getMethodReferenceExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String scope = ((aNode.getScope() != null ? getMappingForExpression(aNode.getScope(), aAbsDepth) + "::" : "")
				+ aNode.getIdentifier());

		String tArgs = getMappingForTypeArguments(aNode.getTypeArguments(), aAbsDepth);

		return combineData2String(getMethodReferenceExpression(), scope, tArgs);
	}

	// this method has to be overwritten by the actual implementation because of the usage
	// of the method list
	@Override
	public default String getMappingForMethodCallExpr(MethodCallExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getMethodCallExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}
		String method = "";
		
		if (aNode.getScope() != null) {
			method += getMappingForExpression(aNode.getScope(), aAbsDepth);
		}
		
//		if (privMethodBL.contains(aNode.getName())) {
//			method += getPrivateMethodCallExpression();
//		} else {
//			if (aNode.getScope() != null) {
//				method += getMappingForExpression(aNode.getScope(), aAbsDepth);
//			}
//		}

		String name = aNode.getName();
		String exprList = getMappingForExpressionList(aNode.getArgs(), aAbsDepth);
		String typeList = getMappingForTypeList(aNode.getTypeArgs(), aAbsDepth);

		return combineData2String(getMethodCallExpression(), method, name, exprList, typeList);
	}

	@Override
	public default String getMappingForFieldAccessExpr(FieldAccessExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getFieldAccessExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String nameExpr = getMappingForNameExpr(aNode.getFieldExpr(), aAbsDepth).toString();
		String typeList = getMappingForTypeList(aNode.getTypeArgs(), aAbsDepth);

		return combineData2String(getFieldAccessExpression(), nameExpr, typeList);

		// if ( aFieldAccessExpr.getScope() != null ) {
		// result2 += getMappingForNode(aFieldAccessExpr.getScope()) + ".";
		// }
	}

	// TODO: rework implements and extends -> add as simple tokens and push the
	// list traversal in the ASTTokenReader class
	@Override
	public default String getMappingForExtendsStmt(ExtendsStmt aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getExtendsStatement());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		// TODO what is this?
		// mappings.addAll(getMappingsForClassOrInterfaceTypeList(aNode.getExtends(),
		// aAbsDepth).getMappings());
		String extendsStr = getMappingForClassOrInterfaceTypeList(aNode.getExtends(), aAbsDepth);

		return combineData2String(getExtendsStatement(), extendsStr);
	}

	@Override
	public default String getMappingForImplementsStmt(ImplementsStmt aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getImplementsStatement());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		// mappings.addAll(getMappingsForClassOrInterfaceTypeList(aNode.getImplements(),
		// aAbsDepth).getMappings());
		String implementsStr = getMappingForClassOrInterfaceTypeList(aNode.getImplements(), aAbsDepth);

		return combineData2String(getImplementsStatement(), implementsStr);
	}

	@Override
	public default String getMappingForTypeExpr(TypeExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getTypeExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String type = getMappingForType(aNode.getType(), aAbsDepth).toString();

		return combineData2String(getTypeExpression(), type);
	}

	@Override
	public default String getMappingForUnaryExpr(UnaryExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getUnaryExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String operator = aNode.getOperator().toString();
		String expr = getMappingForExpression(aNode.getExpr(), aAbsDepth).toString();

		return combineData2String(getUnaryExpression(), operator, expr);
	}

	@Override
	public default String getMappingForClassExpr(ClassExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getClassExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String type = getMappingForType(aNode.getType(), aAbsDepth).toString();

		return combineData2String(getClassExpression(), type);
	}

	@Override
	public default String getMappingForCastExpr(CastExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getCastExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String type = getMappingForType(aNode.getType(), aAbsDepth).toString();
		String expr = getMappingForExpression(aNode.getExpr(), aAbsDepth).toString();

		return combineData2String(getCastExpression(), type, expr);
	}

	@Override
	public default String getMappingForBinaryExpr(BinaryExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getBinaryExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String exprLeft = getMappingForExpression(aNode.getLeft(), aAbsDepth).toString();
		String operator = aNode.getOperator().toString();
		String exprRight = getMappingForExpression(aNode.getRight(), aAbsDepth).toString();

		return combineData2String(getBinaryExpression(), exprLeft, operator, exprRight);
	}

	@Override
	public default String getMappingForAssignExpr(AssignExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getAssignExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String exprTar = getMappingForExpression(aNode.getTarget(), aAbsDepth).toString();
		String operator = aNode.getOperator().toString();
		String exprValue = getMappingForExpression(aNode.getValue(), aAbsDepth).toString();

		return combineData2String(getAssignExpression(), exprTar, operator, exprValue);
	}

	@Override
	public default String getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getDoubleLiteralExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String value = aNode.getValue();

		return combineData2String(getDoubleLiteralExpression(), value);
	}

	@Override
	public default String getMappingForLongLiteralExpr(LongLiteralExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getLongLiteralExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String value = aNode.getValue();

		return combineData2String(getLongLiteralExpression(), value);
	}

	@Override
	public default String getMappingForLongLiteralMinValueExpr(LongLiteralMinValueExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getLongLiteralMinValueExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String value = aNode.getValue();

		return combineData2String(getLongLiteralMinValueExpression(), value);
	}

	@Override
	public default String getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getIntegerLiteralExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String value = aNode.getValue();

		return combineData2String(getIntegerLiteralExpression(), value);
	}

	@Override
	public default String getMappingForIntegerLiteralMinValueExpr(IntegerLiteralMinValueExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getIntegerLiteralMinValueExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String value = aNode.getValue();

		return combineData2String(getIntegerLiteralMinValueExpression(), value);
	}

	@Override
	public default String getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getBooleanLiteralExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String value = String.valueOf(aNode.getValue());

		return combineData2String(getBooleanLiteralExpression(), value);
	}

	@Override
	public default String getMappingForIfStmt(IfStmt aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getIfStatement());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}

		String cond = getMappingForExpression(aNode.getCondition(), aAbsDepth).toString();

		return combineData2String(getIfStatement(), cond);
	}

	// Here are some special cases that will always only consist of their
	// keyword but I need to overwrite the simple mapper anyway to get the group
	// brackets
	@Override
	public default String getMappingForInitializerDeclaration(InitializerDeclaration aNode, int aAbsDepth) {
		return combineData2String(getInitializerDeclaration());
	}

	@Override
	public default String getMappingForThrowStmt(ThrowStmt aNode, int aAbsDepth) {
		return combineData2String(getThrowStatement());
	}

	@Override
	public default String getMappingForNameExpr(NameExpr aNode, int aAbsDepth) {
		return combineData2String(getNameExpression());
	}

	@Override
	public default String getMappingForTryStmt(TryStmt aNode, int aAbsDepth) {
		return combineData2String(getTryStatement());
	}

	@Override
	public default String getMappingForMethodBodyStmt(BodyStmt aNode, int aAbsDepth) {
		return combineData2String(getBodyStmt());
	}

	@Override
	public default String getMappingForStringLiteralExpr(StringLiteralExpr aNode, int aAbsDepth) {
		return combineData2String(getStringLiteralExpression());
	}

	@Override
	public default String getMappingForCharLiteralExpr(CharLiteralExpr aNode, int aAbsDepth) {
		return combineData2String(getCharLiteralExpression());
	}

	@Override
	public default String getMappingForNullLiteralExpr(NullLiteralExpr aNode, int aAbsDepth) {
		return combineData2String(getNullLiteralExpression());
	}

	@Override
	public default String getMappingForThisExpr(ThisExpr aNode, int aAbsDepth) {
		return combineData2String(getThisExpression());
	}

	@Override
	public default String getMappingForBlockComment(BlockComment aNode, int aAbsDepth) {
		return combineData2String(getBlockComment());
	}

	@Override
	public default String getMappingForExpressionStmt(ExpressionStmt aNode, int aAbsDepth) {
		return combineData2String(getExpressionStatement());
	}

	@Override
	public default String getMappingForSuperExpr(SuperExpr aNode, int aAbsDepth) {
		return combineData2String(getSuperExpression());
	}

	@Override
	public default String getMappingForReturnStmt(ReturnStmt aNode, int aAbsDepth) {
		return combineData2String(getReturnStatement());
	}

	@Override
	public default String getMappingForLabeledStmt(LabeledStmt aNode, int aAbsDepth) {
		return combineData2String(getLabeledStatement());
	}

	@Override
	public default String getMappingForThrowsStmt(ThrowsStmt aNode, int aAbsDepth) {
		return combineData2String(getThrowsStatement());
	}

	@Override
	public default String getMappingForElseStmt(ElseStmt aNode, int aAbsDepth) {
		return combineData2String(getElseStatement());
	}

	@Override
	public default String getMappingForBreakStmt(BreakStmt aNode, int aAbsDepth) {
		return combineData2String(getBreak());
	}

	@Override
	public default String getMappingForEmptyTypeDeclaration(EmptyTypeDeclaration aNode, int aAbsDepth) {
		return combineData2String(getEmptyTypeDeclaration());
	}

	@Override
	public default String getMappingForEmptyMemberDeclaration(EmptyMemberDeclaration aNode, int aAbsDepth) {
		return combineData2String(getEmptyMemberDeclaration());
	}

	@Override
	public default String getMappingForEmptyStmt(EmptyStmt aNode, int aAbsDepth) {
		return combineData2String(getEmptyStatement());
	}

	@Override
	public default String getMappingForSingleMemberAnnotationExpr(SingleMemberAnnotationExpr aNode, int aAbsDepth) {
		return combineData2String(getSingleMemberAnnotationExpression());
	}

	@Override
	public default String getMappingForNormalAnnotationExpr(NormalAnnotationExpr aNode, int aAbsDepth) {
		return combineData2String(getNormalAnnotationExpression());
	}

	@Override
	public default String getMappingForMarkerAnnotationExpr(MarkerAnnotationExpr aNode, int aAbsDepth) {
		return combineData2String(getMarkerAnnotationExpression());
	}

	@Override
	public default String getMappingForWildcardType(WildcardType aNode, int aAbsDepth) {
		return combineData2String(getTypeWildcard());
	}

	@Override
	public default String getMappingForVoidType(VoidType aNode, int aAbsDepth) {
		return combineData2String(getTypeVoid());
	}

	@Override
	public default String getMappingForUnknownType(UnknownType aNode, int aAbsDepth) {
		return combineData2String(getTypeUnknown());
	}

	@Override
	public default String getMappingForBlockStmt(BlockStmt aNode, int aAbsDepth) {
		return combineData2String(getBlockStatement());
	}

	@Override
	public default String getMappingForContinueStmt(ContinueStmt aNode, int aAbsDepth) {
		return combineData2String(getContinueStatement());
	}

	@Override
	public default String getMappingForSynchronizedStmt(SynchronizedStmt aNode, int aAbsDepth) {
		return combineData2String(getSynchronizedStatement());
	}

	@Override
	public default String getMappingForCatchClause(CatchClause aNode, int aAbsDepth) {
		return combineData2String(getCatchClauseStatement());
	}
	
	@Override
	public default String getMappingForCompilationUnit(CompilationUnit aNode, int aDepth) {
		return combineData2String(getCompilationUnit());
	}
	
	@Override
	public default String getMappingForAnnotationDeclaration(AnnotationDeclaration aNode, int aDepth) {
		return combineData2String(getAnnotationDeclaration());
	}

	@Override
	public default String getMappingForAnnotationMemberDeclaration(AnnotationMemberDeclaration aNode, int aDepth) {
		return combineData2String(getAnnotationMemberDeclaration());
	}
	
	@Override
	public default String getMappingForQualifiedNameExpr(QualifiedNameExpr aNode, int aDepth) {
		return combineData2String(getQualifiedNameExpression());
	}
	
	@Override
	public default String getMappingForJavadocComment(JavadocComment aNode, int aDepth) {
		return combineData2String(getJavadocComment());
	}
	
	@Override
	public default String getMappingForLineComment(LineComment aNode, int aDepth) {
		return combineData2String(getLineComment());
	}
}
