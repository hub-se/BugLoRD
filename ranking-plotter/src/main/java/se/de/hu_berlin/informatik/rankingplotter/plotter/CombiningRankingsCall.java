/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.nio.file.Path;
import java.util.List;
import se.de.hu_berlin.informatik.rankingplotter.modules.CombiningRankingsModule;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithReturn;

/**
 * {@link CallableWithReturn} object that ...
 * 
 * @author Simon Heiden
 */
public class CombiningRankingsCall extends CallableWithReturn<List<RankingFileWrapper>> {

	private ParserStrategy strategy;
	private boolean zeroOption;
	private String[] sbflPercentages;
	private String[] nlflPercentages;
	
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

}

