package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.Function;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CloneableIterator;

public interface ReplaceableCloneableIterator<T> extends CloneableIterator<T> {

	public T processNextAndReplaceWithResult(Function<T,T> function);

	public void setToPosition(long i);
	
}