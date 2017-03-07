/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.ExpectedException;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.CmdOptions;
import se.de.hu_berlin.informatik.utils.miscellaneous.Abort;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

/**
 * @author Simon
 *
 */
public class PlotterTest extends TestSettings {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		deleteTestOutputs();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		deleteTestOutputs();
	}

	@Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
	
	@Rule
	public final ExpectedException exception = ExpectedException.none();
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
	 */
	@Test
	public void testMainAveragePlotLarger() {
		Log.off();
		String[] args = { 
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "largerProject",
				CmdOptions.AVERAGE_PLOT.asArg(), "tarantula",
				CmdOptions.OUTPUT.asArg(), getStdTestDir(), "myAverageRankingLarger" };
		Plotter.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingLarger_meanRanks.csv")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingLarger_MR.csv")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
	 */
	@Test
	public void testMainPlotAll() {
		Log.off();
		String[] args = { 
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "someProject" 
		+ File.separator + "3" + File.separator + BugLoRDConstants.DATA_DIR_NAME,
				CmdOptions.NORMAL_PLOT.asArg(), 
				CmdOptions.OUTPUT.asArg(), getStdTestDir(), "myRanking" };
		Plotter.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "jaccard", "3", "myRanking_SIGLOW.csv")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "3", "myRanking_SIGLOW.csv")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
	 */
	@Test
	public void testMainPlotSpecifiedFolder() {
		Log.off();
		String[] args = { 
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "someOtherProject" 
		+ File.separator + "3" + File.separator + BugLoRDConstants.DATA_DIR_NAME,
				CmdOptions.NORMAL_PLOT.asArg(), "tarantula",
				CmdOptions.OUTPUT.asArg(), getStdTestDir(), "myRankingSingle" };
		Plotter.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "3", "myRankingSingle_SIGLOW.csv")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "3", "myRankingSingle_SIGMEDIUM.csv")));
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
				CmdOptions.OUTPUT.asArg(), getStdTestDir(), "myRankingSingle" };
		exception.expect(Abort.class);
		Plotter.main(args);
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
				CmdOptions.OUTPUT.asArg(), getStdTestDir(), "myAverageRanking" };
		Plotter.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRanking_meanRanks.csv")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "jaccard", "jaccard_myAverageRanking_meanRanks.csv")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRanking_minRanks.csv")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "jaccard", "jaccard_myAverageRanking_minRanks.csv")));
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
				CmdOptions.OUTPUT.asArg(), getStdTestDir(), "myAverageRanking" };
		Plotter.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRanking_MR.csv")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRanking_MFR.csv")));
		
		Plotter.plotFromCSV("tarantula", getStdTestDir(), 
				getStdTestDir(), "myAverageRankingCSV");
		
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "_latex", "tarantula_myAverageRankingCSV_MR.tex")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "_latex", "tarantula_myAverageRankingCSV_MFR.tex")));
	}
	

}
