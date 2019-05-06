package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CompressedTraceBase;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ExecutionTraceCollector;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.BufferedArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ArrayIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CloneableIterator;

public abstract class GSArrayTree<T,K> {
	
	// some element not contained in the input sequences TODO maybe set to 0?
	// in the future, negative indices will possibly point to node sequences, themselves...
//	public static final int SEQUENCE_END = -2;
	public static final int BAD_INDEX = -3;
//	public static final GSTreeNode END_NODE = new GSTreeNode();

	// the (virtual) root node has a lot of branches, the inner nodes should not branch that much
	// so we use a map here, and we use lists of edges in the inner nodes
	private final Map<K, GSArrayTreeNode<T,K>> branches = new HashMap<>();
	
	private int endNodeCount = 0;
	
	// once created end nodes may be removed later when reinserting sequences; (no reinserting an more)
	public GSArrayTreeNode<T,K> getNewEndNode() {
		return new GSArrayTreeEndNode<>(endNodeCount++);
	}
	
	abstract K getSequenceEndMarker();
	abstract K getBadIndexMarker();
	abstract K getSuccessfulEndMarker();
	abstract K getRepresentation(T element);
	abstract T[] newArray(int size);
	
	public int getEndNodeCount() {
		return endNodeCount;
	}
	
	public boolean addSequence(T[] sequence) {
		if (sequence == null) {
			return false;
		}
		
		return __addSequence(new ArrayIterator<>(sequence), sequence.length);
	}
	
	public boolean addSequence(CloneableIterator<T> unprocessedIterator, int length) {
		if (unprocessedIterator == null) {
			return false;
		}

		return __addSequence(unprocessedIterator, length);
	}
	
	boolean __addSequence(CloneableIterator<T> unprocessedIterator, int length) {
		if (length == 0) {
			System.out.println("adding empty sequence..."); // this should not occur, normally
			if (!branches.containsKey(getSequenceEndMarker())) {
				branches.put(getSequenceEndMarker(), getNewEndNode());
			}
			return true;
		}
		T firstElement = unprocessedIterator.peek();
		
		return __addSequence(unprocessedIterator, length, getRepresentation(firstElement));
	}
	
	boolean __addSequence(CloneableIterator<T> unprocessedIterator, int length, K firstElement) {
		GSArrayTreeNode<T,K> startingNode = branches.get(firstElement);
		if (startingNode == null) {
			return addSequenceInNewBranch(unprocessedIterator, length, firstElement);
		} else {
//			System.out.println("adding existing: " + firstElement);
			// branch with this starting element already exists
			startingNode.addSequence(unprocessedIterator, length);
			return true;
		}
	}

	public boolean addSequenceInNewBranch(CloneableIterator<T> unprocessedIterator, int length,
			K firstElement) {
		// new starting element
//			System.out.println("new start: " + firstElement);
		branches.put(firstElement, newTreeNode(this, unprocessedIterator, length));
		return true;
	}
	
	abstract GSArrayTreeNode<T, K> newTreeNode(GSArrayTree<T, K> treeReference2,
			CloneableIterator<T> unprocessedIterator, int i);
	
	abstract GSArrayTreeNode<T, K> newTreeNode(GSArrayTree<T, K> treeReference2, T[] remainingSequence,
			List<GSArrayTreeNode<T, K>> existingEdges);
	

	public int getSequenceIndex(ArraySequenceIndexer<T,K> indexer, SingleLinkedArrayQueue<T> sequence) {
		if (sequence == null) {
			return BAD_INDEX;
		}
		if (sequence.isEmpty()) {
			return indexer.getSequenceIdForEndNode(branches.get(getSequenceEndMarker()));
		}

		Iterator<T> iterator = sequence.iterator();
		GSArrayTreeNode<T,K> startingNode = branches.get(getRepresentation(iterator.next()));
		if (startingNode != null) {
			// some sequence with this starting element exists in the tree
			return startingNode.getSequenceIndex(indexer, iterator, sequence.size());
		} else {
			// no sequence with this starting element exists in the tree
			return BAD_INDEX;
		}
	}
	
	
	public boolean checkIfStartingElementExists(K elementRep) {
		return branches.containsKey(elementRep);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("GS Tree: ").append(branches.values().size()).append(" different starting elements, ").append(endNodeCount).append(" sequences");
		sb.append(System.lineSeparator());
		// iterate over all different branches (starting with the same element)
		for (GSArrayTreeNode<T,K> node : branches.values()) {
			collectAllSuffixes("", node, sb);
		}
		return sb.toString();
	}

	private void collectAllSuffixes(String sequence, GSArrayTreeNode<T,K> node, StringBuilder sb) {
		if (node instanceof GSArrayTreeEndNode) {
			sb.append(sequence).append("#");
			sb.append(System.lineSeparator());
			return;
		}
		
		sequence += "#";
		StringBuilder sequenceBuilder = new StringBuilder(sequence);
		for (final T element : node.getSequence()) {
			sequenceBuilder.append(getRepresentation(element)).append(",");
		}
		sequence = sequenceBuilder.toString();
		for (GSArrayTreeNode<T,K> edge : node.getEdges()) {
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
	
	public Map<K, GSArrayTreeNode<T,K>> getBranches() {
		return branches;
	}

	public BufferedArrayQueue<Integer> generateIndexedTrace(
			CompressedTraceBase<T, ?> rawTrace, ArraySequenceIndexer<T,K> indexer) {
		if (rawTrace == null) {
			return null;
		}
		
		if (rawTrace.getCompressedTrace().isEmpty()) {
			return new BufferedArrayQueue<>(
					rawTrace.getCompressedTrace().getOutputDir(), UUID.randomUUID().toString(), ExecutionTraceCollector.CHUNK_SIZE);
		}
		
		if (!indexer.isIndexed()) {
			indexer.generateSequenceIndex();
		}
		
		BufferedArrayQueue<Integer> indexedtrace = new BufferedArrayQueue<>(
				rawTrace.getCompressedTrace().getOutputDir(), UUID.randomUUID().toString(), ExecutionTraceCollector.CHUNK_SIZE);
		
		Iterator<T> iterator = rawTrace.iterator();
		K startElement = getRepresentation(iterator.next());
		while (!startElement.equals(getSuccessfulEndMarker())) {
			startElement = addNextSequenceIndexToTrace(indexer, startElement, iterator, indexedtrace);
			if (startElement.equals(getBadIndexMarker())) {
				System.err.flush();
				throw new IllegalStateException("Could not get index for a sequence in the input trace.");
			}
		}

		return indexedtrace;
	}
	
	public K addNextSequenceIndexToTrace(ArraySequenceIndexer<T,K> indexer, K firstElement, 
			Iterator<T> rawTraceIterator, Queue<Integer> indexedtrace) {
		GSArrayTreeNode<T,K> startingNode = branches.get(firstElement);
		if (startingNode != null) {
			// some sequence with this starting element exists in the tree
			return startingNode.getNextSequenceIndex(indexer, rawTraceIterator, indexedtrace);
		} else {
			// no sequence with this starting element exists in the tree
			System.err.println("No sequence starting with " + firstElement);
			return getBadIndexMarker();
		}
	}
	
}
