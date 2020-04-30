package se.de.hu_berlin.informatik.benchmark;

public interface FaultInformation {

    /**
     * Represents the suspiciousness of a fault location / the confidence
     * that a given fault location really is a fault.
     */
    public enum Suspiciousness {
        /**
         * 100% secure bug location
         */
        DEFINITIVE,
        /**
         * high suspiciousness
         */
        HIGH,
        /**
         * normal suspiciousness
         */
        NORMAL,
        /**
         * low suspiciousness
         */
        LOW,
        /**
         * unknown suspiciousness
         */
        UNKNOWN,
    }

    public void setSuspiciousness(Suspiciousness suspiciousness) throws UnsupportedOperationException;

    public Suspiciousness getSuspiciousness() throws UnsupportedOperationException;

    public void setComment(String comment) throws UnsupportedOperationException;

    public String getComment() throws UnsupportedOperationException;

}
