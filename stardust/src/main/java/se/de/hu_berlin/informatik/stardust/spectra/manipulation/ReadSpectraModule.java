/**
 * 
 */
package se.de.hu_berlin.informatik.stardust.spectra.manipulation;

import java.nio.file.Path;

import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.util.Indexable;
import se.de.hu_berlin.informatik.stardust.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Reads a compressed spectra file and outputs a Spectra object.
 * 
 * @author Simon Heiden
 * 
 * @param <T>
 * the type of nodes in the spectra
 */
public class ReadSpectraModule<T extends Indexable<T>> extends AbstractProcessor<Path, ISpectra<T>> {

	final private T dummy;

	public ReadSpectraModule(T dummy) {
		super();
		this.dummy = dummy;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public ISpectra<T> processItem(final Path input) {
		return SpectraFileUtils.loadSpectraFromZipFile(dummy, input);
	}

}
