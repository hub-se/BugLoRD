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
import se.de.hu_berlin.informatik.utils.experiments.ranking.NormalizedRanking.NormalizationStrategy;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.tm.user.AbstractConsumingProcessorUser;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class PlotAverageEH extends AbstractConsumingProcessorUser<String> {
	
	private final static String SEP = File.separator;
	
	private final ParserStrategy strategy;
	private final String project;
	private String outputDir;
	private final int threadCount;
	private final NormalizationStrategy normStrategy;

	private final boolean isProject;

	final private static String[] gp = BugLoRD.getValueOf(BugLoRDProperties.RANKING_PERCENTAGES).split(" ");
	
	/**
	 * Initializes a {@link PlotAverageEH} object with the given parameters.
	 * @param strategy
	 * the strategy to use when encountering equal-rank data points
	 * @param project
	 * the project
	 * @param outputDir
	 * the main plot output directory
	 * @param threadCount 
	 * the number of parallel threads
	 * @param normStrategy
	 * whether the rankings should be normalized before combination
	 */
	public PlotAverageEH(ParserStrategy strategy,
			String project, String outputDir, 
			int threadCount, NormalizationStrategy normStrategy) {
		super();
		this.strategy = strategy;
		this.project = project;
		this.outputDir = outputDir;
		this.threadCount = threadCount;
		this.normStrategy = normStrategy;
		
		this.isProject = Defects4J.validateProject(project, false);
		
		if (!isProject && !project.equals("super")) {
			Log.abort(this, "Project doesn't exist: '" + project + "'.");
		}
		
		if (this.outputDir == null) {
			this.outputDir = Defects4J.getValueOf(Defects4JProperties.PLOT_DIR);
		}
	}

	@Override
	public void consume(String localizer) {
		
		List<BuggyFixedEntity> entities = new ArrayList<>();
		String plotOutputDir = generatePlotOutputDir(outputDir, project, normStrategy);
		
		fillEntities(entities);
		
		Plotter.plotAverage(entities, localizer, strategy, plotOutputDir, project, gp,
				threadCount, normStrategy);
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
	
	public static String generatePlotOutputDir(String outputDir, String identifier, NormalizationStrategy normStrategy2) {
		String plotOutputDir;	
		/* #====================================================================================
		 * # plot averaged rankings for given identifier (project, super, ...)
		 * #==================================================================================== */
		if (normStrategy2 == null) {
			plotOutputDir = outputDir + SEP + "average" + SEP + identifier;
		} else {
			plotOutputDir = outputDir + SEP + "average" + SEP + identifier + "_" + normStrategy2;
		}

		return plotOutputDir;
	}

}

