/**
 * 
 */
package se.de.hu_berlin.informatik.stardust.spectra.manipulation;

import java.nio.file.Path;

import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.util.Indexable;
import se.de.hu_berlin.informatik.stardust.util.SpectraUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Saves a Spectra object and forwards it to the output.
 * 
 * @author Simon Heiden
 * 
 * @param <T>
 * the type of nodes in the spectra
 */
public class SaveSpectraModule<T extends Indexable<T>> extends AbstractProcessor<ISpectra<T>, ISpectra<T>> {
	
	final private Path output;
	final private T dummy;

	public SaveSpectraModule(T dummy, final Path output) {
		super();
		this.output = output;
		this.dummy = dummy;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public ISpectra<T> processItem(final ISpectra<T> input) {
		if (input.isEmpty()) {
			Log.out(this, "Spectra is empty and will not be saved.");
		} else {
			Log.out(this, "Saving spectra...");
			SpectraUtils.saveSpectraToZipFile(dummy, input, output, true, true, true);
		}
		return input;
	}

}
