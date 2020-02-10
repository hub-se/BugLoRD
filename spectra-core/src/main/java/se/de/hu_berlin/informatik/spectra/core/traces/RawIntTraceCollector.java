package se.de.hu_berlin.informatik.spectra.core.traces;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.zip.ZipException;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.output.OutputSequence;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.compression.ziputils.AddNamedByteArrayToZipFileProcessor;
import se.de.hu_berlin.informatik.utils.compression.ziputils.MoveNamedByteArraysBetweenZipFilesProcessor;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileWrapper;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Abort;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;
import se.de.hu_berlin.informatik.utils.processors.sockets.module.Module;

public class RawIntTraceCollector {
	
//	private static final String RAW_TRACE_FILE_EXTENSION = ".raw";
//	private static final String REP_MARKER_FILE_EXTENSION = ".rep";
//	private static final String EXEC_TRACE_FILE_EXTENSION = ".exec";

	private final Path output;

//	public RawTraceCollector() {
//		this(null);
//	}
	
	public RawIntTraceCollector(Path outputDir) {
		Objects.requireNonNull(outputDir, "Path to store raw traces must not be null!");
		outputDir.toFile().mkdirs();
		this.output = outputDir.resolve("rawTraces.zip");
		new AddNamedByteArrayToZipFileProcessor(this.output, true).asModule();
//		if (globalIdToSubTraceMap == null) {
//			globalIdToSubTraceMap = getNewSubTraceMap(output.getParent());
//		}
		
		this.output.toFile().deleteOnExit();
//		Runtime.getRuntime().addShutdownHook(new Thread(new RemoveOutput(this.output)));
	}
	
	// only used for testing purposes
	public boolean addRawTraceToPool(int traceIndex, int threadId, 
			byte[] trace, boolean log) {
		addTrace(traceIndex, threadId, trace, log);
		return true;
	}
	
	// only used for testing purposes
	public boolean addRawTraceToPool(int traceIndex, int threadId, 
			int[] traceArray, boolean log, Path outputDir, String prefix) {

		OutputSequence outSeq = new OutputSequence();
        for (int i : traceArray) {
            outSeq.append(i);
        }
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut;
		try {
			objOut = new ObjectOutputStream(byteOut);
			outSeq.writeOut(objOut, true);
	        objOut.close();
	        byte[] trace = byteOut.toByteArray();
	        
			return addRawTraceToPool(traceIndex, threadId, trace, log);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return false;
	}
	
	private void addTrace(int traceIndex, int threadId, 
			byte[] trace, boolean log) {
		addTrace(traceIndex, threadId, trace);
	}
	
	public boolean addRawTraceToPool(int traceIndex, int threadId, byte[] eTrace) {
		addTrace(traceIndex, threadId, eTrace);
		return true;
	}

	private void addTrace(int traceIndex, int threadId, byte[] eTrace) {
		// collect raw trace
		String traceFileName = traceIndex + "-" + threadId + SpectraFileUtils.EXECUTION_TRACE_FILE_EXTENSION;
		// avoid storing traces in memory...
		// store the compressed trace
		try {
			SpectraFileUtils.storeCompressedIntegerTrace(eTrace, output, traceFileName);
		} catch (IOException e) {
			Log.abort(this, e, "Could not store raw trace.");
		}
	}

	
	public Collection<ExecutionTrace> getRawTraces(int traceIndex) throws ZipException {
		if (!output.toFile().exists()) {
			return null;
		}
		// retrieve the raw traces from the zip file
		List<ExecutionTrace> result = new ArrayList<>(1);
		ZipFileWrapper zip = ZipFileWrapper.getZipFileWrapper(output);

		int traceCounter = -1;
		// assume IDs to start at 0
		while (true) {
			String compressedTraceFile = traceIndex + "-" + (++traceCounter) + SpectraFileUtils.EXECUTION_TRACE_FILE_EXTENSION;
			if (zip.exists(compressedTraceFile)) {
				ExecutionTrace rawTrace = SpectraFileUtils
						.loadExecutionTraceFromZipFile(zip, compressedTraceFile);
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
		ZipFileWrapper zip = ZipFileWrapper.getZipFileWrapper(output);
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

	public Collection<ExecutionTrace> calculateExecutionTraces(int traceIndex, boolean log) throws ZipException {
		// check if a stored execution trace exists
		if (!output.toFile().exists()) {
			Log.err(this, "%s does not exist!", output);
			return null;
		}
		
		return getRawTraces(traceIndex);
	}


	@Override
	public void finalize() throws Throwable {
		if (output != null) {
			FileUtils.delete(output);
		}
		super.finalize();
	}

	public boolean moveExecutionTraces(int traceIndex, Path outputFile, Supplier<String> traceFileNameSupplier) throws ZipException, Abort {
		// check if a stored execution trace exists
		if (!output.toFile().exists()) {
			return false;
		}
		ZipFileWrapper zip = ZipFileWrapper.getZipFileWrapper(output);
		Module<Pair<String, String>, Boolean> module = 
				new MoveNamedByteArraysBetweenZipFilesProcessor(output, outputFile).asModule();
		int threadIndex = -1;
		while (true) {
			String traceFile = traceIndex + "-" + (++threadIndex) + SpectraFileUtils.EXECUTION_TRACE_FILE_EXTENSION;
			if (zip.exists(traceFile)) {
//				Log.out(this, "Moving '%s' from %s to %s", traceFile, output, outputFile);
				String traceFileTarget = traceFileNameSupplier.get();

				// move trace file
				boolean successful = module.submit(new Pair<>(traceFile, traceFileTarget)).getResult();

				if (!successful) {
					Log.abort(SpectraFileUtils.class, "Could not move trace file.");
				}

//				String repMarkerFile = (traceIndex) + "-" + (threadIndex) + 
//						SpectraFileUtils.EXECUTION_TRACE_REPETITIONS_FILE_EXTENSION;
//				if (zip.exists(repMarkerFile)) {
//					// move repetition marker file
//					successful = module.submit(new Pair<>(repMarkerFile, repMarkerFileTarget)).getResult();
//
//					if (!successful) {
//						Log.abort(SpectraFileUtils.class, "Could not move repetition marker file.");
//					}
//				}

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
