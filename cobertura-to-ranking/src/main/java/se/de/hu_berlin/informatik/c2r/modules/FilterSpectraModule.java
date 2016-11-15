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
 */
public class FilterSpectraModule extends AbstractModule<ISpectra<String>, ISpectra<String>> {

	public FilterSpectraModule() {
		super(true);
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public ISpectra<String> processItem(final ISpectra<String> input) {
		
		List<ITrace<String>> failedTraces = input.getFailingTraces();
		for (INode<String> node : input.getNodes()) {
			boolean isInvolvedInFailedTrace = false;
			for (ITrace<String> failedTrace : failedTraces) {
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
