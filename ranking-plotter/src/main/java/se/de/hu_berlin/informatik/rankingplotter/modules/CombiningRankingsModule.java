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
import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.benchmark.ranking.Ranking;
import se.de.hu_berlin.informatik.benchmark.ranking.Ranking.RankingStrategy;
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
	
	private final ParserStrategy strategy;
	private final String[] sbflPercentages;

	private final String localizer;
	private final boolean normalized;
	
	/**
	 * Creates a new {@link CombiningRankingsModule} object.
	 * @param localizer
	 * the localizer
	 * @param strategy
	 * which strategy to use. May take the lowest or the highest ranking 
	 * of a range of equal-value rankings or may compute the average
	 * @param sbflPercentages
	 * an array of percentage values that determine the weighting 
	 * of the SBFL ranking to the NLFL ranking
	 * @param normalized
	 * whether the rankings should be normalized before combining
	 */
	public CombiningRankingsModule(String localizer, ParserStrategy strategy, 
			String[] sbflPercentages, boolean normalized) {
		super(true);
		this.localizer = localizer;
		this.strategy = strategy;
		this.sbflPercentages = sbflPercentages;
		this.normalized = normalized;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public List<RankingFileWrapper> processItem(BuggyFixedEntity entity) {
		Entity bug = entity.getBuggyVersion();
		
		Path sbflRankingFile = bug.getWorkDataDir().resolve(BugLoRDConstants.DIR_NAME_RANKING).resolve(localizer).resolve(BugLoRDConstants.FILENAME_RANKING_FILE);
		Ranking<String> sbflRanking = Ranking.load(sbflRankingFile, false, RankingStrategy.WORST, 
				RankingStrategy.BEST, RankingStrategy.WORST);
		
		Path lmRankingFile = bug.getWorkDataDir().resolve(BugLoRDConstants.DIR_NAME_RANKING).resolve(BugLoRDConstants.FILENAME_LM_RANKING);
		Ranking<String>lmRanking = Ranking.load(lmRankingFile, false, RankingStrategy.ZERO,
				RankingStrategy.BEST, RankingStrategy.WORST);

		
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
		String project = bug.getWorkDataDir().getParent().getParent().getFileName().toString();
		String bugDirName = bug.getWorkDataDir().getParent().getFileName().toString();
		int bugId = Integer.valueOf(bugDirName);
		for (double sbflPercentage : sBFLpercentages) {
				files.add(CombiningRankingsEH.getRankingWrapper(
						sbflRanking, lmRanking, changeInformation,
						project, bugId, sbflPercentage, strategy, normalized));
		}
		
		//sort the ranking wrappers
		files.sort(null);
		
		return files;
	}
	
}
