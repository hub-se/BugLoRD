/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.io.IOException;

import org.jdom.JDOMException;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.provider.CoberturaXMLProvider;
import se.de.hu_berlin.informatik.stardust.provider.CoverageWrapper;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class AddXMLCoverageToProviderAndGenerateSpectraModule extends AbstractModule<CoverageWrapper, ISpectra<SourceCodeBlock>> {

	final private CoberturaXMLProvider provider;
	private boolean saveFailedTraces = false;
	private XMLCoverageToHitTraceModule hitTraceModule = null;
	
	public AddXMLCoverageToProviderAndGenerateSpectraModule(final boolean aggregateSpectra, 
			final String failedTracesOutputDir) {
		super(true);
		this.provider = new CoberturaXMLProvider(aggregateSpectra);
		if (failedTracesOutputDir != null) {
			this.saveFailedTraces = true;
			hitTraceModule = new XMLCoverageToHitTraceModule(failedTracesOutputDir);
		}
	}
	
	public AddXMLCoverageToProviderAndGenerateSpectraModule(final boolean aggregateSpectra) {
		this(aggregateSpectra, null);
	}
	
	public AddXMLCoverageToProviderAndGenerateSpectraModule() {
		this(false);
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public ISpectra<SourceCodeBlock> processItem(final CoverageWrapper coverage) {

		if (saveFailedTraces && !coverage.isSuccessful()) {
			hitTraceModule.submit(coverage);
		}
		
		try {
			provider.addTraceFile(coverage.getXmlCoverageFile().toString(), 
					coverage.getIdentifier(), coverage.isSuccessful());
		} catch (IOException e) {
			Log.err(this, "Could not add XML coverage file '%s'.", coverage.getXmlCoverageFile());
		} catch (JDOMException e) {
			Log.err(this, "The XML coverage file '%s' could not be loaded by JDOM.", coverage.getXmlCoverageFile());
		}

		return null;
	}

	@Override
	public ISpectra<SourceCodeBlock> getResultFromCollectedItems() {
		try {
			return provider.loadSpectra();
		} catch (Exception e) {
			Log.err(this, "Providing the spectra failed.");
		}
		return null;
	}

}
