package se.de.hu_berlin.informatik.benchmark.api;

import java.nio.file.Path;

public abstract class AbstractDirectoryProvider implements DirectoryProvider {

    private Path mainSrcDir = null;
    private Path testSrcDir = null;
    private Path mainBinDir = null;
    private Path testBinDir = null;


    @Override
    public Path getMainSourceDir(boolean executionMode) {
        if (mainSrcDir == null) {
            mainSrcDir = computeMainSourceDir(executionMode);
        }
        return mainSrcDir;
    }

    abstract public Path computeMainSourceDir(boolean executionMode);

    @Override
    public Path getTestSourceDir(boolean executionMode) {
        if (testSrcDir == null) {
            testSrcDir = computeTestSourceDir(executionMode);
        }
        return testSrcDir;
    }

    abstract public Path computeTestSourceDir(boolean executionMode);

    @Override
    public Path getMainBinDir(boolean executionMode) {
        if (mainBinDir == null) {
            mainBinDir = computeMainBinDir(executionMode);
        }
        return mainBinDir;
    }

    abstract public Path computeMainBinDir(boolean executionMode);

    @Override
    public Path getTestBinDir(boolean executionMode) {
        if (testBinDir == null) {
            testBinDir = computeTestBinDir(executionMode);
        }
        return testBinDir;
    }

    abstract public Path computeTestBinDir(boolean executionMode);
}
