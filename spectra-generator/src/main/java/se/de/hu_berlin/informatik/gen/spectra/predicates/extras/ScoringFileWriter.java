package se.de.hu_berlin.informatik.gen.spectra.predicates.extras;

import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ScoringFileWriter {

    private static boolean fileIsReset = true;
    private static final ScoringFileWriter instance = new ScoringFileWriter();

    public synchronized void write(String line) {
        try {
            FileOutputStream fos = new FileOutputStream(Defects4J.Defects4JProperties.ARCHIVE_DIR.getValue() + "/result.csv", true);
            fos.write(line.getBytes());
            fos.write(System.lineSeparator().getBytes());
            fos.flush();
            fos.close();
        }
        catch (IOException ex) {
           Log.err(ex, "IOException while writing to file");
        }
    }

    public synchronized static ScoringFileWriter getInstance() {
        if (!fileIsReset) {
            fileIsReset = true;
            FileUtils.delete(new File(Defects4J.Defects4JProperties.ARCHIVE_DIR.getValue() + "/result.csv"));
        }
        return instance;
    }
}
