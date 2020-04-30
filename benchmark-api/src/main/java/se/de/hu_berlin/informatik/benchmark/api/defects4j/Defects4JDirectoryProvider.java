package se.de.hu_berlin.informatik.benchmark.api.defects4j;

import se.de.hu_berlin.informatik.benchmark.api.AbstractDirectoryProvider;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J.Defects4JProperties;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Defects4JDirectoryProvider extends AbstractDirectoryProvider {

    private final boolean buggyVersion;
    private final int bugID;
    private final String project;

    /**
     * @param project a project identifier, serving as a directory name
     * @param bugID   id of the bug
     * @param buggy   whether to use the buggy or the fixed version of the bug with the given id
     */
    public Defects4JDirectoryProvider(String project, String bugID, boolean buggy) {
        try {
            this.bugID = Integer.parseInt(bugID);
        } catch (NumberFormatException e) {
            throw e;
        }
        this.project = project;

        this.buggyVersion = buggy;

    }

    /**
     * @param project a project identifier, serving as a directory name
     * @param bugID   id of the bug
     * @param buggy   whether to use the buggy or the fixed version of the bug with the given id
     */
    public Defects4JDirectoryProvider(String project, int bugID, boolean buggy) {
        this.bugID = bugID;
        this.project = project;

        this.buggyVersion = buggy;

    }

    public Path getProjectDir(boolean executionMode) {
        return getBenchmarkDir(executionMode).resolve(project);
    }

    @Override
    public Path computeMainSourceDir(boolean executionMode) {
        return Paths.get(Defects4J.getD4JExport(getWorkDir(executionMode).toString(), "dir.src.classes"));
    }

    @Override
    public Path computeTestSourceDir(boolean executionMode) {
        return Paths.get(Defects4J.getD4JExport(getWorkDir(executionMode).toString(), "dir.src.tests"));
    }

    @Override
    public Path computeMainBinDir(boolean executionMode) {
        return Paths.get(Defects4J.getD4JExport(getWorkDir(executionMode).toString(), "dir.bin.classes"));
    }

    @Override
    public Path computeTestBinDir(boolean executionMode) {
        return Paths.get(Defects4J.getD4JExport(getWorkDir(executionMode).toString(), "dir.bin.tests"));
    }

    @Override
    public String getBenchmarkArchiveDir() {
        return Defects4J.getValueOf(Defects4JProperties.ARCHIVE_DIR);
    }

    @Override
    public String getBenchmarkExecutionDir() {
        return Defects4J.getValueOf(Defects4JProperties.EXECUTION_DIR);
    }

    @Override
    public Path getRelativeEntityPath() {
        return Paths.get(project, String.valueOf(bugID));
    }

    @Override
    public String getEntityIdentifier() {
        if (this.buggyVersion) {
            return bugID + "b";
        } else {
            return bugID + "f";
        }
    }


}
