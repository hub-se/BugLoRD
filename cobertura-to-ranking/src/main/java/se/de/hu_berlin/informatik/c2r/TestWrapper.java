package se.de.hu_berlin.informatik.c2r;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import junit.framework.Test;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class TestWrapper {
	
	private Test test = null;
	
	private Request request = null;
	
	private ClassLoader customLoader = null;
	private FrameworkMethod method = null;
	private Class<?> testClazz = null;
	
	final private String testClazzName;
	final private String testMethodName;
	
	private final String identifier;
	
	private TestWrapper(ClassLoader customLoader, String clazz, String method, Object dummy) {
		this.customLoader = customLoader;
		this.testClazzName = clazz;
		this.testMethodName = method;
		this.identifier = this.testClazzName + "::" + this.testMethodName;
	}
	
	public TestWrapper(ClassLoader customLoader, String clazz, String method) {
		this(customLoader, clazz, method, null);
		
		try {
			Class<?> testClazz = null;
			if (customLoader == null) {
				testClazz = Class.forName(clazz);
			} else {
				testClazz = SeparateClassLoaderTestAdapter.getFromTestClassloader(clazz, customLoader);
			}
			request = Request.method(testClazz, method);
		} catch (InitializationError e) {
			Log.err(this, e, "Class '%s' not found.", clazz);
		} catch (ClassNotFoundException e) {
			Log.err(this, e, "Class '%s' not found.", clazz);
		}
	}
	
	public TestWrapper(String clazz, String method) {
		this((ClassLoader)null, clazz, method);
	}
	
	public TestWrapper(Class<?> clazz, String method) {
		this(null, clazz, method);
	}
	
	public TestWrapper(ClassLoader customLoader, Class<?> clazz, String method) {
		this(customLoader, clazz.getCanonicalName(), method, null);
		try {
			Class<?> testClazz = null;
			if (customLoader == null) {
				testClazz = clazz;
			} else {
				testClazz = SeparateClassLoaderTestAdapter.getFromTestClassloader(clazz, customLoader);
			}
			request = Request.method(testClazz, method);
		} catch (InitializationError e) {
			Log.err(this, e, "Class '%s' not found.", clazz);
		}
	}
	
	public TestWrapper(ClassLoader customLoader, Request request, String clazz, String method) {
		this(customLoader, clazz, method, null);
		this.request = request;
	}
	
	public TestWrapper(Request request, String clazz, String method) {
		this(null, request, clazz, method);
	}
	
	public TestWrapper(ClassLoader customLoader, Test test, Class<?> clazz) {
		this.customLoader = customLoader;
		this.testClazzName = clazz.getCanonicalName();
		this.identifier = test.toString();
		String temp = test.toString();
		if (temp.contains("(")) {
			this.testMethodName = temp.substring(0, temp.indexOf('('));
			request = Request.method(clazz, this.testMethodName);
		} else {
			this.testMethodName = null;
			Log.warn(this, "Test '%s' in class '%s' not parseable.", temp, clazz);
		}
		this.test = test;
	}
	
	public TestWrapper(ClassLoader customLoader, Class<?> testClazz, FrameworkMethod method) {
		this(customLoader, testClazz.getCanonicalName(), method.getName(), null);
		this.request = Request.method(testClazz, method.getName());
	}

	public String getTestClassName() {
		return testClazzName;
	}
	
	public String getTestMethodName() {
		return testMethodName;
	}
	
	public TestRunFutureTask getTest() {
		if (customLoader != null && request == null && test == null) {
			return new TestRunFutureTask(customLoader, testClazz, method);
		} else if (request != null) {
			return new TestRunFutureTask(customLoader, request);
		} else if (test != null) {
			return new TestRunFutureTask(customLoader, test);
		} else {
			return null;
		}
	}
	
	@Override
	public String toString() {
		return identifier;
	}




	private static class TestRunFutureTask extends FutureTask<Result> {

		public TestRunFutureTask(ClassLoader customLoader, Request request) {
			super(new RequestRunCall(customLoader, request));
		}
		
		public TestRunFutureTask(ClassLoader customLoader, Test test) {
			super(new TestRunCall(customLoader, test));
		}
		
		public TestRunFutureTask(ClassLoader customLoader, Class<?> testClazz, FrameworkMethod method) {
			super(new RunnerCall(customLoader, testClazz, method));
		}
		
		private static class RunnerCall implements Callable<Result> {

			private ClassLoader customLoader;
			private FrameworkMethod method;
			private Class<?> testClazz;
			
			public RunnerCall(ClassLoader customLoader, Class<?> testClazz, FrameworkMethod method) {
				super();
				this.customLoader = customLoader;
				this.method = method;
				this.testClazz = testClazz;
			}

			@Override
			public Result call() throws Exception {
				if (customLoader != null) {
					Thread.currentThread().setContextClassLoader(customLoader);
				}
				BlockJUnit4ClassRunner runner = new SeparateClassLoaderRunner(testClazz, method, customLoader);
				Request request = Request.runner(runner);
//				Result result = new Result();
//				RunNotifier runNotifier = new RunNotifier();
//				RunListener listener = result.createListener();
//				runNotifier.addFirstListener(listener);
//				try {
//					runNotifier.fireTestRunStarted(runner.getDescription());
//		            runner.run(runNotifier);
//		            runNotifier.fireTestRunFinished(result);
//		        } finally {
//		        	runNotifier.removeListener(listener);
//		        }
//		        return result;
				return new JUnitCore().run(request);
			}

		}

		private static class RequestRunCall implements Callable<Result> {

//			private ClassLoader customLoader;
			private final Request crequest;

			public RequestRunCall(ClassLoader customLoader, Request request) {
//				this.customLoader = customLoader;
				this.crequest = request;
			}
			
			@Override
			public Result call() throws Exception {
//				if (customLoader != null) {
//					Thread.currentThread().setContextClassLoader(customLoader);
//				}
//				Class<?> clazz = Class.forName("coberturatest.tests.SimpleProgramTest", true, Thread.currentThread().getContextClassLoader());
//				if( clazz != null ) {
//	            	Log.out(this, "Found asfafasfasf class: '%s'.", clazz);
//	            	Log.out(this, "Found asfafasdafa class path: '%s'.", clazz.getResource(clazz.getSimpleName() + ".class"));
//	            }
				return new JUnitCore().run(crequest);
			}
			
		}
		
		private static class TestRunCall implements Callable<Result> {

			private ClassLoader customLoader;
			private final Test ctest;

			public TestRunCall(ClassLoader customLoader, Test test) {
				this.customLoader = customLoader;
				this.ctest = test;
			}
			
			@Override
			public Result call() throws Exception {
				if (customLoader != null) {
					Thread.currentThread().setContextClassLoader(customLoader);
				}
				return new JUnitCore().run(ctest);
			}
			
		}
	}
}
