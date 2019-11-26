package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedMap;

/**
 * Stores/handles repetition markers for compressed traces.
 */
public abstract class RepetitionMarkerBase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3743380720924000714L;

	protected static final int MAX_ITERATION_COUNT = 10;
	
	private List<RepetitionMarkerWrapper> repetitionMarkerWrappers;
	
	private transient boolean markedForDeletion;
	
	protected RepetitionMarkerBase() {
		
	}
	
	protected static BufferedMap<int[]> constructFromArray(int[] repetitionMarkers, File outputDir, String filePreix, int subMapSize, boolean deleteOnExit) {
		BufferedMap<int[]> map = new RepetitionMarkerBufferedMap(outputDir, filePreix, subMapSize, deleteOnExit);
		for (int i = 0; i < repetitionMarkers.length; i += 3) {
			map.put(repetitionMarkers[i], new int[] {repetitionMarkers[i+1], repetitionMarkers[i+2]});
		}
		return map;
	}
	
	protected void addRepetitionMarkers(BufferedMap<int[]> repetitionMarkers, long traceSize) {
		if (repetitionMarkerWrappers == null) {
			this.repetitionMarkerWrappers = new ArrayList<>(MAX_ITERATION_COUNT);
		}
		this.repetitionMarkerWrappers.add(new RepetitionMarkerWrapper(repetitionMarkers, traceSize));
	}
	
	protected void setRepetitionMarkers(List<RepetitionMarkerWrapper> repetitionMarkerWrappers) {
		this.repetitionMarkerWrappers = repetitionMarkerWrappers;
	}
	
	public List<RepetitionMarkerWrapper> getRepetitionMarkers() {
		if (repetitionMarkerWrappers == null) {
			return Collections.emptyList();
		}
		return repetitionMarkerWrappers;
	}

	public BufferedMap<int[]> getRepetitionMarkers(int level) {
		return this.repetitionMarkerWrappers.get(level).getRepetitionMarkers();
	}
	
//	protected void setBackwardsRepetitionMarkers(BufferedMap<int[]> backwardsRepetitionMarkers) {
//		this.backwardsRepetitionMarkers = backwardsRepetitionMarkers;
//	}
	
	public BufferedMap<int[]> getBackwardsRepetitionMarkers(int level) {
		if (repetitionMarkerWrappers == null || level >= repetitionMarkerWrappers.size()) {
			return null;
		}
		
		return repetitionMarkerWrappers.get(level).getBackwardsRepetitionMarkers();
	}
	
	
	public void clear() {
		if (repetitionMarkerWrappers != null) {
			for (RepetitionMarkerWrapper wrapper : repetitionMarkerWrappers) {
				wrapper.clear();
			}
			repetitionMarkerWrappers.clear();
		}
	}

	public void sleep() {
		if (repetitionMarkerWrappers != null) {
			for (RepetitionMarkerWrapper wrapper : repetitionMarkerWrappers) {
				wrapper.sleep();
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
			for (RepetitionMarkerWrapper wrapper : repetitionMarkerWrappers) {
				wrapper.deleteOnExit();
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		int level = 1;
		if (repetitionMarkerWrappers != null) {
			for (RepetitionMarkerWrapper wrapper : repetitionMarkerWrappers) {
				builder.append("lvl").append(level++).append(", size: ").append(wrapper.traceSize()).append(" -> ")
				.append(wrapper.toString()).append(System.lineSeparator());
			}
		}
		return builder.toString();
	}
	
}
