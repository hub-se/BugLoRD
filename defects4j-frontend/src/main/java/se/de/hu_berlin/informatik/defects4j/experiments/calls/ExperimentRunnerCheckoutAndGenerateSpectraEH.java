/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.experiments.calls;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import se.de.hu_berlin.informatik.c2r.Cob2Instr2Coverage2Ranking;
import se.de.hu_berlin.informatik.constants.Defects4JConstants;
import se.de.hu_berlin.informatik.defects4j.frontend.Defects4J;
import se.de.hu_berlin.informatik.defects4j.frontend.Prop;
import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.ADisruptorEventHandlerFactory;
import se.de.hu_berlin.informatik.utils.threaded.EHWithInput;
import se.de.hu_berlin.informatik.utils.threaded.DisruptorFCFSEventHandler;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ExperimentRunnerCheckoutAndGenerateSpectraEH extends EHWithInput<String> {

	public static class Factory extends ADisruptorEventHandlerFactory<String> {

		private final String project;
		
		/**
		 * Initializes a {@link Factory} object with the given parameters.
		 * @param project
		 * the id of the project under consideration
		 */
		public Factory(String project) {
			super(ExperimentRunnerCheckoutAndGenerateSpectraEH.class);
			this.project = project;
		}

		@Override
		public DisruptorFCFSEventHandler<String> newInstance() {
			return new ExperimentRunnerCheckoutAndGenerateSpectraEH(project);
		}
	}
	
	private final String project;
	
	/**
	 * Initializes a {@link ExperimentRunnerCheckoutAndGenerateSpectraEH} object with the given parameters.
	 * @param project
	 * the id of the project under consideration
	 */
	public ExperimentRunnerCheckoutAndGenerateSpectraEH(String project) {
		super();
		this.project = project;
	}

	private boolean tryToGetSpectraFromArchive(Prop prop) {
		File spectra = FileUtils.searchFileContainingPattern(new File(prop.spectraArchiveDir), 
				prop.getProject() + "-" + prop.getBugID() + "b.zip", 1);
		if (spectra == null) {
			return false;
		}
		
		File destination = new File(prop.buggyWorkDir + Prop.SEP + Defects4JConstants.DIR_NAME_RANKING + Prop.SEP + Defects4JConstants.SPECTRA_FILE_NAME);
		try {
			FileUtils.copyFileOrDir(spectra, destination);
		} catch (IOException e) {
			Log.err(this, "Found spectra '%s', but could not copy to '%s'.", spectra, destination);
			return false;
		}
		return true;
	}

	@Override
	public void resetAndInit() {
		//not needed
	}

	@Override
	public boolean processInput(String input) {
		Log.out(this, "Processing project '%s', bug %s.", project, input);
		Defects4J defects4j = new Defects4J(project, input);
		defects4j.switchToExecutionMode();
		
//		//make sure that the current experiment hasn't been run yet
//		Path progressFile = Paths.get(prop.progressFile);
//		try {
//			String progress = Misc.readFile2String(progressFile);
//			if (progress.contains(project + id)) {
//				//experiment in progress or finished
//				return true;
//			} else {
//				//new experiment -> make a new entry in the file
//				Misc.appendString2File(project + id, progressFile.toFile());
//			}
//		} catch (IOException e) {
//			//error while reading or writing file
//			Log.err(this, "Could not read from or write to '%s'.", progressFile);
//		}
		

		/* #====================================================================================
		 * # checkout and generate SBFL spectra
		 * #==================================================================================== */
		//delete existing directory, if existing
		defects4j.tryDeleteExecutionDirectory(true, true);
		
		/* #====================================================================================
		 * # checkout buggy version
		 * #==================================================================================== */
		defects4j.checkoutBug(true);
		
		/* #====================================================================================
		 * # collect bug info
		 * #==================================================================================== */
		String infoOutput = defects4j.getInfo();

		String infoFile = defects4j.getProperties().buggyWorkDir + Prop.SEP + Defects4JConstants.FILENAME_INFO;
		try {
			FileUtils.writeString2File(infoOutput, new File(infoFile));
		} catch (IOException e) {
			Log.err(this, "IOException while trying to write to file '%s'.", infoFile);
			Log.err(this, "Error while checking out or generating rankings. Skipping project '"
					+ project + "', bug '" + input + "'.");
			defects4j.tryDeleteExecutionDirectory(true, false);
			return false;
		}

		/* #====================================================================================
		 * # try to get spectra from archive, if existing
		 * #==================================================================================== */
		boolean foundSpectra = tryToGetSpectraFromArchive(defects4j.getProperties());

		/* #====================================================================================
		 * # if not found a spectra, then run all the tests and build a new one
		 * #==================================================================================== */
		if (!foundSpectra) {
			/* #====================================================================================
			 * # collect paths
			 * #==================================================================================== */
			String buggyMainSrcDir = defects4j.getMainSrcDir(true);

			String srcDirFile = defects4j.getProperties().buggyWorkDir + Prop.SEP + Defects4JConstants.FILENAME_SRCDIR;
			try {
				FileUtils.writeString2File(buggyMainSrcDir, new File(srcDirFile));
			} catch (IOException e1) {
				Log.err(this, "IOException while trying to write to file '%s'.", srcDirFile);
			}

			String buggyMainBinDir = defects4j.getMainBinDir(true);
			String buggyTestBinDir = defects4j.getTestBinDir(true);
			String buggyTestCP = defects4j.getTestCP(true);

			/* #====================================================================================
			 * # compile buggy version
			 * #==================================================================================== */
			defects4j.compile(true);

			/* #====================================================================================
			 * # generate coverage traces via cobertura and calculate rankings
			 * #==================================================================================== */
			String testClasses = defects4j.getTests(true);

			String testClassesFile = defects4j.getProperties().buggyWorkDir + Prop.SEP + Defects4JConstants.FILENAME_TEST_CLASSES;
			try {
				FileUtils.writeString2File(testClasses, new File(testClassesFile));
			} catch (IOException e) {
				Log.err(this, "IOException while trying to write to file '%s'.", testClassesFile);
				Log.err(this, "Error while checking out or generating rankings. Skipping project '"
						+ project + "', bug '" + input + "'.");
				defects4j.tryDeleteExecutionDirectory(true, false);
				return false;
			}


			String rankingDir = defects4j.getProperties().buggyWorkDir + Prop.SEP + Defects4JConstants.DIR_NAME_RANKING;
			Cob2Instr2Coverage2Ranking.generateRankingForDefects4JElement(
					defects4j.getProperties().buggyWorkDir, buggyMainSrcDir, buggyTestBinDir, buggyTestCP, 
					defects4j.getProperties().buggyWorkDir + Prop.SEP + buggyMainBinDir, testClassesFile, 
					rankingDir, null);

			/* #====================================================================================
			 * # clean up unnecessary directories (binary classes)
			 * #==================================================================================== */
			FileUtils.delete(Paths.get(defects4j.getProperties().buggyWorkDir + Prop.SEP + buggyMainBinDir));
			FileUtils.delete(Paths.get(defects4j.getProperties().buggyWorkDir + Prop.SEP + buggyTestBinDir));
		}
		
		/* #====================================================================================
		 * # clean up unnecessary directories (doc files, svn/git files)
		 * #==================================================================================== */
		FileUtils.delete(Paths.get(defects4j.getProperties().buggyWorkDir + Prop.SEP + "doc"));
		FileUtils.delete(Paths.get(defects4j.getProperties().buggyWorkDir + Prop.SEP + ".git"));
		FileUtils.delete(Paths.get(defects4j.getProperties().buggyWorkDir + Prop.SEP + ".svn"));
		
		/* #====================================================================================
		 * # move to archive directory, in case it differs from the execution directory
		 * #==================================================================================== */
		defects4j.tryMovingExecutionDirToArchive(true);

		defects4j.tryDeleteExecutionDirectory(true, false);
		return true;
	}

}

