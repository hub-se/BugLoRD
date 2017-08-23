/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JBuggyFixedEntity;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.BugLoRDProperties;
import se.de.hu_berlin.informatik.experiments.defects4j.calls.ERQueryLMRankingsEH;
import se.de.hu_berlin.informatik.utils.files.processors.FileToStringListReader;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.basics.ThreadedProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.PipeLinker;

/**
 * Runs experiments with different LMs
 * 
 * @author SimHigh
 */
public class RunBenchmark {

	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		SUFFIX("s", "suffix", true, "The suffix used for creating the ranking directory.", false),
		LM("lm", "globalLMFile", true, "Path to a file with language model binary (kenLM) paths.", true),

		LOCALIZERS(Option.builder("l").longOpt("localizers").required(false).hasArgs().desc(
				"A list of localizers (e.g. 'Tarantula', 'Jaccard', ...). If not set, "
						+ "the localizers will be retrieved from the properties file.")
				.build());

//		CROSS_VALIDATION_SEED("cv", "cvSeed", true, "A seed to use for generating the buckets.", false),
//		BUCKET_COUNT("bc", "bucketCount", true, "The number of buckets to create (default: 10).", false),
//
//		STRATEGY("strat", "parserStrategy", true,
//				"What strategy should be used when encountering a range of"
//						+ "equal rankings. Options are: 'BEST', 'WORST', 'NOCHANGE' and 'AVERAGE'. Default is 'AVERAGE'.",
//				false),
//
//		OUTPUT("o", "outputDir", true, "Main plot output directory.", false),
//
//		NORMALIZED(Option.builder("n").longOpt("normalized").hasArg().optionalArg(true).desc(
//				"Indicates whether the ranking should be normalized before combination. May take the "
//						+ "type of normalization strategy as an argument. Available strategies include: "
//						+ "'01rankingvalue', '01rank', '01worstrank', '01bestrank', '01meanrank', "
//						+ "'rprank', 'rpworstrank', 'rpbestrank', 'rpmeanrank'. If no argument is given, "
//						+ "'rpworstrank' will be used.")
//				.required(false).build(), 0);

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
				int groupId) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(false).hasArg(hasArg).desc(description).build(),
					groupId);
		}

		// adds the given option that will be part of the group with the given
		// id
		CmdOptions(Option option, int groupId) {
			this.option = new OptionWrapper(option, groupId);
		}

		// adds the given option that will be part of no group
		CmdOptions(Option option) {
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
	 * command line arguments
	 */
	public static void main(String[] args) {

		OptionParser options = OptionParser.getOptions("RunBenchmark", true, CmdOptions.class, args);

//		String output = options.getOptionValue(CmdOptions.OUTPUT, null);
//		if (output != null && (new File(output)).isFile()) {
//			Log.abort(RunBenchmark.class, "Given output path '%s' is a file.", output);
//		}
//		if (output == null) {
//			output = Defects4J.getValueOf(Defects4JProperties.PLOT_DIR);
//		}

//		ParserStrategy strategy = options.getOptionValue(CmdOptions.STRATEGY, 
//				ParserStrategy.class, ParserStrategy.AVERAGE_CASE, true);
//		
//		NormalizationStrategy normStrategy = null;
//		if (options.hasOption(CmdOptions.NORMALIZED)) {
//			normStrategy = options.getOptionValue(CmdOptions.NORMALIZED, 
//					NormalizationStrategy.class, NormalizationStrategy.ReciprocalRankWorst, true);
//		}

		String[] localizers = options.getOptionValues(CmdOptions.LOCALIZERS);
		if (localizers == null) {
			localizers = BugLoRD.getValueOf(BugLoRDProperties.LOCALIZERS).split(" ");
		}

		int threadCount = options.getNumberOfThreads();
//		int thirdOfThreads = threadCount / 3 < 1 ? 1 : threadCount / 3;

		String suffix = options.getOptionValue(CmdOptions.SUFFIX, null);

		/*
		 * #====================================================================
		 * # query sentences with different lm
		 * #====================================================================
		 */

		Path globalLMFile = options.isFile(CmdOptions.LM, true);
		List<String> lms = new FileToStringListReader().submit(globalLMFile).getResult();
		String[] projects = { "Mockito", "Closure", "Time", "Math", "Lang", "Chart" };

		for (String globalLM : lms) {
			Log.warn(RunBenchmark.class, "Starting with '%s'...", globalLM);
			if (!new File(globalLM).exists()) {
				Log.err(RunBenchmark.class, "'%s' does not exist.", globalLM);
				continue;
			}

			PipeLinker linker = new PipeLinker();
			linker.append(
					new ThreadedProcessor<>(threadCount,
							new ERQueryLMRankingsEH(suffix, globalLM)));

			// iterate over all projects
			for (String project : projects) {
				String[] ids = Defects4J.getAllBugIDs(project);
				for (String id : ids) {
					linker.submit(new Defects4JBuggyFixedEntity(project, id));
				}
			}
			linker.shutdown();
		}
		
//		/*
//		 * #====================================================================
//		 * # run the plotter
//		 * #====================================================================
//		 */
//		
//		String seedOption = options.getOptionValue(CmdOptions.CROSS_VALIDATION_SEED, null);
//
//		new ThreadedListProcessor<String>(3,
//				new PlotAverageEH(suffix, strategy, "super", output, thirdOfThreads, normStrategy))
//		.submit(Arrays.asList(localizers));
//
//		if (seedOption != null) {
//			Long seed = Long.valueOf(seedOption);
//			int bc = Integer.valueOf(options.getOptionValue(CmdOptions.BUCKET_COUNT, "10"));
//			new ThreadedListProcessor<String>(3, new PlotAverageBucketsEH(suffix, strategy, seed, bc, "super",
//					output, thirdOfThreads, normStrategy)).submit(Arrays.asList(localizers));
//		}

		Log.out(RunBenchmark.class, "All done!");

	}

}
