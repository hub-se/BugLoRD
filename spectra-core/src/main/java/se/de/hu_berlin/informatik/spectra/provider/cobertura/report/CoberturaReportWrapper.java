/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.provider.cobertura.report;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.NativeReport;

/**
 * @author Simon
 *
 */
public class CoberturaReportWrapper {

	final private NativeReport report;
	final private boolean successful;
	final String testIdentifier;

	public CoberturaReportWrapper(final NativeReport report, final String testIdentifier, final boolean successful) {
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
