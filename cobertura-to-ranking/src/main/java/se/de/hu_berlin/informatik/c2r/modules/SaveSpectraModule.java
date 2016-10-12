/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.nio.file.Path;

import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.util.SpectraUtils;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * Saves a Spectra object and forwards it to the output.
 * 
 * @author Simon Heiden
 */
public class SaveSpectraModule extends AbstractModule<ISpectra<String>, ISpectra<String>> {
	
	final private Path output;
	final private boolean compressed;

	public SaveSpectraModule(final Path output, final boolean compressed) {
		super(true);
		this.output = output;
		this.compressed = compressed;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public ISpectra<String> processItem(final ISpectra<String> input) {
		SpectraUtils.saveSpectraToZipFile(input, output, compressed);
		return input;
	}

}
