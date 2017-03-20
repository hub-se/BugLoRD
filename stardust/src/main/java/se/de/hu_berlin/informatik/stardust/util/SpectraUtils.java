package se.de.hu_berlin.informatik.stardust.util;

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


