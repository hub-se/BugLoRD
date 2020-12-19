package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.benchmark.modification.Modification;
import se.de.hu_berlin.informatik.gen.spectra.predicates.extras.ScoringFileWriter;
import se.de.hu_berlin.informatik.gen.spectra.predicates.mining.Signature;
import se.de.hu_berlin.informatik.gen.spectra.predicates.modules.Output;
import se.de.hu_berlin.informatik.gen.spectra.predicates.modules.Predicate;
import se.de.hu_berlin.informatik.gen.spectra.predicates.pdg.CodeLocation;
import se.de.hu_berlin.informatik.gen.spectra.predicates.pdg.SootConnector;
import se.de.hu_berlin.informatik.utils.experiments.ranking.NormalizedRanking.NormalizationStrategy;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractConsumingProcessor;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import soot.SootMethod;
import soot.Unit;
import soot.UnitPatchingChain;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.UnitGraph;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class GenCodeLocationBasedRankings extends AbstractProcessor<BuggyFixedEntity<?>, BuggyFixedEntity<?>> {

    private final String suffix;
    private final String subDirName;


    public GenCodeLocationBasedRankings(String suffix) {
        super();
        this.suffix = suffix;
        this.subDirName = "predicates";
    }

    @Override
    public BuggyFixedEntity<?> processItem(BuggyFixedEntity<?> buggyEntity) {
        Log.out(this, "Processing %s.", buggyEntity);

        Entity bug = buggyEntity.getBuggyVersion();

        bug.compile(true);

        File buggyVersionDir = bug.getWorkDir(true).toFile();

        if (!buggyVersionDir.exists()) {
            Log.err(this, "Work directory doesn't exist: '" + buggyVersionDir + "'.");
            Log.err(this, "Error while querying sentences and/or combining rankings. Skipping '"
                    + buggyEntity + "'.");
            return buggyEntity;
        }

        Path rankingDir = bug.getWorkDir(true).resolve(suffix == null ?
                BugLoRDConstants.DIR_NAME_RANKING : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix);

        String folder = Paths.get(rankingDir.resolve(subDirName).toString()).toString();

        HashMap<Signature.Identifier, Signature> signatures = this.readFromFile(folder, "signatures.dat");

        //resolve Code locations
        Output.readFromFile(folder);
        Output.writeToHumanFile(folder);

        Log.out(this, "getTargets %s.", folder);
        List<CodeLocation> targets =  getTargets(buggyEntity);

        Log.out(this, "Scoring %s.", folder);
        double score = getScore(signatures, targets, buggyEntity);

        Log.out(this, "Finish scoring %s.", folder);


        //Output
        String line = buggyEntity.getUniqueIdentifier() + ";" + score;
        ScoringFileWriter.getInstance().write(line);

        Log.out(this, "Score for %s is %f.", folder, score);

        return buggyEntity;
    }

    private HashMap<Signature.Identifier, Signature> readFromFile(String folder , String filename){
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(folder + "/" + filename))) {

            return ((HashMap<Signature.Identifier, Signature>) ois.readObject());
        } catch (Exception ex) {
            Log.err(Output.class, ex);
        }
        return null;
    }

    private double getScore(HashMap<Signature.Identifier, Signature> signatures, List<CodeLocation> targets, BuggyFixedEntity<?> buggyEntity) {
        List<Double> scores = new ArrayList<>();
        if (signatures.size() == 0)
            Log.out(this, "score with %s signatures in %s", signatures.size() , buggyEntity.getUniqueIdentifier());
        if (targets.size() == 0)
            Log.out(this, "score with %s targets in %s", targets.size(), buggyEntity.getUniqueIdentifier());
        signatures.forEach((key, signature) -> {
            signature.predicates.forEach(predicate -> {
                predicate.getLocation().forEach(predicateLocation -> {
                    for (CodeLocation target : targets) {
                        if (scores.contains(0.0)) //skip calculating if we got a full hit
                            continue;
                        scores.add(calculateScore(predicateLocation, target, buggyEntity));
                    }
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
        //Log.out(this, "Calculating score with %s methods", methods.size());
        for (SootMethod sootMethod : methods) {
            if (!sootMethod.hasActiveBody())
                continue;
            sootMethod.getActiveBody().getUnits().forEach(unit -> {
                if (unit.getJavaSourceStartLineNumber() == predicateLine)
                    predicateLocations.add(new CodeLocation(unit, className, sootMethod));
            });
        }

        //Log.out(this, "Calculating score with %s predicateLocations", predicateLocations.size());
        if (predicateLocations.isEmpty())
            return Double.POSITIVE_INFINITY;

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
                List<Double> edgeDistances = new ArrayList<>();
                while (iteratorOnCallsOutOfMethod.hasNext()) {
                    Edge edge = iteratorOnCallsOutOfMethod.next();
                    if (edge.tgt() == target.method) {
                        Integer internDistanceInGoal = this.getDistanceInUnits(sc, goal.method, goal.unit, edge.srcUnit());
                        if (internDistanceInGoal == null)
                            continue; //we wont get a result that is fair to compare
                        Integer internDistanceInTarget = this.getDistanceInUnits(sc, target.method, target.unit, edge.tgt().getActiveBody().getUnits().getFirst());
                        if (internDistanceInTarget == null)
                            continue; //we wont get a result that is fair to compare
                        Log.out(this, "Found a edge connection!");
                        edgeDistances.add((double) (internDistanceInGoal + internDistanceInTarget + 10)); //TODO EdgeCost
                    }
                }
                return edgeDistances.stream().min(Double::compareTo).orElse(Double.NaN);
            }
        }
        return Double.MAX_VALUE;
    }

    private Integer getDistanceInUnits(SootConnector sc, SootMethod method, Unit unit1, Unit unit2) {
        UnitGraph unitGraph =  sc.getCFGForMethod(method);
        List<Unit> unitPath = unitGraph.getExtendedBasicBlockPathBetween(unit1,unit2);
        if (unitPath == null)
            unitPath = unitGraph.getExtendedBasicBlockPathBetween(unit2, unit1);
        if (unitPath != null) {
            Log.out(this, "Found a path with length %s!", unitPath.size());
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
                //Log.out(this, "Calculating score with %s methods", methods.size());
                for (SootMethod sootMethod : methods) {
                    if (!sootMethod.hasActiveBody())
                        continue;
                    UnitPatchingChain unitPatchingChain = sootMethod.getActiveBody().getUnits();
                    for (Unit unit : unitPatchingChain) {
                        int start = unit.getJavaSourceStartLineNumber();
                        int end = -1;
                        Unit succ = unitPatchingChain.getSuccOf(unit);
                        if (succ != null)
                            end = succ.getJavaSourceStartLineNumber();

                        List<Integer> lines = this.getLinesBetween(start, end);

                        for (int possibleLine : modification.getPossibleLines()) {
                            //Log.out(this, "possibleLine %s ", possibleLine);
                            if (lines.contains(possibleLine))
                                targets.add(new CodeLocation(unit, className, sootMethod));
                        }
                    }
                }
            });
        });
        return targets;
    }

    private List<Integer> getLinesBetween(int start, int end) {
        List<Integer> lines = new ArrayList<>();
        if (start == -1)
            return lines;
        lines.add(start);
        if (end == -1)
            return lines;
        for (int i = start + 1; i < end; i++) {
            lines.add(i);
        }
        return lines;
    }

}

