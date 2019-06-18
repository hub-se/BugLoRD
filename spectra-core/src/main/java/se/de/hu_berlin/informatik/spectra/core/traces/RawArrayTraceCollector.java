package se.de.hu_berlin.informatik.spectra.core.traces;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;

import net.lingala.zip4j.model.FileHeader;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CloneableIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CompressedTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CompressedTraceBase;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.IntArrayWrapper;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.compression.ziputils.AddNamedByteArrayToZipFileProcessor;
import se.de.hu_berlin.informatik.utils.compression.ziputils.MoveNamedByteArraysBetweenZipFilesProcessor;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileReader;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileWrapper;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;
import se.de.hu_berlin.informatik.utils.processors.sockets.module.Module;
import se.de.hu_berlin.informatik.utils.tracking.ProgressTracker;

public class RawArrayTraceCollector {
	
	private static final String RAW_TRACE_FILE_EXTENSION = ".raw";
	private static final String REP_MARKER_FILE_EXTENSION = ".rep";
//	private static final String EXEC_TRACE_FILE_EXTENSION = ".exec";

	private Map<Integer,List<CompressedTraceBase<int[],?>>> rawTracePool;
	
	private final Path output;

	private final GSArrayTree<int[],IntArrayWrapper> gsTree = new GSIntArrayTree();
	private final Map<Integer,List<ExecutionTrace>> executionTracePool = new HashMap<>();
	
	private ArraySequenceIndexer<int[],IntArrayWrapper> indexer = null;
	
	private final Set<IntArrayWrapper> startElements = new HashSet<>();
	
	
//	public RawTraceCollector() {
//		this(null);
//	}
	
	public RawArrayTraceCollector(Path outputDir) {
		Objects.requireNonNull(outputDir, "Path to store raw traces must not be null!");
		outputDir.toFile().mkdirs();
		this.output = outputDir.resolve("rawTraces.zip");
		new AddNamedByteArrayToZipFileProcessor(this.output, true).asModule();
		
		this.output.toFile().deleteOnExit();
//		Runtime.getRuntime().addShutdownHook(new Thread(new RemoveOutput(this.output)));
	}
	
	public boolean addRawTraceToPool(int traceIndex, int threadId, BufferedArrayQueue<int[]> trace, boolean log) {
		addTrace(traceIndex, threadId, trace, log);
		return true;
	}
	
	// only used for testing purposes
	public boolean addRawTraceToPool(int traceIndex, int threadId, int[][] traceArray, boolean log, Path outputDir, String prefix) {
		BufferedArrayQueue<int[]> trace = new BufferedArrayQueue<>(outputDir.toFile(), prefix, 100);
        trace.addAll(Arrays.asList(traceArray));
//		trace.clear(1);
//		for (Iterator<Integer> iterator = trace.iterator(); iterator.hasNext();) {
//			Integer integer = iterator.next();
//			System.out.print(integer + ", ");
//		}
//		System.out.println();
		return addRawTraceToPool(traceIndex, threadId, trace, log);
	}

	private void addTrace(int traceIndex, int threadId, BufferedArrayQueue<int[]> trace, boolean log) {
		addTrace(traceIndex, threadId, new CompressedTrace(trace, log));
	}
	
	public boolean addRawTraceToPool(int traceIndex, int threadId, CompressedTraceBase<int[],IntArrayWrapper> eTrace) {
		addTrace(traceIndex, threadId, eTrace);
		return true;
	}

