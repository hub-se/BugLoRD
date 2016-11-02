/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.modules;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.rankingplotter.plotter.CombiningRankingsEH;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JConstants;
import se.de.hu_berlin.informatik.benchmark.ranking.Ranking;
import se.de.hu_berlin.informatik.benchmark.ranking.RankingNaNStrategy;
import se.de.hu_berlin.informatik.benchmark.ranking.RankingPosInfStrategy;
import se.de.hu_berlin.informatik.benchmark.ranking.RankingNegInfStrategy;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.rankingplotter.plotter.RankingFileWrapper;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * Parses the SBFL ranking file
 * and each combined ranking file from a submitted directory,
 * producing a {@link List} of {@link RankingFileWrapper} objects.
 * The SBFL ranking file has to be located in the parent directory of
 * the submitted directory.
 * 
 * @author Simon Heiden
 */
public class CombiningRankingsModule extends AbstractModule<BuggyFixedEntity, List<RankingFileWrapper>> {
	
	private ParserStrategy strategy;
	private String[] sbflPercentages;

	private String localizer;
	
	/**
	 * Creates a new {@link CombiningRankingsModule} object.
	 * @param strategy
	 * which strategy to use. May take the lowest or the highest ranking 
	 * of a range of equal-value rankings or may compute the average
	 * @param sbflPercentages
	 * an array of percentage values that determine the weighting 
	 * of the SBFL ranking to the NLFL ranking
	 */
	public CombiningRankingsModule(String localizer, ParserStrategy strategy, 
			String[] sbflPercentages) {
		super(true);
		this.localizer = localizer;
		this.strategy = strategy;
		this.sbflPercentages = sbflPercentages;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public List<RankingFileWrapper> processItem(BuggyFixedEntity entity) {
		
		Path sbflRankingFile = entity.getWorkDataDir().resolve(Defects4JConstants.DIR_NAME_RANKING).resolve(localizer).resolve(Defects4JConstants.FILENAME_RANKING_FILE);
		Ranking<String> sbflRanking = Ranking.load(sbflRankingFile, false, RankingNaNStrategy.Strategy.WORST, 
				RankingPosInfStrategy.Strategy.BEST, RankingNegInfStrategy.Strategy.WORST);
		
		Path lmRankingFile = entity.getWorkDataDir().resolve(Defects4JConstants.DIR_NAME_RANKING).resolve(Defects4JConstants.FILENAME_LM_RANKING);
		Ranking<String>lmRanking = Ranking.load(lmRankingFile, false, RankingNaNStrategy.Strategy.ZERO,
				RankingPosInfStrategy.Strategy.BEST, RankingNegInfStrategy.Strategy.WORST);

		
		Map<String, List<ChangeWrapper>> changeInformation = entity.loadChangesFromFile(); 
		
		
		//a list of files with parsed SBFL and NLFL percentages (for sorting later on)
		final List<RankingFileWrapper> files = new ArrayList<>();
		
		double[] sBFLpercentages = {0.0, 10.0, 20.0, 50.0, 75.0, 90.0, 100.0};
		if (sbflPercentages != null) {
			sBFLpercentages = new double[sbflPercentages.length];
			for (int i = 0; i < sbflPercentages.length; ++i) {
				sBFLpercentages[i] = Double.parseDouble(sbflPercentages[i]);
			}
		}
		
		//TODO: change that for other benchmarks...
		String project = entity.getWorkDataDir().getParent().getParent().getFileName().toString();
		String bugDirName = entity.getWorkDataDir().getParent().getFileName().toString();
		int bugId = Integer.valueOf(bugDirName);
		for (double sbflPercentage : sBFLpercentages) {
				files.add(CombiningRankingsEH.getRankingWrapper(
						sbflRanking, lmRanking, changeInformation,
						project, bugId, sbflPercentage, strategy));
		}
		
		//sort the ranking wrappers
		files.sort(null);
		
		return files;
	}
	
}
