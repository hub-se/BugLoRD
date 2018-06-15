package se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IBasicKeyWords;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider.KeyWords;

public interface IAbstractionMapperBasics extends IMapper<String> {

	public int getMaxListMembers();

	public static StringBuilder getStringBuilderWithKeyWord(String keyWord) {
		StringBuilder result = new StringBuilder();
		// append the identifier
		result.append(keyWord);
		return result;
	}

	public static void appendGroupedToken(StringBuilder result, String token) {
		if (token == null) {
			result.append(IBasicKeyWords.GROUP_START);
			result.append(IBasicKeyWords.KEYWORD_NULL);
			result.append(IBasicKeyWords.GROUP_END);
		} else if (token.length() == 0) {
			result.append(IBasicKeyWords.GROUP_START);
			result.append(IBasicKeyWords.GROUP_END);
		} else if (token.charAt(0) != IBasicKeyWords.GROUP_START) {
			result.append(IBasicKeyWords.GROUP_START);
			result.append(token);
			result.append(IBasicKeyWords.GROUP_END);
		} else {
			result.append(token);
		}
	}

	public default String combineData2String(String keyWord) {
		StringBuilder result = getStringBuilderWithKeyWord(keyWord);

		return result.toString();
	}

	public default String combineData2String(String keyWord, Supplier<String> token1) {
		StringBuilder result = getStringBuilderWithKeyWord(keyWord);

		// there is some data to be put into the string
		// fix the tokens that did not get the child group brackets
		appendGroupedToken(result, token1.get());

		return result.toString();
	}

	public default String combineData2String(String keyWord, Supplier<String> token1, Supplier<String> token2) {
		StringBuilder result = getStringBuilderWithKeyWord(keyWord);

		// there is some data to be put into the string
		// fix the tokens that did not get the child group brackets
		appendGroupedToken(result, token1.get());
		appendGroupedToken(result, token2.get());

		return result.toString();
	}

	public default String combineData2String(String keyWord, Supplier<String> token1, Supplier<String> token2,
			Supplier<String> token3) {
		StringBuilder result = getStringBuilderWithKeyWord(keyWord);

		// there is some data to be put into the string
		// fix the tokens that did not get the child group brackets
		appendGroupedToken(result, token1.get());
		appendGroupedToken(result, token2.get());
		appendGroupedToken(result, token3.get());

		return result.toString();
	}

	public default String combineData2String(String keyWord, Supplier<String> token1, Supplier<String> token2,
			Supplier<String> token3, Supplier<String> token4) {
		StringBuilder result = getStringBuilderWithKeyWord(keyWord);

		// there is some data to be put into the string
		// fix the tokens that did not get the child group brackets
		appendGroupedToken(result, token1.get());
		appendGroupedToken(result, token2.get());
		appendGroupedToken(result, token3.get());
		appendGroupedToken(result, token4.get());

		return result.toString();
	}

	public default String combineData2String(String keyWord, Supplier<String> token1, Supplier<String> token2,
			Supplier<String> token3, Supplier<String> token4, Supplier<String> token5) {
		StringBuilder result = getStringBuilderWithKeyWord(keyWord);

		// there is some data to be put into the string
		// fix the tokens that did not get the child group brackets
		appendGroupedToken(result, token1.get());
		appendGroupedToken(result, token2.get());
		appendGroupedToken(result, token3.get());
		appendGroupedToken(result, token4.get());
		appendGroupedToken(result, token5.get());

		return result.toString();
	}

	@SafeVarargs
	public static String combineData2String(String keyWord, Supplier<String>... aTokens) {
		StringBuilder result = getStringBuilderWithKeyWord(keyWord);

		if (aTokens != null) {
			// there is some data to be put into the string
			// fix the tokens that did not get the child group brackets
			for (int i = 0; i < aTokens.length; ++i) {
				appendGroupedToken(result, aTokens[i].get());
			}
		}

		return result.toString();
	}

