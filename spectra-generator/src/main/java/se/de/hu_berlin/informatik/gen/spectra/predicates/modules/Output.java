package se.de.hu_berlin.informatik.gen.spectra.predicates.modules;


import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Output {

    public static Map<Integer, Predicate> Predicates = new HashMap<>();
    public static Map<Integer, Integer> Triggers = new ConcurrentHashMap<>();
    public static int nextPredicateNumber;

    public static JOINSTRATEGY joinStrategy = JOINSTRATEGY.PAIRS;
    private static Integer lastPredicateId = -1;
    private static final Map<Pair<Integer,Integer>,Integer> joinPredicateMap = new ConcurrentHashMap<>();
    private static boolean shouldWrite = false;
    private static Object lock;

    public Output()
    {
    }

    public enum JOINSTRATEGY {
        NONE,PAIRS,TRIPLES
    }

    public static void addPredicate(Predicate newPredicate){
        Predicates.put(newPredicate.id,newPredicate);
    }

    //trigger Predicate
    public static synchronized void triggerPredicate(int id){
        Triggers.put(id, Triggers.getOrDefault(id, 0) + 1);
        if (joinStrategy == JOINSTRATEGY.PAIRS) {

                if (!joinPredicateMap.containsKey(new Pair<>(id, lastPredicateId))) {
                    Predicate jointPredicate = new Predicate(id, lastPredicateId);
                    Predicates.put(jointPredicate.id, jointPredicate);
                    joinPredicateMap.put(new Pair<>(id, lastPredicateId), jointPredicate.id);
                }
                Triggers.put(joinPredicateMap.get(new Pair<>(id, lastPredicateId)), 1);
                shouldWrite = true;
        }
        lastPredicateId = id;

    }

    public static void outputPredicates(){
        for (Integer i: Predicates.keySet()) {
            Predicate entry = Predicates.get(i);
            if (entry.neverTriggered())
                continue;
             String string = entry.toString();
            System.out.println(string);
        }
    }

    public static void writeToFile(File outputDir, String filename, boolean forceWrite){
        if (forceWrite)
            shouldWrite = true;
        if (shouldWrite) {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputDir + "/" + filename));
                oos.writeObject(Predicates);
                oos.flush();
                oos.close();
            } catch (Exception ex) {
                Log.err(Output.class, ex);
            }
        }
    }

    public static void readFromFile(String outputDir) {
        try {
            ObjectInputStream ois;
            if (new File(outputDir + "/jointPredicates.db").exists()) {
                ois = new ObjectInputStream(new FileInputStream(outputDir + "/jointPredicates.db"));
            }
            else {
                ois = new ObjectInputStream(new FileInputStream(outputDir + "/Predicates.db"));
            }
            Predicates = ((Map<Integer, Predicate>) ois.readObject());
            ois.close();
            nextPredicateNumber = Predicates.size();
        }
        catch (Exception ex) {
            Log.err(Output.class, ex);
        }
        Predicates.values().stream().filter(predicate -> predicate.joined).forEach(predicate -> joinPredicateMap.put(new Pair<>(predicate.firstId, predicate.secondId), predicate.id));
    }

    public static void writeToHumanFile(String outputDir) {
        if (Predicates.isEmpty())
            return;
        try {
            FileWriter fw = new FileWriter(outputDir + "/Predicates.csv");
            StringBuilder line = new StringBuilder();
            for (Predicate pred : Predicates.values()) {
                line.append(pred.id).append(";").append(pred.toString());
                line.append(System.lineSeparator());
            }
            fw.append(line);
            fw.flush();
            fw.close();
        }
        catch (IOException ex) {
            System.out.println("IOException while writing to file");
        }
    }

    public static boolean writeTriggersToFile(String outputDir,boolean successful) {
        if (Triggers.isEmpty())
            return false;
        try {
            FileWriter fw = new FileWriter(outputDir + "/Triggers.csv",true);
            StringBuilder line = new StringBuilder();
            for (Integer i: Triggers.keySet()) {
                line.append(i).append(",");
                //  System.out.println(i + " : " + Triggers.get(i));
            }
            line.setLength(line.length() -1);
            line.append(";");
            line.append(successful);
            line.append(System.getProperty("line.separator"));
            fw.append(line);
            fw.flush();
            fw.close();
        }
        catch (IOException ex) {
            System.out.println("IOException while writing to file");
        }
        return true;
    }

    public static void resetTriggers() {
        Triggers.clear();
    }




}


