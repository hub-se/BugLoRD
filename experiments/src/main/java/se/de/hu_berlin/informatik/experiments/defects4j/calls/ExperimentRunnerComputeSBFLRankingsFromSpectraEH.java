/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import java.io.File;
import java.nio.file.Path;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.c2r.Spectra2Ranking;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.BugLoRDProperties;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturn;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturnFactory;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ExperimentRunnerComputeSBFLRankingsFromSpectraEH extends EHWithInputAndReturn<BuggyFixedEntity,BuggyFixedEntity> {

	public static class Factory extends EHWithInputAndReturnFactory<BuggyFixedEntity,BuggyFixedEntity> {
		
		final private boolean removeIrrelevantNodes;
		final private boolean condenseNodes;
		
		public Factory(final boolean removeIrrelevantNodes, 
				final boolean condenseNodes) {
			super(ExperimentRunnerComputeSBFLRankingsFromSpectraEH.class);
			this.removeIrrelevantNodes = removeIrrelevantNodes;
			this.condenseNodes = condenseNodes;
		}

		@Override
		public EHWithInputAndReturn<BuggyFixedEntity, BuggyFixedEntity> newFreshInstance() {
			return new ExperimentRunnerComputeSBFLRankingsFromSpectraEH(
					removeIrrelevantNodes, condenseNodes);
		}
	}
	
	final private static String[] localizers = BugLoRD.getValueOf(BugLoRDProperties.LOCALIZERS).split(" ");
	final private boolean removeIrrelevantNodes;
	final private boolean condenseNodes;
	
	/**
	 * Initializes a {@link ExperimentRunnerComputeSBFLRankingsFromSpectraEH} object.
	 * @param removeIrrelevantNodes
	 * whether to remove nodes that were not touched by any failed traces
	 * @param condenseNodes
	 * whether to combine several lines with equal trace involvement
	 */
	public ExperimentRunnerComputeSBFLRankingsFromSpectraEH(
			final boolean removeIrrelevantNodes, final boolean condenseNodes) {
		super();
		this.removeIrrelevantNodes = removeIrrelevantNodes;
		this.condenseNodes = condenseNodes;
	}

	@Override
	public void resetAndInit() {
		//not needed
	}

	@Override
	public BuggyFixedEntity processInput(BuggyFixedEntity buggyEntity) {
		Log.out(this, "Processing %s.", buggyEntity);
		
		/* #====================================================================================
		 * # compute SBFL rankings for the given localizers
		 * #==================================================================================== */
		if (!(buggyEntity.getWorkDataDir().toFile()).exists()) {
			Log.err(this, "Work data directory doesn't exist: '" + buggyEntity.getWorkDataDir() + "'.");
			Log.err(this, "Error while computing SBFL rankings. Skipping '"
					+ buggyEntity + "'.");
			return null;
		}
		
		/* #====================================================================================
		 * # calculate rankings from existing spectra file
		 * #==================================================================================== */
		Path rankingDir = buggyEntity.getWorkDataDir().resolve(BugLoRDConstants.DIR_NAME_RANKING);

		String compressedSpectraFile = rankingDir.resolve(BugLoRDConstants.SPECTRA_FILE_NAME).toString();
		if (!(new File(compressedSpectraFile)).exists()) {
			Log.err(this, "Spectra file doesn't exist: '" + compressedSpectraFile + "'.");
			Log.err(this, "Error while computing SBFL rankings. Skipping '" + buggyEntity + "'.");
			return null;
		}
		
		Spectra2Ranking.generateRanking(compressedSpectraFile, rankingDir.toString(), 
				localizers, removeIrrelevantNodes, condenseNodes);
		
		return buggyEntity;
	}

}

