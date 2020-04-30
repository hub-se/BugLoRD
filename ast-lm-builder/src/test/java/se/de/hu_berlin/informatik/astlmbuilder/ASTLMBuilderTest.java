package se.de.hu_berlin.informatik.astlmbuilder;

import org.junit.*;
import se.de.hu_berlin.informatik.astlmbuilder.ASTLMBOptions.ASTLMBCmdOptions;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

/**
 * @author Simon
 */
public class ASTLMBuilderTest extends TestSettings {

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
        deleteTestOutputs();
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
        deleteTestOutputs();
    }

    /**
     * Test method for {@link se.de.hu_berlin.informatik.astlmbuilder.ASTLMBuilder#main(java.lang.String[])}.
     */
    @Test
    public void testMain() {
        String[] args = {
                ASTLMBCmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "training_files",
                ASTLMBCmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "out.lm",
                ASTLMBCmdOptions.GRANULARITY.asArg(), "all",
                ASTLMBCmdOptions.ENTRY_POINT.asArg(), "root",
                ASTLMBCmdOptions.CREATE_ARPA_TEXT.asArg(),
                ASTLMBCmdOptions.NGRAM_ORDER.asArg(), "3"};
        ASTLMBuilder.main(args);
        assertTrue(Files.exists(Paths.get(getStdTestDir(), "out.lm.bin")));
        assertTrue(Files.exists(Paths.get(getStdTestDir(), "out.lm.arpa")));
    }

    /**
     * Test method for {@link se.de.hu_berlin.informatik.astlmbuilder.ASTLMBuilder#main(java.lang.String[])}.
     */
    @Test
    public void testMainSingleFile() {
        String[] args = {
                ASTLMBCmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "training_files" + File.separator + "StringUtils.java",
                ASTLMBCmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "outSingle.lm",
                ASTLMBCmdOptions.GRANULARITY.asArg(), "all",
                ASTLMBCmdOptions.ENTRY_POINT.asArg(), "all",
                ASTLMBCmdOptions.CREATE_ARPA_TEXT.asArg(),
                ASTLMBCmdOptions.NGRAM_ORDER.asArg(), "6"};
        ASTLMBuilder.main(args);
        assertTrue(Files.exists(Paths.get(getStdTestDir(), "outSingle.lm.bin")));
        assertTrue(Files.exists(Paths.get(getStdTestDir(), "outSingle.lm.arpa")));
    }

    /**
     * Test method for {@link se.de.hu_berlin.informatik.astlmbuilder.ASTLMBuilder#main(java.lang.String[])}.
     */
    @Test
    public void testMainSingleFileSmallTest() {
        String[] args = {
                ASTLMBCmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "training_files" + File.separator + "smallTest.java",
                ASTLMBCmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "small.lm",
                ASTLMBCmdOptions.GRANULARITY.asArg(), "all",
                ASTLMBCmdOptions.ENTRY_POINT.asArg(), "all",
//				ASTLMBCmdOptions.SINGLE_TOKENS.asArg(),
                ASTLMBCmdOptions.CREATE_ARPA_TEXT.asArg(),
                ASTLMBCmdOptions.NGRAM_ORDER.asArg(), "6"};
        ASTLMBuilder.main(args);
        assertTrue(Files.exists(Paths.get(getStdTestDir(), "small.lm.bin")));
        assertTrue(Files.exists(Paths.get(getStdTestDir(), "small.lm.arpa")));
    }

    /**
     * Test method for {@link se.de.hu_berlin.informatik.astlmbuilder.ASTLMBuilder#main(java.lang.String[])}.
     */
    @Test
    public void testMainSingleTokens() {
        String[] args = {
                ASTLMBCmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "training_files",
                ASTLMBCmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "out3.lm",
                ASTLMBCmdOptions.GRANULARITY.asArg(), "all",
                ASTLMBCmdOptions.ENTRY_POINT.asArg(), "root",
                ASTLMBCmdOptions.CREATE_ARPA_TEXT.asArg(),
//				ASTLMBCmdOptions.SINGLE_TOKENS.asArg(),
                ASTLMBCmdOptions.NGRAM_ORDER.asArg(), "3"};
        ASTLMBuilder.main(args);
        assertTrue(Files.exists(Paths.get(getStdTestDir(), "out3.lm.bin")));
        assertTrue(Files.exists(Paths.get(getStdTestDir(), "out3.lm.arpa")));
    }

    /**
     * Test method for {@link se.de.hu_berlin.informatik.astlmbuilder.ASTLMBuilder#main(java.lang.String[])}.
     */
    @Test
    public void testMainNormalGranularityMethods() {
        String[] args = {
                ASTLMBCmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "training_files",
                ASTLMBCmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "out2.lm",
                ASTLMBCmdOptions.GRANULARITY.asArg(), "normal",
                ASTLMBCmdOptions.ENTRY_POINT.asArg(), "method",
                ASTLMBCmdOptions.CREATE_ARPA_TEXT.asArg(),
                ASTLMBCmdOptions.NGRAM_ORDER.asArg(), "3"};
        ASTLMBuilder.main(args);
        assertTrue(Files.exists(Paths.get(getStdTestDir(), "out2.lm.bin")));
        assertTrue(Files.exists(Paths.get(getStdTestDir(), "out2.lm.arpa")));
    }

}
