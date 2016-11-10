/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter.datatables;

/**
 * @author Simon Heiden
 */
public class SinglePlotStatisticsCollection extends StatisticsCollection<SinglePlotStatisticsCollection.StatisticsCategories> {
	
	public static enum StatisticsCategories {

		MOD_CHANGE("MODCHANGE"),
		MOD_DELETE("MODDELETE"),
		MOD_INSERT("MODINSERT"),
		MOD_UNKNOWN("MODUNK"),

		SIGNIFICANCE_NONE("SIGNONE"),
		SIGNIFICANCE_LOW("SIGLOW"),
		SIGNIFICANCE_MEDIUM("SIGMEDIUM"),
		SIGNIFICANCE_HIGH("SIGHIGH"),
		SIGNIFICANCE_CRUCIAL("SIGCRUCIAL"),
		
		ALL("ALL"),

		UNKNOWN("UNKNOWNCATEGORY");

		private final String identifier;
		StatisticsCategories(String identifier) {
			this.identifier = identifier;
		}
		@Override public String toString() { return identifier; }
	}

	public SinglePlotStatisticsCollection() {
		super();
	}

	public SinglePlotStatisticsCollection(String identifier) {
		super(identifier);
	}

}
