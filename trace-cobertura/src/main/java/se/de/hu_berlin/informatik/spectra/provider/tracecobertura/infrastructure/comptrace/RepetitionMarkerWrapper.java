package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedMap;

import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Stores/handles repetition markers for compressed traces.
 */
public class RepetitionMarkerWrapper implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5549208997766294856L;

    private BufferedMap<int[]> repetitionMarkers;
    private BufferedMap<int[]> backwardsRepetitionMarkers;

    private long traceSize;

    protected RepetitionMarkerWrapper() {

    }

    public RepetitionMarkerWrapper(BufferedMap<int[]> traceRepetitions, long originalTraceSize) {
//		if (originalTraceSize > Integer.MAX_VALUE) {
//			throw new IllegalStateException("Trace size too large: " + originalTraceSize);
//		}
        this.repetitionMarkers = traceRepetitions;
        this.traceSize = originalTraceSize;
    }

    protected static BufferedMap<int[]> constructFromArray(int[] repetitionMarkers, File outputDir, String filePreix, int subMapSize, boolean deleteOnExit) {
        BufferedMap<int[]> map = new RepetitionMarkerBufferedMap(outputDir, filePreix, subMapSize, deleteOnExit);
        for (int i = 0; i < repetitionMarkers.length; i += 3) {
            map.put(repetitionMarkers[i], new int[]{repetitionMarkers[i + 1], repetitionMarkers[i + 2]});
        }
        return map;
    }

    protected void setTraceSize(long size) {
        this.traceSize = size;
    }

    public long traceSize() {
        return traceSize;
    }

    protected void setRepetitionMarkers(BufferedMap<int[]> repetitionMarkers) {
        this.repetitionMarkers = repetitionMarkers;
    }

    public BufferedMap<int[]> getRepetitionMarkers() {
        return repetitionMarkers;
    }

    protected void setBackwardsRepetitionMarkers(BufferedMap<int[]> backwardsRepetitionMarkers) {
        this.backwardsRepetitionMarkers = backwardsRepetitionMarkers;
    }

    public BufferedMap<int[]> getBackwardsRepetitionMarkers() {
        if (backwardsRepetitionMarkers == null) {
            if (repetitionMarkers == null) {
                return null;
            } else {
                generateBackwardsRepetitionMarkers();
            }
        }
        return backwardsRepetitionMarkers;
    }

    private void generateBackwardsRepetitionMarkers() {
        backwardsRepetitionMarkers = new RepetitionMarkerBufferedMap(repetitionMarkers.getOutputDir(),
                "rev_" + repetitionMarkers.getFilePrefix(), repetitionMarkers.getMaxSubMapSize(), repetitionMarkers.isDeleteOnExit());
        Iterator<Entry<Integer, int[]>> entrySetIterator = repetitionMarkers.entrySetIterator();
        while (entrySetIterator.hasNext()) {
            Entry<Integer, int[]> next = entrySetIterator.next();
            backwardsRepetitionMarkers.put(next.getKey() + next.getValue()[0] - 1, next.getValue());
//			System.out.println((next.getKey()) + ", nidx: " + (next.getKey() + next.getValue()[0] - 1) + ", len: " + next.getValue()[0] + ", rpt: " + next.getValue()[1]);
        }
    }

    public void clear() {
        if (repetitionMarkers != null) {
            repetitionMarkers.clear();
        }
        if (backwardsRepetitionMarkers != null) {
            backwardsRepetitionMarkers.clear();
        }
    }

    public void sleep() {
        if (repetitionMarkers != null) {
            repetitionMarkers.sleep();
        }
        if (backwardsRepetitionMarkers != null) {
            backwardsRepetitionMarkers.sleep();
        }
    }

    public void deleteOnExit() {
        if (repetitionMarkers != null) {
            repetitionMarkers.deleteOnExit();
        }
        if (backwardsRepetitionMarkers != null) {
            backwardsRepetitionMarkers.deleteOnExit();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (repetitionMarkers != null) {
            Iterator<Entry<Integer, int[]>> iterator = repetitionMarkers.entrySetIterator();
            while (iterator.hasNext()) {
                Entry<Integer, int[]> next = iterator.next();
                builder.append("[")
                        .append(next.getKey()).append(",")
                        .append(next.getValue()[0]).append(",")
                        .append(next.getValue()[1]).append("]");
            }
        }
        if (backwardsRepetitionMarkers != null) {
            builder.append(System.lineSeparator()).append("rev: ");
            Iterator<Entry<Integer, int[]>> iterator = backwardsRepetitionMarkers.entrySetIterator();
            while (iterator.hasNext()) {
                Entry<Integer, int[]> next = iterator.next();
                builder.append("[")
                        .append(next.getKey()).append(",")
                        .append(next.getValue()[0]).append(",")
                        .append(next.getValue()[1]).append("]");
            }
        }
        return builder.toString();
    }

}
