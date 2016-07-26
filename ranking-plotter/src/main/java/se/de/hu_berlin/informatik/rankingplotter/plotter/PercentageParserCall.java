/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.nio.file.Path;
import java.util.List;

import se.de.hu_berlin.informatik.rankingplotter.modules.PercentageParserModule;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithReturn;

/**
 * {@link CallableWithReturn} object that ...
 * 
 * @author Simon Heiden
 */
public class PercentageParserCall extends CallableWithReturn<List<RankingFileWrapper>> {

	private ParserStrategy strategy;
	private boolean zeroOption;
	private boolean ignoreMainRaning;
	
	/**
	 * Initializes a {@link PercentageParserCall} object with the given parameters.
	 * @param strategy
	 *  which strategy to use. May take the lowest or the highest ranking of a range of 
	 *  equal-value rankings or may compute the average
	 * @param zeroOption
	 * whether to ignore ranking values that are zero or below zero
	 * @param ignoreMainRaning
	 * whether to ignore the main ranking file
	 */
	public PercentageParserCall(ParserStrategy strategy, boolean zeroOption, boolean ignoreMainRaning) {
		super();
		this.strategy = strategy;
		this.zeroOption = zeroOption;
		this.ignoreMainRaning = ignoreMainRaning;
	}

	@Override
	public List<RankingFileWrapper> processInput(Path input) {
		return new PercentageParserModule(true, strategy, true, zeroOption, ignoreMainRaning)
				.submit(getInputPath())
				.getResult();
	}

}

