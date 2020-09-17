package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.ToolSpecific;
import se.de.hu_berlin.informatik.gen.spectra.AbstractSpectraGenerator.AbstractBuilder;
import se.de.hu_berlin.informatik.gen.spectra.main.PredicatesSpectraGenerator;
import se.de.hu_berlin.informatik.gen.spectra.predicates.mining.Miner;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.files.processors.SearchFileOrDirToListProcessor;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

/**
 * Runs a single experiment.
 *
 * @author Simon Heiden
 */
public class ERProducePredicates extends AbstractProcessor<BuggyFixedEntity<?>, BuggyFixedEntity<?>> {

    private final String suffix;
    final private int port;
    private final String subDirName;
    private final boolean fillEmptyLines;

    /**
     * @param toolSpecific   chooses what kind of tool to use to generate the spectra
     * @param suffix         a suffix to append to the ranking directory (may be null)
     * @param port           the port to use for the JaCoCo Java agent
     * @param fillEmptyLines whether to fill up empty lines between statements
     */
    public ERProducePredicates(ToolSpecific toolSpecific, String suffix, int port, boolean fillEmptyLines) {
        this.fillEmptyLines = fillEmptyLines;
        this.subDirName = "predicates"; //getSubDirName(toolSpecific);
        this.suffix = suffix;
        this.port = port;
    }

    @Override
    public BuggyFixedEntity<?> processItem(BuggyFixedEntity<?> buggyEntity) {
        Log.out(this, "Processing %s.", buggyEntity);

        Entity bug = buggyEntity.getBuggyVersion();


        /* #====================================================================================
         * # checkout buggy version if necessary
         * #==================================================================================== */
        buggyEntity.requireBug(true);

        /* #====================================================================================
         * # collect paths
         * #==================================================================================== */
        String buggyMainSrcDir = bug.getMainSourceDir(true).toString();
        String buggyMainBinDir = bug.getMainBinDir(true).toString();
        String buggyTestBinDir = bug.getTestBinDir(true).toString();
        String buggyTestCP = bug.getTestClassPath(true);

        /* #====================================================================================
         * # compile buggy version
         * #==================================================================================== */
        bug.compile(true);

        /* #====================================================================================
         * # generate predicates
         * #==================================================================================== */
        String testClasses = Misc.listToString(bug.getTestClasses(true), System.lineSeparator(), "", "");

        String testClassesFile = bug.getWorkDir(true).resolve(BugLoRDConstants.FILENAME_TEST_CLASSES).toString();
        FileUtils.delete(new File(testClassesFile));
        try {
            FileUtils.writeString2File(testClasses, new File(testClassesFile));
        } catch (IOException e) {
            Log.err(this, "IOException while trying to write to file '%s'.", testClassesFile);
            Log.err(this, "Error while checking out or generating rankings. Skipping '"
                    + buggyEntity + "'.");
            bug.tryDeleteExecutionDirectory(false);
            return null;
        }

        List<String> failingTests = bug.getFailingTests(true);


        Path rankingDir = bug.getWorkDir(true).resolve(suffix == null ?
                BugLoRDConstants.DIR_NAME_RANKING : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix);
        Path statsDirData = bug.getWorkDataDir().resolve(suffix == null ?
                BugLoRDConstants.DIR_NAME_STATS : BugLoRDConstants.DIR_NAME_STATS + "_" + suffix);

        // generate tool specific spectra
        createMajoritySpectra(1, buggyEntity, bug, buggyMainSrcDir, buggyMainBinDir,
                buggyTestBinDir, buggyTestCP, testClassesFile,
                rankingDir.resolve(subDirName), failingTests);





        String[] args = {Paths.get(rankingDir.resolve(subDirName).toString()).toString()};

        Miner.main(args);


        /* #====================================================================================
         * # cleanup
         * #==================================================================================== */

        File spectraFile = rankingDir.resolve(subDirName)
                .resolve(BugLoRDConstants.SPECTRA_FILE_NAME).toFile();
        File spectraFileFiltered = rankingDir.resolve(subDirName)
                .resolve(BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME).toFile();

        if (!spectraFile.exists()) {
            Log.err(this, "Error while generating spectra. Skipping '" + buggyEntity + "'.");
            return null;
        }

        try {
            FileUtils.copyFileOrDir(
                    spectraFile,
                    bug.getWorkDataDir().resolve(subDirName)
                            .resolve(BugLoRDConstants.SPECTRA_FILE_NAME).toFile(),
                    StandardCopyOption.REPLACE_EXISTING);
            FileUtils.delete(spectraFile);
        } catch (IOException e) {
            Log.err(this, e, "Could not copy the spectra to the data directory.");
        }

        if (spectraFileFiltered.exists()) {
            try {
                FileUtils.copyFileOrDir(
                        spectraFileFiltered,
                        bug.getWorkDataDir().resolve(subDirName)
                                .resolve(BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME).toFile(),
                        StandardCopyOption.REPLACE_EXISTING);
                FileUtils.delete(spectraFileFiltered);
            } catch (IOException e) {
                Log.err(this, e, "Could not copy the filtered spectra to the data directory.");
            }
        } else {
            Log.warn(this, "Filtered spectra file does not exist.");
        }

        try {
            List<Path> result = new SearchFileOrDirToListProcessor("**cobertura.ser", true)
                    .searchForFiles().submit(rankingDir).getResult();
            for (Path file : result) {
                FileUtils.delete(file);
            }
            List<Path> result2 = new SearchFileOrDirToListProcessor("**" + BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME, true)
                    .searchForFiles().submit(rankingDir).getResult();
            for (Path file : result2) {
                FileUtils.delete(file);
            }
            List<Path> result3 = new SearchFileOrDirToListProcessor("**instrumented", true)
                    .searchForDirectories().skipSubTreeAfterMatch().submit(rankingDir).getResult();
            for (Path dir : result3) {
                FileUtils.delete(dir);
            }

            //delete old stats data directory
            FileUtils.delete(statsDirData);
            FileUtils.copyFileOrDir(
                    rankingDir.toFile(),
                    statsDirData.toFile());
        } catch (IOException e) {
            Log.err(this, e, "Could not clean up...");
        }



        /* #====================================================================================
         * # clean up unnecessary directories (doc files, svn/git files, binary classes)
         * #==================================================================================== */
        bug.removeUnnecessaryFiles(true);


//		/* #====================================================================================
//		 * # move to archive directory, in case it differs from the execution directory
//		 * #==================================================================================== */
//		buggyEntity.tryMovingExecutionDirToArchive();
//
//		buggyEntity.tryDeleteExecutionDirectory(false);

        return buggyEntity;
    }

