package se.de.hu_berlin.informatik.gen.spectra.predicates;

import se.de.hu_berlin.informatik.gen.spectra.AbstractInstrumenter;
import se.de.hu_berlin.informatik.gen.spectra.AbstractSpectraGenerationFactory;
import se.de.hu_berlin.informatik.gen.spectra.internal.Java7TestRunnerJar;
import se.de.hu_berlin.informatik.gen.spectra.internal.RunAllTestsAndGenSpectra;
import se.de.hu_berlin.informatik.gen.spectra.internal.RunTestsAndGenSpectraProcessor;
import se.de.hu_berlin.informatik.gen.spectra.modules.AbstractRunSingleTestAndReportModule;
import se.de.hu_berlin.informatik.gen.spectra.predicates.extras.PredicateReportWrapper;
import se.de.hu_berlin.informatik.gen.spectra.predicates.extras.Profile;
import se.de.hu_berlin.informatik.gen.spectra.predicates.modules.PredicateInstrumenter;
import se.de.hu_berlin.informatik.gen.spectra.predicates.modules.PredicateProcessor;
import se.de.hu_berlin.informatik.gen.spectra.predicates.modules.PredicateSaver;
import se.de.hu_berlin.informatik.gen.spectra.predicates.modules.PredicatesRunSingleTestAndReportModule;
import se.de.hu_berlin.informatik.junittestutils.data.StatisticsData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ProjectData;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.processors.AbstractConsumingProcessor;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class PredicatesSpectraGeneratorFactory  extends AbstractSpectraGenerationFactory<ProjectData, PredicateReportWrapper, Profile> {

    private final String joinStrategy;

    public PredicatesSpectraGeneratorFactory(String joinStrategy) {
        this.joinStrategy = joinStrategy;
    }

    @Override
    public Strategy getStrategy() {
        return Strategy.PREDICATES;
    }

    @Override
    public AbstractInstrumenter getInstrumenter(Path projectDir, String outputDir, String testClassPath,
                                                String... pathsToBinaries) {
        return new PredicateInstrumenter(projectDir, outputDir, testClassPath, this.joinStrategy, pathsToBinaries);
    }

    @Override
    public String[] getElementsToAddToTestClassPathForMainTestRunner() {
        return null;
    }

    @Override
    public String[] getPropertiesForMainTestRunner(Path projectDir, boolean useSeparateJVM) {
        return  getJVMConfigArguments();
    }

    @Override
    public String[] getSpecificArgsForMainTestRunner() {
        return null;
    }

    @Override
    public AbstractRunSingleTestAndReportModule<ProjectData, PredicateReportWrapper> getTestRunnerModule(
            OptionParser options, ClassLoader testAndInstrumentClassLoader, String testClassPath,
            StatisticsCollector<StatisticsData> statisticsContainer) {
        final Path projectDir = options.isDirectory(RunAllTestsAndGenSpectra.CmdOptions.PROJECT_DIR, true);
        final Path srcDir = options.isDirectory(projectDir, RunAllTestsAndGenSpectra.CmdOptions.SOURCE_DIR, true);
        final String outputDir = options.isDirectory(RunAllTestsAndGenSpectra.CmdOptions.OUTPUT, false).toString();

        File testrunnerJar = null;
        try {
            testrunnerJar = Java7TestRunnerJar.extractToTempLocation();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new PredicatesRunSingleTestAndReportModule(null, outputDir,
                projectDir.toFile(), srcDir.toString(), options.hasOption(RunAllTestsAndGenSpectra.CmdOptions.FULL_SPECTRA),
                RunTestsAndGenSpectraProcessor.TEST_DEBUG_OUTPUT,
                options.hasOption(RunAllTestsAndGenSpectra.CmdOptions.TIMEOUT) ? Long.valueOf(options.getOptionValue(RunAllTestsAndGenSpectra.CmdOptions.TIMEOUT)) : null,
                options.hasOption(RunAllTestsAndGenSpectra.CmdOptions.REPEAT_TESTS)
                        ? Integer.valueOf(options.getOptionValue(RunAllTestsAndGenSpectra.CmdOptions.REPEAT_TESTS)) : 1,
                testClassPath, options.getOptionValue(RunAllTestsAndGenSpectra.CmdOptions.JAVA_HOME_DIR, null),
//				RunTestsAndGenSpectraProcessor.class.getResource("/testrunner.jar").getPath(),
                Objects.requireNonNull(testrunnerJar).getAbsolutePath(),
                options.hasOption(RunAllTestsAndGenSpectra.CmdOptions.SEPARATE_JVM), options.hasOption(RunAllTestsAndGenSpectra.CmdOptions.JAVA7),
                options.getOptionValueAsInt(RunAllTestsAndGenSpectra.CmdOptions.MAX_ERRORS, 0),
                options.getOptionValues(RunAllTestsAndGenSpectra.CmdOptions.FAILING_TESTS), statisticsContainer, testAndInstrumentClassLoader,
                getSmallJVMConfigArguments());
    }

    @Override
    public AbstractProcessor<PredicateReportWrapper, Profile> getReportToSpectraProcessor(
            OptionParser options, StatisticsCollector<StatisticsData> statisticsContainer) {
        final String outputDir = options.isDirectory(RunAllTestsAndGenSpectra.CmdOptions.OUTPUT, false).toString();
        return new PredicateProcessor(outputDir);
//        return new TraceCoberturaAddReportToProviderAndGenerateSpectraModule(
//                null/* outputDir + File.separator + "fail" */, options.hasOption(RunAllTestsAndGenSpectra.CmdOptions.FULL_SPECTRA),
//                statisticsContainer, Paths.get(outputDir));
    }

    @Override
    public AbstractConsumingProcessor<Profile> getSpectraProcessor(OptionParser options) {
        final String outputDir = options.isDirectory(RunAllTestsAndGenSpectra.CmdOptions.OUTPUT, false).toString();
        return new PredicateSaver(outputDir);
//        return new SpectraSaveProcessor(options);
    }

}
