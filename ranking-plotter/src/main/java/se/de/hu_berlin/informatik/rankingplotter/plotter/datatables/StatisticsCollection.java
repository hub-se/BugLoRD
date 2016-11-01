/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter.datatables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Simon Heiden
 */
public class StatisticsCollection {
	
	public static final String MFR_ID = "o";
	public static final String ALL_ID = "x";
	public static final String UNSIGNIFICANT_ID = "n";
	public static final String LOW_SIGNIFICANCE_ID = "l";
	public static final String MEDIUM_SIGNIFICANCE_ID = "m";
	public static final String HIGH_SIGNIFICANCE_ID = "h";
	public static final String CRUCIAL_SIGNIFICANCE_ID = "c";
	
	public static final String CHANGE_ID = "chg";
	public static final String DELETE_ID = "del";
	public static final String INSERT_ID = "ins";
	public static final String UNKNOWN_ID = "unk";
	
	public static final String HIT_AT_1 = "@1";
	public static final String HIT_AT_5 = "@5";
	public static final String HIT_AT_10 = "@10";
	public static final String HIT_AT_20 = "@20";
	public static final String HIT_AT_30 = "@30";
	public static final String HIT_AT_50 = "@50";
	public static final String HIT_AT_100 = "@100";
	public static final String HIT_AT_INF = "@inf";
	
	public static final String MOD_CHANGE = "CHANGE";
	public static final String MOD_DELETE = "DELETE";
	public static final String MOD_INSERT = "INSERT";
	public static final String MOD_UNKNOWN = "UNK";
	
	public static final String SIGNIFICANCE_NONE = "NONE";
	public static final String SIGNIFICANCE_LOW = "LOW";
	public static final String SIGNIFICANCE_MEDIUM = "MEDIUM";
	public static final String SIGNIFICANCE_HIGH = "HIGH";
	public static final String SIGNIFICANCE_CRUCIAL = "CRUCIAL";
	
	public static final String SIGNIFICANCE_ALL = "ALL";
	
	public static final String MEAN_FIRST_RANK = "FIRST_RANK";

	private Map<String, List<Double[]>> statisticsMap = new HashMap<>();
	
	public StatisticsCollection() {
		super();
	}

	public boolean addValuePair(String tableIdentifier, Double... values) {
		return statisticsMap.computeIfAbsent(tableIdentifier, k -> new ArrayList<>()).add(values);
	}
	
	public List<Double[]> getStatistics(String tableIdentifier) {
		return statisticsMap.get(tableIdentifier);
	}
	
	public Map<String, List<Double[]>> getStatisticsmap() {
		return statisticsMap;
	}

}
