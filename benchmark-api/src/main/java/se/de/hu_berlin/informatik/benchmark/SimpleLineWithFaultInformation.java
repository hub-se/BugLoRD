package se.de.hu_berlin.informatik.benchmark;

import se.de.hu_berlin.informatik.benchmark.FaultInformation.Suspiciousness;

/**
 * Represents a real fault location
 */
public class SimpleLineWithFaultInformation implements LineWithFaultInformation {
    /**
     * line number
     */
    private final int lineNumber;

    private FaultInformation faultInformation;

    /**
     * Create new line
     *
     * @param line line number
     */
    public SimpleLineWithFaultInformation(final int line) {
        super();
        this.lineNumber = line;
    }

    /**
     * Create new line
     *
     * @param line           line number
     * @param suspiciousness suspiciousness of this line / confidence that this line really is a fault
     * @param comment        comment why this line is suspicious
     */
    public SimpleLineWithFaultInformation(final int line, final Suspiciousness suspiciousness, final String comment) {
        super();
        this.lineNumber = line;
        this.faultInformation = new SimpleFaultInformation(suspiciousness, comment);
    }

    /**
     * Create new line
     *
     * @param line        line number
     * @param information fault information
     */
    public SimpleLineWithFaultInformation(final int line, FaultInformation information) {
        super();
        this.lineNumber = line;
        this.faultInformation = information;
    }


    @Override
    public int getLineNo() {
        return this.lineNumber;
    }


    @Override
    public boolean hasFaultInformation() {
        return faultInformation != null;
    }

    @Override
    public FaultInformation getFaultInformation() throws UnsupportedOperationException {
        return faultInformation;
    }

    @Override
    public boolean setFaultInformation(FaultInformation information) {
        if (faultInformation == null && information != null) {
            faultInformation = information;
            return true;
        } else {
            return false;
        }
    }

}
