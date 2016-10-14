/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.experiments.plot;

import java.io.File;
import se.de.hu_berlin.informatik.defects4j.frontend.Prop;
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
public class PlotAverageEH extends EHWithInput<String> {

	public static class Factory extends EHWithInputFactory<String> {

		private final ParserStrategy strategy;
		private final String project;
		private String outputDir;
		
		/**
		 * Initializes a {@link Factory} object with the given parameters.
		 * @param strategy
		 * the strategy to use when encountering equal-rank data points
		 * @param project
		 * the project
		 * @param outputDir
		 * the main plot output directory
		 */
		public Factory(ParserStrategy strategy, String project, String outputDir) {
			super(PlotAverageEH.class);
			this.strategy = strategy;
			this.project = project;
			this.outputDir = outputDir;
		}

		@Override
		public EHWithInput<String> newFreshInstance() {
			return new PlotAverageEH(strategy, project, outputDir);
		}
	}
	
	private final static String SEP = File.separator;
	
	private final ParserStrategy strategy;
	private final String project;
	private String outputDir;
	
	/**
	 * Initializes a {@link PlotAverageEH} object with the given parameters.
	 * @param strategy
	 * the strategy to use when encountering equal-rank data points
	 * @param project
	 * the project
	 * @param outputDir
	 * the main plot output directory
	 */
	public PlotAverageEH(ParserStrategy strategy, String project, String outputDir) {
		super();
		this.strategy = strategy;
		this.project = project;
		this.outputDir = outputDir;
	}

	@Override
	public void resetAndInit() {
		//not needed
	}

	@Override
	public boolean processInput(String input) {
		Prop prop = new Prop(project, "", false);
		prop.switchToArchiveMode();

		if (outputDir == null) {
			outputDir = prop.plotMainDir;
		}
		
		String height = "120";
		
		String[] gp = prop.percentages.split(" ");
		String[] lp = { "100" };
		
		String[] localizer = { input };
		
		if (!Prop.validateProjectAndBugID(project, 1, false)) {
			if (project.equals("super")) {
				/* #====================================================================================
				 * # plot averaged rankings for super directory
				 * #==================================================================================== */
				String plotOutputDir = outputDir + SEP + "average" + SEP + "super";
				
				Plotter.plotAverageDefects4JProject(
						prop.mainDir, plotOutputDir, strategy, height, localizer, gp, lp);
				
				return true;
			} else {
				Log.err(this, "Archive project directory doesn't exist: '" + prop.projectDir + "'.");
				return false;
			}
		}
			
		/* #====================================================================================
		 * # plot averaged rankings for given project
		 * #==================================================================================== */
		String plotOutputDir = outputDir + SEP + "average" + SEP + input;
		
		Plotter.plotAverageDefects4JProject(
				prop.projectDir, plotOutputDir, strategy, height, localizer, gp, lp);
		
		return true;
	}

}

