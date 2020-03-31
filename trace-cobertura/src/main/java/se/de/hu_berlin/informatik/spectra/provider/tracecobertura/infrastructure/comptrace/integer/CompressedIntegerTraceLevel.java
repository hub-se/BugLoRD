package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedIntArrayQueue.MyBufferedIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedMap;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.RepetitionMarkerBufferedMap;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class CompressedIntegerTraceLevel implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4785567973646428932L;

    /**
     * We are able to process traces with a larger index, but still are
     * restricted in terms of repetition marker index, at the moment.
     */
    private static int MAX_INDEX = Integer.MAX_VALUE;
    MyBufferedIterator resultTraceIterator;

    //	BufferedIntArrayQueue bufferTrace;
    private BufferedIntArrayQueue traceWithoutRepetitions;
    BufferedMap<int[]> traceRepetitions;

    // should indicate up to which index the trace has been checked for repetitions
    private int bufferStartIndex = 0;
    MyBufferedIterator inputTraceIterator;
    // mapping from elements to their most recent positions in the result list
    Map<Integer, Integer> elementToPositionMap;
    // current position
    Integer position = null;
    int maxPosMapSize = 0;
    private long originalSize = 0;
    int maxBufferSize = 0;
    int maxTraceSize = 0;


    public CompressedIntegerTraceLevel(File outputDir, String prefix, int nodeSize, int mapSize, boolean deleteOnExit, boolean flat) {
        String uuid = UUID.randomUUID().toString();
        traceWithoutRepetitions = new BufferedIntArrayQueue(outputDir,
                prefix + "cpr_trace_lvl_" + uuid, nodeSize, deleteOnExit);

        // don't need all this in flat mode!
        if (!flat) {
            traceRepetitions = new RepetitionMarkerBufferedMap(outputDir,
                    prefix + "cpr_trace_rpt_" + uuid, mapSize, deleteOnExit);
            elementToPositionMap = new HashMap<>();
            resultTraceIterator = traceWithoutRepetitions.iterator();
            inputTraceIterator = traceWithoutRepetitions.iterator();
        }
    }

    public long size() {
        return originalSize;
    }

    public boolean isEmpty() {
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
			
	public boolean add(int element, boolean checkForRepetitions) {
		// there are certain circumstances, in which we don't want to check for 
		// repetitions in processed elements;
		// (mainly if we reach the maximum number of iterations/levels)
		if (!checkForRepetitions) {
            traceWithoutRepetitions.add(element);
            ++bufferStartIndex;
            ++originalSize;
            return false;
		}
		
		// different procedures based on the state...
		if (repetitionCheckMode) {
			// in repetition check mode/state, we build up a buffer trace instead of the real trace.
			// repeated parts are removed from the buffer trace on the fly
//			System.out.println("(check) buffer add: " + element + ", pos: " + bufferStartIndex + ", bs: " + currentBufferSize());
			traceWithoutRepetitions.add(element);
			maxBufferSize = Math.max(maxBufferSize, currentBufferSize());

			// ensure that we are either in a state with not enough information (buffer)
			// or in a state right after a repetition! (false or true, respectively);
			return addFromBuffer(false, false);
        } else {
            // no repetition check mode!
            // we will only get to here when the buffer is empty!
            // check for repetition of the current element
            position = elementToPositionMap.get(element);
            if (position == null) {
                // no repetition possible; remember node containing the element
                if (originalSize <= MAX_INDEX) {
                    elementToPositionMap.put(element, bufferStartIndex);
                    maxPosMapSize = Math.max(maxPosMapSize, elementToPositionMap.size());
//					System.out.println("norm add: " + element + ", pos: " + bufferStartIndex + ", bs: " + currentBufferSize());
                }
//				System.out.println("norm add: " + element + ", pos: " + bufferStartIndex + ", bs: " + currentBufferSize());
                // build up the result trace on the fly
                traceWithoutRepetitions.add(element);
                ++bufferStartIndex;
                maxTraceSize = Math.max(maxTraceSize, bufferStartIndex);
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
		if (bufferStartIndex >= traceWithoutRepetitions.size()) {
			return false;
		}
		
		// different procedures based on the state...
		if (repetitionCheckMode) {
			// in repetition check mode/state, we build up a buffer trace instead of the real trace.
			// repeated parts are removed from the buffer trace on the fly
//			System.out.println("check buf: " + traceWithoutRepetitions.get(bufferStartIndex) + ", pos: " + bufferStartIndex + ", bs: " + currentBufferSize());
			return checkForRepetitions(recheckBuffer, endOfLine);
        } else {
            // no repetition check mode!
            // check for repetition of the current element
            int firstBufferElement = traceWithoutRepetitions.get(bufferStartIndex);
            position = elementToPositionMap.get(firstBufferElement);
            if (position == null) {
                // no repetition possible; remember node containing the element
                if (originalSize <= MAX_INDEX) {
                    elementToPositionMap.put(firstBufferElement, bufferStartIndex);
                    maxPosMapSize = Math.max(maxPosMapSize, elementToPositionMap.size());
                }
//				System.out.println("norm buf: " + traceWithoutRepetitions.get(bufferStartIndex) + ", pos: " + bufferStartIndex + ", bs: " + currentBufferSize());
                // build up the result trace on the fly
                ++bufferStartIndex;
                maxTraceSize = Math.max(maxTraceSize, bufferStartIndex);
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

            // enough space for a repetition? (only check if there can be exactly one repetition);
            // also ensure that we can properly use reversed repetition markers (first term in the condition)!
            if (originalSize - 1 <= MAX_INDEX && currentBufferSize() >= bufferStartIndex - position) {
                // element was repeated
                // check if the sequence of elements between the last position of the element
                // and this position is the same as the following sequence(s) in the input trace

                // updates state variables and potentially removes
                // repeated elements from the start of the buffer trace
                checkForRepetitionsFromPosition(position);
//				System.out.println("1idx: " + (originalSize - repetitionLength) + ", len: " + repetitionLength + ", rpt: " + (repetitionCounter+1));
            }

            // are there any repetitions and are we not still inside of a repetition (potentially)?
            if ((endOfLine || inRepetitionFromPosition < 0) && repetitionCounter > 0) {
                // add a marker for the the current (finished) repetition
                addCurrentRepetitionMarker();
                resetStateAndContinue();
                // we can add all of the previously seen trace to the next compression level;
                // after we return true, we expect the supervising method to work through the remaining buffer
                // after feeding the finished part of the trace to the next level
                return true;
            } else if (endOfLine || (inRepetitionFromPosition < 0 && currentBufferSize() >= bufferStartIndex - position)) {
                // no repetition found with a sufficiently large buffer trace...
                // add the new position to the list of remembered positions
                if (originalSize <= MAX_INDEX) {
                    elementToPositionMap.put(traceWithoutRepetitions.get(bufferStartIndex), bufferStartIndex);
                    maxPosMapSize = Math.max(maxPosMapSize, elementToPositionMap.size());
                } else {
                    elementToPositionMap.remove(traceWithoutRepetitions.get(bufferStartIndex));
//					System.out.println("removed: " + traceWithoutRepetitions.get(bufferStartIndex));
                }
//				position = bufferStartIndex;
//				System.out.println("check sec add: " + traceWithoutRepetitions.get(bufferStartIndex) + ", pos: " + bufferStartIndex + ", bs: " + currentBufferSize());
                // build up the result trace on the fly
                ++bufferStartIndex;
                maxTraceSize = Math.max(maxTraceSize, bufferStartIndex);
                ++originalSize;

                resetStateAndContinue();
                // no repetition...
                return null;
			}
			
			// either in an ongoing repetition or not enough buffer
			return false;
		} else {
			// yes: we're in an ongoing repetition and need to check for following repetitions of that same sequence
			
			// enough space for (another) repetition?
			if (currentBufferSize() >= bufferStartIndex - inRepetitionFromPosition) {
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

	private int currentBufferSize() {
        long size = traceWithoutRepetitions.size() - bufferStartIndex;
        if (size > Integer.MAX_VALUE) {
            throw new IllegalStateException("Buffer size too large: " + size);
        }
        return (int) size;
	}

	private void addCurrentRepetitionMarker() {
		if (repetitionCounter > 0) {
            // add a marker to the list: (key: start index, value: { length, #repetitions })
            long index = originalSize - repetitionLength;
            if (index > Integer.MAX_VALUE) {
                throw new IllegalStateException("Repetition index too large: " + index);
            }
            traceRepetitions.put((int) index, new int[]{repetitionLength, repetitionCounter + 1});
//			System.out.println(this + "new idx: " + (originalSize - repetitionLength) + ", len: " + repetitionLength + ", rpt: " + (repetitionCounter+1));

            // reset repetition recognition
            elementToPositionMap.clear();

            // may feed the already processed part of the trace to the next compression level
		}
	}

	private void resetStateAndContinue() {
		resetState();
	}

	private void checkForRepetitionsFromPosition(int position) {
		int lengthToRemove = 0;
		// reuse existing iterators
		inputTraceIterator.setToPosition(bufferStartIndex);
		resultTraceIterator.setToPosition(position);
//		System.out.println(traceWithoutRepetitions.size() + ": " + position + " -> " + bufferStartIndex);
//		MyBufferedIntIterator iterator2 = traceWithoutRepetitions.iterator(0);
//		while (iterator2.hasNext()) {
//			System.out.print(iterator2.next() + ",");
//		}
//		System.out.println();
//		MyBufferedIntIterator iterator3 = traceWithoutRepetitions.iterator(bufferStartIndex);
//		while (iterator3.hasNext()) {
//			System.out.print(iterator3.next() + ",");
//		}
//		System.out.println();
		// count the number of elements that need to be removed later with count variable;
		// variable count should start at 1 to skip the very first (known to be equal) element
		int sequenceLength = bufferStartIndex - position;
		for (int count = 0; ; ++count) {
			if (count == sequenceLength) {
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
		traceWithoutRepetitions.clearFrom(bufferStartIndex, lengthToRemove);
	}
	
	public int getNextCheckedElement() {
		if (bufferStartIndex > 0) {
			--bufferStartIndex;
			return traceWithoutRepetitions.remove();
		} else {
			throw new IllegalStateException("Buffer start index is too low: " + bufferStartIndex);
		}
	}
	
	public boolean hasCheckedElements() {
		return bufferStartIndex > 0;
	}

	public BufferedIntArrayQueue getCompressedTrace() {
		return traceWithoutRepetitions;
	}

	public BufferedMap<int[]> getRepetitionMarkers() {
		return traceRepetitions;
	}
	
}
