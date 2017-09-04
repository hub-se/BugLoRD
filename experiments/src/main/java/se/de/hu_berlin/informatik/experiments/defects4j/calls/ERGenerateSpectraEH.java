/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.sbfl.spectra.cobertura.CoberturaToSpectra;
import se.de.hu_berlin.informatik.sbfl.spectra.jacoco.JaCoCoToSpectra;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.manipulation.FilterSpectraModule;
import se.de.hu_berlin.informatik.stardust.spectra.manipulation.SaveSpectraModule;
import se.de.hu_berlin.informatik.stardust.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.stardust.util.SpectraUtils;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.files.processors.SearchFileOrDirToListProcessor;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.module.ModuleLinker;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ERGenerateSpectraEH extends AbstractProcessor<BuggyFixedEntity<?>,BuggyFixedEntity<?>> {

	private String suffix;
	final private Integer port;

	/**
	 * @param suffix
	 * a suffix to append to the ranking directory (may be null)
	 * @param port
	 * the port to use for the JaCoCo Java agent
	 */
	public ERGenerateSpectraEH(String suffix, int port) {
		this.suffix = suffix;
		this.port = port;
	}

	private boolean tryToGetSpectraFromArchive(Entity entity) {
		File spectra = Paths.get(Defects4J.getValueOf(Defects4JProperties.SPECTRA_ARCHIVE_DIR), 
				Misc.replaceWhitespacesInString(entity.getUniqueIdentifier(), "_") + ".zip").toFile();
		if (!spectra.exists()) {
			return false;
		}
		
		File destination = new File(entity.getWorkDataDir() + Defects4J.SEP + BugLoRDConstants.SPECTRA_FILE_NAME);
		try {
			FileUtils.copyFileOrDir(spectra, destination, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Log.err(this, "Found spectra '%s', but could not copy to '%s'.", spectra, destination);
			return false;
		}
		
		// JaCoCo
		spectra = Paths.get(Defects4J.getValueOf(Defects4JProperties.SPECTRA_ARCHIVE_DIR),
				BugLoRDConstants.DIR_NAME_JACOCO,
				Misc.replaceWhitespacesInString(entity.getUniqueIdentifier(), "_") + ".zip").toFile();
		if (!spectra.exists()) {
			return false;
		}
		
		destination = new File(entity.getWorkDataDir() + Defects4J.SEP + 
				BugLoRDConstants.DIR_NAME_JACOCO + Defects4J.SEP + BugLoRDConstants.SPECTRA_FILE_NAME);
		try {
			FileUtils.copyFileOrDir(spectra, destination, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Log.err(this, "Found spectra '%s', but could not copy to '%s'.", spectra, destination);
			return false;
		}
		
		// Cobertura
		spectra = Paths.get(Defects4J.getValueOf(Defects4JProperties.SPECTRA_ARCHIVE_DIR),
				BugLoRDConstants.DIR_NAME_COBERTURA,
				Misc.replaceWhitespacesInString(entity.getUniqueIdentifier(), "_") + ".zip").toFile();
		if (!spectra.exists()) {
			return false;
		}

		destination = new File(entity.getWorkDataDir() + Defects4J.SEP + 
				BugLoRDConstants.DIR_NAME_COBERTURA + Defects4J.SEP + BugLoRDConstants.SPECTRA_FILE_NAME);
		try {
			FileUtils.copyFileOrDir(spectra, destination, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Log.err(this, "Found spectra '%s', but could not copy to '%s'.", spectra, destination);
			return false;
		}
		
		return true;
	}
	
	private boolean tryToGetFilteredSpectraFromArchive(Entity entity) {
		File spectra = Paths.get(Defects4J.getValueOf(Defects4JProperties.SPECTRA_ARCHIVE_DIR), 
				Misc.replaceWhitespacesInString(entity.getUniqueIdentifier(), "_") + "_filtered.zip").toFile();
		if (!spectra.exists()) {
			return false;
		}
		
		File destination = new File(entity.getWorkDataDir() + Defects4J.SEP + BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME);
		try {
			FileUtils.copyFileOrDir(spectra, destination, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Log.err(this, "Found filtered spectra '%s', but could not copy to '%s'.", spectra, destination);
			return false;
		}
		
		// JaCoCo
		spectra = Paths.get(Defects4J.getValueOf(Defects4JProperties.SPECTRA_ARCHIVE_DIR),
				BugLoRDConstants.DIR_NAME_JACOCO,
				Misc.replaceWhitespacesInString(entity.getUniqueIdentifier(), "_") + "_filtered.zip").toFile();
		if (!spectra.exists()) {
			return false;
		}
		
		destination = new File(entity.getWorkDataDir() + Defects4J.SEP + 
				BugLoRDConstants.DIR_NAME_JACOCO + Defects4J.SEP + BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME);
		try {
			FileUtils.copyFileOrDir(spectra, destination, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Log.err(this, "Found filtered spectra '%s', but could not copy to '%s'.", spectra, destination);
			return false;
		}
		
		// Cobertura
		spectra = Paths.get(Defects4J.getValueOf(Defects4JProperties.SPECTRA_ARCHIVE_DIR),
				BugLoRDConstants.DIR_NAME_COBERTURA,
				Misc.replaceWhitespacesInString(entity.getUniqueIdentifier(), "_") + "_filtered.zip").toFile();
		if (!spectra.exists()) {
			return false;
		}

		destination = new File(entity.getWorkDataDir() + Defects4J.SEP + 
				BugLoRDConstants.DIR_NAME_COBERTURA + Defects4J.SEP + BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME);
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
		ISpectra<SourceCodeBlock> spectra = SpectraFileUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, spectraFile);
		
		Path destination = entity.getWorkDataDir().resolve(BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME);
		SpectraFileUtils.saveBlockSpectraToZipFile(
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
			
			// generate a spectra with cobertura
			Log.out(this, "%s: Generating spectra with Cobertura...", buggyEntity);
			ISpectra<SourceCodeBlock> majorityCoberturaSpectra = createMajoritySpectra(true, 1,
					buggyEntity, bug, buggyMainSrcDir, buggyMainBinDir, buggyTestBinDir, buggyTestCP, testClassesFile,
					rankingDir, failingTests);

			Path majorityCoberturaSpectraFile = null;
			if (majorityCoberturaSpectra != null) {
				// save the generated spectra while computing the spectras with JaCoCo...
				majorityCoberturaSpectraFile = rankingDir.resolve("majorityCoberturaSpectra.zip");
				new SaveSpectraModule<SourceCodeBlock>(SourceCodeBlock.DUMMY, majorityCoberturaSpectraFile)
				.submit(majorityCoberturaSpectra);
				// let the GC collect the spectra while computing a spectra with JaCoCo (possible RAM issues)
				majorityCoberturaSpectra = null;
			}

			// generate a spectra with jacoco
			Log.out(this, "%s: Generating spectra with JaCoCo...", buggyEntity);
			ISpectra<SourceCodeBlock> majorityJaCoCoSpectra = createMajoritySpectra(false, 1,
					buggyEntity, bug, buggyMainSrcDir, buggyMainBinDir, buggyTestBinDir, buggyTestCP, testClassesFile,
					rankingDir, failingTests);
			
			Path majorityJaCoCoSpectraFile = null;
			if (majorityJaCoCoSpectra != null) {
				// temporarily save the generated spectra
				majorityJaCoCoSpectraFile = rankingDir.resolve("majorityJaCoCoSpectra.zip");
				new SaveSpectraModule<SourceCodeBlock>(SourceCodeBlock.DUMMY, majorityJaCoCoSpectraFile)
				.submit(majorityJaCoCoSpectra);
			}

			Path mergedSpectraFile = null;
			Path mergedFilteredSpectraFile = null;
			if (majorityCoberturaSpectraFile != null && majorityJaCoCoSpectraFile != null) {
				// load both majority spectras into a list
				List<ISpectra<SourceCodeBlock>> generatedSpectras = new ArrayList<>();
				generatedSpectras.add(majorityJaCoCoSpectra);
				generatedSpectras.add(SpectraFileUtils.loadBlockSpectraFromZipFile(majorityCoberturaSpectraFile));


				// generate a merged spectra from both majority spectras
				Log.out(this, "%s: Merging spectra...", buggyEntity);
				ISpectra<SourceCodeBlock> mergedSpectra = SpectraUtils.mergeSpectras(generatedSpectras, true, true);
				majorityCoberturaSpectra = null;
				majorityJaCoCoSpectra = null;

				//generate these afterwards... maybe we need the original data later!?
				//			Log.out(this, "%s: Generating coherent spectra...", buggyEntity);
				//			mergedSpectra = new BuildCoherentSpectraModule().submit(mergedSpectra).getResult();

				Log.out(this, "%s: Saving merged spectra...", buggyEntity);
				mergedSpectraFile = rankingDir.resolve(BugLoRDConstants.SPECTRA_FILE_NAME);
				new SaveSpectraModule<SourceCodeBlock>(SourceCodeBlock.DUMMY, mergedSpectraFile)
				.submit(mergedSpectra);

				

				// save the merged trace and the filtered merged spectra
				mergedFilteredSpectraFile = rankingDir.resolve(BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME);
				new ModuleLinker().append(
//						new TraceFileModule<SourceCodeBlock>(rankingDir.toAbsolutePath().toString()),
						new FilterSpectraModule<SourceCodeBlock>(INode.CoverageType.EF_EQUALS_ZERO),
						new SaveSpectraModule<SourceCodeBlock>(SourceCodeBlock.DUMMY, mergedFilteredSpectraFile))
				.submit(mergedSpectra);

				mergedSpectra = null;

				
			}
			
			if (mergedSpectraFile == null || !mergedSpectraFile.toFile().exists()) {
				Log.err(this, "Spectra file doesn't exist: '" + mergedSpectraFile + "'.");
				Log.err(this, "Error while generating spectra. Skipping '" + buggyEntity + "'.");
				return null;
			}
			
			if (mergedFilteredSpectraFile == null || !mergedFilteredSpectraFile.toFile().exists()) {
				Log.err(this, "Spectra file doesn't exist: '" + mergedFilteredSpectraFile + "'.");
				Log.err(this, "Error while generating spectra. Skipping '" + buggyEntity + "'.");
				return null;
			}
			
			try {
				FileUtils.copyFileOrDir(
						majorityCoberturaSpectraFile.toFile(), 
						bug.getWorkDataDir().resolve(BugLoRDConstants.DIR_NAME_COBERTURA)
						.resolve(BugLoRDConstants.SPECTRA_FILE_NAME).toFile(), 
						StandardCopyOption.REPLACE_EXISTING);
				FileUtils.delete(majorityCoberturaSpectraFile);
				
				FileUtils.copyFileOrDir(
						majorityJaCoCoSpectraFile.toFile(), 
						bug.getWorkDataDir().resolve(BugLoRDConstants.DIR_NAME_JACOCO)
						.resolve(BugLoRDConstants.SPECTRA_FILE_NAME).toFile(), 
						StandardCopyOption.REPLACE_EXISTING);
				FileUtils.delete(majorityJaCoCoSpectraFile);
				
				FileUtils.copyFileOrDir(
						rankingDir.resolve(BugLoRDConstants.SPECTRA_FILE_NAME).toFile(), 
						bug.getWorkDataDir().resolve(BugLoRDConstants.SPECTRA_FILE_NAME).toFile(), 
						StandardCopyOption.REPLACE_EXISTING);
				FileUtils.delete(rankingDir.resolve(BugLoRDConstants.SPECTRA_FILE_NAME));
				
				FileUtils.copyFileOrDir(
						rankingDir.resolve(BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME).toFile(), 
						bug.getWorkDataDir().resolve(BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME).toFile(), 
						StandardCopyOption.REPLACE_EXISTING);
				FileUtils.delete(rankingDir.resolve(BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME));
				
				
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
				Log.err(this, e, "Could not copy the spectra to the data directory.");
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
			computeFilteredSpectraFromFoundSpectra(bug);
		}
		
//		/* #====================================================================================
//		 * # move to archive directory, in case it differs from the execution directory
//		 * #==================================================================================== */
//		buggyEntity.tryMovingExecutionDirToArchive();
//
//		buggyEntity.tryDeleteExecutionDirectory(false);
		
		return buggyEntity;
	}

	private ISpectra<SourceCodeBlock> createMajoritySpectra(boolean useCobertura, int iterations,
			BuggyFixedEntity<?> buggyEntity, Entity bug, String buggyMainSrcDir,
			String buggyMainBinDir, String buggyTestBinDir, String buggyTestCP, String testClassesFile,
			Path rankingDir, List<String> failingTests) {
		// generate the spectra 3 times and compare them afterwards to avoid false data...
		List<File> generatedSpectraFiles = new ArrayList<>();
//		List<File> generatedFilteredSpectraFiles = new ArrayList<>();
		for (int i = 0; i < iterations; ++i) {
			// 1200s == 20 minutes as test timeout should be reasonable!?
			// repeat tests 2 times to generate more correct coverage data!?
			Path uniqueRankingDir = null;
			if (useCobertura) {
				Log.out(this, "%s: Cobertura run %s...", buggyEntity, String.valueOf(i+1));
				uniqueRankingDir = rankingDir.resolve("cobertura_" + i);
				new CoberturaToSpectra.Builder()
				.setJavaHome(Defects4JProperties.JAVA7_HOME.getValue())
				.setProjectDir(bug.getWorkDir(true).toString())
				.setSourceDir(buggyMainSrcDir)
				.setTestClassDir(buggyTestBinDir)
				.setTestClassPath(buggyTestCP)
				.setPathsToBinaries(bug.getWorkDir(true).resolve(buggyMainBinDir).toString())
				.setOutputDir(uniqueRankingDir.toString())
				.setTestClassList(testClassesFile)
				.setFailingTests(failingTests)
				.useSeparateJVM(false)
				.setTimeout(1200L)
				.setTestRepeatCount(1)
				.setMaxErrors(2)
				.run();
//				CoberturaToSpectra.generateRankingForDefects4JElement(
////						Defects4JProperties.JAVA7_HOME.getValue(),
//						null,
//						bug.getWorkDir(true).toString(), buggyMainSrcDir, buggyTestBinDir, buggyTestCP, 
//						bug.getWorkDir(true).resolve(buggyMainBinDir).toString(), testClassesFile, 
//						uniqueRankingDir.toString(), 600L, 1, true, false);
			} else {
				Log.out(this, "%s: JaCoCo run %s...", buggyEntity, String.valueOf(i+1));
				uniqueRankingDir = rankingDir.resolve("jacoco_" + i);
				new JaCoCoToSpectra.Builder()
				.setAgentPort(port)
				.setJavaHome(Defects4JProperties.JAVA7_HOME.getValue())
				.setProjectDir(bug.getWorkDir(true).toString())
				.setSourceDir(buggyMainSrcDir)
				.setTestClassDir(buggyTestBinDir)
				.setTestClassPath(buggyTestCP)
				.setPathsToBinaries(bug.getWorkDir(true).resolve(buggyMainBinDir).toString())
				.setOutputDir(uniqueRankingDir.toString())
				.setTestClassList(testClassesFile)
				.setFailingTests(failingTests)
				.useSeparateJVM(false)
				.setTimeout(1200L)
				.setTestRepeatCount(1)
				.setMaxErrors(2)
				.run();
//				JaCoCoToSpectra.generateRankingForDefects4JElement(
////						Defects4JProperties.JAVA7_HOME.getValue(),
//						null,
//						bug.getWorkDir(true).toString(), buggyMainSrcDir, buggyTestBinDir, buggyTestCP, 
//						bug.getWorkDir(true).resolve(buggyMainBinDir).toString(), testClassesFile, 
//						uniqueRankingDir.toString(), port, 600L, 1, true, false);
			}

			File spectraFile = uniqueRankingDir.resolve(BugLoRDConstants.SPECTRA_FILE_NAME).toFile();
			if (spectraFile.exists()) {
				generatedSpectraFiles.add(spectraFile);
			} else {
				Log.err(this, "%s: Error generating spectra during run %s...", buggyEntity, String.valueOf(i+1));
			}
			
			
//			File filteredSpectraFile = createCopyOfSpectraFile(rankingDir, i, BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME);
//			if (filteredSpectraFile == null) {
//				Log.err(this, "Error while generating spectra. Skipping '" + buggyEntity + "'.");
//				return null;
//			}
//			generatedFilteredSpectraFiles.add(filteredSpectraFile);
		}
		
		// load the generated spectras and delete the respective files
		List<ISpectra<SourceCodeBlock>> generatedSpectras = new ArrayList<>();
		for (File spectraFile : generatedSpectraFiles) {
			generatedSpectras.add(SpectraFileUtils.loadBlockSpectraFromZipFile(spectraFile.toPath()));
			FileUtils.delete(spectraFile);
		}
		
//		// make sure that the spectras have the same amount of traces
//		int size = -1;
//		for (ISpectra<SourceCodeBlock> spectra : generatedSpectras) {
//			if (size < 0) {
//				size = spectra.getTraces().size();
//			} else {
//				if (size != spectra.getTraces().size()) {
//					Log.err(this, "Generated spectras have different number of traces. Skipping '" + buggyEntity + "'.");
//					return null;
//				}
//			}
//		}
		
		if (generatedSpectraFiles.size() > 0) {
			Log.out(this, "%s: Merging tool-specific spectra...", buggyEntity);
			return SpectraUtils.mergeSpectras(generatedSpectras, false, false);
		} else {
			return null;
		}
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

