package se.de.hu_berlin.informatik.stardust.provider.tracecobertura.coveragedata;

import net.sourceforge.cobertura.dsl.ReportFormat;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.reporting.NullReport;
import net.sourceforge.cobertura.reporting.Report;
import net.sourceforge.cobertura.reporting.ReportFormatStrategyRegistry;
import net.sourceforge.cobertura.reporting.ReportName;
import net.sourceforge.cobertura.util.FileFinder;

import java.io.File;

public class NativeReport implements Report {
	private NullReport nullReport;
	private TraceProjectData projectData;
	private File destinationDir;
	private FileFinder finder;
	private ComplexityCalculator complexity;
	private String encoding;
	private ReportFormatStrategyRegistry formatStrategyRegistry;

	public NativeReport(TraceProjectData projectData, File destinationDir,
			FileFinder finder, ComplexityCalculator complexity, String encoding) {
		this.nullReport = new NullReport();
		this.projectData = projectData;
		this.destinationDir = destinationDir;
		this.finder = finder;
		this.complexity = complexity;
		this.destinationDir = destinationDir;
		this.encoding = encoding;
		formatStrategyRegistry = ReportFormatStrategyRegistry.getInstance();
	}

	public void export(ReportFormat reportFormat) {
		formatStrategyRegistry.getReportFormatStrategy(reportFormat).save(this);
	}

	public ReportName getName() {
		return ReportName.COVERAGE_REPORT;
	}

	public Report getByName(ReportName name) {
		if (getName().equals(name)) {
			return this;
		}
		return nullReport;
	}

	public TraceProjectData getProjectData() {
		return projectData;
	}

	public File getDestinationDir() {
		return destinationDir;
	}

	public FileFinder getFinder() {
		return finder;
	}

	public ComplexityCalculator getComplexity() {
		return complexity;
	}

	public String getEncoding() {
		return encoding;
	}
}