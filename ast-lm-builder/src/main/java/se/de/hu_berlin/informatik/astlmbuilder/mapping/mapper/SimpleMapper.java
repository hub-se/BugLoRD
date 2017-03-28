package se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper;

import java.util.Collection;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider;

/**
 * The simplest form of token mapper. Merely uses the default 
 * methods from the IMapper interface. 
 */
public class SimpleMapper<T> implements IMapper<T> {
	
	final private IKeyWordProvider<T> provider;
	
	public SimpleMapper(IKeyWordProvider<T> provider) {
		super();
		this.provider = provider;
	}
	
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
	public IKeyWordProvider<T> getKeyWordProvider() {
		return provider;
	}

	@Override
	public Collection<String> getPrivMethodBlackList() {
		return privMethodBL;
	}

}
