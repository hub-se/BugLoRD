package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data;


import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class representing a touch-point connected to a single line of source-code
 * 
 * <p>A LINE touch-point have assigned only one counter.</p>
 * 
 * <p>We also storing a {@link #methodName} and a {@link #methodSignature} (consider to move this fields into {@link TouchPointDescriptor}).
 * Those fields are needed to properly create instance of {@code LineData}. </p>
 *
 * @author piotr.tabor@gmail.com
 */
public class LineTouchPointDescriptor extends TouchPointDescriptor {
	private Integer counterId;

	/**
	 * Name of a method, the line belongs to
	 */
	private final String methodName;

	/**
	 * Signature (description) of a method, the line belongs to.
	 */
	private final String methodSignature;

	public LineTouchPointDescriptor(int eventId, int lineNumber,
			String methodName, String methodSignature) {
		super(eventId, lineNumber);
		this.methodName = methodName;
		this.methodSignature = methodSignature;
	}

	@Override
	public int assignCounters(AtomicInteger idGenerator) {
		counterId = idGenerator.incrementAndGet();
		return 1;
	}

	public Integer getCounterId() {
		return counterId;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getMethodSignature() {
		return methodSignature;
	}
}

