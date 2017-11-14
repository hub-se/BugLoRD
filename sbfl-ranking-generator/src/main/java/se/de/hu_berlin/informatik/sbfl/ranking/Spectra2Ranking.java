/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.ranking;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.sbfl.ranking.modules.RankingFromTraceFileModule;
import se.de.hu_berlin.informatik.sbfl.ranking.modules.RankingModule;
import se.de.hu_berlin.informatik.sbfl.spectra.modules.TraceFileModule;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.AbstractSpectrumBasedFaultLocalizer.ComputationStrategies;
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.manipulation.BuildBlockSpectraModule;
import se.de.hu_berlin.informatik.stardust.spectra.manipulation.FilterSpectraModule;
import se.de.hu_berlin.informatik.stardust.spectra.manipulation.ReadSpectraModule;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.sockets.module.ModuleLinker;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;


/**
 * Computes SBFL rankings or hit traces from an existing spectra file 
 * with the support of the stardust API.
 * 
 * @author Simon Heiden
 */
final public class Spectra2Ranking {
	
	private Spectra2Ranking() {
		//disallow instantiation
	}

	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		INPUT("i", "input", true, "A compressed spectra file.", true),
		OUTPUT("o", "output", true, "Path to output directory.", true),
		FILTER("f", "filterNodes", false, "Whether to remove irrelevant nodes, i.e. "
				+ "nodes that were not touched by any failing trace.", false),
		CONDENSE("c", "condenseNodes", false, "Whether to combine several lines "
				+ "with equal trace involvement to larger blocks.", false),
		SIMILARITY_SBFL("sim", "similarity", false, "Whether the ranking should be based on similarity between traces.", false),
		LOCALIZERS(Option.builder("l").longOpt("localizers").optionalArg(true)
				.hasArgs().desc("A list of identifiers of Cobertura localizers "
						+ "(e.g. 'Tarantula', 'Jaccard', ...).").build());
		
		/* the following code blocks should not need to be changed */
		final private OptionWrapper option;

