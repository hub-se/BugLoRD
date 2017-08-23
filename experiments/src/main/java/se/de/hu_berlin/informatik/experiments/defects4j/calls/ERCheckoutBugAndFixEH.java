/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ERCheckoutBugAndFixEH extends AbstractProcessor<BuggyFixedEntity<?>,BuggyFixedEntity<?>> {

	@Override
	public BuggyFixedEntity<?> processItem(BuggyFixedEntity<?> buggyEntity) {
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

