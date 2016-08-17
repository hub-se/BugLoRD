/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import se.de.hu_berlin.informatik.changechecker.ChangeChecker;
import se.de.hu_berlin.informatik.defects4j.frontend.Prop;
import se.de.hu_berlin.informatik.utils.fileoperations.ListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;

/**
 * Checks out a Defects4J fixed project version. Will delete existing
 * versions if executed again.
 * 
 * @author SimHigh
 */
public class CheckoutFixAndCheckForChanges {
	
	private final static String SEP = File.separator;
	
	public final static String PATH_MARK = "#";
	
	/**
	 * Parses the options from the command line.
	 * @param args
	 * the application's arguments
	 * @return
	 * an {@link OptionParser} object that provides access to all parsed options and their values
	 */
	private static OptionParser getOptions(String[] args) {
//		final String tool_usage = "CheckoutAndGenerateSpectra -p project -b bugID"; 
		final String tool_usage = "CheckoutAndGenerateSpectra";
		final OptionParser options = new OptionParser(tool_usage, args);

        options.add(Prop.OPT_PROJECT, "project", true, "A project of the Defects4J benchmark. "
        		+ "Should be either 'Lang', 'Chart', 'Time', 'Closure' or 'Math'.", true);
        options.add(Prop.OPT_BUG_ID, "bugID", true, "A number indicating the id of a buggy project version. "
        		+ "Value ranges differ based on the project.", true);
        
//        options.add("r", "onlyRelevant", false, "Set if only relevant tests shall be executed.");
        
        options.parseCommandLine();
        
        return options;
	}
	
	
	/**
	 * @param args
	 * -p project -b bugID
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
		
		File executionProjectDir = Paths.get(prop.projectDir).toFile();
		executionProjectDir.mkdirs();
		File archiveBuggyVersionDir = Paths.get(prop.archiveBuggyWorkDir).toFile();
		File executionFixedVersionDir = Paths.get(prop.executionFixedWorkDir).toFile();
		
		//delete existing directories, if any
		Misc.delete(Paths.get(prop.executionFixedWorkDir));
		
		String infoFile = prop.archiveBuggyWorkDir + SEP + Prop.FILENAME_INFO;
		
		/* #====================================================================================
		 * # checkout fixed version for comparison purposes
		 * #==================================================================================== */
		prop.executeCommand(executionProjectDir, 
				prop.defects4jExecutable, "checkout", "-p", project, "-v", fixedID, "-w", prop.executionFixedWorkDir);
		
		/* #====================================================================================
		 * # check modifications
		 * #==================================================================================== */
		String modifiedSourcesFile = prop.archiveBuggyWorkDir + SEP + Prop.FILENAME_INFO_MOD_SOURCES;
		
		//TODO is storing this as a file really valuable?
		List<String> modifiedSources = parseInfoFile(infoFile);
		new ListToFileWriterModule<List<String>>(Paths.get(modifiedSourcesFile), true)
		.submit(modifiedSources);
		
		String srcDirFile = prop.archiveBuggyWorkDir + SEP + Prop.FILENAME_SRCDIR;
		String buggyMainSrcDir = null;
		
		try {
			buggyMainSrcDir = Misc.readFile2String(Paths.get(srcDirFile));
		} catch (IOException e) {
			Log.err(CheckoutFixAndCheckForChanges.class, "IOException while trying to read file '%s'.", srcDirFile);
		}
		
		if (buggyMainSrcDir == null) {
			buggyMainSrcDir = prop.executeCommandWithOutput(archiveBuggyVersionDir, false, 
					prop.defects4jExecutable, "export", "-p", "dir.src.classes");

			try {
				Misc.writeString2File(buggyMainSrcDir, new File(srcDirFile));
			} catch (IOException e1) {
				Log.err(CheckoutFixAndCheckForChanges.class, "IOException while trying to write to file '%s'.", srcDirFile);
			}
		}
		
		String fixedMainSrcDir = prop.executeCommandWithOutput(executionFixedVersionDir, false, 
				prop.defects4jExecutable, "export", "-p", "dir.src.classes");
		Log.out(CheckoutFixAndCheckForChanges.class, "main source directory: <" + fixedMainSrcDir + ">");
		
		//iterate over all modified source files
		List<String> result = new ArrayList<>();
		for (String modifiedSourceIdentifier : modifiedSources) {
			String path = modifiedSourceIdentifier.replace('.','/') + ".java";
			result.add(PATH_MARK + path);
			
			//extract the changes
			result.addAll(ChangeChecker.checkForChanges(
					Paths.get(prop.archiveBuggyWorkDir, buggyMainSrcDir, path).toFile(), 
					Paths.get(prop.executionFixedWorkDir, fixedMainSrcDir, path).toFile()));
		}
		
		//save the gathered information about modified lines in a file
		new ListToFileWriterModule<List<String>>(Paths.get(prop.archiveBuggyWorkDir, ".modifiedLines"), true)
		.submit(result);
		
		//delete the fixed version directory, since it's not needed anymore
		Misc.delete(Paths.get(prop.executionFixedWorkDir));
	}
	
	/**
	 * Parses the info file and returns a String which contains all modified
	 * source files with one file per line.
	 * @param infoFile
	 * the path to the info file
	 * @return
	 * modified source files, separated by new lines
	 */
	private static List<String> parseInfoFile(String infoFile) {
		List<String> lines = new ArrayList<>();
		try (BufferedReader bufRead = new BufferedReader(new FileReader(infoFile))) {
			String line = null;
			boolean modifiedSourceLine = false;
			while ((line = bufRead.readLine()) != null) {
				if (line.equals("List of modified sources:")) {
					modifiedSourceLine = true;
					continue;
				}
				if (modifiedSourceLine && line.startsWith(" - ")) {
					lines.add(line.substring(3));
				} else {
					modifiedSourceLine = false;
				}
			}
		} catch (FileNotFoundException e) {
			Log.abort(CheckoutFixAndCheckForChanges.class, "Info file does not exist: '" + infoFile + "'.");
		} catch (IOException e) {
			Log.abort(CheckoutFixAndCheckForChanges.class, "IOException while reading info file: '" + infoFile + "'.");
		}
		
		return lines;
	}
}
