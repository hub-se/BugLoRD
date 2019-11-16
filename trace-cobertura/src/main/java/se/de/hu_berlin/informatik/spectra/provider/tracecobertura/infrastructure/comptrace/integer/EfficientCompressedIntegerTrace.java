package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedMap;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.RepetitionMarkerBase;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.RepetitionMarkerWrapper;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.longs.EfficientCompressedLongTrace;

/**
 * An execution trace consists structurally of a list of executed nodes
 * and a list of tuples that mark repeated sequences in the trace.
 */
public class EfficientCompressedIntegerTrace extends RepetitionMarkerBase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1663120246473002672L;
	
	private int originalSize = 0;
	private BufferedIntArrayQueue compressedTrace;
	
	private File outputDir;
	private int nodeSize;
	private int mapSize;
	private boolean deleteOnExit;
	
	private boolean locked = false;

	private String prefix;

	public EfficientCompressedIntegerTrace(File outputDir, String prefix, int nodeSize, int mapSize, boolean deleteOnExit) {
		this.outputDir = outputDir;
		this.prefix = prefix;
		this.nodeSize = nodeSize;
		this.mapSize = mapSize;
		this.deleteOnExit = deleteOnExit;
		String uuid = UUID.randomUUID().toString();
		this.compressedTrace = new BufferedIntArrayQueue(outputDir, 
				prefix + "cpr_trace_" + uuid, nodeSize, deleteOnExit);
		addNewLevel();
	}
	
	/**
	 * Adds the given queue's contents to the trace. 
	 * ATTENTION: the queue's contents will be removed in the process! 
	 * @param trace
	 * a queue that should be compressed
	 * @param log
	 * whether to log some status information
	 */
	public EfficientCompressedIntegerTrace(BufferedIntArrayQueue trace, boolean log) {
		this(trace.getOutputDir(), trace.getFilePrefix(), trace.getNodeSize(), trace.getNodeSize(), trace.isDeleteOnExit());
		while (!trace.isEmpty()) {
			add(trace.remove());
		}
		// need to finalize things now!
		endOfLine();
	}

	public EfficientCompressedIntegerTrace(BufferedIntArrayQueue traceOfNodeIDs, EfficientCompressedIntegerTrace otherCompressedTrace) {
		this.originalSize = otherCompressedTrace.size();
		this.compressedTrace = traceOfNodeIDs;
		setRepetitionMarkers(otherCompressedTrace.getRepetitionMarkers());
		// disallow addition of new elements
		locked = true;
	}
	
	public EfficientCompressedIntegerTrace(BufferedIntArrayQueue traceOfNodeIDs, EfficientCompressedLongTrace otherCompressedTrace) {
		this.originalSize = otherCompressedTrace.size();
		this.compressedTrace = traceOfNodeIDs;
		setRepetitionMarkers(otherCompressedTrace.getRepetitionMarkers());
		// disallow addition of new elements
		locked = true;
	}

	public EfficientCompressedIntegerTrace(BufferedIntArrayQueue compressedTrace, BufferedArrayQueue<int[]> repetitionMarkers, boolean log) {
		this.compressedTrace = compressedTrace;
		if (repetitionMarkers != null) {
			int size = compressedTrace.size();
			int index = 0;
			while (index < repetitionMarkers.size()) {
				BufferedMap<int[]> repMarkers = RepetitionMarkerBase.constructFromArray(repetitionMarkers.get(index++), 
						compressedTrace.getOutputDir(), 
						compressedTrace.getFilePrefix() + "-map-" + index, compressedTrace.getArrayLength(),
						compressedTrace.isDeleteOnExit());
				// calculate the trace's size on the current level
				for (Iterator<Entry<Integer, int[]>> iterator = repMarkers.entrySetIterator(); iterator.hasNext();) {
					Entry<Integer, int[]> repMarker = iterator.next();
					// [length, repetitionCount]
					size += (repMarker.getValue()[0] * (repMarker.getValue()[1]-1));
				}
				// add the level to the list
				addRepetitionMarkers(repMarkers, size);
			}
			this.originalSize = size;
		} else {
			this.originalSize = compressedTrace.size();
		}
		// disallow addition of new elements
		locked = true;
	}
	
