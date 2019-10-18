package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.longs;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.Function;

public interface ReplaceableCloneableLongIterator {

	public long processNextAndReplaceWithResult(Function<Long,Long> function);
	
	public ReplaceableCloneableLongIterator clone();
	
	public long peek();
	
	public boolean hasNext();
	
	public long next();
	
}