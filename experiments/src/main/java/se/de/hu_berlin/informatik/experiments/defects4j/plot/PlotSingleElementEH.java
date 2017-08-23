/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.plot;

import java.io.File;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JBuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J.Defects4JProperties;
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
public class PlotSingleElementEH extends AbstractConsumingProcessor<String> {

	private final static String SEP = File.separator;

	private final String project;
	private final String[] localizers;
	private String outputDir;

	private final NormalizationStrategy normStrategy;

	private String suffix;

	final private static String[] gp = BugLoRD.getValueOf(BugLoRDProperties.RANKING_PERCENTAGES).split(" ");

	/**
	 * Initializes a {@link PlotSingleElementEH} object with the given
	 * parameters.
	 * @param suffix
	 * a suffix to append to the ranking directory (may be null)
	 * @param project
	 * the id of the project under consideration
	 * @param localizers
	 * the SBFL localizers to use
	 * @param outputDir
	 * the main plot output directory
	 * @param normStrategy
	 * whether the rankings should be normalized before combination
	 */
	public PlotSingleElementEH(String suffix, String project, String[] localizers, String outputDir,
			NormalizationStrategy normStrategy) {
		super();
		this.suffix = suffix;
		this.project = project;
		this.localizers = localizers;
		this.outputDir = outputDir;
		this.normStrategy = normStrategy;
	}

	@Override
	public void consumeItem(String input) {
		BuggyFixedEntity<?> buggyEntity = new Defects4JBuggyFixedEntity(project, input);

		if (!buggyEntity.getBuggyVersion().getWorkDataDir().toFile().exists()) {
			Log.abort(this, "Data directory doesn't exist for: '%s'.", buggyEntity);
		}

		if (outputDir == null) {
			outputDir = Defects4J.getValueOf(Defects4JProperties.PLOT_DIR);
		}

		/*
		 * #====================================================================
		 * # plot a single Defects4J element
		 * #====================================================================
		 */

		String plotOutputDir = outputDir + SEP + "single" + (suffix == null ? "" : "_" + suffix) + SEP + project;
		if (normStrategy != null) {
			plotOutputDir += "_" + normStrategy;
		}

		for (String localizer : localizers) {
			Plotter.plotSingle(
					buggyEntity, suffix, localizer, ParserStrategy.NO_CHANGE, plotOutputDir, "", gp, normStrategy);
		}
	}

}
