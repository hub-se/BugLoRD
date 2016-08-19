package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.javaparser.ast.*;

import se.de.hu_berlin.informatik.astlmbuilder.TokenWrapper;

public class Node2TokenWrapperMapping<V> implements ITokenMapperShort<TokenWrapper,V> {
	
	private final ITokenMapperShort<String,V> mapper;
	
	public Node2TokenWrapperMapping(ITokenMapperShort<String,V> mapper) {
		super();
		this.mapper = mapper;
	}

	/**
	 * Returns a TokenWrapper object with the help of the given
	 * TokenMapper object that maps nodes to Strings.
	 * @param aNode The node that should be mapped
	 * @return a TokenWrapper object
	 */
	@Override
	public MappingWrapper<TokenWrapper> getMappingForNode(Node aNode, @SuppressWarnings("unchecked") V... values) {
		MappingWrapper<String> mapping = mapper.getMappingForNode(aNode, values);
		List<TokenWrapper> tokens = new ArrayList<>(mapping.getNumberOfMappings());
		
		for (String token : mapping.getMappings()) {
			tokens.add(new TokenWrapper(token, 
					aNode.getBeginLine(), aNode.getEndLine()));
		}
		
		return new MappingWrapper<>(tokens);
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.ITokenMapper#getClosingToken(com.github.javaparser.ast.Node)
	 */
	@Override
	public TokenWrapper getClosingToken(Node aNode, @SuppressWarnings("unchecked") V... values) {
		String closingToken = mapper.getClosingToken(aNode, values);
		if (closingToken == null) {
			return null;
		} else {
			return new TokenWrapper(
					closingToken, 
					aNode.getEndLine(),
					aNode.getEndLine());
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
