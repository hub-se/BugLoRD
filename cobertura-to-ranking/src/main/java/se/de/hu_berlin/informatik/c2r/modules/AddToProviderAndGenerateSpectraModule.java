/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.io.IOException;
import org.jdom.JDOMException;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.provider.CoberturaProvider;
import se.de.hu_berlin.informatik.stardust.provider.CoverageWrapper;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class AddToProviderAndGenerateSpectraModule extends AbstractModule<CoverageWrapper, ISpectra<SourceCodeBlock>> {

	final private CoberturaProvider provider;
	final private boolean deleteXMLFiles;
	private boolean saveFailedTraces = false;
	private HitTraceModule hitTraceModule = null;
	
	public AddToProviderAndGenerateSpectraModule(final boolean aggregateSpectra, 
			final boolean deleteXMLFiles, final String failedTracesOutputDir) {
		super(true);
		this.provider = new CoberturaProvider(aggregateSpectra);
		this.deleteXMLFiles = deleteXMLFiles;		
		if (failedTracesOutputDir != null) {
			this.saveFailedTraces = true;
			hitTraceModule = new HitTraceModule(failedTracesOutputDir, false);
		}
	}
	
	public AddToProviderAndGenerateSpectraModule(final boolean aggregateSpectra, 
			final boolean deleteXMLFiles) {
		this(aggregateSpectra, deleteXMLFiles, null);
	}
	
	public AddToProviderAndGenerateSpectraModule() {
		this(false, false);
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
			if (deleteXMLFiles) {
				FileUtils.delete(coverage.getXmlCoverageFile());
			}
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
