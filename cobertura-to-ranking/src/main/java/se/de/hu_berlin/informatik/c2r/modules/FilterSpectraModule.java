/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.util.List;

import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.ITrace;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * Reads a compressed spectra file and outputs a Spectra object.
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
		
		List<ITrace<T>> failedTraces = input.getFailingTraces();
		for (INode<T> node : input.getNodes()) {
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
