package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * An execution trace consists structurally of a list of executed nodes
 * and a list of tuples that mark repeated sequences in the trace.
 *
 */
public class CompressedIntegerIdTrace extends CompressedIntegerTraceBase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5338572074372094337L;

	public CompressedIntegerIdTrace(BufferedIntArrayQueue trace, boolean log) {
		super(trace, log);
	}

	public CompressedIntegerIdTrace(BufferedIntArrayQueue trace, CompressedIntegerTraceBase otherCompressedTrace) {
		super(trace, otherCompressedTrace);
	}

	public CompressedIntegerIdTrace(BufferedIntArrayQueue compressedTrace, 
			BufferedArrayQueue<int[]> repetitionMarkers, int index) {
		super(compressedTrace, repetitionMarkers, index);
	}

	@Override
	public CompressedIntegerTraceBase newChildInstance(BufferedIntArrayQueue trace,
			CompressedIntegerTraceBase otherCompressedTrace) {
		return new CompressedIntegerIdTrace(trace, otherCompressedTrace);
	}

	@Override
	public CompressedIntegerTraceBase newChildInstance(BufferedIntArrayQueue compressedTrace,
			BufferedArrayQueue<int[]> repMarkerLists, int index) {
		return new CompressedIntegerIdTrace(compressedTrace, repMarkerLists, index);
	}
	
	@Override
	public CompressedIntegerTraceBase newChildInstance(BufferedIntArrayQueue trace, boolean log, int iteration) {
		return new CompressedIntegerIdTrace(trace, log);
	}

}
