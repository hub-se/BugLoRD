package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import java.io.Serializable;
import java.util.Map.Entry;

/**
 * An execution trace consists structurally of a list of executed nodes
 * and a list of tuples that mark repeated sequences in the trace.
 *
 */
public class CompressedTrace extends CompressedTraceBase<int[],IntArrayWrapper> implements Serializable, Iterable<int[]> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3143792958649174671L;

	public CompressedTrace(SingleLinkedArrayQueue<int[]> trace, boolean log) {
		super(trace, log);
	}

	public CompressedTrace(SingleLinkedArrayQueue<int[]> trace, CompressedTraceBase<?, ?> otherCompressedTrace) {
		super(trace, otherCompressedTrace);
	}

	public CompressedTrace(int[][] compressedTrace, int[][] repMarkerLists, int index) {
		super(compressedTrace, repMarkerLists, index);
	}

	@Override
	public CompressedTraceBase<int[], IntArrayWrapper> newChildInstance(SingleLinkedArrayQueue<int[]> trace,
			CompressedTraceBase<?, ?> otherCompressedTrace) {
		return new CompressedTrace(trace, otherCompressedTrace);
	}

	@Override
	public CompressedTraceBase<int[], IntArrayWrapper> newChildInstance(int[][] compressedTrace,
			int[][] repMarkerLists, int index) {
		return new CompressedTrace(compressedTrace, repMarkerLists, index);
	}
	
	@Override
	public CompressedTraceBase<int[], IntArrayWrapper> newChildInstance(SingleLinkedArrayQueue<int[]> trace, boolean log) {
		return new CompressedTrace(trace, log);
	}

	@Override
	public boolean isEqual(int[] first, int[] second) {
		return first.length == second.length &&
				first[1] == second[1] && 
				first[0] == second[0];
	}

	@Override
	public IntArrayWrapper getRepresentation(int[] element) {
		return new IntArrayWrapper(element);
	}

	@Override
	public int[][] newArrayOfSize(int size) {
		return new int[size][];
	}
	
	@Override
	public int getMaxStoredValue() {
		if (getChild() == null) {
			int max = 0;
			for (int[] i : getCompressedTrace()) {
				for (int j : i) {
					max = Math.max(j, max);
				}
			}
			return max;
		} else {
			int max = getChild().getMaxStoredValue();
			for (Entry<Integer, int[]> i : getRepetitionMarkers().entrySet()) {
				max = Math.max(i.getKey(), max);
				max = Math.max(i.getValue()[0], max);
				max = Math.max(i.getValue()[1], max);
			}
			return max;
		}
	}
	
}
