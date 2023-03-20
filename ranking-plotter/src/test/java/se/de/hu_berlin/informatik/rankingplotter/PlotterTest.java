package se.de.hu_berlin.informatik.rankingplotter;

import org.junit.*;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.ExpectedException;
import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.CmdOptions;
import se.de.hu_berlin.informatik.utils.miscellaneous.Abort;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

/**
 * @author Simon
 */
public class PlotterTest extends TestSettings {

    /**
     *
     */
    @BeforeClass
    public static void setUpBeforeClass() {
    }

    /**
     *
     */
    @AfterClass
    public static void tearDownAfterClass() {
        deleteTestOutputs();
    }

    /**
     *
     */
    @Before
    public void setUp() {
    }

    /**
     *
     */
    @After
    public void tearDown() {
        deleteTestOutputs();
    }

//    @Rule
//    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
//
//    @Rule
//    public final ExpectedException exception = ExpectedException.none();

    /**
     * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
     */
    @Test
    public void testMainAveragePlotLarger() {
//		Log.off();
        String[] args = {
                CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "largerProject",
                CmdOptions.AVERAGE_PLOT.asArg(), "tarantula",
                CmdOptions.OUTPUT.asArg(), getStdTestDir(), "myAverageRankingLarger"};
        Plotter.main(args);
        assertTrue(Files.exists(Paths.get(getStdTestDir(), ".lm_ranking_tmp", "tarantula", "tarantula_.lm_ranking_tmp_myAverageRankingLarger_meanRanks.csv")));
        assertTrue(Files.exists(Paths.get(getStdTestDir(), ".lm_ranking_tmp", "tarantula", "tarantula_.lm_ranking_tmp_myAverageRankingLarger_MR.csv")));
    }

    /**
     * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
     */
    @Test
    public void testMainPlotAll() {
//		Log.off();
        String[] args = {
                CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "someProject"
                + File.separator + "3" + File.separator + BugLoRDConstants.DATA_DIR_NAME,
                CmdOptions.NORMAL_PLOT.asArg(),
                CmdOptions.OUTPUT.asArg(), getStdTestDir(), "myRanking"};
        Plotter.main(args);
        assertTrue(Files.exists(Paths.get(getStdTestDir(), ".lm_ranking_tmp", "jaccard", "3", "myRanking_MODINSERT.csv")));
        assertTrue(Files.exists(Paths.get(getStdTestDir(), ".lm_ranking_tmp", "tarantula", "3", "myRanking_MODINSERT.csv")));
    }

    /**
     * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
     */
    @Test
    public void testMainPlotSpecifiedFolder() {
//		Log.off();
        String[] args = {
                CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "someOtherProject"
                + File.separator + "3" + File.separator + BugLoRDConstants.DATA_DIR_NAME,
                CmdOptions.NORMAL_PLOT.asArg(), "tarantula",
                CmdOptions.OUTPUT.asArg(), getStdTestDir(), "myRankingSingle"};
        Plotter.main(args);
        assertTrue(Files.exists(Paths.get(getStdTestDir(), ".lm_ranking_tmp", "tarantula", "3", "myRankingSingle_MODINSERT.csv")));
        assertTrue(Files.exists(Paths.get(getStdTestDir(), ".lm_ranking_tmp", "tarantula", "3", "myRankingSingle_ALL.csv")));
    }

    /**
     * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
     */
    @Test
    public void testMainPlotWrongSpecifiedFolder() {
        Log.off();
        String[] args = {
                CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "someProject"
                + File.separator + "3" + File.separator + BugLoRDConstants.DATA_DIR_NAME,
                CmdOptions.NORMAL_PLOT.asArg(), "dirThatNotExists",
                CmdOptions.OUTPUT.asArg(), getStdTestDir(), "myRankingSingle"};
//        exception.expect(Abort.class);
        try {
        	Plotter.main(args);
        	assertTrue(false);
        } catch (Abort e) {
        	// this is correct
        }
    }

    /**
     * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
     */
    @Test
    public void testMainAveragePlot() {
//		Log.off();
        String[] args = {
                CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "someProject",
                CmdOptions.AVERAGE_PLOT.asArg(), "jaccard", "tarantula",
                CmdOptions.OUTPUT.asArg(), getStdTestDir(), "myAverageRanking"};
        Plotter.main(args);
        assertTrue(Files.exists(Paths.get(getStdTestDir(), ".lm_ranking_tmp", "tarantula", "tarantula_.lm_ranking_tmp_myAverageRanking_meanRanks.csv")));
        assertTrue(Files.exists(Paths.get(getStdTestDir(), ".lm_ranking_tmp", "jaccard", "jaccard_.lm_ranking_tmp_myAverageRanking_meanRanks.csv")));
        assertTrue(Files.exists(Paths.get(getStdTestDir(), ".lm_ranking_tmp", "tarantula", "tarantula_.lm_ranking_tmp_myAverageRanking_minRanks.csv")));
        assertTrue(Files.exists(Paths.get(getStdTestDir(), ".lm_ranking_tmp", "jaccard", "jaccard_.lm_ranking_tmp_myAverageRanking_minRanks.csv")));
    }

    /**
     * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
     */
    @Test
    public void testMainAveragePlotCSV() {
//		Log.off();
        String[] args = {
                CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "someProject",
                CmdOptions.AVERAGE_PLOT.asArg(), "tarantula",
                CmdOptions.OUTPUT.asArg(), getStdTestDir(), "myAverageRanking"};
        Plotter.main(args);
        assertTrue(Files.exists(Paths.get(getStdTestDir(), ".lm_ranking_tmp", "tarantula", "tarantula_.lm_ranking_tmp_myAverageRanking_MR.csv")));
        assertTrue(Files.exists(Paths.get(getStdTestDir(), ".lm_ranking_tmp", "tarantula", "tarantula_.lm_ranking_tmp_myAverageRanking_MFR.csv")));

        Plotter.plotFromCSV("tarantula", getStdTestDir(),
                getStdTestDir(), "myAverageRankingCSV");

        assertTrue(Files.exists(Paths.get(getStdTestDir(), "_latex", "tarantula_myAverageRankingCSV_MR.tex")));
        assertTrue(Files.exists(Paths.get(getStdTestDir(), "_latex", "tarantula_myAverageRankingCSV_MFR.tex")));
    }


}
