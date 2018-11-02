package se.de.hu_berlin.informatik.spectra.provider.jacoco;

import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.hit.HitSpectra;
import se.de.hu_berlin.informatik.spectra.core.hit.HitTrace;
import se.de.hu_berlin.informatik.spectra.provider.jacoco.report.HierarchicalJaCoCoReportProvider;
import se.de.hu_berlin.informatik.spectra.provider.jacoco.report.JaCoCoReportProvider;

public class JaCoCoSpectraProviderFactory {

	public static JaCoCoReportProvider<HitTrace<SourceCodeBlock>> getHitSpectraProvider(boolean fullSpectra) {
		return new JaCoCoReportProvider<HitTrace<SourceCodeBlock>>(new HitSpectra<SourceCodeBlock>(null), fullSpectra);
	}

	public static HierarchicalJaCoCoReportProvider<HitTrace<SourceCodeBlock>> getHierarchicalHitSpectraProvider(
			boolean fullSpectra) {
		return new HierarchicalJaCoCoReportProvider<HitTrace<SourceCodeBlock>>(new HitSpectra<SourceCodeBlock>(null),
				fullSpectra);
	}

}
