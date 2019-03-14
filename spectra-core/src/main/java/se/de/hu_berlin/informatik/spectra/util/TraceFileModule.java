package se.de.hu_berlin.informatik.spectra.util;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
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
	private final String suffix;

	/**
	 * @param output
	 * output directory
	 * @param suffix
	 * a suffix to append
	 */
	public TraceFileModule(final Path output, String suffix) {
		super();
		this.output = output;
		this.suffix = suffix;
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
		nodes.sort(Comparator.comparing(INode::getIdentifier));

		List<String> traceFileLines = new ArrayList<>();
		List<String> csvLines = new ArrayList<>();
		

		// iterate over the identifiers
		for (INode<T> node : nodes) {
			traceFileLines.add(node.getIdentifier().toString());
			
			String[] line = new String[] {
					Integer.toString((int)node.getEF()),
					Integer.toString((int)node.getEP()),
					Integer.toString((int)node.getNF()),
					Integer.toString((int)node.getNP())
					};
			csvLines.add(CSVUtils.toCsvLine(line));
		}

		// save the trace and csv file containing the 4 numbers
		new ListToFileWriter<>(output.resolve(BugLoRDConstants.getTraceFileFileName(suffix)), true).submit(traceFileLines);
		new ListToFileWriter<>(output.resolve(BugLoRDConstants.getMetricsFileFileName(suffix)), true).submit(csvLines);

		return spectra;
	}

}
