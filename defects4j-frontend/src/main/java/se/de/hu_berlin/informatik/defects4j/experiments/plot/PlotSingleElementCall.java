/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.experiments.plot;

import java.io.File;
import java.util.concurrent.Callable;

import se.de.hu_berlin.informatik.defects4j.frontend.Prop;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.ADisruptorEventHandlerFactory;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithInput;
import se.de.hu_berlin.informatik.utils.threaded.DisruptorFCFSEventHandler;

/**
 * {@link Callable} object that runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class PlotSingleElementCall extends CallableWithInput<String> {

	public static class Factory extends ADisruptorEventHandlerFactory<String> {

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
			super(PlotSingleElementCall.class);
			this.project = project;
			this.localizers = localizers;
			this.outputDir = outputDir;
		}

		@Override
		public DisruptorFCFSEventHandler<String> newInstance() {
			return new PlotSingleElementCall(project, localizers, outputDir);
		}
	}
	
	private final static String SEP = File.separator;
	
	private final String project;
	private final String[] localizers;
	private String outputDir;
	
	/**
	 * Initializes a {@link PlotSingleElementCall} object with the given parameters.
	 * @param project
	 * the id of the project under consideration
	 * @param localizers
	 * the SBFL localizers to use
	 * @param outputDir
	 * the main plot output directory
	 */
	public PlotSingleElementCall(String project, String[] localizers, String outputDir) {
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
		Prop prop = new Prop(project, input, true);
		prop.switchToArchiveMode();

		File archiveBuggyWorkDir = new File(prop.buggyWorkDir);
		
		if (!archiveBuggyWorkDir.exists()) {
			Log.abort(this, "Archive buggy project version directory doesn't exist: '" + prop.buggyWorkDir + "'.");
		}
		
		if (outputDir == null) {
			outputDir = prop.plotMainDir;
		}
		
		/* #====================================================================================
		 * # plot a single Defects4J element
		 * #==================================================================================== */
		String rankingDir = prop.buggyWorkDir + SEP + "ranking";
		
		String plotOutputDir = outputDir + SEP + project;
		
		String range = "200";
		String height = "120";
		
		String[] gp = prop.percentages.split(" ");
		String[] lp = { "100" };
		
		Plotter.plotSingleDefects4JElement(project, input, rankingDir, plotOutputDir, range, height, localizers, gp, lp);
		
		return true;
	}

}

