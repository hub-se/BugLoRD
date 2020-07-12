package se.de.hu_berlin.informatik.spectra.core.cfg;

import java.util.Arrays;

public class Node {

	private final int index;
	private int[] mergedIndices;
	private int[] prevNodes;
	private long[] prevNodeHits;
	private int[] succNodes;
	private long[] succNodeHits;
	
	public Node(int index) {
		this.index = index;
	}

	public Node(int index, int[] mergedNodes, 
			int[] predecessors, long[] predecessorHits, 
			int[] successors, long[] successorHits) {
		this.index = index;
		this.mergedIndices = mergedNodes;
		this.prevNodes = predecessors;
		this.prevNodeHits = predecessorHits;
		this.succNodes = successors;
		this.succNodeHits = successorHits;
	}

	public Node connectTo(int index, DynamicCFG<?> cfg) {
		Node successor = cfg.getOrCreateNode(index);
		return this.connectTo(successor);
	}
	
	public Node connectTo(Node successorNode) {
		this.addToSuccNodes(successorNode);
		successorNode.addToPrevNodes(this);
		return successorNode;
	}

	private int hasNode(int[] nodes, Node node) {
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] == node.index) {
				return i;
			}
			
		}
		return -1;
	}
	
	private void addToPrevNodes(Node node) {
		if (prevNodes == null) {
			prevNodes = new int[] {node.getIndex()};
			prevNodeHits = new long[] {1};
		} else {
			int index = hasNode(prevNodes, node);
			if (index < 0) {
				// new predecessor node
				int[] newPrevNodes = new int[prevNodes.length + 1];
				System.arraycopy(prevNodes, 0, newPrevNodes, 0, prevNodes.length);
				newPrevNodes[prevNodes.length] = node.getIndex();
				prevNodes = newPrevNodes;
						
				long[] newHits = new long[prevNodeHits.length + 1];
				System.arraycopy(prevNodeHits, 0, newHits, 0, prevNodeHits.length);
				newHits[prevNodeHits.length] = 1;
				prevNodeHits = newHits;
			} else {
				++prevNodeHits[index];
			}
		}
	}

	private void addToSuccNodes(Node node) {
		if (succNodes == null) {
			succNodes = new int[] {node.getIndex()};
			succNodeHits = new long[] {1};
		} else {
			int index = hasNode(succNodes, node);
			if (index < 0) {
				// new successor node
				int[] newSuccNodes = new int[succNodes.length + 1];
				System.arraycopy(succNodes, 0, newSuccNodes, 0, succNodes.length);
				newSuccNodes[succNodes.length] = node.getIndex();
				succNodes = newSuccNodes;
				
				long[] newHits = new long[succNodeHits.length + 1];
				System.arraycopy(succNodeHits, 0, newHits, 0, succNodeHits.length);
				newHits[succNodeHits.length] = 1;
				succNodeHits = newHits;
			} else {
				++succNodeHits[index];
			}
		}
	}

	public int getIndex() {
		return index;
	}
	
	public boolean hasPredecessors() {
		return prevNodes != null;
	}
	
	public boolean hasSuccessors() {
		return succNodes != null;
	}
	
	public int getPredecessorCount() {
		return prevNodes.length;
	}
	
	public int getSuccessorCount() {
		return succNodes.length;
	}
	
	public int[] getPredecessors() {
		return prevNodes;
	}
	
	public int[] getSuccessors() {
		return succNodes;
	}

	public long[] getPredecessorHits() {
		return prevNodeHits;
	}

	public long[] getSuccessorHits() {
		return succNodeHits;
	}
	
	public void setSuccessors(int[] successors) {
		succNodes = successors;
	}

	public void setSuccessorHits(long[] succNodeHits) {
		this.succNodeHits = succNodeHits;
	}


	public boolean isMerged() {
		return mergedIndices != null;
	}

	public int[] getMergedIndices() {
		return mergedIndices;
	}

	public void setMergedIndices(int[] mergedIndices) {
		this.mergedIndices = mergedIndices;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("node ").append(getIndex()).append(":");
		if (isMerged()) {
			sb.append(" +").append(Arrays.toString(getMergedIndices()));
		}
		sb.append(System.lineSeparator());
		if (hasPredecessors()) {
			sb.append("\tpred: ").append(Arrays.toString(getPredecessors())).append(System.lineSeparator());
			sb.append("\thits: ").append(Arrays.toString(getPredecessorHits())).append(System.lineSeparator());
		}
		if (hasSuccessors()) {
			sb.append("\tsucc: ").append(Arrays.toString(getSuccessors())).append(System.lineSeparator());
			sb.append("\thits: ").append(Arrays.toString(getSuccessorHits())).append(System.lineSeparator());
		}
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		return 17 + 31 * index;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Node) {
			Node o = (Node) obj;
			if (o.index != this.index) {
				return false;
			}
			return true;
		}
		return false;
	}
	
}
