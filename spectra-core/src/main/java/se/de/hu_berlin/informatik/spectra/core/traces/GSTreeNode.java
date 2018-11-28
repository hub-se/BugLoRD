package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CloneableIterator;

public class GSTreeNode {
	
	private GSTree treeReference;
	private int[] sequence;
	
	private List<GSTreeNode> edges;

	public GSTreeNode(int index) {
		this.treeReference = null;
		this.sequence = new int[] { GSTree.SEQUENCE_END, index };
	}
	
//	public GSTreeNode(GeneralizedSuffixTree treeReference, int[] sequence) {
//		this.treeReference = treeReference;
//		this.setSequenceAndAddEndingEdge(sequence);
//	}
	
//	public GSTreeNode(GSTree treeReference, int[] sequence, int startingIndex, int endIndex) {
//		this.treeReference = treeReference;
//		// check if an element in the sequence has already been identified as a starting element
//		for (int i = startingIndex + 1; i < endIndex; ++i) {
//			// check if the next element has already been identified as a starting element, previously
//			if (treeReference.checkIfStartingElementExists(sequence[i])) {
//				// set the sequence to end at position i and add an ending edge
//				this.setSequenceAndAddEndingEdge(
//						Arrays.copyOfRange(sequence, startingIndex, i));
//				// add the remaining sequence to the tree
//				treeReference.addSequence(sequence, i, endIndex);
//				// stop at this point
//				return;
//			}
//		}
//		// set the sequence to end at position endIndex and add an ending edge
//		this.setSequenceAndAddEndingEdge(
//				Arrays.copyOfRange(sequence, startingIndex, endIndex));
//	}

	public GSTreeNode(GSTree treeReference, int[] remainingSequence, List<GSTreeNode> existingEdges) {
		this.treeReference = treeReference;
		this.sequence = remainingSequence;
		this.edges = existingEdges;
	}

	public GSTreeNode(GSTree treeReference, CloneableIterator<Integer> sequence, int length) {
		this.treeReference = treeReference;
		Iterator<Integer> iterator = sequence.clone();
		iterator.next();
		// check if an element in the sequence has already been identified as a starting element
		for (int i = 1; i < length; ++i) {
			// check if the next element has already been identified as a starting element, previously
			if (treeReference.checkIfStartingElementExists(iterator.next())) {
				// set the sequence to end at position i and add an ending edge
				int[] seq = new int[i];
				for (int j = 0; j < i; ++j) {
					seq[j] = sequence.next();
				}
				this.setSequenceAndAddEndingEdge(seq);
				// add the remaining sequence to the tree
				treeReference.addSequence(sequence, length - i);
				// stop at this point
				return;
			}
		}
		// set the sequence and add an ending edge
		int[] seq = new int[length];
		for (int j = 0; j < length; ++j) {
			seq[j] = sequence.next();
		}
		this.setSequenceAndAddEndingEdge(seq);
	}

	public int[] getSequence() {
		return sequence;
	}

	private void setSequenceAndAddEndingEdge(int[] sequence) {
		Objects.requireNonNull(sequence, "Input sequence to GSTree node should not be null.");
//		System.out.println(Arrays.toString(sequence));
		this.sequence = sequence;
		this.edges = new ArrayList<>(1);
		edges.add(treeReference.getNewEndNode());
	}

	public List<GSTreeNode> getEdges() {
		return edges == null ? Collections.emptyList() : edges;
	}

	private void setEdges(List<GSTreeNode> edges) {
		this.edges = edges;
	}
	
