/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.experiments;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.defects4j.experiments.calls.ExperimentRunnerCheckoutAndGenerateSpectraEH;
import se.de.hu_berlin.informatik.defects4j.experiments.calls.ExperimentRunnerCheckoutFixAndCheckForChangesEH;
import se.de.hu_berlin.informatik.defects4j.experiments.calls.ExperimentRunnerComputeSBFLRankingsFromSpectraEH;
import se.de.hu_berlin.informatik.defects4j.experiments.calls.ExperimentRunnerQueryAndCombineRankingsEH;
import se.de.hu_berlin.informatik.defects4j.frontend.Defects4JEntity;
import se.de.hu_berlin.informatik.defects4j.frontend.BuggyFixedBenchmarkEntity;
import se.de.hu_berlin.informatik.defects4j.frontend.Defects4J;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.threaded.ThreadLimit;
import se.de.hu_berlin.informatik.utils.threaded.SemaphoreThreadLimit;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.PipeLinker;
import se.de.hu_berlin.informatik.utils.tm.pipes.ThreadedProcessorPipe;


/**
 * 
 * @author Simon Heiden
 */
public class ExperimentRunner {

	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		PROJECTS(Option.builder("p").longOpt("projects").required().hasArgs()
				.desc("A list of projects to consider of the Defects4J benchmark. "
						+ "Should be either 'Lang', 'Chart', 'Time', 'Closure' or 'Math'. Set this to 'all' to "
						+ "iterate over all projects.").build()),
		BUG_IDS(Option.builder("b").longOpt("bugIDs").required().hasArgs()
				.desc("A list of numbers indicating the ids of buggy project versions to consider. "
						+ "Value ranges differ based on the project. Set this to 'all' to "
						+ "iterate over all bugs in a project.").build()),
        EXECUTE(Option.builder("e").longOpt("execute").hasArgs().required()
        		.desc("A list of all experiments to execute. ('checkout', 'checkChanges', 'computeSBFL', "
        				+ "'query' or 'all')").build()),
        LM("lm", "globalLM", true, "Path to a language model binary (kenLM).", false);

		/* the following code blocks should not need to be changed */
		final private OptionWrapper option;

		//adds an option that is not part of any group
		CmdOptions(final String opt, final String longOpt, 
				final boolean hasArg, final String description, final boolean required) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(required).
					hasArg(hasArg).desc(description).build(), NO_GROUP);
		}
		
		//adds an option that is part of the group with the specified index (positive integer)
		//a negative index means that this option is part of no group
		//this option will not be required, however, the group itself will be
		CmdOptions(final String opt, final String longOpt, 
				final boolean hasArg, final String description, int groupId) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(false).
					hasArg(hasArg).desc(description).build(), groupId);
		}
		
		//adds the given option that will be part of the group with the given id
		CmdOptions(Option option, int groupId) {
			this.option = new OptionWrapper(option, groupId);
		}
		
		//adds the given option that will be part of no group
		CmdOptions(Option option) {
			this(option, NO_GROUP);
		}

		@Override public String toString() { return option.getOption().getOpt(); }
		@Override public OptionWrapper getOptionWrapper() { return option; }
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
		Log.out(ExperimentRunner.class, "Using %d parallel threads.", threadCount);

		if (projects[0].equals("all")) {
			projects = Defects4J.getAllProjects();
		}
		
		PipeLinker linker = new PipeLinker();
		ThreadLimit limit = new SemaphoreThreadLimit(threadCount);
		
		if (toDoContains(toDo, "checkout") || toDoContains(toDo, "all")) {
			linker.append(new ThreadedProcessorPipe<BuggyFixedBenchmarkEntity,BuggyFixedBenchmarkEntity>(threadCount, limit, 
					new ExperimentRunnerCheckoutAndGenerateSpectraEH.Factory()));
		}

		if (toDoContains(toDo, "checkChanges") || toDoContains(toDo, "all")) {
			linker.append(new ThreadedProcessorPipe<BuggyFixedBenchmarkEntity,BuggyFixedBenchmarkEntity>(threadCount, limit, 
					new ExperimentRunnerCheckoutFixAndCheckForChangesEH.Factory()));
		}
			
		if (toDoContains(toDo, "computeSBFL") || toDoContains(toDo, "all")) {
			linker.append(new ThreadedProcessorPipe<BuggyFixedBenchmarkEntity,BuggyFixedBenchmarkEntity>(threadCount, limit, 
					new ExperimentRunnerComputeSBFLRankingsFromSpectraEH.Factory()));
		}

		if (toDoContains(toDo, "query") || toDoContains(toDo, "all")) {
			String globalLM = options.getOptionValue(CmdOptions.LM, null);
			
//			if (globalLM != null && !(new File(globalLM)).exists()) {
//				Log.abort(ExperimentRunner.class, "Given global LM doesn't exist: '" + globalLM + "'.");
//			}
			
			linker.append(new ThreadedProcessorPipe<BuggyFixedBenchmarkEntity,BuggyFixedBenchmarkEntity>(threadCount, limit, 
					new ExperimentRunnerQueryAndCombineRankingsEH.Factory(globalLM)));
		}
		
		//iterate over all projects
		for (String project : projects) {
			if (all) {
				ids = Defects4J.getAllBugIDs(project); 
			}
			for (String id : ids) {
				linker.submit(Defects4JEntity.getBuggyDefects4JEntity(project, id));
			}
		}
		
		linker.shutdown();
	}
	
	private static boolean toDoContains(String[] toDo, String item) {
		for (String element : toDo) {
			if (element.toLowerCase().equals(item.toLowerCase()))
				return true;
		}
		return false;
	}
	
}
