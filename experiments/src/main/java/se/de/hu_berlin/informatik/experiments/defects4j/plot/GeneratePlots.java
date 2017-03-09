/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.plot;

import java.io.File;
import java.util.Arrays;
import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.BugLoRDProperties;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
import se.de.hu_berlin.informatik.utils.experiments.ranking.NormalizedRanking.NormalizationStrategy;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
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
 
        SINGLE_PLOTS("s", "singleElementPlots", false, "Whether to plot single plots for each Defects4J element "
        		+ "that show the ranks of faulty code lines for the given localizer(s).", false),
        AVERAGE_PLOTS("a", "averagePlots", false, "Whether to plot average plots for each Defects4J project.", false),
        
        LOCALIZERS(Option.builder("l").longOpt("localizers").required(false)
				.hasArgs().desc("A list of localizers (e.g. 'Tarantula', 'Jaccard', ...). If not set, "
						+ "the locliazers will be retrieved from the properties file.").build()),
        
        CROSS_VALIDATION_SEED("cv", "cvSeed", true, "A seed to use for generating the buckets.", false),
        BUCKET_COUNT("bc", "bucketCount", true, "The number of buckets to create (default: 10).", false),

        STRATEGY("strat", "parserStrategy", true, "What strategy should be used when encountering a range of"
				+ "equal rankings. Options are: 'BEST', 'WORST', 'NOCHANGE' and 'AVERAGE'. Default is 'AVERAGE'.", false),
        
        NORMALIZED(Option.builder("n").longOpt("normalized").hasArg().optionalArg(true)
				.desc("Indicates whether the ranking should be normalized before combination. May take the "
						+ "type of normalization strategy as an argument. Available strategies include: "
						+ "'01rankingvalue', '01rank', '01worstrank', '01bestrank', '01meanrank', "
						+ "'rprank', 'rpworstrank', 'rpbestrank', 'rpmeanrank'. If no argument is given, "
						+ "'rpmeanrank' will be used.").required(false).build(), 0),
        
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
		
		ParserStrategy strategy = ParserStrategy.AVERAGE_CASE;
		if (options.hasOption(CmdOptions.STRATEGY)) {
			switch(options.getOptionValue(CmdOptions.STRATEGY)) {
			case Plotter.STRAT_BEST:
				strategy = ParserStrategy.BEST_CASE;
				break;
			case Plotter.STRAT_WORST:
				strategy = ParserStrategy.WORST_CASE;
				break;
			case Plotter.STRAT_AVERAGE:
				strategy = ParserStrategy.AVERAGE_CASE;
				break;
			case Plotter.STRAT_NOCHANGE:
				strategy = ParserStrategy.NO_CHANGE;
				break;
			default:
				Log.abort(GeneratePlots.class, "Unknown strategy: '%s'", options.getOptionValue(CmdOptions.STRATEGY));
			}
		}
		
		NormalizationStrategy normStrategy = null;
		if (options.hasOption(CmdOptions.NORMALIZED)) {
			if (options.getOptionValue(CmdOptions.NORMALIZED) == null) {
				normStrategy = NormalizationStrategy.ReciprocalRankMean;
			} else {
				normStrategy = NormalizationStrategy
						.getStrategyFromString(options.getOptionValue(CmdOptions.NORMALIZED));
				if (normStrategy == null) {
					Log.abort(GeneratePlots.class, "Unknown normalization strategy: '%s'", options.getOptionValue(CmdOptions.NORMALIZED));
				}
			}
		}
		
		String[] localizers = options.getOptionValues(CmdOptions.LOCALIZERS);
		if (localizers == null) {
			localizers = BugLoRD.getValueOf(BugLoRDProperties.LOCALIZERS).split(" ");
		}
				
		int threadCount = options.getNumberOfThreads();

		if (allProjects) {
			projects = Defects4J.getAllProjects();
		}
		
		if (options.hasOption(CmdOptions.SINGLE_PLOTS)) {	
			//iterate over all projects
			for (String project : projects) {
				if (allIDs) {
					ids = Defects4J.getAllBugIDs(project); 
				}

				for (String localizer : localizers) {
					String[] temp = { localizer };
					new ThreadedListProcessor<String>(threadCount > ids.length ? ids.length : threadCount, 
							new PlotSingleElementEH(project, temp, output, normStrategy))
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
					new ThreadedListProcessor<String>(3, 
							new PlotAverageEH(strategy,project, output, threads, normStrategy))
					.submit(Arrays.asList(localizers));
				}
			} else {
				Long seed = Long.valueOf(seedOption);
				int bc = Integer.valueOf(options.getOptionValue(CmdOptions.BUCKET_COUNT, "10"));
				for (String project : projects) {
					new ThreadedListProcessor<String>(3, 
							new PlotAverageBucketsEH(strategy, seed, bc,
									project, output, threads, normStrategy))
					.submit(Arrays.asList(localizers));
				}
			}
		}
		
		if (options.hasOption(CmdOptions.CSV_PLOTS)) {
			for (String project : projects) {
				new ThreadedListProcessor<String>(threadCount, 
						new PlotFromCsvEH(project, output))
				.submit(Arrays.asList(localizers));
			}
		}

	}
	
}
