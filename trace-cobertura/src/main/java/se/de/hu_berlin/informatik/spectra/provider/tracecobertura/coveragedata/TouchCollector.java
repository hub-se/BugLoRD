package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.LightClassmapListener;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@CoverageIgnore
public class TouchCollector {
	private static final Logger logger = LoggerFactory.getLogger(TouchCollector.class);
	/*In fact - concurrentHashset*/
	public static Map<Class<?>, Integer> registeredClasses = new ConcurrentHashMap<Class<?>, Integer>();
	public static Map<String, Integer> registeredClassesStringsToIdMap = new ConcurrentHashMap<String, Integer>();
	public static Map<Integer, String> registeredClassesIdToStringsMap = new ConcurrentHashMap<Integer, String>();
	private static volatile int currentIndex = -1;
	
	static {
		ProjectData.getGlobalProjectData(false); // To call ProjectData.initialize();
	}
	
	public static void setRegisteredClasses(Map<Class<?>, Integer> input) {
		registeredClasses = input;
		for (Class<?> clazz : registeredClasses.keySet()) {
			registeredClassesStringsToIdMap.put(clazz.getName().replace('.','/'), ++currentIndex);
			registeredClassesIdToStringsMap.put(currentIndex, clazz.getName().replace('.','/'));
		}
	}

	public static synchronized void registerClass(Class<?> classa) {
		registeredClasses.put(classa, 0);
//		logger.error("Registering class: " + classa);
		registeredClassesStringsToIdMap.put(classa.getName().replace('.','/'), ++currentIndex);
		registeredClassesIdToStringsMap.put(currentIndex, classa.getName().replace('.','/'));
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
//                for (Method meth : clazz.getMethods()) {
//                    if (meth.toString().contains("tracecobertura")) { // TODO this is very important to find the classes...
                // just register the class... TODO!
                        registerClass(clazz);
                        found = true;
//                        break;
//                    }
//                }
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
			ProjectData projectData, boolean collectExecutionTraces) {
//		logger.debug("=================== START OF REPORT ======================== ");
		for (Class<?> c : registeredClasses.keySet()) {
//			logger.debug("Report: " + c.getName());
			ClassData cd = projectData.getOrCreateClassData(c.getName());
			applyTouchesToSingleClassOnProjectData(cd, c);
		}
		
		if (collectExecutionTraces) {
			projectData.addExecutionTraces(ExecutionTraceCollector.getAndResetExecutionTraces());
//		for (Entry<Long, List<String>> entry : projectData.getExecutionTraces().entrySet()) {
//			StringBuilder builder = new StringBuilder();
//			for (String string : entry.getValue()) {
//				builder.append(string).append(",");
//			}
//			logger.debug("trace " + entry.getKey() + ": " + builder.toString());
//		}
			projectData.addIdToClassNameMap(ExecutionTraceCollector.getAndResetIdToClassNameMap());
		}
//		logger.debug("===================  END OF REPORT  ======================== ");
	}

