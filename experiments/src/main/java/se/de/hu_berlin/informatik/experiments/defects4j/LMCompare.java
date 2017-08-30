/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.Option;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.files.FileUtils.SearchOption;
import se.de.hu_berlin.informatik.utils.files.csv.CSVUtils;
import se.de.hu_berlin.informatik.utils.files.processors.ListToFileWriter;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.MathUtils;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;

/**
 * @author SimHigh
 */
public class LMCompare {

	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		SUFFIX("s", "suffix", true, "The suffix used for creating the ranking directory.", false),
		PLOT_DIR("i", "plotDir", true, "Path to the main plot directory.", false),

		OUTPUT("o", "output", true, "Output file prefix (LaTeX).", true);

		/* the following code blocks should not need to be changed */
		final private OptionWrapper option;

		// adds an option that is not part of any group
		CmdOptions(final String opt, final String longOpt, final boolean hasArg, final String description,
				final boolean required) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(required).hasArg(hasArg).desc(description).build(),
					NO_GROUP);
		}

		// adds an option that is part of the group with the specified index
		// (positive integer)
		// a negative index means that this option is part of no group
		// this option will not be required, however, the group itself will be
		CmdOptions(final String opt, final String longOpt, final boolean hasArg, final String description,
				int groupId) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(false).hasArg(hasArg).desc(description).build(),
					groupId);
		}

		// adds the given option that will be part of the group with the given
		// id
		CmdOptions(Option option, int groupId) {
			this.option = new OptionWrapper(option, groupId);
		}

		// adds the given option that will be part of no group
		CmdOptions(Option option) {
			this(option, NO_GROUP);
		}

		@Override
		public String toString() {
			return option.getOption().getOpt();
		}

		@Override
		public OptionWrapper getOptionWrapper() {
			return option;
		}
	}

	/**
	 * @param args
	 * command line arguments
	 */
	public static void main(String[] args) {

		OptionParser options = OptionParser.getOptions("LMCompare", true, CmdOptions.class, args);

		String input = options.getOptionValue(CmdOptions.PLOT_DIR, null);
		if (input != null && (new File(input)).isFile()) {
			Log.abort(LMCompare.class, "Given input path '%s' is a file.", input);
		}
		if (input == null) {
			input = Defects4J.getValueOf(Defects4JProperties.PLOT_DIR);
		}

		String suffix = options.getOptionValue(CmdOptions.SUFFIX, null);
		suffix = suffix == null ? "" : "_" + suffix;

		File inputDir = new File(input);

		/*
		 * #====================================================================
		 * # iterate through all subdirectories and collect the LM ranking
		 * statistics
		 * #====================================================================
		 */

		// depth -> order -> LM ranking value
		Map<Integer, Map<Integer, Double>> MRdata = new HashMap<>();
		Map<Integer, Map<Integer, Double>> MFRdata = new HashMap<>();
		Map<Integer, Map<Integer, Double>> MEDRdata = new HashMap<>();
		Map<Integer, Map<Integer, Double>> MEDFRdata = new HashMap<>();
		
		// we assume a directory structure like this:
		// ...plotDir/average[_suffix]/project[_normalization]/lm_ranking_id/sbfl_localizer_id/someFile.csv

		File suffixDir = FileUtils.searchDirectoryContainingPattern(inputDir, "average" + suffix, SearchOption.ENDS_WITH);
		if (suffixDir == null) {
			Log.abort(LMCompare.class, "Can not find directory ending with '%s' in '%s'.", "average" + suffix, inputDir);
		}
		
		for (int d = 0; d < 6; ++d) {
			for (int order = 2; order <= 10; ++order) {
				File lmDir = FileUtils.searchDirectoryContainingPattern(inputDir, "_d" + d + "_order" + order, 1);
				if (lmDir != null) {
					for (File localizerDir : lmDir.listFiles()) {
						if (!localizerDir.isDirectory() || localizerDir.getName().startsWith("_")) {
							continue;
						} else {
							getDataFromCSV(MRdata, d, order, localizerDir, "_MR.csv");
							getDataFromCSV(MFRdata, d, order, localizerDir, "_MFR.csv");
							getDataFromCSV(MEDRdata, d, order, localizerDir, "_MEDR.csv");
							getDataFromCSV(MEDFRdata, d, order, localizerDir, "_MEDFR.csv");
							// only collect data for one localizer!
							break;
						}
					}
				} else {
					Log.warn(LMCompare.class, "Did not find plot directory containing pattern '%s'.", "_d" + d + "_order" + order);
				}
			}
		}

		String[] colors = { "blue", "red", "green", "black", "orange", "yellow", "blue", "red" };
		String[] marks = { "square", "o", "triangle", "*", "+", "|", "circle", "x" };

		generateLatexFile(options, MRdata, colors, marks, "_MR");
		generateLatexFile(options, MFRdata, colors, marks, "_MFR");
		generateLatexFile(options, MEDRdata, colors, marks, "_MEDR");
		generateLatexFile(options, MEDFRdata, colors, marks, "_MEDFR");

		Log.out(LMCompare.class, "All done!");

	}

	private static void generateLatexFile(OptionParser options, Map<Integer, Map<Integer, Double>> MRdata,
			String[] colors, String[] marks, String MR_suffix) {
		String MROutput = options.getOptionValue(CmdOptions.OUTPUT) + MR_suffix + ".tex";
		List<String> lines = new ArrayList<>();
		appendHeader(lines);
		for (Entry<Integer, Map<Integer, Double>> depthEntry : MRdata.entrySet()) {
			int d = depthEntry.getKey();
			appendPlotHeader(colors[d], marks[d], lines);
			StringBuilder stringBuilder = new StringBuilder("      ");
			for (Entry<Integer, Double> orderEntry : depthEntry.getValue().entrySet()) {
				stringBuilder.append("(").append(orderEntry.getKey()).append(",")
						.append(MathUtils.roundToXDecimalPlaces(orderEntry.getValue(), 2)).append(")");
			}
			lines.add(stringBuilder.toString());
			appendPlotFooter(d, lines);
		}
		appendFooter(MR_suffix, lines);
		new ListToFileWriter<List<String>>(Paths.get(MROutput), true).submit(lines);
	}

	private static void getDataFromCSV(Map<Integer, Map<Integer, Double>> MRdata, int d, int order, File localizerDir,
			String pattern) {
		File csv = FileUtils.searchFileContainingPattern(localizerDir, pattern);
		if (csv != null) {
			List<Double[]> rows = CSVUtils.readCSVFileToListOfDoubleArrays(csv.toPath());
			for (Double[] row : rows) {
				if (row.length == 2 && row[0] == 0) {
					MRdata.computeIfAbsent(d, k -> new HashMap<>()).put(order, row[1]);
					break;
				}
			}
		}
	}

	private static void appendHeader(List<String> lines) {
		lines.add("%\\begin{figure}");
		lines.add("  \\begin{tikzpicture}[scale=\\plotscaleLM]");
		lines.add("    \\begin{axis}[");
		lines.add("      small,");
		lines.add("      height=0.18\\textheight,");
		lines.add("      scaled y ticks = false,");
		lines.add("        y tick label style={/pgf/number format/fixed,");
		lines.add("        /pgf/number format/1000 sep = \\thinspace},");
		lines.add("      xlabel={order $n$},");
		lines.add("      %ylabel={$\\mr_0(\\Omega)$},");
		lines.add("      xmin=1.5, xmax=8.5,");
		lines.add("      ymin=0, ymax=5000,");
		lines.add("      xtick={2, 3, 4, 5, 6, 7, 8},");
		lines.add("      %ytick={9000, 10000, 11000, 12000, 13000},");
		lines.add("      legend pos=outer north east,");
		lines.add("      ymajorgrids=true,");
		lines.add("      grid style=dashed,");
		lines.add("      ]");
	}

	private static void appendPlotHeader(String color, String mark, List<String> lines) {
		// \edef\tmp{\noexpand\addplot[{\plotoptionsMR}]}\tmp
		lines.add("");
		lines.add("      \\addplot[");
		lines.add("      color=" + color + ",");
		lines.add("      mark=" + mark + ",");
		lines.add("      ]");
		lines.add("      coordinates {");
	}

	private static void appendPlotFooter(int depth, List<String> lines) {
		lines.add("      };");
		lines.add("      \\addlegendentry{$d=" + depth + "$}");
	}

	private static void appendFooter(String type, List<String> lines) {
		lines.add("");
		lines.add("    \\end{axis}");
		lines.add("   \\end{tikzpicture}");
		lines.add("%  \\caption{}\\label{fig:lm-plot-" + type + "}");
		lines.add("%\\end{figure}");
	}

}
