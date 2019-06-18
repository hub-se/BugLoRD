package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * An execution trace consists structurally of a list of executed nodes
 * and a list of tuples that mark repeated sequences in the trace.
 *
 */
public class CompressedIdTrace extends CompressedTraceBase<Integer,Integer> implements Serializable, Iterable<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3861000625585356336L;

	public CompressedIdTrace(BufferedArrayQueue<Integer> trace, boolean log) {
		super(trace, log);
	}

	public CompressedIdTrace(BufferedArrayQueue<Integer> trace, CompressedTraceBase<?, ?> otherCompressedTrace) {
		super(trace, otherCompressedTrace);
	}

	public CompressedIdTrace(BufferedArrayQueue<Integer> compressedTrace, BufferedArrayQueue<int[]> repetitionMarkers, int index) {
		super(compressedTrace, repetitionMarkers, index);
	}

	@Override
	public CompressedTraceBase<Integer, Integer> newChildInstance(BufferedArrayQueue<Integer> trace,
			CompressedTraceBase<?, ?> otherCompressedTrace) {
		return new CompressedIdTrace(trace, otherCompressedTrace);
	}

	@Override
	public CompressedTraceBase<Integer, Integer> newChildInstance(BufferedArrayQueue<Integer> compressedTrace,
			BufferedArrayQueue<int[]> repMarkerLists, int index) {
		return new CompressedIdTrace(compressedTrace, repMarkerLists, index);
	}
	
	@Override
	public CompressedTraceBase<Integer, Integer> newChildInstance(BufferedArrayQueue<Integer> trace, boolean log, int iteration) {
		return new CompressedIdTrace(trace, log);
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
	public int getMaxStoredValue() {
		if (getChild() == null) {
			int max = 0;
			for (Integer i : getCompressedTrace()) {
				max = Math.max(i, max);
			}
			return max;
		} else {
			int max = getChild().getMaxStoredValue();
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
