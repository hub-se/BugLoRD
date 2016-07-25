package se.de.hu_berlin.informatik.aspectj.frontend;

import java.io.IOException;
import java.nio.file.Paths;

import de.unistuttgart.iste.rss.bugminer.coverage.CoverageReport;
import de.unistuttgart.iste.rss.bugminer.coverage.CoverageReportDeserializer;
import de.unistuttgart.iste.rss.bugminer.coverage.FileCoverage;
import de.unistuttgart.iste.rss.bugminer.coverage.SourceCodeFile;
import de.unistuttgart.iste.rss.bugminer.coverage.TestCase;

public class Importer {
	public Importer() throws IOException {
		// read single bug
		final CoverageReportDeserializer deserializer = new CoverageReportDeserializer();
		final CoverageReport report = deserializer.deserialize(Paths.get("/path/to/trace/bugId.zip"));
		// iterate through source code files and test cases
		for (final SourceCodeFile file : report.getFiles()) {
			for (final TestCase testCase : report.getTestCases()) {
				// get coverage for source file and test case
				final FileCoverage coverage = report.getCoverage(testCase, file);
				for (final int line : file.getLineNumbers()) {
					// compute + count SBFL metrics
					final boolean involvedInPassing = coverage.isCovered(line) && testCase.isPassed();
					
				}
			}
		}
	}
}
