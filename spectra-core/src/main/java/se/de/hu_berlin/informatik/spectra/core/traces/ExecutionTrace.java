package se.de.hu_berlin.informatik.spectra.core.traces;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CompressedTraceBase;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.TraceIterator;

/**
 * An execution trace consists structurally of a list of executed nodes (or references to node lists)
 * and a list of tuples that mark repeated sequences in the trace.
 *
 */
public class ExecutionTrace extends CompressedTraceBase<Integer, Integer> implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -7333694882324910595L;

	public ExecutionTrace(SingleLinkedArrayQueue<Integer> trace, boolean log) {
		super(trace, log);
	}
	
	public ExecutionTrace(SingleLinkedArrayQueue<Integer> trace, CompressedTraceBase<?, ?> otherCompressedTrace) {
		super(trace, otherCompressedTrace);
	}

	public ExecutionTrace(Integer[] compressedTrace, List<int[]> repMarkerLists, int index) {
		super(compressedTrace, repMarkerLists, index);
	}
	
	@Override
	public CompressedTraceBase<Integer, Integer> newChildInstance(SingleLinkedArrayQueue<Integer> trace,
			CompressedTraceBase<?, ?> otherCompressedTrace) {
		return new ExecutionTrace(trace, otherCompressedTrace);
	}

	@Override
	public CompressedTraceBase<Integer, Integer> newChildInstance(Integer[] compressedTrace, 
			List<int[]> repMarkerLists, int index) {
		return new ExecutionTrace(compressedTrace, repMarkerLists, index);
	}
	
	@Override
	public CompressedTraceBase<Integer, Integer> newChildInstance(SingleLinkedArrayQueue<Integer> trace, boolean log) {
		return new ExecutionTrace(trace, log);
	}
	

	public int[] reconstructFullMappedTrace(SequenceIndexer indexer) {
		Integer[] indexedFullTrace = reconstructTrace();
		List<Integer> fullTrace = new ArrayList<>();
		for (int index : indexedFullTrace) {
			int[] sequence = indexer.getSequence(index);
			for (int i : sequence) {
				fullTrace.add(i);
			}
		}
		return fullTrace.stream().mapToInt(i -> i).toArray();
	}

	@Override
	public int getMaxStoredValue() {
		if (getChild() == null) {
			int max = 0;
			for (int i : getCompressedTrace()) {
				max = Math.max(i, max);
			}
			return max;
		}
		
		int max = getChild().getMaxStoredValue();
		for (int i : getRepetitionMarkers()) {
			max = Math.max(i, max);
		}
		return max;
	}

	@Override
	public boolean isEqual(Integer first, Integer second) {
		return first == second;
	}

	@Override
	public Integer getRepresentation(Integer element) {
		return element;
	}

	@Override
	public Integer[] newArrayOfSize(int size) {
		return new Integer[size];
	}
	
	
	public Iterator<Integer> mappedIterator(SequenceIndexer indexer) {
		return new Iterator<Integer>(){
			
			TraceIterator<Integer> iterator = ExecutionTrace.this.iterator();
			int[] currentSequence;
			int currentIndex = 0;

			@Override
			public boolean hasNext() {
				if (currentSequence == null || currentIndex >= currentSequence.length) {
					while (iterator.hasNext()) {
						currentSequence = indexer.getSequence(iterator.next());
						if (currentSequence.length > 0) {
							// found a "good" sequence
							break;
						}
						currentSequence = null;
					}
					
					// found no sequence?
					if (currentSequence == null) {
						return false;
					}
					// reset current index
					currentIndex = 0;
				}
				
				return true;
			}

			@Override
			public Integer next() {
				return currentSequence[currentIndex++];
			}};
	}
	
}
