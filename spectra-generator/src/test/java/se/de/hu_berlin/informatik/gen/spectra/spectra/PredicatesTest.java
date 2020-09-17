package se.de.hu_berlin.informatik.gen.spectra.spectra;

import org.junit.Test;
import se.de.hu_berlin.informatik.gen.spectra.main.PredicatesSpectraGenerator;
import se.de.hu_berlin.informatik.gen.spectra.predicates.mining.Miner;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PredicatesTest extends TestSettings {

    private static final String GC = "-XX:+UseG1GC";

    private static final String MAX_SMALL_HEAP = "-Xmx1g";

    private static final String MAX_HEAP = "-Xmx2g";
    private static final String extraTestOutput = "target" + File.separator + "testoutputPredicates";

    private static final String extraTestClassPath = "";

    private void testOnProjectWithTestList(TestProject project, String outputDirName,
                                           long timeout, int testrepeatCount, boolean fullSpectra,
                                           boolean separateJVM, boolean useJava7, boolean successful, String testListPath) {
        Path profilesFile = Paths.get(extraTestOutput, outputDirName, "Profiles.csv");

        FileUtils.delete(profilesFile);
        long startTime = new Date().getTime();
        new PredicatesSpectraGenerator.Builder()
                .setProjectDir(project.getProjectMainDir())
                .setSourceDir(project.getSrcDir())
                .setTestClassDir(project.getBinTestDir())
                .setTestClassPath(project.getTestCP())
                .setPathsToBinaries(project.getBinDir())
                .setOutputDir(extraTestOutput + File.separator + outputDirName)
//		.setOutputDir(Paths.get("src","test","resources","Lang10b").toString())
                .setTestList(testListPath)
                .setFailingTests(project.getFailingTests())
                .useFullSpectra(fullSpectra)
                .useSeparateJVM(separateJVM)
                .useJava7only(useJava7)
                .setTimeout(timeout)
                .setTestRepeatCount(testrepeatCount)
                .setCustomJvmArgs(new String[]{MAX_HEAP, GC, "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5009"})
                .setCustomSmallJvmArgs(new String[]{MAX_SMALL_HEAP, GC, "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5010"})
                .run();
        long endTime = new Date().getTime();

        System.out.println("Execution time: " + Misc.getFormattedTimerString(endTime - startTime));

        if (successful) {
            assertTrue(Files.exists(profilesFile));
            //checkTraceSpectra(spectraZipFile);
        } else {
            assertFalse(Files.exists(profilesFile));
        }
    }
    private void testOnProject(TestProject project, String outputDirName,
                               long timeout, int testrepeatCount, boolean fullSpectra,
                               boolean separateJVM, boolean useJava7, boolean successful) {

        Path profilesFile = Paths.get(extraTestOutput, outputDirName, "Profiles.csv");

        FileUtils.delete(profilesFile);
        long startTime = new Date().getTime();
        new PredicatesSpectraGenerator.Builder()
                .setProjectDir(project.getProjectMainDir())
                .setSourceDir(project.getSrcDir())
                .setTestClassDir(project.getBinTestDir())
                .setTestClassPath(project.getTestCP())
                .setPathsToBinaries(project.getBinDir())
                .setOutputDir(extraTestOutput + File.separator + outputDirName)
                .setTestClassList(project.getTestClassListPath())
                .setFailingTests(project.getFailingTests())
                .useFullSpectra(fullSpectra)
                .useSeparateJVM(separateJVM)
                .useJava7only(useJava7)
                .setTimeout(timeout)
                .setTestRepeatCount(testrepeatCount)
                .setCustomJvmArgs(new String[]{MAX_HEAP, GC})
                .setCustomSmallJvmArgs(new String[]{MAX_SMALL_HEAP, GC})
                .run();
        long endTime = new Date().getTime();

        System.out.println("Execution time: " + Misc.getFormattedTimerString(endTime - startTime));

        if (successful) {
            assertTrue(Files.exists(profilesFile));
        } else {
            assertFalse(Files.exists(profilesFile));
        }
    }

    @Test
    public void testGenerateRankingForLang10TestListSmall() {
        // org.apache.commons.lang3.time.FastDateParser, counter ID 166, line 399
        String outputDirName = "reportLang10bTestListSmall";
        testOnProjectWithTestList(new TestProjects.Lang10b(), outputDirName,
                10000L, 1, false, false, false, true, "lang10testsSmall.txt");
    }

    @Test
    public void testGenerateRankingAndMineForLang10TestListSmall() {
        // org.apache.commons.lang3.time.FastDateParser, counter ID 166, line 399
        String outputDirName = "reportLang10bTestListSmall";
        testOnProjectWithTestList(new TestProjects.Lang10b(), outputDirName,
                10000L, 1, false, false, false, true, "lang10tests.txt");
        String[] args = {Paths.get(extraTestOutput, outputDirName).toString()};

        Miner.main(args);
    }
    @Test
    public void testGenerateRankingAndMineForTime3bTest() {
        // org.apache.commons.lang3.time.FastDateParser, counter ID 166, line 399
        String outputDirName = "reportTime3bTest";
        testOnProject(new TestProjects.Time3b(), outputDirName,
                10000L, 1, false, false, false, true);
        String[] args = {Paths.get(extraTestOutput, outputDirName).toString()};

        //Miner.main(args);
    }

    //needs old  results in folder
    @Test
    public void MineForLang10TestListSmall() {
        // org.apache.commons.lang3.time.FastDateParser, counter ID 166, line 399
        String outputDirName = "reportLang10bTestListSmall";
        String[] args = {Paths.get(extraTestOutput, outputDirName).toString()};
        Miner.main(args);
    }
}
