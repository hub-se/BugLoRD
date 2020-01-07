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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import se.de.hu_berlin.informatik.spectra.core.traces.RawIntTraceCollector;
import se.de.hu_berlin.informatik.spectra.core.traces.SequenceIndexerCompressed;


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
    private final Map<Integer, INode<T>> nodesByIndex = new ConcurrentHashMap<>();
    private final AtomicInteger currentIndex = new AtomicInteger(-1);
    
    /** Additionally access to all nodes belonging to this spectra, indexed by the node's identifier */
    protected final Map<T, INode<T>> nodesByIdentifier = new ConcurrentHashMap<>();

    /** Holds all traces belonging to this spectra */
    private final Map<String,K> traces = new ConcurrentHashMap<>();
    
    private Map<K, Map<K, TraceInfo>> similarities = null;

	private LocalizerCache<T> localizer;
	private SequenceIndexerCompressed indexer;
	private Path spectraZipFile;
	private RawIntTraceCollector rawTraceCollector;

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
    
    public AbstractSpectra(RawIntTraceCollector rawTraceCollector) {
    	super();
    	this.rawTraceCollector = rawTraceCollector;
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
    public RawIntTraceCollector getRawTraceCollector() {
    	return rawTraceCollector;
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
        	int index = currentIndex.incrementAndGet();
        	Node<T> node = new Node<>(index, identifier, this);
        	nodesByIndex.put(index, node);
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
    		nodesByIndex.remove(node.getIndex());
    		//remove node from traces
    		for (K trace : traces.values()) {
    			trace.setInvolvement(node, false);
    			trace.sleep();
    		}
    		removeNodeFromSequences(node);
    	}
    	invalidateCachedValues();
    	return true;
    }

    private void removeNodeFromSequences(INode<T> node) {
		if (indexer != null) {
			indexer.removeFromSequences(node.getIndex());
		}
	}
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeNode(final int index) {
    	INode<T> node = nodesByIndex.remove(index);
    	if (node != null) {
    		//remove node from identifier map
    		nodesByIdentifier.remove(node.getIdentifier());
    		//remove node from traces
    		for (K trace : traces.values()) {
    			trace.setInvolvement(node, false);
    			trace.sleep();
    		}
    		removeNodeFromSequences(node);
    	}
    	invalidateCachedValues();
    	return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeNodes(final Collection<T> identifiers) {
    	Collection<Integer> nodesToRemove = new HashSet<>();
    	for (T identifier : identifiers) {
    		INode<T> node = nodesByIdentifier.remove(identifier);
    		if (node != null) {
    			//remove node from index map
    			nodesByIndex.remove(node.getIndex());
    			//remove node from traces
    			for (K trace : traces.values()) {
    				trace.setInvolvement(node, false);
    				trace.sleep();
    			}
    		}
    		nodesToRemove.add(node.getIndex());
    	}
    	removeNodesFromSequences(nodesToRemove);
    	invalidateCachedValues();
    	return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeNodesByIndex(final Collection<Integer> indices) {
    	for (Integer index : indices) {
    		INode<T> node = nodesByIndex.remove(index);
    		if (node != null) {
    			//remove node from index map
    			nodesByIdentifier.remove(node.getIdentifier());
    			//remove node from traces
    			for (K trace : traces.values()) {
    				trace.setInvolvement(node, false);
    				trace.sleep();
    			}
    		}
    	}
    	removeNodesFromSequences(indices);
    	invalidateCachedValues();
    	return true;
    }
    
    private void removeNodesFromSequences(Collection<Integer> nodesToRemove) {
		if (indexer != null) {
			indexer.removeFromSequences(nodesToRemove);
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
	public Map<K, TraceInfo> getSimilarityMap(ITrace<T> failingTrace) {
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
    		Map<K, TraceInfo> similarityScores = new HashMap<>();
    		similarities.put(failingTrace, similarityScores);
    		
//    		double failingTraceSize = failingTrace.getInvolvedNodes().size();
    		//for every trace, compute a similarity score to the current failing trace
    		for (final K trace : this.getTraces()) {
    			TraceInfo info = new TraceInfo();
    			
    			int equallyInvolvedNodes = 0;
                for (final int index : failingTrace.getInvolvedNodes()) {
                	if (trace.isInvolved(index)) {
                		++equallyInvolvedNodes;
                	}
                }
                info.setSameHitCount(equallyInvolvedNodes);
                similarityScores.put(trace, info);
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
	public SequenceIndexerCompressed getIndexer() {
		return this.indexer;
	}

	@Override
	public void setIndexer(SequenceIndexerCompressed indexer) {
		this.indexer = indexer;
	}
	
	@Override
	public void setRawTraceCollector(RawIntTraceCollector rawTraceCollector) {
		this.rawTraceCollector = rawTraceCollector;
	}
	
}
