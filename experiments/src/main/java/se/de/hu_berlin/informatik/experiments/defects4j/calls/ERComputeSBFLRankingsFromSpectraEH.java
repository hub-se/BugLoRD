package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.BugLoRDProperties;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.ToolSpecific;
import se.de.hu_berlin.informatik.gen.ranking.Spectra2Ranking;
import se.de.hu_berlin.informatik.spectra.core.ComputationStrategies;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.branch.ProgramBranch;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Runs a single experiment.
 *
 * @author Simon Heiden
 */
public class ERComputeSBFLRankingsFromSpectraEH extends AbstractProcessor<BuggyFixedEntity<?>, BuggyFixedEntity<?>> {

    final private static String[] localizers = BugLoRD.getValueOf(BugLoRDProperties.LOCALIZERS).split(" ");
    final private boolean removeIrrelevantNodes;
    final private boolean removeTestClassNodes;
    final private boolean condenseNodes;
    final private boolean forceLoadSpectra;
    final private String suffix;
    private final ToolSpecific toolSpecific;

    /**
     * Initializes a {@link ERComputeSBFLRankingsFromSpectraEH} object.
     *
     * @param toolSpecific          chooses what kind of spectra to use
     * @param suffix                a suffix to append to the ranking directory (may be null)
     * @param removeIrrelevantNodes whether to remove nodes that were not touched by any failed traces
     * @param removeTestClassNodes  whether to remove nodes that are part of test classes
     * @param condenseNodes         whether to combine several lines with equal trace involvement
     * @param forceLoadSpectra      whether the spectra file should be used regardless of whether a trace file exists
     */
    public ERComputeSBFLRankingsFromSpectraEH(ToolSpecific toolSpecific,
                                              String suffix, final boolean removeIrrelevantNodes, final boolean removeTestClassNodes,
                                              final boolean condenseNodes, boolean forceLoadSpectra) {
        super();
        this.toolSpecific = toolSpecific;
        this.suffix = suffix;
        this.removeIrrelevantNodes = removeIrrelevantNodes;
        this.removeTestClassNodes = removeTestClassNodes;
        this.condenseNodes = condenseNodes;
        this.forceLoadSpectra = forceLoadSpectra;
    }

    @Override
    public BuggyFixedEntity<?> processItem(BuggyFixedEntity<?> buggyEntity) {
        Log.out(this, "Processing %s.", buggyEntity);

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
        File compressedSpectraFile = BugLoRD.getSpectraFilePath(bug, subDirName).toAbsolutePath().toFile();

        if (!compressedSpectraFile.exists()) {
            Log.err(this, "Spectra file doesn't exist: '" + compressedSpectraFile + "'.");
            Log.err(this, "Error while computing SBFL rankings. Skipping '" + buggyEntity + "'.");
            return null;
        }

        Path rankingDir = bug.getWorkDataDir().resolve(suffix == null ?
                BugLoRDConstants.DIR_NAME_RANKING : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix);
        Path traceFile = rankingDir.resolve(BugLoRDConstants.getTraceFileFileName(null));
        Path metricsFile = rankingDir.resolve(BugLoRDConstants.getMetricsFileFileName(null));

        if (!forceLoadSpectra && traceFile.toFile().exists() && metricsFile.toFile().exists()) {
            // reuse computed data for repeated computations (don't need to load the spectra again)
            if (toolSpecific.equals(ToolSpecific.BRANCH_SPECTRA)) {
                Spectra2Ranking.generateRankingFromTraceFile(ProgramBranch.DUMMY,
                        traceFile.toAbsolutePath().toString(),
                        metricsFile.toAbsolutePath().toString(),
                        rankingDir.toString(), localizers, ComputationStrategies.STANDARD_SBFL);
            } else {
                Spectra2Ranking.generateRankingFromTraceFile(SourceCodeBlock.DUMMY,
                        traceFile.toAbsolutePath().toString(),
                        metricsFile.toAbsolutePath().toString(),
                        rankingDir.toString(), localizers, ComputationStrategies.STANDARD_SBFL);
            }
        } else {
        	// copy spectra file to execution directory for faster loading...
        	Path spectraDestination = bug.getWorkDir(true).resolve(subDirName)
                    .resolve(BugLoRDConstants.SPECTRA_FILE_NAME).toAbsolutePath();
            try {
                FileUtils.copyFileOrDir(compressedSpectraFile, spectraDestination.toFile(), StandardCopyOption.REPLACE_EXISTING);
                Log.out(this, "Copied spectra '%s' to '%s'.", compressedSpectraFile, spectraDestination);
            } catch (IOException e) {
                Log.err(this, "Found spectra '%s', but could not copy to '%s'.", compressedSpectraFile, spectraDestination);
                return null;
            }
            
            // use spectra file in execution directory
            compressedSpectraFile = spectraDestination.toFile();
            
            if (toolSpecific.equals(ToolSpecific.BRANCH_SPECTRA)) {
                if (removeIrrelevantNodes) {
//                    String compressedSpectraFileFiltered = BugLoRD.getFilteredSpectraFilePath(bug, subDirName).toString();
//
//                    if (new File(compressedSpectraFileFiltered).exists()) {
//                        Spectra2Ranking.generateRanking(ProgramBranch.DUMMY, compressedSpectraFileFiltered, rankingDir.toString(),
//                                localizers, false, removeTestClassNodes, ComputationStrategies.STANDARD_SBFL, null);
//                    } else {
                        Spectra2Ranking.generateRanking(ProgramBranch.DUMMY, compressedSpectraFile.toString(), rankingDir.toString(),
                                localizers, true, removeTestClassNodes, ComputationStrategies.STANDARD_SBFL, null);
//                    }
                } else {
                    Spectra2Ranking.generateRanking(ProgramBranch.DUMMY, compressedSpectraFile.toString(), rankingDir.toString(),
                            localizers, false, removeTestClassNodes, ComputationStrategies.STANDARD_SBFL, null);
                }
            } else {
                if (removeIrrelevantNodes) {
//                    String compressedSpectraFileFiltered = BugLoRD.getFilteredSpectraFilePath(bug, subDirName).toString();
//
//                    if (new File(compressedSpectraFileFiltered).exists()) {
//                        Spectra2Ranking.generateRanking(compressedSpectraFileFiltered, rankingDir.toString(),
//                                localizers, false, removeTestClassNodes, condenseNodes, ComputationStrategies.STANDARD_SBFL, null);
//                    } else {
                        Spectra2Ranking.generateRanking(compressedSpectraFile.toString(), rankingDir.toString(),
                                localizers, true, removeTestClassNodes, condenseNodes, ComputationStrategies.STANDARD_SBFL, null);
//                    }
                } else {
                    Spectra2Ranking.generateRanking(compressedSpectraFile.toString(), rankingDir.toString(),
                            localizers, false, removeTestClassNodes, condenseNodes, ComputationStrategies.STANDARD_SBFL, null);
                }
            }
        }

        return buggyEntity;
    }

}

