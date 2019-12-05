package se.de.hu_berlin.informatik.spectra.core.branch;


import org.junit.Test;
import se.de.hu_berlin.informatik.spectra.core.*;
import se.de.hu_berlin.informatik.spectra.core.branch.ProgramBranch;
import se.de.hu_berlin.informatik.spectra.core.branch.ProgramBranchSpectra;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedIntArrayQueue.MyBufferedIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.TraceIterator;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


/**
 * Reads a Spectra object and combines sequences of nodes to larger blocks based
 * on whether they are within the same branch.
 *
 * @author Duc Anh Vu
 *
 */
public class StatementSpectraToBranchSpectra {

    /*====================================================================================
     * TEST
     *====================================================================================*/

    @Test
    public void foo(){

        //assert(false); //uncomment to check if asserts are enabled

        final String traceLocations = "../../resources/spectraTraces";
        Path path = Paths.get(traceLocations, "Lang-10b.zip");
        ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>>
                statementSpectra = SpectraFileUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, path);

        @SuppressWarnings("unused")
		ProgramBranchSpectra branchingSpectra = generateBranchingSpectraFromStatementSpectra(statementSpectra, "");

        HashSet<Integer> executionBranchIds = new HashSet<Integer>();
        Collection<Integer> branchIds = new ArrayList<>();