//	private int computeFullTraceLengths() {
//		int size = compressedTrace.size();
//		
//		for (int i = getRepetitionMarkers().size() - 1; i >= 0; --i) {
//			RepetitionMarkerWrapper repMarkerWrapper = getRepetitionMarkers().get(i);
//			// calculate the trace's size on the current level
//			for (Iterator<Entry<Integer, int[]>> iterator = repMarkerWrapper.getRepetitionMarkers().entrySetIterator(); iterator.hasNext();) {
//				Entry<Integer, int[]> repMarker = iterator.next();
//				// [length, repetitionCount]
//				size += (repMarker.getValue()[0] * (repMarker.getValue()[1]-1));
//			}
//			repMarkerWrapper.setTraceSize(size);
//		}
//		
//		return size;
//	}
	
	public int getMaxStoredValue() {
		int max = 0;
		ReplaceableCloneableIntIterator iterator = baseIterator();
		while (iterator.hasNext()) {
			max = Math.max(iterator.next(), max);
		}
		if (getRepetitionMarkers() != null) {
			for (RepetitionMarkerWrapper repMarkerWrapper : getRepetitionMarkers()) {
				Iterator<Entry<Integer, int[]>> entrySetIterator = repMarkerWrapper.getRepetitionMarkers().entrySetIterator();
				while (entrySetIterator.hasNext()) {
					Entry<Integer, int[]> entry = entrySetIterator.next();
					max = Math.max(entry.getKey(), max);
					max = Math.max(entry.getValue()[0], max);
					max = Math.max(entry.getValue()[1], max);
				}
				max = Math.max(repMarkerWrapper.getRepetitionMarkers().size(), max);
			}
		}
		
		return max;
	}

	public int size() {
		return originalSize;
	}
	
	public boolean isEmpty( ) {
		return originalSize <= 0;
	}
	
	private List<CompressedIntegerTraceLevel> levels = new ArrayList<>(MAX_ITERATION_COUNT + 1);
	
	
	public void add(int element) {
		if (locked) {
			throw new IllegalStateException("Can not add element to already locked trace.");
		}
		++originalSize;
		if (originalSize % 10000 == 0)
			System.out.print('.');
		if (originalSize % 1000000 == 0)
			System.out.println(originalSize);
		CompressedIntegerTraceLevel level = levels.get(0);
		boolean endOfRepetition = level.add(element, 0 < MAX_ITERATION_COUNT);

		if (endOfRepetition) {
			feedToHigherLevel(0, false);
		}
	}

	private void feedToHigherLevel(int levelIndex, boolean endOfLine) {
//		System.out.println("f level " + levelIndex + ", " + endOfLine);
		CompressedIntegerTraceLevel level = levels.get(levelIndex);
		BufferedIntArrayQueue trace = level.getCompressedTrace();

		if (levels.size() <= levelIndex + 1) {
			addNewLevel();
		}
		CompressedIntegerTraceLevel nextLevel = levels.get(levelIndex + 1);

		boolean endOfRepetition = true;
		if (endOfLine) {
			// we are in a state where the built up trace is either empty or simply not a finished repetition;
			// this should only be the case if we process the remaining elements at the end of the line...

			// we need to recheck the buffer
			// will process the entire remaining buffer if endOfLine is true
			endOfRepetition = level.addFromBuffer(true, endOfLine);
		}

		// if we are in a state after a tracked repetition (true), we need to process the
		// built up trace from the current level (and work through the remaining buffer, if any)
		while (endOfRepetition) {
			while (!trace.isEmpty()) {
				int element = trace.remove();
//				System.out.print(element + ", ");
				endOfRepetition = nextLevel.add(element, levelIndex + 1 < MAX_ITERATION_COUNT);
				if (endOfRepetition) {
					feedToHigherLevel(levelIndex + 1, false);
				}
			}
//			System.out.println("f1");

			// after a finished repetition, we need to recheck the buffer
			// will process the entire remaining buffer if endOfLine is true
			endOfRepetition = level.addFromBuffer(true, endOfLine);
		}

		// endOfRepetition is false! (state of not enough information)

		if (endOfLine) {
			// if endOfLine is true, the buffer is checked and empty! (there can still be elements in the processed trace)
			// feed the rest (if any) of this level's trace to the next level
			while (!trace.isEmpty()) {
				int element = trace.remove();
//				System.out.print(element + ", ");
				endOfRepetition = nextLevel.add(element, levelIndex + 1 < MAX_ITERATION_COUNT);
				if (endOfRepetition) {
					feedToHigherLevel(levelIndex + 1, false);
				}
			}
//			System.out.println("f2");

			if (levels.size() <= levelIndex + 2) {
				// no more levels exist (we're at the end);
				// the next level is the last and it has no repetition markers!
				feedToCompressedTrace(levelIndex + 1, true);
			} else {
				// finish up the next level
				feedToHigherLevel(levelIndex + 1, true);
			}
		}

	}
	
	private void feedToCompressedTrace(int levelIndex, boolean endOfLine) {
//		System.out.println("c level " + levelIndex + ", " + endOfLine);
		CompressedIntegerTraceLevel level = levels.get(levelIndex);
		BufferedIntArrayQueue trace = level.getCompressedTrace();

		// actually, endOfLine should always be true here... this method is only called right at the end!
		boolean endOfRepetition = true;
		if (endOfLine) {
			// we are in a state where the built up trace is either empty or simply not a finished repetition;
			// this should only be the case if we process the remaining elements at the end of the line...

			// we need to recheck the buffer
			// will process the entire remaining buffer if endOfLine is true
			endOfRepetition = level.addFromBuffer(true, endOfLine);
		}

		// if we are in a state after a tracked repetition (true), we need to process the
		// built up trace from the current level (and work through the remaining buffer, if any)
		while (endOfRepetition) {
			while (!trace.isEmpty()) {
				int element = trace.remove();
//				System.out.print(element + ", ");
				compressedTrace.add(element);
			}
//			System.out.println("c1");

			// after a finished repetition, we need to recheck the buffer
			// will process the entire remaining buffer if endOfLine is true
			endOfRepetition = level.addFromBuffer(true, endOfLine);
		}

		// endOfRepetition is false! (state of not enough information)
		
		if (endOfLine) {
			// if endOfLine is true, the buffer is checked and empty! (there can still be elements in the processed trace)
			// feed the rest (if any) of this level's trace to the next level
			while (!trace.isEmpty()) {
				int element = trace.remove();
//				System.out.print(element + ", ");
				compressedTrace.add(element);
			}
//			System.out.println("c2");
		}

	}

	private void endOfLine() {
		if (!locked) { 
			// add lingering elements to higher levels
			int level = 0;
			feedToHigherLevel(0, true);
			
			StringBuilder builder = new StringBuilder();
			for (level = levels.size() - 1; level >= 0; --level) {
				CompressedIntegerTraceLevel trace = levels.get(level);
				if (!trace.getRepetitionMarkers().isEmpty()) {
					builder.append(String.format("level %d: max buffer size: %d, max trace size: %d%n", level, trace.maxBufferSize, trace.maxTraceSize));
					addRepetitionMarkers(trace.getRepetitionMarkers(), trace.size());
				}
			}
			
//			// grab the last level's compressed trace (should be filled already, though...)
			// end of line
			if (compressedTrace.isEmpty()) {
				compressedTrace = levels.get(levels.size() - 1).getCompressedTrace();
			}
			builder.append(String.format("full size: %d, compressed size: %d (%.2f%%)", originalSize, compressedTrace.size(), -100.00+100.0*(double)compressedTrace.size()/(double)originalSize));
			System.out.println(builder.toString());

			// don't need the levels anymore now
			levels.clear();
			// TODO: need to clean up each level?

			// disallow addition of new elements
			locked = true;
		}
	}

	private void addNewLevel() {
		levels.add(new CompressedIntegerTraceLevel(outputDir, prefix, nodeSize, mapSize, deleteOnExit));
	}


	public BufferedIntArrayQueue getCompressedTrace() {
		endOfLine();
		return compressedTrace;
	}

	
	/**
	 * @return
	 * iterator over the full trace
	 */
	public IntTraceIterator iterator() {
		endOfLine();
		return new IntTraceIterator(this);
	}
	
	/**
	 * @return
	 * iterator over the compressed trace (ignores repetitions)
	 */
	public ReplaceableCloneableIntIterator baseIterator() {
		endOfLine();
		return compressedTrace.iterator();
	}
	
	/**
	 * @return
	 * reverse iterator over the full trace, starting at the end of the trace
	 */
	public IntTraceReverseIterator reverseIterator() {
		endOfLine();
		return new IntTraceReverseIterator(this);
	}
	
