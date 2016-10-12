/**
 * 
 */
package se.de.hu_berlin.informatik.c2r;

import java.io.File;

/**
 * @author Simon
 *
 */
public class CoverageWrapper {
	
	final private File xmlCoverageFile;
	final private boolean successful;
	
	public CoverageWrapper(final File xmlCoverageFile, final boolean successful) {
		this.xmlCoverageFile = xmlCoverageFile;
		this.successful = successful;
	}

	public File getXmlCoverageFile() {
		return xmlCoverageFile;
	}

	public boolean isSuccessful() {
		return successful;
	}

	@Override
	public String toString() {
		return "[ " + xmlCoverageFile.toString() + ", " + successful + " ]";
	}
}
