package se.de.hu_berlin.informatik.spectra.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.Map.Entry;

import de.unistuttgart.iste.rss.bugminer.coverage.CoverageReport;
import de.unistuttgart.iste.rss.bugminer.coverage.CoverageReportDeserializer;
import de.unistuttgart.iste.rss.bugminer.coverage.CoverageReportSerializer;
import de.unistuttgart.iste.rss.bugminer.coverage.FileCoverage;
import de.unistuttgart.iste.rss.bugminer.coverage.SourceCodeFile;
import de.unistuttgart.iste.rss.bugminer.coverage.TestCase;
import se.de.hu_berlin.informatik.utils.compression.CompressedByteArraysToByteArraysProcessor;
import se.de.hu_berlin.informatik.utils.compression.IntSequencesToCompressedByteArrayProcessor;
import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.count.CountSpectra;
import se.de.hu_berlin.informatik.spectra.core.count.CountTrace;
import se.de.hu_berlin.informatik.spectra.core.hit.HitSpectra;
import se.de.hu_berlin.informatik.spectra.core.hit.HitTrace;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;
import se.de.hu_berlin.informatik.spectra.core.traces.SequenceIndexer;
import se.de.hu_berlin.informatik.spectra.core.traces.SimpleIndexer;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.BufferedMap;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CompressedTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CompressedTraceBase;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ExecutionTraceCollector;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.BufferedArrayQueue;
import se.de.hu_berlin.informatik.utils.compression.BufferedCompressedByteArrayToIntArrayQueueProcessor;
import se.de.hu_berlin.informatik.utils.compression.BufferedIntArraysToCompressedByteArrayProcessor;
import se.de.hu_berlin.informatik.utils.compression.CompressedByteArrayToIntArraysProcessor;
import se.de.hu_berlin.informatik.utils.compression.CompressedByteArrayToIntSequencesProcessor;
import se.de.hu_berlin.informatik.utils.compression.single.BufferedCompressedByteArrayToIntegerQueueProcessor;
import se.de.hu_berlin.informatik.utils.compression.single.BufferedIntegersToCompressedByteArrayProcessor;
import se.de.hu_berlin.informatik.utils.compression.single.ByteArrayToCompressedByteArrayProcessor;
import se.de.hu_berlin.informatik.utils.compression.single.CompressedByteArrayToByteArrayProcessor;
import se.de.hu_berlin.informatik.utils.compression.single.CompressedByteArrayToIntSequenceProcessor;
import se.de.hu_berlin.informatik.utils.compression.single.IntSequenceToCompressedByteArrayProcessor;
import se.de.hu_berlin.informatik.utils.compression.ziputils.AddNamedByteArrayToZipFileProcessor;
import se.de.hu_berlin.informatik.utils.compression.ziputils.MoveNamedByteArraysBetweenZipFilesProcessor;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileReader;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileWrapper;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.files.csv.CSVUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;
import se.de.hu_berlin.informatik.utils.processors.basics.StringsToFileWriter;
import se.de.hu_berlin.informatik.utils.processors.sockets.module.Module;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.Pipe;
import se.de.hu_berlin.informatik.utils.tracking.ProgressTracker;

import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;

/**
 * Helper class to save and load spectra objects.
 * 
 * @author Simon
 *
 */
public class SpectraFileUtils {

	private static final String SEQ_INDEX_DIR = "SeqIndex";

	// TODO: what about hit count spectra? They are saved as normal hit spectra
	// atm...

	private static final String IDENTIFIER_DELIMITER = "\t";

	private static final String NODE_IDENTIFIER_FILE_INDEX = "0.bin";
	private static final String TRACE_IDENTIFIER_FILE_INDEX = "1.bin";
	private static final String INVOLVEMENT_TABLE_FILE_INDEX = "2.bin";
	private static final String STATUS_FILE_INDEX = "3.bin";
	private static final String INDEX_FILE_INDEX = "4.bin";

	private static final String NODE_IDENTIFIER_FILE_NAME = ".nodeIDs";
	private static final String TRACE_IDENTIFIER_FILE_NAME = ".traceIDs";
	private static final String STATUS_FILE_NAME = ".status";
	private static final String INDEX_FILE_NAME = ".index";

	private static final String TRACE_FILE_EXTENSION = ".trc";
	public static final String EXECUTION_TRACE_FILE_EXTENSION = ".flw";
	public static final String EXECUTION_TRACE_REPETITIONS_FILE_EXTENSION = ".rflw";
	
//	private static final String SEQUENCE_INDEX_FILE_EXTENSION = ".seq";
	private static final String SEQUENCE_FILE_NAME = ".sequences";

	public static final byte STATUS_UNCOMPRESSED = 0;
	public static final byte STATUS_COMPRESSED = 1;
	public static final byte STATUS_UNCOMPRESSED_INDEXED = 2;
	public static final byte STATUS_COMPRESSED_INDEXED = 3;
	public static final byte STATUS_SPARSE = 4;
	public static final byte STATUS_SPARSE_INDEXED = 5;

	public static final byte STATUS_COMPRESSED_COUNT = 6;
	public static final byte STATUS_COMPRESSED_INDEXED_COUNT = 7;

	// suppress default constructor (class should not be instantiated)
	private SpectraFileUtils() {
		throw new AssertionError();
	}

	/**
	 * Saves a Spectra object to hard drive. Has to be used if the type T is not
	 * indexable.
	 * @param spectra
	 * the Spectra object to save
	 * @param output
	 * the output path to the zip file to be created
	 * @param compress
	 * whether or not to use an additional compression procedure apart from
	 * zipping
	 * @param sparse
	 * whether or not to use a sparse matrix representation (less space needed
	 * for storage)
	 * @param <T>
	 * the type of nodes in the spectra; does not have to be indexable and will
	 * thus not be indexed
	 */
	public static <T> void saveSpectraToZipFile(ISpectra<T, ?> spectra, Path output, boolean compress, boolean sparse) {
		if (spectra.getTraces().size() == 0 || spectra.getNodes().size() == 0) {
			Log.err(SpectraFileUtils.class, "Can not save empty spectra...");
			return;
		}

		Collection<INode<T>> nodes = spectra.getNodes();

		String nodeIdentifiers = getNodeIdentifierListString(nodes);

		String traceIdentifiers = getTraceIdentifierListString(spectra.getTraces());

		saveSpectraToZipFile(spectra, output, compress, sparse, false, nodes, null, nodeIdentifiers, traceIdentifiers);
	}

