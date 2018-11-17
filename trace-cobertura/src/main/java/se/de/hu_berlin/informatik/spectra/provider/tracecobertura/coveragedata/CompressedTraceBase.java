package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedQueue.Node;

/**
 * An execution trace consists structurally of a list of executed nodes
 * and a list of tuples that mark repeated sequences in the trace.
 *
 * @param <T>
 * type of elements in the trace
 * @param <K>
 * type of a representation for storing elements in a map
 */
public abstract class CompressedTraceBase<T, K> implements Serializable, Iterable<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5903218865249529299L;
	
	private int originalSize;
	private T[] compressedTrace;
	private int[] repetitionMarkers;
	
	private CompressedTraceBase<T,K> child;
	
	public CompressedTraceBase(SingleLinkedQueue<T> trace, boolean log) {
		this.originalSize = trace.size();
		SingleLinkedQueue<T> traceWithoutRepetitions = extractRepetitions(trace, log);
		trace = null;
		// did something change?
		if (originalSize == traceWithoutRepetitions.size()) {
			if (log) {
				System.out.println("=> " + originalSize);
			}
			// no... then just store the compressed trace TODO keep as queue?
			this.compressedTrace = newArrayOfSize(traceWithoutRepetitions.size());
			for (int i = 0; i < this.compressedTrace.length; ++i) {
				compressedTrace[i] = traceWithoutRepetitions.remove();
			}
		} else {
			if (log) {
				System.out.println(originalSize + " -> " + traceWithoutRepetitions.size());
			}
			// yes... then try again recursively
			this.child = newChildInstance(traceWithoutRepetitions, log);
		}
	}
	
	public CompressedTraceBase(SingleLinkedQueue<T> traceOfNodeIDs, CompressedTraceBase<?,?> otherCompressedTrace) {
		if (otherCompressedTrace.getChild() == null) {
			this.compressedTrace = newArrayOfSize(traceOfNodeIDs.size());
			for (int i = 0; i < this.compressedTrace.length; ++i) {
				compressedTrace[i] = traceOfNodeIDs.remove();
			}
		} else {
			this.repetitionMarkers = otherCompressedTrace.getRepetitionMarkers();
			this.child = newChildInstance(traceOfNodeIDs, otherCompressedTrace.getChild());
			this.originalSize = computeFullTraceLength();
		}
	}
	
	public CompressedTraceBase(T[] compressedTrace, List<int[]> repMarkerLists, int index) {
		if (index >= repMarkerLists.size()) {
			this.compressedTrace = compressedTrace;
		} else {
			this.repetitionMarkers = repMarkerLists.get(index);
			this.child = newChildInstance(compressedTrace, repMarkerLists, ++index);
			this.originalSize = computeFullTraceLength();
		}
	}
	
	public abstract CompressedTraceBase<T,K> newChildInstance(SingleLinkedQueue<T> trace, CompressedTraceBase<?,?> otherCompressedTrace);
	
	public abstract CompressedTraceBase<T,K> newChildInstance(T[] compressedTrace, List<int[]> repMarkerLists, int index);
	
	public abstract CompressedTraceBase<T,K> newChildInstance(SingleLinkedQueue<T> trace, boolean log);
	
	public int getMaxStoredValue() {
		throw new UnsupportedOperationException("not implemented!");
	}
	
	public int computeFullTraceLength() {
		if (child == null) {
			return compressedTrace.length;
		}
		
		int length = child.computeFullTraceLength();
		for (int j = 0; j < repetitionMarkers.length; j += 3) {
			// rangeStart, length, repetitionCount
			length += (repetitionMarkers[j+1] * (repetitionMarkers[j+2]-1));
		}
		return length;
	}

	private SingleLinkedQueue<T> extractRepetitions(SingleLinkedQueue<T> trace, boolean log) {
		SingleLinkedQueue<T> traceWithoutRepetitions = new SingleLinkedQueue<>();
		List<Integer> traceRepetitions = new ArrayList<>();
		
		// mapping from elements to their most recent positions in the result list
		Map<K,SingleLinkedQueue.Node<T>> elementToPositionMap = new HashMap<>();
		while (!trace.isEmpty()) {
			T element = trace.remove();
			K repr = getRepresentation(element);

			// check for repetition of the current element
			Node<T> position = elementToPositionMap.get(repr);
			if (position == null) {
				// build up the result trace on the fly
				traceWithoutRepetitions.add(element);
				// no repetition: remember node containing the element
				elementToPositionMap.put(repr, traceWithoutRepetitions.getLastNode());
			} else {
				// element was repeated
				// check if the sequence of elements between the last position of the element
				// and this position is the same as the following sequence(s) in the input trace
				int repetitionCounter = 0;
				int lengthToRemove = 0;
				Iterator<T> inputTraceIterator = trace.iterator();
				Node<T> currentNode = position.next;
				// count the number of elements that need to be removed later with count variable;
				// variable count can start at 0 here, since we already removed the very first element
				for (int count = 0; ; ++count) {
					if (currentNode == null) {
						// at the end of the sequence
						++repetitionCounter;
						// start over
						currentNode = position;
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
					T second = currentNode.item;
					if (!isEqual(first, second)) {
						break;
					}
					
					// continue with the next node of the remaining sequence
					currentNode = currentNode.next;
				}
				// remove repeated elements
				trace.clear(lengthToRemove);
				

				// are there any repetitions?
				if (repetitionCounter > 0) {
					// compute the length of one repetition
					int length = (lengthToRemove+1)/repetitionCounter;
					
					// add a triplet to the list
					traceRepetitions.add(traceWithoutRepetitions.size() - length);
					traceRepetitions.add(length);
					traceRepetitions.add(repetitionCounter + 1);
					if (log) {
						System.out.println("idx: " + (traceWithoutRepetitions.size() - length) + ", len: " + length + ", rpt: " + (repetitionCounter+1));
					}
					
					// reset repetition recognition
					elementToPositionMap.clear();
				} else {
					// no repetition found...
					// build up the result trace on the fly
					traceWithoutRepetitions.add(element);
					// no repetition: remember only the last node containing the element (update)
					elementToPositionMap.put(repr, traceWithoutRepetitions.getLastNode());
				}
			}
		}

		if (!traceRepetitions.isEmpty()) {
			this.repetitionMarkers = new int[traceRepetitions.size()];
			for (int i = 0; i < traceRepetitions.size(); ++i) {
				repetitionMarkers[i] = traceRepetitions.get(i);
			}
		}
		return traceWithoutRepetitions;
	}

	public abstract boolean isEqual(T first, T second);

	public abstract K getRepresentation(T element);

	public T[] getCompressedTrace() {
		if (child != null) {
			return child.getCompressedTrace();
		} else {
			return compressedTrace;
		}
	}

	public int[] getRepetitionMarkers() {
		return repetitionMarkers;
	}
	
	public T[] reconstructFullTrace() {
		return reconstructTrace();
	}

	protected T[] reconstructTrace() {
		if (child == null) {
			return this.compressedTrace;
		}
		
		// got a child object? then reconstruct the trace...
		T[] compressedTrace = child.reconstructTrace();

		T[] result = newArrayOfSize(originalSize);
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
		compressedTrace = null;

		return result;
	}

	public abstract T[] newArrayOfSize(int size);

	public CompressedTraceBase<T,K> getChild() {
		return child;
	}

	@Override
	public TraceIterator<T> iterator() {
		return new TraceIterator<>(this, Integer.MAX_VALUE);
	}
	
	public TraceIterator<T> iterator(int maxRepetitionCount) {
		return new TraceIterator<>(this,maxRepetitionCount);
	}
	
}
