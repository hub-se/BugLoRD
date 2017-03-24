package se.de.hu_berlin.informatik.astlmbuilder.mapping.shortKW;

import java.util.Collection;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.IAbsTokenMapper;

/**
 * Maps nodes to sequences of tokens that are either the abstract identifiers themselves, 
 * or they wrap the identifiers and various information of the respecting nodes in the following 
 * manner:
 * 
 * <p> {@code ($NODE_IDENTIFIER) ($NODE_IDENTIFIER;[list,with,information],information) ($NODE_IDENTIFIER;more,information) ...}
 * 
 * @author Simon
 */
public class Node2AbstractionTokenMapperShort extends KeyWordConstantsShort implements IAbsTokenMapper {

	// a collection of blacklisted private method names
	// the simple mapper makes no use of this
	public Collection<String> privMethodBL = null;
	
	@Override
	public void setPrivMethodBlackList(Collection<String> aBL) {
		privMethodBL = aBL;
	}

	@Override
	public void clearPrivMethodBlackList() {
		privMethodBL = null;
	}

	@Override
	public Collection<String> getPrivMethodBlackList() {
		return privMethodBL;
	}
	
}
