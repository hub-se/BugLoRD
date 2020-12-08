package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.benchmark.modification.Modification;
import se.de.hu_berlin.informatik.gen.spectra.AbstractSpectraGenerator.AbstractBuilder;
import se.de.hu_berlin.informatik.gen.spectra.main.PredicatesSpectraGenerator;
import se.de.hu_berlin.informatik.gen.spectra.predicates.extras.ScoringFileWriter;
import se.de.hu_berlin.informatik.gen.spectra.predicates.mining.Miner;
import se.de.hu_berlin.informatik.gen.spectra.predicates.mining.Signature;
import se.de.hu_berlin.informatik.gen.spectra.predicates.modules.Output;
import se.de.hu_berlin.informatik.gen.spectra.predicates.modules.Predicate;
import se.de.hu_berlin.informatik.gen.spectra.predicates.pdg.CodeLocation;
import se.de.hu_berlin.informatik.gen.spectra.predicates.pdg.SootConnector;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.files.processors.SearchFileOrDirToListProcessor;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.toolkits.graph.pdg.PDGRegion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Runs a single experiment.
 *
 * @author Simon Heiden
 */
public class ERProducePredicates extends AbstractProcessor<BuggyFixedEntity<?>, BuggyFixedEntity<?>> {

    private final String suffix;
    private final String subDirName;
    private final boolean fillEmptyLines;

    /**
     * @param suffix         a suffix to append to the ranking directory (may be null)
     * @param fillEmptyLines whether to fill up empty lines between statements
     */
    public ERProducePredicates(String suffix, boolean fillEmptyLines) {
        this.fillEmptyLines = fillEmptyLines;
        this.subDirName = "predicates"; //getSubDirName(toolSpecific);
        this.suffix = suffix;
    }

