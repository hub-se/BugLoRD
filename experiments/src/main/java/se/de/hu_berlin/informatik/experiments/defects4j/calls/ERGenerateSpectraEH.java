/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.c2r.CoberturaToSpectra;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.manipulation.FilterSpectraModule;
import se.de.hu_berlin.informatik.stardust.util.SpectraUtils;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ERGenerateSpectraEH extends AbstractProcessor<BuggyFixedEntity,BuggyFixedEntity> {

	private String suffix;

	/**
	 * @param suffix
	 * a suffix to append to the ranking directory (may be null)
	 */
	public ERGenerateSpectraEH(String suffix) {
		this.suffix = suffix;
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
		return true;
	}
	
	private void computeFilteredSpectraFromFoundSpectra(Entity entity) {
		Path spectraFile = entity.getWorkDataDir().resolve(BugLoRDConstants.SPECTRA_FILE_NAME);
		ISpectra<SourceCodeBlock> spectra = SpectraUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, spectraFile);
		
		Path destination = entity.getWorkDataDir().resolve(BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME);
		SpectraUtils.saveBlockSpectraToZipFile(
				new FilterSpectraModule<SourceCodeBlock>().submit(spectra).getResult(),
				destination, true, true, true);
	}

	@Override
	public BuggyFixedEntity processItem(BuggyFixedEntity buggyEntity) {
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
			 * # generate coverage traces via cobertura and calculate rankings
			 * #==================================================================================== */
			String testClasses = Misc.listToString(bug.getTestClasses(true), System.lineSeparator(), "", "");

			String testClassesFile = bug.getWorkDataDir().resolve(BugLoRDConstants.FILENAME_TEST_CLASSES).toString();
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

			boolean useSeparateJVM = false;
			if (buggyEntity.toString().contains("Mockito")) {
				useSeparateJVM = true;
			}

			Path rankingDir = bug.getWorkDir(true).resolve(suffix == null ? 
					BugLoRDConstants.DIR_NAME_RANKING : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix);
			//TODO: 5 minutes as test timeout should be reasonable!?
			//TODO: repeat tests 2 times to generate more correct coverage data?
			CoberturaToSpectra.generateRankingForDefects4JElement(
//					Defects4JProperties.JAVA7_HOME.getValue(),
					null,
					bug.getWorkDir(true).toString(), buggyMainSrcDir, buggyTestBinDir, buggyTestCP, 
					bug.getWorkDir(true).resolve(buggyMainBinDir).toString(), testClassesFile, 
					rankingDir.toString(), 300L, 1, true, useSeparateJVM);
			
			Path rankingDirData = bug.getWorkDataDir().resolve(suffix == null ? 
					BugLoRDConstants.DIR_NAME_RANKING : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix);
			
			String compressedSpectraFile = rankingDir.resolve(BugLoRDConstants.SPECTRA_FILE_NAME).toString();
			if (!(new File(compressedSpectraFile)).exists()) {
				Log.err(this, "Spectra file doesn't exist: '" + compressedSpectraFile + "'.");
				Log.err(this, "Error while generating spectra. Skipping '" + buggyEntity + "'.");
				return null;
			}
			
			try {
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
				
				FileUtils.delete(rankingDir.resolve("cobertura.ser"));
				//delete old data directory
				FileUtils.delete(rankingDirData);
				FileUtils.copyFileOrDir(
						rankingDir.toFile(), 
						rankingDirData.toFile());
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

}