        for(ITrace<SourceCodeBlock> testCaseTrace : statementSpectra.getTraces()){
            for(ExecutionTrace executionTrace : testCaseTrace.getExecutionTraces()){
                branchIds = collectExecutionBranchIds(executionTrace, statementSpectra);
                executionBranchIds.addAll(collectExecutionBranchIds(executionTrace, statementSpectra));
            }
            assert(branchesAreExactlyCoveredByThisTestCase(testCaseTrace, branchIds, statementSpectra));
            branchIds.clear();
        }

    }

    /*====================================================================================
     * MAIN FUNCTIONALITY
     *====================================================================================*/

    public static ProgramBranchSpectra generateBranchingSpectraFromStatementSpectra
            (ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> statementSpectra, String pathToSpectraZipFile){

        /*====================================================================================*/
        assert(statementSpectra != null);
        assert(pathToSpectraZipFile != null);
        /*====================================================================================*/

        ProgramBranchSpectra programBranchSpectra = new ProgramBranchSpectra(Paths.get(pathToSpectraZipFile));
        Collection<Integer> failingExecutionBranchIds = collectExecutionBranchIds(statementSpectra.getFailingTraces(), statementSpectra);
        Map<Integer, INode<ProgramBranch>> branchIdMap = new HashMap<>();
        INode<ProgramBranch> branchNode;
        ProgramBranch programBranch;

        /* extract the "failing" branches from the statement spectra, i.e.
         *  branches that are covered in a failing test case
         *  branch := list of statements
         */
        for(Integer failingExecutionBranchId : failingExecutionBranchIds){
            programBranch = new ProgramBranch(getExecutedStatementsFromBranch(failingExecutionBranchId, statementSpectra));
            branchNode = programBranchSpectra.getOrCreateNode(programBranch);
            branchIdMap.put(failingExecutionBranchId, branchNode);
        }

        programBranchSpectra.setBranchIdMap(branchIdMap);

        ITrace<ProgramBranch> newTrace;

        /* the branchingSpectra has the same traces as the statementSpectra
         *  --> copy every trace, but set the involvement for each trace according to which branches
         *  they cover
         *  (in the statement spectra the traces only know which statements they cover)
         */
        for(ITrace<SourceCodeBlock> testCase : statementSpectra.getTraces()){

            newTrace = programBranchSpectra.addTrace(testCase.getIdentifier(), testCase.getIndex(), testCase.isSuccessful());

            for(ExecutionTrace executionTrace : testCase.getExecutionTraces()){
                for(Integer executionBranchId : collectExecutionBranchIds(executionTrace, statementSpectra)){

                    branchNode = programBranchSpectra.getBranchNode(executionBranchId);
                    if(branchNode != null){
                        newTrace.setInvolvement(branchNode, true);
                    }

                }
            }
        }

        /*====================================================================================*/
        assert(programBranchSpectra != null);
        // some check for correctness required, e.g. do the resulting branches match the executed statements?
        // --> assert(branchingSpectraMatchesStatementSpectra) or something
        // kind of hard to do now, since the existing data structures don't have easy ways to access
        // the required information
        assert(branchesHaveAtMostKDecisionPoints(programBranchSpectra, 1)); // k=1 --> number of decisions in a branch is at most 1 for now ; next plan is to merge multiple branches therefore they can have multiple decision points
        /*====================================================================================*/

        return programBranchSpectra;

    }

    /*====================================================================================
     * HELPER METHODS
     *====================================================================================*/

    private static Collection<Integer> collectExecutionBranchIds(Collection<? extends ITrace<SourceCodeBlock>> testCaseTraces,
                                                                 ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> statementSpectra){

        /*====================================================================================*/
        assert(testCaseTraces != null);
        /*====================================================================================*/

        HashSet<Integer> executionBranchIds = new HashSet<Integer>();

        for(ITrace<SourceCodeBlock> testCaseTrace : testCaseTraces){
            for(ExecutionTrace executionTrace : testCaseTrace.getExecutionTraces()){
                executionBranchIds.addAll(collectExecutionBranchIds(executionTrace, statementSpectra));
            }
        }

        /*====================================================================================*/
        assert(executionBranchIds != null);
        /*====================================================================================*/

        return executionBranchIds;

    }

    private static Collection<Integer> collectExecutionBranchIds(ExecutionTrace executionTrace,
                                                                 ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> statementSpectra){

        /*====================================================================================*/
        assert(executionTrace != null);
        /*====================================================================================*/

        HashSet<Integer> executionBranchIds = new HashSet<Integer>();
        MyBufferedIterator myExecutionBranchIterator = executionTrace.getCompressedTrace().iterator();

        while(myExecutionBranchIterator.hasNext()){
            Iterator<Integer> subTraceIdIterator = statementSpectra.getIndexer().getSubTraceIDSequenceIterator(myExecutionBranchIterator.next());
            while (subTraceIdIterator.hasNext()) {
                executionBranchIds.add(subTraceIdIterator.next());
            }

        }

        /*====================================================================================*/
        assert(executionBranchIds != null);
        /*====================================================================================*/

        return executionBranchIds;

    }

    private static List<Integer> getStatementIndicesFromBranch(int branchId, ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> statementSpectra){

        /*====================================================================================*/
        assert(statementSpectra != null);
        /*====================================================================================*/

        List<Integer> statementIndices = null;

        TraceIterator statementIndicesIterator = statementSpectra.getIndexer().getNodeIdSequenceIterator(branchId);
        statementIndices = new ArrayList<Integer>();

        while(statementIndicesIterator.hasNext()){
            statementIndices.add(statementIndicesIterator.next());
        }

        /*====================================================================================*/
        assert(statementIndices != null);
        assert(isExecutedListOfStatementIndicesForThisBranch(branchId, statementIndices, statementSpectra));
        /*====================================================================================*/

        return statementIndices;

    }

    private static List<SourceCodeBlock> getExecutedStatementsFromBranch(int branchId, ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> statementSpectra){

        /*====================================================================================*/
        assert(statementSpectra != null);
        /*====================================================================================*/

        List<SourceCodeBlock> statements = null;

        List<Integer> statementIndices = getStatementIndicesFromBranch(branchId, statementSpectra);

        statements = new ArrayList<SourceCodeBlock>();
        for(Integer statementIndex : statementIndices){
            statements.add(statementSpectra.getNode((statementIndex)).getIdentifier());
        }

        /*====================================================================================*/
        assert(statements != null);
        assert(isExecutedListOfStatementsOfThisBranch(branchId, statements, statementSpectra));
        /*====================================================================================*/

        return statements;

    }

    /*====================================================================================
     * CHECKS
     *====================================================================================*/

    private static boolean isBranchingSpectraOfStatementSpectra(ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> statementSpectra, ISpectra<Integer, ? extends ITrace<Integer>> branchingSpectra){
        return true;
    }

    /**
     * Exactly means not less and not more statements are covered
     *
     * @param testCase
     * @param statementIndices
     * @return
     */
    private static boolean statementsAreExactlyCoveredByThisTestCase(ITrace<SourceCodeBlock> testCase, Collection<Integer> statementIndices){

        boolean result = false;

        Collection<Integer> testCaseInvolvedStatementIndices = testCase.getInvolvedNodes();

        boolean isSubset = testCaseInvolvedStatementIndices.containsAll(statementIndices);
        boolean isSuperset = statementIndices.containsAll(testCaseInvolvedStatementIndices);

        System.out.println("---------------------");
        System.out.println(testCaseInvolvedStatementIndices);
        System.out.println(statementIndices);
        System.out.println(testCaseInvolvedStatementIndices.size() - statementIndices.size());
        System.out.println("---------------------");

        result = isSubset && isSuperset;

        return result;

    }

    /**
     * Exactly means not less and not more is covered
     *
     * @param testCase
     * @param branchIds
     * @param spectra
     * @return
     */
    private static boolean branchesAreExactlyCoveredByThisTestCase(ITrace<SourceCodeBlock> testCase, Collection<Integer> branchIds, ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> spectra){

        boolean result = false;

        HashSet<Integer> statementIndicesFromBranches = new HashSet<>();

        for(Integer branchId : branchIds){
            statementIndicesFromBranches.addAll(getStatementIndicesFromBranch(branchId, spectra));
        }

        result = statementsAreExactlyCoveredByThisTestCase(testCase, statementIndicesFromBranches);

        return result;

    }

    /**
     * Check if the branchId yields a list of executed statements equal to the list of statements we get from the provided statement indices.
     *
     * @param branchId
     * @param statementIndices
     * @param statementSpectra
     * @return
     */
    private static boolean isExecutedListOfStatementIndicesForThisBranch(int branchId, List<Integer> statementIndices, ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> statementSpectra){

        boolean result = false;

        TraceIterator _statementIndicesIterator = statementSpectra.getIndexer().getNodeIdSequenceIterator(branchId);
        List<Integer> _statementIndices = new ArrayList<Integer>();

        while(_statementIndicesIterator.hasNext()){
            _statementIndices.add(_statementIndicesIterator.next());
        }

        result = _statementIndices.equals(statementIndices);

        return result;

    }

    /**
     * Check if the branchId yields a list of executed statements equal to the list of statements provided as input
     *
     * @param branchId
     * @param statements
     * @param statementSpectra
     * @return
     */
    private static boolean isExecutedListOfStatementsOfThisBranch(int branchId, List<SourceCodeBlock> statements, ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> statementSpectra){

        boolean result = false;

        List<Integer> statementIndices = new ArrayList<>();

        for(SourceCodeBlock statement : statements){
            statementIndices.add(statementSpectra.getNode(statement).getIndex());
        }

        result = isExecutedListOfStatementIndicesForThisBranch(branchId, statementIndices, statementSpectra);

        return result;

    }

    /**
     * Check if the branches in this branching spectra have at most k decision points.
     * E.g. k=2 would allow a branch to cover two "if" statements.
     *
     * @param programBranchSpectra
     * @param k
     * @return
     */
    private static boolean branchesHaveAtMostKDecisionPoints(ProgramBranchSpectra programBranchSpectra, int k){

        boolean result = false;

        ProgramBranch branch;

        for(INode<ProgramBranch> branchNode : programBranchSpectra.getNodes()){
            branch = branchNode.getIdentifier();
            result = branchHasAtMostKDecisionPoints(branch, k);
            if(result == false) break;
        }

        return result;

    }

    /**
     * Check if the branch has at most k decision points.
     * E.g. k=2 would allow a branch to cover two "if" statements.
     *
     * @param branch
     * @param k
     * @return
     */
    private static boolean branchHasAtMostKDecisionPoints(ProgramBranch branch, int k){

        boolean result = false;

        int nrOfDecisionPoints = 0;
        Node.NodeType type;

        for(SourceCodeBlock statement : branch.getElements()){
            type = statement.getNodeType();
            if(type == Node.NodeType.TRUE_BRANCH || type == Node.NodeType.FALSE_BRANCH){
                nrOfDecisionPoints++;
            }
        }

        result = nrOfDecisionPoints <= k;

        return result;

    }


    /*====================================================================================
     * FIELDS
     *====================================================================================*/

}
