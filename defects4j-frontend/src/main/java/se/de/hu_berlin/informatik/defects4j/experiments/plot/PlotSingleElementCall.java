/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.experiments.plot;

import java.io.File;
import java.util.concurrent.Callable;

import se.de.hu_berlin.informatik.defects4j.frontend.Prop;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithPaths;
import se.de.hu_berlin.informatik.utils.threaded.DisruptorEventHandler;
import se.de.hu_berlin.informatik.utils.threaded.IDisruptorEventHandlerFactory;

/**
 * {@link Callable} object that runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class PlotSingleElementCall extends CallableWithPaths<String, Boolean> {

	public static class Factory implements IDisruptorEventHandlerFactory<String> {

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
			super();
			this.project = project;
			this.localizers = localizers;
			this.outputDir = outputDir;
		}
		
		@Override
		public Class<? extends DisruptorEventHandler<String>> getEventHandlerClass() {
			return PlotSingleElementCall.class;
		}

		@Override
		public DisruptorEventHandler<String> newInstance() {
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

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() {
		String id = getInput();
		
		Prop prop = new Prop(project, id, true);
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
		
		Plotter.plotSingleDefects4JElement(project, id, rankingDir, plotOutputDir, range, height, localizers, gp, lp);
		
		return true;
	}

	@Override
	public void resetAndInit() {
		//not needed
	}

}

