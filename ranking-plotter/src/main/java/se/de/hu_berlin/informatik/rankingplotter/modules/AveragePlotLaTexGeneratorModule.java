/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.modules;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.AveragePlotStatisticsCollection;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.AveragePlotStatisticsCollection.StatisticsCategories;
import se.de.hu_berlin.informatik.utils.fileoperations.ListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * A module that takes a {@link AveragePlotStatisticsCollection} object and produces 
 * various CSV files.
 * 
 * @author Simon Heiden
 */
public class AveragePlotLaTexGeneratorModule extends AbstractModule<AveragePlotStatisticsCollection, AveragePlotStatisticsCollection> {

	private String outputPrefix;

	/**
	 * Creates a new {@link AveragePlotLaTexGeneratorModule} object with the given parameters.
	 * @param outputPrefix
	 * the output filename prefix 
	 */
	public AveragePlotLaTexGeneratorModule(String outputPrefix) {
		super(true);
		this.outputPrefix = outputPrefix;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public AveragePlotStatisticsCollection processItem(AveragePlotStatisticsCollection tables) {

		for (Entry<StatisticsCategories, List<Double[]>> entry : tables.getStatisticsmap().entrySet()) {
			Path output = Paths.get(outputPrefix + "_" + entry.getKey() + ".tex");
			new ListToFileWriterModule<List<String>>(output, true)
			.submit(generateLaTexFromTable(tables.getIdentifier(), entry.getKey(), entry));
		}
		
		//MR + MFR
		{
			Path output = Paths.get(outputPrefix + "_" + StatisticsCategories.MEAN_RANK + "_" + StatisticsCategories.MEAN_FIRST_RANK + ".tex");
			new ListToFileWriterModule<List<String>>(output, true)
			.submit(generateLaTexFromTable(tables.getIdentifier(), StatisticsCategories.MEAN_RANK, 
					new AbstractMap.SimpleEntry<>(StatisticsCategories.MEAN_RANK, tables.getStatisticsmap().get(StatisticsCategories.MEAN_RANK)),
					new AbstractMap.SimpleEntry<>(StatisticsCategories.MEAN_FIRST_RANK, tables.getStatisticsmap().get(StatisticsCategories.MEAN_FIRST_RANK))));
		}
		
		//MEDR + MEDFR
		{
			Path output = Paths.get(outputPrefix + "_" + StatisticsCategories.MEDIAN_RANK + "_" + StatisticsCategories.MEDIAN_FIRST_RANK + ".tex");
			new ListToFileWriterModule<List<String>>(output, true)
			.submit(generateLaTexFromTable(tables.getIdentifier(), StatisticsCategories.MEDIAN_RANK, 
					new AbstractMap.SimpleEntry<>(StatisticsCategories.MEDIAN_RANK, tables.getStatisticsmap().get(StatisticsCategories.MEDIAN_RANK)),
					new AbstractMap.SimpleEntry<>(StatisticsCategories.MEDIAN_FIRST_RANK, tables.getStatisticsmap().get(StatisticsCategories.MEDIAN_FIRST_RANK))));
		}
		
		//MR + MEDR
		{
			Path output = Paths.get(outputPrefix + "_" + StatisticsCategories.MEAN_RANK + "_" + StatisticsCategories.MEDIAN_RANK + ".tex");
			new ListToFileWriterModule<List<String>>(output, true)
			.submit(generateLaTexFromTable(tables.getIdentifier(), StatisticsCategories.MEAN_RANK, 
					new AbstractMap.SimpleEntry<>(StatisticsCategories.MEAN_RANK, tables.getStatisticsmap().get(StatisticsCategories.MEAN_RANK)),
					new AbstractMap.SimpleEntry<>(StatisticsCategories.MEDIAN_RANK, tables.getStatisticsmap().get(StatisticsCategories.MEDIAN_RANK))));
		}
		
		//MFR + MEDFR
		{
			Path output = Paths.get(outputPrefix + "_" + StatisticsCategories.MEAN_FIRST_RANK + "_" + StatisticsCategories.MEDIAN_FIRST_RANK + ".tex");
			new ListToFileWriterModule<List<String>>(output, true)
			.submit(generateLaTexFromTable(tables.getIdentifier(), StatisticsCategories.MEAN_FIRST_RANK, 
					new AbstractMap.SimpleEntry<>(StatisticsCategories.MEAN_FIRST_RANK, tables.getStatisticsmap().get(StatisticsCategories.MEAN_FIRST_RANK)),
					new AbstractMap.SimpleEntry<>(StatisticsCategories.MEDIAN_FIRST_RANK, tables.getStatisticsmap().get(StatisticsCategories.MEDIAN_FIRST_RANK))));
		}
		
		return tables;

	}

	
	private static String truncateDoubleString(String string, int keepAfterPoint) {
		int pos = string.indexOf('.');
		if (pos != -1) {
			while (keepAfterPoint >= -1) {
				try {
					return string.substring(0, pos + 1 + keepAfterPoint);
				} catch(IndexOutOfBoundsException e) {
					--keepAfterPoint;
				}
			}
		}
		
		return string;
	}
	
	private static String truncateDoubleString(String string) {
		return truncateDoubleString(string, 2);
	}
	
	
	@SafeVarargs
	private static List<String> generateLaTexFromTable(String localizer, StatisticsCategories typeIdentifier, Entry<StatisticsCategories, List<Double[]>>... pairs) {
		List<String> lines = new ArrayList<>();

		appendHeader(localizer, typeIdentifier.toString(), lines);
		
		for(Entry<StatisticsCategories, List<Double[]>> plot : pairs) {
			appendPlotHeader(localizer, plot.getKey().toString(), lines);
			for(Double[] pair : plot.getValue()) {
				lines.add("          " + truncateDoubleString(String.valueOf(pair[0]/100.0)) + " " + truncateDoubleString(String.valueOf(pair[1])));
			}
			appendPlotFooter(localizer, lines);
		}
		
		appendFooter(localizer, lines);
		
		return lines;
	}
	
	private static void appendHeader(String legendEntry, String csvType, List<String> lines) {
		lines.add("%\\begin{figure}");
		lines.add("  \\begin{tikzpicture}[scale=\\plotscale" + csvType + "]");
		lines.add("    \\begin{axis}[");
		lines.add("      \\subplotstyle" + csvType + ",");
		lines.add("      %width=\\plotwidthscale" + csvType + "\\columnwidth,");
		lines.add("      %title={average LM ranking for different setups},");
		lines.add("      xlabel={$\\lambda$},");
		lines.add("      %ylabel={$\\overline{rank_{avg}(\\Omega,\\lambda)}$},");
		lines.add("      xmin=\\plotxmin" + csvType + ", xmax=\\plotxmax" + csvType + ",");
		lines.add("      ymin=\\plotymin" + csvType + ", ymax=\\plotymax" + csvType + ",");
		lines.add("      %xtick={0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0},");
		lines.add("      %ytick={9000, 10000, 11000, 12000, 13000},");
		lines.add("      legend pos=\\legendpos" + csvType + ",");
		lines.add("      ymajorgrids=true,");
		lines.add("      grid style=dashed,");
		lines.add("      ]");
	}
	
	private static void appendPlotHeader(String legendEntry, String csvType, List<String> lines) {
		lines.add("      \\addplot[color=black, mark=\\plotmark" + csvType + ",]");
		lines.add("        table {");
	}
	
	private static void appendPlotFooter(String legendEntry, List<String> lines) {
		String legendEntryShort = legendEntry.replace("_", "-") + "-";
		legendEntryShort = legendEntryShort.substring(0, legendEntryShort.indexOf("-"));
		lines.add("        };");
		lines.add("      \\addlegendentry{" + getEscapedLocalizerName(legendEntryShort) + "}");
	}
	
	private static void appendFooter(String legendEntry, List<String> lines) {
		lines.add("    \\end{axis}");
		lines.add("   \\end{tikzpicture}");
		lines.add("%  \\caption{}\\label{fig:csv-plot-" + legendEntry + "}");
		lines.add("%\\end{figure}");
	}
	
	private static String getEscapedLocalizerName(String localizer) {
		return "\\" + localizer.replace("1","ONE").replace("2","TWO").replace("3","THREE");
	}
}
