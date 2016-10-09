/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.experiments;

import java.util.Arrays;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.defects4j.experiments.calls.ExperimentRunnerCheckoutAndGenerateSpectraEH;
import se.de.hu_berlin.informatik.defects4j.experiments.calls.ExperimentRunnerCheckoutFixAndCheckForChangesEH;
import se.de.hu_berlin.informatik.defects4j.experiments.calls.ExperimentRunnerComputeSBFLRankingsFromSpectraEH;
import se.de.hu_berlin.informatik.defects4j.experiments.calls.ExperimentRunnerQueryAndCombineRankingsEH;
import se.de.hu_berlin.informatik.defects4j.frontend.Prop;
import se.de.hu_berlin.informatik.utils.optionparser.IOptions;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.modules.ThreadedListProcessorModule;


/**
 * 
 * @author Simon Heiden
 */
public class ExperimentRunner {

	public static enum CmdOptions implements IOptions {
		/* add options here according to your needs */
		PROJECTS(Option.builder(Prop.OPT_PROJECT).longOpt("projects").required().hasArgs()
				.desc("A list of projects to consider of the Defects4J benchmark. "
						+ "Should be either 'Lang', 'Chart', 'Time', 'Closure' or 'Math'. Set this to 'all' to "
						+ "iterate over all projects.").build()),
		BUG_IDS(Option.builder(Prop.OPT_BUG_ID).longOpt("bugIDs").required().hasArgs()
				.desc("A list of numbers indicating the ids of buggy project versions to consider. "
						+ "Value ranges differ based on the project. Set this to 'all' to "
						+ "iterate over all bugs in a project.").build()),
        EXECUTE(Option.builder("e").longOpt("execute").hasArgs().required()
        		.desc("A list of all experiments to execute. ('checkout', 'checkChanges', 'computeSBFL', "
        				+ "'query' or 'all')").build()),
        LM(Prop.OPT_LM, "globalLM", true, "Path to a language model binary (kenLM).", false);

		/* the following code blocks should not need to be changed */
		final private Option option;
		final private int groupId;

		//adds an option that is not part of any group
		CmdOptions(final String opt, final String longOpt, final boolean hasArg, final String description, final boolean required) {
			this.option = Option.builder(opt).longOpt(longOpt).required(required).hasArg(hasArg).desc(description).build();
			this.groupId = NO_GROUP;
		}
		
		//adds an option that is part of the group with the specified index (positive integer)
		//a negative index means that this option is part of no group
		//this option will not be required, however, the group itself will be
		CmdOptions(final String opt, final String longOpt, final boolean hasArg, final String description, int groupId) {
			this.option = Option.builder(opt).longOpt(longOpt).required(false).hasArg(hasArg).desc(description).build();
			this.groupId = groupId;
		}
		
		//adds the given option that will be part of the group with the given id
		CmdOptions(Option option, int groupId) {
			this.option = option;
			this.groupId = groupId;
		}
		
		//adds the given option that will be part of no group
		CmdOptions(Option option) {
			this(option, NO_GROUP);
		}
		
		@Override public Option option() { return option; }
		@Override public int groupId() { return groupId; }
		@Override public String toString() { return option.getOpt(); }
	}

	/**
	 * @param args
	 * command line arguments
	 */
	public static void main(String[] args) {
		
		OptionParser options = OptionParser.getOptions("ExperimentRunner", true, CmdOptions.class, args);
		
		String[] projects = options.getOptionValues(CmdOptions.PROJECTS);
		String[] ids = options.getOptionValues(CmdOptions.BUG_IDS);
		
		String[] toDo = options.getOptionValues(CmdOptions.EXECUTE);
		boolean all = ids[0].equals("all");
		
		int threadCount = options.getNumberOfThreads();

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
						new ExperimentRunnerCheckoutAndGenerateSpectraEH.Factory(project))
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
						new ExperimentRunnerCheckoutFixAndCheckForChangesEH.Factory(project))
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
						new ExperimentRunnerComputeSBFLRankingsFromSpectraEH.Factory(project))
				.submit(Arrays.asList(ids));
			}
		}

		if (toDoContains(toDo, "query") || toDoContains(toDo, "all")) {
			String globalLM = options.getOptionValue(CmdOptions.LM, null);
			
//			if (globalLM != null && !(new File(globalLM)).exists()) {
//				Log.abort(ExperimentRunner.class, "Given global LM doesn't exist: '" + globalLM + "'.");
//			}
			
			//iterate over all projects
			for (String project : projects) {
				if (all) {
					ids = Prop.getAllBugIDs(project); 
				}

				new ThreadedListProcessorModule<String>(threadCount, 
						new ExperimentRunnerQueryAndCombineRankingsEH.Factory(project, globalLM))
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
