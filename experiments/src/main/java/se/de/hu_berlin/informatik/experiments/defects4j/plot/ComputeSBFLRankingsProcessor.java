package se.de.hu_berlin.informatik.experiments.defects4j.plot;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.changechecker.ChangeCheckerUtils;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.rankingplotter.plotter.RankingUtils;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.utils.experiments.ranking.MarkedRanking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.RankingMetric;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.MathUtils;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.ProcessorSocket;

public class ComputeSBFLRankingsProcessor extends AbstractProcessor<BuggyFixedEntity<?>, ComputeSBFLRankingsProcessor.ResultCollection> {

	final private String rankingIdentifier;
	private String suffix;
	private Path mainBugDir;
	
	private List<Integer> worstRankings = new ArrayList<>();
	private List<Double> averageRankings = new ArrayList<>();
	private List<Integer> bestRankings = new ArrayList<>();
	
	private List<Integer> worstRankingsBugs = new ArrayList<>();
	private List<Double> averageRankingsBugs = new ArrayList<>();
	private List<Integer> bestRankingsBugs = new ArrayList<>();

	public ComputeSBFLRankingsProcessor(Path mainBugDir, String suffix, String rankingIdentifier) {
		this.mainBugDir = mainBugDir;
		this.suffix = suffix;
		this.rankingIdentifier = rankingIdentifier;
	}

	@Override
	public ResultCollection processItem(BuggyFixedEntity<?> entity, 
			ProcessorSocket<BuggyFixedEntity<?>, ResultCollection> socket) {
//		Log.out(this, "Processing %s.", entity);
		Entity bug = entity.getBuggyVersion();

		Map<String, List<ChangeWrapper>> changeInformation = entity.loadChangesFromFile();

		Ranking<SourceCodeBlock> ranking = RankingUtils.getRanking(
				mainBugDir.resolve(bug.getUniqueIdentifier()), bug, suffix, rankingIdentifier);
		if (ranking == null) {
			Log.abort(this, "Found no ranking with identifier '%s'.", rankingIdentifier);
		}

		MarkedRanking<SourceCodeBlock, List<ChangeWrapper>> markedRanking = new MarkedRanking<>(ranking);

		for (SourceCodeBlock block : markedRanking.getElements()) {
			List<ChangeWrapper> list = ChangeCheckerUtils.getModifications(
					block.getFilePath(), block.getStartLineNumber(), block.getEndLineNumber(), true,
					changeInformation);
			// found changes for this line? then mark the line with the
			// change(s)...
			if (list != null) {
				markedRanking.markElementWith(block, list);
			}
		}

		boolean first = true;
		for (SourceCodeBlock changedElement : markedRanking.getMarkedElements()) {
			RankingMetric<SourceCodeBlock> metric = ranking.getRankingMetrics(changedElement);

			// List<ChangeWrapper> changes =
			// markedRanking.getMarker(changedElement);

			bestRankings.add(metric.getBestRanking());
			worstRankings.add(metric.getWorstRanking());
			averageRankings.add((metric.getBestRanking() + metric.getWorstRanking()) / 2.0);
			
			if (first) {
				bestRankingsBugs.add(metric.getBestRanking());
				worstRankingsBugs.add(metric.getWorstRanking());
				averageRankingsBugs.add((metric.getBestRanking() + metric.getWorstRanking()) / 2.0);
				first = false;
			}
			
		}

		return null;
	}

