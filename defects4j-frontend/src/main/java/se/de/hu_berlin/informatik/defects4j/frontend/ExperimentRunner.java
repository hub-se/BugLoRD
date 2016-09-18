/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend;

import java.util.Arrays;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.defects4j.frontend.tools.calls.ExperimentRunnerCheckoutAndGenerateSpectraCall;
import se.de.hu_berlin.informatik.defects4j.frontend.tools.calls.ExperimentRunnerCheckoutFixAndCheckForChangesCall;
import se.de.hu_berlin.informatik.defects4j.frontend.tools.calls.ExperimentRunnerComputeSBFLRankingsFromSpectraCall;
import se.de.hu_berlin.informatik.defects4j.frontend.tools.calls.ExperimentRunnerQueryAndCombineRankingsCall;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.modules.ThreadedListProcessorModule;


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
						+ "Should be either 'Lang', 'Chart', 'Time', 'Closure' or 'Math'. Set this to 'all' to "
						+ "iterate over all projects.").build());
		options.add(Option.builder(Prop.OPT_BUG_ID).longOpt("bugIDs").required().hasArgs()
				.desc("A list of numbers indicating the ids of buggy project versions to consider. "
						+ "Value ranges differ based on the project. Set this to 'all' to "
						+ "iterate over all bugs in a project.").build());

//        options.add("r", "onlyRelevant", false, "Set if only relevant tests shall be executed.");
        
		final Option thread_opt = new Option("t", "threads", true, "Number of threads to run "
				+ "experiments in parallel. (Default is 1.)");
		thread_opt.setOptionalArg(true);
		thread_opt.setType(Integer.class);
		options.add(thread_opt);
		
        options.add(Option.builder("e").longOpt("execute").hasArgs().required()
        		.desc("A list of all experiments to execute. ('checkout', 'checkChanges', 'computeSBFL', "
        				+ "'query' or 'all')").build());
        
        options.add(Prop.OPT_LM, "globalLM", true, "Path to a language model binary (kenLM).", false);
        
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
		
		String[] toDo = options.getOptionValues("e");
		boolean all = ids[0].equals("all");
		
		int threadCount = 1;
		if (options.hasOption('t')) {
			//parse number of threads
			threadCount = Integer.parseInt(options.getOptionValue('t', "1"));
		}

		if (projects[0].equals("all")) {
			projects = Prop.getAllProjects();
		}
		
		
		if (toDoContains(toDo, "checkout") || toDoContains(toDo, "all")) {
			//iterate over all projects
			for (String project : projects) {
				if (all) {
					ids = Prop.getAllBugIDs(project); 
				}

				new ThreadedListProcessorModule<String>(threadCount, 
						new ExperimentRunnerCheckoutAndGenerateSpectraCall.Factory(project))
				.submit(Arrays.asList(ids));
			}
		}

		if (toDoContains(toDo, "checkChanges") || toDoContains(toDo, "all")) {
			//iterate over all projects
			for (String project : projects) {
				if (all) {
					ids = Prop.getAllBugIDs(project); 
				}

				new ThreadedListProcessorModule<String>(threadCount, 
						new ExperimentRunnerCheckoutFixAndCheckForChangesCall.Factory(project))
				.submit(Arrays.asList(ids));
			}
		}
			
		if (toDoContains(toDo, "computeSBFL") || toDoContains(toDo, "all")) {
			//iterate over all projects
			for (String project : projects) {
				if (all) {
					ids = Prop.getAllBugIDs(project); 
				}

				new ThreadedListProcessorModule<String>(threadCount, 
						new ExperimentRunnerComputeSBFLRankingsFromSpectraCall.Factory(project))
				.submit(Arrays.asList(ids));
			}
		}

		if (toDoContains(toDo, "query") || toDoContains(toDo, "all")) {
			String globalLM = options.getOptionValue(Prop.OPT_LM, null);
			
//			if (globalLM != null && !(new File(globalLM)).exists()) {
//				Log.abort(ExperimentRunner.class, "Given global LM doesn't exist: '" + globalLM + "'.");
//			}
			
			//iterate over all projects
			for (String project : projects) {
				if (all) {
					ids = Prop.getAllBugIDs(project); 
				}

				new ThreadedListProcessorModule<String>(threadCount, 
						new ExperimentRunnerQueryAndCombineRankingsCall.Factory(project, globalLM))
				.submit(Arrays.asList(ids));
			}
		}
		
	}
	
	private static boolean toDoContains(String[] toDo, String item) {
		for (String element : toDo) {
			if (element.toLowerCase().equals(item.toLowerCase()))
				return true;
		}
		return false;
	}
	
}
