/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.experiments.calls;

import java.io.File;
import se.de.hu_berlin.informatik.c2r.Spectra2Ranking;
import se.de.hu_berlin.informatik.constants.Defects4JConstants;
import se.de.hu_berlin.informatik.defects4j.frontend.Defects4JEntity;
import se.de.hu_berlin.informatik.defects4j.frontend.BugLoRD;
import se.de.hu_berlin.informatik.defects4j.frontend.BugLoRD.BugLoRDProperties;
import se.de.hu_berlin.informatik.defects4j.frontend.Defects4J;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturn;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturnFactory;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ExperimentRunnerComputeSBFLRankingsFromSpectraEH extends EHWithInputAndReturn<Defects4JEntity,Defects4JEntity> {

	public static class Factory extends EHWithInputAndReturnFactory<Defects4JEntity,Defects4JEntity> {
		
		/**
		 * Initializes a {@link Factory} object.
		 */
		public Factory() {
			super(ExperimentRunnerComputeSBFLRankingsFromSpectraEH.class);
		}

		@Override
		public EHWithInputAndReturn<Defects4JEntity, Defects4JEntity> newFreshInstance() {
			return new ExperimentRunnerComputeSBFLRankingsFromSpectraEH();
		}
	}
	
	final private static String[] localizers = BugLoRD.getValueOf(BugLoRDProperties.LOCALIZERS).split(" ");
	
	/**
	 * Initializes a {@link ExperimentRunnerComputeSBFLRankingsFromSpectraEH} object.
	 */
	public ExperimentRunnerComputeSBFLRankingsFromSpectraEH() {
		super();
	}

	@Override
	public void resetAndInit() {
		//not needed
	}

	@Override
	public Defects4JEntity processInput(Defects4JEntity buggyEntity) {
		Log.out(this, "Processing project '%s', bug %s.", buggyEntity.getProject(), buggyEntity.getBugId());
		buggyEntity.switchToArchiveDir();

		/* #====================================================================================
		 * # compute SBFL rankings for the given localizers
		 * #==================================================================================== */
		if (!(buggyEntity.getWorkDir().toFile()).exists()) {
			Log.err(this, "Archive buggy project version directory doesn't exist: '" + buggyEntity.getWorkDir() + "'.");
			Log.err(this, "Error while computing SBFL rankings. Skipping project '"
					+ buggyEntity.getProject() + "', bug '" + buggyEntity.getBugId() + "'.");
			return null;
		}
		
		/* #====================================================================================
		 * # calculate rankings from existing spectra file
		 * #==================================================================================== */
		String rankingDir = buggyEntity.getWorkDir() + Defects4J.SEP + Defects4JConstants.DIR_NAME_RANKING;

		String compressedSpectraFile = rankingDir + Defects4J.SEP + Defects4JConstants.SPECTRA_FILE_NAME;
		if (!(new File(compressedSpectraFile)).exists()) {
			Log.err(this, "Spectra file doesn't exist: '" + compressedSpectraFile + "'.");
			Log.err(this, "Error while computing SBFL rankings. Skipping project '"
					+ buggyEntity.getProject() + "', bug '" + buggyEntity.getBugId() + "'.");
			return null;
		}
		
		Spectra2Ranking.generateRankingForDefects4JElement(compressedSpectraFile, rankingDir, localizers);
		
		return buggyEntity;
	}

}

