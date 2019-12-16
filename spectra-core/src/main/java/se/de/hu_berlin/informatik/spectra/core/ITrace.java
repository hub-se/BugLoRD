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
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;

/**
 * A basic execution trace that provides read-only access.
 *
 * @param <T>
 *            type used to identify nodes in the system.
 */
public interface ITrace<T> {

    /**
     * Returns true if the actual execution of the trace was successful and false if an error occured during execution.
     *
     * @return successful
     */
    public boolean isSuccessful();

    /**
     * Set the involvement of a single node. If no node
     * with the given identifier exists, it will be created.
     *
     * @param identifier
     *            the node to set the involvement for
     * @param involved
     *            true if the node was involved, false otherwise
     */
    public abstract void setInvolvement(T identifier, boolean involved);

    /**
     * Set the involvement of a single node.
     *
     * @param node
     *            the node to set the involvement for
     * @param involved
     *            true if the node was involved, false otherwise
     */
    public abstract void setInvolvement(INode<T> node, boolean involved);

    /**
     * Set the involvement of a single node. If no node
     * with the given identifier exists, it will be created.
     *
     * @param index
     *            the node index to set the involvement for
     * @param involved
     *            true if the node was involved, false otherwise
     */
    public abstract void setInvolvement(int index, boolean involved);
    
    /**
     * Sets the involvement of multiple nodes belonging to the given set of identifiers
     *
     * @param involvement
     *            a map of node-involvement pairs
     */
    public abstract void setInvolvementForIdentifiers(Map<T, Boolean> involvement);

    /**
     * Sets the involvement of multiple node
     *
     * @param involvement
     *            a map of node-involvement pairs
     */
    public abstract void setInvolvementForNodes(Map<INode<T>, Boolean> involvement);

    /**
     * Checks whether the given node is involved in the current trace.
     *
     * @param node
     *            the node to check
     * @return true if it was involved, false otherwise
     */
    public boolean isInvolved(INode<T> node);
    
    /**
     * Checks whether the given node is involved in the current trace.
     *
     * @param identifier
     *            the identifier of the node to check
     * @return true if there exists a node with the given identifier
     * in the spectra and the node was involved, false otherwise
     */
    public boolean isInvolved(T identifier);
    
    /**
     * Checks whether the given node is involved in the current trace.
     *
     * @param index
     *            the index of the node to check
     * @return true if there exists a node with the given index
     * in the spectra and the node was involved, false otherwise
     */
    public boolean isInvolved(int index);
    
    /**
     * @return
     * the number of nodes that are involved with this trace
     */
    public int involvedNodesCount();
    
    /**
     * @return
     * a collection holding all involved nodes
     */
    public Collection<Integer> getInvolvedNodes();
    
    /**
     * @return
     * a collection holding all execution traces for all threads
     */
    public Collection<ExecutionTrace> getExecutionTraces();
    
//    /**
//     * @return
//     * a collection holding all execution traces for all threads 
//     * (as byte arrays for storing in zip archive)
//     */
//    public Collection<byte[]> getExecutionTracesByteArrays();
    
    /**
     * Adds an execution trace to this trace object.
     *
     * @param executionTrace
     *            a list of executed nodes for one thread
     */
    public abstract void addExecutionTrace(ExecutionTrace executionTrace);
    
    /**
     * @return
     * the identifier (usually the test case name) of the trace
     */
    public String getIdentifier();
    
    /**
     * @return
     * the integer index of the trace
     */
    public int getIndex();

	public boolean storeExecutionTracesInZipFile(Path outputFile, 
			Supplier<String> traceFileNameSupplier, Supplier<String> repMarkerFileNameSupplier);

	/**
	 * Free resources when not in use.
	 */
	public void sleep();

}