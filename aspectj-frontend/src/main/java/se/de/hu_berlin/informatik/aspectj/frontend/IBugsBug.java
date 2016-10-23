package se.de.hu_berlin.informatik.aspectj.frontend;

import se.de.hu_berlin.informatik.benchmark.SimpleBug;

/**
 * Holds all faulty files for an iBugs bug
 */
public class IBugsBug extends SimpleBug {
    /** the iBugs bug id of the bug */
    private final int id;
    
    /**
     * Creates a new bug
     *
     * @param bugId
     *            iBugs bug id of this bug
     */
    public IBugsBug(final int bugId) {
        super();
        this.id = bugId;
    }

    /**
     * return the iBugs bug id for this bug
     *
     * @return the id
     */
    public int getId() {
        return this.id;
    }

}
