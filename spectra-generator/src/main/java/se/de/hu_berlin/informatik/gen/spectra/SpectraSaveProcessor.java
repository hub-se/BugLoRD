package se.de.hu_berlin.informatik.gen.spectra;

import java.nio.file.Paths;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.gen.spectra.internal.RunAllTestsAndGenSpectra.CmdOptions;
import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.manipulation.FilterSpectraModule;
import se.de.hu_berlin.informatik.spectra.core.manipulation.SaveSpectraModule;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.processors.AbstractConsumingProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.module.ModuleLinker;

public class SpectraSaveProcessor extends AbstractConsumingProcessor<ISpectra<SourceCodeBlock, ?>> {

	private String outputDir;

	public SpectraSaveProcessor(OptionParser options) {
		outputDir = options.isDirectory(CmdOptions.OUTPUT, false).toString();
	}

	@Override
	public void consumeItem(ISpectra<SourceCodeBlock, ?> item) throws UnsupportedOperationException {
		new ModuleLinker().append(
				// new BuildCoherentSpectraModule(),
				new SaveSpectraModule<SourceCodeBlock>(SourceCodeBlock.DUMMY,
						Paths.get(outputDir, BugLoRDConstants.SPECTRA_FILE_NAME)),
				// new TraceFileModule<SourceCodeBlock>(outputDir),
				new FilterSpectraModule<SourceCodeBlock>(INode.CoverageType.EF_EQUALS_ZERO),
				new SaveSpectraModule<SourceCodeBlock>(SourceCodeBlock.DUMMY,
						Paths.get(outputDir, BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME)))
				.submit(item);
	}

}
