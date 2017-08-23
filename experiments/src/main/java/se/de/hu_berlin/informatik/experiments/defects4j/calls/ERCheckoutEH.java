/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ERCheckoutEH extends AbstractProcessor<BuggyFixedEntity<?>,BuggyFixedEntity<?>> {

	@Override
	public BuggyFixedEntity<?> processItem(BuggyFixedEntity<?> buggyEntity) {
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

