package se.de.hu_berlin.informatik.rankingplotter.plotter.datatables;

import se.de.hu_berlin.informatik.utils.statistics.StatisticsAPI;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsOptions;

public enum StatisticsData implements StatisticsAPI {
	RANKS("ranks", StatisticType.INTEGER_VALUE, StatisticsOptions.PREF_BIGGER),
	MIN_RANKS("ranks", StatisticType.INTEGER_VALUE, StatisticsOptions.PREF_BIGGER);

	final private String label;
	final private StatisticType type;
	final private StatisticsOptions[] options;
	StatisticsData(String label, StatisticType type, StatisticsOptions... options) {
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
