package se.de.hu_berlin.informatik.spectra.core.traces;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CloneableIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CompressedIntegerTraceBase;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CompressedTraceBase;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.IntTraceIterator;

import java.util.Objects;

/**
 * An execution trace consists structurally of a list of executed nodes (or references to node lists)
 * and a list of tuples that mark repeated sequences in the trace.
 *
 */
public class ExecutionTrace extends CompressedIntegerTraceBase implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -7333694882324910595L;

	public ExecutionTrace(BufferedIntArrayQueue trace, boolean log) {
		super(trace, log);
	}
	
	public ExecutionTrace(BufferedIntArrayQueue trace, CompressedIntegerTraceBase otherCompressedTrace) {
		super(trace, otherCompressedTrace);
	}

	public ExecutionTrace(BufferedIntArrayQueue compressedTrace, BufferedArrayQueue<int[]> repMarkerLists, int index) {
		super(compressedTrace, repMarkerLists, index);
	}
	
	@Override
	public CompressedIntegerTraceBase newChildInstance(BufferedIntArrayQueue trace,
			CompressedIntegerTraceBase otherCompressedTrace) {
		return new ExecutionTrace(trace, otherCompressedTrace);
	}

	@Override
	public CompressedIntegerTraceBase newChildInstance(BufferedIntArrayQueue compressedTrace, 
			BufferedArrayQueue<int[]> repMarkerLists, int index) {
		return new ExecutionTrace(compressedTrace, repMarkerLists, index);
	}
	
	@Override
	public CompressedIntegerTraceBase newChildInstance(BufferedIntArrayQueue trace, boolean log, int iteration) {
		return new ExecutionTrace(trace, log);
	}
	

	public int[] reconstructFullMappedTrace(SequenceIndexer indexer) {
		IntTraceIterator indexedFullTrace = iterator();
		List<Integer> fullTrace = new ArrayList<>();
		while (indexedFullTrace.hasNext()) {
			Iterator<Integer> sequence = indexer.getFullSequenceIterator(indexedFullTrace.next());
			while (sequence.hasNext()) {
				fullTrace.add(sequence.next());
			}
		}
		return fullTrace.stream().mapToInt(i -> i).toArray();
	}
	
	public Iterator<Integer> mappedIterator(SequenceIndexer indexer) {
		return new Iterator<Integer>(){
			
			final IntTraceIterator iterator = ExecutionTrace.this.iterator();
			Iterator<Integer> currentSequence;

			@Override
			public boolean hasNext() {
				if (currentSequence == null || !currentSequence.hasNext()) {
					while (iterator.hasNext()) {
						currentSequence = indexer.getFullSequenceIterator(iterator.next());
						if (currentSequence.hasNext()) {
							// found a "good" sequence
							break;
						}
						currentSequence = null;
					}
					
					// found no sequence?
                    return currentSequence != null;
				}
				
				return true;
			}

			@Override
			public Integer next() {
				return currentSequence.next();
			}};
	}
	
}
