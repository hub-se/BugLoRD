package se.de.hu_berlin.informatik.spectra.core.traces;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimerTask;

import se.de.hu_berlin.informatik.utils.compression.single.CompressedByteArrayToIntArrayProcessor;
import se.de.hu_berlin.informatik.utils.compression.single.IntArrayToCompressedByteArrayProcessor;
import se.de.hu_berlin.informatik.utils.compression.ziputils.AddNamedByteArrayToZipFileProcessor;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileReader;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileWrapper;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;
import se.de.hu_berlin.informatik.utils.processors.sockets.module.Module;

public class RawTraceCollector {
	
	private static final String RAW_TRACE_FILE_EXTENSION = ".raw";

	private Map<Integer,List<int[]>> rawTracePool;
	
	private Path output;
	private Module<Pair<String, byte[]>, byte[]> zipModule;
	
	private GSTree gsTree = new GSTree();
	private Map<Integer,List<ExecutionTrace>> executionTracePool = new HashMap<>();
	
	private SequenceIndexer indexer = null;
	
	
//	public RawTraceCollector() {
//		this(null);
//	}
	
	public RawTraceCollector(Path outputDir) {
		Objects.requireNonNull(outputDir, "Path to store raw traces must not be null!");
//		if (outputDir == null) {
//			rawTracePool = new HashMap<>();
//		} else {
		outputDir.toFile().mkdirs();
		this.output = outputDir.resolve("rawTraces.zip");
		zipModule = new AddNamedByteArrayToZipFileProcessor(this.output, true).asModule();
//		}
			
		Runtime.getRuntime().addShutdownHook(new Thread(new RemoveOutput(this.output)));
	}
	
	public boolean addRawTraceToPool(int traceIndex, int threadId, List<Integer> trace) {
//		if (rawTracePool.get(testID) != null) {
//			return false;
//		}
		int[] traceArray = trace.stream().mapToInt(i->i).toArray();
		addTrace(traceIndex, threadId, traceArray);
		return true;
	}
	
	public boolean addRawTraceToPool(int traceIndex, int threadId, int[] trace) {
//		if (rawTracePool.get(testID) != null) {
//			return false;
//		}
		addTrace(traceIndex, threadId, trace);
		return true;
	}

	private void addTrace(int traceIndex, int threadId, int[] trace) {
		// collect raw trace
		if (output == null) {
			List<int[]> list = rawTracePool.computeIfAbsent(traceIndex, k -> { return new ArrayList<>(1); });
			list.add(trace);
		} else {
			// avoid storing raw traces in memory...
			IntArrayToCompressedByteArrayProcessor module = new IntArrayToCompressedByteArrayProcessor();
			// store the raw trace
			byte[] involvement = module.submit(trace).getResult();

			// store each trace separately
			zipModule.submit(new Pair<>(traceIndex + "-" + threadId + RAW_TRACE_FILE_EXTENSION, involvement));
		}
		
		// new input may or may not invalidate previously generated execution traces
		// (generally, the execution traces should only be generated at the end of trace collection)
//		executionTracePool.clear();
		// we need to extract repetitions in the trace and add them to the GS tree
		extractRepetitions(trace);
	}
	
	public List<int[]> getRawTraces(int traceIndex) {
		if (output == null) {
			return rawTracePool.get(traceIndex);
		} else {
			if (!output.toFile().exists()) {
				return null;
			}
			// retrieve the raw traces from the zip file
			List<int[]> result = new ArrayList<>(1);
			CompressedByteArrayToIntArrayProcessor traceProcessor = new CompressedByteArrayToIntArrayProcessor();
			ZipFileWrapper zip = new ZipFileReader().submit(output).getResult();
			byte[] traceInvolvement;
			int traceCounter = -1;
			// assume IDs to start at 0
			while ((traceInvolvement = zip.get(traceIndex + "-" + (++traceCounter) + RAW_TRACE_FILE_EXTENSION, false)) != null) {
				result.add(traceProcessor.submit(traceInvolvement).getResult());
			}
			return result.isEmpty() ? null : result;
		}
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

	public List<ExecutionTrace> getExecutionTraces(int traceIndex) {
		List<int[]> rawTraces = getRawTraces(traceIndex);
		if (rawTraces == null) {
			return null;
		}
		List<ExecutionTrace> executionTraces = executionTracePool.get(traceIndex);
		if (executionTraces == null) {
			executionTraces = generateExecutiontraceFromRawTraces(rawTraces);
			executionTracePool.put(traceIndex, executionTraces);
			// may still be null?
			return executionTraces;
		} else {
			return executionTraces;
		}
	}

	private List<ExecutionTrace> generateExecutiontraceFromRawTraces(List<int[]> rawTraces) {
		// generate execution trace from current GS tree
		if (indexer == null) {
			indexer = new GSTreeIndexer(gsTree);
		}

		// replace sequences in the raw trace with indices
		List<ExecutionTrace> traces = new ArrayList<>(rawTraces.size());
		for (int[] rawTrace : rawTraces) {
			traces.add(new ExecutionTrace(gsTree.generateIndexedTrace(rawTrace, indexer)));
		}
		
		return traces;
	}

	public GSTree getGsTree() {
		return gsTree;
	}

	public SequenceIndexer getIndexer() {
		if (indexer == null) {
			indexer = new GSTreeIndexer(gsTree);
		}
		return indexer;
	}

	@Override
	public void finalize() throws Throwable {
		if (output != null) {
			FileUtils.delete(output);
		}
		super.finalize();
	}
	
	private class RemoveOutput extends TimerTask {
		
		private Path output;

		public RemoveOutput(Path output) {
			this.output = output;
		}

		public void run() {
			if (this.output != null) {
				FileUtils.delete(this.output);
			}
		}
	}
	
}
