/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend;

import java.util.concurrent.Callable;

import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithPaths;
import se.de.hu_berlin.informatik.utils.tm.modules.ExecuteMainClassInNewJVMModule;

/**
 * {@link Callable} object that runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ExperimentRunnerComputeSBFLRankingsFromSpectraCall extends CallableWithPaths<String, Boolean> {

	final String project;
	String[] localizers;
	
	/**
	 * Initializes a {@link ExperimentRunnerComputeSBFLRankingsFromSpectraCall} object with the given parameters.
	 * @param project
	 * the id of the project under consideration
	 * @param localizers
	 * the SBFL localizers to use
	 */
	public ExperimentRunnerComputeSBFLRankingsFromSpectraCall(String project, String[] localizers) {
		super();
		this.project = project;
		this.localizers = localizers;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() {
		String id = getInput();
		
		if (!Prop.validateProjectAndBugID(project, Integer.parseInt(id), false)) {
			Misc.err("Combination of project '" + project + "' and bug '" + id + "' "
					+ "is not valid. Skipping...");
			return false;
		}
		String buggyID = id + "b";
		String fixedID = id + "f";
		
		//this is important!!
		Prop prop = new Prop().loadProperties(project, buggyID, fixedID);
		
//		//make sure that the current experiment hasn't been run yet
//		Path progressFile = Paths.get(prop.progressFile);
//		try {
//			String progress = Misc.readFile2String(progressFile);
//			if (progress.contains(project + id)) {
//				//experiment in progress or finished
//				return true;
//			} else {
//				//new experiment -> make a new entry in the file
//				Misc.appendString2File(project + id, progressFile.toFile());
//			}
//		} catch (IOException e) {
//			//error while reading or writing file
//			Misc.err(this, "Could not read from or write to '%s'.", progressFile);
//		}
		
		int result = 0;
		
		/* #====================================================================================
		 * # compute SBFL rankings for the given localizers
		 * #==================================================================================== */
		String[] computeSBFLRankingArgs = {
				"-" + Prop.OPT_PROJECT, project,
				"-" + Prop.OPT_BUG_ID, id,
				"-" + Prop.OPT_LOCALIZERS
		};
		computeSBFLRankingArgs = Misc.joinArrays(computeSBFLRankingArgs, localizers);
		result = new ExecuteMainClassInNewJVMModule(
				"se.de.hu_berlin.informatik.defects4j.frontend.GenerateSBFLRankingsFromSpectra", null,
				"-XX:+UseNUMA")
				.submit(computeSBFLRankingArgs).getResult();

		if (result != 0) {
			Misc.err("Error while computing SBFL rankings. Skipping project '"
					+ project + "', bug '" + id + "'.");
			prop.tryDeletingExecutionDirectory();
			return false;
		}
		
		prop.tryDeletingExecutionDirectory();
		return true;
	}

}

