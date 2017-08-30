/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.modules;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import se.de.hu_berlin.informatik.rankingplotter.plotter.RankingFileWrapper;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.AveragePlotStatisticsCollection;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.AveragePlotStatisticsCollection.StatisticsCategories;
import se.de.hu_berlin.informatik.utils.files.csv.CSVUtils;
import se.de.hu_berlin.informatik.utils.files.processors.ListToFileWriter;
import se.de.hu_berlin.informatik.utils.miscellaneous.MathUtils;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * A module that takes a {@link AveragePlotStatisticsCollection} object and produces 
 * various CSV files.
 * 
 * @author Simon Heiden
 */
public class AverageplotCSVGeneratorModule extends AbstractProcessor<AveragePlotStatisticsCollection, AveragePlotStatisticsCollection> {

	private String outputPrefix;

	/**
	 * Creates a new {@link AverageplotCSVGeneratorModule} object with the given parameters.
	 * @param outputPrefix
	 * the output filename prefix 
	 */
	public AverageplotCSVGeneratorModule(String outputPrefix) {
		super();
		this.outputPrefix = outputPrefix;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public AveragePlotStatisticsCollection processItem(AveragePlotStatisticsCollection tables) {

		//create CSV files from all included tables
		for (Entry<StatisticsCategories, List<Double[]>> entry : tables.getStatisticsmap().entrySet()) {
			Path output = Paths.get(outputPrefix + "_" + entry.getKey() + ".csv");
			new ListToFileWriter<List<String>>(output, true)
			.submit(CSVUtils.toCsv(entry.getValue()));
		}
		
		if (tables.getPercentageToProjectToBugToRankingMap() != null) {
			//generate a CSV file that holds all minimal (top) rankings for all bugs
			Path output = Paths.get(outputPrefix + "_minRanks.csv");
			new ListToFileWriter<List<String>>(output, true)
			.submit(generateMinRankCSV(tables.getPercentageToProjectToBugToRankingMap()));

			//generate a CSV file that holds all mean rankings for all bugs
			Path output2 = Paths.get(outputPrefix + "_meanRanks.csv");
			new ListToFileWriter<List<String>>(output2, true)
			.submit(generateMeanRankCSV(tables.getPercentageToProjectToBugToRankingMap()));

			//perc -> mean rank
			Map<Double, Double> percToMeanRankMap = new HashMap<>();
			for (Double[] pair : tables.getStatistics(StatisticsCategories.MEAN_RANK)) {
				percToMeanRankMap.put(pair[0], pair[1]);
			}

			//perc -> mean first rank
			Map<Double, Double> percToMeanFirstRankMap = new HashMap<>();
			for (Double[] pair : tables.getStatistics(StatisticsCategories.MEAN_FIRST_RANK)) {
				percToMeanFirstRankMap.put(pair[0], pair[1]);
			}
			
			//perc -> median rank
			Map<Double, Double> percToMedianRankMap = new HashMap<>();
			for (Double[] pair : tables.getStatistics(StatisticsCategories.MEDIAN_RANK)) {
				percToMedianRankMap.put(pair[0], pair[1]);
			}

			//perc -> median first rank
			Map<Double, Double> percToMedianFirstRankMap = new HashMap<>();
			for (Double[] pair : tables.getStatistics(StatisticsCategories.MEDIAN_FIRST_RANK)) {
				percToMedianFirstRankMap.put(pair[0], pair[1]);
			}

			//generate a CSV file that holds some statistics
			Path output3 = Paths.get(outputPrefix + "_statistics.csv");
			new ListToFileWriter<List<String>>(output3, true)
			.submit(generateStatisticsCSV(tables.getPercentageToProjectToBugToRankingMap(), 
					percToMeanRankMap, percToMeanFirstRankMap, percToMedianRankMap, percToMedianFirstRankMap));
		}
		
		return tables;

	}
	
	private static List<String> generateMinRankCSV(Map<Double, Map<String, Map<Integer, RankingFileWrapper>>> percentageToBugMap) {
		int entryCount = 0;
		String[] projects = null;
		List<Object[]> csvLineArrays = new ArrayList<>();
		
		Map<String,Integer[]> projectToBugIds = new HashMap<>();
		
		boolean isFirst = true;
		Double[] percentages = percentageToBugMap.keySet().toArray(new Double[0]);
		Arrays.sort(percentages);
		//for each SBFL percentage
		for (double sbflPercentage : percentages) {
			Map<String, Map<Integer, RankingFileWrapper>> projectMap = percentageToBugMap.get(sbflPercentage);
			
			//first, fill the identifier lines at the top
			if (isFirst) {
				//for each project
				for (String project : projectMap.keySet()) {
					//get total number of bugs
					entryCount += projectMap.get(project).keySet().size();
				}
				//get array of all projects and sort it
				projects = projectMap.keySet().toArray(new String[0]);
				Arrays.sort(projects);
				
				//initialize arrays for projects and bug ids for the csv file
				String[] projectArray = new String[entryCount+1];
				projectArray[0] = "";
				Integer[] bugIdArray = new Integer[entryCount+1];
				bugIdArray[0] = 0;
				
				//iterate through all projects and bugs and fill the csv line arrays
				int total = 1;
				for (String project : projects) {
					Map<Integer, RankingFileWrapper> bugs = projectMap.get(project);
					Integer[] bugArray = bugs.keySet().toArray(new Integer[0]);
					Arrays.sort(bugArray);
					//insert sorted bug id arrays for easier reference
					projectToBugIds.put(project, bugArray);
					//for each bug id of the project, fill the respective array spots
					for (int i = 0; i < bugArray.length; ++i) {
						projectArray[total] = project;
						bugIdArray[total] = bugArray[i];
						++total;
					}
				}
				
				
				csvLineArrays.add(projectArray);
				csvLineArrays.add(bugIdArray);
				isFirst = false;
			}
			
			//now, fill the actual lines with values
			Integer[] values = new Integer[entryCount+1];
			values[0] = (int) (sbflPercentage * 100);
			
			//iterate over all projects
			int total = 1;
			for (String project : projects) {
				Map<Integer, RankingFileWrapper> bugs = projectMap.get(project);
				Integer[] bugArray = projectToBugIds.get(project);
				//for each bug id of the project, fill the respective array spots
				for (int i = 0; i < bugArray.length; ++i) {
					values[total++] = bugs.get(bugArray[i]).getMinRank();
				}
			}
			csvLineArrays.add(values);
		}
		
		return CSVUtils.toCsv(csvLineArrays);
	}
	
	private static List<String> generateMeanRankCSV(Map<Double, Map<String, Map<Integer, RankingFileWrapper>>> percentageToBugMap) {
		int entryCount = 0;
		String[] projects = null;
		List<Object[]> csvLineArrays = new ArrayList<>();
		
		Map<String,Integer[]> projectToBugIds = new HashMap<>();
		
		boolean isFirst = true;
		Double[] percentages = percentageToBugMap.keySet().toArray(new Double[0]);
		Arrays.sort(percentages);
		//for each SBFL percentage
		for (double sbflPercentage : percentages) {
			Map<String, Map<Integer, RankingFileWrapper>> projectMap = percentageToBugMap.get(sbflPercentage);
			
			//first, fill the identifier lines at the top
			if (isFirst) {
				//for each project
				for (String project : projectMap.keySet()) {
					//get total number of bugs
					entryCount += projectMap.get(project).keySet().size();
				}
				//get array of all projects and sort it
				projects = projectMap.keySet().toArray(new String[0]);
				Arrays.sort(projects);
				
				//initialize arrays for projects and bug ids for the csv file
				String[] projectArray = new String[entryCount+1];
				projectArray[0] = "";
				Integer[] bugIdArray = new Integer[entryCount+1];
				bugIdArray[0] = 0;
				
				//iterate through all projects and bugs and fill the csv line arrays
				int total = 1;
				for (String project : projects) {
					Map<Integer, RankingFileWrapper> bugs = projectMap.get(project);
					Integer[] bugArray = bugs.keySet().toArray(new Integer[0]);
					Arrays.sort(bugArray);
					//insert sorted bug id arrays for easier reference
					projectToBugIds.put(project, bugArray);
					//for each bug id of the project, fill the respective array spots
					for (int i = 0; i < bugArray.length; ++i) {
						projectArray[total] = project;
						bugIdArray[total] = bugArray[i];
						++total;
					}
				}
				
				
				csvLineArrays.add(projectArray);
				csvLineArrays.add(bugIdArray);
				isFirst = false;
			}
			
			//now, fill the actual lines with values
			Double[] values = new Double[entryCount+1];
			values[0] = MathUtils.roundToXDecimalPlaces(sbflPercentage * 100, 0);
			
			//iterate over all projects
			int total = 1;
			for (String project : projects) {
				Map<Integer, RankingFileWrapper> bugs = projectMap.get(project);
				Integer[] bugArray = projectToBugIds.get(project);
				//for each bug id of the project, fill the respective array spots
				for (int i = 0; i < bugArray.length; ++i) {
					values[total++] = MathUtils.roundToXDecimalPlaces(
							Double.valueOf(bugs.get(bugArray[i]).getAllSum()) 
							/ Double.valueOf(bugs.get(bugArray[i]).getAll()), 2);
				}
			}
			csvLineArrays.add(values);
		}
		
		return CSVUtils.toCsv(csvLineArrays);
	}
	
	private static List<String> generateStatisticsCSV(Map<Double, Map<String, Map<Integer, RankingFileWrapper>>> percentageToBugMap, 
			Map<Double, Double> percToMeanRankMap, Map<Double, Double> percToMeanFirstRankMap,
			Map<Double, Double> percToMedianRankMap, Map<Double, Double> percToMedianFirstRankMap) {
		String[] projects = null;
		List<Object[]> csvLineArrays = new ArrayList<>();
		
		Map<String,Integer[]> projectToBugIds = new HashMap<>();
		
		boolean isFirst = true;
		Double[] percentages = percentageToBugMap.keySet().toArray(new Double[0]);
		Arrays.sort(percentages);
		//for each SBFL percentage
		for (double sbflPercentage : percentages) {
			Map<String, Map<Integer, RankingFileWrapper>> projectMap = percentageToBugMap.get(sbflPercentage);
			
			//first, fill the identifier lines at the top
			if (isFirst) {
				projects = projectMap.keySet().toArray(new String[0]);
				Arrays.sort(projects);
				
				//iterate through all projects
				for (String project : projects) {
					Map<Integer, RankingFileWrapper> bugs = projectMap.get(project);
					Integer[] bugArray = bugs.keySet().toArray(new Integer[0]);
					Arrays.sort(bugArray);
					//insert sorted bug id arrays for easier reference
					projectToBugIds.put(project, bugArray);
				}
				
				
				String[] header = { "perc", "variance", "stdDev" };
				
				csvLineArrays.add(header);
				isFirst = false;
			}
			
			//now, fill the actual lines with values
			String[] values = new String[3];
			values[0] = String.valueOf(MathUtils.roundToXDecimalPlaces(sbflPercentage * 100, 0));
			
			List<Integer> rankingPositions = new ArrayList<>();
			//iterate over all projects
			for (String project : projects) {
				Map<Integer, RankingFileWrapper> bugs = projectMap.get(project);
				Integer[] bugArray = projectToBugIds.get(project);
				//for each bug id of the project, get all ranking positions of the faulty lines
				for (int i = 0; i < bugArray.length; ++i) {
					for (int rankPos : bugs.get(bugArray[i]).getChangedLinesRankings()) {
						rankingPositions.add(rankPos);
					}
				}
			}
			
			double[] rankingPositionsArray = new double[rankingPositions.size()];
			for (int i = 0; i < rankingPositions.size(); ++i) {
				rankingPositionsArray[i] = rankingPositions.get(i);
			}
			
			Variance variance = new Variance();
			
			values[1] = String.valueOf(MathUtils.roundToXDecimalPlaces(
					variance.evaluate(rankingPositionsArray, percToMeanRankMap.get(sbflPercentage)), 2));
			
			StandardDeviation deviation = new StandardDeviation();
			
			values[2] = String.valueOf(MathUtils.roundToXDecimalPlaces(
					deviation.evaluate(rankingPositionsArray, percToMeanRankMap.get(sbflPercentage)), 2));
			
			csvLineArrays.add(values);
		}
		
		return CSVUtils.toCsv(csvLineArrays);
	}
	
}
