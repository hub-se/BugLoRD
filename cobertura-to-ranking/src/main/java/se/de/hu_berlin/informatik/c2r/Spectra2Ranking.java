/**
 * 
 */
package se.de.hu_berlin.informatik.c2r;

import java.nio.file.Path;
import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.c2r.modules.RankingModule;
import se.de.hu_berlin.informatik.c2r.modules.ReadSpectraModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.ModuleLinker;


/**
 * Computes SBFL rankings or hit traces from an existing spectra file 
 * with the support of the stardust API.
 * 
 * @author Simon Heiden
 */
public class Spectra2Ranking {

	/**
	 * Parses the options from the command line.
	 * @param args
	 * the application's arguments
	 * @return
	 * an {@link OptionParser} object that provides access to all parsed options and their values
	 */
	private static OptionParser getOptions(String[] args) {
//		final String tool_usage = "Spectra2Ranking -i input-file [-l loc1 loc2 ...] -o output"; 
		final String tool_usage = "Spectra2Ranking";
		final OptionParser options = new OptionParser(tool_usage, args);

		options.add("i", "input", true, "A compressed spectra file.", true);
		options.add(Option.builder("o").longOpt("output").hasArg().required()
				.desc("Path to output directory.").build());       

		options.add(Option.builder("l").longOpt("localizers")
				.hasArgs().desc("A list of identifiers of Cobertura localizers (e.g. 'Tarantula', 'Jaccard', ...).")
				.build());

		options.parseCommandLine();

		return options;
	}

	/**
	 * @param args
	 * -i input-file [-l loc1 loc2 ...] -o output
	 */
	public static void main(String[] args) {

		OptionParser options = getOptions(args);

		Path spectraFile = options.isFile('i', true);
		String outputDir = options.isDirectory('o', false).toString();

		if (spectraFile.toFile().isDirectory()) {
			Misc.abort(Spectra2Ranking.class, "Input has to be a file.");
		}
		String[] localizers = null;
		if ((localizers = options.getOptionValues('l')) == null) {
			Misc.abort(Spectra2Ranking.class, "No localizers given.");
		}
		new ModuleLinker().link(
				new ReadSpectraModule(),
				new RankingModule(outputDir, localizers))
		.submit(spectraFile);
		
	}

	/**
	 * Convenience method for easier use in a special case.
	 * @param spectraFile
	 * a compressed spectra file
	 * @param rankingDir
	 * path to the main ranking directory
	 * @param localizers
	 * an array of String representation of fault localizers
	 * as used by STARDUST
	 */
	public static void generateRankingForDefects4JElement(
			String spectraFile, String rankingDir, String[] localizers) {
		String[] args = { 
				"-i", spectraFile,
				"-o", rankingDir,
				"-l"};
		args = Misc.joinArrays(args, localizers);
		
		main(args);
	}

}
