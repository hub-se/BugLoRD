package se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
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
import com.github.javaparser.ast.modules.ModuleDeclaration;
import com.github.javaparser.ast.modules.ModuleStmt;
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
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.IModifierHandler;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.IOperatorHandler;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.ITypeHandler;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IBasicKeyWords;
import se.de.hu_berlin.informatik.astlmbuilder.nodes.BodyStmt;
import se.de.hu_berlin.informatik.astlmbuilder.nodes.ElseStmt;
import se.de.hu_berlin.informatik.astlmbuilder.nodes.ExtendsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.nodes.ImplementsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.nodes.ThrowsStmt;

public interface IAbstractionMapper extends IMapper<String>, IModifierHandler, IOperatorHandler, ITypeHandler {
	
	public int getMaxListMembers();
	
	public boolean usesStringAndCharAbstraction();
	public boolean usesNumberAbstraction();
	public boolean usesLocalMethodAbstraction();
	public boolean usesMethodNameAbstraction();
	public boolean usesVariableNameAbstraction();
	public boolean usesGenericTypeNameAbstraction();
	public boolean usesClassNameAbstraction();
	public boolean usesPackageAndImportAbstraction();
	public boolean usesAnnotationAbstraction();
	
	
	/**
	 * All tokens will be put together into one string that can be parsed later.
	 * <p> General format for elements with 
	 * <br> maximum abstraction: {@code $node_id}, and
	 * <br> other abstraction level: {@code ($node_id,[member_1],[member_2],...,[member_n])},
	 * <br> where each {@code member_k} is again an element itself.
	 * @param provider
	 * the keyword provider to use
	 * @param aIdentifier
	 *            The keyword of the node
	 * @param aTokens
	 *            All its data blocks
	 * @return A finished string for the language model
	 */
	@SafeVarargs
	public static String combineData2String(Supplier<String> aIdentifier, Supplier<String>... aTokens) {
		// in contrast to the other methods i decided to use a StringBuilder
		// here because we will have more tokens
		StringBuilder result = new StringBuilder();
		result.append( IBasicKeyWords.BIG_GROUP_START );
		result.append(aIdentifier.get());

		if (aTokens != null && aTokens.length != 0) {
			// there are some data to be put into the string
			//result.append(provider.getIdMarker());

			// fix the tokens that did not get the child group brackets
			String[] fixedTokens = new String[aTokens.length];

			for (int i = 0; i < aTokens.length; ++i) {
				String fixedT = aTokens[i].get();
				// startsWith with chars
				if( fixedT == null ) {
					fixedT = "" + IBasicKeyWords.GROUP_START + IBasicKeyWords.KEYWORD_NULL + IBasicKeyWords.GROUP_END;
				} else if( fixedT.length() == 0 ) {
					fixedT = "" + IBasicKeyWords.GROUP_START + IBasicKeyWords.GROUP_END;
				} else if (fixedT.charAt(0) != IBasicKeyWords.GROUP_START) {
					fixedT = IBasicKeyWords.GROUP_START + fixedT + IBasicKeyWords.GROUP_END;
				}

				fixedTokens[i] = fixedT;
			}

			// String.join does not work for chars :(
			for (int i = 0; i < fixedTokens.length; ++i) {
				result.append(IBasicKeyWords.SPLIT);
				result.append(fixedTokens[i]);
			}
		}

		result.append(IBasicKeyWords.BIG_GROUP_END);

		return result.toString();
	}
	
	@SafeVarargs
	public static String applyCombination(Object base, 
			Supplier<String> getKeyWord, int aAbsDepth, Supplier<String>... mappings) {
		if (base == null) {
			return String.valueOf(IBasicKeyWords.KEYWORD_NULL);
		}
		if (aAbsDepth <= 0) { // maximum abstraction
			//return combineData2String(provider, getKeyWord);
			return getKeyWord.get();
		} else { // still at a higher level of abstraction (either negative or greater than 0)
			return combineData2String(getKeyWord, mappings);
		}
	}
	

//	/**
//	 * Creates a mapping for type arguments
//	 * 
//	 * @param typeArguments
//	 *            The type arguments
//	 * @param aAbsDepth
//	 *            The depth of the mapping
//	 * @return A token that represents the mapping with the given depth
//	 */
//	public default String getMappingForTypeArguments(NodeWithTypeArguments<?> typeArguments, int aAbsDepth) {
//		String result = "" + IBasicKeyWords.GROUP_START;
//		
//		if (typeArguments.getTypeArguments().isPresent()) {
//			NodeList<Type> tArgs = typeArguments.getTypeArguments().get();
////			result += getKeyWordProvider().getTypeArgStart();
//			
//			if (!tArgs.isEmpty()) {
//				result += getMappingForType(tArgs.get(0), aAbsDepth);
//
////				int bound = getMaxListMembers() < 0 ? tArgs.size()
////						: Math.min(getMaxListMembers(), tArgs.size()); 
//				for (int i = 1; i < tArgs.size(); ++i) {
//					result += IBasicKeyWords.SPLIT + getMappingForType(tArgs.get(i), aAbsDepth);
//				}
//			}
//
////			result += getKeyWordProvider().getTypeArgEnd();
//		} else {
//			result += IBasicKeyWords.KEYWORD_NULL;
//		}
//		
//		return result + IBasicKeyWords.GROUP_END;
//	}
	
	/**
	 * Creates a mapping for a list of type parameters
	 * @param list
	 * The list of type parameters
	 * @param aAbsDepth
	 * The depth of the mapping
	 * @param getMappingForT
	 * a function that gets a mapping for an object of type T
	 * @param alwaysUseFullList
	 * whether to always get Tokens for the full list
	 * @return
	 * A token that represents the mapping with the given depth
	 * @param <T>
	 * the type of objects in the list
	 */
	public default <T> String getMappingForList(List<T> list, int aAbsDepth, 
			BiFunction<T, Integer, String> getMappingForT, boolean alwaysUseFullList) {
		String result = "" + IBasicKeyWords.GROUP_START;

		if (list == null) { //this should never happen, actually
			result += IBasicKeyWords.KEYWORD_NULL;
		} else if (!list.isEmpty()) {
			result += getMappingForT.apply(list.get(0), aAbsDepth);

			int bound = getMaxListMembers() < 0 ? list.size()
					: Math.min(getMaxListMembers(), list.size());
			if (alwaysUseFullList) {
				bound = list.size();
			}
			for (int i = 1; i < bound; ++i) {
				result += IBasicKeyWords.SPLIT + getMappingForT.apply(list.get(i), aAbsDepth);
			}

		}

		return result + IBasicKeyWords.GROUP_END;
	}
	
