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

import se.de.hu_berlin.informatik.rankingplotter.plotter.Plot;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
import se.de.hu_berlin.informatik.rankingplotter.plotter.RankingFileWrapper;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.DataTableCollection;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.DiffDataTableCollection;
import se.de.hu_berlin.informatik.utils.fileoperations.ListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.fileoperations.csv.CSVUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.MathUtils;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * Module that takes a {@link List} of {@link RankingFileWrapper} objects,  adds the data points
 * to the corresponding data tables, produces a label map and returns a {@link DataTableCollection}.
 * 
 * @author Simon Heiden
 */
public class RankingAveragerModule extends AbstractModule<List<RankingFileWrapper>, DataTableCollection> {

	public static final String MOD_CHANGE = "CHANGE";
	public static final String MOD_DELETE = "DELETE";
	public static final String MOD_INSERT = "INSERT";
	public static final String MOD_UNKNOWN = "UNK";
	
	public static final String SIGNIFICANCE_NONE = "NONE";
	public static final String SIGNIFICANCE_LOW = "LOW";
	public static final String SIGNIFICANCE_MEDIUM = "MEDIUM";
	public static final String SIGNIFICANCE_HIGH = "HIGH";
	public static final String SIGNIFICANCE_CRUCIAL = "CRUCIAL";
	
	public static final String SIGNIFICANCE_ALL = "ALL";
	
	public static final String MEAN_FIRST_RANK = "FIRST_RANK";
	
	private String localizerName;
	
	private List<RankingFileWrapper> averagedRankings;
	private boolean firstInput = true;
	
	final private Path outputOfCsvMain;
	//percentage -> project -> id -> ranking wrapper
	private Map<Double,Map<String, Map<Integer, RankingFileWrapper>>> percentageToProjectToBugToRanking;
	
