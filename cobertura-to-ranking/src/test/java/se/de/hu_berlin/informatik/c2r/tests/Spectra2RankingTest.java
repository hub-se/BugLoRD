/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.tests;

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
import org.junit.contrib.java.lang.system.SystemErrRule;

import se.de.hu_berlin.informatik.c2r.Spectra2Ranking;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

/**
 * @author Simon
 *
 */
public class Spectra2RankingTest extends TestSettings {

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
	public final SystemErrRule systemErrRule = new SystemErrRule().enableLog();

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.c2r.Spectra2Ranking#main(java.lang.String[])}.
	 */
	@Test
	public void testMainRankingGeneration() {
		String[] args = { 
				"-i", getStdResourcesDir() + File.separator + "spectraCompressed.zip", 
				"-l", "tarantula", "jaccard",
				"-o", getStdTestDir() + File.separator + "rankings" };
		Spectra2Ranking.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "rankings", "tarantula", "ranking.rnk")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "rankings", "jaccard", "ranking.rnk")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.c2r.Spectra2Ranking#main(java.lang.String[])}.
	 */
	@Test
	public void testMainRankingGenerationWrongFile() {
		String[] args = { 
				"-i", getStdResourcesDir() + File.separator + "testclasses.txt", 
				"-l", "tarantula", "jaccard",
				"-o", getStdTestDir() + File.separator + "rankings" };
		exit.expectSystemExitWithStatus(1);
		Spectra2Ranking.main(args);
	}
	
}
