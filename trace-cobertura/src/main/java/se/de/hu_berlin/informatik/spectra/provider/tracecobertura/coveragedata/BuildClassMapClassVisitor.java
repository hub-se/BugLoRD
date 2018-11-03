package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.AbstractFindTouchPointsClassInstrumenter;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.FindTouchPointsMethodAdapter;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.HistoryMethodAdapter;

import org.objectweb.asm.*;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * <p>Analyzes given class. Builds {@link ClassMap} that represents any touch-points and other important information
 * for instrumentation.</p>
 * 
 * This instrumenter ({@link ClassVisitor}) does not change the bytecode of the class. It makes only analyzys and fills {@link ClassMap}.
 *
 * @author piotr.tabor@gmail.com
 */
public class BuildClassMapClassVisitor
		extends
			AbstractFindTouchPointsClassInstrumenter {
	/**
	 * {@link ClassMap} for the currently analyzed class.
	 */
	private final ClassMap classMap = new ClassMap();

	/**
	 * Information about important 'events' (instructions) are sent into the listener that is internally
	 * responsible for modifying the {@link #classMap} content.
	 */
	private final BuildClassMapTouchPointListener touchPointListener = new BuildClassMapTouchPointListener(
			classMap);

	/**
	 * It's flag that signals if the class should be instrumented by cobertura.
	 * After analyzing the class you can check the field using {@link #shouldBeInstrumented()}.
	 */
	private boolean toInstrument = true;

	private final Set<String> ignoredMethods;
	private final Set<String> ignoredClassAnnotations;

	/*
	 * @param cv                 - a listener for code-instrumentation events
	 * @param ignoreRegexes       - list of patters of method calls that should be ignored from line-coverage-measurement
	 * @param ignoreClassAnnotations - list of class annotations to exclude them from instrumentation at all
     * @param duplicatedLinesMap - map of found duplicates in the class. You should use {@link DetectDuplicatedCodeClassVisitor} to find the duplicated lines.
	 */
	public BuildClassMapClassVisitor(ClassVisitor cv,
			Collection<Pattern> ignoreRegexes,
			Set<String> ignoreClassAnnotations,
			Map<Integer, Map<Integer, Integer>> duplicatedLinesMap,
			Set<String> ignoredMethods) {
		super(cv, ignoreRegexes, duplicatedLinesMap);
		this.ignoredMethods = ignoredMethods;
		this.ignoredClassAnnotations = ignoreClassAnnotations;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String name, boolean arg1) {
		if (Type.getDescriptor(CoverageIgnore.class).equals(name)) {
			toInstrument = false;
		} else if (ignoredClassAnnotations != null) {
			String className = Type.getObjectType(name).getClassName();
			// Class name contains artifacts anyway so trimming them out before
			// matching
			String normalizedClassName = className.replaceAll("[L;]", "");
			if (ignoredClassAnnotations.contains(normalizedClassName)) {
				toInstrument = false;
			}
		}

		return super.visitAnnotation(name, arg1);
	}

	/*
	 * Stores in {@link #classMap} information of className and if the class should be instrumented ({@link #shouldBeInstrumented()})
	 */
	@Override
	public void visit(int version, int access, String name, String signature,
			String parent, String[] interfaces) {
		classMap.setClassName(name);

		if ((access & Opcodes.ACC_INTERFACE) != 0) {
			toInstrument = false;
		}
		super.visit(version, access, name, signature, parent, interfaces);
	}

	/*
	 * Stores in {@link #classMap} information of source filename
	 */
	@Override
	public void visitSource(String file, String debug) {
		classMap.setSource(file);
		super.visitSource(file, debug);
	}

	/*
	 * Analyzes given method and stores  information about all found important places into {@link #classMap}
	 */
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		if (((access & Opcodes.ACC_STATIC) != 0)
				&& CodeProvider.COBERTURA_INIT_METHOD_NAME.equals(name)) {
			toInstrument = false; //  The class has bean already instrumented.
		}

		MethodVisitor mv = super.visitMethod(access, name, desc, signature,
				exceptions);
		if (ignoredMethods.contains(name + desc)) {
			return mv;
		}
		FindTouchPointsMethodAdapter instrumenter = new FindTouchPointsMethodAdapter(
				new HistoryMethodAdapter(mv, 4), classMap.getClassName(), name,
				desc, eventIdGenerator, duplicatedLinesMap, lineIdGenerator);
		instrumenter.setTouchPointListener(touchPointListener);
		instrumenter.setIgnoreRegexp(getIgnoreRegexp());
		return instrumenter;
	}

	/**
	 * Returns classMap build for the analyzed map. The classmap is filled after running the analyzer ({@link ClassReader#accept(ClassVisitor, int)}).
	 *
	 * @return the classmap.
	 */
	public ClassMap getClassMap() {
		return classMap;
	}

	/*
	 * It's flag that signals if the class should be instrumented by Cobertura.
	 */
	public boolean shouldBeInstrumented() {
		return toInstrument;
	}
}