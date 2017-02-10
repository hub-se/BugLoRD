/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturn;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturnMethodProvider;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ERCheckoutEH extends EHWithInputAndReturnMethodProvider<BuggyFixedEntity,BuggyFixedEntity> {

	@Override
	public BuggyFixedEntity processInput(BuggyFixedEntity buggyEntity,
			EHWithInputAndReturn<BuggyFixedEntity, BuggyFixedEntity> executingHandler) {
		Log.out(this, "Processing %s.", buggyEntity);
		
		Entity bug = buggyEntity.getBuggyVersion();

		/* #====================================================================================
		 * # checkout buggy version and delete possibly existing directory
		 * #==================================================================================== */
		bug.deleteAllButData();
		buggyEntity.requireBug(true);
		
		/* #====================================================================================
		 * # clean up unnecessary directories (doc files, svn/git files, binary classes)
		 * #==================================================================================== */
		bug.removeUnnecessaryFiles(true);
		
		return buggyEntity;
	}

}

