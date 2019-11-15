package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.longs;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedMap;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.SingleLinkedArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedLongArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedLongArrayQueue.MyBufferedLongIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.RepetitionMarkerBufferedMap;

/**
 * An execution trace consists structurally of a list of executed nodes
 * and a list of tuples that mark repeated sequences in the trace.
 */
public class CompressedLongTraceLevel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5908947588259301365L;

	private int originalSize = 0;

	BufferedLongArrayQueue bufferTrace;
	BufferedLongArrayQueue traceWithoutRepetitions;
	BufferedMap<int[]> traceRepetitions;
	
	MyBufferedLongIterator resultTraceIterator;
	MyBufferedLongIterator inputTraceIterator;
	
	// mapping from elements to their most recent positions in the result list
	Map<Long,Queue<Integer>> elementToPositionMap = new HashMap<>();
	// current positions
	Queue<Integer> positions = null;
	

	int maxBufferSize = 0;
	int maxTraceSize = 0;
	
	public CompressedLongTraceLevel(File outputDir, String prefix, int nodeSize, int mapSize, boolean deleteOnExit) {
		String uuid = UUID.randomUUID().toString();
		bufferTrace = new BufferedLongArrayQueue(outputDir, 
				prefix + "cpr_trace_buf_" + uuid, nodeSize, deleteOnExit);
		traceWithoutRepetitions = new BufferedLongArrayQueue(outputDir,
				prefix + "cpr_trace_lvl_" + uuid, nodeSize, deleteOnExit);
		traceRepetitions = new RepetitionMarkerBufferedMap(outputDir, 
				prefix + "cpr_trace_rpt_" + uuid, mapSize, deleteOnExit);
		
		resultTraceIterator = traceWithoutRepetitions.iterator();
		inputTraceIterator = bufferTrace.iterator();
	}
	
	public int size() {
		return originalSize;
	}
	
	public boolean isEmpty( ) {
		return originalSize <= 0;
	}
	
	
	boolean repetitionCheckMode = false;
	int inRepetitionFromPosition = -1;
	int repetitionCounter = 0;
	int repetitionLength = 0;
	
	private void resetState() {
		repetitionCheckMode = false;
		inRepetitionFromPosition = -1;
		repetitionCounter = 0;
		repetitionLength = 0;
	}
			
	public boolean add(long element, boolean checkForRepetitions) {
		// there are certain circumstances, in which we don't want to check for 
		// repetitions in processed elements;
		// (mainly if we reach the maximum number of iterations/levels)
		if (!checkForRepetitions) {
			traceWithoutRepetitions.add(element);
			return false;
		}
		
		// different procedures based on the state...
		if (repetitionCheckMode) {
			// in repetition check mode/state, we build up a buffer trace instead of the real trace.
			// repeated parts are removed from the buffer trace on the fly
//			System.out.println("(check) buffer add: " + element + ", pos: " + traceWithoutRepetitions.size() + ", bs: " + bufferTrace.size());
			bufferTrace.add(element);
			maxBufferSize = Math.max(maxBufferSize, bufferTrace.size());

			// ensure that we are either in a state with not enough information (buffer)
			// or in a state right after a repetition! (false or true, respectively);
			return addFromBuffer(false, false);
		} else {
			// no repetition check mode!
			// check for repetition of the current element
			positions = elementToPositionMap.get(element);
			if (positions == null) {
				// no repetition possible; remember node containing the element
				Queue<Integer> list = new SingleLinkedArrayQueue<>(5);
				list.add(traceWithoutRepetitions.size());
				elementToPositionMap.put(element, list);
//				System.out.println("norm add: " + element + ", pos: " + traceWithoutRepetitions.size() + ", bs: " + bufferTrace.size());
				// build up the result trace on the fly
				traceWithoutRepetitions.add(element);
				maxTraceSize = Math.max(maxTraceSize, traceWithoutRepetitions.size());
				++originalSize;
				return false;
			} else {
				// already have seen this element before!
				// switch to repetition check mode
				repetitionCheckMode = true;
				// will add the element to the buffer trace and continue from there...
				return add(element, checkForRepetitions);
			}
		}
	}
	
	public boolean addFromBuffer(boolean recheckBuffer, boolean endOfLine) {
		Boolean result = null;
		while (result == null) {
			// if "null" was returned, then we check the next element in the buffer;
			// we repeat adding buffer elements until we get to a finished repetition (true)
			// or to a point with not enough information (false);
			// if endOfLine is true, this will only return false when the buffer is empty, eventually
			result = _addFromBuffer(recheckBuffer, endOfLine);
		}
		
		// ensure that we are either in a state with not enough information (buffer)
		// or in a state right after a repetition! (return false or true, respectively);
		// if endOfLine is true, this will only return false when the buffer is empty, eventually
		return result.booleanValue();
	}
	
	private Boolean _addFromBuffer(boolean recheckBuffer, boolean endOfLine) {
		if (endOfLine) {
			// we will not add new elements at this point;
			// so if there are any lingering repetitions, we have to finish them up now!
			if (repetitionCounter > 0) {
				// add a marker for the the current (finished) repetition
				addCurrentRepetitionMarker();
				resetStateAndContinue();
				// we can add all of the previously seen trace to the next compression level;
				// after we return true, we expect the supervising method to work through the remaining buffer
				// after feeding the finished part of the trace to the next level
				return true;
			}
		}
		
		// an empty buffer means not enough information!
		if (bufferTrace.isEmpty()) {
			return false;
		}
		
		// different procedures based on the state...
		if (repetitionCheckMode) {
			// in repetition check mode/state, we build up a buffer trace instead of the real trace.
			// repeated parts are removed from the buffer trace on the fly
//			System.out.println("check buf: " + bufferTrace.element() + ", pos: " + traceWithoutRepetitions.size() + ", bs: " + bufferTrace.size());
			return checkForRepetitions(recheckBuffer, endOfLine);
		} else {
			// no repetition check mode!
			// check for repetition of the current element
			positions = elementToPositionMap.get(bufferTrace.element());
			if (positions == null) {
				// no repetition possible; remember node containing the element
				Queue<Integer> list = new SingleLinkedArrayQueue<>(5);
				list.add(traceWithoutRepetitions.size());
				elementToPositionMap.put(bufferTrace.element(), list);
//				System.out.println("norm buf: " + bufferTrace.element() + ", pos: " + traceWithoutRepetitions.size() + ", bs: " + bufferTrace.size());
				// build up the result trace on the fly
				traceWithoutRepetitions.add(bufferTrace.remove());
				maxTraceSize = Math.max(maxTraceSize, traceWithoutRepetitions.size());
				++originalSize;
				return null;
			} else {
				// already have seen this element before!
				// switch to repetition check mode
				repetitionCheckMode = true;
				// will continue from there...
				return null;
			}
		}
	}

	private Boolean checkForRepetitions(boolean recheckBuffer, boolean endOfLine) {
		// are we in a (potentially) ongoing repetition?
		if (inRepetitionFromPosition < 0) {
			// no: potential (new) repetition from the last stored positions...

			// check for all remembered positions
			for (int position : positions) {
				// enough space for a repetition? (only check if there can be exactly one repetition)
				if (bufferTrace.size() >= traceWithoutRepetitions.size() - position) {
					// element was repeated
					// check if the sequence of elements between the last position of the element
					// and this position is the same as the following sequence(s) in the input trace

					// updates state variables and potentially removes 
					// repeated elements from the start of the buffer trace
					checkForRepetitionsFromPosition(position);
//					System.out.println("1idx: " + (originalSize - repetitionLength) + ", len: " + repetitionLength + ", rpt: " + (repetitionCounter+1));
					// found some repetition...
					if (repetitionCounter > 0) {
						break;
					}
				}

				if (!recheckBuffer && bufferTrace.size() >= traceWithoutRepetitions.size() - position) {
					// may stop looking (already checked smaller parts)
					break;
				}
			}

			// are there any repetitions and are we not still inside of a repetition (potentially)?
			if (inRepetitionFromPosition < 0 && repetitionCounter > 0) {
				// add a marker for the the current (finished) repetition
				addCurrentRepetitionMarker();
				resetStateAndContinue();
				// we can add all of the previously seen trace to the next compression level;
				// after we return true, we expect the supervising method to work through the remaining buffer
				// after feeding the finished part of the trace to the next level
				return true;
			} else if (endOfLine || (inRepetitionFromPosition < 0 && bufferTrace.size() >= traceWithoutRepetitions.size() - positions.peek())) {
				// no repetition found with a sufficiently large buffer trace...
				// add the new position to the list of remembered positions
				positions.clear();
				positions.add(traceWithoutRepetitions.size());
//				System.out.println("check sec add: " + bufferTrace.element() + ", pos: " + traceWithoutRepetitions.size() + ", bs: " + bufferTrace.size());
				// build up the result trace on the fly
				traceWithoutRepetitions.add(bufferTrace.remove());
				maxTraceSize = Math.max(maxTraceSize, traceWithoutRepetitions.size());
				++originalSize;
				resetStateAndContinue();
				// no repetition... should start over checking the remaining buffer?
				return null;
			}
			
			// either in an ongoing repetition or not enough buffer
			return false;
		} else {
			// yes: we're in an ongoing repetition and need to check for following repetitions of that same sequence
			
			// enough space for (another) repetition?
			if (bufferTrace.size() >= traceWithoutRepetitions.size() - inRepetitionFromPosition) {
				// updates state variables and potentially removes 
				// repeated elements from the start of the buffer trace
				checkForRepetitionsFromPosition(inRepetitionFromPosition);
//				System.out.println("2idx: " + (originalSize - repetitionLength) + ", len: " + repetitionLength + ", rpt: " + (repetitionCounter+1));
				// are there any repetitions and are we not inside of a repetition (potentially)?
				// repetition counter should not be 0 hat this point! (we check anyway)
				if (inRepetitionFromPosition < 0 && repetitionCounter > 0) {
					// add a marker for the the current (finished) repetition
					addCurrentRepetitionMarker();
					resetStateAndContinue();
					// we can add all of the previously seen trace to the next compression level;
					// after we return true, we expect the supervising method to work through the remaining buffer
					// after feeding the finished part of the trace to the next level
					return true;
				} else if (inRepetitionFromPosition < 0) {
					// should not happen!
					throw new IllegalStateException("No repetitions tracked...");
				}
			}
			
			// not enough buffer
			return false;
		}
	}

	private void addCurrentRepetitionMarker() {
		if (repetitionCounter > 0) {
			// add a marker to the list: (key: start index, value: { length, #repetitions})
			traceRepetitions.put(
					originalSize - repetitionLength, 
					new int[] { repetitionLength, repetitionCounter + 1 });
//			if (log) {
//			System.out.println(this + "new idx: " + (originalSize - repetitionLength) + ", len: " + repetitionLength + ", rpt: " + (repetitionCounter+1));
//			}

			// reset repetition recognition
			elementToPositionMap.clear();
			positions.clear();
			positions = null;
			
			// may feed the already processed part of the trace to the next compression level
		}
	}

	private void resetStateAndContinue() {
		resetState();
//		// work through the buffer trace as much as possible
//		if (!bufferTrace.isEmpty()) {
//			// will append the element to the trace without repetitions
//			// NOT to the buffer trace!
//			add(bufferTrace.remove());
//		}
	}

	private void checkForRepetitionsFromPosition(int position) {
		int lengthToRemove = 0;
		// reuse existing iterators
		inputTraceIterator.setToPosition(0);
		resultTraceIterator.setToPosition(position);
		// count the number of elements that need to be removed later with count variable;
		// variable count should start at 1 to skip the very first (known to be equal) element
		// TODO: this could now be changed by removing the loop; 
		// we only check for one repetition at a time
		for (int count = 0; ; ++count) {
			if (!resultTraceIterator.hasNext()) {
				// at the end of the compressed sequence
				++repetitionCounter;
				// start over
				resultTraceIterator.setToPosition(position);
				// later remove the processed nodes that have been repeated
				lengthToRemove += count;
				repetitionLength = count;
				count = 0;
			}

			if (!inputTraceIterator.hasNext()) {
				// no further remaining sequence, but maybe repetition continues
				// in subsequently added elements...
				if (repetitionCounter > 0) {
					inRepetitionFromPosition = position;
				}
				break;
			}

			// check if elements are equal
			if (inputTraceIterator.next() != resultTraceIterator.next()) {
				// no repetition or end of repetition
				inRepetitionFromPosition = -1;
				break;
			}

			// continue with the next node of the remaining sequence
		}
		
		// may remove the repeated elements from the buffer
		bufferTrace.clear(lengthToRemove);
	}

//	private void processRemainingBufferTrace() {
//		while (!bufferTrace.isEmpty()) {
//			// process any lingering repetitions
//			addCurrentRepetitionMarker();
//			// reset the state and process any remaining element in the buffer
//			resetStateAndContinue();
//		}
//		
//		// finalize? TODO
//		elementToPositionMap.clear();
//		if (!traceRepetitions.isEmpty()) {
//			traceRepetitions.sleep();
//			// will be added elsewhere
////			addRepetitionMarkers(traceRepetitions, originalTraceSize);
//		}
//	}

	public BufferedLongArrayQueue getCompressedTrace() {
		return traceWithoutRepetitions;
	}

	public BufferedMap<int[]> getRepetitionMarkers() {
		return traceRepetitions;
	}
	
}
