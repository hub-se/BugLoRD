/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend.tools.calls;

import java.io.File;
import java.util.concurrent.Callable;

import se.de.hu_berlin.informatik.c2r.Spectra2Ranking;
import se.de.hu_berlin.informatik.constants.Defects4JConstants;
import se.de.hu_berlin.informatik.defects4j.frontend.Prop;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithPaths;
import se.de.hu_berlin.informatik.utils.threaded.DisruptorEventHandler;
import se.de.hu_berlin.informatik.utils.threaded.IDisruptorEventHandlerFactory;

/**
 * {@link Callable} object that runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ExperimentRunnerComputeSBFLRankingsFromSpectraCall extends CallableWithPaths<String, Boolean> {

	public static class Factory implements IDisruptorEventHandlerFactory<String> {

		private final String project;
		
		/**
		 * Initializes a {@link Factory} object with the given parameters.
		 * @param project
		 * the id of the project under consideration
		 */
		public Factory(String project) {
			super();
			this.project = project;
		}
		
		@Override
		public Class<? extends DisruptorEventHandler<String>> getEventHandlerClass() {
			return ExperimentRunnerComputeSBFLRankingsFromSpectraCall.class;
		}

		@Override
		public DisruptorEventHandler<String> newInstance() {
			return new ExperimentRunnerComputeSBFLRankingsFromSpectraCall(project);
		}
	}
	
	final private String project;
	
	/**
	 * Initializes a {@link ExperimentRunnerComputeSBFLRankingsFromSpectraCall} object with the given parameters.
	 * @param project
	 * the id of the project under consideration
	 */
	public ExperimentRunnerComputeSBFLRankingsFromSpectraCall(String project) {
		super();
		this.project = project;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() {
		String id = getInput();
		
		Prop prop = new Prop(project, id, true);
		prop.switchToArchiveMode();

		/* #====================================================================================
		 * # compute SBFL rankings for the given localizers
		 * #==================================================================================== */
		if (!(new File(prop.buggyWorkDir)).exists()) {
			Log.err(this, "Archive buggy project version directory doesn't exist: '" + prop.buggyWorkDir + "'.");
			Log.err(this, "Error while computing SBFL rankings. Skipping project '"
					+ project + "', bug '" + id + "'.");
			return false;
		}
		
		/* #====================================================================================
		 * # calculate rankings from existing spectra file
		 * #==================================================================================== */
		String rankingDir = prop.buggyWorkDir + Prop.SEP + Defects4JConstants.DIR_NAME_RANKING;

		String compressedSpectraFile = rankingDir + Prop.SEP + Defects4JConstants.SPECTRA_FILE_NAME;
		if (!(new File(compressedSpectraFile)).exists()) {
			Log.err(this, "Spectra file doesn't exist: '" + compressedSpectraFile + "'.");
			Log.err(this, "Error while computing SBFL rankings. Skipping project '"
					+ project + "', bug '" + id + "'.");
			return false;
		}
		
		String[] localizers = prop.localizers.split(" ");
		Spectra2Ranking.generateRankingForDefects4JElement(compressedSpectraFile, rankingDir, localizers);
		
		return true;
	}

	@Override
	public void resetAndInit() {
		//not needed
	}

}

