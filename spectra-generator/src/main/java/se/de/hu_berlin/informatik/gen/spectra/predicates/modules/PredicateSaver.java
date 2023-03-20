package se.de.hu_berlin.informatik.gen.spectra.predicates.modules;

import se.de.hu_berlin.informatik.gen.spectra.predicates.extras.Profile;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractConsumingProcessor;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class PredicateSaver  extends AbstractConsumingProcessor<Profile> {

    private final String outputDir;

    public PredicateSaver(String outputDir) {
        this.outputDir = outputDir;
        //reset file
        try {
            new FileOutputStream(outputDir + "/Profiles.csv").close();
        }
        catch (IOException ex) {
            Log.abort(this,"could not reset Profiles.csv");
        }
    }

    @Override
    public void consumeItem(Profile profile)  {

        try {
            FileWriter fw = new FileWriter(Paths.get(outputDir, "Profiles.csv").toString(),true);
            StringBuilder line = new StringBuilder();
            line.append(profile.predicates.stream().map(String::valueOf).collect(Collectors.joining(",")));
            line.append(";");
            line.append(profile.positiveSupport);
            line.append(System.getProperty("line.separator"));
            fw.append(line);
            fw.flush();
            fw.close();
        }
        catch (IOException ex) {
            System.out.println("IOException while writing to file");
        }
    }
}
