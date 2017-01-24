/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturn;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturnFactory;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ERCheckoutEH extends EHWithInputAndReturn<BuggyFixedEntity,BuggyFixedEntity> {

	public static class Factory extends EHWithInputAndReturnFactory<BuggyFixedEntity,BuggyFixedEntity> {

		/**
		 * Initializes a {@link Factory} object.
		 */
		public Factory() {
			super(ERCheckoutEH.class);
		}

		@Override
		public EHWithInputAndReturn<BuggyFixedEntity, BuggyFixedEntity> newFreshInstance() {
			return new ERCheckoutEH();
		}
	}
	
	/**
	 * Initializes a {@link ERCheckoutEH} object.
	 */
	public ERCheckoutEH() {
		super();
	}

	@Override
	public void resetAndInit() {
		//not needed
	}

	@Override
	public BuggyFixedEntity processInput(BuggyFixedEntity buggyEntity) {
		Log.out(this, "Processing %s.", buggyEntity);
		
		Entity bug = buggyEntity.getBuggyVersion();

		/* #====================================================================================
		 * # checkout buggy version and delete possibly existing directory
		 * #==================================================================================== */
		bug.deleteAll();
		buggyEntity.requireBug(true);
		
		/* #====================================================================================
		 * # clean up unnecessary directories (doc files, svn/git files, binary classes)
		 * #==================================================================================== */
		bug.removeUnnecessaryFiles(true);
		
		return buggyEntity;
	}

}

