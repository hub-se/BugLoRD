package se.de.hu_berlin.informatik.spectra.core.traces;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import net.lingala.zip4j.model.FileHeader;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CompressedTraceBase;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedBufferedArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CloneableIterator;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.compression.ziputils.AddNamedByteArrayToZipFileProcessor;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileReader;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileWrapper;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.tracking.ProgressTracker;

public class RawTraceCollector {
	
	private static final String RAW_TRACE_FILE_EXTENSION = ".raw";
	private static final String EXEC_TRACE_FILE_EXTENSION = ".exec";
	private static final String RAW_TRACE_REP_FILE_EXTENSION = ".rraw";
	private static final String EXEC_TRACE_REP_FILE_EXTENSION = ".rexec";
	
	private Map<Integer,List<CompressedTraceBase<Integer,?>>> rawTracePool;
	
	private Path output;

	private GSTree gsTree = new GSTree();
	private Map<Integer,List<ExecutionTrace>> executionTracePool = new HashMap<>();
	
	private SequenceIndexer indexer = null;
	
	private Set<Integer> startElements = new HashSet<>();
	
	
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
		new AddNamedByteArrayToZipFileProcessor(this.output, true).asModule();
//		}
		
		this.output.toFile().deleteOnExit();
//		Runtime.getRuntime().addShutdownHook(new Thread(new RemoveOutput(this.output)));
	}
	
	public boolean addRawTraceToPool(int traceIndex, int threadId, SingleLinkedBufferedArrayQueue<Integer> trace, boolean log) {
//		if (rawTracePool.get(testID) != null) {
//			return false;
//		}
		addTrace(traceIndex, threadId, trace, log);
		return true;
	}
	
	// only used for testing purposes
		public boolean addRawTraceToPool(int traceIndex, int threadId, int[] traceArray, boolean log, Path outputDir, String prefix) {
			SingleLinkedBufferedArrayQueue<Integer> trace = new SingleLinkedBufferedArrayQueue<Integer>(outputDir.toFile(), prefix, 100);
			for (int i = 0; i < traceArray.length; i++) {
				trace.add(traceArray[i]);
			}
//			trace.clear(1);
//			for (Iterator<Integer> iterator = trace.iterator(); iterator.hasNext();) {
//				Integer integer = iterator.next();
//				System.out.print(integer + ", ");
//			}
//			System.out.println();
			return addRawTraceToPool(traceIndex, threadId, trace, log);
		}

	private void addTrace(int traceIndex, int threadId, SingleLinkedBufferedArrayQueue<Integer> trace, boolean log) {
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
			int traceCounter = -1;
			// assume IDs to start at 0
			while (zip.exists(traceIndex + "-" + (++traceCounter) + RAW_TRACE_FILE_EXTENSION)) {
				// this is false
				ExecutionTrace executionTrace = SpectraFileUtils.loadExecutionTraceFromZipFile(zip, 
						traceIndex + "-" + (traceCounter) + RAW_TRACE_FILE_EXTENSION, 
						traceIndex + "-" + (traceCounter) + RAW_TRACE_REP_FILE_EXTENSION);
				result.add(executionTrace);
			}
			return result.isEmpty() ? null : result;
		}
	}
	
//	private void extractRepetitions(CloneableIterator<Integer> traceIterator) {
//		Map<Integer,Integer> elementToPositionMap = new HashMap<>();
//
//		// remember starting position
//		CloneableIterator<Integer> unprocessedIterator = traceIterator.clone();
//		int processedElements = 0;
//		while (traceIterator.hasNext()) {
//			int element = traceIterator.peek();
////			System.out.println("next: " + element);
//			// TODO prevent negative elements!
//			if (element < 0) {
//				// check if the element is a correct id, which has to be positive (atm) TODO
//				// this may mark elements which shall be ignored/skipped when executing exactly this method...
//				throw new IllegalStateException("Tried to add an negative item: " + element);
//			}
//			if (gsTree.checkIfStartingElementExists(element)) {
//				// the element was already recognized as a starting element, previously
//				if (processedElements > 0) {
//					// there exists an unprocessed sequence 
//					// before this element's position
//					gsTree.addSequence(unprocessedIterator, processedElements);
//	
//					unprocessedIterator = traceIterator.clone();
//					// forget all previously remembered positions of elements
//					elementToPositionMap.clear();
//					processedElements = 0;
//					// remember position of element
//					elementToPositionMap.put(element, 0);
//				}
//			} else {
//				// check for repetitions
//				Integer position = elementToPositionMap.get(element);
//				if (position == null) {
//					// no repetition: remember position of element 
//					//(will be the next element to be added to the unprocessed sequence)
//					elementToPositionMap.put(element, processedElements);
//				} else {
//					// element was repeated
//					if (position > 0) {
//						// there exists an unprocessed sequence 
//						// before the element's first position
//						gsTree.addSequence(unprocessedIterator, position);
//					}
//					// add the sequence from the element's first position to the element's second position
//					gsTree.__addSequence(unprocessedIterator, processedElements - position, element);
//					
//					unprocessedIterator = traceIterator.clone();
//					// forget all previously remembered positions of elements
//					elementToPositionMap.clear();
//					processedElements = 0;
//					// remember position of element
//					elementToPositionMap.put(element, 0);
//				}
//			}
//			
//			// add the current element to the list of unprocessed elements
//			++processedElements;
//			traceIterator.next();
//		}
//		
//		// process remaining elements
//		if (processedElements > 0) {
//			// there exists an unprocessed sequence 
//			// before this element's position
//			gsTree.addSequence(unprocessedIterator, processedElements);
//			
//			// forget all previously remembered positions of elements
//			elementToPositionMap.clear();
//		}
//		
////		// at this point, the lists of found sequences include sequences that begin with 
////		// previously identified starting elements and sequences that begin with 
////		// (usually repeated) elements that have not yet been identified as starting elements;
////		// the sequences should NOT include elements in the middle of the sequence that were 
////		// previously identified as starting elements (just for some efficiency)
////		
////		// add the sequences to the tree
////		for (List<int[]> list : elementToSequencesMap.values()) {
////			for (int[] sequence : list) {
////				gsTree.addSequence(sequence);
////			}
////		}
//	}

