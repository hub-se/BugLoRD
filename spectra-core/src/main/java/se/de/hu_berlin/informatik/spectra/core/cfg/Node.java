package se.de.hu_berlin.informatik.spectra.core.cfg;

import java.util.Arrays;

import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;

public class Node {

	private final int index;
	private int[] mergedIndices;
	private Node[] prevNodes;
	private Node[] succNodes;
	
	public Node(int index) {
		this.index = index;
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

	private void addToPrevNodes(Node node) {
		if (prevNodes == null) {
			prevNodes = new Node[] {node};
		} else if (!hasNode(prevNodes, node)) {
			prevNodes = Misc.addToArrayAndReturnResult(prevNodes, node);
		}
	}

	private boolean hasNode(Node[] nodes, Node node) {
		for (Node node2 : nodes) {
			if (node2.index == node.index) {
				return true;
			}
		}
		return false;
	}

	private void addToSuccNodes(Node node) {
		if (succNodes == null) {
			succNodes = new Node[] {node};
		} else if (!hasNode(succNodes, node)) {
			succNodes = Misc.addToArrayAndReturnResult(succNodes, node);
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
	
	public Node[] getPredecessors() {
		return prevNodes;
	}
	
	public Node[] getSuccessors() {
		return succNodes;
	}

	public void setSuccessors(Node[] successors) {
		succNodes = successors;
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
			sb.append("\tpred: ").append(Arrays.toString(Arrays.stream(getPredecessors()).mapToInt(k -> k.getIndex()).toArray())).append(System.lineSeparator());
		}
		if (hasSuccessors()) {
			sb.append("\tsucc: ").append(Arrays.toString(Arrays.stream(getSuccessors()).mapToInt(k -> k.getIndex()).toArray())).append(System.lineSeparator());
		}
		return sb.toString();
	}
	
}
