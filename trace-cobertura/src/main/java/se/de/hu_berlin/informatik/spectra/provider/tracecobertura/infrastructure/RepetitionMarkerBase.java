package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Stores/handles repetition markers for compressed traces.
 */
public abstract class RepetitionMarkerBase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5549208997766294856L;

	protected static final int MAX_ITERATION_COUNT = 10;
	
	private BufferedMap<int[]> repetitionMarkers;
	private BufferedMap<int[]> backwardsRepetitionMarkers;
	
	private transient boolean markedForDeletion;
	
	protected RepetitionMarkerBase() {
		
	}
	
	protected BufferedMap<int[]> constructFromArray(int[] repetitionMarkers, File outputDir, String filePreix, int subMapSize, boolean deleteOnExit) {
		BufferedMap<int[]> map = new RepetitionMarkerBufferedMap(outputDir, filePreix, subMapSize, deleteOnExit);
		for (int i = 0; i < repetitionMarkers.length; i += 3) {
			map.put(repetitionMarkers[i], new int[] {repetitionMarkers[i+1], repetitionMarkers[i+2]});
		}
		return map;
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
		backwardsRepetitionMarkers = new BufferedMap<>(repetitionMarkers.getOutputDir(), 
				"rew_" + repetitionMarkers.getFilePrefix(), repetitionMarkers.maxSubMapSize, repetitionMarkers.deleteOnExit);
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
		if (repetitionMarkers != null) {
			repetitionMarkers.deleteOnExit();
		}
		if (backwardsRepetitionMarkers != null) {
			backwardsRepetitionMarkers.deleteOnExit();
		}
	}
	
}
