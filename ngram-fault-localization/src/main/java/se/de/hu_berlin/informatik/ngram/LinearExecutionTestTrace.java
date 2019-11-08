package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;

import java.util.ArrayList;
import java.util.Collection;

public class LinearExecutionTestTrace {
    private int testID;
    private ISpectra spectra;
    private Collection<LinearBlockSequence> traces;

    public LinearExecutionTestTrace(int testID, ISpectra spectra) {
        this.testID = testID;
        this.spectra = spectra;
        traces = new ArrayList<>();
    }

    public int getTestID() {
        return testID;
    }

    public Collection<LinearBlockSequence> getTraces() {
        return traces;
    }
    @Override
    public String toString(){
        return "LEBTrace{" + "testID=" + testID + ", traces=" + traces + '}';
    }
}
