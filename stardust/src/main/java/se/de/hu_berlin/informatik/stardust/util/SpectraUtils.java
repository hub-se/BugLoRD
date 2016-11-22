package se.de.hu_berlin.informatik.stardust.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import se.de.hu_berlin.informatik.utils.compression.ByteArrayToCompressedByteArrayModule;
import se.de.hu_berlin.informatik.utils.compression.CompressedByteArrayToByteArrayModule;
import se.de.hu_berlin.informatik.utils.compression.ziputils.AddByteArrayToZipFileModule;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ReadZipFileModule;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * Helper class to save and load spectra objects.
 * 
 * @author Simon
 *
 */
public class SpectraUtils {

	private static final String IDENTIFIER_DELIMITER = "\t";
	
	public static final byte STATUS_NOTHING = 0;
	public static final byte STATUS_COMPRESSED = 1;
	public static final byte STATUS_INDEXED = 2;
	public static final byte STATUS_COMPRESSED_INDEXED = 3;
	
	/**
	 * Saves a Spectra object to hard drive.
	 * @param spectra
	 * the Spectra object to save
	 * @param output
	 * the output path to the zip file to be created
	 * @param compress
	 * whether or not to use an additional compression procedure apart from zipping
	 * @param <T>
	 * the type of spectra
	 */
	public static <T> void saveSpectraToZipFile(ISpectra<T> spectra, Path output, boolean compress) {
		StringBuilder buffer = new StringBuilder();
		byte[] involvement = new byte[spectra.getTraces().size()*(spectra.getNodes().size()+1)];
		
		if (involvement.length == 0) {
			Log.err(SpectraUtils.class, "Can not save empty spectra...");
			return;
		}	
		
		List<INode<T>> nodes = spectra.getNodes();

		//store the identifiers (order is important)
		for (INode<T> node : nodes) {
			buffer.append(node.getIdentifier() + IDENTIFIER_DELIMITER);
		}
		if (buffer.length() > 0) {
			buffer.deleteCharAt(buffer.length()-1);
		}
		
		int byteCounter = -1;
		
		//iterate through the traces
		for (ITrace<T> trace : spectra.getTraces()) {
			//the first element is a flag that marks successful traces with '1'
			if (trace.isSuccessful()) {
				involvement[++byteCounter] = 1;
			} else {
				involvement[++byteCounter] = 0;
			}
			//the following elements are flags that mark the trace's involvement with nodes with '1'
			for (INode<T> node : nodes) {
				if (trace.isInvolved(node)) {
					involvement[++byteCounter] = 1;
				} else {
					involvement[++byteCounter] = 0;
				}
			}
		}
		
		byte[] status = { STATUS_NOTHING };
		if (compress) {
			involvement = new ByteArrayToCompressedByteArrayModule(1, nodes.size()+1).submit(involvement).getResultFromCollectedItems();
			status[0] = STATUS_COMPRESSED;
		}
		
		//now, we have a list of identifiers and the involvement table
		//so add them to the output zip file
		new AddByteArrayToZipFileModule(output, true)
		.submit(buffer.toString().getBytes()) //0.bin
		.submit(involvement) //1.bin
		.submit(status); //2.bin
	}
	
	public static void saveBlockSpectraToZipFile(ISpectra<SourceCodeBlock> spectra, Path output, boolean compress, boolean index) {
		saveBlockSpectraToZipFile(SourceCodeBlock.DUMMY, spectra, output, compress, index);
	}
	
