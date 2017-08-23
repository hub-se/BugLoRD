/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import java.io.File;
import java.nio.file.Path;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.BugLoRDProperties;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.ToolSpecific;
import se.de.hu_berlin.informatik.sbfl.ranking.Spectra2Ranking;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.AbstractSpectrumBasedFaultLocalizer.ComputationStrategies;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ERComputeSBFLRankingsFromSpectraEH extends AbstractProcessor<BuggyFixedEntity<?>,BuggyFixedEntity<?>> {

	final private static String[] localizers = BugLoRD.getValueOf(BugLoRDProperties.LOCALIZERS).split(" ");
	final private boolean removeIrrelevantNodes;
	final private boolean condenseNodes;
	final private ComputationStrategies strategy;
	final private String suffix;
	private ToolSpecific toolSpecific;
	
	/**
	 * Initializes a {@link ERComputeSBFLRankingsFromSpectraEH} object.
	 * @param toolSpecific
	 * chooses what kind of spectra to use
	 * @param suffix 
	 * a suffix to append to the ranking directory (may be null)
	 * @param removeIrrelevantNodes
	 * whether to remove nodes that were not touched by any failed traces
	 * @param condenseNodes
	 * whether to combine several lines with equal trace involvement
	 * @param strategy
	 * the strategy to use for computation of the rankings
	 */
	public ERComputeSBFLRankingsFromSpectraEH(ToolSpecific toolSpecific,
			String suffix, final boolean removeIrrelevantNodes, final boolean condenseNodes, ComputationStrategies strategy) {
		super();
		this.toolSpecific = toolSpecific;
		this.suffix = suffix;
		this.removeIrrelevantNodes = removeIrrelevantNodes;
		this.condenseNodes = condenseNodes;
		this.strategy = strategy;
	}

	@Override
	public BuggyFixedEntity<?> processItem(BuggyFixedEntity<?> buggyEntity) {
		Log.out(this, "Processing %s.", buggyEntity);

		Entity bug = buggyEntity.getBuggyVersion();

		/* #====================================================================================
		 * # compute SBFL rankings for the given localizers
		 * #==================================================================================== */
		if (!(bug.getWorkDataDir().toFile()).exists()) {
			Log.err(this, "Work data directory doesn't exist: '" + bug.getWorkDataDir() + "'.");
			Log.err(this, "Error while computing SBFL rankings. Skipping '" + buggyEntity + "'.");
			return null;
		}
		
		/* #====================================================================================
		 * # calculate rankings from existing spectra file
		 * #==================================================================================== */
		String subDirName = BugLoRD.getSubDirName(toolSpecific);
		String compressedSpectraFile = BugLoRD.getSpectraFilePath(bug, subDirName).toString();
		
		if (!(new File(compressedSpectraFile)).exists()) {
			Log.err(this, "Spectra file doesn't exist: '" + compressedSpectraFile + "'.");
			Log.err(this, "Error while computing SBFL rankings. Skipping '" + buggyEntity + "'.");
			return null;
		}
		
		Path rankingDir = bug.getWorkDataDir().resolve(suffix == null ? 
				BugLoRDConstants.DIR_NAME_RANKING : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix);
		if (removeIrrelevantNodes) {
			String compressedSpectraFileFiltered = BugLoRD.getFilteredSpectraFilePath(bug, subDirName).toString();
			
			if (new File(compressedSpectraFileFiltered).exists()) {
				Spectra2Ranking.generateRanking(compressedSpectraFileFiltered, rankingDir.toString(), 
						localizers, false, condenseNodes, strategy);
			} else {
				Spectra2Ranking.generateRanking(compressedSpectraFile, rankingDir.toString(), 
						localizers, true, condenseNodes, strategy);
			}
		} else {
			Spectra2Ranking.generateRanking(compressedSpectraFile, rankingDir.toString(), 
					localizers, false, condenseNodes, strategy);
		}
		
		return buggyEntity;
	}

}

