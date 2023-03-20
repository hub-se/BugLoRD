package se.de.hu_berlin.informatik.gen.spectra.predicates.modules;

import org.objectweb.asm.Opcodes;

import java.io.Serializable;
import java.util.HashSet;

public class Predicate implements Serializable
{
    public int id;
    int count;
    String description;
    int linenumber;
    String file;
    int firstVariableId;
    int secondVariableId;
    String comparisonType;
    boolean joined;
    Integer firstId;
    Integer secondId;



    public Predicate(String description, int linenumber, String File)
    {
        this.id = Output.nextPredicateNumber++;
        this.description = description;
        this.linenumber = linenumber;
        this.file = File;
    }

    public Predicate(int firstId, int secondId)
    {
        this.id = Output.nextPredicateNumber++;
        this.joined = true;
        this.firstId = firstId;
        this.secondId = secondId;
        this.description = "Joined Pred between " + firstId + " and " + secondId + " .";
    }

    public Predicate(int linenumber, String File, int firstVariableId, int secondVariableId, int comparisonType) //compare two vars
    {
        this.id = Output.nextPredicateNumber++;
        this.linenumber = linenumber;
        this.file = File;
        this.firstVariableId =  firstVariableId;
        this.secondVariableId = secondVariableId;
        this.comparisonType = getString(comparisonType);
        //description is set in the adapter after we know the variable names
    }

    public Predicate(int linenumber, String File, int comparisonType, String methodName) //Compare Method to 0
    {
        this.id = Output.nextPredicateNumber++;
        this.linenumber = linenumber;
        this.file = File;
        this.description = methodName + " returned" + this.getString(comparisonType) + "0";
    }

    public Predicate(int linenumber, String File, String comparisonType, int VariableId) //Compare ref to null
    {
        this.id = Output.nextPredicateNumber++;
        this.linenumber = linenumber;
        this.file = File;
        this.comparisonType = comparisonType;
        //description is set in the adapter after we know the variable names
    }



    public Predicate(int id){ //Debug only
        this.id = id;
    }

    @Override
    public String toString() {
        if (this.file != null) {
            return "(" + id + "," +
                    this.description + "," +
                    this.file + ":" + this.linenumber + ")";
        }
        return "(" + id + "," +
                this.description + ")";
    }

    public HashSet<String> getLocation() {
        HashSet<String> locations = new HashSet<>();
        if (this.joined) { //so much info lost we need a better method...
            Predicate firstPred = Output.Predicates.get(this.firstId);
            Predicate secondPred = Output.Predicates.get(this.secondId);
            if (firstPred != null)
                locations.addAll(firstPred.getLocation());
            if (secondPred != null)
                locations.addAll(secondPred.getLocation());
        }
        else {
            locations.add(this.file + ":" + this.linenumber);
        }
        return locations;
    }

    public boolean neverTriggered() {
        return count == 0;
    }

    public void trigger() {
        this.count++;
    }

    private String getString(int comparisonType) {
        String comparisonString;
        switch (comparisonType){ //swap for output
            case Opcodes.IFEQ:
                comparisonString = " != ";
                break;
            case Opcodes.IFNE:
                comparisonString = " == ";
                break;
            case Opcodes.IFLT:
                comparisonString = " >= ";
                break;
            case Opcodes.IFGE:
                comparisonString = " < ";
                break;
            case Opcodes.IFGT:
                comparisonString = " <= ";
                break;
            case Opcodes.IFLE:
                comparisonString = " > ";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + comparisonType);
        }
        return comparisonString;
    }
}
