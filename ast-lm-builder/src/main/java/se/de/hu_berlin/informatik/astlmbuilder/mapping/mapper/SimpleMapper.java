package se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper;

import java.util.Collection;
import java.util.Collections;
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
	
	// a collection of blacklisted local method names
	// the simple mapper makes no use of this
	public Collection<String> privMethodBL = Collections.emptyList();

	@Override
	public void setPrivateMethodBlackList(Collection<String> aBL) {
		privMethodBL = aBL;
	}

	@Override
	public void clearPrivateMethodBlackList() {
		privMethodBL = Collections.emptyList();
	}

	@Override
	public IKeyWordProvider<T> getKeyWordProvider() {
		return provider;
	}

	@Override
	public Collection<String> getPrivateMethodBlackList() {
		return privMethodBL;
	}

	@Override
	public T getClosingMapping(T mapping) {
		return provider.markAsClosing(mapping);
	}
	
	@Override
	public boolean isClosingMapping(T mapping) {
		return provider.isMarkedAsClosing(mapping);
	}

	@Override
	public T concatenateMappings(T firstMapping, T secondMapping) {
		throw new UnsupportedOperationException("Cannot concatenate mappings.");
	}

}
