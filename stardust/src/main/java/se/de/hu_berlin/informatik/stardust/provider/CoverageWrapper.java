/**
 * 
 */
package se.de.hu_berlin.informatik.stardust.provider;

import java.io.File;

/**
 * @author Simon
 *
 */
public class CoverageWrapper {
	
	final private File xmlCoverageFile;
	final private boolean successful;
	final String testIdentifier;
	
	public CoverageWrapper(final File xmlCoverageFile, final String testIdentifier, final boolean successful) {
		this.xmlCoverageFile = xmlCoverageFile;
		this.successful = successful;
		this.testIdentifier = testIdentifier;
	}

	public File getXmlCoverageFile() {
		return xmlCoverageFile;
	}
	
	public String getIdentifier() {
		return testIdentifier;
	}

	public boolean isSuccessful() {
		return successful;
	}

	@Override
	public String toString() {
		return "[ " + testIdentifier + ", " + successful + " ]";
	}
}
