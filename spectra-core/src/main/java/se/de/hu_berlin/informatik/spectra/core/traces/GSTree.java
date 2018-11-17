package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CompressedTraceBase;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedQueue;

public class GSTree {
	
	// some element not contained in the input sequences TODO maybe set to 0?
	// in the future, negative indices will probably point to node sequences, themselves...
	public static final int SEQUENCE_END = Integer.MIN_VALUE;
	public static final int BAD_INDEX = Integer.MIN_VALUE;
//	public static final GSTreeNode END_NODE = new GSTreeNode();

	// the (virtual) root node has a lot of branches, the inner nodes should not branch that much
	// so we use a map here, and we use lists of edges in the inner nodes
	private Map<Integer, GSTreeNode> branches = new HashMap<>();
	
	public static GSTreeNode getNewEndNode() {
		return new GSTreeNode();
	}
	
	public boolean addSequence(int[] sequence) {
		if (sequence == null) {
			return false;
		}
		
		SingleLinkedQueue<Integer> queue = new SingleLinkedQueue<>();
		for (int i : sequence) {
			queue.add(i);
		}
		
		return __addSequence(queue, queue.size());
	}
	
	public boolean addSequence(int[] sequence, int from, int to) {
		if (sequence == null) {
			return false;
		}
		
		return __addSequence(sequence, from, to);
	}
	
	public boolean addSequence(SingleLinkedQueue<Integer> sequence, int length) {
		if (sequence == null) {
			return false;
		}
		checkSequenceLength(sequence, length);
		
		return __addSequence(sequence, length);
	}
	
	private void checkSequenceLength(SingleLinkedQueue<Integer> sequence2, int length) {
		if (sequence2.size() < length) {
			throw new IllegalStateException(sequence2.size() + " < " + length);
		}
	}
	
//	public boolean addSequence(List<Integer> sequence) {
//		if (sequence == null) {
//			return false;
//		}
//		
//		return __addSequence(sequence.stream().mapToInt(i->i).toArray());
//	}
	
	private boolean __addSequence(SingleLinkedQueue<Integer> sequence, int length) {
		if (length == 0) {
			branches.put(SEQUENCE_END, getNewEndNode());
			return true;
		}
		int firstElement = sequence.element();
		
		GSTreeNode startingNode = branches.get(firstElement);
		if (startingNode == null) {
			// new starting element
			branches.put(firstElement, new GSTreeNode(this, sequence, length));
			// check for the starting element in existing branches 
			// and extract the remaining sequences
			for (Entry<Integer, GSTreeNode> entry : branches.entrySet()) {
				if (entry.getKey() == firstElement || entry.getKey() == SEQUENCE_END) {
					continue;
				}
				extractAndReinsertSequences(entry.getValue(), firstElement);
			}
			return true;
		} else {
			int prevLength = sequence.size();
			// branch with this starting element already exists
			startingNode.addSequence(sequence, length);
			assert prevLength == sequence.size() + length :
				"prev length: " + prevLength + ", length: " + length + ", remaining: " + sequence.size();
			return true;
		}
	}

	private boolean __addSequence(int[] sequence, int from, int to) {
		if (from < 0 || to < 0 || to < from || to > sequence.length) {
			return false;
		}
		
		if (to - from == 0) {
			branches.put(SEQUENCE_END, getNewEndNode());
			return true;
		}
		int firstElement = sequence[from];
		
		GSTreeNode startingNode = branches.get(firstElement);
		if (startingNode == null) {
			// new starting element
			branches.put(firstElement, new GSTreeNode(this, sequence, from, to));
			// check for the starting element in existing branches 
			// and extract the remaining sequences
			for (Entry<Integer, GSTreeNode> entry : branches.entrySet()) {
				if (entry.getKey() == firstElement || entry.getKey() == SEQUENCE_END) {
					continue;
				}
				extractAndReinsertSequences(entry.getValue(), firstElement);
			}
			return true;
		} else {
			// branch with this starting element already exists
			startingNode.addSequence(sequence, from, to);
			return true;
		}
	}

	private void extractAndReinsertSequences(GSTreeNode node, int firstElement) {
		for (int i = 0; i < node.getSequence().length; i++) {
			int element = node.getSequence()[i];
			if (element == firstElement) {
				// found the starting element
				List<int[]> remainingSequences = node.extractAndRemoveRemainingSequences(i);
				// add the sequences to the tree
				for (int[] sequence : remainingSequences) {
					addSequence(sequence);
				}
				return;
			}
		}
		
		boolean needsEndingEdge = false;
		// check all edges, if the starting element has not been found in this node's sequence
		for (Iterator<GSTreeNode> iterator = node.getEdges().iterator(); iterator.hasNext();) {
			GSTreeNode edge = iterator.next();
			if (edge.getFirstElement() == SEQUENCE_END) {
				continue;
			}
			extractAndReinsertSequences(edge, firstElement);
			if (edge.getFirstElement() == firstElement) {
				// when removing the first element in a node, we remove the entire edge
				iterator.remove();
				needsEndingEdge = true;
			}
		}
		
		// check for an ending edge if we removed an entire node
		if (needsEndingEdge) {
			for (GSTreeNode edge : node.getEdges()) {
				if (edge.getFirstElement() == SEQUENCE_END) {
					return;
				}
			}
			
			// no ending edge found
			node.getEdges().add(getNewEndNode());
		}
	}
	
	
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
			return branches.get(SEQUENCE_END) != null;
		}
		int firstElement = sequence[from];
		
		GSTreeNode startingNode = branches.get(firstElement);
		if (startingNode != null) {
			// some sequence with this starting element exists in the tree
			return startingNode.checkIfMatch(sequence, from, to);
		} else {
			// no sequence with this starting element exists in the tree
			return false;
		}
	}

	public int getSequenceIndex(SequenceIndexer indexer, SingleLinkedQueue<Integer> sequence) {
		if (sequence == null) {
			return BAD_INDEX;
		}
		if (sequence.isEmpty()) {
			return indexer.getSequenceIdForEndNode(branches.get(SEQUENCE_END));
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
	
	
	public boolean checkIfStartingElementExists(int element) {
		return branches.get(element) != null;
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

	public SingleLinkedQueue<Integer> generateIndexedTrace(CompressedTraceBase<Integer, ?> rawTrace, SequenceIndexer indexer) {
		if (rawTrace == null || rawTrace.getCompressedTrace().length == 0) {
			return new SingleLinkedQueue<>();
		}
		
		SingleLinkedQueue<Integer> indexedtrace = new SingleLinkedQueue<>();
		
		SingleLinkedQueue<Integer> unprocessedSequence = new SingleLinkedQueue<>();
		Iterator<Integer> iterator = rawTrace.iterator();
		unprocessedSequence.add(iterator.next());
		for (; iterator.hasNext();) {
			Integer element = iterator.next();
			if (checkIfStartingElementExists(element)) {
				indexedtrace.add(getSequenceIndex(indexer, unprocessedSequence));
				unprocessedSequence.clear();
			}
			unprocessedSequence.add(element);
		}
		indexedtrace.add(getSequenceIndex(indexer, unprocessedSequence));
		
		return indexedtrace;
	}
	
}
