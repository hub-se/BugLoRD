package se.de.hu_berlin.informatik.benchmark.api;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.changechecker.ChangeCheckerUtils;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.utils.files.processors.SearchFileOrDirProcessor;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.PipeLinker;

public abstract class AbstractBuggyFixedEntity<T extends Entity> implements BuggyFixedEntity<T> {

	private Map<String, List<ChangeWrapper>> changesMap = null;

	private T bug;
	private T fix;

	public AbstractBuggyFixedEntity(T bug, T fix) {
		this.bug = bug;
		this.fix = fix;
	}

	@Override
	public Map<String, List<ChangeWrapper>> getAllChanges(boolean executionModeBug, boolean resetBug,
			boolean deleteBugAfterwards, boolean executionModeFix, boolean resetFix, boolean deleteFixAfterwards) {
		if (changesMap == null) {
			changesMap = computeAllChanges(
					executionModeBug, resetBug, deleteBugAfterwards, executionModeFix, resetFix, deleteFixAfterwards);
		}
		return changesMap;
	}

	private Map<String, List<ChangeWrapper>> computeAllChanges(boolean executionModeBug, boolean resetBug,
			boolean deleteBugAfterwards, boolean executionModeFix, boolean resetFix, boolean deleteFixAfterwards) {
		T bug = getBuggyVersion();
		T fix = getFixedVersion();

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

		Map<String, List<ChangeWrapper>> map = new HashMap<>();

		new PipeLinker().append(
				new SearchFileOrDirProcessor("**/*.java").searchForFiles().relative(),
				new AbstractProcessor<Path, Object>() {

					@Override
					public Object processItem(Path path) {
						List<ChangeWrapper> changes = getChanges(path, bug, executionModeBug, fix, executionModeFix);
						if (changes == null || changes.isEmpty()) {
							return null;
						}
						ChangeCheckerUtils.removeChangesWithNoDeltaLines(changes);
						if (changes.isEmpty()) {
							Log.warn(this, "No Changes found: '%s'.", bug);
						} else {
							// String clazz = getClassFromJavaFile(path);
							map.put(changes.get(0).getClassName().replace('.', '/').concat(".java"), changes);
						}
						return null;
					}
				}).submitAndShutdown(bug.getWorkDir(executionModeBug).resolve(bug.getMainSourceDir(executionModeBug)));

		if (deleteBugAfterwards) {
			bug.deleteAllButData();
		}
		if (deleteFixAfterwards) {
			fix.deleteAllButData();
		}

		return map;
	}

	// private String getClassFromJavaFile(Path path) {
	// try {
	// String fileContent = FileUtils.readFile2String(path);
	// int pos = fileContent.
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return null;
	// }

	private List<ChangeWrapper> getChanges(Path path, T bug, boolean executionModeBug, T fix,
			boolean executionModeFix) {
		return ChangeCheckerUtils.checkForChanges(
				bug.getWorkDir(executionModeBug).resolve(bug.getMainSourceDir(executionModeBug)).resolve(path).toFile(),
				fix.getWorkDir(executionModeFix).resolve(fix.getMainSourceDir(executionModeFix)).resolve(path)
						.toFile(), false, false);
	}

	@Override
	public T getBuggyVersion() {
		return bug;
	}

	@Override
	public T getFixedVersion() {
		return fix;
	}

	@Override
	public String toString() {
		return getBuggyVersion().toString();
	}

}
