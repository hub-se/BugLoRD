package se.de.hu_berlin.informatik.spectra.core.traces;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;

import net.lingala.zip4j.model.FileHeader;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CoberturaStatementEncoding;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CompressedIntegerIdTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CompressedIntegerTraceBase;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CompressedLongTraceBase;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.ReplaceableCloneableIntIterator;
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

public class RawIntTraceCollector {
	
	private static final String RAW_TRACE_FILE_EXTENSION = ".raw";
	private static final String REP_MARKER_FILE_EXTENSION = ".rep";
//	private static final String EXEC_TRACE_FILE_EXTENSION = ".exec";

	/**
	 * map from (global) sub trace ids to actual sub traces;
	 * this stores all existing (and executed) sub traces!
	 */
	private Map<Integer,CompressedLongTraceBase> globalIdToSubTraceMap;

	// stores (sub trace representation -> sub trace id) to retrieve subtrace ids
	// the long value representation has to contain start and ending node of the sub trace
	// if the sub trace is longer than one statement(s)
	private  Map<Long,Integer> subTraceGlobalIdMap = new HashMap<>();
	private int currentId = 0; 
	
	private final Path output;

	private final IntGSArrayTree gsTree = new IntGSArrayTree();

	private IntArraySequenceIndexer indexer = null;
	
	private final Set<Integer> startElements = new HashSet<>();
	
	public Map<Integer,CompressedLongTraceBase> getGlobalIdToSubTraceMap() {
		return globalIdToSubTraceMap;
	}
	
//	public RawTraceCollector() {
//		this(null);
//	}
	
	public RawIntTraceCollector(Path outputDir) {
		Objects.requireNonNull(outputDir, "Path to store raw traces must not be null!");
		outputDir.toFile().mkdirs();
		this.output = outputDir.resolve("rawTraces.zip");
		new AddNamedByteArrayToZipFileProcessor(this.output, true).asModule();
		if (globalIdToSubTraceMap == null) {
			globalIdToSubTraceMap = getNewSubTraceMap(output.getParent());
		}
		
		this.output.toFile().deleteOnExit();
//		Runtime.getRuntime().addShutdownHook(new Thread(new RemoveOutput(this.output)));
	}
	
	public boolean addRawTraceToPool(int traceIndex, int threadId, 
			BufferedIntArrayQueue trace, boolean log,
			Map<Integer,CompressedLongTraceBase> idToSubTraceMap) {
		addTrace(traceIndex, threadId, trace, log, idToSubTraceMap);
		return true;
	}
	
	// only used for testing purposes
	public boolean addRawTraceToPool(int traceIndex, int threadId, 
			int[] traceArray, boolean log, Path outputDir, String prefix,
			Map<Integer,CompressedLongTraceBase> idToSubTraceMap) {
		BufferedIntArrayQueue trace = new BufferedIntArrayQueue(outputDir.toFile(), prefix, 100);
//        trace.addAll(Arrays.asList(traceArray));
		for (int i : traceArray) {
			trace.add(i);
		}
//		trace.clear(1);
//		for (ReplaceableCloneableIntIterator iterator = trace.iterator(); iterator.hasNext();) {
//			int integer = iterator.next();
//			System.out.print(integer + ", ");
//		}
//		System.out.println();
		return addRawTraceToPool(traceIndex, threadId, trace, log, idToSubTraceMap);
	}

	private void addTrace(int traceIndex, int threadId, 
			BufferedIntArrayQueue trace, boolean log,
			Map<Integer,CompressedLongTraceBase> idToSubTraceMap) {
		addTrace(traceIndex, threadId, new CompressedIntegerIdTrace(trace, log), idToSubTraceMap);
	}
	
	public boolean addRawTraceToPool(int traceIndex, int threadId, 
			CompressedIntegerTraceBase eTrace,
			Map<Integer, CompressedLongTraceBase> map) {
		addTrace(traceIndex, threadId, eTrace, map);
		return true;
	}