	/**
	 * Saves a Spectra object to hard drive.
	 * @param dummy
	 * a dummy object of type T that is used for obtaining indexed identifiers
	 * @param spectra
	 * the Spectra object to save
	 * @param output
	 * the output path to the zip file to be created
	 * @param compress
	 * whether or not to use an additional compression procedure apart from zipping
	 * @param index
	 * whether to index the identifiers to minimize the needed storage space
	 * @param <T>
	 * the type of spectra
	 */
	public static <T extends Indexable<T>> void saveBlockSpectraToZipFile(T dummy, ISpectra<T> spectra, Path output, boolean compress, boolean index) {
		StringBuilder buffer = new StringBuilder();
		byte[] involvement = new byte[spectra.getTraces().size()*(spectra.getNodes().size()+1)];
		
		if (involvement.length == 0) {
			Log.err(SpectraUtils.class, "Can not save empty spectra...");
			return;
		}	
		
		List<INode<T>> nodes = spectra.getNodes();
		
		Map<String,Integer> map = new HashMap<>();

		if (index) {
			//store the identifiers in indexed (shorter) format (order is important)
			for (INode<T> node : nodes) {
				buffer.append(dummy.getIndexedIdentifier(node.getIdentifier(), map) + IDENTIFIER_DELIMITER);
			}
		} else {
			//store the identifiers (order is important)
			for (INode<T> node : nodes) {
				buffer.append(node.getIdentifier() + IDENTIFIER_DELIMITER);
			}
		}
		if (buffer.length() > 0) {
			buffer.deleteCharAt(buffer.length()-1);
		}
		
		int byteCounter = -1;
		
		//iterate through the traces
		for (ITrace<T> trace : spectra.getTraces()) {
			//the first element is a flag that marks successful traces with '1'
			if (trace.isSuccessful()) {
				involvement[++byteCounter] = 1;
			} else {
				involvement[++byteCounter] = 0;
			}
			//the following elements are flags that mark the trace's involvement with nodes with '1'
			for (INode<T> node : nodes) {
				if (trace.isInvolved(node)) {
					involvement[++byteCounter] = 1;
				} else {
					involvement[++byteCounter] = 0;
				}
			}
		}
		
		byte[] status = { STATUS_NOTHING };
		if (compress) {
			involvement = new ByteArrayToCompressedByteArrayModule(1, nodes.size()+1).submit(involvement).getResultFromCollectedItems();
			if (index) {
				status[0] = STATUS_COMPRESSED_INDEXED;
			} else {
				status[0] = STATUS_COMPRESSED;
			}
		} else if (index) {
			status[0] = STATUS_INDEXED;
		}
		
		
		//now, we have a list of identifiers and the involvement table
		//so add them to the output zip file
		AbstractModule<byte[], byte[]> module = new AddByteArrayToZipFileModule(output, true)
		.submit(buffer.toString().getBytes()) //0.bin
		.submit(involvement) //1.bin
		.submit(status); //2.bin
		
		if (index) {
			//store the actual identifier names (order is important here, too)
			StringBuilder identifierBuilder = new StringBuilder();
			List<String> identifiers = Misc.sortKeysByValueToList(map);
			for (String identifier : identifiers) {
				identifierBuilder.append(identifier + IDENTIFIER_DELIMITER);
			}
			if (identifierBuilder.length() > 0) {
				identifierBuilder.deleteCharAt(identifierBuilder.length()-1);
			}
			
			module.submit(identifierBuilder.toString().getBytes()); //3.bin
		}
	}
	
//	private static String getIndexedIdentifier(INode<SourceCodeBlock> node, Map<String,Integer> map) {
//		StringBuilder builder = new StringBuilder();
//		SourceCodeBlock block = node.getIdentifier();
//		
//		builder.append(map.computeIfAbsent(block.getPackageName(), k -> map.size()) + SourceCodeBlock.IDENTIFIER_SEPARATOR_CHAR);
//		builder.append(map.computeIfAbsent(block.getClassName(), k -> map.size()) + SourceCodeBlock.IDENTIFIER_SEPARATOR_CHAR);
//		builder.append(map.computeIfAbsent(block.getMethodName(), k -> map.size()) + SourceCodeBlock.IDENTIFIER_SEPARATOR_CHAR);
//		builder.append(block.getStartLineNumber() + SourceCodeBlock.IDENTIFIER_SEPARATOR_CHAR);
//		builder.append(block.getEndLineNumber() + SourceCodeBlock.IDENTIFIER_SEPARATOR_CHAR);
//		builder.append(block.getCoveredStatements());
//		
////		Log.out(SpectraUtils.class, builder.toString());
//		return builder.toString();
//	}
	
