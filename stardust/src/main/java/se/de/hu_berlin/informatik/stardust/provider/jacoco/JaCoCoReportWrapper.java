/**
 * 
 */
package se.de.hu_berlin.informatik.stardust.provider.jacoco;

import org.jacoco.core.analysis.IBundleCoverage;

/**
 * @author Simon
 *
 */
public class JaCoCoReportWrapper {
	
	final private IBundleCoverage report;
	final private boolean successful;
	final String testIdentifier;
	
	public JaCoCoReportWrapper(final IBundleCoverage report, final String testIdentifier, final boolean successful) {
		this.report = report;
		this.successful = successful;
		this.testIdentifier = testIdentifier;
	}

	public IBundleCoverage getCoverageBundle() {
		return report;
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