	private void addTrace(int traceIndex, int threadId, 
			CompressedIntegerTraceBase eTrace,
			Map<Integer, CompressedLongTraceBase> map) {
		
		boolean error = false;
		for (ReplaceableCloneableIntIterator iterator = eTrace.getCompressedTrace().iterator(); iterator.hasNext();) {
			// check if all IDs are actually stored in the map
			Integer id = iterator.next();
			if (!map.containsKey(id)) {
				// this should definitely not happen!
				error = true;
				System.err.println(traceIndex + "-" + threadId + ": No sub trace mapping found for id " + id);
			}
		}
		
		if (error) {
			throw new IllegalStateException("No sub trace mapping found for some ID(s).");
		}
		
		// the next few commands are necessary to ensure consistency in sub trace ids!!!
		// each project data comes potentially with its own mapping from ids to sub traces...
		// so we need to get the respective ids for existing sub traces and produce 
		// new ones for newly added sub traces.
		Map<Integer,Integer> localIdToGlobalIdMap = getAndCreateSubTraceIdMapping(map, output.getParent());
		// and then replace all local ids with the global ones...
		globalizeTrace(eTrace, localIdToGlobalIdMap);
		
		// collect raw trace
		String traceFileName = traceIndex + "-" + threadId + RAW_TRACE_FILE_EXTENSION;
		String repMarkerFileName = traceIndex + "-" + threadId + REP_MARKER_FILE_EXTENSION;
		// avoid storing traces in memory...
		// store the compressed trace
		try {
			SpectraFileUtils.storeCompressedIntegerTrace(eTrace, output, traceFileName, repMarkerFileName);
		} catch (IOException e) {
			Log.abort(this, e, "Could not store raw trace.");
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
	
	private void globalizeTrace(CompressedIntegerTraceBase eTrace,
			Map<Integer, Integer> localIdToGlobalIdMap) {
		for (ReplaceableCloneableIntIterator iterator = eTrace.getCompressedTrace().iterator(); iterator.hasNext();) {
			// this should now replace all local sub trace ids with the respective global ids...
			@SuppressWarnings("unused")
			Integer previous = iterator.processNextAndReplaceWithResult(k -> {
				Integer globalId = localIdToGlobalIdMap.get(k);
				if (globalId == null) {
					// this should definitely not happen!
					throw new IllegalStateException("No global id found for local id " + k);
				}
				return globalId;
			});
		}
		
	}

	private Map<Integer, Integer> getAndCreateSubTraceIdMapping(
			Map<Integer, CompressedLongTraceBase> map, Path tempDir) {
		if (globalIdToSubTraceMap == null) {
			globalIdToSubTraceMap = getNewSubTraceMap(tempDir);
		}
		// should map local sub trace ids (parameter) to global ids (field),
		// so that the ids in the submitted raw traces can be replaced.
		Map<Integer, Integer> subTraceIdMapping = new HashMap<>();
		Iterator<Entry<Integer, CompressedLongTraceBase>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, CompressedLongTraceBase> entry = iterator.next();
//			System.out.println(entry.getKey() + ">> " + entry.getValue());
			int globalId = getOrCreateIdForSubTrace(entry.getValue());
			subTraceIdMapping.put(entry.getKey(), globalId);
		}
		return subTraceIdMapping;
	}
	
	private Map<Integer, CompressedLongTraceBase> getNewSubTraceMap(Path tempDir) {
		return new HashMap<>();
//		// can delete buffered map on exit, due to no necessary serialization?? TODO check if correct...
//		return new BufferedMap<>(tempDir.toAbsolutePath().toFile(), 
//				String.valueOf(UUID.randomUUID()), ExecutionTraceCollector.MAP_CHUNK_SIZE, true);
	}
	
	// TODO reuse (parts of) the more or less identical method in ExecutionTraceCollector?
	private int getOrCreateIdForSubTrace(CompressedLongTraceBase compressedLongTraceBase) {
		if (compressedLongTraceBase == null || compressedLongTraceBase.size() == 0) {
			// id 0 indicates empty sub trace
			return 0;
		}

		long wrapper = CoberturaStatementEncoding.generateUniqueRepresentationForSubTrace(compressedLongTraceBase);
		Integer id = subTraceGlobalIdMap.get(wrapper);
		if (id == null) {
			// sub trace is not yet part of the global sub trace map
			// starts with id 1
			id = ++currentId;

			// new sub trace, so store new id and store sub trace
			subTraceGlobalIdMap.put(wrapper, currentId);
			compressedLongTraceBase.sleep();
			globalIdToSubTraceMap.put(currentId, compressedLongTraceBase);
		} else {
			if (!globalIdToSubTraceMap.get(id).equals(compressedLongTraceBase)) {
				// already got this sub trace in the global map!
				// (and it's not the exact same object!) 
				// delete any stored nodes from disk after processing all threads' traces!
				compressedLongTraceBase.markForDeletion();
//				compressedLongTraceBase.clear();
			}
		}
		
		return id;

	}

	public List<CompressedIntegerTraceBase> getRawTraces(int traceIndex) {
		if (!output.toFile().exists()) {
			return null;
		}
		// retrieve the raw traces from the zip file
		List<CompressedIntegerTraceBase> result = new ArrayList<>(1);
		ZipFileWrapper zip = new ZipFileReader().submit(output).getResult();

		int traceCounter = -1;
		// assume IDs to start at 0
		while (true) {
			String compressedTraceFile = traceIndex + "-" + (++traceCounter) + RAW_TRACE_FILE_EXTENSION;
			if (zip.exists(compressedTraceFile)) {
				String repetitionFile = traceIndex + "-" + (traceCounter) + REP_MARKER_FILE_EXTENSION;
				CompressedIntegerTraceBase rawTrace = SpectraFileUtils
						.loadRawTraceFromZipFile(zip, compressedTraceFile, repetitionFile);
				result.add(rawTrace);
			} else {
				break;
			}
		}
		return result.isEmpty() ? null : result;
	}
	
	public List<byte[]> getExecutionTracesByteArrays(int traceIndex, boolean log) {
		// check if a stored execution trace exists
		if (!output.toFile().exists()) {
			return null;
		}
		// try to retrieve the execution traces from the zip file
		List<byte[]> result = new ArrayList<>(1);
		ZipFileWrapper zip = new ZipFileReader().submit(output).getResult();
		byte[] traceInvolvement;
		int traceCounter = -1;
		// assume IDs to start at 0
		while ((traceInvolvement = zip.get(traceIndex + "-" + (++traceCounter) + 
				SpectraFileUtils.EXECUTION_TRACE_FILE_EXTENSION, false)) != null) {
			result.add(traceInvolvement);
		}
		if (!result.isEmpty()) {
			return result;
		} else {
			return null;
		}
	}

	public List<ExecutionTrace> getExecutionTraces(int traceIndex, boolean log) {
		// check if a stored execution trace exists
		if (!output.toFile().exists()) {
			return null;
		}
		// try to retrieve the execution traces from the zip file
		List<ExecutionTrace> result = new ArrayList<>(1);
		ZipFileWrapper zip = new ZipFileReader().submit(output).getResult();

		int traceCounter = -1;
		// assume IDs to start at 0
		while (true) {
			String compressedTraceFile = traceIndex + "-" + (++traceCounter) + 
					SpectraFileUtils.EXECUTION_TRACE_FILE_EXTENSION;
			if (zip.exists(compressedTraceFile)) {
				String repetitionFile = traceIndex + "-" + (traceCounter) + 
						SpectraFileUtils.EXECUTION_TRACE_REPETITIONS_FILE_EXTENSION;
				ExecutionTrace executionTrace = 
						SpectraFileUtils.loadExecutionTraceFromZipFile(zip, compressedTraceFile, repetitionFile);
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
			indexer = new IntGSArrayTreeIndexer(gsTree);
		}

		// if at this point, try generating the execution traces from raw traces on the fly
		List<CompressedIntegerTraceBase> rawTraces = getRawTraces(traceIndex);
		if (rawTraces == null) {
			return null;
		}
		List<ExecutionTrace> executionTraces = generateExecutiontraceFromRawTraces(rawTraces, log);

		// collect execution traces
		traceCounter = -1;
		for (ExecutionTrace executionTrace : executionTraces) {
			String traceFileName = traceIndex + "-" + (++traceCounter) + 
					SpectraFileUtils.EXECUTION_TRACE_FILE_EXTENSION;
			String repMarkerFileName = traceIndex + "-" + (traceCounter) + 
					SpectraFileUtils.EXECUTION_TRACE_REPETITIONS_FILE_EXTENSION;
			// avoid storing traces in memory...
			// store the execution trace and repetition markers
			try {
				SpectraFileUtils.storeCompressedIntegerTrace(executionTrace, output, traceFileName, repMarkerFileName);
			} catch (IOException e) {
				Log.abort(this, e, "Could not store execution trace.");
			}
		}
		// may still be null?
		return executionTraces;
	}

	private List<ExecutionTrace> generateExecutiontraceFromRawTraces(List<CompressedIntegerTraceBase> rawTraces, boolean log) {
		// replace sequences in the raw trace with indices
		List<ExecutionTrace> traces = new ArrayList<>(rawTraces.size());
		for (CompressedIntegerTraceBase rawTrace : rawTraces) {
			try {
				ExecutionTrace trace = new ExecutionTrace(gsTree.generateIndexedTrace(rawTrace, indexer), log);
				trace.sleep();
				traces.add(trace);
			} catch (IllegalStateException e) {
				Log.warn(this, "Generating indexed Trace not successful: %s", e.getMessage());
				Log.out(this, "Trying to add entire trace... (%d elements)", rawTrace.size());
				// a sequence was not matched correctly, so try to add the entire trace to the tree...
				extractCommonSequencesFromRawTrace(rawTrace.iterator());
				ExecutionTrace trace = new ExecutionTrace(gsTree.generateIndexedTrace(rawTrace, indexer), log);
				trace.sleep();
				traces.add(trace);
			}
		}
		
		return traces;
	}

	private void extractCommonSequencesFromRawTraces() {
		if (indexer == null) {
			indexer = new IntGSArrayTreeIndexer(gsTree);
		}
		
		ProgressTracker tracker = new ProgressTracker(false);

		if (!output.toFile().exists()) {
			return;
		}
		try {
			// retrieve the raw traces from the zip file
			ZipFileWrapper zip = new ZipFileReader().submit(output).getResult();
			List<FileHeader> rawTraceFiles = 
					zip.getFileHeadersContainingString(RAW_TRACE_FILE_EXTENSION);
			for (FileHeader fileHeader : rawTraceFiles) {
				tracker.track("processing " + fileHeader.getFileName());
				CompressedIntegerIdTrace rawTrace = SpectraFileUtils
						.loadRawTraceFromZipFile(zip, fileHeader.getFileName(), fileHeader.getFileName()
								.replace(RAW_TRACE_FILE_EXTENSION, REP_MARKER_FILE_EXTENSION));

//				extractCommonSequencesFromRawTrace(executionTrace.iterator());
				// it should suffice to only iterate over the compressed traces...
				// (if not, we will try processing the entire sequence later, when generating the execution traces)
				extractCommonSequencesFromRawTrace(rawTrace.getCompressedTrace().iterator());
				rawTrace = null;
			}
		} catch (Exception e) {
			Log.abort(this, e, "Error reading or processing raw traces from zip file.");
		}
	}

	private void extractCommonSequencesFromRawTrace(ReplaceableCloneableIntIterator replaceableCloneableIntIterator) {
		if (indexer != null) {
			indexer.reset();
		}
		// remember starting position
		ReplaceableCloneableIntIterator unprocessedIterator = replaceableCloneableIntIterator.clone();
		int processedElements = 0;
		while (replaceableCloneableIntIterator.hasNext()) {
			Integer element = replaceableCloneableIntIterator.peek();
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
							unprocessedIterator.peek());
	
					unprocessedIterator = replaceableCloneableIntIterator.clone();
					processedElements = 0;
				}
			}
			
			// add the current element to the list of unprocessed elements
			++processedElements;
			replaceableCloneableIntIterator.next();
		}
		
