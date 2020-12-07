package se.de.hu_berlin.informatik.gen.spectra.spectra;

import org.junit.Test;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.gen.spectra.main.PredicatesSpectraGenerator;
import se.de.hu_berlin.informatik.gen.spectra.predicates.mining.Miner;
import se.de.hu_berlin.informatik.gen.spectra.predicates.mining.Signature;
import se.de.hu_berlin.informatik.gen.spectra.predicates.modules.Output;
import se.de.hu_berlin.informatik.gen.spectra.predicates.pdg.SootConnector;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;
import soot.SootMethod;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.toolkits.graph.pdg.PDGRegion;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PredicatesTest extends TestSettings {

    private static final String GC = "-XX:+UseG1GC";

    private static final String MAX_SMALL_HEAP = "-Xmx1g";

    private static final String MAX_HEAP = "-Xmx2g";
    private static final String extraTestOutput = "target" + File.separator + "testoutputPredicates";

    private static final String extraTestClassPath = "";

    private void testOnProjectWithTestList(TestProject project, String outputDirName,
                                           long timeout, int testrepeatCount, boolean fullSpectra,
                                           boolean separateJVM, boolean useJava7, boolean successful, String testListPath) {
        Path profilesFile = Paths.get(extraTestOutput, outputDirName, "Profiles.csv");

        FileUtils.delete(profilesFile);
        FileUtils.delete(Paths.get(extraTestOutput, outputDirName, "Predicates.db"));
        FileUtils.delete(Paths.get(extraTestOutput, outputDirName, "jointPredicates.db"));

        long startTime = new Date().getTime();
        new PredicatesSpectraGenerator.Builder()
                .setJoinStrategy(Output.JOINSTRATEGY.PAIRS.toString())
                .setProjectDir(project.getProjectMainDir())
                .setSourceDir(project.getSrcDir())
                .setTestClassDir(project.getBinTestDir())
                .setTestClassPath(project.getTestCP())
                .setPathsToBinaries(project.getBinDir())
                .setOutputDir(extraTestOutput + File.separator + outputDirName)
//		.setOutputDir(Paths.get("src","test","resources","Lang10b").toString())
                .setTestList(testListPath)
                .setFailingTests(project.getFailingTests())
                .useFullSpectra(fullSpectra)
                .useSeparateJVM(separateJVM)
                .useJava7only(useJava7)
                .setTimeout(timeout)
                .setTestRepeatCount(testrepeatCount)
                .setCustomJvmArgs(new String[]{MAX_HEAP, GC, "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5009"})
                .setCustomSmallJvmArgs(new String[]{MAX_SMALL_HEAP, GC, "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5010"})
                .run();
        long endTime = new Date().getTime();

        System.out.println("Execution time: " + Misc.getFormattedTimerString(endTime - startTime));

        if (successful) {
            assertTrue(Files.exists(profilesFile));
        } else {
            assertFalse(Files.exists(profilesFile));
        }
    }
    private void testOnProject(TestProject project, String outputDirName,
                               long timeout, int testrepeatCount, boolean fullSpectra,
                               boolean separateJVM, boolean useJava7, boolean successful) {

        Path profilesFile = Paths.get(extraTestOutput, outputDirName, "Profiles.csv");

        FileUtils.delete(profilesFile);
        FileUtils.delete(Paths.get(extraTestOutput, outputDirName, "Predicates.db"));
        FileUtils.delete(Paths.get(extraTestOutput, outputDirName, "jointPredicates.db"));

        long startTime = new Date().getTime();
        new PredicatesSpectraGenerator.Builder()
                .setJoinStrategy(Output.JOINSTRATEGY.NONE.toString())
                .setProjectDir(project.getProjectMainDir())
                .setSourceDir(project.getSrcDir())
                .setTestClassDir(project.getBinTestDir())
                .setTestClassPath(project.getTestCP())
                .setPathsToBinaries(project.getBinDir())
                .setOutputDir(extraTestOutput + File.separator + outputDirName)
                .setTestClassList(project.getTestClassListPath())
                .setFailingTests(project.getFailingTests())
                .useFullSpectra(fullSpectra)
                .useSeparateJVM(separateJVM)
                .useJava7only(useJava7)
                .setTimeout(timeout)
                .setTestRepeatCount(testrepeatCount)
                .setCustomJvmArgs(new String[]{MAX_HEAP, GC})
                .setCustomSmallJvmArgs(new String[]{MAX_SMALL_HEAP, GC})
                .run();
        long endTime = new Date().getTime();

        System.out.println("Execution time: " + Misc.getFormattedTimerString(endTime - startTime));

        if (successful) {
            assertTrue(Files.exists(profilesFile));
        } else {
            assertFalse(Files.exists(profilesFile));
        }
    }

    @Test
    public void testGenerateRankingForLang10TestListSmall() {
        // org.apache.commons.lang3.time.FastDateParser, counter ID 166, line 399
        String outputDirName = "reportLang10bTestListSmall";
        testOnProjectWithTestList(new TestProjects.Lang10b(), outputDirName,
                10000L, 1, false, false, false, true, "lang10testsSmall.txt");
    }

    @Test
    public void testGenerateRankingAndMineForLang10TestList() {
        // org.apache.commons.lang3.time.FastDateParser, counter ID 166, line 399
        String outputDirName = "reportLang10bTestListSmall";
        testOnProjectWithTestList(new TestProjects.Lang10b(), outputDirName,
                10000L, 1, false, false, false, true, "lang10tests.txt");

        // mine Signatures
        this.MineForLang10TestListSmall();
    }

    @Test
    public void testGenerateRankingAndMineForTime3bTest() {
        // org.apache.commons.lang3.time.FastDateParser, counter ID 166, line 399
        String outputDirName = "reportTime3bTest";
        testOnProject(new TestProjects.Time3b(), outputDirName,
                10000L, 1, false, false, false, true);
        // mine Signatures
        String folder = Paths.get(extraTestOutput, outputDirName).toString();
        Miner miner = new Miner();
        HashMap<Signature.Identifier, Signature> signatures = miner.mine(folder);
    }

    //needs old  results in folder
    @Test
    public void MineForLang10TestListSmall() {
        long startTime = new Date().getTime();
        TestProject project = new TestProjects.Lang10b();
        String outputDirName = "reportLang10bTestListSmall";
        //String outputDirName = "reportTime3bTest";
        // mine Signatures
        String folder = Paths.get(extraTestOutput, outputDirName).toString();
        Miner miner = new Miner();
        HashMap<Signature.Identifier, Signature> signatures = miner.mine(folder);
        long endTime = new Date().getTime();
        Log.out(this, "Mining time: %s",  Misc.getFormattedTimerString(endTime - startTime));

        startTime = new Date().getTime();

        Output.readFromFile(folder);
        Output.writeToHumanFile(folder);

        signatures.values().forEach(signature -> {
            signature.setPredicates();
            signature.predicates.forEach(predicate -> {
                signature.locations.addAll(predicate.getLocation());
            });
        });

        System.out.println();
        signatures.forEach((identifier, signature) -> {
            System.out.println("DS: " + identifier.DS
                    + "; "
                    + "Support: ( +" + identifier.positiveSupport + ", -" + identifier.negativeSupport
                    + " ); "
                    + Arrays.toString(signature.allItems.stream().map(item -> item.prefixedId).toArray())
                    + signature.locations);
        });
        endTime = new Date().getTime();
        Log.out(this, "Output time: %s",  Misc.getFormattedTimerString(endTime - startTime));

        Map<String, PDGRegion> regionMap = new HashMap<>();
        Defects4J.getProperties();


        signatures.forEach((identifier, signature) -> {
            signature.predicates.forEach(predicate -> {
                predicate.getLocation().forEach(s -> {
                    String method = s.split(":")[0];
                    String line = s.split(":")[1];
                    String[] packageAndClassPath = method.split("/");
                    String[] packagePath = Arrays.copyOf(packageAndClassPath, packageAndClassPath.length - 1);
                    String pck = String.join(".", packagePath);
                    String className = packageAndClassPath[packageAndClassPath.length - 1];
                    SootConnector sc = SootConnector.getInstance(pck, className, project.getTestCP(), false);
                    List<SootMethod> methods = sc.getAllMethods();
                    methods.forEach(sootMethod -> {
                        UnitGraph ug = sc.getCFGForMethod(sootMethod);
                        if (ug != null) {
                            HashMutablePDG pdg = new HashMutablePDG(ug);
                            //System.out.println(pdg.toString());
                            List<PDGRegion> regions = pdg.getPDGRegions();
                            regions.forEach(pdgNodes -> {
                                pdgNodes.getUnits().forEach(unit -> {
                                    if (unit.hasTag("LineNumberTag")) {
                                        if (unit.getTag("LineNumberTag").toString().equals(line))
                                            regionMap.put(s, pdgNodes);
                                        //System.out.println(unit.getTag("LineNumberTag").toString());
                                        //System.out.println(unit.getJavaSourceStartLineNumber());
                                    }

                                });
                            });
                        }
                    });
                });
            });
        });
        regionMap.keySet().forEach(s1 -> {

        });
        System.out.println("2");

    }
}
