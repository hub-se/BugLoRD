package se.de.hu_berlin.informatik.gen.ranking;

import org.junit.*;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.rules.ExpectedException;
import se.de.hu_berlin.informatik.gen.ranking.Spectra2Ranking.CmdOptions;
import se.de.hu_berlin.informatik.utils.miscellaneous.Abort;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

/**
 * @author Simon
 *
 */
public class Spectra2RankingTest extends TestSettings {

	/**
     */
	@BeforeClass
	public static void setUpBeforeClass() {
	}

	/**
     */
	@AfterClass
	public static void tearDownAfterClass() {
//		deleteTestOutputs();
	}

	/**
     */
	@Before
	public void setUp() {
	}

	/**
     */
	@After
	public void tearDown() {
//		deleteTestOutputs();
	}
	
	@Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
	
	@Rule
	public final ExpectedException exception = ExpectedException.none();
	
	@Rule
	public final SystemErrRule systemErrRule = new SystemErrRule().enableLog();

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.gen.ranking.Spectra2Ranking#main(java.lang.String[])}.
	 */
	@Test
	public void testMainRankingGeneration() {
		String[] args = { 
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "spectraCompressed.zip", 
				CmdOptions.LOCALIZERS.asArg(), "tarantula", "jaccard",
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "rankings" };
		Spectra2Ranking.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "rankings", "tarantula", "ranking.rnk")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "rankings", "jaccard", "ranking.rnk")));
	}

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.gen.ranking.Spectra2Ranking#main(java.lang.String[])}.
	 */
	@Test
	public void testNessaRankingGeneration() {
		String[] args = {
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "Math-84b.zip",
				CmdOptions.LOCALIZERS.asArg(), "tarantula", "jaccard", "nessa",
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "rankingsNGram"};
		Spectra2Ranking.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "rankingsNGram", "tarantula", "ranking.rnk")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "rankingsNGram", "jaccard", "ranking.rnk")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "rankingsNGram", "nessa", "ranking.rnk")));
	}

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.gen.ranking.Spectra2Ranking#main(java.lang.String[])}.
	 */
	@Test
	public void testNessa2true() {
		String[] args = {
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "Math-84b.zip",
				CmdOptions.LOCALIZERS.asArg(), "nessa-2-true",
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "nessa-2-true"};
		Spectra2Ranking.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "nessa-2-true", "nessa", "ranking.rnk")));
	}

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.gen.ranking.Spectra2Ranking#main(java.lang.String[])}.
	 */
	@Test
	public void testNessa204() {
		String[] args = {
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "Math-84b.zip",
				CmdOptions.LOCALIZERS.asArg(), "nessa-2-0.4",
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "nessa-2-04"};
		Spectra2Ranking.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "nessa-2-04", "nessa", "ranking.rnk")));
	}

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.gen.ranking.Spectra2Ranking#main(java.lang.String[])}.
	 */
	@Test
	public void testNessa304() {
		String[] args = {
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "Math-84b.zip",
				CmdOptions.LOCALIZERS.asArg(), "nessa-3-0.4",
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "nessa-3-04"};
		Spectra2Ranking.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "nessa-3-04", "nessa", "ranking.rnk")));
	}

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.gen.ranking.Spectra2Ranking#main(java.lang.String[])}.
	 */
	@Test
	public void testNessa209() {
		String[] args = {
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "Math-84b.zip",
				CmdOptions.LOCALIZERS.asArg(), "nessa-2-0.9",
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "nessa-2-09"};
		Spectra2Ranking.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "nessa-2-09", "nessa", "ranking.rnk")));
	}

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.gen.ranking.Spectra2Ranking#main(java.lang.String[])}.
	 */
	@Test
	public void testNessa3true() {
		String[] args = {
                CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "Chart-26b.zip",
				CmdOptions.LOCALIZERS.asArg(), "nessa-3-true",
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "nessa-3-true"};
		Spectra2Ranking.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "nessa-3-true", "nessa", "ranking.rnk")));
	}

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.gen.ranking.Spectra2Ranking#main(java.lang.String[])}.
	 */
	@Test
	public void testMainRankingGenerationSimilaritySBFL() {
		String[] args = {
				CmdOptions.SIMILARITY_SBFL.asArg(),
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "spectraCompressed.zip", 
				CmdOptions.LOCALIZERS.asArg(), "tarantula", "jaccard",
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "rankingsSim" };
		Spectra2Ranking.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "rankingsSim", "tarantula", "ranking.rnk")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "rankingsSim", "jaccard", "ranking.rnk")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.gen.ranking.Spectra2Ranking#main(java.lang.String[])}.
	 */
	@Test
	public void testMainRankingGenerationWrongFile() {
		String[] args = { 
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "testclasses.txt", 
				CmdOptions.LOCALIZERS.asArg(), "tarantula", "jaccard",
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "rankings" };
		exception.expect(Abort.class);
		Spectra2Ranking.main(args);
	}
	
}
