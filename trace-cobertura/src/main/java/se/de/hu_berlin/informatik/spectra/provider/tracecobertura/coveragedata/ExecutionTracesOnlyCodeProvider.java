package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


public class ExecutionTracesOnlyCodeProvider extends AbstractCodeProvider
		implements
			CodeProvider {

	private final boolean collectExecutionTrace;

	public ExecutionTracesOnlyCodeProvider(boolean collectExecutionTrace) {
		this.collectExecutionTrace = collectExecutionTrace;
	}

	@SuppressWarnings("deprecation")
	public void generateCodeThatIncrementsCoberturaCounterFromInternalVariable(
			MethodVisitor nextMethodVisitor, int lastJumpIdVariableIndex,
			String className, int classId) {
		if (collectExecutionTrace) {
			// add the statement to the execution trace AND increment counter
			nextMethodVisitor.visitLdcInsn(classId);
			nextMethodVisitor.visitVarInsn(Opcodes.ILOAD, lastJumpIdVariableIndex);
			nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
					.getInternalName(ExecutionTraceCollector.class), "variableAddStatementToExecutionTraceAndIncrementCounter",
					"(II)V");
		} else {
			// increment counter
			nextMethodVisitor.visitLdcInsn(classId);
			nextMethodVisitor.visitVarInsn(Opcodes.ILOAD, lastJumpIdVariableIndex);
			nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
					.getInternalName(ExecutionTraceCollector.class), "incrementCounter",
					"(II)V");
		}
	}

	@SuppressWarnings("deprecation")
	public void generateCodeThatIncrementsCoberturaCounter(
			MethodVisitor nextMethodVisitor, int counterId, 
			String className, int classId) {
		if (collectExecutionTrace) {
			// add the statement to the execution trace AND increment counter
			nextMethodVisitor.visitLdcInsn(classId);
			nextMethodVisitor.visitLdcInsn(counterId);
			nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
					.getInternalName(ExecutionTraceCollector.class), "addStatementToExecutionTraceAndIncrementCounter",
					"(II)V");
		} else {
			// increment counter
			nextMethodVisitor.visitLdcInsn(classId);
			nextMethodVisitor.visitLdcInsn(counterId);
			nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
					.getInternalName(ExecutionTraceCollector.class), "incrementCounter",
					"(II)V");
		}
	}
	
	@SuppressWarnings("deprecation")
	public void generateCodeThatIncrementsCoberturaCounterAfterJump(
			MethodVisitor nextMethodVisitor, int counterId, 
			String className, int classId) {
		if (collectExecutionTrace) {
			// add the statement to the execution trace AND increment counter
			nextMethodVisitor.visitLdcInsn(classId);
			nextMethodVisitor.visitLdcInsn(counterId);
			nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
					.getInternalName(ExecutionTraceCollector.class), "jumpAddStatementToExecutionTraceAndIncrementCounter",
					"(II)V");
		} else {
			// increment counter
			nextMethodVisitor.visitLdcInsn(classId);
			nextMethodVisitor.visitLdcInsn(counterId);
			nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
					.getInternalName(ExecutionTraceCollector.class), "incrementCounter",
					"(II)V");
		}
	}
	
	@SuppressWarnings("deprecation")
	public void generateCodeThatIncrementsCoberturaCounterAfterSwitchLabel(
			MethodVisitor nextMethodVisitor, int counterId, 
			String className, int classId) {
		if (collectExecutionTrace) {
			// add the statement to the execution trace AND increment counter
			nextMethodVisitor.visitLdcInsn(classId);
			nextMethodVisitor.visitLdcInsn(counterId);
			nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
					.getInternalName(ExecutionTraceCollector.class), "switchAddStatementToExecutionTraceAndIncrementCounter",
					"(II)V");
		} else {
			// increment counter
			nextMethodVisitor.visitLdcInsn(classId);
			nextMethodVisitor.visitLdcInsn(counterId);
			nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
					.getInternalName(ExecutionTraceCollector.class), "incrementCounter",
					"(II)V");
		}
	}

	public void generateCountersField(ClassVisitor cv) {
	}

	public void generateCINITmethod(MethodVisitor mv, String className,
			int classId, int counters_cnt) {
		// necessary for registration of instrumented classes
		generateRegisterClass(mv, className, classId, counters_cnt);
	}

	public void generateCoberturaGetAndResetCountersMethod(ClassVisitor cv,
			String className) {
	}

}
