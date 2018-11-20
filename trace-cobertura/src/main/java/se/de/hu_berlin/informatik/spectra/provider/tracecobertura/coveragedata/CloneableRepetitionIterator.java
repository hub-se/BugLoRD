package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

public interface CloneableRepetitionIterator<T> extends CloneableIterator<T> {

	public boolean isStartOfRepetition();
	
	public boolean isEndOfRepetition();
	
}