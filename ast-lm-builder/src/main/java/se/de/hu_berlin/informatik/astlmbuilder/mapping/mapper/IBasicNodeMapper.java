package se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper;

import java.util.Collection;
import java.util.List;

import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;

public interface IBasicNodeMapper<T> {

	static class EmptyListNode extends Node {

		public EmptyListNode(TokenRange tokenRange) {
			super(tokenRange);
		}

		@Override
		public <A> void accept(VoidVisitor<A> v, A arg) {
			// nothing
		}

		@Override
		public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
			// nothing
			return null;
		}
		
	}

	static class NullListNode extends Node {

		public NullListNode(TokenRange tokenRange) {
			super(tokenRange);
		}

		@Override
		public <A> void accept(VoidVisitor<A> v, A arg) {
			// nothing
		}

		@Override
		public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
			// nothing
			return null;
		}
	}
	
	static class NullNode extends Node {

		public NullNode(TokenRange tokenRange) {
			super(tokenRange);
		}

		@Override
		public <A> void accept(VoidVisitor<A> v, A arg) {
			// nothing
		}

		@Override
		public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
			// nothing
			return null;
		}
	}

	/**
	 * Returns the mapping of the abstract syntax tree node to fit the language
	 * model
	 * @param aNode
	 * The node that will be used for the creation of the tokens
	 * @param aDepth
	 * The depth of serialization or abstraction
	 * @param parent
	 * the parent node, if any; null otherwise
	 * @param includeParent
	 * whether to include information about the parent node
	 * @param nextNodes
	 * a list of child nodes to generate tokens for next
	 * @return A token that represents the nodes according to the used method
	 * and depth
	 */
	public T getMappingForNode(Node aNode, Node parent, int aDepth, boolean includeParent, List<Node> nextNodes);

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
