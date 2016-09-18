/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.io.File;
import se.de.hu_berlin.informatik.c2r.CoverageWrapper;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AModule;

/**
 * 
 * 
 * @author Simon Heiden
 * 
 */
public class XMLCoverageWrapperModule extends AModule<File,CoverageWrapper> {
	
	public XMLCoverageWrapperModule() {
		super(true, true);
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public CoverageWrapper processItem(File xmlCoverageFile) {
		if (xmlCoverageFile.getParent().contains("fail")) {
			return new CoverageWrapper(xmlCoverageFile, false);
		} else {
			return new CoverageWrapper(xmlCoverageFile, true);
		}
	}

}
