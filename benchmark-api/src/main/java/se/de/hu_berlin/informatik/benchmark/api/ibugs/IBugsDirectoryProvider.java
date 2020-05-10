package se.de.hu_berlin.informatik.benchmark.api.ibugs;

import se.de.hu_berlin.informatik.benchmark.api.AbstractDirectoryProvider;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IBugsDirectoryProvider extends AbstractDirectoryProvider {

    private final boolean buggyVersion;
    private final String fixedId;

    // this will most likely always be aspect4 for now
    private final String project;

    private final File project_root; // the root of the project including the build and properties.xml
    private final File entity_root; // the root of the checked out repository


    /**
     * Constructor for the directory provider for an iBugsEntity
     *
     * @param aProject     a project identifier, serving as a directory name
     * @param aProjectRoot the root directory of the project storing the build.xml and properties.xml
     * @param aFixedId     id of the bug
     * @param aBuggy       * whether to use the buggy or the fixed version of the bug with the given id
     */
    public IBugsDirectoryProvider(String aProject, String aProjectRoot, String aFixedId, boolean aBuggy) {
        project = aProject;
        project_root = new File(aProjectRoot);
        fixedId = aFixedId;
        buggyVersion = aBuggy;

        String preOrPost = buggyVersion ? IBugs.PRE_FIX : IBugs.POST_FIX;
        Path er_path = Paths.get(project_root.getAbsolutePath(), IBugs.VERSION_SUBDIR, fixedId, preOrPost);
        entity_root = er_path.toFile();
    }

    public Path getProjectDir(boolean executionMode) {
        return project_root.toPath();
    }

    @Override
    public Path computeMainSourceDir(boolean executionMode) {
//		return Paths.get(Defects4J.getD4JExport(getWorkDir(executionMode).toString(), buggyVersion, "dir.src.classes"));
        // TODO implement
        return null;
    }

    @Override
    public Path computeTestSourceDir(boolean executionMode) {
//		return Paths.get(Defects4J.getD4JExport(getWorkDir(executionMode).toString(), buggyVersion, "dir.src.tests"));
        // TODO implement
        return null;
    }

    @Override
    public Path computeMainBinDir(boolean executionMode) {
//		return Paths.get(Defects4J.getD4JExport(getWorkDir(executionMode).toString(), buggyVersion, "dir.bin.classes"));
        // TODO implement
        return null;
    }

    @Override
    public Path computeTestBinDir(boolean executionMode) {
//		return Paths.get(Defects4J.getD4JExport(getWorkDir(executionMode).toString(), buggyVersion, "dir.bin.tests"));
        // TODO implement
        return null;
    }

    @Override
    public String getBenchmarkArchiveDir() {
        // the same for archive and execution with iBugs
        return entity_root.getAbsolutePath();
    }

    @Override
    public String getBenchmarkExecutionDir() {
        // the same for archive and execution with iBugs
        return entity_root.getAbsolutePath();
    }

    @Override
    public Path getRelativeEntityPath() {
        return entity_root.toPath();
    }

    @Override
    public String getEntityIdentifier() {
        // i use the command strings
        if (this.buggyVersion) {
            return fixedId + IBugs.PRE_FIX;
        } else {
            return fixedId + IBugs.POST_FIX;
        }
    }

    /**
     * @return the buggyVersion
     */
    public boolean isBuggyVersion() {
        return buggyVersion;
    }

    /**
     * @return the fixedId
     */
    public String getFixedId() {
        return fixedId;
    }

    /**
     * @return the project
     */
    public String getProject() {
        return project;
    }

    /**
     * @return the project_root
     */
    public File getProject_root() {
        return project_root;
    }

    /**
     * @return the entity_root
     */
    public File getEntity_root() {
        return entity_root;
    }


}
