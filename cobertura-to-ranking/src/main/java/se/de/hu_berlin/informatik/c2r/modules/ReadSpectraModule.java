/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.nio.file.Path;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.util.SpectraUtils;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Reads a compressed spectra file and outputs a Spectra object.
 * 
 * @author Simon Heiden
 */
public class ReadSpectraModule extends AbstractProcessor<Path, ISpectra<SourceCodeBlock>> {

	public ReadSpectraModule() {
		super();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public ISpectra<SourceCodeBlock> processItem(final Path input) {
		return SpectraUtils.loadBlockSpectraFromZipFile(input);
	}

}
