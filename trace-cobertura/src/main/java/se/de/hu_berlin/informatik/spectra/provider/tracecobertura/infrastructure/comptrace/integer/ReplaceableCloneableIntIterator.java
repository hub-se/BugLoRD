package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.Function;

public interface ReplaceableCloneableIntIterator {

	public int processNextAndReplaceWithResult(Function<Integer,Integer> function);
	
	public ReplaceableCloneableIntIterator clone();
	
	public int peek();
	
	public boolean hasNext();
	
	public int next();
	
}