    @Override
    public BuggyFixedEntity<?> processItem(BuggyFixedEntity<?> buggyEntity) {
        Log.out(this, "test");
        Log.out(this, "Processing %s.", buggyEntity);


        Entity bug = buggyEntity.getBuggyVersion();

        /* #====================================================================================
         * # checkout buggy version if necessary
         * #==================================================================================== */
        buggyEntity.requireBug(true);

        /* #====================================================================================
         * # collect paths
         * #==================================================================================== */
        String buggyMainSrcDir = bug.getMainSourceDir(true).toString();
        String buggyMainBinDir = bug.getMainBinDir(true).toString();
        String buggyTestBinDir = bug.getTestBinDir(true).toString();
        String buggyTestCP = bug.getTestClassPath(true);

        /* #====================================================================================
         * # compile buggy version
         * #==================================================================================== */
        bug.compile(true);

        /* #====================================================================================
         * # generate predicates
         * #==================================================================================== */
        String testClasses = Misc.listToString(bug.getTestClasses(true), System.lineSeparator(), "", "");

        String testClassesFile = bug.getWorkDir(true).resolve(BugLoRDConstants.FILENAME_TEST_CLASSES).toString();
        FileUtils.delete(new File(testClassesFile));
        try {
            FileUtils.writeString2File(testClasses, new File(testClassesFile));
        } catch (IOException e) {
            Log.err(this, "IOException while trying to write to file '%s'.", testClassesFile);
            Log.err(this, "Error while checking out or generating rankings. Skipping '"
                    + buggyEntity + "'.");
            bug.tryDeleteExecutionDirectory(false);
            return null;
        }

        List<String> failingTests = bug.getFailingTests(true);


        Path rankingDir = bug.getWorkDir(true).resolve(suffix == null ?
                BugLoRDConstants.DIR_NAME_RANKING : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix);
        Path statsDirData = bug.getWorkDataDir().resolve(suffix == null ?
                BugLoRDConstants.DIR_NAME_STATS : BugLoRDConstants.DIR_NAME_STATS + "_" + suffix);

        //Clean last run
        FileUtils.delete(rankingDir.resolve(subDirName));
        FileUtils.delete(rankingDir.resolve(statsDirData));

        String[] args = {Paths.get(rankingDir.resolve(subDirName).toString()).toString()};
        String folder = Paths.get(rankingDir.resolve(subDirName).toString()).toString();
        // generate signatures
        createSignatures(buggyEntity, bug, buggyMainSrcDir, buggyMainBinDir,
                buggyTestBinDir, buggyTestCP, testClassesFile,
                rankingDir.resolve(subDirName), failingTests);



        // mine Signatures
        Miner miner = new Miner();
        HashMap<Signature.Identifier, Signature> signatures = miner.mine(folder);

        //resolve Code locations
        Output.readFromFile(folder);
        Output.writeToHumanFile(folder);

        signatures.values().forEach(signature -> {
            signature.setPredicates();
            for (Predicate predicate : signature.predicates) {
                signature.locations.addAll(predicate.getLocation());
            }
        });

        Log.out(this, "getTargets %s.", folder);
        List<CodeLocation> targets =  getTargets(buggyEntity);

        Log.out(this, "Scoring %s.", folder);
        double score = getScore(signatures, targets, buggyEntity);

        Log.out(this, "Finish scoring %s.", folder);


        //Output
        String line = buggyEntity.getUniqueIdentifier() + ";" + score;
        ScoringFileWriter.getInstance().write(line);

        System.out.println();
        signatures.forEach((identifier, signature) -> System.out.println("DS: " + identifier.DS
                + "; "
                + "Support: ( +" + identifier.positiveSupport + ", -" + identifier.negativeSupport
                + " ); "
                //+ Arrays.toString(signature.allItems.stream().map(item -> item.prefixedId).toArray()));
                + signature.locations));
        Log.out(this, "Score for %s is %f.", folder, score);

        System.out.println();
        signatures.forEach((identifier, signature) ->  writeSignatures("DS: " + identifier.DS
                + "; "
                + "Support: ( +" + identifier.positiveSupport + ", -" + identifier.negativeSupport
                + " ); "
                //+ Arrays.toString(signature.allItems.stream().map(item -> item.prefixedId).toArray()), folder));
                + signature.locations, folder));


        File signatureFile = Paths.get(folder).resolve("Signatures.csv").toFile();
        if (!signatureFile.exists()) {
            Log.err(this, "%s: Error generating signatures...", buggyEntity);
        } else {
            Log.out(this, "%s: Generating signatures was successful!", buggyEntity);
        }


        /* #====================================================================================
         * # cleanup
         * #==================================================================================== */

        File spectraFile = rankingDir.resolve(subDirName)
                .resolve(BugLoRDConstants.SPECTRA_FILE_NAME).toFile();
        File spectraFileFiltered = rankingDir.resolve(subDirName)
                .resolve(BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME).toFile();

        File signaturesFile = rankingDir.resolve(subDirName)
                .resolve("Signatures.csv").toFile();
        File predicatesFile = rankingDir.resolve(subDirName)
                .resolve("Predicates.csv").toFile();
        File dBFile = rankingDir.resolve(subDirName)
                .resolve("jointPredicates.db").toFile();

        if (!signaturesFile.exists()) {
            Log.err(this, "Error while generating signatures. Skipping '" + buggyEntity + "'.");
            return null;
        }

        copyFile(bug, signaturesFile, "Signatures.csv", "Could not copy the signatures to the data directory.");
        copyFile(bug, predicatesFile, "Predicates.csv", "Could not copy the predicates to the data directory.");
        copyFile(bug, dBFile, "jointPredicates.db", "Could not copy the predicate db to the data directory.");

        try {
            List<Path> result3 = new SearchFileOrDirToListProcessor("**instrumented", true)
                    .searchForDirectories().skipSubTreeAfterMatch().submit(rankingDir).getResult();
            for (Path dir : result3) {
                FileUtils.delete(dir);
            }

            //delete old stats data directory
            FileUtils.delete(statsDirData);
            FileUtils.copyFileOrDir(
                    rankingDir.toFile(),
                    statsDirData.toFile());
        } catch (IOException e) {
            Log.err(this, e, "Could not clean up...");
        }



        /* #====================================================================================
         * # clean up unnecessary directories (doc files, svn/git files, binary classes)
         * #==================================================================================== */
        bug.removeUnnecessaryFiles(true);


//		/* #====================================================================================
//		 * # move to archive directory, in case it differs from the execution directory
//		 * #==================================================================================== */
//		buggyEntity.tryMovingExecutionDirToArchive();
//
//		buggyEntity.tryDeleteExecutionDirectory(false);

        return buggyEntity;
    }

    private double getScore(HashMap<Signature.Identifier, Signature> signatures, List<CodeLocation> targets,BuggyFixedEntity<?> buggyEntity) {
        List<Double> scores = new ArrayList<>();
        signatures.forEach((key, signature) -> {
            signature.predicates.forEach(predicate -> {
                predicate.getLocation().forEach(predicateLocation -> {
                    targets.forEach(target -> {
                        if (scores.contains(0.0)) //skip calculating if we got a full hit
                            return;
                        scores.add(calculateScore(predicateLocation, target, buggyEntity));
                    });
                });
            });
        });
        Optional<Double> aDouble = scores.stream().min(Double::compareTo);

        return aDouble.orElse(Double.NaN);
    }

