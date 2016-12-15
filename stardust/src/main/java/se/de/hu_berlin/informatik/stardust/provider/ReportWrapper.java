/**
 * 
 */
package se.de.hu_berlin.informatik.stardust.provider;

import net.sourceforge.cobertura.reporting.NativeReport;

/**
 * @author Simon
 *
 */
public class ReportWrapper {
	
	final private NativeReport report;
	final private boolean successful;
	final String testIdentifier;
	
	public ReportWrapper(final NativeReport report, final String testIdentifier, final boolean successful) {
		this.report = report;
		this.successful = successful;
		this.testIdentifier = testIdentifier;
	}

	public NativeReport getReport() {
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
