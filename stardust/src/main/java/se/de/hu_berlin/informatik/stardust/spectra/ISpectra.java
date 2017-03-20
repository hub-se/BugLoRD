/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.spectra;

import java.util.ArrayList;
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
    public Collection<INode<T>> getNodes();

    /**
     * Returns the node for the given identifier.
     *
     * If the node is not present in the current spectra, the node will be created.
     *
     * @param identifier
     *            identifier
     * @return the spectra node object for the identifier
     */
    public INode<T> getOrCreateNode(T identifier);
    
    /**
     * Removes (deletes) a node from the spectra.
     *
     * @param identifier
     *            identifier
     * @return true if successful, false otherwise
     */
    public boolean removeNode(T identifier);

    /**
     * Checks whether the node with the given identifier is present in the current spectra.
     *
     * @param identifier
     *            of the node
     * @return true if it is present, false otherwise
     */
    public boolean hasNode(T identifier);

    /**
     * Returns a list of all traces available in the spectra.
     *
     * @return traces
     */
    public Collection<? extends ITrace<T>> getTraces();
    
    /**
     * @param identifier
     * the identifier of a trace
     * @return
     * the trace with the given identifier or null if
     * no trace with the given identifier exists.
     */
    public ITrace<T> getTrace(String identifier);

    /**
     * Returns all failing traces in this spectra.
     *
     * @return failingTraces
     */
    public Collection<ITrace<T>> getFailingTraces();

    /**
     * Returns all successful traces in this spectra.
     *
     * @return successfulTraces
     */
    public List<ITrace<T>> getSuccessfulTraces();
    
    /**
     * @return
     * whether this spectra contains zero traces and/or zero nodes
     */
    default public boolean isEmpty() {
    	return (getNodes().size() == 0 || getTraces().size() == 0);
    }
    
    /**
     * Removes all nodes from this spectra that were not
     * executed by any failing trace.
     * @return
     * this spectra (modified)
     */
    default public ISpectra<T> removePurelySuccessfulNodes() {
    	Collection<ITrace<T>> failedTraces = getFailingTraces();
		//get a copy of the current set of nodes, since we will be removing nodes
		List<INode<T>> nodes = new ArrayList<>(getNodes());
		for (INode<T> node : nodes) {
			boolean isInvolvedInFailedTrace = false;
			for (ITrace<T> failedTrace : failedTraces) {
				if (failedTrace.isInvolved(node)) {
					isInvolvedInFailedTrace = true;
					break;
				}
			}
			if (!isInvolvedInFailedTrace) {
				removeNode(node.getIdentifier());
			}
		}
		return this;
    }
    
    /**
     * Removes all nodes from this spectra that were
     * executed by at least one failing trace.
     * @return
     * this spectra (modified)
     */
    default public ISpectra<T> removeFailingNodes() {
    	Collection<ITrace<T>> failedTraces = getFailingTraces();
		//get a copy of the current set of nodes, since we will be removing nodes
		List<INode<T>> nodes = new ArrayList<>(getNodes());
		for (INode<T> node : nodes) {
			boolean isInvolvedInFailedTrace = false;
			for (ITrace<T> failedTrace : failedTraces) {
				if (failedTrace.isInvolved(node)) {
					isInvolvedInFailedTrace = true;
					break;
				}
			}
			if (isInvolvedInFailedTrace) {
				removeNode(node.getIdentifier());
			}
		}
		return this;
    }
    
    /**
     * Removes all nodes from this spectra that were not
     * executed by any successful trace.
     * @return
     * this spectra (modified)
     */
    default public ISpectra<T> removePurelyFailingNodes() {
    	Collection<ITrace<T>> successfulTraces = getSuccessfulTraces();
		//get a copy of the current set of nodes, since we will be removing nodes
		List<INode<T>> nodes = new ArrayList<>(getNodes());
		for (INode<T> node : nodes) {
			boolean isInvolvedInSuccessfulTrace = false;
			for (ITrace<T> successfulTrace : successfulTraces) {
				if (successfulTrace.isInvolved(node)) {
					isInvolvedInSuccessfulTrace = true;
					break;
				}
			}
			if (!isInvolvedInSuccessfulTrace) {
				removeNode(node.getIdentifier());
			}
		}
		return this;
    }
    
    /**
     * Removes all nodes from this spectra that were
     * executed by at least one successful trace.
     * @return
     * this spectra (modified)
     */
    default public ISpectra<T> removeSuccessfulNodes() {
    	Collection<ITrace<T>> successfulTraces = getSuccessfulTraces();
		//get a copy of the current set of nodes, since we will be removing nodes
		List<INode<T>> nodes = new ArrayList<>(getNodes());
		for (INode<T> node : nodes) {
			boolean isInvolvedInSuccessfulTrace = false;
			for (ITrace<T> successfulTrace : successfulTraces) {
				if (successfulTrace.isInvolved(node)) {
					isInvolvedInSuccessfulTrace = true;
					break;
				}
			}
			if (isInvolvedInSuccessfulTrace) {
				removeNode(node.getIdentifier());
			}
		}
		return this;
    }

    /**
     * Inverts involvements of nodes for successful and/or 
     * failing traces to the respective opposite. 
     * Returns a new Spectra object that has the required properties.
     * This spectra is left unmodified.
     * @param invertSuccessfulTraces
     * whether to invert involvements of nodes in successful traces
     * @param invertFailedTraces
     * whether to invert involvements of nodes in failed traces
     * @return
     * a new spectra with inverted involvements
     */
    default public ISpectra<T> createInvertedSpectra(boolean invertSuccessfulTraces, boolean invertFailedTraces) {
    	Spectra<T> spectra = new Spectra<>();

    	//populate new spectra with nodes from input spectra
    	for (INode<T> node : getNodes()) {
    		spectra.getOrCreateNode(node.getIdentifier());
    	}

    	for (ITrace<T> inputTrace : getTraces()) {
    		boolean successful = inputTrace.isSuccessful();
    		IMutableTrace<T> addedTrace = spectra.addTrace(inputTrace.getIdentifier(), successful);
    		for (INode<T> node : spectra.getNodes()) {
    			boolean isInvolved = inputTrace.isInvolved(node.getIdentifier());
    			if (successful) {
    				if (invertSuccessfulTraces) {
    					addedTrace.setInvolvement(node, !isInvolved);
    				} else {
    					addedTrace.setInvolvement(node, isInvolved);
    				}
    			} else {
    				if (invertFailedTraces) {
    					addedTrace.setInvolvement(node, !isInvolved);
    				} else {
    					addedTrace.setInvolvement(node, isInvolved);
    				}
    			}
    		}
    	}

    	return spectra;
	}

}