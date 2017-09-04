package se.de.hu_berlin.informatik.stardust.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

import de.unistuttgart.iste.rss.bugminer.coverage.CoverageReport;
import de.unistuttgart.iste.rss.bugminer.coverage.CoverageReportDeserializer;
import de.unistuttgart.iste.rss.bugminer.coverage.CoverageReportSerializer;
import de.unistuttgart.iste.rss.bugminer.coverage.FileCoverage;
import de.unistuttgart.iste.rss.bugminer.coverage.SourceCodeFile;
import de.unistuttgart.iste.rss.bugminer.coverage.TestCase;
import net.lingala.zip4j.exception.ZipException;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.IMutableTrace;
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.ITrace;
import se.de.hu_berlin.informatik.stardust.spectra.Spectra;
import se.de.hu_berlin.informatik.utils.compression.ByteArraysToCompressedByteArrayProcessor;
import se.de.hu_berlin.informatik.utils.compression.CompressedByteArrayToByteArrayProcessor;
import se.de.hu_berlin.informatik.utils.compression.CompressedByteArrayToIntSequenceProcessor;
import se.de.hu_berlin.informatik.utils.compression.IntSequencesToCompressedByteArrayProcessor;
import se.de.hu_berlin.informatik.utils.compression.ziputils.AddByteArrayToZipFileProcessor;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileReader;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileWrapper;
import se.de.hu_berlin.informatik.utils.files.csv.CSVUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.processors.basics.StringsToFileWriter;
import se.de.hu_berlin.informatik.utils.processors.sockets.module.Module;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.Pipe;

/**
 * Helper class to save and load spectra objects.
 * 
 * @author Simon
 *
 */
public class SpectraFileUtils {

	private static final String IDENTIFIER_DELIMITER = "\t";

	private static final int NODE_IDENTIFIER_FILE_INDEX = 0;
	private static final int TRACE_IDENTIFIER_FILE_INDEX = 1;
	private static final int INVOLVEMENT_TABLE_FILE_INDEX = 2;
	private static final int STATUS_FILE_INDEX = 3;
	private static final int INDEX_FILE_INDEX = 4;

	public static final byte STATUS_UNCOMPRESSED = 0;
	public static final byte STATUS_COMPRESSED = 1;
	public static final byte STATUS_UNCOMPRESSED_INDEXED = 2;
	public static final byte STATUS_COMPRESSED_INDEXED = 3;
	public static final byte STATUS_SPARSE = 4;
	public static final byte STATUS_SPARSE_INDEXED = 5;

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
	public static <T> void saveSpectraToZipFile(ISpectra<T> spectra, Path output, boolean compress, boolean sparse) {
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
			buffer.append(node.getIdentifier() + IDENTIFIER_DELIMITER);
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
			buffer.append(trace.getIdentifier() + IDENTIFIER_DELIMITER);
		}
		if (buffer.length() > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		return buffer.toString();
	}

