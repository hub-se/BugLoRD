/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.c2r.Spectra2Ranking;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;

/**
 * Generates SBFL rankings from existing spectra files. Overwrites
 * existing ranking files.
 * 
 * @author SimHigh
 */
public class GenerateSBFLRankingsFromSpectra {
	
	private final static String SEP = File.separator;
	
	/**
	 * Parses the options from the command line.
	 * @param args
	 * the application's arguments
	 * @return
	 * an {@link OptionParser} object that provides access to all parsed options and their values
	 */
	private static OptionParser getOptions(String[] args) {
//		final String tool_usage = "Defects4JStarter -p project -b bugID [-l loc1 loc2 ...]"; 
		final String tool_usage = "Defects4JStarter";
		final OptionParser options = new OptionParser(tool_usage, args);

        options.add("p", "project", true, "A project of the Defects4J benchmark. "
        		+ "Should be either 'Lang', 'Chart', 'Time', 'Closure' or 'Math'.", true);
        options.add("b", "bugID", true, "A number indicating the id of a buggy project version. "
        		+ "Value ranges differ based on the project.", true);
		
        options.add(Option.builder("l").longOpt("localizers").optionalArg(true).hasArgs()
        		.desc("A list of identifiers of Cobertura localizers (e.g. 'Tarantula', 'Jaccard', ...).")
				.build());
        
        options.parseCommandLine();
        
        return options;
	}
	
	
	/**
	 * @param args
	 * -p project -b bugID [-l loc1 loc2 ...]
	 */
	public static void main(String[] args) {
		
		OptionParser options = getOptions(args);	
		
		String project = options.getOptionValue('p');
		String id = options.getOptionValue('b');
		int parsedID = Integer.parseInt(id);
		
		Prop.validateProjectAndBugID(project, parsedID);
		
		String buggyID = id + "b";
		
		//this is important!!
		Prop.loadProperties(project, buggyID);

		/* #====================================================================================
		 * # calculate rankings from existing spectra file
		 * #==================================================================================== */
		String rankingDir = Prop.workDir + SEP + "ranking";
		String compressedSpectraFile = rankingDir + SEP + "spectraCompressed.zip";
		if (!Paths.get(compressedSpectraFile).toFile().exists()) {
			Misc.abort("Spectra file doesn't exist: '" + compressedSpectraFile + "'.");
		}
		String[] localizers = options.getOptionValues('l');
		Spectra2Ranking.generateRankingForDefects4JElement(compressedSpectraFile, rankingDir, localizers);
		
	}
	
}
