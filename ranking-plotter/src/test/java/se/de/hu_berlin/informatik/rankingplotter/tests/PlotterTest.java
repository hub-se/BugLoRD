/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.tests;

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

import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter;
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
//		deleteTestOutputs();
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
//		deleteTestOutputs();
	}

	@Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
	 */
	@Test
	public void testMainPlotAll() {
		String[] args = { 
				"-i", getStdResourcesDir() + File.separator + "someProject" + File.separator + "ranking",
				"-p",
				"-r", "80", 
				"-u", getStdResourcesDir() + File.separator + "someProject" + File.separator + "unranked_mod_lines",
				"-n",
				//"-s",
				"-pdf",
				"-png",
				"-o", getStdTestDir(), "myRanking" };
		Plotter.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "jaccard", "myRanking_traceFile1.pdf")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "jaccard", "myRanking_traceFile1.png")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "myRanking_traceFile2.pdf")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "myRanking_traceFile2.png")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
	 */
	@Test
	public void testMainPlotSpecifiedFolder() {
		String[] args = { 
				"-i", getStdResourcesDir() + File.separator + "someOtherProject" + File.separator + "ranking",
				"-p", "tarantula",
				"-r", "80", 
				"-u", getStdResourcesDir() + File.separator + "someOtherProject" + File.separator + "unranked_mod_lines",
				"-n",
				//"-s",
				"-pdf",
				"-png",
				"-o", getStdTestDir(), "myRankingSingle" };
		Plotter.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "myRankingSingle_traceFile2.pdf")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "myRankingSingle_traceFile2.png")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
	 */
	@Test
	public void testMainPlotSpecifiedFolderIgnoreZero() {
		String[] args = { 
				"-i", getStdResourcesDir() + File.separator + "someOtherProject" + File.separator + "ranking",
				"-p", "tarantula", 
				"-u", getStdResourcesDir() + File.separator + "someOtherProject" + File.separator + "unranked_mod_lines",
				"-n",
				"-zero",
				"-autoY",
				//"-s",
				"-pdf",
				"-png",
				"-eps",
				"-svg",
				"-o", getStdTestDir(), "myRankingSingleIgnoreZero" };
		Plotter.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "myRankingSingleIgnoreZero_traceFile2.pdf")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "myRankingSingleIgnoreZero_traceFile2.png")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "myRankingSingleIgnoreZero_traceFile2.eps")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "myRankingSingleIgnoreZero_traceFile2.svg")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
	 */
	@Test
	public void testMainPlotWrongSpecifiedFolder() {
		String[] args = { 
				"-i", getStdResourcesDir() + File.separator + "someProject" + File.separator + "ranking",
				"-p", "dirThatNotExists",
				"-r", "80", 
				"-u", getStdResourcesDir() + File.separator + "someProject" + File.separator + "unranked_mod_lines",
				"-n",
				//"-s",
				"-pdf",
				"-png",
				"-o", getStdTestDir(), "myRankingSingle" };
		exit.expectSystemExitWithStatus(1);
		Plotter.main(args);
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
	 */
	@Test
	public void testMainAveragePlot() {
		String[] args = { 
				"-i", getStdResourcesDir(),
				"-a", "jaccard", "tarantula",
				"-n",
				"-c",
				"-all",
				"-single",
				"-autoY",
				//"-s",
				"-pdf",
				"-png",
				"-o", getStdTestDir(), "myAverageRanking" };
		Plotter.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRanking.pdf")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "jaccard", "jaccard_myAverageRanking.pdf")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
	 */
	@Test
	public void testMainAveragePlotCSV() {
		String[] args = { 
				"-i", getStdResourcesDir(),
				"-a", "tarantula",
				"-r", "80", 
				"-n",
				"-c",
				//"-s",
				"-csv",
				"-o", getStdTestDir(), "myAverageRankingCSV" };
		Plotter.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingCSV.appends.csv")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingCSV.changes.csv")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingCSV.deletes.csv")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingCSV.neighbors.csv")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
	 */
	@Test
	public void testMainGlobalAveragePlot() {
		String[] args = { 
				"-i", getStdResourcesDir(),
				"-a", "jaccard", "tarantula",
				"-r", "1", 
				"-n",
				"-csv",
				"-o", getStdTestDir(), "myAverageRanking" };
		Plotter.main(args);

		String[] args2 = { 
				"-i", getStdTestDir(),
				"-g", "tarantula", "jaccard",
				"-r", "80", 
				"-n",
				"-c",
				//"-s",
				"-csv",
				"-pdf",
				"-png",
				"-o", getStdTestDir(), "myAverageRankingCSV" };
		Plotter.main(args2);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "jaccard", "jaccard_myAverageRankingCSV.png")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "jaccard", "jaccard_myAverageRankingCSV.pdf")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingCSV.pdf")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingCSV.pdf")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
	 */
	@Test
	public void testMainAveragePlotIgnoreZero() {
		String[] args = { 
				"-i", getStdResourcesDir(),
				"-a", "jaccard", "tarantula",
				"-r", "150", 
				"-n",
				"-c",
				"-autoY",
				"-zero",
				//"-s",
				"-pdf",
				"-png",
				"-o", getStdTestDir(), "myAverageRankingIgnoreZero" };
		Plotter.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingIgnoreZero.pdf")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "jaccard", "jaccard_myAverageRankingIgnoreZero.pdf")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
	 */
	@Test
	public void testMainAveragePlotIgnoreZeroSinglePlots() {
		String[] args = { 
				"-i", getStdResourcesDir(),
				"-a", "jaccard", "tarantula",
				"-n",
				"-c",
				"-all",
				"-single",
				"-r", "4500", "8100",
				"-height", "120",
				"-autoY", /*"1", "2", "3",*/
//				"-zero",
				//"-s",
				"-pdf",
				"-png",
				"-o", getStdTestDir(), "myAverageRankingIgnoreZero_single" };
		Plotter.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingIgnoreZero_single_appends.pdf")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingIgnoreZero_single_changes.pdf")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingIgnoreZero_single_deletes.pdf")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingIgnoreZero_single_neighbors.pdf")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingIgnoreZero_single_all.pdf")));
	}

}
