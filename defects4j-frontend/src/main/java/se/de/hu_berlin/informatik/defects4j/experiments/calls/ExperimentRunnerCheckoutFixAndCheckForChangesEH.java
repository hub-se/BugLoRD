/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.experiments.calls;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import se.de.hu_berlin.informatik.changechecker.ChangeChecker;
import se.de.hu_berlin.informatik.constants.Defects4JConstants;
import se.de.hu_berlin.informatik.defects4j.frontend.Defects4J;
import se.de.hu_berlin.informatik.defects4j.frontend.Prop;
import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.fileoperations.ListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.threaded.ADisruptorEventHandlerFactory;
import se.de.hu_berlin.informatik.utils.threaded.EHWithInput;
import se.de.hu_berlin.informatik.utils.threaded.DisruptorFCFSEventHandler;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ExperimentRunnerCheckoutFixAndCheckForChangesEH extends EHWithInput<String> {
	
	public static class Factory extends ADisruptorEventHandlerFactory<String> {

		private final String project;
		
		/**
		 * Initializes a {@link Factory} object with the given parameters.
		 * @param project
		 * the id of the project under consideration
		 */
		public Factory(String project) {
			super(ExperimentRunnerCheckoutFixAndCheckForChangesEH.class);
			this.project = project;
		}

		@Override
		public DisruptorFCFSEventHandler<String> newInstance() {
			return new ExperimentRunnerCheckoutFixAndCheckForChangesEH(project);
		}
	}

	private final String project;
	
	/**
	 * Initializes a {@link ExperimentRunnerCheckoutFixAndCheckForChangesEH} object with the given parameters.
	 * @param project
	 * the id of the project under consideration
	 */
	public ExperimentRunnerCheckoutFixAndCheckForChangesEH(String project) {
		super();
		this.project = project;
	}
	
	/**
	 * Parses the info file and returns a String which contains all modified
	 * source files with one file per line.
	 * @param infoFile
	 * the path to the info file
	 * @return
	 * modified source files, separated by new lines
	 */
	private List<String> parseInfoFile(String infoFile) {
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
			Log.abort(this, "Info file does not exist: '" + infoFile + "'.");
		} catch (IOException e) {
			Log.abort(this, "IOException while reading info file: '" + infoFile + "'.");
		}
		
		return lines;
	}

	@Override
	public void resetAndInit() {
		//not needed
	}

	@Override
	public boolean processInput(String input) {
		Log.out(this, "Processing project '%s', bug %s.", project, input);
		Defects4J defects4j = new Defects4J(project, input);
		defects4j.switchToArchiveMode();

		/* #====================================================================================
		 * # checkout fixed version and check for changes
		 * #==================================================================================== */

		String infoFile = defects4j.getProperties().buggyWorkDir + Prop.SEP + Defects4JConstants.FILENAME_INFO;
		
		/* #====================================================================================
		 * # prepare checking modifications
		 * #==================================================================================== */
		String archiveBuggyWorkDir = defects4j.getProperties().buggyWorkDir;
		String modifiedSourcesFile = defects4j.getProperties().buggyWorkDir + Prop.SEP + Defects4JConstants.FILENAME_INFO_MOD_SOURCES;
		
		List<String> modifiedSources = parseInfoFile(infoFile);
		new ListToFileWriterModule<List<String>>(Paths.get(modifiedSourcesFile), true)
		.submit(modifiedSources);
		
		String srcDirFile = defects4j.getProperties().buggyWorkDir + Prop.SEP + Defects4JConstants.FILENAME_SRCDIR;
		String buggyMainSrcDir = null;
		
		try {
			buggyMainSrcDir = Misc.replaceNewLinesInString(FileUtils.readFile2String(Paths.get(srcDirFile)), "");
		} catch (IOException e) {
			Log.err(this, "IOException while trying to read file '%s'.", srcDirFile);
		}
		
		if (buggyMainSrcDir == null) {
			buggyMainSrcDir = defects4j.getMainSrcDir(true);

			try {
				FileUtils.writeString2File(buggyMainSrcDir, new File(srcDirFile));
			} catch (IOException e1) {
				Log.err(this, "IOException while trying to write to file '%s'.", srcDirFile);
			}
		}
		
		/* #====================================================================================
		 * # checkout fixed version for comparison purposes
		 * #==================================================================================== */
		defects4j.switchToExecutionMode();
		//delete existing fixed version directory, if existing
		defects4j.tryDeleteExecutionDirectory(false, true);
		String executionFixedWorkDir = defects4j.getProperties().fixedWorkDir;
		defects4j.checkoutBug(false);

		String fixedMainSrcDir = defects4j.getMainSrcDir(false);

		/* #====================================================================================
		 * # check modifications
		 * #==================================================================================== */
		//iterate over all modified source files
		List<String> result = new ArrayList<>();
		for (String modifiedSourceIdentifier : modifiedSources) {
			String path = modifiedSourceIdentifier.replace('.','/') + ".java";
			result.add(Defects4JConstants.PATH_MARK + path);
			
			//extract the changes
			result.addAll(ChangeChecker.checkForChanges(
					Paths.get(archiveBuggyWorkDir, buggyMainSrcDir, path).toFile(), 
					Paths.get(executionFixedWorkDir, fixedMainSrcDir, path).toFile()));
		}
		
		//save the gathered information about modified lines in a file
		new ListToFileWriterModule<List<String>>(Paths.get(archiveBuggyWorkDir, Defects4JConstants.FILENAME_MOD_LINES), true)
		.submit(result);
		
		//delete the fixed version directory, since it's not needed anymore
		defects4j.tryDeleteExecutionDirectory(false, true);
		
		return true;
	}

}

