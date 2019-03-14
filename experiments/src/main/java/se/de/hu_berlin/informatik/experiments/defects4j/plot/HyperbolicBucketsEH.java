package se.de.hu_berlin.informatik.experiments.defects4j.plot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JBuggyFixedEntity;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.ToolSpecific;
import se.de.hu_berlin.informatik.experiments.defects4j.plot.ComputeSBFLRankingsProcessor.ResultCollection;
import se.de.hu_berlin.informatik.experiments.defects4j.plot.HyperbolicEvoCrossValidation.StatisticsData;
import se.de.hu_berlin.informatik.faultlocalizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Hyperbolic;
import se.de.hu_berlin.informatik.gen.ranking.Spectra2Ranking;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.utils.experiments.evo.EvoItem;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.files.csv.CSVUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractConsumingProcessor;
import se.de.hu_berlin.informatik.utils.processors.basics.CollectionSequencer;
import se.de.hu_berlin.informatik.utils.processors.basics.ItemCollector;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.PipeLinker;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class HyperbolicBucketsEH extends AbstractConsumingProcessor<StatisticsCollector<HyperbolicEvoCrossValidation.StatisticsData>> {
	
	private final static String SEP = File.separator;

	private final int threadCount;

	private final String cvOutputDir;
	private final List<BuggyFixedEntity<?>>[] buckets;

	private final String suffix;

	private final static Object lock = new Object();

	private final List<IFaultLocalizer<SourceCodeBlock>> localizers;

	private final boolean onlyAddLocalizerValues;

	/**
	 * Initializes a {@link HyperbolicBucketsEH} object with the given parameters.
	 * @param onlyAddLocalizerValues
	 * whether to only compute the results for the given localizers on the test set
	 * @param suffix 
	 * a suffix to append to the ranking directory (may be null)
	 * @param seed
	 * a seed for the random generator to generate the buckets
	 * @param bc 
	 * the number of buckets to generate
	 * @param project
	 * the project
	 * @param outputDir
	 * the main plot output directory
	 * @param localizers
	 * the localizers to include for the test set
	 * @param threadCount 
	 * the number of parallel threads
	 */
	public HyperbolicBucketsEH(boolean onlyAddLocalizerValues, String suffix, Long seed, 
			int bc, String project, String outputDir, String[] localizers,
			int threadCount) {
		super();
		this.onlyAddLocalizerValues = onlyAddLocalizerValues;
		this.suffix = suffix;
		this.threadCount = threadCount;
		
		boolean isProject = Defects4J.validateProject(project, false);
		
		if (!isProject && !project.equals("super")) {
			Log.abort(this, "Project doesn't exist: '" + project + "'.");
		}
		
		this.cvOutputDir = generateOutputDir(outputDir, this.suffix, project, seed, bc);
		
		Path outputCsvFile = Paths.get(cvOutputDir).resolve(seed + ".csv").toAbsolutePath();
		
		this.localizers = Spectra2Ranking.getLocalizers(localizers, "hyperbolic");
		
		if (outputCsvFile.toFile().exists()) {
			this.buckets = Defects4J.readBucketsFromFile(outputCsvFile);
		} else {
			//only synchronize when absolutely necessary
			synchronized (lock) {
				if (outputCsvFile.toFile().exists()) {
					this.buckets = Defects4J.readBucketsFromFile(outputCsvFile);
				} else {
					this.buckets = Defects4J.generateNBuckets(fillEntities(project, isProject), bc, seed, outputCsvFile);
				}
			}
		}
	}

	@Override
	public void consumeItem(StatisticsCollector<HyperbolicEvoCrossValidation.StatisticsData> statContainer) {
		String addSuffix = onlyAddLocalizerValues ? "_add" : "";
		int i = 0;
		for (List<BuggyFixedEntity<?>> bucket : buckets) {
			++i;
			List<IFaultLocalizer<SourceCodeBlock>> allLocalizers = new ArrayList<>(localizers.size() + 1);
			allLocalizers.addAll(localizers);
			
			if (!onlyAddLocalizerValues) {
				// 1. use evo algorithm to get hyperbolic function coefficients
				EvoItem<Double[], Double, ChangeId> result = new HyperbolicEvoProcessor(threadCount, 
						cvOutputDir + SEP + "bucket_" + i, suffix, null)
						.submit(bucket)
						.getResult();

				String res1 = "training set " + i + ", hyperbolic: fitness = " + result.getFitness();
				Log.out(this, res1);
				statContainer.addStatisticsElement(StatisticsData.RESULT_MSG, res1);
				String res2 = "training set " + i + ", hyperbolic: K1=" + result.getItem()[0] 
						+ ", K2=" + result.getItem()[1] + ", K3=" + result.getItem()[2];
				Log.out(this, res2);
				statContainer.addStatisticsElement(StatisticsData.RESULT_MSG, res2);

				try {
					FileUtils.writeStrings2File(Paths.get(cvOutputDir, "current_stats.txt").toFile(), statContainer.printStatistics());
				} catch (IOException e) {
					Log.err(HyperbolicEvoCrossValidation.class, "Can not write statistics to '%s'.", Paths.get(cvOutputDir, "current_stats.txt"));
				}


				// use coefficients to generate hyperbolic function localizer
				Hyperbolic<SourceCodeBlock> hyperbolic = new Hyperbolic<>(
						result.getItem()[0], result.getItem()[1], result.getItem()[2]);
				allLocalizers.add(hyperbolic);
			}
			
			// 2. compute sbfl scores for the rest of the buckets and all localizers...
			List<BuggyFixedEntity<?>> testSet = sumUpAllBucketsButOne(buckets, i-1);
			String testSetOutputDir = cvOutputDir + SEP + "bucket_" + i + "_rest";
			
			// compute the rankings and mean rankings, etc. for the test set
			new PipeLinker().append(
					new CollectionSequencer<>(),
					new HyperbolicComputeSBFLRankingsEH(allLocalizers, 
							ToolSpecific.TRACE_COBERTURA, testSetOutputDir, suffix, null))
			.submitAndShutdown(testSet);
			
			List<String[]> listOfLines = new ArrayList<>(allLocalizers.size()+1);
			
			listOfLines.add(new String[] {
					"localizer",
					"meanAvg",
					"meanWorst",
					"meanBest",
					"medianAvg",
					"medianWorst",
					"medianBest",
					"bestHit@10",
					"worstHit@10",
					"avgHit@10",
					"bestHit@100",
					"worstHit@100",
					"avgHit@100",
					"bestHit@1000",
					"worstHit@1000",
					"avgHit@1000",
					"meanAvgBugs",
					"meanWorstBugs",
					"meanBestBugs",
					"medianAvgBugs",
					"medianWorstBugs",
					"medianBestBugs",
					"bestHit@10Bugs",
					"worstHit@10Bugs",
					"avgHit@10Bugs",
					"bestHit@100Bugs",
					"worstHit@100Bugs",
					"avgHit@100Bugs",
					"bestHit@1000Bugs",
					"worstHit@1000Bugs",
					"avgHit@1000Bugs"
					});
			
			for (IFaultLocalizer<SourceCodeBlock> localizer : allLocalizers) {
				ItemCollector<ResultCollection> collector = new ItemCollector<>();
				new PipeLinker().append(
						new CollectionSequencer<>(),
						new ComputeSBFLRankingsProcessor(Paths.get(testSetOutputDir), 
								suffix, localizer.getName().toLowerCase(Locale.getDefault())),
						collector)
				.submitAndShutdown(testSet);

				List<ResultCollection> collectedItems = collector.getCollectedItems();

				if (collectedItems.size() != 1) {
					Log.err(this, "Ranking computation for '%s' was not successful -> %s.", 
							testSetOutputDir, localizer.getName());
					statContainer.addStatisticsElement(StatisticsData.RESULT_MSG, 
							"test set " + i + ", " + localizer.getName() + ": not successful!");
					continue;
				}

				String res3 = "test set " + i + ", " + localizer.getName() 
				+ ": meanAvg = " + collectedItems.get(0).getMeanAvgRanking()
				+ ", meanWorst = " + collectedItems.get(0).getMeanWorstRanking()
				+ ", meanBest = " + collectedItems.get(0).getMeanBestRanking();
				Log.out(this, res3);
				statContainer.addStatisticsElement(StatisticsData.RESULT_MSG, res3);

				String res4 = "test set " + i + ", " + localizer.getName() 
				+ ": medianAvg = " + collectedItems.get(0).getMedianAvgRanking()
				+ ", medianWorst = " + collectedItems.get(0).getMedianWorstRanking()
				+ ", medianBest = " + collectedItems.get(0).getMedianBestRanking();
				Log.out(this, res4);
				statContainer.addStatisticsElement(StatisticsData.RESULT_MSG, res4);
				
//				String res5 = "test set " + i + ", " + localizer.getName() 
//				+ ": bestHit@10 = " + collectedItems.get(0).getBestHitAt10()
//				+ ", worstHit@10 = " + collectedItems.get(0).getWorstHitAt10()
//				+ ", avgHit@10 = " + collectedItems.get(0).getAverageHitAt10();
//				Log.out(this, res5);
//				statContainer.addStatisticsElement(StatisticsData.RESULT_MSG, res5);
//				
//				String res6 = "test set " + i + ", " + localizer.getName() 
//				+ ": bestHit@100 = " + collectedItems.get(0).getBestHitAt100()
//				+ ", worstHit@100 = " + collectedItems.get(0).getWorstHitAt100()
//				+ ", avgHit@100 = " + collectedItems.get(0).getAverageHitAt100();
//				Log.out(this, res6);
//				statContainer.addStatisticsElement(StatisticsData.RESULT_MSG, res6);
//				
//				String res7 = "test set " + i + ", " + localizer.getName() 
//				+ ": bestHit@1000 = " + collectedItems.get(0).getBestHitAt1000()
//				+ ", worstHit@1000 = " + collectedItems.get(0).getWorstHitAt1000()
//				+ ", avgHit@1000 = " + collectedItems.get(0).getAverageHitAt1000();
//				Log.out(this, res7);
//				statContainer.addStatisticsElement(StatisticsData.RESULT_MSG, res7);
				
				String res8 = "test set " + i + ", " + localizer.getName() 
				+ ": meanAvgBugs = " + collectedItems.get(0).getMeanAvgRankingBugs()
				+ ", meanWorstBugs = " + collectedItems.get(0).getMeanWorstRankingBugs()
				+ ", meanBestBugs = " + collectedItems.get(0).getMeanBestRankingBugs();
				Log.out(this, res8);
				statContainer.addStatisticsElement(StatisticsData.RESULT_MSG, res8);

				String res9 = "test set " + i + ", " + localizer.getName() 
				+ ": medianAvgBugs = " + collectedItems.get(0).getMedianAvgRankingBugs()
				+ ", medianWorstBugs = " + collectedItems.get(0).getMedianWorstRankingBugs()
				+ ", medianBestBugs = " + collectedItems.get(0).getMedianBestRankingBugs();
				Log.out(this, res9);
				statContainer.addStatisticsElement(StatisticsData.RESULT_MSG, res9);
				
				listOfLines.add(new String[] {
						localizer.getName().toLowerCase(Locale.getDefault()),
						Double.valueOf(collectedItems.get(0).getMeanAvgRanking()).toString(),
						Double.valueOf(collectedItems.get(0).getMeanWorstRanking()).toString(),
						Double.valueOf(collectedItems.get(0).getMeanBestRanking()).toString(),
						Double.valueOf(collectedItems.get(0).getMedianAvgRanking()).toString(),
						Double.valueOf(collectedItems.get(0).getMedianWorstRanking()).toString(),
						Double.valueOf(collectedItems.get(0).getMedianBestRanking()).toString(),
						Integer.valueOf(collectedItems.get(0).getBestHitAt10()).toString(),
						Integer.valueOf(collectedItems.get(0).getWorstHitAt10()).toString(),
						Integer.valueOf(collectedItems.get(0).getAverageHitAt10()).toString(),
						Integer.valueOf(collectedItems.get(0).getBestHitAt100()).toString(),
						Integer.valueOf(collectedItems.get(0).getWorstHitAt100()).toString(),
						Integer.valueOf(collectedItems.get(0).getAverageHitAt100()).toString(),
						Integer.valueOf(collectedItems.get(0).getBestHitAt1000()).toString(),
						Integer.valueOf(collectedItems.get(0).getWorstHitAt1000()).toString(),
						Integer.valueOf(collectedItems.get(0).getAverageHitAt1000()).toString(),
						Double.valueOf(collectedItems.get(0).getMeanAvgRankingBugs()).toString(),
						Double.valueOf(collectedItems.get(0).getMeanWorstRankingBugs()).toString(),
						Double.valueOf(collectedItems.get(0).getMeanBestRankingBugs()).toString(),
						Double.valueOf(collectedItems.get(0).getMedianAvgRankingBugs()).toString(),
						Double.valueOf(collectedItems.get(0).getMedianWorstRankingBugs()).toString(),
						Double.valueOf(collectedItems.get(0).getMedianBestRankingBugs()).toString(),
						Integer.valueOf(collectedItems.get(0).getBestHitAt10Bugs()).toString(),
						Integer.valueOf(collectedItems.get(0).getWorstHitAt10Bugs()).toString(),
						Integer.valueOf(collectedItems.get(0).getAverageHitAt10Bugs()).toString(),
						Integer.valueOf(collectedItems.get(0).getBestHitAt100Bugs()).toString(),
						Integer.valueOf(collectedItems.get(0).getWorstHitAt100Bugs()).toString(),
						Integer.valueOf(collectedItems.get(0).getAverageHitAt100Bugs()).toString(),
						Integer.valueOf(collectedItems.get(0).getBestHitAt1000Bugs()).toString(),
						Integer.valueOf(collectedItems.get(0).getWorstHitAt1000Bugs()).toString(),
						Integer.valueOf(collectedItems.get(0).getAverageHitAt1000Bugs()).toString()
						});
				
			}
			
			try {
				FileUtils.writeStrings2File(Paths.get(cvOutputDir, "current_stats" + addSuffix + ".txt").toFile(), statContainer.printStatistics());
			} catch (IOException e) {
				Log.err(HyperbolicEvoCrossValidation.class, "Can not write statistics to '%s'.", Paths.get(cvOutputDir, "current_stats" + addSuffix + ".txt"));
			}
			
			CSVUtils.toCsvFile(listOfLines, Paths.get(cvOutputDir, "bucket" + i + addSuffix + ".csv"));

			// delete computed rankings on the hard drive to free space
			FileUtils.delete(Paths.get(testSetOutputDir));
			
		}
	}
	
	private static List<BuggyFixedEntity<?>> sumUpAllBucketsButOne(List<BuggyFixedEntity<?>>[] buckets, int index) {
		List<BuggyFixedEntity<?>> list = new ArrayList<>();
		
		for (int i = 0; i < buckets.length; ++i) {
			if (i != index) {
				list.addAll(buckets[i]);
			}
		}
		
		return list;
	}

	private static BuggyFixedEntity<?>[] fillEntities(String identifier, boolean isProject) {
		BuggyFixedEntity<?>[] entities;
		if (isProject) {
			/* #====================================================================================
			 * # plot averaged rankings for given project
			 * #==================================================================================== */
			//iterate over all ids
			String[] ids = Defects4J.getAllBugIDs(identifier);
			entities = new BuggyFixedEntity[ids.length];
			int i = 0;
			for (String id : ids) {
				entities[i++] = new Defects4JBuggyFixedEntity(identifier, id);
			}
			
		} else { //given project name was "super"; iterate over all project directories
			int numberOfEntities = 0;
			//iterate over all projects
			for (String project : Defects4J.getAllProjects()) {
//				if (project.equals("Mockito")) {
//					continue;
//				}
				numberOfEntities += Defects4J.getMaxBugID(project); 
			}
			
			entities = new BuggyFixedEntity[numberOfEntities];
			int i = 0;
			//iterate over all projects
			for (String project : Defects4J.getAllProjects()) {
//				if (project.equals("Mockito")) {
//					continue;
//				}
				String[] ids = Defects4J.getAllBugIDs(project); 
				for (String id : ids) {
					entities[i++] = new Defects4JBuggyFixedEntity(project, id);
				}
			}
			
		}
		return entities;
	}
	
	public static String generateOutputDir(String outputDir, String suffix, String identifier, 
			Long seed, int bc) {

		return outputDir + SEP + "sbfl_cv" + (suffix == null ? "" : "_" + suffix)
					+ SEP + identifier + SEP + seed + SEP + bc + "_buckets_total";
	}
	

	public static class ChangeId implements Comparable<ChangeId> {

		private double change;
		private int location;

		public ChangeId(double change, int location) {
			this.setChange(change);
			this.setLocation(location);
		}

		@Override
		public int compareTo(ChangeId o) {
			//first, order by locations
			if (this.getLocation() == o.getLocation()) {
				//second, order by change amounts
				return Double.compare(this.getChange(), o.getChange());
			} else if (this.getLocation() < o.getLocation()) {
				return -1;
			} else {
				return 1;
			}
		}

		public double getChange() {
			return change;
		}

		public void setChange(double change) {
			this.change = change;
		}

		public int getLocation() {
			return location;
		}

		public void setLocation(int location) {
			this.location = location;
		}

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ChangeId)) return false;
            return compareTo((ChangeId) o) == 0;
        }
    }

}

