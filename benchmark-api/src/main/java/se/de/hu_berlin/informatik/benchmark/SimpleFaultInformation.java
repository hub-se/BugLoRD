package se.de.hu_berlin.informatik.benchmark;

/**
 * Represents a real fault location
 */
public class SimpleFaultInformation implements FaultInformation {

    /**
     * suspiciousness of this line / confidence that this line really is a fault
     */
    private Suspiciousness suspiciousness;
    /**
     * comment why this line is suspicious
     */
    private String comment;

    /**
     * Create new line
     *
     * @param suspiciousness suspiciousness of this line / confidence that this line really is a fault
     * @param comment        comment why this line is suspicious
     */
    public SimpleFaultInformation(final Suspiciousness suspiciousness, final String comment) {
        super();
        this.suspiciousness = suspiciousness;
        this.comment = comment;
    }

    /**
     * suspiciousness of this line / confidence that this line really is a fault
     *
     * @return the suspiciousness
     */
    public Suspiciousness getSuspiciousness() {
        return this.suspiciousness;
    }

    /**
     * comment why this line is suspicious
     *
     * @return the comment
     */
    public String getComment() {
        return this.comment;
    }

    @Override
    public void setSuspiciousness(Suspiciousness suspiciousness) {
        this.suspiciousness = suspiciousness;
    }

    @Override
    public void setComment(String comment) throws UnsupportedOperationException {
        this.comment = comment;
    }

}
