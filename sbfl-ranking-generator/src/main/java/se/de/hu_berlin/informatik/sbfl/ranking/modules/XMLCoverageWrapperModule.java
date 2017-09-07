/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.ranking.modules;

import java.io.File;

import se.de.hu_berlin.informatik.stardust.provider.cobertura.xml.CoberturaCoverageWrapper;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * 
 * 
 * @author Simon Heiden
 * 
 */
public class XMLCoverageWrapperModule extends AbstractProcessor<File,CoberturaCoverageWrapper> {
	
	public XMLCoverageWrapperModule() {
		super();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public CoberturaCoverageWrapper processItem(final File xmlCoverageFile) {
		if (xmlCoverageFile.getParent().contains("fail")) {
			return new CoberturaCoverageWrapper(xmlCoverageFile, 
					FileUtils.getFileNameWithoutExtension(xmlCoverageFile.toString()), false);
		} else {
			return new CoberturaCoverageWrapper(xmlCoverageFile, 
					FileUtils.getFileNameWithoutExtension(xmlCoverageFile.toString()), true);
		}
	}

}
