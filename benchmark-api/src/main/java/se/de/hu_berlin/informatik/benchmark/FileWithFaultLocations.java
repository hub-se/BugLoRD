package se.de.hu_berlin.informatik.benchmark;

import java.util.List;

public interface FileWithFaultLocations {

    /**
     * Returns the filename.
     *
     * @return the name
     */
    public String getFileName();

    /**
     * Returns the class name of this file
     *
     * @return class name
     */
    public String getClassName();

    public boolean hasFaultLocations();

    public FaultLocations getFaultLocations() throws UnsupportedOperationException;

    public boolean setFaultLocations(FaultLocations faultLocations);


    default public void addFaultyLine(int lineNo) throws IllegalStateException {
        getFaultLocations().addFaultyLine(lineNo);
    }

    default public void addFaultyLine(int lineNo, FaultInformation info) throws IllegalStateException {
        getFaultLocations().addFaultyLine(lineNo, info);
    }

    default public void addFaultyLine(LineWithFaultInformation line) throws IllegalStateException {
        getFaultLocations().addFaultyLine(line);
    }

    default public List<Integer> getFaultyLineNumbers() {
        return getFaultLocations().getFaultyLineNumbers();
    }

    default public List<LineWithFaultInformation> getFaultyLines() {
        return getFaultLocations().getFaultyLines();
    }

    default public boolean hasFaultInformation(int lineNo) {
        return getFaultLocations().hasFaultInformation(lineNo);
    }

    default public FaultInformation getFaultInformation(int line) throws UnsupportedOperationException {
        return getFaultLocations().getFaultInformation(line);
    }

}
