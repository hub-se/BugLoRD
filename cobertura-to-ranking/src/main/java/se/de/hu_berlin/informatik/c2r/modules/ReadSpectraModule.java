/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.nio.file.Path;

import se.de.hu_berlin.informatik.stardust.traces.ISpectra;
import se.de.hu_berlin.informatik.stardust.util.SpectraUtils;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AModule;

/**
 * Reads a compressed spectra file and outputs a Spectra object.
 * 
 * @author Simon Heiden
 */
public class ReadSpectraModule extends AModule<Path, ISpectra<String>> {

	public ReadSpectraModule() {
		super(true);
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public ISpectra<String> processItem(Path input) {
		return SpectraUtils.loadSpectraFromZipFile(input, true);
	}

}
