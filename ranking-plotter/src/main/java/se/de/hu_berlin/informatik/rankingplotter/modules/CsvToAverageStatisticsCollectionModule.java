package se.de.hu_berlin.informatik.rankingplotter.modules;

import java.io.File;
import java.util.EnumSet;
import java.util.List;

import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.AveragePlotStatisticsCollection;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.AveragePlotStatisticsCollection.StatisticsCategories;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.files.csv.CSVUtils;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * @author Simon Heiden
 */
public class CsvToAverageStatisticsCollectionModule extends AbstractProcessor<File, AveragePlotStatisticsCollection> {

	private final String localizer;
	
	/**
	 * Creates a new {@link CsvToAverageStatisticsCollectionModule} object with the given parameters.
	 * @param localizer
	 * the current localizer
	 */
	public CsvToAverageStatisticsCollectionModule(String localizer) {
		super();
		this.localizer = localizer;
	}
	
	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public AveragePlotStatisticsCollection processItem(File csvFileLocation) {
		
		AveragePlotStatisticsCollection tables = new AveragePlotStatisticsCollection(localizer);
		
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