    private double calculateScore(String predicateLocation, CodeLocation target, BuggyFixedEntity<?> buggyEntity) {
        //Log.out(this, "calculateScore for  %s", predicateLocation);

        LinkedList<CodeLocation> predicateLocations =  new LinkedList<>();

        String path = predicateLocation.split(":")[0];
        int predicateLine = Integer.parseInt(predicateLocation.split(":")[1]);
        String[] packageAndClassPath = path.split("/");
        String[] packagePath = Arrays.copyOf(packageAndClassPath, packageAndClassPath.length - 1);
        String pck = String.join(".", packagePath);
        String className = packageAndClassPath[packageAndClassPath.length - 1];
        String classPaths = buggyEntity.getBuggyVersion().getClassPath(true) + ":" + buggyEntity.getBuggyVersion().getTestClassPath(true);
        SootConnector sc = SootConnector.getInstance(pck, className, classPaths, false);
        List<SootMethod> methods = sc.getAllMethods();
        methods.forEach(sootMethod -> {
            UnitGraph ug = sc.getCFGForMethod(sootMethod);
            if (ug != null) {
                HashMutablePDG pdg = new HashMutablePDG(ug);
                List<PDGRegion> regions = pdg.getPDGRegions();
                regions.forEach(pdgNodes -> {
                    pdgNodes.getUnits().forEach(unit -> {
                    if (unit.getJavaSourceStartLineNumber() == predicateLine)
                        predicateLocations.add(new CodeLocation(unit, className, sootMethod));
                    });
                });
            }
        });
        if (predicateLocations.isEmpty())
            return Double.NaN;

        //Log.out(this, MessageFormat.format("calculating Score between : {0} in Method: {1} and: {2} with method: {3} and class {4}", target.getLocationString(), target.MethodName, predicateLocation, predicateLocations.getFirst().MethodName, className));
        for (CodeLocation goal : predicateLocations) {
            if (target.equals(goal) || target.unit.equals(goal.unit))
                return 0.0;
            else if ((target.className + target.methodName).equals(goal.className + goal.methodName)) {
                Integer unitPathDistance = this.getDistanceInUnits(sc, goal.method, goal.unit, target.unit);
                if (unitPathDistance != null)
                    return unitPathDistance;
            }
            else if (target.className.equals(goal.className)) {
                Iterator<Edge> iteratorOnCallsOutOfMethod = sc.getIteratorOnCallsOutOfMethod(goal.method);
                while (iteratorOnCallsOutOfMethod.hasNext()) {
                    Edge edge = iteratorOnCallsOutOfMethod.next();
                    if (edge.tgt() == target.method) {
                        Integer internDistanceInGoal = this.getDistanceInUnits(sc, goal.method, goal.unit, edge.srcUnit());
                        if (internDistanceInGoal == null)
                            return Double.NaN; //we wont get a result that is fair to compare
                        Integer internDistanceInTarget = this.getDistanceInUnits(sc, target.method, target.unit, edge.tgt().getActiveBody().getUnits().getFirst());
                        if (internDistanceInTarget == null)
                            return Double.NaN; //we wont get a result that is fair to compare
                        Log.out(this, "Found a edge connection!");
                        return internDistanceInGoal + internDistanceInTarget + 10; //TODO EdgeCost
                    }
                }
            }
        }

        return Double.NaN;
    }

    private Integer getDistanceInUnits(SootConnector sc, SootMethod method, Unit unit1, Unit unit2) {
        UnitGraph unitGraph =  sc.getCFGForMethod(method);
        List<Unit> unitPath = unitGraph.getExtendedBasicBlockPathBetween(unit1,unit2);
        if (unitPath == null)
            unitPath = unitGraph.getExtendedBasicBlockPathBetween(unit2, unit1);
        if (unitPath != null) {
            return unitPath.size();
        }
        return null;
    }

    private  List<CodeLocation> getTargets(BuggyFixedEntity<?> buggyEntity) {
        List<CodeLocation> targets = new ArrayList<>();
        Map<String, List<Modification>> changes = buggyEntity.getAllChanges(true, true, true, true, true, true);
        changes.forEach((s, modifications) -> {
            modifications.forEach(modification -> {
                String cleanedPath = modification.getClassPath().substring(0, modification.getClassPath().length() - 5); //remove .java ending
                String[] packageAndClassPath = cleanedPath.split("/");
                String[] packagePath = Arrays.copyOf(packageAndClassPath, packageAndClassPath.length - 1);
                String pck = String.join(".",packagePath);
                String className = packageAndClassPath[packageAndClassPath.length -1];
                String classPaths = buggyEntity.getBuggyVersion().getClassPath(true) + ":" + buggyEntity.getBuggyVersion().getTestClassPath(true);
                SootConnector sc = SootConnector.getInstance(pck, className, classPaths);
                List<SootMethod> methods = sc.getAllMethods();
                methods.forEach(sootMethod -> {
                    UnitGraph ug = sc.getCFGForMethod(sootMethod);
                    if (ug != null) {
                        HashMutablePDG pdg = new HashMutablePDG(ug);
                        List<PDGRegion> regions = pdg.getPDGRegions();
                        regions.forEach(pdgNodes -> {
                            pdgNodes.getUnits().forEach(unit -> {
                                if (unit.hasTag("LineNumberTag")) {
                                    for (int possibleLine : modification.getPossibleLines()) {
                                        if (unit.getJavaSourceStartLineNumber() == possibleLine)
                                            targets.add(new CodeLocation(unit, className, sootMethod));
                                    }
                                }
                            });
                        });
                    }
                });
            });
        });
        return targets;
    }

