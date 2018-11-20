package se.de.hu_berlin.informatik.spectra.core.traces;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CompressedTraceBase;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CloneableIterator;

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
		CloneableIterator<Integer> indexedFullTrace = iterator();
		List<Integer> fullTrace = new ArrayList<>();
		while (indexedFullTrace.hasNext()) {
			Iterator<Integer> sequence = indexer.getSequenceIterator(indexedFullTrace.next());
			while (sequence.hasNext()) {
				fullTrace.add(sequence.next());
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
		for (Entry<Integer, int[]> i : getRepetitionMarkers().entrySet()) {
			max = Math.max(i.getKey(), max);
			max = Math.max(i.getValue()[0], max);
			max = Math.max(i.getValue()[1], max);
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
			
			CloneableIterator<Integer> iterator = ExecutionTrace.this.iterator();
			Iterator<Integer> currentSequence;

			@Override
			public boolean hasNext() {
				if (currentSequence == null || !currentSequence.hasNext()) {
					while (iterator.hasNext()) {
						currentSequence = indexer.getSequenceIterator(iterator.next());
						if (currentSequence.hasNext()) {
							// found a "good" sequence
							break;
						}
						currentSequence = null;
					}
					
					// found no sequence?
					if (currentSequence == null) {
						return false;
					}
				}
				
				return true;
			}

			@Override
			public Integer next() {
				return currentSequence.next();
			}};
	}
	
}
