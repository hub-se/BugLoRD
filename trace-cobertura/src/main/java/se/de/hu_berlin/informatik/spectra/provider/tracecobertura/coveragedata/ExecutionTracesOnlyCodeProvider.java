package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CoberturaStatementEncoding;


public class ExecutionTracesOnlyCodeProvider extends AbstractCodeProvider
		implements
			CodeProvider {

	private final boolean collectExecutionTrace;
	private Set<Integer> statementsToInstrument;

	public ExecutionTracesOnlyCodeProvider(Set<Integer> statementsToInstrument, boolean collectExecutionTrace) {
		this.statementsToInstrument = statementsToInstrument;
		this.collectExecutionTrace = collectExecutionTrace;
	}

	@SuppressWarnings("deprecation")
	public void generateCodeThatIncrementsCoberturaCounterFromInternalVariable(
			MethodVisitor nextMethodVisitor, int lastJumpIdVariableIndex,
			String className, int classId) {
		if (shouldNotBeInstrumented(classId, lastJumpIdVariableIndex)) {
			return;
		}
		// false branch?! (we skipped the true branch jump and continue in 'else' construct or after if-statement)
		if (collectExecutionTrace) {
			// add the statement to the execution trace AND increment counter
			nextMethodVisitor.visitLdcInsn(classId);
			// load the counter id of the last stored/remembered branching statement (before jump)
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

	private boolean shouldNotBeInstrumented(int classId, int lastJumpIdVariableIndex) {
		return statementsToInstrument != null && 
				statementsToInstrument.contains(CoberturaStatementEncoding
						.generateUniqueRepresentationForStatement(classId, lastJumpIdVariableIndex));
	}

	@SuppressWarnings("deprecation")
	public void generateCodeThatProcessesLastSubtrace(
			MethodVisitor nextMethodVisitor) {
		nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
				.getInternalName(ExecutionTraceCollector.class), "processLastSubTrace",
				"()V");
	}
	
	
	@SuppressWarnings("deprecation")
	public void generateCodeThatIncrementsCoberturaCounter(
			MethodVisitor nextMethodVisitor, int counterId, 
			String className, int classId) {
		if (shouldNotBeInstrumented(classId, counterId)) {
			return;
		}
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
		if (shouldNotBeInstrumented(classId, counterId)) {
			return;
		}
		// true branch?! (jump to code in true branch)
		if (collectExecutionTrace) {
			// add the statement to the execution trace AND increment counter
			nextMethodVisitor.visitLdcInsn(classId);
			// this is the counter id of the true branch?!
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
		if (shouldNotBeInstrumented(classId, counterId)) {
			return;
		}
		if (collectExecutionTrace) {
			generateCodeThatProcessesLastSubtrace(nextMethodVisitor);
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
