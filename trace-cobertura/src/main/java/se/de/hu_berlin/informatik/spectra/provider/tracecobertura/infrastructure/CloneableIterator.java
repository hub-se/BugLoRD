package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import java.util.Iterator;

public interface CloneableIterator<T> extends Iterator<T> {

	public CloneableIterator<T> clone();
	
	public T peek();
	
}