/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.nio.file.Path;
import java.util.List;

import se.de.hu_berlin.informatik.rankingplotter.modules.PercentageParserModule;
import se.de.hu_berlin.informatik.rankingplotter.modules.PercentageParserModule.ParserStrategy;
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

