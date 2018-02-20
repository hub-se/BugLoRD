package se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper;

import com.github.javaparser.ast.Node;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider;

/**
 * Maps nodes to sequences of tokens that are either the abstract identifiers themselves, 
 * or they wrap the identifiers and various information of the respecting nodes in the following 
 * manner:
 * 
 * <p> General format for elements with 
 * <br> maximum abstraction: {@code node_id}, and
 * <br> other abstraction level: {@code (node_id,[member_1],[member_2],...,[member_n])},
 * <br> where each {@code member_k} is again an element itself.
 * 
 * @author Simon
 */
public class Node2AbstractionMapperWithMetaData extends Node2AbstractionMapper {

	private final int childCountStepWidth;
	
	protected Node2AbstractionMapperWithMetaData(Builder builder) {
		super(builder);
		this.childCountStepWidth = builder.childCountStepWidth;
	}

	@Override
	public String finalizeMapping(String mapping, Node aNode, int aDepth, boolean includeParent) {
		String result = super.finalizeMapping(mapping, aNode, aDepth, includeParent);
		
		if (this.childCountStepWidth > 0 && result != null && aNode != null) {
			int childCount = getNumberOfChildNodes(aNode);
			if (childCount > 0) {
				// ceil ( log_stepWidth(childCount) )
				int group = (int) Math.ceil(Math.log10(childCount)/Math.log10(childCountStepWidth));
				result += "(" + group + ")";
			}
		}
		
		return result;
	}

	private static int getNumberOfChildNodes(Node node) {
		int result = 0;
		for (Node child : node.getChildNodes()) {
			result += getNumberOfChildNodes(child);
		}
		result += node.getChildNodes().size();
		return result;
	}
	
	public static class Builder extends Node2AbstractionMapper.Builder {

		private int childCountStepWidth = 0;
		
		/**
		 * Creates an {@link Builder} object with the given parameters.
		 * @param provider
		 * a keyword provider
		 */
		public Builder(IKeyWordProvider<String> provider) {
			super(provider);
		}
		
		public Builder setchildCountStepWidth(int childCountStepWidth) {
			this.childCountStepWidth = childCountStepWidth;
			return this;
		}
		
		@Override
		public Node2AbstractionMapper build() {
			return new Node2AbstractionMapperWithMetaData(this);
		}

	}

}
