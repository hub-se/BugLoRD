package se.de.hu_berlin.informatik.experiments.defects4j.plot;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.ToolSpecific;
import se.de.hu_berlin.informatik.faultlocalizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.gen.ranking.Spectra2Ranking;
import se.de.hu_berlin.informatik.spectra.core.ComputationStrategies;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class HyperbolicComputeSBFLRankingsEH extends AbstractProcessor<BuggyFixedEntity<?>,BuggyFixedEntity<?>> {

	final private List<IFaultLocalizer<SourceCodeBlock>> localizers;
	final private ComputationStrategies strategy;
	final private String suffix;
	private final ToolSpecific toolSpecific;
	private final String bucketPath;
	
	/**
	 * Initializes a {@link HyperbolicComputeSBFLRankingsEH} object.
	 * @param localizers
	 * the localizers to compute rankings for
	 * @param toolSpecific
	 * chooses what kind of spectra to use
	 * @param bucketPath
	 * a path to use as for storing of rankings
	 * @param suffix 
	 * a suffix to append to the ranking directory (may be null)
	 * @param strategy
	 * the strategy to use for computation of the rankings
	 */
	public HyperbolicComputeSBFLRankingsEH(List<IFaultLocalizer<SourceCodeBlock>> localizers, 
			ToolSpecific toolSpecific, String bucketPath,
			String suffix, ComputationStrategies strategy) {
		super();
		this.localizers = localizers;
		this.toolSpecific = toolSpecific;
		this.bucketPath = bucketPath;
		this.suffix = suffix;
		this.strategy = strategy;
	}

	@Override
	public BuggyFixedEntity<?> processItem(BuggyFixedEntity<?> buggyEntity) {
//		Log.out(this, "Processing %s.", buggyEntity);

		Entity bug = buggyEntity.getBuggyVersion();

		/* #====================================================================================
		 * # compute SBFL rankings for the given localizers
		 * #==================================================================================== */
		if (!(bug.getWorkDataDir().toFile()).exists()) {
			Log.err(this, "Work data directory doesn't exist: '" + bug.getWorkDataDir() + "'.");
			Log.err(this, "Error while computing SBFL rankings. Skipping '" + buggyEntity + "'.");
			return null;
		}
		
		/* #====================================================================================
		 * # calculate rankings from existing spectra file
		 * #==================================================================================== */
		String subDirName = BugLoRD.getSubDirName(toolSpecific);
		String compressedSpectraFile = BugLoRD.getSpectraFilePath(bug, subDirName).toString();
		
		if (!(new File(compressedSpectraFile)).exists()) {
			Log.err(this, "Spectra file doesn't exist: '" + compressedSpectraFile + "'.");
			Log.err(this, "Error while computing SBFL rankings. Skipping '" + buggyEntity + "'.");
			return null;
		}
		
		Path rankingDir = bug.getWorkDataDir().resolve(suffix == null ? 
				BugLoRDConstants.DIR_NAME_RANKING : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix);
		Path traceFile = rankingDir.resolve(BugLoRDConstants.getTraceFileFileName(null));
		Path metricsFile = rankingDir.resolve(BugLoRDConstants.getMetricsFileFileName(null));

		Path bucketOutput = Paths.get(bucketPath).resolve(bug.getUniqueIdentifier()).resolve(suffix == null ? 
				BugLoRDConstants.DIR_NAME_RANKING : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix);
		
		if (traceFile.toFile().exists() && metricsFile.toFile().exists()) {
			// reuse computed data for repeated computations (don't need to load the spectra again)
			Spectra2Ranking.generateRankingFromTraceFileForLocalizers(
					traceFile.toAbsolutePath().toString(),
					metricsFile.toAbsolutePath().toString(),
					bucketOutput.toString(), localizers, strategy);
		} else {
			Log.abort(this, "Trace file or metrics file not found for %s.", bug.getUniqueIdentifier());
		}

		return buggyEntity;
	}

}

