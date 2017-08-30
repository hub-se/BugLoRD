/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra.modules;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.utils.files.processors.ListToFileWriter;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Computes a trace file for all coverage data stored in the 
 * input spectra.
 * 
 * @author Simon Heiden
 */
public class TraceFileModule<T extends Comparable<T>> extends AbstractProcessor<ISpectra<T>, Object> {

	final private Path output;

	/**
	 * @param output
	 * output file
	 */
	public TraceFileModule(final Path output) {
		super();
		this.output = output;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public ISpectra<T> processItem(final ISpectra<T> spectra) {
		//save a trace file that contains all executed lines
		List<INode<T>> nodes = new ArrayList<>(spectra.getNodes());
		
		//order the nodes based on the order of their identifiers
		Collections.sort(nodes, new Comparator<INode<T>>() {
			@Override
			public int compare(INode<T> o1, INode<T> o2) {
				return o1.getIdentifier().compareTo(o2.getIdentifier());
			}
		});

		List<String> lines = new ArrayList<>();
		//iterate over the identifiers
		for (INode<T> node : nodes) {
			lines.add(node.getIdentifier().toString());
		}

		//save the trace
		new ListToFileWriter<>(output, true)
		.submit(lines);

		return spectra;
	}

}
