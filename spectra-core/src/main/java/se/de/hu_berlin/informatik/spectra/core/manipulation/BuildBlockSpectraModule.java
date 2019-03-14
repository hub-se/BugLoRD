package se.de.hu_berlin.informatik.spectra.core.manipulation;

import java.util.Arrays;
import java.util.Collection;

import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Reads a Spectra object and combines sequences of nodes to larger blocks based
 * on whether they were executed by the same set of traces (which would result in the same ranking).
 * 
 * @author Simon Heiden
 */
public class BuildBlockSpectraModule extends AbstractProcessor<ISpectra<SourceCodeBlock,?>, ISpectra<SourceCodeBlock,?>> {

	public BuildBlockSpectraModule() {
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
		
		Collection<? extends ITrace<SourceCodeBlock>> traces = input.getTraces();
		SourceCodeBlock lastLine = new SourceCodeBlock("", "", "", -1);
		INode<SourceCodeBlock> lastNode = null;
		//iterate over all lines
		for (SourceCodeBlock line : array) {
			INode<SourceCodeBlock> node = input.getOrCreateNode(line);
			//see if we are inside the same method in the same package
			if (line.getMethodName().equals(lastLine.getMethodName())
					&& line.getPackageName().equals(lastLine.getPackageName())) {
				boolean isInvolvedInSameTraces = true;
				//see if the involvements match for consecutive nodes
				for (ITrace<SourceCodeBlock> trace : traces) {
					//if we find an involvement that doesn't match, then we can break the loop
					if (trace.isInvolved(node) != trace.isInvolved(lastNode)) {
						isInvolvedInSameTraces = false;
						break;
					}
				}
				//if this line is involved in the same traces as the last, then 
				//we can safely extend the last block to this line;
				//there cannot be any other covered lines in between due
				//to the ordering of SourceCodeLine objects
				if (isInvolvedInSameTraces) {
					//(note that the following only works because the end line
					//numbers do not influence the hash code, and the changed 
					//node's sort order remains identical)
					//extend the range of the last block
					lastLine.setLineNumberEnd(line.getEndLineNumber());
					//remove the superfluous node from the spectra
					input.removeNode(line);
				} else {
					//if this line isn't involved in the same traces as the last 
					//one, then go on to the next line
					lastLine = line;
					lastNode = node;
				}
			} else {
				//if we change into another method or package, also go
				//to the next line
				lastLine = line;
				lastNode = node;
			}
		}
		
		return input;
	}

}
