/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.plot;

import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInput;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputFactory;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class PlotFromCsvEH extends EHWithInput<String> {

	public static class Factory extends EHWithInputFactory<String> {

		private final String project;
		private final String outputDir;

		public Factory(String project, String outputDir) {
			super(PlotFromCsvEH.class);
			this.project = project;
			this.outputDir = outputDir;
		}

		@Override
		public EHWithInput<String> newFreshInstance() {
			return new PlotFromCsvEH(project, outputDir);
		}
	}
	
	private final String project;
	private String outputDir;
	
	private final boolean isProject;
	
	/**
	 * Initializes a {@link PlotFromCsvEH} object with the given parameters.
	 * @param project
	 * the project
	 * @param outputDir
	 * the main plot output directory
	 */
	public PlotFromCsvEH(String project, String outputDir) {
		super();
		this.project = project;
		this.outputDir = outputDir;
		
		this.isProject = Defects4J.validateProject(project, false);
		
		if (!isProject && !project.equals("super")) {
			Log.abort(this, "Project doesn't exist: '" + project + "'.");
		}
		
		if (this.outputDir == null) {
			this.outputDir = Defects4J.getValueOf(Defects4JProperties.PLOT_DIR);
		}
	}

	@Override
	public void resetAndInit() {
		//not needed
	}

	@Override
	public boolean processInput(String localizer) {
		
		String plotOutputDir = PlotAverageEH.generatePlotOutputDir(outputDir, localizer, null);
		
		Plotter.plotFromCSV(localizer, plotOutputDir, plotOutputDir, project);
		
		return true;
	}

	

}

