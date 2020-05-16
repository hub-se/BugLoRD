package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedArrayQueue;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

/**
 * An execution trace consists structurally of a list of executed nodes
 * and a list of tuples that mark repeated sequences in the trace.
 */
@CoverageIgnore
public class CompressedIdTrace extends CompressedTrace<Long, Long> implements Serializable, Iterable<Long> {

    /**
     *
     */
    private static final long serialVersionUID = -2642578139303309605L;

    public CompressedIdTrace(BufferedArrayQueue<Long> trace, boolean log) {
        super(trace, log);
    }

    public CompressedIdTrace(BufferedArrayQueue<Long> trace, CompressedTrace<?, ?> otherCompressedTrace) {
        super(trace, otherCompressedTrace);
    }

    public CompressedIdTrace(BufferedArrayQueue<Long> compressedTrace, List<Queue<Integer>> repetitionMarkers) {
        super(compressedTrace, repetitionMarkers);
    }

    @Override
    public boolean isEqual(Long first, Long second) {
        return first == second;
    }

    @Override
    public Long getRepresentation(Long element) {
        return element;
    }

    @Override
    public long getMaxStoredValue() {
        long max = 0;
        ReplaceableCloneableIterator<Long> iterator = baseIterator();
        while (iterator.hasNext()) {
            max = Math.max(iterator.next(), max);
        }
        if (getRepetitionMarkers() != null) {
            for (RepetitionMarkerWrapper repMarkerWrapper : getRepetitionMarkers()) {
                if (repMarkerWrapper == null) {
                    break;
                }
                Iterator<Entry<Integer, int[]>> entrySetIterator = repMarkerWrapper.getRepetitionMarkers().entrySetIterator();
                while (entrySetIterator.hasNext()) {
                    Entry<Integer, int[]> entry = entrySetIterator.next();
                    max = Math.max(entry.getKey(), max);
                    max = Math.max(entry.getValue()[0], max);
                    max = Math.max(entry.getValue()[1], max);
                }
//				max = Math.max(repMarkerWrapper.getRepetitionMarkers().size(), max);
            }
        }

        return max;
    }

}
