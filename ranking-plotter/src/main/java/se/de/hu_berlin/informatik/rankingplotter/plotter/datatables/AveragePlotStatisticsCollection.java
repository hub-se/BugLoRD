/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter.datatables;

import java.util.Map;
import se.de.hu_berlin.informatik.rankingplotter.plotter.RankingFileWrapper;

/**
 * @author Simon Heiden
 */
public class AveragePlotStatisticsCollection extends StatisticsCollection<AveragePlotStatisticsCollection.StatisticsCategories> {
	
	public static enum StatisticsCategories {
		HIT_AT_1("HITAT1"),
		HIT_AT_5("HITAT5"),
		HIT_AT_10("HITAT10"),
		HIT_AT_20("HITAT20"),
		HIT_AT_30("HITAT30"),
		HIT_AT_50("HITAT50"),
		HIT_AT_100("HITAT100"),

		MOD_CHANGE("AVGMODCHANGE"),
		MOD_DELETE("AVGMODDELETE"),
		MOD_INSERT("AVGMODINSERT"),
		MOD_UNKNOWN("AVGMODUNK"),

		SIGNIFICANCE_NONE("AVGSIGNONE"),
		SIGNIFICANCE_LOW("AVGSIGLOW"),
		SIGNIFICANCE_MEDIUM("AVGSIGMEDIUM"),
		SIGNIFICANCE_HIGH("AVGSIGHIGH"),
		SIGNIFICANCE_CRUCIAL("AVGSIGCRUCIAL"),
		
		ALL("HITALL"),

		MEAN_RANK("MR"),
		MEAN_FIRST_RANK("MFR"),
		MEDIAN_RANK("MEDR"),
		MEDIAN_FIRST_RANK("MEDFR"),
		
		UNKNOWN("AVGUNKNOWNCATEGORY");

		private final String identifier;
		StatisticsCategories(String identifier) {
			this.identifier = identifier;
		}
		@Override public String toString() { return identifier; }
	}

	//percentage -> project -> id -> ranking wrapper
	private final Map<Double,Map<String, Map<Integer, RankingFileWrapper>>> percentageToProjectToBugToRanking;
	
	public AveragePlotStatisticsCollection(String identifier, Map<Double,Map<String, Map<Integer, RankingFileWrapper>>> percentageToProjectToBugToRanking) {
		super(identifier);
		this.percentageToProjectToBugToRanking = percentageToProjectToBugToRanking;
	}
	
	public AveragePlotStatisticsCollection(String identifier) {
		this(identifier, null);
	}
	
	public AveragePlotStatisticsCollection() {
		this("", null);
	}

	public Map<Double,Map<String, Map<Integer, RankingFileWrapper>>> getPercentageToProjectToBugToRankingMap() {
		return percentageToProjectToBugToRanking;
	}

}
