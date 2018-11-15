package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An execution trace consists structurally of a list of executed nodes (or references to node lists)
 * and a list of tuples that mark repeated sequences in the trace.
 *
 */
public class ExecutionTrace {

	private int originalSize;
	private int[] compressedTrace;
	private int[] repetitionMarkers;
	
	private ExecutionTrace child;
	
	private ExecutionTrace(List<Integer> trace, ExecutionTrace parent) {
		this.originalSize = trace.size();
		List<Integer> traceWithoutRepetitions = extractRepetitions(trace);
		// did something change?
		if (originalSize == traceWithoutRepetitions.size()) {
//			System.out.println("e=> " + originalSize);
			// no... then just store the compressed trace
			this.compressedTrace = traceWithoutRepetitions.stream().mapToInt(i->i).toArray();
		} else {
//			System.out.println(originalSize + " e-> " + traceWithoutRepetitions.size());
			// yes... then try again recursively
			this.child = new ExecutionTrace(traceWithoutRepetitions, this);
		}
	}
	
	public ExecutionTrace(List<Integer> trace) {
		this(trace, null);
	}
	
	public ExecutionTrace(List<int[]> compressedTrace, int index) {
		if (index >= compressedTrace.size()) {
			this.compressedTrace = compressedTrace.get(0);
		} else {
			this.repetitionMarkers = compressedTrace.get(index);
			this.child = new ExecutionTrace(compressedTrace, ++index);
			this.originalSize = computeFullTraceLength();
		}
	}

	private int computeFullTraceLength() {
		if (child == null) {
			return compressedTrace.length;
		}
		
		int length = child.computeFullTraceLength();
		for (int j = 0; j < repetitionMarkers.length; j += 3) {
			// rangeStart, length, repetitionCount
			length += (repetitionMarkers[j+1] * repetitionMarkers[j+2]) - 1;
		}
		return length;
	}

	private List<Integer> extractRepetitions(List<Integer> trace) {
		ArrayList<Integer> traceWithoutRepetitions = new ArrayList<>();
		List<Integer> traceRepetitions = new ArrayList<>();
		int currentIndex = 0;
		
		// mapping from elements to their most recent positions
		Map<Integer,Integer> elementToPositionMap = new HashMap<>();
		int startingPosition = 0;
		for (int i = 0; i < trace.size(); i++) {
			int element = trace.get(i);

			// check for repetition of the current element
			Integer position = elementToPositionMap.get(element);
			if (position == null) {
				// no repetition: remember position of element
				elementToPositionMap.put(element, i);
			} else {
				// element was repeated
				// check if the sequence of elements between the last position of the element
				// and this position is the same as the following sequence
				int length = i - position;
				// only check if there would actually be enough space for a repeated sequence
				if (i + length - 1 < trace.size()) {
					int repetitionCounter = 0;
					for (int pos = 0; i + pos < trace.size(); ++pos) {
						if (trace.get(position + pos) != trace.get(i + pos)) {
							break;
						}
						// check for end of sequence
						if ((pos + 1) % length == 0) {
							++repetitionCounter;
						}
					}
					// the length of the complete repeated parts needs to be greater than 
					// the length of one part + 3 to be worth the effort
					if (repetitionCounter > 0 // && (repetitionCounter + 1) * length > length + 3
							) {
						traceWithoutRepetitions.ensureCapacity(
								traceWithoutRepetitions.size() + (position + length - startingPosition));
						// add the previous sequence
						for (int pos = startingPosition; pos < position; ++pos) {
							traceWithoutRepetitions.add(trace.get(pos));
							trace.set(pos, null);
						}
						currentIndex += position - startingPosition;
						
						// add a triplet to the list
						traceRepetitions.add(currentIndex);
						traceRepetitions.add(length);
						traceRepetitions.add(repetitionCounter + 1);
//						System.out.println("index: " + currentIndex + ", length: " + length + ", repetitions: " + (repetitionCounter+1));
						// add one repeated sequence to the trace
						for (int pos = position; pos < position + length; ++pos) {
							traceWithoutRepetitions.add(trace.get(pos));
							trace.set(pos, null);
						}
						currentIndex += length;
						// continue after the repeated sequences;
						// right now, i is at the beginning of the second repetition,
						// so move i by the count of repetitions decreased by one
						i += ((repetitionCounter) * length) - 1;
						// reset repetition recognition and the index to continue
						elementToPositionMap.clear();
						// reset the starting position (all previous sequences have been processed)
						startingPosition = i + 1;
					} else {
						// no repetitions found: update position of element;
						// this only stores the most recent position of each element
						elementToPositionMap.put(element, i);
					}
				} else {
					// no repetitions possible: update position of element;
					// this only stores the most recent position of each element
					elementToPositionMap.put(element, i);
				}
			}
		}
		
		// process remaining elements
		if (startingPosition < trace.size()) {
			// there exists an unprocessed sequence 
			// before this element's position
			
			traceWithoutRepetitions.ensureCapacity(
					traceWithoutRepetitions.size() + (trace.size() - startingPosition));
			// add the previous sequence
			for (int pos = startingPosition; pos < trace.size(); ++pos) {
				traceWithoutRepetitions.add(trace.get(pos));
				trace.set(pos, null);
			}
			
			// forget all previously remembered positions of elements
			elementToPositionMap.clear();
		}

		if (!traceRepetitions.isEmpty()) {
			this.repetitionMarkers = traceRepetitions.stream().mapToInt(i->i).toArray();
		}
		
		return traceWithoutRepetitions;
	}

