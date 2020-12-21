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
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import soot.SootMethod;
import soot.Unit;
import soot.UnitPatchingChain;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.UnitGraph;
import sun.security.krb5.SCDynamicStoreConfig;

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
        buggyEntity.requireBug(true);
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
        Path statsDirData = bug.getWorkDataDir().resolve(suffix == null ?
                BugLoRDConstants.DIR_NAME_STATS : BugLoRDConstants.DIR_NAME_STATS + "_" + suffix);

        String folder = Paths.get(rankingDir.resolve(subDirName).toString()).toString();


        LinkedHashMap<Signature.Identifier, Signature> signatures = this.readFromFile(bug.getWorkDataDir().toString(), "signatures.dat");

        //resolve Code locations
        Output.readFromFile(folder);
        Output.writeToHumanFile(folder);

        Log.out(this, "getTargets %s.", folder);
        List<CodeLocation> targets =  getTargets(buggyEntity);

        Log.out(this, "Scoring %s.", folder);
        Score score = getScore(signatures, targets, buggyEntity);

        Log.out(this, "Finish scoring %s.", folder);


        //Output
        String line;
        if (score.Failed) {
            line = buggyEntity.getUniqueIdentifier() + ";" + Double.NaN + ";" + Double.NaN + ";" + Double.NaN + ";" + Double.NaN + ";" + Double.NaN;
        }
        else {
            line = buggyEntity.getUniqueIdentifier() + ";" + score.getBestScore() + ";" + score.getWorstScore() + ";" + score.getAverageScore() + ";" + score.PathCost + ";" + score.DS;
        }
        ScoringFileWriter.getInstance().write(line);

        Log.out(this, "Score for %s is %f.", folder, score.getBestScore());

        return buggyEntity;
    }

    private LinkedHashMap<Signature.Identifier, Signature> readFromFile(String folder , String filename){
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(folder + "/" + filename))) {

            return ((LinkedHashMap<Signature.Identifier, Signature>) ois.readObject());
        } catch (Exception ex) {
            Log.err(Output.class, ex);
        }
        return null;
    }

    private Score getScore(HashMap<Signature.Identifier, Signature> signatures, List<CodeLocation> targets, BuggyFixedEntity<?> buggyEntity) {
        List<Score> scores = new ArrayList<>();
        double currentDS = 1;
        int codeLocationCounterBestCase = 0;
        int codeLocationCounterWorstCase = 0;
        if (signatures.size() == 0)
            Log.out(this, "score with %s signatures in %s", signatures.size() , buggyEntity.getUniqueIdentifier());
        if (targets.size() == 0)
            Log.out(this, "score with %s targets in %s", targets.size(), buggyEntity.getUniqueIdentifier());
        currentDS = signatures.keySet().iterator().next().DS;
        for (Map.Entry<Signature.Identifier, Signature> entry : signatures.entrySet()) {
            Signature.Identifier key = entry.getKey();
            Signature signature = entry.getValue();
            if (key.DS == currentDS) {
                codeLocationCounterWorstCase += signature.locations.size();
            }
            else if (key.DS < currentDS) {
                codeLocationCounterBestCase += codeLocationCounterWorstCase + 1;
                codeLocationCounterWorstCase += signature.locations.size();
                currentDS = key.DS;
            }
            else {
                throw new RuntimeException("signatures need to be sorted");
            }
            for (Predicate predicate : signature.predicates) {
                for (String predicateLocation : predicate.getLocation()) {
                    for (CodeLocation target : targets) {
                        Score score = new Score(calculateScore(predicateLocation, target, buggyEntity), codeLocationCounterBestCase, codeLocationCounterWorstCase, currentDS);
                        scores.add(score);
                    }
                }
            }
        }
        Optional<Score> best = scores.stream().min(Comparator.comparingDouble(Score::getBestScore));
        return best.orElse(new Score());
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
        return Double.POSITIVE_INFINITY;
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

    private static class Score {
        double PathCost;
        int CodeLocationsBeforeBestCase;
        int CodeLocationsBeforeWorstCase;
        double DS;
        boolean Failed;

        public Score(double pathCost, int codeLocationCounterBestCase, int codeLocationCounterWorstCase, double currentDS) {
            this.PathCost = pathCost;
            this.CodeLocationsBeforeBestCase = codeLocationCounterBestCase;
            this.CodeLocationsBeforeWorstCase = codeLocationCounterWorstCase;
            this.DS = currentDS;
        }

        public Score() {
            this.Failed = true;
        }

        public double getBestScore() {
            return this.CodeLocationsBeforeBestCase + (this.PathCost * Math.sqrt(this.CodeLocationsBeforeBestCase));
        }

        public double getWorstScore() {
            return this.CodeLocationsBeforeWorstCase + (this.PathCost * Math.sqrt(this.CodeLocationsBeforeWorstCase));
        }

        public double getAverageScore() {
            return 0.5 * (this.getBestScore() + this.getWorstScore());
        }
    }
}