	public void addSequence(CloneableIterator<Integer> unprocessedIterator, int length) {
		// check how much of this node's sequence is identical to the sequence to add;
		// we can start from index 1 (and posIndex + 1), since the first elements 
		// are guaranteed to be identical
		CloneableIterator<Integer> iterator = unprocessedIterator;
		iterator.next();
		int i = 1;
		for (; i < this.sequence.length; i++, iterator.next()) {
			if (i < length) {
				// both sequences still contain elements
				int nextAddedElement = iterator.peek();
				// check if the next element has already been identified as a starting element, previously
				if (treeReference.checkIfStartingElementExists(nextAddedElement)) {
					// sequence to add is smaller than existing sequence
					// split sequence at this point and add branch with marked ending point
					splitSequenceAtIndex(i);
					
					// add a branch for the new, diverging sequence (sequence ending, in this case)
					this.getEdges().add(treeReference.getNewEndNode());
					
					// add the remaining sequence to the tree
					treeReference.__addSequence(unprocessedIterator, length - i, nextAddedElement);
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
					this.getEdges().add(new GSTreeNode(treeReference, unprocessedIterator, length - i));
					return;
				}
			} else {
				// sequence to add is smaller than existing sequence
				// split sequence at this point and add branch with marked ending point
				splitSequenceAtIndex(i);
				
				// add a branch for the new, diverging sequence (sequence ending, in this case)
				this.getEdges().add(treeReference.getNewEndNode());
				return;
			}
		}
		
		// this node's sequence has ended;
		// check if there are still elements in the sequence to add
		if (i < length && iterator.hasNext()) {
			// sequence to add is larger than existing sequence
			int nextAddedElement = iterator.peek();
			
			// check if the next element has already been identified as a starting element, previously
			if (treeReference.checkIfStartingElementExists(nextAddedElement)) {
				// we need to check if there exists an ending edge
				for (GSTreeNode edge : this.getEdges()) {
					if (edge.getFirstElement() == GSTree.SEQUENCE_END) {
						// add the remaining sequence to the tree
						treeReference.__addSequence(unprocessedIterator, length - i, nextAddedElement);
						// stop at this point
						return;
					}
				}
				
				// no ending edge was found
				this.getEdges().add(treeReference.getNewEndNode());
				
				// add the remaining sequence to the tree
				treeReference.__addSequence(unprocessedIterator, length - i, nextAddedElement);
				// stop at this point
				return;
			}
//			System.out.println("new edge start: " + nextAddedElement);
			
			// the next element is an element that has NOT been identified as a starting element before
			for (GSTreeNode edge : this.getEdges()) {
				if (edge.getFirstElement() == nextAddedElement) {
					// follow the branch and add the remaining sequence
					edge.addSequence(unprocessedIterator, length - i);
					return;
				}
			}
			// no branch with the next element exists, so simply add a new branch with the remaining sequence
			edges.add(new GSTreeNode(treeReference, unprocessedIterator, length - i));
			return;
		} else {
			// if we get to this point, both sequences are identical up to the end

			// we still need to check if there already exists an ending edge, in this case
			for (GSTreeNode edge : this.getEdges()) {
				if (edge.getFirstElement() == GSTree.SEQUENCE_END) {
					// we are done!
					return;
				}
			}

			// no ending edge was found
			this.getEdges().add(treeReference.getNewEndNode());
			return;
		}
	}

//	public void addSequence(int[] sequenceToAdd, int posIndex, int endIndex) {
//		// check how much of this node's sequence is identical to the sequence to add;
//		// we can start from index 1 (and posIndex + 1), since the first elements 
//		// are guaranteed to be identical 
//		++posIndex;
//		for (int i = 1; i < this.sequence.length; i++, posIndex++) {
//			if (posIndex < endIndex) {
//				// both sequences still contain elements
//				int nextAddedElement = sequenceToAdd[posIndex];
//				// check if the next element has already been identified as a starting element, previously
//				if (treeReference.checkIfStartingElementExists(nextAddedElement)) {
//					// sequence to add is smaller than existing sequence
//					// split sequence at this point and add branch with marked ending point
//					splitSequenceAtIndex(i);
//					
//					// add a branch for the new, diverging sequence (sequence ending, in this case)
//					this.getEdges().add(treeReference.getNewEndNode());
//					
//					// add the remaining sequence to the tree
//					treeReference.addSequence(sequenceToAdd, posIndex, endIndex);
//					// stop at this point
//					return;
//				}
//				
//				int nextExistingElement = this.sequence[i];
//				// check if the sequences differ at this position
//				if (nextAddedElement != nextExistingElement) {
//					// split the sequence at this position
//					splitSequenceAtIndex(i);
//					
//					// add a branch for the new, diverging sequence
////					int[] remainingSequenceToAdd = Arrays.copyOfRange(sequenceToAdd, posIndex, endIndex);
//					this.getEdges().add(new GSTreeNode(treeReference, sequenceToAdd, posIndex, endIndex));
//					return;
//				}
//			} else {
//				// sequence to add is smaller than existing sequence
//				// split sequence at this point and add branch with marked ending point
//				splitSequenceAtIndex(i);
//				
//				// add a branch for the new, diverging sequence (sequence ending, in this case)
//				this.getEdges().add(treeReference.getNewEndNode());
//				return;
//			}
//		}
//		
//		// this node's sequence has ended;
//		// check if there are still elements in the sequence to add
//		if (posIndex < endIndex) {
//			// sequence to add is larger than existing sequence
//			int nextAddedElement = sequenceToAdd[posIndex];
//			// check if the next element has already been identified as a starting element, previously
//			if (treeReference.checkIfStartingElementExists(nextAddedElement)) {
//				// we need to check if there exists an ending edge
//				for (GSTreeNode edge : this.getEdges()) {
//					if (edge.getFirstElement() == GSTree.SEQUENCE_END) {
//						// add the remaining sequence to the tree
//						treeReference.addSequence(sequenceToAdd, posIndex, endIndex);
//						// stop at this point
//						return;
//					}
//				}
//				
//				// no ending edge was found
//				this.getEdges().add(treeReference.getNewEndNode());
//				
//				// add the remaining sequence to the tree
//				treeReference.addSequence(sequenceToAdd, posIndex, endIndex);
//				// stop at this point
//				return;
//			}
//			
//			// the next element is an element that has NOT been identified as a starting element before
//			for (GSTreeNode edge : this.getEdges()) {
//				if (edge.getFirstElement() == nextAddedElement) {
//					// follow the branch and add the remaining sequence
//					edge.addSequence(sequenceToAdd, posIndex, endIndex);
//					return;
//				}
//			}
//			// no branch with the next element exists, so simply add a new branch with the remaining sequence
//			edges.add(new GSTreeNode(treeReference, sequenceToAdd, posIndex, endIndex));
//			return;
//		}
//		
//		// if we get to this point, both sequences are identical up to the end
//		// we still need to check if there already exists an ending edge, in this case
//		for (GSTreeNode edge : this.getEdges()) {
//			if (edge.getFirstElement() == GSTree.SEQUENCE_END) {
//				// we are done!
//				return;
//			}
//		}
//		
//		// no ending edge was found
//		this.getEdges().add(treeReference.getNewEndNode());
//	}

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
	
