package se.de.hu_berlin.informatik.c2r;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

import junit.framework.Test;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class TestWrapper {
	
	private Test test = null;
	
	private Request request = null;
	
	private final String identifier;
	
	public TestWrapper(String clazz, String method) {
		this.identifier = clazz + "::" + method;
		
		Class<?> testClazz = null;
		try {
			testClazz = Class.forName(clazz);
			request = Request.method(testClazz, method);
		} catch (ClassNotFoundException e1) {
			Log.err(this, e1, "Could not find class '%s'.", clazz);
		}
	}
	
	public TestWrapper(Class<?> clazz, String method) {
		this.identifier = clazz + "::" + method;
		request = Request.method(clazz, method);
	}
	
	public TestWrapper(Request request, String clazz, String method) {
		this.identifier = clazz + "::" + method;
		this.request = request;
	}
	
	public TestWrapper(Test test, Class<?> clazz) {
		this.identifier = test.toString();
		String temp = test.toString();
		if (temp.contains("(")) {
			request = Request.method(clazz, temp.substring(0, temp.indexOf('(')));
		} else {
			Log.warn(this, "Test '%s' in class '%s' not parseable.", temp, clazz);
		}
		this.test = test;
	}
	
	public TestRunFutureTask getTest() {
		if (request != null) {
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
