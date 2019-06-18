package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.Function;

public interface ReplaceableCloneableIterator<T> extends CloneableIterator<T> {

	public T processNextAndReplaceWithResult(Function<T,T> function);
	
}