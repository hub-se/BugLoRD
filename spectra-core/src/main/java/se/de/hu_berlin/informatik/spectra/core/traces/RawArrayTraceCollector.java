package se.de.hu_berlin.informatik.spectra.core.traces;

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
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.IntArrayArrayIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.IntArrayWrapper;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CloneableIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CompressedTrace;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.compression.ziputils.AddNamedByteArrayToZipFileProcessor;
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
	private static final String EXEC_TRACE_FILE_EXTENSION = ".exec";

	private Map<Integer,List<CompressedTraceBase<int[],?>>> rawTracePool;
	
	private Path output;
	private Module<Pair<String, byte[]>, byte[]> zipModule;
	
	private GSArrayTree<int[],IntArrayWrapper> gsTree = new GSIntArrayTree();
	private Map<Integer,List<ExecutionTrace>> executionTracePool = new HashMap<>();
	
	private ArraySequenceIndexer<int[],IntArrayWrapper> indexer = null;
	
	private Set<IntArrayWrapper> startElements = new HashSet<>();
	
	
//	public RawTraceCollector() {
//		this(null);
//	}
	
	public RawArrayTraceCollector(Path outputDir) {
		Objects.requireNonNull(outputDir, "Path to store raw traces must not be null!");
		outputDir.toFile().mkdirs();
		this.output = outputDir.resolve("rawTraces.zip");
		zipModule = new AddNamedByteArrayToZipFileProcessor(this.output, true).asModule();
		
		this.output.toFile().deleteOnExit();
//		Runtime.getRuntime().addShutdownHook(new Thread(new RemoveOutput(this.output)));
	}
	
	public boolean addRawTraceToPool(int traceIndex, int threadId, SingleLinkedArrayQueue<int[]> trace, boolean log) {
		addTrace(traceIndex, threadId, trace, log);
		return true;
	}
	
	// only used for testing purposes
	public boolean addRawTraceToPool(int traceIndex, int threadId, int[][] traceArray, boolean log) {
		SingleLinkedArrayQueue<int[]> trace = new SingleLinkedArrayQueue<>(100);
		for (int i = 0; i < traceArray.length; i++) {
			trace.add(traceArray[i]);
		}
//		trace.clear(1);
//		for (Iterator<Integer> iterator = trace.iterator(); iterator.hasNext();) {
//			Integer integer = iterator.next();
//			System.out.print(integer + ", ");
//		}
//		System.out.println();
		return addRawTraceToPool(traceIndex, threadId, trace, log);
	}

	private void addTrace(int traceIndex, int threadId, SingleLinkedArrayQueue<int[]> trace, boolean log) {
		addTrace(traceIndex, threadId, new CompressedTrace(trace, log));
	}
	
	public boolean addRawTraceToPool(int traceIndex, int threadId, CompressedTraceBase<int[],IntArrayWrapper> eTrace) {
		addTrace(traceIndex, threadId, eTrace);
		return true;
	}

	private void addTrace(int traceIndex, int threadId, CompressedTraceBase<int[],IntArrayWrapper> eTrace) {
		// collect raw trace
		if (output == null) {
			List<CompressedTraceBase<int[], ?>> list = rawTracePool.computeIfAbsent(traceIndex, k -> { return new ArrayList<>(1); });
			list.add(eTrace);
		} else {
			// avoid storing traces in memory...
			// store the compressed trace
			byte[] involvement = SpectraFileUtils.storeCompressedTraceForRawTraceAsByteArray(eTrace);
			zipModule.submit(new Pair<>(traceIndex + "-" + threadId + RAW_TRACE_FILE_EXTENSION, involvement));
			involvement = null;
			
			// store the repetition markers
			involvement = SpectraFileUtils.storeRepetitionMarkersForRawTraceAsByteArray(eTrace);
			zipModule.submit(new Pair<>(traceIndex + "-" + threadId + REP_MARKER_FILE_EXTENSION, involvement));
			involvement = null;
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
			byte[] traceInvolvement;
			int traceCounter = -1;
			// assume IDs to start at 0
			while ((traceInvolvement = zip.get(traceIndex + "-" + (++traceCounter) + RAW_TRACE_FILE_EXTENSION, false)) != null) {
				byte[] repMarkerInvolvement = zip.get(traceIndex + "-" + (++traceCounter) + REP_MARKER_FILE_EXTENSION, false);
				CompressedTraceBase<int[],?> rawTrace = SpectraFileUtils
				.loadRawTraceFromByteArrays(traceInvolvement, repMarkerInvolvement);
				result.add(rawTrace);
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
			while ((traceInvolvement = zip.get(traceIndex + "-" + (++traceCounter) + EXEC_TRACE_FILE_EXTENSION, false)) != null) {
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
			byte[] traceInvolvement;
			int traceCounter = -1;
			// assume IDs to start at 0
			while ((traceInvolvement = zip.get(traceIndex + "-" + (++traceCounter) + EXEC_TRACE_FILE_EXTENSION, false)) != null) {
				ExecutionTrace executionTrace = SpectraFileUtils.loadExecutionTraceFromByteArray(traceInvolvement);
				result.add(executionTrace);
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
				// avoid storing traces in memory...
				// store the execution trace
				byte[] involvement = SpectraFileUtils.storeAsByteArray(executionTrace);

				// store each trace separately
				zipModule.submit(new Pair<>(traceIndex + "-" + (++traceCounter) + EXEC_TRACE_FILE_EXTENSION, involvement));
				involvement = null;
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
			traces.add(new ExecutionTrace(gsTree.generateIndexedTrace(rawTrace, indexer), log));
		}
		
		return traces;
	}

	private void extractCommonSequencesFromRawTraces() {
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
					byte[] traceInvolvement = zip.uncheckedGet(fileHeader);
					byte[] repMarkerInvolvement = zip.uncheckedGet(fileHeader.getFileName()
							.replace(RAW_TRACE_FILE_EXTENSION, REP_MARKER_FILE_EXTENSION));
					CompressedTraceBase<int[],?> rawTrace = SpectraFileUtils
							.loadRawTraceFromByteArrays(traceInvolvement, repMarkerInvolvement);
					traceInvolvement = null;
//					extractCommonSequencesFromRawTrace(executionTrace.iterator());
					// it should suffice to only iterate over the compressed traces...
					extractCommonSequencesFromRawTrace(new IntArrayArrayIterator(rawTrace.getCompressedTrace()));
					rawTrace = null;
				}
			} catch (Exception e) {
				Log.abort(this, e, "Error reading or processing raw traces from zip file.");
			}
		}
	}

	private void extractCommonSequencesFromRawTrace(CloneableIterator<int[]> traceIterator) {
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
