package se.de.hu_berlin.informatik.gen.spectra.cobertura;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import se.de.hu_berlin.informatik.gen.spectra.AbstractInstrumenter;
import se.de.hu_berlin.informatik.gen.spectra.AbstractSpectraGenerationFactory;
import se.de.hu_berlin.informatik.gen.spectra.SpectraSaveProcessor;
import se.de.hu_berlin.informatik.gen.spectra.cobertura.modules.CoberturaAddReportToProviderAndGenerateSpectraModule;
import se.de.hu_berlin.informatik.gen.spectra.cobertura.modules.CoberturaInstrumenter;
import se.de.hu_berlin.informatik.gen.spectra.cobertura.modules.CoberturaRunSingleTestAndReportModule;
import se.de.hu_berlin.informatik.gen.spectra.internal.Java7TestRunnerJar;
import se.de.hu_berlin.informatik.gen.spectra.internal.RunTestsAndGenSpectraProcessor;
import se.de.hu_berlin.informatik.gen.spectra.internal.RunAllTestsAndGenSpectra.CmdOptions;
import se.de.hu_berlin.informatik.gen.spectra.modules.AbstractRunSingleTestAndReportModule;
import se.de.hu_berlin.informatik.junittestutils.data.StatisticsData;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.provider.cobertura.report.CoberturaReportWrapper;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ProjectData;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.processors.AbstractConsumingProcessor;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;

public class CoberturaSpectraGenerationFactory
		extends AbstractSpectraGenerationFactory<ProjectData, CoberturaReportWrapper, ISpectra<SourceCodeBlock, ?>> {

	private File coberturaDataFile;

	public CoberturaSpectraGenerationFactory(File coberturaDataFile) {
		this.coberturaDataFile = coberturaDataFile;
	}

	public CoberturaSpectraGenerationFactory(String outputDir) {
		this.coberturaDataFile = Paths.get(outputDir, "cobertura.ser").toAbsolutePath().toFile();
	}

	@Override
	public Strategy getStrategy() {
		return Strategy.COBERTURA;
	}

	@Override
	public AbstractInstrumenter getInstrumenter(Path projectDir, String outputDir, String testClassPath,
			String... pathsToBinaries) {
		return new CoberturaInstrumenter(projectDir, outputDir, testClassPath, (String[]) pathsToBinaries,
				coberturaDataFile);
	}

	@Override
	public String[] getElementsToAddToTestClassPathForMainTestRunner() {
		return null;
	}

	@Override
	public String[] getPropertiesForMainTestRunner(Path projectDir, boolean useSeparateJVM) {
		return new String[] { "-Dnet.sourceforge.cobertura.datafile=" + coberturaDataFile.getAbsolutePath().toString(),
				"-XX:+UseNUMA", "-XX:+UseConcMarkSweepGC", "-Xmx1024m" };
	}

	@Override
	public String[] getSpecificArgsForMainTestRunner() {
		return null;
	}

	@Override
	public AbstractRunSingleTestAndReportModule<ProjectData, CoberturaReportWrapper> getTestRunnerModule(
			OptionParser options, ClassLoader testAndInstrumentClassLoader, String testClassPath,
			StatisticsCollector<StatisticsData> statisticsContainer) {
		final Path projectDir = options.isDirectory(CmdOptions.PROJECT_DIR, true);
		final Path srcDir = options.isDirectory(projectDir, CmdOptions.SOURCE_DIR, true);
		final String outputDir = options.isDirectory(CmdOptions.OUTPUT, false).toString();
		
		File testrunnerJar = null;
		try {
			testrunnerJar = Java7TestRunnerJar.extractToTempLocation();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new CoberturaRunSingleTestAndReportModule(coberturaDataFile.toPath().toAbsolutePath(), outputDir,
				projectDir.toFile(), srcDir.toString(), options.hasOption(CmdOptions.FULL_SPECTRA),
				RunTestsAndGenSpectraProcessor.TEST_DEBUG_OUTPUT,
				options.hasOption(CmdOptions.TIMEOUT) ? Long.valueOf(options.getOptionValue(CmdOptions.TIMEOUT)) : null,
				options.hasOption(CmdOptions.REPEAT_TESTS)
						? Integer.valueOf(options.getOptionValue(CmdOptions.REPEAT_TESTS)) : 1,
				testClassPath, options.getOptionValue(CmdOptions.JAVA_HOME_DIR, null),
//				RunTestsAndGenSpectraProcessor.class.getResource("/testrunner.jar").getPath(),
				testrunnerJar.getAbsolutePath(),
				options.hasOption(CmdOptions.SEPARATE_JVM), options.hasOption(CmdOptions.JAVA7),
				options.getOptionValueAsInt(CmdOptions.MAX_ERRORS, 0),
				options.getOptionValues(CmdOptions.FAILING_TESTS), statisticsContainer, testAndInstrumentClassLoader);
	}

	@Override
	public AbstractProcessor<CoberturaReportWrapper, ISpectra<SourceCodeBlock, ?>> getReportToSpectraProcessor(
			OptionParser options, StatisticsCollector<StatisticsData> statisticsContainer) {
		return new CoberturaAddReportToProviderAndGenerateSpectraModule(
				null/* outputDir + File.separator + "fail" */, options.hasOption(CmdOptions.FULL_SPECTRA),
				statisticsContainer);
	}

	@Override
	public AbstractConsumingProcessor<ISpectra<SourceCodeBlock, ?>> getSpectraProcessor(OptionParser options) {
		return new SpectraSaveProcessor(options);
	}
	
}
