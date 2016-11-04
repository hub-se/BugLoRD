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
	public Map<String, List<ChangeWrapper>> getAllChanges(
			boolean executionModeBug, boolean resetBug, boolean deleteBugAfterwards,
			boolean executionModeFix, boolean resetFix, boolean deleteFixAfterwards) {
		if (changesMap == null) {
			changesMap = computeAllChanges(
					executionModeBug, resetBug, deleteBugAfterwards, 
					executionModeFix, resetFix, deleteFixAfterwards);
		}
		return changesMap;
	}
	
	private Map<String, List<ChangeWrapper>> computeAllChanges(
			boolean executionModeBug, boolean resetBug, boolean deleteBugAfterwards,
			boolean executionModeFix, boolean resetFix, boolean deleteFixAfterwards) {
		BuggyFixedEntity bug = getBuggyVersion();
		BuggyFixedEntity fix = getFixedVersion();
		
		if (resetBug) {
			if (!bug.resetAndInitialize(executionModeBug, true)) {
				Log.err(this, "Could not initialize buggy version: '%s'.", bug);
				return null;
			}
		}
		if (resetFix) {
			if (!fix.resetAndInitialize(executionModeFix, true)) {
				Log.err(this, "Could not initialize fixed version: '%s'.", fix);
				return null;
			}
		}
		
		List<Path> allPaths = new SearchForFilesOrDirsModule("**/*.java", true)
				.searchForFiles()
				.relative()
				.submit(bug.getWorkDir(executionModeBug).resolve(bug.getMainSourceDir(executionModeBug)))
				.getResult();
		
		Map<String, List<ChangeWrapper>> map = new HashMap<>();
		for (Path path : allPaths) {
			List<ChangeWrapper> changes = getChanges(path, bug, executionModeBug, fix, executionModeFix);
			if (changes == null || changes.isEmpty()) {
				continue;
			}
//			String clazz = getClassFromJavaFile(path);
			map.put(changes.get(0).getClassName().replace('.', '/').concat(".java"), changes);
		}
		
		if (deleteBugAfterwards) {
			bug.deleteAllButData();
		}
		if (deleteFixAfterwards) {
			fix.deleteAllButData();
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

	private List<ChangeWrapper> getChanges(Path path, BuggyFixedEntity bug, boolean executionModeBug, BuggyFixedEntity fix, boolean executionModeFix) {
		return ChangeChecker.checkForChanges(
				bug.getWorkDir(executionModeBug).resolve(bug.getMainSourceDir(executionModeBug)).resolve(path).toFile(), 
				fix.getWorkDir(executionModeFix).resolve(fix.getMainSourceDir(executionModeFix)).resolve(path).toFile());
	}
	
}
