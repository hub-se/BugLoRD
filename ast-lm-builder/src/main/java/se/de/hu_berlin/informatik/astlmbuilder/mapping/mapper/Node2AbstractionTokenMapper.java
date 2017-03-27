package se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider;

/**
 * Maps nodes to sequences of tokens that are either the abstract identifiers themselves, 
 * or they wrap the identifiers and various information of the respecting nodes in the following 
 * manner:
 * 
 * <p> General format for elements with 
 * <br> maximum abstraction: {@code $node_id}, and
 * <br> other abstraction level: {@code ($node_id,[member_1],[member_2],...,[member_n])},
 * <br> where each {@code member_k} is again an element itself.
 * 
 * @author Simon
 */
public class Node2AbstractionTokenMapper extends SimpleMapper<String> implements IAbstractionMapper {

	final private int maxListMembers;
	
	public Node2AbstractionTokenMapper(IKeyWordProvider<String> provider, int maxListMembers) {
		super(provider);
		this.maxListMembers = maxListMembers;
	}
	
	public Node2AbstractionTokenMapper(IKeyWordProvider<String> provider) {
		this(provider, -1);
	}

	@Override
	public int getMaxListMembers() {
		return maxListMembers;
	}
	
}