    private void copyFile(Entity bug, File signaturesFile, String s, String s2) {
        try {
            FileUtils.copyFileOrDir(
                    signaturesFile,
                    bug.getWorkDataDir().resolve(subDirName)
                            .resolve(s).toFile(),
                    StandardCopyOption.REPLACE_EXISTING);
            FileUtils.delete(signaturesFile);
        } catch (IOException e) {
            Log.err(this, e, s2);
        }
    }

    private void createSignatures(BuggyFixedEntity<?> buggyEntity, Entity bug, String buggyMainSrcDir,
                                  String buggyMainBinDir, String buggyTestBinDir, String buggyTestCP, String testClassesFile,
                                  Path rankingDir, List<String> failingTests) {
        // 1200s == 20 minutes as test timeout should be reasonable!?
        // repeat tests 2 times to generate more correct coverage data!?

        Log.out(this, "%s: Generating predicates...", buggyEntity);
        AbstractBuilder builder = new PredicatesSpectraGenerator.Builder();

        builder
                .setCustomJvmArgs(Defects4JProperties.MAIN_JVM_ARGS.getValue().split(" ", 0))
                .setCustomSmallJvmArgs(Defects4JProperties.SMALL_JVM_ARGS.getValue().split(" ", 0))
                .setJavaHome(Defects4JProperties.JAVA7_HOME.getValue())
                .setProjectDir(bug.getWorkDir(true).toString())
                .setSourceDir(buggyMainSrcDir)
                .setTestClassDir(buggyTestBinDir)
                .setTestClassPath(buggyTestCP);
//        if (bug.getUniqueIdentifier().contains("Mockito")) {
//        	builder
//        	// don't include test class binaries for Mockito?
//        	.setPathsToBinaries(bug.getWorkDir(true).resolve(buggyMainBinDir).toString());
//        } else {
        builder
                // include the test class binaries in instrumentation, because why not?...
                // for instrumentation, there are cases (i.e. Closure 154) where classes in the test class directory
                // have the same name as classes in the main class directory. Originally, Cobertura would silently
                // overwrite already instrumented classes with instrumented classes with the same name...
                // We will instead keep the first found class. This is why we put the test class directory first.
                .setPathsToBinaries(
                        //bug.getWorkDir(true).resolve(buggyTestBinDir).toString(),
                        bug.getWorkDir(true).resolve(buggyMainBinDir).toString());
//        }
        builder
                .setOutputDir(rankingDir.toString())
                .setTestClassList(testClassesFile)
                .setFailingTests(failingTests)
                .useSeparateJVM(Boolean.parseBoolean(Defects4JProperties.ALWAYS_USE_SEPJVM.getValue()))
                .useJava7only(Boolean.parseBoolean(Defects4JProperties.ALWAYS_USE_JAVA7.getValue()))
//		.setTimeout(5000L)
                .setCondenseNodes(fillEmptyLines)
                .setTimeout(Long.valueOf(Defects4JProperties.TEST_TIMEOUT.getValue()))
                .setTestRepeatCount(1)
                .setMaxErrors(Integer.parseInt(Defects4JProperties.MAX_ERRORS.getValue()))
                .setPipeBufferSize(Integer.parseInt(Defects4JProperties.PIPE_BUFFER_SIZE.getValue()));

        long startTime = new Date().getTime();

        builder.run();


        long endTime = new Date().getTime();

        Log.out(this, "%s: Execution time: %s", buggyEntity, Misc.getFormattedTimerString(endTime - startTime));

    }

    static void writeSignatures(String line, String fileString) {
        try {
            FileWriter fw = new FileWriter(Paths.get(fileString, "Signatures.csv").toString(),true);
            fw.append(line);
            fw.append(System.lineSeparator());
            fw.flush();
            fw.close();
        }
        catch (IOException ex) {
            System.out.println("IOException while writing to file");
        }
    }


}

