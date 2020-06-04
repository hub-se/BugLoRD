package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.ToolSpecific;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.branch.ProgramBranch;
import se.de.hu_berlin.informatik.spectra.core.manipulation.BuildCoherentSpectraModule;
import se.de.hu_berlin.informatik.spectra.core.manipulation.SaveSpectraModule;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Runs a single experiment.
 *
 * @author Simon Heiden
 */
public class ERLoadAndSaveSpectraEH extends AbstractProcessor<BuggyFixedEntity<?>, BuggyFixedEntity<?>> {

    private final ToolSpecific toolSpecific;
    private String subDirName;
    private boolean fillEmptyLines;

    /**
     * @param toolSpecific   chooses what kind of tool to use to generate the spectra
     * @param fillEmptyLines whether to fill up empty lines between statements
     */
    public ERLoadAndSaveSpectraEH(ToolSpecific toolSpecific, boolean fillEmptyLines) {
        this.toolSpecific = toolSpecific;
        this.fillEmptyLines = fillEmptyLines;
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

            case BRANCH_SPECTRA:
                subDirName = BugLoRDConstants.DIR_NAME_BRANCH_SPECTRA;
                break;

            default:
                throw new IllegalStateException("Spectra Generation Tool unknown.");
        }
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

    @Override
    public BuggyFixedEntity<?> processItem(BuggyFixedEntity<?> buggyEntity) {
        Log.out(this, "Processing %s.", buggyEntity);

        Entity bug = buggyEntity.getBuggyVersion();

        /* #====================================================================================
         * # try to get spectra from archive, if existing
         * #==================================================================================== */
        boolean foundSpectra = tryToGetSpectraFromArchive(bug);
        //        boolean foundFilteredSpectra = tryToGetFilteredSpectraFromArchive(bug);

        /* #====================================================================================
         * # if not found a spectra, then run all the tests and build a new one
         * #==================================================================================== */
        if (foundSpectra) {

        	File spectraFile = new File(bug.getWorkDataDir() + Defects4J.SEP +
        			subDirName + Defects4J.SEP + BugLoRDConstants.SPECTRA_FILE_NAME);
        	if (!spectraFile.exists()) {
        		Log.err(this, "Error while loading spectra. Skipping '" + buggyEntity + "'.");
        		return null;
        	}
        	
        	// copy spectra file to execution directory for faster loading/saving...
        	Path spectraDestination = bug.getWorkDir(true).resolve(subDirName)
                    .resolve(BugLoRDConstants.SPECTRA_FILE_NAME).toAbsolutePath();
            try {
                FileUtils.copyFileOrDir(spectraFile, spectraDestination.toFile(), StandardCopyOption.REPLACE_EXISTING);
                Log.out(this, "Copied spectra '%s' to '%s'.", spectraFile, spectraDestination);
            } catch (IOException e) {
                Log.err(this, "Found spectra '%s', but could not copy to '%s'.", spectraFile, spectraDestination);
                return null;
            }

        	ISpectra<?, ?> spectra;
        	if (toolSpecific.equals(ToolSpecific.BRANCH_SPECTRA)) {
        		spectra = SpectraFileUtils.loadSpectraFromZipFile(ProgramBranch.DUMMY,
        				spectraDestination.toAbsolutePath());
        	} else {
        		spectra = SpectraFileUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY,
        				spectraDestination.toAbsolutePath());	
			}

        	// fill up empty lines in between statements?
        	if (fillEmptyLines) {
        		new BuildCoherentSpectraModule().submit(spectra);
        	}

        	new SaveSpectraModule<>(spectraDestination.toAbsolutePath()).submit(spectra);

        	try {
                FileUtils.copyFileOrDir(spectraDestination.toFile(), spectraFile, StandardCopyOption.REPLACE_EXISTING);
                Log.out(this, "Copied spectra '%s' to '%s'.", spectraDestination, spectraFile);
            } catch (IOException e) {
                Log.err(this, "Found spectra '%s', but could not copy to '%s'.", spectraDestination, spectraFile);
                return null;
            }
        	
        	FileUtils.delete(spectraDestination);
        	
        	return buggyEntity;
        } else {
        	Log.warn(this, "Spectra file does not exist.");
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

