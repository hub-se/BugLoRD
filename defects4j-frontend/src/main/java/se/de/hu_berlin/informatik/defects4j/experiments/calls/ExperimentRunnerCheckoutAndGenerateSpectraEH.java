/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.experiments.calls;

import java.io.File;
import java.io.IOException;
import se.de.hu_berlin.informatik.c2r.Cob2Instr2Coverage2Ranking;
import se.de.hu_berlin.informatik.constants.Defects4JConstants;
import se.de.hu_berlin.informatik.defects4j.frontend.Defects4JEntity;
import se.de.hu_berlin.informatik.defects4j.frontend.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.defects4j.frontend.BenchmarkEntity;
import se.de.hu_berlin.informatik.defects4j.frontend.Defects4J;
import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturn;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturnFactory;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ExperimentRunnerCheckoutAndGenerateSpectraEH extends EHWithInputAndReturn<BenchmarkEntity,BenchmarkEntity> {

	public static class Factory extends EHWithInputAndReturnFactory<Defects4JEntity,Defects4JEntity> {

		/**
		 * Initializes a {@link Factory} object.
		 */
		public Factory() {
			super(ExperimentRunnerCheckoutAndGenerateSpectraEH.class);
		}

		@Override
		public EHWithInputAndReturn<BenchmarkEntity, BenchmarkEntity> newFreshInstance() {
			return new ExperimentRunnerCheckoutAndGenerateSpectraEH();
		}
	}
	
	/**
	 * Initializes a {@link ExperimentRunnerCheckoutAndGenerateSpectraEH} object.
	 */
	public ExperimentRunnerCheckoutAndGenerateSpectraEH() {
		super();
	}

	private boolean tryToGetSpectraFromArchive(Defects4JEntity entity) {
		File spectra = FileUtils.searchFileContainingPattern(new File(Defects4J.getValueOf(Defects4JProperties.SPECTRA_ARCHIVE_DIR)), 
				entity.getProject() + "-" + entity.getBugId() + "b.zip", 1);
		if (spectra == null) {
			return false;
		}
		
		File destination = new File(entity.getWorkDir() + Defects4J.SEP + Defects4JConstants.DIR_NAME_RANKING + Defects4J.SEP + Defects4JConstants.SPECTRA_FILE_NAME);
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
	public Defects4JEntity processInput(BenchmarkEntity buggyEntity) {
		Log.out(this, "Processing project '%s', bug %s.", buggyEntity.getProject(), buggyEntity.getBugId());
		buggyEntity.switchToExecutionDir();

		/* #====================================================================================
		 * # checkout buggy version and delete possibly existing directory
		 * #==================================================================================== */
		buggyEntity.resetAndInitialize(true);
		
		/* #====================================================================================
		 * # collect bug info
		 * #==================================================================================== */
		String infoOutput = buggyEntity.getInfo();

		String infoFile = buggyEntity.getWorkDir() + Defects4J.SEP + Defects4JConstants.FILENAME_INFO;
		try {
			FileUtils.writeString2File(infoOutput, new File(infoFile));
		} catch (IOException e) {
			Log.err(this, "IOException while trying to write to file '%s'.", infoFile);
			Log.err(this, "Error while checking out or generating rankings. Skipping project '"
					+ buggyEntity.getProject() + "', bug '" + buggyEntity.getBugId() + "'.");
			buggyEntity.tryDeleteExecutionDirectory(false);
			return null;
		}

		/* #====================================================================================
		 * # try to get spectra from archive, if existing
		 * #==================================================================================== */
		boolean foundSpectra = tryToGetSpectraFromArchive(buggyEntity);

		/* #====================================================================================
		 * # if not found a spectra, then run all the tests and build a new one
		 * #==================================================================================== */
		if (!foundSpectra) {
			/* #====================================================================================
			 * # collect paths
			 * #==================================================================================== */
			String buggyMainSrcDir = buggyEntity.getMainSourceDir().toString();

			String srcDirFile = buggyEntity.getWorkDir() + Defects4J.SEP + Defects4JConstants.FILENAME_SRCDIR;
			try {
				FileUtils.writeString2File(buggyMainSrcDir, new File(srcDirFile));
			} catch (IOException e1) {
				Log.err(this, "IOException while trying to write to file '%s'.", srcDirFile);
			}

			String buggyMainBinDir = buggyEntity.getMainBinDir().toString();
			String buggyTestBinDir = buggyEntity.getTestBinDir().toString();
			String buggyTestCP = buggyEntity.getTestClassPath();

			/* #====================================================================================
			 * # compile buggy version
			 * #==================================================================================== */
			buggyEntity.compile();

			/* #====================================================================================
			 * # generate coverage traces via cobertura and calculate rankings
			 * #==================================================================================== */
			String testClasses = Misc.listToString(buggyEntity.getTestClasses(), System.lineSeparator(), "", "");

			String testClassesFile = buggyEntity.getWorkDir() + Defects4J.SEP + Defects4JConstants.FILENAME_TEST_CLASSES;
			try {
				FileUtils.writeString2File(testClasses, new File(testClassesFile));
			} catch (IOException e) {
				Log.err(this, "IOException while trying to write to file '%s'.", testClassesFile);
				Log.err(this, "Error while checking out or generating rankings. Skipping project '"
						+ buggyEntity.getProject() + "', bug '" + buggyEntity.getBugId() + "'.");
				buggyEntity.tryDeleteExecutionDirectory(false);
				return null;
			}


			String rankingDir = buggyEntity.getWorkDir() + Defects4J.SEP + Defects4JConstants.DIR_NAME_RANKING;
			Cob2Instr2Coverage2Ranking.generateRankingForDefects4JElement(
					buggyEntity.getWorkDir().toString(), buggyMainSrcDir, buggyTestBinDir, buggyTestCP, 
					buggyEntity.getWorkDir() + Defects4J.SEP + buggyMainBinDir, testClassesFile, 
					rankingDir, null);

		}
		
		/* #====================================================================================
		 * # clean up unnecessary directories (doc files, svn/git files, binary classes)
		 * #==================================================================================== */
		buggyEntity.removeUnnecessaryFiles();
		
		/* #====================================================================================
		 * # move to archive directory, in case it differs from the execution directory
		 * #==================================================================================== */
		buggyEntity.tryMovingExecutionDirToArchive();

		buggyEntity.tryDeleteExecutionDirectory(false);
		
		return buggyEntity;
	}

}

