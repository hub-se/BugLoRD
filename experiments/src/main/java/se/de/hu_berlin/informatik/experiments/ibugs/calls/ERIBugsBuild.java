/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.ibugs.calls;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.ibugs.IBugsEntity;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturn;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturnFactory;

/**
 * 
 * @author Roy Lieck
 *
 */
public class ERIBugsBuild extends EHWithInputAndReturn<BuggyFixedEntity,BuggyFixedEntity> {

	public static class Factory extends EHWithInputAndReturnFactory<BuggyFixedEntity,BuggyFixedEntity> {

		/**
		 * Initializes a {@link Factory} object.
		 */
		public Factory() {
			super(ERIBugsBuild.class);
		}

		// event handler
		@Override
		public EHWithInputAndReturn<BuggyFixedEntity, BuggyFixedEntity> newFreshInstance() {
			return new ERIBugsBuild();
		}
	}
	
	/**
	 * Initializes a {@link ERIBugsBuild} object.
	 */
	public ERIBugsBuild() {
		super();
	}

	@Override
	public void resetAndInit() {
		//not needed
	}

	@Override
	public BuggyFixedEntity processInput(BuggyFixedEntity buggyEntity) {
		Log.out(this, "Processing %s.", buggyEntity);
		
		/* #====================================================================================
		 * # Builds the repository. Currently builds the tests too.
		 * # Because of issues with the building a clean is performed and the build is done twice.
		 * # This is the suggested work around from the official mailing list.
		 * #==================================================================================== */
		IBugsEntity ibeBuggy = (IBugsEntity) buggyEntity.getBuggyVersion();
		ibeBuggy.clean();
		ibeBuggy.compile( true );
		ibeBuggy.compile( true );
		// currently we also build the test files because there is no reason to not do it
		ibeBuggy.compileTests( true );
		ibeBuggy.compileTests( true );
		
		IBugsEntity ibeFixed = (IBugsEntity) buggyEntity.getFixedVersion();
		ibeFixed.clean();
		ibeFixed.compile( true );
		ibeFixed.compile( true );
		// currently we also build the test files because there is no reason to not do it
		ibeFixed.compileTests( true );
		ibeFixed.compileTests( true );

		return buggyEntity;
	}

}