//	public Set<Integer> computeStartingElements() {
//		Set<Integer> set = new HashSet<>();
//		addStartingElementsToSet(set);
//		return set;
//	}
	
	public void addStartingElementsToSet(Set<Integer> set) {
		IntTraceIterator iterator = iterator();
		boolean lastElementWasSequenceEnd = false;
		if (iterator.hasNext()) {
			lastElementWasSequenceEnd = iterator.isEndOfRepetition();
			set.add(iterator.next());
		}
		while (iterator.hasNext()) {
			if (lastElementWasSequenceEnd || iterator.isStartOfRepetition()) {
				lastElementWasSequenceEnd = iterator.isEndOfRepetition();
				set.add(iterator.next());
			} else {
				lastElementWasSequenceEnd = iterator.isEndOfRepetition();
				iterator.next();
			}
		}
	}

	@Override
	public void clear() {
		getCompressedTrace().clear();
		super.clear();
	}

	public int getFirstElement() {
		return getCompressedTrace().element();
	}

	public int getLastElement() {
		return getCompressedTrace().lastElement();
	}

	@Override
	public void sleep() {
		getCompressedTrace().sleep();
		super.sleep();
	}
	
	@Override
	public void lock() {
		getCompressedTrace().lock();
	}
	
	@Override
	public void unlock() {
		getCompressedTrace().unlock();
	}
	
	@Override
	public String toString() {
		endOfLine();
		StringBuilder builder = new StringBuilder();
//		builder.append("lvl0, size: ").append(getCompressedTrace().size()).append(System.lineSeparator());
		builder.append(super.toString());
		builder.append("full, size: ").append(originalSize).append(System.lineSeparator());
		builder.append("full trace: ");
		IntTraceIterator eTraceIterator = iterator();
		while (eTraceIterator.hasNext()) {
			builder.append(eTraceIterator.next() + ", ");
		}
		return builder.toString();
	}
	
	@Override
	public void deleteIfMarked() {
		if (isMarkedForDeletion()) {
			this.unlock();
			this.clear();
		}
	}
	
	@Override
	public void deleteOnExit() {
		getCompressedTrace().deleteOnExit();
		super.deleteOnExit();
	}
	
}
