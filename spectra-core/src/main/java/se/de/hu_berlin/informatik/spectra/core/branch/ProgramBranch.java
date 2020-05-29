package se.de.hu_berlin.informatik.spectra.core.branch;

import com.google.common.collect.ImmutableList;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.util.Indexable;
import se.de.hu_berlin.informatik.spectra.util.Shortened;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A program branch is a list of program elements.
 * In this case modeled as a list of Source Code Blocks.
 */
public class ProgramBranch implements Shortened, Comparable<ProgramBranch>, Indexable<ProgramBranch>, Cloneable {

    private int id;

	/*====================================================================================
     * CONSTRUCTORS
     *====================================================================================*/

    public ProgramBranch(int id, SourceCodeBlock... programBlocks) {

        this(id, Arrays.asList(programBlocks));

        /*====================================================================================*/
        //probably check if branch is gapless, but need better data structures probably
        /*====================================================================================*/



        /*====================================================================================*/
        assert (Arrays.asList(programBlocks).equals(this.getElements()));
        /*====================================================================================*/

    }
 
    public ProgramBranch(int id, List<SourceCodeBlock> programBlocks) {

    	
        /*====================================================================================*/
        //probably check if branch is gapless, but need better data structures probably
        /*====================================================================================*/

    	this.id = id;
		// immutable lists prohibit the removal of nodes which we need to, for example, remove statements in test classes
//        this.branchElements = ImmutableList.copyOf(programBlocks);
        this.branchElements = programBlocks;

        //store the hashCode now
        int immutableHashCode = 31 * (527 + branchElements.size());
        for (SourceCodeBlock block : branchElements) {
            immutableHashCode = immutableHashCode * 31 + block.hashCode();
        }
        this.immutableHashCode = immutableHashCode;

        /*====================================================================================*/
        assert (programBlocks.equals(this.getElements()));
        /*====================================================================================*/

    }

    public ProgramBranch(int id, SourceCodeBlock programBlock) {

        /*====================================================================================*/
        //probably check if branch is gapless, but need better data structures probably
        /*====================================================================================*/

    	this.id = id;
        this.branchElements = ImmutableList.of(programBlock);

        //store the hashCode now
        int immutableHashCode = 31 * (527 + branchElements.size());
        for (SourceCodeBlock block : branchElements) {
            immutableHashCode = immutableHashCode * 31 + block.hashCode();
        }
        this.immutableHashCode = immutableHashCode;
    }

