package se.de.hu_berlin.informatik.stardust.provider.cobertura;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.report.CoberturaReportProvider;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.report.HierarchicalCoberturaReportProvider;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.xml.CoberturaXMLProvider;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.xml.HierarchicalCoberturaXMLProvider;
import se.de.hu_berlin.informatik.stardust.spectra.count.CountSpectra;
import se.de.hu_berlin.informatik.stardust.spectra.count.CountTrace;
import se.de.hu_berlin.informatik.stardust.spectra.hit.HitSpectra;
import se.de.hu_berlin.informatik.stardust.spectra.hit.HitTrace;

public class CoberturaSpectraProviderFactory {

	public static CoberturaReportProvider<HitTrace<SourceCodeBlock>> getHitSpectraFromReportProvider(
			boolean fullSpectra) {
		return new CoberturaReportProvider<HitTrace<SourceCodeBlock>>(new HitSpectra<SourceCodeBlock>(), fullSpectra);
	}

	public static CoberturaXMLProvider<HitTrace<SourceCodeBlock>> getHitSpectraFromXMLProvider(boolean fullSpectra) {
		return new CoberturaXMLProvider<HitTrace<SourceCodeBlock>>(new HitSpectra<SourceCodeBlock>(), fullSpectra);
	}

	public static CoberturaReportProvider<CountTrace<SourceCodeBlock>> getCountSpectraFromReportProvider(
			boolean fullSpectra) {
		return new CoberturaReportProvider<CountTrace<SourceCodeBlock>>(new CountSpectra<SourceCodeBlock>(),
				fullSpectra);
	}

	public static <K extends CountTrace<SourceCodeBlock>> CoberturaXMLProvider<CountTrace<SourceCodeBlock>> getCountSpectraFromXMLProvider(
			boolean fullSpectra) {
		return new CoberturaXMLProvider<CountTrace<SourceCodeBlock>>(new CountSpectra<SourceCodeBlock>(), fullSpectra);
	}

	public static HierarchicalCoberturaReportProvider<HitTrace<SourceCodeBlock>> getHierarchicalHitSpectraFromReportProvider(
			boolean fullSpectra) {
		return new HierarchicalCoberturaReportProvider<HitTrace<SourceCodeBlock>>(new HitSpectra<SourceCodeBlock>(),
				fullSpectra);
	}

	public static HierarchicalCoberturaXMLProvider<HitTrace<SourceCodeBlock>> getHierarchicalHitSpectraFromXMLProvider(
			boolean fullSpectra) {
		return new HierarchicalCoberturaXMLProvider<HitTrace<SourceCodeBlock>>(new HitSpectra<SourceCodeBlock>(),
				fullSpectra);
	}

	public static <K extends CountTrace<SourceCodeBlock>> HierarchicalCoberturaReportProvider<CountTrace<SourceCodeBlock>> getHierarchicalCountSpectraFromReportProvider(
			boolean fullSpectra) {
		return new HierarchicalCoberturaReportProvider<CountTrace<SourceCodeBlock>>(new CountSpectra<SourceCodeBlock>(),
				fullSpectra);
	}

	public static <K extends CountTrace<SourceCodeBlock>> HierarchicalCoberturaXMLProvider<CountTrace<SourceCodeBlock>> getHierarchicalCountSpectraFromXMLProvider(
			boolean fullSpectra) {
		return new HierarchicalCoberturaXMLProvider<CountTrace<SourceCodeBlock>>(new CountSpectra<SourceCodeBlock>(),
				fullSpectra);
	}

}
