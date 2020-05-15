package se.de.hu_berlin.informatik.gen.spectra.jacoco;

import org.jacoco.agent.AgentJar;
import org.jacoco.core.runtime.AgentOptions;
import se.de.hu_berlin.informatik.gen.spectra.AbstractInstrumenter;
import se.de.hu_berlin.informatik.gen.spectra.AbstractSpectraGenerationFactory;
import se.de.hu_berlin.informatik.gen.spectra.SpectraSaveProcessor;
import se.de.hu_berlin.informatik.gen.spectra.internal.Java7TestRunnerJar;
import se.de.hu_berlin.informatik.gen.spectra.internal.RunAllTestsAndGenSpectra.CmdOptions;
import se.de.hu_berlin.informatik.gen.spectra.internal.RunTestsAndGenSpectraProcessor;
import se.de.hu_berlin.informatik.gen.spectra.jacoco.modules.JaCoCoAddReportToProviderAndGenerateSpectraModule;
import se.de.hu_berlin.informatik.gen.spectra.jacoco.modules.JaCoCoInstrumenter;
import se.de.hu_berlin.informatik.gen.spectra.jacoco.modules.JaCoCoRunSingleTestAndReportModule;
import se.de.hu_berlin.informatik.gen.spectra.jacoco.modules.SerializableExecFileLoader;
import se.de.hu_berlin.informatik.gen.spectra.main.JaCoCoSpectraGenerator;
import se.de.hu_berlin.informatik.gen.spectra.modules.AbstractRunSingleTestAndReportModule;
import se.de.hu_berlin.informatik.junittestutils.data.StatisticsData;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.provider.jacoco.report.JaCoCoReportWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.processors.AbstractConsumingProcessor;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Random;

