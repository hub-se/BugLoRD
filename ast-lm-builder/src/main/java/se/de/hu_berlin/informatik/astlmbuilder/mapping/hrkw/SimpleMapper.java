package se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw;

import java.util.Collection;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.ITokenMapper;

/**
 * The simple mapper only maps a node to a single keyword depending on the type.
 * It basically always assumes a depth of 0 and therefore ignoring this argument completely
 * 
 */
public class SimpleMapper extends KeyWordConstants implements ITokenMapper<String> {
	
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

}
