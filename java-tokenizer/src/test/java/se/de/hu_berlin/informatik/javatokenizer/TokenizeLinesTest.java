/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer;

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
import se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines.CmdOptions;
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
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines#main(java.lang.String[])}.
	 */
	@Test
	public void testMainSyntax() {
		String[] args = {
				CmdOptions.SOURCE_PATH.asArg(), getStdResourcesDir(),  
				CmdOptions.TRACE_FILE.asArg(), getStdResourcesDir() + File.separator + "LocalizedFormats.xml.trc",
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "LocalizedFormats.xml.trc.sentences",
				CmdOptions.OVERWRITE.asArg() };
		TokenizeLines.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "LocalizedFormats.xml.trc.sentences")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines#main(java.lang.String[])}.
	 */
	@Test
	public void testMainSemantic() {
		String[] args = {
				CmdOptions.SOURCE_PATH.asArg(), getStdResourcesDir(),  
				CmdOptions.TRACE_FILE.asArg(), getStdResourcesDir() + File.separator + "LocalizedFormats.xml.trc",
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "LocalizedFormats.xml.trc.sem.sentences",
				CmdOptions.STRATEGY.asArg(), "SEMANTIC",
				CmdOptions.ABSTRACTION_DEPTH.asArg(), "4",
//				CmdOptions.START_METHODS.asArg(),
				CmdOptions.INCLUDE_PARENT.asArg(),
				CmdOptions.CONTEXT.asArg(), "10",
				CmdOptions.OVERWRITE.asArg() };
		TokenizeLines.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "LocalizedFormats.xml.trc.sem.sentences")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines#main(java.lang.String[])}.
	 */
	@Test
	public void testMainSemantic3() {
		String[] args = {
				CmdOptions.SOURCE_PATH.asArg(), getStdResourcesDir(),  
				CmdOptions.TRACE_FILE.asArg(), getStdResourcesDir() + File.separator + "test.trc",
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "test.sem.sentences",
				CmdOptions.STRATEGY.asArg(), "SEMANTIC",
				CmdOptions.ABSTRACTION_DEPTH.asArg(), "2",
				CmdOptions.START_METHODS.asArg(),
				CmdOptions.CONTEXT.asArg(), "10",
				CmdOptions.OVERWRITE.asArg() };
		TokenizeLines.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "test.sem.sentences")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines#main(java.lang.String[])}.
	 */
	@Test
	public void testMainSemantic2() {
		String[] args = {
				CmdOptions.SOURCE_PATH.asArg(), getStdResourcesDir(),  
				CmdOptions.TRACE_FILE.asArg(), getStdResourcesDir() + File.separator + "SystemUtils.trc",
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "SystemUtils.trc.sem.sentences",
				CmdOptions.STRATEGY.asArg(), "SEMANTIC_LONG",
				CmdOptions.ABSTRACTION_DEPTH.asArg(), "3",
//				CmdOptions.START_METHODS.asArg(),
				CmdOptions.CONTEXT.asArg(), "10",
				CmdOptions.OVERWRITE.asArg() };
		TokenizeLines.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "SystemUtils.trc.sem.sentences")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines#main(java.lang.String[])}.
	 */
	@Test
	public void testMainMerge() {
		String[] args = {
				CmdOptions.SOURCE_PATH.asArg(), getStdResourcesDir(),  
				CmdOptions.TRACE_FILE.asArg(), getStdResourcesDir(),
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "all.trc.mrg.sentences",
				CmdOptions.OVERWRITE.asArg() };
		TokenizeLines.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "all.trc.mrg.sentences")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "all.trc.mrg")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines#main(java.lang.String[])}.
	 */
	@Test
	public void testMainWithContext() {
		String[] args = {
				CmdOptions.SOURCE_PATH.asArg(), getStdResourcesDir(),  
				CmdOptions.TRACE_FILE.asArg(), getStdResourcesDir() + File.separator + "LocalizedFormats.xml.trc",
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "LocalizedFormats.xml.trc.context.sentences",
				CmdOptions.CONTEXT.asArg(), CmdOptions.OVERWRITE.asArg() };
		TokenizeLines.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "LocalizedFormats.xml.trc.context.sentences")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines#main(java.lang.String[])}.
	 */
	@Test
	public void testMainLookAhead() {
		String[] args = {
				CmdOptions.SOURCE_PATH.asArg(), getStdResourcesDir(),  
				CmdOptions.TRACE_FILE.asArg(), getStdResourcesDir() + File.separator + "LocalizedFormats.xml.trc",
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "LocalizedFormats.xml.trc.la.sentences",
				CmdOptions.LOOK_AHEAD.asArg(),
				CmdOptions.OVERWRITE.asArg() };
		TokenizeLines.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "LocalizedFormats.xml.trc.la.sentences")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines#main(java.lang.String[])}.
	 */
	@Test
	public void testMainWithContextLookAhead() {
		String[] args = {
				CmdOptions.SOURCE_PATH.asArg(), getStdResourcesDir(),  
				CmdOptions.TRACE_FILE.asArg(), getStdResourcesDir() + File.separator + "LocalizedFormats.xml.trc",
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "LocalizedFormats.xml.trc.context.la.sentences",
				CmdOptions.LOOK_AHEAD.asArg(),
				CmdOptions.CONTEXT.asArg(), CmdOptions.OVERWRITE.asArg() };
		TokenizeLines.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "LocalizedFormats.xml.trc.context.la.sentences")));
	}

}