    /*====================================================================================
     * OVERRIDE OBJECT
     *====================================================================================*/

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.id);
        for (SourceCodeBlock block : branchElements) {
            builder.append(IDENTIFIER_SEPARATOR_CHAR)
            .append(block.toString());
        }
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return immutableHashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProgramBranch) {
            ProgramBranch o = (ProgramBranch) obj;
            if (this.id != o.id) {
            	// this is a workaround for generating branch spectra from equal sub trace sequences...
            	return false;
            }
            if (this.getLength() != o.getLength()) {
                return false;
            }
            for (int i = 0; i < this.getLength(); ++i) {
                if (!this.getElement(i).equals(o.getElement(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /*====================================================================================
     * OVERRIDE COMPARABLE
     *====================================================================================*/

    @Override
    public int compareTo(ProgramBranch o) {
        if (this.getLength() == 0 && o.getLength() == 0) {
            return 0;
        }
        if (this.getLength() == 0 && o.getLength() != 0) {
            return -1;
        }
        if (this.getLength() != 0 && o.getLength() == 0) {
            return 1;
        }
        // first element in the trace decides the order...
        return this.getElement(0).compareTo(o.getElement(0));
    }

    /*====================================================================================
     * OVERRIDE INDEXABLE
     *====================================================================================*/

    @Override
    public ProgramBranch getOriginalFromIndexedIdentifier(String identifier, Map<Integer, String> map) throws IllegalArgumentException {
        String[] elements = identifier.split(ProgramBranch.IDENTIFIER_SEPARATOR_CHAR);
        SourceCodeBlock[] trace = new SourceCodeBlock[elements.length-1];
        for (int i = 0; i < trace.length; i++) {
            try {
                trace[i] = SourceCodeBlock.DUMMY.getOriginalFromIndexedIdentifier(elements[i+1], map);
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Wrong input format: '" + identifier + "'.");
            }
        }
        return new ProgramBranch(Integer.valueOf(elements[0]), trace);
    }

    @Override
    public String getIndexedIdentifier(ProgramBranch original, Map<String, Integer> map) {
        StringBuilder builder = new StringBuilder();
        builder.append(original.id);
        for (SourceCodeBlock block : original.getElements()) {
            builder.append(IDENTIFIER_SEPARATOR_CHAR)
            .append(SourceCodeBlock.DUMMY.getIndexedIdentifier(block, map));
        }
        return builder.toString();
    }

    /*====================================================================================
     * OVERRIDE FROMSTRING
     *====================================================================================*/

    @Override
    public ProgramBranch getFromString(String identifier) throws IllegalArgumentException {
        return getNewProgramBranchFromString(identifier);
    }

    public static ProgramBranch getNewProgramBranchFromString(String identifier) throws IllegalArgumentException {
        String[] elements = identifier.split(IDENTIFIER_SEPARATOR_CHAR);
        SourceCodeBlock[] trace = new SourceCodeBlock[elements.length-1];
        for (int i = 0; i < trace.length; i++) {
            trace[i] = SourceCodeBlock.getNewBlockFromString(elements[i+1]);
        }
        return new ProgramBranch(Integer.valueOf(elements[0]), trace);
    }

    /*====================================================================================
     * OVERRIDE SHORTENED
     *====================================================================================*/

    @Override
    public String getShortIdentifier() throws IllegalArgumentException {
        StringBuilder builder = new StringBuilder();
        builder.append(this.id);
        for (SourceCodeBlock block : getElements()) {
            builder.append(IDENTIFIER_SEPARATOR_CHAR).append(block.getShortIdentifier());
        }
        return builder.toString();
    }

    /*====================================================================================
     * OVERRIDE CLONEABLE
     *====================================================================================*/

    @Override
    public ProgramBranch clone() {

        /*====================================================================================*/
        //
        /*====================================================================================*/

        ProgramBranch clonedBranch;

        SourceCodeBlock[] clonedBranchElements = new SourceCodeBlock[this.getLength()];
        for (int i = 0; i < this.getLength(); i++) {
            clonedBranchElements[i] = this.getElement(i).clone();
        }

        clonedBranch = new ProgramBranch(this.id, clonedBranchElements);

        /*====================================================================================*/
        assert (this.equals(clonedBranch));
        assert (this != clonedBranch);
        /*====================================================================================*/

        return clonedBranch;
    }

    /*====================================================================================
     * GETTER
     *====================================================================================*/

    
    public int getLength() {
        return this.branchElements.size();
    }

    public int getId() {
		return id;
	}

	public SourceCodeBlock getElement(int index) {
        return this.branchElements.get(index);
    }

    public List<SourceCodeBlock> getElements() {
        return this.branchElements;
    }

    //DANGER : temporary stuff here to make things "work", this should really be made better by e.g. making an abstract class called ProgramElement or something
    public String getFilePath() {
        return this.getElement(0).getFilePath();
    }

    public List<String> getFilePaths() {
        List<String> filePaths = new ArrayList<>();
        for (SourceCodeBlock element : this.getElements()) {
            filePaths.add(element.getFilePath());
        }
        return filePaths;
    }

    public List<String> getMethodNames() {
        List<String> methodNames = new ArrayList<>();
        for (SourceCodeBlock element : this.getElements()) {
            methodNames.add(element.getFilePath() + element.getMethodName());
        }
        return methodNames;
    }

    public int getStartLineNumber() {
        return this.getElement(0).getStartLineNumber();
    }

    public int getEndLineNumber() {
        return this.getElement(this.getLength() - 1).getEndLineNumber();
    }
    //DANGER over

    /*====================================================================================
     * CHECKS
     *====================================================================================*/

    /*====================================================================================
     * FIELDS
     *====================================================================================*/

    private List<SourceCodeBlock> branchElements;

    public final static String IDENTIFIER_SEPARATOR_CHAR = "#";

    public final static String UNKNOWN_ELEMENT = "_";

    public static final ProgramBranch DUMMY = new ProgramBranch(-1, SourceCodeBlock.DUMMY);

    private final int immutableHashCode;

}
