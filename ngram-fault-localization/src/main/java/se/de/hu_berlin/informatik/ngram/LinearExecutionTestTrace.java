package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;

import java.util.ArrayList;
import java.util.List;

public class LinearExecutionTestTrace {
    private int testID;
    private String idString;
    private ISpectra spectra;
    private ArrayList<LinearBlockSequence> traces;
    private boolean isSuccessful;

    public LinearExecutionTestTrace(int testID, String idString, ISpectra spectra, boolean isSuccessful) {
        this.testID = testID;
        this.idString = idString;
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

    public List<Integer> getInvolvedBlocks() {

        List<Integer> allBlocks = new ArrayList<>(spectra.getTrace(idString).involvedNodesCount());
        traces.forEach(t -> {
            allBlocks.addAll(t.involvedBlocks());
        });
        return allBlocks;
    }
}
