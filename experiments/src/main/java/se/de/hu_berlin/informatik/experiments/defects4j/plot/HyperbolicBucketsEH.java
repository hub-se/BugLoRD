/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.plot;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JBuggyFixedEntity;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.ToolSpecific;
import se.de.hu_berlin.informatik.experiments.defects4j.plot.ComputeSBFLRankingsProcessor.ResultCollection;
import se.de.hu_berlin.informatik.experiments.defects4j.plot.HyperbolicEvoCrossValidation.StatisticsData;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.localizers.Hyperbolic;
import se.de.hu_berlin.informatik.utils.experiments.evo.EvoItem;
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

	private String outputDir;
	private final int threadCount;

	private String cvOutputDir;
	private List<BuggyFixedEntity<?>>[] buckets;

	private String suffix;

	private final static Object lock = new Object();

	/**
	 * Initializes a {@link HyperbolicBucketsEH} object with the given parameters.
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
	 * @param threadCount 
	 * the number of parallel threads
	 */
	public HyperbolicBucketsEH(String suffix, Long seed, 
			int bc, String project, String outputDir, 
			int threadCount) {
		super();
		this.suffix = suffix;
		this.outputDir = outputDir;
		this.threadCount = threadCount;
		
		boolean isProject = Defects4J.validateProject(project, false);
		
		if (!isProject && !project.equals("super")) {
			Log.abort(this, "Project doesn't exist: '" + project + "'.");
		}
		
		if (this.outputDir == null) {
			this.outputDir = Defects4J.getValueOf(Defects4JProperties.PLOT_DIR);
		}
		
		this.cvOutputDir = generateOutputDir(this.outputDir, this.suffix, project, seed, bc);
		
		Path outputCsvFile = Paths.get(cvOutputDir).resolve(String.valueOf(seed) + ".csv").toAbsolutePath();
		
		
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
		int i = 0;
		for (List<BuggyFixedEntity<?>> bucket : buckets) {
			++i;
			// 1. use evo algorithm to get hyperbolic function coefficients
			EvoItem<Double[], Double, ChangeId> result = new HyperbolicEvoProcessor(threadCount, 
					cvOutputDir + SEP + "bucket_" + String.valueOf(i), suffix, null)
					.submit(bucket)
					.getResult();
			
			String res1 = "training set " + i + ": fitness = " + result.getFitness();
			Log.out(this, res1);
			statContainer.addStatisticsElement(StatisticsData.RESULT_MSG, res1);
			String res2 = "training set " + i + ": K1=" + result.getItem()[0] 
					+ ", K2=" + result.getItem()[1] + ", K3=" + result.getItem()[2];
			Log.out(this, res2);
			statContainer.addStatisticsElement(StatisticsData.RESULT_MSG, res2);
			
			// use coefficients to generate hyperbolic function localizer
			Hyperbolic<String> hyperbolic = new Hyperbolic<>(
							result.getItem()[0], result.getItem()[1], result.getItem()[2]);

//			Plotter.plotAverage(bucket, suffix, localizer, lmRankingFileName, strategy, 
//					cvOutputDir + SEP + "bucket_" + String.valueOf(i), 
//					project, gp, threadCount, normStrategy);
			
			// 2. compute sbfl scores for the rest of the buckets...
			
			String testSetOutputDir = cvOutputDir + SEP + "bucket_" + String.valueOf(i) + "_rest";
			ItemCollector<ResultCollection> collector = new ItemCollector<ResultCollection>();
			// compute the rankings and mean rankings, etc. for the test set
			new PipeLinker().append(
					new CollectionSequencer<>(),
					new HyperbolicComputeSBFLRankingsEH(Collections.singletonList(hyperbolic), 
							ToolSpecific.MERGED, testSetOutputDir, suffix, null),
					new ComputeSBFLRankingsProcessor(Paths.get(testSetOutputDir), 
							suffix, "hyperbolic"),
					collector)
			.submitAndShutdown(sumUpAllBucketsButOne(buckets, i-1));

			List<ResultCollection> collectedItems = collector.getCollectedItems();
			
			if (collectedItems.size() != 1) {
				Log.abort(this, "Ranking computation for '%s' was not successful -> %d.", 
						testSetOutputDir, collectedItems.size());
			}
			
			String res3 = "test set " + i + ": fitness = " + collectedItems.get(0).getMeanAvgRanking();
			Log.out(this, res3);
			statContainer.addStatisticsElement(StatisticsData.RESULT_MSG, res3);
			
//			Plotter.plotAverage(sumUpAllBucketsButOne(buckets, i-1), suffix, localizer, lmRankingFileName, strategy, 
//					cvOutputDir + SEP + "bucket_" + String.valueOf(i) + "_rest", 
//					project, gp, threadCount, normStrategy);
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
		String bucketOutputDir = outputDir + SEP + "sbfl_cv" + (suffix == null ? "" : "_" + suffix) 
					+ SEP + identifier + SEP + String.valueOf(seed) + SEP + Integer.valueOf(bc) + "_buckets_total";

		return bucketOutputDir;
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
				if (this.getChange() == o.getChange()) {
					return 0;
				} else if (this.getChange() < o.getChange()) {
					return -1;
				} else {
					return 1;
				}
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

	}

}

