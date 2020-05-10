package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.IntArrayWrapper;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

/**
 * An execution trace consists structurally of a list of executed nodes
 * and a list of tuples that mark repeated sequences in the trace.
 */
public class CompressedIntArrayTrace extends CompressedTrace<int[], IntArrayWrapper> implements Serializable, Iterable<int[]> {

    /**
     *
     */
    private static final long serialVersionUID = -4702552264676962458L;

    public CompressedIntArrayTrace(BufferedArrayQueue<int[]> trace, boolean log) {
        super(trace, log);
    }

    public CompressedIntArrayTrace(BufferedArrayQueue<int[]> trace, CompressedTrace<?, ?> otherCompressedTrace) {
        super(trace, otherCompressedTrace);
    }

    public CompressedIntArrayTrace(BufferedArrayQueue<int[]> compressedTrace, List<Queue<Integer>> repetitionMarkers) {
        super(compressedTrace, repetitionMarkers);
    }

    @Override
    public boolean isEqual(int[] first, int[] second) {
        return new IntArrayWrapper(first).equals(new IntArrayWrapper(second));
    }

    @Override
    public IntArrayWrapper getRepresentation(int[] element) {
        return new IntArrayWrapper(element);
    }

    @Override
    public long getMaxStoredValue() {
        long max = 0;
        ReplaceableCloneableIterator<int[]> iterator = baseIterator();
        while (iterator.hasNext()) {
            int[] next = iterator.next();
            for (int i : next) {
                max = Math.max(i, max);
            }
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
