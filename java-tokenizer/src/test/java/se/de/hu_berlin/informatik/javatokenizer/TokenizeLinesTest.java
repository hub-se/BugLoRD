package se.de.hu_berlin.informatik.javatokenizer;

import org.junit.*;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines;
import se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines.CmdOptions;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

/**
 * @author Simon
 */
public class TokenizeLinesTest extends TestSettings {

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
//		deleteTestOutputs();
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
//		deleteTestOutputs();
    }

//    @Rule
//    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    /**
     * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines#main(java.lang.String[])}.
     */
    @Test
    public void testMainSyntax() {
        String[] args = {
                CmdOptions.SOURCE_PATH.asArg(), getStdResourcesDir(),
                CmdOptions.TRACE_FILE.asArg(), getStdResourcesDir() + File.separator + "LocalizedFormats.xml.trc",
                CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "LocalizedFormats.xml.trc.sentences",
                CmdOptions.OVERWRITE.asArg()};
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
                CmdOptions.OVERWRITE.asArg()};
        TokenizeLines.main(args);
        assertTrue(Files.exists(Paths.get(getStdTestDir(), "LocalizedFormats.xml.trc.sem.sentences")));
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
                CmdOptions.OVERWRITE.asArg()};
        TokenizeLines.main(args);
        assertTrue(Files.exists(Paths.get(getStdTestDir(), "SystemUtils.trc.sem.sentences")));
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
                CmdOptions.ABSTRACTION_DEPTH.asArg(), "1",
                CmdOptions.START_METHODS.asArg(),
                CmdOptions.INCLUDE_PARENT.asArg(),
                CmdOptions.CONTEXT.asArg(), "10",
                CmdOptions.CHILD_COUNT_STEPS.asArg(), "10",
                CmdOptions.OVERWRITE.asArg()};
        TokenizeLines.main(args);
        assertTrue(Files.exists(Paths.get(getStdTestDir(), "test.sem.sentences")));
    }

    /**
     * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines#main(java.lang.String[])}.
     */
    @Test
    public void testMainSemantic4() {
        String[] args = {
                CmdOptions.SOURCE_PATH.asArg(), getStdResourcesDir(),
                CmdOptions.TRACE_FILE.asArg(), getStdResourcesDir() + File.separator + "test2.trc",
                CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "smallTest2.trc.sem.sentences",
                CmdOptions.STRATEGY.asArg(), "SEMANTIC_LONG",
                CmdOptions.ABSTRACTION_DEPTH.asArg(), "2",
//				CmdOptions.START_METHODS.asArg(),
                CmdOptions.INCLUDE_PARENT.asArg(),
                CmdOptions.CONTEXT.asArg(), "3",
                CmdOptions.CHILD_COUNT_STEPS.asArg(), "10",
                CmdOptions.OVERWRITE.asArg()};
        TokenizeLines.main(args);
        assertTrue(Files.exists(Paths.get(getStdTestDir(), "smallTest2.trc.sem.sentences")));
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
                CmdOptions.OVERWRITE.asArg()};
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
                CmdOptions.CONTEXT.asArg(), CmdOptions.OVERWRITE.asArg()};
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
                CmdOptions.OVERWRITE.asArg()};
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
                CmdOptions.CONTEXT.asArg(), CmdOptions.OVERWRITE.asArg()};
        TokenizeLines.main(args);
        assertTrue(Files.exists(Paths.get(getStdTestDir(), "LocalizedFormats.xml.trc.context.la.sentences")));
    }

}