	public static ISpectra<SourceCodeBlock> loadBlockSpectraFromZipFile(Path zipFilePath) {
		return loadSpectraFromBlockZipFile(SourceCodeBlock.DUMMY, zipFilePath);
	}
	
	/**
	 * Loads a Spectra object from a zip file.
	 * @param zipFilePath
	 * the path to the zip file containing the Spectra object
	 * @return
	 * the loaded Spectra object
	 */
	public static <T extends Indexable<T>> ISpectra<T> loadSpectraFromBlockZipFile(T dummy, Path zipFilePath) {
		ZipFileWrapper zip = new ReadZipFileModule().submit(zipFilePath).getResult();
		
		//parse the file containing the (possibly indexed) identifiers
		String[] identifiers = new String(zip.get(0)).split(IDENTIFIER_DELIMITER);
		
		//parse the file containing the involvement table
		byte[] involvementTable = zip.get(1);

		//parse the status byte (0 -> uncompressed, 1 -> compressed)
		byte[] status;
		try {
			status = zip.uncheckedGet(2);
		} catch (ZipException e) {
			Log.warn(SpectraUtils.class, "Unable to get compression status. (Might be an older format file.) Assuming compressed spectra.");
			status = new byte[1];
			status[0] = STATUS_COMPRESSED;
		}

		//check if we have a compressed byte array at hand
		if (isCompressed(status)) {
			involvementTable = new CompressedByteArrayToByteArrayModule().submit(involvementTable).getResult();
		}
		
		//create a new spectra
		Spectra<T> spectra = new Spectra<>();
		
		List<T> lineArray = new ArrayList<>(identifiers.length);
		if (isIndexed(status)) {
			//parse the file containing the identifier names
			String[] identifierNames = new String(zip.get(3)).split(IDENTIFIER_DELIMITER);
			Map<Integer, String> map = new HashMap<>();
			int index = 0;
			for (String identifier : identifierNames) {
				map.put(index++, identifier);
			}
			
			for (int i = 0; i < identifiers.length; ++i) {
				lineArray.add(dummy.getOriginalFromIndexedIdentifier(identifiers[i], map));
//				Log.out(SpectraUtils.class, lineArray[i].toString());
			}
		} else {
			for (int i = 0; i < identifiers.length; ++i) {
				lineArray.add(dummy.getOriginalFromIdentifier(identifiers[i]));
//				Log.out(SpectraUtils.class, lineArray[i].toString());
			}
		}
		
		int tablePosition = -1;
		//iterate over the involvement table and fill the spectra object with traces
		while (tablePosition+1 < involvementTable.length) {
			//the first element is always the 'successful' flag
			IMutableTrace<T> trace = spectra.addTrace(involvementTable[++tablePosition] == 1);

			for (int i = 0; i < lineArray.size(); ++i) {
				trace.setInvolvement(lineArray.get(i), involvementTable[++tablePosition] == 1);
			}
		}
		
		return spectra;
	}
	
	private static boolean isCompressed(byte[] status) {
		return status[0] == STATUS_COMPRESSED || status[0] == STATUS_COMPRESSED_INDEXED;
	}
	
	private static boolean isIndexed(byte[] status) {
		return status[0] == STATUS_INDEXED || status[0] == STATUS_COMPRESSED_INDEXED;
	}
	
//	private static SourceCodeBlock getBlockFromIndexedIdentifier(String identifier, Map<Integer,String> map) {
//		String[] elements = identifier.split(SourceCodeBlock.IDENTIFIER_SEPARATOR_CHAR);
//		if (elements.length == 5) {
//			return new SourceCodeBlock(map.get(Integer.valueOf(elements[0])), 
//					map.get(Integer.valueOf(elements[1])), map.get(Integer.valueOf(elements[2])), 
//					Integer.valueOf(elements[3]), Integer.valueOf(elements[4]), 1);
//		} else if (elements.length == 6) {
//			return new SourceCodeBlock(map.get(Integer.valueOf(elements[0])), 
//					map.get(Integer.valueOf(elements[1])), map.get(Integer.valueOf(elements[2])), 
//					Integer.valueOf(elements[3]), Integer.valueOf(elements[4]), Integer.valueOf(elements[5]));
//		} else {
//			Log.abort(SourceCodeBlock.class, "Wrong input format: '%s'", identifier);
//			return null;
//		}
//	}
	
