/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.tests;

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

import se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

/**
 * @author Simon
 *
 */
public class TokenizeLinesTest extends TestSettings {

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
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines#main(java.lang.String[])}.
	 */
	@Test
	public void testMainSyntax() {
		String[] args = {
				"-s", getStdResourcesDir(),  
				"-t", getStdResourcesDir() + File.separator + "LocalizedFormats.xml.trc",
				"-o", getStdTestDir() + File.separator + "LocalizedFormats.xml.trc.sentences",
				"-w" };
		TokenizeLines.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "LocalizedFormats.xml.trc.sentences")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines#main(java.lang.String[])}.
	 */
	@Test
	public void testMainSemantic() {
		String[] args = {
				"-s", getStdResourcesDir(),  
				"-t", getStdResourcesDir() + File.separator + "LocalizedFormats.xml.trc",
				"-o", getStdTestDir() + File.separator + "LocalizedFormats.xml.trc.sem.sentences",
				"-strat", "SEMANTIC",
//				"-m",
				"-c",
				"-w" };
		TokenizeLines.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "LocalizedFormats.xml.trc.sem.sentences")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines#main(java.lang.String[])}.
	 */
	@Test
	public void testMainMerge() {
		String[] args = {
				"-s", getStdResourcesDir(),  
				"-t", getStdResourcesDir(),
				"-o", getStdTestDir() + File.separator + "all.trc.mrg.sentences",
				"-w" };
		TokenizeLines.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "all.trc.mrg.sentences")));
		assertTrue(Files.exists(Paths.get(getStdResourcesDir(), "all.trc.mrg")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines#main(java.lang.String[])}.
	 */
	@Test
	public void testMainWithContext() {
		String[] args = {
				"-s", getStdResourcesDir(),  
				"-t", getStdResourcesDir() + File.separator + "LocalizedFormats.xml.trc",
				"-o", getStdTestDir() + File.separator + "LocalizedFormats.xml.trc.context.sentences",
				"-c", "-w" };
		TokenizeLines.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "LocalizedFormats.xml.trc.context.sentences")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines#main(java.lang.String[])}.
	 */
	@Test
	public void testMainLookAhead() {
		String[] args = {
				"-s", getStdResourcesDir(),  
				"-t", getStdResourcesDir() + File.separator + "LocalizedFormats.xml.trc",
				"-o", getStdTestDir() + File.separator + "LocalizedFormats.xml.trc.la.sentences",
				"-l",
				"-w" };
		TokenizeLines.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "LocalizedFormats.xml.trc.la.sentences")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines#main(java.lang.String[])}.
	 */
	@Test
	public void testMainWithContextLookAhead() {
		String[] args = {
				"-s", getStdResourcesDir(),  
				"-t", getStdResourcesDir() + File.separator + "LocalizedFormats.xml.trc",
				"-o", getStdTestDir() + File.separator + "LocalizedFormats.xml.trc.context.la.sentences",
				"-l",
				"-c", "-w" };
		TokenizeLines.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "LocalizedFormats.xml.trc.context.la.sentences")));
	}

}
