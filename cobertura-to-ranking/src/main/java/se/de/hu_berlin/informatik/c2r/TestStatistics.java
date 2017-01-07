package se.de.hu_berlin.informatik.c2r;

import se.de.hu_berlin.informatik.utils.statistics.Statistics;

public class TestStatistics extends Statistics<StatisticsData> {

	private long duration;
	private boolean successful;
	private boolean timeoutOccurred;
	private boolean exceptionOccured;
	private boolean wasInterrupted;
	private boolean couldBeFinished;
	private String errorMsg;
	
	public TestStatistics(String errorMsg) {
		super();
		this.couldBeFinished = false;
		this.errorMsg = errorMsg;
		
		this.duration = 0;
		this.successful = false;
		this.timeoutOccurred = false;
		this.exceptionOccured = false;
		this.wasInterrupted = false;
		
		addStatisticsElement(StatisticsData.ERROR_MSG, errorMsg);
//		addStatisticsElement(StatisticsData.COULD_BE_FINISHED, false);
//		addStatisticsElement(StatisticsData.DURATION, 0);
		addStatisticsElement(StatisticsData.COUNT, 1);
//		addStatisticsElement(StatisticsData.IS_SUCCESSFUL, false);
//		addStatisticsElement(StatisticsData.TIMEOUT_OCCURRED, false);
//		addStatisticsElement(StatisticsData.EXCEPTION_OCCURRED, false);
//		addStatisticsElement(StatisticsData.WAS_INTERRUPTED, false);
	}
	
	public TestStatistics(long duration, boolean successful, 
			boolean timeoutOccurred, boolean exceptionOccured, 
			boolean wasInterrupted, boolean couldBeFinished, String errorMsg) {
		super();
		this.couldBeFinished = couldBeFinished;
		addStatisticsElement(StatisticsData.COULD_BE_FINISHED, couldBeFinished ? 1 : 0);
		this.errorMsg = errorMsg;
		addStatisticsElement(StatisticsData.ERROR_MSG, errorMsg);
		
		addStatisticsElement(StatisticsData.COUNT, 1);
		this.duration = duration;
		this.successful = successful;
		this.timeoutOccurred = timeoutOccurred;
		addStatisticsElement(StatisticsData.TIMEOUT_OCCURRED, timeoutOccurred ? 1 : 0);
		if (couldBeFinished) {
			addStatisticsElement(StatisticsData.DURATION, duration);
			addStatisticsElement(StatisticsData.IS_SUCCESSFUL, successful);
		}
		this.exceptionOccured = exceptionOccured;
		addStatisticsElement(StatisticsData.EXCEPTION_OCCURRED, exceptionOccured ? 1 : 0);
		this.wasInterrupted = wasInterrupted;
		addStatisticsElement(StatisticsData.WAS_INTERRUPTED, wasInterrupted ? 1 : 0);
	}
	
//	public TestStatistics mergeWith(final TestStatistics other) {
//		//if a test failed once, it counts as failed
//		successful = other.wasSuccessful() ? this.wasSuccessful() : other.wasSuccessful();
//		//if a test could not be executed, it counts as not being able to be executed
//		couldBeExecuted = other.couldBeExecuted() ? this.couldBeExecuted() : other.couldBeExecuted();
//		//if a test had a timeout once, it counts as always having a timeout
//		timeoutOccurred = other.timeoutOccurred() ? other.timeoutOccurred() : this.timeoutOccurred();
//		//if a test had a timeout once, it counts as always having a timeout
//		exceptionOccured = other.exceptionOccured() ? other.exceptionOccured() : this.exceptionOccured();
//		//if a test was not interrupted once, it counts as not interrupted
//		wasInterrupted = other.wasInterrupted() ? this.wasInterrupted() : other.wasInterrupted();
//		//use the maximum duration
//		duration = other.getTestDuration() > this.getTestDuration() ? 
//				other.getTestDuration() : this.getTestDuration();
//		//simply use the last error message...
//		errorMsg = other.getErrorMsg() == null ? this.getErrorMsg() : other.getErrorMsg();
//		
//		return this;
//	}
	
	public String getErrorMsg() {
		return errorMsg;
	}

	public boolean couldBeFinished() {
		return couldBeFinished;
	}

	public long getTestDuration() {
		return duration;
	}


	public boolean wasSuccessful() {
		return successful;
	}


	public boolean timeoutOccurred() {
		return timeoutOccurred;
	}


	public boolean exceptionOccured() {
		return exceptionOccured;
	}


	public boolean wasInterrupted() {
		return wasInterrupted;
	}
	
	
	
}
