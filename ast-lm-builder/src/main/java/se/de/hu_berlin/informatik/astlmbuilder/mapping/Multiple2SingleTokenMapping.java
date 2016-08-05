package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import com.github.javaparser.ast.Node;

/**
 * Combines multiple tokens that are possibly generated for a node
 * into a single tokens by concatenation of the strings.
 * 
 * @author Simon
 */
public class Multiple2SingleTokenMapping implements ITokenMapper<String> {

	final private ITokenMapper<String> mapper;
	
	public Multiple2SingleTokenMapping(ITokenMapper<String> mapper) {
		super();
		this.mapper = mapper;
	}

	@Override
	public MappingWrapper<String> getMappingForNode(Node aNode) {
		return combineTokens(mapper.getMappingForNode(aNode));
	}

	@Override
	public String getClosingToken(Node aNode) {
		return mapper.getClosingToken(aNode);
	}
	
	private MappingWrapper<String> combineTokens(MappingWrapper<String> mapping) {
		if (mapping.getNumberOfMappings() <= 1) {
			return mapping;
		} else {
			String result = "";
			for (String token : mapping.getMappings()) {
				result += token;
			}
			return new MappingWrapper<>(result);
		}
	}
}
