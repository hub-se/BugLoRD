package se.de.hu_berlin.informatik.c2r;

import se.de.hu_berlin.informatik.utils.statistics.StatisticsAPI;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsOptions;

public enum StatisticsData implements StatisticsAPI {
	COUNT("executed tests", StatisticType.INTEGER, StatisticsOptions.PREF_BIGGER),
	DURATION("test duration (seconds)", StatisticType.DOUBLE, StatisticsOptions.PREF_BIGGER),
	IS_SUCCESSFUL("was successful", StatisticType.BOOLEAN, StatisticsOptions.PREF_FALSE),
	TIMEOUT_OCCURRED("timeout occured", StatisticType.BOOLEAN, StatisticsOptions.PREF_TRUE),
	EXCEPTION_OCCURRED("exception occurred", StatisticType.BOOLEAN, StatisticsOptions.PREF_TRUE),
	WAS_INTERRUPTED("was interrupted", StatisticType.BOOLEAN, StatisticsOptions.PREF_FALSE),
	COULD_BE_EXECUTED("could be executed", StatisticType.BOOLEAN, StatisticsOptions.PREF_TRUE),
	ERROR_MSG("error message(s)", StatisticType.STRING, StatisticsOptions.PREF_NEW);

	final private String label;
	final private StatisticType type;
	final private StatisticsOptions[] options;
	private StatisticsData(String label, StatisticType type, StatisticsOptions... options) {
		this.label = label;
		this.type = type;
		this.options = options;
	}
	
	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public StatisticType getType() {
		return type;
	}

	@Override
	public StatisticsOptions[] getOptions() {
		return options;
	}
}
