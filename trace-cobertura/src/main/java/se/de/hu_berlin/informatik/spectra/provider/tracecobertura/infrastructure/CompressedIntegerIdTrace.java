package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * An execution trace consists structurally of a list of executed nodes
 * and a list of tuples that mark repeated sequences in the trace.
 *
 */
public class CompressedIntegerIdTrace extends CompressedTraceBase<Integer,Integer> implements Serializable, Iterable<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3861000625585356336L;

	public CompressedIntegerIdTrace(BufferedArrayQueue<Integer> trace, boolean log) {
		super(trace, log);
	}

	public CompressedIntegerIdTrace(BufferedArrayQueue<Integer> trace, CompressedTraceBase<?, ?> otherCompressedTrace) {
		super(trace, otherCompressedTrace);
	}

	public CompressedIntegerIdTrace(BufferedArrayQueue<Integer> compressedTrace, 
			BufferedArrayQueue<int[]> repetitionMarkers, int index) {
		super(compressedTrace, repetitionMarkers, index);
	}

	@Override
	public CompressedTraceBase<Integer, Integer> newChildInstance(BufferedArrayQueue<Integer> trace,
			CompressedTraceBase<?, ?> otherCompressedTrace) {
		return new CompressedIntegerIdTrace(trace, otherCompressedTrace);
	}

	@Override
	public CompressedTraceBase<Integer, Integer> newChildInstance(BufferedArrayQueue<Integer> compressedTrace,
			BufferedArrayQueue<int[]> repMarkerLists, int index) {
		return new CompressedIntegerIdTrace(compressedTrace, repMarkerLists, index);
	}
	
	@Override
	public CompressedTraceBase<Integer, Integer> newChildInstance(BufferedArrayQueue<Integer> trace, boolean log, int iteration) {
		return new CompressedIntegerIdTrace(trace, log);
	}

	@Override
	public boolean isEqual(Integer first, Integer second) {
		return first.equals(second);
	}

	@Override
	public Integer getRepresentation(Integer element) {
		return element;
	}
	
	@Override
	public long getMaxStoredValue() {
		if (getChild() == null) {
			long max = 0;
			for (Integer i : getCompressedTrace()) {
				max = Math.max(i, max);
			}
			return max;
		} else {
			long max = getChild().getMaxStoredValue();
			Iterator<Entry<Integer, int[]>> entrySetIterator = getRepetitionMarkers().entrySetIterator();
			while (entrySetIterator.hasNext()) {
				Entry<Integer, int[]> entry = entrySetIterator.next();
				max = Math.max(entry.getKey(), max);
				max = Math.max(entry.getValue()[0], max);
				max = Math.max(entry.getValue()[1], max);
			}
			return max;
		}
	}
	
}
