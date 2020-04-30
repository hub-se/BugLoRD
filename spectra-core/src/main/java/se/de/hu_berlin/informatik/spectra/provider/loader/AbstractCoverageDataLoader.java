package se.de.hu_berlin.informatik.spectra.provider.loader;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;

public abstract class AbstractCoverageDataLoader<T, K extends ITrace<T>, D> implements ICoverageDataLoader<T, K, D> {

    protected void onNewPackage(String packageName, K currentTrace) {
        // nothing to do
    }

    protected void onNewClass(String packageName, String classFilePath, K currentTrace) {
        // nothing to do
    }

    protected void onNewMethod(String packageName, String classFilePath, String methodName, K currentTrace) {
        // nothing to do
    }

    protected void onNewLine(String packageName, String classFilePath, String methodName, T lineIdentifier,
                             ISpectra<T, K> lineSpectra, K currentTrace, boolean fullSpectra, long numberOfHits) {
        if (numberOfHits > 0) {
            currentTrace.setInvolvement(lineIdentifier, true);
        } else if (fullSpectra) {
            lineSpectra.getOrCreateNode(lineIdentifier);
        }
    }

    protected void onLeavingPackage(String packageName, ISpectra<T, K> lineSpectra, K currentTrace) {
        // nothing to do
    }

    protected void onLeavingClass(String packageName, String sourceFilePath, ISpectra<T, K> lineSpectra,
                                  K currentTrace) {
        // nothing to do
    }

    protected void onLeavingMethod(String packageName, String sourceFilePath, String methodIdentifier,
                                   ISpectra<T, K> lineSpectra, K currentTrace) {
        // nothing to do
    }

}