public class JaCoCoSpectraGenerationFactory extends
        AbstractSpectraGenerationFactory<SerializableExecFileLoader, JaCoCoReportWrapper, ISpectra<SourceCodeBlock, ?>> {

    /**
     * set this to true to instrument the classes first, before execution; if
     * this is set to false, then the "on-the-fly" instrumentation of JaCoCo
     * will be used
     */
    public final static boolean OFFLINE_INSTRUMENTATION = true;

    private File jacocoAgentJar = null;
    private final Integer agentPort;

    public JaCoCoSpectraGenerationFactory(Integer agentPort) {
        this.agentPort = agentPort;
        try {
            jacocoAgentJar = AgentJar.extractToTempLocation();
        } catch (IOException e) {
            Log.abort(JaCoCoSpectraGenerator.class, e, "Could not create JaCoCo agent jar file.");
        }
    }

    @Override
    public Strategy getStrategy() {
        return Strategy.JACOCO;
    }

    @Override
    public AbstractInstrumenter getInstrumenter(Path projectDir, String outputDir, String testClassPath,
                                                String... pathsToBinaries) {
        return new JaCoCoInstrumenter(projectDir, outputDir, testClassPath, pathsToBinaries);
    }

    @Override
    public String[] getElementsToAddToTestClassPathForMainTestRunner() {
        if (OFFLINE_INSTRUMENTATION) {
            if (jacocoAgentJar != null) {
                return new String[]{jacocoAgentJar.getAbsolutePath()};
            }
        }
        return null;
    }

    @Override
    public String[] getPropertiesForMainTestRunner(Path projectDir, boolean useSeparateJVM) {
        int port = AgentOptions.DEFAULT_PORT;
        if (agentPort != null) {
            port = agentPort;
        }
        String[] properties;
        if (useSeparateJVM) {
            properties = new String[]{NUMA, GC, INITIAL_HEAP, MAX_HEAP};
        } else {
            // get a port that is not yet used...
            port = getFreePort(port);
            if (port == -1) {
                Log.abort(JaCoCoSpectraGenerator.class, "Could not find an unused port...");
            }
            Log.out(JaCoCoSpectraGenerator.class, "Using port %d.", port);

            if (OFFLINE_INSTRUMENTATION) {
                properties = new String[]{"-Djacoco-agent.dumponexit=false", "-Djacoco-agent.output=tcpserver",
                        "-Djacoco-agent.excludes=*", "-Djacoco-agent.port=" + port, NUMA,
                        GC, INITIAL_HEAP, MAX_HEAP};
            } else {
                properties = new String[]{
                        "-javaagent:" + jacocoAgentJar.getAbsolutePath() + "=dumponexit=false," + "output=tcpserver,"
                                + "excludes=se.de.hu_berlin.informatik.*:org.junit.*," + "port=" + port,
                                NUMA, GC, INITIAL_HEAP, MAX_HEAP};
            }
        }
        return properties;
    }

    @Override
    public String[] getSpecificArgsForMainTestRunner() {
        return null;
    }

    @Override
    public AbstractRunSingleTestAndReportModule<SerializableExecFileLoader, JaCoCoReportWrapper> getTestRunnerModule(
            OptionParser options, ClassLoader testAndInstrumentClassLoader, String testClassPath,
            StatisticsCollector<StatisticsData> statisticsContainer) {
        final Path projectDir = options.isDirectory(CmdOptions.PROJECT_DIR, true);
        final Path srcDir = options.isDirectory(projectDir, CmdOptions.SOURCE_DIR, true);
        final String outputDir = options.isDirectory(CmdOptions.OUTPUT, false).toString();

        int port = AgentOptions.DEFAULT_PORT;
        if (options.hasOption(CmdOptions.AGENT_PORT)) {
            try {
                port = Integer.valueOf(options.getOptionValue(CmdOptions.AGENT_PORT));
            } catch (NumberFormatException e) {
                Log.abort(
                        JaCoCoSpectraGenerator.class, "Could not parse given agent port: %s.",
                        options.getOptionValue(CmdOptions.AGENT_PORT));
            }
        }

        File testrunnerJar = null;
        try {
            testrunnerJar = Java7TestRunnerJar.extractToTempLocation();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new JaCoCoRunSingleTestAndReportModule(Paths.get(outputDir, "__jacoco.exec").toAbsolutePath(), outputDir,
                projectDir.toFile(), srcDir.toString(), options.getOptionValues(CmdOptions.ORIGINAL_CLASSES_DIRS), port,
                RunTestsAndGenSpectraProcessor.TEST_DEBUG_OUTPUT,
                options.hasOption(CmdOptions.TIMEOUT) ? Long.valueOf(options.getOptionValue(CmdOptions.TIMEOUT)) : null,
                options.hasOption(CmdOptions.REPEAT_TESTS)
                        ? Integer.valueOf(options.getOptionValue(CmdOptions.REPEAT_TESTS)) : 1,
                // new ClassPathParser().parseSystemClasspath().getClasspath() +
                // File.pathSeparator +
                testClassPath, options.getOptionValue(CmdOptions.JAVA_HOME_DIR, null),
//				RunTestsAndGenSpectraProcessor.class.getResource("/testrunner.jar").getPath(),
                Objects.requireNonNull(testrunnerJar).getAbsolutePath(),
                options.hasOption(CmdOptions.SEPARATE_JVM), options.hasOption(CmdOptions.JAVA7),
                options.getOptionValueAsInt(CmdOptions.MAX_ERRORS, 0),
                options.getOptionValues(CmdOptions.FAILING_TESTS), statisticsContainer, testAndInstrumentClassLoader);
    }

    @Override
    public AbstractProcessor<JaCoCoReportWrapper, ISpectra<SourceCodeBlock, ?>> getReportToSpectraProcessor(
            OptionParser options, StatisticsCollector<StatisticsData> statisticsContainer) {
        return new JaCoCoAddReportToProviderAndGenerateSpectraModule(
                null/* outputDir + File.separator + "fail" */, options.hasOption(CmdOptions.FULL_SPECTRA),
                statisticsContainer);
    }

    private static int getFreePort(final int startPort) {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(AgentOptions.DEFAULT_ADDRESS);
        } catch (UnknownHostException e1) {
            // should not happen
            return -1;
        }
        // port between 0 and 65535 !
        Random random = new Random();
        int currentPort = startPort;
        int count = 0;
        while (true) {
            if (count > 1000) {
                return -1;
            }
            ++count;
            try {
                new Socket(inetAddress, currentPort).close();
            } catch (final IOException e) {
                // found a free port
                break;
            } catch (IllegalArgumentException e) {
                // should only happen on first try (if argument wrong)
            }
            currentPort = random.nextInt(60536) + 5000;
        }
        return currentPort;
    }

    @Override
    public AbstractConsumingProcessor<ISpectra<SourceCodeBlock, ?>> getSpectraProcessor(OptionParser options) {
        return new SpectraSaveProcessor(options);
    }

}
