package se.de.hu_berlin.informatik.stardust.provider.tracecobertura.coveragedata;

import java.lang.reflect.Method;
import java.util.Map;

import net.sourceforge.cobertura.coveragedata.LightClassmapListener;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class MyTouchCollector extends TouchCollector {

	public static synchronized void applyTouchesOnProjectData2(
			Map<Class<?>, Integer> registeredClasses, TraceProjectData projectData) {
		for (Class<?> c : registeredClasses.keySet()) {
			ClassData cd = projectData.getOrCreateClassData(c.getName());
			applyTouchesToSingleClassOnProjectData2(cd, c);
		}
		projectData.addExecutionTraces(ExecutionTraceCollector.getAndResetExecutionTraces());
		projectData.addIdToClassNameMap(ExecutionTraceCollector.getAndResetIdToClassNameMap());
	}

	private static void applyTouchesToSingleClassOnProjectData2(
			final ClassData classData, final Class<?> c) {
		try {
			Method m0 = c.getDeclaredMethod(AbstractCodeProvider.COBERTURA_GET_AND_RESET_COUNTERS_METHOD_NAME);
			m0.setAccessible(true);
			if(!m0.isAccessible()) {
				throw new Exception("'get and reset counters' method not accessible.");
			}
			final int[] res = (int[]) m0.invoke(null, new Object[]{});
			
//			// in some instances, we have to try to reset the hit counters...
//			boolean isResetted = false;
//			while (!isResetted) {
//				isResetted = true;
//				int[] test = (int[]) m0.invoke(null, new Object[]{});
//				for (int hits : test) {
//					if (hits != 0) {
//						isResetted = false;
//						break;
//					}
//				}
//			}
			
			LightClassmapListener lightClassmap = 
					new MyApplyToClassDataLightClassmapListener(classData, res);
			Method m = c.getDeclaredMethod(
					AbstractCodeProvider.COBERTURA_CLASSMAP_METHOD_NAME,
					LightClassmapListener.class);
			m.setAccessible(true);
			if(!m.isAccessible()) {
				throw new Exception("'classmap' method not accessible.");
			}
			m.invoke(null, lightClassmap);
		} catch (Exception e) {
			Log.err(MyTouchCollector.class, e, "Cannot apply touches");
		}
	}
	
	public static synchronized void resetTouchesOnProjectData2(
			Map<Class<?>, Integer> registeredClasses, TraceProjectData projectData) {
		for (Class<?> c : registeredClasses.keySet()) {
			ClassData cd = projectData.getOrCreateClassData(c.getName());
			resetTouchesToSingleClassOnProjectData2(cd, c);
		}
	}
	
	private static void resetTouchesToSingleClassOnProjectData2(
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
					new MyApplyToClassDataLightClassmapListener(classData, res);
			Method m = c.getDeclaredMethod(
					AbstractCodeProvider.COBERTURA_CLASSMAP_METHOD_NAME,
					LightClassmapListener.class);
			m.setAccessible(true);
			if(!m.isAccessible()) {
				throw new Exception("'classmap' method not accessible.");
			}
			m.invoke(null, lightClassmap);
		} catch (Exception e) {
			Log.err(MyTouchCollector.class, e, "Cannot apply touches");
		}
	}
	
	private static class MyApplyToClassDataLightClassmapListener implements LightClassmapListener {
		//private AtomicInteger idProvider=new AtomicInteger(0);
		private final MyClassData classData;
		private final int[] res;

		public MyApplyToClassDataLightClassmapListener(ClassData cd, int[] res) {
			classData = (MyClassData) cd;
			this.res = res;
		}

		@Override
		public void setSource(String source) {
			classData.setSourceFileName(source);
		}

		@Override
		public void setClazz(Class<?> clazz) {
		}

		@Override
		public void setClazz(String clazz) {
		}

		@Override
		public void putLineTouchPoint(int classLine, int counterId,
				String methodName, String methodDescription) {
			MyLineData ld = classData.addLine(classLine, methodName,
					methodDescription, res[counterId]);
			classData.getCounterIdToMyLineDataMap().put(counterId, ld);
		}

		@Override
		public void putSwitchTouchPoint(int classLine, int maxBranches,
				int... counterIds) {
			//do nothing
		}

		@Override
		public void putJumpTouchPoint(int classLine, int trueCounterId,
				int falseCounterId) {
			//do nothing
		}

	}
}
