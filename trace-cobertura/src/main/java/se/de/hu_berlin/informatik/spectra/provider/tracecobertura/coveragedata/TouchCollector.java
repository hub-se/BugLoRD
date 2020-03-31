package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.LightClassmapListener;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

@CoverageIgnore
public class TouchCollector {

    /*In fact - concurrentHashset*/
    public static final Map<Class<?>, Integer> registeredClasses = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(TouchCollector.class);

    static {
        ProjectData.getGlobalProjectData(); // To call ProjectData.initialize();
    }

//	public static void setRegisteredClasses(Map<Class<?>, Integer> input) {
//		registeredClasses = input;
//		for (Class<?> clazz : registeredClasses.keySet()) {
//			registeredClassesStringsToIdMap.put(clazz.getName().replace('.','/'), ++currentIndex);
//			registeredClassesIdToStringsMap.put(currentIndex, clazz.getName().replace('.','/'));
//		}
//	}

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
	 * if class not found
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
                    if (meth.toString().contains("tracecobertura")) { // TODO this is very important to find the classes...
                        registerClass(clazz);
                        found = true;
                        break;
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
	
	
	public static synchronized void registerClass(Class<?> classa, int classId, int countersCnt) {
		if (registeredClasses.get(classa) == null) {
			registeredClasses.put(classa, classId);
//			logger.debug("Registering class: " + classa);
			ExecutionTraceCollector.initializeCounterArrayForClass(classId, countersCnt);
		}
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
	 * @param classId unique class id that needs to be registered.
	 * @param countersCnt the length of the counter array of that class
	 * 
	 * @throws ClassNotFoundException 
	 * if class not found
	 */
	public static synchronized void registerClass(String classa, int classId, int countersCnt)
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
                    if (meth.toString().contains("tracecobertura")) { // TODO this is very important to find the classes...
                        registerClass(clazz, classId, countersCnt);
                        found = true;
                        break;
                    }
                }
            } catch (NoClassDefFoundError ncdfe) {
                // "Expected", try described fallback
            }

