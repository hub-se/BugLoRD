/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend.tools.calls;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import se.de.hu_berlin.informatik.c2r.Cob2Instr2Coverage2Ranking;
import se.de.hu_berlin.informatik.defects4j.frontend.Defects4J;
import se.de.hu_berlin.informatik.defects4j.frontend.Prop;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithPaths;

/**
 * {@link Callable} object that runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ExperimentRunnerCheckoutAndGenerateSpectraCall extends CallableWithPaths<String, Boolean> {

	private final String project;
	
	/**
	 * Initializes a {@link ExperimentRunnerCheckoutAndGenerateSpectraCall} object with the given parameters.
	 * @param project
	 * the id of the project under consideration
	 */
	public ExperimentRunnerCheckoutAndGenerateSpectraCall(String project) {
		super();
		this.project = project;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() {
		String id = getInput();
		
		Defects4J defects4j = new Defects4J(project, id);
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

		String infoFile = defects4j.getProperties().buggyWorkDir + Prop.SEP + Prop.FILENAME_INFO;
		try {
			Misc.writeString2File(infoOutput, new File(infoFile));
		} catch (IOException e) {
			Log.err(this, "IOException while trying to write to file '%s'.", infoFile);
			Log.err(this, "Error while checking out or generating rankings. Skipping project '"
					+ project + "', bug '" + id + "'.");
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

			String srcDirFile = defects4j.getProperties().buggyWorkDir + Prop.SEP + Prop.FILENAME_SRCDIR;
			try {
				Misc.writeString2File(buggyMainSrcDir, new File(srcDirFile));
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

			String testClassesFile = defects4j.getProperties().buggyWorkDir + Prop.SEP + Prop.FILENAME_TEST_CLASSES;
			try {
				Misc.writeString2File(testClasses, new File(testClassesFile));
			} catch (IOException e) {
				Log.err(this, "IOException while trying to write to file '%s'.", testClassesFile);
				Log.err(this, "Error while checking out or generating rankings. Skipping project '"
						+ project + "', bug '" + id + "'.");
				defects4j.tryDeleteExecutionDirectory(true, false);
				return false;
			}


			String rankingDir = defects4j.getProperties().buggyWorkDir + Prop.SEP + "ranking";
			Cob2Instr2Coverage2Ranking.generateRankingForDefects4JElement(
					defects4j.getProperties().buggyWorkDir, buggyMainSrcDir, buggyTestBinDir, buggyTestCP, 
					defects4j.getProperties().buggyWorkDir + Prop.SEP + buggyMainBinDir, testClassesFile, 
					rankingDir, null);

			/* #====================================================================================
			 * # clean up unnecessary directories (binary classes)
			 * #==================================================================================== */
			Misc.delete(Paths.get(defects4j.getProperties().buggyWorkDir + Prop.SEP + buggyMainBinDir));
			Misc.delete(Paths.get(defects4j.getProperties().buggyWorkDir + Prop.SEP + buggyTestBinDir));
		}
		
		/* #====================================================================================
		 * # clean up unnecessary directories (doc files, svn/git files)
		 * #==================================================================================== */
		Misc.delete(Paths.get(defects4j.getProperties().buggyWorkDir + Prop.SEP + "doc"));
		Misc.delete(Paths.get(defects4j.getProperties().buggyWorkDir + Prop.SEP + ".git"));
		Misc.delete(Paths.get(defects4j.getProperties().buggyWorkDir + Prop.SEP + ".svn"));
		
		/* #====================================================================================
		 * # move to archive directory, in case it differs from the execution directory
		 * #==================================================================================== */
		defects4j.tryMovingExecutionDirToArchive(true);

		defects4j.tryDeleteExecutionDirectory(true, false);
		return true;
	}

	private boolean tryToGetSpectraFromArchive(Prop prop) {
		File spectra = Misc.searchFileContainingPattern(new File(prop.spectraArchiveDir), 
				prop.getProject() + "-" + prop.getBugID() + "b.zip", 1);
		if (spectra == null) {
			return false;
		}
		
		File destination = new File(prop.buggyWorkDir + Prop.SEP + "ranking" + Prop.SEP + "spectraCompressed.zip");
		try {
			Misc.copyFileOrDir(spectra, destination);
		} catch (IOException e) {
			Log.err(this, "Found spectra '%s', but could not copy to '%s'.", spectra, destination);
			return false;
		}
		return true;
	}

	

}

