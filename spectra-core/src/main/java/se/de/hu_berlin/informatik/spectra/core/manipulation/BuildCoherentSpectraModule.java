/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.core.manipulation;

import java.util.Arrays;
import java.util.Collection;

import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Reads a Spectra object and fills up "empty" space between covered nodes by extending their ranges to larger blocks.
 * 
 * @author Simon Heiden
 */
public class BuildCoherentSpectraModule extends AbstractProcessor<ISpectra<SourceCodeBlock,?>, ISpectra<SourceCodeBlock,?>> {

	public BuildCoherentSpectraModule() {
		super();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public ISpectra<SourceCodeBlock,?> processItem(final ISpectra<SourceCodeBlock,?> input) {
		
		//get lines in the spectra and sort them
		Collection<INode<SourceCodeBlock>> nodes = input.getNodes();
		SourceCodeBlock[] array = new SourceCodeBlock[nodes.size()];
		int counter = -1;
		for (INode<SourceCodeBlock> node : nodes) {
			array[++counter] = node.getIdentifier();
		}
		Arrays.sort(array);
		
		SourceCodeBlock lastLine = new SourceCodeBlock("", "", "", -1);
		//iterate over all lines
		for (SourceCodeBlock line : array) {
			//see if we are inside the same method in the same package
			if (line.getMethodName().equals(lastLine.getMethodName())
					&& line.getPackageName().equals(lastLine.getPackageName())) {
				//set the end line number of the last covered line to be equal 
				//to the line before the next covered line
				lastLine.setLineNumberEnd(line.getStartLineNumber()-1);
			}
			//next line...
			lastLine = line;
		}
		
		return input;
	}

}
