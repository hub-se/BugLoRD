package se.de.hu_berlin.informatik.benchmark.api.ibugs.parser;

/**
 * @author Roy Lieck
 */
public class IBugsTestResultWrapper {

    private final String FILENAME; // looks like this is almost always a xml file
    private final String JARFILE;
    private final String NAME;

    // did the test run cause errors?
    private boolean finishedWithError = false;

    public IBugsTestResultWrapper(String aTestFile, String aJarFile, String aName) {
        FILENAME = aTestFile;
        JARFILE = aJarFile;
        NAME = aName;
    }

    /**
     * @param errorneous the finishedWithError to set
     */
    public void finishedWithError(boolean errorneous) {
        finishedWithError = errorneous;
    }

    /**
     * @return the finishedWithError
     */
    public boolean hasFinishedWithError() {
        return finishedWithError;
    }

    /**
     * @return the fILENAME
     */
    public String getFILENAME() {
        return FILENAME;
    }

    /**
     * @return the jARFILE
     */
    public String getJARFILE() {
        return JARFILE;
    }

    /**
     * @return the nAME
     */
    public String getNAME() {
        return NAME;
    }

    public String toString() {
        return "Filename[\"" + FILENAME + "\"],Jarfile[\"" + JARFILE + "\"], Name[\"" + NAME + "\"]";
    }

}
