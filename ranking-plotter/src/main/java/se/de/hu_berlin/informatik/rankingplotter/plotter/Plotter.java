/*
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.rankingplotter.modules.AverageplotCSVGeneratorModule;
import se.de.hu_berlin.informatik.rankingplotter.modules.CsvToAverageStatisticsCollectionModule;
import se.de.hu_berlin.informatik.rankingplotter.modules.CsvToSingleStatisticsCollectionModule;
import se.de.hu_berlin.informatik.rankingplotter.modules.DataAdderModule;
import se.de.hu_berlin.informatik.rankingplotter.modules.AveragePlotLaTexGeneratorModule;
import se.de.hu_berlin.informatik.rankingplotter.modules.RankingAveragerModule;
import se.de.hu_berlin.informatik.rankingplotter.modules.SinglePlotCSVGeneratorModule;
import se.de.hu_berlin.informatik.rankingplotter.modules.SinglePlotLaTexGeneratorModule;
import se.de.hu_berlin.informatik.utils.experiments.ranking.NormalizedRanking.NormalizationStrategy;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.files.processors.SearchFileOrDirToListProcessor;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.basics.CollectionSequencer;
import se.de.hu_berlin.informatik.utils.processors.basics.ItemCollector;
import se.de.hu_berlin.informatik.utils.processors.basics.ThreadedProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.module.ModuleLinker;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.PipeLinker;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;


/**
 * Plots SBFL and NLFL rankings.
 * 
 * @author Simon Heiden
 */
public class Plotter {
	
	public final static String STRAT_NOCHANGE = "NOCHANGE";
	public final static String STRAT_BEST = "BEST";
	public final static String STRAT_AVERAGE = "AVERAGE";
	public final static String STRAT_WORST = "WORST";
	
	public enum ParserStrategy { 
		NO_CHANGE, 
		BEST_CASE, 
		AVERAGE_CASE, 
		WORST_CASE;

		@Override
		public String toString() {
			switch(this) {
			case NO_CHANGE:
				return STRAT_NOCHANGE;
			case BEST_CASE:
				return STRAT_BEST;
			case AVERAGE_CASE:
				return STRAT_AVERAGE;
			case WORST_CASE:
				return STRAT_WORST;
			default:
				throw new UnsupportedOperationException("Not implemented.");
			}
		}
	}
	
	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		INPUT("i", "input", true, "Path to ranking directory or directory with defects4J projects.", true),
		
		NORMAL_PLOT(Option.builder("p").longOpt("plot").hasArgs().optionalArg(true)
				.desc("Create plots of the specified subfolders of the input ranking directory "
						+ "(if given), or of all subfolders (if none are given).").build(), 0),
		AVERAGE_PLOT(Option.builder("a").longOpt("averagePlot").hasArgs()
				.desc("Parse all Rankings of the specified types (e.g. 'tarantula') that are found "
						+ "anywhere in the input directory and plot the averages.").build(), 0),
		
		SUFFIX("s", "suffix", true, "A suffix to append to the plot sub-directory.", false),
	
		OUTPUT(Option.builder("o").longOpt("output").hasArgs().numberOfArgs(2).required()
				.desc("Path to output directory and a prefix for the output files (two arguments).").build()),
	
		STRATEGY("strat", "parserStrategy", ParserStrategy.class, ParserStrategy.AVERAGE_CASE, 
				"What strategy should be used when encountering a range of equal rankings.", false),

		GLOBAL_PERCENTAGES(Option.builder("gp").longOpt("globalPercentages")
        		.hasArgs().desc("Global Percentages (with multiple arguments).")
        		.build()),

