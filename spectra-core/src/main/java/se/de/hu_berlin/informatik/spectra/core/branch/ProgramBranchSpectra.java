package se.de.hu_berlin.informatik.spectra.core.branch;

import se.de.hu_berlin.informatik.spectra.core.hit.HitSpectra;

import java.nio.file.Path;

public class ProgramBranchSpectra extends HitSpectra<ProgramBranch> {

    /*====================================================================================
     * CONSTRUCTORS
     *====================================================================================*/

    public ProgramBranchSpectra(Path spectraZipFile) {
        super(spectraZipFile);
    }

//    /*====================================================================================
//     * PUBLIC
//     *====================================================================================*/
//
//    public INode<ProgramBranch> getBranchNode(int branchId) {
//
//        INode<ProgramBranch> branchNode;
//
//        branchNode = branchIdMap.get(branchId);
//
//        return branchNode;
//
//    }
//
//    /*====================================================================================
//     * PRIVATE
//     *====================================================================================*/
//
//    public void setBranchIdMap(Map<Integer, INode<ProgramBranch>> branchIdMap) {
//
//        /*====================================================================================*/
//        assert (branchIdMap != null);
//        /*====================================================================================*/
//
//        this.branchIdMap = ImmutableMap.copyOf(branchIdMap);
//
//    }
//
//    /*====================================================================================
//     * FIELDS
//     *====================================================================================*/
//
//    //branches in the execution traces are only identified by an integer id
//    //--> this map maps the id to the actual node representing the branch
//    private ImmutableMap<Integer, INode<ProgramBranch>> branchIdMap;

}
