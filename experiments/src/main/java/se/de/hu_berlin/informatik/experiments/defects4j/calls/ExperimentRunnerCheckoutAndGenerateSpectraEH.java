/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import java.io.File;
import java.io.IOException;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedBenchmarkEntity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JConstants;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.c2r.Cob2Instr2Coverage2Ranking;
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
public class ExperimentRunnerCheckoutAndGenerateSpectraEH extends EHWithInputAndReturn<BuggyFixedBenchmarkEntity,BuggyFixedBenchmarkEntity> {

	public static class Factory extends EHWithInputAndReturnFactory<BuggyFixedBenchmarkEntity,BuggyFixedBenchmarkEntity> {

		/**
		 * Initializes a {@link Factory} object.
		 */
		public Factory() {
			super(ExperimentRunnerCheckoutAndGenerateSpectraEH.class);
		}

		@Override
		public EHWithInputAndReturn<BuggyFixedBenchmarkEntity, BuggyFixedBenchmarkEntity> newFreshInstance() {
			return new ExperimentRunnerCheckoutAndGenerateSpectraEH();
		}
	}
	
	/**
	 * Initializes a {@link ExperimentRunnerCheckoutAndGenerateSpectraEH} object.
	 */
	public ExperimentRunnerCheckoutAndGenerateSpectraEH() {
		super();
	}

	private boolean tryToGetSpectraFromArchive(BuggyFixedBenchmarkEntity entity) {
		File spectra = FileUtils.searchFileContainingPattern(new File(Defects4J.getValueOf(Defects4JProperties.SPECTRA_ARCHIVE_DIR)), 
				entity.getUniqueIdentifier() + ".zip", 1);
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
	public BuggyFixedBenchmarkEntity processInput(BuggyFixedBenchmarkEntity buggyEntity) {
		Log.out(this, "Processing %s.", buggyEntity);
		buggyEntity.switchToExecutionDir();

		/* #====================================================================================
		 * # checkout buggy version and delete possibly existing directory
		 * #==================================================================================== */
		buggyEntity.resetAndInitialize(true);

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
				Log.err(this, "Error while checking out or generating rankings. Skipping '"
						+ buggyEntity + "'.");
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

