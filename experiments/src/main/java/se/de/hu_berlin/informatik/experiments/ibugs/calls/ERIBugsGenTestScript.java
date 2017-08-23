/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.ibugs.calls;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.ibugs.IBugsEntity;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * 
 * @author Roy Lieck
 *
 */
public class ERIBugsGenTestScript extends AbstractProcessor<BuggyFixedEntity<?>,BuggyFixedEntity<?>> {
	
	/**
	 * Initializes a {@link ERIBugsGenTestScript} object.
	 */
	public ERIBugsGenTestScript() {
		super();
	}

	@Override
	public BuggyFixedEntity<?> processItem(BuggyFixedEntity<?> buggyEntity) {
		Log.out(this, "Processing %s.", buggyEntity);
		
		/* #====================================================================================
		 * # this is a special case that may be removed at all because we will have no need for the test scripts
		 * #==================================================================================== */
		IBugsEntity ibeBuggy = (IBugsEntity) buggyEntity.getBuggyVersion();
		ibeBuggy.genTestScript();
		
		IBugsEntity ibeFixed = (IBugsEntity) buggyEntity.getFixedVersion();
		ibeFixed.genTestScript();

		return buggyEntity;
	}

}
