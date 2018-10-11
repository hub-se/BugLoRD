package se.de.hu_berlin.informatik.stardust.provider.tracecobertura.coveragedata;


import net.sourceforge.cobertura.CoverageIgnore;
import net.sourceforge.cobertura.coveragedata.LightClassmapListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@CoverageIgnore
public class TouchCollector {
	private static final Logger logger = LoggerFactory.getLogger(TouchCollector.class);
	/*In fact - concurrentHashset*/
	private static Map<Class<?>, Integer> registeredClasses = new ConcurrentHashMap<Class<?>, Integer>();

	static {
		TraceProjectData.getGlobalProjectData(); // To call ProjectData.initialize();
	}

	public static synchronized void registerClass(Class<?> classa) {
		registeredClasses.put(classa, 0);
	}

	/**
	 * This method is only for backward compatibility
	 * 
	 * Information:
	 * ASM version 4.1 does not allow for the data type java.lang.Class to be a parameter
	 * to the method visitLdcInsn which causes issues for anything below .class versions
	 * 49 and lower. Changing the registered class to use instead a String parameter and
	 * search for the class in the classpath helped resolve the issue.
	 * Also as a side note: The replace parameters might enter as "java/lang/String" and
	 * need to be translated to "java.lang.String" so the forName method can understand it.
	 * 
	 * @param classa Class that needs to be registered.
	 * @throws ClassNotFoundException 
	 */
	public static synchronized void registerClass(String classa)
			throws ClassNotFoundException {
		try {
			// If it's not in the system jvm, then search the current thread for the class.
			// This is a dirty hack to guarantee that multiple classloaders can invoke cobertura code.

			// We try 2 methods to register the classes
			// First method we try to call the invoker classloader. If the invoker causes an exception (NoClassDefFound) it
			// will then call Thread.currentThread.getContextClassLoader() which gets the current threads classloader and
			// checks to see if cobertura code is in there. This is here because there are situations where multiple
			// classloaders might be invoked and it requires the check of multiple classloaders.

			boolean found = false;
			Class<?> clazz;
            try {
                clazz = Class.forName(classa.replace("/", "."), false,
                        Thread.currentThread().getContextClassLoader());
                for (Method meth : clazz.getMethods()) {
                    if (meth.toString().contains("net.sourceforge.cobertura")) {
                        registerClass(clazz);
                        found = true;
                    }
                }
            } catch (NoClassDefFoundError ncdfe) {
                // "Expected", try described fallback
            }

			if (!found) {
				clazz = Class.forName(classa.replace("/", "."));
				registerClass(clazz);
			}
		} catch (ClassNotFoundException e) {
			logger.error("Exception when registering class: "
					+ classa, e);
			throw e;
		}
	}

	public static synchronized void applyTouchesOnProjectData(
			TraceProjectData projectData) {
		logger.debug("=================== START OF REPORT ======================== ");
		for (Class<?> c : registeredClasses.keySet()) {
			logger.debug("Report: " + c.getName());
			ClassData cd = projectData.getOrCreateClassData(c.getName());
			applyTouchesToSingleClassOnProjectData(cd, c);
		}
		
		projectData.addExecutionTraces(ExecutionTraceCollector.getAndResetExecutionTraces());
		projectData.addIdToClassNameMap(ExecutionTraceCollector.getAndResetIdToClassNameMap());
		logger.debug("===================  END OF REPORT  ======================== ");
	}

	private static void applyTouchesToSingleClassOnProjectData(
			final ClassData classData, final Class<?> c) {
		logger.trace("----------- " + maybeCanonicalName(c)
				+ " ---------------- ");
		try {
			Method m0 = c
					.getDeclaredMethod(AbstractCodeProvider.COBERTURA_GET_AND_RESET_COUNTERS_METHOD_NAME);
			m0.setAccessible(true);
			final int[] res = (int[]) m0.invoke(null, new Object[]{});

			LightClassmapListener lightClassmap = new ApplyToClassDataLightClassmapListener(
					classData, res);
			Method m = c.getDeclaredMethod(
					AbstractCodeProvider.COBERTURA_CLASSMAP_METHOD_NAME,
					LightClassmapListener.class);
			m.setAccessible(true);
			m.invoke(null, lightClassmap);
		} catch (Exception e) {
			logger.error("Cannot apply touches", e);
		}
	}

    private static String maybeCanonicalName(final Class<?> c) {

        /* observed getCanonicalName throwing a

           java.lang.InternalError: Malformed class name

           on the scala generated class name

           com.twitter.dataproducts.authcache.storage.GnipStream$Visibility$IsExternal$

           Conclusion: getCanonicalName is flaky
        */

        try {
            return c.getCanonicalName();
        } catch (Throwable t) {
            return c.getName();
        }
    }

	@CoverageIgnore
	private static class ApplyToClassDataLightClassmapListener
			implements
				LightClassmapListener {
		//private AtomicInteger idProvider=new AtomicInteger(0);
		private final ClassData classData;
		private final int[] res;

		private int currentLine = 0;
		private int jumpsInLine = 0;
		private int switchesInLine = 0;

		private void updateLine(int new_line) {
			if (new_line != currentLine) {
				currentLine = new_line;
				jumpsInLine = 0;
				switchesInLine = 0;
			}
		}

		public ApplyToClassDataLightClassmapListener(ClassData cd, int[] res) {
			classData = cd;
			this.res = res;
		}

		public void setSource(String source) {
			logger.debug("source: " + source);
			classData.setSourceFileName(source);

		}

		public void setClazz(Class<?> clazz) {
		}

		public void setClazz(String clazz) {
		}

		public void putLineTouchPoint(int classLine, int counterId,
				String methodName, String methodDescription) {
			updateLine(classLine);
			LineData ld = classData.addLine(classLine, methodName,
					methodDescription);
			ld.touch(res[counterId]);
			classData.getCounterIdToLineDataMap().put(counterId, ld);
		}

		public void putSwitchTouchPoint(int classLine, int maxBranches,
				int... counterIds) {
			updateLine(classLine);
			LineData ld = getOrCreateLine(classLine);
			int switchId = switchesInLine++;
			classData.addLineSwitch(classLine, switchId, 0,
					counterIds.length - 2, maxBranches);
			for (int i = 0; i < counterIds.length; i++) {
				ld.touchSwitch(switchId, i - 1, res[counterIds[i]]);
				classData.getCounterIdToLineDataMap().put(counterIds[i], ld);
			}
		}

		public void putJumpTouchPoint(int classLine, int trueCounterId,
				int falseCounterId) {
			updateLine(classLine);
			LineData ld = getOrCreateLine(classLine);
			int branchId = jumpsInLine++;
			classData.addLineJump(classLine, branchId);
			ld.touchJump(branchId, true, res[trueCounterId]);
			classData.getCounterIdToLineDataMap().put(trueCounterId, ld);
			ld.touchJump(branchId, false, res[falseCounterId]);
			classData.getCounterIdToLineDataMap().put(falseCounterId, ld);
		}

		private LineData getOrCreateLine(int classLine) {
			LineData ld = classData.getLineData(classLine);
			if (ld == null) {
				ld = classData.addLine(classLine, null, null);
			}
			return ld;
		}
	}
}