	/**
	 * Creates a mapping for a list of general nodes.
	 * @param nodes
	 * the list of nodes that should be mapped
	 * @param aAbsDepth
	 * the depth of the mapping
	 * @param alwaysUseFullList
	 * whether to always get Tokens for the full list
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForNodeList(List<? extends Node> nodes, boolean alwaysUseFullList, int aAbsDepth) {
		return applyCombination(nodes, () -> String.valueOf(IBasicKeyWords.KEYWORD_LIST) + nodes.size(), aAbsDepth, 
				() -> getMappingForList(nodes, aAbsDepth-1, this::getMappingForNode, alwaysUseFullList));
	}
	
	/**
	 * Creates a mapping for a list of variable declarators.
	 * @param vars
	 * the list of variable declarators that should be mapped
	 * @param aAbsDepth
	 * the depth of the mapping
	 * @param alwaysUseFullList
	 * whether to always get Tokens for the full list
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForVariableDeclaratorList(List<VariableDeclarator> vars, boolean alwaysUseFullList, int aAbsDepth) {
		return applyCombination(vars, () -> String.valueOf(IBasicKeyWords.KEYWORD_LIST) + vars.size(), aAbsDepth, 
				() -> getMappingForList(vars, aAbsDepth-1, this::getMappingForVariableDeclarator, alwaysUseFullList));
	}

	/**
	 * Creates a mapping for a list of types.
	 * @param types
	 * the list of types that should be mapped
	 * @param aAbsDepth
	 * the depth of the mapping
	 * @param alwaysUseFullList
	 * whether to always get Tokens for the full list
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForTypeList(List<? extends Type> types, boolean alwaysUseFullList, int aAbsDepth) {
		return applyCombination(types, () -> String.valueOf(IBasicKeyWords.KEYWORD_LIST) + types.size(), aAbsDepth, 
				() -> getMappingForList(types, aAbsDepth-1, this::getMappingForType, alwaysUseFullList));
	}
	
	/**
	 * Creates a mapping for a list of statements.
	 * @param statements
	 * the list of statements that should be mapped
	 * @param aAbsDepth
	 * the depth of the mapping
	 * @param alwaysUseFullList
	 * whether to always get Tokens for the full list
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForStatementList(List<? extends Statement> statements, boolean alwaysUseFullList, int aAbsDepth) {
		return applyCombination(statements, () -> String.valueOf(IBasicKeyWords.KEYWORD_LIST) + statements.size(), aAbsDepth, 
				() -> getMappingForList(statements, aAbsDepth-1, this::getMappingForStatement, alwaysUseFullList));
	}

	/**
	 * Creates a mapping for a list of arguments.
	 * @param parameters
	 * the list of parameters
	 * @param aAbsDepth
	 * the depth of the mapping
	 * @param alwaysUseFullList
	 * whether to always get Tokens for the full list
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForParameterList(List<Parameter> parameters, boolean alwaysUseFullList, int aAbsDepth) {
		return applyCombination(parameters, () -> String.valueOf(IBasicKeyWords.KEYWORD_LIST) + parameters.size(), aAbsDepth, 
				() -> getMappingForList(parameters, aAbsDepth-1, this::getMappingForParameter, alwaysUseFullList));
	}

	/**
	 * Creates a mapping for a list of expressions.
	 * @param expressions
	 * the list of expressions
	 * @param aAbsDepth
	 * the depth of the mapping
	 * @param alwaysUseFullList
	 * whether to always get Tokens for the full list
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForExpressionList(List<? extends Expression> expressions, boolean alwaysUseFullList, int aAbsDepth) {
		return applyCombination(expressions, () -> String.valueOf(IBasicKeyWords.KEYWORD_LIST) + expressions.size(), aAbsDepth, 
				() -> getMappingForList(expressions, aAbsDepth-1, this::getMappingForExpression, alwaysUseFullList));
	}
	
	/**
	 * Creates a mapping for a list of array creation levels.
	 * @param levels
	 * the list of array creation levels
	 * @param aAbsDepth
	 * the depth of the mapping
	 * @param alwaysUseFullList
	 * whether to always get Tokens for the full list
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForArrayCreationLevelList(List<ArrayCreationLevel> levels, boolean alwaysUseFullList, int aAbsDepth) {
		return applyCombination(levels, () -> String.valueOf(IBasicKeyWords.KEYWORD_LIST) + levels.size(), aAbsDepth, 
				() -> getMappingForList(levels, aAbsDepth-1, this::getMappingForArrayCreationLevel, alwaysUseFullList));
	}

	/**
	 * Creates a mapping for a list of body declarations.
	 * @param bodyDeclarations
	 * the list of body declarations
	 * @param aAbsDepth
	 * the depth of the mapping
	 * @param alwaysUseFullList
	 * whether to always get Tokens for the full list
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForBodyDeclarationList(List<? extends BodyDeclaration<?>> bodyDeclarations, boolean alwaysUseFullList, int aAbsDepth) {
		return applyCombination(bodyDeclarations, () -> String.valueOf(IBasicKeyWords.KEYWORD_LIST) + bodyDeclarations.size(), aAbsDepth, 
				() -> getMappingForList(bodyDeclarations, aAbsDepth-1, this::getMappingForBodyDeclaration, alwaysUseFullList));
	}

	/**
	 * Creates a mapping for a list of body declarations.
	 * @param types
	 * the list of body declarations
	 * @param aAbsDepth
	 * the depth of the mapping
	 * @param alwaysUseFullList
	 * whether to always get Tokens for the full list
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForClassOrInterfaceTypeList(List<ClassOrInterfaceType> types, boolean alwaysUseFullList, int aAbsDepth) {
		return applyCombination(types, () -> String.valueOf(IBasicKeyWords.KEYWORD_LIST) + types.size(), aAbsDepth, 
				() -> getMappingForList(types, aAbsDepth-1, this::getMappingForType, alwaysUseFullList));
	}

	/**
	 * Creates a mapping for a list of type parameters.
	 * @param typeParameters
	 * the list of type parameters
	 * @param aAbsDepth
	 * the depth of the mapping
	 * @param alwaysUseFullList
	 * whether to always get Tokens for the full list
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingsForTypeParameterList(List<TypeParameter> typeParameters, boolean alwaysUseFullList, int aAbsDepth) {
		return applyCombination(typeParameters, () -> String.valueOf(IBasicKeyWords.KEYWORD_LIST) + typeParameters.size(), aAbsDepth, 
				() -> getMappingForList(typeParameters, aAbsDepth-1, this::getMappingForTypeParameter, alwaysUseFullList));
	}
	
	

	//all tokens (if not abstract) are stored with all respective constructor arguments (@allFieldsConstructor)

	@Override
	public default String getMappingForMemberValuePair(MemberValuePair aNode, int aAbsDepth) {
		//final SimpleName name, final Expression value
		return applyCombination(aNode, getKeyWordProvider()::getMemberValuePair, aAbsDepth, 
				() -> getMappingForSimpleName(aNode.getName(), usesVariableNameAbstraction() ? 0 : aAbsDepth-1), 
				() -> getMappingForExpression(aNode.getValue(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForSwitchEntryStmt(SwitchEntryStmt aNode, int aAbsDepth) {
		//final Expression label, final NodeList<Statement> statements
		return applyCombination(aNode, getKeyWordProvider()::getSwitchEntryStatement, aAbsDepth, 
				() -> getMappingForExpression(aNode.getLabel().orElse(null), aAbsDepth-1),
				() -> getMappingForStatementList(aNode.getStatements(), false, aAbsDepth-1));
	}

	@Override
	public default String getMappingForUnionType(UnionType aNode, int aAbsDepth) {
		//NodeList<ReferenceType> elements
		return applyCombination(aNode, getKeyWordProvider()::getTypeUnion, aAbsDepth,
				() -> getMappingForTypeList(aNode.getElements(), true, aAbsDepth-1));
	}

	@Override
	public default String getMappingForIntersectionType(IntersectionType aNode, int aAbsDepth) {
		//NodeList<ReferenceType> elements
		return applyCombination(aNode, getKeyWordProvider()::getTypeIntersection, aAbsDepth,
				() -> getMappingForTypeList(aNode.getElements(), true, aAbsDepth-1));
	}

	@Override
	public default String getMappingForLambdaExpr(LambdaExpr aNode, int aAbsDepth) {
		//NodeList<Parameter> parameters, Statement body, boolean isEnclosingParameters
		return applyCombination(aNode, getKeyWordProvider()::getLambdaExpression, aAbsDepth,
				() -> getMappingForParameterList(aNode.getParameters(), true, aAbsDepth-1),
				() -> getMappingForStatement(aNode.getBody(), aAbsDepth-1),
				() -> getMappingForBoolean(aNode.isEnclosingParameters()));
	}

	@Override
	public default String getMappingForInstanceOfExpr(InstanceOfExpr aNode, int aAbsDepth) {
		//final Expression expression, final ReferenceType<?> type
		return applyCombination(aNode, getKeyWordProvider()::getInstanceofExpression, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression(), aAbsDepth-1),
				() -> getMappingForType(aNode.getType(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForConditionalExpr(ConditionalExpr aNode, int aAbsDepth) {
		//Expression condition, Expression thenExpr, Expression elseExpr
		return applyCombination(aNode, getKeyWordProvider()::getConditionalExpression, aAbsDepth,
				() -> getMappingForExpression(aNode.getCondition(), aAbsDepth-1),
				() -> getMappingForExpression(aNode.getThenExpr(), aAbsDepth-1),
				() -> getMappingForExpression(aNode.getElseExpr(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForObjectCreationExpr(ObjectCreationExpr aNode, int aAbsDepth) {
		//final Expression scope, final ClassOrInterfaceType type, final NodeList<Type> typeArguments, 
		//final NodeList<Expression> arguments, final NodeList<BodyDeclaration<?>> anonymousClassBody
		return applyCombination(aNode, getKeyWordProvider()::getObjCreateExpression, aAbsDepth,
				() -> getMappingForExpression(aNode.getScope().orElse(null), aAbsDepth-1), //TODO: get full scope i depth > 0?
				() -> getMappingForClassOrInterfaceType(aNode.getType(), aAbsDepth-1),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, aAbsDepth-1),
				() -> getMappingForExpressionList(aNode.getArguments(), true, aAbsDepth-1),
				() -> getMappingForBodyDeclarationList(aNode.getAnonymousClassBody().orElse(null), false, aAbsDepth-1));
	}

	@Override
	public default String getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode, int aAbsDepth) {
		//final ClassOrInterfaceType scope, final SimpleName name, final NodeList<Type> typeArguments
		return applyCombination(aNode, getKeyWordProvider()::getClassOrInterfaceType, aAbsDepth,
				() -> getMappingForType(aNode.getScope().orElse(null), aAbsDepth), //get full scope if depth > 0
				() -> getMappingForSimpleName(aNode.getName(), usesClassNameAbstraction() ? 0 : aAbsDepth-1),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, aAbsDepth-1));
	}

	@Override
	public default String getMappingForEnclosedExpr(EnclosedExpr aNode, int aAbsDepth) {
		//final Expression inner
		return applyCombination(aNode, getKeyWordProvider()::getEnclosedExpression, aAbsDepth,
				() -> getMappingForExpression(aNode.getInner().orElse(null), aAbsDepth-1));
	}

	@Override
	public default String getMappingForArrayInitializerExpr(ArrayInitializerExpr aNode, int aAbsDepth) {
		//NodeList<Expression> values
		return applyCombination(aNode, getKeyWordProvider()::getArrayInitExpression, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getValues(), true, aAbsDepth-1));
	}

	@Override
	public default String getMappingForArrayCreationExpr(ArrayCreationExpr aNode, int aAbsDepth) {
		//Type elementType, NodeList<ArrayCreationLevel> levels, ArrayInitializerExpr initializer
		return applyCombination(aNode, getKeyWordProvider()::getArrayCreateExpression, aAbsDepth,
				() -> getMappingForType(aNode.getElementType(), aAbsDepth-1),
				() -> getMappingForArrayCreationLevelList(aNode.getLevels(), true, aAbsDepth-1),
				() -> getMappingForExpression(aNode.getInitializer().orElse(null), aAbsDepth-1));
	}

	@Override
	public default String getMappingForArrayAccessExpr(ArrayAccessExpr aNode, int aAbsDepth) {
		//Expression name, Expression index
		return applyCombination(aNode, getKeyWordProvider()::getArrayAccessExpression, aAbsDepth,
				() -> getMappingForExpression(aNode.getName(), aAbsDepth-1),
				() -> getMappingForExpression(aNode.getIndex(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForTypeParameter(TypeParameter aNode, int aAbsDepth) {
		//SimpleName name, NodeList<ClassOrInterfaceType> typeBound, NodeList<AnnotationExpr> annotations
		return applyCombination(aNode, getKeyWordProvider()::getTypePar, aAbsDepth,
				() -> getMappingForSimpleName(aNode.getName(), usesGenericTypeNameAbstraction() ? 0 : aAbsDepth-1),
				() -> getMappingForClassOrInterfaceTypeList(aNode.getTypeBound(), true, aAbsDepth-1),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth-1));
	}

	@Override
	public default String getMappingForVariableDeclarator(VariableDeclarator aNode, int aAbsDepth) {
		//Type type, SimpleName name, Expression initializer
		return applyCombination(aNode, getKeyWordProvider()::getVariableDeclaration, aAbsDepth,
				() -> getMappingForType(aNode.getType(), aAbsDepth-1),
				() -> getMappingForSimpleName(aNode.getName(), usesVariableNameAbstraction() ? 0 : aAbsDepth-1),
				() -> aNode.getInitializer().isPresent() ? getMappingForExpression(aNode.getInitializer().get(), aAbsDepth-1) : "");
	}

	@Override
	public default String getMappingForImportDeclaration(ImportDeclaration aNode, int aAbsDepth) {
		//Name name, boolean isStatic, boolean isAsterisk
		return applyCombination(aNode, getKeyWordProvider()::getImportDeclaration, aAbsDepth,
				() -> getMappingForName(aNode.getName(), usesPackageAndImportAbstraction() ? 0 : aAbsDepth-1),
				() -> getMappingForBoolean(aNode.isStatic()),
				() -> getMappingForBoolean(aNode.isAsterisk()));
	}

	@Override
	public default String getMappingForPackageDeclaration(PackageDeclaration aNode, int aAbsDepth) {
		//NodeList<AnnotationExpr> annotations, Name name
		return applyCombination(aNode, getKeyWordProvider()::getPackageDeclaration, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth-1),
				() -> getMappingForName(aNode.getName(), usesPackageAndImportAbstraction() ? 0 : aAbsDepth-1));
	}

	@Override
	public default String getMappingForParameter(Parameter aNode, int aAbsDepth) {
		//EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations, Type type, 
		//boolean isVarArgs, NodeList<AnnotationExpr> varArgsAnnotations, SimpleName name
		return applyCombination(aNode, getKeyWordProvider()::getParameter, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth-1),
				() -> getMappingForType(aNode.getType(), aAbsDepth-1),
				() -> getMappingForBoolean(aNode.isVarArgs()),
				() -> getMappingForExpressionList(aNode.getVarArgsAnnotations(), true, aAbsDepth-1),
				() -> getMappingForSimpleName(aNode.getName(), usesVariableNameAbstraction() ? 0 : aAbsDepth-1));
	}

	@Override
	public default String getMappingForEnumDeclaration(EnumDeclaration aNode, int aAbsDepth) {
		//EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations, SimpleName name, 
		//NodeList<ClassOrInterfaceType> implementedTypes, NodeList<EnumConstantDeclaration> entries, NodeList<BodyDeclaration<?>> members
		return applyCombination(aNode, getKeyWordProvider()::getEnumDeclaration, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth-1),
				() -> getMappingForSimpleName(aNode.getName(), usesVariableNameAbstraction() ? 0 : aAbsDepth-1),
				() -> getMappingForClassOrInterfaceTypeList(aNode.getImplementedTypes(), true, aAbsDepth-1),
				() -> getMappingForBodyDeclarationList(aNode.getEntries(), true, aAbsDepth-1),
				() -> getMappingForBodyDeclarationList(aNode.getMembers(), false, aAbsDepth-1));
	}

	@Override
	public default String getMappingForClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration aNode, int aAbsDepth) {
		//final EnumSet<Modifier> modifiers, final NodeList<AnnotationExpr> annotations, final boolean isInterface, 
		//final SimpleName name, final NodeList<TypeParameter> typeParameters, final NodeList<ClassOrInterfaceType> extendedTypes, 
		//final NodeList<ClassOrInterfaceType> implementedTypes, final NodeList<BodyDeclaration<?>> members
		return applyCombination(aNode, getKeyWordProvider()::getClassOrInterfaceDeclaration, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth-1),
				() -> getMappingForBoolean(aNode.isInterface()),
				() -> getMappingForSimpleName(aNode.getName(), usesClassNameAbstraction() ? 0 : aAbsDepth-1),
				() -> getMappingsForTypeParameterList(aNode.getTypeParameters(), true, aAbsDepth-1),
				() -> getMappingForClassOrInterfaceTypeList(aNode.getExtendedTypes(), true, aAbsDepth-1),
				() -> getMappingForClassOrInterfaceTypeList(aNode.getImplementedTypes(), true, aAbsDepth-1),
				() -> getMappingForBodyDeclarationList(aNode.getMembers(), false, aAbsDepth-1));
	}

	@Override
	public default String getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode, int aAbsDepth) {
		//NodeList<AnnotationExpr> annotations, SimpleName name, NodeList<Expression> arguments, NodeList<BodyDeclaration<?>> classBody
		return applyCombination(aNode, getKeyWordProvider()::getEnumConstantDeclaration, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth-1),
				() -> getMappingForSimpleName(aNode.getName(), usesVariableNameAbstraction() ? 0 : aAbsDepth-1),
				() -> getMappingForExpressionList(aNode.getArguments(), true, aAbsDepth-1),
				() -> getMappingForBodyDeclarationList(aNode.getClassBody(), false, aAbsDepth-1));
	}

	@Override
	public default String getMappingForMethodDeclaration(MethodDeclaration aNode, int aAbsDepth) {
		//final EnumSet<Modifier> modifiers, final NodeList<AnnotationExpr> annotations, final NodeList<TypeParameter> typeParameters, 
		//final Type type, final SimpleName name, final boolean isDefault, final NodeList<Parameter> parameters, 
		//final NodeList<ReferenceType> thrownExceptions, final BlockStmt body
		return applyCombination(aNode, getKeyWordProvider()::getMethodDeclaration, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth-1),
				() -> getMappingsForTypeParameterList(aNode.getTypeParameters(), true, aAbsDepth-1),
				() -> getMappingForType(aNode.getType(), aAbsDepth-1),
				() -> getMappingForSimpleName(aNode.getName(), usesMethodNameAbstraction() ? 0 : aAbsDepth-1),
				() -> getMappingForBoolean(aNode.isDefault()),
				() -> getMappingForParameterList(aNode.getParameters(), true, aAbsDepth-1),
				() -> getMappingForTypeList(aNode.getThrownExceptions(), true, aAbsDepth-1),
				() -> getMappingForStatement(aNode.getBody().orElse(null), aAbsDepth-1));
	}

	@Override
	public default String getMappingForFieldDeclaration(FieldDeclaration aNode, int aAbsDepth) {
		//EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations, NodeList<VariableDeclarator> variables
		return applyCombination(aNode, getKeyWordProvider()::getFieldDeclaration, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth-1),
				() -> getMappingForVariableDeclaratorList(aNode.getVariables(), true, aAbsDepth-1));
	}

	@Override
	public default String getMappingForConstructorDeclaration(ConstructorDeclaration aNode, int aAbsDepth) {
		//EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations, NodeList<TypeParameter> typeParameters, 
		//SimpleName name, NodeList<Parameter> parameters, NodeList<ReferenceType> thrownExceptions, BlockStmt body
		return applyCombination(aNode, getKeyWordProvider()::getConstructorDeclaration, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth-1),
				() -> getMappingsForTypeParameterList(aNode.getTypeParameters(), true, aAbsDepth-1),
				() -> getMappingForSimpleName(aNode.getName(), usesClassNameAbstraction() ? 0 : aAbsDepth-1),
				() -> getMappingForParameterList(aNode.getParameters(), true, aAbsDepth-1),
				() -> getMappingForTypeList(aNode.getThrownExceptions(), true, aAbsDepth-1),
				() -> getMappingForStatement(aNode.getBody(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForWhileStmt(WhileStmt aNode, int aAbsDepth) {
		//final Expression condition, final Statement body
		return applyCombination(aNode, getKeyWordProvider()::getWhileStatement, aAbsDepth,
				() -> getMappingForExpression(aNode.getCondition(), aAbsDepth-1),
				() -> getMappingForStatement(aNode.getBody(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForSwitchStmt(SwitchStmt aNode, int aAbsDepth) {
		//final Expression selector, final NodeList<SwitchEntryStmt> entries
		return applyCombination(aNode, getKeyWordProvider()::getSwitchStatement, aAbsDepth,
				() -> getMappingForExpression(aNode.getSelector(), aAbsDepth-1),
				() -> getMappingForStatementList(aNode.getEntries(), false, aAbsDepth-1));
	}

	@Override
	public default String getMappingForForStmt(ForStmt aNode, int aAbsDepth) {
		//final NodeList<Expression> initialization, final Expression compare, final NodeList<Expression> update, final Statement body
		return applyCombination(aNode, getKeyWordProvider()::getForStatement, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getInitialization(), true, aAbsDepth-1),
				() -> getMappingForExpression(aNode.getCompare().orElse(null), aAbsDepth-1),
				() -> getMappingForExpressionList(aNode.getUpdate(), true, aAbsDepth-1),
				() -> getMappingForStatement(aNode.getBody(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForForeachStmt(ForeachStmt aNode, int aAbsDepth) {
		//final VariableDeclarationExpr variable, final Expression iterable, final Statement body
		return applyCombination(aNode, getKeyWordProvider()::getForEachStatement, aAbsDepth,
				() -> getMappingForVariableDeclarationExpr(aNode.getVariable(), aAbsDepth-1),
				() -> getMappingForExpression(aNode.getIterable(), aAbsDepth-1),
				() -> getMappingForStatement(aNode.getBody(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForExplicitConstructorInvocationStmt(ExplicitConstructorInvocationStmt aNode, int aAbsDepth) {
		//final NodeList<Type> typeArguments, final boolean isThis, final Expression expression, final NodeList<Expression> arguments
		return applyCombination(aNode, getKeyWordProvider()::getExplicitConstructorStatement, aAbsDepth,
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, aAbsDepth-1),
				() -> getMappingForBoolean(aNode.isThis()),
				() -> getMappingForExpression(aNode.getExpression().orElse(null), aAbsDepth-1),
				() -> getMappingForExpressionList(aNode.getArguments(), true, aAbsDepth-1));
	}

	@Override
	public default String getMappingForDoStmt(DoStmt aNode, int aAbsDepth) {
		//final Statement body, final Expression condition
		return applyCombination(aNode, getKeyWordProvider()::getDoStatement, aAbsDepth,
				() -> getMappingForStatement(aNode.getBody(), aAbsDepth-1),
				() -> getMappingForExpression(aNode.getCondition(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForAssertStmt(AssertStmt aNode, int aAbsDepth) {
		//final Expression check, final Expression message
		return applyCombination(aNode, getKeyWordProvider()::getAssertStmt, aAbsDepth,
				() -> getMappingForExpression(aNode.getCheck(), aAbsDepth-1),
				() -> getMappingForExpression(aNode.getMessage().orElse(null), aAbsDepth-1));
	}

	@Override
	public default String getMappingForPrimitiveType(PrimitiveType aNode, int aAbsDepth) {
		//final Primitive type
		return applyCombination(aNode, getKeyWordProvider()::getTypePrimitive, aAbsDepth,
				() -> getMappingForPrimitive(aNode.getType()));
	}

	@Override
	public default String getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode, int aAbsDepth) {
		//final EnumSet<Modifier> modifiers, final NodeList<AnnotationExpr> annotations, final NodeList<VariableDeclarator> variables
		return applyCombination(aNode, getKeyWordProvider()::getVariableDeclarationExpression, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth-1),
				() -> getMappingForVariableDeclaratorList(aNode.getVariables(), true, aAbsDepth-1));
	}

	@Override
	public default String getMappingForMethodReferenceExpr(MethodReferenceExpr aNode, int aAbsDepth) {
		//Expression scope, NodeList<Type> typeArguments, String identifier
		return applyCombination(aNode, getKeyWordProvider()::getMethodReferenceExpression, aAbsDepth,
				() -> getMappingForExpression(aNode.getScope(), aAbsDepth-1),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, aAbsDepth-1),
				() -> usesMethodNameAbstraction() ? getKeyWordProvider().getMethodIdentifier() : getMappingForString(aNode.getIdentifier()));
	}

	//TODO: is this really correct?
	@Override
	public default String getMappingForMethodCallExpr(MethodCallExpr aNode, int aAbsDepth) {
		//final Expression scope, final NodeList<Type> typeArguments, final SimpleName name, final NodeList<Expression> arguments
		boolean isLocal = aNode == null ? false : getPrivateMethodBlackList().contains(getMappingForSimpleName(aNode.getName(), 1));
		return applyCombination(aNode, getKeyWordProvider()::getMethodCallExpression, aAbsDepth,
				() -> getMappingForExpression(aNode.getScope().orElse(null), isLocal ? 0 : aAbsDepth-1),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, aAbsDepth-1),
				() -> isLocal ? getKeyWordProvider().getLocalMethodCallExpression() : 
					getMappingForSimpleName(aNode.getName(), usesMethodNameAbstraction() ? 0 : aAbsDepth-1),
				() -> getMappingForExpressionList(aNode.getArguments(), true, aAbsDepth-1));
	}

	@Override
	public default String getMappingForFieldAccessExpr(FieldAccessExpr aNode, int aAbsDepth) {
		//final Expression scope, final NodeList<Type> typeArguments, final SimpleName name
		return applyCombination(aNode, getKeyWordProvider()::getFieldAccessExpression, aAbsDepth,
				() -> getMappingForExpression(aNode.getScope().orElse(null), aAbsDepth-1),
				() -> getMappingForTypeList(aNode.getTypeArguments().orElse(null), true, aAbsDepth-1),
				() -> getMappingForSimpleName(aNode.getName(), usesVariableNameAbstraction() ? 0 : aAbsDepth-1));
	}

	// TODO: rework implements and extends -> add as simple tokens and push the
	// list traversal in the ASTTokenReader class
	@Override
	public default String getMappingForExtendsStmt(ExtendsStmt aNode, int aAbsDepth) {
		//List<ClassOrInterfaceType> extendsList
		return applyCombination(aNode, getKeyWordProvider()::getExtendsStatement, aAbsDepth,
				() -> getMappingForClassOrInterfaceTypeList(aNode.getExtends(), true, aAbsDepth-1));
	}

	@Override
	public default String getMappingForImplementsStmt(ImplementsStmt aNode, int aAbsDepth) {
		//List<ClassOrInterfaceType> implementsList
		return applyCombination(aNode, getKeyWordProvider()::getImplementsStatement, aAbsDepth,
				() -> getMappingForClassOrInterfaceTypeList(aNode.getImplements(), true, aAbsDepth-1));
	}

	@Override
	public default String getMappingForTypeExpr(TypeExpr aNode, int aAbsDepth) {
		//Type type
		return applyCombination(aNode, getKeyWordProvider()::getTypeExpression, aAbsDepth,
				() -> getMappingForType(aNode.getType(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForClassExpr(ClassExpr aNode, int aAbsDepth) {
		//Type type
		return applyCombination(aNode, getKeyWordProvider()::getClassExpression, aAbsDepth,
				() -> getMappingForType(aNode.getType(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForCastExpr(CastExpr aNode, int aAbsDepth) {
		//Type type, Expression expression
		return applyCombination(aNode, getKeyWordProvider()::getCastExpression, aAbsDepth,
				() -> getMappingForType(aNode.getType(), aAbsDepth-1),
				() -> getMappingForExpression(aNode.getExpression(), aAbsDepth-1));
	}
	
	@Override
	public default String getMappingForUnaryExpr(UnaryExpr aNode, int aAbsDepth) {
		//final Expression expression, final Operator operator
		return applyCombination(aNode, getKeyWordProvider()::getUnaryExpression, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression(), aAbsDepth-1),
				() -> getMappingForUnaryOperator(aNode.getOperator()));
	}

	@Override
	public default String getMappingForBinaryExpr(BinaryExpr aNode, int aAbsDepth) {
		//Expression left, Expression right, Operator operator
		return applyCombination(aNode, getKeyWordProvider()::getBinaryExpression, aAbsDepth,
				() -> getMappingForExpression(aNode.getLeft(), aAbsDepth-1),
				() -> getMappingForExpression(aNode.getRight(), aAbsDepth-1),
				() -> getMappingForBinaryOperator(aNode.getOperator()));
	}

	@Override
	public default String getMappingForAssignExpr(AssignExpr aNode, int aAbsDepth) {
		//Expression target, Expression value, Operator operator
		return applyCombination(aNode, getKeyWordProvider()::getAssignExpression, aAbsDepth,
				() -> getMappingForExpression(aNode.getTarget(), aAbsDepth-1),
				() -> getMappingForExpression(aNode.getValue(), aAbsDepth-1),
				() -> getMappingForAssignOperator(aNode.getOperator()));
	}
	
	@Override
	public default String getMappingForIfStmt(IfStmt aNode, int aAbsDepth) {
		//final Expression condition, final Statement thenStmt, final Statement elseStmt
		return applyCombination(aNode, getKeyWordProvider()::getIfStatement, aAbsDepth,
				() -> getMappingForExpression(aNode.getCondition(), aAbsDepth-1),
				() -> getMappingForStatement(aNode.getThenStmt(), aAbsDepth-1),
				() -> getMappingForStatement(aNode.getElseStmt().orElse(null), aAbsDepth-1));
	}
	
	@Override
	default String getMappingForLocalClassDeclarationStmt(LocalClassDeclarationStmt aNode, int aAbsDepth) {
		//final ClassOrInterfaceDeclaration classDeclaration
		return applyCombination(aNode, getKeyWordProvider()::getLocalClassDeclarationStmt, aAbsDepth,
				() -> getMappingForClassOrInterfaceDeclaration(aNode.getClassDeclaration(), aAbsDepth-1));
	}

	@Override
	default String getMappingForArrayType(ArrayType aNode, int aAbsDepth) {
		//Type componentType, NodeList<AnnotationExpr> annotations
		return applyCombination(aNode, getKeyWordProvider()::getArrayType, aAbsDepth,
				() -> getMappingForType(aNode.getComponentType(), aAbsDepth-1),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth-1));
	}

	@Override
	default String getMappingForArrayCreationLevel(ArrayCreationLevel aNode, int aAbsDepth) {
		//Expression dimension, NodeList<AnnotationExpr> annotations
		return applyCombination(aNode, getKeyWordProvider()::getArrayCreationLevel, aAbsDepth,
				() -> getMappingForExpression(aNode.getDimension().orElse(null), aAbsDepth-1),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth-1));
	}
	
	@Override
	public default String getMappingForInitializerDeclaration(InitializerDeclaration aNode, int aAbsDepth) {
		//boolean isStatic, BlockStmt body
		return applyCombination(aNode, getKeyWordProvider()::getInitializerDeclaration, aAbsDepth,
				() -> getMappingForBoolean(aNode.isStatic()),
				() -> getMappingForStatement(aNode.getBody(), aAbsDepth-1));
	}
	
	@Override
	public default String getMappingForThrowStmt(ThrowStmt aNode, int aAbsDepth) {
		//final Expression expression
		return applyCombination(aNode, getKeyWordProvider()::getThrowStatement, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression(), aAbsDepth-1));
	}
	
	@Override
	public default String getMappingForNameExpr(NameExpr aNode, int aAbsDepth) {
		//final SimpleName name
		return applyCombination(aNode, getKeyWordProvider()::getNameExpression, aAbsDepth,
				() -> getMappingForSimpleName(aNode.getName(), usesVariableNameAbstraction() ? 0 : aAbsDepth-1));
	}

	@Override
	public default String getMappingForTryStmt(TryStmt aNode, int aAbsDepth) {
		//NodeList<VariableDeclarationExpr> resources, final BlockStmt tryBlock, final NodeList<CatchClause> catchClauses, final BlockStmt finallyBlock
		return applyCombination(aNode, getKeyWordProvider()::getTryStatement, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getResources(), true, aAbsDepth-1),
				() -> getMappingForStatement(aNode.getTryBlock().orElse(null), aAbsDepth-1),
				() -> getMappingForNodeList(aNode.getCatchClauses(), true, aAbsDepth-1),
				() -> getMappingForStatement(aNode.getFinallyBlock().orElse(null), aAbsDepth-1));
	}
	
	@Override
	public default String getMappingForThisExpr(ThisExpr aNode, int aAbsDepth) {
		//final Expression classExpr
		return applyCombination(aNode, getKeyWordProvider()::getThisExpression, aAbsDepth,
				() -> getMappingForExpression(aNode.getClassExpr().orElse(null), aAbsDepth-1));
	}
	
	@Override
	public default String getMappingForBlockComment(BlockComment aNode, int aAbsDepth) {
		//String content
		return applyCombination(aNode, getKeyWordProvider()::getBlockComment, aAbsDepth,
				() -> getMappingForString(aNode.getContent()));
	}

	@Override
	public default String getMappingForExpressionStmt(ExpressionStmt aNode, int aAbsDepth) {
		//final Expression expression
		return applyCombination(aNode, getKeyWordProvider()::getExpressionStatement, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForSuperExpr(SuperExpr aNode, int aAbsDepth) {
		//final Expression classExpr
		return applyCombination(aNode, getKeyWordProvider()::getSuperExpression, aAbsDepth,
				() -> getMappingForExpression(aNode.getClassExpr().orElse(null), aAbsDepth-1));
	}

	@Override
	public default String getMappingForReturnStmt(ReturnStmt aNode, int aAbsDepth) {
		//final Expression expression
		return applyCombination(aNode, getKeyWordProvider()::getReturnStatement, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression().orElse(null), aAbsDepth-1));
	}

	@Override
	public default String getMappingForLabeledStmt(LabeledStmt aNode, int aAbsDepth) {
		//final SimpleName label, final Statement statement
		return applyCombination(aNode, getKeyWordProvider()::getLabeledStatement, aAbsDepth,
				() -> getMappingForSimpleName(aNode.getLabel(), usesVariableNameAbstraction() ? 0 : aAbsDepth-1),
				() -> getMappingForStatement(aNode.getStatement(), aAbsDepth-1));
	}
	
	@Override
	public default String getMappingForBreakStmt(BreakStmt aNode, int aAbsDepth) {
		//final SimpleName label
		return applyCombination(aNode, getKeyWordProvider()::getBreak, aAbsDepth,
				() -> getMappingForSimpleName(aNode.getLabel().orElse(null), usesVariableNameAbstraction() ? 0 : aAbsDepth-1));
	}

	@Override
	public default String getMappingForSingleMemberAnnotationExpr(SingleMemberAnnotationExpr aNode, int aAbsDepth) {
		//final Name name, final Expression memberValue
		return applyCombination(aNode, getKeyWordProvider()::getSingleMemberAnnotationExpression, aAbsDepth,
				() -> getMappingForName(aNode.getName(), usesAnnotationAbstraction() ? 0 : aAbsDepth-1),
				() -> getMappingForExpression(aNode.getMemberValue(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForNormalAnnotationExpr(NormalAnnotationExpr aNode, int aAbsDepth) {
		//final Name name, final NodeList<MemberValuePair> pairs
		return applyCombination(aNode, getKeyWordProvider()::getNormalAnnotationExpression, aAbsDepth,
				() -> getMappingForName(aNode.getName(), usesAnnotationAbstraction() ? 0 : aAbsDepth-1),
				() -> getMappingForNodeList(aNode.getPairs(), true, aAbsDepth-1));
	}

	@Override
	public default String getMappingForMarkerAnnotationExpr(MarkerAnnotationExpr aNode, int aAbsDepth) {
		//final Name name
		return applyCombination(aNode, getKeyWordProvider()::getMarkerAnnotationExpression, aAbsDepth,
				() -> getMappingForName(aNode.getName(), usesAnnotationAbstraction() ? 0 : aAbsDepth-1));
	}

	@Override
	public default String getMappingForWildcardType(WildcardType aNode, int aAbsDepth) {
		//final ReferenceType extendedType, final ReferenceType superType
		return applyCombination(aNode, getKeyWordProvider()::getTypeWildcard, aAbsDepth,
				() -> getMappingForType(aNode.getExtendedType().orElse(null), aAbsDepth-1),
				() -> getMappingForType(aNode.getSuperType().orElse(null), aAbsDepth-1));
	}
	
	@Override
	public default String getMappingForBlockStmt(BlockStmt aNode, int aAbsDepth) {
		//final NodeList<Statement> statements
		return applyCombination(aNode, getKeyWordProvider()::getBlockStatement, aAbsDepth,
				() -> getMappingForStatementList(aNode.getStatements(), false, aAbsDepth-1));
	}

	@Override
	public default String getMappingForContinueStmt(ContinueStmt aNode, int aAbsDepth) {
		//final SimpleName label
		return applyCombination(aNode, getKeyWordProvider()::getContinueStatement, aAbsDepth,
				() -> getMappingForSimpleName(aNode.getLabel().orElse(null), usesVariableNameAbstraction() ? 0 : aAbsDepth-1));
	}

	@Override
	public default String getMappingForSynchronizedStmt(SynchronizedStmt aNode, int aAbsDepth) {
		//final Expression expression, final BlockStmt body
		return applyCombination(aNode, getKeyWordProvider()::getSynchronizedStatement, aAbsDepth,
				() -> getMappingForExpression(aNode.getExpression(), aAbsDepth-1),
				() -> getMappingForStatement(aNode.getBody(), aAbsDepth-1));
	}

	@Override
	public default String getMappingForCatchClause(CatchClause aNode, int aAbsDepth) {
		//final Parameter parameter, final BlockStmt body
		return applyCombination(aNode, getKeyWordProvider()::getCatchClauseStatement, aAbsDepth,
				() -> getMappingForParameter(aNode.getParameter(), aAbsDepth-1),
				() -> getMappingForStatement(aNode.getBody(), aAbsDepth-1));
	}
	
	@Override
	public default String getMappingForCompilationUnit(CompilationUnit aNode, int aAbsDepth) {
		//PackageDeclaration packageDeclaration, NodeList<ImportDeclaration> imports, NodeList<TypeDeclaration<?>> types, ModuleDeclaration module
		return applyCombination(aNode, getKeyWordProvider()::getCompilationUnit, aAbsDepth,
				() -> getMappingForNode(aNode.getPackageDeclaration().orElse(null), aAbsDepth-1),
				() -> getMappingForNodeList(aNode.getImports(), true, aAbsDepth-1),
				() -> getMappingForBodyDeclarationList(aNode.getTypes(), true, aAbsDepth-1),
				() -> getMappingForNode(aNode.getModule().orElse(null), aAbsDepth-1));
	}
	
	@Override
	public default String getMappingForAnnotationDeclaration(AnnotationDeclaration aNode, int aAbsDepth) {
		//EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations, SimpleName name, NodeList<BodyDeclaration<?>> members
		return applyCombination(aNode, getKeyWordProvider()::getAnnotationDeclaration, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth-1),
				() -> getMappingForSimpleName(aNode.getName(), usesAnnotationAbstraction() ? 0 : aAbsDepth-1),
				() -> getMappingForBodyDeclarationList(aNode.getMembers(), false, aAbsDepth-1));
	}

	@Override
	public default String getMappingForAnnotationMemberDeclaration(AnnotationMemberDeclaration aNode, int aAbsDepth) {
		//EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations, Type type, SimpleName name, Expression defaultValue
		return applyCombination(aNode, getKeyWordProvider()::getAnnotationMemberDeclaration, aAbsDepth,
				() -> getMappingForModifiers(aNode.getModifiers()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth-1),
				() -> getMappingForType(aNode.getType(), aAbsDepth-1),
				() -> getMappingForSimpleName(aNode.getName(), usesAnnotationAbstraction() ? 0 : aAbsDepth-1),
				() -> getMappingForExpression(aNode.getDefaultValue().orElse(null), aAbsDepth-1));
	}
	
	@Override
	public default String getMappingForJavadocComment(JavadocComment aNode, int aAbsDepth) {
		//String content
		return applyCombination(aNode, getKeyWordProvider()::getJavadocComment, aAbsDepth,
				() -> getMappingForString(aNode.getContent()));
	}
	
	@Override
	public default String getMappingForLineComment(LineComment aNode, int aAbsDepth) {
		//String content
		return applyCombination(aNode, getKeyWordProvider()::getLineComment, aAbsDepth,
				() -> getMappingForString(aNode.getContent()));
	}
	
	@Override
	default String getMappingForName(Name aNode, int aAbsDepth) {
		//Name qualifier, final String identifier, NodeList<AnnotationExpr> annotations
		return applyCombination(aNode, getKeyWordProvider()::getName, aAbsDepth,
				() -> getMappingForName(aNode.getQualifier().orElse(null), aAbsDepth), //get full qualifier if depth > 0
				() -> getMappingForString(aNode.getIdentifier()),
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth-1));
	}

	@Override
	default String getMappingForSimpleName(SimpleName aNode, int aAbsDepth) {
		//final String identifier
		return applyCombination(aNode, getKeyWordProvider()::getSimpleName, aAbsDepth,
				() -> getMappingForString(aNode.getIdentifier()));
	}
	
	@Override
	default String getMappingForModuleDeclaration(ModuleDeclaration aNode, int aAbsDepth) {
		//NodeList<AnnotationExpr> annotations, Name name, boolean isOpen, NodeList<ModuleStmt> moduleStmts
		return applyCombination(aNode, getKeyWordProvider()::getModuleDeclaration, aAbsDepth,
				() -> getMappingForExpressionList(aNode.getAnnotations(), true, aAbsDepth-1),
				() -> getMappingForName(aNode.getName(), usesClassNameAbstraction() ? 0 : aAbsDepth-1),
				() -> getMappingForBoolean(aNode.isOpen()),
				() -> getMappingForNodeList(aNode.getModuleStmts(), false, aAbsDepth-1));
	}
	
	@Override
	public default String getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode, int aAbsDepth) {
		//final String value
		return applyCombination(aNode, getKeyWordProvider()::getDoubleLiteralExpression, usesNumberAbstraction() ? 0 : aAbsDepth,
				() -> aNode.getValue());
	}

	@Override
	public default String getMappingForLongLiteralExpr(LongLiteralExpr aNode, int aAbsDepth) {
		//final String value
		return applyCombination(aNode, getKeyWordProvider()::getLongLiteralExpression, usesNumberAbstraction() ? 0 : aAbsDepth,
				() -> aNode.getValue());
	}

	@Override
	public default String getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode, int aAbsDepth) {
		//final String value
		return applyCombination(aNode, getKeyWordProvider()::getIntegerLiteralExpression, usesNumberAbstraction() ? 0 : aAbsDepth,
				() -> aNode.getValue());
	}

	@Override
	public default String getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode, int aAbsDepth) {
		//boolean value
		return applyCombination(aNode, getKeyWordProvider()::getBooleanLiteralExpression, aAbsDepth,
				() -> getMappingForBoolean(aNode.getValue()));
	}
	
	//should not differentiate between different String values...
	//before using the values, we need to rework the parsing to not get stuck on brackets or commata in Strings...
	@Override
	public default String getMappingForStringLiteralExpr(StringLiteralExpr aNode, int aAbsDepth) {
		//final String value
		return applyCombination(aNode, getKeyWordProvider()::getStringLiteralExpression, usesStringAndCharAbstraction() ? 0 : aAbsDepth,
				() -> getMappingForString(aNode.getValue()));
	}
	
	//char values may be important, but we may still get problems with '[' or ']', for example...
	@Override
	public default String getMappingForCharLiteralExpr(CharLiteralExpr aNode, int aAbsDepth) {
		//String value
		return applyCombination(aNode, getKeyWordProvider()::getCharLiteralExpression, usesStringAndCharAbstraction() ? 0 : aAbsDepth,
				() -> getMappingForChar(aNode.getValue()));
	}
	
	
	// Here are some special cases that will always only consist of their
	// keyword but I need to overwrite the simple mapper anyway to get the group
	// brackets

	@Override
	public default String getMappingForMethodBodyStmt(BodyStmt aNode, int aAbsDepth) {
		return applyCombination(aNode, getKeyWordProvider()::getBodyStmt, 0);
	}

	@Override
	public default String getMappingForNullLiteralExpr(NullLiteralExpr aNode, int aAbsDepth) {
		return applyCombination(aNode, getKeyWordProvider()::getNullLiteralExpression, 0);
	}

	@Override
	public default String getMappingForThrowsStmt(ThrowsStmt aNode, int aAbsDepth) {
		return applyCombination(aNode, getKeyWordProvider()::getThrowsStatement, 0);
	}

	@Override
	public default String getMappingForElseStmt(ElseStmt aNode, int aAbsDepth) {
		return applyCombination(aNode, getKeyWordProvider()::getElseStatement, 0);
	}

	@Override
	public default String getMappingForVoidType(VoidType aNode, int aAbsDepth) {
		return applyCombination(aNode, getKeyWordProvider()::getTypeVoid, 0);
	}

	@Override
	public default String getMappingForUnknownType(UnknownType aNode, int aAbsDepth) {
		return applyCombination(aNode, getKeyWordProvider()::getTypeUnknown, 0);
	}
	
	@Override
	default String getMappingForModuleStmt(ModuleStmt aNode, int aAbsDepth) {
		return applyCombination(aNode, getKeyWordProvider()::getModuleStmt, 0);
	}

	@Override
	default String getMappingForUnknownNode(Node aNode, int aAbsDepth) {
		return applyCombination(aNode, () -> getKeyWordProvider().getUnknown(aNode), 0);
	}
	
}
