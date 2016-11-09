/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturn;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturnFactory;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ExperimentRunnerCheckoutFixAndCheckForChangesEH extends EHWithInputAndReturn<BuggyFixedEntity,BuggyFixedEntity> {
	
	public static class Factory extends EHWithInputAndReturnFactory<BuggyFixedEntity,BuggyFixedEntity> {

		/**
		 * Initializes a {@link Factory} object.
		 */
		public Factory() {
			super(ExperimentRunnerCheckoutFixAndCheckForChangesEH.class);
		}

		@Override
		public EHWithInputAndReturn<BuggyFixedEntity, BuggyFixedEntity> newFreshInstance() {
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
	public BuggyFixedEntity processInput(BuggyFixedEntity buggyEntity) {
		Log.out(this, "Processing %s.", buggyEntity);
		
		boolean bugExisted = true;
		if (!buggyEntity.getWorkDir(true).toFile().exists()) {
			bugExisted = false;
			buggyEntity.resetAndInitialize(true, true);
		}
		
		buggyEntity.saveAllChangesToFile(true, false, false, true, true, true);
		
		if (!bugExisted) {
			buggyEntity.deleteAllButData();
		}
		
		return buggyEntity;
	}

}

