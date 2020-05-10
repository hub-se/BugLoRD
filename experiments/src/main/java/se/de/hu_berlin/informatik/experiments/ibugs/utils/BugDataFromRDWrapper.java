package se.de.hu_berlin.informatik.experiments.ibugs.utils;

/**
 * This wrapper is used for extraction of bug data from the repository descriptor file.
 * Currently it only stores the bug and transaction id.
 *
 * @author Roy Lieck
 */
public class BugDataFromRDWrapper {

    private String bugId = "";
    private String transId = "";

    public BugDataFromRDWrapper(String aId, String aTransId) {
        bugId = aId;
        transId = aTransId;
    }

    /**
     * @return the bugId
     */
    public String getBugId() {
        return bugId;
    }

    /**
     * @param bugId the bugId to set
     */
    public void setBugId(String bugId) {
        this.bugId = bugId;
    }

    /**
     * @return the transId
     */
    public String getTransId() {
        return transId;
    }

    /**
     * @param transId the transId to set
     */
    public void setTransId(String transId) {
        this.transId = transId;
    }

}
