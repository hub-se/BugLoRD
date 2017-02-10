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
public class ERCheckoutBugAndFixEH extends EHWithInputAndReturnMethodProvider<BuggyFixedEntity,BuggyFixedEntity> {

	@Override
	public BuggyFixedEntity processInput(BuggyFixedEntity buggyEntity,
			EHWithInputAndReturn<BuggyFixedEntity, BuggyFixedEntity> executingHandler) {
		Log.out(this, "Processing %s.", buggyEntity);

		/* #====================================================================================
		 * # checkout buggy version and fixed version
		 * #==================================================================================== */
		buggyEntity.requireBug(true);
		buggyEntity.requireFix(true);


		/* #====================================================================================
		 * # clean up unnecessary directories (doc files, svn/git files, binary classes)
		 * #==================================================================================== */
		buggyEntity.getBuggyVersion().removeUnnecessaryFiles(true);
		buggyEntity.getFixedVersion().removeUnnecessaryFiles(true);

		return buggyEntity;
	}

}

