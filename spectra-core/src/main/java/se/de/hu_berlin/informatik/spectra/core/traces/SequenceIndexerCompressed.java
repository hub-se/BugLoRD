package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.Iterator;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.EfficientCompressedIntegerTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.TraceIterator;

public interface SequenceIndexerCompressed {

	public void removeFromSequences(int index);

	int[][] getSubTraceIdSequences();

	EfficientCompressedIntegerTrace[] getNodeIdSequences();

	int[] getSubTraceIdSequence(int index);

	EfficientCompressedIntegerTrace getNodeIdSequence(int subTraceIndex);

	/**
	 * Iterates over the sequence of sub trace IDs 
	 * with the given index and each node ID in the
	 * respective indexed sub trace.
	 * @param index
	 * an index of a sequence of sub trace IDs
	 * @return
	 * an iterator over all sub traces in the 
	 * specified sequence of sub trace IDs
	 */
	Iterator<Integer> getFullSequenceIterator(int index);

	/**
	 * Iterates over the sub trace with the given index.
	 * The sub trace contains spectra node IDs.
	 * @param index
	 * an index of a sub trace 
	 * @return
	 * an iterator over the specified sub trace
	 */
	TraceIterator getNodeIdSequenceIterator(int index);
	
	/**
	 * Iterates over the sequence of sub trace IDs 
	 * with the given index.
	 * @param index
	 * an index of a sequence of sub trace IDs
	 * @return
	 * an iterator over the specified sequence of sub trace IDs
	 */
	Iterator<Integer> getSubTraceIDSequenceIterator(int index);
	
}
