/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.io.IOException;
import org.jdom.JDOMException;

import se.de.hu_berlin.informatik.stardust.provider.CoberturaProvider;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.c2r.CoverageWrapper;
import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AModule;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class AddToProviderAndGenerateSpectraModule extends AModule<CoverageWrapper, ISpectra<String>> {

	private CoberturaProvider provider;
	private boolean deleteXMLFiles = false;
	private boolean saveFailedTraces = false;
	private HitTraceModule hitTraceModule = null;
	
	public AddToProviderAndGenerateSpectraModule(boolean aggregateSpectra, boolean deleteXMLFiles, String FailedTracesOutputDir) {
		super(true);
		this.provider = new CoberturaProvider(aggregateSpectra);
		this.deleteXMLFiles = deleteXMLFiles;		
		if (FailedTracesOutputDir != null) {
			this.saveFailedTraces = true;
			hitTraceModule = new HitTraceModule(FailedTracesOutputDir, false);
		}
	}
	
	public AddToProviderAndGenerateSpectraModule(boolean aggregateSpectra, boolean deleteXMLFiles) {
		this(aggregateSpectra, deleteXMLFiles, null);
	}
	
	public AddToProviderAndGenerateSpectraModule() {
		this(false, false);
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public ISpectra<String> processItem(CoverageWrapper coverage) {

		if (saveFailedTraces && !coverage.isSuccessful()) {
			hitTraceModule.submit(coverage);
		}
		
		try {
			provider.addTraceFile(coverage.getXmlCoverageFile().toString(), coverage.isSuccessful());
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
	public ISpectra<String> getResultFromCollectedItems() {
		try {
			return provider.loadSpectra();
		} catch (Exception e) {
			Log.err(this, "Providing the spectra failed.");
		}
		return null;
	}

}
