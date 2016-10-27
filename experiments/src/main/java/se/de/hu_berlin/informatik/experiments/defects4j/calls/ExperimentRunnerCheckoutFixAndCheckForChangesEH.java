/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedBenchmarkEntity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JConstants;
import se.de.hu_berlin.informatik.changechecker.ChangeChecker;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
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
		
		List<String> modifiedClasses = buggyEntity.getModifiedClasses();
		new ListToFileWriterModule<List<String>>(Paths.get(modifiedSourcesFile), true)
		.submit(modifiedClasses);
		
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
		 * # check modifications and save to hard drive
		 * #==================================================================================== */
		Map<String, List<ChangeWrapper>> changeMap = new HashMap<>();
		//iterate over all modified source files
		for (String modifiedSourceIdentifier : modifiedClasses) {
			String path = modifiedSourceIdentifier.replace('.','/') + ".java";

			//extract the changes
			changeMap.put(modifiedSourceIdentifier, 
					ChangeChecker.checkForChanges(
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