		// process remaining elements
		if (processedElements > 0) {
			// there exists an unprocessed sequence
			gsTree.__addSequence(unprocessedIterator, processedElements, 
					unprocessedIterator.peek());
		}
	}

	public IntGSArrayTree getGsTree() {
		if (indexer == null) {
			extractCommonSequencesFromRawTraces();
			indexer = new IntGSArrayTreeIndexer(gsTree);
		}
		return gsTree;
	}

	public IntArraySequenceIndexer getIndexer() {
		if (indexer == null) {
			extractCommonSequencesFromRawTraces();
			indexer = new IntGSArrayTreeIndexer(gsTree);
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
		if (!output.toFile().exists()) {
			return false;
		}
		ZipFileWrapper zip = new ZipFileReader().submit(output).getResult();
		Module<Pair<String, String>, Boolean> module = 
				new MoveNamedByteArraysBetweenZipFilesProcessor(output, outputFile).asModule();
		int threadIndex = -1;
		while (true) {
			String traceFile = traceIndex + "-" + (++threadIndex) + 
					SpectraFileUtils.EXECUTION_TRACE_FILE_EXTENSION;
			if (zip.exists(traceFile)) {
				String traceFileTarget = traceFileNameSupplier.get();
				String repMarkerFileTarget = repMarkerFileNameSupplier.get();

				// move trace file
				boolean successful = module.submit(new Pair<>(traceFile, traceFileTarget)).getResult();

				if (!successful) {
					Log.abort(SpectraFileUtils.class, "Could not move trace file.");
				}

				String repMarkerFile = (traceIndex) + "-" + (threadIndex) + 
						SpectraFileUtils.EXECUTION_TRACE_REPETITIONS_FILE_EXTENSION;
				if (zip.exists(repMarkerFile)) {
					// move repetition marker file
					successful = module.submit(new Pair<>(repMarkerFile, repMarkerFileTarget)).getResult();

					if (!successful) {
						Log.abort(SpectraFileUtils.class, "Could not move repetition marker file.");
					}
				}

//				// load the repetition marker array
//				executionTraceThreadInvolvement = zip.get((traceCounter) + "-" + (threadIndex) 
//						+ SpectraFileUtils.EXECUTION_TRACE_REPETITIONS_FILE_EXTENSION, false);
//				if (executionTraceThreadInvolvement == null) {
//					traces.add(new ExecutionTrace(compressedTrace, new int[] {}));
//				} else {
//					int[] repetitionMarkers = execTraceProcessor.submit(executionTraceThreadInvolvement).getResult();
//					traces.add(new ExecutionTrace(compressedTrace, repetitionMarkers));
//				}
			} else {
				break;
			}
		}
		return true;
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
