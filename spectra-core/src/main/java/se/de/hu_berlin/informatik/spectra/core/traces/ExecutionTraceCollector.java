package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExecutionTraceCollector {
	
	private Map<String,int[]> rawTracePool = new HashMap<>();
	
	private GSTree gsTree = new GSTree();
	private Map<String,ExecutionTrace> executionTracePool = new HashMap<>();
	
	private GSTreeIndexer indexer = null;
	
	public boolean addRawTraceToPool(String testID, List<Integer> trace) {
		if (rawTracePool.get(testID) != null) {
			return false;
		}
		int[] traceArray = trace.stream().mapToInt(i->i).toArray();
		addTrace(testID, traceArray);
		return true;
	}
	
	public boolean addRawTraceToPool(String testID, int[] trace) {
		if (rawTracePool.get(testID) != null) {
			return false;
		}
		addTrace(testID, trace);
		return true;
	}

	private void addTrace(String testID, int[] trace) {
		// collect raw trace
		rawTracePool.put(testID, trace);
		// new input may or may not invalidate previously generated execution traces
		// (generally, the execution traces should only be generated at the end of trace collection)
		executionTracePool.clear();
		// we need to extract repetitions in the trace and add them to the GS tree
		extractRepetitions(trace);
	}
	
	public int[] getRawTrace(String testID) {
		return rawTracePool.get(testID);
	}
	
	private void extractRepetitions(int[] traceArray) {
		// mapping from starting elements to found repeated sequences
		Map<Integer,List<int[]>> elementToSequencesMap = new HashMap<>();
		Map<Integer,Integer> elementToPositionMap = new HashMap<>();
		int startingPosition = 0;
		for (int i = 0; i < traceArray.length; i++) {
			int element = traceArray[i];
			if (gsTree.checkIfStartingElementExists(element)) {
				// the element was already recognized as a starting element, previously
				if (startingPosition < i) {
					// there exists an unprocessed sequence 
					// before this element's position
					checkAndAddSequence(traceArray, elementToSequencesMap, startingPosition, i);
					
					// forget all previously remembered positions of elements
					elementToPositionMap.clear();
					// reset the starting position (all previous sequences have been processed)
					startingPosition = i;
				}
			} else {
				// check for repetitions
				Integer position = elementToPositionMap.get(element);
				if (position == null) {
					// no repetition: remember position of element
					elementToPositionMap.put(element, i);
				} else {
					// element was repeated
					if (startingPosition < position) {
						// there exists an unprocessed sequence 
						// before the element's first position
						checkAndAddSequence(traceArray, elementToSequencesMap, startingPosition, position);
					}
					// check the sequence from the element's first position to the element's second position
					checkAndAddSequence(traceArray, elementToSequencesMap, position, i);
					
					// forget all previously remembered positions of elements
					elementToPositionMap.clear();
					// remember position of element
					elementToPositionMap.put(element, i);
					// reset the starting position (all previous sequences have been processed)
					startingPosition = i;
				}
			}
		}
		
		// process remaining elements
		if (startingPosition < traceArray.length) {
			// there exists an unprocessed sequence 
			// before this element's position
			checkAndAddSequence(traceArray, elementToSequencesMap, startingPosition, traceArray.length);
			
			// forget all previously remembered positions of elements
			elementToPositionMap.clear();
		}
		
		// at this point, the lists of found sequences include sequences that begin with 
		// previously identified starting elements and sequences that begin with 
		// (usually repeated) elements that have not yet been identified as starting elements;
		// the sequences should NOT include elements in the middle of the sequence that were 
		// previously identified as starting elements (just for some efficiency)
		
		// add the sequences to the tree
		for (List<int[]> list : elementToSequencesMap.values()) {
			for (int[] sequence : list) {
				gsTree.addSequence(sequence);
			}
		}
	}

	private void checkAndAddSequence(int[] traceArray, 
			Map<Integer, List<int[]>> elementToSequencesMap,
			int startPosition, Integer endPosition) {
		int length = endPosition-startPosition;
		int[] sequence = new int[length];
		System.arraycopy(traceArray, startPosition, sequence, 0, length);
		List<int[]> foundSequences = elementToSequencesMap.computeIfAbsent(traceArray[startPosition],
				k -> { return new ArrayList<>(); });
		boolean foundIdentical = false;
		for (int[] foundSequence : foundSequences) {
			if (foundSequence.length == sequence.length) {
				boolean identical = true;
				for (int j = 0; j < foundSequence.length; j++) {
					if (foundSequence[j] != sequence[j]) {
						identical = false;
						break;
					}
				}
				if (identical) {
					foundIdentical = true;
					break;
				}
			}
		}
		if (!foundIdentical) {
			foundSequences.add(sequence);
		}
	}

	public ExecutionTrace getExecutionTrace(String testID) {
		int[] rawTrace = rawTracePool.get(testID);
		if (rawTrace == null) {
			return null;
		}
		ExecutionTrace executionTrace = executionTracePool.get(testID);
		if (executionTrace == null) {
			executionTrace = generateExecutiontraceFromRawTrace(rawTrace);
			executionTracePool.put(testID, executionTrace);
			// may still be null?
			return executionTrace;
		} else {
			return executionTrace;
		}
	}

	private ExecutionTrace generateExecutiontraceFromRawTrace(int[] rawTrace) {
		// generate execution trace from current GS tree
		int[] indexedTrace = generateIndexedTrace(rawTrace);
		return new ExecutionTrace(indexedTrace);
	}

	private int[] generateIndexedTrace(int[] rawTrace) {
		// replace sequences in the raw trace with indices
		if (indexer == null) {
			indexer = new GSTreeIndexer(gsTree);
		}
		
		return gsTree.generateIndexedTrace(rawTrace, indexer);
	}

	public GSTree getGsTree() {
		return gsTree;
	}

}
