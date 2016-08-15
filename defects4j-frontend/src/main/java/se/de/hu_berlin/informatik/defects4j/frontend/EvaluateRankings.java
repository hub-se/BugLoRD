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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.changechecker.ChangeChecker;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.utils.fileoperations.FileLineProcessorModule;
import se.de.hu_berlin.informatik.utils.fileoperations.SearchForFilesOrDirsModule;
import se.de.hu_berlin.informatik.utils.fileoperations.ListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.modules.stringprocessor.StringsToListProcessor;

/**
 * Evaluates computed rankings based on changed statements.
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
//		final String tool_usage = "EvaluateRankings -p project -b bugID [-l loc1 loc2 ...]"; 
		final String tool_usage = "EvaluateRankings";
		final OptionParser options = new OptionParser(tool_usage, args);

        options.add(Prop.OPT_PROJECT, "project", true, "A project of the Defects4J benchmark. "
        		+ "Should be either 'Lang', 'Chart', 'Time', 'Closure' or 'Math'.", true);
        options.add(Prop.OPT_BUG_ID, "bugID", true, "A number indicating the id of a buggy project version. "
        		+ "Value ranges differ based on the project.", true);
        
        options.parseCommandLine();
        
        return options;
	}
	
	
	/**
	 * @param args
	 * -p project -b bugID [-l loc1 loc2 ...]
	 */
	public static void main(String[] args) {
		
		OptionParser options = getOptions(args);	
		
		String project = options.getOptionValue(Prop.OPT_PROJECT);
		String id = options.getOptionValue(Prop.OPT_BUG_ID);
		int parsedID = Integer.parseInt(id);
		
		Prop.validateProjectAndBugID(project, parsedID, true);
		
		String buggyID = id + "b";
		String fixedID = id + "f";
		
		//this is important!!
		Prop prop = new Prop().loadProperties(project, buggyID, fixedID);
		
		if (!Paths.get(prop.archiveBuggyWorkDir).toFile().exists()) {
			Log.abort(EvaluateRankings.class, 
					"Archive buggy project version directory doesn't exist: '" + prop.archiveBuggyWorkDir + "'.");
		}
			
		/* #====================================================================================
		 * # evaluate ranking files based on changed lines
		 * #==================================================================================== */
		String modifiedLinesFile = prop.archiveBuggyWorkDir + SEP + ".modifiedLines";
		
		List<String> lines = new FileLineProcessorModule<List<String>>(new StringsToListProcessor())
				.submit(Paths.get(modifiedLinesFile))
				.getResultFromCollectedItems();
		
		//store the change information in a map for efficiency
		//source file path identifiers are linked to all changes in the respective file
		Map<String, List<ChangeWrapper>> changeInformation = new HashMap<>();
		try {
			List<ChangeWrapper> currentElement = null;
			//iterate over all modified source files and modified lines
			Iterator<String> i = lines.listIterator();
			while (i.hasNext()) {
				String element = i.next();

				//if an entry starts with the specific marking String, then it
				//is a path identifier and a new map entry is created
				if (element.startsWith(CheckoutAndGenerateSpectra.PATH_MARK)) {
					currentElement = new ArrayList<>();
					changeInformation.put(
							element.substring(CheckoutAndGenerateSpectra.PATH_MARK.length()), 
							currentElement);
					continue;
				}

				//format: 0          1            2             3                4
				// | start_line | end_line | entity type | change type | significance level |
				String[] attributes = element.split(ChangeChecker.SEPARATION_CHAR);
				assert attributes.length == 5;

				//ignore change in case of comment related changes
				if (attributes[3].startsWith("COMMENT")) {
					continue;
				}
				
				//add to the list of changes
				currentElement.add(new ChangeWrapper(
						Integer.parseInt(attributes[0]), Integer.parseInt(attributes[1]),
						attributes[2], attributes[3], attributes[4], 0));
			}
		} catch (NullPointerException e) {
			Log.abort(EvaluateRankings.class, 
					"Null pointer exception thrown. Probably due to the file '" + modifiedLinesFile 
					+ "' not starting with a path identifier. (Has to begin with the sub string '"
					+ CheckoutAndGenerateSpectra.PATH_MARK + "'.)");
		} catch (AssertionError e) {
			Log.abort(EvaluateRankings.class, 
					"Processed line is in wrong format. Maybe due to containing "
					+ "an additional separation char '" + ChangeChecker.SEPARATION_CHAR + "'.\n"
					+ e.getMessage());
		}
		
		String rankingDir = prop.archiveBuggyWorkDir + SEP + "ranking";
		List<Path> rankingFiles = new SearchForFilesOrDirsModule("**/*{rnk}", false, true, true)
				.submit(Paths.get(rankingDir)).getResult();
		
		//iterate over all ranking files
		for (Path rankingFile : rankingFiles) {
			List<String> result = parseRankingFile(rankingFile.toString(), changeInformation);
			
			new ListToFileWriterModule<List<String>>(
					rankingFile.getParent().resolve(rankingFile.getFileName() + ".modlines"), true)
			.submit(result);
		}
		
	}
	
	/**
	 * Parses a ranking file and returns a list of Strings that connect lines of the ranking
	 * file with respective changes. Format of the lines is:
	 * <p>line_numer:start_line:end_line:entity_type:change_type:significance_level
	 * @param rankingFile
	 * path to a ranking file as a String
	 * @param changeInformation
	 * a mapping that maps path identifiers to change information
	 * @return
	 * a list of Strings, connecting lines of the ranking file with their respective changes
	 */
	private static List<String> parseRankingFile(String rankingFile, Map<String, List<ChangeWrapper>> changeInformation) {
		List<String> lines = new ArrayList<>();
		try (BufferedReader bufRead = new BufferedReader(new FileReader(rankingFile))) {
			String line = null;
			int lineCounter = 0;
			while ((line = bufRead.readLine()) != null) {
				++lineCounter;
				//format: path:line_number: ranking
				String[] ranking = line.split(":");
				assert ranking.length == 3;
				
				if (changeInformation.containsKey(ranking[0])) {
					int lineNumber = Integer.parseInt(ranking[1]);
					List<ChangeWrapper> changes = changeInformation.get(ranking[0]);
					
					for (ChangeWrapper entry : changes) {
						//is the ranked line inside of a changed statement?
						if (lineNumber >= entry.getStart() && lineNumber <= entry.getEnd()) {
							lines.add(lineCounter + ChangeChecker.SEPARATION_CHAR
									+ ranking[2].substring(1) + ChangeChecker.SEPARATION_CHAR
									+ entry.toString());
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			Log.abort(EvaluateRankings.class, 
					"Ranking file does not exist: '" + rankingFile + "'.");
		} catch (IOException e) {
			Log.abort(EvaluateRankings.class, 
					"IOException while reading ranking file: '" + rankingFile + "'.");
		}
		
		return lines;
	}
	
}
