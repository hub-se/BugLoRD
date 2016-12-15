/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.spectra;

import java.util.Collection;
import java.util.List;


/**
 * Provides the interface that can represent a whole spectra.
 *
 * @param <T>
 *            type used to identify nodes in the system.
 */
public interface ISpectra<T> {

    /**
     * Returns the collection of all nodes.
     * 
     * @return
     * a collection of all nodes
     */
    public abstract Collection<INode<T>> getNodes();

    /**
     * Returns the node for the given identifier.
     *
     * If the node is not present in the current spectra, the node will be created.
     *
     * @param identifier
     *            identifier
     * @return the spectra node object for the identifier
     */
    public abstract INode<T> getOrCreateNode(T identifier);
    
    /**
     * Removes (deletes) a node from the spectra.
     *
     * @param identifier
     *            identifier
     * @return true if successful, false otherwise
     */
    public abstract boolean removeNode(T identifier);

    /**
     * Checks whether the node with the given identifier is present in the current spectra.
     *
     * @param identifier
     *            of the node
     * @return true if it is present, false otherwise
     */
    public abstract boolean hasNode(T identifier);

    /**
     * Returns a list of all traces available in the spectra.
     *
     * @return traces
     */
    public abstract Collection<ITrace<T>> getTraces();
    
    /**
     * @param identifier
     * the identifier of a trace
     * @return
     * the trace with the given identifier or null if
     * no trace with the given identifier exists.
     */
    public abstract ITrace<T> getTrace(String identifier);

    /**
     * Returns all failing traces in this spectra.
     *
     * @return failingTraces
     */
    public abstract Collection<ITrace<T>> getFailingTraces();

    /**
     * Returns all successful traces in this spectra.
     *
     * @return successfulTraces
     */
    public abstract List<ITrace<T>> getSuccessfulTraces();

}