package se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider;

/**
 * Maps nodes to sequences of tokens that are either the abstract identifiers themselves, 
 * or they wrap the identifiers and various information of the respecting nodes in the following 
 * manner:
 * 
 * <p> {@code ($NODE_IDENTIFIER) ($NODE_IDENTIFIER;[list,with,information],information) ($NODE_IDENTIFIER;more,information) ...}
 * 
 * @author Simon
 */
public class Node2AbstractionTokenMapper extends SimpleMapper<String> implements IAbstractionMapper {

	public Node2AbstractionTokenMapper(IKeyWordProvider<String> provider) {
		super(provider);
	}
	
}
