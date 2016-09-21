/*
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.rankingplotter.modules.CombiningRankingsModule;
import se.de.hu_berlin.informatik.rankingplotter.modules.DataLabelAdderModule;
import se.de.hu_berlin.informatik.rankingplotter.modules.PlotModule;
import se.de.hu_berlin.informatik.rankingplotter.modules.RankingAveragerModule;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.DataTableCollection;
import se.de.hu_berlin.informatik.utils.fileoperations.FileLineProcessorModule;
import se.de.hu_berlin.informatik.utils.fileoperations.SearchForFilesOrDirsModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.ModuleLinker;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.PipeLinker;
import se.de.hu_berlin.informatik.utils.tm.pipes.SearchFileOrDirPipe;
import se.de.hu_berlin.informatik.utils.tm.pipes.ThreadedProcessorPipe;


/**
 * Plots SBFL and NLFL rankings.
 * 
 * @author Simon Heiden
 */
public class Plotter {
	
	public final static String STRAT_NOCHANGE = "NOCHANGE";
	public final static String STRAT_BEST = "BEST";
	public final static String STRAT_AVERAGE = "AVERAGE";
	public final static String STRAT_WORST = "WORST";
	
	public enum ParserStrategy { NO_CHANGE(0), BEST_CASE(1), AVERAGE_CASE(2), WORST_CASE(3);
		private final int id;
		private ParserStrategy(int id) {
			this.id = id;
		}

		@Override
		public String toString() {
			switch(id) {
			case 0:
				return STRAT_NOCHANGE;
			case 1:
				return STRAT_BEST;
			case 2:
				return STRAT_AVERAGE;
			case 3:
				return STRAT_WORST;
			default:
				return STRAT_NOCHANGE;
			}
		}
	}
	
	/**
	 * Parses the options from the command line.
	 * @param args
	 * the application's arguments
	 * @return
	 * an {@link OptionParser} object that provides access to all parsed options and their values
	 */
	private static OptionParser getOptions(String[] args) {
//		final String tool_usage = "Plotter -i input-dir [(-p|-a) loc1 loc2 ...] -o output-dir output-prefix [-r range] [-l] [-pdf] [-png] [-u unranked-file] [-m ranked-file] [-s] [-z]"; 
		final String tool_usage = "Plotter";
		final OptionParser options = new OptionParser(tool_usage, true, args);
		
		options.add("i", "input", true, "Path to ranking directory or directory with defects4J projects.", true);
		options.addGroup(true,
				Option.builder("p").longOpt("plot").hasArgs().optionalArg(true)
				.desc("Create plots of the specified subfolders of the input ranking directory "
						+ "(if given), or of all subfolders (if none are given).").build(),
				Option.builder("a").longOpt("averagePlot").hasArgs()
				.desc("Parse all Rankings of the specified types (e.g. 'tarantula') that are found "
						+ "anywhere in the input directory and plot the averages.").build(),
				Option.builder("g").longOpt("globalAveragePlot").hasArgs()
				.desc("Parse all .csv data files in the specified localizer directories (e.g. 'tarantula') that are found "
						+ "anywhere in the input directory and plot the averages.").build()
        		);
		
		options.add(Option.builder("o").longOpt("output").hasArgs().numberOfArgs(2).required()
				.desc("Path to output directory and a prefix for the output files (two arguments).").build());
		options.add(Option.builder("r").longOpt("range").hasArgs().desc("y-axis range (relative). Default will be 50 if not auto-sized. "
				+ "If two arguments are given, they are treated as an absolute range.").build());
		options.add("l", "labelsOn", false, "Should labels be plotted (a/c/d instead of points).");

		options.add("zero", "ignoreUnranked", false, "Should rankings that are equal or below zero be ignored?");
		options.add("c", "connectPoints", false, "When plotting averages, should the data points be connected with lines?");
		
		options.add("strat", "parserStrategy", true, "What strategy should be used when encountering a range of"
				+ "equal rankings. Options are: 'BEST', 'WORST', 'NOCHANGE' and 'AVERAGE'. Default is 'NOCHANGE'.");
		
		options.add("u", "uModFile", true, "Path to a file with unranked modified lines (path:line# <a|c|d>).");
		options.add("m", "rModFile", true, "Path to a file with ranked modified lines (path:line# <a|c|d>).");
		
		options.add("s", "showPanel", false, "Should the plot be shown in a panel? (Only in desktop execution.)");
		
		options.add(Option.builder("autoY").longOpt("autoSizeY").hasArgs().optionalArg(true)
				.desc("Should the plot be sized, so that all data fits inside? If a range is set, data above the range gets clipped. "
						+ "Optional arguments are the data columns that should be included in the auto sizing. If no arguments are given, "
						+ "all columns are involved.")
				.build());
		options.add("height", "plotHeight", true, "An absolute height for the plot. The default is equal to the given range of values.");
		
		options.add("single", "plotSingleTables", false, "Whether single tables should be plotted in different plots. (additional plots)");
		options.add("all", "plotAverageOverAll", false, "Whether all modifications should be treated as equal. (additional plot)");
		options.add("mfr", "plotMeanFirstRank", false, "Whether the mean first ranks should be plotted. (additional plot)");
		options.add("hit", "plotHitAtX", false, "Whether the HitAtX rankings should be plotted. (additional plot)");
		
		options.add("png", "savePng", false, "png output format."); 
		options.add("pdf", "savePdf", false, "pdf output format.");
		options.add("eps", "saveEps", false, "eps output format."); 
		options.add("svg", "saveSvg", false, "svg output format.");
		
		options.add(Option.builder("gp").longOpt("globalPercentages")
        		.hasArgs().desc("Global Percentages (with multiple arguments).")
        		.build());
		options.add(Option.builder("lp").longOpt("localPercentages")
        		.hasArgs().desc("Local Percentages (with multiple arguments).")
        		.build());
		
		options.add("csv", "saveCsv", false, "Save data as .csv files.");
        
        options.parseCommandLine();
        
        return options;
	}

