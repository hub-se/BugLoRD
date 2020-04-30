package se.de.hu_berlin.informatik.benchmark;

import java.util.List;

public interface FaultLocations {

    public void addFaultyLine(int lineNo) throws IllegalStateException;

    public void addFaultyLine(int lineNo, FaultInformation info) throws IllegalStateException;

    public void addFaultyLine(LineWithFaultInformation line) throws IllegalStateException;

    public LineWithFaultInformation getFaultyLine(int lineNo);

    public List<LineWithFaultInformation> getFaultyLines();

    public List<Integer> getFaultyLineNumbers();

    default public boolean hasFaultInformation(int lineNo) {
        LineWithFaultInformation line = getFaultyLine(lineNo);
        if (line == null) {
            return false;
        }
        return line.hasFaultInformation();
    }

    default public FaultInformation getFaultInformation(int lineNo) throws UnsupportedOperationException {
        LineWithFaultInformation line = getFaultyLine(lineNo);
        if (line == null) {
            return null;
        }
        return line.getFaultInformation();
    }

}
