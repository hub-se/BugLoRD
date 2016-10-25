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
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.CmdOptions;
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
	public void testMainAveragePlotLarger() {
		String[] args = { 
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "largerProject",
				CmdOptions.AVERAGE_PLOT.asArg(), "tarantula",
				CmdOptions.CONNECT_POINTS.asArg(),
				CmdOptions.PLOT_ALL_AVERAGE.asArg(), "-hit",
				CmdOptions.PLOT_SINGLE_TABLES.asArg(),
				CmdOptions.AUTO_Y.asArg(),
//				CmdOptions.SHOW_PANEL.asArg(),
				CmdOptions.CSV.asArg(),
				CmdOptions.PNG.asArg(),
				CmdOptions.OUTPUT.asArg(), getStdTestDir(), "myAverageRankingLarger" };
		Plotter.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingLarger.png")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
	 */
	@Test
	public void testMainPlotAll() {
		String[] args = { 
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "someProject" + File.separator + "3b" + File.separator + "ranking",
				CmdOptions.NORMAL_PLOT.asArg(),
				CmdOptions.RANGE.asArg(), "80",
				//CmdOptions.SHOW_PANEL.asArg(),
				CmdOptions.PDF.asArg(),
				CmdOptions.PNG.asArg(),
				CmdOptions.OUTPUT.asArg(), getStdTestDir(), "myRanking" };
		Plotter.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "jaccard", "myRanking.pdf")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "jaccard", "myRanking.png")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "myRanking.pdf")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "myRanking.png")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
	 */
	@Test
	public void testMainPlotSpecifiedFolder() {
		String[] args = { 
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "someOtherProject" + File.separator + "3b" + File.separator + "ranking",
				CmdOptions.NORMAL_PLOT.asArg(), "tarantula",
				CmdOptions.RANGE.asArg(), "80",
				//CmdOptions.SHOW_PANEL.asArg(),
				CmdOptions.PDF.asArg(),
				CmdOptions.PNG.asArg(),
				CmdOptions.OUTPUT.asArg(), getStdTestDir(), "myRankingSingle" };
		Plotter.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "myRankingSingle.pdf")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "myRankingSingle.png")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
	 */
	@Test
	public void testMainPlotSpecifiedFolderIgnoreZero() {
		String[] args = { 
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "someOtherProject" + File.separator + "3b" + File.separator + "ranking",
				CmdOptions.NORMAL_PLOT.asArg(), "tarantula", 
				CmdOptions.IGNORE_ZERO.asArg(),
				CmdOptions.AUTO_Y.asArg(),
				//CmdOptions.SHOW_PANEL.asArg(),
				CmdOptions.PDF.asArg(),
				CmdOptions.PNG.asArg(),
				CmdOptions.EPS.asArg(),
				CmdOptions.SVG.asArg(),
				CmdOptions.OUTPUT.asArg(), getStdTestDir(), "myRankingSingleIgnoreZero" };
		Plotter.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "myRankingSingleIgnoreZero.pdf")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "myRankingSingleIgnoreZero.png")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "myRankingSingleIgnoreZero.eps")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "myRankingSingleIgnoreZero.svg")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
	 */
	@Test
	public void testMainPlotWrongSpecifiedFolder() {
		String[] args = { 
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "someProject" + File.separator + "3b" + File.separator + "ranking",
				CmdOptions.NORMAL_PLOT.asArg(), "dirThatNotExists",
				CmdOptions.RANGE.asArg(), "80", 
				//CmdOptions.SHOW_PANEL.asArg(),
				CmdOptions.PDF.asArg(),
				CmdOptions.PNG.asArg(),
				CmdOptions.OUTPUT.asArg(), getStdTestDir(), "myRankingSingle" };
		exit.expectSystemExitWithStatus(1);
		Plotter.main(args);
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
	 */
	@Test
	public void testMainAveragePlot() {
		String[] args = { 
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "someProject",
				CmdOptions.AVERAGE_PLOT.asArg(), "jaccard", "tarantula",
				CmdOptions.CONNECT_POINTS.asArg(),
				CmdOptions.PLOT_ALL_AVERAGE.asArg(),
				CmdOptions.PLOT_SINGLE_TABLES.asArg(),
				CmdOptions.AUTO_Y.asArg(),
//				CmdOptions.SHOW_PANEL.asArg(),
				CmdOptions.PNG.asArg(),
				CmdOptions.OUTPUT.asArg(), getStdTestDir(), "myAverageRanking" };
		Plotter.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRanking.png")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "jaccard", "jaccard_myAverageRanking.png")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
	 */
	@Test
	public void testMainAveragePlotCSV() {
		String[] args = { 
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "someProject",
				CmdOptions.AVERAGE_PLOT.asArg(), "tarantula",
				CmdOptions.RANGE.asArg(), "80", 
				CmdOptions.CONNECT_POINTS.asArg(),
				//CmdOptions.SHOW_PANEL.asArg(),
				CmdOptions.CSV.asArg(),
				CmdOptions.OUTPUT.asArg(), getStdTestDir(), "myAverageRankingCSV" };
		Plotter.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingCSV.unsignificant.csv")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingCSV.low_significance.csv")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingCSV.medium_significance.csv")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingCSV.high_significance.csv")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingCSV.crucial_significance.csv")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter#main(java.lang.String[])}.
	 */
	@Test
	public void testMainGlobalAveragePlot() {
		String[] args = { 
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "someProject",
				CmdOptions.AVERAGE_PLOT.asArg(), "jaccard", "tarantula",
				CmdOptions.RANGE.asArg(), "1", 
				CmdOptions.CSV.asArg(),
				CmdOptions.OUTPUT.asArg(), getStdTestDir(), "myAverageRanking" };
		Plotter.main(args);

		String[] args2 = { 
				CmdOptions.INPUT.asArg(), getStdTestDir(),
				CmdOptions.CSV_PLOT.asArg(), "tarantula", "jaccard",
				CmdOptions.RANGE.asArg(), "80", 
				CmdOptions.CONNECT_POINTS.asArg(),
				//CmdOptions.SHOW_PANEL.asArg(),
				CmdOptions.CSV.asArg(),
				CmdOptions.PDF.asArg(),
				CmdOptions.PNG.asArg(),
				CmdOptions.OUTPUT.asArg(), getStdTestDir(), "myAverageRankingCSV" };
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
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "someProject",
				CmdOptions.AVERAGE_PLOT.asArg(), "jaccard", "tarantula",
				CmdOptions.RANGE.asArg(), "150", 
				CmdOptions.CONNECT_POINTS.asArg(),
				CmdOptions.AUTO_Y.asArg(),
				CmdOptions.IGNORE_ZERO.asArg(),
				//CmdOptions.SHOW_PANEL.asArg(),
				CmdOptions.PDF.asArg(),
				CmdOptions.PNG.asArg(),
				CmdOptions.OUTPUT.asArg(), getStdTestDir(), "myAverageRankingIgnoreZero" };
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
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "someProject",
				CmdOptions.AVERAGE_PLOT.asArg(), "jaccard", "tarantula",
				CmdOptions.CONNECT_POINTS.asArg(),
				CmdOptions.PLOT_ALL_AVERAGE.asArg(),
				CmdOptions.PLOT_SINGLE_TABLES.asArg(),
//				CmdOptions.RANGE.asArg(), "4500", "8100",
				CmdOptions.HEIGHT.asArg(), "120",
				CmdOptions.AUTO_Y.asArg(), /*"1", "2", "3",*/
				CmdOptions.IGNORE_ZERO.asArg(),
				//CmdOptions.SHOW_PANEL.asArg(),
				CmdOptions.PDF.asArg(),
				CmdOptions.PNG.asArg(),
				CmdOptions.OUTPUT.asArg(), getStdTestDir(), "myAverageRankingIgnoreZero_single" };
		Plotter.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingIgnoreZero_single_unsignificant.pdf")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingIgnoreZero_single_low_significance.pdf")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingIgnoreZero_single_medium_significance.pdf")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingIgnoreZero_single_high_significance.pdf")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingIgnoreZero_single_crucial_significance.pdf")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tarantula", "tarantula_myAverageRankingIgnoreZero_single_all.pdf")));
	}

}
