package se.de.hu_berlin.informatik.stardust.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import se.de.hu_berlin.informatik.stardust.spectra.IMutableTrace;
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.ITrace;
import se.de.hu_berlin.informatik.stardust.spectra.Spectra;

/**
 * Helper class that handles spectra objects.
 * 
 * @author Simon
 *
 */
public class SpectraUtils {
	
	//suppress default constructor (class should not be instantiated)
	private SpectraUtils() {
		throw new AssertionError();
	}
	
	/**
     * Removes all nodes from the given spectra that were not
     * executed by any failing trace.
     * @param spectra
     * the spectra
     * @param <T>
     * the type of node identifiers
     */
    public static <T> void removePurelySuccessfulNodes(ISpectra<T> spectra) {
    	Collection<ITrace<T>> failedTraces = spectra.getFailingTraces();
		//get a copy of the current set of nodes, since we will be removing nodes
		List<INode<T>> nodes = new ArrayList<>(spectra.getNodes());
		for (INode<T> node : nodes) {
			boolean isInvolvedInFailedTrace = false;
			for (ITrace<T> failedTrace : failedTraces) {
				if (failedTrace.isInvolved(node)) {
					isInvolvedInFailedTrace = true;
					break;
				}
			}
			if (!isInvolvedInFailedTrace) {
				spectra.removeNode(node.getIdentifier());
			}
		}
    }
    
    /**
     * Removes all nodes from the given spectra that were
     * executed by at least one failing trace.
     * @param spectra
     * the spectra
     * @param <T>
     * the type of node identifiers
     */
    public static <T> void removeFailingNodes(ISpectra<T> spectra) {
    	Collection<ITrace<T>> failedTraces = spectra.getFailingTraces();
		//get a copy of the current set of nodes, since we will be removing nodes
		List<INode<T>> nodes = new ArrayList<>(spectra.getNodes());
		for (INode<T> node : nodes) {
			boolean isInvolvedInFailedTrace = false;
			for (ITrace<T> failedTrace : failedTraces) {
				if (failedTrace.isInvolved(node)) {
					isInvolvedInFailedTrace = true;
					break;
				}
			}
			if (isInvolvedInFailedTrace) {
				spectra.removeNode(node.getIdentifier());
			}
		}
    }
    
    /**
     * Removes all nodes from the given spectra that were not
     * executed by any successful trace.
     * @param spectra
     * the spectra
     * @param <T>
     * the type of node identifiers
     */
    public static <T> void removePurelyFailingNodes(ISpectra<T> spectra) {
    	Collection<ITrace<T>> successfulTraces = spectra.getSuccessfulTraces();
		//get a copy of the current set of nodes, since we will be removing nodes
		List<INode<T>> nodes = new ArrayList<>(spectra.getNodes());
		for (INode<T> node : nodes) {
			boolean isInvolvedInSuccessfulTrace = false;
			for (ITrace<T> successfulTrace : successfulTraces) {
				if (successfulTrace.isInvolved(node)) {
					isInvolvedInSuccessfulTrace = true;
					break;
				}
			}
			if (!isInvolvedInSuccessfulTrace) {
				spectra.removeNode(node.getIdentifier());
			}
		}
    }
    
    /**
     * Removes all nodes from the given spectra that were
     * executed by at least one successful trace.
     * @param spectra
     * the spectra
     * @param <T>
     * the type of node identifiers
     */
    public static <T> void removeSuccessfulNodes(ISpectra<T> spectra) {
    	Collection<ITrace<T>> successfulTraces = spectra.getSuccessfulTraces();
		//get a copy of the current set of nodes, since we will be removing nodes
		List<INode<T>> nodes = new ArrayList<>(spectra.getNodes());
		for (INode<T> node : nodes) {
			boolean isInvolvedInSuccessfulTrace = false;
			for (ITrace<T> successfulTrace : successfulTraces) {
				if (successfulTrace.isInvolved(node)) {
					isInvolvedInSuccessfulTrace = true;
					break;
				}
			}
			if (isInvolvedInSuccessfulTrace) {
				spectra.removeNode(node.getIdentifier());
			}
		}
    }

	/**
     * Inverts involvements of nodes for successful and/or 
     * failing traces to the respective opposite. 
     * Returns a new Spectra object that has the required properties.
     * The given spectra is left unmodified. Node identifiers are shared
     * between the two spectra objects, though.
     * @param toInvert
     * the spectra for which to invert the traces
     * @param invertSuccessfulTraces
     * whether to invert involvements of nodes in successful traces
     * @param invertFailedTraces
     * whether to invert involvements of nodes in failed traces
     * @return
     * a new spectra with inverted involvements
     * @param <T>
     * the type of node identifiers
     */
    public static <T> ISpectra<T> createInvertedSpectra(ISpectra<T> toInvert, boolean invertSuccessfulTraces, boolean invertFailedTraces) {
    	Spectra<T> spectra = new Spectra<>();

    	//populate new spectra with nodes from input spectra
    	for (INode<T> node : toInvert.getNodes()) {
    		spectra.getOrCreateNode(node.getIdentifier());
    	}

    	//iterate over all traces of the spectra to invert
    	for (ITrace<T> inputTrace : toInvert.getTraces()) {
    		//check whether the trace is successful
    		boolean successful = inputTrace.isSuccessful();
    		//create a new trace in the new spectra
    		IMutableTrace<T> addedTrace = spectra.addTrace(inputTrace.getIdentifier(), successful);
    		//iterate over all nodes
    		for (INode<T> node : spectra.getNodes()) {
    			//check for the involvement of the node in the input spectra
    			boolean nodeIsInvolved = inputTrace.isInvolved(node.getIdentifier());
    			//invert involvement based on given parameters
    			if (successful) {
    				if (invertSuccessfulTraces) {
    					addedTrace.setInvolvement(node, !nodeIsInvolved);
    				} else {
    					addedTrace.setInvolvement(node, nodeIsInvolved);
    				}
    			} else {
    				if (invertFailedTraces) {
    					addedTrace.setInvolvement(node, !nodeIsInvolved);
    				} else {
    					addedTrace.setInvolvement(node, nodeIsInvolved);
    				}
    			}
    		}
    	}

    	return spectra;
	}
	
}