	public default String createFullKeyWord(Node base, Node parent, boolean includeParent, KeyWords keyWord) {
		if (!includeParent) {
			if (base == null || base instanceof NullNode) {
				return getKeyWordProvider().getKeyWord(KeyWords.NULL);
			} else if (base instanceof NullListNode) {
				return getKeyWordProvider().getKeyWord(KeyWords.NULL_LIST);
			} else if (base instanceof EmptyListNode) {
				return getKeyWordProvider().getKeyWord(KeyWords.EMPTY_LIST);
			} else {
				return getKeyWordProvider().getKeyWord(keyWord);
			}
		} else {
			if (parent == null) {
				parent = getRelevantParent(base);
			}
			String parentNodeMapping = getMappingForNode(parent, null, 0, false, null);
			if (parentNodeMapping == null) {
				if (base == null || base instanceof NullNode) {
					return getKeyWordProvider().getKeyWord(KeyWords.NULL);
				} else if (base instanceof NullListNode) {
					return getKeyWordProvider().getKeyWord(KeyWords.NULL_LIST);
				} else if (base instanceof EmptyListNode) {
					return getKeyWordProvider().getKeyWord(KeyWords.EMPTY_LIST);
				} else {
					return getKeyWordProvider().getKeyWord(keyWord);
				}
			} else {
				if (base == null || base instanceof NullNode) {
					return getKeyWordProvider().getKeyWord(KeyWords.NULL) + IBasicKeyWords.PARENT_START + parentNodeMapping;
					//+ IBasicKeyWords.PARENT_END;
				} else if (base instanceof NullListNode) {
					return getKeyWordProvider().getKeyWord(KeyWords.NULL_LIST) + IBasicKeyWords.PARENT_START + parentNodeMapping;
					//+ IBasicKeyWords.PARENT_END;
				} else if (base instanceof EmptyListNode) {
					return getKeyWordProvider().getKeyWord(KeyWords.EMPTY_LIST) + IBasicKeyWords.PARENT_START + parentNodeMapping;
					//+ IBasicKeyWords.PARENT_END;
				} else {
					return getKeyWordProvider().getKeyWord(keyWord) + IBasicKeyWords.PARENT_START + parentNodeMapping;
					//+ IBasicKeyWords.PARENT_END;
				}
			}
		}
	}

	public default Node getRelevantParent(Node base) {
		if (base == null) {
			return null;
		}
		Node parent = base.getParentNode().orElse(null);
		if (parent == null) {
			return null;
		} else {
			if (parent instanceof BlockStmt || parent instanceof EnclosedExpr || parent instanceof ExpressionStmt) {
				return getRelevantParent(parent);
			} else {
				return parent;
			}
		}
	}

	public default String applyCombination(Node base, Node parent, boolean includeParent, KeyWords keyWord, int aAbsDepth) {
		if (base == null || aAbsDepth == 0) { // maximum abstraction
			return createFullKeyWord(base, parent, includeParent, keyWord);
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			return combineData2String(createFullKeyWord(base, parent, includeParent, keyWord));
		}
	}

	public default String applyCombination(Node base, Node parent, boolean includeParent, KeyWords keyWord, int aAbsDepth,
			Supplier<String> mapping1) {
		if (base == null || aAbsDepth == 0) { // maximum abstraction
			return createFullKeyWord(base, parent, includeParent, keyWord);
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			return combineData2String(createFullKeyWord(base, parent, includeParent, keyWord), mapping1);
		}
	}

	public default String applyCombination(Node base, Node parent, boolean includeParent, KeyWords keyWord, int aAbsDepth,
			Supplier<String> mapping1, Supplier<String> mapping2) {
		if (base == null || aAbsDepth == 0) { // maximum abstraction
			return createFullKeyWord(base, parent, includeParent, keyWord);
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			return combineData2String(createFullKeyWord(base, parent, includeParent, keyWord), mapping1, mapping2);
		}
	}

