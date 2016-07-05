/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.modules.ExecuteMainClassInNewJVMModule;


/**
 * 
 * @author Simon Heiden
 */
public class ExperimentRunner {

	/**
	 * Parses the options from the command line.
	 * @param args
	 * the application's arguments
	 * @return
	 * an {@link OptionParser} object that provides access to all parsed options and their values
	 */
	private static OptionParser getOptions(String[] args) { 
		final String tool_usage = "ExperimentRunner";
		final OptionParser options = new OptionParser(tool_usage, args);

        options.add(Option.builder(Prop.OPT_PROJECT).longOpt("projects").required().hasArgs()
        		.desc("A list of projects to consider of the Defects4J benchmark. "
        		+ "Should be either 'Lang', 'Chart', 'Time', 'Closure' or 'Math'.").build());
        options.add(Option.builder(Prop.OPT_BUG_ID).longOpt("bugIDs").required().hasArgs()
        		.desc("A list of numbers indicating the ids of buggy project versions to consider. "
        		+ "Value ranges differ based on the project. Set this to 'all' to "
        		+ "iterate over all bugs in a project.").build());
        
//        options.add("r", "onlyRelevant", false, "Set if only relevant tests shall be executed.");
		
        options.add(Option.builder(Prop.OPT_LOCALIZERS).longOpt("localizers").optionalArg(true).hasArgs()
        		.desc("A list of identifiers of Cobertura localizers (e.g. 'Tarantula', 'Jaccard', ...).")
				.build());
        
        options.parseCommandLine();
        
        return options;
	}

	/**
	 * @param args
	 * command line arguments
	 */
	public static void main(String[] args) {
		
		OptionParser options = getOptions(args);	
		
		String[] projects = options.getOptionValues(Prop.OPT_PROJECT);
		String[] ids = options.getOptionValues(Prop.OPT_BUG_ID);
		boolean all = ids[0].equals("all");

		//iterate over all projects
		for (String project : projects) {
			if (all) {
				ids = getAllBugIDs(project); 
			}
			for (String id : ids) {
				if (!Prop.validateProjectAndBugID(project, Integer.parseInt(id), false)) {
					Misc.err("Combination of project '" + project + "' and bug '" + id + "' "
							+ "is not valid. Skipping...");
					continue;
				}
				
				//checkout and calculate rankings
				String[] checkoutArgs = {
						"-" + Prop.OPT_PROJECT, project,
						"-" + Prop.OPT_BUG_ID, id,
						"-" + Prop.OPT_LOCALIZERS
				};
				checkoutArgs = Misc.joinArrays(checkoutArgs, options.getOptionValues(Prop.OPT_LOCALIZERS));
				int result = new ExecuteMainClassInNewJVMModule(
						"se.de.hu_berlin.informatik.defects4j.frontend.CheckoutAndGenerateSBFLRankings", null,
						"-XX:+UseNUMA")
						.submit(checkoutArgs).getResult();

				if (result != 0) {
					Misc.err("Error while checking out or generating rankings. Skipping project '"
							+ project + "', bug '" + id + "'.");
					continue;
				}
				
				//evaluate rankings based on changes in the source code files
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
					continue;
				}
			}
		}

	}
	
	private static String[] getAllBugIDs(String project) {
		int maxID = 0;
		switch (project) {
		case "Lang":
			maxID = 65;			
			break;
		case "Math":
			maxID = 106;
			break;
		case "Chart":
			maxID = 26;
			break;
		case "Time":
			maxID = 27;
			break;
		case "Closure":
			maxID = 133;
			break;
		default:
			maxID = 0;
			break;	
		}
		String[] result = new String[maxID];
		for (int i = 0; i < maxID; ++i) {
			result[i] = String.valueOf(i + 1);
		}
		return result;
	}
	
}
