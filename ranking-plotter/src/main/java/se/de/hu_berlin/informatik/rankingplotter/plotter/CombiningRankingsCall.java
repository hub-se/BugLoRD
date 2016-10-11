/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.nio.file.Path;
import java.util.List;
import se.de.hu_berlin.informatik.rankingplotter.modules.CombiningRankingsModule;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
import se.de.hu_berlin.informatik.utils.threaded.EHWithInputAndReturn;
import se.de.hu_berlin.informatik.utils.threaded.EHWithInputAndReturnFactory;

/**
 * {@link EHWithInputAndReturn} object that ...
 * 
 * @author Simon Heiden
 */
public class CombiningRankingsCall extends EHWithInputAndReturn<Path,List<RankingFileWrapper>> {

	final private ParserStrategy strategy;
	final private boolean zeroOption;
	final private String[] sbflPercentages;
	final private String[] nlflPercentages;
	
	/**
	 * Initializes a {@link CombiningRankingsCall} object with the given parameters.
	 * @param strategy
	 *  which strategy to use. May take the lowest or the highest ranking of a range of 
	 *  equal-value rankings or may compute the average
	 * @param zeroOption
	 * whether to ignore ranking values that are zero or below zero
	 * @param sbflPercentages
	 * an array of percentage values that determine the weighting 
	 * of the SBFL ranking to the NLFL ranking
	 * @param nlflPercentages
	 * an array of percentage values that determine the weighting 
	 * of the global NLFL ranking to the local NLFL ranking
	 */
	public CombiningRankingsCall(ParserStrategy strategy, boolean zeroOption,
			String[] sbflPercentages, String[] nlflPercentages) {
		super();
		this.strategy = strategy;
		this.zeroOption = zeroOption;
		
		this.sbflPercentages = sbflPercentages;
		this.nlflPercentages = nlflPercentages;
	}

	@Override
	public List<RankingFileWrapper> processInput(Path input) {
		return new CombiningRankingsModule(true, strategy, true, zeroOption, sbflPercentages, nlflPercentages)
				.submit(input)
				.getResult();
	}

	public static class Factory extends EHWithInputAndReturnFactory<Path,List<RankingFileWrapper>> {

		final private ParserStrategy strategy;
		final private boolean zeroOption;
		final private String[] sbflPercentages;
		final private String[] nlflPercentages;
		
		/**
		 * Initializes a {@link Factory} object with the given parameters.
		 * @param strategy
		 *  which strategy to use. May take the lowest or the highest ranking of a range of 
		 *  equal-value rankings or may compute the average
		 * @param zeroOption
		 * whether to ignore ranking values that are zero or below zero
		 * @param sbflPercentages
		 * an array of percentage values that determine the weighting 
		 * of the SBFL ranking to the NLFL ranking
		 * @param nlflPercentages
		 * an array of percentage values that determine the weighting 
		 * of the global NLFL ranking to the local NLFL ranking
		 */
		public Factory(ParserStrategy strategy, boolean zeroOption,
				String[] sbflPercentages, String[] nlflPercentages) {
			super(CombiningRankingsCall.class);
			this.strategy = strategy;
			this.zeroOption = zeroOption;
			this.sbflPercentages = sbflPercentages;
			this.nlflPercentages = nlflPercentages;
		}

		@Override
		public EHWithInputAndReturn<Path, List<RankingFileWrapper>> newFreshInstance() {
			return new CombiningRankingsCall(strategy, zeroOption, sbflPercentages, nlflPercentages);
		}

	}

	@Override
	public void resetAndInit() {
		//not needed
	}
}

