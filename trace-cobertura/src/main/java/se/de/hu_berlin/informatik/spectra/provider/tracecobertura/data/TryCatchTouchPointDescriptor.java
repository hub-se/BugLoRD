package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data;


import java.util.concurrent.atomic.AtomicInteger;


/**
 * Class representing a touch-point connected to a try catch block label in source-code.
 */
public class TryCatchTouchPointDescriptor extends TouchPointDescriptor {
//	private int counterIdForTrue;
//	private int counterIdForFalse;

    public TryCatchTouchPointDescriptor(int eventId, int currentLine) {
        super(eventId, currentLine);
    }

//	public int getCounterIdForFalse() {
//		return counterIdForFalse;
//	}
//
//	public int getCounterIdForTrue() {
//		return counterIdForTrue;
//	}
//
//	public void setCounterIdForFalse(int counterIdForFalse) {
//		this.counterIdForFalse = counterIdForFalse;
//	}
//
//	public void setCounterIdForTrue(int counterIdForTrue) {
//		this.counterIdForTrue = counterIdForTrue;
//	}

    @Override
    public int assignCounters(AtomicInteger idGenerator) {
//		counterIdForFalse = idGenerator.incrementAndGet();
//		counterIdForTrue = idGenerator.incrementAndGet();
//		return 2;
        return 0;
    }

}
