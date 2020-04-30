package se.de.hu_berlin.informatik.benchmark.api.ibugs;

import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

import java.util.Collection;

/**
 * Just a simple test class to see if the search for the relevant jars works and if the results are usable.
 * This is not supposed to be executed automatically since it requires the path to the
 * file that should be parsed.
 *
 * @author Roy Lieck
 */
public class TestGetTargetJars {

    /**
     * @param args args
     */
    public static void main(String[] args) {
        TestGetTargetJars testCPB = new TestGetTargetJars();
        testCPB.doAction(args);

    }

    /**
     * Non static entry method
     *
     * @param args Arguments
     */
    public void doAction(String[] args) {
        // no checks for valid arguments since I know how i want to call it
        String projectName = "AspectJ";
        String projectRoot = args[0];
        String fixedId = args[1];
        IBugsBuggyFixedEntity buggyFixedE = new IBugsBuggyFixedEntity(projectName, projectRoot, fixedId);

        IBugsEntity buggyE = buggyFixedE.getBuggyVersion();
        Collection<String> allJarsToMonitor = buggyE.getJarsToInstrument();

        for (String s : allJarsToMonitor) {
            Log.out(this, "Found important jar: " + s);
        }
    }

}
