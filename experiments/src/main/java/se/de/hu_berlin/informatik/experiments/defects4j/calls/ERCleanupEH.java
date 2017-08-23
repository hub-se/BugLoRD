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
public class ERCleanupEH extends AbstractProcessor<BuggyFixedEntity<?>,BuggyFixedEntity<?>> {

	@Override
	public BuggyFixedEntity<?> processItem(BuggyFixedEntity<?> buggyEntity) {
		Log.out(this, "Processing %s.", buggyEntity);
		
		/* #====================================================================================
		 * # delete everything but the data directory
		 * #==================================================================================== */
		buggyEntity.getBuggyVersion().deleteAllButData();
		buggyEntity.getFixedVersion().deleteAllButData();
		
		return buggyEntity;
	}

}

