package se.de.hu_berlin.informatik.astlmbuilder;

import com.github.javaparser.ast.*;

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
	public TokenWrapper getMappingForNode( Node aNode ) {
		return new TokenWrapper(
				mapper.getMappingForNode(aNode), 
				aNode.getBeginLine(),
				aNode.getEndLine());
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.ITokenMapper#getClosingToken(com.github.javaparser.ast.Node)
	 */
	@Override
	public TokenWrapper getClosingToken(Node aNode) {
		String closingToken = mapper.getClosingToken(aNode);
		if (closingToken == null) {
			return null;
		} else {
			//TODO maybe it has to be end line + 1...
			return new TokenWrapper(
					closingToken, 
					aNode.getEndLine(),
					aNode.getEndLine());
		}
	}
	
}