		//adds an option that is not part of any group
		CmdOptions(final String opt, final String longOpt, 
				final boolean hasArg, final String description, final boolean required) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(required).
					hasArg(hasArg).desc(description).build(), NO_GROUP);
		}
		
		//adds an option that is part of the group with the specified index (positive integer)
		//a negative index means that this option is part of no group
		//this option will not be required, however, the group itself will be
		CmdOptions(final String opt, final String longOpt, 
				final boolean hasArg, final String description, final int groupId) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(false).
					hasArg(hasArg).desc(description).build(), groupId);
		}
		
		//adds the given option that will be part of the group with the given id
		CmdOptions(final Option option, final int groupId) {
			this.option = new OptionWrapper(option, groupId);
		}
		
		//adds the given option that will be part of no group
		CmdOptions(final Option option) {
			this(option, NO_GROUP);
		}

		@Override public String toString() { return option.getOption().getOpt(); }
		@Override public OptionWrapper getOptionWrapper() { return option; }
	}
	
	/**
	 * @param args
	 * -i input-file [-l loc1 loc2 ...] -o output
	 */
	public static void main(final String[] args) {
		
		final OptionParser options = OptionParser.getOptions("Spectra2Ranking", false, CmdOptions.class, args);

		generateRanking(options.getOptionValue(CmdOptions.INPUT), 
				options.getOptionValue(CmdOptions.OUTPUT), 
				options.getOptionValues(CmdOptions.LOCALIZERS),
				options.hasOption(CmdOptions.FILTER),
				options.hasOption(CmdOptions.CONDENSE),
				options.hasOption(CmdOptions.SIMILARITY_SBFL) ? ComputationStrategies.SIMILARITY_SBFL : ComputationStrategies.STANDARD_SBFL);
		
	}

	/**
	 * Convenience method. Generates a ranking from the given spectra file. Checks the inputs
	 * for correctness and aborts the application with an error message if one of
	 * the inputs is not correct.
	 * @param spectraFileOption
	 * a compressed spectra file
	 * @param rankingDir
	 * path to the main ranking directory
	 * @param localizers
	 * an array of String representation of fault localizers
	 * as used by STARDUST
	 * @param removeIrrelevantNodes
	 * whether to remove nodes that were not touched by any failed traces
	 * @param condenseNodes
	 * whether to combine several lines with equal trace involvement
	 * @param strategy
	 * the strategy to use for computation of the rankings
	 */
	public static void generateRanking(
			final String spectraFileOption, final String rankingDir, final String[] localizers, 
			final boolean removeIrrelevantNodes, final boolean condenseNodes, ComputationStrategies strategy) {
		final Path spectraFile = FileUtils.checkIfAnExistingFile(null, spectraFileOption);
		if (spectraFile == null) {
			Log.abort(Spectra2Ranking.class, "'%s' is not an existing file.", spectraFileOption);
		}
		final Path outputDir = FileUtils.checkIfNotAnExistingFile(null, rankingDir);
		if (outputDir == null) {
			Log.abort(Spectra2Ranking.class, "'%s' is not a directory.", rankingDir);
		}
		if (localizers == null) {
			Log.abort(Spectra2Ranking.class, "No localizers given.");
		}
		generateRankingForCheckedInputs(spectraFile, outputDir.toString(), 
				localizers, removeIrrelevantNodes, condenseNodes, strategy);
	}

	/**
	 * Generates the ranking. Assumes that inputs have been checked to be correct.
	 * @param spectraFileOption
	 * a compressed spectra file
	 * @param rankingDir
	 * path to the main ranking directory
	 * @param localizers
	 * an array of String representation of fault localizers
	 * as used by STARDUST
	 * @param removeIrrelevantNodes
	 * whether to remove nodes that were not touched by any failed traces
	 * @param condenseNodes
	 * whether to combine several lines with equal trace involvement
	 * @param strategy
	 * the strategy to use for computation of the rankings
	 */
	private static void generateRankingForCheckedInputs(final Path spectraFile, 
			final String outputDir, final String[] localizers, 
			final boolean removeIrrelevantNodes, final boolean condenseNodes, ComputationStrategies strategy) {
		ModuleLinker linker = new ModuleLinker()
				.append(new ReadSpectraModule<SourceCodeBlock>(SourceCodeBlock.DUMMY));
		if (removeIrrelevantNodes) {
			linker.append(new FilterSpectraModule<SourceCodeBlock>(INode.CoverageType.EF_EQUALS_ZERO));
		}
		if (condenseNodes) {
			linker.append(new BuildBlockSpectraModule());
		}
		linker.append(new TraceFileModule<SourceCodeBlock>(Paths.get(outputDir), BugLoRDConstants.FILENAME_TRACE_FILE_PREFIX));
		linker.append(new RankingModule<SourceCodeBlock>(strategy, outputDir, localizers))
		.submit(spectraFile);
	}
	
	
	
	/**
	 * Convenience method. Generates a ranking from the given spectra file. Checks the inputs
	 * for correctness and aborts the application with an error message if one of
	 * the inputs is not correct.
	 * @param traceFile
	 * a trace file
	 * @param rankingDir
	 * path to the main ranking directory
	 * @param localizers
	 * an array of String representation of fault localizers
	 * as used by STARDUST
	 * @param strategy
	 * the strategy to use for computation of the rankings
	 */
	public static void generateRankingFromTraceFile(
			final String traceFile, final String rankingDir, final String[] localizers, 
			ComputationStrategies strategy) {
		final Path traceFilePath = FileUtils.checkIfAnExistingFile(null, traceFile);
		if (traceFilePath == null) {
			Log.abort(Spectra2Ranking.class, "'%s' is not an existing file.", traceFile);
		}
		final Path outputDir = FileUtils.checkIfNotAnExistingFile(null, rankingDir);
		if (outputDir == null) {
			Log.abort(Spectra2Ranking.class, "'%s' is not a directory.", rankingDir);
		}
		if (localizers == null) {
			Log.abort(Spectra2Ranking.class, "No localizers given.");
		}
		generateRankingFromTraceFileForCheckedInputs(traceFilePath, outputDir.toString(), 
				localizers, strategy);
	}

	/**
	 * Generates the ranking. Assumes that inputs have been checked to be correct.
	 * @param traceFilePath
	 * a trace file
	 * @param rankingDir
	 * path to the main ranking directory
	 * @param localizers
	 * an array of String representation of fault localizers
	 * as used by STARDUST
	 * @param strategy
	 * the strategy to use for computation of the rankings
	 */
	private static void generateRankingFromTraceFileForCheckedInputs(final Path traceFilePath, 
			final String outputDir, final String[] localizers, ComputationStrategies strategy) {
		new RankingFromTraceFileModule<SourceCodeBlock>(strategy, outputDir, localizers)
		.submit(traceFilePath.toString());
	}
	
}
