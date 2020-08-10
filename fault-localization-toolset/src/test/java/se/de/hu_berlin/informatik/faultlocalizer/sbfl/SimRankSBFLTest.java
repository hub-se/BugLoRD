package se.de.hu_berlin.informatik.faultlocalizer.sbfl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.junit.*;

import se.de.hu_berlin.informatik.faultlocalizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.branch.ProgramBranch;
import se.de.hu_berlin.informatik.spectra.core.branch.ProgramBranchSpectra;
import se.de.hu_berlin.informatik.spectra.core.branch.StatementSpectraToBranchSpectra;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;


/**
 * @author Simon
 */
public class SimRankSBFLTest extends TestSettings {

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
    }
    
    @Test
    public void testSimRankSBFL() throws IOException {
    	//assert(false); //uncomment to check if asserts are enabled

        ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> statementSpectra = loadStatementSpectra("Lang-56b_new.zip");

        generateRankings(statementSpectra, "stmtSR");
    }

	private static <T> void generateRankings(ISpectra<T, ? extends ITrace<T>> statementSpectra, String subDir)
			throws IOException {
		Paths.get(getStdTestDir(), subDir).toFile().mkdirs();
		
		generateRanking(statementSpectra, subDir, "dstar");
        generateRanking(statementSpectra, subDir, "barinel");
        generateRanking(statementSpectra, subDir, "overlap");
     
        generateRanking(statementSpectra, subDir, "sr_avg_0.5_2_dstar");
        generateRanking(statementSpectra, subDir, "sr_avg_0.5_2_overlap");
        generateRanking(statementSpectra, subDir, "sr_avg_0.5_0_dstar");
        generateRanking(statementSpectra, subDir, "sr_avg_0.8_0_dstar");
        generateRanking(statementSpectra, subDir, "sr_avg_0.3_0_dstar");
        generateRanking(statementSpectra, subDir, "sr_avg_0.5_0_overlap");
        
        generateRanking(statementSpectra, subDir, "isr_avg_0.5_2_dstar");
        generateRanking(statementSpectra, subDir, "isr_avg_0.5_2_overlap");
        generateRanking(statementSpectra, subDir, "isr_avg_0.5_0_dstar");
        generateRanking(statementSpectra, subDir, "isr_avg_0.5_0_overlap");
        
        generateRanking(statementSpectra, subDir, "vsr_avg_0.5_2_dstar");
        generateRanking(statementSpectra, subDir, "vsr_avg_0.5_2_overlap");
        generateRanking(statementSpectra, subDir, "vsr_avg_0.5_0_dstar");
        generateRanking(statementSpectra, subDir, "vsr_avg_0.5_0_overlap");
        
        generateRanking(statementSpectra, subDir, "hsr_avg_0.5_2_dstar");
        generateRanking(statementSpectra, subDir, "hsr_avg_0.5_2_overlap");
        generateRanking(statementSpectra, subDir, "hsr_avg_0.5_0_dstar");
        generateRanking(statementSpectra, subDir, "hsr_avg_0.5_0_overlap");
        
        generateRanking(statementSpectra, subDir, "hisr_avg_0.5_2_dstar");
        generateRanking(statementSpectra, subDir, "hisr_avg_0.5_2_overlap");
        generateRanking(statementSpectra, subDir, "hisr_avg_0.5_0_dstar");
        generateRanking(statementSpectra, subDir, "hisr_avg_0.5_0_overlap");
        
        generateRanking(statementSpectra, subDir, "hvsr_avg_0.5_2_dstar");
        generateRanking(statementSpectra, subDir, "hvsr_avg_0.5_2_overlap");
        generateRanking(statementSpectra, subDir, "hvsr_avg_0.5_0_dstar");
        generateRanking(statementSpectra, subDir, "hvsr_avg_0.5_0_overlap");
	}

	private static <T> void generateRanking(ISpectra<T, ? extends ITrace<T>> statementSpectra, String subDir,
			String localizerString) throws IOException {
		System.out.println("calculating " + localizerString);
		IFaultLocalizer<T> localizer = FaultLocalizerFactory.newInstance(localizerString);
		Ranking<INode<T>> ranking = localizer.localize(statementSpectra);
        ranking.save(Paths.get(getStdTestDir(), subDir, localizer.getName() + ".rnk").toAbsolutePath().toString());
	}

    
    @Test
    public void testSimRankBranchSBFL() throws IOException {
    	//assert(false); //uncomment to check if asserts are enabled

        ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> statementSpectra = loadStatementSpectra("Lang-56b_new.zip");

        ProgramBranchSpectra<ProgramBranch> branchingSpectra = StatementSpectraToBranchSpectra
        		.generateBranchingSpectraFromStatementSpectra(statementSpectra, null);

        generateRankings(branchingSpectra, "branchSR");
        
    }
    
    private ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> loadStatementSpectra(String fileName) throws IOException {
		Path directory = Paths.get(getStdResourcesDir()).toAbsolutePath();
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