			if (!found) {
				clazz = Class.forName(classa.replace("/", "."));
				registerClass(clazz, classId, countersCnt);
			}
		} catch (ClassNotFoundException e) {
			logger.error("Exception when registering class: "
					+ classa, e);
			throw e;
		}
	}

	public static synchronized void applyTouchesOnProjectData(
			ProjectData projectData) {
//		logger.debug("=================== START OF REPORT ======================== ");
		for (Entry<Class<?>, Integer> c : registeredClasses.entrySet()) {
//			logger.debug("Report: " + c.getName());
//			if (c.getName().contains("FunctionInjector")) {
//				logger.debug("----------- " + c.getName()
//				+ " ---------------- ");
//			}
//			ClassData cd = projectData.getOrCreateClassData(c.getName());
			ClassData cd = projectData.getOrCreateClassData(c.getKey().getName(), c.getValue());
			applyTouchesToSingleClassOnProjectData(cd, c.getKey());
		}
		
		projectData.addExecutionTraces(ExecutionTraceCollector.getAndResetExecutionTraces());

//		projectData.addIdToSubTraceMap(ExecutionTraceCollector.getAndResetIdToSubtraceMap());
		
//		if (!projectData.getExecutionTraces().isEmpty() && projectData.getIdToSubtraceMap().isEmpty()) {
//			throw new IllegalStateException("Execution traces are available, but sub trace map is empty.");
//		}
		
//		for (Entry<Long, CompressedIntegerTrace> entry : projectData.getExecutionTraces().entrySet()) {
////			StringBuilder builder = new StringBuilder();
////			for (String string : entry.getValue()) {
////				builder.append(string).append(",");
////			}
//			logger.debug("trace " + entry.getKey() + ": " + entry.getValue().toString());
//		}
//		logger.debug("===================  END OF REPORT  ======================== ");
	}

	private static void applyTouchesToSingleClassOnProjectData(
			final ClassData classData, final Class<?> c) {
//		logger.debug("----------- " + maybeCanonicalName(c)
//		+ " ---------------- ");
		
		// first, try to get the counter array from the execution trace collector
		int[] res = ExecutionTraceCollector.getAndResetCounterArrayForClass(classData.getClassId());
		if (res == null) {
			try {
				Method m0 = c
						.getDeclaredMethod(AbstractCodeProvider.COBERTURA_GET_AND_RESET_COUNTERS_METHOD_NAME);
				m0.setAccessible(true);
				res = (int[]) m0.invoke(null, new Object[]{});
			} catch (Exception e) {
				// if there is no such method...
				throw new IllegalStateException("Can not get counter array from " + c.getCanonicalName() + "!", e);
			}
		}

		try {
			LightClassmapListener lightClassmap = new ApplyToClassDataLightClassmapListener(
					classData, res);
			Method m = c.getDeclaredMethod(
					AbstractCodeProvider.COBERTURA_CLASSMAP_METHOD_NAME,
					LightClassmapListener.class);
			m.setAccessible(true);
			if(!m.isAccessible()) {
				throw new Exception("'classmap' method not accessible.");
			}
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
//			if (classData.getName().equals("com.google.javascript.jscomp.FunctionInjector$CallSiteType")) {
//				logger.debug("got a listener!");
//			}
			this.res = res;
		}
		
		public void setClazz(Class<?> clazz) {
			
		}

		public void setClazz(String clazz) {
			
		}


		public void setSource(String source) {
//			logger.debug("source: " + source);
			classData.setSourceFileName(source);

		}

		public void putLineTouchPoint(int classLine, int counterId,
				String methodName, String methodDescription) {
//			if (classData.getName().equals("com.google.javascript.jscomp.FunctionInjector$CallSiteType")) {
//				logger.debug("put a line touch point");
//			}
			updateLine(classLine);
			LineData ld = classData.addLine(classLine, methodName,
					methodDescription);
			ld.touch(res == null ? 0 : res[counterId]);
//			classData.getCounterIdToLineNumberMap().put(counterId, classLine);
		}

		public void putSwitchTouchPoint(int classLine, int maxBranches,
				int... counterIds) {
//			if (classData.getName().equals("com.google.javascript.jscomp.FunctionInjector$CallSiteType")) {
//				logger.debug("put a switch touch point at " + classLine);
//			}
			updateLine(classLine);
			LineData ld = classData.addLineWithNoMethodName(classLine);
			int switchId = switchesInLine++;
			classData.addLineSwitch(classLine, switchId, 0,
					counterIds.length - 2, maxBranches);
			for (int i = 0; i < counterIds.length; i++) {
				ld.touchSwitch(switchId, i - 1, res == null ? 0 : res[counterIds[i]]);
//				System.out.print(counterIds[i]+ ",");
//				classData.getCounterIdToLineNumberMap().put(counterIds[i], classLine);
			}
		}

		public void putJumpTouchPoint(int classLine, int trueCounterId,
				int falseCounterId) {
//			if (classData.getName().equals("com.google.javascript.jscomp.FunctionInjector$CallSiteType")) {
//				logger.debug("put a jump touch point");
//			}
			updateLine(classLine);
			LineData ld = classData.addLineWithNoMethodName(classLine);
			int branchId = jumpsInLine++;
			classData.addLineJump(classLine, branchId);
			ld.touchJump(branchId, true, res == null ? 0 : res[trueCounterId]);
//			classData.getCounterIdToLineNumberMap().put(trueCounterId, classLine);
			ld.touchJump(branchId, false, res == null ? 0 : res[falseCounterId]);
//			classData.getCounterIdToLineNumberMap().put(falseCounterId, classLine);
		}

	}
	
	public static synchronized boolean resetTouchesOnRegisteredClasses() {
		boolean allWorked = true;
		for (Entry<Class<?>, Integer> c : registeredClasses.entrySet()) {
			allWorked &= resetTouchesToSingleClass(c.getKey(), c.getValue());
		}
		return allWorked;
	}
	
	private static boolean resetTouchesToSingleClass(final Class<?> c, int classId) {
		// first, try to get/reset the counter array from the execution trace collector
		int[] res = ExecutionTraceCollector.getAndResetCounterArrayForClass(classId);
		if (res == null) {
			try {
				Method m0 = c
						.getDeclaredMethod(AbstractCodeProvider.COBERTURA_GET_AND_RESET_COUNTERS_METHOD_NAME);
				m0.setAccessible(true);
				// should reset the counter array for the next time it is called!
				res = (int[]) m0.invoke(null, new Object[]{});
				
				// reset the hit counters...
				boolean isResetted = false;
				int tryCount = 0;
				while (!isResetted && tryCount < 1000) {
					++tryCount;
					isResetted = true;
					// check the contents of the counter array (and reset again)
					res = (int[]) m0.invoke(null, new Object[]{});
					for (int hits : res) {
						if (hits != 0) {
							isResetted = false;
							logger.error("Resetting counters has issues: " + maybeCanonicalName(c));
							break;
						}
					}
				}
				
				return isResetted;
			} catch (Exception e) {
				// if there is no such method...
				throw new IllegalStateException("Can not get counter array from " + c.getCanonicalName() + "!", e);
			}
		} else {
			// reset the hit counters...
			boolean isResetted = false;
			int tryCount = 0;
			while (!isResetted && tryCount < 1000) {
				++tryCount;
				isResetted = true;
				// check the contents of the counter array (and reset again)
				res = ExecutionTraceCollector.getAndResetCounterArrayForClass(classId);
				for (int hits : res) {
					if (hits != 0) {
						isResetted = false;
						logger.error("Resetting counters has issues: " + maybeCanonicalName(c));
						break;
					}
				}
			}
			
			return isResetted;
		}
	}

}