	private void addTrace(int traceIndex, int threadId, CompressedTraceBase<int[],IntArrayWrapper> eTrace) {
		// collect raw trace
		if (output == null) {
			List<CompressedTraceBase<int[], ?>> list = rawTracePool.computeIfAbsent(traceIndex, k -> new ArrayList<>(1));
			list.add(eTrace);
		} else {
			String traceFileName = traceIndex + "-" + threadId + RAW_TRACE_FILE_EXTENSION;
			String repMarkerFileName = traceIndex + "-" + threadId + REP_MARKER_FILE_EXTENSION;
			// avoid storing traces in memory...
			// store the compressed trace
			try {
				SpectraFileUtils.storeCompressedArrayTraceForRawTraceInZipFile(eTrace, output, traceFileName, repMarkerFileName);
			} catch (IOException e) {
				Log.abort(this, e, "Could not store raw trace.");
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
		eTrace.clear();
		eTrace = null;
	}
	
	public List<CompressedTraceBase<int[],?>> getRawTraces(int traceIndex) {
		if (output == null) {
			return rawTracePool.get(traceIndex);
		} else {
			if (!output.toFile().exists()) {
				return null;
			}
			// retrieve the raw traces from the zip file
			List<CompressedTraceBase<int[],?>> result = new ArrayList<>(1);
			ZipFileWrapper zip = new ZipFileReader().submit(output).getResult();

			int traceCounter = -1;
			// assume IDs to start at 0
			while (true) {
				String compressedTraceFile = traceIndex + "-" + (++traceCounter) + RAW_TRACE_FILE_EXTENSION;
				if (zip.exists(compressedTraceFile)) {
					String repetitionFile = traceIndex + "-" + (traceCounter) + REP_MARKER_FILE_EXTENSION;
					CompressedTraceBase<int[],?> rawTrace = SpectraFileUtils
							.loadRawArrayTraceFromZipFile(zip, compressedTraceFile, repetitionFile);
					result.add(rawTrace);
				} else {
					break;
				}
			}
			return result.isEmpty() ? null : result;
		}
	}
	
	public List<byte[]> getExecutionTracesByteArrays(int traceIndex, boolean log) {
		// check if a stored execution trace exists
		if (output != null) {
			if (!output.toFile().exists()) {
				return null;
			}
			// try to retrieve the execution traces from the zip file
			List<byte[]> result = new ArrayList<>(1);
			ZipFileWrapper zip = new ZipFileReader().submit(output).getResult();
			byte[] traceInvolvement;
			int traceCounter = -1;
			// assume IDs to start at 0
			while ((traceInvolvement = zip.get(traceIndex + "-" + (++traceCounter) + SpectraFileUtils.EXECUTION_TRACE_FILE_EXTENSION, false)) != null) {
				result.add(traceInvolvement);
			}
			if (!result.isEmpty()) {
				return result;
			}
		}
		return null;
	}

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
			while (true) {
				String compressedTraceFile = traceIndex + "-" + (++traceCounter) + SpectraFileUtils.EXECUTION_TRACE_FILE_EXTENSION;
				if (zip.exists(compressedTraceFile)) {
					String repetitionFile = traceIndex + "-" + (traceCounter) + SpectraFileUtils.EXECUTION_TRACE_REPETITIONS_FILE_EXTENSION;
					ExecutionTrace executionTrace = SpectraFileUtils.loadExecutionTraceFromZipFile(zip, compressedTraceFile, repetitionFile);
					result.add(executionTrace);
				} else {
					break;
				}
			}
			if (!result.isEmpty()) {
				return result;
			}
			
			// generate execution trace from current GS tree
			if (indexer == null) {
				extractCommonSequencesFromRawTraces();
				indexer = new GSIntArrayTreeIndexer(gsTree);
			}
			
			// if at this point, try generating the execution traces from raw traces on the fly
			List<CompressedTraceBase<int[], ?>> rawTraces = getRawTraces(traceIndex);
			if (rawTraces == null) {
				return null;
			}
			List<ExecutionTrace> executionTraces = generateExecutiontraceFromRawTraces(rawTraces, log);
			
			// collect execution traces
			traceCounter = -1;
			for (ExecutionTrace executionTrace : executionTraces) {
				String traceFileName = traceIndex + "-" + (++traceCounter) + SpectraFileUtils.EXECUTION_TRACE_FILE_EXTENSION;
				String repMarkerFileName = traceIndex + "-" + (traceCounter) + SpectraFileUtils.EXECUTION_TRACE_REPETITIONS_FILE_EXTENSION;
				// avoid storing traces in memory...
				// store the execution trace and repetition markers
				try {
					SpectraFileUtils.storeExecutionTrace(executionTrace, output, traceFileName, repMarkerFileName);
				} catch (IOException e) {
					Log.abort(this, e, "Could not store execution trace.");
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
					indexer = new GSIntArrayTreeIndexer(gsTree);
				}

				List<CompressedTraceBase<int[], ?>> rawTraces = getRawTraces(traceIndex);
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

	private List<ExecutionTrace> generateExecutiontraceFromRawTraces(List<CompressedTraceBase<int[], ?>> rawTraces, boolean log) {
		// replace sequences in the raw trace with indices
		List<ExecutionTrace> traces = new ArrayList<>(rawTraces.size());
		for (CompressedTraceBase<int[], ?> rawTrace : rawTraces) {
			try {
				traces.add(new ExecutionTrace(gsTree.generateIndexedTrace(rawTrace, indexer), log));
			} catch (IllegalStateException e) {
				Log.warn(this, "Generating indexed Trace not successful: %s", e.getMessage());
				Log.out(this, "Trying to add entire trace... (%d elements)", rawTrace.size());
				// a sequence was not matched correctly, so try to add the entire trace to the tree...
				extractCommonSequencesFromRawTrace(rawTrace.iterator());
				traces.add(new ExecutionTrace(gsTree.generateIndexedTrace(rawTrace, indexer), log));
			}
		}
		
		return traces;
	}

	private void extractCommonSequencesFromRawTraces() {
		if (indexer == null) {
			indexer = new GSIntArrayTreeIndexer(gsTree);
		}
		
		ProgressTracker tracker = new ProgressTracker(false);
		
		if (output == null) {
			for (Entry<Integer, List<CompressedTraceBase<int[], ?>>> entry : rawTracePool.entrySet()) {
				tracker.track("processing trace " + entry.getKey());
				for (CompressedTraceBase<int[], ?> trace : entry.getValue()) {
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
					CompressedTraceBase<int[],?> rawTrace = SpectraFileUtils
							.loadRawArrayTraceFromZipFile(zip, fileHeader.getFileName(), fileHeader.getFileName()
							.replace(RAW_TRACE_FILE_EXTENSION, REP_MARKER_FILE_EXTENSION));

//					extractCommonSequencesFromRawTrace(executionTrace.iterator());
					// it should suffice to only iterate over the compressed traces...
					// (if not, we will try processing the entire sequence later, when generating the execution traces)
					extractCommonSequencesFromRawTrace(rawTrace.getCompressedTrace().iterator());
					rawTrace = null;
				}
			} catch (Exception e) {
				Log.abort(this, e, "Error reading or processing raw traces from zip file.");
			}
		}
	}

	private void extractCommonSequencesFromRawTrace(CloneableIterator<int[]> traceIterator) {
		if (indexer != null) {
			indexer.reset();
		}
		// remember starting position
		CloneableIterator<int[]> unprocessedIterator = traceIterator.clone();
		int processedElements = 0;
		while (traceIterator.hasNext()) {
			IntArrayWrapper element = gsTree.getRepresentation(traceIterator.peek());
//			System.out.println("next: " + element);
			// TODO prevent negative elements!
//			if (element < 0) {
//				// check if the element is a correct id, which has to be positive (atm) TODO
//				// this may mark elements which shall be ignored/skipped when executing exactly this method...
//				throw new IllegalStateException("Tried to add an negative item: " + element);
//			}
			
			if (startElements.contains(element)) {
				// the element was recognized as a starting element, previously
				if (processedElements > 0) {
					// there exists an unprocessed sequence 
					// before this element's position
					gsTree.__addSequence(unprocessedIterator, processedElements, 
							gsTree.getRepresentation(unprocessedIterator.peek()));
	
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
			gsTree.__addSequence(unprocessedIterator, processedElements, 
					gsTree.getRepresentation(unprocessedIterator.peek()));
		}
	}

	public GSArrayTree<int[],IntArrayWrapper> getGsTree() {
		return gsTree;
	}

	public ArraySequenceIndexer<int[],IntArrayWrapper> getIndexer() {
		if (indexer == null) {
			extractCommonSequencesFromRawTraces();
			indexer = new GSIntArrayTreeIndexer(gsTree);
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

	public boolean moveExecutionTraces(int traceIndex, Path outputFile, Supplier<String> traceFileNameSupplier,
			Supplier<String> repMarkerFileNameSupplier) {
		// check if a stored execution trace exists
		if (output != null) {
			if (!output.toFile().exists()) {
				return false;
			}
			ZipFileWrapper zip = new ZipFileReader().submit(output).getResult();
			Module<Pair<String, String>, Boolean> module = new MoveNamedByteArraysBetweenZipFilesProcessor(output, outputFile).asModule();
			int threadIndex = -1;
			while (true) {
				String traceFile = traceIndex + "-" + (++threadIndex) + SpectraFileUtils.EXECUTION_TRACE_FILE_EXTENSION;
				if (zip.exists(traceFile)) {
					String traceFileTarget = traceFileNameSupplier.get();
					String repMarkerFileTarget = repMarkerFileNameSupplier.get();
					
					// move trace file
					boolean successful = module.submit(new Pair<>(traceFile, traceFileTarget)).getResult();
					
					if (!successful) {
						Log.abort(SpectraFileUtils.class, "Could not move trace file.");
					}
					
					String repMarkerFile = (traceIndex) + "-" + (threadIndex) + SpectraFileUtils.EXECUTION_TRACE_REPETITIONS_FILE_EXTENSION;
					if (zip.exists(repMarkerFile)) {
						// move repetition marker file
						successful = module.submit(new Pair<>(repMarkerFile, repMarkerFileTarget)).getResult();
						
						if (!successful) {
							Log.abort(SpectraFileUtils.class, "Could not move repetition marker file.");
						}
					}
					
//					// load the repetition marker array
//					executionTraceThreadInvolvement = zip.get((traceCounter) + "-" + (threadIndex) 
//							+ SpectraFileUtils.EXECUTION_TRACE_REPETITIONS_FILE_EXTENSION, false);
//					if (executionTraceThreadInvolvement == null) {
//						traces.add(new ExecutionTrace(compressedTrace, new int[] {}));
//					} else {
//						int[] repetitionMarkers = execTraceProcessor.submit(executionTraceThreadInvolvement).getResult();
//						traces.add(new ExecutionTrace(compressedTrace, repetitionMarkers));
//					}
				} else {
					break;
				}
			}
			return true;
		}
		return false;
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
