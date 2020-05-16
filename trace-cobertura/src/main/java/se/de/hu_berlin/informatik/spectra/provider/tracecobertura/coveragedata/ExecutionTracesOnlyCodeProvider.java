package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import org.objectweb.asm.*;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CoberturaStatementEncoding;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.output.OutputSequence;

import java.util.Set;

@CoverageIgnore
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
            MethodVisitor nextMethodVisitor, int lastJumpIdVariableIndex, int threadIdVariableIndex,
            String className, int classId) {
        if (shouldNotBeInstrumented(classId, lastJumpIdVariableIndex)) { //TODO
            return;
        }
        // false branch?! (we skipped the true branch jump and continue in 'else' construct or after if-statement)
        if (collectExecutionTrace) {
//			if (lastJumpIdVariableIndex == 233) {
//				System.out.println(classId + ":" + lastJumpIdVariableIndex);
//			}

            // check if value of jump variable is not 0 (fake jump)
            nextMethodVisitor.visitVarInsn(Opcodes.ILOAD, lastJumpIdVariableIndex);
            Label afterJump = new Label();
            nextMethodVisitor.visitJumpInsn(Opcodes.IFEQ, afterJump);

            // add the statement to the execution trace AND increment counter
            nextMethodVisitor.visitLdcInsn(classId);
            // load the counter id of the last stored/remembered branching statement (before jump)
            nextMethodVisitor.visitVarInsn(Opcodes.ILOAD, lastJumpIdVariableIndex);
            // load the current thread's trace
            nextMethodVisitor.visitVarInsn(Opcodes.ALOAD, threadIdVariableIndex);
            nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
                            .getInternalName(ExecutionTraceCollector.class), "variableAddStatementToExecutionTraceAndIncrementCounter",
                    "(IIL" + Type.getInternalName(OutputSequence.class) + ";)V");

            generateCodeThatZeroJumpCounterIdVariable(nextMethodVisitor,
                    lastJumpIdVariableIndex);
//			generateCodeThatProcessesLastSubtrace(nextMethodVisitor);

            nextMethodVisitor.visitLabel(afterJump);
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
            MethodVisitor nextMethodVisitor, int threadIdVariableIndex) {
//		nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
//				.getInternalName(ExecutionTraceCollector.class), "processLastSubTrace",
//				"()V");
    	if (collectExecutionTrace) {
    		// load the current thread's trace
    		nextMethodVisitor.visitVarInsn(Opcodes.ALOAD, threadIdVariableIndex);
    		nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
    				.getInternalName(ExecutionTraceCollector.class), "startNewSubTrace",
    				"(L" + Type.getInternalName(OutputSequence.class) + ";)V");
    	}
    }

    @SuppressWarnings("deprecation")
    @Override
    public void generateCodeThatSetsCurrentThreadOutputSequence(
            MethodVisitor mv, int threadIdVariableIndex) {
        // should request the current thread's id like this:
//		long threadId = Thread.currentThread().getId();
    	if (collectExecutionTrace) {
    		mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type
    				.getInternalName(ExecutionTraceCollector.class),
    				"getOutputSequence", "()L" + Type.getInternalName(OutputSequence.class) + ";");
    		mv.visitVarInsn(Opcodes.ASTORE, threadIdVariableIndex);
    	}
    }


    @SuppressWarnings("deprecation")
    public void generateCodeThatIncrementsCoberturaCounter(
            MethodVisitor nextMethodVisitor, int threadIdVariableIndex, int counterId,
            String className, int classId) {
        if (shouldNotBeInstrumented(classId, counterId)) {
            return;
        }
        if (collectExecutionTrace) {
            // add the statement to the execution trace AND increment counter
            nextMethodVisitor.visitLdcInsn(classId);
            nextMethodVisitor.visitLdcInsn(counterId);
            // load the current thread's trace
            nextMethodVisitor.visitVarInsn(Opcodes.ALOAD, threadIdVariableIndex);
            nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
                            .getInternalName(ExecutionTraceCollector.class), "addStatementToExecutionTraceAndIncrementCounter",
                    "(IIL" + Type.getInternalName(OutputSequence.class) + ";)V");
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
            MethodVisitor nextMethodVisitor, int threadIdVariableIndex, int counterId,
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
            // load the current thread's trace
            nextMethodVisitor.visitVarInsn(Opcodes.ALOAD, threadIdVariableIndex);
            nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
                            .getInternalName(ExecutionTraceCollector.class), "jumpAddStatementToExecutionTraceAndIncrementCounter",
                    "(IIL" + Type.getInternalName(OutputSequence.class) + ";)V");
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
            MethodVisitor nextMethodVisitor, int threadIdVariableIndex, int counterId,
            String className, int classId) {
        if (shouldNotBeInstrumented(classId, counterId)) {
            return;
        }
        if (collectExecutionTrace) {
            // add the statement to the execution trace AND increment counter
            nextMethodVisitor.visitLdcInsn(classId);
            nextMethodVisitor.visitLdcInsn(counterId);
            // load the current thread's trace
            nextMethodVisitor.visitVarInsn(Opcodes.ALOAD, threadIdVariableIndex);
            nextMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type
                            .getInternalName(ExecutionTraceCollector.class), "switchAddStatementToExecutionTraceAndIncrementCounter",
                    "(IIL" + Type.getInternalName(OutputSequence.class) + ";)V");
//			generateCodeThatProcessesLastSubtrace(nextMethodVisitor);
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
