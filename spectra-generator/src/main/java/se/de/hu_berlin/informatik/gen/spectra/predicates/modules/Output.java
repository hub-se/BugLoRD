package se.de.hu_berlin.informatik.gen.spectra.predicates.modules;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Output {

    public static Map<Integer, Predicate> Predicates = new HashMap<>();
    public static Map<Integer, Integer> Triggers = new HashMap<>();
    public static int nextPredicateNumber;
    public Output()
    {
    }

    public static void addPredicate(Predicate newPredicate){
        Predicates.put(newPredicate.id,newPredicate);
    }

    public static void triggerPredicate(int id){
        Triggers.put(id, Triggers.getOrDefault(id, 0) + 1);
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

    public static void writeToFile(File outputDir) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputDir.getParent() + "/Predicates.db"));
        oos.writeObject(Predicates);
        oos.flush();
        oos.close();
    }

    public static void writeToHumanFile(File outputDir) {
        if (Predicates.isEmpty())
            return;
        try {
            FileWriter fw = new FileWriter(outputDir.getParent() + "/Predicates.csv");
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


