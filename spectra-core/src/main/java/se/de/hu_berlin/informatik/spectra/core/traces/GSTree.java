package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CompressedTraceBase;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.BufferedArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.IntArrayIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CloneableIterator;

public class GSTree {
	
	// some element not contained in the input sequences TODO maybe set to 0?
	// in the future, negative indices will possibly point to node sequences, themselves...
	public static final int SEQUENCE_END = -2;
	public static final int BAD_INDEX = -3;
//	public static final GSTreeNode END_NODE = new GSTreeNode();

	// the (virtual) root node has a lot of branches, the inner nodes should not branch that much
	// so we use a map here, and we use lists of edges in the inner nodes
	private Map<Integer, GSTreeNode> branches = new HashMap<>();
	
	private int endNodeCount = 0;
	
	// once created end nodes may be removed later when reinserting sequences;
	// TODO can this be prohibited?
	public GSTreeNode getNewEndNode() {
		return new GSTreeNode(-1);
	}
	
	public int getEndNodeCount() {
		return endNodeCount;
	}
	
	public boolean addSequence(int[] sequence) {
		if (sequence == null) {
			return false;
		}
		
//		SingleLinkedArrayQueue<Integer> queue = new SingleLinkedArrayQueue<>();
//		for (int i : sequence) {
//			queue.add(i);
//		}
//		ExecutionTrace executionTrace = new ExecutionTrace(queue, true);
//		return __addSequence(executionTrace.iterator(), executionTrace.size());

		return __addSequence(new IntArrayIterator(sequence), sequence.length);
	}
	
//	public boolean addSequence(int[] sequence, int from, int to) {
//		if (sequence == null) {
//			return false;
//		}
//		
//		return __addSequence(sequence, from, to);
//	}
	
	public boolean addSequence(CloneableIterator<Integer> unprocessedIterator, int length) {
		if (unprocessedIterator == null) {
			return false;
		}

		return __addSequence(unprocessedIterator, length);
	}
	
//	public boolean addSequence(List<Integer> sequence) {
//		if (sequence == null) {
//			return false;
//		}
//		
//		return __addSequence(sequence.stream().mapToInt(i->i).toArray());
//	}
	
	boolean __addSequence(CloneableIterator<Integer> unprocessedIterator, int length) {
		if (length == 0) {
			System.out.println("adding empty sequence..."); // this should not occur, normally
			if (!branches.containsKey(Integer.valueOf(GSTree.SEQUENCE_END))) {
				branches.put(Integer.valueOf(SEQUENCE_END), getNewEndNode());
			}
			return true;
		}
		int firstElement = unprocessedIterator.peek();
		
		return __addSequence(unprocessedIterator, length, firstElement);
	}
	
	boolean __addSequence(CloneableIterator<Integer> unprocessedIterator, int length, int firstElement) {
		GSTreeNode startingNode = branches.get(Integer.valueOf(firstElement));
		if (startingNode == null) {
			return addSequenceInNewBranch(unprocessedIterator, length, firstElement);
		} else {
//			System.out.println("adding existing: " + firstElement);
			// branch with this starting element already exists
			startingNode.addSequence(unprocessedIterator, length);
			return true;
		}
	}

