/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.modules;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.SinglePlotStatisticsCollection;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.SinglePlotStatisticsCollection.StatisticsCategories;
import se.de.hu_berlin.informatik.utils.files.processors.ListToFileWriter;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * A module that takes a {@link SinglePlotStatisticsCollection} object and produces 
 * various CSV files.
 * 
 * @author Simon Heiden
 */
public class SinglePlotLaTexGeneratorModule extends AbstractProcessor<SinglePlotStatisticsCollection, SinglePlotStatisticsCollection> {

	private String outputPrefix;

	/**
	 * Creates a new {@link SinglePlotLaTexGeneratorModule} object with the given parameters.
	 * @param outputPrefix
	 * the output filename prefix 
	 */
	public SinglePlotLaTexGeneratorModule(String outputPrefix) {
		super();
		this.outputPrefix = outputPrefix;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public SinglePlotStatisticsCollection processItem(SinglePlotStatisticsCollection tables) {

		for (Entry<StatisticsCategories, List<Double[]>> entry : tables.getStatisticsmap().entrySet()) {
			Path output = Paths.get(outputPrefix + "_" + entry.getKey() + ".tex");
			new ListToFileWriter<List<String>>(output, true)
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
			lines.add("          " + truncateDoubleString(String.valueOf(pair[0]/100.0)) + 
					" " + truncateDoubleString(String.valueOf(pair[1])) + 
					" " + truncateDoubleString(String.valueOf(pair[2])) + 
					" " + truncateDoubleString(String.valueOf(pair[3])));
		}
		
		appendMiddle(typeIdentifier.toString(), lines);
		
		for(Double[] pair : pairs) {
			lines.add("          " + truncateDoubleString(String.valueOf(pair[0]/100.0)) + 
					" " + truncateDoubleString(String.valueOf(pair[1])) + 
					" " + truncateDoubleString(String.valueOf(pair[2])) + 
					" " + truncateDoubleString(String.valueOf(pair[3])));
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
		lines.add("      \\addplot+[forget plot, mark size=1, only marks, mark options={black}, error bars/error bar style={red}, mark=\\plotmark" + csvType + ",error bars/.cd,y dir=plus,y explicit]");
		lines.add("        table[x=x, y=y, y error expr=\\thisrow{y_max}-\\thisrow{y}] {");
		lines.add("          x y y_min y_max");
	}
	
	private static void appendMiddle(String csvType, List<String> lines) {
		lines.add("        };");
		lines.add("      \\addplot+[mark size=1, mark options={black}, error bars/error bar style={red}, only marks, mark=\\plotmark" + csvType + ",error bars/.cd,y dir=minus,y explicit]");
		lines.add("        table[x=x,y=y,y error expr=\\thisrow{y}-\\thisrow{y_min}] {");
		lines.add("          x y y_min y_max");
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