	/**
	 * Creates a new {@link RankingAveragerModule} object with the given parameters.
	 * @param localizerName
	 * identifier of the SBFL localizer
	 * @param outputOfCsvMain
	 * the output path for generated CSV files
	 */
	public RankingAveragerModule(String localizerName, Path outputOfCsvMain) {
		super(true);
		this.outputOfCsvMain = outputOfCsvMain;
		this.localizerName = localizerName;
		averagedRankings = new ArrayList<>();
		percentageToProjectToBugToRanking = new HashMap<>();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public DataTableCollection processItem(List<RankingFileWrapper> rankingFiles) {
		//create an empty ranking file wrapper that will hold the averaged rankings, etc.
		//only need to create this once! (and can't create it at initialization...)
		if (firstInput ) {
			for (final RankingFileWrapper item : rankingFiles) {
				averagedRankings.add(new RankingFileWrapper("", 0, null, 
						item.getSBFLPercentage(),
						null, ParserStrategy.NO_CHANGE));
			}
			firstInput = false;
		}
		
		//update the averaged rankings
		int fileno = 0;
		for (final RankingFileWrapper item : rankingFiles) {
			//add the minimum rank of this item to the map
			percentageToProjectToBugToRanking
			.computeIfAbsent(item.getSBFL(), k -> new HashMap<>())
			.computeIfAbsent(item.getProject(), k -> new HashMap<>())
			.put(item.getBugId(), item);

			updateValues(averagedRankings.get(fileno), item);
			++fileno;
		}
		
		return null;
	}
	
	private List<String> generateMinRankCSV(Map<Double, Map<String, Map<Integer, RankingFileWrapper>>> percentageToBugMap) {
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
	
	private List<String> generateMeanRankCSV(Map<Double, Map<String, Map<Integer, RankingFileWrapper>>> percentageToBugMap) {
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
	
	private List<String> generateStatisticsCSV(Map<Double, Map<String, Map<Integer, RankingFileWrapper>>> percentageToBugMap, 
			Map<Double, Double> percToMeanRankMap, Map<Double, Double> percToMeanFirstRankMap) {
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

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#getResultFromCollectedItems()
	 */
	public DataTableCollection getResultFromCollectedItems() {
		
		Path output = Paths.get(outputOfCsvMain.toString() + "minRanks.csv");
		new ListToFileWriterModule<List<String>>(output, true)
		.submit(generateMinRankCSV(percentageToProjectToBugToRanking));
		
		Path output2 = Paths.get(outputOfCsvMain.toString() + "meanRanks.csv");
		new ListToFileWriterModule<List<String>>(output2, true)
		.submit(generateMeanRankCSV(percentageToProjectToBugToRanking));
		
		
		DiffDataTableCollection tables = new DiffDataTableCollection();
		Integer temp = Plot.DEFAULT_RANGE;
		
		//set the labels
		int fileno = 0;
		tables.getLabelMap().put((double)fileno, localizerName);

		for (final RankingFileWrapper item : averagedRankings) {
			++fileno;
			tables.addLabel(fileno, item.getSBFL());
		}
		
		//perc -> mean rank
		Map<Double, Double> percToMeanRankMap = new HashMap<>();
		
		//perc -> mean first rank
		Map<Double, Double> percToMeanFirstRankMap = new HashMap<>();
		
		//add the data points to the tables
		final int[] outlierCount = new int[averagedRankings.size()];
		fileno = 0;
		for (final RankingFileWrapper averagedRanking : averagedRankings) {
			++fileno;
			double rank;
			
			tables.addData(DiffDataTableCollection.HIT_AT_1, fileno, averagedRanking.getHitAtXMap().get(1));
			tables.addData(DiffDataTableCollection.HIT_AT_5, fileno, averagedRanking.getHitAtXMap().get(5));
			tables.addData(DiffDataTableCollection.HIT_AT_10, fileno, averagedRanking.getHitAtXMap().get(10));
			tables.addData(DiffDataTableCollection.HIT_AT_20, fileno, averagedRanking.getHitAtXMap().get(20));
			tables.addData(DiffDataTableCollection.HIT_AT_30, fileno, averagedRanking.getHitAtXMap().get(30));
			tables.addData(DiffDataTableCollection.HIT_AT_50, fileno, averagedRanking.getHitAtXMap().get(50));
			tables.addData(DiffDataTableCollection.HIT_AT_100, fileno, averagedRanking.getHitAtXMap().get(100));
			tables.addData(DiffDataTableCollection.HIT_AT_INF, fileno, averagedRanking.getHitAtXMap().get(Integer.MAX_VALUE));
			
			if (averagedRanking.getMinRankSum() > 0) {
				rank = averagedRanking.getMeanFirstRank();
				percToMeanFirstRankMap.put(averagedRanking.getSBFL(), rank);
				if (tables.addData(MEAN_FIRST_RANK, fileno, rank) && rank > temp) {
//					++outlierCount[fileno-1];
				}
			}
			if (averagedRanking.getAll() > 0) {
				rank = averagedRanking.getAllAverage();
				percToMeanRankMap.put(averagedRanking.getSBFL(), rank);
				if (tables.addData(SIGNIFICANCE_ALL, fileno, rank) && rank > temp) {
//					++outlierCount[fileno-1];
				}
			}
			if (averagedRanking.getUnsignificantChanges() > 0) {
				rank = averagedRanking.getUnsignificantChangesAverage();
				if (tables.addData(SIGNIFICANCE_NONE, fileno, rank) && rank > temp) {
					++outlierCount[fileno-1];
				}
			}
			if (averagedRanking.getLowSignificanceChanges() > 0) {
				rank = averagedRanking.getLowSignificanceChangesAverage();
				if (tables.addData(SIGNIFICANCE_LOW, fileno, rank) && rank > temp) {
					++outlierCount[fileno-1];
				}
			}
			if (averagedRanking.getMediumSignificanceChanges() > 0) {
				rank = averagedRanking.getMediumSignificanceChangesAverage();
				if (tables.addData(SIGNIFICANCE_MEDIUM, fileno, rank) && rank > temp) {
					++outlierCount[fileno-1];
				}
			}
			if (averagedRanking.getHighSignificanceChanges() > 0) {
				rank = averagedRanking.getHighSignificanceChangesAverage();
				if (tables.addData(SIGNIFICANCE_HIGH, fileno, rank) && rank > temp) {
					++outlierCount[fileno-1];
				}
			}
			if (averagedRanking.getCrucialSignificanceChanges() > 0) {
				rank = averagedRanking.getCrucialSignificanceChangesAverage();
				if (tables.addData(SIGNIFICANCE_CRUCIAL, fileno, rank) && rank > temp) {
					++outlierCount[fileno-1];
				}
			}
			
			
			if (averagedRanking.getModChanges() > 0) {
				tables.addData(MOD_CHANGE, fileno, averagedRanking.getModChangesAverage());
			}
			
			if (averagedRanking.getModDeletes() > 0) {
				tables.addData(MOD_DELETE, fileno, averagedRanking.getModDeletesAverage());
			}
			
			if (averagedRanking.getModUnknowns() > 0) {
				tables.addData(MOD_UNKNOWN, fileno, averagedRanking.getModUnknownsAverage());
			}
			
			if (averagedRanking.getModInserts() > 0) {
				tables.addData(MOD_INSERT, fileno, averagedRanking.getModInsertsAverage());
			}
			
		}
		
		tables.addOutlierData(temp, outlierCount);
		
		
		Path output3 = Paths.get(outputOfCsvMain.toString() + "statistics.csv");
		new ListToFileWriterModule<List<String>>(output3, true)
		.submit(generateStatisticsCSV(percentageToProjectToBugToRanking, percToMeanRankMap, percToMeanFirstRankMap));
		
		return tables;
	}

	private static void updateValues(RankingFileWrapper ar, RankingFileWrapper item) {
		if (item.getMinRank() != null) {
			ar.addToMinRankSum(item.getMinRank());
			ar.addToMinRankCount(1);
		}
		
		for (Entry<Integer,Integer> entry : item.getHitAtXMap().entrySet()) {
			int key = entry.getKey();
			ar.getHitAtXMap().put(key, entry.getValue() + ar.getHitAtXMap().get(key));
		}
		
		ar.addToAllSum(item.getAllSum());
		ar.addToAll(item.getAll());
		
		ar.addToUnsignificantChangesSum(item.getUnsignificantChangesSum());
		ar.addToUnsignificantChanges(item.getUnsignificantChanges());
		
		ar.addToLowSignificanceChangesSum(item.getLowSignificanceChangesSum());
		ar.addToLowSignificanceChanges(item.getLowSignificanceChanges());
		
		ar.addToMediumSignificanceChangesSum(item.getMediumSignificanceChangesSum());
		ar.addToMediumSignificanceChanges(item.getMediumSignificanceChanges());
		
		ar.addToHighSignificanceChangesSum(item.getHighSignificanceChangesSum());
		ar.addToHighSignificanceChanges(item.getHighSignificanceChanges());
		
		ar.addToCrucialSignificanceChangesSum(item.getCrucialSignificanceChangesSum());
		ar.addToCrucialSignificanceChanges(item.getCrucialSignificanceChanges());
		
		
		ar.addToModChangesSum(item.getModChangesSum());
		ar.addToModChanges(item.getModChanges());
		
		ar.addToModDeletesSum(item.getModDeletesSum());
		ar.addToModDeletes(item.getModDeletes());
		
		ar.addToModInsertsSum(item.getModInsertsSum());
		ar.addToModInserts(item.getModInserts());
		
		ar.addToModUnknownsSum(item.getModUnknownsSum());
		ar.addToModUnknowns(item.getModUnknowns());
	}
	
//	private class RankingUpdateCall implements Runnable {
//
//		final private RankingFileWrapper ar;
//		final private RankingFileWrapper item;
//		
//		
//		public RankingUpdateCall(RankingFileWrapper ar, RankingFileWrapper item) {
//			super();
//			this.ar = ar;
//			this.item = item;
//		}
//
//		@Override
//		public void run() {
//			updateValues(ar, item);
//		}
//
//	}
	
}
