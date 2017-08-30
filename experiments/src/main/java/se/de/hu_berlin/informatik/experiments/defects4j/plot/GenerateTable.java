/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.plot;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.BugLoRDProperties;
import se.de.hu_berlin.informatik.rankingplotter.modules.AveragePlotLaTexGeneratorModule;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.AveragePlotStatisticsCollection.StatisticsCategories;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.basics.CollectionSequencer;
import se.de.hu_berlin.informatik.utils.processors.sockets.ProcessorSocket;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.PipeLinker;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.files.csv.CSVUtils;
import se.de.hu_berlin.informatik.utils.files.processors.ListToFileWriter;
import se.de.hu_berlin.informatik.utils.files.processors.SearchFileOrDirToListProcessor;
import se.de.hu_berlin.informatik.utils.miscellaneous.LaTexUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.MathUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;

/**
 * Generates plots of the experiments' results.
 * 
 * @author SimHigh
 */
public class GenerateTable {

	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		PROJECTS(Option.builder("p").longOpt("projects").hasArgs().required()
				.desc("A list of projects to consider of the Defects4J benchmark. "
						+ "Should be either 'Lang', 'Chart', 'Time', 'Closure', 'Mockito', 'Math' or "
						+ "'super' for the super directory (only for the average plots). Set this to 'all' to "
						+ "iterate over all projects.").build()),
		SUFFIX("s", "suffix", true, "A suffix to append to the plot sub-directory.", false),
		LOCALIZERS(Option.builder("l").longOpt("localizers").required(false)
				.hasArgs().desc("A list of localizers (e.g. 'Tarantula', 'Jaccard', ...). If not set, "
						+ "the locliazers will be retrieved from the properties file.").build()),
		CROSS_VALIDATION("cv", "crossValidation", false, "If this is set, then tables for "
				+ "cross validation will be generated (if possible).", false),
		PLOTS("pl", "plots", false, "If this is set, then plots will "
				+ "be generated, each specified localizers in a separate plot.", false),
		COMBINED_PLOTS("cpl", "combinedPlots", false, "If this is set, then plots will "
				+ "be generated, including all specified localizers in one, single plot (for selected metrics).", false),
		PERCENTAGES(Option.builder("pc").longOpt("percentages").hasArgs()
				.desc("Generate a table with different ranking combinations. Takes as arguments a "
						+ "list of percentage values to include in the table.").build());

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

		OptionParser options = OptionParser.getOptions("GenerateTable", false, CmdOptions.class, args);

		String[] projects = options.getOptionValues(CmdOptions.PROJECTS);
		boolean allProjects = false;
		if (projects != null) {
			allProjects = projects[0].equals("all");
		} else {
			projects = new String[0];
		}

		if (allProjects) {
			projects = Defects4J.getAllProjects();
		}


		String[] localizers = options.getOptionValues(CmdOptions.LOCALIZERS);
		if (localizers == null) {
			localizers = BugLoRD.getValueOf(BugLoRDProperties.LOCALIZERS).split(" ");
		}

		String suffix = options.getOptionValue(CmdOptions.SUFFIX, null);
		String outputDir = Defects4J.getValueOf(Defects4JProperties.PLOT_DIR) + File.separator 
				+ "average" + (suffix == null ? "" : "_" + suffix);