	public default String applyCombination(Node base, Node parent, boolean includeParent, KeyWords keyWord, int aAbsDepth,
			Supplier<String> mapping1, Supplier<String> mapping2, Supplier<String> mapping3) {
		if (base == null || aAbsDepth == 0) { // maximum abstraction
			return createFullKeyWord(base, parent, includeParent, keyWord);
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			return combineData2String(createFullKeyWord(base, parent, includeParent, keyWord), mapping1, mapping2, mapping3);
		}
	}

	public default String applyCombination(Node base, Node parent, boolean includeParent, KeyWords keyWord, int aAbsDepth,
			Supplier<String> mapping1, Supplier<String> mapping2, Supplier<String> mapping3,
			Supplier<String> mapping4) {
		if (base == null || aAbsDepth == 0) { // maximum abstraction
			return createFullKeyWord(base, parent, includeParent, keyWord);
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			return combineData2String(
					createFullKeyWord(base, parent, includeParent, keyWord), mapping1, mapping2, mapping3, mapping4);
		}
	}

	public default String applyCombination(Node base, Node parent, boolean includeParent, KeyWords keyWord, int aAbsDepth,
			Supplier<String> mapping1, Supplier<String> mapping2, Supplier<String> mapping3, Supplier<String> mapping4,
			Supplier<String> mapping5) {
		if (base == null || aAbsDepth == 0) { // maximum abstraction
			return createFullKeyWord(base, parent, includeParent, keyWord);
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			return combineData2String(
					createFullKeyWord(base, parent, includeParent, keyWord), mapping1, mapping2, mapping3, mapping4, mapping5);
		}
	}

	public default String applyCombination(Node base, Node parent, boolean includeParent, KeyWords keyWord, int aAbsDepth,
			@SuppressWarnings("unchecked") Supplier<String>... mappings) {
		if (base == null || aAbsDepth == 0) { // maximum abstraction
			return createFullKeyWord(base, parent, includeParent, keyWord);
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			return combineData2String(createFullKeyWord(base, parent, includeParent, keyWord), mappings);
		}
	}

	public default <T> String applyCombinationForList(List<T> base, String keyWord, int aAbsDepth) {
		if (base == null) {
			return getKeyWordProvider().getKeyWord(KeyWords.NULL_LIST);
		}
		if (aAbsDepth == 0) { // maximum abstraction
			return keyWord;
			// } else if (aAbsDepth == 1) { // a little less abstraction
			// return keyWord + base.size();
		} else { // still at a higher level of abstraction (either negative or
					// greater than 1)
			return combineData2String(keyWord + base.size());
		}
	}

	public default <T> String applyCombinationForList(List<T> base, String keyWord, int aAbsDepth,
			Supplier<String> mapping) {
		if (base == null) {
			return getKeyWordProvider().getKeyWord(KeyWords.NULL_LIST);
		}
		if (aAbsDepth == 0) { // maximum abstraction
			return keyWord;
			// } else if (aAbsDepth == 1) { // a little less abstraction
			// return keyWord + base.size();
		} else { // still at a higher level of abstraction (either negative or
					// greater than 1)
			return combineData2String(keyWord + base.size(), mapping);
		}
	}

	public static <T extends Node> List<T> getOrderedNodeList(List<T> list2) {
		List<T> list = new ArrayList<>(list2);
		Collections.sort(list, Node.NODE_BY_BEGIN_POSITION);
		return list;
	}

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
	 * @param nextNodes
	 * a list of child nodes to generate tokens for next
	 * @return A token that represents the mapping with the given depth
	 * @param <T>
	 * the type of objects in the list
	 * @param <P>
	 * the type of parent nodes
	 */
	public default <T extends Node,P extends Node> String getMappingForList(List<T> list, int aAbsDepth,
			QuattroFunction<T, P, Integer, Boolean, List<Node>, String> getMappingForT, boolean alwaysUseFullList,
			List<Node> nextNodes) {
		StringBuilder stringBuilder = new StringBuilder();

		if (list == null) { // this should never happen
			stringBuilder.append(getKeyWordProvider().getKeyWord(KeyWords.NULL));
		} else if (!list.isEmpty()) {
			int bound = getMaxListMembers() < 0 ? list.size() : Math.min(getMaxListMembers(), list.size());
			if (alwaysUseFullList) {
				bound = list.size();
			}

			List<T> orderedNodeList = getOrderedNodeList(list);
			for (int i = 0; i < bound; ++i) {
				stringBuilder.append(String.valueOf(IBasicKeyWords.GROUP_START));
				stringBuilder.append(getMappingForT.apply(orderedNodeList.get(i), null, aAbsDepth, false, nextNodes));
				stringBuilder.append(String.valueOf(IBasicKeyWords.GROUP_END));
			}
		}

		return stringBuilder.toString();
	}

