/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.plot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JBuggyFixedEntity;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.BugLoRDProperties;
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
		private final String outputDir;
		private final int threadCount;
		private final boolean normalized;
		private final double baseEntropy;
		
		/**
		 * Initializes a {@link Factory} object with the given parameters.
		 * @param strategy
		 * the strategy to use when encountering equal-rank data points
		 * @param baseEntropy
		 * a base value for the entropy (serving as a threshold)
		 * @param project
		 * the project
		 * @param outputDir
		 * the main plot output directory
		 * @param threadCount
		 * number of parallel threads to use
		 * @param normalized
		 * whether the rankings should be normalized before combination
		 */
		public Factory(ParserStrategy strategy, double baseEntropy,
				String project, String outputDir, 
				int threadCount, boolean normalized) {
			super(PlotAverageEH.class);
			this.strategy = strategy;
			this.project = project;
			this.outputDir = outputDir;
			this.threadCount = threadCount;
			this.normalized = normalized;
			this.baseEntropy = baseEntropy;
		}

		@Override
		public EHWithInput<String> newFreshInstance() {
			return new PlotAverageEH(strategy, baseEntropy, project, outputDir, threadCount, normalized);
		}
	}
	
	private final static String SEP = File.separator;
	
	private final ParserStrategy strategy;
	private final String project;
	private String outputDir;
	private final int threadCount;
	private final boolean normalized;

	private final boolean isProject;

	private final double baseEntropy;
	
	final private static String[] gp = BugLoRD.getValueOf(BugLoRDProperties.RANKING_PERCENTAGES).split(" ");
	
	/**
	 * Initializes a {@link PlotAverageEH} object with the given parameters.
	 * @param strategy
	 * the strategy to use when encountering equal-rank data points
	 * @param baseEntropy
	 * a base value for the entropy (serving as a threshold)
	 * @param project
	 * the project
	 * @param outputDir
	 * the main plot output directory
	 * @param threadCount 
	 * the number of parallel threads
	 * @param normalized
	 * whether the rankings should be normalized before combination
	 */
	public PlotAverageEH(ParserStrategy strategy, double baseEntropy, 
			String project, String outputDir, 
			int threadCount, boolean normalized) {
		super();
		this.strategy = strategy;
		this.project = project;
		this.outputDir = outputDir;
		this.threadCount = threadCount;
		this.normalized = normalized;
		this.baseEntropy = baseEntropy;
		
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
		
		List<BuggyFixedEntity> entities = new ArrayList<>();
		String plotOutputDir = generatePlotOutputDir(isProject, project, outputDir);
		
		fillEntities(entities);
		
		Plotter.plotAverage(entities, localizer, strategy, plotOutputDir, project, gp, baseEntropy,
				threadCount, normalized);
		
		return true;
	}

	private void fillEntities(List<BuggyFixedEntity> entities) {
		if (isProject) {
			/* #====================================================================================
			 * # plot averaged rankings for given project
			 * #==================================================================================== */
			//iterate over all ids
			String[] ids = Defects4J.getAllBugIDs(project); 
			for (String id : ids) {
				entities.add(new Defects4JBuggyFixedEntity(project, id));
			}
			
		} else { //given project name was "super"; iterate over all project directories
			
			//iterate over all projects
			for (String project : Defects4J.getAllProjects()) {
				String[] ids = Defects4J.getAllBugIDs(project); 
				for (String id : ids) {
					entities.add(new Defects4JBuggyFixedEntity(project, id));
				}
			}
			
		}
	}
	
	public static String generatePlotOutputDir(Boolean isProject, String project, String outputDir) {
		String plotOutputDir;
		if (isProject) {
			
			/* #====================================================================================
			 * # plot averaged rankings for given project
			 * #==================================================================================== */
			plotOutputDir = outputDir + SEP + "average" + SEP + project;

			
		} else { //given project name was "super"
			
			/* #====================================================================================
			 * # plot averaged rankings for super directory
			 * #==================================================================================== */
			plotOutputDir = outputDir + SEP + "average" + SEP + "super";

		}
		return plotOutputDir;
	}

}

