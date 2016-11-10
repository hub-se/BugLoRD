/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import java.io.File;
import java.io.IOException;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
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
public class ExperimentRunnerCheckoutAndGenerateSpectraEH extends EHWithInputAndReturn<BuggyFixedEntity,BuggyFixedEntity> {

	public static class Factory extends EHWithInputAndReturnFactory<BuggyFixedEntity,BuggyFixedEntity> {

		/**
		 * Initializes a {@link Factory} object.
		 */
		public Factory() {
			super(ExperimentRunnerCheckoutAndGenerateSpectraEH.class);
		}

		@Override
		public EHWithInputAndReturn<BuggyFixedEntity, BuggyFixedEntity> newFreshInstance() {
			return new ExperimentRunnerCheckoutAndGenerateSpectraEH();
		}
	}
	
	/**
	 * Initializes a {@link ExperimentRunnerCheckoutAndGenerateSpectraEH} object.
	 */
	public ExperimentRunnerCheckoutAndGenerateSpectraEH() {
		super();
	}

	private boolean tryToGetSpectraFromArchive(BuggyFixedEntity entity) {
		File spectra = FileUtils.searchFileContainingPattern(new File(Defects4J.getValueOf(Defects4JProperties.SPECTRA_ARCHIVE_DIR)), 
				entity.getUniqueIdentifier() + ".zip", 1);
		if (spectra == null) {
			return false;
		}
		
		File destination = new File(entity.getWorkDataDir() + Defects4J.SEP + BugLoRDConstants.DIR_NAME_RANKING + Defects4J.SEP + BugLoRDConstants.SPECTRA_FILE_NAME);
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
	public BuggyFixedEntity processInput(BuggyFixedEntity buggyEntity) {
		Log.out(this, "Processing %s.", buggyEntity);
		
		buggyEntity.deleteAll();

		/* #====================================================================================
		 * # checkout buggy version and delete possibly existing directory
		 * #==================================================================================== */
		buggyEntity.resetAndInitialize(true, true);

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
			String buggyMainSrcDir = buggyEntity.getMainSourceDir(true).toString();
			String buggyMainBinDir = buggyEntity.getMainBinDir(true).toString();
			String buggyTestBinDir = buggyEntity.getTestBinDir(true).toString();
			String buggyTestCP = buggyEntity.getTestClassPath(true);

			/* #====================================================================================
			 * # compile buggy version
			 * #==================================================================================== */
			buggyEntity.compile(true);

			/* #====================================================================================
			 * # generate coverage traces via cobertura and calculate rankings
			 * #==================================================================================== */
			String testClasses = Misc.listToString(buggyEntity.getTestClasses(true), System.lineSeparator(), "", "");

			String testClassesFile = buggyEntity.getWorkDataDir().resolve(BugLoRDConstants.FILENAME_TEST_CLASSES).toString();
			try {
				FileUtils.writeString2File(testClasses, new File(testClassesFile));
			} catch (IOException e) {
				Log.err(this, "IOException while trying to write to file '%s'.", testClassesFile);
				Log.err(this, "Error while checking out or generating rankings. Skipping '"
						+ buggyEntity + "'.");
				buggyEntity.tryDeleteExecutionDirectory(false);
				return null;
			}


			String rankingDir = buggyEntity.getWorkDataDir().resolve(BugLoRDConstants.DIR_NAME_RANKING).toString();
			Cob2Instr2Coverage2Ranking.generateRankingForDefects4JElement(
					buggyEntity.getWorkDir(true).toString(), buggyMainSrcDir, buggyTestBinDir, buggyTestCP, 
					buggyEntity.getWorkDir(true).resolve(buggyMainBinDir).toString(), testClassesFile, 
					rankingDir, null);

		}
		
		/* #====================================================================================
		 * # clean up unnecessary directories (doc files, svn/git files, binary classes)
		 * #==================================================================================== */
		buggyEntity.removeUnnecessaryFiles(true);
		
//		/* #====================================================================================
//		 * # move to archive directory, in case it differs from the execution directory
//		 * #==================================================================================== */
//		buggyEntity.tryMovingExecutionDirToArchive();
//
//		buggyEntity.tryDeleteExecutionDirectory(false);
		
		return buggyEntity;
	}

}

