/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.plot;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
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
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.AveragePlotStatisticsCollection.StatisticsCategories;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.AbstractPipe;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.PipeLinker;
import se.de.hu_berlin.informatik.utils.tm.pipes.ListSequencerPipe;
import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.fileoperations.ListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.fileoperations.SearchForFilesOrDirsModule;
import se.de.hu_berlin.informatik.utils.fileoperations.csv.CSVUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.LaTexUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.MathUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
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
		PROJECTS(Option.builder("p").longOpt("projects").hasArgs()
        		.desc("A list of projects to consider of the Defects4J benchmark. "
        		+ "Should be either 'Lang', 'Chart', 'Time', 'Closure', 'Mockito', 'Math' or "
        		+ "'super' for the super directory (only for the average plots). Set this to 'all' to "
        		+ "iterate over all projects.").build()),
        PERCENTAGES(Option.builder("pc").longOpt("percentages").hasArgs().required()
        		.desc("A list of percentage values to include in the table.").build());

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
		
		String[] percentagesStrings = options.getOptionValues(CmdOptions.PERCENTAGES);
		Double[] percentages = new Double[percentagesStrings.length];
		for (int i = 0; i < percentages.length; ++i) {
			percentages[i] = Double.valueOf(percentagesStrings[i]);
		}
		Arrays.sort(percentages, (Double x, Double y) -> Double.compare(y, x));
		
		
		String[] localizers = BugLoRD.getValueOf(BugLoRDProperties.LOCALIZERS).split(" ");
		
		String outputDir = Defects4J.getValueOf(Defects4JProperties.PLOT_DIR) + File.separator + "average";
		
		for (String project : projects) {
			Log.out(GenerateTable.class, "Processing '%s'.", project);
			boolean isProject = Defects4J.validateProject(project, false);

			if (!isProject && !project.equals("super")) {
				Log.abort(GenerateTable.class, "Project doesn't exist: '" + project + "'.");
			}

//			Path plotDir = Paths.get(PlotAverageEH.generatePlotOutputDir(outputDir, project));
			File foundDir = FileUtils.searchDirectoryContainingPattern(new File(outputDir), project);
			if (foundDir == null) {
				Log.abort(GenerateTable.class, "No subdirectory found in '%s' containing pattern '%s'.", outputDir, project);
			}

			Path foundPath = foundDir.toPath().toAbsolutePath();
			List<Path> foundPaths = new SearchForFilesOrDirsModule("**bucket_**", true)
					.searchForDirectories()
					.skipSubTreeAfterMatch()
					.submit(foundPath)
					.getResult();

			foundPaths.add(foundPath);

			for (Path plotDir : foundPaths) {
				Log.out(GenerateTable.class, "\t '%s' -> mean.", plotDir.getFileName().toString());
				
				computeAndSaveTable(project, plotDir, 
						StatisticsCategories.MEAN_RANK, StatisticsCategories.MEAN_FIRST_RANK, 
						percentages, localizers);
				
				Log.out(GenerateTable.class, "\t '%s' -> mean, best lambdas.", plotDir.getFileName().toString());
				
				computeAndSaveTableBestLambdas(project, plotDir, 
						StatisticsCategories.MEAN_RANK, StatisticsCategories.MEAN_FIRST_RANK, 
						percentages, localizers);


				Log.out(GenerateTable.class, "\t '%s' -> median.", plotDir.getFileName().toString());
				
				computeAndSaveTable(project, plotDir, 
						StatisticsCategories.MEDIAN_RANK, StatisticsCategories.MEDIAN_FIRST_RANK, 
						percentages, localizers);
				
				Log.out(GenerateTable.class, "\t '%s' -> median, best lambdas.", plotDir.getFileName().toString());
				
				computeAndSaveTableBestLambdas(project, plotDir, 
						StatisticsCategories.MEDIAN_RANK, StatisticsCategories.MEDIAN_FIRST_RANK, 
						percentages, localizers);
				
			}
			
			List<Path> foundMainBucketPaths = new SearchForFilesOrDirsModule("**_buckets_total**", true)
					.searchForDirectories()
					.skipSubTreeAfterMatch()
					.submit(foundPath)
					.getResult();
			
			for (Path bucketsDir : foundMainBucketPaths) {
				Log.out(GenerateTable.class, "\t '%s' -> mean, cross-validation.", bucketsDir.getFileName().toString());
				
				computeAndSaveTableForMainBucketDir(project, bucketsDir, 
						StatisticsCategories.MEAN_RANK, 
						percentages, localizers);
				computeAndSaveTableForMainBucketDir(project, bucketsDir, 
						StatisticsCategories.MEAN_FIRST_RANK, 
						percentages, localizers);
				
				Log.out(GenerateTable.class, "\t '%s' -> median, cross-validation.", bucketsDir.getFileName().toString());
				
				computeAndSaveTableForMainBucketDir(project, bucketsDir, 
						StatisticsCategories.MEDIAN_RANK, 
						percentages, localizers);
				computeAndSaveTableForMainBucketDir(project, bucketsDir, 
						StatisticsCategories.MEDIAN_FIRST_RANK, 
						percentages, localizers);
				
			}
		}

	}

	private static void computeAndSaveTableForMainBucketDir(String project, Path bucketsDir,
			StatisticsCategories rank, Double[] percentages, String[] localizers) {
		List<Path> foundLargeBucketPaths = new SearchForFilesOrDirsModule("**_rest**", true)
				.searchForDirectories()
				.skipSubTreeAfterMatch()
				.submit(bucketsDir)
				.getResult();
		
			new PipeLinker().append(
					new ListSequencerPipe<String>(),
					new AbstractPipe<String, String[]>(true) {

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
							List<Double> sbflImprovementsByBestSmallLambda = new ArrayList<>(10);
							
							List<Double> lmImprovementsByBestLargeLambda = new ArrayList<>(10);
							List<Double> lmImprovementsByBestSmallLambda = new ArrayList<>(10);
							
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
								Double smallbestPercentage = getBestPercentage(rank, localizer, smallBucketLocalizerDir);
								
								
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
								sbflImprovementsByBestSmallLambda.add(getImprovement(sbflValue, rankMap.get(smallbestPercentage)));

								lmImprovementsByBestLargeLambda.add(getImprovement(lmValue, rankMap.get(largebestPercentage)));
								lmImprovementsByBestSmallLambda.add(getImprovement(lmValue, rankMap.get(smallbestPercentage)));
							}
							
							double maxSbflImprovementByBestLargeLambda = MathUtils.getMax(sbflImprovementsByBestLargeLambda);
							double minSbflImprovementByBestLargeLambda = MathUtils.getMin(sbflImprovementsByBestLargeLambda);
							double medianSbflImprovementByBestLargeLambda = MathUtils.getMedian(sbflImprovementsByBestLargeLambda);
							double meanSbflImprovementByBestLargeLambda = MathUtils.getMean(sbflImprovementsByBestLargeLambda);
							
							double maxSbflImprovementByBestSmallLambda = MathUtils.getMax(sbflImprovementsByBestSmallLambda);
							double minSbflImprovementByBestSmallLambda = MathUtils.getMin(sbflImprovementsByBestSmallLambda);
							double medianSbflImprovementByBestSmallLambda = MathUtils.getMedian(sbflImprovementsByBestSmallLambda);
							double meanSbflImprovementByBestSmallLambda = MathUtils.getMean(sbflImprovementsByBestSmallLambda);
							
							double maxLmImprovementByBestLargeLambda = MathUtils.getMax(lmImprovementsByBestLargeLambda);
							double minLmImprovementByBestLargeLambda = MathUtils.getMin(lmImprovementsByBestLargeLambda);
							double medianLmImprovementByBestLargeLambda = MathUtils.getMedian(lmImprovementsByBestLargeLambda);
							double meanLmImprovementByBestLargeLambda = MathUtils.getMean(lmImprovementsByBestLargeLambda);
							
							double maxLmImprovementByBestSmallLambda = MathUtils.getMax(lmImprovementsByBestSmallLambda);
							double minLmImprovementByBestSmallLambda = MathUtils.getMin(lmImprovementsByBestSmallLambda);
							double medianLmImprovementByBestSmallLambda = MathUtils.getMedian(lmImprovementsByBestSmallLambda);
							double meanLmImprovementByBestSmallLambda = MathUtils.getMean(lmImprovementsByBestSmallLambda);

							/* & localizer 
							 * & median_best_large_lambda 
							 *   (mean_best_large_lambda)
							 * & range(best_large_lambda) 
							 * 
							 * & median_sbfl_improvement_by_best_large_lambda
							 *   (mean_sbfl_improvement_by_best_large_lambda)
							 * & range(sbfl_improvement_by_best_large_lambda)
							 * 
							 * & median_sbfl_improvement_by_best_small_lambda
							 *   (mean_sbfl_improvement_by_best_small_lambda)
							 * & range(sbfl_improvement_by_best_small_lambda)
							 * 
							 * & median_lm_improvement_by_best_large_lambda
							 *   (mean_lm_improvement_by_best_large_lambda)
							 * & range(lm_improvement_by_best_large_lambda)
							 * 
							 * & median_lm_improvement_by_best_small_lambda
							 *   (mean_lm_improvement_by_best_small_lambda)
							 * & range(lm_improvement_by_best_small_lambda)
							 */
							String[] result = new String[11];
							int counter = 0;
							result[counter++] = getEscapedLocalizerName(localizer);
							result[counter++] = getLambdaAsString(medianLargeLambda) 
									+ "(" + getLambdaAsString(meanLargeLambda) + ")";
//							result[counter++] = getLambdaAsString(meanLargeLambda);
							result[counter++] = getLambdaRange(minLargeLambda, maxLargeLambda);
							
							result[counter++] = getPercentageAsString(medianSbflImprovementByBestLargeLambda) 
									+ "(" + getPercentageAsString(meanSbflImprovementByBestLargeLambda) + ")";
//							result[counter++] = getPercentageAsString(meanSbflImprovementByBestLargeLambda);
							result[counter++] = getPercentageRange(minSbflImprovementByBestLargeLambda, maxSbflImprovementByBestLargeLambda);
							
							result[counter++] = getPercentageAsString(medianSbflImprovementByBestSmallLambda)
									+ "(" + getPercentageAsString(meanSbflImprovementByBestSmallLambda) + ")";
//							result[counter++] = getPercentageAsString(meanSbflImprovementByBestSmallLambda);
							result[counter++] = getPercentageRange(minSbflImprovementByBestSmallLambda, maxSbflImprovementByBestSmallLambda);
							
							result[counter++] = getPercentageAsString(medianLmImprovementByBestLargeLambda)
									+ "(" + getPercentageAsString(meanLmImprovementByBestLargeLambda) + ")";
//							result[counter++] = getPercentageAsString(meanLmImprovementByBestLargeLambda);
							result[counter++] = getPercentageRange(minLmImprovementByBestLargeLambda, maxLmImprovementByBestLargeLambda);
							
							result[counter++] = getPercentageAsString(medianLmImprovementByBestSmallLambda)
									+ "(" + getPercentageAsString(meanLmImprovementByBestSmallLambda) + ")";
//							result[counter++] = getPercentageAsString(meanLmImprovementByBestSmallLambda);
							result[counter++] = getPercentageRange(minLmImprovementByBestSmallLambda, maxLmImprovementByBestSmallLambda);
							
							return result;
						}

					},
					new AbstractPipe<String[], List<String>>(true) {
						Map<String, String[]> map = new HashMap<>();
						@Override
						public List<String> processItem(String[] item) {
							map.put(item[0], item);
							return null;
						}
						@Override
						public List<String> getResultFromCollectedItems() {
							/* & localizer 
							 * & median_best_large_lambda (\\lambda_{pred})
							 *   (mean_best_large_lambda)
							 * & range(best_large_lambda) 
							 * 
							 * & median_sbfl_improvement_by_best_large_lambda
							 *   (mean_sbfl_improvement_by_best_large_lambda)
							 * & range(sbfl_improvement_by_best_large_lambda)
							 * 
							 * & median_sbfl_improvement_by_best_small_lambda (\\lambda_{tr})
							 *   (mean_sbfl_improvement_by_best_small_lambda)
							 * & range(sbfl_improvement_by_best_small_lambda)
							 * 
							 * & median_lm_improvement_by_best_large_lambda
							 *   (mean_lm_improvement_by_best_large_lambda)
							 * & range(lm_improvement_by_best_large_lambda)
							 * 
							 * & median_lm_improvement_by_best_small_lambda
							 *   (mean_lm_improvement_by_best_small_lambda)
							 * & range(lm_improvement_by_best_small_lambda)
							 */
							String[] titleArray1 = new String[11];
							int counter = 0;
							titleArray1[counter++] = "\\hfill SBFL ranking metric \\hfill";
							titleArray1[counter++] = "$\\tilde{\\lambda_{pred}}(\\bar{\\lambda_{pred}})$";
							titleArray1[counter++] = "$[\\min,\\max]$";
							
							titleArray1[counter++] = "$\\tilde{RRI^{SBFL}_{\\lambda_{pred}}}(\\bar{RRI^{SBFL}_{\\lambda_{pred}}})$";
							titleArray1[counter++] = "$[\\min,\\max]$";
							
							titleArray1[counter++] = "$\\tilde{RRI^{SBFL}_{\\lambda_{tr}}}(\\bar{RRI^{SBFL}_{\\lambda_{tr}}})$";
							titleArray1[counter++] = "$[\\min,\\max]$";
							
							titleArray1[counter++] = "$\\tilde{RRI^{LM}_{\\lambda_{pred}}}(\\bar{RRI^{LM}_{\\lambda_{pred}}})$";
							titleArray1[counter++] = "$[\\min,\\max]$";
							
							titleArray1[counter++] = "$\\tilde{RRI^{LM}_{\\lambda_{tr}}}(\\bar{RRI^{LM}_{\\lambda_{tr}}})$";
							titleArray1[counter++] = "$[\\min,\\max]$";
							map.put("1", titleArray1);

							return LaTexUtils.generateSimpleLaTexTable(Misc.sortByKeyToValueList(map));
						}
					},
					new ListToFileWriterModule<List<String>>(bucketsDir.resolve(project + "_" + rank + "_crossValidationTable.tex"), true)
					).submitAndShutdown(Arrays.asList(localizers));
	}
	
	private static double getImprovement(Double baseValue, Double newValue) {
		return -(newValue.doubleValue() / baseValue.doubleValue() * 100.0 - 100.0);
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

	public static void computeAndSaveTable(String project, Path plotDir, 
			StatisticsCategories rank, StatisticsCategories firstRank, 
			Double[] percentages, String[] localizers) {
		new PipeLinker().append(
				new ListSequencerPipe<String>(),
				new AbstractPipe<String, String[]>(true) {

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
							counter = fillArrayWithComputedValuesForFile(rank, 
									percentages, localizer, result, counter, rankFile);
						}

						{
							File firstRankFile = FileUtils.searchFileContainingPattern(localizerDir, "_" + firstRank + ".csv");
							counter = fillArrayWithComputedValuesForFile(firstRank, 
									percentages, localizer, result, counter, firstRankFile);
						}

						return result;
					}

					public int fillArrayWithComputedValuesForFile(StatisticsCategories rank, Double[] percentages,
							String localizer, String[] result, int counter, File rankFile) {
						if (rankFile == null) {
							Log.abort(GenerateTable.class, rank + " csv file doesn't exist for localizer '" + localizer + "'.");
						}
						
						List<Double[]> rankList = CSVUtils.readCSVFileToListOfDoubleArrays(rankFile.toPath());
						return fillArrayWithComputedValues(rank, percentages, result, counter, rankList);
					}
				},
				new AbstractPipe<String[], List<String>>(true) {
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
							titleArray2[counter++] = "$\\lambda=" + MathUtils.roundToXDecimalPlaces(percentage/100.0, 2) +"$";
						}
						titleArray2[counter++] = "improv.";
						for (double percentage : percentages) {
							titleArray2[counter++] = "$\\lambda=" + MathUtils.roundToXDecimalPlaces(percentage/100.0, 2) +"$";
						}
						titleArray2[counter++] = "improv.";
						map.put("2", titleArray2);

						return LaTexUtils.generateSimpleLaTexTable(Misc.sortByKeyToValueList(map));
					}
				},
				new ListToFileWriterModule<List<String>>(plotDir.resolve(project + "_big_" + rank + "_" + firstRank + "_Table.tex"), true)
				).submitAndShutdown(Arrays.asList(localizers));
	}
	
	public static void computeAndSaveTableBestLambdas(String project, Path plotDir, 
			StatisticsCategories rank, StatisticsCategories firstRank, 
			Double[] percentages, String[] localizers) {
		new PipeLinker().append(
				new ListSequencerPipe<String>(),
				new AbstractPipe<String, String[]>(true) {

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
							counter = fillArrayWithComputedValuesBestLambda(rank, percentages, result, counter, rankList);
						}

						{
							List<Double[]> firstRankList = CSVUtils.readCSVFileToListOfDoubleArrays(firstRankFile.toPath());
							counter = fillArrayWithComputedValuesBestLambda(firstRank, percentages, result, counter, firstRankList);
						}

						return result;
					}
				},
				new AbstractPipe<String[], List<String>>(true) {
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
						titleArray1[counter++] = "best";
						titleArray1[counter++] = "best";
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
				new ListToFileWriterModule<List<String>>(plotDir.resolve(project + "_bestLambda_" + rank + "_" + firstRank + "_Table.tex"), true)
				).submitAndShutdown(Arrays.asList(localizers));
	}
	
	private static String getEscapedLocalizerName(String localizer) {
		return "\\" + localizer.replace("1","ONE").replace("2","TWO").replace("3","THREE");
	}

	private static int fillArrayWithComputedValues(StatisticsCategories rank, Double[] percentages, 
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
					result[counter++] = "\\textbf{" + String.valueOf(MathUtils.roundToXDecimalPlaces(value, 1)) + "}";
				} else { 
					result[counter++] = String.valueOf(MathUtils.roundToXDecimalPlaces(value, 1));
				}
			}
			result[counter++] = String.valueOf(MathUtils.roundToXDecimalPlaces(
					-(bestValue.doubleValue() / fullValue.doubleValue() * 100.0 - 100.0), 1)) + "\\%";
		}
		return counter;
	}
	
	private static int fillArrayWithComputedValuesBestLambda(StatisticsCategories rank, Double[] percentages, 
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
	
	private static int fillArrayWithComputedValues(StatisticsCategories rank, Double[] percentages,
			String[] result, int counter, List<Double[]> largeRankList, List<Double[]> smallRankList) {
		// best_large_lambda & large_absolute_ranking & best_small_lambda & small_absolute_ranking & 
		//		absolute_worsening & percentage_based_worsening
		Map<Double, Double> largeRankMap = new HashMap<>();
		for (Double[] array : largeRankList) {
			largeRankMap.put(array[0], array[1]);
		}
		Double largeFullValue = largeRankMap.get(100.0);
		if (largeFullValue == null) {
			Log.abort(GenerateTable.class, "percentage value '100.0%' not existing in large " + rank + " csv file.");
		}
		//compute the lamda value with the best ranking result
		Double largebestValue = largeFullValue;
		Double largebestPercentage = 100.0;
		for (Entry<Double, Double> entry : largeRankMap.entrySet()) {
			Double value = entry.getValue();
			if (value == null) {
				Log.abort(GenerateTable.class, "percentage value '" + entry.getKey() + "%' has null value in large " + rank + " csv file.");
			}
			if (value.compareTo(largebestValue) < 0) {
				largebestValue = value;
				largebestPercentage = entry.getKey();
			}
		}
		result[counter++] = String.valueOf(MathUtils.roundToXDecimalPlaces(largebestPercentage.doubleValue() / 100.0, 2));
		result[counter++] = String.valueOf(MathUtils.roundToXDecimalPlaces(largebestValue.doubleValue(), 1));


		Map<Double, Double> smallRankMap = new HashMap<>();
		for (Double[] array : smallRankList) {
			smallRankMap.put(array[0], array[1]);
		}
		Double smallFullValue = smallRankMap.get(100.0);
		if (smallFullValue == null) {
			Log.abort(GenerateTable.class, "percentage value '100.0%' not existing in small " + rank + " csv file.");
		}
		//compute the lamda value with the best ranking result
		Double smallbestValue = smallFullValue;
		Double smallbestPercentage = 100.0;
		for (Entry<Double, Double> entry : smallRankMap.entrySet()) {
			Double value = entry.getValue();
			if (value == null) {
				Log.abort(GenerateTable.class, "percentage value '" + entry.getKey() + "%' has null value in small " + rank + " csv file.");
			}
			if (value.compareTo(smallbestValue) < 0) {
				smallbestValue = value;
				smallbestPercentage = entry.getKey();
			}
		}
		result[counter++] = String.valueOf(MathUtils.roundToXDecimalPlaces(smallbestPercentage.doubleValue() / 100.0, 2));
		result[counter++] = String.valueOf(MathUtils.roundToXDecimalPlaces(smallbestValue.doubleValue(), 1));
		
		Double smallOtherValue = smallRankMap.get(largebestPercentage);
		
		result[counter++] = String.valueOf(MathUtils.roundToXDecimalPlaces(smallOtherValue.doubleValue() - smallbestValue.doubleValue(), 1));
		result[counter++] = String.valueOf(MathUtils.roundToXDecimalPlaces(smallOtherValue.doubleValue() / smallbestValue.doubleValue() * 100 - 100, 1)) + "\\%";

		return counter;
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
