/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter.datatables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import se.de.hu_berlin.informatik.rankingplotter.plotter.RankingFileWrapper;

/**
 * @author Simon Heiden
 */
public class StatisticsCollection {
	
	public static enum StatisticsCategories {
		HIT_AT_1("HIT@1"),
		HIT_AT_5("HIT@5"),
		HIT_AT_10("HIT@10"),
		HIT_AT_20("HIT@20"),
		HIT_AT_30("HIT@30"),
		HIT_AT_50("HIT@50"),
		HIT_AT_100("HIT@100"),
		HIT_AT_INF("HIT@INF"),

		MOD_CHANGE("MOD_CHANGE"),
		MOD_DELETE("MOD_DELETE"),
		MOD_INSERT("MOD_INSERT"),
		MOD_UNKNOWN("MOD_UNK"),

		SIGNIFICANCE_NONE("SIG_NONE"),
		SIGNIFICANCE_LOW("SIG_LOW"),
		SIGNIFICANCE_MEDIUM("SIG_MEDIUM"),
		SIGNIFICANCE_HIGH("SIG_HIGH"),
		SIGNIFICANCE_CRUCIAL("SIG_CRUCIAL"),

		MEAN_RANK("MR"),
		MEAN_FIRST_RANK("MFR"),
		
		UNKNOWN("UNKNOWN_CATEGORY");

		private final String identifier;
		StatisticsCategories(String identifier) {
			this.identifier = identifier;
		}
		@Override public String toString() { return identifier; }
	}

	private final Map<StatisticsCategories, List<Double[]>> statisticsMap;
	
	//percentage -> project -> id -> ranking wrapper
	private final Map<Double,Map<String, Map<Integer, RankingFileWrapper>>> percentageToProjectToBugToRanking;
	
	private final String identifier;
	
	public StatisticsCollection(String identifier, Map<Double,Map<String, Map<Integer, RankingFileWrapper>>> percentageToProjectToBugToRanking) {
		super();
		this.percentageToProjectToBugToRanking = percentageToProjectToBugToRanking;
		this.identifier = identifier;
		Objects.requireNonNull(this.identifier);
		statisticsMap = new HashMap<>();
	}
	
	public StatisticsCollection(String identifier) {
		this(identifier, null);
	}
	
	public StatisticsCollection() {
		this("", null);
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public Map<Double,Map<String, Map<Integer, RankingFileWrapper>>> getPercentageToProjectToBugToRankingMap() {
		return percentageToProjectToBugToRanking;
	}
	
	public boolean addValuePair(StatisticsCategories tableIdentifier, Double... values) {
		return statisticsMap.computeIfAbsent(tableIdentifier, k -> new ArrayList<>()).add(values);
	}
	
	public List<Double[]> getStatistics(StatisticsCategories tableIdentifier) {
		return statisticsMap.get(tableIdentifier);
	}
	
	public Map<StatisticsCategories, List<Double[]>> getStatisticsmap() {
		return statisticsMap;
	}

}
