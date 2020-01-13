package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.ToolSpecific;
import se.de.hu_berlin.informatik.gen.spectra.AbstractSpectraGenerator.AbstractBuilder;
import se.de.hu_berlin.informatik.gen.spectra.main.CoberturaSpectraGenerator;
import se.de.hu_berlin.informatik.gen.spectra.main.JaCoCoSpectraGenerator;
import se.de.hu_berlin.informatik.gen.spectra.main.TraceCoberturaSpectraGenerator;
import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.branch.ProgramBranchSpectra;
import se.de.hu_berlin.informatik.spectra.core.branch.StatementSpectraToBranchSpectra;
import se.de.hu_berlin.informatik.spectra.core.manipulation.BuildCoherentSpectraModule;
import se.de.hu_berlin.informatik.spectra.core.manipulation.FilterSpectraModule;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.files.processors.SearchFileOrDirToListProcessor;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ERGenerateSpectraEH extends AbstractProcessor<BuggyFixedEntity<?>,BuggyFixedEntity<?>> {

	private String suffix;
	final private int port;
	private final ToolSpecific toolSpecific;
	private String subDirName;
	private boolean condenseNodes;

	/**
	 * @param toolSpecific
	 * chooses what kind of tool to use to generate the spectra
	 * @param suffix
	 * a suffix to append to the ranking directory (may be null)
	 * @param port
	 * the port to use for the JaCoCo Java agent
	 * @param condense
	 * whether to fill up empty lines between statements
	 */
	public ERGenerateSpectraEH(ToolSpecific toolSpecific, String suffix, int port, boolean condense) {
		this.toolSpecific = toolSpecific;
		this.condenseNodes = condense;
		switch (toolSpecific) {
		case COBERTURA:
			subDirName = BugLoRDConstants.DIR_NAME_COBERTURA;
			break;
		case JACOCO:
			subDirName = BugLoRDConstants.DIR_NAME_JACOCO;
			break;
		case TRACE_COBERTURA:
			subDirName = BugLoRDConstants.DIR_NAME_TRACE_COBERTURA;
			break;

			case  BRANCH_SPECTRA:
				subDirName = BugLoRDConstants.DIR_NAME_BRANCH_SPECTRA;
				break;

		default:
			throw new IllegalStateException("Spectra Generation Tool unknown.");
		}
		this.suffix = suffix;
		this.port = port;
	}

	private boolean tryToGetSpectraFromArchive(Entity entity) {
		File spectra;
		File destination;

		spectra = Paths.get(Defects4J.getValueOf(Defects4JProperties.SPECTRA_ARCHIVE_DIR),
				subDirName, Misc.replaceWhitespacesInString(entity.getUniqueIdentifier(), "_") + ".zip").toFile();
		if (!spectra.exists()) {
			return false;
		}

		destination = new File(entity.getWorkDataDir() + Defects4J.SEP +
				subDirName + Defects4J.SEP + BugLoRDConstants.SPECTRA_FILE_NAME);
		try {
			FileUtils.copyFileOrDir(spectra, destination, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Log.err(this, "Found spectra '%s', but could not copy to '%s'.", spectra, destination);
			return false;
		}

		return true;
	}
	
	private boolean tryToGetFilteredSpectraFromArchive(Entity entity) {
		File spectra;
		File destination;
		
		spectra = Paths.get(Defects4J.getValueOf(Defects4JProperties.SPECTRA_ARCHIVE_DIR),
				subDirName, Misc.replaceWhitespacesInString(entity.getUniqueIdentifier(), "_") + "_filtered.zip").toFile();
		if (!spectra.exists()) {
			return false;
		}

		destination = new File(entity.getWorkDataDir() + Defects4J.SEP + 
				subDirName + Defects4J.SEP + BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME);
		try {
			FileUtils.copyFileOrDir(spectra, destination, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Log.err(this, "Found filtered spectra '%s', but could not copy to '%s'.", spectra, destination);
			return false;
		}
		
		return true;
	}
	
	private void computeFilteredSpectraFromFoundSpectra(Entity entity) {
		Path spectraFile = entity.getWorkDataDir().resolve(BugLoRDConstants.SPECTRA_FILE_NAME);
		ISpectra<SourceCodeBlock, ?> spectra = SpectraFileUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, spectraFile);
		
		Path destination = entity.getWorkDataDir().resolve(BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME);
		SpectraFileUtils.saveSpectraToZipFile(
				new FilterSpectraModule<SourceCodeBlock>(INode.CoverageType.EF_EQUALS_ZERO).submit(spectra).getResult(),
				destination, true, true, true);
	}

	@Override
	public BuggyFixedEntity<?> processItem(BuggyFixedEntity<?> buggyEntity) {
		Log.out(this, "Processing %s.", buggyEntity);
		
		Entity bug = buggyEntity.getBuggyVersion();

		/* #====================================================================================
		 * # try to get spectra from archive, if existing
		 * #==================================================================================== */
		boolean foundSpectra = tryToGetSpectraFromArchive(bug);
		boolean foundFilteredSpectra = tryToGetFilteredSpectraFromArchive(bug);

		/* #====================================================================================
		 * # if not found a spectra, then run all the tests and build a new one
		 * #==================================================================================== */
		if (!foundSpectra) {
			if (toolSpecific.equals(ToolSpecific.BRANCH_SPECTRA)) {
				File spectraFile = Paths.get(Defects4J.getValueOf(Defects4JProperties.SPECTRA_ARCHIVE_DIR),
						BugLoRDConstants.DIR_NAME_TRACE_COBERTURA,
						Misc.replaceWhitespacesInString(bug.getUniqueIdentifier(), "_") + ".zip").toFile();
				if (!spectraFile.exists()) {
					Log.err(this, "Error while generating spectra. Skipping '" + buggyEntity + "'.");
					return null;
				}

				Path traceSpectraDestination = bug.getWorkDataDir().resolve(BugLoRDConstants.DIR_NAME_TRACE_COBERTURA)
						.resolve(BugLoRDConstants.SPECTRA_FILE_NAME);
				try {
					FileUtils.copyFileOrDir(spectraFile, traceSpectraDestination.toFile(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					Log.err(this, "Found spectra '%s', but could not copy to '%s'.", spectraFile, traceSpectraDestination);
					return null;
				}

				ISpectra<SourceCodeBlock, ?> spectra = SpectraFileUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY,
						traceSpectraDestination.toAbsolutePath());
				
				// fill up empty lines in between statements?
				if (condenseNodes) {
					new BuildCoherentSpectraModule().submit(spectra);
				}

				Path destination = bug.getWorkDataDir().resolve(subDirName)
						.resolve(BugLoRDConstants.SPECTRA_FILE_NAME);
				ProgramBranchSpectra programBranchSpectra = StatementSpectraToBranchSpectra
						.generateBranchingSpectraFromStatementSpectra(spectra, destination.toAbsolutePath().toString());

				
				SpectraFileUtils.saveSpectraToZipFile(programBranchSpectra, destination,
						true, true, true);

				return buggyEntity;
			}

			/* #====================================================================================
			 * # checkout buggy version if necessary
			 * #==================================================================================== */
			buggyEntity.requireBug(true);
			
			/* #====================================================================================
			 * # collect paths
			 * #==================================================================================== */
			String buggyMainSrcDir = bug.getMainSourceDir(true).toString();
			String buggyMainBinDir = bug.getMainBinDir(true).toString();
			String buggyTestBinDir = bug.getTestBinDir(true).toString();
			String buggyTestCP = bug.getTestClassPath(true);

			/* #====================================================================================
			 * # compile buggy version
			 * #==================================================================================== */
			bug.compile(true);

			/* #====================================================================================
			 * # generate coverage traces and calculate spectra
			 * #==================================================================================== */
			String testClasses = Misc.listToString(bug.getTestClasses(true), System.lineSeparator(), "", "");

			String testClassesFile = bug.getWorkDir(true).resolve(BugLoRDConstants.FILENAME_TEST_CLASSES).toString();
			FileUtils.delete(new File(testClassesFile));
			try {
				FileUtils.writeString2File(testClasses, new File(testClassesFile));
			} catch (IOException e) {
				Log.err(this, "IOException while trying to write to file '%s'.", testClassesFile);
				Log.err(this, "Error while checking out or generating rankings. Skipping '"
						+ buggyEntity + "'.");
				bug.tryDeleteExecutionDirectory(false);
				return null;
			}
			
			List<String> failingTests = bug.getFailingTests(true);

//			boolean useSeparateJVM = false;
//			if (buggyEntity.toString().contains("Mockito")) {
//				useSeparateJVM = true;
//			}

			Path rankingDir = bug.getWorkDir(true).resolve(suffix == null ? 
					BugLoRDConstants.DIR_NAME_RANKING : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix);
			Path statsDirData = bug.getWorkDataDir().resolve(suffix == null ? 
					BugLoRDConstants.DIR_NAME_STATS : BugLoRDConstants.DIR_NAME_STATS + "_" + suffix);
			
			// generate tool specific spectra
			createMajoritySpectra(1, buggyEntity, bug, buggyMainSrcDir, buggyMainBinDir, 
					buggyTestBinDir, buggyTestCP, testClassesFile,
					rankingDir.resolve(subDirName), failingTests);

			
			
			File spectraFile = rankingDir.resolve(subDirName)
			.resolve(BugLoRDConstants.SPECTRA_FILE_NAME).toFile();
			File spectraFileFiltered = rankingDir.resolve(subDirName)
					.resolve(BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME).toFile();
			
			if (!spectraFile.exists()) {
				Log.err(this, "Error while generating spectra. Skipping '" + buggyEntity + "'.");
				return null;
			}
			
			try {
				FileUtils.copyFileOrDir(
						spectraFile, 
						bug.getWorkDataDir().resolve(subDirName)
						.resolve(BugLoRDConstants.SPECTRA_FILE_NAME).toFile(), 
						StandardCopyOption.REPLACE_EXISTING);
				FileUtils.delete(spectraFile);
			} catch (IOException e) {
				Log.err(this, e, "Could not copy the spectra to the data directory.");
			}
			
			if (spectraFileFiltered.exists()) {
				try {
					FileUtils.copyFileOrDir(
							spectraFileFiltered, 
							bug.getWorkDataDir().resolve(subDirName)
							.resolve(BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME).toFile(), 
							StandardCopyOption.REPLACE_EXISTING);
					FileUtils.delete(spectraFileFiltered);
				} catch (IOException e) {
					Log.err(this, e, "Could not copy the filtered spectra to the data directory.");
				}
			} else {
				Log.warn(this, "Filtered spectra file does not exist.");
			}
			
			try {
				List<Path> result = new SearchFileOrDirToListProcessor("**cobertura.ser", true)
						.searchForFiles().submit(rankingDir).getResult();
				for (Path file : result) {
					FileUtils.delete(file);
				}
				List<Path> result2 = new SearchFileOrDirToListProcessor("**" + BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME, true)
						.searchForFiles().submit(rankingDir).getResult();
				for (Path file : result2) {
					FileUtils.delete(file);
				}
				List<Path> result3 = new SearchFileOrDirToListProcessor("**instrumented", true)
						.searchForDirectories().skipSubTreeAfterMatch().submit(rankingDir).getResult();
				for (Path dir : result3) {
					FileUtils.delete(dir);
				}
				
//				FileUtils.delete(rankingDir.resolve(BugLoRDConstants.FILENAME_TRACE_FILE));
				//delete old stats data directory
				FileUtils.delete(statsDirData);
				FileUtils.copyFileOrDir(
						rankingDir.toFile(), 
						statsDirData.toFile());
//				FileUtils.delete(rankingDir.resolve(BugLoRDConstants.SPECTRA_FILE_NAME));
			} catch (IOException e) {
				Log.err(this, e, "Could not clean up...");
			}
			
//			try {
//				FileUtils.copyFileOrDir(
//						rankingDir.resolve(BugLoRDConstants.FILENAME_TRACE_FILE).toFile(), 
//						rankingDirData.resolve(BugLoRDConstants.FILENAME_TRACE_FILE).toFile());
//				FileUtils.delete(rankingDir.resolve(BugLoRDConstants.FILENAME_TRACE_FILE));
//			} catch (IOException e) {
//				Log.err(this, e, "Could not copy the trace file to the data directory.");
//			}
			
			/* #====================================================================================
			 * # clean up unnecessary directories (doc files, svn/git files, binary classes)
			 * #==================================================================================== */
			bug.removeUnnecessaryFiles(true);

		} else if (!foundFilteredSpectra) {
//			computeFilteredSpectraFromFoundSpectra(bug);
			Log.warn(this, "Filtered spectra file does not exist.");
		}
		
//		/* #====================================================================================
//		 * # move to archive directory, in case it differs from the execution directory
//		 * #==================================================================================== */
//		buggyEntity.tryMovingExecutionDirToArchive();
//
//		buggyEntity.tryDeleteExecutionDirectory(false);
		
		return buggyEntity;
	}

	private void createMajoritySpectra(int iterations,
			BuggyFixedEntity<?> buggyEntity, Entity bug, String buggyMainSrcDir,
			String buggyMainBinDir, String buggyTestBinDir, String buggyTestCP, String testClassesFile,
			Path rankingDir, List<String> failingTests) {
		// 1200s == 20 minutes as test timeout should be reasonable!?
		// repeat tests 2 times to generate more correct coverage data!?
		AbstractBuilder builder;
		switch (toolSpecific) {
		case COBERTURA:
			Log.out(this, "%s: Generating spectra with Cobertura...", buggyEntity);
			builder = new CoberturaSpectraGenerator.Builder();
//			if (bug.getUniqueIdentifier().contains("Mockito")) {
//			builder.useJava7only(true);
//			}
			break;
		case JACOCO:
			Log.out(this, "%s: Generating spectra with JaCoCo...", buggyEntity);
			builder = new JaCoCoSpectraGenerator.Builder()
					.setAgentPort(port);
			break;
		case TRACE_COBERTURA:
			Log.out(this, "%s: Generating spectra with Trace Cobertura...", buggyEntity);
			builder = new TraceCoberturaSpectraGenerator.Builder();
//			if (bug.getUniqueIdentifier().contains("Mockito")) {
//			builder.useJava7only(true);
//			}
			break;
		default:
			throw new IllegalStateException("No implementation for the given tool was set!");
		}

		builder
		.setJavaHome(Defects4JProperties.JAVA7_HOME.getValue())
		.setProjectDir(bug.getWorkDir(true).toString())
		.setSourceDir(buggyMainSrcDir)
		.setTestClassDir(buggyTestBinDir)
		.setTestClassPath(buggyTestCP)
		.setPathsToBinaries(bug.getWorkDir(true).resolve(buggyMainBinDir).toString())
		.setOutputDir(rankingDir.toString())
		.setTestClassList(testClassesFile)
		.setFailingTests(failingTests)
//		.useSeparateJVM(true)
		.useJava7only(Boolean.valueOf(Defects4JProperties.ALWAYS_USE_JAVA7.getValue()))
//		.setTimeout(5000L)
		.setCondenseNodes(condenseNodes)
		//~139h
		.setTimeout(500000L)
		.setTestRepeatCount(1)
		.setMaxErrors(0);

		long startTime = new Date().getTime();
		
		builder
		.run();

		File spectraFile = rankingDir.resolve(BugLoRDConstants.SPECTRA_FILE_NAME).toFile();
		if (!spectraFile.exists()) {
			Log.err(this, "%s: Error generating spectra...", buggyEntity);
			return;
		} else {
			Log.out(this, "%s: Generating spectra was successful!", buggyEntity);
		}
		long endTime = new Date().getTime();
		
		Log.out(this, "%s: Execution time: %s", buggyEntity, Misc.getFormattedTimerString(endTime - startTime));
		
		// filtering is already done beforehand
//		Log.out(this, "%s: Reloading spectra for filtering...", buggyEntity);
//		ISpectra<SourceCodeBlock, ?> spectra = SpectraFileUtils.loadBlockSpectraFromZipFile(spectraFile.toPath());
//		
//		Log.out(this, "%s: Filtering out irrelevant nodes...", buggyEntity);
//		// save the merged trace and the filtered merged spectra
//		Path filteredSpectraFile = rankingDir.resolve(BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME);
//		new ModuleLinker().append(
////				new TraceFileModule<SourceCodeBlock>(rankingDir.toAbsolutePath().toString()),
//				new FilterSpectraModule<SourceCodeBlock>(INode.CoverageType.EF_EQUALS_ZERO),
//				new SaveSpectraModule<SourceCodeBlock>(SourceCodeBlock.DUMMY, filteredSpectraFile))
//		.submit(spectra);
//		Log.out(this, "%s: Filtering spectra done!", buggyEntity);
	}

//	public File createCopyOfSpectraFile(Path rankingDir, int i, String spectraFileName) {
//		String compressedSpectraFile = rankingDir.resolve(spectraFileName).toString();
//		File spectraFile = new File(compressedSpectraFile);
//		if (!spectraFile.exists()) {
//			Log.err(this, "Spectra file doesn't exist: '" + compressedSpectraFile + "'.");
//			return null;
//		}
//		
//		File target = rankingDir.resolve("coberturaSpectra_" + spectraFileName + "_" + i + ".zip").toFile();
//		try {
//			FileUtils.copyFileOrDir(spectraFile, target, 
//					StandardCopyOption.REPLACE_EXISTING);
//		} catch (IOException e) {
//			Log.err(this, e, "Could not copy spectra file '%s'.", compressedSpectraFile);
//			return null;
//		}
//		
//		// delete the old file
//		FileUtils.delete(spectraFile);
//		return target;
//	}

}

