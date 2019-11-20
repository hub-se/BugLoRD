package se.de.hu_berlin.informatik.faultlocalizer.ngram;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;

import java.util.ArrayList;
import java.util.HashSet;

public class LinearExecutionTestTrace {
    private int testID;
    private ISpectra spectra;
    private ArrayList<LinearBlockSequence> traces;
    private boolean isSuccessful;

    public LinearExecutionTestTrace(int testID, ISpectra spectra, boolean isSuccessful) {
        this.testID = testID;
        this.spectra = spectra;
        this.isSuccessful = isSuccessful;
        traces = new ArrayList<>();
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public int getTestID() {
        return testID;
    }

    public ArrayList<LinearBlockSequence> getTraces() {
        return traces;
    }

    public LinearBlockSequence getTrace(int index) {
        return traces.get(index);
    }


    @Override
    public String toString() {
        return "LEBTrace{" + "testID=" + testID + ", traces=" + traces + '}';
    }

    public HashSet<Integer> getInvolvedBlocks() {

        HashSet<Integer> allBlocks = new HashSet<>();
        traces.forEach(t -> {
            allBlocks.addAll(t.involvedBlocks());
        });
        return allBlocks;
    }
}
