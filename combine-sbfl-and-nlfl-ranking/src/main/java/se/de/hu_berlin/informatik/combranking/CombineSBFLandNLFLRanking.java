/**
 * 
 */

package se.de.hu_berlin.informatik.combranking;

import java.nio.file.Path;
import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.combranking.modules.ParseRankingsModule;
import se.de.hu_berlin.informatik.combranking.modules.SaveRankingsModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.ModuleLinker;


/**
 * Combines SBFL and NLFL rankings with user specified percentage values.
 * 
 * @author Simon Heiden
 */
public class CombineSBFLandNLFLRanking {
	
	/**
	 * Parses the options from the command line.
	 * @param args
	 * the application's arguments
	 * @return
	 * an {@link OptionParser} object that provides access to all parsed options and their values
	 */
	private static OptionParser getOptions(String[] args) {
//		final String tool_usage = "CombineSBFLandNLFLRanking -i SBFL-ranking-file -t rel_path:line-file -g NLFL-ranking-file -o output-dir [-l local-NLFL-ranking-file] [-gp #1 #2 ...] [-lp #1 #2 ...]"; 
		final String tool_usage = "CombineSBFLandNLFLRanking";
		final OptionParser options = new OptionParser(tool_usage, args);
		
		options.add("i", "input", true, "Path to SBFL-ranking-file with format: 'relative/path/To/File:line#: ranking#'.", true);
		options.add("o", "outputDir", true, "Path to output directory.", true);
		options.add("t", "traceFile", true, "Path to trace file with format: 'relative/path/To/File:line#'.", true);
		options.add("g", "globalRanking", true, "Path to NLFL ranking file computed from global LM with format: 'globalRanking#'.", true);
		options.add("l", "localRanking", true, "Path to NLFL ranking file computed from local LM with format: 'localRanking#'.");
		
		options.add(Option.builder("gp").longOpt("globalPercentages")
        		.hasArgs().desc("Global Percentages (with multiple arguments).")
        		.build());
		options.add(Option.builder("lp").longOpt("localPercentages")
        		.hasArgs().desc("Local Percentages (with multiple arguments).")
        		.build());
        
        options.parseCommandLine();
        
        return options;
	}
	
	/**
	 * @param args
	 * -i SBFL-ranking-file -t rel_path:line-file -g NLFL-ranking-file -o output-dir [-l local-NLFL-ranking-file] [-gp #1 #2 ...] [-lp #1 #2 ...]
	 */
	public static void main(String[] args) {
        
		OptionParser options = getOptions(args);
		
		//file with SBFL ranking 				(format: relative/path/To/File:line#: ranking#)
        Path SBFLFile = options.isFile('i', true);
		//file with file names and line numbers (format: relative/path/To/File:line#)
		Path lineFile = options.isFile('t', true);
		//file with global NLFL ranking			(format: ranking#)
		Path rankingFile = options.isFile('g', true);
		//file with local NLFL ranking			(format: ranking#)
		Path localRankingFile = null;
		if (options.hasOption('l')) {
			localRankingFile = options.isFile('l', true);
		}
		
		//path to output directory
		final Path outputDir = options.isDirectory('o', false);
		
		new ModuleLinker().link(
				new ParseRankingsModule(SBFLFile, lineFile, rankingFile, localRankingFile), 
				new SaveRankingsModule(lineFile, outputDir, options.hasOption('l'), 
						options.getOptionValues("gp"), options.getOptionValues("lp")))
		.start();
		
	}

	/**
	 * Convenience method for easier use in a special case.
	 * @param inputSBFLFile
	 * the input SBFL ranking file
	 * @param inputTraceFile
	 * a file with file names and line numbers that were executed
	 * (may be the same as the SBFL ranking file)
	 * @param inputGlobalNLFL
	 * the input file that contains the global NLFL rankings
	 * @param inputLocalNLFL
	 * the input file that contains the local NLFL rankings
	 * @param mainOutputDir
	 * main output directory
	 * @param globalPercentages
	 * global percentages to consider
	 * @param localPercentages
	 * local percentage to consider
	 */
	public static void combineSBFLandNLFLRankingsForDefects4JElement(
			String inputSBFLFile, String inputTraceFile, String inputGlobalNLFL, String inputLocalNLFL,
			String mainOutputDir, String[] globalPercentages, String[] localPercentages) {
		String[] args = { 
				"-i", inputSBFLFile,
				"-t", inputTraceFile,
				"-g", inputGlobalNLFL,
				"-o", mainOutputDir,
				"-gp"};
		
		args = Misc.joinArrays(args, globalPercentages);
		
		if (inputLocalNLFL != null) {
			args = Misc.addToArrayAndReturnResult(args, "-l", inputLocalNLFL, "-lp");
			args = Misc.joinArrays(args, localPercentages);
		}
		
		main(args);
	}
	
}
