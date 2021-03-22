package se.de.hu_berlin.informatik.gen.spectra.internal;

import org.apache.commons.cli.Option;
import org.jacoco.core.runtime.AgentOptions;
import se.de.hu_berlin.informatik.gen.spectra.AbstractSpectraGenerationFactory;
import se.de.hu_berlin.informatik.gen.spectra.AbstractSpectraGenerationFactory.Strategy;
import se.de.hu_berlin.informatik.gen.spectra.cobertura.CoberturaSpectraGenerationFactory;
import se.de.hu_berlin.informatik.gen.spectra.jacoco.JaCoCoSpectraGenerationFactory;
import se.de.hu_berlin.informatik.gen.spectra.predicates.PredicatesSpectraGeneratorFactory;
import se.de.hu_berlin.informatik.gen.spectra.tracecobertura.TraceCoberturaSpectraGenerationFactory;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RunAllTestsAndGenSpectra {

    private RunAllTestsAndGenSpectra() {
        //disallow instantiation
    }

    public enum CmdOptions implements OptionWrapperInterface {
        /* add options here according to your needs */
        JAVA_HOME_DIR("java", "javaHomeDir", true, "Path to a Java home directory (at least v1.8). Set if you encounter any version problems. "
                + "If not set, the default JRE is used.", false),
        TEST_CLASS_PATH("cp", "classPath", true, "A class path which may be needed for the execution of tests.", false),
        INSTRUMENTED_DIR("instr", "instrumentedDir", true, "The path to a directory with instrumented classes (if any).", false),
        STRATEGY("strat", "strategy", Strategy.class, Strategy.COBERTURA,
                "The coverage generation strategy to use (i.e., which tool to use with which properties, etc.).", false),
        ORIGINAL_CLASSES_DIRS(Option.builder("oc").longOpt("originalClasses")
                .hasArgs().desc("Paths to the original (not instrumented) classes.")
                .build()),
        TEST_CLASS_DIR("tcd", "testClassDir", true, "The path to the test classes classes.", false),
        AGENT_PORT("p", "port", true, "The port to use for connecting to the JaCoCo Java agent. Default: " + AgentOptions.DEFAULT_PORT, false),
        MAX_ERRORS("maxErr", "maxErrors", true, "The maximum of test execution errors to tolerate. Default: 0", false),
        PIPE_BUFFER_SIZE("pb", "pipeBuffer", true, "Amount of elements in the test pipeline buffers. "
        		+ "(Increases parallelity; don't set too high!) Default: 1", false),
        TEST_LIST("t", "testList", true, "File with all tests to execute.", 0),
        TEST_CLASS_LIST("tcl", "testClassList", true, "File with a list of test classes from which all tests shall be executed.", 0),
        TIMEOUT("tm", "timeout", true, "A timeout (in seconds) for the execution of each test. Tests that run "
                + "longer than the timeout will abort and will count as failing.", false),
        REPEAT_TESTS("r", "repeatTests", true, "Execute each test a set amount of times to (hopefully) "
                + "generate correct coverage data. Default is '1'.", false),
        FULL_SPECTRA("f", "fullSpectra", false, "Set this if a full spectra should be generated with all executable statements. Otherwise, only "
                + "these statements are included that are executed by at least one test case.", false),
        SEPARATE_JVM("jvm", "separateJvm", false, "Set this if each test shall be run in a separate JVM.", false),
        JAVA7("java7", "onlyJava7", false, "Set this if each test shall only be run in a separate JVM with Java 7 (if Java 7 home directory given).", false),
        CONDENSE_NODES("con", "condense", false, "Set this if empty lines in between statements should be filled up.", false),
        //		JAVA7_RUNNER("j7r", "java7Runner", true, "The path to the java 7 runner jar.", false),
        PROJECT_DIR("pd", "projectDir", true, "Path to the directory of the project under test.", true),
        SOURCE_DIR("sd", "sourceDir", true, "Relative path to the main directory containing the sources from the project directory.", true),
        OUTPUT("o", "output", true, "Path to output directory.", true),
        FAILING_TESTS(Option.builder("ft").longOpt("failingTests")
                .hasArgs().desc("A list of tests that should (only) fail. format: qualified.class.name::testMethodName").build()),
        TESTRUNNER_JVM_ARGS(Option.builder("vm").longOpt("jvmArgs")
        		.hasArgs().desc("Configuration arguments for starting separate test runner JVMs.").build());

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
                   final boolean hasArg, final String description, final int groupId) {
            this.option = new OptionWrapper(
                    Option.builder(opt).longOpt(longOpt).required(false).
                            hasArg(hasArg).desc(description).build(), groupId);
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

        //adds the given option that will be part of the group with the given id
        CmdOptions(final Option option, final int groupId) {
            this.option = new OptionWrapper(option, groupId);
        }

        //adds the given option that will be part of no group
        CmdOptions(final Option option) {
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
    public static void main(final String[] args) {

        final OptionParser options = OptionParser.getOptions("RunTestsAndGenSpectra", false, CmdOptions.class, args);

        AbstractSpectraGenerationFactory<?, ?, ?> factory;
        Strategy strategy = options.getOptionValue(CmdOptions.STRATEGY, Strategy.class, Strategy.COBERTURA, true);
        // continue based on chosen strategy; add new strategies in analogy to the existing ones.
        // possibly, new options have to be added to this class (as the agent port had to be added, for example...)
        switch (strategy) {
            case TRACE_COBERTURA: {
                if (System.getProperty("net.sourceforge.cobertura.datafile") == null) {
                    Log.abort(RunAllTestsAndGenSpectra.class, "Please include property '-Dnet.sourceforge.cobertura.datafile=.../cobertura.ser' in the application's call.");
                }
                Path coberturaDataFile = Paths.get(System.getProperty("net.sourceforge.cobertura.datafile"));
                factory = new TraceCoberturaSpectraGenerationFactory(coberturaDataFile.toFile());
                break;
            }
            case COBERTURA: {
                if (System.getProperty("net.sourceforge.cobertura.datafile") == null) {
                    Log.abort(RunAllTestsAndGenSpectra.class, "Please include property '-Dnet.sourceforge.cobertura.datafile=.../cobertura.ser' in the application's call.");
                }
                Path coberturaDataFile = Paths.get(System.getProperty("net.sourceforge.cobertura.datafile"));
                factory = new CoberturaSpectraGenerationFactory(coberturaDataFile.toFile());
                break;
            }
            case JACOCO:
                if (!options.hasOption(CmdOptions.ORIGINAL_CLASSES_DIRS)) {
                    Log.abort(RunAllTestsAndGenSpectra.class, "Option '%s' not set.", CmdOptions.ORIGINAL_CLASSES_DIRS.asArg());
                }
                Integer agentPort = options.getOptionValueAsInt(CmdOptions.AGENT_PORT);
                factory = new JaCoCoSpectraGenerationFactory(agentPort);
                break;
            case PREDICATES:
                factory = new PredicatesSpectraGeneratorFactory(null);
                break;
            default:
                Log.abort(RunAllTestsAndGenSpectra.class, "Unimplemented strategy: '%s'", strategy);
                return;
        }

        new RunTestsAndGenSpectraProcessor<>(factory).submit(options);

        // we have to specifically call exit(0) here, because for some applications under test,
        // this application does not end due to some reason... (e.g. Mockito causes problems)
        Runtime.getRuntime().exit(0);
    }

}
