/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.core;

import se.de.hu_berlin.informatik.spectra.core.traces.RawIntTraceCollector;
import se.de.hu_berlin.informatik.spectra.core.traces.SequenceIndexerCompressed;
import se.de.hu_berlin.informatik.spectra.util.SpectraUtils;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * Provides the interface that can represent a whole spectra.
 *
 * @param <T> type used to identify nodes in the system.
 * @param <K> type of traces
 */
public interface ISpectra<T, K extends ITrace<T>> {

    /**
     * @return the path to the spectra zip file, if existing
     */
    public Path getPathToSpectraZipFile();

    /**
     * Returns the collection of all nodes.
     *
     * @return a collection of all nodes
     */
    public Collection<INode<T>> getNodes();

    /**
     * Returns the node for the given identifier.
     * <p>
     * If the node is not present in the current spectra, the node will be created.
     * (indices should start at 0)
     *
     * @param identifier identifier
     * @return the spectra node object for the identifier
     */
    public INode<T> getOrCreateNode(T identifier);

    /**
     * Returns the node for the given identifier.
     * <p>
     * If the node is not present in the current spectra, null will be returned.
     *
     * @param identifier identifier
     * @return the spectra node object for the identifier, if existing; null otherwise
     */
    public INode<T> getNode(T identifier);

    /**
     * Returns the node for the given index.
     * <p>
     * If the node is not present in the current spectra, null will be returned.
     *
     * @param index the index
     * @return the spectra node object for the identifier, if existing; null otherwise
     */
    public INode<T> getNode(int index);

    /**
     * Removes (deletes) a node from the spectra.
     *
     * @param identifier identifier
     * @return true if successful, false otherwise
     */
    public boolean removeNode(T identifier);

    /**
     * Removes (deletes) a node from the spectra.
     *
     * @param index index
     * @return true if successful, false otherwise
     */
    public boolean removeNode(int index);

    /**
     * Removes (deletes) the given nodes from the spectra.
     *
     * @param identifiers identifiers
     * @return true if successful, false otherwise
     */
    public boolean removeNodes(Collection<T> identifiers);

    /**
     * Removes (deletes) the given nodes from the spectra.
     *
     * @param indices indices
     * @return true if successful, false otherwise
     */
    public boolean removeNodesByIndex(Collection<Integer> indices);

    /**
     * Checks whether the node with the given identifier is present in the current spectra.
     *
     * @param identifier of the node
     * @return true if it is present, false otherwise
     */
    public boolean hasNode(T identifier);

    /**
     * Returns a list of all traces available in the spectra.
     *
     * @return traces
     */
    public Collection<K> getTraces();

    /**
     * @param identifier the identifier of a trace
     * @return the trace with the given identifier or null if
     * no trace with the given identifier exists.
     */
    public K getTrace(String identifier);

    /**
     * Adds a new trace to this spectra.
     *
     * @param identifier the identifier of the trace (usually the test case name)
     * @param traceIndex the index of the trace to add
     * @param successful true if the trace execution was successful, false otherwise
     * @return the trace object
     */
    public K addTrace(final String identifier, int traceIndex, final boolean successful);

    /**
     * Returns all failing traces in this spectra.
     *
     * @return failingTraces
     */
    public Collection<K> getFailingTraces();

    /**
     * Returns all successful traces in this spectra.
     *
     * @return successfulTraces
     */
    public List<K> getSuccessfulTraces();

    /**
     * @return whether this spectra contains zero traces and/or zero nodes
     */
    default public boolean isEmpty() {
        return (getNodes().size() == 0 || getTraces().size() == 0);
    }

    /**
     * Removes all nodes from this spectra that are of the specified type (at this moment).
     *
     * @param coverageType the type of the nodes to remove
     * @return this spectra (modified)
     */
    default public ISpectra<T, K> removeNodesWithCoverageType(INode.CoverageType coverageType) {
        SpectraUtils.removeNodesWithCoverageType(this, coverageType);
        return this;
    }
    
    /**
     * Removes all nodes from this spectra that belong to test classes.
     *
     * @return this spectra (modified)
     */
    default public ISpectra<T, K> removeTestClassNodes(T dummy) {
        SpectraUtils.removeTestClassNodes(dummy, this);
        return this;
    }

    public Map<K, TraceInfo> getSimilarityMap(ITrace<T> failingTrace);

    public ILocalizerCache<T> getLocalizer();

    /**
     * Invalidates any cached values that may have been stored for the node.
     */
    public void invalidateCachedValues();

    /**
     * @return an indexer used for indexing sequences of execution traces, if available
     */
    public SequenceIndexerCompressed getIndexer();

    /**
     * @param indexer an indexer used for indexing sequences of execution traces
     */
    public void setIndexer(SequenceIndexerCompressed indexer);

    /**
     * @return the raw trace collector, if existing
     */
    public RawIntTraceCollector getRawTraceCollector();

    /**
     * @param traceCollector a raw execution trace collector, used to generate execution traces
     */
    public void setRawTraceCollector(RawIntTraceCollector traceCollector);


}