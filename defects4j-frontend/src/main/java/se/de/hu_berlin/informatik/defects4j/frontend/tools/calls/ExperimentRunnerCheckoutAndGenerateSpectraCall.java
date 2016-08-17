/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend.tools.calls;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import se.de.hu_berlin.informatik.defects4j.frontend.Prop;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithPaths;
import se.de.hu_berlin.informatik.utils.tm.modules.ExecuteMainClassInNewJVMModule;

/**
 * {@link Callable} object that runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ExperimentRunnerCheckoutAndGenerateSpectraCall extends CallableWithPaths<String, Boolean> {

	final String project;
	
	/**
	 * Initializes a {@link ExperimentRunnerCheckoutAndGenerateSpectraCall} object with the given parameters.
	 * @param project
	 * the id of the project under consideration
	 */
	public ExperimentRunnerCheckoutAndGenerateSpectraCall(String project) {
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
		
		//make sure that the current experiment hasn't been run yet
		Path progressFile = Paths.get(prop.progressFile);
		try {
			String progress = Misc.readFile2String(progressFile);
			if (progress.contains(project + id)) {
				//experiment in progress or finished
				return true;
			} else {
				//new experiment -> make a new entry in the file
				Misc.appendString2File(project + id, progressFile.toFile());
			}
		} catch (IOException e) {
			//error while reading or writing file
			Log.err(this, "Could not read from or write to '%s'.", progressFile);
		}
		
		int result = 0;
		

		/* #====================================================================================
		 * # checkout and generate SBFL spectra
		 * #==================================================================================== */
		String[] checkoutArgs = {
				"-" + Prop.OPT_PROJECT, project,
				"-" + Prop.OPT_BUG_ID, id
		};
		result = new ExecuteMainClassInNewJVMModule(
				"se.de.hu_berlin.informatik.defects4j.frontend.tools.CheckoutAndGenerateSpectra", null,
				"-XX:+UseNUMA")
				.submit(checkoutArgs).getResult();

		if (result != 0) {
			Log.err(this, "Error while checking out or generating rankings. Skipping project '"
					+ project + "', bug '" + id + "'.");
			prop.tryDeletingExecutionDirectory();
			return false;
		}

		prop.tryDeletingExecutionDirectory();
		return true;
	}

}

