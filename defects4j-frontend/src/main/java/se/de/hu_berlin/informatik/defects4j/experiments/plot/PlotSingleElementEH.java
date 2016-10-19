/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.experiments.plot;

import java.io.File;

import se.de.hu_berlin.informatik.defects4j.frontend.Defects4JEntity;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter;
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
		
		/**
		 * Initializes a {@link Factory} object with the given parameters.
		 * @param project
		 * the id of the project under consideration
		 * @param localizers
		 * the SBFL localizers to use
		 * @param outputDir
		 * the main plot output directory
		 */
		public Factory(String project, String[] localizers, String outputDir) {
			super(PlotSingleElementEH.class);
			this.project = project;
			this.localizers = localizers;
			this.outputDir = outputDir;
		}

		@Override
		public EHWithInput<String> newFreshInstance() {
			return new PlotSingleElementEH(project, localizers, outputDir);
		}
	}
	
	private final static String SEP = File.separator;
	
	private final String project;
	private final String[] localizers;
	private String outputDir;

	final private static String[] gp = Defects4JEntity.getProperties().percentages.split(" ");
	final private static String[] lp = { "100" };
	
	/**
	 * Initializes a {@link PlotSingleElementEH} object with the given parameters.
	 * @param project
	 * the id of the project under consideration
	 * @param localizers
	 * the SBFL localizers to use
	 * @param outputDir
	 * the main plot output directory
	 */
	public PlotSingleElementEH(String project, String[] localizers, String outputDir) {
		super();
		this.project = project;
		this.localizers = localizers;
		this.outputDir = outputDir;
	}

	@Override
	public void resetAndInit() {
		//not needed
	}

	@Override
	public boolean processInput(String input) {
		Defects4JEntity buggyEntity = Defects4JEntity.getBuggyDefects4JEntity(project, input);
		buggyEntity.switchToArchiveDir();

		File archiveBuggyWorkDir = buggyEntity.getWorkDir().toFile();
		
		if (!archiveBuggyWorkDir.exists()) {
			Log.abort(this, "Archive buggy project version directory doesn't exist: '" + buggyEntity.getWorkDir() + "'.");
		}
		
		if (outputDir == null) {
			outputDir = Defects4JEntity.getProperties().plotMainDir;
		}
		
		/* #====================================================================================
		 * # plot a single Defects4J element
		 * #==================================================================================== */
		String rankingDir = buggyEntity.getWorkDir() + SEP + "ranking";
		
		String plotOutputDir = outputDir + SEP + project;
		
		String range = "200";
		String height = "120";
		
		Plotter.plotSingleDefects4JElement(project, input, rankingDir, plotOutputDir, range, height, localizers, gp, lp);
		
		return true;
	}

}

