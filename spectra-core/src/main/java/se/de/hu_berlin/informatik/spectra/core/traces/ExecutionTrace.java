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
	private int[] trace;
	private List<Triplet> repetitionMarkers = new ArrayList<>();
	
	public ExecutionTrace(int[] trace) {
		this.originalSize = trace.length;
		this.trace = extractRepetitions(trace);
	}

	private int[] extractRepetitions(int[] traceArray) {
		List<Integer> traceWithoutRepetitions = new ArrayList<>();
		int currentIndex = 0;
		
		// mapping from elements to their most recent positions
		Map<Integer,Integer> elementToPositionMap = new HashMap<>();
		int startingPosition = 0;
		for (int i = 0; i < traceArray.length; i++) {
			int element = traceArray[i];

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
				if (i + length - 1 < traceArray.length) {
					int repetitionCounter = 0;
					for (int pos = 0; i + pos < traceArray.length; ++pos) {
						if (traceArray[position + pos] != traceArray[i + pos]) {
							break;
						}
						// check for end of sequence
						if ((pos + 1) % length == 0) {
							++repetitionCounter;
						}
					}
					if (repetitionCounter > 0) {
						// add the previous sequence
						for (int pos = startingPosition; pos < position; ++pos) {
							traceWithoutRepetitions.add(traceArray[pos]);
						}
						currentIndex += position - startingPosition;
						
						// add a triplet to the list
						repetitionMarkers.add(new Triplet(currentIndex, length, repetitionCounter + 1));
						System.out.println("index: " + currentIndex + ", length: " + length + ", repetitions: " + (repetitionCounter+1));
						// add one repeated sequence to the trace
						for (int pos = position; pos < position + length; ++pos) {
							traceWithoutRepetitions.add(traceArray[pos]);
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
		if (startingPosition < traceArray.length) {
			// there exists an unprocessed sequence 
			// before this element's position
			
			// add the previous sequence
			for (int pos = startingPosition; pos < traceArray.length; ++pos) {
				traceWithoutRepetitions.add(traceArray[pos]);
			}
			
			// forget all previously remembered positions of elements
			elementToPositionMap.clear();
		}

		return traceWithoutRepetitions.stream().mapToInt(i->i).toArray();
	}

	public int[] getCompressedTrace() {
		return trace;
	}
	
	public int[] getFullTrace() {
		return reconstructTrace();
	}

	private int[] reconstructTrace() {
		int[] result = new int[originalSize];
		int startPos = 0;
		int currentIndex = 0;
		for (Triplet triplet : repetitionMarkers) {
			int unprocessedLength = triplet.getRangeStart() - startPos;
			if (unprocessedLength > 0) {
				// the previous sequence has not been repeated
				System.arraycopy(trace, startPos, result, currentIndex, unprocessedLength);
				// move the index for the result array
				currentIndex += unprocessedLength;
			}
			
			// add the repeated sequences
			for (int i = 0; i < triplet.getCount(); ++i) {
				System.arraycopy(trace, triplet.getRangeStart(), result, currentIndex, triplet.getRangeLength());
				currentIndex += triplet.getRangeLength();
			}
			
			// move the start position in the source trace array
			startPos = triplet.getRangeStart() + triplet.getRangeLength();
		}
		
		if (startPos < trace.length) {
			// the remaining sequence has not been repeated
			System.arraycopy(trace, startPos, result, currentIndex, trace.length - startPos);
		}
		
		return result;
	}
	
}