	public boolean addSequenceInNewBranch(CloneableIterator<Integer> unprocessedIterator, int length,
			int firstElement) {
		// new starting element
//			System.out.println("new start: " + firstElement);
		branches.put(Integer.valueOf(firstElement), new GSTreeNode(this, unprocessedIterator, length));
		
		// the following should not be necessary any more...
//		// check for the starting element in existing branches 
//		// and extract the remaining sequences
//		for (Entry<Integer, GSTreeNode> entry : branches.entrySet()) {
//			if (entry.getKey() == firstElement || entry.getKey() == SEQUENCE_END) {
//				continue;
//			}
////				System.out.println(entry.getKey() +" -> extracting: " + firstElement);
//			extractAndReinsertSequences(entry.getValue(), firstElement);
//		}
		return true;
	}

//	private boolean __addSequence(int[] sequence, int from, int to) {
//		if (from < 0 || to < 0 || to < from || to > sequence.length) {
//			return false;
//		}
//		
//		if (to - from == 0) {
//			if (!branches.containsKey(Integer.valueOf(GSTree.SEQUENCE_END))) {
//				branches.put(Integer.valueOf(SEQUENCE_END), getNewEndNode());
//			}
//			return true;
//		}
//		int firstElement = sequence[from];
//		
//		GSTreeNode startingNode = branches.get(Integer.valueOf(firstElement));
//		if (startingNode == null) {
//			// new starting element
//			branches.put(firstElement, new GSTreeNode(this, sequence, from, to));
//			// check for the starting element in existing branches 
//			// and extract the remaining sequences
//			for (Entry<Integer, GSTreeNode> entry : branches.entrySet()) {
//				if (entry.getKey() == firstElement || entry.getKey() == SEQUENCE_END) {
//					continue;
//				}
//				extractAndReinsertSequences(entry.getValue(), firstElement);
//			}
//			return true;
//		} else {
//			// branch with this starting element already exists
//			startingNode.addSequence(sequence, from, to);
//			return true;
//		}
//	}

//	private void extractAndReinsertSequences(GSTreeNode node, int firstElement) {
//		for (int i = 0; i < node.getSequence().length; ++i) {
//			if (node.getSequence()[i] == firstElement) {
//				// found the starting element
//				List<int[]> remainingSequences = node.extractAndRemoveRemainingSequences(i, firstElement);
//				// add the sequences to the tree
//				for (int[] sequence : remainingSequences) {
////					System.out.println("r seq: " + Arrays.toString(sequence));
//					IntArrayIterator unprocessedIterator = new IntArrayIterator(sequence);
//					__addSequence(unprocessedIterator, sequence.length, firstElement);
//					unprocessedIterator = null;
//				}
//				return;
//			}
//		}
//		
//		// when we are here, the element has NOT been found in the given node's sequence
//		boolean needsEndingEdge = false;
//		// check all edges recursively
//		for (Iterator<GSTreeNode> iterator = node.getEdges().iterator(); iterator.hasNext();) {
//			GSTreeNode edge = iterator.next();
//			if (edge.getFirstElement() == SEQUENCE_END) {
//				continue;
//			}
//			extractAndReinsertSequences(edge, firstElement);
//			if (edge.getFirstElement() == firstElement) {
//				// when removing the first element in a node, we remove the entire edge
//				iterator.remove();
//				needsEndingEdge = true;
//			}
//		}
//		
//		// check for an ending edge if we removed an entire node
//		if (needsEndingEdge) {
//			for (GSTreeNode edge : node.getEdges()) {
//				if (edge.getFirstElement() == SEQUENCE_END) {
//					return;
//				}
//			}
//			
//			// no ending edge found
//			node.getEdges().add(getNewEndNode());
//		}
//	}
	
	
	public boolean checkIfMatch(int[] sequence, int from, int to) {
		if (sequence == null) {
			return false;
		}
		
		return __checkIfMatch(sequence, from, to);
	}
	
	public boolean checkIfMatch(int[] sequence) {
		return checkIfMatch(sequence, 0, sequence.length);
	}
	
	public boolean checkIfMatch(List<Integer> sequence) {
		if (sequence == null) {
			return false;
		}
		
		return checkIfMatch(sequence.stream().mapToInt(i->i).toArray());
	}

	private boolean __checkIfMatch(int[] sequence, int from, int to) {
		if (from < 0 || to < 0 || to < from || to > sequence.length) {
			return false;
		}
		if (to - from == 0) {
			return branches.get(Integer.valueOf(SEQUENCE_END)) != null;
		}
		int firstElement = sequence[from];
		
		GSTreeNode startingNode = branches.get(Integer.valueOf(firstElement));
		if (startingNode != null) {
			// some sequence with this starting element exists in the tree
			return startingNode.checkIfMatch(sequence, from, to);
		} else {
			// no sequence with this starting element exists in the tree
			return false;
		}
	}

