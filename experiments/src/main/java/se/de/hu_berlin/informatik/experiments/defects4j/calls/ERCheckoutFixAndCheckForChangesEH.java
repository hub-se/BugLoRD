/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ERCheckoutFixAndCheckForChangesEH extends AbstractProcessor<BuggyFixedEntity<?>,BuggyFixedEntity<?>> {
	
	private boolean tryToGetChangesFromArchive(BuggyFixedEntity<?> input) {
		Entity bug = input.getBuggyVersion();
		File changesFile = Paths.get(Defects4J.getValueOf(Defects4JProperties.CHANGES_ARCHIVE_DIR), 
				Misc.replaceWhitespacesInString(bug.getUniqueIdentifier(), "_") + ".changes").toFile();
		if (!changesFile.exists()) {
			return false;
		}
		
		
		Map<String, List<ChangeWrapper>> changes = ChangeWrapper.readChangesFromFile(changesFile.toPath());

		if (changes == null) {
			Log.err(this, "Found changes file '%s', but could not load changes.", changesFile);
			return false;
		}
		
		Path destination = bug.getWorkDataDir().resolve(BugLoRDConstants.CHANGES_FILE_NAME);
		ChangeWrapper.storeChanges(changes, destination);
			
		return true;
	}
	
	private boolean tryToGetChangesHumanFromArchive(BuggyFixedEntity<?> input) {
		Entity bug = input.getBuggyVersion();
		File changesFile = Paths.get(Defects4J.getValueOf(Defects4JProperties.CHANGES_ARCHIVE_DIR), 
				Misc.replaceWhitespacesInString(bug.getUniqueIdentifier(), "_") + ".changes_human").toFile();
		if (!changesFile.exists()) {
			return false;
		}
		
		File destination = bug.getWorkDataDir().resolve(BugLoRDConstants.CHANGES_FILE_NAME_HUMAN).toFile();
		try {
			FileUtils.copyFileOrDir(changesFile, destination, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Log.err(this, "Found changes file '%s', but could not copy to '%s'.", changesFile, destination);
			return false;
		}
		return true;
	}

	@Override
	public BuggyFixedEntity<?> processItem(BuggyFixedEntity<?> buggyEntity) {
		Log.out(this, "Processing %s.", buggyEntity);
		
		/* #====================================================================================
		 * # try to get changes from archive, if existing
		 * #==================================================================================== */
		boolean foundChanges = tryToGetChangesFromArchive(buggyEntity);
		//human readable changes are not as important...
		tryToGetChangesHumanFromArchive(buggyEntity);
		
		/* #====================================================================================
		 * # if not found a changes file, then generate a new one
		 * #==================================================================================== */
		if (!foundChanges) {
			boolean bugExisted = buggyEntity.requireBug(true);
			boolean fixExisted = buggyEntity.requireFix(true);

			buggyEntity.getAndSaveAllChangesToFile(true, false, false, true, false, false);

			if (!bugExisted) {
				buggyEntity.getBuggyVersion().deleteAllButData();
			}

			if (!fixExisted) {
				buggyEntity.getFixedVersion().deleteAllButData();
			}
		}
		
		return buggyEntity;
	}

}

