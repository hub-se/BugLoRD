package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data;

public interface BranchCoverageData {

    double getBranchCoverageRate();

    int getNumberOfCoveredBranches();

    int getNumberOfValidBranches();

    /**
     * Warning: This is generally implemented as a
     * "shallow" merge.  For our current use, this
     * should be fine, but in the future it may make
     * sense to modify the merge methods of the
     * various classes to do a deep copy of the
     * appropriate objects.
     *
     * @param coverageData coverage data to merge with
     */
    void merge(BranchCoverageData coverageData);
}
