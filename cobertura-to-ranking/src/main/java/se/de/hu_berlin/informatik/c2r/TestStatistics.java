package se.de.hu_berlin.informatik.c2r;

public class TestStatistics {

	private long duration;
	private boolean successful;
	private boolean timeoutOccurred;
	private boolean exceptionOccured;
	private boolean wasInterrupted;
	private boolean couldBeExecuted;
	private String errorMsg;
	
	public TestStatistics(String errorMsg) {
		super();
		this.couldBeExecuted = false;
		this.errorMsg = errorMsg;
		
		this.duration = 0;
		this.successful = false;
		this.timeoutOccurred = false;
		this.exceptionOccured = false;
		this.wasInterrupted = false;
	}
	
	public TestStatistics(long duration, boolean successful, 
			boolean timeoutOccurred, boolean exceptionOccured, boolean wasInterrupted) {
		super();
		this.couldBeExecuted = true;
		this.errorMsg = null;
		
		this.duration = duration;
		this.successful = successful;
		this.timeoutOccurred = timeoutOccurred;
		this.exceptionOccured = exceptionOccured;
		this.wasInterrupted = wasInterrupted;
	}
	
	public TestStatistics mergeWith(final TestStatistics other) {
		//if a test failed once, it counts as failed
		successful = other.wasSuccessful() ? this.wasSuccessful() : other.wasSuccessful();
		//if a test could not be executed, it counts as not being able to be executed
		couldBeExecuted = other.couldBeExecuted() ? this.couldBeExecuted() : other.couldBeExecuted();
		//if a test had a timeout once, it counts as always having a timeout
		timeoutOccurred = other.timeoutOccurred() ? other.timeoutOccurred() : this.timeoutOccurred();
		//if a test had a timeout once, it counts as always having a timeout
		exceptionOccured = other.exceptionOccured() ? other.exceptionOccured() : this.exceptionOccured();
		//if a test was not interrupted once, it counts as not interrupted
		wasInterrupted = other.wasInterrupted() ? this.wasInterrupted() : other.wasInterrupted();
		//use the maximum duration
		duration = other.getTestDuration() > this.getTestDuration() ? 
				other.getTestDuration() : this.getTestDuration();
		//simply use the last error message...
		errorMsg = other.getErrorMsg() == null ? this.getErrorMsg() : other.getErrorMsg();
		
		return this;
	}
	
	public String getErrorMsg() {
		return errorMsg;
	}

	public boolean couldBeExecuted() {
		return couldBeExecuted;
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
