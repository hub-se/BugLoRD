package se.de.hu_berlin.informatik.spectra.core.manipulation;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.util.Indexable;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

import java.nio.file.Path;

/**
 * Reads a compressed spectra file and outputs a Spectra object.
 *
 * @param <T> the type of nodes in the spectra
 * @author Simon Heiden
 */
public class ReadSpectraModule<T extends Indexable<T>> extends AbstractProcessor<Path, ISpectra<T, ?>> {

    final private T dummy;

    public ReadSpectraModule(T dummy) {
        super();
        this.dummy = dummy;
    }

    /* (non-Javadoc)
     * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
     */
    @Override
    public ISpectra<T, ?> processItem(final Path input) {
        return SpectraFileUtils.loadSpectraFromZipFile(dummy, input);
    }

}
