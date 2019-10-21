package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.longs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedLongArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedMap;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedLongArrayQueue.MyBufferedLongIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.RepetitionMarkerBase;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.RepetitionMarkerBufferedMap;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.RepetitionMarkerWrapper;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.CompressedIntegerTrace;

/**
 * An execution trace consists structurally of a list of executed nodes
 * and a list of tuples that mark repeated sequences in the trace.
 */
public class CompressedLongTrace extends RepetitionMarkerBase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8437645161640018765L;
	private int originalSize;
	private BufferedLongArrayQueue compressedTrace;

	/**
	 * Adds the given queue's contents to the trace. 
	 * ATTENTION: the queue's contents will be removed in the process! 
	 * @param trace
	 * a queue that should be compressed
	 * @param log
	 * whether to log some status information
	 */
	public CompressedLongTrace(BufferedLongArrayQueue trace, boolean log) {
		this.originalSize = trace.size();
		int iteration = 0;
		while (this.compressedTrace == null && iteration <= MAX_ITERATION_COUNT) {
			int oldSize = trace.size();
			// inherit whether buffered trace files should be deleted on exit
			BufferedLongArrayQueue traceWithoutRepetitions = extractRepetitions(trace, log, trace.isDeleteOnExit());
			trace = null;
			// did the trace size remain the same or did we reach the upper bound of iterations?
			if (oldSize == traceWithoutRepetitions.size() || ++iteration >= MAX_ITERATION_COUNT) {
				if (log) {
					System.out.println("=> " + traceWithoutRepetitions.size());
				}
				// no... then just store the compressed trace
				this.compressedTrace = traceWithoutRepetitions;
			} else {
				if (log) {
					System.out.println(oldSize + " -> " + traceWithoutRepetitions.size());
				}
				// yes... then try again recursively
				trace = traceWithoutRepetitions;
			}
		}
		if (getRepetitionMarkers() != null) {
			// reverse the order of repetition marker levels (smallest first)
			Collections.reverse(getRepetitionMarkers());
		}
	}
	
	public CompressedLongTrace(BufferedLongArrayQueue traceOfNodeIDs, CompressedIntegerTrace otherCompressedTrace) {
		this.originalSize = otherCompressedTrace.size();
		this.compressedTrace = traceOfNodeIDs;
		setRepetitionMarkers(otherCompressedTrace.getRepetitionMarkers());
	}
	
	public CompressedLongTrace(BufferedLongArrayQueue traceOfNodeIDs, CompressedLongTrace otherCompressedTrace) {
		this.originalSize = otherCompressedTrace.size();
		this.compressedTrace = traceOfNodeIDs;
		setRepetitionMarkers(otherCompressedTrace.getRepetitionMarkers());
	}

	public CompressedLongTrace(BufferedLongArrayQueue compressedTrace, BufferedArrayQueue<int[]> repetitionMarkers) {
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
	
	public long getMaxStoredValue() {
		long max = 0;
		ReplaceableCloneableLongIterator iterator = baseIterator();
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
	
//	// mapping from elements to their most recent positions in the result list
//	Map<K,Integer> elementToPositionMap = new HashMap<>();
//	BufferedIntArrayQueue traceWithoutRepetitions;
//	BufferedMap<int[]> traceRepetitions;
//	BufferedIntArrayQueue bufferTrace;
//	
//	boolean possibleRepetition = false;
//	Integer position;
//	int elementCount = 0;
//	int repetitionCounter = 0;
//	Iterator<T> resultTraceIterator;
//	
//	public void add(T element) {
//		if (possibleRepetition) {
//
//				if (!resultTraceIterator.hasNext()) {
//					// at the end of the sequence
//					++repetitionCounter;
//					// start over
//					resultTraceIterator = traceWithoutRepetitions.iterator(position);
//					// remove the processed nodes that have been repeated
//					bufferTrace.clear();
//				}
//
//
//				// check if elements are equal
//				if (!isEqual(element, resultTraceIterator.next())) {
//					// end of equal sequence
//					// are there any repetitions?
//					if (repetitionCounter > 0) {
//						// compute the length of one repetition
//						int length = (lengthToRemove+1)/repetitionCounter;
//
//						// add a triplet to the list
//						traceRepetitions.put(traceWithoutRepetitions.size() - length, new int[] { length, repetitionCounter + 1 });
//						//				if (log) {
//						//					System.out.println("idx: " + (traceWithoutRepetitions.size() - length) + ", len: " + length + ", rpt: " + (repetitionCounter+1));
//						//				}
//
//						// reset repetition recognition
//						elementToPositionMap.clear();
//					} else {
//						// no repetition found...
//						// no repetition: remember only the last node containing the element (update)
//						elementToPositionMap.put(repr, traceWithoutRepetitions.size());
//						// build up the result trace on the fly
//						traceWithoutRepetitions.add(element);
//					}
//				}
//		} else {
//			K repr = getRepresentation(element);
//
//			// check for repetition of the current element
//			position = elementToPositionMap.get(repr);
//			if (position == null) {
//				// no repetition: remember node containing the element
//				elementToPositionMap.put(repr, traceWithoutRepetitions.size());
//				// build up the result trace on the fly
//				traceWithoutRepetitions.add(element);
//			} else {
//				possibleRepetition = true;
//				resultTraceIterator = traceWithoutRepetitions.iterator(position);
//				resultTraceIterator.next();
//				bufferTrace.add(element);
//			}
//		}
//	}

	private BufferedLongArrayQueue extractRepetitions(BufferedLongArrayQueue trace, boolean log, boolean deleteOnExit) {
		int originalTraceSize = trace.size();
		String filePrefix = "cpr_trace_" + UUID.randomUUID().toString();
		BufferedLongArrayQueue traceWithoutRepetitions = 
				new BufferedLongArrayQueue(trace.getOutputDir(), filePrefix, 
						trace.getNodeSize(), deleteOnExit);
		BufferedMap<int[]> traceRepetitions = new RepetitionMarkerBufferedMap(trace.getOutputDir(), 
				"cpr_trace_rpt_" + UUID.randomUUID().toString(), trace.getArrayLength(), deleteOnExit);
		MyBufferedLongIterator resultTraceIterator = traceWithoutRepetitions.iterator();
		MyBufferedLongIterator inputTraceIterator = trace.iterator();
		
		// mapping from elements to their most recent positions in the result list
		Map<Long,List<Integer>> elementToPositionMap = new HashMap<>();
		while (!trace.isEmpty()) {
			long element = trace.remove();

			// check for repetition of the current element
			List<Integer> positions = elementToPositionMap.get(element);
			if (positions == null) {
				// no repetition: remember node containing the element
				List<Integer> list = new ArrayList<>();
				list.add(traceWithoutRepetitions.size());
				elementToPositionMap.put(element, list);
				// build up the result trace on the fly
				traceWithoutRepetitions.add(element);
			} else {
				boolean foundRepetition = false;
				// check for all remembered positions
				for (int position : positions) {
					// element was repeated
					// check if the sequence of elements between the last position of the element
					// and this position is the same as the following sequence(s) in the input trace
					int repetitionCounter = 0;
					int lengthToRemove = 0;
					// avoid instantiating new iterators
					inputTraceIterator.setToPosition(0);
					resultTraceIterator.setToPosition(position+1);
					//				resultTraceIterator.next();
					// count the number of elements that need to be removed later with count variable;
					// variable count can start at 0 here, since we already removed the very first element
					for (int count = 0; ; ++count) {
						if (!resultTraceIterator.hasNext()) {
							// at the end of the sequence
							++repetitionCounter;
							// start over
							resultTraceIterator.setToPosition(position);
							// later remove the processed nodes that have been repeated
							lengthToRemove += count;
							count = 0;
						}

						if (!inputTraceIterator.hasNext()) {
							// no further remaining sequence
							break;
						}

						// check if elements are equal
						if (inputTraceIterator.next() != resultTraceIterator.next()) {
							break;
						}

						// continue with the next node of the remaining sequence
					}

					// are there any repetitions?
					if (repetitionCounter > 0) {
						foundRepetition = true;
						// remove repeated elements
						trace.clear(lengthToRemove);
						// compute the length of one repetition
						int length = (lengthToRemove+1)/repetitionCounter;

						// add a triplet to the list
						traceRepetitions.put(traceWithoutRepetitions.size() - length, new int[] { length, repetitionCounter + 1 });
						//					if (log) {
						//						System.out.println("idx: " + (traceWithoutRepetitions.size() - length) + ", len: " + length + ", rpt: " + (repetitionCounter+1));
						//					}

						// reset repetition recognition
						elementToPositionMap.clear();
						break;
					}
				}

				if (!foundRepetition) {
					// no repetition found...
					//							// no repetition: remember only the last node containing the element (update)
					//							elementToPositionMap.put(element, traceWithoutRepetitions.size());

					// add the new position to the list of remembered positions
					positions.add(traceWithoutRepetitions.size());
					// build up the result trace on the fly
					traceWithoutRepetitions.add(element);
				}
			}
		}

		if (!traceRepetitions.isEmpty()) {
			traceRepetitions.sleep();
			addRepetitionMarkers(traceRepetitions, originalTraceSize);
		}
		return traceWithoutRepetitions;
	}

	public BufferedLongArrayQueue getCompressedTrace() {
		return compressedTrace;
	}

	//	public T[] reconstructFullTrace() {
//		return reconstructTrace();
//	}
//
//	protected T[] reconstructTrace() {
//		if (child == null) {
//			return this.compressedTrace;
//		}
//		
//		// got a child object? then reconstruct the trace...
//		T[] compressedTrace = child.reconstructTrace();
//
//		T[] result = newArrayOfSize(originalSize);
//		int startPos = 0;
//		int currentIndex = 0;
//		Set<Integer> keySet = repetitionMarkers.keySet();
//		int k = 0;
//		int[] keyArray = new int[keySet.size()];
//		for (Iterator<Integer> iterator = keySet.iterator(); iterator.hasNext();) {
//			keyArray[k++] = iterator.next();
//		}
//		Arrays.sort(keyArray);
//		for (int j = 0; j < keyArray.length; ++j) {
//			int[] repetitionMarker = repetitionMarkers.get(keyArray[j]);
//			// key: rangeStart, value: [length, repetitionCount]
//			int unprocessedLength = keyArray[j] - startPos;
//			if (unprocessedLength > 0) {
//				// the previous sequence has not been repeated
//				System.arraycopy(compressedTrace, startPos, result, currentIndex, unprocessedLength);
//				// move the index for the result array
//				currentIndex += unprocessedLength;
//			}
//
//			// add the repeated sequences
//			for (int i = 0; i < repetitionMarker[1]; ++i) {
//				System.arraycopy(compressedTrace, keyArray[j], result, currentIndex, repetitionMarker[0]);
//				currentIndex += repetitionMarker[0];
//			}
//
//			// move the start position in the source trace array
//			startPos = keyArray[j] + repetitionMarker[0];
//		}
//
//		if (startPos < compressedTrace.length) {
//			// the remaining sequence has not been repeated
//			System.arraycopy(compressedTrace, startPos, result, currentIndex, compressedTrace.length - startPos);
//		}
//		compressedTrace = null;
//
//		return result;
//	}
//
//	public abstract T[] newArrayOfSize(int size);

	/**
	 * @return
	 * iterator over the full trace
	 */
	public LongTraceIterator iterator() {
		return new LongTraceIterator(this);
	}
	
	/**
	 * @return
	 * iterator over the compressed trace (ignores repetitions)
	 */
	public ReplaceableCloneableLongIterator baseIterator() {
		return compressedTrace.iterator();
	}
	
	/**
	 * @return
	 * reverse iterator over the full trace, starting at the end of the trace
	 */
	public LongTraceReverseIterator reverseIterator() {
		return new LongTraceReverseIterator(this);
	}
	
//	public Set<Integer> computeStartingElements() {
//		Set<Integer> set = new HashSet<>();
//		addStartingElementsToSet(set);
//		return set;
//	}
	
	public void addStartingElementsToSet(Set<Long> set) {
		LongTraceIterator iterator = iterator();
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
		super.clear();
		compressedTrace.clear();
	}

	public long getFirstElement() {
		return getCompressedTrace().element();
	}

	public long getLastElement() {
		return getCompressedTrace().lastElement();
	}

	@Override
	public void sleep() {
		super.sleep();
		compressedTrace.sleep();
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
		StringBuilder builder = new StringBuilder();
		builder.append("lvl0, size: ").append(compressedTrace.size()).append(System.lineSeparator());
		builder.append(super.toString());
		builder.append("full trace: ");
		LongTraceIterator eTraceIterator = iterator();
		while (eTraceIterator.hasNext()) {
			builder.append(eTraceIterator.next() + ", ");
		}
		return builder.toString();
	}
	
	@Override
	public void deleteIfMarked() {
		if (isMarkedForDeletion()) {
			this.unlock();
			super.clear();
			this.clear();
		}
	}
	
	@Override
	public void deleteOnExit() {
		super.deleteOnExit();
		compressedTrace.deleteOnExit();
	}
	
}
