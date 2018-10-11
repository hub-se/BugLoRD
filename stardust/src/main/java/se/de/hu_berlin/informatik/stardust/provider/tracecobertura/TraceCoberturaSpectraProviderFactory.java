package se.de.hu_berlin.informatik.stardust.provider.tracecobertura;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.xml.CoberturaCountXMLProvider;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.xml.CoberturaXMLProvider;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.xml.HierarchicalCoberturaCountXMLProvider;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.xml.HierarchicalCoberturaXMLProvider;
import se.de.hu_berlin.informatik.stardust.provider.tracecobertura.report.HierarchicalTraceCoberturaCountReportProvider;
import se.de.hu_berlin.informatik.stardust.provider.tracecobertura.report.HierarchicalTraceCoberturaReportProvider;
import se.de.hu_berlin.informatik.stardust.provider.tracecobertura.report.TraceCoberturaCountReportProvider;
import se.de.hu_berlin.informatik.stardust.provider.tracecobertura.report.TraceCoberturaReportProvider;
import se.de.hu_berlin.informatik.stardust.spectra.count.CountSpectra;
import se.de.hu_berlin.informatik.stardust.spectra.count.CountTrace;
import se.de.hu_berlin.informatik.stardust.spectra.hit.HitSpectra;
import se.de.hu_berlin.informatik.stardust.spectra.hit.HitTrace;

public class TraceCoberturaSpectraProviderFactory {

	public static TraceCoberturaReportProvider<HitTrace<SourceCodeBlock>> getHitSpectraFromReportProvider(
			boolean fullSpectra) {
		return new TraceCoberturaReportProvider<HitTrace<SourceCodeBlock>>(new HitSpectra<SourceCodeBlock>(), fullSpectra);
	}

	public static CoberturaXMLProvider<HitTrace<SourceCodeBlock>> getHitSpectraFromXMLProvider(boolean fullSpectra) {
		return new CoberturaXMLProvider<HitTrace<SourceCodeBlock>>(new HitSpectra<SourceCodeBlock>(), fullSpectra);
	}

	public static TraceCoberturaCountReportProvider<CountTrace<SourceCodeBlock>> getCountSpectraFromReportProvider(
			boolean fullSpectra) {
		return new TraceCoberturaCountReportProvider<CountTrace<SourceCodeBlock>>(new CountSpectra<SourceCodeBlock>(),
				fullSpectra);
	}

	public static <K extends CountTrace<SourceCodeBlock>> CoberturaCountXMLProvider<CountTrace<SourceCodeBlock>> getCountSpectraFromXMLProvider(
			boolean fullSpectra) {
		return new CoberturaCountXMLProvider<CountTrace<SourceCodeBlock>>(new CountSpectra<SourceCodeBlock>(),
				fullSpectra);
	}

	public static HierarchicalTraceCoberturaReportProvider<HitTrace<SourceCodeBlock>> getHierarchicalHitSpectraFromReportProvider(
			boolean fullSpectra) {
		return new HierarchicalTraceCoberturaReportProvider<HitTrace<SourceCodeBlock>>(new HitSpectra<SourceCodeBlock>(),
				fullSpectra);
	}

	public static HierarchicalCoberturaXMLProvider<HitTrace<SourceCodeBlock>> getHierarchicalHitSpectraFromXMLProvider(
			boolean fullSpectra) {
		return new HierarchicalCoberturaXMLProvider<HitTrace<SourceCodeBlock>>(new HitSpectra<SourceCodeBlock>(),
				fullSpectra);
	}

	public static <K extends CountTrace<SourceCodeBlock>> HierarchicalTraceCoberturaCountReportProvider<CountTrace<SourceCodeBlock>> getHierarchicalCountSpectraFromReportProvider(
			boolean fullSpectra) {
		return new HierarchicalTraceCoberturaCountReportProvider<CountTrace<SourceCodeBlock>>(
				new CountSpectra<SourceCodeBlock>(), fullSpectra);
	}

	public static <K extends CountTrace<SourceCodeBlock>> HierarchicalCoberturaCountXMLProvider<CountTrace<SourceCodeBlock>> getHierarchicalCountSpectraFromXMLProvider(
			boolean fullSpectra) {
		return new HierarchicalCoberturaCountXMLProvider<CountTrace<SourceCodeBlock>>(
				new CountSpectra<SourceCodeBlock>(), fullSpectra);
	}

}
