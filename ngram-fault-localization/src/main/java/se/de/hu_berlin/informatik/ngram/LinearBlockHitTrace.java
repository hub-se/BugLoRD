package se.de.hu_berlin.informatik.ngram;


import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.hit.HitTrace;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

public class LinearBlockHitTrace<T>implements ITrace<T> {


    @Override
    public boolean isSuccessful() {
        return false;
    }

    @Override
    public void setInvolvement(T identifier, boolean involved) {

    }

    @Override
    public void setInvolvement(INode<T> node, boolean involved) {

    }

    @Override
    public void setInvolvement(int index, boolean involved) {

    }

    @Override
    public void setInvolvementForIdentifiers(Map<T, Boolean> involvement) {

    }

    @Override
    public void setInvolvementForNodes(Map<INode<T>, Boolean> involvement) {

    }

    @Override
    public boolean isInvolved(INode<T> node) {
        return false;
    }

    @Override
    public boolean isInvolved(T identifier) {
        return false;
    }

    @Override
    public boolean isInvolved(int index) {
        return false;
    }

    @Override
    public int involvedNodesCount() {
        return 0;
    }

    @Override
    public Collection<Integer> getInvolvedNodes() {
        return null;
    }

    @Override
    public Collection<ExecutionTrace> getExecutionTraces() {
        return null;
    }

    @Override
    public void addExecutionTrace(ExecutionTrace executionTrace) {

    }

    @Override
    public String getIdentifier() {
        return null;
    }

    @Override
    public int getIndex() {
        return 0;
    }

    @Override
    public boolean storeExecutionTracesInZipFile(Path outputFile, Supplier<String> traceFileNameSupplier, Supplier<String> repMarkerFileNameSupplier) {
        return false;
    }
}
