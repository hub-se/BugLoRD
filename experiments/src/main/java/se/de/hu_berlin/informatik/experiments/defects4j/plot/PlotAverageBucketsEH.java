package se.de.hu_berlin.informatik.experiments.defects4j.plot;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JBuggyFixedEntity;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.BugLoRDProperties;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
import se.de.hu_berlin.informatik.utils.experiments.ranking.NormalizedRanking.NormalizationStrategy;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractConsumingProcessor;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs a single experiment.
 *
 * @author Simon Heiden
 */
public class PlotAverageBucketsEH extends AbstractConsumingProcessor<String> {

    private final static String SEP = File.separator;

    private final ParserStrategy strategy;
    private final String project;
    private final int threadCount;
    private final NormalizationStrategy normStrategy;

    private final String plotOutputDir;
    private final List<BuggyFixedEntity<?>>[] buckets;

    private final String suffix;

    private final static Object lock = new Object();

    final private static String[] gp = BugLoRD.getValueOf(BugLoRDProperties.RANKING_PERCENTAGES).split(" ");

    /**
     * Initializes a {@link PlotAverageBucketsEH} object with the given parameters.
     *
     * @param suffix       a suffix to append to the ranking directory (may be null)
     * @param strategy     the strategy to use when encountering equal-rank data points
     * @param seed         a seed for the random generator to generate the buckets
     * @param bc           the number of buckets to generate
     * @param project      the project
     * @param outputDir    the main plot output directory
     * @param threadCount  the number of parallel threads
     * @param normStrategy whether the rankings should be normalized before combination
     */
    public PlotAverageBucketsEH(String suffix, ParserStrategy strategy, Long seed,
                                int bc, String project, String outputDir,
                                int threadCount, NormalizationStrategy normStrategy) {
        super();
        this.suffix = suffix;
        this.strategy = strategy;
        this.project = project;
        String outputDir1 = outputDir;
        this.threadCount = threadCount;
        this.normStrategy = normStrategy;

        boolean isProject = Defects4J.validateProject(project, false);

        if (!isProject && !project.equals("super")) {
            Log.abort(this, "Project doesn't exist: '" + project + "'.");
        }

        if (outputDir1 == null) {
            outputDir1 = Defects4J.getValueOf(Defects4JProperties.PLOT_DIR);
        }

        this.plotOutputDir = generatePlotOutputDir(outputDir1, this.suffix, project, normStrategy, seed, bc);

        Path outputCsvFile = Paths.get(plotOutputDir).resolve(seed + ".csv").toAbsolutePath();


        if (outputCsvFile.toFile().exists()) {
            this.buckets = Defects4J.readBucketsFromFile(outputCsvFile);
        } else {
            //only synchronize when absolutely necessary
            synchronized (lock) {
                if (outputCsvFile.toFile().exists()) {
                    this.buckets = Defects4J.readBucketsFromFile(outputCsvFile);
                } else {
                    this.buckets = Defects4J.generateNBuckets(fillEntities(project, isProject), bc, seed, outputCsvFile);
                }
            }
        }
    }

    @Override
    public void consumeItem(String localizer) {
        File allLMRankingFileNamesFile = new File(BugLoRDConstants.LM_RANKING_FILENAMES_FILE);

        List<String> allRankingFileNames = FileUtils.readFile2List(allLMRankingFileNamesFile.toPath());

        for (String lmRankingFileName : allRankingFileNames) {
            int i = 0;
            for (List<BuggyFixedEntity<?>> bucket : buckets) {
                ++i;
                Plotter.plotAverage(bucket, suffix, localizer, lmRankingFileName, strategy,
                        plotOutputDir + SEP + "bucket_" + i,
                        project, gp, threadCount, normStrategy);
            }

            for (int j = 0; j < buckets.length; ++j) {
                Plotter.plotAverage(sumUpAllBucketsButOne(buckets, j), suffix, localizer, lmRankingFileName, strategy,
                        plotOutputDir + SEP + "bucket_" + (j + 1) + "_rest",
                        project, gp, threadCount, normStrategy);
            }
        }
    }

    private static List<BuggyFixedEntity<?>> sumUpAllBucketsButOne(List<BuggyFixedEntity<?>>[] buckets, int index) {
        List<BuggyFixedEntity<?>> list = new ArrayList<>();

        for (int i = 0; i < buckets.length; ++i) {
            if (i != index) {
                list.addAll(buckets[i]);
            }
        }

        return list;
    }

    private static BuggyFixedEntity<?>[] fillEntities(String identifier, boolean isProject) {
        BuggyFixedEntity<?>[] entities;
        if (isProject) {
            /* #====================================================================================
             * # plot averaged rankings for given project
             * #==================================================================================== */
            //iterate over all ids
            String[] ids = Defects4J.getAllBugIDs(identifier);
            entities = new BuggyFixedEntity[ids.length];
            int i = 0;
            for (String id : ids) {
                entities[i++] = new Defects4JBuggyFixedEntity(identifier, id);
            }

        } else { //given project name was "super"; iterate over all project directories
            int numberOfEntities = 0;
            //iterate over all projects
            for (String project : Defects4J.getAllProjects()) {
//				if (project.equals("Mockito")) {
//					continue;
//				}
                numberOfEntities += Defects4J.getMaxBugID(project);
            }

            entities = new BuggyFixedEntity[numberOfEntities];
            int i = 0;
            //iterate over all projects
            for (String project : Defects4J.getAllProjects()) {
//				if (project.equals("Mockito")) {
//					continue;
//				}
                String[] ids = Defects4J.getAllBugIDs(project);
                for (String id : ids) {
                    entities[i++] = new Defects4JBuggyFixedEntity(project, id);
                }
            }

        }
        return entities;
    }

    public static String generatePlotOutputDir(String outputDir, String suffix, String identifier,
                                               NormalizationStrategy normStrategy2, Long seed, int bc) {
        String plotOutputDir;
        /* #====================================================================================
         * # plot averaged rankings for given identifier (project, super, ...)
         * #==================================================================================== */
        if (normStrategy2 == null) {
            plotOutputDir = outputDir + SEP + "average" + (suffix == null ? "" : "_" + suffix)
                    + SEP + identifier + SEP + seed + SEP + bc + "_buckets_total";
        } else {
            plotOutputDir = outputDir + SEP + "average" + (suffix == null ? "" : "_" + suffix)
                    + SEP + identifier + "_" + normStrategy2 + SEP + seed + SEP + bc + "_buckets_total";
        }

        return plotOutputDir;
    }

}

