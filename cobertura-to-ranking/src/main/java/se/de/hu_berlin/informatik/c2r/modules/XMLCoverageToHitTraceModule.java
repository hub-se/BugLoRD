/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.jdom.JDOMException;

import se.de.hu_berlin.informatik.stardust.localizer.HitRanking;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.NoRanking;
import se.de.hu_berlin.informatik.stardust.provider.CoberturaXMLProvider;
import se.de.hu_berlin.informatik.stardust.provider.CoverageWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * Computes the hit trace for the wrapped Cobertura report and saves the
 * trace file to the hard drive.
 * 
 * @author Simon Heiden
 */
public class XMLCoverageToHitTraceModule extends AbstractModule<CoverageWrapper, Object> {

	final private String outputdir;
	
	/**
	 * Creates a new {@link XMLCoverageToHitTraceModule} object with the given parameters.
	 * @param outputdir
	 * path to output directory
	 */
	public XMLCoverageToHitTraceModule(final String outputdir) {
		super(true);
		this.outputdir = outputdir;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public Object processItem(final CoverageWrapper coverage) {
		computeHitTrace(coverage);
		return null;
	}
	
	/**
	 * Calculates a single hit trace from the given input xml file to output/inputfilename.trc.
	 * @param input
	 * path to Cobertura trace file in xml format
	 */
	private void computeHitTrace(final CoverageWrapper coverage) {
		try {
			final CoberturaXMLProvider provider = new CoberturaXMLProvider();
			provider.addTraceFile(coverage.getXmlCoverageFile().toString(), coverage.getIdentifier(), true);
			
			try {
				final HitRanking<SourceCodeBlock> ranking = new NoRanking<SourceCodeBlock>(true).localizeHit(provider.loadSpectra());
				Paths.get(outputdir).toFile().mkdirs();
				ranking.save(outputdir + File.separator + coverage.getXmlCoverageFile().getName().replace(':','_') + ".trc");
			} catch (Exception e1) {
				Log.err(this, e1, "Could not save ranking for trace file '%s' in '%s'. (hit trace)%n", 
						coverage.getXmlCoverageFile().toString(), outputdir + File.separator + coverage.getXmlCoverageFile().getName().replace(':','_') + ".trc");
			}
		} catch (IOException e) {
			Log.err(this, "Could not add XML coverage file '%s'.", coverage.getXmlCoverageFile().toString());
		} catch (JDOMException e) {
			Log.err(this, "The XML coverage file '%s' could not be loaded by JDOM.", coverage.getXmlCoverageFile().toString());
		}
	}

}
