package se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IBasicKeyWords;

public interface IAbstractionMapperBasics extends IMapper<String> {
	
	public int getMaxListMembers();
	
	/**
	 * All tokens will be put together into one string that can be parsed later.
	 * <p> General format for elements with 
	 * <br> maximum abstraction: {@code $node_id}, and
	 * <br> other abstraction level: {@code $node_id[member_1][member_2]...[member_n]},
	 * <br> where each {@code member_k} is again an element itself.
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
		//result.append( IBasicKeyWords.BIG_GROUP_START );
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
					fixedTokens[i] = "" + IBasicKeyWords.GROUP_START + IBasicKeyWords.KEYWORD_NULL + IBasicKeyWords.GROUP_END;
				} else if( fixedT.length() == 0 ) {
					fixedTokens[i] = IBasicKeyWords.GROUP_START + "" + IBasicKeyWords.GROUP_END;
				} else if (fixedT.charAt(0) != IBasicKeyWords.GROUP_START) {
					fixedTokens[i] = IBasicKeyWords.GROUP_START + fixedT + IBasicKeyWords.GROUP_END;
				} else {
					fixedTokens[i] = fixedT;
				}
			}

			// String.join does not work for chars :(
			for (int i = 0; i < fixedTokens.length; ++i) {
				result.append(fixedTokens[i]);
			}
		}

		//result.append(IBasicKeyWords.BIG_GROUP_END);

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
		StringBuilder stringBuilder = new StringBuilder();

		if (list == null) { //this should never happen, actually
			stringBuilder.append(IBasicKeyWords.KEYWORD_NULL);
		} else if (!list.isEmpty()) {
			int bound = getMaxListMembers() < 0 ? list.size()
					: Math.min(getMaxListMembers(), list.size());
			if (alwaysUseFullList) {
				bound = list.size();
			}
			
			for (int i = 0; i < bound; ++i) {
				stringBuilder.append(String.valueOf(IBasicKeyWords.GROUP_START));
				stringBuilder.append(getMappingForT.apply(list.get(i), aAbsDepth));
				stringBuilder.append(String.valueOf(IBasicKeyWords.GROUP_END));
			}
		}

		return stringBuilder.toString();
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
	
	
}
