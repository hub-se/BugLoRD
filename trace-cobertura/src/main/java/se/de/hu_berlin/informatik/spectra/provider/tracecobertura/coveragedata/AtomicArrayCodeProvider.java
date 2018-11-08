package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import org.objectweb.asm.*;

import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * <p>The {@link CodeProvider} uses {@link AtomicArrayCodeProvider} to store counters.</p>
 * 
 * This implementation is totally thread-safe, but significantly slower then {@link FastArrayCodeProvider}.
 *
 * @author piotr.tabor@gmail.com
 */
@Deprecated
public class AtomicArrayCodeProvider extends AbstractCodeProvider
		implements
			CodeProvider {
	/**
	 * Type of the generated field, that is used to store counters
	 */
	static final String COBERTURA_COUNTERS_FIELD_TYPE = Type.getType(
			AtomicIntegerArray.class).toString();
	private boolean collectExecutionTrace;

	public AtomicArrayCodeProvider(boolean collectExecutionTrace) {
		this.collectExecutionTrace = collectExecutionTrace;
	}

	public void generateCountersField(ClassVisitor cv) {
		FieldVisitor fv = cv.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC
				| Opcodes.ACC_FINAL | Opcodes.ACC_TRANSIENT,
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE,
				null, null);
		fv.visitEnd();
	}

	@SuppressWarnings("deprecation")
	public void generateCINITmethod(MethodVisitor mv, String className,
			int counters_cnt) {
		mv.visitFieldInsn(Opcodes.GETSTATIC, className,
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		Label l1 = new Label();
		mv.visitJumpInsn(Opcodes.IFNONNULL, l1);

		mv.visitTypeInsn(Opcodes.NEW, Type
				.getInternalName(AtomicIntegerArray.class));
		mv.visitInsn(Opcodes.DUP);
		mv.visitLdcInsn(counters_cnt);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type
				.getInternalName(AtomicIntegerArray.class), "<init>", "(I)V");
		mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		generateRegisterClass(mv, className);
		mv.visitLabel(l1);
	}

	@SuppressWarnings("deprecation")
	public void generateCodeThatIncrementsCoberturaCounter(
			MethodVisitor nextMethodVisitor, int counterId, String className) {
		/*cobertura_counters.incrementAndGet(i);*/
		/*cobertura_counters.*/
		nextMethodVisitor.visitFieldInsn(Opcodes.GETSTATIC, className,
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		/*index:*/
		nextMethodVisitor.visitLdcInsn(counterId);
		nextMethodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type
				.getInternalName(AtomicIntegerArray.class), "incrementAndGet",
				"(I)I");
		nextMethodVisitor.visitInsn(Opcodes.POP);
		
		if (collectExecutionTrace) {
			// add the statement to the execution trace... TODO
			nextMethodVisitor.visitLdcInsn(className);
			nextMethodVisitor.visitLdcInsn(counterId);
			nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
					.getInternalName(ExecutionTraceCollector.class), "addStatementToExecutionTrace",
					"(Ljava/lang/String;I)V");
		}
	}
	
	public void generateCodeThatIncrementsCoberturaCounterAfterJump(
			MethodVisitor nextMethodVisitor, int counterId, String className) {
		generateCodeThatIncrementsCoberturaCounter(nextMethodVisitor, counterId, className);
	}
	
	public void generateCodeThatIncrementsCoberturaCounterAfterSwitchLabel(
			MethodVisitor nextMethodVisitor, int counterId, String className) {
		generateCodeThatIncrementsCoberturaCounter(nextMethodVisitor, counterId, className);
	}

	@SuppressWarnings("deprecation")
	public void generateCodeThatIncrementsCoberturaCounterFromInternalVariable(
			MethodVisitor nextMethodVisitor, int lastJumpIdVariableIndex,
			String className) {
		/*cobertura_counters.incrementAndGet(value('lastJumpIdVariableIndex'));*/
		/*cobertura_counters.*/
		nextMethodVisitor.visitFieldInsn(Opcodes.GETSTATIC, className,
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		/*index:*/
		nextMethodVisitor.visitVarInsn(Opcodes.ILOAD, lastJumpIdVariableIndex);
		nextMethodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type
				.getInternalName(AtomicIntegerArray.class), "incrementAndGet",
				"(I)I");
		nextMethodVisitor.visitInsn(Opcodes.POP);

		if (collectExecutionTrace) {
			// add the statement to the execution trace... TODO
			nextMethodVisitor.visitLdcInsn(className);
			nextMethodVisitor.visitLdcInsn(lastJumpIdVariableIndex);
			nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
					.getInternalName(ExecutionTraceCollector.class), "addStatementToExecutionTrace",
					"(Ljava/lang/String;I)V");
		}
	}

	/*
	 * <pre>
	 * int[] __cobertura_get_and_reset_counters() {
	 * int[] res = new int[counters.length()];
	 * for(int i=0; i<counters.length(); i++){
	 * res[i]=counters.getAndSet(i, 0);
	 * }
	 * return res;
	 * }
	 * </pre>
	 */
	@SuppressWarnings("deprecation")
	public void generateCoberturaGetAndResetCountersMethod(ClassVisitor cv,
			String className) {
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC
				| Opcodes.ACC_STATIC,
				COBERTURA_GET_AND_RESET_COUNTERS_METHOD_NAME, "()[I", null,
				null);

		mv.visitCode();
		mv.visitFieldInsn(Opcodes.GETSTATIC, className,
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
				"java/util/concurrent/atomic/AtomicIntegerArray", "length",
				"()I");
		mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
		mv.visitVarInsn(Opcodes.ASTORE, 0);
		mv.visitInsn(Opcodes.ICONST_0);
		mv.visitVarInsn(Opcodes.ISTORE, 1);
		Label l3 = new Label();
		mv.visitJumpInsn(Opcodes.GOTO, l3);
		Label l4 = new Label();
		mv.visitLabel(l4);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ILOAD, 1);
		mv.visitFieldInsn(Opcodes.GETSTATIC, className,
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		mv.visitVarInsn(Opcodes.ILOAD, 1);
		mv.visitInsn(Opcodes.ICONST_0);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
				"java/util/concurrent/atomic/AtomicIntegerArray", "getAndSet",
				"(II)I");
		mv.visitInsn(Opcodes.IASTORE);
		mv.visitIincInsn(1, 1);
		mv.visitLabel(l3);
		mv.visitVarInsn(Opcodes.ILOAD, 1);
		mv.visitFieldInsn(Opcodes.GETSTATIC, className,
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
				"java/util/concurrent/atomic/AtomicIntegerArray", "length",
				"()I");
		mv.visitJumpInsn(Opcodes.IF_ICMPLT, l4);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(0, 0);//will be recalculated by writer
		mv.visitEnd();
	}

}

