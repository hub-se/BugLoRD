/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import se.de.hu_berlin.informatik.c2r.TestStatistics;
import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.OutputStreamManipulationUtilities;
import se.de.hu_berlin.informatik.utils.threaded.ExecutorServiceProvider;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;
import se.de.hu_berlin.informatik.utils.tracking.ProgressTracker;

/**
 * Runs a single test and generates statistics. A timeout may be set
 * such that each executed test that runs longer than this timeout will
 * be aborted and will count as failing.
 * 
 * <p> if the test can't be run at all, this information is given in the
 * returned statistics, together with an error message.
 * 
 * @author Simon Heiden
 */
public class TestRunModule extends AbstractModule<String, TestStatistics> {

	final private String testOutput;
	final private Long timeout;
	final private boolean debugOutput;
	
	final private ProgressTracker tracker = new ProgressTracker(false);
	
	//used to execute the tests in a separate thread, one at a time
	final private ExecutorServiceProvider provider = new ExecutorServiceProvider(1);
	
	public TestRunModule(final String testOutput, final boolean debugOutput, Long timeout) {
		super(true);
		this.testOutput = testOutput;
		this.timeout = timeout;
		this.debugOutput = debugOutput;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public TestStatistics processItem(final String testNameAndClass) {
		tracker.track(testNameAndClass);
//		Log.out(this, "Now processing: '%s'.", testNameAndClass);
		
		//format: test.class::testName
		final String[] test = testNameAndClass.split("::");
		if (test.length != 2) {
			return new TestStatistics("Wrong test identifier format: '" + testNameAndClass + "'.");
		}

		//disable std output
		if (!debugOutput) {
			System.out.flush();
			OutputStreamManipulationUtilities.switchOffStdOut();
		}

		//execute the test case with the given timeout (may be null for no timeout)
		TestStatistics statistics = runTest(test[0], test[1], 
				testOutput + File.separator + testNameAndClass.replace(':','_'), timeout);
		
		//enable std output
		if (!debugOutput) {
			System.out.flush();
			OutputStreamManipulationUtilities.switchOnStdOut();
		}
		
		return statistics;
	}

	private TestStatistics runTest(final String className, 
			final String methodName, final String resultFile, final Long timeout) {
		long startingTime = System.currentTimeMillis();
		Class<?> testClazz = null;
		try {
			testClazz = Class.forName(className);
		} catch (ClassNotFoundException e1) {
			Log.err(this, e1, "Could not find class '%s'.", className);
			return new TestStatistics("Class not found: '" + className + "'.");
		}
		
		final Request request = Request.method(testClazz, methodName);
		
//		Log.out(this, "Start Running");

		FutureTask<Result> task = new TestRunFutureTask(request);
		provider.getExecutorService().submit(task);
		
		Result result = null;
		boolean timeoutOccured = false, wasInterrupted = false, exceptionThrown = false;
		try {
			if (timeout == null) {
				result = task.get();
			} else {
				result = task.get(timeout, TimeUnit.SECONDS);
			}
		} catch (InterruptedException e) {
			Log.err(this, e, "Test execution interrupted: %s::%s.", className, methodName);
			wasInterrupted = true;
		} catch (ExecutionException e) {
			Log.err(this, e, "Test execution exception: %s::%s.", className, methodName);
			exceptionThrown = true;
		} catch (TimeoutException e) {
			Log.err(this, "Time out: %s::%s.", className, methodName);
			timeoutOccured = true;
		}

		if (resultFile != null) {
			final StringBuilder buff = new StringBuilder();
			if (result == null) {
				if (timeoutOccured) {
					buff.append(className + "::" + methodName + " TIMEOUT!!!" + System.lineSeparator());
				} else if (wasInterrupted) {
					buff.append(className + "::" + methodName + " INTERRUPTED!!!" + System.lineSeparator());
				} else if (exceptionThrown) {
					buff.append(className + "::" + methodName + " EXECUTION EXCEPTION!!!" + System.lineSeparator());
				}
			} else if (!result.wasSuccessful()) {
				buff.append("#ignored:" + result.getIgnoreCount() + ", " + "FAILED!!!" + System.lineSeparator());
				for (final Failure f : result.getFailures()) {
					buff.append(f.toString() + System.lineSeparator());
				}
			}
			
			final File out = new File(resultFile);
			try {
				FileUtils.writeString2File(buff.toString(), out);
			} catch (IOException e) {
				Log.err(this, e, "IOException while trying to write to file '%s'", out);
			}
		}

		long endingTime = System.currentTimeMillis();
//		Misc.writeString2File(Long.toString(endingTime - startingTime),
//				new File(resultFile.substring(0, resultFile.lastIndexOf('.')) + ".runtime"));
		
		long duration = (endingTime - startingTime) / 1000;
		if (result == null) {
			return new TestStatistics(duration, false, timeoutOccured, exceptionThrown, wasInterrupted);
		} else {
			return new TestStatistics(duration, result.wasSuccessful(), timeoutOccured, exceptionThrown, wasInterrupted);
		}
	}
	
	private static class TestRunFutureTask extends FutureTask<Result> {

		public TestRunFutureTask(Request request) {
			super(new TestRunCall(request));
		}
		
		private static class TestRunCall implements Callable<Result> {

			private final Request request;

			public TestRunCall(Request request) {
				this.request = request;
			}
			
			@Override
			public Result call() throws Exception {
				return new JUnitCore().run(request);
			}
			
		}
	}
	

	@Override
	public boolean finalShutdown() {
		provider.shutdownAndWaitForTermination();
		return super.finalShutdown();
	}
	
}
