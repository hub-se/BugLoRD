package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


public class ExecutionTracesOnlyCodeProvider extends AbstractCodeProvider
		implements
			CodeProvider {

	private boolean collectExecutionTrace;

	public ExecutionTracesOnlyCodeProvider(boolean collectExecutionTrace) {
		this.collectExecutionTrace = collectExecutionTrace;
	}

	@SuppressWarnings("deprecation")
	public void generateCodeThatIncrementsCoberturaCounterFromInternalVariable(
			MethodVisitor nextMethodVisitor, int lastJumpIdVariableIndex,
			String className) {
		if (collectExecutionTrace) {
			// add the statement to the execution trace... TODO
			nextMethodVisitor.visitLdcInsn(className);
			nextMethodVisitor.visitLdcInsn(lastJumpIdVariableIndex);
			nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
					.getInternalName(ExecutionTraceCollector.class), "addStatementToExecutionTrace",
					"(Ljava/lang/String;I)V");
		}
	}

	@SuppressWarnings("deprecation")
	public void generateCodeThatIncrementsCoberturaCounter(
			MethodVisitor nextMethodVisitor, Integer counterId, String className) {
		if (collectExecutionTrace) {
			// add the statement to the execution trace... TODO
			nextMethodVisitor.visitLdcInsn(className);
			nextMethodVisitor.visitLdcInsn((int) counterId);
			nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
					.getInternalName(ExecutionTraceCollector.class), "addStatementToExecutionTrace",
					"(Ljava/lang/String;I)V");
		}
	}

	public void generateCountersField(ClassVisitor cv) {
	}

	public void generateCINITmethod(MethodVisitor mv, String className,
			int counters_cnt) {
//		generateRegisterClass(mv, className);
	}

	public void generateCoberturaGetAndResetCountersMethod(ClassVisitor cv,
			String className) {
//		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC
//				| Opcodes.ACC_STATIC,
//				COBERTURA_GET_AND_RESET_COUNTERS_METHOD_NAME, "()[I", null,
//				null);
//		mv.visitCode();
//		mv.visitInsn(Opcodes.ACONST_NULL);
//		mv.visitInsn(Opcodes.ARETURN);
//		mv.visitMaxs(0, 0);//will be recalculated by writer
//		mv.visitEnd();
	}

}
