/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithPaths;
import se.de.hu_berlin.informatik.utils.tm.modules.ExecuteMainClassInNewJVMModule;

/**
 * {@link Callable} object that runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ExperimentRunnerCall extends CallableWithPaths<String, Boolean> {

	/**
	 * The id of the project under consideration.
	 */
	final String project;
	
	String[] localizers;
	
	/**
	 * Initializes a {@link ExperimentRunnerCall} object with the given parameters.
	 * @param project
	 * the id of the project under consideration
	 * @param localizers
	 * the SBFL localizers to use
	 */
	public ExperimentRunnerCall(String project, String[] localizers) {
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
		Prop.loadProperties(project, buggyID, fixedID);
		
		/* #====================================================================================
		 * # checkout and generate SBFL rankings
		 * #==================================================================================== */
		String[] checkoutArgs = {
				"-" + Prop.OPT_PROJECT, project,
				"-" + Prop.OPT_BUG_ID, id,
				"-" + Prop.OPT_LOCALIZERS
		};
		checkoutArgs = Misc.joinArrays(checkoutArgs, localizers);
		int result = new ExecuteMainClassInNewJVMModule(
				"se.de.hu_berlin.informatik.defects4j.frontend.CheckoutAndGenerateSBFLRankings", null,
				"-XX:+UseNUMA")
				.submit(checkoutArgs).getResult();

		if (result != 0) {
			Misc.err("Error while checking out or generating rankings. Skipping project '"
					+ project + "', bug '" + id + "'.");
			return false;
		}
		
//		/* #====================================================================================
//		 * # build a local LM
//		 * #==================================================================================== */
//		String[] localLMArgs = {
//				"-" + Prop.OPT_PROJECT, project,
//				"-" + Prop.OPT_BUG_ID, id
//		};
//		result = new ExecuteMainClassInNewJVMModule(
//				"se.de.hu_berlin.informatik.defects4j.frontend.BuildLocalLMFromSourceFiles", null,
//				"-XX:+UseNUMA")
//				.submit(localLMArgs).getResult();
//
//		if (result != 0) {
//			Misc.err("Error while building local LM. Skipping project '"
//					+ project + "', bug '" + id + "'.");
//			continue;
//		}
		
		/* #====================================================================================
		 * # build a local LM,
		 * # query sentences to the global and local LM via kenLM,
		 * # combine the generated rankings
		 * #==================================================================================== */
		String[] queryCombineArgs = {
				"-" + Prop.OPT_PROJECT, project,
				"-" + Prop.OPT_BUG_ID, id
		};
		result = new ExecuteMainClassInNewJVMModule(
				"se.de.hu_berlin.informatik.defects4j.frontend.QueryAndCombine", null,
				"-XX:+UseNUMA")
				.submit(queryCombineArgs).getResult();

		if (result != 0) {
			Misc.err("Error while querying sentences and/or combining rankings. Skipping project '"
					+ project + "', bug '" + id + "'.");
			return false;
		}
		
		/* #====================================================================================
		 * # evaluate rankings based on changes in the source code files
		 * #==================================================================================== */
		String[] evaluateArgs = {
				"-" + Prop.OPT_PROJECT, project,
				"-" + Prop.OPT_BUG_ID, id
		};
		result = new ExecuteMainClassInNewJVMModule(
				"se.de.hu_berlin.informatik.defects4j.frontend.EvaluateRankings", null,
				"-XX:+UseNUMA")
				.submit(evaluateArgs).getResult();

		if (result != 0) {
			Misc.err("Error while evaluating rankings. Skipping project '"
					+ project + "', bug '" + id + "'.");
			return false;
		}
		
		
		/* #====================================================================================
		 * # delete the buggy version execution directory if archive and execution directory 
		 * # aren't identical... (if an error occurs in the process, no deletion takes place)
		 * #==================================================================================== */
		File executionProjectDir = Paths.get(Prop.projectDir).toFile();
		File archiveProjectDir = Paths.get(Prop.archiveProjectDir).toFile();
		if (!archiveProjectDir.equals(executionProjectDir)) {
			Misc.delete(Paths.get(Prop.executionBuggyWorkDir).toFile());
		}
		return true;
	}

}