	public int getSequenceIndex(SequenceIndexer indexer, SingleLinkedArrayQueue<Integer> sequence) {
		if (sequence == null) {
			return BAD_INDEX;
		}
		if (sequence.isEmpty()) {
			return indexer.getSequenceIdForEndNode(branches.get(Integer.valueOf(SEQUENCE_END)));
		}

		Iterator<Integer> iterator = sequence.iterator();
		GSTreeNode startingNode = branches.get(iterator.next());
		if (startingNode != null) {
			// some sequence with this starting element exists in the tree
			return startingNode.getSequenceIndex(indexer, iterator, sequence.size());
		} else {
			// no sequence with this starting element exists in the tree
			return BAD_INDEX;
		}
	}
	
	public int addNextSequenceIndexToTrace(SequenceIndexer indexer, int firstElement, 
			Iterator<Integer> rawTraceIterator, Queue<Integer> indexedtrace) {
		GSTreeNode startingNode = branches.get(Integer.valueOf(firstElement));
		if (startingNode != null) {
			// some sequence with this starting element exists in the tree
			return startingNode.getNextSequenceIndex(indexer, rawTraceIterator, indexedtrace);
		} else {
			// no sequence with this starting element exists in the tree
			System.err.println("No sequence starting with " + firstElement);
			return BAD_INDEX;
		}
	}
	
	
	public boolean checkIfStartingElementExists(int element) {
		return branches.containsKey(Integer.valueOf(element));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("GS Tree: " + branches.values().size() + " different starting elements");
		sb.append(System.lineSeparator());
		// iterate over all different branches (starting with the same element)
		for (GSTreeNode node : branches.values()) {
			collectAllSuffixes("", node, sb);
		}
		return sb.toString();
	}

	private void collectAllSuffixes(String sequence, GSTreeNode node, StringBuilder sb) {
		if (node.getFirstElement() == SEQUENCE_END) {
			sb.append(sequence + "#");
			sb.append(System.lineSeparator());
			return;
		}
		
		sequence += "#";
		for (final int element : node.getSequence()) {
			sequence += element + ",";
		}
		for (GSTreeNode edge : node.getEdges()) {
			collectAllSuffixes(sequence, edge, sb);
		}
	}
	
	

	public int countAllSuffixes() {
		int count = 0;
		for (GSTreeNode node : branches.values()) {
			count += countAllSuffixes(node);
		}
		endNodeCount = count;
		return count;
	}

	private int countAllSuffixes(GSTreeNode node) {
		if (node.getFirstElement() == SEQUENCE_END) {
			return 1;
		}
		
		int count = 0;
		for (GSTreeNode edge : node.getEdges()) {
			count += countAllSuffixes(edge);
		}
		return count;
	}
	
	public Map<Integer, GSTreeNode> getBranches() {
		return branches;
	}

	public BufferedArrayQueue<Integer> generateIndexedTrace(CompressedTraceBase<Integer, ?> rawTrace, SequenceIndexer indexer) {
		if (rawTrace == null) {
			return null;
		}
		
		if (rawTrace.getCompressedTrace().isEmpty()) {
			return new BufferedArrayQueue<>(rawTrace.getCompressedTrace().getOutputDir(), UUID.randomUUID().toString(), 50000);
		}
		
		BufferedArrayQueue<Integer> indexedtrace = new BufferedArrayQueue<>(
				rawTrace.getCompressedTrace().getOutputDir(), UUID.randomUUID().toString(), 50000);
		
		Iterator<Integer> iterator = rawTrace.iterator();
		int startElement = iterator.next();
		while (startElement >= 0) {
			startElement = addNextSequenceIndexToTrace(indexer, startElement, iterator, indexedtrace);
			if (startElement == BAD_INDEX) {
				System.err.flush();
				throw new IllegalStateException("Could not get index for a sequence in the input trace.");
			}
		}

		return indexedtrace;
	}
	
}
