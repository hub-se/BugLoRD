/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import se.de.hu_berlin.informatik.c2r.TestStatistics;
import se.de.hu_berlin.informatik.c2r.TestWrapper;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.OutputStreamManipulationUtilities;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.threaded.ExecutorServiceProvider;

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
public class TestRunModule extends AbstractProcessor<TestWrapper, TestStatistics> {

	final private String testOutput;
	final private Long timeout;
	final private boolean debugOutput;
	final private int repeatCount;

	//used to execute the tests in a separate thread, one at a time
	final private ExecutorServiceProvider provider = new ExecutorServiceProvider(1);
	
	public TestRunModule(final String testOutput, final boolean debugOutput, final Long timeout) {
		this(testOutput, debugOutput, timeout, 1);
	}
	
	public TestRunModule(final String testOutput, final boolean debugOutput, final Long timeout, final int repeatCount) {
		super();
		allowOnlyForcedTracks();
		this.testOutput = testOutput;
		this.timeout = timeout;
		this.debugOutput = debugOutput;
		this.repeatCount = repeatCount > 0 ? repeatCount : 1;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public TestStatistics processItem(final TestWrapper testWrapper) {
		forceTrack(testWrapper.toString());
//		Log.out(this, "Now processing: '%s'.", testWrapper);

		//disable std output
		if (!debugOutput) {
			System.out.flush();
			OutputStreamManipulationUtilities.switchOffStdOut();
			System.err.flush();
			OutputStreamManipulationUtilities.switchOffStdErr();
		}

		TestStatistics statistics = null;
		for (int i = 0; i < repeatCount; ++i) {
			//execute the test case with the given timeout (may be null for no timeout)
			TestStatistics tempStatistics = runTest(testWrapper, 
					testOutput + File.separator + testWrapper.toString().replace(':','_'), timeout);
			if (statistics == null) {
				statistics = tempStatistics;
			} else {
				statistics.mergeWith(tempStatistics);
			}
		}
		
		//enable std output
		if (!debugOutput) {
			System.out.flush();
			OutputStreamManipulationUtilities.switchOnStdOut();
			System.err.flush();
			OutputStreamManipulationUtilities.switchOnStdErr();
		}
		
		return statistics;
	}

	private TestStatistics runTest(final TestWrapper testWrapper, final String resultFile, final Long timeout) {
		long startingTime = System.currentTimeMillis();
//		Log.out(this, "Start Running " + testWrapper);

		FutureTask<Result> task = testWrapper.getTest();
		provider.getExecutorService().submit(task);
		
		Result result = null;
		boolean timeoutOccured = false, wasInterrupted = false, exceptionThrown = false;
		boolean couldBeFinished = true;
		String errorMsg = null;
		try {
			if (timeout == null) {
				result = task.get();
			} else {
				result = task.get(timeout, TimeUnit.SECONDS);
			}
		} catch (InterruptedException e) {
			errorMsg = testWrapper + ": Test execution interrupted!";
			wasInterrupted = true;
			couldBeFinished = false;
			task.cancel(true);
		} catch (ExecutionException e) {
			errorMsg = testWrapper + ": Test execution exception! " + e.getCause();
			e.getCause().printStackTrace();
			Log.err(this, e, errorMsg);
			exceptionThrown = true;
			couldBeFinished = false;
			task.cancel(true);
		} catch (TimeoutException e) {
			errorMsg = testWrapper + ": Time out! ";
			timeoutOccured = true;
			couldBeFinished = false;
			task.cancel(true);
		}

		if (resultFile != null) {
			final StringBuilder buff = new StringBuilder();
			if (result == null) {
				if (timeoutOccured) {
					buff.append(testWrapper + " TIMEOUT!!!" + System.lineSeparator());
				} else if (wasInterrupted) {
					buff.append(testWrapper + " INTERRUPTED!!!" + System.lineSeparator());
				} else if (exceptionThrown) {
					buff.append(testWrapper + " EXECUTION EXCEPTION!!!" + System.lineSeparator());
				}
			} else if (!result.wasSuccessful()) {
				buff.append("#ignored:" + result.getIgnoreCount() + ", " + "FAILED!!!" + System.lineSeparator());
				for (final Failure f : result.getFailures()) {
					buff.append(f.toString() + System.lineSeparator());
				}
			}
			
			if (buff.length() != 0) {
				final File out = new File(resultFile);
				try {
					FileUtils.writeString2File(buff.toString(), out);
				} catch (IOException e) {
					Log.err(this, e, "IOException while trying to write to file '%s'", out);
				}
			}
		}

		long endingTime = System.currentTimeMillis();
//		Misc.writeString2File(Long.toString(endingTime - startingTime),
//				new File(resultFile.substring(0, resultFile.lastIndexOf('.')) + ".runtime"));
		
		long duration = (endingTime - startingTime);
		if (result == null) {
			return new TestStatistics(duration, false, timeoutOccured, 
					exceptionThrown, wasInterrupted, false, errorMsg);
		} else {
			if (result.getIgnoreCount() > 0) {
				couldBeFinished = false;
			}
			return new TestStatistics(duration, result.wasSuccessful(), 
					timeoutOccured, exceptionThrown, wasInterrupted, couldBeFinished, errorMsg);
		}
	}

	@Override
	public boolean finalShutdown() {
		provider.shutdownAndWaitForTermination();
		return super.finalShutdown();
	}
	
	
	
}
