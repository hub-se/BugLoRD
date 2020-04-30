package se.de.hu_berlin.informatik.benchmark.api.ibugs.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Roy Lieck
 */
public class IBugsTestSuiteWrapper {

    private int failing = 0;
    private int passing = 0;
    private int size = 0;
    private List<IBugsTestResultWrapper> allTests = new ArrayList<>();
    private List<IBugsTestResultWrapper> allTestsWithErrors = new ArrayList<>();

    /**
     * @return the failing
     */
    public int getFailing() {
        return failing;
    }

    /**
     * @param failing the failing to set
     */
    public void setFailing(int failing) {
        this.failing = failing;
    }

    /**
     * @return the passing
     */
    public int getPassing() {
        return passing;
    }

    /**
     * @param passing the passing to set
     */
    public void setPassing(int passing) {
        this.passing = passing;
    }

    /**
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * @return the allTests
     */
    public List<IBugsTestResultWrapper> getAllTests() {
        return allTests;
    }

    /**
     * @param allTests the allTests to set
     */
    public void setAllTests(List<IBugsTestResultWrapper> allTests) {
        this.allTests = allTests;
    }

    /**
     * @return the allTestsWithErrors
     */
    public List<IBugsTestResultWrapper> getAllTestsWithErrors() {
        return allTestsWithErrors;
    }

    /**
     * @param allTestsWithErrors the allTestsWithErrors to set
     */
    public void setAllTestsWithErrors(List<IBugsTestResultWrapper> allTestsWithErrors) {
        this.allTestsWithErrors = allTestsWithErrors;
    }

}
