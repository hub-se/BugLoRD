package se.de.hu_berlin.informatik.spectra.core.cfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.SingleLinkedIntArrayQueue;

public class DynamicCFG<T> implements CFG<T> {
	
	private Map<Integer, Node> nodes = new HashMap<>();
	private Set<Integer> startNodes = new HashSet<>();
	private ISpectra<T, ?> spectra;

	public DynamicCFG(ISpectra<T, ?> spectra) {
		this.spectra = spectra;
	}
	
	@Override
	public Map<Integer, Node> getNodes() {
		return nodes;
	}
	
	@Override
	public Node getOrCreateNode(int index) {
		return nodes.computeIfAbsent(index, k -> new Node(index));
	}

	@Override
	public Node getNode(int index) {
		return nodes.get(index);
	}
	
	@Override
	public boolean containsNode(int index) {
		return nodes.containsKey(index);
	}

	@Override
	public void addExecutionTrace(ExecutionTrace executionTrace) {
		Iterator<Integer> nodeIdIterator = executionTrace.mappedIterator(spectra.getIndexer());
		Node lastNode = null;
		while (nodeIdIterator.hasNext()) {
			int nodeIndex = nodeIdIterator.next();
			Node node = getOrCreateNode(nodeIndex);
			if (lastNode != null) {
				lastNode.connectTo(node);
			} else {
				startNodes.add(nodeIndex);
			}
			lastNode = node;
		}
	}

	@Override
	public void generateCompleteCFG() {
		for (ITrace<T> trace : spectra.getTraces()) {
			Collection<ExecutionTrace> executionTraces = trace.getExecutionTraces();
			for (ExecutionTrace executionTrace : executionTraces) {
				addExecutionTrace(executionTrace);
			}
		}
	}
	
	@Override
	public void mergeLinearSequeces() {
		SingleLinkedIntArrayQueue openNodes = new SingleLinkedIntArrayQueue(50);
		Set<Integer> seenNodes = new HashSet<>();
		
		for (Integer index : startNodes) {
			openNodes.add(index);
			
			while (!openNodes.isEmpty()) {
				traverseMerge(openNodes, seenNodes);
			}
		}
	}

	private void traverseMerge(SingleLinkedIntArrayQueue openNodes, Set<Integer> seenNodes) {
		int startIndex = openNodes.remove();
		seenNodes.add(startIndex);
		
		Node startNode = getNode(startIndex);
		assert startNode != null;
		ArrayList<Integer> nodeIndices = new ArrayList<>();
//		nodeIndices.add(startIndex);
		
		Node lastNode = startNode;
		Node tempNode = startNode;
		while (tempNode.hasSuccessors()) {
			if (tempNode.getSuccessorCount() == 1) {
				// get next node in sequence
				tempNode = tempNode.getSuccessors()[0];
				
				// check for self-loops
				if (tempNode.getIndex() == lastNode.getIndex()) {
					break;
				}

				// if next node has other predecessors, we can't merge it to the previous ones;
				// additionally, we also can't merge start nodes!
				if (tempNode.getPredecessorCount() > 1 || startNodes.contains(tempNode.getIndex())) {
					if (!seenNodes.contains(tempNode.getIndex())) {
						openNodes.add(tempNode.getIndex());
					}
					break;
				}

				// node has only one predecessor, so we can merge it
				nodeIndices.add(tempNode.getIndex());
				lastNode = tempNode;
			} else {
				// end of linear sequence
				if (tempNode.getSuccessorCount() > 1) {
					for (Node node : tempNode.getSuccessors()) {
						if (!seenNodes.contains(node.getIndex())) {
							openNodes.add(node.getIndex());
						}
					}
				}
				break;
			}
		}

		if (!nodeIndices.isEmpty()) {
			// we can merge multiple nodes!
			// set the successors of the start node to be the successors of the last merged node
			startNode.setSuccessors(lastNode.getSuccessors());
			startNode.setMergedIndices(nodeIndices.stream().mapToInt(k -> k).toArray());
			nodeIndices.forEach(k -> {
				// remove merged nodes
				nodes.remove(k);
			});
			
			if (startNode.hasSuccessors()) {
				// adjust predecessor nodes of successor nodes
				for (Node successor : startNode.getSuccessors()) {
					for (int i = 0; i < successor.getPredecessors().length; i++) {
						Node predecessor = successor.getPredecessors()[i];
						if (predecessor.getIndex() == lastNode.getIndex()) {
							successor.getPredecessors()[i] = startNode;
							break;
						}
					}
				}
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Entry<Integer, Node> entry : nodes.entrySet()) {
			Node node = entry.getValue();
//			if (node.getIndex() != entry.getKey()) {
//				continue;
//			}
			
			sb.append(node.toString())
			.append("--------------").append(System.lineSeparator());
		}
		return sb.toString();
	}
	
}
