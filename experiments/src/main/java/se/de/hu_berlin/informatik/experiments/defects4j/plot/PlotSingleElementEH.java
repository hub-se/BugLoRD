/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.plot;

import java.io.File;

import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.BugLoRDProperties;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JEntity;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInput;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputFactory;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class PlotSingleElementEH extends EHWithInput<String> {

	public static class Factory extends EHWithInputFactory<String> {

		private final String project;
		private final String[] localizers;
		private String outputDir;
		private final boolean normalized;
		
		/**
		 * Initializes a {@link Factory} object with the given parameters.
		 * @param project
		 * the id of the project under consideration
		 * @param localizers
		 * the SBFL localizers to use
		 * @param outputDir
		 * the main plot output directory
		 * @param normalized
		 * whether the rankings should be normalized before combination
		 */
		public Factory(String project, String[] localizers, String outputDir, boolean normalized) {
			super(PlotSingleElementEH.class);
			this.project = project;
			this.localizers = localizers;
			this.outputDir = outputDir;
			this.normalized = normalized;
		}

		@Override
		public EHWithInput<String> newFreshInstance() {
			return new PlotSingleElementEH(project, localizers, outputDir, normalized);
		}
	}
	
	private final static String SEP = File.separator;
	
	private final String project;
	private final String[] localizers;
	private String outputDir;

	private final boolean normalized;

	final private static String[] gp = BugLoRD.getValueOf(BugLoRDProperties.RANKING_PERCENTAGES).split(" ");
	
	/**
	 * Initializes a {@link PlotSingleElementEH} object with the given parameters.
	 * @param project
	 * the id of the project under consideration
	 * @param localizers
	 * the SBFL localizers to use
	 * @param outputDir
	 * the main plot output directory
	 * @param normalized
	 * whether the rankings should be normalized before combination
	 */
	public PlotSingleElementEH(String project, String[] localizers, String outputDir, boolean normalized) {
		super();
		this.project = project;
		this.localizers = localizers;
		this.outputDir = outputDir;
		this.normalized = normalized;
	}

	@Override
	public void resetAndInit() {
		//not needed
	}

	@Override
	public boolean processInput(String input) {
		Defects4JEntity buggyEntity = Defects4JEntity.getBuggyDefects4JEntity(project, input);

		if (!buggyEntity.getWorkDataDir().toFile().exists()) {
			Log.abort(this, "Data directory doesn't exist for: '%s'.", buggyEntity);
		}
		
		if (outputDir == null) {
			outputDir = Defects4J.getValueOf(Defects4JProperties.PLOT_DIR);
		}
		
		/* #====================================================================================
		 * # plot a single Defects4J element
		 * #==================================================================================== */

		String plotOutputDir = outputDir + SEP + project;
		
		for (String localizer : localizers) {
			Plotter.plotSingle(buggyEntity, localizer, ParserStrategy.NO_CHANGE, plotOutputDir, "", gp, normalized);
		}
		
		return true;
	}

}