		NORMALIZED("n", "normalized", NormalizationStrategy.class, NormalizationStrategy.ReciprocalRankWorst, 
				"Indicates whether the ranking should be normalized before combination.", false);
		
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
	 * -i input-dir [(-p|-a) loc1 loc2 ...] -o output-dir output-prefix [-r range] [-l] [-pdf] [-png] [-u unranked-file] [-m ranked-file] [-s] [-z]
	 */
	public static void main(String[] args) {
		
		OptionParser options = OptionParser.getOptions("Plotter", true, CmdOptions.class, args);
		
		//directory with SBFL ranking 	(format: relative/path/To/File:lineNumber: ranking)
		Path inputDir = options.isDirectory(CmdOptions.INPUT, true);
		
		//output file path
		String outputDir = options.isDirectory(CmdOptions.OUTPUT, false).toString();
		
		String outputPrefix = "";
		if (options.getOptionValues(CmdOptions.OUTPUT).length > 1) {
			outputPrefix = options.getOptionValues(CmdOptions.OUTPUT)[1];
		}
		
		ParserStrategy strategy = options.getOptionValue(CmdOptions.STRATEGY, 
				ParserStrategy.class, ParserStrategy.AVERAGE_CASE, true);
		
		NormalizationStrategy normStrategy = null;
		if (options.hasOption(CmdOptions.NORMALIZED)) {
			normStrategy = options.getOptionValue(CmdOptions.NORMALIZED, 
					NormalizationStrategy.class, NormalizationStrategy.ReciprocalRankWorst, true);
			outputDir += "_" + normStrategy;
		}
		
		String suffix = options.getOptionValue(CmdOptions.SUFFIX, null);
		
		if (options.hasOption(CmdOptions.NORMAL_PLOT)) {
			
			List<Path> changesFiles = new SearchFileOrDirToListProcessor("**/" + BugLoRDConstants.CHANGES_FILE_NAME, true)
					.searchForFiles()
					.submit(inputDir)
					.getResult();
			
			for (Path changesFile : changesFiles) {
				BuggyFixedEntity<?> entity = new WorkDataDummyBuggyFixedEntity(changesFile.getParent());
				
				String[] localizers = options.getOptionValues(CmdOptions.NORMAL_PLOT);
				if (localizers == null) {
					List<Path> localizerDirs = new SearchFileOrDirToListProcessor("**/" + (suffix == null ? 
							BugLoRDConstants.DIR_NAME_RANKING : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix) + "/*", true)
							.searchForDirectories()
							.skipSubTreeAfterMatch()
							.submit(changesFile.toAbsolutePath().getParent().resolve(BugLoRDConstants.DIR_NAME_RANKING))
							.getResult();
					Iterator<Path> iter = localizerDirs.iterator();
					while (iter.hasNext()) {
						Path directory = iter.next();
						if (directory.getFileName().toString().equals(BugLoRDConstants.DIR_NAME_LM_RANKING)) {
							iter.remove();
							break;
						}
					}
					List<String> localizerList = new ArrayList<>(localizerDirs.size());
					for (Path localizerDir : localizerDirs) {
						localizerList.add(localizerDir.getFileName().toString());
					}
					localizers = localizerList.toArray(new String[0]);
				}

				for (String localizerDir : localizers) {
					plotSingle(entity, suffix, localizerDir, strategy, outputDir, 
							changesFile.getParent().getParent().getFileName().toString() + File.separator + outputPrefix, 
							options.getOptionValues(CmdOptions.GLOBAL_PERCENTAGES),
							normStrategy);
				}

			}
		} else if (options.hasOption(CmdOptions.AVERAGE_PLOT)) {
			
			List<BuggyFixedEntity<?>> entities = new ArrayList<>();
//			//iterate over all projects
//			for (String project : Defects4J.getAllProjects()) {
//				String[] ids = Defects4J.getAllBugIDs(project); 
//				for (String id : ids) {
//					entities.add(Defects4JEntity.getBuggyDefects4JEntity(project, id));
//				}
//			}
			
			List<Path> changesFiles = new SearchFileOrDirToListProcessor("**/" + BugLoRDConstants.CHANGES_FILE_NAME, true)
					.searchForFiles()
					.submit(inputDir)
					.getResult();
			
			for (Path changesFile : changesFiles) {
				entities.add(new WorkDataDummyBuggyFixedEntity(changesFile.getParent()));
			}
			
			File allLMRankingFileNamesFile = new File(BugLoRDConstants.LM_RANKING_FILENAMES_FILE);
			
			List<String> allRankingFileNames = FileUtils.readFile2List(allLMRankingFileNamesFile.toPath());
			
			for (String lmRankingFileName : allRankingFileNames) {
				for (String localizerDir : options.getOptionValues(CmdOptions.AVERAGE_PLOT)) {
					plotAverage(entities, suffix, localizerDir, lmRankingFileName, strategy, outputDir, outputPrefix, 
							options.getOptionValues(CmdOptions.GLOBAL_PERCENTAGES),
							options.getNumberOfThreads(4), normStrategy);
				}
			}
		}
		
	}
	