	public int[] getCompressedTrace() {
		if (child != null) {
			return child.getCompressedTrace();
		} else {
			return compressedTrace;
		}
	}
	
	public int[] getRepetitionMarkers() {
		return repetitionMarkers;
	}
	
	public int[] reconstructFullIndexedTrace() {
		return reconstructTrace();
	}

	private int[] reconstructTrace() {
		if (child == null) {
			return this.compressedTrace;
		}
		
		// got a child object? then reconstruct the trace...
		int[] compressedTrace = child.reconstructTrace();
		
		int[] result = new int[originalSize];
		int startPos = 0;
		int currentIndex = 0;
		for (int j = 0; j < repetitionMarkers.length; j += 3) {
			// rangeStart, length, repetitionCount
			int unprocessedLength = repetitionMarkers[j] - startPos;
			if (unprocessedLength > 0) {
				// the previous sequence has not been repeated
				System.arraycopy(compressedTrace, startPos, result, currentIndex, unprocessedLength);
				// move the index for the result array
				currentIndex += unprocessedLength;
			}
			
			// add the repeated sequences
			for (int i = 0; i < repetitionMarkers[j+2]; ++i) {
				System.arraycopy(compressedTrace, repetitionMarkers[j], result, currentIndex, repetitionMarkers[j+1]);
				currentIndex += repetitionMarkers[j+1];
			}
			
			// move the start position in the source trace array
			startPos = repetitionMarkers[j] + repetitionMarkers[j+1];
		}
		
		if (startPos < compressedTrace.length) {
			// the remaining sequence has not been repeated
			System.arraycopy(compressedTrace, startPos, result, currentIndex, compressedTrace.length - startPos);
		}
		
		return result;
	}

	public int[] reconstructFullTrace(SequenceIndexer indexer) {
		int[] indexedFullTrace = reconstructTrace();
		List<Integer> fullTrace = new ArrayList<>();
		for (int index : indexedFullTrace) {
			int[] sequence = indexer.getSequence(index);
			for (int i : sequence) {
				fullTrace.add(i);
			}
		}
		return fullTrace.stream().mapToInt(i -> i).toArray();
	}

	public int getMaxStoredValue() {
		if (child == null) {
			int max = 0;
			for (int i : compressedTrace) {
				max = Math.max(i, max);
			}
			return max;
		}
		
		int max = child.getMaxStoredValue();
		for (int i : repetitionMarkers) {
			max = Math.max(i, max);
		}
		return max;
	}

	public ExecutionTrace getChild() {
		return child;
	}
	
}
