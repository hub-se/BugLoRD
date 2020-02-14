package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;


import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.JumpTouchPointDescriptor;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.LightClassmapListener;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.LineTouchPointDescriptor;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.SwitchTouchPointDescriptor;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.TouchPointDescriptor;

import org.objectweb.asm.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Common method used by most of {@link AbstractCodeProvider} implementations.
 * The methods realized here are independent on counters storing structure.
 *
 * @author piotr.tabor@gmail.com
 */
public abstract class AbstractCodeProvider implements CodeProvider {

	/**
	 * CounterId used to store unnecessary events to avoid fake jump counting in
	 * instrumented(generated) code
	 */
	public static final int FAKE_COUNTER_ID = 0;

	public AbstractCodeProvider() {
		super();
	}

	@Override
	public void generateCodeThatSetsJumpCounterIdVariable(
			MethodVisitor nextMethodVisitor, int new_value,
			int lastJumpIdVariableIndex) {
		nextMethodVisitor.visitLdcInsn(new_value);
		nextMethodVisitor.visitVarInsn(Opcodes.ISTORE, lastJumpIdVariableIndex);
	}

	@Override
	public void generateCodeThatZeroJumpCounterIdVariable(
			MethodVisitor nextMethodVisitor, int lastJumpIdVariableIndex) {
		generateCodeThatSetsJumpCounterIdVariable(nextMethodVisitor,
				FAKE_COUNTER_ID, lastJumpIdVariableIndex);
	}
	
//	@Override
//	public void generateCodeThatSetsThreadIdVariable(
//			MethodVisitor nextMethodVisitor, long new_value,
//			int threadIdVariableIndex) {
//		nextMethodVisitor.visitLdcInsn(new_value);
//		nextMethodVisitor.visitVarInsn(Opcodes.LSTORE, threadIdVariableIndex);
//	}
	
//	@Override
//	public void generateCodeThatSetsDecisionIndicatorVariable(
//			MethodVisitor nextMethodVisitor,
//			int decisionIndicatorVariableIndex) {
//		nextMethodVisitor.visitInsn(Opcodes.ICONST_1); // loads 1 (true) on stack
//		nextMethodVisitor.visitVarInsn(Opcodes.ISTORE, decisionIndicatorVariableIndex);
//	}
//	
//	@Override
//	public void generateCodeThatUnsetsDecisionIndicatorVariable(
//			MethodVisitor nextMethodVisitor,
//			int decisionIndicatorVariableIndex) {
//		nextMethodVisitor.visitInsn(Opcodes.ICONST_0); // loads 0 (false) on stack
//		nextMethodVisitor.visitVarInsn(Opcodes.ISTORE, decisionIndicatorVariableIndex);
//	}

