package se.de.hu_berlin.informatik.spectra.core.branch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Iterator;
import org.junit.*;

import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.hit.HitTrace;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;
import se.de.hu_berlin.informatik.utils.tracking.ProgressTracker;


/**
 * @author Simon
 */
public class BranchSpectraTest extends TestSettings {

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
    public void testBranchSpectraSaveAndLoad() throws IOException {
    	//assert(false); //uncomment to check if asserts are enabled

        ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> statementSpectra = loadStatementSpectra("Lang-56b.zip");
        
        ProgramBranchSpectra<ProgramBranch> branchingSpectra = StatementSpectraToBranchSpectra.generateBranchingSpectraFromStatementSpectra(statementSpectra);
 
        Path output1 = Paths.get(getStdTestDir(), "spectra_trace.zip");
        FileUtils.delete(output1);
        SpectraFileUtils.saveSpectraToZipFile(branchingSpectra, output1, true, false, true);
        Log.out(this, "saved...");

        ISpectra<ProgramBranch, ?> spectra2 = SpectraFileUtils.loadSpectraFromZipFile(ProgramBranch.DUMMY, output1);
        Log.out(this, "loaded...");
        assertNotNull(spectra2.getIndexer());
        
        Path output2 = Paths.get(getStdTestDir(), "spectra2_trace.zip");
        FileUtils.delete(output2);
        SpectraFileUtils.saveSpectraToZipFile(spectra2, output2, true, false, true);
        Log.out(this, "saved indexed...");
        ISpectra<ProgramBranch, ?> spectra3 = SpectraFileUtils.loadSpectraFromZipFile(ProgramBranch.DUMMY, output2);
        Log.out(this, "loaded...");
        assertEquals(spectra2, spectra3);

        Path output3 = Paths.get(getStdTestDir(), "spectra3_trace.zip");
        FileUtils.delete(output3);
        SpectraFileUtils.saveSpectraToZipFile(spectra2, output3, true, false, false);
        Log.out(this, "saved non-indexed...");
        ISpectra<ProgramBranch, ?> spectra4 = SpectraFileUtils.loadSpectraFromZipFile(ProgramBranch.DUMMY, output3);
        Log.out(this, "loaded...");
        assertEquals(spectra2, spectra4);

        assertTrue(output1.toFile().exists());
        assertTrue(output2.toFile().exists());
        assertEquals(output1.toFile().length(), output2.toFile().length());
        assertTrue(output3.toFile().exists());
        assertTrue(output2.toFile().length() > output3.toFile().length());
    }
    
    @Test
    public void testBranchSpectraGeneration() throws IOException {
    	//assert(false); //uncomment to check if asserts are enabled

        ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> statementSpectra = loadStatementSpectra("Lang-56b.zip");

        ProgramBranchSpectra<ProgramBranch> branchingSpectra = StatementSpectraToBranchSpectra.generateBranchingSpectraFromStatementSpectra(statementSpectra);
  
        Collection<? extends ITrace<SourceCodeBlock>> statementTests = statementSpectra.getTraces();
        Collection<? extends ITrace<ProgramBranch>> branchTests = branchingSpectra.getTraces();
        
        assertEquals(statementTests.size(), branchTests.size());
        
//        HashSet<Integer> executionBranchIds = new HashSet<Integer>();
//        Collection<Integer> branchIds = new ArrayList<>();
//
        ProgressTracker tracker = new ProgressTracker(false);
        for (ITrace<SourceCodeBlock> testCaseTrace : statementSpectra.getTraces()) {
        	HitTrace<ProgramBranch> branchingTrace = branchingSpectra.getTrace(testCaseTrace.getIdentifier());
        	assertNotNull(branchingTrace);
        	
        	Collection<ExecutionTrace> statementExecutionTraces = testCaseTrace.getExecutionTraces();
        	Collection<ExecutionTrace> branchExecutionTraces = branchingTrace.getExecutionTraces();
        	assertEquals(statementExecutionTraces.size(), branchExecutionTraces.size());
        	
        	if (statementExecutionTraces.size() == 1) {
        		// iterates over node IDs in the execution trace
        		ExecutionTrace executionTrace = statementExecutionTraces.iterator().next();
				Iterator<Integer> statementTraceIterator = executionTrace.mappedIterator(statementSpectra.getIndexer());
        		// iterates over branch IDs in the execution trace
        		ExecutionTrace executionTrace2 = branchExecutionTraces.iterator().next();
        		tracker.track(String.format("size: %,8d, test: %s ", executionTrace2.size(), testCaseTrace.getIdentifier()));
				Iterator<Integer> branchTraceIterator = executionTrace2.mappedIterator(branchingSpectra.getIndexer());
        		
        		long counter = 0;
        		while (branchTraceIterator.hasNext()) {
        			if (++counter % 20000 == 0) {
    					tracker.track(String.format("cancelling after %,d branches.", counter));
    					break;
    				}
        			INode<ProgramBranch> branch = branchingSpectra.getNode(branchTraceIterator.next());
//        			tracker.track(String.format("branch: %s", branch.getIdentifier()));
        			Iterator<SourceCodeBlock> branchIterator = branch.getIdentifier().getElements().iterator();
        			while (branchIterator.hasNext()) {
        				assertTrue(statementTraceIterator.hasNext());
        				assertEquals(statementSpectra.getNode(statementTraceIterator.next()).getIdentifier(), branchIterator.next());
        			}
        		}
        		
        		assertTrue(counter % 20000 == 0 || !statementTraceIterator.hasNext());
        	}
        }
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