	public List<int[]> extractAndRemoveRemainingSequences(int index, int firstElement) {
		List<int[]> remainingSequences = new ArrayList<>();
		
		// we still need to check for further occurrences of the new starting element in the remaining sequences
		extractRemainingSequences(new int[] {}, this, index, firstElement, true, remainingSequences);
		
		if (index == 0) {
			// if index is 0, then the node will be removed later, 
			// but we need to keep the first element of the sequence;
			// we can remove the edges, though
			this.setEdges(null);
		} else {
			// the new sequence of this node is the sequence up to the new ending position;
			this.sequence = Arrays.copyOfRange(this.sequence, 0, index);
			// the new set of edges contains only the ending edge
			this.setEdges(new ArrayList<>(1));
			// add a branch for the new sequence ending
			this.getEdges().add(treeReference.getNewEndNode());
		}
		return remainingSequences;
	}

	private void extractRemainingSequences(int[] previousSequence, GSTreeNode node, int index, 
			int firstElement, boolean firstCall, List<int[]> collector) {
		if (node.getFirstElement() == GSTree.SEQUENCE_END) {
			collector.add(previousSequence);
			return;
		}
		
		// check for occurrences of the new starting element
		for (int i = index; i < node.getSequence().length; i++) {
			int element = node.getSequence()[i];
			if (element == firstElement) {
				if (firstCall && i == index) {
					// the first checked element should 
					// always be identical to the new starting element
					continue;
				} else if (i == index) {
					// found the starting element, and it is the first element to be checked in this node
					collector.add(previousSequence);

					extractRemainingSequences(new int[] {}, node, i, firstElement, true, collector);
					return;
				} else {
					// found the starting element at a later index
					// combine previous sequence with the given node's sequence, starting from index, ending at i
					int[] target = new int[previousSequence.length + (i - index)];
					System.arraycopy(previousSequence, 0, target, 0, previousSequence.length);
					System.arraycopy(node.getSequence(), index, target, previousSequence.length, i - index);
					collector.add(target);

					extractRemainingSequences(new int[] {}, node, i, firstElement, true, collector);
					return;
				}
			}
		}
		
		// combine previous sequence with the given node's sequence, starting from the given index
		int[] target = new int[previousSequence.length + (node.getSequence().length - index)];
		System.arraycopy(previousSequence, 0, target, 0, previousSequence.length);
		System.arraycopy(node.getSequence(), index, target, previousSequence.length, node.getSequence().length - index);
		
		for (GSTreeNode edge : node.getEdges()) {
			extractRemainingSequences(target, edge, 0, firstElement, false, collector);
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
	
	
	public int getSequenceIndex(SequenceIndexer indexer, Iterator<Integer> iterator, int remainingLength) {
		// check how much of this node's sequence is identical to the sequence to check;
		// we can start from index 1, since the first elements 
		// are guaranteed to be identical (has been checked previously);
		// the iterator parameter is already pointing to the second element in the sequence
		
		// sequence to check is smaller than existing sequence
		if (remainingLength < this.sequence.length) {
			return GSTree.BAD_INDEX;
		}
		
		// iterate over the sequence and check for equality
		for (int i = 1; i < this.sequence.length; ++i) {
			// both sequences still contain elements
			int nextAddedElement = iterator.next();
			int nextExistingElement = this.sequence[i];
			
			// check if the sequences differ at this position
			if (nextAddedElement != nextExistingElement) {
				return GSTree.BAD_INDEX;
			}
		}
		
		// check if there are still elements in the sequence to check
		if (remainingLength > this.sequence.length) {
			// sequence to check is larger than existing sequence
			int nextAddedElement = iterator.next();
			for (GSTreeNode edge : this.getEdges()) {
				if (edge.getFirstElement() == nextAddedElement) {
					// follow the branch and add the remaining sequence
					return edge.getSequenceIndex(indexer, iterator, remainingLength - this.sequence.length);
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

	public boolean contains(int index) {
		for (int i : sequence) {
			if (i == index) {
				return true;
			}
		}
		return false;
	}

	public void setSequence(int[] array) {
		this.sequence = array;
	}

	public int getNextSequenceIndex(SequenceIndexer indexer, Iterator<Integer> rawTraceIterator,
			Queue<Integer> indexedtrace) {
		// we are iterating through the raw trace until we find an element that is a starting element of the tree;
		// this element is returned in the end to check the index of the following sequence, and so on;
		// we start at the second element of the sequence to check
		
		// check how much of this node's sequence is identical to the sequence to check;

		// iterate over the sequence and check for equality
		for (int i = 1; i < this.sequence.length; ++i) {
			if (!rawTraceIterator.hasNext()) {
				// sequence to check is smaller than existing sequence
				System.err.println("Sequence to check is smaller than existing sequence in tree.");
				return GSTree.BAD_INDEX;
			}
			// both sequences still contain elements
			int nextAddedElement = rawTraceIterator.next();
			int nextExistingElement = this.sequence[i];

			// check if the sequences differ at this position
			// TODO is this necessary?
			if (nextAddedElement != nextExistingElement) {
				System.err.println("Sequence to check differs from existing sequence in tree.");
				return GSTree.BAD_INDEX;
			}
		}

		// check if there are still elements in the sequence to check
		if (rawTraceIterator.hasNext()) {
			// sequence to check is larger than existing sequence
			int nextElement = rawTraceIterator.next();
			if (treeReference.checkIfStartingElementExists(nextElement)) {
				// start of a new sequence!
				// if we get to this point, both sequences are identical up to the end
				// we still need to check if there exists an ending edge, in this case
				for (GSTreeNode edge : this.getEdges()) {
					if (edge.getFirstElement() == GSTree.SEQUENCE_END) {
						// we are done!
						indexedtrace.add(indexer.getSequenceIdForEndNode(edge));
						// return the element that marks the start of a new sequence
						return nextElement;
					}
				}
				// no ending edge was found
				System.err.println("No ending edge present after existing sequence in tree.");
				return GSTree.BAD_INDEX;
			} else {
				// still inside of a sequence, so check the following nodes
				for (GSTreeNode edge : this.getEdges()) {
					if (edge.getFirstElement() == nextElement) {
						// follow the branch and check the remaining sequence
						return edge.getNextSequenceIndex(indexer, rawTraceIterator, indexedtrace);
					}
				}
				// no branch with the next element exists
				System.err.println("No matching edge present after existing sequence in tree.");
				return GSTree.BAD_INDEX;
			}
		} else {
			// raw trace is at its end!
			// if we get to this point, both sequences are identical up to the end
			// we still need to check if there exists an ending edge, in this case
			for (GSTreeNode edge : this.getEdges()) {
				if (edge.getFirstElement() == GSTree.SEQUENCE_END) {
					// we are done!
					indexedtrace.add(indexer.getSequenceIdForEndNode(edge));
					// return an invalid index to mark the (successful) end
					return -1;
				}
			}
			// no ending edge was found
			System.err.println("No ending edge present at the end.");
			return GSTree.BAD_INDEX;
		}
	}
	
}
