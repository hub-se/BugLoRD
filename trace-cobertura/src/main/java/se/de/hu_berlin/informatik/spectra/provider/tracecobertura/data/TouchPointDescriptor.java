package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data;


import java.util.concurrent.atomic.AtomicInteger;

/**
 * Root class for all touch-points (points in source-code that we want to monitor)
 *
 * @author piotr.tabor@gmail.com
 */
public abstract class TouchPointDescriptor {

    public TouchPointDescriptor(int eventId, int lineNumber) {
        this.eventId = eventId;
        this.lineNumber = lineNumber;
    }

    /**
     * eventId (asm code identifier) of the interesting instruction
     */
    private int eventId;

    /**
     * Number of line in which the touch-point is localized
     */
    private int lineNumber;

    /**
     * @return eventId (asm code identifier) of the interesting instruction
     */
    public int getEventId() {
        return eventId;
    }

    /**
     * Sets eventId (asm code identifier) of the interesting instruction
     *
     * @param eventId event id
     */

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    /**
     * @return number of line in which the touch-point is localized
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * @param lineNumber number of line in which the touch-point is localized
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * Every touch-point will have assigned some counters. This methods assigne the ids to the touch-point
     * using given idGenerator
     *
     * @param idGenerator id generator
     * @return number of used 'ids' for the touch-point.
     */
    public abstract int assignCounters(AtomicInteger idGenerator);
}
