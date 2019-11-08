package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;

import java.util.HashMap;

public class LinearExecutionHitTrace {
    public HashMap<Integer, LinearExecutionTestTrace> getTestTrace() {
        return TestTrace;
    }

    private HashMap<Integer, LinearExecutionTestTrace> TestTrace;
    private ISpectra spectra;

    public LinearExecutionHitTrace( ISpectra spectra) {
        TestTrace = new HashMap<>();
        this.spectra = spectra;
    }
    @Override
    public String toString() {
        return "LEBHitTrace{" + "TestTrace=" + TestTrace + '}';
    }
}
