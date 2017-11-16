package se.de.hu_berlin.informatik.experiments.defects4j.plot;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.changechecker.ChangeCheckerUtils;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.experiments.defects4j.GenerateCsvBugDataFiles;
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
	
	List<Integer> worstRankings = new ArrayList<>();
	List<Double> averageRankings = new ArrayList<>();
	List<Integer> bestRankings = new ArrayList<>();

	public ComputeSBFLRankingsProcessor(Path mainBugDir, String suffix, String rankingIdentifier) {
		this.mainBugDir = mainBugDir;
		this.suffix = suffix;
		this.rankingIdentifier = rankingIdentifier;
	}

	@Override
	public ResultCollection processItem(BuggyFixedEntity<?> entity, 
			ProcessorSocket<BuggyFixedEntity<?>, ResultCollection> socket) {
		Log.out(GenerateCsvBugDataFiles.class, "Processing %s.", entity);
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

		// BugID, Line, EF, EP, NF, NP, BestRanking, WorstRanking,
		// MinWastedEffort, MaxWastedEffort, Suspiciousness,
		// MinFiles, MaxFiles, MinMethods, MaxMethods

		for (SourceCodeBlock changedElement : markedRanking.getMarkedElements()) {
			RankingMetric<SourceCodeBlock> metric = ranking.getRankingMetrics(changedElement);

			// List<ChangeWrapper> changes =
			// markedRanking.getMarker(changedElement);

			bestRankings.add(metric.getBestRanking());
			worstRankings.add(metric.getWorstRanking());
			averageRankings.add((metric.getBestRanking() + metric.getWorstRanking()) / 2.0);
			
		}

		return null;
	}

	@Override
	public ResultCollection getResultFromCollectedItems() {
		double meanWorst = MathUtils.getMean(worstRankings);
		double meanBest = MathUtils.getMean(bestRankings);
		double meanAverage = MathUtils.getMean(averageRankings);
		
		return new ResultCollection(meanAverage, meanWorst, meanBest);
	}
	
	
	public static class ResultCollection {

		private double meanAvgRanking;
		private double meanWorstRanking;
		private double meanBestRanking;

		public ResultCollection(double meanAvgRanking, 
				double meanWorstRanking, double meanBestRanking) {
					this.setMeanAvgRanking(meanAvgRanking);
					this.setMeanWorstRanking(meanWorstRanking);
					this.setMeanBestRanking(meanBestRanking);
		}

		public double getMeanAvgRanking() {
			return meanAvgRanking;
		}

		public void setMeanAvgRanking(double meanAvgRanking) {
			this.meanAvgRanking = meanAvgRanking;
		}

		public double getMeanWorstRanking() {
			return meanWorstRanking;
		}

		public void setMeanWorstRanking(double meanWorstRanking) {
			this.meanWorstRanking = meanWorstRanking;
		}

		public double getMeanBestRanking() {
			return meanBestRanking;
		}

		public void setMeanBestRanking(double meanBestRanking) {
			this.meanBestRanking = meanBestRanking;
		}
		
	}

	
}
