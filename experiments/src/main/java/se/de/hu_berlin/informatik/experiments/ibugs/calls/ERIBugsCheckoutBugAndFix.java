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
public class ERIBugsCheckoutBugAndFix extends AbstractProcessor<BuggyFixedEntity<?>,BuggyFixedEntity<?>> {

	/**
	 * Initializes a {@link ERIBugsCheckoutBugAndFix} object.
	 */
	public ERIBugsCheckoutBugAndFix() {
		super();
	}

	@Override
	public BuggyFixedEntity<?> processItem(BuggyFixedEntity<?> buggyEntity) {
		Log.out(this, "Processing %s.", buggyEntity);
		
		/* #====================================================================================
		 * # checkout buggy version and fixed version
		 * #==================================================================================== */
		IBugsEntity ibeBuggy = (IBugsEntity) buggyEntity.getBuggyVersion();
		ibeBuggy.checkoutRepository();
		
		IBugsEntity ibeFixed = (IBugsEntity) buggyEntity.getFixedVersion();
		ibeFixed.checkoutRepository();

		
		return buggyEntity;
	}

}
