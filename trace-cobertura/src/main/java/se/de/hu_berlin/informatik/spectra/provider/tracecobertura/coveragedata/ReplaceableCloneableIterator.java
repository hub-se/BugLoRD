package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import java.util.function.Function;

public interface ReplaceableCloneableIterator<T> extends CloneableIterator<T> {

	public T processNextAndReplaceWithResult(Function<T,T> function);
	
}