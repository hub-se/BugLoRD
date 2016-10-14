/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.experiments.calls;

import java.io.File;
import se.de.hu_berlin.informatik.c2r.Spectra2Ranking;
import se.de.hu_berlin.informatik.constants.Defects4JConstants;
import se.de.hu_berlin.informatik.defects4j.experiments.ExperimentToken;
import se.de.hu_berlin.informatik.defects4j.frontend.Prop;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturn;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturnFactory;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ExperimentRunnerComputeSBFLRankingsFromSpectraEH extends EHWithInputAndReturn<ExperimentToken,ExperimentToken> {

	public static class Factory extends EHWithInputAndReturnFactory<ExperimentToken,ExperimentToken> {
		
		/**
		 * Initializes a {@link Factory} object.
		 */
		public Factory() {
			super(ExperimentRunnerComputeSBFLRankingsFromSpectraEH.class);
		}

		@Override
		public EHWithInputAndReturn<ExperimentToken, ExperimentToken> newFreshInstance() {
			return new ExperimentRunnerComputeSBFLRankingsFromSpectraEH();
		}
	}
	
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
	public ExperimentToken processInput(ExperimentToken input) {
		Log.out(this, "Processing project '%s', bug %s.", input.getProject(), input.getBugId());
		Prop prop = new Prop(input.getProject(), input.getBugId(), true);
		prop.switchToArchiveMode();

		/* #====================================================================================
		 * # compute SBFL rankings for the given localizers
		 * #==================================================================================== */
		if (!(new File(prop.buggyWorkDir)).exists()) {
			Log.err(this, "Archive buggy project version directory doesn't exist: '" + prop.buggyWorkDir + "'.");
			Log.err(this, "Error while computing SBFL rankings. Skipping project '"
					+ input.getProject() + "', bug '" + input.getBugId() + "'.");
			return null;
		}
		
		/* #====================================================================================
		 * # calculate rankings from existing spectra file
		 * #==================================================================================== */
		String rankingDir = prop.buggyWorkDir + Prop.SEP + Defects4JConstants.DIR_NAME_RANKING;

		String compressedSpectraFile = rankingDir + Prop.SEP + Defects4JConstants.SPECTRA_FILE_NAME;
		if (!(new File(compressedSpectraFile)).exists()) {
			Log.err(this, "Spectra file doesn't exist: '" + compressedSpectraFile + "'.");
			Log.err(this, "Error while computing SBFL rankings. Skipping project '"
					+ input.getProject() + "', bug '" + input.getBugId() + "'.");
			return null;
		}
		
		String[] localizers = prop.localizers.split(" ");
		Spectra2Ranking.generateRankingForDefects4JElement(compressedSpectraFile, rankingDir, localizers);
		
		return input;
	}

}

