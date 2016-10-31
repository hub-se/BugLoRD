package se.de.hu_berlin.informatik.benchmark.api;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.changechecker.ChangeChecker;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.utils.fileoperations.SearchForFilesOrDirsModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public abstract class AbstractBuggyFixedEntity extends AbstractEntity implements BuggyFixedEntity {
	
	private Map<String, List<ChangeWrapper>> changesMap = null;
	
	public AbstractBuggyFixedEntity(DirectoryProvider directoryProvider) {
		super(directoryProvider);
	}

	@Override
	public Map<String, List<ChangeWrapper>> getAllChanges() {
		if (changesMap == null) {
			changesMap = computeAllChanges();
		}
		return changesMap;
	}
	
	private Map<String, List<ChangeWrapper>> computeAllChanges() {
		BuggyFixedEntity bug = getBuggyVersion();
		BuggyFixedEntity fix = getFixedVersion();
		boolean bugWasInitialized = bug.isInitialized();
		boolean fixWasInitialized = fix.isInitialized();
		if (!bugWasInitialized) {
			if (!bug.resetAndInitialize(true)) {
				Log.err(this, "Could not initialize buggy version: '%s'.", bug);
				return null;
			}
		}
		if (!fixWasInitialized) {
			if (!fix.resetAndInitialize(true)) {
				Log.err(this, "Could not initialize fixed version: '%s'.", fix);
				return null;
			}
		}
		
		List<Path> allPaths = new SearchForFilesOrDirsModule("**/*.java", true)
				.searchForFiles()
				.relative()
				.submit(bug.getWorkDir().resolve(bug.getMainSourceDir()))
				.getResult();
		
		Map<String, List<ChangeWrapper>> map = new HashMap<>();
		for (Path path : allPaths) {
			List<ChangeWrapper> changes = getChanges(path, bug, fix);
			if (changes == null || changes.isEmpty()) {
				continue;
			}
//			String clazz = getClassFromJavaFile(path);
			map.put(changes.get(0).getClassName(), changes);
		}
		
		if (!bugWasInitialized) {
			bug.deleteAll();
		}
		if (!fixWasInitialized) {
			fix.deleteAll();
		}
		
		return map;
	}

//	private String getClassFromJavaFile(Path path) {
//		try {
//			String fileContent = FileUtils.readFile2String(path);
//			int pos = fileContent.
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}

	private List<ChangeWrapper> getChanges(Path path, BuggyFixedEntity bug, BuggyFixedEntity fix) {
		return ChangeChecker.checkForChanges(
				bug.getWorkDir().resolve(bug.getMainSourceDir()).resolve(path).toFile(), 
				fix.getWorkDir().resolve(fix.getMainSourceDir()).resolve(path).toFile());
	}
	
}
