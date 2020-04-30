package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data;


import java.util.concurrent.atomic.AtomicInteger;


/**
 * Class representing a touch-point connected to a JUMP instruction in source-code.
 *
 * <p>A JUMP touch-point have assigned two counters:</p>
 * <ul>
 * <li>TRUE - touched in case when jump condition is meet</li>
 * <li>FALSE - touched when jump condition is not meet</li>
 * </ul>
 *
 * @author piotr.tabor@gmail.com
 */
public class JumpTouchPointDescriptor extends TouchPointDescriptor {
    private int counterIdForTrue;
    private int counterIdForFalse;

    public JumpTouchPointDescriptor(int eventId, int currentLine) {
        super(eventId, currentLine);
    }

    public int getCounterIdForFalse() {
        return counterIdForFalse;
    }

    public int getCounterIdForTrue() {
        return counterIdForTrue;
    }

    public void setCounterIdForFalse(int counterIdForFalse) {
        this.counterIdForFalse = counterIdForFalse;
    }

    public void setCounterIdForTrue(int counterIdForTrue) {
        this.counterIdForTrue = counterIdForTrue;
    }

    @Override
    public int assignCounters(AtomicInteger idGenerator) {
        counterIdForFalse = idGenerator.incrementAndGet();
        counterIdForTrue = idGenerator.incrementAndGet();
        return 2;
    }

}
