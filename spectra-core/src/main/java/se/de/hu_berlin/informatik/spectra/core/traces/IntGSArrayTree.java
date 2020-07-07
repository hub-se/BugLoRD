package se.de.hu_berlin.informatik.spectra.core.traces;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.IntArrayIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.ReplaceableCloneableIterator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntGSArrayTree {

    // some element not contained in the input sequences TODO maybe set to 0?
    // in the future, negative indices will possibly point to node sequences, themselves...
//	public static final int SEQUENCE_END = -2;
    public static final int BAD_INDEX = -3;
    //	public static final GSTreeNode END_NODE = new GSTreeNode();
    public static final int SUCC_END = -1;
    private static final Integer END_NODE = -2;

    int[] newArray(int size) {
        return new int[size];
    }

    IntGSArrayTreeNode newTreeNode(IntGSArrayTree treeReference,
                                   ReplaceableCloneableIterator unprocessedIterator, int i) {
        return new IntGSArrayTreeNode(treeReference, unprocessedIterator, i);
    }

    IntGSArrayTreeNode newTreeNode(IntGSArrayTree treeReference,
                                   int[] remainingSequence, List<IntGSArrayTreeNode> existingEdges) {
        return new IntGSArrayTreeNode(treeReference, remainingSequence, existingEdges);
    }

    // the (virtual) root node has a lot of branches, the inner nodes should not branch that much
    // so we use a map here, and we use lists of edges in the inner nodes
    private final Map<Integer, IntGSArrayTreeNode> branches = new HashMap<>();

    private int endNodeCount = 0;

    // once created end nodes may be removed later when reinserting sequences; (no reinserting an more)
    public IntGSArrayTreeNode getNewEndNode() {
        return new IntGSArrayTreeEndNode(endNodeCount++);
    }

    public int getEndNodeCount() {
        return endNodeCount;
    }

    public boolean addSequence(int[] sequence) {
        if (sequence == null) {
            return false;
        }

        return __addSequence(new IntArrayIterator(sequence), sequence.length);
    }

    public boolean addSequence(ReplaceableCloneableIterator unprocessedIterator, int length) {
        if (unprocessedIterator == null) {
            return false;
        }

        return __addSequence(unprocessedIterator, length);
    }

    boolean __addSequence(ReplaceableCloneableIterator unprocessedIterator, int length) {
        if (length == 0) {
            System.out.println("adding empty sequence..."); // this should not occur, normally
            if (!branches.containsKey(END_NODE)) {
                branches.put(END_NODE, getNewEndNode());
            }
            return true;
        }
        int firstElement = unprocessedIterator.peek();

        return __addSequence(unprocessedIterator, length, firstElement);
    }

    boolean __addSequence(ReplaceableCloneableIterator unprocessedIterator, int length, int firstElement) {
        IntGSArrayTreeNode startingNode = branches.get(firstElement);
        if (startingNode == null) {
            return addSequenceInNewBranch(unprocessedIterator, length, firstElement);
        } else {
//			System.out.println("adding existing: " + firstElement);
            // branch with this starting element already exists
            startingNode.addSequence(unprocessedIterator, length);
            return true;
        }
    }

    public boolean addSequenceInNewBranch(ReplaceableCloneableIterator unprocessedIterator, int length,
                                          int firstElement) {
        // new starting element
//			System.out.println("new start: " + firstElement);
        branches.put(firstElement, newTreeNode(this, unprocessedIterator, length));
        return true;
    }

//	public int getSequenceIndex(IntArraySequenceIndexer indexer, SingleLinkedArrayQueue<T> sequence) {
//		if (sequence == null) {
//			return BAD_INDEX;
//		}
//		if (sequence.isEmpty()) {
//			return indexer.getSequenceIdForEndNode(branches.get(getSequenceEndMarker()));
//		}
//
//		Iterator<T> iterator = sequence.iterator();
//		GSArrayTreeNode<T,K> startingNode = branches.get(getRepresentation(iterator.next()));
//		if (startingNode != null) {
//			// some sequence with this starting element exists in the tree
//			return startingNode.getSequenceIndex(indexer, iterator, sequence.size());
//		} else {
//			// no sequence with this starting element exists in the tree
//			return BAD_INDEX;
//		}
//	}


    public boolean checkIfStartingElementExists(int elementRep) {
        return branches.containsKey(elementRep);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GS Tree: ").append(branches.values().size()).append(" different starting elements, ").append(endNodeCount).append(" sequences");
        sb.append(System.lineSeparator());
        // iterate over all different branches (starting with the same element)
        for (IntGSArrayTreeNode node : branches.values()) {
            collectAllSuffixes("", node, sb);
        }
        return sb.toString();
    }

    private void collectAllSuffixes(String sequence, IntGSArrayTreeNode node, StringBuilder sb) {
        if (node instanceof IntGSArrayTreeEndNode) {
            sb.append(sequence).append("#");
            sb.append(System.lineSeparator());
            return;
        }

        sequence += "#";
        StringBuilder sequenceBuilder = new StringBuilder(sequence);
        for (final int element : node.getSequence()) {
            sequenceBuilder.append(element).append(",");
        }
        sequence = sequenceBuilder.toString();
        for (IntGSArrayTreeNode edge : node.getEdges()) {
            collectAllSuffixes(sequence, edge, sb);
        }
    }


    public int countAllSuffixes() {
        return endNodeCount;
//		int count = 0;
//		for (GSArrayTreeNode<T,K> node : branches.values()) {
//			count += countAllSuffixes(node);
//		}
//		endNodeCount = count;
//		return count;
    }

//	private int countAllSuffixes(GSArrayTreeNode<T,K> node) {
//		if (node instanceof GSArrayTreeEndNode) {
//			return 1;
//		}
//		
//		int count = 0;
//		for (GSArrayTreeNode<T,K> edge : node.getEdges()) {
//			count += countAllSuffixes(edge);
//		}
//		return count;
//	}

    public Map<Integer, IntGSArrayTreeNode> getBranches() {
        return branches;
    }

//	public ExecutionTrace generateIndexedTrace(
//			EfficientCompressedIntegerTrace rawTrace, IntArraySequenceIndexer indexer) {
//		if (rawTrace == null) {
//			return null;
//		}
//		
//		if (rawTrace.getCompressedTrace().isEmpty()) {
//			rawTrace.sleep();
//			return new ExecutionTrace(
//					rawTrace.getCompressedTrace().getOutputDir(), UUID.randomUUID().toString(), 
//					EXECUTION_TRACE_CHUNK_SIZE, MAP_CHUNK_SIZE, true);
//		}
//		
//		if (!indexer.isIndexed()) {
//			indexer.generateSequenceIndex();
//		}
//		
//		ExecutionTrace indexedtrace = new ExecutionTrace(
//				rawTrace.getCompressedTrace().getOutputDir(), UUID.randomUUID().toString(), 
//				EXECUTION_TRACE_CHUNK_SIZE, MAP_CHUNK_SIZE, true);
//		
//		TraceIterator iterator = rawTrace.iterator();
//		int startElement = iterator.next();
//		while (startElement != SUCC_END) {
//			startElement = addNextSequenceIndexToTrace(indexer, startElement, iterator, indexedtrace);
//			if (startElement == BAD_INDEX) {
//				System.err.flush();
//				throw new IllegalStateException("Could not get index for a sequence in the input trace.");
//			}
//		}
//		rawTrace.sleep();
//		
//		return indexedtrace;
//	}
//	
//	public int addNextSequenceIndexToTrace(IntArraySequenceIndexer indexer, int firstElement, 
//			TraceIterator rawTraceIterator, EfficientCompressedIntegerTrace indexedtrace) {
//		IntGSArrayTreeNode startingNode = branches.get(firstElement);
//		if (startingNode != null) {
//			// some sequence with this starting element exists in the tree
//			return startingNode.getNextSequenceIndex(indexer, rawTraceIterator, indexedtrace);
//		} else {
//			// no sequence with this starting element exists in the tree
//			System.err.println("No sequence starting with " + firstElement);
//			return BAD_INDEX;
//		}
//	}

}