//	private void checkAndAddSequence(CloneableIterator<Integer> unprocessedIterator, int length) {
//		
//		gsTree.addSequence(unprocessedIterator, length);
//		
////		List<int[]> foundSequences = elementToSequencesMap.computeIfAbsent(traceArray.peek(),
////				k -> { return new ArrayList<>(); });
////		boolean foundIdentical = false;
////		for (int[] foundSequence : foundSequences) {
////			if (foundSequence.length == length) {
////				boolean identical = true;
////				Iterator<Integer> iterator = traceArray.iterator();
////				for (int j = 0; j < foundSequence.length; j++) {
////					if (foundSequence[j] != iterator.next()) {
////						identical = false;
////						break;
////					}
////				}
////				if (identical) {
////					foundIdentical = true;
////					break;
////				}
////			}
////		}
////		
////		if (!foundIdentical) {
////			int[] sequence = new int[length];
//////			System.arraycopy(traceArray, startPosition, sequence, 0, length);
////			for (int i = 0; i < length; i++) {
////				sequence[i] = traceArray.remove();
////			}
////			foundSequences.add(sequence);
////		} else {
////			traceArray.clear(length);
////		}
//	}

	public List<ExecutionTrace> getExecutionTraces(int traceIndex, boolean log) {
		// check if a stored execution trace exists
		if (output != null) {
			if (!output.toFile().exists()) {
				return null;
			}
			// try to retrieve the execution traces from the zip file
			List<ExecutionTrace> result = new ArrayList<>(1);
			ZipFileWrapper zip = new ZipFileReader().submit(output).getResult();
			int traceCounter = -1;
			// assume IDs to start at 0
			while (zip.exists(traceIndex + "-" + (++traceCounter) + EXEC_TRACE_FILE_EXTENSION)) {
				// this is false
				ExecutionTrace executionTrace = SpectraFileUtils.loadExecutionTraceFromZipFile(zip, 
						traceIndex + "-" + (traceCounter) + EXEC_TRACE_FILE_EXTENSION, 
						traceIndex + "-" + (traceCounter) + EXEC_TRACE_REP_FILE_EXTENSION);
				result.add(executionTrace);
			}
			if (!result.isEmpty()) {
				return result;
			}
			
			// generate execution trace from current GS tree
			if (indexer == null) {
				extractCommonSequencesFromRawTraces();
				indexer = new GSTreeIndexer(gsTree);
			}
			
			// if at this point, try generating the execution traces from raw traces on the fly
			List<CompressedTraceBase<Integer, ?>> rawTraces = getRawTraces(traceIndex);
			if (rawTraces == null) {
				return null;
			}
			List<ExecutionTrace> executionTraces = generateExecutiontraceFromRawTraces(rawTraces, log);
			
			// collect execution traces
			traceCounter = -1;
			for (ExecutionTrace executionTrace : executionTraces) {
				// avoid storing traces in memory...
				// store the execution trace
				try {
					SpectraFileUtils.storeExecutionTrace(executionTrace, output, 
							traceIndex + "-" + (++traceCounter) + EXEC_TRACE_FILE_EXTENSION, 
							traceIndex + "-" + (traceCounter) + EXEC_TRACE_FILE_EXTENSION);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// may still be null?
			return executionTraces;

		} else {
			List<ExecutionTrace> executionTraces = executionTracePool.get(traceIndex);
			if (executionTraces == null) {
				// generate execution trace from current GS tree
				if (indexer == null) {
					extractCommonSequencesFromRawTraces();
					indexer = new GSTreeIndexer(gsTree);
				}

				List<CompressedTraceBase<Integer, ?>> rawTraces = getRawTraces(traceIndex);
				if (rawTraces == null) {
					return null;
				}
				// remove the raw traces from the pool, as they should not be necessary any more
				rawTracePool.remove(traceIndex);
				executionTraces = generateExecutiontraceFromRawTraces(rawTraces, log);
				executionTracePool.put(traceIndex, executionTraces);
				// may still be null?
				return executionTraces;
			} else {
				return executionTraces;
			}
		}
	}

	private List<ExecutionTrace> generateExecutiontraceFromRawTraces(List<CompressedTraceBase<Integer, ?>> rawTraces, boolean log) {
		// replace sequences in the raw trace with indices
		List<ExecutionTrace> traces = new ArrayList<>(rawTraces.size());
		for (CompressedTraceBase<Integer, ?> rawTrace : rawTraces) {
			traces.add(new ExecutionTrace(gsTree.generateIndexedTrace(rawTrace, indexer), log));
		}
		
		return traces;
	}

	private void extractCommonSequencesFromRawTraces() {
		ProgressTracker tracker = new ProgressTracker(false);
		
		if (output == null) {
			for (Entry<Integer, List<CompressedTraceBase<Integer, ?>>> entry : rawTracePool.entrySet()) {
				tracker.track("processing trace " + entry.getKey());
				for (CompressedTraceBase<Integer, ?> trace : entry.getValue()) {
					extractCommonSequencesFromRawTrace(trace.iterator());
				}
			}
		} else {
			if (!output.toFile().exists()) {
				return;
			}
			try {
				// retrieve the raw traces from the zip file
				ZipFileWrapper zip = new ZipFileReader().submit(output).getResult();
				List<FileHeader> rawTraceFiles = zip.getFileHeadersContainingString(RAW_TRACE_FILE_EXTENSION);
				for (FileHeader fileHeader : rawTraceFiles) {
					tracker.track("processing " + fileHeader.getFileName());
					ExecutionTrace executionTrace = SpectraFileUtils.loadExecutionTraceFromZipFile(zip, 
							fileHeader.getFileName(), 
							fileHeader.getFileName().replace(RAW_TRACE_FILE_EXTENSION, RAW_TRACE_REP_FILE_EXTENSION));

//					extractCommonSequencesFromRawTrace(executionTrace.iterator());
					// it should suffice to only iterate over the compressed traces...
					extractCommonSequencesFromRawTrace(executionTrace.getCompressedTrace().iterator());
					executionTrace = null;
				}
			} catch (Exception e) {
				Log.err(this, e, "Error reading or processing raw traces from zip file.");
			}
		}
	}

	private void extractCommonSequencesFromRawTrace(CloneableIterator<Integer> traceIterator) {
		// remember starting position
		CloneableIterator<Integer> unprocessedIterator = traceIterator.clone();
		int processedElements = 0;
		while (traceIterator.hasNext()) {
			int element = traceIterator.peek();
//			System.out.println("next: " + element);
			// TODO prevent negative elements!
			if (element < 0) {
				// check if the element is a correct id, which has to be positive (atm) TODO
				// this may mark elements which shall be ignored/skipped when executing exactly this method...
				throw new IllegalStateException("Tried to add an negative item: " + element);
			}
			
			if (startElements.contains(element)) {
				// the element was recognized as a starting element, previously
				if (processedElements > 0) {
					// there exists an unprocessed sequence 
					// before this element's position
					gsTree.__addSequence(unprocessedIterator, processedElements, unprocessedIterator.peek());
	
					unprocessedIterator = traceIterator.clone();
					processedElements = 0;
				}
			}
			
			// add the current element to the list of unprocessed elements
			++processedElements;
			traceIterator.next();
		}
		
		// process remaining elements
		if (processedElements > 0) {
			// there exists an unprocessed sequence
			gsTree.__addSequence(unprocessedIterator, processedElements, unprocessedIterator.peek());
		}
	}

	public GSTree getGsTree() {
		return gsTree;
	}

	public SequenceIndexer getIndexer() {
		if (indexer == null) {
			extractCommonSequencesFromRawTraces();
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

	private void addTrace(int traceIndex, int threadId, CompressedTraceBase<Integer,Integer> eTrace) {
		// collect raw trace
		if (output == null) {
			List<CompressedTraceBase<Integer,?>> list = rawTracePool.computeIfAbsent(traceIndex, k -> { return new ArrayList<>(1); });
			list.add(eTrace);
		} else {
			// avoid storing traces in memory...
			// store the raw trace
			try {
				SpectraFileUtils.storeExecutionTrace(eTrace, output, 
						traceIndex + "-" + threadId + RAW_TRACE_FILE_EXTENSION, 
						traceIndex + "-" + threadId + RAW_TRACE_REP_FILE_EXTENSION);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// new input may or may not invalidate previously generated execution traces
		// (generally, the execution traces should only be generated at the end of trace collection)
//		executionTracePool.clear();
		// we need to extract repetitions in the trace and add them to the GS tree
		// (2 repetitions should be enough for each repeated sequence) TODO
//		extractRepetitions(eTrace.iterator());
		
		// only extract all elements that mark the beginning of sequences;
		// adding the traces to the GS tree has to be done in a separate, last step
		eTrace.addStartingElementsToSet(startElements);
		eTrace = null;
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