	@Override
	public ResultCollection getResultFromCollectedItems() {
		double meanWorst = MathUtils.getMean(worstRankings);
		double meanBest = MathUtils.getMean(bestRankings);
		double meanAverage = MathUtils.getMean(averageRankings);
		
		double medianWorst = MathUtils.getMedian(worstRankings);
		double medianBest = MathUtils.getMedian(bestRankings);
		double medianAverage = MathUtils.getMedian(averageRankings);
		
		int bestHitAt10 = 0;
		int bestHitAt100 = 0;
		int bestHitAt1000 = 0;
		for (int rank : bestRankings) {
			if (rank <= 1000) {
				++bestHitAt1000;
				if (rank <= 100) {
					++bestHitAt100;
					if (rank <= 10) {
						++bestHitAt10;
					}
				}
			}
		}
		
		int worstHitAt10 = 0;
		int worstHitAt100 = 0;
		int worstHitAt1000 = 0;
		for (int rank : worstRankings) {
			if (rank <= 1000) {
				++worstHitAt1000;
				if (rank <= 100) {
					++worstHitAt100;
					if (rank <= 10) {
						++worstHitAt10;
					}
				}
			}
		}
		
		int averageHitAt10 = 0;
		int averageHitAt100 = 0;
		int averageHitAt1000 = 0;
		for (double rank : averageRankings) {
			if (rank <= 1000.0) {
				++averageHitAt1000;
				if (rank <= 100.0) {
					++averageHitAt100;
					if (rank <= 10.0) {
						++averageHitAt10;
					}
				}
			}
		}
		
		double meanWorstBugs = MathUtils.getMean(worstRankingsBugs);
		double meanBestBugs = MathUtils.getMean(bestRankingsBugs);
		double meanAverageBugs = MathUtils.getMean(averageRankingsBugs);
		
		double medianWorstBugs = MathUtils.getMedian(worstRankingsBugs);
		double medianBestBugs = MathUtils.getMedian(bestRankingsBugs);
		double medianAverageBugs = MathUtils.getMedian(averageRankingsBugs);
		
		int bestHitAt10Bugs = 0;
		int bestHitAt100Bugs = 0;
		int bestHitAt1000Bugs = 0;
		for (int rank : bestRankingsBugs) {
			if (rank <= 1000) {
				++bestHitAt1000Bugs;
				if (rank <= 100) {
					++bestHitAt100Bugs;
					if (rank <= 10) {
						++bestHitAt10Bugs;
					}
				}
			}
		}
		
		int worstHitAt10Bugs = 0;
		int worstHitAt100Bugs = 0;
		int worstHitAt1000Bugs = 0;
		for (int rank : worstRankingsBugs) {
			if (rank <= 1000) {
				++worstHitAt1000Bugs;
				if (rank <= 100) {
					++worstHitAt100Bugs;
					if (rank <= 10) {
						++worstHitAt10Bugs;
					}
				}
			}
		}
		
		int averageHitAt10Bugs = 0;
		int averageHitAt100Bugs = 0;
		int averageHitAt1000Bugs = 0;
		for (double rank : averageRankingsBugs) {
			if (rank <= 1000.0) {
				++averageHitAt1000Bugs;
				if (rank <= 100.0) {
					++averageHitAt100Bugs;
					if (rank <= 10.0) {
						++averageHitAt10Bugs;
					}
				}
			}
		}
		
		return new ResultCollection(meanAverage, meanWorst, meanBest, 
				medianAverage, medianWorst, medianBest,
				bestHitAt10, bestHitAt100, bestHitAt1000,
				worstHitAt10, worstHitAt100, worstHitAt1000,
				averageHitAt10, averageHitAt100, averageHitAt1000,
				meanAverageBugs, meanWorstBugs, meanBestBugs, 
				medianAverageBugs, medianWorstBugs, medianBestBugs,
				bestHitAt10Bugs, bestHitAt100Bugs, bestHitAt1000Bugs,
				worstHitAt10Bugs, worstHitAt100Bugs, worstHitAt1000Bugs,
				averageHitAt10Bugs, averageHitAt100Bugs, averageHitAt1000Bugs);
	}
	
	
	public static class ResultCollection {

		private double meanAvgRanking;
		private double meanWorstRanking;
		private double meanBestRanking;
		private double medianAvgRanking;
		private double medianWorstRanking;
		private double medianBestRanking;
		private int bestHitAt10;
		private int bestHitAt100;
		private int bestHitAt1000;
		private int worstHitAt10;
		private int worstHitAt100;
		private int worstHitAt1000;
		private int averageHitAt10;
		private int averageHitAt100;
		private int averageHitAt1000;
		private double meanAvgRankingBugs;
		private double meanWorstRankingBugs;
		private double meanBestRankingBugs;
		private double medianAvgRankingBugs;
		private double medianWorstRankingBugs;
		private double medianBestRankingBugs;
		private int bestHitAt10Bugs;
		private int bestHitAt100Bugs;
		private int bestHitAt1000Bugs;
		private int worstHitAt10Bugs;
		private int worstHitAt100Bugs;
		private int worstHitAt1000Bugs;
		private int averageHitAt10Bugs;
		private int averageHitAt100Bugs;
		private int averageHitAt1000Bugs;

