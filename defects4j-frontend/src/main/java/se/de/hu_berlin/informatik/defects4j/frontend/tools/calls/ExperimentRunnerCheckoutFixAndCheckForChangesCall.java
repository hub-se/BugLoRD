/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend.tools.calls;

import java.util.concurrent.Callable;

import se.de.hu_berlin.informatik.defects4j.frontend.Prop;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithPaths;
import se.de.hu_berlin.informatik.utils.tm.modules.ExecuteMainClassInNewJVMModule;

/**
 * {@link Callable} object that runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ExperimentRunnerCheckoutFixAndCheckForChangesCall extends CallableWithPaths<String, Boolean> {

	final String project;
	
	/**
	 * Initializes a {@link ExperimentRunnerCheckoutFixAndCheckForChangesCall} object with the given parameters.
	 * @param project
	 * the id of the project under consideration
	 */
	public ExperimentRunnerCheckoutFixAndCheckForChangesCall(String project) {
		super();
		this.project = project;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() {
		String id = getInput();
		
		if (!Prop.validateProjectAndBugID(project, Integer.parseInt(id), false)) {
			Log.err(this, "Combination of project '" + project + "' and bug '" + id + "' "
					+ "is not valid. Skipping...");
			return false;
		}
		String buggyID = id + "b";
		String fixedID = id + "f";
		
		//this is important!!
		Prop prop = new Prop().loadProperties(project, buggyID, fixedID);
		
		int result = 0;

		/* #====================================================================================
		 * # checkout fixed version and check for changes
		 * #==================================================================================== */
		String[] checkoutArgs = {
				"-" + Prop.OPT_PROJECT, project,
				"-" + Prop.OPT_BUG_ID, id
		};
		result = new ExecuteMainClassInNewJVMModule(
				"se.de.hu_berlin.informatik.defects4j.frontend.tools.CheckoutFixAndCheckForChanges", null,
				"-XX:+UseNUMA")
				.submit(checkoutArgs).getResult();

		if (result != 0) {
			Log.err(this, "Error while checking out or checking for changes. Skipping project '"
					+ project + "', bug '" + id + "'.");
			prop.tryDeletingExecutionDirectory();
			return false;
		}

		prop.tryDeletingExecutionDirectory();
		return true;
	}

}

