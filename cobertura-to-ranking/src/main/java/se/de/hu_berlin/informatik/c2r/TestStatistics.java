package se.de.hu_berlin.informatik.c2r;

public class TestStatistics {

	private final long duration;
	private final boolean successful;
	private final boolean timeoutOccurred;
	private final boolean exceptionOccured;
	private final boolean wasInterrupted;
	private final boolean couldBeExecuted;
	private final String errorMsg;
	
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


	public boolean WasInterrupted() {
		return wasInterrupted;
	}
	
	
	
}
