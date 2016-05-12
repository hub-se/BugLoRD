/**
 * 
 */
package se.de.hu_berlin.informatik.combranking.tests;

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

import se.de.hu_berlin.informatik.combranking.CombineSBFLandNLFLRanking;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

/**
 * @author Simon
 *
 */
public class CombineSBFLandNLFLRankingTest extends TestSettings {

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

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.combranking.CombineSBFLandNLFLRanking#main(java.lang.String[])}.
	 */
	@Test
	public void testMain() {
		String[] args = { 
				"-i", getStdResourcesDir() + File.separator + "ranking.rnk", 
				"-t", getStdResourcesDir() + File.separator + "all.trc.mrg", 
				"-g", getStdResourcesDir() + File.separator + "all.trc.mrg.sentences.ce",
				"-l", getStdResourcesDir() + File.separator + "all.trc.mrg.sentences.lce",
				"-o", getStdTestDir() };
		CombineSBFLandNLFLRanking.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "all_trc_mrg")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.combranking.CombineSBFLandNLFLRanking#main(java.lang.String[])}.
	 */
	@Test
	public void testMainGivenPercentages() {
		String[] args = { 
				"-i", getStdResourcesDir() + File.separator + "ranking.rnk", 
				"-t", getStdResourcesDir() + File.separator + "all.trc2.mrg", 
				"-g", getStdResourcesDir() + File.separator + "all.trc.mrg.sentences.ce",
				"-l", getStdResourcesDir() + File.separator + "all.trc.mrg.sentences.lce",
				"-gp", "10", "50", "0", "100",
				"-lp", "0", "100", "20",
				"-o", getStdTestDir() };
		CombineSBFLandNLFLRanking.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "all_trc2_mrg")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.combranking.CombineSBFLandNLFLRanking#main(java.lang.String[])}.
	 */
	@Test
	public void testMainWrongTraceFile() {
		String[] args = { 
				"-i", getStdResourcesDir() + File.separator + "ranking.rnk", 
				"-t", getStdResourcesDir() + File.separator + "x.trc", 
				"-g", getStdResourcesDir() + File.separator + "all.trc.mrg.sentences.ce",
				"-l", getStdResourcesDir() + File.separator + "all.trc.mrg.sentences.lce",
				"-o", getStdTestDir() };
		exit.expectSystemExitWithStatus(1);
		CombineSBFLandNLFLRanking.main(args);
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.combranking.CombineSBFLandNLFLRanking#main(java.lang.String[])}.
	 */
	@Test
	public void testMainWrongRankingFile() {
		String[] args = { 
				"-i", getStdResourcesDir() + File.separator + "ranking.rnk", 
				"-t", getStdResourcesDir() + File.separator + "all.trc.mrg", 
				"-g", getStdResourcesDir() + File.separator + "test.ce",
				"-o", getStdTestDir() };
		exit.expectSystemExitWithStatus(1);
		CombineSBFLandNLFLRanking.main(args);
	}

}
