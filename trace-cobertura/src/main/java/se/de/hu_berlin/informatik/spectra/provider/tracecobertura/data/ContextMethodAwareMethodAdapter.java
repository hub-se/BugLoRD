package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract implementation of {@link MethodVisitor} that:
 * <ul>
 * <li>provides information about {@link #className},{@link #methodName} and {@link #methodSignature} of method currently being instrumented/analyzed</li>
 * <li>Assign line identifiers (see {@link AbstractFindTouchPointsClassInstrumenter#lineIdGenerator} to every LINENUMBER asm instruction found</li>
 * </ul>
 *
 * @author ptab
 */
public abstract class ContextMethodAwareMethodAdapter extends MethodVisitor {
	protected final String className;
	protected final String methodName;
	protected final String methodSignature;

	/**
	 * What was the last lineId assigned. We can read this field to know which line (by identifier) we are currently analyzing
	 */
	protected int lastLineId;

	/**
	 * Generator that assigns unique (in scope of single class) identifiers to every LINENUMBER asm derective.
	 * 
	 * <p>We will use this 'generator' to provide this identifiers. Remember to acquire identifiers using {@link AtomicInteger#incrementAndGet()} (not {@link AtomicInteger#getAndIncrement()}!!!)</p>
	 */
	protected final AtomicInteger lineIdGenerator;

	public ContextMethodAwareMethodAdapter(MethodVisitor mv, String className,
			String methodName, String methodSignature,
			AtomicInteger lineIdGenerator) {
		super(Opcodes.ASM4, mv);
		this.className = className;
		this.methodName = methodName;
		this.methodSignature = methodSignature;
		lastLineId = 0;
		this.lineIdGenerator = lineIdGenerator;
	}

	@Override
	public void visitLineNumber(int number, Label label) {
		lastLineId = lineIdGenerator.incrementAndGet();
		super.visitLineNumber(number, label);
	}

}
