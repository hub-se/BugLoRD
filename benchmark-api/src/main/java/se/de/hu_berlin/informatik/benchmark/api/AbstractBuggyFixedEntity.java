package se.de.hu_berlin.informatik.benchmark.api;

import se.de.hu_berlin.informatik.benchmark.modification.Modification;
import se.de.hu_berlin.informatik.changechecker.ChangeCheckerUtils;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.files.processors.SearchFileOrDirProcessor;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.PipeLinker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractBuggyFixedEntity<T extends Entity> implements BuggyFixedEntity<T> {

    protected Map<String, List<Modification>> changesMap = null;

    private final T bug;
    private final T fix;

    public AbstractBuggyFixedEntity(T bug, T fix) {
        this.bug = bug;
        this.fix = fix;
    }

    @Override
    public Map<String, List<Modification>> getAllChanges(boolean executionModeBug, boolean resetBug,
                                                         boolean deleteBugAfterwards, boolean executionModeFix, boolean resetFix, boolean deleteFixAfterwards) {
        if (changesMap == null) {
            changesMap = computeAllChanges(
                    executionModeBug, resetBug, deleteBugAfterwards, executionModeFix, resetFix, deleteFixAfterwards);
        }
        return changesMap;
    }

    private Map<String, List<Modification>> computeAllChanges(boolean executionModeBug, boolean resetBug,
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

        Map<String, List<Modification>> map = new HashMap<>();

        new PipeLinker().append(
                new SearchFileOrDirProcessor("**/*.java").searchForFiles().relative(),
                new AbstractProcessor<Path, Object>() {

                    @Override
                    public Object processItem(Path path) {
                        List<ChangeWrapper> changes = getChanges(path, bug, executionModeBug, fix, executionModeFix);
                        if (changes == null || changes.isEmpty()) {
                            return null;
                        }
                        String classPath = changes.get(0).getClassName().replace('.', '/').concat(".java");

                        List<Modification> modifications = Modification.convertChangeWrappersToModifications(classPath, changes);
                        if (modifications.isEmpty()) {
                            Log.warn(this, "No Changes found: '%s'.", bug);
                        } else {
							// for extracting the changes, copy the changed files for easier access...
                        	File bugFile = getFilePath(path, bug, executionModeBug);
                        	File fixFile = getFilePath(path, fix, executionModeFix);
                        	Path outputDir = Paths.get("changes_tmp", this.toString());
                        	try {
                        		outputDir.toFile().mkdirs();
								FileUtils.copyFileOrDir(bugFile, outputDir.resolve(bugFile.getName() + ".b").toFile(), 
										StandardCopyOption.REPLACE_EXISTING);
								FileUtils.copyFileOrDir(fixFile, outputDir.resolve(fixFile.getName() + ".f").toFile(), 
										StandardCopyOption.REPLACE_EXISTING);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
                        map.put(classPath, modifications);
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
                getFilePath(path, bug, executionModeBug),
                getFilePath(path, fix, executionModeFix), false, false);
    }

	private File getFilePath(Path path, T bug, boolean executionModeBug) {
		return bug.getWorkDir(executionModeBug).resolve(bug.getMainSourceDir(executionModeBug)).resolve(path).toFile();
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
