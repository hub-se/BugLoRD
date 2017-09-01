/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.ranking;

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
import org.junit.rules.ExpectedException;

import se.de.hu_berlin.informatik.sbfl.ranking.Coverage2Ranking;
import se.de.hu_berlin.informatik.sbfl.ranking.Coverage2Ranking.CmdOptions;
import se.de.hu_berlin.informatik.utils.miscellaneous.Abort;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

/**
 * @author Simon
 *
 */
public class Coverage2RankingTest extends TestSettings {

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
	
	@Rule
	public final ExpectedException exception = ExpectedException.none();
	
	@Rule
	public final SystemErrRule systemErrRule = new SystemErrRule().enableLog();
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.sbfl.ranking.Coverage2Ranking#main(java.lang.String[])}.
	 */
	@Test
	public void testMainRankingGeneration() {
		String[] args = { 
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "coverage-xml",
				CmdOptions.LOCALIZERS.asArg(), "tarantula", "jaccard",
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "rankings" };
		Coverage2Ranking.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "rankings", "tarantula", "ranking.rnk")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "rankings", "jaccard", "ranking.rnk")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.sbfl.ranking.Coverage2Ranking#main(java.lang.String[])}.
	 */
	@Test
	public void testMainRankingGenerationNoLocalizers() {
		String[] args = { 
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "coverage-xml", 
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "rankings" };
		;
		Coverage2Ranking.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "rankings", "spectraCompressed.zip")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.sbfl.ranking.Coverage2Ranking#main(java.lang.String[])}.
	 */
	@Test
	public void testMainRankingGenerationWrongLocalizer() {
		String[] args = { 
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "coverage-xml", 
				CmdOptions.LOCALIZERS.asArg(), "tarantulululula",
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "rankings" };
		;
		exception.expect(Abort.class);
		Coverage2Ranking.main(args);
	}

}