	private static <T> String getNodeIdentifierListString(Collection<INode<T>> nodes) {
		StringBuilder buffer = new StringBuilder();
		// store the identifiers (order is important)
		for (INode<T> node : nodes) {
			buffer.append(node.getIdentifier()).append(IDENTIFIER_DELIMITER);
		}
		if (buffer.length() > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		return buffer.toString();
	}

	private static <T> String getTraceIdentifierListString(Collection<? extends ITrace<T>> traces) {
		StringBuilder buffer = new StringBuilder();
		// store the identifiers (order is important)
		for (ITrace<T> trace : traces) {
			buffer.append(trace.getIdentifier()).append(IDENTIFIER_DELIMITER);
		}
		if (buffer.length() > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		return buffer.toString();
	}

	public static void saveBlockSpectraToZipFile(ISpectra<SourceCodeBlock, ?> spectra, Path output, boolean compress,
			boolean sparse, boolean index) {
		saveSpectraToZipFile(SourceCodeBlock.DUMMY, spectra, output, compress, sparse, index);
	}

	/**
	 * Saves a Spectra object to hard drive.
	 * @param dummy
	 * a dummy object of type T that is used for obtaining indexed identifiers;
	 * if the dummy is null, then no index can be created and the result is
	 * equal to calling the non-indexable version of this method
	 * @param spectra
	 * the Spectra object to save
	 * @param output
	 * the output path to the zip file to be created
	 * @param compress
	 * whether or not to use an additional compression procedure apart from
	 * zipping
	 * @param sparse
	 * whether or not to use a sparse matrix representation (less space needed
	 * for storage)
	 * @param index
	 * whether to index the identifiers to minimize the needed storage space
	 * @param <T>
	 * the type of nodes in the spectra
	 */
	public static <T extends Indexable<T>> void saveSpectraToZipFile(T dummy, ISpectra<T, ?> spectra, Path output,
			boolean compress, boolean sparse, boolean index) {
		if (dummy == null) {
			saveSpectraToZipFile(spectra, output, compress, sparse);
			return;
		}

		// if (spectra.getTraces().size() == 0 || spectra.getNodes().size() ==
		// 0) {
		// Log.err(SpectraFileUtils.class, "Can not save empty spectra...");
		// return;
		// }

		// (the following would not be necessary, as long as the ordering stays the same throughout processing)
		// make sure that the nodes are ordered by index
		List<INode<T>> nodes = spectra.getNodes().stream().sorted(comparingInt(INode::getIndex))
				.collect(Collectors.toList());
		
//		Collection<INode<T>> nodes = spectra.getNodes();
		
		Map<String, Integer> map = new HashMap<>();

		String nodeIdentifiers = getIdentifierString(dummy, index, nodes, map);
		String traceIdentifiers = getTraceIdentifierListString(spectra.getTraces());

		saveSpectraToZipFile(spectra, output, compress, sparse, index, nodes, map, nodeIdentifiers, traceIdentifiers);
	}

	private static <T extends Indexable<T>> String getIdentifierString(T dummy, boolean index,
			Collection<INode<T>> nodes, Map<String, Integer> map) {
		StringBuilder buffer = new StringBuilder();
		if (index) {
			// store the identifiers in indexed (shorter) format (order is
			// important)
			for (INode<T> node : nodes) {
				buffer.append(dummy.getIndexedIdentifier(node.getIdentifier(), map)).append(IDENTIFIER_DELIMITER);
			}
		} else {
			// store the identifiers (order is important)
			for (INode<T> node : nodes) {
				buffer.append(node.getIdentifier()).append(IDENTIFIER_DELIMITER);
			}
		}
		if (buffer.length() > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		return buffer.toString();
	}
	
	@SuppressWarnings("unchecked")
	private static <T, K extends ITrace<T>> void saveSpectraToZipFile(ISpectra<T, K> spectra, Path output,
			boolean compress, boolean sparse, boolean index, Collection<INode<T>> nodes, Map<String, Integer> map,
			String nodeIdentifiers, String traceIdentifiers) {
		
		FileUtils.delete(output);
		
		// create a map that maps node IDs to storing IDs 
		// (node IDs may not be consecutive due to removal of nodes, etc.)
		Map<Integer, Integer> nodeIndexToStoreIdMap = new ConcurrentHashMap<>();
		int orderID = -1;
		for (INode<T> node : nodes) {
			nodeIndexToStoreIdMap.put(node.getIndex(), ++orderID);
		}
		
		byte[] status = { STATUS_UNCOMPRESSED };

		// byte[] involvement;
		if (!spectra.getTraces().isEmpty() && spectra.getTraces().iterator().next() instanceof CountTrace) {
			saveInvolvementArrayForCountSpectra(
					(ISpectra<T, ? extends CountTrace<T>>) spectra, nodes, index, 
					status, nodeIndexToStoreIdMap, output);
		} else {
			saveInvolvementArray(spectra, nodes, sparse, compress, index, 
					status, nodeIndexToStoreIdMap, output);
		}

		// now, we have a list of identifiers and the involvement table
		// so add them to the output zip file

		Module<Pair<String, byte[]>, byte[]> module = new AddNamedByteArrayToZipFileProcessor(output, false).asModule();

		module.submit(new Pair<>(NODE_IDENTIFIER_FILE_NAME, nodeIdentifiers.getBytes()))
				.submit(new Pair<>(TRACE_IDENTIFIER_FILE_NAME, traceIdentifiers.getBytes()))
				.submit(new Pair<>(STATUS_FILE_NAME, status));

		if (index) {
			// store the actual identifier names (order is important here, too)
			StringBuilder identifierBuilder = new StringBuilder();
			List<String> identifierNames = Misc.sortByValueToKeyList(map);
			for (String identifier : identifierNames) {
				identifierBuilder.append(identifier).append(IDENTIFIER_DELIMITER);
			}
			if (identifierBuilder.length() > 0) {
				identifierBuilder.deleteCharAt(identifierBuilder.length() - 1);
			}

			module.submit(new Pair<>(INDEX_FILE_NAME, identifierBuilder.toString().getBytes()));
		}
	}

	private static <T> void saveInvolvementArray(ISpectra<T, ?> spectra, Collection<INode<T>> nodes, boolean sparse,
			boolean compress, boolean index, byte[] status,
			Map<Integer, Integer> nodeIndexToStoreIdMap, Path outputFile) {
		int traceCount = 0;

		if (sparse) {
			Module<Pair<String, byte[]>, byte[]> zipModule = new AddNamedByteArrayToZipFileProcessor(outputFile, false).asModule();
			IntSequenceToCompressedByteArrayProcessor module = new IntSequenceToCompressedByteArrayProcessor();
			// iterate through the traces
			for (ITrace<T> trace : spectra.getTraces()) {
				++traceCount;
				// is automatically compressed right now... TODO?
				List<Integer> sparseEntries = new ArrayList<>(trace.involvedNodesCount() + 1);
				// the first element is a flag that marks successful traces with
				// '1'
				if (trace.isSuccessful()) {
					sparseEntries.add(1);
				} else {
					sparseEntries.add(0);
				}
				int nodeCounter = 0;
				// the following elements represent the nodes that are involved
				// in the current trace
				for (INode<T> node : nodes) {
					++nodeCounter;
					if (trace.isInvolved(node)) {
						sparseEntries.add(nodeCounter);
					}
				}

				byte[] involvement = module.submit(sparseEntries).getResult();

				// store each trace separately
				zipModule.submit(new Pair<>(traceCount + TRACE_FILE_EXTENSION, involvement));
			}

			if (index) {
				status[0] = STATUS_SPARSE_INDEXED;
			} else {
				status[0] = STATUS_SPARSE;
			}
		} else {
			Module<Pair<String, byte[]>, byte[]> zipModule = new AddNamedByteArrayToZipFileProcessor(outputFile, false).asModule();
			// iterate through the traces
			for (ITrace<T> trace : spectra.getTraces()) {
				++traceCount;
				byte[] involvement = new byte[nodes.size() + 1];
				int byteCounter = -1;
				// the first element is a flag that marks successful traces with
				// '1'
				if (trace.isSuccessful()) {
					involvement[++byteCounter] = 1;
				} else {
					involvement[++byteCounter] = 0;
				}
				// the following elements are flags that mark the trace's
				// involvement with nodes with '1'
				for (INode<T> node : nodes) {
					if (trace.isInvolved(node)) {
						involvement[++byteCounter] = 1;
					} else {
						involvement[++byteCounter] = 0;
					}
				}

				if (compress) {
					involvement = new ByteArrayToCompressedByteArrayProcessor().submit(involvement).getResult();
				}

				// store each trace separately
				zipModule.submit(new Pair<>(traceCount + TRACE_FILE_EXTENSION, involvement));
			}

			if (compress) {
				if (index) {
					status[0] = STATUS_COMPRESSED_INDEXED;
				} else {
					status[0] = STATUS_COMPRESSED;
				}
			} else if (index) {
				status[0] = STATUS_UNCOMPRESSED_INDEXED;
			}
		}
		
		saveExecutionTraces(spectra, nodeIndexToStoreIdMap, outputFile);
	}
	
	private static class TraceFileNameSupplier implements Supplier<String> {

		int threadId = -1;
		final int traceId;
		
		public TraceFileNameSupplier(int traceId) {
			this.traceId = traceId;
		}

		@Override
		public String get() {
			return traceId + "-" + (++threadId) + EXECUTION_TRACE_FILE_EXTENSION;
		}
		
	}
	
	private static class RepMarkerFileNameSupplier implements Supplier<String> {

		int threadId = -1;
		final int traceId;
		
		public RepMarkerFileNameSupplier(int traceId) {
			this.traceId = traceId;
		}

		@Override
		public String get() {
			return traceId + "-" + (++threadId) + EXECUTION_TRACE_REPETITIONS_FILE_EXTENSION;
		}
		
	}

	private static <T> void saveExecutionTraces(ISpectra<T, ?> spectra,
			Map<Integer, Integer> nodeIndexToStoreIdMap, Path outputFile) {
		SequenceIndexer indexer = spectra.getIndexer();

		try {
			if (indexer != null) {
				// should have been done after loading all raw traces, already
//				Log.out(SpectraFileUtils.class, "Generating sequence index...");
//				try {
//					indexer.getSequences();
//				} catch (UnsupportedOperationException e) {
//					indexer.getMappedSequences();
//				}

				Log.out(SpectraFileUtils.class, "Moving execution traces...");
				ProgressTracker tracker = new ProgressTracker(false);
				int traceCount;
				// add files for the execution traces, if any
				boolean hasExecutionTraces = false;
				traceCount = 0;
				// iterate through the traces
				for (ITrace<T> trace : spectra.getTraces()) {
					++traceCount;
					tracker.track("mem: " + Runtime.getRuntime().freeMemory() + ", " + trace.getIdentifier());
					//				Runtime.getRuntime().gc();

					hasExecutionTraces |= trace.storeExecutionTracesInZipFile(outputFile, new TraceFileNameSupplier(traceCount), new RepMarkerFileNameSupplier(traceCount));

				}
				
				if (hasExecutionTraces) {
					int maxValue = 0;
					for (int nodeID : nodeIndexToStoreIdMap.values()) {
						maxValue = Math.max(maxValue, nodeID);
					}
					IntSequencesToCompressedByteArrayProcessor module2 = new IntSequencesToCompressedByteArrayProcessor(maxValue, true);

					Log.out(SpectraFileUtils.class, "Storing indexed sequences...");
					// store the referenced sequence parts

					try {
						for (int i = 0; i < indexer.getSequences().length; i++) {
							Iterator<Integer> iterator = indexer.getSequenceIterator(i);
							List<Integer> result = new ArrayList<>();
							// we have to ensure that the node IDs are based on the order of the nodes as they are stored
							while (iterator.hasNext()) {
								// this might fail (i.e., return null) in filtered spectra!?
								Integer next = iterator.next();
								Integer e = nodeIndexToStoreIdMap.get(next);
								if (e != null) {
									result.add(e);
								} else {
									// this should not happen!!
									throw new IllegalStateException("No node store index for node with id: " + next);
								}
							}
							// add next sequence to the compressed byte array
							module2.submit(result);
						}
					} catch (UnsupportedOperationException e) {
						for (int i = 0; i < indexer.getMappedSequences().length; i++) {
							List<Integer> result = new ArrayList<>(indexer.getMappedSequences()[i].length);
							// we have to ensure that the node IDs are based on the order of the nodes as they are stored
							for (int j = 0; j < indexer.getMappedSequences()[i].length; ++j) {
								// this might fail (i.e., return null) in filtered spectra!?
								Integer f = nodeIndexToStoreIdMap.get(indexer.getMappedSequences()[i][j]);
								if (f != null) {
									result.add(f);
								} else {
									// this should not happen!!
									throw new IllegalStateException(
											"No node store index for node with id: " + indexer.getMappedSequences()[i][j]);
								}
							}
							// add next sequence to the compressed byte array
							module2.submit(result);
						}
					}

					Module<Pair<String, byte[]>, byte[]> zipModule = new AddNamedByteArrayToZipFileProcessor(outputFile, false).asModule();
					// store sequences
					zipModule.submit(new Pair<>(SEQUENCE_FILE_NAME, 
							module2.getResultFromCollectedItems()));

					FileUtils.delete(outputFile.getParent().resolve(SEQ_INDEX_DIR));
					Log.out(SpectraFileUtils.class, "Stored indexed sequences!");
				} else {
					Log.out(SpectraFileUtils.class, "No execution traces!");
				}
			}
		} catch (Throwable e) {
			// anything bad happened?
			FileUtils.delete(outputFile.getParent().resolve(SEQ_INDEX_DIR));
			FileUtils.delete(outputFile);
			throw e;
		}
		
//		if (hasExecutionTraces) {
//			Log.out(SpectraFileUtils.class, "Storing indexed sequences...");
//			// store the referenced sequence parts
//			SequenceIndexer indexer = spectra.getIndexer();
//			
//			for (int i = 0; i < indexer.getSequences().length; i++) {
//				int length = indexer.getSequences()[i].length;
//				// only store if length is not zero (may happen due to filtering)
//				if (length > 0) {
//					int[] result = new int[length];
//					// we have to ensure that the node IDs are based on the order of the nodes as they are stored
//					for (int j = 0; j < indexer.getSequences()[i].length; j++) {
//						// this might fail (i.e., return null) in filtered spectra!?
//						Integer e = nodeIndexToStoreIdMap.get(indexer.getSequences()[i][j]);
//						if (e != null) {
//							result[j] = e;
//						} else {
//							// this should not happen!!
//							result[j] = -1;
//						}
//					}
//					byte[] involvement = module.submit(result).getResult();
//
//					// store each sequence separately in a designated directory
//					zipModule.submit(new Pair<>(SEQ_INDEX_DIR + File.separator + 
//							i + SEQUENCE_INDEX_FILE_EXTENSION, involvement));
//				}
//			}
//			
//			FileUtils.delete(zipOutputDirectory.resolve(SEQ_INDEX_DIR));
//			Log.out(SpectraFileUtils.class, "Stored indexed sequences!");
//		}
	}
	
	public static void storeExecutionTrace(CompressedTraceBase<Integer, ?> eTrace, 
			Path zipFilePath, String traceFileName, String repMarkerFileName) throws IOException {
		int maxStoredValue = eTrace.getMaxStoredValue();
		BufferedIntegersToCompressedByteArrayProcessor module = new BufferedIntegersToCompressedByteArrayProcessor(
				zipFilePath, traceFileName, false, maxStoredValue, true);
        for (Integer integer : eTrace.getCompressedTrace()) {
            module.submit(integer);
        }
		module.finalShutdown();
		
		if (eTrace.getRepetitionMarkers() != null) {
			BufferedIntArraysToCompressedByteArrayProcessor module2 = 
					new BufferedIntArraysToCompressedByteArrayProcessor(zipFilePath, repMarkerFileName, false, maxStoredValue, true);
			storeRepetitionMarkers(eTrace, module2);
			module2.finalShutdown();
		}
	}

//	public static byte[] storeAsByteArray(CompressedTraceBase<Integer, ?> eTrace) {
//		IntegerArraysToCompressedByteArrayProcessor module = 
//				new IntegerArraysToCompressedByteArrayProcessor(eTrace.getMaxStoredValue(), true);
//		// store the compressed execution trace (containing indices to sequences)
//		module.submit(eTrace.getCompressedTrace());
//		storeRepetitionMarkers(eTrace, module);
//		byte[] involvement = module.getResultFromCollectedItems();
//		return involvement;
//	}
	
//	private static void storeRepetitionMarkers(CompressedTraceBase<?, ?> compressedTrace,
//			IntegerArraysToCompressedByteArrayProcessor module) {
//		Map<Integer, int[]> repetitionMarkers = compressedTrace.getRepetitionMarkers();
//		if (repetitionMarkers != null) {
//			Integer[] result = new Integer[repetitionMarkers.size() * 3];
//			int i = 0;
//			for (Entry<Integer, int[]> entry : repetitionMarkers.entrySet()) {
//				result[i] = entry.getKey();
//				result[i+1] = entry.getValue()[0];
//				result[i+2] = entry.getValue()[1];
//				i += 3;
//			}
//			module.submit(result);
//			storeRepetitionMarkers(compressedTrace.getChild(), module);
//		}
//	}
	
//	public static byte[] storeCompressedTraceForRawTraceAsByteArray(CompressedTraceBase<int[], ?> rawTrace) {
//		IntArraysToCompressedByteArrayProcessor module = 
//				new IntArraysToCompressedByteArrayProcessor(rawTrace.getMaxStoredValue(), true);
//		// store the compressed trace
//		for (CloneableIterator<int[]> iterator = rawTrace.getCompressedTrace().iterator(); iterator.hasNext();) {
//			module.submit(iterator.next()); // TODO buffered saving!!
//		}
////		Log.out(SpectraFileUtils.class, " length of compressed trace: %d", rawTrace.getCompressedTrace().length);
//		byte[] involvement = module.getResultFromCollectedItems();
//		return involvement;
//	}
//	
//	public static byte[] storeRepetitionMarkersForRawTraceAsByteArray(CompressedTraceBase<int[], ?> rawTrace) {
//		IntArraysToCompressedByteArrayProcessor module = 
//				new IntArraysToCompressedByteArrayProcessor(rawTrace.getMaxStoredValue(), true);
//		// store the repetition markers
//		storeRepetitionMarkers(rawTrace, module);
//		byte[] involvement = module.getResultFromCollectedItems();
//		return involvement;
//	}

	private static void storeRepetitionMarkers(CompressedTraceBase<?, ?> compressedTrace,
			BufferedIntArraysToCompressedByteArrayProcessor module) {
		BufferedMap<int[]> repetitionMarkers = compressedTrace.getRepetitionMarkers();
		if (repetitionMarkers != null) {
			int[] result = new int[repetitionMarkers.size() * 3];
			int i = 0;
			Iterator<Entry<Integer, int[]>> entrySetIterator = repetitionMarkers.entrySetIterator();
			while (entrySetIterator.hasNext()) {
				Entry<Integer, int[]> entry = entrySetIterator.next();
				result[i] = entry.getKey();
				result[i+1] = entry.getValue()[0];
				result[i+2] = entry.getValue()[1];
				i += 3;
			}
			module.submit(result);
			storeRepetitionMarkers(compressedTrace.getChild(), module);
		}
	}
	
	public static void storeCompressedTraceForRawTraceInZipFile(CompressedTraceBase<int[], ?> rawTrace,
			Path zipFilePath, String traceFileName, String repMarkerFileName) throws IOException {
		int maxStoredValue = rawTrace.getMaxStoredValue();
		BufferedIntArraysToCompressedByteArrayProcessor module = 
				new BufferedIntArraysToCompressedByteArrayProcessor(zipFilePath, traceFileName, false, maxStoredValue, 2, true);
		// store the compressed trace
        for (int[] ints : rawTrace.getCompressedTrace()) {
            module.submit(ints);
        }
		module.finalShutdown();
		
		if (rawTrace.getRepetitionMarkers() != null) {
			BufferedIntArraysToCompressedByteArrayProcessor module2 = 
					new BufferedIntArraysToCompressedByteArrayProcessor(zipFilePath, repMarkerFileName, false, maxStoredValue, true);
			storeRepetitionMarkers(rawTrace, module2);
			module2.finalShutdown();
		}
	}
	
//	public static byte[] storeRepetitionMarkersForRawTraceInZipFile(CompressedTraceBase<int[], ?> rawTrace) {
//		IntArraysToCompressedByteArrayProcessor module = 
//				new IntArraysToCompressedByteArrayProcessor(rawTrace.getMaxStoredValue(), true);
//		// store the repetition markers
//		storeRepetitionMarkers(rawTrace, module);
//		byte[] involvement = module.getResultFromCollectedItems();
//		return involvement;
//	}

	private static <T, K extends CountTrace<T>> void saveInvolvementArrayForCountSpectra(ISpectra<T, K> spectra,
			Collection<INode<T>> nodes, boolean index, byte[] status,
			Map<Integer, Integer> nodeIndexToStoreIdMap, Path outputFile) {
		Module<Pair<String, byte[]>, byte[]> zipModule = new AddNamedByteArrayToZipFileProcessor(outputFile, false).asModule();
		IntSequenceToCompressedByteArrayProcessor module = new IntSequenceToCompressedByteArrayProcessor();
		int traceCount = 0;
		// iterate through the traces
		for (K trace : spectra.getTraces()) {
			++traceCount;
			List<Integer> traceHits = new ArrayList<>(nodes.size() + 1);
			// the first element is a flag that marks successful traces with '1'
			if (trace.isSuccessful()) {
				traceHits.add(1);
			} else {
				traceHits.add(0);
			}
			// the following elements are the hit counts
			for (INode<T> node : nodes) {
				if (trace.isInvolved(node)) {
					int hits = trace.getHits(node);
					traceHits.add(hits);
				} else {
					traceHits.add(0);
				}
			}

			byte[] involvement = module.submit(traceHits).getResult();
			// store each trace separately
			zipModule.submit(new Pair<>(traceCount + TRACE_FILE_EXTENSION, involvement));
		}

		if (index) {
			status[0] = STATUS_COMPRESSED_INDEXED_COUNT;
		} else {
			status[0] = STATUS_COMPRESSED_COUNT;
		}
		
		saveExecutionTraces(spectra, nodeIndexToStoreIdMap, outputFile);
	}

	

	public static ISpectra<SourceCodeBlock, ?> loadBlockSpectraFromZipFile(Path zipFilePath) {
		return loadSpectraFromZipFile(SourceCodeBlock.DUMMY, zipFilePath);
	}

	/**
	 * Loads a Spectra object from a zip file.
	 * @param dummy
	 * a dummy object of type T that is used for obtaining indexed identifiers
	 * @param zipFilePath
	 * the path to the zip file containing the Spectra object
	 * @return the loaded Spectra object
	 * @param <T>
	 * the type of nodes in the spectra
	 * @throws NullPointerException
	 * if dummy is null
	 */
	public static <T extends Indexable<T>> ISpectra<T, ?> loadSpectraFromZipFile(T dummy, Path zipFilePath)
			throws NullPointerException {
		ZipFileWrapper zip = new ZipFileReader().submit(zipFilePath).getResult();

		byte[] status = getStatusByte(zip);

		List<T> lineArray = getNodeIdentifiersFromZipFile(dummy, zip, status);

		return loadSpectraFromZipFile(zip, status, lineArray);
	}

	/**
	 * Loads a Spectra object from a zip file.
	 * @param dummy
	 * a dummy object of type T that is used for obtaining indexed identifiers
	 * @param zipFilePath
	 * the path to the zip file containing the Spectra object
	 * @return the loaded Spectra object
	 * @param <T>
	 * the type of nodes in the spectra
	 * @throws NullPointerException
	 * if dummy is null
	 */
	public static <T extends Indexable<T>> CountSpectra<T> loadCountSpectraFromZipFile(T dummy, Path zipFilePath)
			throws NullPointerException {
		ZipFileWrapper zip = new ZipFileReader().submit(zipFilePath).getResult();

		byte[] status = getStatusByte(zip);

		List<T> lineArray = getNodeIdentifiersFromZipFile(dummy, zip, status);

		return loadCountSpectraFromZipFile(zip, status, lineArray);
	}

	private static byte[] getStatusByte(ZipFileWrapper zip) {
		// parse the status byte (0 -> uncompressed, 1 -> compressed)
		byte[] status = zip.tryGetFromOneOf(STATUS_FILE_NAME, STATUS_FILE_INDEX);
		if (status == null) {
			Log.warn(
					SpectraFileUtils.class,
					"Unable to get compression status. (Might be an older format file.) Assuming compressed spectra.");
			status = new byte[1];
			status[0] = STATUS_COMPRESSED;
		}
		return status;
	}

	private static <T> ISpectra<T, ?> loadSpectraFromZipFile(ZipFileWrapper zip, byte[] status, List<T> lineArray) {
		return loadWithSpectraTypes(zip, status, lineArray, 
				() -> new HitSpectra<>(zip.getzipFilePath()), 
				() -> new CountSpectra<>(zip.getzipFilePath()));
	}

	private static <T> CountSpectra<T> loadCountSpectraFromZipFile(ZipFileWrapper zip, byte[] status,
			List<T> lineArray) {
		Supplier<CountSpectra<T>> countSpectraSupplier = () -> new CountSpectra<>(zip.getzipFilePath());
		return loadWithSpectraTypes(zip, status, lineArray,
				countSpectraSupplier,
				countSpectraSupplier);
	}

	private static <T, D extends ISpectra<T, ?>> D loadWithSpectraTypes(ZipFileWrapper zip, byte[] status,
			List<T> lineArray, Supplier<D> hitSpectraSupplier,
			Supplier<? extends CountSpectra<T>> countSpectraSupplier) {
		// create a new spectra
		D result;

		// parse the file containing the involvement table
		byte[] involvementTable = zip.get(INVOLVEMENT_TABLE_FILE_INDEX, false);
		if (involvementTable != null) {
			result = loadFromOldSpectraFileFormat(
					zip, involvementTable, status, lineArray, hitSpectraSupplier, countSpectraSupplier);
		} else {
			result = loadFromNewSpectraFileFormat(zip, status, lineArray, hitSpectraSupplier, countSpectraSupplier);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private static <T, D extends ISpectra<T, ?>> D loadFromOldSpectraFileFormat(ZipFileWrapper zip,
			byte[] involvementTable, byte[] status, List<T> lineArray, Supplier<D> hitSpectraSupplier,
			Supplier<? extends CountSpectra<T>> countSpectraSupplier) {
		D result;

		// get the trace identifiers
		String[] traceIdentifiers = getRawTraceIdentifiersFromZipFile(zip);

		if (isSparse(status)) {
			D spectra = hitSpectraSupplier.get();
			List<List<Integer>> involvementLists = new CompressedByteArrayToIntSequencesProcessor()
					.submit(involvementTable).getResult();

			int traceCounter = -1;
			// iterate over the lists and fill the spectra object with traces
			for (List<Integer> involvedNodes : involvementLists) {
				// the first element is always the 'successful' flag
				ITrace<T> trace = spectra.addTrace(traceIdentifiers[++traceCounter], traceCounter+1, involvedNodes.get(0) == 1);
				int nodeIndex = 1;
				int node;
				if (nodeIndex < involvedNodes.size()) {
					node = involvedNodes.get(nodeIndex);
				} else {
					node = -1;
				}
				for (int i = 0; i < lineArray.size(); ++i) {
					if (i + 1 == node) {
						trace.setInvolvement(lineArray.get(i), true);
						++nodeIndex;
						if (nodeIndex < involvedNodes.size()) {
							node = involvedNodes.get(nodeIndex);
						} else {
							node = -1;
						}
					} else {
						trace.setInvolvement(lineArray.get(i), false);
					}
				}
			}
			result = spectra;
		} else if (isCountSpectra(status)) {
			CountSpectra<T> spectra = countSpectraSupplier.get();
			List<List<Integer>> spectraData = new CompressedByteArrayToIntSequencesProcessor().submit(involvementTable)
					.getResult();

			int traceCounter = -1;
			// iterate over the lists and fill the spectra object with traces
			for (List<Integer> traceData : spectraData) {
				Iterator<Integer> iterator = traceData.iterator();
				// the first element is always the 'successful' flag
				CountTrace<T> trace = spectra.addTrace(traceIdentifiers[++traceCounter], traceCounter+1, iterator.next() == 1);

				int i = -1;
				while (iterator.hasNext()) {
					trace.setHits(lineArray.get(++i), iterator.next());
				}
			}
			result = (D) spectra;
		} else {
			D spectra = hitSpectraSupplier.get();
			// check if we have a compressed byte array at hand
			if (isCompressed(status)) {
				involvementTable = new CompressedByteArraysToByteArraysProcessor().submit(involvementTable).getResult();
			}

			int tablePosition = -1;
			int traceCounter = -1;
			// iterate over the involvement table and fill the spectra object
			// with traces
			while (tablePosition + 1 < involvementTable.length) {
				// the first element is always the 'successful' flag
				ITrace<T> trace = spectra
						.addTrace(traceIdentifiers[++traceCounter], traceCounter+1, involvementTable[++tablePosition] == 1);

                for (T t : lineArray) {
                    trace.setInvolvement(t, involvementTable[++tablePosition] == 1);
                }
			}
			result = spectra;
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static <T, D extends ISpectra<T, ?>> D loadFromNewSpectraFileFormat(ZipFileWrapper zip, byte[] status,
			List<T> lineArray, Supplier<D> hitSpectraSupplier,
			Supplier<? extends CountSpectra<T>> countSpectraSupplier) {
		D result;

		// get the trace identifiers
		String[] traceIdentifiers = getRawTraceIdentifiersFromZipFile(zip);

		if (isSparse(status)) {
			D spectra = hitSpectraSupplier.get();
			
			// add the nodes in the correct order
            for (T t : lineArray) {
                spectra.getOrCreateNode(t);
            }

			CompressedByteArrayToIntSequenceProcessor processor = new CompressedByteArrayToIntSequenceProcessor();

			int traceCounter = 0;
			// iterate over the trace files and fill the spectra object
			byte[] traceInvolvement;
			while ((traceInvolvement = zip.get((++traceCounter) + TRACE_FILE_EXTENSION, false)) != null) {
				List<Integer> involvedNodes = processor.submit(traceInvolvement).getResult();

				// the first element is always the 'successful' flag
				ITrace<T> trace = spectra.addTrace(
						traceIdentifiers[traceCounter - 1], traceCounter, involvedNodes.get(0) == 1);
				int nodeIndex = 1;
				int node;
				if (nodeIndex < involvedNodes.size()) {
					node = involvedNodes.get(nodeIndex);
				} else {
					node = -1;
				}
				for (int i = 0; i < lineArray.size(); ++i) {
					if (i + 1 == node) {
						trace.setInvolvement(i, true);
						++nodeIndex;
						if (nodeIndex < involvedNodes.size()) {
							node = involvedNodes.get(nodeIndex);
						} else {
							node = -1;
						}
					} else {
						trace.setInvolvement(i, false);
					}
				}
				
				loadExecutionTraces(zip, traceCounter, trace);
			}
			result = spectra;
		} else if (isCountSpectra(status)) {
			CountSpectra<T> spectra = countSpectraSupplier.get();

			// add the nodes in the correct order
            for (T t : lineArray) {
                spectra.getOrCreateNode(t);
            }

			CompressedByteArrayToIntSequenceProcessor processor = new CompressedByteArrayToIntSequenceProcessor();

			int traceCounter = 0;
			// iterate over the trace files and fill the spectra object
			byte[] traceInvolvement;
			while ((traceInvolvement = zip.get((++traceCounter) + TRACE_FILE_EXTENSION, false)) != null) {
				List<Integer> hits = processor.submit(traceInvolvement).getResult();

				Iterator<Integer> iterator = hits.iterator();
				// the first element is always the 'successful' flag
				CountTrace<T> trace = spectra.addTrace(
						traceIdentifiers[traceCounter - 1], traceCounter, iterator.next() == 1);

				int i = -1;
				while (iterator.hasNext()) {
					trace.setHits(++i, iterator.next());
				}
				
				loadExecutionTraces(zip, traceCounter, trace);
			}
			result = (D) spectra;
		} else {
			D spectra = hitSpectraSupplier.get();
			
			// add the nodes in the correct order
            for (T t : lineArray) {
                spectra.getOrCreateNode(t);
            }

			CompressedByteArrayToByteArrayProcessor processor = new CompressedByteArrayToByteArrayProcessor();

			int traceCounter = 0;
			// iterate over the trace files and fill the spectra object
			byte[] traceInvolvement;
			while ((traceInvolvement = zip.get((++traceCounter) + TRACE_FILE_EXTENSION, false)) != null) {

				// check if we have a compressed byte array at hand
				if (isCompressed(status)) {
					traceInvolvement = processor.submit(traceInvolvement).getResult();
				}

				// the first element is always the 'successful' flag
				ITrace<T> trace = spectra.addTrace(
						traceIdentifiers[traceCounter - 1], traceCounter, traceInvolvement[0] == 1);

				for (int i = 0; i < lineArray.size(); ++i) {
					trace.setInvolvement(i, traceInvolvement[i + 1] == 1);
				}
				
				loadExecutionTraces(zip, traceCounter, trace);
			}
			result = spectra;
		}
		
		loadSequenceIndexer(zip, result);
		return result;
	}

	public static <T> Collection<ExecutionTrace> loadExecutionTraces(ZipFileWrapper zip, int traceCounter) {
		List<ExecutionTrace> traces = new ArrayList<>(1);
		// we assume a file name like 1-2.flw, where 1 is the trace id and 2 is a thread id
		// the stored IDs have to match the IDs of the node identifiers in the line array
		int threadIndex = -1;
		while (true) {
			String file = (traceCounter) + "-" + (++threadIndex) + SpectraFileUtils.EXECUTION_TRACE_FILE_EXTENSION;
			if (zip.exists(file)) {
				String repetitionFile = (traceCounter) + "-" + (threadIndex) + SpectraFileUtils.EXECUTION_TRACE_REPETITIONS_FILE_EXTENSION;
				ExecutionTrace e = loadExecutionTraceFromZipFile(zip, file, repetitionFile);

				traces.add(e);
			} else {
				break;
			}
			
//			// load the repetition marker array
//			executionTraceThreadInvolvement = zip.get((traceCounter) + "-" + (threadIndex) 
//					+ SpectraFileUtils.EXECUTION_TRACE_REPETITIONS_FILE_EXTENSION, false);
//			if (executionTraceThreadInvolvement == null) {
//				traces.add(new ExecutionTrace(compressedTrace, new int[] {}));
//			} else {
//				int[] repetitionMarkers = execTraceProcessor.submit(executionTraceThreadInvolvement).getResult();
//				traces.add(new ExecutionTrace(compressedTrace, repetitionMarkers));
//			}
		}
		
		return traces;
	}
	
	public static <T> Collection<byte[]> loadExecutionTracesByteArrays(ZipFileWrapper zip, int traceCounter) {
		List<byte[]> traces = new ArrayList<>(1);
		// we assume a file name like 1-2.flw, where 1 is the trace id and 2 is a thread id
		// the stored IDs have to match the IDs of the node identifiers in the line array
		int threadIndex = -1;
		byte[] executionTraceThreadInvolvement;
		while ((executionTraceThreadInvolvement = zip.get((traceCounter) + "-" + (++threadIndex) 
				+ SpectraFileUtils.EXECUTION_TRACE_FILE_EXTENSION, false)) != null) {
			
			traces.add(executionTraceThreadInvolvement);
			
//			// load the repetition marker array
//			executionTraceThreadInvolvement = zip.get((traceCounter) + "-" + (threadIndex) 
//					+ SpectraFileUtils.EXECUTION_TRACE_REPETITIONS_FILE_EXTENSION, false);
//			if (executionTraceThreadInvolvement == null) {
//				traces.add(new ExecutionTrace(compressedTrace, new int[] {}));
//			} else {
//				int[] repetitionMarkers = execTraceProcessor.submit(executionTraceThreadInvolvement).getResult();
//				traces.add(new ExecutionTrace(compressedTrace, repetitionMarkers));
//			}
		}
		
		return traces;
	}
	
	public static boolean moveExecutionTraces(ZipFileWrapper zip, int traceCounter, 
			Path outputFile, Supplier<String> traceFileNameSupplier, Supplier<String> repMarkerFileNameSupplier) {
		// we assume a file name like 1-2.flw, where 1 is the trace id and 2 is a thread id
		// the stored IDs have to match the IDs of the node identifiers in the line array
		Module<Pair<String, String>, Boolean> module = new MoveNamedByteArraysBetweenZipFilesProcessor(zip.getzipFilePath(), outputFile).asModule();
		int threadIndex = -1;
		while (true) {
			String traceFile = (traceCounter) + "-" + (++threadIndex) + SpectraFileUtils.EXECUTION_TRACE_FILE_EXTENSION;
			if (zip.exists(traceFile)) {
				System.out.println("moving " + traceFile + " from " + zip.getzipFilePath() + " to " + outputFile);
				String traceFileTarget = traceFileNameSupplier.get();
				String repMarkerFileTarget = repMarkerFileNameSupplier.get();
				
				// move trace file
				boolean successful = module.submit(new Pair<>(traceFile, traceFileTarget)).getResult();
				
				if (!successful) {
					Log.abort(SpectraFileUtils.class, "Could not move trace file.");
				}
				
				String repMarkerFile = (traceCounter) + "-" + (threadIndex) + SpectraFileUtils.EXECUTION_TRACE_REPETITIONS_FILE_EXTENSION;
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

//	public static ExecutionTrace loadExecutionTraceFromByteArray(byte[] executionTraceByteArray) {
//		CompressedByteArrayToIntArraysProcessor execTraceProcessor = new CompressedByteArrayToIntArraysProcessor(true);
//		// load the compressed execution trace +  repetition markers (if any)
//		int[][] compressedTrace = execTraceProcessor.submit(executionTraceByteArray).getResult();
//		ExecutionTrace e = new ExecutionTrace(
//				Arrays.stream(compressedTrace[0]).boxed().toArray(Integer[]::new), compressedTrace, 1);
//		return e;
//	}
	
	public static ExecutionTrace loadExecutionTraceFromZipFile(ZipFileWrapper zipFileWrapper, String compressedTraceFile, String repetitionFile) {
		BufferedArrayQueue<Integer> queue = new BufferedArrayQueue<>(
				zipFileWrapper.getzipFilePath().getParent().resolve("execTraceTemp").toAbsolutePath().toFile(), UUID.randomUUID().toString(), ExecutionTraceCollector.CHUNK_SIZE);
		BufferedCompressedByteArrayToIntegerQueueProcessor execTraceProcessor = new BufferedCompressedByteArrayToIntegerQueueProcessor(zipFileWrapper, true, queue);
		// load the compressed execution trace +  repetition markers (if any)
		BufferedArrayQueue<Integer> compressedTrace = (BufferedArrayQueue<Integer>) execTraceProcessor.submit(compressedTraceFile).getResult();

		// load the repetition markers (if any)
		ExecutionTrace e;
		if (zipFileWrapper.exists(repetitionFile)) {
			BufferedArrayQueue<int[]> queueRep = new BufferedArrayQueue<>(
					zipFileWrapper.getzipFilePath().getParent().resolve("execTraceTemp").toAbsolutePath().toFile(), 
					UUID.randomUUID().toString(), 1);
			BufferedCompressedByteArrayToIntArrayQueueProcessor repProcessor = new BufferedCompressedByteArrayToIntArrayQueueProcessor(zipFileWrapper, true, queueRep);
			BufferedArrayQueue<int[]> repetitionMarkers = (BufferedArrayQueue<int[]>) repProcessor.submit(repetitionFile).getResult();
			e = new ExecutionTrace(compressedTrace, repetitionMarkers, 0);
		} else {
			e = new ExecutionTrace(compressedTrace, null, 0);
		}
		return e;
	}
	
//	public static CompressedTrace loadRawTraceFromByteArrays(byte[] compressedTraceByteArray, byte[] repetitionByteArray) {
//		CompressedByteArrayToIntArraysProcessor execTraceProcessor = new CompressedByteArrayToIntArraysProcessor(true);
//		// load the compressed execution trace +  repetition markers (if any)
//		int[][] compressedTrace = execTraceProcessor.submit(compressedTraceByteArray).getResult();
//		execTraceProcessor = new CompressedByteArrayToIntArraysProcessor(true);
//		CompressedTrace e;
//		if (repetitionByteArray != null) {
//			int[][] repetitionMarkers = execTraceProcessor.submit(repetitionByteArray).getResult();
//			e = new CompressedTrace(compressedTrace, repetitionMarkers, 0);
//		} else {
//			e = new CompressedTrace(compressedTrace, null, 0);
//		}
//		return e;
//	}
	
	public static CompressedTrace loadRawTraceFromZipFile(ZipFileWrapper zipFileWrapper, String compressedTraceFile, String repetitionFile) {
		BufferedArrayQueue<int[]> queue = new BufferedArrayQueue<>(
				zipFileWrapper.getzipFilePath().getParent().resolve("execTraceTemp").toAbsolutePath().toFile(), 
				UUID.randomUUID().toString(), ExecutionTraceCollector.CHUNK_SIZE);
		BufferedCompressedByteArrayToIntArrayQueueProcessor execTraceProcessor = new BufferedCompressedByteArrayToIntArrayQueueProcessor(zipFileWrapper, 2, true, queue);
		// load the compressed raw trace
		BufferedArrayQueue<int[]> compressedTrace = (BufferedArrayQueue<int[]>) execTraceProcessor.submit(compressedTraceFile).getResult();
		
		
		// load the repetition markers (if any)
		CompressedTrace e;
		if (zipFileWrapper.exists(repetitionFile)) {
			BufferedArrayQueue<int[]> queueRep = new BufferedArrayQueue<>(
					zipFileWrapper.getzipFilePath().getParent().resolve("execTraceTemp").toAbsolutePath().toFile(), 
					UUID.randomUUID().toString(), 1);
			BufferedCompressedByteArrayToIntArrayQueueProcessor repProcessor = new BufferedCompressedByteArrayToIntArrayQueueProcessor(zipFileWrapper, true, queueRep);
			BufferedArrayQueue<int[]> repetitionMarkers = (BufferedArrayQueue<int[]>) repProcessor.submit(repetitionFile).getResult();
			e = new CompressedTrace(compressedTrace, repetitionMarkers, 0);
		} else {
			e = new CompressedTrace(compressedTrace, null, 0);
		}
		return e;
	}
	
	private static <T> void loadExecutionTraces(ZipFileWrapper zip, int traceCounter, ITrace<T> trace) {
		// ATTENTION!! Do NOT load the execution traces at this point!
		// they will be loaded later, when they are actually accessed.
		
//		Collection<ExecutionTrace> traces = loadExecutionTraces(zip, traceCounter);
//		for (ExecutionTrace executionTrace : traces) {
//			trace.addExecutionTrace(executionTrace);
//		}
	}
	
	private static <T> void loadSequenceIndexer(ZipFileWrapper zip, ISpectra<T, ?> spectra) {
		byte[] sequencesByteArray = zip.get(SEQUENCE_FILE_NAME, false);

		if (sequencesByteArray == null) {
			Log.out(SpectraFileUtils.class, "File with sequences not found. (Probably no execution traces stored.)");
			return;
		}
		CompressedByteArrayToIntArraysProcessor execTraceProcessor = new CompressedByteArrayToIntArraysProcessor(true);
		
		int[][] list = execTraceProcessor.submit(sequencesByteArray).getResult();
		
		// set the indexer for the spectra
		int[][] sequences = new int[list.length][];
		System.arraycopy(list, 0, sequences, 0, list.length);
		spectra.setIndexer(new SimpleIndexer(sequences));
	}
	
//	private static <T> void loadSequenceIndexer(ZipFileWrapper zip, ISpectra<T, ?> spectra) {
//		List<FileHeader> headersContainingString = null;
//		try {
//			headersContainingString = zip.getFileHeadersContainingString(SEQUENCE_INDEX_FILE_EXTENSION);
//		} catch (ZipException e) {
//			Log.abort(SpectraFileUtils.class, "Zip file '%s' does not exist!", zip.getzipFilePath());
//		}
//		
//		Map<Integer, int[]> sequenceMap = new HashMap<>();
//		int maxSequenceIndex = -1;
//		
//		CompressedByteArrayToIntArrayProcessor execTraceProcessor = new CompressedByteArrayToIntArrayProcessor();
//		byte[] sequenceByteArray = null;
//		for (FileHeader fileHeader : headersContainingString) {
//			try {
//				sequenceByteArray = zip.uncheckedGet(fileHeader);
//			} catch (ZipException e) {
//				// should not happen, but oh well
//				Log.abort(SpectraFileUtils.class, "Could not load file '%s' from zip file!", fileHeader.getFileName());
//			}
//			
//			int[] sequence = execTraceProcessor.submit(sequenceByteArray).getResult();
//			
//			try {
//				String fileName = FileUtils.getFileNameWithoutExtension(fileHeader.getFileName());
//				int id = Integer.valueOf(fileName);
//				sequenceMap.put(id, sequence);
//				maxSequenceIndex = Math.max(maxSequenceIndex, id);
//			} catch (NumberFormatException e) {
//				// should not happen, but oh well
//				Log.abort(SpectraFileUtils.class, "Could not process file '%s' from zip file!", fileHeader.getFileName());
//			}
//		}
//		
//		if (maxSequenceIndex < 0) {
//			return;
//		}
//		
//		// set the indexer for the spectra
//		int[][] sequences = new int[maxSequenceIndex+1][];
//		for (int i = 0; i <= maxSequenceIndex; ++i) {
//			int[] sequence = sequenceMap.get(i);
//			if (sequence == null) {
//				sequences[i] = new int[] {};
//			} else {
//				sequences[i] = sequence;
//			}
//		}
//		spectra.setIndexer(new SimpleIndexer(sequences));
//	}

	private static boolean isCountSpectra(byte[] status) {
		return status[0] == STATUS_COMPRESSED_COUNT || status[0] == STATUS_COMPRESSED_INDEXED_COUNT;
	}

	private static boolean isCompressed(byte[] status) {
		return status[0] == STATUS_COMPRESSED || status[0] == STATUS_COMPRESSED_INDEXED
				|| status[0] == STATUS_COMPRESSED_COUNT || status[0] == STATUS_COMPRESSED_INDEXED_COUNT;
	}

	private static boolean isSparse(byte[] status) {
		return status[0] == STATUS_SPARSE || status[0] == STATUS_SPARSE_INDEXED;
	}

	private static boolean isIndexed(byte[] status) {
		return status[0] == STATUS_UNCOMPRESSED_INDEXED || status[0] == STATUS_COMPRESSED_INDEXED
				|| status[0] == STATUS_SPARSE_INDEXED || status[0] == STATUS_COMPRESSED_INDEXED_COUNT;
	}

	/**
	 * Gets a list of the identifiers from a zip file.
	 * @param dummy
	 * a dummy object of type T that is used for obtaining indexed identifiers
	 * @param zipFilePath
	 * the path to the zip file containing the Spectra object
	 * @return array of node identifiers
	 * @param <T>
	 * the type of nodes in the spectra
	 */
	public static <T extends Indexable<T>> List<T> getNodeIdentifiersFromSpectraFile(T dummy, Path zipFilePath) {
		ZipFileWrapper zip = new ZipFileReader().submit(zipFilePath).getResult();

		byte[] status = getStatusByte(zip);

		return getNodeIdentifiersFromZipFile(dummy, zip, status);
	}

	private static <T extends Indexable<T>> List<T> getNodeIdentifiersFromZipFile(T dummy, ZipFileWrapper zip,
			byte[] status) throws NullPointerException {
		Objects.requireNonNull(dummy);
		String[] rawIdentifiers = getRawNodeIdentifiersFromZipFile(zip);

		List<T> identifiers = new ArrayList<>(rawIdentifiers.length);
		if (isIndexed(status)) {
			// parse the file containing the identifier names
			byte[] bytes = Objects
					.requireNonNull(zip.tryGetFromOneOf(INDEX_FILE_NAME, INDEX_FILE_INDEX), "Index file not found.");
			String[] identifierNames = new String(bytes).split(IDENTIFIER_DELIMITER);
			Map<Integer, String> map = new HashMap<>();
			int index = 0;
			for (String identifier : identifierNames) {
				map.put(index++, identifier);
			}

            for (String rawIdentifier : rawIdentifiers) {
                identifiers.add(dummy.getOriginalFromIndexedIdentifier(rawIdentifier, map));
                // Log.out(SpectraUtils.class, lineArray[i].toString());
            }
		} else {
            for (String rawIdentifier : rawIdentifiers) {
                identifiers.add(dummy.getFromString(rawIdentifier));
                // Log.out(SpectraUtils.class, lineArray[i].toString());
            }
		}

		return identifiers;
	}

	/**
	 * Loads a Spectra object from a zip file.
	 * @param zipFilePath
	 * the path to the zip file containing the Spectra object
	 * @return the loaded Spectra object
	 */
	public static ISpectra<String, ?> loadStringSpectraFromZipFile(Path zipFilePath) {
		ZipFileWrapper zip = new ZipFileReader().submit(zipFilePath).getResult();

		byte[] status = getStatusByte(zip);

		List<String> identifiers = getIdentifiersFromZipFile(zip);

		return loadSpectraFromZipFile(zip, status, identifiers);
	}

	private static List<String> getIdentifiersFromZipFile(ZipFileWrapper zip) {
		// parse the file containing the (possibly indexed) identifiers
		String[] rawIdentifiers = getRawNodeIdentifiersFromZipFile(zip);

		List<String> lineArray = new ArrayList<>(rawIdentifiers.length);
		Collections.addAll(lineArray, rawIdentifiers);

		return lineArray;
	}

	private static String[] getRawNodeIdentifiersFromZipFile(ZipFileWrapper zip) {
		byte[] bytes = Objects.requireNonNull(
				zip.tryGetFromOneOf(NODE_IDENTIFIER_FILE_NAME, NODE_IDENTIFIER_FILE_INDEX),
				"Node identifier names file not found.");
		String[] split = new String(bytes).split(IDENTIFIER_DELIMITER);
		if (split.length == 1 && split[0].isEmpty()) {
			return new String[0];
		} else {
			return split;
		}
	}

	private static String[] getRawTraceIdentifiersFromZipFile(ZipFileWrapper zip) {
		byte[] bytes = Objects.requireNonNull(
				zip.tryGetFromOneOf(TRACE_IDENTIFIER_FILE_NAME, TRACE_IDENTIFIER_FILE_INDEX),
				"Trace identifier names file not found.");
		String[] split = new String(bytes).split(IDENTIFIER_DELIMITER);
		if (split.length == 1 && split[0].isEmpty()) {
			return new String[0];
		} else {
			return split;
		}
	}

	/**
	 * Gets a list of the raw identifiers from a zip file.
	 * @param zipFilePath
	 * the path to the zip file containing the Spectra object
	 * @return a list of identifiers as Strings
	 */
	public static List<String> getIdentifiersFromSpectraFile(Path zipFilePath) {
		ZipFileWrapper zip = new ZipFileReader().submit(zipFilePath).getResult();

		return getIdentifiersFromZipFile(zip);
	}

	public static void saveBlockSpectraToCsvFile(ISpectra<SourceCodeBlock, ?> spectra, Path output,
			boolean biclusterFormat, boolean shortened) {
		saveSpectraToCsvFile(SourceCodeBlock.DUMMY, spectra, output, biclusterFormat, shortened);
	}

	/**
	 * Saves a Spectra object to hard drive as a matrix.
	 * @param dummy
	 * a dummy object of type T that is used for obtaining indexed identifiers;
	 * if the dummy is null, then no index can be created and the result is
	 * equal to calling the non-indexable version of this method
	 * @param spectra
	 * the Spectra object to save
	 * @param output
	 * the output path to the zip file to be created
	 * @param biclusterFormat
	 * whether to use a special bicluster format
	 * @param shortened
	 * whether to use short identifiers
	 * @param <T>
	 * the type of nodes in the spectra
	 */
	public static <T extends Comparable<T> & Shortened & Indexable<T>> void saveSpectraToCsvFile(T dummy,
			ISpectra<T, ?> spectra, Path output, boolean biclusterFormat, boolean shortened) {
		if (spectra.getTraces().size() == 0 || spectra.getNodes().size() == 0) {
			Log.err(SpectraFileUtils.class, "Can not save empty spectra...");
			return;
		}

		Collection<? extends ITrace<T>> failingTraces = spectra.getFailingTraces();
		Collection<? extends ITrace<T>> successfulTraces = spectra.getSuccessfulTraces();
		int arraySize = failingTraces.size() + successfulTraces.size() + 1;

		Pipe<String, String> fileWriterPipe = new StringsToFileWriter<String>(output, true).asPipe();

		List<INode<T>> nodes = new ArrayList<>(spectra.getNodes());
		nodes.sort(comparing(INode::getIdentifier));

		for (INode<T> node : nodes) {
			String[] row = new String[arraySize];
			int count = 0;
			row[count] = shortened ? node.getIdentifier().getShortIdentifier() : node.getIdentifier().toString();
			++count;
			for (ITrace<T> trace : failingTraces) {
				if (trace.isInvolved(node)) {
					row[count] = biclusterFormat ? "3" : "1";
				} else {
					row[count] = biclusterFormat ? "2" : "0";
				}
				++count;
			}
			for (ITrace<T> trace : successfulTraces) {
				if (trace.isInvolved(node)) {
					row[count] = "1";
				} else {
					row[count] = "0";
				}
				++count;
			}
			fileWriterPipe.submit(CSVUtils.toCsvLine(row));
		}

		if (!biclusterFormat) {
			String[] row = new String[arraySize];
			int count = 0;
			row[count] = "";
			++count;
			for (@SuppressWarnings("unused")
			ITrace<T> trace : failingTraces) {
				row[count] = "fail";
				++count;
			}
			for (@SuppressWarnings("unused")
			ITrace<T> trace : successfulTraces) {
				row[count] = "successful";
				++count;
			}
			fileWriterPipe.submit(CSVUtils.toCsvLine(row));
		}

		fileWriterPipe.shutdown();
	}
	
	/**
	 * Saves a count Spectra object to hard drive as a matrix.
	 * @param dummy
	 * a dummy object of type T that is used for obtaining indexed identifiers;
	 * if the dummy is null, then no index can be created and the result is
	 * equal to calling the non-indexable version of this method
	 * @param spectra
	 * the Spectra object to save
	 * @param output
	 * the output path to the zip file to be created
	 * @param shortened
	 * whether to use short identifiers
	 * @param <T>
	 * the type of nodes in the spectra
	 */
	public static <T extends Comparable<T> & Shortened & Indexable<T>> void saveCountSpectraToCsvFile(T dummy,
			ISpectra<T, ? extends CountTrace<T>> spectra, Path output, boolean shortened) {
		if (spectra.getTraces().size() == 0 || spectra.getNodes().size() == 0) {
			Log.err(SpectraFileUtils.class, "Can not save empty spectra...");
			return;
		}

		Collection<? extends CountTrace<T>> failingTraces = spectra.getFailingTraces();
		Collection<? extends CountTrace<T>> successfulTraces = spectra.getSuccessfulTraces();
		int arraySize = failingTraces.size() + successfulTraces.size() + 1;

		Pipe<String, String> fileWriterPipe = new StringsToFileWriter<String>(output, true).asPipe();

		List<INode<T>> nodes = new ArrayList<>(spectra.getNodes());
		nodes.sort(comparing(INode::getIdentifier));

		for (INode<T> node : nodes) {
			String[] row = new String[arraySize];
			int count = 0;
			row[count] = shortened ? node.getIdentifier().getShortIdentifier() : node.getIdentifier().toString();
			++count;
			for (CountTrace<T> trace : failingTraces) {
				row[count] = String.valueOf(trace.getHits(node));
				++count;
			}
			for (CountTrace<T> trace : successfulTraces) {
				row[count] = String.valueOf(trace.getHits(node));
				++count;
			}
			fileWriterPipe.submit(CSVUtils.toCsvLine(row));
		}

		String[] row = new String[arraySize];
		int count = 0;
		row[count] = "";
		++count;
		for (@SuppressWarnings("unused")
		ITrace<T> trace : failingTraces) {
			row[count] = "fail";
			++count;
		}
		for (@SuppressWarnings("unused")
		ITrace<T> trace : successfulTraces) {
			row[count] = "successful";
			++count;
		}
		fileWriterPipe.submit(CSVUtils.toCsvLine(row));

		fileWriterPipe.shutdown();
	}

	public static <T extends Indexable<T>> String[] getNodeInvolvements(Collection<INode<T>> nodes, int arraySize,
			ITrace<T> trace, String ifInvolved, String ifNotInvolved) {
		String[] nodeInvolvements = new String[arraySize];
		int count = 0;
		for (INode<T> node : nodes) {
			nodeInvolvements[count] = trace.isInvolved(node) ? ifInvolved : ifNotInvolved;
			++count;
		}
		return nodeInvolvements;
	}

	/**
	 * Loads a Spectra object from a BugMiner coverage zip file.
	 * @param zipFilePath
	 * the path to the BugMiner coverage zip file
	 * @return the loaded Spectra object
	 * @throws IOException
	 * in case of not being able to read the zip file
	 */
	public static ISpectra<String, ?> loadSpectraFromBugMinerZipFile(Path zipFilePath) throws IOException {
		// read single bug
		final CoverageReport report = new CoverageReportDeserializer().deserialize(zipFilePath);

		return convertCoverageReportToSpectra(report);
	}

	/**
	 * Converts a CoverageReport object to a Spectra object.
	 * @param report
	 * the coverage report to convert
	 * @return a corresponding spectra
	 */
	public static ISpectra<String, ?> convertCoverageReportToSpectra(CoverageReport report) {
		// create a new spectra
		HitSpectra<String> spectra = new HitSpectra<>(null);

		int traceCount = 0;
		// iterate through the test cases
		for (final TestCase testCase : report.getTestCases()) {
			ITrace<String> trace = spectra.addTrace("_", ++traceCount, testCase.isPassed());
			// iterate through the source files
			for (final SourceCodeFile file : report.getFiles()) {
				// get coverage for source file and test case
				final FileCoverage coverage = report.getCoverage(testCase, file);
				for (final int line : file.getLineNumbers()) {
					trace.setInvolvement(
							file.getFileName() + SourceCodeBlock.IDENTIFIER_SEPARATOR_CHAR + line,
							coverage.isCovered(line));
				}
			}
		}

		return spectra;
	}

	/**
	 * Loads a Spectra object from a BugMiner coverage zip file.
	 * @param zipFilePath
	 * the path to the BugMiner coverage zip file
	 * @return the loaded Spectra object
	 * @throws IOException
	 * in case of not being able to read the zip file
	 */
	public static ISpectra<SourceCodeBlock, HitTrace<SourceCodeBlock>> loadSpectraFromBugMinerZipFile2(Path zipFilePath)
			throws IOException {
		// read single bug
		final CoverageReport report = new CoverageReportDeserializer().deserialize(zipFilePath);

		return convertCoverageReportToSpectra2(report);
	}

	/**
	 * Converts a CoverageReport object to a Spectra object.
	 * @param report
	 * the coverage report to convert
	 * @return a corresponding spectra
	 */
	public static ISpectra<SourceCodeBlock, HitTrace<SourceCodeBlock>> convertCoverageReportToSpectra2(
			CoverageReport report) {
		// create a new spectra
		HitSpectra<SourceCodeBlock> spectra = new HitSpectra<>(null);

		int traceCount = 0;
		// iterate through the test cases
		for (final TestCase testCase : report.getTestCases()) {
			ITrace<SourceCodeBlock> trace = spectra.addTrace(
					String.valueOf(++traceCount), traceCount, testCase.isPassed());
			// iterate through the source files
			for (final SourceCodeFile file : report.getFiles()) {
				// get coverage for source file and test case
				final FileCoverage coverage = report.getCoverage(testCase, file);
				for (final int line : file.getLineNumbers()) {
					// TODO: no package and method name given here...
					trace.setInvolvement(
							new SourceCodeBlock("_", file.getFileName(), "_", line), coverage.isCovered(line));
				}
			}
		}

		return spectra;
	}

	/**
	 * Saves a Spectra object to hard drive.
	 * @param spectra
	 * the Spectra object to save
	 * @param output
	 * the output path to the zip file to be created
	 * @param <T>
	 * the type of spectra
	 */
	public static <T extends CharSequence> void saveSpectraToBugMinerZipFile(ISpectra<T, ?> spectra, Path output) {
		CoverageReport report = convertSpectraToReport(spectra);

		// serialize the report
		CoverageReportSerializer serializer = new CoverageReportSerializer();
		try {
			serializer.serialize(report, output);
		} catch (IOException e) {
			Log.abort(SpectraFileUtils.class, e, "Could not save serialized spectra to '%s'.", output);
		}
	}

	/**
	 * Converts a Spectra object to a BugMiner CoverageReport object.
	 * @param spectra
	 * the Spectra object to convert
	 * @return a respective coverage report
	 * @param <T>
	 * the type of nodes
	 */
	public static <T extends CharSequence> CoverageReport convertSpectraToReport(ISpectra<T, ?> spectra) {
		Map<String, List<INode<T>>> nodesForFile = new HashMap<>();
		Map<INode<T>, Integer> linesOfNodes = new HashMap<>();

		// iterate over all nodes
		for (INode<T> node : spectra.getNodes()) {
			String identifier = node.getIdentifier().toString();
			int pos = identifier.indexOf(':');
			if (pos == -1) {
				throw new IllegalStateException("Can not derive file from identifier '" + identifier + "'.");
			}
			nodesForFile.computeIfAbsent(identifier.substring(0, pos), k -> new ArrayList<>()).add(node);
			linesOfNodes.put(node, Integer.valueOf(identifier.substring(pos + 1)));
		}

		List<SourceCodeFile> sourceCodeFiles = new ArrayList<>(nodesForFile.entrySet().size());
		// iterate over all node lists (one for each file) and collect the line
		// numbers into arrays
		for (Entry<String, List<INode<T>>> entry : nodesForFile.entrySet()) {
			int[] lineNumbers = new int[entry.getValue().size()];
			List<INode<T>> nodes = entry.getValue();
			for (int i = 0; i < nodes.size(); ++i) {
				lineNumbers[i] = linesOfNodes.get(nodes.get(i));
			}

			// add a source file object
			sourceCodeFiles.add(new SourceCodeFile(entry.getKey(), lineNumbers));
		}

		Map<ITrace<T>, TestCase> testCaseMap = new HashMap<>();
		List<TestCase> testCases = new ArrayList<>(spectra.getTraces().size());

		// iterate over all traces and produce corresponding test cases
		for (ITrace<T> trace : spectra.getTraces()) {
			TestCase testCase = new TestCase(trace.toString(), trace.isSuccessful());
			testCaseMap.put(trace, testCase);
			testCases.add(testCase);
		}

		CoverageReport report = new CoverageReport(sourceCodeFiles, testCases);

		// iterate over all traces
		for (ITrace<T> trace : spectra.getTraces()) {
			// iterate over all files
			for (SourceCodeFile file : sourceCodeFiles) {
				// compute coverage for each file for each trace
				FileCoverage coverage = new FileCoverage();
				for (INode<T> node : nodesForFile.get(file.getFileName())) {
					if (trace.isInvolved(node)) {
						coverage.put(linesOfNodes.get(node), true);
					} else {
						coverage.put(linesOfNodes.get(node), false);
					}
				}
				// add the coverage to the report
				report.setCoverage(testCaseMap.get(trace), file, coverage);
			}
		}

		return report;
	}

}
