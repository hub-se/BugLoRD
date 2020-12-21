package se.de.hu_berlin.informatik.experiments.defects4j;

import org.apache.commons.cli.Option;
import org.jacoco.core.runtime.AgentOptions;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JBuggyFixedEntity;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.ToolSpecific;
import se.de.hu_berlin.informatik.experiments.defects4j.calls.*;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.basics.ThreadedProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.eh.EHWithInputAndReturn;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.PipeLinker;
import se.de.hu_berlin.informatik.utils.threaded.SemaphoreThreadLimit;
import se.de.hu_berlin.informatik.utils.threaded.ThreadLimit;

import java.util.Locale;

/**
 * @author Simon Heiden
 */
public class ExperimentRunner {

    public enum CmdOptions implements OptionWrapperInterface {
        /* add options here according to your needs */
        PROJECTS(Option.builder("p").longOpt("projects").required().hasArgs().desc(
                "A list of projects to consider of the Defects4J benchmark. "
                        + "Should be either 'Lang', 'Chart', 'Time', 'Closure', 'Mockito' or 'Math'. Set this to 'all' to "
                        + "iterate over all projects.")
                .build()),
        BUG_IDS(Option.builder("b").longOpt("bugIDs").required().hasArgs().desc(
                "A list of numbers indicating the ids of buggy project versions to consider. "
                        + "Value ranges differ based on the project. Set this to 'all' to "
                        + "iterate over all bugs in a project.")
                .build()),
        EXECUTE(Option.builder("e").longOpt("execute").hasArgs().required().desc(
                "A list of all experiments to execute. (Acceptable values are 'checkout', 'genSpectra', 'checkChanges', 'bugDiagnosis', "
                        + "'computeSBFL', 'query' or 'all') Only one option for computing the SBFL rankings should be used. "
                        + "Additionally, you can just checkout the bug and fix with 'check' and clean up with 'cleanup'.")
                .build()),
        SPECTRA_TOOL("st", "spectraTool", ToolSpecific.class, ToolSpecific.TRACE_COBERTURA,
                "When generating spectra and computing rankings, which spectra should be used?.", false),
        FILL_EMPTY_LINES("fe", "fill", false, "Whether to fill up empty lines between statements "
                + "in the same method. (Only for experiment 'genSpectra'!)", false),
        CONDENSE("c", "condenseNodes", false, "Whether to combine several lines "
                + "with equal trace involvement to larger blocks. (Only for experiment 'computeSBFL'!)", false),
        FILTER("f", "filterSpectra", false,
                "Whether to compute rankings based on filtered spectra. (Only for experiment 'computeSBFL'!)",
                false),
        REMOVE_TEST_CLASSES("r", "removeTestClasses", false, 
        		"Whether to remove nodes that are part of a test class. (Only for experiment 'computeSBFL'!)", false),
        SUFFIX("s", "suffix", true, "A suffix to append to the ranking directory.", false),
        FORCE_LOAD_SPECTRA_FOR_FL("frc", "forceload", false, "Whether the spectra should always be loaded, regardless if a "
                + "trace file has previously been created that would usually be sufficient to compute SBFL rankings.",
                false),
        LM("lm", "globalLM", true, "Path to a language model binary (kenLM).", false),
        RANKING_TYPE("rt", "rankingType", true, "Rank predicates or sbfl", false);

        /* the following code blocks should not need to be changed */
        final private OptionWrapper option;

        // adds an option that is not part of any group
        CmdOptions(final String opt, final String longOpt, final boolean hasArg, final String description,
                   final boolean required) {
            this.option = new OptionWrapper(
                    Option.builder(opt).longOpt(longOpt).required(required).hasArg(hasArg).desc(description).build(),
                    NO_GROUP);
        }

        // adds an option that is part of the group with the specified index
        // (positive integer)
        // a negative index means that this option is part of no group
        // this option will not be required, however, the group itself will be
        CmdOptions(final String opt, final String longOpt, final boolean hasArg, final String description,
                   int groupId) {
            this.option = new OptionWrapper(
                    Option.builder(opt).longOpt(longOpt).required(false).hasArg(hasArg).desc(description).build(),
                    groupId);
        }

        //adds an option that may have arguments from a given set (Enum)
        <T extends Enum<T>> CmdOptions(final String opt, final String longOpt,
                                       Class<T> valueSet, T defaultValue, final String description, final boolean required) {
            if (defaultValue == null) {
                this.option = new OptionWrapper(
                        Option.builder(opt).longOpt(longOpt).required(required).
                                hasArgs().desc(description + " Possible arguments: " +
                                Misc.enumToString(valueSet) + ".").build(), NO_GROUP);
            } else {
                this.option = new OptionWrapper(
                        Option.builder(opt).longOpt(longOpt).required(required).
                                hasArg(true).desc(description + " Possible arguments: " +
                                Misc.enumToString(valueSet) + ". Default: " +
                                defaultValue.toString() + ".").build(), NO_GROUP);
            }
        }

