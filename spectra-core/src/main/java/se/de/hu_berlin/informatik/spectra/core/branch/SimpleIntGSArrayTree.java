package se.de.hu_berlin.informatik.spectra.core.branch;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.SingleLinkedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.SingleLinkedIntArrayQueue.MyIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleIntGSArrayTree {

    // some element not contained in the input sequences TODO maybe set to 0?
    // in the future, negative indices will possibly point to node sequences, themselves...
//	public static final int SEQUENCE_END = -2;
    public static final int BAD_INDEX = -3;

    // the (virtual) root node has a lot of branches, the inner nodes should not branch that much
    // so we use a map here, and we use lists of edges in the inner nodes
    private IntGSArrayTreeNode startNode;
    
    // used to obtain new, unique IDs
	private final AtomicInteger idGenerator;
	private int endNodeCount;
    
    public SimpleIntGSArrayTree(AtomicInteger idGenerator) {
		this.idGenerator = idGenerator;
	}

    // for testing purposes
    public int addSequence(int[] is) {
		SingleLinkedIntArrayQueue queue = new SingleLinkedIntArrayQueue(50);
		for (int i : is) {
			queue.addNoAutoBoxing(i);
		}
		return addSequence(queue);
	}
    
    public int addSequence(SingleLinkedIntArrayQueue sequence) {
        if (sequence == null) {
            return BAD_INDEX;
        }

        return addSequence(sequence.iterator2(), sequence.size());
    }

    private int addSequence(MyIterator unprocessedIterator, int length) {
        return addNewNodeOrGetID(unprocessedIterator, length);
    }

    private int addNewNodeOrGetID(MyIterator unprocessedIterator, int length) {
		if (startNode == null) {
			startNode = new IntGSArrayTreeNode();
		}
		return startNode.addSequence(this, unprocessedIterator, length);
	}

	
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GS Tree: ").append(endNodeCount).append(" sequences");
        sb.append(System.lineSeparator());
        // iterate over the tree structure
        collectAllSuffixes("", startNode, sb);
        return sb.toString();
    }

    private void collectAllSuffixes(String sequence, IntGSArrayTreeNode node, StringBuilder sb) {
        sequence += ">";
        StringBuilder sequenceBuilder = new StringBuilder(sequence);
        for (final int element : node.getSequence()) {
            sequenceBuilder.append(element).append(",");
        }
        sequence = sequenceBuilder.toString();
        
        if (node.index != BAD_INDEX) {
            sb.append(sequence).append("(#").append(node.index).append(")");
            sb.append(System.lineSeparator());
//            return;
        }
        
        
        for (IntGSArrayTreeNode edge : node.getEdges()) {
            collectAllSuffixes(sequence, edge, sb);
        }
    }


    public int getNumberOfDifferentBranches() {
        return endNodeCount;
    }
    
    public static class IntGSArrayTreeNode {

        private int[] sequence;
        private List<IntGSArrayTreeNode> edges;
        private int index = BAD_INDEX;

        public IntGSArrayTreeNode() {
        }

        private IntGSArrayTreeNode(int[] sequence, List<IntGSArrayTreeNode> edges, int index) {
			this.sequence = sequence;
			this.edges = edges;
			this.index = index;
		}

		public int[] getSequence() {
            return sequence;
        }

        public List<IntGSArrayTreeNode> getEdges() {
            return edges == null ? Collections.emptyList() : edges;
        }

        public int addSequence(SimpleIntGSArrayTree tree, MyIterator unprocessedIterator, int length) {
        	if (this.sequence == null) {
        		// fresh node!
        		// set the sequence and add an ending edge
        		sequence = new int[length];
                for (int j = 0; j < length; ++j) {
                	sequence[j] = unprocessedIterator.nextNoAutoBoxing();
                }
//                this.edges = new ArrayList<>(1);
                return addNewEndNode(tree);
        	} else {
        		// check how much of this node's sequence is identical to the sequence to add;
        		
//        		// we can start from index 1 (and posIndex + 1), since the first elements 
//        		// are guaranteed to be identical
//        		unprocessedIterator.next();
//        		int i = 1;
        		
        		int i = 0;
        		for (; i < this.sequence.length; i++, unprocessedIterator.nextNoAutoBoxing()) {
        			if (i < length) {
        				// both sequences still contain elements
        				int nextAddedElement = unprocessedIterator.peek();

        				int nextExistingElement = this.sequence[i];
        				// check if the sequences differ at this position
        				if (nextAddedElement != nextExistingElement) {
        					// split the sequence at this position
        					splitSequenceAtIndex(i);

        					// add a branch for the new, diverging sequence
        					//    					int[] remainingSequenceToAdd = Arrays.copyOfRange(sequenceToAdd, posIndex, endIndex);
        					return addNewBranch(tree, unprocessedIterator, length - i);
        				}
        			} else {
        				// sequence to add is smaller than existing sequence
        				// split sequence at this point and add branch with marked ending point
        				splitSequenceAtIndex(i);

        				// add a branch for the new, diverging sequence (sequence ending, in this case)
        				return addNewEndNode(tree);
        			}
        		}

        		// this node's sequence has ended;
        		// check if there are still elements in the sequence to add
        		if (i < length && unprocessedIterator.hasNext()) {
        			// sequence to add is larger than existing sequence
        			int nextAddedElement = unprocessedIterator.peek();

        			//    			System.out.println("new edge start: " + nextAddedElement);

        			// the next element is an element that has NOT been identified as a starting element before
        			for (IntGSArrayTreeNode edge : this.getEdges()) {
        				if (edge.getFirstElement() == nextAddedElement) {
        					// follow the branch and add the remaining sequence
        					return edge.addSequence(tree, unprocessedIterator, length - i);
        				}
        			}
        			// no branch with the next element exists, so simply add a new branch with the remaining sequence
        			return addNewBranch(tree, unprocessedIterator, length - i);
        		} else {
        			// if we get to this point, both sequences are identical up to the end

        			// we still need to check if there already exists an ending edge, in this case
        			if (this.index == BAD_INDEX) {
        				return addNewEndNode(tree);
        			} else {
						return this.index;
					}

        		}
        	}
        }

		private int addNewBranch(SimpleIntGSArrayTree tree, MyIterator unprocessedIterator, int length) {
			IntGSArrayTreeNode node = new IntGSArrayTreeNode();
			if (this.edges == null) {
				this.edges  = new ArrayList<>(1);
			}
			this.edges.add(node);
			return node.addSequence(tree, unprocessedIterator, length);
		}

		public int addNewEndNode(SimpleIntGSArrayTree tree) {
			this.index = tree.idGenerator.incrementAndGet();
//			edges.add(new IntGSArrayTreeEndNode(id));
			++tree.endNodeCount;
			return index;
		}

        private void splitSequenceAtIndex(int index) {
            // split sequence at this point and add a branch
            int[] remainingSequence = Arrays.copyOfRange(this.sequence, index, this.sequence.length);
            // the new sequence of this node is the sequence up to the diverging position
            this.sequence = Arrays.copyOfRange(this.sequence, 0, index);
            // remember the previously existing edges
            List<IntGSArrayTreeNode> existingEdges = this.edges;
            // the new set of edges contains the remaining sequence of the previously existing node
            // and the diverging sequence that is added
            this.edges  = new ArrayList<>(2);
            // split this node and move the previously existing edges to the newly created node
            IntGSArrayTreeNode node = new IntGSArrayTreeNode(remainingSequence, existingEdges, this.index);
			this.edges.add(node);
			
			// invalidate this node's index
			this.index = BAD_INDEX;
        }

        public int getFirstElement() {
            return this.sequence == null ? BAD_INDEX : this.sequence[0];
        }

        public boolean contains(int element) {
            for (int i : sequence) {
                if (i == element) {
                    return true;
                }
            }
            return false;
        }
        
    }
    
}
