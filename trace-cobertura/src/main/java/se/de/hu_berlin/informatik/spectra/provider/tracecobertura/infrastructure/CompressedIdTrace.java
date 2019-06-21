package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * An execution trace consists structurally of a list of executed nodes
 * and a list of tuples that mark repeated sequences in the trace.
 *
 */
public class CompressedIdTrace extends CompressedTraceBase<Long,Long> implements Serializable, Iterable<Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3861000625585356336L;

	public CompressedIdTrace(BufferedArrayQueue<Long> trace, boolean log) {
		super(trace, log);
	}

	public CompressedIdTrace(BufferedArrayQueue<Long> trace, CompressedTraceBase<?, ?> otherCompressedTrace) {
		super(trace, otherCompressedTrace);
	}

	public CompressedIdTrace(BufferedArrayQueue<Long> compressedTrace, BufferedArrayQueue<int[]> repetitionMarkers, int index) {
		super(compressedTrace, repetitionMarkers, index);
	}

	@Override
	public CompressedTraceBase<Long, Long> newChildInstance(BufferedArrayQueue<Long> trace,
			CompressedTraceBase<?, ?> otherCompressedTrace) {
		return new CompressedIdTrace(trace, otherCompressedTrace);
	}

	@Override
	public CompressedTraceBase<Long, Long> newChildInstance(BufferedArrayQueue<Long> compressedTrace,
			BufferedArrayQueue<int[]> repMarkerLists, int index) {
		return new CompressedIdTrace(compressedTrace, repMarkerLists, index);
	}
	
	@Override
	public CompressedTraceBase<Long, Long> newChildInstance(BufferedArrayQueue<Long> trace, boolean log, int iteration) {
		return new CompressedIdTrace(trace, log);
	}

	@Override
	public boolean isEqual(Long first, Long second) {
		return first.equals(second);
	}

	@Override
	public Long getRepresentation(Long element) {
		return element;
	}
	
	@Override
	public long getMaxStoredValue() {
		if (getChild() == null) {
			long max = 0;
			for (Long i : getCompressedTrace()) {
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