	public static void plotSingle(BuggyFixedEntity<?> entity, String suffix, String localizer, ParserStrategy strategy,
			String outputDir, String outputPrefix, String[] globalPercentages, NormalizationStrategy normStrategy) {
		
			localizer = localizer.toLowerCase(Locale.getDefault());
			Log.out(Plotter.class, "Plotting rankings for '" + localizer + "'.");

			File allLMRankingFileNamesFile = new File(BugLoRDConstants.LM_RANKING_FILENAMES_FILE);
			
			List<String> allRankingFileNames = FileUtils.readFile2List(allLMRankingFileNamesFile.toPath());
			
			for (String lmRankingFileName : allRankingFileNames) {
					ItemCollector<RankingFileWrapper> collector = new ItemCollector<RankingFileWrapper>();

					new ModuleLinker().append(
							new CombiningRankingsEH(suffix, localizer, lmRankingFileName, strategy, globalPercentages, normStrategy),
							collector)
					.submit(entity);

					new ModuleLinker().append(
							new DataAdderModule(localizer),
							new SinglePlotCSVGeneratorModule(outputDir + File.separator + lmRankingFileName + File.separator + localizer + File.separator + outputPrefix),
							new SinglePlotLaTexGeneratorModule(outputDir + File.separator + lmRankingFileName + File.separator + "_latex" + File.separator + localizer + "_" + outputPrefix))
					.submit(collector.getCollectedItems());
				}

			Log.out(Plotter.class, "...Done with '" + localizer + "'.");
	}
	
	public static void plotAverage(List<BuggyFixedEntity<?>> entities, String suffix, String rankingIdentifier1, String rankingIdentifier2, ParserStrategy strategy,
			String outputDir, String outputPrefix, String[] globalPercentages, int numberOfThreads, NormalizationStrategy normStrategy) {
		
//		    rankingIdentifier1 = rankingIdentifier1.toLowerCase(Locale.getDefault());
			Log.out(Plotter.class, "Submitting '%s' and '%s'.", rankingIdentifier1, rankingIdentifier2);
			
			//Creates a list of all directories with the same name (localizerDir), sequences the list and
			//parses all found combined rankings and computes the averages. Parsing and averaging is done 
			//as best as possible in parallel with pipes.
			//When all averages are computed, we can plot the results (collected by the averager module).
			new PipeLinker().append(
					new CollectionSequencer<BuggyFixedEntity<?>>(),
					new ThreadedProcessor<>(numberOfThreads, 
							new CombiningRankingsEH(suffix, rankingIdentifier1, rankingIdentifier2, strategy, globalPercentages, normStrategy)),
					new RankingAveragerModule(rankingIdentifier1, rankingIdentifier2).asPipe().enableTracking(10),
					new AverageplotCSVGeneratorModule(outputDir + File.separator + rankingIdentifier2 + File.separator + rankingIdentifier1 + File.separator + rankingIdentifier1 + "_" + rankingIdentifier2 + "_" + outputPrefix),
					new AveragePlotLaTexGeneratorModule(outputDir + File.separator + rankingIdentifier2 + File.separator + "_latex" + File.separator + rankingIdentifier1 + "_" + rankingIdentifier2 + "_" + outputPrefix))
			.submitAndShutdown(entities);

			Log.out(Plotter.class, "...Done with '%s' and '%s'.", rankingIdentifier1, rankingIdentifier2);
	}
	
	public static void plotFromCSV(String localizer, String inputDir, String outputDir, String outputPrefix) {
			localizer = localizer.toLowerCase(Locale.getDefault());
			Log.out(Plotter.class, "Submitting '" + localizer + "'.");
			
			File localizerDir = FileUtils.searchDirectoryContainingPattern(new File(inputDir), localizer);
			
			if (localizerDir == null) {
				Log.err(Plotter.class, "Could not find subdirectory with name '%s' in '%s'.", localizer, inputDir);
			} else {
				new ModuleLinker().append(
						new CsvToAverageStatisticsCollectionModule(localizer),
						new AveragePlotLaTexGeneratorModule(outputDir + File.separator + "_latex" + File.separator + localizer + "_" + outputPrefix))
				.submit(localizerDir);
				
				new ModuleLinker().append(
						new CsvToSingleStatisticsCollectionModule(localizer),
						new SinglePlotLaTexGeneratorModule(outputDir + File.separator + "_latex" + File.separator + localizer + "_" + outputPrefix))
				.submit(localizerDir);
			}
			
			Log.out(Plotter.class, "...Done with '" + localizer + "'.");
	}
	
	
}
