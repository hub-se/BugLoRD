package se.de.hu_berlin.informatik.spectra.core.branch;

import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.SingleLinkedArrayQueue;
import se.de.hu_berlin.informatik.spectra.util.CachedMap;
import se.de.hu_berlin.informatik.spectra.util.Indexable;
import se.de.hu_berlin.informatik.spectra.util.Shortened;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A program branch is a list of program elements.
 * In this case modeled as a list of Source Code Blocks.
 */
public class ProgramBranch implements Shortened, Comparable<ProgramBranch>, Indexable<ProgramBranch>, Cloneable {

    private final int id;
	private final ProgramBranchSpectra<?> programBranchSpectra;
	private final List<SourceCodeBlock> elements;

	/*====================================================================================
     * CONSTRUCTORS
     *====================================================================================*/

    public ProgramBranch(int id, ProgramBranchSpectra<?> programBranchSpectra) {
		this.id = id;
		this.programBranchSpectra = programBranchSpectra;
		this.elements = null;
  	}
    
    private ProgramBranch(int id, List<SourceCodeBlock> elements) {
		this.id = id;
		this.elements = elements;
		this.programBranchSpectra = null;
  	}

    /*====================================================================================
     * OVERRIDE OBJECT
     *====================================================================================*/

	@Override
    public String toString() {
		StringBuilder builder = new StringBuilder();
        builder.append(this.id);
        Collection<SourceCodeBlock> branchElements = getElements();
		for (SourceCodeBlock block : branchElements) {
            builder.append(IDENTIFIER_SEPARATOR_CHAR)
            .append(block.toString());
        }
        return builder.toString();
    }
    
    public String toReadableString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.id);
        Collection<SourceCodeBlock> branchElements = getElements();
		for (SourceCodeBlock block : branchElements) {
            builder.append(IDENTIFIER_SEPARATOR_CHAR)
            .append(block.toString());
        }
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return 31 * (527 + this.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProgramBranch) {
            ProgramBranch o = (ProgramBranch) obj;
            return this.id == o.id;
        } else {
            return false;
        }
    }

    /*====================================================================================
     * OVERRIDE COMPARABLE
     *====================================================================================*/

    @Override
    public int compareTo(ProgramBranch o) {
        return Integer.compare(this.id, o.id);
    }

    /*====================================================================================
     * OVERRIDE INDEXABLE
     *====================================================================================*/

    @Override
    public ProgramBranch getOriginalFromIndexedIdentifier(String identifier, Map<Integer, String> map) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getIndexedIdentifier(ProgramBranch original, Map<String, Integer> map) {
//        StringBuilder builder = new StringBuilder();
//        builder.append(original.id);
//        for (SourceCodeBlock block : original.getElements()) {
//            builder.append(IDENTIFIER_SEPARATOR_CHAR)
//            .append(SourceCodeBlock.DUMMY.getIndexedIdentifier(block, map));
//        }
//        return builder.toString();
    	return String.valueOf(original.id);
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
    	 int id = Integer.valueOf(elements[0]);
         List<SourceCodeBlock> trace = new ArrayList<>(elements.length-1);
         for (int i = 1; i < elements.length; i++) {
             trace.add(SourceCodeBlock.getNewBlockFromString(elements[i]));
         }
         return new ProgramBranch(id, trace);
    }


    /*====================================================================================
     * OVERRIDE SHORTENED
     *====================================================================================*/

    @Override
    public String getShortIdentifier() throws IllegalArgumentException {
//        StringBuilder builder = new StringBuilder();
//        builder.append(this.id);
//        for (SourceCodeBlock block : getElements()) {
//            builder.append(IDENTIFIER_SEPARATOR_CHAR).append(block.getShortIdentifier());
//        }
//        return builder.toString();
    	throw new UnsupportedOperationException();
    }

    /*====================================================================================
     * OVERRIDE CLONEABLE
     *====================================================================================*/

    @Override
    public ProgramBranch clone() {

        ProgramBranch clonedBranch = new ProgramBranch(this.id, programBranchSpectra);

        /*====================================================================================*/
        assert (this.equals(clonedBranch));
        assert (this != clonedBranch);
        /*====================================================================================*/

        return clonedBranch;
    }

    /*====================================================================================
     * GETTER
     *====================================================================================*/

    Integer size = null;
    public int getLength() {
    	if (size == null) {
    		size = getElements().size();
    	}
        return size;
    }

    public int getId() {
		return id;
	}

//	public SourceCodeBlock getElement(int index) {
//        return this.branchElements.get(index);
//    }

    /**
     * Generates list of executed statements. Avoid calling this multiple times!
     * @return list of executed statements
     */
    public Collection<SourceCodeBlock> getElements() {
    	if (elements != null) {
    		// usually, this is not the case; may happen when a branch was read from a ranking file
    		return elements;
    	}
    	
    	int[] subTraceSequence = programBranchSpectra.getSubTraceSequenceMap().get(id);
    	if (subTraceSequence == null) {
//    		throw new IllegalStateException("No branch found for id " + this.id);
    		return Collections.emptyList();
    	}
    	CachedMap<int[]> nodeSequenceMap = programBranchSpectra.getNodeSequenceMap();
    	
    	CachedMap<SourceCodeBlock> statementMap = programBranchSpectra.getStatementMap();
    	Collection<SourceCodeBlock> result = new SingleLinkedArrayQueue<SourceCodeBlock>(20);
    	for (int index : subTraceSequence) {
			int[] nodeIds = nodeSequenceMap.get(index);
			for (int i : nodeIds) {
				result.add(statementMap.get(i));
			}
		}
    	
        return result;
    }
    
    public Collection<? extends Integer> getExecutedNodeIDs() {
    	int[] subTraceSequence = programBranchSpectra.getSubTraceSequenceMap().get(id);
    	if (subTraceSequence == null) {
    		throw new IllegalStateException("No branch found for id " + this.id);
    	}
    	CachedMap<int[]> nodeSequenceMap = programBranchSpectra.getNodeSequenceMap();
    	
    	Set<Integer> result = new HashSet<>();
    	for (int index : subTraceSequence) {
			int[] nodeIds = nodeSequenceMap.get(index);
			for (int i : nodeIds) {
				result.add(i);
			}
		}
    	
        return result;
	}

    /*====================================================================================
     * CHECKS
     *====================================================================================*/

    /*====================================================================================
     * FIELDS
     *====================================================================================*/

    public final static String IDENTIFIER_SEPARATOR_CHAR = "#";

    public final static String UNKNOWN_ELEMENT = "_";

    public static final ProgramBranch DUMMY = new ProgramBranch(-1, Collections.emptyList());

	

}
