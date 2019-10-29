package se.de.hu_berlin.informatik.spectra.core.manipulation;

import java.nio.file.Path;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.util.Indexable;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
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
public class SaveSpectraModule<T extends Indexable<T>> extends AbstractProcessor<ISpectra<T,?>, ISpectra<T,?>> {
	
	final private Path output;

	public SaveSpectraModule(final Path output) {
		super();
		this.output = output;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public ISpectra<T,?> processItem(final ISpectra<T,?> input) {
//		if (input.isEmpty()) {
//			Log.out(this, "Spectra is empty and will not be saved.");
//			return null;
//		} else {
			Log.out(this, "Saving spectra...");
			SpectraFileUtils.saveSpectraToZipFile(input, output, true, true, true);
//		}
		return input;
	}

}
