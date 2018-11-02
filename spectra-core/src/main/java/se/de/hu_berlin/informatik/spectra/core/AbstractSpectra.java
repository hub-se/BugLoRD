/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.core;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.spectra.core.traces.SequenceIndexer;


/**
 * The spectra class holds all nodes and traces belonging to the spectra.
 *
 * You can imagine the information accessible through this class has a matrix layout:
 *
 * <pre>
 *          | Trace1 | Trace2 | Trace3 | ... | TraceN |
 *  --------|--------|--------|--------|-----|--------|
 *  Node1   |   1    |   0    |   0    | ... |   1    |
 *  Node2   |   1    |   0    |   1    | ... |   1    |
 *  Node3   |   0    |   1    |   0    | ... |   1    |
 *  ...     |  ...   |  ...   |  ...   | ... |  ...   |
 *  NodeX   |   1    |   1    |   1    | ... |   0    |
 *  --------|--------|--------|--------|-----|--------|
 *  Result  |   1    |   1    |   0    | ... |   0    |
 * </pre>
 *
 * The nodes are the components of a system that are analyzed. For each trace the involvement of the node is stored. A
 * '1' denotes node involvement, a '0' denotes no involvement of the node in the current execution trace. For each
 * execution trace we also know whether the execution was successful or not.
 *
 * Given this information, it is possible to use this spectra as input for various fault localization techniques.
 *
 * @param <T>
 *            type used to identify nodes in the system.
 */
public abstract class AbstractSpectra<T,K extends ITrace<T>> implements Cloneable, ISpectra<T,K> {

    /** Holds all nodes belonging to this spectra, retrievable by an integer index */
    protected final List<INode<T>> nodesByIndex = new ArrayList<>();
    protected int currentIndex = -1;
    
    /** Additionally access to all nodes belonging to this spectra, indexed by the node's identifier */
    protected final Map<T, INode<T>> nodesByIdentifier = new HashMap<>();

    /** Holds all traces belonging to this spectra */
    private final Map<String,K> traces = new HashMap<>();
    
    private Map<K, Map<K, Double>> similarities = null;

	private LocalizerCache<T> localizer;
	private SequenceIndexer indexer;
	private Path spectraZipFile;

//    /**
//     * Creates a new spectra.
//     */
//    public AbstractSpectra() {
//        super();
//    }
    