	/**
	 * @param args
	 * -i input-dir [(-p|-a) loc1 loc2 ...] -o output-dir output-prefix [-r range] [-l] [-pdf] [-png] [-u unranked-file] [-m ranked-file] [-s] [-z]
	 */
	public static void main(String[] args) {
		
		OptionParser options = getOptions(args);
		
		//directory with SBFL ranking 	(format: relative/path/To/File:lineNumber: ranking)
		Path inputDir = options.isDirectory('i', true);
		
		//output file path
		String outputDir = options.isDirectory('o', false).toString();
		
		String outputPrefix = "";
		if (options.getOptionValues('o').length > 1) {
			outputPrefix = options.getOptionValues('o')[1];
		}

		boolean pdf = options.hasOption("pdf");
		boolean png = options.hasOption("png");
		boolean eps = options.hasOption("eps");
		boolean svg = options.hasOption("svg");
		boolean csv = options.hasOption("csv");
		boolean showPanel = options.hasOption('s');
		
		if (!pdf && !png && !eps && !svg && !showPanel && !csv) {
			Log.abort(Plotter.class, "No output format or visual display set.");
		}
		
		Integer[] range = null;
		if (options.getOptionValues('r') != null) {
			List<Integer> templist = new ArrayList<>(options.getOptionValues('r').length);
			for (String element : options.getOptionValues('r')) {
				templist.add(Integer.parseInt(element));
			}
			Collections.sort(templist);
			range = templist.toArray(new Integer[0]);
		}
		
		Integer plotHeight = null; 
		if (options.hasOption("height")) {
			plotHeight = Integer.parseInt(options.getOptionValue("height"));
		}
		
		ParserStrategy strategy = ParserStrategy.NO_CHANGE;
		if (options.hasOption("strat")) {
			switch(options.getOptionValue("strat")) {
			case STRAT_BEST:
				strategy = ParserStrategy.BEST_CASE;
				break;
			case STRAT_WORST:
				strategy = ParserStrategy.WORST_CASE;
				break;
			case STRAT_AVERAGE:
				strategy = ParserStrategy.AVERAGE_CASE;
				break;
			case STRAT_NOCHANGE:
				strategy = ParserStrategy.NO_CHANGE;
				break;
			default:
				Log.abort(Plotter.class, "Unknown strategy: '%s'", options.getOptionValue("strat"));
			}
		}
		
		Integer[] autoYvalues = null;
		String[] autoYvaluesStrings = options.getOptionValues("autoY");
		if (autoYvaluesStrings != null) {
			List<Integer> templist = new ArrayList<>(autoYvaluesStrings.length);
			for (String element : autoYvaluesStrings) {
				templist.add(Integer.parseInt(element));
			}
			Collections.sort(templist);
			autoYvalues = templist.toArray(new Integer[0]);
		}
		
		if (options.hasOption('p')) {
			String title = "";
			if (options.getOptionValue('m', null) != null) {
				Path file = Paths.get(options.getOptionValue('m'));
				if (file.toFile().isDirectory()) {
					options.printHelp(1, "m");
				}

				title += new FileLineProcessorModule<String>(new TitleBuilder("ranked"))
						.submit(file)
						.getResult();
			}

			if (options.getOptionValue('u', null) != null) {
				final Path file = Paths.get(options.getOptionValue('u'));
				if (file.toFile().isDirectory()) {
					options.printHelp(1, "u");
				}

				if (options.hasOption('m')) {
					title += ",   ";
				}

				title += new FileLineProcessorModule<String>(new TitleBuilder("unranked"))
						.submit(file)
						.getResult();
			}

			String[] folderNames = options.getOptionValues('p');
			
			List<Path> folderList;
			if (folderNames == null) {
				folderList = new SearchForFilesOrDirsModule(null, false).searchForDirectories()
						.submit(inputDir)
						.getResult();
			} else {
				folderList = new ArrayList<>();
				for (String folderName : folderNames) {
					folderName = folderName.toLowerCase();
					folderList.add(Paths.get(inputDir.toString(), folderName));
				}
			}
			
			for (Path localizerDir : folderList) {
				Log.out(Plotter.class, "Plotting rankings in '" + localizerDir + "'.");

				new ModuleLinker().link(
						new CombiningRankingsModule(true, strategy, false, options.hasOption("zero"), 
								options.getOptionValues("gp"), options.getOptionValues("lp")), 
						new DataLabelAdderModule(localizerDir.getFileName().toString(), range), 
						new PlotModule(options.hasOption('l'), false, title, range, pdf, png, eps, svg, 
								outputDir + File.separator + localizerDir.getFileName().toString() + File.separator + outputPrefix, 
								showPanel, csv, options.hasOption("autoY"), autoYvalues, plotHeight, 
								options.hasOption("single"), false, false, false))
				.submit(localizerDir.resolve("ranking.rnk"));

				Log.out(Plotter.class, "...Done with '" + localizerDir + "'.");
			}
		} else if (options.hasOption('a')) {
			for (String localizerDir : options.getOptionValues('a')) {
				localizerDir = localizerDir.toLowerCase();
				Log.out(Plotter.class, "Submitting '" + localizerDir + "'.");
				
				//Creates a list of all directories with the same name (localizerDir), sequences the list and
				//parses all found combined rankings and computes the averages. Parsing and averaging is done 
				//as best as possible in parallel with pipes.
				//When all averages are computed, we can plot the results (collected by the averager module).
				new PipeLinker().link(
						new SearchFileOrDirPipe("**/" + localizerDir + "/ranking.rnk").searchForFiles(),
						new ThreadedProcessorPipe<Path,List<RankingFileWrapper>>(options.getNumberOfThreads(4), 
								new CombiningRankingsCall.Factory(strategy, options.hasOption("zero"), 
										options.getOptionValues("gp"), options.getOptionValues("lp"))),
						new RankingAveragerModule(localizerDir, range)
						.enableTracking(1),
						new PlotModule(options.hasOption('l'), options.hasOption('c'),
								/*localizerDir + " averaged"*/ null, range, pdf, png, eps, svg,
								outputDir + File.separator + localizerDir + File.separator + localizerDir + "_" + outputPrefix, 
								showPanel, csv, options.hasOption("autoY"), autoYvalues, plotHeight, 
								options.hasOption("single"), options.hasOption("all"), options.hasOption("mfr"), options.hasOption("hit")))
				.submitAndShutdown(inputDir);
				
				Log.out(Plotter.class, "...Done with '" + localizerDir + "'.");
			}
		} else if (options.hasOption('g')) {
			for (String localizerDir : options.getOptionValues('g')) {
				localizerDir = localizerDir.toLowerCase();
				Log.out(Plotter.class, "Submitting '" + localizerDir + "'.");
				
				//searches all csv files in the given localizer directories over a range of projects.
				//all included data points with the same modification id ('a', 'c', 'd' or 'n') get averaged
				//and get plotted in the end.
				new PipeLinker().link(
						new SearchFileOrDirPipe("**/" + localizerDir + "/*.csv").searchForFiles()
						.enableTracking(10),
						new FileLineProcessorModule<DataTableCollection>(new CSVDataCollector()),
						new PlotModule(options.hasOption('l'), options.hasOption('c'),
								/*localizerDir + ": all projects averaged"*/null, range, pdf, png, eps, svg,
								outputDir + File.separator + localizerDir + File.separator + localizerDir + "_" + outputPrefix, 
								showPanel, csv, options.hasOption("autoY"), autoYvalues, plotHeight, 
								options.hasOption("single"), options.hasOption("all"), options.hasOption("mfr"), options.hasOption("hit")))
				.submitAndShutdown(inputDir);
				
				Log.out(Plotter.class, "...Done with '" + localizerDir + "'.");
			}
		}
		
	}
	
