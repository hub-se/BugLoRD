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
			// add the statement to the execution trace AND increment counter
			nextMethodVisitor.visitLdcInsn(className);
			nextMethodVisitor.visitVarInsn(Opcodes.ILOAD, lastJumpIdVariableIndex);
			nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
					.getInternalName(ExecutionTraceCollector.class), "variableAddStatementToExecutionTraceAndIncrementCounter",
					"(Ljava/lang/String;I)V");
		} else {
			// increment counter
			nextMethodVisitor.visitLdcInsn(className);
			nextMethodVisitor.visitVarInsn(Opcodes.ILOAD, lastJumpIdVariableIndex);
			nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
					.getInternalName(ExecutionTraceCollector.class), "incrementCounter",
					"(Ljava/lang/String;I)V");
		}
	}

	@SuppressWarnings("deprecation")
	public void generateCodeThatIncrementsCoberturaCounter(
			MethodVisitor nextMethodVisitor, int counterId, 
			String className) {
		if (collectExecutionTrace) {
			// add the statement to the execution trace AND increment counter
			nextMethodVisitor.visitLdcInsn(className);
			nextMethodVisitor.visitLdcInsn(counterId);
			nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
					.getInternalName(ExecutionTraceCollector.class), "addStatementToExecutionTraceAndIncrementCounter",
					"(Ljava/lang/String;I)V");
		} else {
			// increment counter
			nextMethodVisitor.visitLdcInsn(className);
			nextMethodVisitor.visitLdcInsn(counterId);
			nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
					.getInternalName(ExecutionTraceCollector.class), "incrementCounter",
					"(Ljava/lang/String;I)V");
		}
	}
	
	@SuppressWarnings("deprecation")
	public void generateCodeThatIncrementsCoberturaCounterAfterJump(
			MethodVisitor nextMethodVisitor, int counterId, 
			String className) {
		if (collectExecutionTrace) {
			// add the statement to the execution trace AND increment counter
			nextMethodVisitor.visitLdcInsn(className);
			nextMethodVisitor.visitLdcInsn(counterId);
			nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
					.getInternalName(ExecutionTraceCollector.class), "jumpAddStatementToExecutionTraceAndIncrementCounter",
					"(Ljava/lang/String;I)V");
		} else {
			// increment counter
			nextMethodVisitor.visitLdcInsn(className);
			nextMethodVisitor.visitLdcInsn(counterId);
			nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
					.getInternalName(ExecutionTraceCollector.class), "incrementCounter",
					"(Ljava/lang/String;I)V");
		}
	}
	
	@SuppressWarnings("deprecation")
	public void generateCodeThatIncrementsCoberturaCounterAfterSwitchLabel(
			MethodVisitor nextMethodVisitor, int counterId, 
			String className) {
		if (collectExecutionTrace) {
			// add the statement to the execution trace AND increment counter
			nextMethodVisitor.visitLdcInsn(className);
			nextMethodVisitor.visitLdcInsn(counterId);
			nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
					.getInternalName(ExecutionTraceCollector.class), "switchAddStatementToExecutionTraceAndIncrementCounter",
					"(Ljava/lang/String;I)V");
		} else {
			// increment counter
			nextMethodVisitor.visitLdcInsn(className);
			nextMethodVisitor.visitLdcInsn(counterId);
			nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
					.getInternalName(ExecutionTraceCollector.class), "incrementCounter",
					"(Ljava/lang/String;I)V");
		}
	}

	public void generateCountersField(ClassVisitor cv) {
	}

	public void generateCINITmethod(MethodVisitor mv, String className,
			int counters_cnt) {
		// necessary for registration of instrumented classes
		generateRegisterClass(mv, className, counters_cnt);
	}

	public void generateCoberturaGetAndResetCountersMethod(ClassVisitor cv,
			String className) {
	}

}
