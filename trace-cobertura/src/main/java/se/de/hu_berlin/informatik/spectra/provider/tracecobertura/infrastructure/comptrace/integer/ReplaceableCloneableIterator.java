package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.Function;

public interface ReplaceableCloneableIterator {

	public int processNextAndReplaceWithResult(Function<Integer,Integer> function);
	
	public ReplaceableCloneableIterator clone();
	
	public int peek();
	
	public boolean hasNext();
	
	public int next();
	
}