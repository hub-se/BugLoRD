/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.nio.file.Path;

import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.util.SpectraUtils;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AModule;

/**
 * Saves a Spectra object and forwards it to the output.
 * 
 * @author Simon Heiden
 */
public class SaveSpectraModule extends AModule<ISpectra<String>, ISpectra<String>> {
	
	private Path output;
	private boolean compressed;
	private boolean store = false;

	public SaveSpectraModule(Path output, boolean compressed) {
		super(true);
		this.output = output;
		this.compressed = compressed;
		store = true;
	}
	
	public SaveSpectraModule() {
		super(true);
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public ISpectra<String> processItem(ISpectra<String> input) {
		if (store) {
			SpectraUtils.saveSpectraToZipFile(input, output, compressed);
		}
		return input;
	}

}
