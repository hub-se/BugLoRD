package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CloneableIterator;

public abstract class GSArrayTreeNode<T,K> {
	
	private GSArrayTree<T,K> treeReference;
	private T[] sequence;
	
	private List<GSArrayTreeNode<T,K>> edges;

	protected GSArrayTreeNode() {
		// only for extension (end nodes)
	}
	
	public GSArrayTreeNode(GSArrayTree<T,K> treeReference, T[] remainingSequence, List<GSArrayTreeNode<T,K>> existingEdges) {
		this.treeReference = treeReference;
		this.sequence = remainingSequence;
		this.edges = existingEdges;
	}

	public GSArrayTreeNode(GSArrayTree<T,K> treeReference, CloneableIterator<T> sequence, int length) {
		this.treeReference = treeReference;
		Iterator<T> iterator = sequence.clone();
		iterator.next();
		// check if an element in the sequence has already been identified as a starting element
		for (int i = 1; i < length; ++i) {
			// check if the next element has already been identified as a starting element, previously
			if (treeReference.checkIfStartingElementExists(treeReference.getRepresentation(iterator.next()))) {
				// set the sequence to end at position i and add an ending edge
				T[] seq = treeReference.newArray(i);
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
		T[] seq = treeReference.newArray(length);
		for (int j = 0; j < length; ++j) {
			seq[j] = sequence.next();
		}
		this.setSequenceAndAddEndingEdge(seq);
	}

	public T[] getSequence() {
		return sequence;
	}

	private void setSequenceAndAddEndingEdge(T[] sequence) {
		Objects.requireNonNull(sequence, "Input sequence to GSTree node should not be null.");
//		System.out.println(Arrays.toString(sequence));
		this.sequence = sequence;
		this.edges = new ArrayList<>(1);
		edges.add(treeReference.getNewEndNode());
	}

	public List<GSArrayTreeNode<T,K>> getEdges() {
		return edges == null ? Collections.emptyList() : edges;
	}

	private void setEdges(List<GSArrayTreeNode<T,K>> edges) {
		this.edges = edges;
	}
	
	public void addSequence(CloneableIterator<T> unprocessedIterator, int length) {
		// check how much of this node's sequence is identical to the sequence to add;
		// we can start from index 1 (and posIndex + 1), since the first elements 
		// are guaranteed to be identical
		CloneableIterator<T> iterator = unprocessedIterator;
		iterator.next();
		int i = 1;
		for (; i < this.sequence.length; i++, iterator.next()) {
			if (i < length) {
				// both sequences still contain elements
				T nextAddedElement = iterator.peek();
				K rep = treeReference.getRepresentation(nextAddedElement);
				// check if the next element has already been identified as a starting element, previously
				if (treeReference.checkIfStartingElementExists(rep)) {
					// sequence to add is smaller than existing sequence
					// split sequence at this point and add branch with marked ending point
					splitSequenceAtIndex(i);
					
					// add a branch for the new, diverging sequence (sequence ending, in this case)
					this.getEdges().add(treeReference.getNewEndNode());
					
					// add the remaining sequence to the tree
					treeReference.__addSequence(unprocessedIterator, length - i, rep);
					// stop at this point
					return;
				}
				
				K nextExistingElement = treeReference.getRepresentation(this.sequence[i]);
				// check if the sequences differ at this position
				if (!rep.equals(nextExistingElement)) {
					// split the sequence at this position
					splitSequenceAtIndex(i);
					
					// add a branch for the new, diverging sequence
//					int[] remainingSequenceToAdd = Arrays.copyOfRange(sequenceToAdd, posIndex, endIndex);
					this.getEdges().add(treeReference.newTreeNode(treeReference, unprocessedIterator, length - i));
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
			K nextAddedElement = treeReference.getRepresentation(iterator.peek());
			
			// check if the next element has already been identified as a starting element, previously
			if (treeReference.checkIfStartingElementExists(nextAddedElement)) {
				// we need to check if there exists an ending edge
				for (GSArrayTreeNode<T,K> edge : this.getEdges()) {
					if (edge instanceof GSArrayTreeEndNode) {
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
			for (GSArrayTreeNode<T,K> edge : this.getEdges()) {
				if (edge instanceof GSArrayTreeEndNode) {
					continue;
				}
				if (edge.getFirstElement().equals(nextAddedElement)) {
					// follow the branch and add the remaining sequence
					edge.addSequence(unprocessedIterator, length - i);
					return;
				}
			}
			// no branch with the next element exists, so simply add a new branch with the remaining sequence
			edges.add(treeReference.newTreeNode(treeReference, unprocessedIterator, length - i));
			return;
		} else {
			// if we get to this point, both sequences are identical up to the end

			// we still need to check if there already exists an ending edge, in this case
			for (GSArrayTreeNode<T,K> edge : this.getEdges()) {
				if (edge instanceof GSArrayTreeEndNode) {
					// we are done!
					return;
				}
			}

			// no ending edge was found
			this.getEdges().add(treeReference.getNewEndNode());
			return;
		}
	}

	private void splitSequenceAtIndex(int index) {
		// split sequence at this point and add a branch
		T[] remainingSequence = Arrays.copyOfRange(this.sequence, index, this.sequence.length);
		// the new sequence of this node is the sequence up to the diverging position
		this.sequence = Arrays.copyOfRange(this.sequence, 0, index);
		// remember the previously existing edges
		List<GSArrayTreeNode<T,K>> existingEdges = this.getEdges();
		// the new set of edges contains the remaining sequence of the previously existing node
		// and the diverging sequence that is added
		this.setEdges(new ArrayList<>(2));
		// split this node and move the previously existing edges to the newly created node
		this.getEdges().add(treeReference.newTreeNode(treeReference, remainingSequence, existingEdges));
	}
	

	public List<T[]> extractAndRemoveRemainingSequences(int index, K firstElement) {
		List<T[]> remainingSequences = new ArrayList<>();
		
		// we still need to check for further occurrences of the new starting element in the remaining sequences
		extractRemainingSequences(treeReference.newArray(0), this, index, firstElement, true, remainingSequences);
		
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

	private void extractRemainingSequences(T[] previousSequence, GSArrayTreeNode<T,K> node, int index, 
			K firstElement, boolean firstCall, List<T[]> collector) {
		if (node instanceof GSArrayTreeEndNode) {
			// we are done!
			collector.add(previousSequence);
			return;
		}
		
		// check for occurrences of the new starting element
		for (int i = index; i < node.getSequence().length; i++) {
			K element = treeReference.getRepresentation(node.getSequence()[i]);
			if (element.equals(firstElement)) {
				if (firstCall && i == index) {
					// the first checked element should 
					// always be identical to the new starting element
					continue;
				} else if (i == index) {
					// found the starting element, and it is the first element to be checked in this node
					collector.add(previousSequence);

					extractRemainingSequences(treeReference.newArray(0), node, i, firstElement, true, collector);
					return;
				} else {
					// found the starting element at a later index
					// combine previous sequence with the given node's sequence, starting from index, ending at i
					T[] target = treeReference.newArray(previousSequence.length + (i - index));
					System.arraycopy(previousSequence, 0, target, 0, previousSequence.length);
					System.arraycopy(node.getSequence(), index, target, previousSequence.length, i - index);
					collector.add(target);

					extractRemainingSequences(treeReference.newArray(0), node, i, firstElement, true, collector);
					return;
				}
			}
		}
		
		// combine previous sequence with the given node's sequence, starting from the given index
		T[] target = treeReference.newArray(previousSequence.length + (node.getSequence().length - index));
		System.arraycopy(previousSequence, 0, target, 0, previousSequence.length);
		System.arraycopy(node.getSequence(), index, target, previousSequence.length, node.getSequence().length - index);
		
		for (GSArrayTreeNode<T,K> edge : node.getEdges()) {
			extractRemainingSequences(target, edge, 0, firstElement, false, collector);
		}
	}

	public boolean checkIfMatch(T[] sequenceToCheck, int from, int to) {
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
				K nextAddedElement = treeReference.getRepresentation(sequenceToCheck[from]);
				K nextExistingElement = treeReference.getRepresentation(this.sequence[i]);
				// check if the sequences differ at this position
				// TODO null values?
				if (!nextAddedElement.equals(nextExistingElement)) {
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
			K nextAddedElement = treeReference.getRepresentation(sequenceToCheck[from]);
			for (GSArrayTreeNode<T,K> edge : this.getEdges()) {
				if (edge instanceof GSArrayTreeEndNode) {
					continue;
				}
				if (edge.getFirstElement().equals(nextAddedElement)) {
					// follow the branch and add the remaining sequence
					return edge.checkIfMatch(sequenceToCheck, from, to);
				}
			}
			// no branch with the next element exists
			return false;
		}
		
		// if we get to this point, both sequences are identical up to the end
		// we still need to check if there exists an ending edge, in this case
		for (GSArrayTreeNode<T,K> edge : this.getEdges()) {
			if (edge instanceof GSArrayTreeEndNode) {
				// we are done!
				return true;
			}
		}
		
		// no ending edge was found
		return false;
	}
	
	
	public int getSequenceIndex(ArraySequenceIndexer<T,K> indexer, Iterator<T> iterator, int remainingLength) {
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
			K nextAddedElement = treeReference.getRepresentation(iterator.next());
			K nextExistingElement = treeReference.getRepresentation(this.sequence[i]);
			
			// check if the sequences differ at this position
			if (!nextAddedElement.equals(nextExistingElement)) {
				return GSTree.BAD_INDEX;
			}
		}
		
		// check if there are still elements in the sequence to check
		if (remainingLength > this.sequence.length) {
			// sequence to check is larger than existing sequence
			K nextAddedElement = treeReference.getRepresentation(iterator.next());
			for (GSArrayTreeNode<T,K> edge : this.getEdges()) {
				if (edge instanceof GSArrayTreeEndNode) {
					continue;
				}
				if (edge.getFirstElement().equals(nextAddedElement)) {
					// follow the branch and add the remaining sequence
					return edge.getSequenceIndex(indexer, iterator, remainingLength - this.sequence.length);
				}
			}
			// no branch with the next element exists
			return GSTree.BAD_INDEX;
		}
		
		// if we get to this point, both sequences are identical up to the end
		// we still need to check if there exists an ending edge, in this case
		for (GSArrayTreeNode<T,K> edge : this.getEdges()) {
			if (edge instanceof GSArrayTreeEndNode) {
				// we are done!
				return indexer.getSequenceIdForEndNode(edge);
			}
		}
		
		// no ending edge was found
		return GSTree.BAD_INDEX;
	}
	

	public K getFirstElement() {
		return treeReference.getRepresentation(this.sequence[0]);
	}

	public boolean contains(K element) {
		for (T i : sequence) {
			if (treeReference.getRepresentation(i).equals(element)) {
				return true;
			}
		}
		return false;
	}

	public void setSequence(T[] array) {
		this.sequence = array;
	}

	public K getNextSequenceIndex(ArraySequenceIndexer<T,K> indexer, 
			Iterator<T> rawTraceIterator, Queue<Integer> indexedtrace) {
		// we are iterating through the raw trace until we find an element that is a starting element of the tree;
		// this element is returned in the end to check the index of the following sequence, and so on;
		// we start at the second element of the sequence to check
		
		// check how much of this node's sequence is identical to the sequence to check;

		// iterate over the sequence and check for equality
		for (int i = 1; i < this.sequence.length; ++i) {
			if (!rawTraceIterator.hasNext()) {
				// sequence to check is smaller than existing sequence
				System.err.println("Sequence to check is smaller than existing sequence in tree.");
				return treeReference.getBadIndexMarker();
			}
			// both sequences still contain elements
			K nextAddedElement = treeReference.getRepresentation(rawTraceIterator.next());
			K nextExistingElement = treeReference.getRepresentation(this.sequence[i]);

			// check if the sequences differ at this position
			// TODO is this necessary?
			// TODO null values?
			if (!nextAddedElement.equals(nextExistingElement)) {
				System.err.println("Sequence to check differs from existing sequence in tree.");
				return treeReference.getBadIndexMarker();
			}
		}

		// check if there are still elements in the sequence to check
		if (rawTraceIterator.hasNext()) {
			// sequence to check is larger than existing sequence
			K rep = treeReference.getRepresentation(rawTraceIterator.next());
			if (treeReference.checkIfStartingElementExists(rep)) {
				// start of a new sequence!
				// if we get to this point, both sequences are identical up to the end
				// we still need to check if there exists an ending edge, in this case
				for (GSArrayTreeNode<T,K> edge : this.getEdges()) {
					if (edge instanceof GSArrayTreeEndNode) {
						// we are done!
						indexedtrace.add(indexer.getSequenceIdForEndNode(edge));
						// return the element that marks the start of a new sequence
						return rep;
					}
				}
				// no ending edge was found
				System.err.println("No ending edge present after existing sequence in tree.");
				return treeReference.getBadIndexMarker();
			} else {
				// still inside of a sequence, so check the following nodes
				for (GSArrayTreeNode<T,K> edge : this.getEdges()) {
					if (edge instanceof GSArrayTreeEndNode) {
						continue;
					}
					if (edge.getFirstElement().equals(rep)) {
						// follow the branch and check the remaining sequence
						return edge.getNextSequenceIndex(indexer, rawTraceIterator, indexedtrace);
					}
				}
				// no branch with the next element exists
				System.err.println("No matching edge present after existing sequence in tree.");
				return treeReference.getBadIndexMarker();
			}
		} else {
			// raw trace is at its end!
			// if we get to this point, both sequences are identical up to the end
			// we still need to check if there exists an ending edge, in this case
			for (GSArrayTreeNode<T,K> edge : this.getEdges()) {
				if (edge instanceof GSArrayTreeEndNode) {
					// we are done!
					indexedtrace.add(indexer.getSequenceIdForEndNode(edge));
					// return an invalid index to mark the (successful) end
					return treeReference.getSuccessfulEndMarker();
				}
			}
			// no ending edge was found
			System.err.println("No ending edge present at the end.");
			return treeReference.getBadIndexMarker();
		}
	}
	
}
