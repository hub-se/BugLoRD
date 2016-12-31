package se.de.hu_berlin.informatik.c2r;

import se.de.hu_berlin.informatik.utils.statistics.Labeled;

public enum StatisticsData implements Labeled {
	DURATION("test duration (seconds)"),
	IS_SUCCESSFUL("was successful"),
	TIMEOUT_OCCURRED("timeout occured"),
	EXCEPTION_OCCURRED("exception occurred"),
	WAS_INTERRUPTED("was interrupted"),
	COULD_BE_EXECUTED("could be executed"),
	ERROR_MSG("error message(s)");

	final private String label;
	private StatisticsData(String label) {
		this.label = label;
	}
	
	@Override
	public String getLabel() {
		return label;
	}
}
