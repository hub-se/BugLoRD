package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;

import java.util.ArrayList;
import java.util.Collection;
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

    public int getBlockCount() {
        HashSet<Integer> counting = new HashSet<>();
        traces.forEach(t -> {
            t.getBlockSeq().forEach(b -> counting.add(b.getIndex()));
        });
        return counting.size();
    }

    public HashSet<Integer> getInvolvedBlocks() {
        HashSet<Integer> nodes = new HashSet<>();
        traces.forEach(t -> {
            t.getBlockSeq().forEach(b -> nodes.add(b.getIndex()));
        });
        return nodes;
    }

    @Override
    public String toString() {
        return "LEBTrace{" + "testID=" + testID + ", traces=" + traces + '}';
    }
}