	public default <T extends Node,P extends Node> String getMappingForListWithType(List<T> list,
			QuattroFunction<T, P, Integer, Boolean, List<Node>, String> getMappingForT, boolean alwaysUseFullList,
			int aAbsDepth, List<Node> nextNodes) {
		if (list != null && list.size() > 0) {
			return applyCombinationForList(
					list, String.valueOf(IBasicKeyWords.KEYWORD_LIST), aAbsDepth,
					() -> getMappingForList(list, aAbsDepth - 1, getMappingForT, alwaysUseFullList, nextNodes));
		} else {
			return applyCombinationForList(list, String.valueOf(IBasicKeyWords.KEYWORD_LIST) + '0', 0);
		}
	}

	/**
	 * Creates a mapping for a list of general nodes.
	 * @param nodes
	 * the list of nodes that should be mapped
	 * @param aAbsDepth
	 * the depth of the mapping
	 * @param alwaysUseFullList
	 * whether to always get Tokens for the full list
	 * @param nextNodes
	 * a list of child nodes to generate tokens for next
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForNodeList(List<? extends Node> nodes, boolean alwaysUseFullList, int aAbsDepth, List<Node> nextNodes) {
		return getMappingForListWithType(nodes, this::getMappingForNode, alwaysUseFullList, aAbsDepth, nextNodes);
	}

	/**
	 * Creates a mapping for a list of variable declarators.
	 * @param vars
	 * the list of variable declarators that should be mapped
	 * @param aAbsDepth
	 * the depth of the mapping
	 * @param alwaysUseFullList
	 * whether to always get Tokens for the full list
	 * @param nextNodes
	 * a list of child nodes to generate tokens for next
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForVariableDeclaratorList(List<VariableDeclarator> vars, boolean alwaysUseFullList,
			int aAbsDepth, List<Node> nextNodes) {
		return getMappingForListWithType(vars, this::getMappingForVariableDeclarator, alwaysUseFullList, aAbsDepth, nextNodes);
	}

	/**
	 * Creates a mapping for a list of types.
	 * @param types
	 * the list of types that should be mapped
	 * @param aAbsDepth
	 * the depth of the mapping
	 * @param alwaysUseFullList
	 * whether to always get Tokens for the full list
	 * @param nextNodes
	 * a list of child nodes to generate tokens for next
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForTypeList(List<? extends Type> types, boolean alwaysUseFullList, int aAbsDepth, List<Node> nextNodes) {
		return getMappingForListWithType(types, this::getMappingForType, alwaysUseFullList, aAbsDepth, nextNodes);
	}

	/**
	 * Creates a mapping for a list of statements.
	 * @param statements
	 * the list of statements that should be mapped
	 * @param aAbsDepth
	 * the depth of the mapping
	 * @param alwaysUseFullList
	 * whether to always get Tokens for the full list
	 * @param nextNodes
	 * a list of child nodes to generate tokens for next
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForStatementList(List<? extends Statement> statements, boolean alwaysUseFullList,
			int aAbsDepth, List<Node> nextNodes) {
		return getMappingForListWithType(statements, this::getMappingForStatement, alwaysUseFullList, aAbsDepth, nextNodes);
	}

	/**
	 * Creates a mapping for a list of arguments.
	 * @param parameters
	 * the list of parameters
	 * @param aAbsDepth
	 * the depth of the mapping
	 * @param alwaysUseFullList
	 * whether to always get Tokens for the full list
	 * @param nextNodes
	 * a list of child nodes to generate tokens for next
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForParameterList(List<Parameter> parameters, boolean alwaysUseFullList,
			int aAbsDepth, List<Node> nextNodes) {
		return getMappingForListWithType(parameters, this::getMappingForParameter, alwaysUseFullList, aAbsDepth, nextNodes);
	}

	/**
	 * Creates a mapping for a list of expressions.
	 * @param expressions
	 * the list of expressions
	 * @param aAbsDepth
	 * the depth of the mapping
	 * @param alwaysUseFullList
	 * whether to always get Tokens for the full list
	 * @param nextNodes
	 * a list of child nodes to generate tokens for next
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForExpressionList(List<? extends Expression> expressions, boolean alwaysUseFullList,
			int aAbsDepth, List<Node> nextNodes) {
		return getMappingForListWithType(expressions, this::getMappingForExpression, alwaysUseFullList, aAbsDepth, nextNodes);
	}

	/**
	 * Creates a mapping for a list of array creation levels.
	 * @param levels
	 * the list of array creation levels
	 * @param aAbsDepth
	 * the depth of the mapping
	 * @param alwaysUseFullList
	 * whether to always get Tokens for the full list
	 * @param nextNodes
	 * a list of child nodes to generate tokens for next
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForArrayCreationLevelList(List<ArrayCreationLevel> levels,
			boolean alwaysUseFullList, int aAbsDepth, List<Node> nextNodes) {
		return getMappingForListWithType(levels, this::getMappingForArrayCreationLevel, alwaysUseFullList, aAbsDepth, nextNodes);
	}

	/**
	 * Creates a mapping for a list of body declarations.
	 * @param bodyDeclarations
	 * the list of body declarations
	 * @param aAbsDepth
	 * the depth of the mapping
	 * @param alwaysUseFullList
	 * whether to always get Tokens for the full list
	 * @param nextNodes
	 * a list of child nodes to generate tokens for next
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForBodyDeclarationList(List<? extends BodyDeclaration<?>> bodyDeclarations,
			boolean alwaysUseFullList, int aAbsDepth, List<Node> nextNodes) {
		return getMappingForListWithType(
				bodyDeclarations, this::getMappingForBodyDeclaration, alwaysUseFullList, aAbsDepth, nextNodes);
	}

	/**
	 * Creates a mapping for a list of body declarations.
	 * @param types
	 * the list of body declarations
	 * @param aAbsDepth
	 * the depth of the mapping
	 * @param alwaysUseFullList
	 * whether to always get Tokens for the full list
	 * @param nextNodes
	 * a list of child nodes to generate tokens for next
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingForClassOrInterfaceTypeList(List<ClassOrInterfaceType> types,
			boolean alwaysUseFullList, int aAbsDepth, List<Node> nextNodes) {
		return getMappingForListWithType(types, this::getMappingForType, alwaysUseFullList, aAbsDepth, nextNodes);
	}

	/**
	 * Creates a mapping for a list of type parameters.
	 * @param typeParameters
	 * the list of type parameters
	 * @param aAbsDepth
	 * the depth of the mapping
	 * @param alwaysUseFullList
	 * whether to always get Tokens for the full list
	 * @param nextNodes
	 * a list of child nodes to generate tokens for next
	 * @return A token that represents the mapping with the given depth
	 */
	public default String getMappingsForTypeParameterList(List<TypeParameter> typeParameters, boolean alwaysUseFullList,
			int aAbsDepth, List<Node> nextNodes) {
		return getMappingForListWithType(
				typeParameters, this::getMappingForTypeParameter, alwaysUseFullList, aAbsDepth, nextNodes);
	}

	@FunctionalInterface
	public interface QuattroFunction<T, P, U, B, L, R> {

		R apply(T t, P p, U u, B b, L l);
	}
}