		public ResultCollection(double meanAvgRanking, 
				double meanWorstRanking, double meanBestRanking,
				double medianAvgRanking, 
				double medianWorstRanking, double medianBestRanking, 
				int bestHitAt10, int bestHitAt100, int bestHitAt1000, 
				int worstHitAt10, int worstHitAt100, int worstHitAt1000, 
				int averageHitAt10, int averageHitAt100, int averageHitAt1000,
				double meanAvgRankingBugs, 
				double meanWorstRankingBugs, double meanBestRankingBugs,
				double medianAvgRankingBugs, 
				double medianWorstRankingBugs, double medianBestRankingBugs, 
				int bestHitAt10Bugs, int bestHitAt100Bugs, int bestHitAt1000Bugs, 
				int worstHitAt10Bugs, int worstHitAt100Bugs, int worstHitAt1000Bugs, 
				int averageHitAt10Bugs, int averageHitAt100Bugs, int averageHitAt1000Bugs) {
					this.meanAvgRanking = meanAvgRanking;
					this.meanWorstRanking = meanWorstRanking;
					this.meanBestRanking = meanBestRanking;
					this.medianAvgRanking = medianAvgRanking;
					this.medianWorstRanking = medianWorstRanking;
					this.medianBestRanking = medianBestRanking;
					this.bestHitAt10 = bestHitAt10;
					this.bestHitAt100 = bestHitAt100;
					this.bestHitAt1000 = bestHitAt1000;
					this.worstHitAt10 = worstHitAt10;
					this.worstHitAt100 = worstHitAt100;
					this.worstHitAt1000 = worstHitAt1000;
					this.averageHitAt10 = averageHitAt10;
					this.averageHitAt100 = averageHitAt100;
					this.averageHitAt1000 = averageHitAt1000;
					this.meanAvgRankingBugs = meanAvgRankingBugs;
					this.meanWorstRankingBugs = meanWorstRankingBugs;
					this.meanBestRankingBugs = meanBestRankingBugs;
					this.medianAvgRankingBugs = medianAvgRankingBugs;
					this.medianWorstRankingBugs = medianWorstRankingBugs;
					this.medianBestRankingBugs = medianBestRankingBugs;
					this.bestHitAt10Bugs = bestHitAt10Bugs;
					this.bestHitAt100Bugs = bestHitAt100Bugs;
					this.bestHitAt1000Bugs = bestHitAt1000Bugs;
					this.worstHitAt10Bugs = worstHitAt10Bugs;
					this.worstHitAt100Bugs = worstHitAt100Bugs;
					this.worstHitAt1000Bugs = worstHitAt1000Bugs;
					this.averageHitAt10Bugs = averageHitAt10Bugs;
					this.averageHitAt100Bugs = averageHitAt100Bugs;
					this.averageHitAt1000Bugs = averageHitAt1000Bugs;

		}

		
		public double getMeanAvgRanking() {
			return meanAvgRanking;
		}

		
		public double getMeanWorstRanking() {
			return meanWorstRanking;
		}

		
		public double getMeanBestRanking() {
			return meanBestRanking;
		}

		
		public double getMedianAvgRanking() {
			return medianAvgRanking;
		}

		
		public double getMedianWorstRanking() {
			return medianWorstRanking;
		}

		
		public double getMedianBestRanking() {
			return medianBestRanking;
		}

		
		public int getBestHitAt10() {
			return bestHitAt10;
		}

		
		public int getBestHitAt100() {
			return bestHitAt100;
		}

		
		public int getBestHitAt1000() {
			return bestHitAt1000;
		}

		
		public int getWorstHitAt10() {
			return worstHitAt10;
		}

		
		public int getWorstHitAt100() {
			return worstHitAt100;
		}

		
		public int getWorstHitAt1000() {
			return worstHitAt1000;
		}

		
		public int getAverageHitAt10() {
			return averageHitAt10;
		}

		
		public int getAverageHitAt100() {
			return averageHitAt100;
		}

		
		public int getAverageHitAt1000() {
			return averageHitAt1000;
		}

		
		public double getMeanAvgRankingBugs() {
			return meanAvgRankingBugs;
		}

		
		public double getMeanWorstRankingBugs() {
			return meanWorstRankingBugs;
		}

		
		public double getMeanBestRankingBugs() {
			return meanBestRankingBugs;
		}

		
		public double getMedianAvgRankingBugs() {
			return medianAvgRankingBugs;
		}

		
		public double getMedianWorstRankingBugs() {
			return medianWorstRankingBugs;
		}

		
		public double getMedianBestRankingBugs() {
			return medianBestRankingBugs;
		}

		
		public int getBestHitAt10Bugs() {
			return bestHitAt10Bugs;
		}

		
		public int getBestHitAt100Bugs() {
			return bestHitAt100Bugs;
		}

		
		public int getBestHitAt1000Bugs() {
			return bestHitAt1000Bugs;
		}

		
		public int getWorstHitAt10Bugs() {
			return worstHitAt10Bugs;
		}

		
		public int getWorstHitAt100Bugs() {
			return worstHitAt100Bugs;
		}

		
		public int getWorstHitAt1000Bugs() {
			return worstHitAt1000Bugs;
		}

		
		public int getAverageHitAt10Bugs() {
			return averageHitAt10Bugs;
		}

		
		public int getAverageHitAt100Bugs() {
			return averageHitAt100Bugs;
		}

		
		public int getAverageHitAt1000Bugs() {
			return averageHitAt1000Bugs;
		}
		
	}

	
}
