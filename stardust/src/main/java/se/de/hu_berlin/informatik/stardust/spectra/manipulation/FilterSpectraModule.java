/**
 * 
 */
package se.de.hu_berlin.informatik.stardust.spectra.manipulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.ITrace;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * Reads a Spectra object and filters out all nodes that haven't been touched by
 * any failing trace.
 * 
 * @author Simon Heiden
 * 
 * @param <T>
 * the type of nodes in the spectra
 */
public class FilterSpectraModule<T> extends AbstractModule<ISpectra<T>, ISpectra<T>> {

	public FilterSpectraModule() {
		super(true);
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public ISpectra<T> processItem(final ISpectra<T> input) {
		
		Collection<ITrace<T>> failedTraces = input.getFailingTraces();
		//get a copy of the current set of nodes, since we will be removing nodes
		List<INode<T>> nodes = new ArrayList<>(input.getNodes());
		for (INode<T> node : nodes) {
			boolean isInvolvedInFailedTrace = false;
			for (ITrace<T> failedTrace : failedTraces) {
				if (failedTrace.isInvolved(node)) {
					isInvolvedInFailedTrace = true;
					break;
				}
			}
			if (!isInvolvedInFailedTrace) {
				input.removeNode(node.getIdentifier());
			}
		}
		
		return input;
	}

}
