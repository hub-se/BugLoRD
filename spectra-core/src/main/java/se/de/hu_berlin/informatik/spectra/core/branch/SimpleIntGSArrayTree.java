package se.de.hu_berlin.informatik.spectra.core.branch;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.SingleLinkedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.SingleLinkedIntArrayQueue.MyIterator;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleIntGSArrayTree implements Iterable<Pair<Integer, int[]>>, Serializable {
	
	/*
	 * example tree: (int arrays may be empty)
	 * 
	 * 					node(int[], id)
	 *  					|			\
	 *  					|			 \
	 *  				node(int[], id)	node(int[], id)
	 *  					|
	 *  					|
	 *  				node(int[], id)
	 *  			 /		|		   \
	 *  			/		|			\
	 *  node(int[], id)	node(int[], id)	node(int[], id)
	 *  		|
	 *  		|
	 *  node(int[], id)
	 */

	private static final byte MAGIC_1BYTE = (byte) 255;
    private static final byte MAGIC_2BYTES = (byte) 254;
    private static final byte MAGIC_3BYTES = (byte) 253;
    private static final byte MAGIC_4BYTES = (byte) 252;
    private static final int LOWER_8_BITS = 0xff;
    private static final int HIGHER_8_BITS = 0xff000000;
    private static final int HIGHER_16_BITS = 0xffff0000;
    private static final int HIGHER_24_BITS = 0xffffff00;

    private static void writeInt(final OutputStream out, final int value) throws IOException {
        if ((value & HIGHER_24_BITS) == 0) {
            if (value >= 252)
                out.write(MAGIC_1BYTE);
        } else if ((value & HIGHER_16_BITS) == 0) {
            out.write(MAGIC_2BYTES);
            out.write(value >>> 8);
        } else if ((value & HIGHER_8_BITS) == 0) {
            out.write(MAGIC_3BYTES);
            out.write(value >>> 16);
            out.write(value >>> 8);
        } else {
            out.write(MAGIC_4BYTES);
            out.write(value >>> 24);
            out.write(value >>> 16);
            out.write(value >>> 8);
        }
        out.write(value);
    }

    private static int readInt(final InputStream in) throws IOException {
        int b0, b1, b2;
        int b3 = in.read();
        switch (b3) {
            case -1:
                throw new EOFException();
            case MAGIC_1BYTE & LOWER_8_BITS:
                b0 = in.read();
                if (b0 < 0)
                    throw new EOFException();
                return (b0 & LOWER_8_BITS);
            case MAGIC_2BYTES & LOWER_8_BITS:
                b1 = in.read();
                b0 = in.read();
                if ((b0 | b1) < 0)
                    throw new EOFException();
                return ((b1 & LOWER_8_BITS) << 8)
                        | (b0 & LOWER_8_BITS);
            case MAGIC_3BYTES & LOWER_8_BITS:
                b2 = in.read();
                b1 = in.read();
                b0 = in.read();
                if ((b0 | b1 | b2) < 0)
                    throw new EOFException();
                return ((b2 & LOWER_8_BITS) << 16)
                        | ((b1 & LOWER_8_BITS) << 8)
                        | (b0 & LOWER_8_BITS);
            case MAGIC_4BYTES & LOWER_8_BITS:
                b3 = in.read();
                b2 = in.read();
                b1 = in.read();
                b0 = in.read();
                if ((b0 | b1 | b2 | b3) < 0)
                    throw new EOFException();
                return ((b3 & LOWER_8_BITS) << 24)
                        | ((b2 & LOWER_8_BITS) << 16)
                        | ((b1 & LOWER_8_BITS) << 8)
                        | (b0 & LOWER_8_BITS);
            default:
                return (b3 & LOWER_8_BITS);
        }
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
    	writeNode(stream, startNode);
    }

    public static void writeNode(ObjectOutputStream objOut, IntGSArrayTreeNode node) throws IOException {
    	// sequence index
    	writeInt(objOut, node.getIndex());
    	int[] sequence = node.getSequence();
    	// sequence length
		writeInt(objOut, sequence.length);
		// sequence
        for (int value : sequence) {
            writeInt(objOut, value);
        }
        List<IntGSArrayTreeNode> edges = node.getEdges();
        // number of edges (may be 0)
		writeInt(objOut, edges.size());
		// edges (nodes)
		for (IntGSArrayTreeNode edge : edges) {
			// write edges recursively
			writeNode(objOut, edge);
		}
	}
    
    private void readObject(ObjectInputStream stream) throws IOException {
    	this.startNode = readNode(stream);
    }

	public static IntGSArrayTreeNode readNode(ObjectInputStream objIn) throws IOException {
		// sequence index
		int index = readInt(objIn);
		// sequence length
		int seqLength = readInt(objIn);
		// sequence
        int[] sequence = new int[seqLength];
        for (int i = 0; i < seqLength; ++i) {
        	sequence[i] = readInt(objIn);
        }
        // number of edges (may be 0)
        int edgeCount = readInt(objIn);
        // edges (nodes)
        List<IntGSArrayTreeNode> edges;
        if (edgeCount <= 0) {
        	edges = null;
        } else {
        	edges = new ArrayList<>(edgeCount);
        	for (int i = 0; i < edgeCount; ++i) {
        		// read edges recursively
        		edges.add(readNode(objIn));
        	}
        	
		}
		return new IntGSArrayTreeNode(sequence, edges, index);
	}
	
    // some element not contained in the input sequences TODO maybe set to 0?
    // in the future, negative indices will possibly point to node sequences, themselves...
//	public static final int SEQUENCE_END = -2;
    public static final int BAD_INDEX = 0;

    // the (virtual) root node has a lot of branches, the inner nodes should not branch that much
    // so we use a map here, and we use lists of edges in the inner nodes
    private IntGSArrayTreeNode startNode;
    
    public SimpleIntGSArrayTree() {
	}
    
    public SimpleIntGSArrayTree(IntGSArrayTreeNode startNode) {
		this.startNode = startNode;
	}

    // for testing purposes
    public int addSequence(AtomicInteger idGenerator, int[] is) {
		SingleLinkedIntArrayQueue queue = new SingleLinkedIntArrayQueue(50);
		for (int i : is) {
			queue.addNoAutoBoxing(i);
		}
		return addSequence(idGenerator, queue);
	}
    
    public int addSequence(AtomicInteger idGenerator, SingleLinkedIntArrayQueue sequence) {
        if (sequence == null) {
            return BAD_INDEX;
        }

        return addSequence(idGenerator, sequence.iterator2(), sequence.size());
    }

    private int addSequence(AtomicInteger idGenerator, MyIterator unprocessedIterator, int length) {
        return addNewNodeOrGetID(idGenerator, unprocessedIterator, length);
    }

    private int addNewNodeOrGetID(AtomicInteger idGenerator, MyIterator unprocessedIterator, int length) {
		if (startNode == null) {
			startNode = new IntGSArrayTreeNode();
		}
		return startNode.addSequence(idGenerator, unprocessedIterator, length);
	}

	
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GS Tree: ").append(getNumberOfSequences()).append(" sequences");
        sb.append(System.lineSeparator());
        // iterate over the tree structure
        collectAllSuffixes("", startNode, sb);
        return sb.toString();
    }
    
    @Override
	public Iterator<Pair<Integer, int[]>> iterator() {
		// iterates over all valid sequences in the tree
		return new TreeIterator();
	}
    
    private class TreeIterator implements Iterator<Pair<Integer,int[]>> {
    	
    	// depth-first iteration
    	Stack<Pair<int[], IntGSArrayTreeNode>> openNodes;
    	// next element
    	Pair<Integer,int[]> next = null;
    	
    	public TreeIterator() {
    		openNodes = new Stack<Pair<int[],IntGSArrayTreeNode>>();
    		openNodes.push(new Pair<int[], SimpleIntGSArrayTree.IntGSArrayTreeNode>(new int[0], startNode));
		}

		@Override
		public boolean hasNext() {
			if (next == null) {
				next = getNextSequence(openNodes);
			}
			if (next == null) {
				return false;
			} else {
				return true;
			}
		}

		@Override
		public Pair<Integer, int[]> next() {
			Pair<Integer,int[]> temp = next;
			next = null;
			return temp;
		}
		
		private Pair<Integer, int[]> getNextSequence(Stack<Pair<int[], IntGSArrayTreeNode>> openNodes) {
			while (!openNodes.isEmpty()) {
				// get the next node from the stack
				Pair<int[], IntGSArrayTreeNode> node = openNodes.pop();
				// append this node's sequence to the previous sequence (make a copy)
				int[] previousSequence = node.first();
				int[] nodeSequence = node.second().getSequence();
				int[] currentSequence = new int[previousSequence.length + nodeSequence.length];
				System.arraycopy(previousSequence, 0, currentSequence, 0, previousSequence.length);
				System.arraycopy(nodeSequence, 0, currentSequence, previousSequence.length, nodeSequence.length);

				// push edges onto the stack
				for (IntGSArrayTreeNode edge : node.second().getEdges()) {
					openNodes.push(new Pair<>(currentSequence, edge));
				}

				if (node.second().index != BAD_INDEX) {
					return new Pair<Integer, int[]>(node.second().index, currentSequence);
				}
			}

			// no valid sequences found in stack
			return null;
	    }
	};
	
    
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
    
    private int countAllSuffixes(IntGSArrayTreeNode node) {
    	int validNodes = 0;
    	if (node.index != BAD_INDEX) {
            ++validNodes;
        }
        
        
        for (IntGSArrayTreeNode edge : node.getEdges()) {
        	validNodes += countAllSuffixes(edge);
        }
        return validNodes;
    }
    
    public IntGSArrayTreeNode getStartNode() {
    	return startNode;
	}


    public int getNumberOfSequences() {
        return countAllSuffixes(startNode);
    }
    
    public static class IntGSArrayTreeNode {

        private int[] sequence;
        private List<IntGSArrayTreeNode> edges;
        private int index = BAD_INDEX;

        public IntGSArrayTreeNode() {
        }

        public IntGSArrayTreeNode(int[] sequence, List<IntGSArrayTreeNode> edges, int index) {
			this.sequence = sequence;
			this.edges = edges;
			if (edges != null && edges.isEmpty()) {
				this.edges = null;
			}
			this.index = index;
		}
        
        public int getIndex() {
        	return index;
        }

		public int[] getSequence() {
            return sequence;
        }

        public List<IntGSArrayTreeNode> getEdges() {
            return edges == null ? Collections.emptyList() : edges;
        }

        public int addSequence(AtomicInteger idGenerator, MyIterator unprocessedIterator, int length) {
        	if (this.sequence == null) {
        		// fresh node!
        		// set the sequence and add an ending edge
        		sequence = new int[length];
                for (int j = 0; j < length; ++j) {
                	sequence[j] = unprocessedIterator.nextNoAutoBoxing();
                }
//                this.edges = new ArrayList<>(1);
                return addNewEndNode(idGenerator);
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
        					return addNewBranch(idGenerator, unprocessedIterator, length - i);
        				}
        			} else {
        				// sequence to add is smaller than existing sequence
        				// split sequence at this point and add branch with marked ending point
        				splitSequenceAtIndex(i);

        				// add a branch for the new, diverging sequence (sequence ending, in this case)
        				return addNewEndNode(idGenerator);
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
        					return edge.addSequence(idGenerator, unprocessedIterator, length - i);
        				}
        			}
        			// no branch with the next element exists, so simply add a new branch with the remaining sequence
        			return addNewBranch(idGenerator, unprocessedIterator, length - i);
        		} else {
        			// if we get to this point, both sequences are identical up to the end

        			// we still need to check if there already exists an ending edge, in this case
        			if (this.index == BAD_INDEX) {
        				return addNewEndNode(idGenerator);
        			} else {
						return this.index;
					}

        		}
        	}
        }

		private int addNewBranch(AtomicInteger idGenerator, MyIterator unprocessedIterator, int length) {
			IntGSArrayTreeNode node = new IntGSArrayTreeNode();
			if (this.edges == null) {
				this.edges  = new ArrayList<>(1);
			}
			this.edges.add(node);
			return node.addSequence(idGenerator, unprocessedIterator, length);
		}

		private int addNewEndNode(AtomicInteger idGenerator) {
			if (idGenerator == null) {
				throw new IllegalStateException("No ID generator available! Can not add new node.");
			}
			this.index = idGenerator.getAndIncrement();
//			edges.add(new IntGSArrayTreeEndNode(id));
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