	private static void applyTouchesToSingleClassOnProjectData(
			final ClassData classData, final Class<?> c) {
//		logger.debug("----------- " + maybeCanonicalName(c)
//				+ " ---------------- ");
		
		int[] res;
		try {
			Method m0 = c
					.getDeclaredMethod(AbstractCodeProvider.COBERTURA_GET_AND_RESET_COUNTERS_METHOD_NAME);
			m0.setAccessible(true);
			res = (int[]) m0.invoke(null, new Object[]{});
		} catch (Exception e) {
			res = null;
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

    @SuppressWarnings("unused")
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
//		private int jumpsInLine = 0;
//		private int switchesInLine = 0;

		private void updateLine(int new_line) {
			if (new_line != currentLine) {
				currentLine = new_line;
//				jumpsInLine = 0;
//				switchesInLine = 0;
			}
		}

		public ApplyToClassDataLightClassmapListener(ClassData cd, int[] res) {
			classData = cd;
			this.res = res;
		}

		public void setSource(String source) {
//			logger.debug("source: " + source);
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
			ld.touch(res == null ? 0 : res[counterId]);
			classData.getCounterIdToLineDataMap().put(counterId, ld);
		}

		public void putSwitchTouchPoint(int classLine, int maxBranches,
				int... counterIds) {
//			updateLine(classLine);
			LineData ld = getOrCreateLine(classLine);
//			int switchId = switchesInLine++;
//			classData.addLineSwitch(classLine, switchId, 0,
//					counterIds.length - 2, maxBranches);
			for (int i = 0; i < counterIds.length; i++) {
//				ld.touchSwitch(switchId, i - 1, res == null ? 0 : res[counterIds[i]]);
				classData.getCounterIdToLineDataMap().put(counterIds[i], ld);
			}
		}

		public void putJumpTouchPoint(int classLine, int trueCounterId,
				int falseCounterId) {
//			updateLine(classLine);
			LineData ld = getOrCreateLine(classLine);
//			int branchId = jumpsInLine++;
//			classData.addLineJump(classLine, branchId);
//			ld.touchJump(branchId, true, res == null ? 0 : res[trueCounterId]);
			classData.getCounterIdToLineDataMap().put(trueCounterId, ld);
//			ld.touchJump(branchId, false, res == null ? 0 : res[falseCounterId]);
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
	
	
//	public static synchronized void applyTouchesOnProjectData2(
//			ProjectData projectData, boolean collectExecutionTraces) {
//		for (Class<?> c : registeredClasses.keySet()) {
//			ClassData cd = projectData.getOrCreateClassData(c.getName());
//			applyTouchesToSingleClassOnProjectData2(cd, c);
//		}
//		
//		if (collectExecutionTraces) {
//			projectData.addExecutionTraces(ExecutionTraceCollector.getAndResetExecutionTraces());
////		for (Entry<Long, List<String>> entry : projectData.getExecutionTraces().entrySet()) {
////			StringBuilder builder = new StringBuilder();
////			for (String string : entry.getValue()) {
////				builder.append(string).append(",");
////			}
////			logger.debug("trace " + entry.getKey() + ": " + builder.toString());
////		}
//			projectData.addIdToClassNameMap(ExecutionTraceCollector.getAndResetIdToClassNameMap());
//		}
//	}

//	private static void applyTouchesToSingleClassOnProjectData2(
//			final ClassData classData, final Class<?> c) {
//		try {
//			Method m0 = c.getDeclaredMethod(AbstractCodeProvider.COBERTURA_GET_AND_RESET_COUNTERS_METHOD_NAME);
//			m0.setAccessible(true);
//			if(!m0.isAccessible()) {
//				throw new Exception("'get and reset counters' method not accessible.");
//			}
//			final int[] res = (int[]) m0.invoke(null, new Object[]{});
//			
//			LightClassmapListener lightClassmap = 
//					new ApplyToClassDataLightClassmapListener(classData, res);
//			Method m = c.getDeclaredMethod(
//					AbstractCodeProvider.COBERTURA_CLASSMAP_METHOD_NAME,
//					LightClassmapListener.class);
//			m.setAccessible(true);
//			if(!m.isAccessible()) {
//				throw new Exception("'classmap' method not accessible.");
//			}
//			m.invoke(null, lightClassmap);
//		} catch (Exception e) {
////			Log.err(MyTouchCollector.class, e, "Cannot apply touches");
//		}
//	}
	
	public static synchronized void resetTouchesOnProjectData(
			ProjectData projectData) {
		for (Class<?> c : registeredClasses.keySet()) {
			ClassData cd = projectData.getOrCreateClassData(c.getName());
			resetTouchesToSingleClassOnProjectData(cd, c);
		}
	}
	
	private static void resetTouchesToSingleClassOnProjectData(
			final ClassData classData, final Class<?> c) {
		try {
			Method m0 = c.getDeclaredMethod(AbstractCodeProvider.COBERTURA_GET_AND_RESET_COUNTERS_METHOD_NAME);
			m0.setAccessible(true);
			if(!m0.isAccessible()) {
				throw new Exception("'get and reset counters' method not accessible.");
			}
			int[] res = (int[]) m0.invoke(null, new Object[]{});
			
			// reset the hit counters...
			boolean isResetted = false;
			int tryCount = 0;
			while (!isResetted && tryCount < 1000) {
				++tryCount;
				isResetted = true;
				res = (int[]) m0.invoke(null, new Object[]{});
				for (int hits : res) {
					if (hits != 0) {
						isResetted = false;
						break;
					}
				}
			}
			
			LightClassmapListener lightClassmap = 
					new ApplyToClassDataLightClassmapListener(classData, res);
			Method m = c.getDeclaredMethod(
					AbstractCodeProvider.COBERTURA_CLASSMAP_METHOD_NAME,
					LightClassmapListener.class);
			m.setAccessible(true);
			if(!m.isAccessible()) {
				throw new Exception("'classmap' method not accessible.");
			}
			m.invoke(null, lightClassmap);
		} catch (Exception e) {
//			Log.err(MyTouchCollector.class, e, "Cannot apply touches");
		}
	}

	
//	private static class MyApplyToClassDataLightClassmapListener implements LightClassmapListener {
//		//private AtomicInteger idProvider=new AtomicInteger(0);
//		private final MyClassData classData;
//		private final int[] res;
//
//		public MyApplyToClassDataLightClassmapListener(MyClassData cd, int[] res) {
//			classData = (MyClassData) cd;
//			this.res = res;
//		}
//
//		@Override
//		public void setSource(String source) {
//			classData.setSourceFileName(source);
//		}
//
//		@Override
//		public void setClazz(Class<?> clazz) {
//		}
//
//		@Override
//		public void setClazz(String clazz) {
//		}
//
//		@Override
//		public void putLineTouchPoint(int classLine, int counterId,
//				String methodName, String methodDescription) {
//			MyLineData ld = classData.addLine(classLine, methodName,
//					methodDescription, res[counterId]);
//			classData.getCounterIdToMyLineDataMap().put(counterId, ld);
//			classData.getLineNumberToMyLineDataMap().put(classLine, ld);
//		}
//
//		@Override
//		public void putSwitchTouchPoint(int classLine, int maxBranches,
//				int... counterIds) {
//			//do nothing? TODO
//			int sum = 0;
//			for (int i = 0; i < counterIds.length; i++) {
//				sum += counterIds[i];
//			}
//			MyLineData ld = getOrCreateLine(classLine, sum);
//			classData.getLineNumberToMyLineDataMap().put(classLine, ld);
//			for (int i = 0; i < counterIds.length; i++) {
//				classData.getCounterIdToMyLineDataMap().put(counterIds[i], ld);
//			}
//		}
//
//		@Override
//		public void putJumpTouchPoint(int classLine, int trueCounterId,
//				int falseCounterId) {
//			//do nothing? TODO
//			MyLineData ld = getOrCreateLine(classLine, res[trueCounterId] + res[falseCounterId]);
//			classData.getLineNumberToMyLineDataMap().put(classLine, ld);
//			classData.getCounterIdToMyLineDataMap().put(trueCounterId, ld);
//			classData.getCounterIdToMyLineDataMap().put(falseCounterId, ld);
//		}
//
//		private MyLineData getOrCreateLine(int classLine, int hitCount) {
//			MyLineData ld = classData.getMyLineData(classLine);
//			if (ld == null) {
//				ld = classData.addLine(classLine, null, null, hitCount);
//			}
//			return ld;
//		}
//
//	}
}
