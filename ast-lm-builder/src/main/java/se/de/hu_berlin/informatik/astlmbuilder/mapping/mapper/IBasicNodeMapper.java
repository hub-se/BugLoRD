package se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper;

import java.util.Collection;
import com.github.javaparser.ast.Node;

public interface IBasicNodeMapper<T> {

	/**
	 * Returns the mapping of the abstract syntax tree node to fit the language
	 * model
	 * @param aNode
	 * The node that will be used for the creation of the tokens
	 * @param aDepth
	 * The depth of serialization or abstraction
	 * @param includeParent
	 * whether to include information about the parent node
	 * @return A token that represents the nodes according to the used method
	 * and depth
	 */
	public T getMappingForNode(Node aNode, int aDepth, boolean includeParent);

	public T getClosingMapping(T mapping);

	public boolean isClosingMapping(T mapping);

	public T concatenateMappings(T firstMapping, T secondMapping);

	/**
	 * Passes a black list of method names to the mapper.
	 * 
	 * @param aBL
	 * a collection of method names that should be handled differently
	 */
	public void setPrivateMethodBlackList(Collection<String> aBL);

	/**
	 * @return the black list of method names; null if not set
	 */
	public Collection<String> getPrivateMethodBlackList();

	/**
	 * Clears the black list of method names from this mapper
	 */
	public void clearPrivateMethodBlackList();

}