	/**
	 * Gets an array of the identifiers from a zip file.
	 * @param zipFilePath
	 * the path to the zip file containing the Spectra object
	 * @return
	 * array of node identifiers
	 */
	public static String[] getIdentifiersFromSpectraFile(Path zipFilePath) {
		//parse the file containing the identifiers
		return new String(new ReadZipFileModule().submit(zipFilePath).getResult().get(0)).split(IDENTIFIER_DELIMITER);
	}
	
//	/**
//	 * Loads a Spectra object from a zip file. This method is deprecated...
//	 * @param zipFilePath
//	 * the path to the zip file containing the Spectra object
//	 * @param isCompressed
//	 * whether the Spectra has been compressed
//	 * @return
//	 * the loaded Spectra object
//	 */
//	public static ISpectra<String> loadSpectraFromZipFile(Path zipFilePath, boolean isCompressed) {
//		ZipFileWrapper zip = new ReadZipFileModule().submit(zipFilePath).getResult();
//		
//		//parse the file containing the identifiers
//		String[] identifiers = new String(zip.get(0)).split(IDENTIFIER_DELIMITER);
//		
//		//parse the file containing the involvement table
//		byte[] involvementTable = zip.get(1);
//		
//		//for backwards functionality... TODO: throw this whole method away...
//		if (isCompressed) {
//			involvementTable = new CompressedByteArrayToByteArrayModule().submit(involvementTable).getResult();
//		}
//		
//		//create a new spectra
//		Spectra<String> spectra = new Spectra<>();
//		
//		int tablePosition = -1;
//		//iterate over the involvement table and fill the spectra object with traces
//		while (tablePosition+1 < involvementTable.length) {
//			//the first element is always the 'successful' flag
//			IMutableTrace<String> trace = spectra.addTrace(involvementTable[++tablePosition] == 1);
//
//			for (int i = 0; i < identifiers.length; ++i) {
//				trace.setInvolvement(identifiers[i], involvementTable[++tablePosition] == 1);
//			}
//		}
//		
//		return spectra;
//	}
	
