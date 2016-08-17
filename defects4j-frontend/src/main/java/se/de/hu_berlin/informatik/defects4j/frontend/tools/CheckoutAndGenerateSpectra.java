/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import se.de.hu_berlin.informatik.c2r.Cob2Instr2Coverage2Ranking;
import se.de.hu_berlin.informatik.defects4j.frontend.Prop;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;

/**
 * Checks out a Defects4J buggy project version. Will delete existing
 * versions if executed again.
 * 
 * @author SimHigh
 */
public class CheckoutAndGenerateSpectra {
	
	private final static String SEP = File.separator;
	
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
		
		File executionProjectDir = Paths.get(prop.projectDir).toFile();
		executionProjectDir.mkdirs();
		File executionBuggyVersionDir = Paths.get(prop.executionBuggyWorkDir).toFile();
		
		//delete existing directories, if any
		Misc.delete(Paths.get(prop.executionBuggyWorkDir));
		
		/* #====================================================================================
		 * # checkout buggy version
		 * #==================================================================================== */
		prop.executeCommand(executionProjectDir, 
				prop.defects4jExecutable, "checkout", "-p", project, "-v", buggyID, "-w", prop.executionBuggyWorkDir);
		
		/* #====================================================================================
		 * # collect bug info
		 * #==================================================================================== */
		String infoFile = prop.executionBuggyWorkDir + SEP + ".info";
		
		String processOutput = prop.executeCommandWithOutput(executionBuggyVersionDir, false, 
				prop.defects4jExecutable, "info", "-p", project, "-b", id);
		try {
			Misc.writeString2File(processOutput, Paths.get(infoFile).toFile());
		} catch (IOException e) {
			Log.abort(CheckoutAndGenerateSpectra.class, "IOException while trying to write to file '" + infoFile + "'.");
		}
		
		String buggyMainSrcDir = prop.executeCommandWithOutput(executionBuggyVersionDir, false, 
				prop.defects4jExecutable, "export", "-p", "dir.src.classes");
		Log.out(CheckoutAndGenerateSpectra.class, "main source directory: <" + buggyMainSrcDir + ">");
		String buggyMainBinDir = prop.executeCommandWithOutput(executionBuggyVersionDir, false, 
				prop.defects4jExecutable, "export", "-p", "dir.bin.classes");
		Log.out(CheckoutAndGenerateSpectra.class, "main binary directory: <" + buggyMainBinDir + ">");
		String buggyTestBinDir = prop.executeCommandWithOutput(executionBuggyVersionDir, false,
				prop.defects4jExecutable, "export", "-p", "dir.bin.tests");
		Log.out(CheckoutAndGenerateSpectra.class, "test binary directory: <" + buggyTestBinDir + ">");
		
		String buggyTestCP = prop.executeCommandWithOutput(executionBuggyVersionDir, false, 
				prop.defects4jExecutable, "export", "-p", "cp.test");
		Log.out(CheckoutAndGenerateSpectra.class, "test class path: <" + buggyTestCP + ">");
		
		/* #====================================================================================
		 * # compile buggy version
		 * #==================================================================================== */
		if (!Paths.get(prop.executionBuggyWorkDir + SEP + ".defects4j.config").toFile().exists()) {
			Log.abort(CheckoutAndGenerateSpectra.class, "Defects4J config file doesn't exist.");
		}
		prop.executeCommand(executionBuggyVersionDir, prop.defects4jExecutable, "compile");
		
		/* #====================================================================================
		 * # generate coverage traces via cobertura and calculate rankings
		 * #==================================================================================== */
		String testClassesFile = prop.executionBuggyWorkDir + SEP + "test_classes.txt";
		if (prop.relevant) {
			prop.executeCommand(executionBuggyVersionDir, 
					prop.defects4jExecutable, "export", "-p", "tests.relevant", "-o", testClassesFile);
		} else {
			prop.executeCommand(executionBuggyVersionDir, 
					prop.defects4jExecutable, "export", "-p", "tests.all", "-o", testClassesFile);
		}
		
		String rankingDir = prop.executionBuggyWorkDir + SEP + "ranking";
		Cob2Instr2Coverage2Ranking.generateRankingForDefects4JElement(
				prop.executionBuggyWorkDir, buggyMainSrcDir, buggyTestBinDir, buggyTestCP, 
				prop.executionBuggyWorkDir + SEP + buggyMainBinDir, testClassesFile, 
				rankingDir, null);
		
		/* #====================================================================================
		 * # clean up unnecessary directories (binary classes, doc files, svn/git files)
		 * #==================================================================================== */
		Misc.delete(Paths.get(prop.executionBuggyWorkDir + SEP + buggyMainBinDir));
		Misc.delete(Paths.get(prop.executionBuggyWorkDir + SEP + buggyTestBinDir));
		Misc.delete(Paths.get(prop.executionBuggyWorkDir + SEP + "doc"));
		Misc.delete(Paths.get(prop.executionBuggyWorkDir + SEP + ".git"));
		Misc.delete(Paths.get(prop.executionBuggyWorkDir + SEP + ".svn"));
		
		/* #====================================================================================
		 * # move to archive directory, in case it differs from the execution directory
		 * #==================================================================================== */
		File archiveProjectDir = Paths.get(prop.archiveProjectDir).toFile();
		if (!archiveProjectDir.equals(executionProjectDir)) {
			File archiveBuggyVersionDir = Paths.get(prop.archiveProjectDir + SEP + buggyID).toFile();
			Misc.delete(archiveBuggyVersionDir);
			try {
				Misc.copyFileOrDir(executionBuggyVersionDir, archiveBuggyVersionDir);
			} catch (IOException e) {
				Log.abort(CheckoutAndGenerateSpectra.class, "IOException while trying to copy directory '%s' to '%s'.",
						executionBuggyVersionDir, archiveBuggyVersionDir);
			}
			Misc.delete(executionBuggyVersionDir);
		}
	}
	
}
