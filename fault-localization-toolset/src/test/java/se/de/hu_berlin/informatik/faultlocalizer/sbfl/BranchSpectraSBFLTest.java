package se.de.hu_berlin.informatik.faultlocalizer.sbfl;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import org.junit.*;

import se.de.hu_berlin.informatik.gen.ranking.modules.RankingModule;
import se.de.hu_berlin.informatik.spectra.core.ComputationStrategies;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.branch.ProgramBranch;
import se.de.hu_berlin.informatik.spectra.core.branch.ProgramBranchSpectra;
import se.de.hu_berlin.informatik.spectra.core.branch.StatementSpectraToBranchSpectra;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;


/**
 * @author Simon
 */
public class BranchSpectraSBFLTest extends TestSettings {

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
    public void testBranchSpectraGenAndLoading() throws IOException {
    	//assert(false); //uncomment to check if asserts are enabled

        ISpectra<ProgramBranch, ? extends ITrace<ProgramBranch>> loaded = loadBranchSpectra("Lang-56b_branch.zip", "genAndLoad");
        
        ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> statementSpectra = loadStatementSpectra("Lang-56b_new.zip", "genAndLoad");
        
        ProgramBranchSpectra<ProgramBranch> generated = StatementSpectraToBranchSpectra
        		.generateBranchingSpectraFromStatementSpectra(statementSpectra, null);
        
        
        assertEquals(generated.getNodes().size(), loaded.getNodes().size());
        
        for (int i = 0; i < generated.getNodes().size(); ++i) {
        	ProgramBranch generatedBranch = generated.getNode(i).getIdentifier();
        	ProgramBranch loadedBranch = loaded.getNode(i).getIdentifier();
        	
        	assertEquals(generatedBranch.getLength(), loadedBranch.getLength());
        	
        	Iterator<SourceCodeBlock> genIterator = generatedBranch.iterator();
        	Iterator<SourceCodeBlock> loadedIterator = loadedBranch.iterator();
        	
        	while (loadedIterator.hasNext() && genIterator.hasNext()) {
        		try {
        		assertEquals(genIterator.next(), loadedIterator.next());
        		} catch (NullPointerException e) {
        			System.err.println("id: " + i + System.lineSeparator() +
        	        		generatedBranch.toString() + System.lineSeparator() +
        	        		loadedBranch.toString());
        			break;
				}
        	}
//        	assertFalse(genIterator.hasNext());
        }
    }
    
    
    @Test
    public void testBranchSpectraGenAndFL() throws IOException {
    	//assert(false); //uncomment to check if asserts are enabled

        ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> statementSpectra = loadStatementSpectra("Lang-56b_new.zip", "genFL");
        
        ProgramBranchSpectra<ProgramBranch> branchingSpectra = StatementSpectraToBranchSpectra
        		.generateBranchingSpectraFromStatementSpectra(statementSpectra, null);
        
        new RankingModule<>(ComputationStrategies.STANDARD_SBFL, getStdTestDir(), "Dstar").submit(branchingSpectra);
 
//        Path output1 = Paths.get(getStdTestDir(), "spectra_trace.zip");
//        FileUtils.delete(output1);
        
    }

    @Test
    public void testBranchSpectraLoadAndFL() throws IOException {
    	//assert(false); //uncomment to check if asserts are enabled

        ISpectra<ProgramBranch, ? extends ITrace<ProgramBranch>> branchingSpectra = loadBranchSpectra("Lang-56b_branch.zip", "loadFL");
        
        new RankingModule<>(ComputationStrategies.STANDARD_SBFL, getStdTestDir(), "Dstar").submit(branchingSpectra);
 
//        Path output1 = Paths.get(getStdTestDir(), "spectra_trace.zip");
//        FileUtils.delete(output1);
        
    }
    
	private ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> loadStatementSpectra(String fileName, String destDir) throws IOException {
		Path directory = Paths.get(getStdResourcesDir()).toAbsolutePath();
        Path path = directory.resolve(fileName);
        Path target = Paths.get(getStdTestDir(), destDir, fileName).toAbsolutePath();
 
        FileUtils.delete(Paths.get(getStdTestDir(), destDir).resolve("execTraceTemp"));
        FileUtils.delete(Paths.get(getStdTestDir(), destDir).resolve("branchMap.zip"));
        
        FileUtils.copyFileOrDir(path.toFile(), target.toFile(), StandardCopyOption.REPLACE_EXISTING);
        
        ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>>
                statementSpectra = SpectraFileUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, target);
		return statementSpectra;
	}
	
	private ISpectra<ProgramBranch, ? extends ITrace<ProgramBranch>> loadBranchSpectra(String fileName, String destDir) throws IOException {
		Path directory = Paths.get(getStdResourcesDir()).toAbsolutePath();
        Path path = directory.resolve(fileName);
        Path target = Paths.get(getStdTestDir(), destDir, fileName).toAbsolutePath();
 
        FileUtils.delete(Paths.get(getStdTestDir(), destDir).resolve("execTraceTemp"));
        FileUtils.delete(Paths.get(getStdTestDir(), destDir).resolve("branchMap.zip"));
        
        FileUtils.copyFileOrDir(path.toFile(), target.toFile(), StandardCopyOption.REPLACE_EXISTING);

        ISpectra<ProgramBranch, ? extends ITrace<ProgramBranch>>
                statementSpectra = SpectraFileUtils.loadSpectraFromZipFile(ProgramBranch.DUMMY, target);
		return statementSpectra;
	}
    
}
