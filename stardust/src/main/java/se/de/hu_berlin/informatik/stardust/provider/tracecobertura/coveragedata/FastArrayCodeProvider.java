package se.de.hu_berlin.informatik.stardust.provider.tracecobertura.coveragedata;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


/*
 * <p>The {@link CodeProvider} uses int[] to store counters.</p>
 * 
 * <p>This implementation is not fully thread-safe, but significantly (10-100x) then {@link AtomicArrayCodeProvider}.</p>
 * 
 * <p>What does it mean 'not fully thead-safe' ?
 * <ul>
 * <li>Using this provider will never cause throwing any exception because of concurrency problems</li>
 * <li>A code coverage results acquired using this code-provider will be exactly the same as using thread-safe provider)</li> *
 * <li>There could happen small (experiments showed around 1-3%) in value of specific counters because of race-condition.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * The reason of the race condition is fact that instruction: __cobertura_counters[counter_id]++ is translated into
 * sequence of operations: <ol>
 * <li>get value of __cobertura_counters[counter_id]</li>
 * <li>increment value</li>
 * <li>store value into __cobertura_counters[counter_id]</li>
 * </ol>
 * This mean that in case of race condition we can miss some increments. But if a counter was hit at least once, we
 * are sure that we will increment the counter at least one. For code coverage results fact of being hit is crucial.
 * </p>
 *
 * @author piotr.tabor@gmail.com
 */
public class FastArrayCodeProvider extends AbstractCodeProvider
		implements
			CodeProvider {

	/**
	 * Type of the generated field, that is used to store counters
	 */
	static final String COBERTURA_COUNTERS_FIELD_TYPE = "[I";

	@SuppressWarnings("deprecation")
	public void generateCodeThatIncrementsCoberturaCounterFromInternalVariable(
			MethodVisitor nextMethodVisitor, int lastJumpIdVariableIndex,
			String className) {
		/*cobertura_counters[value('lastJumpIdVariableIndex')]++;*/
		/*cobertura_counters.*/
		nextMethodVisitor.visitFieldInsn(Opcodes.GETSTATIC, className,
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		/*index:*/
		nextMethodVisitor.visitVarInsn(Opcodes.ILOAD, lastJumpIdVariableIndex);
		nextMethodVisitor.visitInsn(Opcodes.DUP2);
		nextMethodVisitor.visitInsn(Opcodes.IALOAD);
		nextMethodVisitor.visitLdcInsn(1);
		nextMethodVisitor.visitInsn(Opcodes.IADD);
		nextMethodVisitor.visitInsn(Opcodes.IASTORE);

		// add the statement to the execution trace... TODO
		nextMethodVisitor.visitLdcInsn(className);
		nextMethodVisitor.visitLdcInsn(lastJumpIdVariableIndex);
		nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
				.getInternalName(ExecutionTraceCollector.class), "addStatementToExecutionTrace",
				"(Ljava/lang/String;I)V");
	}

	@SuppressWarnings("deprecation")
	public void generateCodeThatIncrementsCoberturaCounter(
			MethodVisitor nextMethodVisitor, Integer counterId, String className) {
		/*cobertura_counters[value('lastJumpIdVariableIndex')]++;*/
		/*cobertura_counters.*/
		nextMethodVisitor.visitFieldInsn(Opcodes.GETSTATIC, className,
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		/*index:*/
		nextMethodVisitor.visitLdcInsn((int) counterId);
		nextMethodVisitor.visitInsn(Opcodes.DUP2);
		nextMethodVisitor.visitInsn(Opcodes.IALOAD);
		nextMethodVisitor.visitLdcInsn(1);
		nextMethodVisitor.visitInsn(Opcodes.IADD);
		nextMethodVisitor.visitInsn(Opcodes.IASTORE);

		// add the statement to the execution trace... TODO
		nextMethodVisitor.visitLdcInsn(className);
		nextMethodVisitor.visitLdcInsn((int) counterId);
		nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
				.getInternalName(ExecutionTraceCollector.class), "addStatementToExecutionTrace",
				"(Ljava/lang/String;I)V");
	}

	public void generateCountersField(ClassVisitor cv) {
		/*final tooks 270ms, no-modifier 310ms, volatile 500ms*/
		FieldVisitor fv = cv.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC
				| Opcodes.ACC_FINAL | Opcodes.ACC_TRANSIENT,
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE,
				null, null);
		fv.visitEnd();
	}

	//	static int x[];
	//	
	//	static void abc() {
	//		if (x == null) {
	//			x = new int[5];
	//		}
	//	}

	public void generateCINITmethod(MethodVisitor mv, String className,
			int counters_cnt) {
		mv.visitFieldInsn(Opcodes.GETSTATIC, className,
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		Label l1 = new Label();
		mv.visitJumpInsn(Opcodes.IFNONNULL, l1);
		mv.visitLdcInsn(counters_cnt);
		mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
		mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		generateRegisterClass(mv, className);
		mv.visitLabel(l1);
	}

	public void generateCoberturaGetAndResetCountersMethod(ClassVisitor cv,
			String className) {
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC
				| Opcodes.ACC_STATIC,
				COBERTURA_GET_AND_RESET_COUNTERS_METHOD_NAME, "()[I", null,
				null);
		mv.visitCode();
		/*cobertura_counters.*/
		mv.visitFieldInsn(Opcodes.GETSTATIC, className,
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		mv.visitVarInsn(Opcodes.ASTORE, 0);
		/*cobertura_counters.*/
		mv.visitFieldInsn(Opcodes.GETSTATIC, className,
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		mv.visitInsn(Opcodes.ARRAYLENGTH);

		mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
		mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(0, 0);//will be recalculated by writer
		mv.visitEnd();
	}

}
