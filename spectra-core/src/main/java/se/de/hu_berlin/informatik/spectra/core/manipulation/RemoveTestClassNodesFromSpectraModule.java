package se.de.hu_berlin.informatik.spectra.core.manipulation;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Reads a Spectra object and filters out all nodes that haven't been touched by
 * any failing trace. (EF == 0)
 *
 * @param <T> the type of nodes in the spectra
 * @author Simon Heiden
 */
public class RemoveTestClassNodesFromSpectraModule<T> extends AbstractProcessor<ISpectra<T, ?>, ISpectra<T, ?>> {

    final private T dummy;

    public RemoveTestClassNodesFromSpectraModule(T dummy) {
        super();
        this.dummy = dummy;
    }

    /* (non-Javadoc)
     * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
     */
    @Override
    public ISpectra<T, ?> processItem(final ISpectra<T, ?> input) {
        Log.out(this, "Removing test class nodes from spectra...");
        return input.removeTestClassNodes(dummy);
    }

}
