/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.nio.file.Path;

import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.util.Indexable;
import se.de.hu_berlin.informatik.stardust.util.SpectraUtils;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * Saves a Spectra object and forwards it to the output.
 * 
 * @author Simon Heiden
 */
public class SaveSpectraModule<T extends Indexable<T>> extends AbstractModule<ISpectra<T>, ISpectra<T>> {
	
	final private Path output;
	final private T dummy;

	public SaveSpectraModule(T dummy, final Path output) {
		super(true);
		this.output = output;
		this.dummy = dummy;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public ISpectra<T> processItem(final ISpectra<T> input) {
		SpectraUtils.saveSpectraToZipFile(dummy, input, output, true, true, true);
		return input;
	}

}
