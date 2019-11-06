package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.CompressedIntegerTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.longs.CompressedLongTrace;

import java.io.Serializable;
import java.util.List;

public class LinearExecutionBlockTrace extends ExecutionTrace implements Serializable {

    private static final long serialVersionUID = 8492735299388237914L;


    public LinearExecutionBlockTrace(BufferedIntArrayQueue trace, boolean log) {
        super(trace, log);
    }

    public LinearExecutionBlockTrace(BufferedIntArrayQueue trace, CompressedIntegerTrace otherCompressedTrace) {
        super(trace, otherCompressedTrace);
    }

    public LinearExecutionBlockTrace(BufferedIntArrayQueue trace, CompressedLongTrace otherCompressedTrace) {
        super(trace, otherCompressedTrace);
    }

    public LinearExecutionBlockTrace(BufferedIntArrayQueue compressedTrace, BufferedArrayQueue<int[]> repMarkerLists, boolean log) {
        super(compressedTrace, repMarkerLists, log);
    }
}