	/**
	 * Convenience method for easier use in a special case.
	 * @param projectId
	 * the identifier of a single Defects4J project (for naming the output file)
	 * @param bugId
	 * a bug id (for naming the output file)
	 * @param rankingDir
	 * the directory containing the ranking files to consider
	 * @param outputDir
	 * the main output directory
	 * @param range
	 * an upper limit for the y-axis
	 * @param height
	 * the plot height
	 * @param localizers
	 * a list of SBFL localizers to consider
	 * @param lp 
	 * an array of percentage values that determine the weighting 
	 * of the SBFL ranking to the NLFL ranking
	 * @param gp 
	 * an array of percentage values that determine the weighting 
	 * of the global NLFL ranking to the local NLFL ranking
	 */
	public static void plotSingleDefects4JElement(
			String projectId, String bugId, String rankingDir, String outputDir, 
			String range, String height, String[] localizers, String[] gp, String[] lp) {
		String[] args = { 
				"-i", rankingDir,
				"-o", outputDir, projectId + "-" + bugId,
				"-png",
				"-r", range,
				"-height", height,
				"-p" };
		
		args = Misc.joinArrays(args, localizers);
		
		String[] args2 = { "-gp" };
		args2 = Misc.joinArrays(args2, gp);
		String[] args3 = { "-lp" };
		args3 = Misc.joinArrays(args3, lp);
		
		args = Misc.joinArrays(args, args2);
		args = Misc.joinArrays(args, args3);
		
		main(args);
	}
	
