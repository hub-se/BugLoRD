package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.ComputationStrategies;
import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;

public class NGramNode implements INode {
    @Override
    public Object getIdentifier() {
        return null;
    }

    @Override
    public int getIndex() {
        return 0;
    }

    @Override
    public double getNP(ComputationStrategies strategy) {
        return 0;
    }

    @Override
    public double getNF(ComputationStrategies strategy) {
        return 0;
    }

    @Override
    public double getEP(ComputationStrategies strategy) {
        return 0;
    }

    @Override
    public double getEF(ComputationStrategies strategy) {
        return 0;
    }

    @Override
    public double getNP() {
        return 0;
    }

    @Override
    public double getNF() {
        return 0;
    }

    @Override
    public double getEP() {
        return 0;
    }

    @Override
    public double getEF() {
        return 0;
    }

    @Override
    public void invalidateCachedValues() {

    }

    @Override
    public ISpectra getSpectra() {
        return null;
    }
}
