/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.plot;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.BugLoRDProperties;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
import se.de.hu_berlin.informatik.utils.experiments.ranking.NormalizedRanking.NormalizationStrategy;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.ConsumingProcessor;
import se.de.hu_berlin.informatik.utils.processors.basics.ThreadedListProcessor;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;

/**
 * Generates plots of the experiments' results.
 * 
 * @author SimHigh
 */
public class GeneratePlots {
	
	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		PROJECTS(Option.builder("p").longOpt("projects").hasArgs()
        		.desc("A list of projects to consider of the Defects4J benchmark. "
        		+ "Should be either 'Lang', 'Chart', 'Time', 'Closure', 'Mockito', 'Math' or "
        		+ "'super' for the super directory (only for the average plots). Set this to 'all' to "
        		+ "iterate over all projects (and the super directory).").build()),
        BUG_IDS(Option.builder("b").longOpt("bugIDs").hasArgs()
        		.desc("A list of numbers indicating the ids of buggy project versions to consider. "
        		+ "Value ranges differ based on the project. Set this to 'all' to "
        		+ "iterate over all bugs in a project.").build()),
 
        SINGLE_PLOTS("se", "singleElementPlots", false, "Whether to plot single plots for each Defects4J element "
        		+ "that show the ranks of faulty code lines for the given localizer(s).", false),
        AVERAGE_PLOTS("a", "averagePlots", false, "Whether to plot average plots for each Defects4J project.", false),
        
        SUFFIX("s", "suffix", true, "A suffix to append to the plot sub-directory.", false),
        
        LOCALIZERS(Option.builder("l").longOpt("localizers").required(false)
				.hasArgs().desc("A list of localizers (e.g. 'Tarantula', 'Jaccard', ...). If not set, "
						+ "the localizers will be retrieved from the properties file.").build()),
        
        CROSS_VALIDATION_SEED("cv", "cvSeed", true, "A seed to use for generating the buckets.", false),
        BUCKET_COUNT("bc", "bucketCount", true, "The number of buckets to create (default: 10).", false),

        STRATEGY("strat", "parserStrategy", ParserStrategy.class, ParserStrategy.AVERAGE_CASE, 
				"What strategy should be used when encountering a range of equal rankings.", false),
        
        NORMALIZED("n", "normalized", NormalizationStrategy.class, NormalizationStrategy.ReciprocalRankWorst, 
				"Indicates whether the ranking should be normalized before combination.", false),
        
        OUTPUT("o", "outputDir", true, "Main plot output directory.", false), 
        
