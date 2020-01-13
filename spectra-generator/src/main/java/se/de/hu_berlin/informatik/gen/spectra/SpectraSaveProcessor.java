package se.de.hu_berlin.informatik.gen.spectra;

import java.nio.file.Paths;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.gen.spectra.internal.RunAllTestsAndGenSpectra.CmdOptions;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.manipulation.BuildCoherentSpectraModule;
import se.de.hu_berlin.informatik.spectra.core.manipulation.SaveSpectraModule;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.processors.AbstractConsumingProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.module.ModuleLinker;

public class SpectraSaveProcessor extends AbstractConsumingProcessor<ISpectra<SourceCodeBlock, ?>> {

	private final String outputDir;
	private final boolean condenseNodes;

	public SpectraSaveProcessor(OptionParser options) {
		outputDir = options.isDirectory(CmdOptions.OUTPUT, false).toString();
		condenseNodes = options.hasOption(CmdOptions.CONDENSE_NODES);
	}

	@Override
	public void consumeItem(ISpectra<SourceCodeBlock, ?> item) throws UnsupportedOperationException {
		ModuleLinker linker = new ModuleLinker();
		if (condenseNodes) {
			linker.append(new BuildCoherentSpectraModule());
		}
		linker.append(
				// new BuildCoherentSpectraModule(),
                new SaveSpectraModule<>(Paths.get(outputDir, BugLoRDConstants.SPECTRA_FILE_NAME))
				// new TraceFileModule<SourceCodeBlock>(outputDir),
//				new FilterSpectraModule<SourceCodeBlock>(INode.CoverageType.EF_EQUALS_ZERO),
//                new SaveSpectraModule<>(Paths.get(outputDir, BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME))
                )
				.submit(item);
	}

}
