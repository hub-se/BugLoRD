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
import se.de.hu_berlin.informatik.utils.processors.AbstractConsumingProcessor;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class PlotAverageEH extends AbstractConsumingProcessor<String> {
	
	private final static String SEP = File.separator;
	
	private final ParserStrategy strategy;
	private final String project;
	private String outputDir;
	private final int threadCount;
	private final NormalizationStrategy normStrategy;

	private final boolean isProject;

	private String suffix;

	final private static String[] gp = BugLoRD.getValueOf(BugLoRDProperties.RANKING_PERCENTAGES).split(" ");
	
	/**
	 * Initializes a {@link PlotAverageEH} object with the given parameters.
	 * @param suffix 
	 * a suffix to append to the ranking directory (may be null)
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
	public PlotAverageEH(String suffix, ParserStrategy strategy,
			String project, String outputDir, 
			int threadCount, NormalizationStrategy normStrategy) {
		super();
		this.suffix = suffix;
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
	public void consumeItem(String identifier1) {
		
		List<BuggyFixedEntity<?>> entities = new ArrayList<>();
		String plotOutputDir = generatePlotOutputDir(outputDir, suffix, project, normStrategy);
		
		fillEntities(entities);
		
		List<String> allRankingFileNames = GeneratePlots.getAllLMRankingFileIdentifiers();
		
		for (String lmRankingFileName : allRankingFileNames) {
			// skip equal identifiers
			if (identifier1.equals(lmRankingFileName)) {
				continue;
			}
			Plotter.plotAverage(entities, suffix, identifier1, lmRankingFileName, strategy, plotOutputDir, project, gp,
					threadCount, normStrategy);
		}
		
	}

	

	private void fillEntities(List<BuggyFixedEntity<?>> entities) {
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
	
	public static String generatePlotOutputDir(String outputDir, String suffix, String identifier, NormalizationStrategy normStrategy2) {
		String plotOutputDir;
		/* #====================================================================================
		 * # plot averaged rankings for given identifier (project, super, ...)
		 * #==================================================================================== */
		if (normStrategy2 == null) {
			plotOutputDir = outputDir + SEP + "average" + (suffix == null ? "" : "_" + suffix) 
					+ SEP + identifier;
		} else {
			plotOutputDir = outputDir + SEP + "average" + (suffix == null ? "" : "_" + suffix) 
					+ SEP + identifier + "_" + normStrategy2;
		}

		return plotOutputDir;
	}

}

