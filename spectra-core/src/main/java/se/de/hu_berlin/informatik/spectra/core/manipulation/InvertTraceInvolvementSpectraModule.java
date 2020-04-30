package se.de.hu_berlin.informatik.spectra.core.manipulation;

import se.de.hu_berlin.informatik.spectra.core.hit.HitSpectra;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Reads a Spectra object and switches involvements of nodes for
 * successful and/or failing traces to the respective opposite.
 * Returns a new Spectra object that has the required properties.
 * The input spectra is left unmodified.
 *
 * @param <T> the type of nodes in the spectra
 * @author Simon Heiden
 */
public class InvertTraceInvolvementSpectraModule<T> extends AbstractProcessor<HitSpectra<T>, HitSpectra<T>> {

    final private boolean switchSuccessful;
    final private boolean switchFailed;

    public InvertTraceInvolvementSpectraModule(boolean switchSuccessful, boolean switchFailed) {
        super();
        this.switchSuccessful = switchSuccessful;
        this.switchFailed = switchFailed;
    }

    /* (non-Javadoc)
     * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
     */
    @Override
    public HitSpectra<T> processItem(final HitSpectra<T> input) {
        if (switchSuccessful) {
            Log.out(this, "Inverting successful traces...");
        }
        if (switchFailed) {
            Log.out(this, "Inverting failed traces...");
        }
        return input.createInvertedSpectra(switchSuccessful, switchFailed);
    }

}
