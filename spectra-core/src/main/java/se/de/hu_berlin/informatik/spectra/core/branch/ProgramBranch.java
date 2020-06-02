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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A program branch is a list of program elements.
 * In this case modeled as a list of Source Code Blocks.
 */
public class ProgramBranch implements Iterable<SourceCodeBlock>, Shortened, Comparable<ProgramBranch>, Indexable<ProgramBranch>, Cloneable {

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
        Iterator<SourceCodeBlock> branchElements = iterator();
        while (branchElements.hasNext()) {
            builder.append(IDENTIFIER_SEPARATOR_CHAR)
            .append(branchElements.next().toString());
        }
        return builder.toString();
    }

	@Override
    public Iterator<SourceCodeBlock> iterator() {
    	int[] subTraceSequence = programBranchSpectra.getSubTraceSequenceMap().get(id);
    	if (subTraceSequence == null) {
    		return null;
    	}
    	CachedMap<int[]> nodeSequenceMap = programBranchSpectra.getNodeSequenceMap();
    	CachedMap<SourceCodeBlock> statementMap = programBranchSpectra.getStatementMap();
    	
    	return new Iterator<SourceCodeBlock>() {
    		int subTraceIndex = 0; // inner index
    		int subTraceSequenceIndex = 0; // outer index
            int[] currentNodeIdSequence;

            @Override
            public boolean hasNext() {
            	// need new sub trace? look for the next valid sub trace
            	if (currentNodeIdSequence == null || subTraceIndex >= currentNodeIdSequence.length) {
                	// check if we're at the end of the sequence
                	if (subTraceSequenceIndex >= subTraceSequence.length) {
                		return false;
                	}
                	
            		// we're at the end of the current sub trace sequence! (or there is none)
            		currentNodeIdSequence = null;
            		// try to get the next valid sub trace!
            		while (subTraceSequenceIndex < subTraceSequence.length) {
            			// get the next sub trace from the current sub trace sequence
            			currentNodeIdSequence = nodeSequenceMap.get(subTraceSequence[subTraceSequenceIndex++]);
            			if (currentNodeIdSequence.length > 0) {
            				// found a "good" sequence
            				//                        	System.out.println("sub seq start");
            				subTraceIndex = 0;
            				break;
            			}
            			currentNodeIdSequence = null;
            		}
            	}

            	// if we found a valid sub trace, it should be non-null
            	return currentNodeIdSequence != null;
            }

            @Override
            public SourceCodeBlock next() {
                return statementMap.get(currentNodeIdSequence[subTraceIndex++]);
            }
		};
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
    		return null;
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