    public AbstractSpectra(Path spectraZipFile) {
    	super();
    	this.spectraZipFile = spectraZipFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getPathToSpectraZipFile() {
    	return spectraZipFile;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<INode<T>> getNodes() {
        return nodesByIdentifier.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public INode<T> getOrCreateNode(final T identifier) {
        if (!nodesByIdentifier.containsKey(identifier)) {
        	Node<T> node = new Node<T>(++currentIndex, identifier, this);
        	nodesByIndex.add(node);
        	nodesByIdentifier.put(identifier, node);
        	return node;
        } else {
        	return nodesByIdentifier.get(identifier);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public INode<T> getNode(final T identifier) {
        return nodesByIdentifier.get(identifier);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public INode<T> getNode(final int index) {
    	if (index < 0 || index >= nodesByIndex.size()) {
    		return null;
    	} else {
    		return nodesByIndex.get(index);
    	}
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeNode(final T identifier) {
    	INode<T> node = nodesByIdentifier.remove(identifier);
    	if (node != null) {
    		//remove node from index map
    		nodesByIndex.set(node.getIndex(), null);
    		//remove node from traces
    		for (K trace : traces.values()) {
    			trace.setInvolvement(node, false);
    		}
    		removeNodeFromSequences(node);
    	}
    	invalidateCachedValues();
    	return true;
    }

    private void removeNodeFromSequences(INode<T> node) {
		if (indexer != null) {
			int index = node.getIndex();
			// iterate over all sequences
			for (int i = 0; i < indexer.getSequences().length; i++) {
				int[] sequence = indexer.getSequences()[i];
				boolean found = false;
				for (int j = 0; j < sequence.length; j++) {
					if (sequence[j] == index) {
						// sequence contains the node at least once 
						found = true;
						break;
					}
				}
				if (found) {
					// sequence contains the node, so generate a new sequence and replace the old
					List<Integer> newSequence = new ArrayList<>(sequence.length - 1);
					for (int j = 0; j < sequence.length; j++) {
						if (sequence[j] != index) {
							newSequence.add(sequence[j]);
						}
					}
					indexer.getSequences()[i] = newSequence.stream().mapToInt(k -> k).toArray();
				}
			}
		}
	}

	/**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNode(final T identifier) {
        return nodesByIdentifier.containsKey(identifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<K> getTraces() {
        return this.traces.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<K> getFailingTraces() {
        final List<K> failingTraces = new ArrayList<>();
        for (final K trace : this.traces.values()) {
            if (!trace.isSuccessful()) {
                failingTraces.add(trace);
            }
        }
        return failingTraces;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<K> getSuccessfulTraces() {
        final List<K> successTraces = new ArrayList<>();
        for (final K trace : traces.values()) {
            if (trace.isSuccessful()) {
                successTraces.add(trace);
            }
        }
        return successTraces;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public K addTrace(final String identifier, int traceIndex, final boolean successful) {
    	if (traces.containsKey(identifier)) {
    		throw new IllegalArgumentException("Trace '" + identifier + "' is already contained in spectra.");
    	} else {
    		final K trace = createNewTrace(identifier, traceIndex, successful);
    		traces.put(identifier, trace);
    		invalidateCachedValues();
    		return trace;
    	}
    }

    protected abstract K createNewTrace(String identifier, int traceIndex, boolean successful);

	@SuppressWarnings("unchecked")
    @Override
    public AbstractSpectra<T,K> clone() throws CloneNotSupportedException {
        return (AbstractSpectra<T,K>) super.clone();
    }

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + getNodes().size();
		result = 31 * result + getTraces().size();
		result = 31 * result + getFailingTraces().size();
		for (INode<T> node : getNodes()) {
			result = 31 * result + node.hashCode();
		}
		for (ITrace<T> trace : getTraces()) {
			result = 31 * result + trace.hashCode();
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AbstractSpectra) {
			AbstractSpectra<?,?> o = (AbstractSpectra<?,?>) obj;
			//must have the same number of nodes and traces
			if (this.getNodes().size() != o.getNodes().size() ||
					this.getTraces().size() != o.getTraces().size() ||
					this.getFailingTraces().size() != o.getFailingTraces().size()) {
				return false;
			}
			//all nodes have to be identical
			for (INode<?> node : o.getNodes()) {
				if (!this.getNodes().contains(node)) {
					return false;
				}
			}
			//all traces have to be identical
			for (ITrace<?> otherTrace : o.getTraces()) {
				boolean foundEqual = false;
				for (ITrace<T> trace : this.getTraces()) {
					if (otherTrace.equals(trace)) {
						foundEqual = true;
						break;
					}
				}
				if (!foundEqual) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public K getTrace(String identifier) {
		for (K trace : getTraces()) {
			if (trace.getIdentifier().equals(identifier)) {
				return trace;
			}
		}
		return null;
	}
    
	
	@Override
	public Map<K, Double> getSimilarityMap(ITrace<T> failingTrace) {
		//only is computed for failing traces right now!
		if (failingTrace.isSuccessful()) {
			return null;
		}
		if (similarities == null) {
			computeSimilarities();
		}
		return similarities.get(failingTrace);
	}
	
	private void computeSimilarities() {
		similarities = new HashMap<>();
		//have to compute a value for each failing trace
    	for (final K failingTrace : this.getFailingTraces()) {
    		Map<K, Double> similarityScores = new HashMap<>();
    		similarities.put(failingTrace, similarityScores);
    		
    		double failingTraceSize = failingTrace.getInvolvedNodes().size();
    		//for every trace, compute a similarity score to the current failing trace
    		for (final K trace : this.getTraces()) {
    			int equallyInvolvedNodes = 0;
                for (final int index : failingTrace.getInvolvedNodes()) {
                	if (trace.isInvolved(index)) {
                		++equallyInvolvedNodes;
                	}
                }
                similarityScores.put(trace, (double)equallyInvolvedNodes / failingTraceSize);
            }
        }
	}

	@Override
	public void invalidateCachedValues() {
		similarities = null;
	}

	@Override
	public ILocalizerCache<T> getLocalizer() {
		if (localizer == null) {
			localizer = new LocalizerCache<>(this);
		}
		return localizer;
	}

	@Override
	public SequenceIndexer getIndexer() {
		return this.indexer;
	}

	@Override
	public void setIndexer(SequenceIndexer indexer) {
		this.indexer = indexer;
	}
	
}
