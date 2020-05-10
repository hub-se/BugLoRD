package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;


import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.FileFinder;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.Report;

import java.io.File;

public class NativeReport implements Report {
    private final ProjectData projectData;
    private File destinationDir;
    private final FileFinder finder;
    private final String encoding;

    public NativeReport(ProjectData projectData, File destinationDir,
                        FileFinder finder, String encoding) {
        this.projectData = projectData;
        this.destinationDir = destinationDir;
        this.finder = finder;
        this.encoding = encoding;
    }

    public ProjectData getProjectData() {
        return projectData;
    }

    public File getDestinationDir() {
        return destinationDir;
    }

    public FileFinder getFinder() {
        return finder;
    }

    public String getEncoding() {
        return encoding;
    }
}