		// we assume a directory structure like this:
		// ...plotDir/average[_suffix]/project[_normalization]/lm_ranking_id/sbfl_localizer_id/someFile.csv
		//iterate over all main project identifiers
		for (String project : projects) {
			boolean isProject = Defects4J.validateProject(project, false);

			if (!isProject && !project.equals("super")) {
				Log.abort(GenerateTable.class, "Project doesn't exist: '" + project + "'.");
			}

			List<Path> foundDirs = new SearchFileOrDirToListProcessor("**" + project + "*", true)
					.searchForDirectories()
					.skipSubTreeAfterMatch()
					.submit(Paths.get(outputDir))
					.getResult();
			if (foundDirs.isEmpty()) {
				Log.abort(GenerateTable.class, "No subdirectories found in '%s' containing pattern '%s'.", outputDir, project);
			}

			//iterate over all sub directories (due to e.g. normalization)
			for (Path normalizationDir : foundDirs) {
				// list of all lm ranking identifiers (sub directories)
				String[] lmPaths = normalizationDir.toFile().list();

				for (String lmPath : lmPaths) {
					Path foundLMRankingPath = Paths.get(lmPath);
					if (!foundLMRankingPath.toFile().isDirectory()) {
						continue;
					}

					Log.out(GenerateTable.class, "Processing '%s'.", foundLMRankingPath.getFileName());

					if (options.hasOption(CmdOptions.PERCENTAGES)) {
						String[] percentagesStrings = options.getOptionValues(CmdOptions.PERCENTAGES);
						Double[] percentages = new Double[percentagesStrings.length];
						for (int i = 0; i < percentages.length; ++i) {
							percentages[i] = Double.valueOf(percentagesStrings[i]);
						}
						Arrays.sort(percentages, (Double x, Double y) -> Double.compare(y, x));

						//					List<Path> foundSubPaths = new SearchForFilesOrDirsModule("**bucket_**", true)
						//							.searchForDirectories()
						//							.skipSubTreeAfterMatch()
						//							.submit(foundPath)
						//							.getResult();

						List<Path> foundSubPaths = new ArrayList<>(1);

						foundSubPaths.add(foundLMRankingPath);

						for (Path plotDir : foundSubPaths) {
							Log.out(GenerateTable.class, "\t '%s' -> mean.", plotDir.getFileName().toString());

							computeAndSavePercentagesTable(project, plotDir, 
									StatisticsCategories.MEAN_RANK, StatisticsCategories.MEAN_FIRST_RANK, 
									percentages, localizers);

							Log.out(GenerateTable.class, "\t '%s' -> mean, best lambdas.", plotDir.getFileName().toString());

							computeAndSaveTableBestLambdas(project, plotDir, 
									StatisticsCategories.MEAN_RANK, StatisticsCategories.MEAN_FIRST_RANK, localizers);


							Log.out(GenerateTable.class, "\t '%s' -> median.", plotDir.getFileName().toString());

							computeAndSavePercentagesTable(project, plotDir, 
									StatisticsCategories.MEDIAN_RANK, StatisticsCategories.MEDIAN_FIRST_RANK, 
									percentages, localizers);

							Log.out(GenerateTable.class, "\t '%s' -> median, best lambdas.", plotDir.getFileName().toString());

							computeAndSaveTableBestLambdas(project, plotDir, 
									StatisticsCategories.MEDIAN_RANK, StatisticsCategories.MEDIAN_FIRST_RANK, localizers);

						}
					}

					if (options.hasOption(CmdOptions.COMBINED_PLOTS)) {
						Log.out(GenerateTable.class, "\t '%s' -> combined plots, mean.", foundLMRankingPath.getFileName().toString());

						computeAndSaveCombinedLocalizerPlot(project, foundLMRankingPath, localizers, 
								StatisticsCategories.MEAN_RANK);

						Log.out(GenerateTable.class, "\t '%s' -> combined plots, mean first.", foundLMRankingPath.getFileName().toString());

						computeAndSaveCombinedLocalizerPlot(project, foundLMRankingPath, localizers, 
								StatisticsCategories.MEAN_FIRST_RANK);

						Log.out(GenerateTable.class, "\t '%s' -> combined plots, mean + mean first.", foundLMRankingPath.getFileName().toString());

						computeAndSaveCombinedLocalizerPlot(project, foundLMRankingPath, localizers, 
								StatisticsCategories.MEAN_RANK,
								StatisticsCategories.MEAN_FIRST_RANK);
						computeAndSaveCombinedLocalizerPlots(project, foundLMRankingPath, localizers, 
								StatisticsCategories.MEAN_RANK,
								StatisticsCategories.MEAN_FIRST_RANK);

						Log.out(GenerateTable.class, "\t '%s' -> combined plots, median.", foundLMRankingPath.getFileName().toString());

						computeAndSaveCombinedLocalizerPlot(project, foundLMRankingPath, localizers, 
								StatisticsCategories.MEDIAN_RANK);

						Log.out(GenerateTable.class, "\t '%s' -> combined plots, median first.", foundLMRankingPath.getFileName().toString());

						computeAndSaveCombinedLocalizerPlot(project, foundLMRankingPath, localizers, 
								StatisticsCategories.MEDIAN_FIRST_RANK);

						Log.out(GenerateTable.class, "\t '%s' -> combined plots, median + median first.", foundLMRankingPath.getFileName().toString());

						computeAndSaveCombinedLocalizerPlot(project, foundLMRankingPath, localizers, 
								StatisticsCategories.MEDIAN_RANK,
								StatisticsCategories.MEDIAN_FIRST_RANK);
						computeAndSaveCombinedLocalizerPlots(project, foundLMRankingPath, localizers, 
								StatisticsCategories.MEDIAN_RANK,
								StatisticsCategories.MEDIAN_FIRST_RANK);

						Log.out(GenerateTable.class, "\t '%s' -> combined plots, mean + median.", foundLMRankingPath.getFileName().toString());

						computeAndSaveCombinedLocalizerPlot(project, foundLMRankingPath, localizers, 
								StatisticsCategories.MEAN_RANK,
								StatisticsCategories.MEDIAN_RANK);
						computeAndSaveCombinedLocalizerPlots(project, foundLMRankingPath, localizers, 
								StatisticsCategories.MEAN_RANK,
								StatisticsCategories.MEDIAN_RANK);

						Log.out(GenerateTable.class, "\t '%s' -> combined plots, mean first + median first.", foundLMRankingPath.getFileName().toString());

						computeAndSaveCombinedLocalizerPlot(project, foundLMRankingPath, localizers, 
								StatisticsCategories.MEAN_FIRST_RANK,
								StatisticsCategories.MEDIAN_FIRST_RANK);
						computeAndSaveCombinedLocalizerPlots(project, foundLMRankingPath, localizers, 
								StatisticsCategories.MEAN_FIRST_RANK,
								StatisticsCategories.MEDIAN_FIRST_RANK);
					}

					if (options.hasOption(CmdOptions.PLOTS)) {
						Log.out(GenerateTable.class, "\t '%s' -> plots.", foundLMRankingPath.getFileName().toString());

						computeAndSaveLocalizerPlots(project, foundLMRankingPath, localizers);

					}

					if (options.hasOption(CmdOptions.CROSS_VALIDATION)) {
						List<Path> foundMainBucketPaths = new SearchFileOrDirToListProcessor("**_buckets_total**", true)
								.searchForDirectories()
								.skipSubTreeAfterMatch()
								.submit(foundLMRankingPath)
								.getResult();

						for (Path bucketsDir : foundMainBucketPaths) {
							Log.out(GenerateTable.class, "\t '%s' -> mean, cross-validation.", bucketsDir.getFileName().toString());

							computeAndSaveTableForCrossValidation(project, bucketsDir, 
									StatisticsCategories.MEAN_RANK, localizers);
							computeAndSaveTableForCrossValidation(project, bucketsDir, 
									StatisticsCategories.MEAN_FIRST_RANK, localizers);

							Log.out(GenerateTable.class, "\t '%s' -> median, cross-validation.", bucketsDir.getFileName().toString());

							computeAndSaveTableForCrossValidation(project, bucketsDir, 
									StatisticsCategories.MEDIAN_RANK,  localizers);
							computeAndSaveTableForCrossValidation(project, bucketsDir, 
									StatisticsCategories.MEDIAN_FIRST_RANK, localizers);

						}
					}
				}
			}
		}

	}

	private static void computeAndSaveLocalizerPlots(String project, Path plotDir, String[] localizers) {
		new PipeLinker().append(
				new CollectionSequencer<String>(),
				new AbstractProcessor<String, Pair<String, Entry<StatisticsCategories, List<Double[]>>>>() {

					@Override
					public Pair<String, Entry<StatisticsCategories, List<Double[]>>> processItem(String localizer, 
							ProcessorSocket<String, Pair<String, Entry<StatisticsCategories, List<Double[]>>>> socket) {
						localizer = localizer.toLowerCase(Locale.getDefault());
						File localizerDir = plotDir.resolve(localizer).toFile();
						if (!localizerDir.exists()) {
							Log.err(GenerateTable.class, "localizer directory doesn't exist: '" + localizerDir + "'.");
							return null;
						}

						for (StatisticsCategories rank : EnumSet.allOf(StatisticsCategories.class)) {
							File rankFile = FileUtils.searchFileContainingPattern(localizerDir, "_" + rank + ".csv");
							if (rankFile == null) {
								Log.warn(GenerateTable.class, rank + " csv file doesn't exist for localizer '" + localizer + "'.");
								continue;
							}

							List<Double[]> rankList = CSVUtils.readCSVFileToListOfDoubleArrays(rankFile.toPath());

							socket.produce(new Pair<>(localizer, new AbstractMap.SimpleEntry<>(rank, rankList)));
						}

						return null;
					}
				},
				new AbstractProcessor<Pair<String, Entry<StatisticsCategories, List<Double[]>>>, List<String>>() {

					@Override
					public List<String> processItem(Pair<String, Entry<StatisticsCategories, List<Double[]>>> item) {
						new ListToFileWriter<List<String>>(
								plotDir.resolve("_latex").resolve(item.first() 
										+ "_" + project + "_" + item.second().getKey() + ".tex"), true)
						.submit(AveragePlotLaTexGeneratorModule.generateLaTexFromTable(item.first(), item.second().getKey(), item.second()));

						return null;
					}
				}
				).submitAndShutdown(Arrays.asList(localizers));
	}

	private static void computeAndSaveCombinedLocalizerPlot(String project, Path plotDir, String[] localizers, 
			StatisticsCategories... categories) {
		String combinedCategories = "";
		for (StatisticsCategories rank : categories) {
			combinedCategories += rank;
		}
		new PipeLinker().append(
				new CollectionSequencer<String>(),
				new AbstractProcessor<String, Entry<Pair<String, StatisticsCategories>, List<Double[]>>>() {

					@Override
					public Entry<Pair<String, StatisticsCategories>, List<Double[]>> processItem(String localizer, 
							ProcessorSocket<String, Entry<Pair<String, StatisticsCategories>, List<Double[]>>> socket) {
						localizer = localizer.toLowerCase(Locale.getDefault());
						File localizerDir = plotDir.resolve(localizer).toFile();
						if (!localizerDir.exists()) {
							Log.err(GenerateTable.class, "localizer directory doesn't exist: '" + localizerDir + "'.");
							return null;
						}

						for (StatisticsCategories rank : categories) {
							File rankFile = FileUtils.searchFileContainingPattern(localizerDir, "_" + rank + ".csv");
							if (rankFile == null) {
								Log.err(GenerateTable.class, rank + " csv file doesn't exist for localizer '" + localizer + "'.");
								return null;
							}


							List<Double[]> rankList = CSVUtils.readCSVFileToListOfDoubleArrays(rankFile.toPath());

							socket.produce(new AbstractMap.SimpleEntry<>(new Pair<>(localizer,rank), rankList));
						}

						return null;
					}
				},
				new AbstractProcessor<Entry<Pair<String, StatisticsCategories>, List<Double[]>>, List<String>>() {
					Map<Pair<String, StatisticsCategories>, List<Double[]>> map = new HashMap<>();
					@Override
					public List<String> processItem(Entry<Pair<String, StatisticsCategories>, List<Double[]>> item) {
						map.put(item.getKey(), item.getValue());
						return null;
					}
					@Override
					public List<String> getResultFromCollectedItems() {
						return AveragePlotLaTexGeneratorModule.generateLaTexFromTable(null, map.entrySet());
					}
				},
				new ListToFileWriter<List<String>>(plotDir.resolve(project + "_combined_" + combinedCategories + "_plot.tex"), true)
				).submitAndShutdown(Arrays.asList(localizers));

	}

	private static void computeAndSaveCombinedLocalizerPlots(String project, Path plotDir, String[] localizers, 
			StatisticsCategories... categories) {
		if (categories.length == 0) {
			Log.err(GenerateTable.class, "No categories given to plot.");
			return;
		}
		String combinedCategories = "";
		for (StatisticsCategories rank : categories) {
			combinedCategories += rank;
		}
		final String combinedCategoriesFinal = combinedCategories;
		new PipeLinker().append(
				new CollectionSequencer<String>(),
				new AbstractProcessor<String, Entry<Pair<String, StatisticsCategories>, List<Double[]>>>() {

					@Override
					public Entry<Pair<String, StatisticsCategories>, List<Double[]>> processItem(String localizer) {
						localizer = localizer.toLowerCase(Locale.getDefault());
						File localizerDir = plotDir.resolve(localizer).toFile();
						if (!localizerDir.exists()) {
							Log.err(GenerateTable.class, "localizer directory doesn't exist: '" + localizerDir + "'.");
							return null;
						}

						List<Entry<StatisticsCategories, List<Double[]>>> list = new ArrayList<>();
						for (StatisticsCategories rank : categories) {
							File rankFile = FileUtils.searchFileContainingPattern(localizerDir, "_" + rank + ".csv");
							if (rankFile == null) {
								Log.err(GenerateTable.class, rank + " csv file doesn't exist for localizer '" + localizer + "'.");
								return null;
							}


							List<Double[]> rankList = CSVUtils.readCSVFileToListOfDoubleArrays(rankFile.toPath());

							list.add(new AbstractMap.SimpleEntry<>(rank, rankList));
						}

						new ListToFileWriter<List<String>>(
								plotDir.resolve("_latex").resolve(localizer + "_" + project + "_" + combinedCategoriesFinal + ".tex"), true)
						.submit(AveragePlotLaTexGeneratorModule.generateLaTexFromTable(localizer, categories[0], list));
						return null;
					}
				}
				).submitAndShutdown(Arrays.asList(localizers));

	}

	private static void computeAndSaveTableForCrossValidation(String project, Path bucketsDir,
			StatisticsCategories rank, String[] localizers) {
		List<Path> foundLargeBucketPaths = new SearchFileOrDirToListProcessor("**_rest**", true)
				.searchForDirectories()
				.skipSubTreeAfterMatch()
				.submit(bucketsDir)
				.getResult();

		new PipeLinker().append(
				new CollectionSequencer<String>(),
				new AbstractProcessor<String, String[]>() {

					@Override
					public String[] processItem(String localizer) {
						localizer = localizer.toLowerCase(Locale.getDefault());

						List<Double> bestLargePartitionLambdas = new ArrayList<>(10);

						for (Path largeBucketPath : foundLargeBucketPaths) {
							//get ranking csv files for current localizer
							File largeBucketLocalizerDir = largeBucketPath.resolve(localizer).toFile();
							if (!largeBucketLocalizerDir.exists()) {
								Log.err(GenerateTable.class, "localizer directory doesn't exist: '" + largeBucketLocalizerDir + "'.");
								return null;
							}

							Double largebestPercentage = getBestPercentage(rank, localizer, largeBucketLocalizerDir);
							bestLargePartitionLambdas.add(largebestPercentage.doubleValue() / 100.0);
						}

						double maxLargeLambda = MathUtils.getMax(bestLargePartitionLambdas);
						double minLargeLambda = MathUtils.getMin(bestLargePartitionLambdas);
						double medianLargeLambda = MathUtils.getMedian(bestLargePartitionLambdas);
						double meanLargeLambda = MathUtils.getMean(bestLargePartitionLambdas);



						List<Double> sbflImprovementsByBestLargeLambda = new ArrayList<>(10);
						//							List<Double> sbflImprovementsByBestSmallLambda = new ArrayList<>(10);

						List<Double> lmImprovementsByBestLargeLambda = new ArrayList<>(10);
						//							List<Double> lmImprovementsByBestSmallLambda = new ArrayList<>(10);

						for (Path largeBucketPath : foundLargeBucketPaths) {
							//largeBucketPath: '**/bucket_xy_rest'
							String largeBucketFileName = largeBucketPath.getFileName().toString();
							Path smallBucketPath = largeBucketPath.getParent()
									.resolve(largeBucketFileName.substring(0, largeBucketFileName.lastIndexOf('_')));
							if (!smallBucketPath.toFile().exists()) {
								Log.err(GenerateTable.class, "'small' bucket directory doesn't exist: '" + smallBucketPath + "'.");
								return null;
							}

							//get ranking csv files for current localizer
							File largeBucketLocalizerDir = largeBucketPath.resolve(localizer).toFile();
							if (!largeBucketLocalizerDir.exists()) {
								Log.err(GenerateTable.class, "localizer directory doesn't exist: '" + largeBucketLocalizerDir + "'.");
								return null;
							}
							File smallBucketLocalizerDir = smallBucketPath.resolve(localizer).toFile();
							if (!smallBucketLocalizerDir.exists()) {
								Log.err(GenerateTable.class, "localizer directory doesn't exist: '" + smallBucketLocalizerDir + "'.");
								return null;
							}

							//lambda_max1
							Double largebestPercentage = getBestPercentage(rank, localizer, largeBucketLocalizerDir);
							//lambda_max2
							//								Double smallbestPercentage = getBestPercentage(rank, localizer, smallBucketLocalizerDir);


							File smallRankFile = FileUtils.searchFileContainingPattern(smallBucketLocalizerDir, "_" + rank + ".csv");
							if (smallRankFile == null) {
								Log.err(GenerateTable.class, rank + " csv file doesn't exist for localizer '" + localizer + "'.");
								return null;
							}

							List<Double[]> smallRankList = CSVUtils.readCSVFileToListOfDoubleArrays(smallRankFile.toPath());

							Map<Double, Double> rankMap = new HashMap<>();
							for (Double[] array : smallRankList) {
								rankMap.put(array[0], array[1]);
							}
							Double sbflValue = rankMap.get(100.0);
							if (sbflValue == null) {
								Log.abort(GenerateTable.class, "percentage value '100.0%' not existing in " + rank + " csv file.");
							}
							Double lmValue = rankMap.get(0.0);
							if (lmValue == null) {
								Log.abort(GenerateTable.class, "percentage value '0.0%' not existing in " + rank + " csv file.");
							}

							sbflImprovementsByBestLargeLambda.add(getImprovement(sbflValue, rankMap.get(largebestPercentage)));
							//								sbflImprovementsByBestSmallLambda.add(getImprovement(sbflValue, rankMap.get(smallbestPercentage)));

							lmImprovementsByBestLargeLambda.add(getImprovement(lmValue, rankMap.get(largebestPercentage)));
							//								lmImprovementsByBestSmallLambda.add(getImprovement(lmValue, rankMap.get(smallbestPercentage)));
						}

						double maxSbflImprovementByBestLargeLambda = MathUtils.getMax(sbflImprovementsByBestLargeLambda);
						double minSbflImprovementByBestLargeLambda = MathUtils.getMin(sbflImprovementsByBestLargeLambda);
						double medianSbflImprovementByBestLargeLambda = MathUtils.getMedian(sbflImprovementsByBestLargeLambda);
						double meanSbflImprovementByBestLargeLambda = MathUtils.getMean(sbflImprovementsByBestLargeLambda);

						//							double maxSbflImprovementByBestSmallLambda = MathUtils.getMax(sbflImprovementsByBestSmallLambda);
						//							double minSbflImprovementByBestSmallLambda = MathUtils.getMin(sbflImprovementsByBestSmallLambda);
						//							double medianSbflImprovementByBestSmallLambda = MathUtils.getMedian(sbflImprovementsByBestSmallLambda);
						//							double meanSbflImprovementByBestSmallLambda = MathUtils.getMean(sbflImprovementsByBestSmallLambda);

						double maxLmImprovementByBestLargeLambda = MathUtils.getMax(lmImprovementsByBestLargeLambda);
						double minLmImprovementByBestLargeLambda = MathUtils.getMin(lmImprovementsByBestLargeLambda);
						double medianLmImprovementByBestLargeLambda = MathUtils.getMedian(lmImprovementsByBestLargeLambda);
						double meanLmImprovementByBestLargeLambda = MathUtils.getMean(lmImprovementsByBestLargeLambda);

						//							double maxLmImprovementByBestSmallLambda = MathUtils.getMax(lmImprovementsByBestSmallLambda);
						//							double minLmImprovementByBestSmallLambda = MathUtils.getMin(lmImprovementsByBestSmallLambda);
						//							double medianLmImprovementByBestSmallLambda = MathUtils.getMedian(lmImprovementsByBestSmallLambda);
						//							double meanLmImprovementByBestSmallLambda = MathUtils.getMean(lmImprovementsByBestSmallLambda);

						/* & localizer 
						 * 
						 * & median_best_large_lambda, (\\lambda_{pred})
						 *   (mean_best_large_lambda),
						 *   range(best_large_lambda) 
						 * 
						 * & median_sbfl_improvement_by_best_large_lambda,
						 *   (mean_sbfl_improvement_by_best_large_lambda),
						 *   range(sbfl_improvement_by_best_large_lambda)
						 * 
//							 * & median_sbfl_improvement_by_best_small_lambda, (\\lambda_{tr})
//							 *   (mean_sbfl_improvement_by_best_small_lambda),
//							 *   range(sbfl_improvement_by_best_small_lambda)
						 * 
						 * & median_lm_improvement_by_best_large_lambda,
						 *   (mean_lm_improvement_by_best_large_lambda),
						 *   range(lm_improvement_by_best_large_lambda)
						 * 
//							 * & median_lm_improvement_by_best_small_lambda,
//							 *   (mean_lm_improvement_by_best_small_lambda),
//							 *   range(lm_improvement_by_best_small_lambda)
						 */
						String[] result = new String[4];
						int counter = 0;
						result[counter++] = getEscapedLocalizerName(localizer);

						result[counter++] = getLambdaAsString(medianLargeLambda) 
								+ ", (" + getLambdaAsString(meanLargeLambda) + "), "
								+ getLambdaRange(minLargeLambda, maxLargeLambda);

						result[counter++] = getPercentageAsString(medianSbflImprovementByBestLargeLambda) 
								+ ", (" + getPercentageAsString(meanSbflImprovementByBestLargeLambda) + "), "
								+ getPercentageRange(minSbflImprovementByBestLargeLambda, maxSbflImprovementByBestLargeLambda);

						//							result[counter++] = getPercentageAsString(medianSbflImprovementByBestSmallLambda)
						//									+ ", (" + getPercentageAsString(meanSbflImprovementByBestSmallLambda) + "), "
						//									+ getPercentageRange(minSbflImprovementByBestSmallLambda, maxSbflImprovementByBestSmallLambda);

						result[counter++] = getPercentageAsString(medianLmImprovementByBestLargeLambda)
								+ ", (" + getPercentageAsString(meanLmImprovementByBestLargeLambda) + "), "
								+ getPercentageRange(minLmImprovementByBestLargeLambda, maxLmImprovementByBestLargeLambda);

						//							result[counter++] = getPercentageAsString(medianLmImprovementByBestSmallLambda)
						//									+ ", (" + getPercentageAsString(meanLmImprovementByBestSmallLambda) + "), "
						//									+ getPercentageRange(minLmImprovementByBestSmallLambda, maxLmImprovementByBestSmallLambda);

						return result;
					}

				},
				new AbstractProcessor<String[], List<String>>() {
					Map<String, String[]> map = new HashMap<>();
					@Override
					public List<String> processItem(String[] item) {
						map.put(item[0], item);
						return null;
					}
					@Override
					public List<String> getResultFromCollectedItems() {
						/* & localizer 
						 * 
						 * & median_best_large_lambda, (\\lambda_{pred})
						 *   (mean_best_large_lambda),
						 *   range(best_large_lambda) 
						 * 
						 * & median_sbfl_improvement_by_best_large_lambda,
						 *   (mean_sbfl_improvement_by_best_large_lambda),
						 *   range(sbfl_improvement_by_best_large_lambda)
						 * 
//							 * & median_sbfl_improvement_by_best_small_lambda, (\\lambda_{tr})
//							 *   (mean_sbfl_improvement_by_best_small_lambda),
//							 *   range(sbfl_improvement_by_best_small_lambda)
						 * 
						 * & median_lm_improvement_by_best_large_lambda,
						 *   (mean_lm_improvement_by_best_large_lambda),
						 *   range(lm_improvement_by_best_large_lambda)
						 * 
//							 * & median_lm_improvement_by_best_small_lambda,
//							 *   (mean_lm_improvement_by_best_small_lambda),
//							 *   range(lm_improvement_by_best_small_lambda)
						 */
						String[] titleArray1 = new String[4];
						int counter = 0;
						titleArray1[counter++] = "\\hfill SBFL ranking metric \\hfill";

						titleArray1[counter++] = "$\\widetilde{\\lambda_{pred}}$, "
								+ "$(\\overline{\\lambda_{pred}})$, $[\\min,\\max]$";

						titleArray1[counter++] = "$\\widetilde{RRI^{SBFL}_{\\lambda_{pred}}}$, "
								+ "$(\\overline{RRI^{SBFL}_{\\lambda_{pred}}})$, $[\\min,\\max]$";

						//							titleArray1[counter++] = "$\\widetilde{RRI^{SBFL}_{\\lambda_{tr}}}$, "
						//									+ "$(\\overline{RRI^{SBFL}_{\\lambda_{tr}}})$, $[\\min,\\max]$";

						titleArray1[counter++] = "$\\widetilde{RRI^{LM}_{\\lambda_{pred}}}$, "
								+ "$(\\overline{RRI^{LM}_{\\lambda_{pred}}})$, $[\\min,\\max]$";

						//							titleArray1[counter++] = "$\\widetilde{RRI^{LM}_{\\lambda_{tr}}}$, "
						//									+ "$(\\overline{RRI^{LM}_{\\lambda_{tr}}})$, $[\\min,\\max]$";
						map.put("1", titleArray1);

						return LaTexUtils.generateSimpleLaTexTable(Misc.sortByKeyToValueList(map));
					}
				},
				new ListToFileWriter<List<String>>(bucketsDir.resolve(project + "_" + rank + "_crossValidationTable.tex"), true)
				).submitAndShutdown(Arrays.asList(localizers));
	}

	private static Double getBestPercentage(StatisticsCategories rank, String localizer, File localizerDir) {
		File rankFile = FileUtils.searchFileContainingPattern(localizerDir, "_" + rank + ".csv");
		if (rankFile == null) {
			Log.err(GenerateTable.class, rank + " csv file doesn't exist for localizer '" + localizer + "'.");
			return null;
		}

		List<Double[]> rankList = CSVUtils.readCSVFileToListOfDoubleArrays(rankFile.toPath());

		Map<Double, Double> rankMap = new HashMap<>();
		for (Double[] array : rankList) {
			rankMap.put(array[0], array[1]);
		}
		Double fullValue = rankMap.get(100.0);
		if (fullValue == null) {
			Log.abort(GenerateTable.class, "percentage value '100.0%' not existing in " + rank + " csv file.");
		}
		//compute the lamda value with the best ranking result
		Double bestValue = fullValue;
		Double bestPercentage = 100.0;
		for (Entry<Double, Double> entry : rankMap.entrySet()) {
			Double value = entry.getValue();
			if (value == null) {
				Log.abort(GenerateTable.class, "percentage value '" + entry.getKey() + "%' has null value in " + rank + " csv file.");
			}
			if (value.compareTo(bestValue) < 0) {
				bestValue = value;
				bestPercentage = entry.getKey();
			}
		}
		return bestPercentage;
	}

	private static void computeAndSavePercentagesTable(String project, Path plotDir, 
			StatisticsCategories rank, StatisticsCategories firstRank, 
			Double[] percentages, String[] localizers) {
		new PipeLinker().append(
				new CollectionSequencer<String>(),
				new AbstractProcessor<String, String[]>() {

					@Override
					public String[] processItem(String localizer) {
						localizer = localizer.toLowerCase(Locale.getDefault());
						File localizerDir = plotDir.resolve(localizer).toFile();
						if (!localizerDir.exists()) {
							Log.err(GenerateTable.class, "localizer directory doesn't exist: '" + localizerDir + "'.");
							return null;
						}


						String[] result = new String[percentages.length*2 + 3];
						int counter = 0;
						result[counter++] = getEscapedLocalizerName(localizer);

						{
							File rankFile = FileUtils.searchFileContainingPattern(localizerDir, "_" + rank + ".csv");
							counter = fillArrayWithComputedPercentagesValuesForFile(rank, 
									percentages, localizer, result, counter, rankFile);
						}

						{
							File firstRankFile = FileUtils.searchFileContainingPattern(localizerDir, "_" + firstRank + ".csv");
							counter = fillArrayWithComputedPercentagesValuesForFile(firstRank, 
									percentages, localizer, result, counter, firstRankFile);
						}

						return result;
					}					
				},
				new AbstractProcessor<String[], List<String>>() {
					Map<String, String[]> map = new HashMap<>();
					@Override
					public List<String> processItem(String[] item) {
						map.put(item[0], item);
						return null;
					}
					@Override
					public List<String> getResultFromCollectedItems() {
						String[] titleArray1 = new String[percentages.length*2 + 3];
						int counter = 0;
						titleArray1[counter++] = "\\hfill SBFL ranking metric \\hfill";
						for (int i = 0; i < percentages.length; ++i) {
							titleArray1[counter++] = "";
						}
						titleArray1[counter++] = "max";
						for (int i = 0; i < percentages.length; ++i) {
							titleArray1[counter++] = "";
						}
						titleArray1[counter++] = "max";
						map.put("1", titleArray1);

						String[] titleArray2 = new String[percentages.length*2 + 3];
						counter = 0;
						titleArray2[counter++] = "";
						for (double percentage : percentages) {
							titleArray2[counter++] = "$\\lambda=" + getLambdaAsString(percentage/100.0) +"$";
						}
						titleArray2[counter++] = "improv.";
						for (double percentage : percentages) {
							titleArray2[counter++] = "$\\lambda=" + getLambdaAsString(percentage/100.0) +"$";
						}
						titleArray2[counter++] = "improv.";
						map.put("2", titleArray2);

						return LaTexUtils.generateSimpleLaTexTable(Misc.sortByKeyToValueList(map));
					}
				},
				new ListToFileWriter<List<String>>(plotDir.resolve(project + "_big_" + rank + "_" + firstRank + "_Table.tex"), true)
				).submitAndShutdown(Arrays.asList(localizers));
	}

	private static int fillArrayWithComputedPercentagesValuesForFile(StatisticsCategories rank, Double[] percentages,
			String localizer, String[] result, int counter, File rankFile) {
		if (rankFile == null) {
			Log.abort(GenerateTable.class, rank + " csv file doesn't exist for localizer '" + localizer + "'.");
		}

		List<Double[]> rankList = CSVUtils.readCSVFileToListOfDoubleArrays(rankFile.toPath());
		return fillArrayWithComputedPercentagesValues(rank, percentages, result, counter, rankList);
	}

	private static int fillArrayWithComputedPercentagesValues(StatisticsCategories rank, Double[] percentages, 
			String[] result, int counter, List<Double[]> rankList) {
		Map<Double, Double> rankMap = new HashMap<>();
		for (Double[] array : rankList) {
			rankMap.put(array[0], array[1]);
		}
		Double fullValue = rankMap.get(100.0);
		if (fullValue == null) {
			Log.abort(GenerateTable.class, "percentage value '100.0%' not existing in " + rank + " csv file.");
		}
		{ //compute numbers for given percentages and mark the max. improvement
			Double bestValue = fullValue;
			for (double percentage : percentages) {
				Double value = rankMap.get(percentage);
				if (value == null) {
					Log.abort(GenerateTable.class, "percentage value '" + percentage + "%' not existing in " + rank + " csv file.");
				}
				if (value.compareTo(bestValue) < 0) {
					bestValue = value;
				}
			}
			for (double percentage : percentages) {
				Double value = rankMap.get(percentage);
				if (value.equals(bestValue)) {
					result[counter++] = "\\textbf{" + getRankingValueAsString(value) + "}";
				} else { 
					result[counter++] = getRankingValueAsString(value);
				}
			}
			result[counter++] = String.valueOf(MathUtils.roundToXDecimalPlaces(
					getImprovement(fullValue.doubleValue(), bestValue.doubleValue()), 1)) + "\\%";
		}
		return counter;
	}

	private static void computeAndSaveTableBestLambdas(String project, Path plotDir, 
			StatisticsCategories rank, StatisticsCategories firstRank, String[] localizers) {
		new PipeLinker().append(
				new CollectionSequencer<String>(),
				new AbstractProcessor<String, String[]>() {

					@Override
					public String[] processItem(String localizer) {
						localizer = localizer.toLowerCase(Locale.getDefault());
						File localizerDir = plotDir.resolve(localizer).toFile();
						if (!localizerDir.exists()) {
							Log.err(GenerateTable.class, "localizer directory doesn't exist: '" + localizerDir + "'.");
							return null;
						}

						File rankFile = FileUtils.searchFileContainingPattern(localizerDir, "_" + rank + ".csv");
						if (rankFile == null) {
							Log.err(GenerateTable.class, rank + " csv file doesn't exist for localizer '" + localizer + "'.");
							return null;
						}

						File firstRankFile = FileUtils.searchFileContainingPattern(localizerDir, "_" + firstRank + ".csv");
						if (firstRankFile == null) {
							Log.err(GenerateTable.class, firstRank + " csv file doesn't exist for localizer '" + localizer + "'.");
							return null;
						}

						String[] result = new String[3];
						int counter = 0;
						result[counter++] = getEscapedLocalizerName(localizer);

						{
							List<Double[]> rankList = CSVUtils.readCSVFileToListOfDoubleArrays(rankFile.toPath());
							counter = fillArrayWithComputedValuesBestLambda(rank, result, counter, rankList);
						}

						{
							List<Double[]> firstRankList = CSVUtils.readCSVFileToListOfDoubleArrays(firstRankFile.toPath());
							counter = fillArrayWithComputedValuesBestLambda(firstRank, result, counter, firstRankList);
						}

						return result;
					}
				},
				new AbstractProcessor<String[], List<String>>() {
					Map<String, String[]> map = new HashMap<>();
					@Override
					public List<String> processItem(String[] item) {
						map.put(item[0], item);
						return null;
					}
					@Override
					public List<String> getResultFromCollectedItems() {
						String[] titleArray1 = new String[3];
						int counter = 0;
						titleArray1[counter++] = "\\hfill SBFL ranking metric \\hfill";
						titleArray1[counter++] = "best MR";
						titleArray1[counter++] = "best MFR";
						map.put("1", titleArray1);

						String[] titleArray2 = new String[3];
						counter = 0;
						titleArray2[counter++] = "";
						titleArray2[counter++] = "$\\lambda$";
						titleArray2[counter++] = "$\\lambda$";
						map.put("2", titleArray2);

						return LaTexUtils.generateSimpleLaTexTable(Misc.sortByKeyToValueList(map));
					}
				},
				new ListToFileWriter<List<String>>(plotDir.resolve(project + "_bestLambda_" + rank + "_" + firstRank + "_Table.tex"), true)
				).submitAndShutdown(Arrays.asList(localizers));
	}

	private static String getEscapedLocalizerName(String localizer) {
		return "\\" + localizer.replace("1","ONE").replace("2","TWO").replace("3","THREE");
	}

	private static int fillArrayWithComputedValuesBestLambda(StatisticsCategories rank, 
			String[] result, int counter, List<Double[]> rankList) {
		Map<Double, Double> rankMap = new HashMap<>();
		for (Double[] array : rankList) {
			rankMap.put(array[0], array[1]);
		}
		Double fullValue = rankMap.get(100.0);
		if (fullValue == null) {
			Log.abort(GenerateTable.class, "percentage value '100.0%' not existing in " + rank + " csv file.");
		}
		{ //compute the lamda value with the best ranking result
			Double bestValue = fullValue;
			Double bestPercentage = 100.0;
			for (Entry<Double, Double> entry : rankMap.entrySet()) {
				Double value = entry.getValue();
				if (value == null) {
					Log.abort(GenerateTable.class, "percentage value '" + entry.getKey() + "%' has null value in " + rank + " csv file.");
				}
				if (value.compareTo(bestValue) < 0) {
					bestValue = value;
					bestPercentage = entry.getKey();
				}
			}
			result[counter++] = String.valueOf(MathUtils.roundToXDecimalPlaces(bestPercentage.doubleValue() / 100.0, 2));
		}
		return counter;
	}

	private static double getImprovement(Double baseValue, Double newValue) {
		return -(newValue.doubleValue() / baseValue.doubleValue() * 100.0 - 100.0);
	}

	private static String getLambdaRange(double minLambda, double maxLambda) {
		return "[" + getLambdaAsString(minLambda) +
				"," + getLambdaAsString(maxLambda) + "]";
	}

	private static String getPercentageRange(double minPerc, double maxPerc) {
		return "[" + getPercentageAsString(minPerc) +
				"," + getPercentageAsString(maxPerc) + "]";
	}

	private static String getLambdaAsString(double lambda) {
		return String.valueOf(MathUtils.roundToXDecimalPlaces(lambda,2));
	}

	private static String getPercentageAsString(double lambda) {
		return String.valueOf(MathUtils.roundToXDecimalPlaces(lambda,1)) + "\\%";
	}

	private static String getRankingValueAsString(double value) {
		return String.valueOf(MathUtils.roundToXDecimalPlaces(value,1));
	}
}
