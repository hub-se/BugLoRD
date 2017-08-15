/**
 * 
 */
package se.de.hu_berlin.informatik.stardust.provider.cobertura;

import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.reporting.NativeReport;

/**
 * @author Simon
 *
 */
public class CoberturaReportWrapper {
	
	final private NativeReport report;
	final private ProjectData initialProjectData;
	final private boolean successful;
	final String testIdentifier;
	
	public CoberturaReportWrapper(final NativeReport report, ProjectData initialProjectData, final String testIdentifier, final boolean successful) {
		this.report = report;
		this.initialProjectData = initialProjectData;
		this.successful = successful;
		this.testIdentifier = testIdentifier;
	}

	public NativeReport getReport() {
		return report;
	}
	
	public ProjectData getInitialProjectData() {
		return initialProjectData;
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
