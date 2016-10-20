/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.experiments.calls;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedBenchmarkEntity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.changechecker.ChangeChecker;
import se.de.hu_berlin.informatik.constants.Defects4JConstants;
import se.de.hu_berlin.informatik.utils.fileoperations.ListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturn;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturnFactory;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ExperimentRunnerCheckoutFixAndCheckForChangesEH extends EHWithInputAndReturn<BuggyFixedBenchmarkEntity,BuggyFixedBenchmarkEntity> {
	
	public static class Factory extends EHWithInputAndReturnFactory<BuggyFixedBenchmarkEntity,BuggyFixedBenchmarkEntity> {

		/**
		 * Initializes a {@link Factory} object.
		 */
		public Factory() {
			super(ExperimentRunnerCheckoutFixAndCheckForChangesEH.class);
		}

		@Override
		public EHWithInputAndReturn<BuggyFixedBenchmarkEntity, BuggyFixedBenchmarkEntity> newFreshInstance() {
			return new ExperimentRunnerCheckoutFixAndCheckForChangesEH();
		}
	}

	/**
	 * Initializes a {@link ExperimentRunnerCheckoutFixAndCheckForChangesEH} object.
	 */
	public ExperimentRunnerCheckoutFixAndCheckForChangesEH() {
		super();
	}
	

	@Override
	public void resetAndInit() {
		//not needed
	}

	@Override
	public BuggyFixedBenchmarkEntity processInput(BuggyFixedBenchmarkEntity buggyEntity) {
		Log.out(this, "Processing %s.", buggyEntity);
		buggyEntity.switchToArchiveDir();

		/* #====================================================================================
		 * # prepare checking modifications from buggy to fixed entity
		 * #==================================================================================== */
		String archiveBuggyWorkDir = buggyEntity.getWorkDir().toString();
		String modifiedSourcesFile = buggyEntity.getWorkDir() + Defects4J.SEP + Defects4JConstants.FILENAME_INFO_MOD_SOURCES;
		
		List<String> modifiedSources = buggyEntity.getModifiedSources();
		new ListToFileWriterModule<List<String>>(Paths.get(modifiedSourcesFile), true)
		.submit(modifiedSources);
		
		String buggyMainSrcDir = buggyEntity.getMainSourceDir().toString();
		
		/* #====================================================================================
		 * # checkout fixed version for comparison purposes
		 * #==================================================================================== */
		BuggyFixedBenchmarkEntity fixedEntity = buggyEntity.getFixedVersion();
		fixedEntity.switchToExecutionDir();
		String executionFixedWorkDir = fixedEntity.getWorkDir().toString();
		fixedEntity.resetAndInitialize(true);

		String fixedMainSrcDir = fixedEntity.getMainSourceDir().toString();

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
		fixedEntity.deleteAll();
		
		return buggyEntity;
	}

}

