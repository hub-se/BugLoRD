package se.de.hu_berlin.informatik.gen.spectra.main;

import org.apache.commons.cli.Option;
import org.jacoco.core.runtime.AgentOptions;
import se.de.hu_berlin.informatik.gen.spectra.AbstractSpectraGenerationFactory;
import se.de.hu_berlin.informatik.gen.spectra.AbstractSpectraGenerator;
import se.de.hu_berlin.informatik.gen.spectra.predicates.PredicatesSpectraGeneratorFactory;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;

public class PredicatesSpectraGenerator extends AbstractSpectraGenerator {

    public enum CmdOptions implements OptionWrapperInterface {
        /* add options here according to your needs */
        JAVA_HOME_DIR("java", "javaHomeDir", true, "Path to a Java home directory (at least v1.8). Set if you encounter any version problems. "
                + "If not set, the default JRE is used.", false),
        CLASS_PATH("cp", "classPath", true, "An additional class path which may be needed for the execution of tests. "
                + "Will be appended to the regular class path if this option is set.", false),
        AGENT_PORT("p", "port", true, "The port to use for connecting to the JaCoCo Java agent. Default: " + AgentOptions.DEFAULT_PORT, false),
        TIMEOUT("tm", "timeout", true, "A timeout (in seconds) for the execution of each test. Tests that run "
                + "longer than the timeout will abort and will count as failing.", false),
        REPEAT_TESTS("r", "repeatTests", true, "Execute each test a set amount of times to (hopefully) "
                + "generate correct coverage data. Default is '1'.", false),
        MAX_ERRORS("maxErr", "maxErrors", true, "The maximum of test execution errors to tolerate. Default: 0", false),
        FULL_SPECTRA("f", "fullSpectra", false, "Set this if a full spectra should be generated with all executable statements. Otherwise, only "
                + "these statements are included that are executed by at least one test case.", false),
        SEPARATE_JVM("jvm", "separateJvm", false, "Set this if each test shall be run in a separate JVM.", false),
        JAVA7("java7", "onlyJava7", false, "Set this if each test shall only be run in a separate JVM with Java 7 (if Java 7 home directory given).", false),
        TEST_LIST("t", "testList", true, "File with all tests to execute.", 0),
        TEST_CLASS_LIST("tcl", "testClassList", true, "File with a list of test classes from which all tests shall be executed.", 0),
        INSTRUMENT_CLASSES(Option.builder("c").longOpt("classes").required()
                .hasArgs().desc("A list of classes/directories to instrument with JaCoCo.").build()),
        PROJECT_DIR("pd", "projectDir", true, "Path to the directory of the project under test.", true),
        SOURCE_DIR("sd", "sourceDir", true, "Relative path to the main directory containing the sources from the project directory.", true),
        TEST_CLASS_DIR("td", "testClassDir", true, "Relative path to the main directory containing the needed test classes from the project directory.", true),
        OUTPUT("o", "output", true, "Path to output directory.", true),
        JOINSTRATEGY("j", "joinStrategy", true, "Strategy used to construct joint Predicates.", false);

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
     * @param args
     * command line arguments
     */
    public static void main(final String[] args) {

        final OptionParser options = OptionParser.getOptions("Predicates", false, PredicatesSpectraGenerator.CmdOptions.class, args);

        String projectDir = options.getOptionValue(PredicatesSpectraGenerator.CmdOptions.PROJECT_DIR);
        String sourceDir = options.getOptionValue(PredicatesSpectraGenerator.CmdOptions.SOURCE_DIR);
        String testClassDir = options.getOptionValue(PredicatesSpectraGenerator.CmdOptions.TEST_CLASS_DIR);

        String outputDir = options.getOptionValue(PredicatesSpectraGenerator.CmdOptions.OUTPUT);

        final String[] classesToInstrument = options.getOptionValues(PredicatesSpectraGenerator.CmdOptions.INSTRUMENT_CLASSES);

        final String javaHome = options.getOptionValue(PredicatesSpectraGenerator.CmdOptions.JAVA_HOME_DIR, null);

        String testClassPath = options.getOptionValue(PredicatesSpectraGenerator.CmdOptions.CLASS_PATH, null);

        boolean useFullSpectra = options.hasOption(PredicatesSpectraGenerator.CmdOptions.FULL_SPECTRA);
        boolean useSeparateJVM = options.hasOption(PredicatesSpectraGenerator.CmdOptions.SEPARATE_JVM);
        boolean useJava7 = options.hasOption(PredicatesSpectraGenerator.CmdOptions.JAVA7);

        String testClassList = options.getOptionValue(PredicatesSpectraGenerator.CmdOptions.TEST_CLASS_LIST);
        String testList = options.getOptionValue(PredicatesSpectraGenerator.CmdOptions.TEST_LIST);

        Long timeout = options.getOptionValueAsLong(PredicatesSpectraGenerator.CmdOptions.TIMEOUT);
        int testRepeatCount = options.getOptionValueAsInt(PredicatesSpectraGenerator.CmdOptions.REPEAT_TESTS, 1);

        int maxErrors = options.getOptionValueAsInt(PredicatesSpectraGenerator.CmdOptions.MAX_ERRORS, 0);

        Integer agentPort = options.getOptionValueAsInt(PredicatesSpectraGenerator.CmdOptions.AGENT_PORT);

        String joinStrategy = options.getOptionValue(CmdOptions.JOINSTRATEGY);

//		AbstractSpectraGenerationFactory<?, ?> factory = new JaCoCoSpectraGenerationFactory(agentPort);
//		new JaCoCoSpectraGenerator().generateSpectra(
//				factory, projectDir, sourceDir, testClassDir, outputDir,
//				testClassPath, testClassList, testList, javaHome,
//				useFullSpectra, useSeparateJVM, useJava7, timeout, testRepeatCount,
//				maxErrors, agentPort, null, classesToInstrument);

        new PredicatesSpectraGenerator.Builder()
                .setJoinStrategy(joinStrategy)
                .setProjectDir(projectDir)
                .setSourceDir(sourceDir)
                .setTestClassDir(testClassDir)
                .setOutputDir(outputDir)
                .setTestClassPath(testClassPath)
                .setTestClassList(testClassList)
                .setTestList(testList)
                .setJavaHome(javaHome)
                .useFullSpectra(useFullSpectra)
                .useSeparateJVM(useSeparateJVM)
                .useJava7only(useJava7)
                .setTimeout(timeout)
                .setTestRepeatCount(testRepeatCount)
                .setMaxErrors(maxErrors)
                .setPathsToBinaries(classesToInstrument)
                .run();

    }


    public static class Builder extends AbstractBuilder {
        private String joinStrategy;

        @Override
        public void run() {
            AbstractSpectraGenerationFactory<?, ?, ?> factory = new PredicatesSpectraGeneratorFactory(joinStrategy);
            super.run(factory,null);
        }

        public Builder setJoinStrategy(String joinStrategy) {
            this.joinStrategy = joinStrategy;
            return this;
        }
    }
}
