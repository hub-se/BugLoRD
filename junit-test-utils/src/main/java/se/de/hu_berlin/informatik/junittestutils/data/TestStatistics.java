package se.de.hu_berlin.informatik.junittestutils.data;

import se.de.hu_berlin.informatik.utils.statistics.Statistics;

public class TestStatistics extends Statistics<StatisticsData> {

	public TestStatistics() {
		super();
		addStatisticsElement(StatisticsData.TEST_COUNT, 1);
	}
	
	public TestStatistics(Statistics<StatisticsData> statistics) {
		super();
		this.mergeWith(statistics);
		addStatisticsElement(StatisticsData.TEST_COUNT, 1);
	}
	
	public TestStatistics(String errorMsg) {
		super();
		if (errorMsg != null) {
			addStatisticsElement(StatisticsData.ERROR_MSG, errorMsg);
		}
		addStatisticsElement(StatisticsData.TEST_COUNT, 1);
	}
	
	public TestStatistics(long duration, boolean successful, 
			boolean timeoutOccurred, boolean exceptionOccured, 
			boolean wasInterrupted, boolean couldBeFinished, String errorMsg) {
		super();
		addStatisticsElement(StatisticsData.COULD_BE_FINISHED, couldBeFinished ? 1 : 0);
		if (errorMsg != null) {
			addStatisticsElement(StatisticsData.ERROR_MSG, errorMsg);
		}
		addStatisticsElement(StatisticsData.TEST_COUNT, 1);
		addStatisticsElement(StatisticsData.TIMEOUT_OCCURRED, timeoutOccurred ? 1 : 0);
		if (couldBeFinished) {
			addStatisticsElement(StatisticsData.DURATION, duration);
			addStatisticsElement(StatisticsData.IS_SUCCESSFUL, successful);
		}
		addStatisticsElement(StatisticsData.EXCEPTION_OCCURRED, exceptionOccured ? 1 : 0);
		addStatisticsElement(StatisticsData.WAS_INTERRUPTED, wasInterrupted ? 1 : 0);
	}
	
	public String getErrorMsg() {
		return getElement(StatisticsData.ERROR_MSG) == null ? 
				null : getElement(StatisticsData.ERROR_MSG).getValueAsString();
	}

	public boolean couldBeFinished() {
		return getElement(StatisticsData.COULD_BE_FINISHED) == null ? 
				false : getElement(StatisticsData.COULD_BE_FINISHED).getValueAsBoolean();
	}
	
	public boolean coverageGenerationFailed() {
		return getElement(StatisticsData.COVERAGE_GENERATION_FAILED) == null ? 
				false : getElement(StatisticsData.COVERAGE_GENERATION_FAILED).getValueAsBoolean();
	}

	public int getTestDuration() {
		return getElement(StatisticsData.DURATION) == null ? 
				0 : getElement(StatisticsData.DURATION).getValueAsInteger();
	}


	public boolean wasSuccessful() {
		return getElement(StatisticsData.IS_SUCCESSFUL) == null ? 
				false : getElement(StatisticsData.IS_SUCCESSFUL).getValueAsBoolean();
	}


	public boolean timeoutOccurred() {
		return getElement(StatisticsData.TIMEOUT_OCCURRED) == null ? 
				false : getElement(StatisticsData.TIMEOUT_OCCURRED).getValueAsBoolean();
	}


	public boolean exceptionOccured() {
		return getElement(StatisticsData.EXCEPTION_OCCURRED) == null ? 
				false : getElement(StatisticsData.EXCEPTION_OCCURRED).getValueAsBoolean();
	}


	public boolean wasInterrupted() {
		return getElement(StatisticsData.WAS_INTERRUPTED) == null ? 
				false : getElement(StatisticsData.WAS_INTERRUPTED).getValueAsBoolean();
	}
	
	
	
}
