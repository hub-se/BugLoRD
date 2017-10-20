/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra.modules;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.utils.files.csv.CSVUtils;
import se.de.hu_berlin.informatik.utils.files.processors.ListToFileWriter;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Computes a trace file for all coverage data stored in the input spectra.
 * 
 * @author Simon Heiden
 */
public class TraceFileModule<T extends Comparable<T>> extends AbstractProcessor<ISpectra<T, ?>, Object> {

	final private Path output;
	private String fileNamePrefix;

	/**
	 * @param output
	 * output directory
	 * @param fileNamePrefix
	 * the prefix of the file name to use (file name without extension)
	 */
	public TraceFileModule(final Path output, String fileNamePrefix) {
		super();
		this.output = output;
		this.fileNamePrefix = fileNamePrefix;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.
	 * Object)
	 */
	@Override
	public ISpectra<T, ?> processItem(final ISpectra<T, ?> spectra) {
		// save a trace file that contains all executed lines
		List<INode<T>> nodes = new ArrayList<>(spectra.getNodes());

		// order the nodes based on the order of their identifiers
		Collections.sort(nodes, new Comparator<INode<T>>() {

			@Override
			public int compare(INode<T> o1, INode<T> o2) {
				return o1.getIdentifier().compareTo(o2.getIdentifier());
			}
		});

		List<String> traceFileLines = new ArrayList<>();
		
		List<String> csvLines = new ArrayList<>();
		String[] title = new String[] { 
				"Node",
				"EF",
				"EP",
				"NF",
				"NP"
				};
		csvLines.add(CSVUtils.toCsvLine(title));
		
		// iterate over the identifiers
		for (INode<T> node : nodes) {
			traceFileLines.add(node.getIdentifier().toString());
			
			String[] line = new String[] { 
					node.getIdentifier().toString().replace(CSVUtils.CSV_DELIMITER, '§'),
					Integer.toString((int)node.getEF()),
					Integer.toString((int)node.getEP()),
					Integer.toString((int)node.getNF()),
					Integer.toString((int)node.getNP())
					};
			csvLines.add(CSVUtils.toCsvLine(line));
		}

		// save the trace and csv file
		new ListToFileWriter<>(output.resolve(fileNamePrefix + BugLoRDConstants.FILENAME_TRACE_FILE_EXTENSION), true).submit(traceFileLines);
		new ListToFileWriter<>(output.resolve(fileNamePrefix + ".csv"), true).submit(csvLines);

		return spectra;
	}

}
