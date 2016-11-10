/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.modules;

import java.io.File;
import java.util.EnumSet;
import java.util.List;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.StatisticsCollection;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.StatisticsCollection.StatisticsCategories;
import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.fileoperations.csv.CSVUtils;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * @author Simon Heiden
 */
public class CsvToStatisticsCollectionModule extends AbstractModule<File, StatisticsCollection> {

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
	public StatisticsCollection processItem(File csvFileLocation) {
		
		StatisticsCollection tables = new StatisticsCollection(localizer);
		
		//generate enum set with everything but unknown stuff
		EnumSet<StatisticsCategories> set = EnumSet.complementOf(EnumSet.of(StatisticsCategories.UNKNOWN));
		
		for (StatisticsCategories category : set) {
			File csvFile = FileUtils.searchFileContainingPattern(csvFileLocation, "_" + category + ".csv");
			if (csvFile == null) {
				//Log.err(this, "Couldn't find file '%s' in '%s'.", "_" + category + ".csv", csvFileLocation);
				continue;
			}
			
			List<Double[]> data = CSVUtils.readCSVFileToListOfDoubleArrays(csvFile.toPath());
			
			tables.setValueList(category, data);
		}
		
		return tables;
	}
	
}
