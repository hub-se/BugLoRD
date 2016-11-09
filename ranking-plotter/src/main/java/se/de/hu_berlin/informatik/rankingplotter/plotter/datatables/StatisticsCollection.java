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
		HIT_AT_1("HITAT1"),
		HIT_AT_5("HITAT5"),
		HIT_AT_10("HITAT10"),
		HIT_AT_20("HITAT20"),
		HIT_AT_30("HITAT30"),
		HIT_AT_50("HITAT50"),
		HIT_AT_100("HITAT100"),
		HIT_AT_INF("HITATINF"),

		MOD_CHANGE("MODCHANGE"),
		MOD_DELETE("MODDELETE"),
		MOD_INSERT("MODINSERT"),
		MOD_UNKNOWN("MODUNK"),

		SIGNIFICANCE_NONE("SIGNONE"),
		SIGNIFICANCE_LOW("SIGLOW"),
		SIGNIFICANCE_MEDIUM("SIGMEDIUM"),
		SIGNIFICANCE_HIGH("SIGHIGH"),
		SIGNIFICANCE_CRUCIAL("SIGCRUCIAL"),

		MEAN_RANK("MR"),
		MEAN_FIRST_RANK("MFR"),
		
		UNKNOWN("UNKNOWNCATEGORY");

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
