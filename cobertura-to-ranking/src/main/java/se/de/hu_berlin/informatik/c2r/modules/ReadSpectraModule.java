/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.nio.file.Path;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeLine;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.util.SpectraUtils;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * Reads a compressed spectra file and outputs a Spectra object.
 * 
 * @author Simon Heiden
 */
public class ReadSpectraModule extends AbstractModule<Path, ISpectra<SourceCodeLine>> {

	public ReadSpectraModule() {
		super(true);
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public ISpectra<SourceCodeLine> processItem(final Path input) {
		return SpectraUtils.loadSpectraFromZipFile2(input, true);
	}

}
