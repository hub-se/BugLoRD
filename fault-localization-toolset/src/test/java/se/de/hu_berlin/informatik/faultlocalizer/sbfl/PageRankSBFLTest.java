package se.de.hu_berlin.informatik.faultlocalizer.sbfl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.junit.*;

import se.de.hu_berlin.informatik.faultlocalizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.cfg.CfgPageRankFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Barinel;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.DStar;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Overlap;
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
public class PageRankSBFLTest extends TestSettings {

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
    public void testPageRankSBFL() throws IOException {
    	//assert(false); //uncomment to check if asserts are enabled

        ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> statementSpectra = loadStatementSpectra("Lang-56b_new.zip");

        Ranking<INode<SourceCodeBlock>> sbflRanking = new DStar<SourceCodeBlock>().localize(statementSpectra);
        sbflRanking.save(Paths.get(getStdTestDir(), "dstar.rnk").toAbsolutePath().toString());
        
        Ranking<INode<SourceCodeBlock>> sbflRanking2 = new Barinel<SourceCodeBlock>().localize(statementSpectra);
        sbflRanking2.save(Paths.get(getStdTestDir(), "barinel.rnk").toAbsolutePath().toString());
        
        Ranking<INode<SourceCodeBlock>> sbflRanking3 = new Overlap<SourceCodeBlock>().localize(statementSpectra);
        sbflRanking3.save(Paths.get(getStdTestDir(), "overlap.rnk").toAbsolutePath().toString());
     
        IFaultLocalizer<SourceCodeBlock> localizer = FaultLocalizerFactory.newInstance("pr_0.5_2_r_dstar");
        Ranking<INode<SourceCodeBlock>> ranking = localizer.localize(statementSpectra);
        ranking.save(Paths.get(getStdTestDir(), localizer.getName() + ".rnk").toAbsolutePath().toString());
        
        localizer = FaultLocalizerFactory.newInstance("pr_0.5_2_f_dstar");
        ranking = localizer.localize(statementSpectra);
        ranking.save(Paths.get(getStdTestDir(), localizer.getName() + ".rnk").toAbsolutePath().toString());
     
        localizer = FaultLocalizerFactory.newInstance("pr_0.5_2_r_overlap");
        ranking = localizer.localize(statementSpectra);
        ranking.save(Paths.get(getStdTestDir(), localizer.getName() + ".rnk").toAbsolutePath().toString());
      
        localizer = FaultLocalizerFactory.newInstance("pr_0.5_2_f_overlap");
        ranking = localizer.localize(statementSpectra);
        ranking.save(Paths.get(getStdTestDir(), localizer.getName() + ".rnk").toAbsolutePath().toString());
     
    }
    
    @Test
    public void testPageRankBranchSBFL() throws IOException {
    	//assert(false); //uncomment to check if asserts are enabled

        ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> statementSpectra = loadStatementSpectra("Lang-56b_new.zip");

        ProgramBranchSpectra<ProgramBranch> branchingSpectra = StatementSpectraToBranchSpectra
        		.generateBranchingSpectraFromStatementSpectra(statementSpectra, null);

        Ranking<INode<ProgramBranch>> sbflRankingB = new DStar<ProgramBranch>().localize(branchingSpectra);
        sbflRankingB.save(Paths.get(getStdTestDir(), "dstar_branch.rnk").toAbsolutePath().toString());
        
        Ranking<INode<ProgramBranch>> rankingB = new CfgPageRankFaultLocalizer<ProgramBranch>(new DStar<>()).localize(branchingSpectra);
        rankingB.save(Paths.get(getStdTestDir(), "pr_dstar_branch.rnk").toAbsolutePath().toString());
        
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
