package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import java.util.Collection;

import com.github.javaparser.ast.Node;

/**
 * Combines multiple tokens that are possibly generated for a node
 * into a single tokens by concatenation of the strings.
 * 
 * @author Simon
 */
public class Multiple2SingleTokenMapping<V> implements ITokenMapper<String,V> {

	final private ITokenMapper<String,V> mapper;
	
	public Multiple2SingleTokenMapping(ITokenMapper<String,V> mapper) {
		super();
		this.mapper = mapper;
	}

	@Override
	public MappingWrapper<String> getMappingForNode(Node aNode, @SuppressWarnings("unchecked") V... values) {
		return combineTokens(mapper.getMappingForNode(aNode, values));
	}

	@Override
	public String getClosingToken(Node aNode, @SuppressWarnings("unchecked") V... values) {
		return mapper.getClosingToken(aNode, values);
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

	@Override
	public void setPrivMethodBlackList(Collection<String> aBL) {
		mapper.setPrivMethodBlackList( aBL );	
	}

	@Override
	public void clearPrivMethodBlackList() {
		mapper.clearPrivMethodBlackList();	
	}
}
