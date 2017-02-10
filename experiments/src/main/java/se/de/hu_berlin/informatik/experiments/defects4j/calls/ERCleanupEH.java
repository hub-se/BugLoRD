/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturn;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturnMethodProvider;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ERCleanupEH extends EHWithInputAndReturnMethodProvider<BuggyFixedEntity,BuggyFixedEntity> {

	@Override
	public BuggyFixedEntity processInput(BuggyFixedEntity buggyEntity,
			EHWithInputAndReturn<BuggyFixedEntity, BuggyFixedEntity> executingHandler) {
		Log.out(this, "Processing %s.", buggyEntity);
		
		/* #====================================================================================
		 * # delete everything but the data directory
		 * #==================================================================================== */
		buggyEntity.getBuggyVersion().deleteAllButData();
		buggyEntity.getFixedVersion().deleteAllButData();
		
		return buggyEntity;
	}

}

