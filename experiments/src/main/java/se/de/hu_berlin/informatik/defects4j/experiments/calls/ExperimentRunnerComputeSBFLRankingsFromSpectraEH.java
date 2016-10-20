/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.experiments.calls;

import java.io.File;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRD;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedBenchmarkEntity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JConstants;
import se.de.hu_berlin.informatik.benchmark.api.BugLoRD.BugLoRDProperties;
import se.de.hu_berlin.informatik.c2r.Spectra2Ranking;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturn;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturnFactory;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ExperimentRunnerComputeSBFLRankingsFromSpectraEH extends EHWithInputAndReturn<BuggyFixedBenchmarkEntity,BuggyFixedBenchmarkEntity> {

	public static class Factory extends EHWithInputAndReturnFactory<BuggyFixedBenchmarkEntity,BuggyFixedBenchmarkEntity> {
		
		/**
		 * Initializes a {@link Factory} object.
		 */
		public Factory() {
			super(ExperimentRunnerComputeSBFLRankingsFromSpectraEH.class);
		}

		@Override
		public EHWithInputAndReturn<BuggyFixedBenchmarkEntity, BuggyFixedBenchmarkEntity> newFreshInstance() {
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
	public BuggyFixedBenchmarkEntity processInput(BuggyFixedBenchmarkEntity buggyEntity) {
		Log.out(this, "Processing %s.", buggyEntity);
		buggyEntity.switchToArchiveDir();

		/* #====================================================================================
		 * # compute SBFL rankings for the given localizers
		 * #==================================================================================== */
		if (!(buggyEntity.getWorkDir().toFile()).exists()) {
			Log.err(this, "Archive buggy project version directory doesn't exist: '" + buggyEntity.getWorkDir() + "'.");
			Log.err(this, "Error while computing SBFL rankings. Skipping '"
					+ buggyEntity + "'.");
			return null;
		}
		
		/* #====================================================================================
		 * # calculate rankings from existing spectra file
		 * #==================================================================================== */
		String rankingDir = buggyEntity.getWorkDir().resolve(Defects4JConstants.DIR_NAME_RANKING).toString();

		String compressedSpectraFile = rankingDir + Defects4J.SEP + Defects4JConstants.SPECTRA_FILE_NAME;
		if (!(new File(compressedSpectraFile)).exists()) {
			Log.err(this, "Spectra file doesn't exist: '" + compressedSpectraFile + "'.");
			Log.err(this, "Error while computing SBFL rankings. Skipping '"
					+ buggyEntity + "'.");
			return null;
		}
		
		Spectra2Ranking.generateRankingForDefects4JElement(compressedSpectraFile, rankingDir, localizers);
		
		return buggyEntity;
	}

}