	@Override
	public void generateCodeThatIncrementsCoberturaCounterIfVariableEqualsAndCleanVariable(
			MethodVisitor nextMethodVisitor,
			Integer neededJumpCounterIdVariableValue,
			Integer counterIdToIncrement, int lastJumpIdVariableIndex, int threadIdVariableIndex,
			String className, int classId) {

		nextMethodVisitor.visitLdcInsn(neededJumpCounterIdVariableValue);
		nextMethodVisitor.visitVarInsn(Opcodes.ILOAD, lastJumpIdVariableIndex);
		Label afterJump = new Label();
		nextMethodVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, afterJump);
		generateCodeThatIncrementsCoberturaCounterAfterSwitchLabel(nextMethodVisitor, threadIdVariableIndex,
				counterIdToIncrement, className, classId);
		generateCodeThatZeroJumpCounterIdVariable(nextMethodVisitor,
				lastJumpIdVariableIndex);
		nextMethodVisitor.visitLabel(afterJump);
	}

	/*
	 * The code injected by this implementation just registers the class using 
	 * {@link TouchCollector#registerClass(Class)}. This way, during the
	 * execution, touch collector knows that is responsible to ask the class 
	 * after execution about a current status of the counters.
	 */
	@SuppressWarnings("deprecation")
	protected void generateRegisterClass(MethodVisitor mv, String className) {
		mv.visitLdcInsn(className);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type
				.getInternalName(TouchCollector.class), "registerClass",
				"(Ljava/lang/String;)V");
	}
	
	@SuppressWarnings("deprecation")
	protected void generateRegisterClass(MethodVisitor mv, String className, int classId, int countersCnt) {
		mv.visitLdcInsn(className);
		mv.visitLdcInsn(classId);
		mv.visitLdcInsn(countersCnt);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type
				.getInternalName(TouchCollector.class), "registerClass",
				"(Ljava/lang/String;II)V");
	}

	final String CLASSMAP_LISTENER_INTERNALNAME = Type
			.getInternalName(LightClassmapListener.class);

	/**
	 * Generates method (named  {@link #COBERTURA_CLASSMAP_METHOD_NAME}) with such a signature:
	 * __cobertura_classmap( {@link LightClassmapListener} listener).
	 * 
	 * The method informs the listener about all lines, jumps and switches found, and about all counters tracking
	 * the constructions.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void generateCoberturaClassMapMethod(ClassVisitor cv,
			ClassMap classMap) {

		LinkedList<TouchPointDescriptor> touchPointDescriptors = new LinkedList<>(
				classMap.getTouchPointsInLineOrder());
		int parts = 0;
		for (int j = 0; touchPointDescriptors.size() > 0; j++) {
			List<TouchPointDescriptor> bufor = new LinkedList<>();
			for (int i = 0; i < 1000 && touchPointDescriptors.size() > 0; i++) {
				bufor.add(touchPointDescriptors.removeFirst());
			}
			classMapContent(cv, j, bufor);
			parts++;
		}

		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC
				| Opcodes.ACC_STATIC, COBERTURA_CLASSMAP_METHOD_NAME, "("
				+ Type.getType(LightClassmapListener.class).toString() + ")V",
				null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);

		// class name is just ignored, so why bother?...
//		mv.visitInsn(Opcodes.DUP);
//		mv.visitLdcInsn(classMap.getClassName());
//		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
//				CLASSMAP_LISTENER_INTERNALNAME, "setClazz",
//				"(Ljava/lang/String;)V");

		if (classMap.getSource() != null) {
			mv.visitInsn(Opcodes.DUP);
			mv.visitLdcInsn(classMap.getSource());
			mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
					CLASSMAP_LISTENER_INTERNALNAME, "setSource",
					"(Ljava/lang/String;)V");
		}

		for (int i = 0; i < parts; i++) {
			mv.visitInsn(Opcodes.DUP);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, classMap.getClassName(),
					COBERTURA_CLASSMAP_METHOD_NAME + "_" + i, "("
							+ Type.getType(LightClassmapListener.class)
									.toString() + ")V");
		}

		mv.visitInsn(Opcodes.POP);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0);//will be recalculated by writer
		mv.visitEnd();
	}

//	enum Abcd {
//		A, B, C
//    }

	@SuppressWarnings("deprecation")
	private void classMapContent(ClassVisitor cv, int nr,
			List<TouchPointDescriptor> touchPointDescriptors) {
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC
				| Opcodes.ACC_STATIC,
				COBERTURA_CLASSMAP_METHOD_NAME + "_" + nr, "("
						+ Type.getType(LightClassmapListener.class).toString()
						+ ")V", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		for (TouchPointDescriptor tpd : touchPointDescriptors) {
			mv.visitInsn(Opcodes.DUP);
			mv.visitLdcInsn(tpd.getLineNumber());
			if (tpd instanceof LineTouchPointDescriptor) {
				mv
						.visitLdcInsn(((LineTouchPointDescriptor) tpd)
								.getCounterId());
				mv.visitLdcInsn(((LineTouchPointDescriptor) tpd)
						.getMethodName());
				mv.visitLdcInsn(((LineTouchPointDescriptor) tpd)
						.getMethodSignature());
				mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
						CLASSMAP_LISTENER_INTERNALNAME, "putLineTouchPoint",
						"(IILjava/lang/String;Ljava/lang/String;)V");
			} else if (tpd instanceof JumpTouchPointDescriptor) {
				mv.visitLdcInsn(((JumpTouchPointDescriptor) tpd)
						.getCounterIdForTrue());
				mv.visitLdcInsn(((JumpTouchPointDescriptor) tpd)
						.getCounterIdForFalse());
				mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
						CLASSMAP_LISTENER_INTERNALNAME, "putJumpTouchPoint",
						"(III)V");
			} else if (tpd instanceof SwitchTouchPointDescriptor) {
				SwitchTouchPointDescriptor stpd = (SwitchTouchPointDescriptor) tpd;
				final String enum_sign = ((SwitchTouchPointDescriptor) tpd)
						.getEnumType();
				if (enum_sign == null) {
					mv.visitLdcInsn(Integer.MAX_VALUE);
				} else {
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, enum_sign,
							"values", "()[L" + enum_sign + ";");
					mv.visitInsn(Opcodes.ARRAYLENGTH);
				}
				Collection<Integer> ci = stpd.getCountersForLabels();
				mv.visitLdcInsn(ci.size());//Size of a new table
				mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
				int i = 0;
				for (Integer counterId : ci) {
					mv.visitInsn(Opcodes.DUP); //First for addition of items, second ad putSwitchTouchPoint parameter (or next loop iteration)
					mv.visitLdcInsn(i);
					mv.visitLdcInsn(counterId);
					mv.visitInsn(Opcodes.IASTORE);
					i++;
				}
				mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
						CLASSMAP_LISTENER_INTERNALNAME, "putSwitchTouchPoint",
						"(II[I)V");
			}
		}
		mv.visitInsn(Opcodes.POP);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0);//will be recalculated by writer
		mv.visitEnd();
	}

	/**
	 * Generates code that is injected into static constructor of an instrumented class.
	 * 
	 * It is good place to initiate static fields inserted into a class ({@link #generateCountersField(ClassVisitor)}),
	 * or execute other code that should be executed when the class it used for the first time. Registering the class in
	 * {@link TouchCollector} would be a bright idea.
	 * 
	 * It is expected that all counter will be set to zero after that operation.
	 *
	 * @param mv           - {@link MethodVisitor} that is listener of code-generation events
	 * @param className    - internal name (asm) of class being instrumented
	 * @param classId      - unique id of class being instrumented
	 * @param counters_cnt - information about how many counters are expected to be used by instrumentation code.
	 *                     In most cases the method is responsible for allocating objects that will be used to store counters.
	 */
	protected abstract void generateCINITmethod(MethodVisitor mv,
			String className, int classId, int counters_cnt);

	public void generateCoberturaInitMethod(ClassVisitor cv, String className,
			int classId, int countersCnt) {
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC
				| Opcodes.ACC_STATIC, COBERTURA_INIT_METHOD_NAME, "()V", null,
				null);
		mv.visitCode();
		generateCINITmethod(mv, className, classId, countersCnt);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0); //will be recalculated by writer
		mv.visitEnd();
	}

	@SuppressWarnings("deprecation")
	public void generateCallCoberturaInitMethod(MethodVisitor mv,
			String className) {
		mv.visitCode(); // Since we are using the ASM Check Adapter, we need to visit the code before visiting any instructions.
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, className,
				COBERTURA_INIT_METHOD_NAME, "()V");
	}
}