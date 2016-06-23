/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.c2r.Cob2Instr2Coverage2Ranking;
import se.de.hu_berlin.informatik.changechecker.ChangeChecker;
import se.de.hu_berlin.informatik.utils.fileoperations.FileLineProcessorModule;
import se.de.hu_berlin.informatik.utils.fileoperations.SearchForFilesOrDirsModule;
import se.de.hu_berlin.informatik.utils.fileoperations.StringListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.modules.stringprocessor.StringsToListProcessor;

/**
 * Checks out a Defects4J buggy project version. Will delete existing
 * versions if executed again.
 * 
 * @author SimHigh
 */
public class EvaluateRankings {
	
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
        
//        options.add("r", "onlyRelevant", false, "Set if only relevant tests shall be executed.");
		
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
		String fixedID = id + "f";
		
		//this is important!!
		Prop.loadProperties(project, buggyID, fixedID);
			
		/* #====================================================================================
		 * # evaluate ranking files based on changed lines
		 * #==================================================================================== */
		String modifiedLinesFile = Prop.buggyWorkDir + SEP + ".modifiedLines";
		
		List<String> lines = new FileLineProcessorModule<List<String>>(new StringsToListProcessor())
				.submit(Paths.get(modifiedLinesFile))
				.getResultFromCollectedItems();
		
		String rankingDir = Prop.buggyWorkDir + SEP + "ranking";
		List<Path> rankingFiles = new SearchForFilesOrDirsModule("**/*{rnk}", false, true, true)
				.submit(Paths.get(rankingDir)).getResult();
		
		//iterate over all ranking files
		for (Path rankingFile : rankingFiles) {
			String path = null;
			//iterate over all modified source files and modified lines
			Iterator<String> i = lines.listIterator();
			while (i.hasNext()) {
				String element = i.next();

				if (element.endsWith(".java")) {
					path = element;
					continue;
				}

				//format: 0          1            2             3                4
				// | start_line | end_line | entity type | change type | significance level |
				String[] attributes = element.split(ChangeChecker.SEPARATION_CHAR);
				assert attributes.length == 5;

				//do something
			}
		}
		
	}
	
}
