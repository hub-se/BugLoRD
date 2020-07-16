package se.de.hu_berlin.informatik.spectra.core.cfg;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.SingleLinkedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.DataInput;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.output.DataOutput;

public class DynamicCFG<T> implements CFG<T> {
	
	private Map<Integer, Node> nodes = new HashMap<>();
	private Set<Integer> startNodes = new HashSet<>();
	private ISpectra<T, ?> spectra;

	public DynamicCFG(ISpectra<T, ?> spectra) {
		this.spectra = spectra;
	}
	
	public DynamicCFG(ISpectra<T, ?> spectra, File inputFile) {
		this.spectra = spectra;
		parseFromFile(inputFile);
	}
	
	@Override
	public Collection<T> getIdentifiersForNode(int index) {
		Node node = getNode(index);
		if (node != null) {
			INode<T> spectraNode = spectra.getNode(index);
			if (spectraNode == null) {
				return null;
			}
			
			int count = 1;
			if (node.isMerged()) {
				count += node.getMergedIndices().length;
			}
			Collection<T> result = new ArrayList<>(count);
			result.add(spectraNode.getIdentifier());
			if (node.isMerged()) {
				for (int i : node.getMergedIndices()) {
					INode<T> spectraNode2 = spectra.getNode(i);
					if (spectraNode2 == null) {
						return null;
					}
					result.add(spectraNode2.getIdentifier());
				}
			}
			return result;
		}
		return null;
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
				tempNode = getNode(tempNode.getSuccessors()[0]);
				
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
					for (int node : tempNode.getSuccessors()) {
						if (!seenNodes.contains(node)) {
							openNodes.add(node);
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
			startNode.setSuccessorHits(lastNode.getSuccessorHits());
			startNode.setMergedIndices(nodeIndices.stream().mapToInt(k -> k).toArray());
			nodeIndices.forEach(k -> {
				// remove merged nodes
				nodes.remove(k);
			});
			
			if (startNode.hasSuccessors()) {
				// adjust predecessor nodes of successor nodes
				for (int successorIndex : startNode.getSuccessors()) {
					Node successor = getNode(successorIndex);
					for (int i = 0; i < successor.getPredecessors().length; i++) {
						int predecessor = successor.getPredecessors()[i];
						if (predecessor == lastNode.getIndex()) {
							successor.getPredecessors()[i] = startNode.getIndex();
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

	@Override
	public void save(File outputFile) {
		outputFile.getParentFile().mkdirs();
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try (ObjectOutputStream objOut = new ObjectOutputStream(byteOut)) {
        	// node count
        	DataOutput.writeInt(objOut, nodes.size());
        	// write out nodes
        	for (Entry<Integer, Node> entry : nodes.entrySet()) {
    			Node node = entry.getValue();
    			// node index
    			DataOutput.writeInt(objOut, node.getIndex());
    			
    			// merged nodes' indices
    			DataOutput.writeIntArray(objOut, node.getMergedIndices());
    			
    			// predecessor indices
    			DataOutput.writeIntArray(objOut, node.getPredecessors());
    			// predecessor hit counts
    			DataOutput.writeLongArray(objOut, node.getPredecessorHits());
    			
    			// successor indices
    			DataOutput.writeIntArray(objOut, node.getSuccessors());
    			// successor hit counts
    			DataOutput.writeLongArray(objOut, node.getSuccessorHits());
        	}
            objOut.close();

            // include some readable information (will be ignored when reading file again)
            StringBuilder sb = new StringBuilder();
            sb.append(System.lineSeparator()).append(System.lineSeparator())
            .append("nodes: ").append(nodes.size());
            
            // save to file
            try (FileOutputStream fs = new FileOutputStream(outputFile)) {
            	fs.write(byteOut.toByteArray());
            	fs.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private void parseFromFile(File inputFile) {
		// load from file
		try (FileInputStream fs = new FileInputStream(inputFile)) {
			ByteArrayOutputStream b = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[4096];

			while ((nRead = fs.read(data, 0, data.length)) != -1) {
			  b.write(data, 0, nRead);
			}
			
			byte[] input = b.toByteArray();
			
			ByteArrayInputStream byteIn = new ByteArrayInputStream(input);
	        InputStream buffer = new BufferedInputStream(byteIn);
	        ObjectInputStream objIn = new ObjectInputStream(buffer);
	        try {
	        	// node count
	        	int nodeCount = DataInput.readInt(objIn);
	        	// read all nodes
	        	for (int i = 0; i < nodeCount; ++i) {
	    			// node index
	    			int index = DataInput.readInt(objIn);
	    			
	    			// merged nodes' indices
	    			int[] mergedNodes = DataInput.readIntArray(objIn, true);
	    			
	    			// predecessor indices
	    			int[] predecessors = DataInput.readIntArray(objIn, true);
	    			// predecessor hit counts
	    			long[] predecessorHits = DataInput.readLongArray(objIn, true);
	    			
	    			// successor indices
	    			int[] successors = DataInput.readIntArray(objIn, true);
	    			// successor hit counts
	    			long[] successorHits = DataInput.readLongArray(objIn, true);
	    			
	    			// add node
	    			nodes.put(index, new Node(index, mergedNodes, predecessors, predecessorHits, successors, successorHits));
	        	}
	        } finally {
	        	objIn.close();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public int hashCode() {
		int hashCode = 17;
		for (Entry<Integer, Node> entry : nodes.entrySet()) {
			hashCode += 31 * entry.getValue().hashCode();
		}
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DynamicCFG) {
			CFG<?> o = (DynamicCFG<?>) obj;
			if (o.getNodes().size() != this.getNodes().size()) {
				return false;
			}
			for (Entry<Integer, Node> entry : nodes.entrySet()) {
    			Node node = entry.getValue();
    			Node oNode = o.getNode(node.getIndex());
    			// this only checks for same indices
    			if (oNode == null || !node.equals(oNode)) {
    				return false;
    			}
    			
    			if (node.isMerged() != oNode.isMerged() || 
    					node.hasPredecessors() != oNode.hasPredecessors() ||
    							node.hasSuccessors() != oNode.hasSuccessors()) {
    				return false;
    			}
    			if (node.isMerged()) {
    				if (!Arrays.equals(node.getMergedIndices(), oNode.getMergedIndices())) {
    					return false;
    				}
    			}
    			if (node.hasPredecessors()) {
    				if (!Arrays.equals(node.getPredecessors(), oNode.getPredecessors()) || 
    						!Arrays.equals(node.getPredecessorHits(), oNode.getPredecessorHits())) {
    					return false;
    				}
    			}
    			if (node.hasSuccessors()) {
    				if (!Arrays.equals(node.getSuccessors(), oNode.getSuccessors()) || 
    						!Arrays.equals(node.getSuccessorHits(), oNode.getSuccessorHits())) {
    					return false;
    				}
    			}
			}
			return true;
		}
		return false;
	}
	
}
