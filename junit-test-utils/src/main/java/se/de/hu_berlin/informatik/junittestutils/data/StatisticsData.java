package se.de.hu_berlin.informatik.junittestutils.data;

import se.de.hu_berlin.informatik.utils.statistics.StatisticsAPI;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsOptions;

public enum StatisticsData implements StatisticsAPI {
	NODES("#nodes in spectra", StatisticType.COUNT, StatisticsOptions.PREF_NEW),
	TEST_COUNT("tests executed", StatisticType.COUNT, StatisticsOptions.PREF_BIGGER),
	SEPARATE_JVM("tests executed in separate JVM", StatisticType.COUNT, StatisticsOptions.PREF_BIGGER),
	DURATION("test duration (ms)", StatisticType.DOUBLE_VALUE, StatisticsOptions.PREF_BIGGER),
	WRONG_TEST_RESULT("tests with unexpected outcome", StatisticType.COUNT, StatisticsOptions.PREF_BIGGER),
	COVERAGE_GENERATION_FAILED("coverage generation failed", StatisticType.COUNT, StatisticsOptions.PREF_BIGGER),
	IS_SUCCESSFUL("was successful", StatisticType.BOOLEAN, StatisticsOptions.PREF_TRUE),
	TIMEOUT_OCCURRED("timeout occured", StatisticType.COUNT, StatisticsOptions.PREF_BIGGER),
	EXCEPTION_OCCURRED("exception occurred", StatisticType.COUNT, StatisticsOptions.PREF_BIGGER),
	WAS_INTERRUPTED("was interrupted", StatisticType.COUNT, StatisticsOptions.PREF_BIGGER),
	SKIPPED("was skipped", StatisticType.COUNT, StatisticsOptions.PREF_BIGGER),
	COULD_BE_FINISHED("could be finished", StatisticType.COUNT, StatisticsOptions.PREF_BIGGER),
	
	FAILED_TEST_COVERAGE("failed test coverage", StatisticType.STRING, StatisticsOptions.CONCAT),
	ERROR_MSG("error message(s)", StatisticType.STRING, StatisticsOptions.CONCAT);

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
