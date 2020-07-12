package se.de.hu_berlin.informatik.spectra.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.junit.*;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemErrRule;
import se.de.hu_berlin.informatik.spectra.core.INode.CoverageType;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.Node.NodeType;
import se.de.hu_berlin.informatik.spectra.core.branch.ProgramBranch;
import se.de.hu_berlin.informatik.spectra.core.branch.ProgramBranchSpectra;
import se.de.hu_berlin.informatik.spectra.core.branch.StatementSpectraToBranchSpectra;
import se.de.hu_berlin.informatik.spectra.core.cfg.CFG;
import se.de.hu_berlin.informatik.spectra.core.cfg.DynamicCFG;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.hit.HitSpectra;
import se.de.hu_berlin.informatik.spectra.core.hit.HitTrace;
import se.de.hu_berlin.informatik.spectra.provider.cobertura.CoberturaSpectraProviderFactory;
import se.de.hu_berlin.informatik.spectra.provider.cobertura.xml.CoberturaXMLProvider;
import se.de.hu_berlin.informatik.spectra.test.data.SimpleSpectraProvider2;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

/**
 * @author SimHigh
 */
public class SpectraUtilsTest extends TestSettings {

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
//        deleteTestOutputs();
    }

    /**
     *
     */
    @Before
    public void setUp() {
//		Log.off();
    }

    /**
     *
     */
    @After
    public void tearDown() {
//        deleteTestOutputs();
    }

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog();

    @Test
    public void invertSimpleCoverage() {
        final CoberturaXMLProvider<HitTrace<SourceCodeBlock>> c = CoberturaSpectraProviderFactory.getHitSpectraFromXMLProvider(true);
        c.addData("src/test/resources/fk/stardust/provider/simple-coverage.xml", "simple", true);
        //load and invert (only one trace exists - successful)
        HitSpectra<SourceCodeBlock> s = SpectraUtils.createInvertedSpectrum(c.loadSpectra(), true, false);
        checkSimpleNodes(s);
        checkSimpleTraceInverted(s);

        //invert again
        s = s.createInvertedSpectra(true, true);
        checkSimpleNodes(s);
        checkSimpleTraceNormal(s);

        //should not change
        s = s.createInvertedSpectra(false, true);
        checkSimpleNodes(s);
        checkSimpleTraceNormal(s);

        //should not change
        s = s.createInvertedSpectra(false, false);
        checkSimpleNodes(s);
        checkSimpleTraceNormal(s);
    }

    private static void checkSimpleNodes(final HitSpectra<SourceCodeBlock> s) {
        // assert loaded count is correct
        Assert.assertEquals(s.getNodes().size(), 3);
        Assert.assertEquals(s.getTraces().size(), 1);

        // assert we have nodes
        Assert.assertTrue(s.hasNode(new SourceCodeBlock("cobertura", "cobertura/CoverageTest.java", "<init>()V", 3, NodeType.NORMAL)));
        Assert.assertTrue(s.hasNode(new SourceCodeBlock("cobertura", "cobertura/CoverageTest.java", "main([Ljava/lang/String;)V", 9, NodeType.NORMAL)));
        Assert.assertTrue(s.hasNode(new SourceCodeBlock("cobertura", "cobertura/CoverageTest.java", "main([Ljava/lang/String;)V", 10, NodeType.NORMAL)));
    }

    private static void checkSimpleTraceNormal(final HitSpectra<SourceCodeBlock> s) {
        // assert trace has correct involvement loaded
        final ITrace<SourceCodeBlock> t = s.getTraces().iterator().next();
        Assert.assertFalse(t.isInvolved(s.getOrCreateNode(new SourceCodeBlock("cobertura", "cobertura/CoverageTest.java", "<init>()V", 3, NodeType.NORMAL))));
        Assert.assertTrue(t.isInvolved(s.getOrCreateNode(new SourceCodeBlock("cobertura", "cobertura/CoverageTest.java", "main([Ljava/lang/String;)V", 9, NodeType.NORMAL))));
        Assert.assertTrue(t.isInvolved(s.getOrCreateNode(new SourceCodeBlock("cobertura", "cobertura/CoverageTest.java", "main([Ljava/lang/String;)V", 10, NodeType.NORMAL))));

        //assert that trace is loaded as 'successful'
        Assert.assertTrue(t.isSuccessful());
    }

    private static void checkSimpleTraceInverted(final HitSpectra<SourceCodeBlock> s) {
        // assert trace has correct involvement loaded
        final ITrace<SourceCodeBlock> t = s.getTraces().iterator().next();
        Assert.assertTrue(t.isInvolved(s.getOrCreateNode(new SourceCodeBlock("cobertura", "cobertura/CoverageTest.java", "<init>()V", 3, NodeType.NORMAL))));
        Assert.assertFalse(t.isInvolved(s.getOrCreateNode(new SourceCodeBlock("cobertura", "cobertura/CoverageTest.java", "main([Ljava/lang/String;)V", 9, NodeType.NORMAL))));
        Assert.assertFalse(t.isInvolved(s.getOrCreateNode(new SourceCodeBlock("cobertura", "cobertura/CoverageTest.java", "main([Ljava/lang/String;)V", 10, NodeType.NORMAL))));

        //assert that trace is loaded as 'successful'
        Assert.assertTrue(t.isSuccessful());
    }

    @Test
    public void removeNodes() {
        HitSpectra<String> s;

        s = loadSimpleSpectraAndCheck();
        s.removeNodesWithCoverageType(CoverageType.EXECUTED);
        Assert.assertEquals(1, s.getNodes().size());
        s.removeNodesWithCoverageType(CoverageType.NOT_EXECUTED);
        Assert.assertEquals(0, s.getNodes().size());

        s = loadSimpleSpectraAndCheck();
        s.removeNodesWithCoverageType(CoverageType.NOT_EXECUTED);
        Assert.assertEquals(5, s.getNodes().size());
        s.removeNodesWithCoverageType(CoverageType.EXECUTED);
        Assert.assertEquals(0, s.getNodes().size());

        s = loadSimpleSpectraAndCheck();
        s.removeNodesWithCoverageType(CoverageType.EF_GT_ZERO);
        Assert.assertEquals(2, s.getNodes().size());
        s.removeNodesWithCoverageType(CoverageType.EP_EQUALS_ZERO);
        Assert.assertEquals(1, s.getNodes().size());
        s.removeNodesWithCoverageType(CoverageType.EF_EQUALS_ZERO);
        Assert.assertEquals(0, s.getNodes().size());
        s.removeNodesWithCoverageType(CoverageType.EP_GT_ZERO);
        Assert.assertEquals(0, s.getNodes().size());

        s = loadSimpleSpectraAndCheck();
        s.removeNodesWithCoverageType(CoverageType.EP_EQUALS_ZERO);
        Assert.assertEquals(4, s.getNodes().size());
        s.removeNodesWithCoverageType(CoverageType.EF_EQUALS_ZERO);
        Assert.assertEquals(3, s.getNodes().size());
        s.removeNodesWithCoverageType(CoverageType.EP_GT_ZERO);
        Assert.assertEquals(0, s.getNodes().size());
        s.removeNodesWithCoverageType(CoverageType.EF_GT_ZERO);
        Assert.assertEquals(0, s.getNodes().size());

        s = loadSimpleSpectraAndCheck();
        s.removeNodesWithCoverageType(CoverageType.EP_GT_ZERO);
        Assert.assertEquals(2, s.getNodes().size());
        s.removeNodesWithCoverageType(CoverageType.EF_EQUALS_ZERO);
        Assert.assertEquals(1, s.getNodes().size());
        s.removeNodesWithCoverageType(CoverageType.EP_EQUALS_ZERO);
        Assert.assertEquals(0, s.getNodes().size());
        s.removeNodesWithCoverageType(CoverageType.EF_GT_ZERO);
        Assert.assertEquals(0, s.getNodes().size());

        s = loadSimpleSpectraAndCheck();
        s.removeNodesWithCoverageType(CoverageType.EF_EQUALS_ZERO);
        Assert.assertEquals(4, s.getNodes().size(), 4);
        s.removeNodesWithCoverageType(CoverageType.EP_GT_ZERO);
        Assert.assertEquals(1, s.getNodes().size());
        s.removeNodesWithCoverageType(CoverageType.EP_EQUALS_ZERO);
        Assert.assertEquals(0, s.getNodes().size());
        s.removeNodesWithCoverageType(CoverageType.EF_GT_ZERO);
        Assert.assertEquals(0, s.getNodes().size());
    }

    private static HitSpectra<String> loadSimpleSpectraAndCheck() {
        final HitSpectra<String> s = new SimpleSpectraProvider2().loadHitSpectra();
        Assert.assertEquals(s.getNodes().size(), 6);
        Assert.assertEquals(s.getTraces().size(), 6);
        return s;
    }
    
    @Test
    public void testCFGGeneration() throws IOException {
    	//assert(false); //uncomment to check if asserts are enabled

        ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> statementSpectra = loadStatementSpectra("Lang-56b.zip");

        File file = Paths.get(getStdTestDir(), "Lang-56b_statement.cfg").toFile();
        FileUtils.delete(file);
		CFG<SourceCodeBlock> cfg = statementSpectra.getCFG(file);
        
//        System.out.print(cfg);
        
        ProgramBranchSpectra<ProgramBranch> branchingSpectra = StatementSpectraToBranchSpectra
        		.generateBranchingSpectraFromStatementSpectra(statementSpectra, null);
        
        File file2 = Paths.get(getStdTestDir(), "Lang-56b_branch.cfg").toFile();
        FileUtils.delete(file2);
        CFG<ProgramBranch> cfg2 = branchingSpectra.getCFG(file2);
        
//        System.out.print(cfg2);
        
    }
    
    @Test
    public void testCFGSaveAndLoad() throws IOException {
    	//assert(false); //uncomment to check if asserts are enabled

        ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> statementSpectra = loadStatementSpectra("Lang-56b.zip");

        File file = Paths.get(getStdTestDir(), "Lang-56b_statement.cfg").toFile();
        FileUtils.delete(file);
		CFG<SourceCodeBlock> cfg = statementSpectra.getCFG(file);
		
		CFG<SourceCodeBlock> cfg2 = new DynamicCFG<SourceCodeBlock>(statementSpectra, file);
		
		Assert.assertEquals(cfg, cfg2);
    }

	private ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> loadStatementSpectra(String fileName) throws IOException {
		Path directory = Paths.get(getStdResourcesDir(), "traceSpectra").toAbsolutePath();
        Path path = directory.resolve(fileName);
        Path target = Paths.get(getStdTestDir(), fileName).toAbsolutePath();
        Path parent = target.getParent();
        
        FileUtils.copyFileOrDir(path.toFile(), target.toFile(), StandardCopyOption.REPLACE_EXISTING);
        
		FileUtils.delete(parent.resolve("execTraceTemp"));
        FileUtils.delete(parent.resolve("branchMap.zip"));
        
        ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>>
                statementSpectra = SpectraFileUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, target);
		return statementSpectra;
	}

}
