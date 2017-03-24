package se.de.hu_berlin.informatik.astlmbuilder.wrapper;

import java.util.Collection;
import com.github.javaparser.ast.*;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.ITokenMapper;

public class Node2TokenWrapperMapping implements ITokenMapper<TokenWrapper> {
	
	private final ITokenMapper<String> mapper;
	
	public Node2TokenWrapperMapping(ITokenMapper<String> mapper) {
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
	public TokenWrapper getMappingForNode(Node aNode, int aDepth) {
		return new TokenWrapper(mapper.getMappingForNode(aNode, aDepth), 
				aNode.getRange().begin.line, aNode.getRange().end.line);
	}

	@Override
	public TokenWrapper getClosingToken(Node aNode) {
		String closingToken = mapper.getClosingToken(aNode);
		if (closingToken == null) {
			return null;
		} else {
			return new TokenWrapper(
					closingToken, 
					aNode.getRange().begin.line, 
					aNode.getRange().end.line);
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
