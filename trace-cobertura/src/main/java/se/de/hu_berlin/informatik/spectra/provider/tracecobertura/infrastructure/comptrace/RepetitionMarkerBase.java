package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedMap;

import java.io.File;
import java.io.Serializable;
import java.util.Queue;

/**
 * Stores/handles repetition markers for compressed traces.
 */
@CoverageIgnore
public abstract class RepetitionMarkerBase implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3743380720924000714L;

    protected static final int MAX_ITERATION_COUNT = 8;

    private RepetitionMarkerWrapper[] repetitionMarkerWrappers;
    private int currentIndex = 0;

    private transient boolean markedForDeletion;

    protected RepetitionMarkerBase() {

    }

    protected static BufferedMap<int[]> constructFromIntegerQueue(Queue<Integer> repetitionMarkers, File outputDir, String filePrefix, int subMapSize, boolean deleteOnExit) {
        BufferedMap<int[]> map = new RepetitionMarkerBufferedMap(outputDir, filePrefix, subMapSize, deleteOnExit);
        while (repetitionMarkers.size() >= 3) {
            map.put(repetitionMarkers.remove(), new int[]{repetitionMarkers.remove(), repetitionMarkers.remove()});
        }
        if (!repetitionMarkers.isEmpty()) {
            throw new IllegalStateException("Queue with repetition markers is in an incorrect state!");
        }
        return map;
    }

    protected void addRepetitionMarkers(BufferedMap<int[]> repetitionMarkers, long traceSize) {
        if (repetitionMarkerWrappers == null) {
            this.repetitionMarkerWrappers = new RepetitionMarkerWrapper[MAX_ITERATION_COUNT];
        }
        this.repetitionMarkerWrappers[currentIndex++] = new RepetitionMarkerWrapper(repetitionMarkers, traceSize);
    }

    protected void setRepetitionMarkers(RepetitionMarkerWrapper[] repetitionMarkerWrappers) {
        this.repetitionMarkerWrappers = repetitionMarkerWrappers;
    }

    public RepetitionMarkerWrapper[] getRepetitionMarkers() {
        if (repetitionMarkerWrappers == null) {
            return new RepetitionMarkerWrapper[0];
        }
        return repetitionMarkerWrappers;
    }

    public BufferedMap<int[]> getRepetitionMarkers(int level) {
        return this.repetitionMarkerWrappers[level].getRepetitionMarkers();
    }

//	protected void setBackwardsRepetitionMarkers(BufferedMap<int[]> backwardsRepetitionMarkers) {
//		this.backwardsRepetitionMarkers = backwardsRepetitionMarkers;
//	}

    public BufferedMap<int[]> getBackwardsRepetitionMarkers(int level) {
        if (repetitionMarkerWrappers == null || level >= currentIndex) {
            return null;
        }

        return repetitionMarkerWrappers[level].getBackwardsRepetitionMarkers();
    }

    public int levelCount() {
        return currentIndex;
    }

    public void clear() {
        if (repetitionMarkerWrappers != null) {
            for (int i = 0; i < currentIndex; i++) {
                repetitionMarkerWrappers[i].clear();
                repetitionMarkerWrappers[i] = null;
            }
            repetitionMarkerWrappers = null;
            currentIndex = 0;
        }
    }

    public void sleep() {
        if (repetitionMarkerWrappers != null) {
            for (int i = 0; i < currentIndex; i++) {
                repetitionMarkerWrappers[i].sleep();
            }
        }
    }

    abstract public void lock();

    abstract public void unlock();

    public void markForDeletion() {
        this.markedForDeletion = true;
    }

    public boolean isMarkedForDeletion() {
        return markedForDeletion;
    }

    abstract public void deleteIfMarked();

    public void deleteOnExit() {
        if (repetitionMarkerWrappers != null) {
            for (int i = 0; i < currentIndex; i++) {
                repetitionMarkerWrappers[i].deleteOnExit();
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int level = 1;
        if (repetitionMarkerWrappers != null) {
            for (int i = 0; i < currentIndex; i++) {
                RepetitionMarkerWrapper wrapper = repetitionMarkerWrappers[i];
                builder.append("lvl").append(level++).append(", size: ").append(wrapper.traceSize()).append(" -> ")
                        .append(wrapper.toString()).append(System.lineSeparator());
            }
        }
        return builder.toString();
    }

    public void trim() {
        // do nothing
    }

}
