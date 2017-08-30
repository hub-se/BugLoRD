/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.modules;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.SinglePlotStatisticsCollection;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.SinglePlotStatisticsCollection.StatisticsCategories;
import se.de.hu_berlin.informatik.utils.files.csv.CSVUtils;
import se.de.hu_berlin.informatik.utils.files.processors.ListToFileWriter;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * A module that takes a {@link SinglePlotStatisticsCollection} object and produces 
 * various CSV files.
 * 
 * @author Simon Heiden
 */
public class SinglePlotCSVGeneratorModule extends AbstractProcessor<SinglePlotStatisticsCollection, SinglePlotStatisticsCollection> {

	private String outputPrefix;

	/**
	 * Creates a new {@link SinglePlotCSVGeneratorModule} object with the given parameters.
	 * @param outputPrefix
	 * the output filename prefix 
	 */
	public SinglePlotCSVGeneratorModule(String outputPrefix) {
		super();
		this.outputPrefix = outputPrefix;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public SinglePlotStatisticsCollection processItem(SinglePlotStatisticsCollection tables) {

		//create CSV files from all included tables
		for (Entry<StatisticsCategories, List<Double[]>> entry : tables.getStatisticsmap().entrySet()) {
			Path output = Paths.get(outputPrefix + "_" + entry.getKey() + ".csv");
			new ListToFileWriter<List<String>>(output, true)
			.submit(CSVUtils.toCsv(entry.getValue()));
		}
		
		return tables;

	}
	
}
