package se.de.hu_berlin.informatik.spectra.core.traces;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CompressedTraceBase;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.compression.ziputils.AddNamedByteArrayToZipFileProcessor;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileReader;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileWrapper;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;
import se.de.hu_berlin.informatik.utils.miscellaneous.SingleLinkedQueue;
import se.de.hu_berlin.informatik.utils.processors.sockets.module.Module;

public class RawTraceCollector {
	
	private static final String RAW_TRACE_FILE_EXTENSION = ".raw";

	private Map<Integer,List<CompressedTraceBase<Integer,?>>> rawTracePool;
	
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
		
		this.output.toFile().deleteOnExit();
//		Runtime.getRuntime().addShutdownHook(new Thread(new RemoveOutput(this.output)));
	}
	
	public boolean addRawTraceToPool(int traceIndex, int threadId, List<Integer> trace, boolean log) {
//		if (rawTracePool.get(testID) != null) {
//			return false;
//		}
		addTrace(traceIndex, threadId, trace, log);
		return true;
	}
	
	// only used for testing purposes
	public boolean addRawTraceToPool(int traceIndex, int threadId, int[] traceArray, boolean log) {
		List<Integer> trace = new ArrayList<>(traceArray.length);
		for (int i = 0; i < traceArray.length; i++) {
			if (traceArray[i] >= 0) {
				trace.add(traceArray[i]);
			}
		}
		return addRawTraceToPool(traceIndex, threadId, trace, log);
	}

	private void addTrace(int traceIndex, int threadId, List<Integer> trace, boolean log) {
		addTrace(traceIndex, threadId, new ExecutionTrace(trace, log));
	}
	
	public List<CompressedTraceBase<Integer,?>> getRawTraces(int traceIndex) {
		if (output == null) {
			return rawTracePool.get(traceIndex);
		} else {
			if (!output.toFile().exists()) {
				return null;
			}
			// retrieve the raw traces from the zip file
			List<CompressedTraceBase<Integer,?>> result = new ArrayList<>(1);
			ZipFileWrapper zip = new ZipFileReader().submit(output).getResult();
			byte[] traceInvolvement;
			int traceCounter = -1;
			// assume IDs to start at 0
			while ((traceInvolvement = zip.get(traceIndex + "-" + (++traceCounter) + RAW_TRACE_FILE_EXTENSION, false)) != null) {
				ExecutionTrace executionTrace = SpectraFileUtils.loadExecutionTraceFromByteArray(traceInvolvement);
				result.add(executionTrace);
			}
			return result.isEmpty() ? null : result;
		}
	}
	
	private void extractRepetitions(Iterator<Integer> traceIterator) {
		// mapping from starting elements to found repeated sequences
		Map<Integer,List<int[]>> elementToSequencesMap = new HashMap<>();
		Map<Integer,Integer> elementToPositionMap = new HashMap<>();
		int startingPosition = 0;
		int i = 0;
		SingleLinkedQueue<Integer> unprocessedSequence = new SingleLinkedQueue<>();
		for (Iterator<Integer> iterator = traceIterator; iterator.hasNext(); ++i) {
			int element = traceIterator.next();
			if (element < 0) {
				// check if the element is a correct id, which has to be positive (atm) TODO
				// this may mark elements which shall be ignored/skipped when executing exactly this method...
				continue;
			}
			if (gsTree.checkIfStartingElementExists(element)) {
				// the element was already recognized as a starting element, previously
				if (startingPosition < i) {
					// there exists an unprocessed sequence 
					// before this element's position
					checkAndAddSequence(unprocessedSequence, elementToSequencesMap, i - startingPosition);
					
					unprocessedSequence.clear();
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
						checkAndAddSequence(unprocessedSequence, elementToSequencesMap, position - startingPosition);
					}
					// check the sequence from the element's first position to the element's second position
					checkAndAddSequence(unprocessedSequence, elementToSequencesMap, i - position);
					
					unprocessedSequence.clear();
					// forget all previously remembered positions of elements
					elementToPositionMap.clear();
					// remember position of element
					elementToPositionMap.put(element, i);
					// reset the starting position (all previous sequences have been processed)
					startingPosition = i;
				}
			}
			
			// add the current element to the list of unprocessed elements
			unprocessedSequence.add(element);
		}
		
		// process remaining elements
		if (!unprocessedSequence.isEmpty()) {
			// there exists an unprocessed sequence 
			// before this element's position
			checkAndAddSequence(unprocessedSequence, elementToSequencesMap, unprocessedSequence.size());
			
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

	private void checkAndAddSequence(SingleLinkedQueue<Integer> traceArray, 
			Map<Integer, List<int[]>> elementToSequencesMap, int length) {
		
		List<int[]> foundSequences = elementToSequencesMap.computeIfAbsent(traceArray.peek(),
				k -> { return new ArrayList<>(); });
		boolean foundIdentical = false;
		for (int[] foundSequence : foundSequences) {
			if (foundSequence.length == length) {
				boolean identical = true;
				Iterator<Integer> iterator = traceArray.iterator();
				for (int j = 0; j < foundSequence.length; j++) {
					if (foundSequence[j] != iterator.next()) {
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
			int[] sequence = new int[length];
//			System.arraycopy(traceArray, startPosition, sequence, 0, length);
			for (int i = 0; i < length; i++) {
				sequence[i] = traceArray.remove();
			}
			foundSequences.add(sequence);
		} else {
			traceArray.clear(length);
		}
	}

	public List<ExecutionTrace> getExecutionTraces(int traceIndex, boolean log) {
		List<CompressedTraceBase<Integer, ?>> rawTraces = getRawTraces(traceIndex);
		if (rawTraces == null) {
			return null;
		}
		List<ExecutionTrace> executionTraces = executionTracePool.get(traceIndex);
		if (executionTraces == null) {
			executionTraces = generateExecutiontraceFromRawTraces(rawTraces, log);
			executionTracePool.put(traceIndex, executionTraces);
			// may still be null?
			return executionTraces;
		} else {
			return executionTraces;
		}
	}

	private List<ExecutionTrace> generateExecutiontraceFromRawTraces(List<CompressedTraceBase<Integer, ?>> rawTraces, boolean log) {
		// generate execution trace from current GS tree
		if (indexer == null) {
			indexer = new GSTreeIndexer(gsTree);
		}

		// replace sequences in the raw trace with indices
		List<ExecutionTrace> traces = new ArrayList<>(rawTraces.size());
		for (CompressedTraceBase<Integer, ?> rawTrace : rawTraces) {
			traces.add(new ExecutionTrace(gsTree.generateIndexedTrace(rawTrace, indexer), log));
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

	public boolean addRawTraceToPool(int traceIndex, int threadId, ExecutionTrace eTrace) {
		addTrace(traceIndex, threadId, eTrace);
		return true;
	}

	private void addTrace(int traceIndex, int threadId, CompressedTraceBase<Integer,?> eTrace) {
		// collect raw trace
		if (output == null) {
			List<CompressedTraceBase<Integer,?>> list = rawTracePool.computeIfAbsent(traceIndex, k -> { return new ArrayList<>(1); });
			list.add(eTrace);
		} else {
			// avoid storing traces in memory...
			// store the raw trace
			byte[] involvement = SpectraFileUtils.storeAsByteArray(eTrace);

			// store each trace separately
			zipModule.submit(new Pair<>(traceIndex + "-" + threadId + RAW_TRACE_FILE_EXTENSION, involvement));
			involvement = null;
		}
		
		// new input may or may not invalidate previously generated execution traces
		// (generally, the execution traces should only be generated at the end of trace collection)
//		executionTracePool.clear();
		// we need to extract repetitions in the trace and add them to the GS tree
		extractRepetitions(eTrace.iterator());
	}

	
//	private class RemoveOutput extends TimerTask {
//		
//		private Path output;
//
//		public RemoveOutput(Path output) {
//			this.output = output;
//		}
//
//		public void run() {
//			if (this.output != null) {
//				FileUtils.delete(this.output);
//			}
//		}
//	}
	
}
