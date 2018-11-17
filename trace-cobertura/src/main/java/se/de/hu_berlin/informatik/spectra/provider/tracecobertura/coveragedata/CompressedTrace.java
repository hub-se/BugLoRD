package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * An execution trace consists structurally of a list of executed nodes
 * and a list of tuples that mark repeated sequences in the trace.
 *
 */
public class CompressedTrace extends CompressedTraceBase<int[],List<Integer>> implements Serializable, Iterable<int[]> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5651735479940617653L;

	public CompressedTrace(SingleLinkedArrayQueue<int[]> trace, boolean log) {
		super(trace, log);
	}

	public CompressedTrace(SingleLinkedArrayQueue<int[]> trace, CompressedTraceBase<?, ?> otherCompressedTrace) {
		super(trace, otherCompressedTrace);
	}

	public CompressedTrace(int[][] compressedTrace, List<int[]> repMarkerLists, int index) {
		super(compressedTrace, repMarkerLists, index);
	}

	@Override
	public CompressedTraceBase<int[], List<Integer>> newChildInstance(SingleLinkedArrayQueue<int[]> trace,
			CompressedTraceBase<?, ?> otherCompressedTrace) {
		return new CompressedTrace(trace, otherCompressedTrace);
	}

	@Override
	public CompressedTraceBase<int[], List<Integer>> newChildInstance(int[][] compressedTrace,
			List<int[]> repMarkerLists, int index) {
		return new CompressedTrace(compressedTrace, repMarkerLists, index);
	}
	
	@Override
	public CompressedTraceBase<int[], List<Integer>> newChildInstance(SingleLinkedArrayQueue<int[]> trace, boolean log) {
		return new CompressedTrace(trace, log);
	}

	@Override
	public boolean isEqual(int[] first, int[] second) {
		return first.length == second.length &&
				first[1] == second[1] && 
				first[0] == second[0];
	}

	@Override
	public List<Integer> getRepresentation(int[] element) {
		ArrayList<Integer> result = new ArrayList<>(2);
		result.add(element[0]);
		result.add(element[1]);
		return result;
	}

	@Override
	public int[][] newArrayOfSize(int size) {
		return new int[size][];
	}
	
}
