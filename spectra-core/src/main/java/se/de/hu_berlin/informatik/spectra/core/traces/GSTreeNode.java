package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GSTreeNode {
	
	private GSTree treeReference;
	private int[] sequence;
	
	private List<GSTreeNode> edges;

	public GSTreeNode() {
		this.treeReference = null;
		this.sequence = new int[] { GSTree.SEQUENCE_END };
	}
	
//	public GSTreeNode(GeneralizedSuffixTree treeReference, int[] sequence) {
//		this.treeReference = treeReference;
//		this.setSequenceAndAddEndingEdge(sequence);
//	}
	
	public GSTreeNode(GSTree treeReference, int[] sequence, int startingIndex, int endIndex) {
		this.treeReference = treeReference;
		// check if an element in the sequence has already been identified as a starting element
		for (int i = startingIndex + 1; i < endIndex; ++i) {
			// check if the next element has already been identified as a starting element, previously
			if (treeReference.checkIfStartingElementExists(sequence[i])) {
				// set the sequence to end at position i and add an ending edge
				this.setSequenceAndAddEndingEdge(
						Arrays.copyOfRange(sequence, startingIndex, i));
				// add the remaining sequence to the tree
				treeReference.addSequence(sequence, i, endIndex);
				// stop at this point
				return;
			}
		}
		// set the sequence to end at position endIndex and add an ending edge
		this.setSequenceAndAddEndingEdge(
				Arrays.copyOfRange(sequence, startingIndex, endIndex));
	}

	public GSTreeNode(GSTree treeReference, int[] remainingSequence, List<GSTreeNode> existingEdges) {
		this.treeReference = treeReference;
		this.sequence = remainingSequence;
		this.edges = existingEdges;
	}

	public int[] getSequence() {
		return sequence;
	}

	private void setSequenceAndAddEndingEdge(int[] sequence) {
		Objects.requireNonNull(sequence, "Input sequence to GSTree node should not be null.");
		
		this.sequence = sequence;
		this.edges = new ArrayList<>(1);
		edges.add(GSTree.getNewEndNode());
	}

	public List<GSTreeNode> getEdges() {
		return edges == null ? Collections.emptyList() : edges;
	}

	private void setEdges(List<GSTreeNode> edges) {
		this.edges = edges;
	}

	public void addSequence(int[] sequenceToAdd, int posIndex, int endIndex) {
		// check how much of this node's sequence is identical to the sequence to add;
		// we can start from index 1 (and posIndex + 1), since the first elements 
		// are guaranteed to be identical 
		++posIndex;
		for (int i = 1; i < this.sequence.length; i++, posIndex++) {
			if (posIndex < endIndex) {
				// both sequences still contain elements
				int nextAddedElement = sequenceToAdd[posIndex];
				// check if the next element has already been identified as a starting element, previously
				if (treeReference.checkIfStartingElementExists(nextAddedElement)) {
					// sequence to add is smaller than existing sequence
					// split sequence at this point and add branch with marked ending point
					splitSequenceAtIndex(i);
					
					// add a branch for the new, diverging sequence (sequence ending, in this case)
					this.getEdges().add(GSTree.getNewEndNode());
					
					// add the remaining sequence to the tree
					treeReference.addSequence(sequenceToAdd, posIndex, endIndex);
					// stop at this point
					return;
				}
				
				int nextExistingElement = this.sequence[i];
				// check if the sequences differ at this position
				if (nextAddedElement != nextExistingElement) {
					// split the sequence at this position
					splitSequenceAtIndex(i);
					
					// add a branch for the new, diverging sequence
//					int[] remainingSequenceToAdd = Arrays.copyOfRange(sequenceToAdd, posIndex, endIndex);
					this.getEdges().add(new GSTreeNode(treeReference, sequenceToAdd, posIndex, endIndex));
					return;
				}
			} else {
				// sequence to add is smaller than existing sequence
				// split sequence at this point and add branch with marked ending point
				splitSequenceAtIndex(i);
				
				// add a branch for the new, diverging sequence (sequence ending, in this case)
				this.getEdges().add(GSTree.getNewEndNode());
				return;
			}
		}
		
		// this node's sequence has ended;
		// check if there are still elements in the sequence to add
		if (posIndex < endIndex) {
			// sequence to add is larger than existing sequence
			int nextAddedElement = sequenceToAdd[posIndex];
			// check if the next element has already been identified as a starting element, previously
			if (treeReference.checkIfStartingElementExists(nextAddedElement)) {
				// we need to check if there exists an ending edge
				for (GSTreeNode edge : this.getEdges()) {
					if (edge.getFirstElement() == GSTree.SEQUENCE_END) {
						// add the remaining sequence to the tree
						treeReference.addSequence(sequenceToAdd, posIndex, endIndex);
						// stop at this point
						return;
					}
				}
				
				// no ending edge was found
				this.getEdges().add(GSTree.getNewEndNode());
				
				// add the remaining sequence to the tree
				treeReference.addSequence(sequenceToAdd, posIndex, endIndex);
				// stop at this point
				return;
			}
			
			// the next element is an element that has NOT been identified as a starting element before
			for (GSTreeNode edge : this.getEdges()) {
				if (edge.getFirstElement() == nextAddedElement) {
					// follow the branch and add the remaining sequence
					edge.addSequence(sequenceToAdd, posIndex, endIndex);
					return;
				}
			}
			// no branch with the next element exists, so simply add a new branch with the remaining sequence
			edges.add(new GSTreeNode(treeReference, sequenceToAdd, posIndex, endIndex));
			return;
		}
		
		// if we get to this point, both sequences are identical up to the end
		// we still need to check if there already exists an ending edge, in this case
		for (GSTreeNode edge : this.getEdges()) {
			if (edge.getFirstElement() == GSTree.SEQUENCE_END) {
				// we are done!
				return;
			}
		}
		
		// no ending edge was found
		this.getEdges().add(GSTree.getNewEndNode());
	}

	private void splitSequenceAtIndex(int index) {
		// split sequence at this point and add a branch
		int[] remainingSequence = Arrays.copyOfRange(this.sequence, index, this.sequence.length);
		// the new sequence of this node is the sequence up to the diverging position
		this.sequence = Arrays.copyOfRange(this.sequence, 0, index);
		// remember the previously existing edges
		List<GSTreeNode> existingEdges = this.getEdges();
		// the new set of edges contains the remaining sequence of the previously existing node
		// and the diverging sequence that is added
		this.setEdges(new ArrayList<>(2));
		// split this node and move the previously existing edges to the newly created node
		this.getEdges().add(new GSTreeNode(treeReference, remainingSequence, existingEdges));
	}
	
	public List<int[]> extractAndRemoveRemainingSequences(int index) {
		List<int[]> remainingSequences = new ArrayList<>();
		
		extractRemainingSequences(new int[] {}, this, index, remainingSequences);
		
		// the new sequence of this node is the sequence up to the new ending position;
		// if index is 0, then the node will be removed later, but we need to keep the first element
		this.sequence = Arrays.copyOfRange(this.sequence, 0, index == 0 ? 1 : index);
		
		// the new set of edges contains only the ending edge
		this.setEdges(new ArrayList<>(1));
		// add a branch for the new sequence ending
		this.getEdges().add(GSTree.getNewEndNode());
		
		return remainingSequences;
	}

	private void extractRemainingSequences(int[] previousSequence, GSTreeNode node, int i, List<int[]> collector) {
		if (node.getFirstElement() == GSTree.SEQUENCE_END) {
			collector.add(previousSequence);
			return;
		}
		
		// combine previous sequence with the given node's sequence, starting from the given index i
		int[] target = new int[previousSequence.length + (node.getSequence().length - i)];
		System.arraycopy(previousSequence, 0, target, 0, previousSequence.length);
		System.arraycopy(node.getSequence(), i, target, previousSequence.length, node.getSequence().length - i);
		
		for (GSTreeNode edge : node.getEdges()) {
			extractRemainingSequences(target, edge, 0, collector);
		}
	}

	public boolean checkIfMatch(int[] sequenceToCheck, int from, int to) {
		// check how much of this node's sequence is identical to the sequence to check;
		// we can start from index 1 (and from + 1), since the first elements 
		// are guaranteed to be identical
		
		// sequence to check is smaller than existing sequence
		if (to - from < this.sequence.length) {
			return false;
		}
		++from;
		for (int i = 1; i < this.sequence.length; i++, from++) {
			if (from < to) {
				// both sequences still contain elements
				int nextAddedElement = sequenceToCheck[from];
				int nextExistingElement = this.sequence[i];
				// check if the sequences differ at this position
				if (nextAddedElement != nextExistingElement) {
					return false;
				}
			} 
//			else {
//				// sequence to check is smaller than existing sequence
//				// TODO this could be checked at the start
//				return false;
//			}
		}
		
		// check if there are still elements in the sequence to check
		if (from < to) {
			// sequence to check is larger than existing sequence
			int nextAddedElement = sequenceToCheck[from];
			for (GSTreeNode edge : this.getEdges()) {
				if (edge.getFirstElement() == nextAddedElement) {
					// follow the branch and add the remaining sequence
					return edge.checkIfMatch(sequenceToCheck, from, to);
				}
			}
			// no branch with the next element exists
			return false;
		}
		
		// if we get to this point, both sequences are identical up to the end
		// we still need to check if there exists an ending edge, in this case
		for (GSTreeNode edge : this.getEdges()) {
			if (edge.getFirstElement() == GSTree.SEQUENCE_END) {
				// we are done!
				return true;
			}
		}
		
		// no ending edge was found
		return false;
	}
	
	
	public int getSequenceIndex(SequenceIndexer indexer, int[] sequenceToCheck, int from, int to) {
		// check how much of this node's sequence is identical to the sequence to check;
		// we can start from index 1 (and from + 1), since the first elements 
		// are guaranteed to be identical
		
		// sequence to check is smaller than existing sequence
		if (to - from < this.sequence.length) {
			return GSTree.BAD_INDEX;
		}
		++from;
		for (int i = 1; i < this.sequence.length; i++, from++) {
			if (from < to) {
				// both sequences still contain elements
				int nextAddedElement = sequenceToCheck[from];
				int nextExistingElement = this.sequence[i];
				// check if the sequences differ at this position
				if (nextAddedElement != nextExistingElement) {
					return GSTree.BAD_INDEX;
				}
			} 
//			else {
//				// sequence to check is smaller than existing sequence
//				// TODO this could be checked at the start
//				return false;
//			}
		}
		
		// check if there are still elements in the sequence to check
		if (from < to) {
			// sequence to check is larger than existing sequence
			int nextAddedElement = sequenceToCheck[from];
			for (GSTreeNode edge : this.getEdges()) {
				if (edge.getFirstElement() == nextAddedElement) {
					// follow the branch and add the remaining sequence
					return edge.getSequenceIndex(indexer, sequenceToCheck, from, to);
				}
			}
			// no branch with the next element exists
			return GSTree.BAD_INDEX;
		}
		
		// if we get to this point, both sequences are identical up to the end
		// we still need to check if there exists an ending edge, in this case
		for (GSTreeNode edge : this.getEdges()) {
			if (edge.getFirstElement() == GSTree.SEQUENCE_END) {
				// we are done!
				return indexer.getSequenceIdForEndNode(edge);
			}
		}
		
		// no ending edge was found
		return GSTree.BAD_INDEX;
	}
	

	public int getFirstElement() {
		return this.sequence[0];
	}
	
}