	public static void saveBlockSpectraToZipFile(ISpectra<SourceCodeBlock> spectra, Path output, boolean compress,
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
	public static <T extends Indexable<T>> void saveSpectraToZipFile(T dummy, ISpectra<T> spectra, Path output,
			boolean compress, boolean sparse, boolean index) {
		if (dummy == null) {
			saveSpectraToZipFile(spectra, output, compress, sparse);
			return;
		}

//		if (spectra.getTraces().size() == 0 || spectra.getNodes().size() == 0) {
//			Log.err(SpectraFileUtils.class, "Can not save empty spectra...");
//			return;
//		}

		Collection<INode<T>> nodes = spectra.getNodes();

		Map<String, Integer> map = new HashMap<>();

		String nodeIdentifiers = getIdentifierString(dummy, index, nodes, map);
		String traceIdentifiers = getTraceIdentifierListString(spectra.getTraces());

		saveSpectraToZipFile(spectra, output, compress, sparse, index, nodes, map, nodeIdentifiers, traceIdentifiers);
	}

	private static <T> void saveSpectraToZipFile(ISpectra<T> spectra, Path output, boolean compress, boolean sparse,
			boolean index, Collection<INode<T>> nodes, Map<String, Integer> map, String nodeIdentifiers,
			String traceIdentifiers) {

		byte[] status = { STATUS_UNCOMPRESSED };

		byte[] involvement = null;

		if (sparse) {
			// needs at least max vaule of 2
			IntSequencesToCompressedByteArrayProcessor module = new IntSequencesToCompressedByteArrayProcessor(
					nodes.size() > 2 ? nodes.size() : 2);

			// iterate through the traces
			for (ITrace<T> trace : spectra.getTraces()) {
				List<Integer> sparseEntries = new ArrayList<>(trace.involvedNodesCount() + 1);
				// the first element is a flag that marks successful traces with
				// '1'
				if (trace.isSuccessful()) {
					sparseEntries.add(1);
				} else {
					// cannot use 0 here, as it is used as a delimiter
					sparseEntries.add(2);
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
				module.submit(sparseEntries);
			}

			involvement = module.getResultFromCollectedItems();

			if (index) {
				status[0] = STATUS_SPARSE_INDEXED;
			} else {
				status[0] = STATUS_SPARSE;
			}
		}

		// if not using sparse format or if the sparse format results in a
		// higher array size than the normal format, use the normal format
		if (involvement == null
				|| involvement.length > spectra.getTraces().size() * (spectra.getNodes().size() + 1) / 8) {

			// compress the array if we previously computed a sparse version
			if (involvement != null) {
				compress = true;
			}

			involvement = new byte[spectra.getTraces().size() * (spectra.getNodes().size() + 1)];

			int byteCounter = -1;
			// iterate through the traces
			for (ITrace<T> trace : spectra.getTraces()) {
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
			}

			if (compress) {
				involvement = new ByteArraysToCompressedByteArrayProcessor(1, nodes.size() + 1).submit(involvement)
						.getResultFromCollectedItems();
				if (index) {
					status[0] = STATUS_COMPRESSED_INDEXED;
				} else {
					status[0] = STATUS_COMPRESSED;
				}
			} else if (index) {
				status[0] = STATUS_UNCOMPRESSED_INDEXED;
			}
		}

		// now, we have a list of identifiers and the involvement table
		// so add them to the output zip file
		Module<byte[], byte[]> module = new AddByteArrayToZipFileProcessor(output, true)
				.submit(nodeIdentifiers.getBytes()) // 0.bin
				.submit(traceIdentifiers.getBytes()) // 1.bin
				.submit(involvement) // 2.bin
				.submit(status); // 3.bin

		if (index) {
			// store the actual identifier names (order is important here, too)
			StringBuilder identifierBuilder = new StringBuilder();
			List<String> identifierNames = Misc.sortByValueToKeyList(map);
			for (String identifier : identifierNames) {
				identifierBuilder.append(identifier + IDENTIFIER_DELIMITER);
			}
			if (identifierBuilder.length() > 0) {
				identifierBuilder.deleteCharAt(identifierBuilder.length() - 1);
			}

			module.submit(identifierBuilder.toString().getBytes()); // 4.bin
		}
	}

	private static <T extends Indexable<T>> String getIdentifierString(T dummy, boolean index,
			Collection<INode<T>> nodes, Map<String, Integer> map) {
		StringBuilder buffer = new StringBuilder();
		if (index) {
			// store the identifiers in indexed (shorter) format (order is
			// important)
			for (INode<T> node : nodes) {
				buffer.append(dummy.getIndexedIdentifier(node.getIdentifier(), map) + IDENTIFIER_DELIMITER);
			}
		} else {
			// store the identifiers (order is important)
			for (INode<T> node : nodes) {
				buffer.append(node.getIdentifier() + IDENTIFIER_DELIMITER);
			}
		}
		if (buffer.length() > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		return buffer.toString();
	}

	public static ISpectra<SourceCodeBlock> loadBlockSpectraFromZipFile(Path zipFilePath) {
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
	public static <T extends Indexable<T>> ISpectra<T> loadSpectraFromZipFile(T dummy, Path zipFilePath)
			throws NullPointerException {
		ZipFileWrapper zip = new ZipFileReader().submit(zipFilePath).getResult();

		// parse the status byte (0 -> uncompressed, 1 -> compressed)
		byte[] status;
		try {
			status = zip.uncheckedGet(STATUS_FILE_INDEX);
		} catch (ZipException e) {
			Log.warn(
					SpectraFileUtils.class,
					"Unable to get compression status. (Might be an older format file.) Assuming compressed spectra.");
			status = new byte[1];
			status[0] = STATUS_COMPRESSED;
		}

		List<T> lineArray = getNodeIdentifiersFromZipFile(dummy, zip, status);

		return loadSpectraFromZipFile(zip, status, lineArray);
	}

	private static <T> ISpectra<T> loadSpectraFromZipFile(ZipFileWrapper zip, byte[] status, List<T> lineArray) {
		// create a new spectra
		ISpectra<T> spectra = new Spectra<>();

		// parse the file containing the involvement table
		byte[] involvementTable = zip.get(INVOLVEMENT_TABLE_FILE_INDEX);
		// get the trace identifiers
		String[] traceIdentifiers = getRawTraceIdentifiersFromZipFile(zip);

		if (isSparse(status)) {
			List<List<Integer>> involvementLists = new CompressedByteArrayToIntSequenceProcessor()
					.submit(involvementTable).getResult();

			int traceCounter = -1;
			// iterate over the lists and fill the spectra object with traces
			for (List<Integer> involvedNodes : involvementLists) {
				// the first element is always the 'successful' flag
				IMutableTrace<T> trace = spectra.addTrace(traceIdentifiers[++traceCounter], involvedNodes.get(0) == 1);
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
		} else {
			// check if we have a compressed byte array at hand
			if (isCompressed(status)) {
				involvementTable = new CompressedByteArrayToByteArrayProcessor().submit(involvementTable).getResult();
			}

			int tablePosition = -1;
			int traceCounter = -1;
			// iterate over the involvement table and fill the spectra object
			// with traces
			while (tablePosition + 1 < involvementTable.length) {
				// the first element is always the 'successful' flag
				IMutableTrace<T> trace = spectra
						.addTrace(traceIdentifiers[++traceCounter], involvementTable[++tablePosition] == 1);

				for (int i = 0; i < lineArray.size(); ++i) {
					trace.setInvolvement(lineArray.get(i), involvementTable[++tablePosition] == 1);
				}
			}
		}

		return spectra;
	}

	private static boolean isCompressed(byte[] status) {
		return status[0] == STATUS_COMPRESSED || status[0] == STATUS_COMPRESSED_INDEXED;
	}

	private static boolean isSparse(byte[] status) {
		return status[0] == STATUS_SPARSE || status[0] == STATUS_SPARSE_INDEXED;
	}

	private static boolean isIndexed(byte[] status) {
		return status[0] == STATUS_UNCOMPRESSED_INDEXED || status[0] == STATUS_COMPRESSED_INDEXED
				|| status[0] == STATUS_SPARSE_INDEXED;
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

		// parse the status byte (0 -> uncompressed, 1 -> compressed)
		byte[] status;
		try {
			status = zip.uncheckedGet(STATUS_FILE_INDEX);
		} catch (ZipException e) {
			Log.warn(
					SpectraFileUtils.class,
					"Unable to get compression status. (Might be an older format file.) Assuming compressed spectra.");
			status = new byte[1];
			status[0] = STATUS_COMPRESSED;
		}

		return getNodeIdentifiersFromZipFile(dummy, zip, status);
	}

	private static <T extends Indexable<T>> List<T> getNodeIdentifiersFromZipFile(T dummy, ZipFileWrapper zip,
			byte[] status) throws NullPointerException {
		Objects.requireNonNull(dummy);
		String[] rawIdentifiers = getRawNodeIdentifiersFromZipFile(zip);

		List<T> identifiers = new ArrayList<>(rawIdentifiers.length);
		if (isIndexed(status)) {
			// parse the file containing the identifier names
			String[] identifierNames = new String(zip.get(INDEX_FILE_INDEX)).split(IDENTIFIER_DELIMITER);
			Map<Integer, String> map = new HashMap<>();
			int index = 0;
			for (String identifier : identifierNames) {
				map.put(index++, identifier);
			}

			for (int i = 0; i < rawIdentifiers.length; ++i) {
				identifiers.add(dummy.getOriginalFromIndexedIdentifier(rawIdentifiers[i], map));
				// Log.out(SpectraUtils.class, lineArray[i].toString());
			}
		} else {
			for (int i = 0; i < rawIdentifiers.length; ++i) {
				identifiers.add(dummy.getFromString(rawIdentifiers[i]));
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
	public static ISpectra<String> loadStringSpectraFromZipFile(Path zipFilePath) {
		ZipFileWrapper zip = new ZipFileReader().submit(zipFilePath).getResult();

		// parse the status byte (0 -> uncompressed, 1 -> compressed)
		byte[] status;
		try {
			status = zip.uncheckedGet(STATUS_FILE_INDEX);
		} catch (ZipException e) {
			Log.warn(
					SpectraFileUtils.class,
					"Unable to get compression status. (Might be an older format file.) Assuming compressed spectra.");
			status = new byte[1];
			status[0] = STATUS_COMPRESSED;
		}

		List<String> identifiers = getIdentifiersFromZipFile(zip);

		return loadSpectraFromZipFile(zip, status, identifiers);
	}

	private static List<String> getIdentifiersFromZipFile(ZipFileWrapper zip) {
		// parse the file containing the (possibly indexed) identifiers
		String[] rawIdentifiers = getRawNodeIdentifiersFromZipFile(zip);

		List<String> lineArray = new ArrayList<>(rawIdentifiers.length);
		for (int i = 0; i < rawIdentifiers.length; ++i) {
			lineArray.add(rawIdentifiers[i]);
			// Log.out(SpectraUtils.class, lineArray[i].toString());
		}

		return lineArray;
	}

	private static String[] getRawNodeIdentifiersFromZipFile(ZipFileWrapper zip) {
		String[] split = new String(zip.get(NODE_IDENTIFIER_FILE_INDEX)).split(IDENTIFIER_DELIMITER);
		if (split.length == 1 && split[0].equals("")) {
			return new String[0];
		} else {
			return split;
		}
	}

	private static String[] getRawTraceIdentifiersFromZipFile(ZipFileWrapper zip) {
		String[] split = new String(zip.get(TRACE_IDENTIFIER_FILE_INDEX)).split(IDENTIFIER_DELIMITER);
		if (split.length == 1 && split[0].equals("")) {
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

	public static void saveBlockSpectraToCsvFile(ISpectra<SourceCodeBlock> spectra, Path output,
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
	public static <T extends Comparable<T> & Shortened & Indexable<T>> void saveSpectraToCsvFile(T dummy, ISpectra<T> spectra,
			Path output, boolean biclusterFormat, boolean shortened) {
		if (spectra.getTraces().size() == 0 || spectra.getNodes().size() == 0) {
			Log.err(SpectraFileUtils.class, "Can not save empty spectra...");
			return;
		}

		Collection<ITrace<T>> failingTraces = spectra.getFailingTraces();
		Collection<ITrace<T>> successfulTraces = spectra.getSuccessfulTraces();
		int arraySize = failingTraces.size() + successfulTraces.size() + 1;

		Pipe<String, String> fileWriterPipe = new StringsToFileWriter<String>(output, true).asPipe();
		
		List<INode<T>> nodes = new ArrayList<>(spectra.getNodes());
		Collections.sort(nodes, new Comparator<INode<T>>() {
			@Override
			public int compare(INode<T> o1, INode<T> o2) {
				return o1.getIdentifier().compareTo(o2.getIdentifier());
			}
		});
		
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
			for (@SuppressWarnings("unused") ITrace<T> trace : failingTraces) {
				row[count] = "fail";
				++count;
			}
			for (@SuppressWarnings("unused") ITrace<T> trace : successfulTraces) {
				row[count] = "successful";
				++count;
			}
			fileWriterPipe.submit(CSVUtils.toCsvLine(row));
		}
		
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
	public static ISpectra<String> loadSpectraFromBugMinerZipFile(Path zipFilePath) throws IOException {
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
	public static ISpectra<String> convertCoverageReportToSpectra(CoverageReport report) {
		// create a new spectra
		Spectra<String> spectra = new Spectra<>();

		// iterate through the test cases
		for (final TestCase testCase : report.getTestCases()) {
			IMutableTrace<String> trace = spectra.addTrace("_", testCase.isPassed());
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
	public static ISpectra<SourceCodeBlock> loadSpectraFromBugMinerZipFile2(Path zipFilePath) throws IOException {
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
	public static ISpectra<SourceCodeBlock> convertCoverageReportToSpectra2(CoverageReport report) {
		// create a new spectra
		Spectra<SourceCodeBlock> spectra = new Spectra<>();

		// iterate through the test cases
		for (final TestCase testCase : report.getTestCases()) {
			IMutableTrace<SourceCodeBlock> trace = spectra.addTrace("_", testCase.isPassed());
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
	public static <T extends CharSequence> void saveSpectraToBugMinerZipFile(ISpectra<T> spectra, Path output) {
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
	public static <T extends CharSequence> CoverageReport convertSpectraToReport(ISpectra<T> spectra) {
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
