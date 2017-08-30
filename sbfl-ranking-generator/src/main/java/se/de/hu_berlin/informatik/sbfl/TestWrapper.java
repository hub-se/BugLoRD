package se.de.hu_berlin.informatik.sbfl;

import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.PlainJUnitResultFormatter;

import junit.framework.Test;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class TestWrapper {
	
	final private ClassLoader customLoader;

	final private String testClazzName;
	final private String testMethodName;
	
	private final String identifier;
	
	public TestWrapper(String clazz, String method, ClassLoader customLoader) {
		Objects.requireNonNull(clazz, "Test class name must not be null!");
		Objects.requireNonNull(method, "Test method name must not be null!");
		this.customLoader = customLoader;
		this.testClazzName = clazz;
		this.testMethodName = method;
		this.identifier = this.testClazzName + "::" + this.testMethodName;
	}
	
	public TestWrapper(String clazz, String method) {
		this(clazz, method, null);
	}
	
	public TestWrapper(Class<?> clazz, String method, ClassLoader customLoader) {
		this(clazz.getCanonicalName(), method, customLoader);
	}
	
	public TestWrapper(Class<?> clazz, String method) {
		this(clazz, method, null);
	}

	public TestWrapper(String clazz, Test test, ClassLoader testClassLoader) {
		Objects.requireNonNull(clazz, "Test class name must not be null!");
		Objects.requireNonNull(test, "Test must not be null!");
		this.customLoader = testClassLoader;
		this.testClazzName = clazz;
		String temp = test.toString();
		if (temp.contains("(")) {
			this.testMethodName = temp.substring(0, temp.indexOf('('));
			this.identifier = clazz + "::" + this.testMethodName;
		} else {
			this.testMethodName = null;
			Log.warn(this, "Test '%s' in class '%s' not parseable.", temp, clazz);
			this.identifier = clazz + "::" + temp;
		}
	}
	
	public TestWrapper(Class<?> clazz, Test test, ClassLoader customLoader) {
		this(clazz.getCanonicalName(), test, customLoader);
	}
	
	public TestWrapper(Class<?> clazz, Test test) {
		this(clazz, test, null);
	}

	public String getTestClassName() {
		return testClazzName;
	}
	
	public String getTestMethodName() {
		return testMethodName;
	}
	
	public TestRunFutureTask getTest() {
		return new TestRunFutureTask(testClazzName, testMethodName, customLoader);
	}
	
	public TestRunFutureTask getTest(OutputStream outputStream) {
		return new TestRunFutureTask(testClazzName, testMethodName, customLoader, outputStream);
	}
	
	@Override
	public String toString() {
		return identifier;
	}

	private static class TestRunFutureTask extends FutureTask<JUnitTest> {
		
		public TestRunFutureTask(String testClazzName, String testMethodName, ClassLoader customLoader) {
			super(new TestRunCall(testClazzName, testMethodName, customLoader));
		}
		
		public TestRunFutureTask(String testClazzName, String testMethodName, ClassLoader customLoader,
				OutputStream outputStream) {
			super(new TestRunCall(testClazzName, testMethodName, customLoader, outputStream));
		}

		private static class TestRunCall implements Callable<JUnitTest> {

			final private ClassLoader customLoader;
			final private OutputStream outputStream;

			final private String testClazzName;
			final private String testMethodName;

			public TestRunCall(String testClazzName, String testMethodName, ClassLoader customLoader) {
				this(testClazzName, testMethodName, customLoader, null);
			}
			
			public TestRunCall(String testClazzName, String testMethodName, ClassLoader customLoader,
					OutputStream outputStream) {
				this.customLoader = customLoader;
				this.testClazzName = testClazzName;
				this.testMethodName = testMethodName;
				if (outputStream == null) {
					this.outputStream = System.out;
				} else {
					this.outputStream = outputStream;
				}
			}

			@Override
			public JUnitTest call() throws Exception {
				if (customLoader != null) {
					Thread.currentThread().setContextClassLoader(customLoader);
				}
				
				JUnitTest test = new JUnitTest(testClazzName);
				test.setSkipNonTests(false);
				String[] methods = new String[] {testMethodName};
				boolean haltOnError = true;
				boolean filtertrace = true;
				boolean haltOnFailure = true;
				boolean showOutput = true;
				boolean logTestListenerEvents = false;
				
				MyJUnitTestRunner runner = new MyJUnitTestRunner(test, methods, 
						haltOnError, filtertrace, haltOnFailure, showOutput, logTestListenerEvents, customLoader);
				
				JUnitResultFormatter resultFormatter = new PlainJUnitResultFormatter();
				resultFormatter.setOutput(outputStream);
				runner.addFormatter(resultFormatter);
				
				runner.run();
				
				return test;
			}
			
		}
	}
}