    private void createMajoritySpectra(int iterations,
                                       BuggyFixedEntity<?> buggyEntity, Entity bug, String buggyMainSrcDir,
                                       String buggyMainBinDir, String buggyTestBinDir, String buggyTestCP, String testClassesFile,
                                       Path rankingDir, List<String> failingTests) {
        // 1200s == 20 minutes as test timeout should be reasonable!?
        // repeat tests 2 times to generate more correct coverage data!?

        Log.out(this, "%s: Generating predicates...", buggyEntity);
        AbstractBuilder builder = new PredicatesSpectraGenerator.Builder();

        builder
                .setCustomJvmArgs(Defects4JProperties.MAIN_JVM_ARGS.getValue().split(" ", 0))
                .setCustomSmallJvmArgs(Defects4JProperties.SMALL_JVM_ARGS.getValue().split(" ", 0))
                .setJavaHome(Defects4JProperties.JAVA7_HOME.getValue())
                .setProjectDir(bug.getWorkDir(true).toString())
                .setSourceDir(buggyMainSrcDir)
                .setTestClassDir(buggyTestBinDir)
                .setTestClassPath(buggyTestCP);
//        if (bug.getUniqueIdentifier().contains("Mockito")) {
//        	builder
//        	// don't include test class binaries for Mockito?
//        	.setPathsToBinaries(bug.getWorkDir(true).resolve(buggyMainBinDir).toString());
//        } else {
        builder
                // include the test class binaries in instrumentation, because why not?...
                // for instrumentation, there are cases (i.e. Closure 154) where classes in the test class directory
                // have the same name as classes in the main class directory. Originally, Cobertura would silently
                // overwrite already instrumented classes with instrumented classes with the same name...
                // We will instead keep the first found class. This is why we put the test class directory first.
                .setPathsToBinaries(
                        //bug.getWorkDir(true).resolve(buggyTestBinDir).toString(),
                        bug.getWorkDir(true).resolve(buggyMainBinDir).toString());
//        }
        builder
                .setOutputDir(rankingDir.toString())
                .setTestClassList(testClassesFile)
                .setFailingTests(failingTests)
                .useSeparateJVM(Boolean.parseBoolean(Defects4JProperties.ALWAYS_USE_SEPJVM.getValue()))
                .useJava7only(Boolean.parseBoolean(Defects4JProperties.ALWAYS_USE_JAVA7.getValue()))
//		.setTimeout(5000L)
                .setCondenseNodes(fillEmptyLines)
                .setTimeout(Long.valueOf(Defects4JProperties.TEST_TIMEOUT.getValue()))
                .setTestRepeatCount(1)
                .setMaxErrors(Integer.parseInt(Defects4JProperties.MAX_ERRORS.getValue()))
                .setPipeBufferSize(Integer.parseInt(Defects4JProperties.PIPE_BUFFER_SIZE.getValue()));

        long startTime = new Date().getTime();

        builder.run();

        File spectraFile = rankingDir.resolve(BugLoRDConstants.SPECTRA_FILE_NAME).toFile();
        if (!spectraFile.exists()) {
            Log.err(this, "%s: Error generating spectra...", buggyEntity);
            return;
        } else {
            Log.out(this, "%s: Generating spectra was successful!", buggyEntity);
        }
        long endTime = new Date().getTime();

        Log.out(this, "%s: Execution time: %s", buggyEntity, Misc.getFormattedTimerString(endTime - startTime));

    }


}

