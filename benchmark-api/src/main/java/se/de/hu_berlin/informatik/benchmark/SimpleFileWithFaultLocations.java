package se.de.hu_berlin.informatik.benchmark;

import se.de.hu_berlin.informatik.utils.files.FileUtils;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Create a file that contains faults.
 */
public class SimpleFileWithFaultLocations implements FileWithFaultLocations {
    /**
     * file name
     */
    private final String name;
    /**
     * all fault locations in this file
     */
    private FaultLocations locations = null;

    /**
     * Construct new file with simple fault locations.
     *
     * @param name file name
     */
    public SimpleFileWithFaultLocations(final String name) {
        super();
        this.name = name;
        this.locations = new SimpleFaultLocations();
    }

    /**
     * Construct new file.
     *
     * @param name      file name
     * @param locations fault locations
     */
    public SimpleFileWithFaultLocations(final String name, FaultLocations locations) {
        super();
        this.name = name;
        this.locations = locations;
    }

    @Override
    public String getFileName() {
        return this.name;
    }

    @Override
    public boolean hasFaultLocations() {
        return locations != null;
    }

    @Override
    public FaultLocations getFaultLocations() {
        return locations;
    }

    @Override
    public boolean setFaultLocations(FaultLocations faultLocations) {
        if (faultLocations != null) {
            locations = faultLocations;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getClassName() {
        try {
            String content = FileUtils.readFile2String(Paths.get(name));
            int pos = content.indexOf("package ");
            if (pos != -1) {
                int pos2 = content.indexOf(';', pos + 8);
                if (pos2 != -1) {
                    return content.substring(pos + 8, pos2).trim();
                }
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