	/**
	 * Loads a Spectra object from a zip file.
	 * @param zipFilePath
	 * the path to the zip file containing the Spectra object
	 * @return
	 * the loaded Spectra object
	 */
	public static ISpectra<SourceCodeBlock> loadSpectraFromZipFile2(Path zipFilePath) {
		ZipFileWrapper zip = new ReadZipFileModule().submit(zipFilePath).getResult();
		
		//parse the file containing the identifiers
		String[] identifiers = new String(zip.get(0)).split(IDENTIFIER_DELIMITER);
		
		//parse the file containing the involvement table
		byte[] involvementTable = zip.get(1);

		//parse the status byte (0 -> uncompressed, 1 -> compressed)
		byte[] status;
		try {
			status = zip.uncheckedGet(2);
		} catch (ZipException e) {
			Log.warn(SpectraUtils.class, "Unable to get compression status. (Might be an older format file.) Assuming compressed spectra.");
			status = new byte[1];
			status[0] = 1;
		}

		//check if we have a compressed byte array at hand
		if (isCompressed(status)) {
			involvementTable = new CompressedByteArrayToByteArrayModule().submit(involvementTable).getResult();
		}
		
		//create a new spectra
		Spectra<SourceCodeBlock> spectra = new Spectra<>();
		
		SourceCodeBlock[] lineArray = new SourceCodeBlock[identifiers.length];
		for (int i = 0; i < identifiers.length; ++i) {
			String[] array = identifiers[i].split(SourceCodeBlock.IDENTIFIER_SEPARATOR_CHAR);
			if (array.length == 6) {
				lineArray[i] = new SourceCodeBlock(array[0], array[1], array[2], Integer.valueOf(array[3]), Integer.valueOf(array[4]), Integer.valueOf(array[5]));
			} else if (array.length == 5) {
				lineArray[i] = new SourceCodeBlock(array[0], array[1], array[2], Integer.valueOf(array[3]), Integer.valueOf(array[4]), 1);
			} else {
				Log.abort(SpectraUtils.class, "Wrong format: '%s'.", identifiers[i]);
			}
		}
		
		int tablePosition = -1;
		//iterate over the involvement table and fill the spectra object with traces
		while (tablePosition+1 < involvementTable.length) {
			//the first element is always the 'successful' flag
			IMutableTrace<SourceCodeBlock> trace = spectra.addTrace(involvementTable[++tablePosition] == 1);

			for (int i = 0; i < lineArray.length; ++i) {
				trace.setInvolvement(lineArray[i], involvementTable[++tablePosition] == 1);
			}
		}
		
		return spectra;
	}
	
	
	/**
	 * Loads a Spectra object from a zip file.
	 * @param zipFilePath
	 * the path to the zip file containing the Spectra object
	 * @return
	 * the loaded Spectra object
	 */
	public static ISpectra<String> loadSpectraFromZipFile(Path zipFilePath) {
		ZipFileWrapper zip = new ReadZipFileModule().submit(zipFilePath).getResult();
		
		//parse the file containing the identifiers
		String[] identifiers = new String(zip.get(0)).split(IDENTIFIER_DELIMITER);
		
		//parse the file containing the involvement table
		byte[] involvementTable = zip.get(1);
		
		//parse the status byte (0 -> uncompressed, 1 -> compressed)
		byte[] status;
		try {
			status = zip.uncheckedGet(2);
		} catch (ZipException e) {
			Log.warn(SpectraUtils.class, "Unable to get compression status. (Might be an older format file.) Assuming compressed spectra.");
			status = new byte[1];
			status[0] = STATUS_COMPRESSED;
		}

		//check if we have a compressed byte array at hand
		if (isCompressed(status)) {
			involvementTable = new CompressedByteArrayToByteArrayModule().submit(involvementTable).getResult();
		}
		
		//create a new spectra
		Spectra<String> spectra = new Spectra<>();
		
		int tablePosition = -1;
		//iterate over the involvement table and fill the spectra object with traces
		while (tablePosition+1 < involvementTable.length) {
			//the first element is always the 'successful' flag
			IMutableTrace<String> trace = spectra.addTrace(involvementTable[++tablePosition] == 1);

			for (int i = 0; i < identifiers.length; ++i) {
				trace.setInvolvement(identifiers[i], involvementTable[++tablePosition] == 1);
			}
		}
		
		return spectra;
	}
	
	/**
	 * Loads a Spectra object from a BugMiner coverage zip file.
	 * @param zipFilePath
	 * the path to the BugMiner coverage zip file
	 * @return
	 * the loaded Spectra object
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
	 * @return
	 * a corresponding spectra
	 */
	public static ISpectra<String> convertCoverageReportToSpectra(CoverageReport report) {
		//create a new spectra
		Spectra<String> spectra = new Spectra<>();
		
		// iterate through the test cases
		for (final TestCase testCase : report.getTestCases()) {
			IMutableTrace<String> trace = spectra.addTrace(testCase.isPassed());
			// iterate through the source files
			for (final SourceCodeFile file : report.getFiles()) {
				// get coverage for source file and test case
				final FileCoverage coverage = report.getCoverage(testCase, file);
				for (final int line : file.getLineNumbers()) {
					trace.setInvolvement(file.getFileName() + SourceCodeBlock.IDENTIFIER_SEPARATOR_CHAR + line, coverage.isCovered(line));
				}
			}
		}

		return spectra;
	}
	