	/**
	 * Convenience method for easier use in a special case.
	 * @param projectDir
	 * the project directory
	 * @param outputDir
	 * the main output directory
	 * @param strategy
	 * the strategy to use when encountering ranges of equal rankings
	 * @param height
	 * the plot height
	 * @param localizers
	 * a list of SBFL localizers to consider
	 * @param lp 
	 * an array of percentage values that determine the weighting 
	 * of the SBFL ranking to the NLFL ranking
	 * @param gp 
	 * an array of percentage values that determine the weighting 
	 * of the global NLFL ranking to the local NLFL ranking
	 */
	public static void plotAverageDefects4JProject(
			String projectDir, String outputDir, ParserStrategy strategy,
			String height, String[] localizers, String[] gp, String[] lp) {
		String[] args = { 
				"-i", projectDir,
				"-o", outputDir, "average_" + strategy.toString(),
				"-all", "-single", "-mfr", //"-hit",
				"-png", "-csv",
				"-strat", strategy.toString(),
				"-autoY", "-c",
				"-height", height,
				"-a" };
		
		args = Misc.joinArrays(args, localizers);
		
		String[] args2 = { "-gp" };
		args2 = Misc.joinArrays(args2, gp);
		String[] args3 = { "-lp" };
		args3 = Misc.joinArrays(args3, lp);
		
		args = Misc.joinArrays(args, args2);
		args = Misc.joinArrays(args, args3);
		
		main(args);
	}
	
	/**
	 * Convenience method for easier use in a special case.
	 * @param projectDir
	 * the project directory
	 * @param outputDir
	 * the main output directory
	 * @param strategy
	 * the strategy to use when encountering ranges of equal rankings
	 * @param height
	 * the plot height
	 * @param localizers
	 * a list of SBFL localizers to consider
	 */
	public static void plotAverageDefects4JProjectIgnoreZeroAndNegativeRankings(
			String projectDir, String outputDir, ParserStrategy strategy,
			String height, String[] localizers) {
		String[] args = { 
				"-i", projectDir,
				"-o", outputDir, "average_ignoreZero_" + strategy.toString(),
				"-all", "-single", "-mfr", //"-hit",
				"-png", "-csv",
				"-strat", strategy.toString(),
				"-autoY", "-c",
				"-height", height,
				"-zero",
				"-a" };
		
		if (localizers != null) {
			args = Misc.joinArrays(args, localizers);
		}
		
		main(args);
	}
	
}
