package se.de.hu_berlin.informatik.benchmark;

import se.de.hu_berlin.informatik.benchmark.FaultInformation.Suspiciousness;

public interface LineWithFaultInformation {

    //	public void setLineNo(int line);
    public int getLineNo();

    public boolean hasFaultInformation();

    public FaultInformation getFaultInformation() throws UnsupportedOperationException;

    public boolean setFaultInformation(FaultInformation information);

    default public void setSuspiciousness(Suspiciousness suspiciousness) throws UnsupportedOperationException {
        getFaultInformation().setSuspiciousness(suspiciousness);
    }

    default public Suspiciousness getSuspiciousness() throws UnsupportedOperationException {
        return getFaultInformation().getSuspiciousness();
    }

    default public void setComment(String comment) throws UnsupportedOperationException {
        getFaultInformation().setComment(comment);
    }

    default public String getComment() throws UnsupportedOperationException {
        return getFaultInformation().getComment();
    }

}
