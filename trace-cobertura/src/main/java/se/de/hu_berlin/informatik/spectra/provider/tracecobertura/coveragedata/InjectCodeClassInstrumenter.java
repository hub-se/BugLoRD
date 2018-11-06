package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.AbstractFindTouchPointsClassInstrumenter;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.FindTouchPointsMethodAdapter;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.apache.oro.text.regex.Pattern;

/*
 * <p>This class is responsible for real instrumentation of the user's class.</p>
 * 
 * <p>It uses information acquired
 * by {@link BuildClassMapClassVisitor} ( {@link #classMap} ) and
 * {@link DetectDuplicatedCodeClassVisitor} and injects
 * code snippet provided by {@link CodeProvider} ( {@link #codeProvider} ).</p>
 *
 * @author piotr.tabor@gmail.com
 */
public class InjectCodeClassInstrumenter
		extends
			AbstractFindTouchPointsClassInstrumenter {
	/**
	 * This class is responsible for injecting code inside 'interesting places' of methods inside instrumented class
	 */
	private final InjectCodeTouchPointListener touchPointListener;

	/**
	 * {@link ClassMap} generated in previous instrumentation pass by {@link BuildClassMapClassVisitor}
	 */
	private final ClassMap classMap;

	/**
	 * {@link CodeProvider} used to generate pieces of asm code that is injected into instrumented class.
	 * 
	 * We are strictly recommending here using {@link FastArrayCodeProvider} instead of {@link AtomicArrayCodeProvider} because
	 * of performance.
	 */
	private final CodeProvider codeProvider;

	/**
	 * When we processing the class we want to now if we processed 'static initialization block' (clinit method).
	 * 
	 * <p>If there is no such a method in the instrumented class - we will need to generate it at the end</p>
	 */
	private boolean wasStaticInitMethodVisited = false;

	private final Set<String> ignoredMethods;

	/*
	 * @param cv                 - a listener for code-instrumentation events
	 * @param ignoreRegexes       - list of patters of method calls that should be ignored from line-coverage-measurement
	 * @param classMap           - map of all interesting places in the class. You should acquire it by {@link BuildClassMapClassVisitor} and remember to
	 *                           prepare it using {@link ClassMap#assignCounterIds()} before using it with {@link InjectCodeClassInstrumenter}
	 * @param duplicatedLinesMap - map of found duplicates in the class. You should use {@link DetectDuplicatedCodeClassVisitor} to find the duplicated lines.
	 */
	public InjectCodeClassInstrumenter(ClassVisitor cv,
			Collection<Pattern> ignoreRegexes, boolean threadsafeRigorous,
			ClassMap classMap,
			Map<Integer, Map<Integer, Integer>> duplicatedLinesMap,
			Set<String> ignoredMethods, boolean collectExecutionTrace) {
		super(cv, ignoreRegexes, duplicatedLinesMap);
		this.classMap = classMap;
		this.ignoredMethods = ignoredMethods;
		codeProvider = threadsafeRigorous
				? new ExecutionTracesOnlyCodeProvider(collectExecutionTrace)
				: new ExecutionTracesOnlyCodeProvider(collectExecutionTrace);
		touchPointListener = new InjectCodeTouchPointListener(classMap,
				codeProvider);
	}

	/**
	 * <p>Marks the class 'already instrumented' and injects code connected to the fields that are keeping counters.</p>
	 */
	@Override
	public void visit(int version, int access, String name, String signature,
			String supertype, String[] interfaces) {

		super.visit(version, access, name, signature, supertype, interfaces);
		codeProvider.generateCountersField(cv);
	}

	/*
	 * <p>Instrumenting a code in a single method. Special conditions for processing 'static initialization block'.</p>
	 * 
	 * <p>This method also uses {@link ShiftVariableMethodAdapter} that is used firstly to calculate the index of internal
	 * variable injected to store information about last 'processed' jump or switch in runtime ( {@link ShiftVariableMethodAdapter#calculateFirstStackVariable(int, String)} ),
	 * and then is used to inject code responsible for keeping the variable and shifting (+1) all previously seen variables.
	 */
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature,
				exceptions);
		if (ignoredMethods.contains(name + desc)) {
			return mv;
		}
		if ((access & Opcodes.ACC_STATIC) != 0) {
			mv = new GenerateCallCoberturaInitMethodVisitor(mv, classMap
					.getClassName());
			if ("<clinit>".equals(name)) {
				wasStaticInitMethodVisited = true;
			}
		}
		FindTouchPointsMethodAdapter instrumenter = new FindTouchPointsMethodAdapter(
				mv, classMap.getClassName(), name, desc, eventIdGenerator,
				duplicatedLinesMap, lineIdGenerator);
		instrumenter.setTouchPointListener(touchPointListener);
		instrumenter.setIgnoreRegexp(getIgnoreRegexp());
		LocalVariablesSorter sorter = new LocalVariablesSorter(access, desc,
				instrumenter);
		int variable = sorter.newLocal(Type.INT_TYPE);
		touchPointListener.setLastJumpIdVariableIndex(variable);
		return sorter;
		//return new ShiftVariableMethodAdapter(instrumenter, access, desc, 1);
	}

	/**
	 * Method instrumenter that injects {@link CodeProvider#generateCINITmethod(MethodVisitor, String, int)} code, and
	 * then forwards the whole previous content of the method.
	 *
	 * @author piotr.tabor@gmail.com
	 */
	private class GenerateCallCoberturaInitMethodVisitor extends MethodVisitor {
		private String className;
		public GenerateCallCoberturaInitMethodVisitor(MethodVisitor arg0,
				String className) {
			super(Opcodes.ASM4, arg0);
			this.className = className;
		}

		@Override
		public void visitCode() {
			codeProvider.generateCallCoberturaInitMethod(mv, className);
			super.visitCode();
		}
	}

	/**
	 * <p>If there was no 'static initialization block' in the class, the method is responsible for generating the method.
	 * It is also responsible for generating method that keeps mapping of counterIds into source places connected to them</p>
	 */
	@Override
	public void visitEnd() {
		if (!wasStaticInitMethodVisited) {
			//We need to generate new method
			MethodVisitor mv = super.visitMethod(Opcodes.ACC_STATIC,
					"<clinit>", "()V", null, null);
			mv.visitCode();
			codeProvider.generateCallCoberturaInitMethod(mv, classMap
					.getClassName());
			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(/*stack*/3,/*local*/0);
			mv.visitEnd();
			wasStaticInitMethodVisited = true;
		}

		codeProvider.generateCoberturaInitMethod(cv, classMap.getClassName(),
				classMap.getMaxCounterId() + 1);
		codeProvider.generateCoberturaClassMapMethod(cv, classMap);
		codeProvider.generateCoberturaGetAndResetCountersMethod(cv, classMap
				.getClassName());

		super.visitEnd();
	}

}
