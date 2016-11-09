/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.modules;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.StatisticsCollection;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.StatisticsCollection.StatisticsCategories;
import se.de.hu_berlin.informatik.utils.fileoperations.csv.CSVUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * @author Simon Heiden
 */
public class CsvToStatisticsCollectionModule extends AbstractModule<Path, StatisticsCollection> {

	private final String localizer;
	
	/**
	 * Creates a new {@link CsvToStatisticsCollectionModule} object with the given parameters.
	 * @param localizer
	 * the current localizer
	 */
	public CsvToStatisticsCollectionModule(String localizer) {
		super(true);
		this.localizer = localizer;
	}
	
	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public StatisticsCollection processItem(Path outputPrefix) {
		
		StatisticsCollection tables = new StatisticsCollection(localizer);
		
		for (StatisticsCategories category : EnumSet.allOf(StatisticsCategories.class)) {
			Path csvFile = Paths.get(outputPrefix + "_" + category + ".csv");
			if (!csvFile.toFile().exists()) {
				Log.err(this, "File '%s' doesn't exist.", csvFile);
				continue;
			}
			
			List<Double[]> data = CSVUtils.readCSVFileToListOfDoubleArrays(csvFile);
			
			tables.setValueList(category, data);
		}
		
		return tables;
	}
	
}