        // adds the given option that will be part of the group with the given
        // id
        CmdOptions(Option option, int groupId) {
            this.option = new OptionWrapper(option, groupId);
        }

        // adds the given option that will be part of no group
        CmdOptions(Option option) {
            this(option, NO_GROUP);
        }

        @Override
        public String toString() {
            return option.getOption().getOpt();
        }

        @Override
        public OptionWrapper getOptionWrapper() {
            return option;
        }
    }

    /**
     * @param args command line arguments
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
            projects = Defects4J.getAllProjectIDs();
        }

        PipeLinker linker = new PipeLinker();
        ThreadLimit limit = new SemaphoreThreadLimit(threadCount);

        if (toDoContains(toDo, "check")) {
            linker.append(
                    new ThreadedProcessor<>(threadCount, limit,
                            new ERCheckoutBugAndFixEH()));
        }

        if (toDoContains(toDo, "checkout") || toDoContains(toDo, "all")) {
            linker.append(
                    new ThreadedProcessor<>(threadCount, limit, new ERCheckoutEH()));
        }

        ToolSpecific toolSpecific = options.getOptionValue(CmdOptions.SPECTRA_TOOL,
                ToolSpecific.class, ToolSpecific.TRACE_COBERTURA, true);
        
        if (toDoContains(toDo, "genSpectra") || toDoContains(toDo, "all")) {
            // every thread needs its own port for the JaCoCo Java agent, sadly...
            EHWithInputAndReturn<BuggyFixedEntity<?>, BuggyFixedEntity<?>> firstEH =
                    new ERGenerateSpectraEH(toolSpecific, options.getOptionValue(CmdOptions.SUFFIX, null), AgentOptions.DEFAULT_PORT, options.hasOption(CmdOptions.FILL_EMPTY_LINES)).asEH();
            @SuppressWarnings("unchecked") final Class<EHWithInputAndReturn<BuggyFixedEntity<?>, BuggyFixedEntity<?>>> clazz = (Class<EHWithInputAndReturn<BuggyFixedEntity<?>, BuggyFixedEntity<?>>>) firstEH.getClass();
            final EHWithInputAndReturn<BuggyFixedEntity<?>, BuggyFixedEntity<?>>[] handlers = Misc.createGenericArray(clazz, threadCount);

            handlers[0] = firstEH;
            for (int i = 1; i < handlers.length; ++i) {
                // create modules with different port numbers
                handlers[i] = new ERGenerateSpectraEH(toolSpecific, options.getOptionValue(CmdOptions.SUFFIX, null), AgentOptions.DEFAULT_PORT + (i * 3), options.hasOption(CmdOptions.FILL_EMPTY_LINES)).asEH();
            }
            linker.append(
                    new ThreadedProcessor<>(limit, handlers));
        }
        if (toDoContains(toDo, "predicates")) {
            EHWithInputAndReturn<BuggyFixedEntity<?>, BuggyFixedEntity<?>> firstEH =
                    new ERProducePredicates(options.getOptionValue(CmdOptions.SUFFIX, null), options.hasOption(CmdOptions.FILL_EMPTY_LINES)).asEH();
            @SuppressWarnings("unchecked") final Class<EHWithInputAndReturn<BuggyFixedEntity<?>, BuggyFixedEntity<?>>> clazz = (Class<EHWithInputAndReturn<BuggyFixedEntity<?>, BuggyFixedEntity<?>>>) firstEH.getClass();
            final EHWithInputAndReturn<BuggyFixedEntity<?>, BuggyFixedEntity<?>>[] handlers = Misc.createGenericArray(clazz, threadCount);

            handlers[0] = firstEH;
            for (int i = 1; i < handlers.length; ++i) {
                handlers[i] = new ERProducePredicates(options.getOptionValue(CmdOptions.SUFFIX, null), options.hasOption(CmdOptions.FILL_EMPTY_LINES)).asEH();
            }
            linker.append(new ThreadedProcessor<>(limit, handlers));
        }
        if (toDoContains(toDo, "genCodeLocationRanking")) {
            EHWithInputAndReturn<BuggyFixedEntity<?>, BuggyFixedEntity<?>> firstEH =
                    new GenCodeLocationBasedRankings(options.getOptionValue(CmdOptions.SUFFIX, null), options.getOptionValue(CmdOptions.RANKING_TYPE)).asEH();
            @SuppressWarnings("unchecked") final Class<EHWithInputAndReturn<BuggyFixedEntity<?>, BuggyFixedEntity<?>>> clazz = (Class<EHWithInputAndReturn<BuggyFixedEntity<?>, BuggyFixedEntity<?>>>) firstEH.getClass();
            final EHWithInputAndReturn<BuggyFixedEntity<?>, BuggyFixedEntity<?>>[] handlers = Misc.createGenericArray(clazz, threadCount);

            handlers[0] = firstEH;
            for (int i = 1; i < handlers.length; ++i) {
                handlers[i] = new GenCodeLocationBasedRankings(options.getOptionValue(CmdOptions.SUFFIX, null), options.getOptionValue(CmdOptions.RANKING_TYPE)).asEH();
            }
            linker.append(new ThreadedProcessor<>(limit, handlers));
        }

        if (toDoContains(toDo, "reSave")) {
            EHWithInputAndReturn<BuggyFixedEntity<?>, BuggyFixedEntity<?>> firstEH =
                    new ERLoadAndSaveSpectraEH(toolSpecific, options.hasOption(CmdOptions.FILL_EMPTY_LINES)).asEH();
            @SuppressWarnings("unchecked") final Class<EHWithInputAndReturn<BuggyFixedEntity<?>, BuggyFixedEntity<?>>> clazz = (Class<EHWithInputAndReturn<BuggyFixedEntity<?>, BuggyFixedEntity<?>>>) firstEH.getClass();
            final EHWithInputAndReturn<BuggyFixedEntity<?>, BuggyFixedEntity<?>>[] handlers = Misc.createGenericArray(clazz, threadCount);

            handlers[0] = firstEH;
            for (int i = 1; i < handlers.length; ++i) {
                // create modules with different port numbers
                handlers[i] = new ERLoadAndSaveSpectraEH(toolSpecific, options.hasOption(CmdOptions.FILL_EMPTY_LINES)).asEH();
            }
            linker.append(
                    new ThreadedProcessor<>(new SemaphoreThreadLimit(1), handlers));
        }
        
        if (toDoContains(toDo, "computeSBFL") || toDoContains(toDo, "all")) {
            linker.append(
                    new ThreadedProcessor<>(threadCount, limit,
                            new ERComputeSBFLRankingsFromSpectraEH(toolSpecific,
                                    options.getOptionValue(CmdOptions.SUFFIX, null),
                                    options.hasOption(CmdOptions.FILTER), 
                                    options.hasOption(CmdOptions.REMOVE_TEST_CLASSES),
                                    options.hasOption(CmdOptions.CONDENSE),
                                    options.hasOption(CmdOptions.FORCE_LOAD_SPECTRA_FOR_FL))));
        }

        if (toDoContains(toDo, "checkChanges") || toDoContains(toDo, "all")) {
            linker.append(
                    new ThreadedProcessor<>(threadCount, limit,
                            new ERCheckoutFixAndCheckForChangesEH()));
        }
        
        if (toDoContains(toDo, "bugDiagnosis")) {
            linker.append(
                    new ThreadedProcessor<>(threadCount, limit, new ERBugDiagnosisEH()));
        }

        if (toDoContains(toDo, "query") || toDoContains(toDo, "all")) {
            String globalLM = options.getOptionValue(CmdOptions.LM, null);

            // if (globalLM != null && !(new File(globalLM)).exists()) {
            // Log.abort(ExperimentRunner.class, "Given global LM doesn't exist:
            // '" + globalLM + "'.");
            // }

            linker.append(
                    new ThreadedProcessor<>(threadCount, limit,
                            new ERQueryLMRankingsEH(options.getOptionValue(CmdOptions.SUFFIX, null), globalLM)));
        }

        if (toDoContains(toDo, "cleanup")) {
            linker.append(
                    new ThreadedProcessor<>(threadCount, limit, new ERCleanupEH()));
        }

        // iterate over all projects
        for (String project : projects) {
            if (all) {
                ids = Defects4J.getAllBugIDs(project);
            }
            for (String id : ids) {
                linker.submit(new Defects4JBuggyFixedEntity(project, id));
            }
        }

        linker.shutdown();
    }

    private static boolean toDoContains(String[] toDo, String item) {
        for (String element : toDo) {
            if (element.toLowerCase(Locale.getDefault()).equals(item.toLowerCase(Locale.getDefault())))
                return true;
        }
        return false;
    }

}