	/**
	 * Loads a Spectra object from a BugMiner coverage zip file.
	 * @param zipFilePath
	 * the path to the BugMiner coverage zip file
	 * @return
	 * the loaded Spectra object
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
	 * @return
	 * a corresponding spectra
	 */
	public static ISpectra<SourceCodeBlock> convertCoverageReportToSpectra2(CoverageReport report) {
		//create a new spectra
		Spectra<SourceCodeBlock> spectra = new Spectra<>();
		
		// iterate through the test cases
		for (final TestCase testCase : report.getTestCases()) {
			IMutableTrace<SourceCodeBlock> trace = spectra.addTrace(testCase.isPassed());
			// iterate through the source files
			for (final SourceCodeFile file : report.getFiles()) {
				// get coverage for source file and test case
				final FileCoverage coverage = report.getCoverage(testCase, file);
				for (final int line : file.getLineNumbers()) {
					//TODO: no package and method name given here...
					trace.setInvolvement(new SourceCodeBlock("_", file.getFileName(), "_", line), coverage.isCovered(line));
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
		
		//serialize the report
		CoverageReportSerializer serializer = new CoverageReportSerializer();
		try {
			serializer.serialize(report, output);
		} catch (IOException e) {
			Log.abort(SpectraUtils.class, e, "Could not save serialized spectra to '%s'.", output);
		}
	}
	
	/**
	 * Converts a Spectra object to a BugMiner CoverageReport object.
	 * @param spectra
	 * the Spectra object to convert
	 * @return
	 * a respective coverage report
	 * @param <T>
	 * the type of nodes
	 */
	public static <T extends CharSequence> CoverageReport convertSpectraToReport(ISpectra<T> spectra) {
		Map<String, List<INode<T>>> nodesForFile = new HashMap<>();
		Map<INode<T>, Integer> linesOfNodes = new HashMap<>();
		
		//iterate over all nodes
		for (INode<T> node : spectra.getNodes()) {
			String identifier = node.getIdentifier().toString();
			int pos = identifier.indexOf(':');
			if (pos == -1) {
				throw new IllegalStateException("Can not derive file from identifier '" + identifier + "'.");
			}
			nodesForFile.computeIfAbsent(identifier.substring(0, pos), k -> new ArrayList<>()).add(node);
			linesOfNodes.put(node, Integer.valueOf(identifier.substring(pos+1)));
		}
		
		List<SourceCodeFile> sourceCodeFiles = new ArrayList<>(nodesForFile.entrySet().size());
		//iterate over all node lists (one for each file) and collect the line numbers into arrays
		for (Entry<String, List<INode<T>>> entry : nodesForFile.entrySet()) {
			int[] lineNumbers = new int[entry.getValue().size()];
			List<INode<T>> nodes = entry.getValue();
			for (int i = 0; i < nodes.size(); ++i) {
				lineNumbers[i] = linesOfNodes.get(nodes.get(i));
			}
			
			//add a source file object
			sourceCodeFiles.add(new SourceCodeFile(entry.getKey(), lineNumbers));
		}
		
		Map<ITrace<T>,TestCase> testCaseMap = new HashMap<>();
		List<TestCase> testCases = new ArrayList<>(spectra.getTraces().size());

		//iterate over all traces and produce corresponding test cases
		for (ITrace<T> trace : spectra.getTraces()) {
			TestCase testCase = new TestCase(trace.toString(), trace.isSuccessful());
			testCaseMap.put(trace, testCase);
			testCases.add(testCase);
		}
		
		CoverageReport report = new CoverageReport(sourceCodeFiles, testCases);
		
		//iterate over all traces
		for (ITrace<T> trace : spectra.getTraces()) {
			//iterate over all files
			for (SourceCodeFile file : sourceCodeFiles) {
				//compute coverage for each file for each trace
				FileCoverage coverage = new FileCoverage();
				for (INode<T> node : nodesForFile.get(file.getFileName())) {
					if (trace.isInvolved(node)) {
						coverage.put(linesOfNodes.get(node), true);
					} else {
						coverage.put(linesOfNodes.get(node), false);
					}
				}
				//add the coverage to the report
				report.setCoverage(testCaseMap.get(trace), file, coverage);
			}
		}
		
		return report;
	}
	
}


