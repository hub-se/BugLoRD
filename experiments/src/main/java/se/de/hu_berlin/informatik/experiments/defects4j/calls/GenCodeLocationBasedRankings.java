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
import soot.SootResolver;
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
    private final String type;


    public GenCodeLocationBasedRankings(String suffix, String type) {
        super();
        this.suffix = suffix;
        this.subDirName = "predicates";
        this.type = type;
    }

    @Override
    public BuggyFixedEntity<?> processItem(BuggyFixedEntity<?> buggyEntity) {
        Log.out(this, "Processing %s.", buggyEntity);
        Log.out(this, "%s mode", this.type);

        Entity bug = buggyEntity.getBuggyVersion();
        Log.out(this, "checking out bug");
        buggyEntity.requireBug(true);
        Log.out(this, "compiling bug");
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

        Score score = new Score();
        if (this.type.equals("predicates")) {

            LinkedHashMap<Signature.Identifier, Signature> signatures = this.readFromFile(bug.getWorkDataDir().toString(), "signatures.dat");


//            //resolve Code locations
//            String folder = Paths.get(rankingDir.resolve(subDirName).toString()).toString();
//            Output.readFromFile(folder);
//            Output.writeToHumanFile(folder);


            Log.out(this, "getTargets %s.", buggyEntity.getUniqueIdentifier());
            List<CodeLocation> targets = getTargets(buggyEntity);

            Log.out(this, "Scoring %s.", buggyEntity.getUniqueIdentifier());
            score = getScore(signatures, targets, buggyEntity);

            Log.out(this, "Finish scoring %s.", buggyEntity.getUniqueIdentifier());
        }
        else if (this.type.equals("sbfl")) {
            Log.out(this, "getting lines");
            LinkedList<Op2Line> lines = getDstarLines(bug);
            if (lines.size() > 0) {
                Log.out(this, "found %s lines", lines.size());
                Log.out(this, "getTargets %s.", buggyEntity.getUniqueIdentifier());
                List<CodeLocation> targets = getTargets(buggyEntity);
                Log.out(this, "Scoring %s.", buggyEntity.getUniqueIdentifier());
                score = getScore(lines, targets, buggyEntity);
                Log.out(this, "Finish scoring %s.", buggyEntity.getUniqueIdentifier());
            }
        }
        else {
            Log.err(this, "Wrong type argument!");
        }

        //Output
        String line;
        if (score.Failed) {
            line = buggyEntity.getUniqueIdentifier() + ";" + Double.NaN + ";" + Double.NaN + ";" + Double.NaN + ";" + Double.NaN + ";" + Double.NaN;
        }
        else {
            line = buggyEntity.getUniqueIdentifier() + ";" + score.getBestScore() + ";" + score.getWorstScore() + ";" + score.getAverageScore() + ";" + score.PathCost + ";" + score.DS;
        }
        ScoringFileWriter.getInstance().write(line);

        Log.out(this, "Score for %s is %f.", buggyEntity.getUniqueIdentifier(), score.getBestScore());

        return buggyEntity;
    }

    private LinkedList<Op2Line> getDstarLines(Entity bug) {
        LinkedList<Op2Line> lines = new LinkedList<>();
        BufferedReader csvReader;
        try {
            csvReader = new BufferedReader(new FileReader(bug.getWorkDataDir().resolve("ranking").resolve("dstar").resolve("ranking.rnk").toString()));
        String row;
        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(":");
            if (Double.parseDouble(data[6]) == 0.0)
                break;
            lines.add(new Op2Line(data));
        }
        csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
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
        if (signatures == null  || signatures.size() == 0) {
            Log.out(this, "score with 0 signatures in %s", buggyEntity.getUniqueIdentifier());
            return new Score();
        }
        if (targets.size() == 0)
            Log.out(this, "score with 0 targets in %s", buggyEntity.getUniqueIdentifier());
        currentDS = signatures.keySet().iterator().next().DS;
        for (Map.Entry<Signature.Identifier, Signature> entry : signatures.entrySet()) {
            Signature.Identifier key = entry.getKey();
            Signature signature = entry.getValue();
            if (key.DS == currentDS) {
                codeLocationCounterWorstCase += signature.locations.size();
            }
            else if (key.DS < currentDS) {
                codeLocationCounterBestCase += codeLocationCounterWorstCase;
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

    private Score getScore(LinkedList<Op2Line> signatures, List<CodeLocation> targets, BuggyFixedEntity<?> buggyEntity) {
        List<Score> scores = new ArrayList<>();
        if (signatures.size() == 0)
            Log.out(this, "score with 0 signatures in %s",  buggyEntity.getUniqueIdentifier());
        if (targets.size() == 0)
            Log.out(this, "score with 0 targets in %s",  buggyEntity.getUniqueIdentifier());
        for (Op2Line entry : signatures) {
            for (Op2Line sig : signatures) {
                if (entry.suspicion <= sig.suspicion)
                    entry.codeLocationCounterWorstCase++;
                if (entry.suspicion < sig.suspicion)
                    entry.codeLocationCounterBestCase++;
            }
        }

        for (Op2Line line : signatures) {
            for (CodeLocation target : targets) {
                Score score = new Score(calculateScore(line.location, target, buggyEntity), line.codeLocationCounterBestCase, line.codeLocationCounterWorstCase, line.suspicion);
                scores.add(score);
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
                    predicateLocations.add(new CodeLocation(unit, className, sootMethod, sc));
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
            else {
                Iterator<Edge> iteratorOnCallsOutOfGoalMethod = sc.getIteratorOnCallsOutOfMethod(goal.method);
                Iterator<Edge> iteratorOnCallsOutOfTargetMethod = null;
                if (target.sootConnector != null)
                    iteratorOnCallsOutOfTargetMethod = target.sootConnector.getIteratorOnCallsOutOfMethod(target.method);
                int classPenalty = 0;
                if (goal.className.equals(target.className))
                    classPenalty = 25;
                List<Double> edgeDistances = new ArrayList<>();
                //goal -> target
                while (iteratorOnCallsOutOfGoalMethod.hasNext()) {
                    Edge edge = iteratorOnCallsOutOfGoalMethod.next();
                    if (edge.tgt() == target.method) {
                        Integer internDistanceInGoal = this.getDistanceInUnits(sc, goal.method, goal.unit, edge.srcUnit());
                        if (internDistanceInGoal == null)
                            continue; //we wont get a result that is fair to compare
                        Integer internDistanceInTarget = this.getDistanceInUnits(sc, target.method, target.unit, edge.tgt().getActiveBody().getUnits().getFirst());
                        if (internDistanceInTarget == null)
                            continue; //we wont get a result that is fair to compare
                        Log.out(this, "Found a edge connection!");
                        edgeDistances.add((double) (internDistanceInGoal + internDistanceInTarget + classPenalty + 10)); //TODO EdgeCost
                    }
                }
                //target -> goal
                while (iteratorOnCallsOutOfTargetMethod != null && iteratorOnCallsOutOfTargetMethod.hasNext()) {
                    Edge edge = iteratorOnCallsOutOfTargetMethod.next();
                    if (edge.tgt() == goal.method) {
                        Integer internDistanceInGoal = this.getDistanceInUnits(sc, goal.method, goal.unit, edge.tgt().getActiveBody().getUnits().getFirst());
                        if (internDistanceInGoal == null)
                            continue; //we wont get a result that is fair to compare
                        Integer internDistanceInTarget = this.getDistanceInUnits(sc, target.method, target.unit, edge.srcUnit());
                        if (internDistanceInTarget == null)
                            continue; //we wont get a result that is fair to compare
                        Log.out(this, "Found a edge connection!");
                        edgeDistances.add((double) (internDistanceInGoal + internDistanceInTarget + classPenalty + 10)); //TODO EdgeCost
                    }
                }
                return edgeDistances.stream().min(Double::compareTo).orElse(Double.POSITIVE_INFINITY);
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
                List<SootMethod> methods = new ArrayList<>();
                SootConnector sc = null;
                try {
                    sc = SootConnector.getInstance(pck, className, classPaths);
                    methods = sc.getAllMethods();
                }
                catch (SootResolver.SootClassNotFoundException ex) {
                    Log.out(this, "SootClassNotFoundException %s", ex.getMessage());
                }
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
                                targets.add(new CodeLocation(unit, className, sootMethod, sc));
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
            return this.CodeLocationsBeforeBestCase + (this.PathCost * Math.sqrt(this.CodeLocationsBeforeBestCase + 1));
        }

        public double getWorstScore() {
            return this.CodeLocationsBeforeWorstCase + (this.PathCost * Math.sqrt(this.CodeLocationsBeforeWorstCase + 1));
        }

        public double getAverageScore() {
            return 0.5 * (this.getBestScore() + this.getWorstScore());
        }
    }

    private static class Op2Line {
        public int codeLocationCounterWorstCase;
        public int codeLocationCounterBestCase;
        String location;  // target: org/apache/commons/math3/exception/util/ExceptionContext:174
        double suspicion;

        Op2Line(String[] data) { //0: package, 1: classfile, 2: method, 3,4: lines, 5: ?, 6: suspicion
            StringBuilder sBuild = new StringBuilder();
            String clazz = data[1].substring(0, data[1].length() - 5); //remove .java ending
            sBuild.append(clazz);
            sBuild.append(':');
            sBuild.append(data[3]); //line
            this.location = sBuild.toString();
            this.suspicion = Double.parseDouble(data[6]);
        }
    }
}

