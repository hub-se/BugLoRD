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
public class ERIBugsBuildTests extends AbstractProcessor<BuggyFixedEntity<?>,BuggyFixedEntity<?>> {
	
	/**
	 * Initializes a {@link ERIBugsBuildTests} object.
	 */
	public ERIBugsBuildTests() {
		super();
	}

	@Override
	public BuggyFixedEntity<?> processItem(BuggyFixedEntity<?> buggyEntity) {
		Log.out(this, "Processing %s.", buggyEntity);
		
		/* #====================================================================================
		 * # builds the classes that are needed for the tests only
		 * #==================================================================================== */
		IBugsEntity ibeBuggy = (IBugsEntity) buggyEntity.getBuggyVersion();
		ibeBuggy.compileTests( true );
		
		IBugsEntity ibeFixed = (IBugsEntity) buggyEntity.getFixedVersion();
		ibeFixed.compileTests( true );


		return buggyEntity;
	}

}
