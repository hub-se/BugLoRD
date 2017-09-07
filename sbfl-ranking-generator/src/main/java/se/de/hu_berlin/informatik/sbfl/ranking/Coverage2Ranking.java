/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.ranking;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.sbfl.ranking.modules.AddXMLCoverageToProviderAndGenerateSpectraModule;
import se.de.hu_berlin.informatik.sbfl.ranking.modules.RankingModule;
import se.de.hu_berlin.informatik.sbfl.ranking.modules.XMLCoverageWrapperModule;
import se.de.hu_berlin.informatik.sbfl.spectra.modules.TraceFileModule;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.AbstractSpectrumBasedFaultLocalizer.ComputationStrategies;
import se.de.hu_berlin.informatik.stardust.spectra.manipulation.SaveSpectraModule;
import se.de.hu_berlin.informatik.utils.files.processors.PathToFileConverter;
import se.de.hu_berlin.informatik.utils.files.processors.SearchFileOrDirProcessor;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.PipeLinker;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;

/**
 * Computes SBFL rankings or hit traces from existing Cobertura xml coverage
 * files with the support of the stardust API.
 * 
 * @author Simon Heiden
 */
final public class Coverage2Ranking {

	private Coverage2Ranking() {
		// disallow instantiation
	}

	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		INPUT("i", "input", true, "Cobertura xml file or report directory with Cobertura xml files.", true),
		OUTPUT("o", "output", true, "Path to output directory.", true),
		SIMILARITY_SBFL("sim", "similarity", false, "Whether the ranking should be based on similarity between traces.", false),
		LOCALIZERS(Option.builder("l").longOpt("localizers").optionalArg(true).hasArgs()
				.desc("A list of identifiers of Cobertura localizers (e.g. 'Tarantula', 'Jaccard', ...).").build());

		/* the following code blocks should not need to be changed */
		final private OptionWrapper option;

		// adds an option that is not part of any group
		CmdOptions(final String opt, final String longOpt, final boolean hasArg, final String description,
				final boolean required) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(required).hasArg(hasArg).desc(description).build(),
					NO_GROUP);
		}

		// adds an option that is part of the group with the specified index
		// (positive integer)
		// a negative index means that this option is part of no group
		// this option will not be required, however, the group itself will be
		CmdOptions(final String opt, final String longOpt, final boolean hasArg, final String description,
				final int groupId) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(false).hasArg(hasArg).desc(description).build(),
					groupId);
		}

		// adds the given option that will be part of the group with the given
		// id
		CmdOptions(final Option option, final int groupId) {
			this.option = new OptionWrapper(option, groupId);
		}

		// adds the given option that will be part of no group
		CmdOptions(final Option option) {
			this(option, NO_GROUP);
		}

		@Override
		public String toString() {
			return option.getOption().getOpt();
		}

		@Override
		public OptionWrapper getOptionWrapper() {
			return option;
		}
	}

	/**
	 * @param args
	 * -i (input-dir|input-file) (-r [-l loc1 loc2 ...] | -ht) -o output
	 */
	public static void main(final String[] args) {

		final OptionParser options = OptionParser.getOptions("Coverage2Ranking", false, CmdOptions.class, args);

		final Path input = Paths.get(options.getOptionValue(CmdOptions.INPUT));
		final String outputDir = options.isDirectory(CmdOptions.OUTPUT, false).toString();

		// ranking mode
		if (!input.toFile().isDirectory()) {
			Log.abort(Coverage2Ranking.class, "Input has to be a directory.");
		}
		final String[] localizers = options.getOptionValues(CmdOptions.LOCALIZERS);
		if (localizers == null) {
			Log.warn(Coverage2Ranking.class, "No localizers given. Only generating the compressed spectra.");
		}
		new PipeLinker().append(
				new SearchFileOrDirProcessor("**/*.{xml}").searchForFiles(), new PathToFileConverter(),
				new XMLCoverageWrapperModule(),
				new AddXMLCoverageToProviderAndGenerateSpectraModule(null, true).asPipe().enableTracking(50)
						.allowOnlyForcedTracks(),
				new SaveSpectraModule<SourceCodeBlock>(SourceCodeBlock.DUMMY,
						Paths.get(outputDir, "spectraCompressed.zip")),
				new TraceFileModule<SourceCodeBlock>(Paths.get(outputDir, BugLoRDConstants.FILENAME_TRACE_FILE)),
				new RankingModule<SourceCodeBlock>(options.hasOption(CmdOptions.SIMILARITY_SBFL)
						? ComputationStrategies.SIMILARITY_SBFL : ComputationStrategies.STANDARD_SBFL, outputDir,
						localizers))
				.submitAndShutdown(input);
		// if we don't wait here for the pipe to shut down, then
		// the running pipe threads are just cancelled by the JVM
		// at the end of the scope...

	}

}
