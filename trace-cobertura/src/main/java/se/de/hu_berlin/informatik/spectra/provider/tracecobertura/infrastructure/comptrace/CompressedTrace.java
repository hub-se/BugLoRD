package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedMap;
import java.util.Set;
import java.util.UUID;

/**
 * An execution trace consists structurally of a list of executed nodes
 * and a list of tuples that mark repeated sequences in the trace.
 * 
 * @param <T>
 * type of elements in the trace
 * @param <K>
 * type of a representation for storing elements in a map
 */
public abstract class CompressedTrace<T,K> extends RepetitionMarkerBase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7150695907951467178L;
	private static final int MAP_CHUNK_SIZE = 500000;
	private int originalSize;
	private BufferedArrayQueue<T> compressedTrace;

	/**
	 * Adds the given queue's contents to the trace. 
	 * ATTENTION: the queue's contents will be removed in the process! 
	 * @param trace
	 * a queue that should be compressed
	 * @param log
	 * whether to log some status information
	 */
	public CompressedTrace(BufferedArrayQueue<T> trace, boolean log) {
		this.originalSize = trace.size();
		int iteration = 0;
		while (this.compressedTrace == null && iteration <= MAX_ITERATION_COUNT) {
			int oldSize = trace.size();
			// inherit whether buffered trace files should be deleted on exit
			BufferedArrayQueue<T> traceWithoutRepetitions = extractRepetitions(trace, log, trace.isDeleteOnExit());
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
			RepetitionMarkerWrapper[] temp = Arrays.copyOf(getRepetitionMarkers(), levelCount());
			int j = temp.length-1;
			for (int i = 0; i < temp.length; i++, --j) {
				getRepetitionMarkers()[i] = temp[j];
			}
		}
	}
	
	public CompressedTrace(BufferedArrayQueue<T> traceOfNodeIDs, CompressedTrace<?,?> otherCompressedTrace) {
		this.originalSize = otherCompressedTrace.size();
		this.compressedTrace = traceOfNodeIDs;
		setRepetitionMarkers(otherCompressedTrace.getRepetitionMarkers());
	}

	public CompressedTrace(BufferedArrayQueue<T> compressedTrace, List<Queue<Integer>> repetitionMarkers) {
		this.compressedTrace = compressedTrace;
		if (repetitionMarkers != null) {
			int size = compressedTrace.size();
			int index = 0;
			while (index < repetitionMarkers.size()) {
				BufferedMap<int[]> repMarkers = RepetitionMarkerBase.constructFromIntegerQueue(repetitionMarkers.get(index++), 
						compressedTrace.getOutputDir(), 
						compressedTrace.getFilePrefix() + "-map-" + index, MAP_CHUNK_SIZE,
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
			repetitionMarkers.clear();
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
		throw new UnsupportedOperationException("not implemented!");
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

	private BufferedArrayQueue<T> extractRepetitions(BufferedArrayQueue<T> trace, boolean log, boolean deleteOnExit) {
		int originalTraceSize = trace.size();
		String filePrefix = "cpr_trace_" + UUID.randomUUID().toString();
		BufferedArrayQueue<T> traceWithoutRepetitions = 
				new BufferedArrayQueue<>(trace.getOutputDir(), filePrefix, 
						trace.getNodeSize(), deleteOnExit, trace.getSerializationType());
		BufferedMap<int[]> traceRepetitions = new RepetitionMarkerBufferedMap(trace.getOutputDir(), 
				"cpr_trace_rpt_" + UUID.randomUUID().toString(), trace.getArrayLength(), deleteOnExit);
		ReplaceableCloneableIterator<T> resultTraceIterator = traceWithoutRepetitions.iterator();
		ReplaceableCloneableIterator<T> inputTraceIterator = trace.iterator();
		
		// mapping from elements to their most recent positions in the result list
		Map<K,List<Integer>> elementToPositionMap = new HashMap<>();
		while (!trace.isEmpty()) {
			T element = trace.remove();
			K repr = getRepresentation(element);

			// check for repetition of the current element
			List<Integer> positions = elementToPositionMap.get(repr);
			if (positions == null) {
				// no repetition: remember node containing the element
				List<Integer> list = new ArrayList<>();
				list.add(traceWithoutRepetitions.size());
				elementToPositionMap.put(repr, list);
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
						T first = inputTraceIterator.next();
						T second = resultTraceIterator.next();
						if (!isEqual(first, second)) {
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
					//							elementToPositionMap.put(repr, traceWithoutRepetitions.size());

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

	public abstract boolean isEqual(T first, T second);

	public abstract K getRepresentation(T element);


	public BufferedArrayQueue<T> getCompressedTrace() {
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
	public TraceIterator<T> iterator() {
		return new TraceIterator<>(this);
	}
	
	/**
	 * @return
	 * iterator over the compressed trace (ignores repetitions)
	 */
	public ReplaceableCloneableIterator<T> baseIterator() {
		return compressedTrace.iterator();
	}
	
	/**
	 * @return
	 * reverse iterator over the full trace, starting at the end of the trace
	 */
	public TraceReverseIterator<T> reverseIterator() {
		return new TraceReverseIterator<>(this);
	}
	
//	public Set<Integer> computeStartingElements() {
//		Set<Integer> set = new HashSet<>();
//		addStartingElementsToSet(set);
//		return set;
//	}
	
	public void addStartingElementsToSet(Set<T> set) {
		TraceIterator<T> iterator = iterator();
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

	public T getFirstElement() {
		return getCompressedTrace().element();
	}

	public T getLastElement() {
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
		StringBuilder builder = new StringBuilder(originalSize + " ==> ");
		TraceIterator<T> eTraceIterator = iterator();
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
