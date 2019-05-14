package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

public interface ReplaceableCloneableIterator<T> extends CloneableIterator<T> {

	public T processNextAndReplaceWithResult(Function<T,T> function);
	
}