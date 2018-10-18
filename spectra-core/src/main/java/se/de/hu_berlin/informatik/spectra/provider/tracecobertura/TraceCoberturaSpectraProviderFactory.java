package se.de.hu_berlin.informatik.spectra.provider.tracecobertura;

import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.count.CountSpectra;
import se.de.hu_berlin.informatik.spectra.core.count.CountTrace;
import se.de.hu_berlin.informatik.spectra.core.hit.HitSpectra;
import se.de.hu_berlin.informatik.spectra.core.hit.HitTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.report.HierarchicalTraceCoberturaCountReportProvider;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.report.HierarchicalTraceCoberturaReportProvider;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.report.TraceCoberturaCountReportProvider;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.report.TraceCoberturaReportProvider;

public class TraceCoberturaSpectraProviderFactory {

	public static TraceCoberturaReportProvider<HitTrace<SourceCodeBlock>> getHitSpectraFromReportProvider(
			boolean fullSpectra) {
		return new TraceCoberturaReportProvider<HitTrace<SourceCodeBlock>>(new HitSpectra<SourceCodeBlock>(), fullSpectra);
	}

	public static TraceCoberturaCountReportProvider<CountTrace<SourceCodeBlock>> getCountSpectraFromReportProvider(
			boolean fullSpectra) {
		return new TraceCoberturaCountReportProvider<CountTrace<SourceCodeBlock>>(new CountSpectra<SourceCodeBlock>(),
				fullSpectra);
	}

	public static HierarchicalTraceCoberturaReportProvider<HitTrace<SourceCodeBlock>> getHierarchicalHitSpectraFromReportProvider(
			boolean fullSpectra) {
		return new HierarchicalTraceCoberturaReportProvider<HitTrace<SourceCodeBlock>>(new HitSpectra<SourceCodeBlock>(),
				fullSpectra);
	}

	public static <K extends CountTrace<SourceCodeBlock>> HierarchicalTraceCoberturaCountReportProvider<CountTrace<SourceCodeBlock>> getHierarchicalCountSpectraFromReportProvider(
			boolean fullSpectra) {
		return new HierarchicalTraceCoberturaCountReportProvider<CountTrace<SourceCodeBlock>>(
				new CountSpectra<SourceCodeBlock>(), fullSpectra);
	}

}
