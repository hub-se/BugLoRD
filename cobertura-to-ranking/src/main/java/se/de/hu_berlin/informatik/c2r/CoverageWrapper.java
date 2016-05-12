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
	
	private File xmlCoverageFile;
	private boolean successful;
	
	public CoverageWrapper(File xmlCoverageFile, boolean successful) {
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
