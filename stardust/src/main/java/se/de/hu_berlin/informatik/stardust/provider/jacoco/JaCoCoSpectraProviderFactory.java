package se.de.hu_berlin.informatik.stardust.provider.jacoco;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.provider.jacoco.report.HierarchicalJaCoCoReportProvider;
import se.de.hu_berlin.informatik.stardust.provider.jacoco.report.JaCoCoReportProvider;
import se.de.hu_berlin.informatik.stardust.spectra.hit.HitSpectra;
import se.de.hu_berlin.informatik.stardust.spectra.hit.HitTrace;

public class JaCoCoSpectraProviderFactory {

	public static JaCoCoReportProvider<HitTrace<SourceCodeBlock>> getHitSpectraProvider(boolean fullSpectra) {
		return new JaCoCoReportProvider<HitTrace<SourceCodeBlock>>(new HitSpectra<SourceCodeBlock>(), fullSpectra);
	}

	public static HierarchicalJaCoCoReportProvider<HitTrace<SourceCodeBlock>> getHierarchicalHitSpectraProvider(
			boolean fullSpectra) {
		return new HierarchicalJaCoCoReportProvider<HitTrace<SourceCodeBlock>>(new HitSpectra<SourceCodeBlock>(),
				fullSpectra);
	}

}
