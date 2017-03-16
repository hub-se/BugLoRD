package se.de.hu_berlin.informatik.c2r;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
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
	
	public TestWrapper(ClassLoader customLoader, String clazz, String method) {
		this.testClazzName = clazz;
		this.testMethodName = method;
		this.identifier = this.testClazzName + "::" + this.testMethodName;
		
		try {
			Class<?> testClazz = SeparateClassLoaderTestAdapter.getFromTestClassloader(this.testClazzName, customLoader);
			request = Request.method(testClazz, this.testMethodName);
		} catch (InitializationError e) {
			Log.err(this, e, "Class '%s' not found.", clazz);
		}
	}
	
	public TestWrapper(String clazz, String method) {
		this.testClazzName = clazz;
		this.testMethodName = method;
		this.identifier = this.testClazzName + "::" + this.testMethodName;
		
		try {
			Class<?> testClazz = Class.forName(this.testClazzName);
			request = Request.method(testClazz, this.testMethodName);
		} catch (ClassNotFoundException e) {
			Log.err(this, e, "Class '%s' not found.", clazz);
		}
	}
	
	public TestWrapper(Class<?> clazz, String method) {
		this.testClazzName = clazz.getCanonicalName();
		this.testMethodName = method;
		this.identifier = this.testClazzName + "::" + this.testMethodName;
		request = Request.method(clazz, this.testMethodName);
	}
	
	public TestWrapper(Request request, String clazz, String method) {
		this.testClazzName = clazz;
		this.testMethodName = method;
		this.identifier = this.testClazzName + "::" + this.testMethodName;
		this.request = request;
	}
	
	public TestWrapper(Test test, Class<?> clazz) {
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
	
	public TestWrapper(FrameworkMethod method, Class<?> testClazz) {
		this.testClazzName = testClazz.getCanonicalName();
		this.testMethodName = method.getName();
		this.identifier = this.testClazzName + "::" + this.testMethodName;
		this.request = Request.method(testClazz, method.getName());
	}

	public TestWrapper(ClassLoader customLoader, Class<?> testClazz, FrameworkMethod method) {
		this.testClazzName = testClazz.getCanonicalName();
		this.testMethodName = method.getName();
		this.identifier = this.testClazzName + "::" + this.testMethodName;
		this.customLoader = customLoader;
		this.method = method;
		this.testClazz = testClazz;
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
			return new TestRunFutureTask(request);
		} else if (test != null) {
			return new TestRunFutureTask(test);
		} else {
			return null;
		}
	}
	
	@Override
	public String toString() {
		return identifier;
	}




	private static class TestRunFutureTask extends FutureTask<Result> {

		public TestRunFutureTask(Request request) {
			super(new RequestRunCall(request));
		}
		
		public TestRunFutureTask(Test test) {
			super(new TestRunCall(test));
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

			private final Request crequest;

			public RequestRunCall(Request request) {
				this.crequest = request;
			}
			
			@Override
			public Result call() throws Exception {
				return new JUnitCore().run(crequest);
			}
			
		}
		
		private static class TestRunCall implements Callable<Result> {

			private final Test ctest;

			public TestRunCall(Test test) {
				this.ctest = test;
			}
			
			@Override
			public Result call() throws Exception {
				return new JUnitCore().run(ctest);
			}
			
		}
	}
}
