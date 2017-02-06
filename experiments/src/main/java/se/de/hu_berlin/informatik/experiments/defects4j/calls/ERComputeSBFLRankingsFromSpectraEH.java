/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import java.io.File;
import java.nio.file.Path;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
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
public class ERComputeSBFLRankingsFromSpectraEH extends EHWithInputAndReturn<BuggyFixedEntity,BuggyFixedEntity> {

	public static class Factory extends EHWithInputAndReturnFactory<BuggyFixedEntity,BuggyFixedEntity> {
		
		final private boolean removeIrrelevantNodes;
		final private boolean condenseNodes;
		
		public Factory(final boolean removeIrrelevantNodes, 
				final boolean condenseNodes) {
			super(ERComputeSBFLRankingsFromSpectraEH.class);
			this.removeIrrelevantNodes = removeIrrelevantNodes;
			this.condenseNodes = condenseNodes;
		}

		@Override
		public EHWithInputAndReturn<BuggyFixedEntity, BuggyFixedEntity> newFreshInstance() {
			return new ERComputeSBFLRankingsFromSpectraEH(
					removeIrrelevantNodes, condenseNodes);
		}
	}
	
	final private static String[] localizers = BugLoRD.getValueOf(BugLoRDProperties.LOCALIZERS).split(" ");
	final private boolean removeIrrelevantNodes;
	final private boolean condenseNodes;
	
	/**
	 * Initializes a {@link ERComputeSBFLRankingsFromSpectraEH} object.
	 * @param removeIrrelevantNodes
	 * whether to remove nodes that were not touched by any failed traces
	 * @param condenseNodes
	 * whether to combine several lines with equal trace involvement
	 */
	public ERComputeSBFLRankingsFromSpectraEH(
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
		
		Entity bug = buggyEntity.getBuggyVersion();
		
		/* #====================================================================================
		 * # compute SBFL rankings for the given localizers
		 * #==================================================================================== */
		if (!(bug.getWorkDataDir().toFile()).exists()) {
			Log.err(this, "Work data directory doesn't exist: '" + bug.getWorkDataDir() + "'.");
			Log.err(this, "Error while computing SBFL rankings. Skipping '"
					+ buggyEntity + "'.");
			return null;
		}
		
		/* #====================================================================================
		 * # calculate rankings from existing spectra file
		 * #==================================================================================== */
		Path rankingDir = bug.getWorkDataDir().resolve(BugLoRDConstants.DIR_NAME_RANKING);

		String compressedSpectraFile = rankingDir.resolve(BugLoRDConstants.SPECTRA_FILE_NAME).toString();
		if (!(new File(compressedSpectraFile)).exists()) {
			Log.err(this, "Spectra file doesn't exist: '" + compressedSpectraFile + "'.");
			Log.err(this, "Error while computing SBFL rankings. Skipping '" + buggyEntity + "'.");
			return null;
		}
		
		String compressedSpectraFileFiltered = rankingDir.resolve(BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME).toString();
		if (removeIrrelevantNodes) {
			if (new File(compressedSpectraFileFiltered).exists()) {
				Spectra2Ranking.generateRanking(compressedSpectraFileFiltered, rankingDir.toString(), 
						localizers, false, condenseNodes);
			} else {
				Spectra2Ranking.generateRanking(compressedSpectraFile, rankingDir.toString(), 
						localizers, true, condenseNodes);
			}
		} else {
			Spectra2Ranking.generateRanking(compressedSpectraFile, rankingDir.toString(), 
					localizers, false, condenseNodes);
		}
		
		return buggyEntity;
	}

}

