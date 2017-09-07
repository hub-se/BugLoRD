package se.de.hu_berlin.informatik.stardust.provider.jacoco;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.provider.jacoco.report.HierarchicalJaCoCoReportProvider;
import se.de.hu_berlin.informatik.stardust.provider.jacoco.report.JaCoCoReportProvider;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.count.CountSpectra;
import se.de.hu_berlin.informatik.stardust.spectra.count.CountTrace;
import se.de.hu_berlin.informatik.stardust.spectra.hit.HitSpectra;
import se.de.hu_berlin.informatik.stardust.spectra.hit.HitTrace;

public class JaCoCoSpectraProviderFactory {

	@SuppressWarnings("unchecked")
	public static <K extends HitTrace<SourceCodeBlock>> JaCoCoReportProvider<K> getHitSpectraProvider(boolean fullSpectra) {
		return new JaCoCoReportProvider<K>((ISpectra<SourceCodeBlock, K>) new HitSpectra<SourceCodeBlock>(), fullSpectra);
	}
	
	@SuppressWarnings("unchecked")
	public static <K extends CountTrace<SourceCodeBlock>> JaCoCoReportProvider<K> getCountSpectraProvider(boolean fullSpectra) {
		return new JaCoCoReportProvider<K>((ISpectra<SourceCodeBlock, K>) new CountSpectra<SourceCodeBlock>(), fullSpectra);
	}
	
	@SuppressWarnings("unchecked")
	public static <K extends HitTrace<SourceCodeBlock>> HierarchicalJaCoCoReportProvider<K> getHierarchicalHitSpectraProvider(boolean fullSpectra) {
		return new HierarchicalJaCoCoReportProvider<K>((ISpectra<SourceCodeBlock, K>) new HitSpectra<SourceCodeBlock>(), fullSpectra);
	}
	
	@SuppressWarnings("unchecked")
	public static <K extends CountTrace<SourceCodeBlock>> HierarchicalJaCoCoReportProvider<K> getHierarchicalCountSpectraProvider(boolean fullSpectra) {
		return new HierarchicalJaCoCoReportProvider<K>((ISpectra<SourceCodeBlock, K>) new CountSpectra<SourceCodeBlock>(), fullSpectra);
	}
	
}
