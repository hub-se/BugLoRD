package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import java.io.Serializable;

/**
 * An execution trace consists structurally of a list of executed nodes
 * and a list of tuples that mark repeated sequences in the trace.
 *
 */
public class CompressedLongIdTrace extends CompressedLongTraceBase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6682160915410961012L;

	public CompressedLongIdTrace(BufferedLongArrayQueue trace, boolean log) {
		super(trace, log);
	}

	public CompressedLongIdTrace(BufferedLongArrayQueue trace, CompressedLongTraceBase otherCompressedTrace) {
		super(trace, otherCompressedTrace);
	}

	public CompressedLongIdTrace(BufferedLongArrayQueue compressedTrace, 
			BufferedArrayQueue<int[]> repetitionMarkers, int index) {
		super(compressedTrace, repetitionMarkers, index);
	}

	@Override
	public CompressedLongTraceBase newChildInstance(BufferedLongArrayQueue trace,
			CompressedLongTraceBase otherCompressedTrace) {
		return new CompressedLongIdTrace(trace, otherCompressedTrace);
	}

	@Override
	public CompressedLongTraceBase newChildInstance(BufferedLongArrayQueue compressedTrace,
			BufferedArrayQueue<int[]> repMarkerLists, int index) {
		return new CompressedLongIdTrace(compressedTrace, repMarkerLists, index);
	}
	
	@Override
	public CompressedLongTraceBase newChildInstance(BufferedLongArrayQueue trace, boolean log, int iteration) {
		return new CompressedLongIdTrace(trace, log);
	}

}
