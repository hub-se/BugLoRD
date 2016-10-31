/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.util.List;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.rankingplotter.modules.CombiningRankingsModule;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturn;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturnFactory;

/**
 * {@link EHWithInputAndReturn} object that ...
 * 
 * @author Simon Heiden
 */
public class CombiningRankingsCall extends EHWithInputAndReturn<BuggyFixedEntity,List<RankingFileWrapper>> {

	final private String localizer;
	final private ParserStrategy strategy;
	final private String[] sbflPercentages;
	
	/**
	 * Initializes a {@link CombiningRankingsCall} object with the given parameters.
	 * @param localizer
	 * a fault localizer
	 * @param strategy
	 * which strategy to use. May take the lowest or the highest ranking of a range of 
	 * equal-value rankings or may compute the average
	 * @param sbflPercentages
	 * an array of percentage values that determine the weighting 
	 * of the SBFL ranking to the NLFL ranking
	 */
	public CombiningRankingsCall(String localizer, ParserStrategy strategy,
			String[] sbflPercentages) {
		super();
		this.localizer = localizer;
		this.strategy = strategy;
		this.sbflPercentages = sbflPercentages;
	}

	@Override
	public List<RankingFileWrapper> processInput(BuggyFixedEntity input) {
		return new CombiningRankingsModule(localizer, strategy, sbflPercentages)
				.submit(input)
				.getResult();
	}

	public static class Factory extends EHWithInputAndReturnFactory<BuggyFixedEntity,List<RankingFileWrapper>> {

		final private String localizer;
		final private ParserStrategy strategy;
		final private String[] sbflPercentages;
		
		/**
		 * Initializes a {@link Factory} object with the given parameters.
		 * @param localizer
		 * a fault localizer
		 * @param strategy
		 * which strategy to use. May take the lowest or the highest ranking of a range of 
		 * equal-value rankings or may compute the average
		 * @param sbflPercentages
		 * an array of percentage values that determine the weighting 
		 * of the SBFL ranking to the NLFL ranking
		 */
		public Factory(String localizer, ParserStrategy strategy,
				String[] sbflPercentages) {
			super(CombiningRankingsCall.class);
			this.localizer = localizer;
			this.strategy = strategy;
			this.sbflPercentages = sbflPercentages;
		}

		@Override
		public EHWithInputAndReturn<BuggyFixedEntity, List<RankingFileWrapper>> newFreshInstance() {
			return new CombiningRankingsCall(localizer, strategy, sbflPercentages);
		}

	}

	@Override
	public void resetAndInit() {
		//not needed
	}
}