        CSV_PLOTS("c", "csvPlots", false, "Whether to generate plots from existing csv files.", false);

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
				final boolean hasArg, final String description, int groupId) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(false).
					hasArg(hasArg).desc(description).build(), groupId);
		}

		//adds an option that may have arguments from a given set (Enum)
		<T extends Enum<T>> CmdOptions(final String opt, final String longOpt, 
				Class<T> valueSet, T defaultValue, final String description, final boolean required) {
			if (defaultValue == null) {
				this.option = new OptionWrapper(
						Option.builder(opt).longOpt(longOpt).required(required).
						hasArgs().desc(description + " Possible arguments: " +
								Misc.enumToString(valueSet) + ".").build(), NO_GROUP);
			} else {
				this.option = new OptionWrapper(
						Option.builder(opt).longOpt(longOpt).required(required).
						hasArg(true).desc(description + " Possible arguments: " +
								Misc.enumToString(valueSet) + ". Default: " + 
								defaultValue.toString() + ".").build(), NO_GROUP);
			}
		}
		
		//adds the given option that will be part of the group with the given id
		CmdOptions(Option option, int groupId) {
			this.option = new OptionWrapper(option, groupId);
		}
		
		//adds the given option that will be part of no group
		CmdOptions(Option option) {
			this(option, NO_GROUP);
		}

		@Override public String toString() { return option.getOption().getOpt(); }
		@Override public OptionWrapper getOptionWrapper() { return option; }
	}
	
	/**
	 * @param args
	 * -p project -b bugID
	 */
	public static void main(String[] args) {
		
		OptionParser options = OptionParser.getOptions("GeneratePlots", true, CmdOptions.class, args);
		
		options.assertAtLeastOneOptionSet(CmdOptions.AVERAGE_PLOTS, CmdOptions.SINGLE_PLOTS, CmdOptions.CSV_PLOTS);
		
		String[] projects = options.getOptionValues(CmdOptions.PROJECTS);
		boolean allProjects = false;
		if (projects != null) {
			allProjects = projects[0].equals("all");
		} else {
			projects = new String[0];
		}
		
		String[] ids = options.getOptionValues(CmdOptions.BUG_IDS);
		boolean allIDs = false;
		if (ids != null) {
			allIDs = ids[0].equals("all");
		} else {
			ids = new String[0];
		}
		
		String output = options.getOptionValue(CmdOptions.OUTPUT, null);
		if (output != null && (new File(output)).isFile()) {
			Log.abort(GeneratePlots.class, "Given output path '%s' is a file.", output);
		}
		
		ParserStrategy strategy = options.getOptionValue(CmdOptions.STRATEGY, 
				ParserStrategy.class, ParserStrategy.AVERAGE_CASE, true);
		
		NormalizationStrategy normStrategy = null;
		if (options.hasOption(CmdOptions.NORMALIZED)) {
			normStrategy = options.getOptionValue(CmdOptions.NORMALIZED, 
					NormalizationStrategy.class, NormalizationStrategy.ReciprocalRankWorst, true);
		}
		
		String[] localizers = options.getOptionValues(CmdOptions.LOCALIZERS);
		if (localizers == null) {
			localizers = BugLoRD.getValueOf(BugLoRDProperties.LOCALIZERS).split(" ");
		}
				
		int threadCount = options.getNumberOfThreads();

		if (allProjects) {
			projects = Defects4J.getAllProjects();
		}
		
		String suffix = options.getOptionValue(CmdOptions.SUFFIX, null);
		
		if (options.hasOption(CmdOptions.SINGLE_PLOTS)) {	
			//iterate over all projects
			for (String project : projects) {
				if (allIDs) {
					ids = Defects4J.getAllBugIDs(project); 
				}

				for (String localizer : localizers) {
					String[] temp = { localizer };
					new ThreadedListProcessor<String>(threadCount > ids.length ? ids.length : threadCount, 
							new PlotSingleElementEH(suffix, project, temp, output, normStrategy))
					.submit(Arrays.asList(ids));
				}
			}
		}
		
		int threads = threadCount / 3;
		threads = threads < 1 ? 1 : threads;
		
		String seedOption = options.getOptionValue(CmdOptions.CROSS_VALIDATION_SEED, null);

		if (options.hasOption(CmdOptions.AVERAGE_PLOTS)) {
			if (seedOption == null) {
				for (String project : projects) {
					ConsumingProcessor<List<String>> processor = new ThreadedListProcessor<String>(3, 
							new PlotAverageEH(suffix, strategy, project, output, threads, normStrategy));
					// combine all localizers with all lm rankings
					processor.submit(Arrays.asList(localizers));
					
					// combine all lm rankings with other lm rankings
					// this includes some unnecessary combinations... TODO
					// List<String> identifiers = getAllLMRankingFileIdentifiers();
					// processor.submit(identifiers);
				}
			} else {
				Long seed = Long.valueOf(seedOption);
				int bc = Integer.valueOf(options.getOptionValue(CmdOptions.BUCKET_COUNT, "10"));
				for (String project : projects) {
					new ThreadedListProcessor<String>(3, 
							new PlotAverageBucketsEH(suffix, strategy, seed, bc,
									project, output, threads, normStrategy))
					.submit(Arrays.asList(localizers));
				}
			}
		}
		
		if (options.hasOption(CmdOptions.CSV_PLOTS)) {
			for (String project : projects) {
				new ThreadedListProcessor<String>(threadCount, 
						new PlotFromCsvEH(suffix, project, output))
				.submit(Arrays.asList(localizers));
			}
		}

	}
	
	public static List<String> getAllLMRankingFileIdentifiers() {
		File allLMRankingFileNamesFile = new File(BugLoRDConstants.LM_RANKING_FILENAMES_FILE);
		
		List<String> allRankingFileNames = FileUtils.readFile2List(allLMRankingFileNamesFile.toPath());
		return allRankingFileNames;
	}
	
}
