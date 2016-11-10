/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.modules;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.AveragePlotStatisticsCollection;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.AveragePlotStatisticsCollection.StatisticsCategories;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.StatisticsCollection;
import se.de.hu_berlin.informatik.utils.fileoperations.ListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * A module that takes a {@link StatisticsCollection} object and produces 
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
			.submit(generateLaTexFromTable(entry.getValue(), tables.getIdentifier(), entry.getKey()));
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
	
	
	private static List<String> generateLaTexFromTable(List<Double[]> pairs, String localizer, StatisticsCategories typeIdentifier) {
		List<String> lines = new ArrayList<>();

		appendHeader(localizer, typeIdentifier.toString(), lines);
		
		for(Double[] pair : pairs) {
			lines.add("          " + truncateDoubleString(String.valueOf(pair[0]/100.0)) + " " + truncateDoubleString(String.valueOf(pair[1])));
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
		lines.add("      \\addplot[color=black, mark=\\plotmark" + csvType + ",]");
		lines.add("        table {");
	}
	
	
	private static void appendFooter(String legendEntry, List<String> lines) {
		String legendEntryShort = legendEntry.replace("_", "-") + "-";
		legendEntryShort = legendEntryShort.substring(0, legendEntryShort.indexOf("-"));
		lines.add("        };");
		lines.add("      \\addlegendentry{" + getEscapedLocalizerName(legendEntryShort) + "}");
		lines.add("    \\end{axis}");
		lines.add("   \\end{tikzpicture}");
		lines.add("%  \\caption{}\\label{fig:csv-plot-" + legendEntry + "}");
		lines.add("%\\end{figure}");
	}
	
	private static String getEscapedLocalizerName(String localizer) {
		return "\\" + localizer.replace("1","ONE").replace("2","TWO").replace("3","THREE");
	}
}
