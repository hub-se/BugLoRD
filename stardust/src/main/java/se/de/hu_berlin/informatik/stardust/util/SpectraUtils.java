package se.de.hu_berlin.informatik.stardust.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import se.de.hu_berlin.informatik.stardust.spectra.IMutableTrace;
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.ITrace;
import se.de.hu_berlin.informatik.stardust.spectra.Spectra;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

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
     * Removes all nodes from this spectra that are of the specified type (at this moment).
     * @param spectra
     * the spectra
     * @param coverageType
     * the type of the nodes to remove
     * @param <T>
     * the type of node identifiers
     */
	public static <T> void removeNodesWithCoverageType(ISpectra<T> spectra, INode.CoverageType coverageType) {
		switch (coverageType) {
		case EXECUTED:
			removeCoveredNodes(spectra);
			break;
		case NOT_EXECUTED:
			removeUncoveredNodes(spectra);
			break;
		case EF_EQUALS_ZERO:
			removePurelySuccessfulNodes(spectra);
			break;
		case EF_GT_ZERO:
			removeFailingNodes(spectra);
			break;
		case EP_EQUALS_ZERO:
			removePurelyFailingNodes(spectra);
			break;
		case EP_GT_ZERO:
			removeSuccessfulNodes(spectra);
			break;
		case NF_EQUALS_ZERO:
			removeAllFailingNodes(spectra);
			break;
		case NF_GT_ZERO:
			removeNotAllFailingNodes(spectra);
			break;
		case NP_EQUALS_ZERO:
			removeAllSuccessfulNodes(spectra);
			break;
		case NP_GT_ZERO:
			removeNotAllSuccessfulNodes(spectra);
			break;
		default:
			throw new UnsupportedOperationException("Not implemented.");
		}
	}
	
	/**
     * Removes all nodes from the given spectra that were
     * executed by any trace. (EP + EF &gt; 0)
     * @param spectra
     * the spectra
     * @param <T>
     * the type of node identifiers
     */
    private static <T> void removeCoveredNodes(ISpectra<T> spectra) {
    	Collection<? extends ITrace<T>> traces = spectra.getTraces();
    	removeNodesInvolvedInATrace(spectra, traces);
    }
    
    /**
     * Removes all nodes from the given spectra that were not
     * executed by any trace. (EP + EF == 0)
     * @param spectra
     * the spectra
     * @param <T>
     * the type of node identifiers
     */
    private static <T> void removeUncoveredNodes(ISpectra<T> spectra) {
    	Collection<? extends ITrace<T>> traces = spectra.getTraces();
    	removeNodesNotInvolvedInAllTraces(spectra, traces);
    }
	
	/**
     * Removes all nodes from the given spectra that were not
     * executed by any failing trace. (EF == 0)
     * @param spectra
     * the spectra
     * @param <T>
     * the type of node identifiers
     */
    private static <T> void removePurelySuccessfulNodes(ISpectra<T> spectra) {
    	Collection<ITrace<T>> failedTraces = spectra.getFailingTraces();
    	removeNodesNotInvolvedInAllTraces(spectra, failedTraces);
    }
    
    /**
     * Removes all nodes from the given spectra that were
     * executed by at least one failing trace. (EF &gt; 0)
     * @param spectra
     * the spectra
     * @param <T>
     * the type of node identifiers
     */
    private static <T> void removeFailingNodes(ISpectra<T> spectra) {
    	Collection<ITrace<T>> failedTraces = spectra.getFailingTraces();
    	removeNodesInvolvedInATrace(spectra, failedTraces);
    }
    
    /**
     * Removes all nodes from the given spectra that were not
     * executed by any successful trace. (EP == 0)
     * @param spectra
     * the spectra
     * @param <T>
     * the type of node identifiers
     */
    private static <T> void removePurelyFailingNodes(ISpectra<T> spectra) {
    	Collection<ITrace<T>> successfulTraces = spectra.getSuccessfulTraces();
    	removeNodesNotInvolvedInAllTraces(spectra, successfulTraces);
    }
    
    /**
     * Removes all nodes from the given spectra that were
     * executed by at least one successful trace. (EP &gt; 0)
     * @param spectra
     * the spectra
     * @param <T>
     * the type of node identifiers
     */
    private static <T> void removeSuccessfulNodes(ISpectra<T> spectra) {
    	Collection<ITrace<T>> successfulTraces = spectra.getSuccessfulTraces();
		removeNodesInvolvedInATrace(spectra, successfulTraces);
    }
    
    /**
     * Removes all nodes from the given spectra that were 
     * executed by all failing traces. (NF == 0 &lt;=&gt; EF == F)
     * @param spectra
     * the spectra
     * @param <T>
     * the type of node identifiers
     */
    private static <T> void removeAllFailingNodes(ISpectra<T> spectra) {
    	Collection<ITrace<T>> failedTraces = spectra.getFailingTraces();
    	removeNodesInvolvedInAllTraces(spectra, failedTraces);
    }
    
    /**
     * Removes all nodes from the given spectra that were not
     * executed by at least one failing trace. (NF &gt; 0 &lt;=&gt; EF &lt; F)
     * @param spectra
     * the spectra
     * @param <T>
     * the type of node identifiers
     */
    private static <T> void removeNotAllFailingNodes(ISpectra<T> spectra) {
    	Collection<ITrace<T>> failedTraces = spectra.getFailingTraces();
		removeNodesNotInvolvedInATrace(spectra, failedTraces);
    }

    /**
     * Removes all nodes from the given spectra that were 
     * executed by all successful traces.  (NP == 0 &lt;=&gt; EP == P)
     * @param spectra
     * the spectra
     * @param <T>
     * the type of node identifiers
     */
    private static <T> void removeAllSuccessfulNodes(ISpectra<T> spectra) {
    	Collection<ITrace<T>> successfulTraces = spectra.getSuccessfulTraces();
		removeNodesInvolvedInAllTraces(spectra, successfulTraces);
    }
    
    /**
     * Removes all nodes from the given spectra that were not
     * executed by at least one successful trace. (NP &gt; 0 &lt;=&gt; EP &lt; P)
     * @param spectra
     * the spectra
     * @param <T>
     * the type of node identifiers
     */
    private static <T> void removeNotAllSuccessfulNodes(ISpectra<T> spectra) {
    	Collection<ITrace<T>> successfulTraces = spectra.getSuccessfulTraces();
    	removeNodesNotInvolvedInATrace(spectra, successfulTraces);
    }
    
    
	private static <T> void removeNodesInvolvedInATrace(ISpectra<T> spectra, Collection<? extends ITrace<T>> traces) {
		//get a copy of the current set of nodes, since we will be removing nodes
		List<INode<T>> nodes = new ArrayList<>(spectra.getNodes());
		for (INode<T> node : nodes) {
			boolean isInvolvedInTrace = isNodeInvolvedInATrace(traces, node);
			if (isInvolvedInTrace) {
				spectra.removeNode(node.getIdentifier());
			}
		}
	}
	
	private static <T> void removeNodesInvolvedInAllTraces(ISpectra<T> spectra, Collection<? extends ITrace<T>> traces) {
		//get a copy of the current set of nodes, since we will be removing nodes
		List<INode<T>> nodes = new ArrayList<>(spectra.getNodes());
		for (INode<T> node : nodes) {
			boolean isNotInvolvedInTrace = isNodeNotInvolvedInATrace(traces, node);
			if (!isNotInvolvedInTrace) {
				spectra.removeNode(node.getIdentifier());
			}
		}
	}
    
    private static <T> void removeNodesNotInvolvedInATrace(ISpectra<T> spectra, Collection<? extends ITrace<T>> traces) {
		//get a copy of the current set of nodes, since we will be removing nodes
		List<INode<T>> nodes = new ArrayList<>(spectra.getNodes());
		for (INode<T> node : nodes) {
			boolean isNotInvolvedInTrace = isNodeNotInvolvedInATrace(traces, node);
			if (isNotInvolvedInTrace) {
				spectra.removeNode(node.getIdentifier());
			}
		}
	}
    
    private static <T> void removeNodesNotInvolvedInAllTraces(ISpectra<T> spectra, Collection<? extends ITrace<T>> traces) {
		//get a copy of the current set of nodes, since we will be removing nodes
		List<INode<T>> nodes = new ArrayList<>(spectra.getNodes());
		for (INode<T> node : nodes) {
			boolean isInvolvedInTrace = isNodeInvolvedInATrace(traces, node);
			if (!isInvolvedInTrace) {
				spectra.removeNode(node.getIdentifier());
			}
		}
	}
    

    private static <T> boolean isNodeInvolvedInATrace(Collection<? extends ITrace<T>> traces, INode<T> node) {
		boolean isInvolved = false;
		for (ITrace<T> trace : traces) {
			if (trace.isInvolved(node)) {
				isInvolved = true;
				break;
			}
		}
		return isInvolved;
	}
	
	private static <T> boolean isNodeNotInvolvedInATrace(Collection<? extends ITrace<T>> traces, INode<T> node) {
		boolean isNotInvolved = false;
		for (ITrace<T> trace : traces) {
			if (!trace.isInvolved(node)) {
				isNotInvolved = true;
				break;
			}
		}
		return isNotInvolved;
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
    
    /**
     * Merges the given spectras into one single spectra, based on majority decisions.
     * @param <T> the type of nodes in the spectra
     * @param spectras
     * the list of spectras to merge
     * @param preferSuccess
     * whether to declare a trace successful if only one original trace is successful (opposed to majority voting) 
     * @param preferInvolved
     * whether to declare a node involved in a trace if only one original node is involved (opposed to majority voting)
     * @return
     * the merged spectra
     */
    public static <T> ISpectra<T> mergeSpectras(List<ISpectra<T>> spectras, boolean preferSuccess, boolean preferInvolved) {
		ISpectra<T> result = new Spectra<>();
		if (spectras.isEmpty()) {
			Log.warn(SpectraUtils.class, "Spectra is emppty.");
			return result;
		} else if (spectras.size() == 1) {
			return spectras.get(0);
		}
		
		// collect all trace identifiers
		Set<String> allTraceIdentifiers = new HashSet<>();
		for (ISpectra<T> spectra : spectras) {
			for (ITrace<T> trace : spectra.getTraces()) {
				allTraceIdentifiers.add(trace.getIdentifier());
			}
		}

		// collect all node identifiers
		Set<T> allNodeIdentifiers = new HashSet<>();
		for (ISpectra<T> spectra : spectras) {
			for (INode<T> node : spectra.getNodes()) {
				allNodeIdentifiers.add(node.getIdentifier());
			}
		}
		
		// iterate over all nodes and add them to the result spectra
		for (T nodeIdentifier : allNodeIdentifiers) {
			result.getOrCreateNode(nodeIdentifier);
		}
		
		// iterate over all traces
		for (String traceIdentifier : allTraceIdentifiers) {
			int foundTraceCounter = 0;
			int successfulCounter = 0;
			List<ITrace<T>> foundtraces = new ArrayList<>(spectras.size());
			for (ISpectra<T> spectra : spectras) {
				ITrace<T> foundTrace = spectra.getTrace(traceIdentifier);
				if (foundTrace == null) {
					Log.warn(SpectraUtils.class, "Trace '%s' not found in spectra.", traceIdentifier);
					continue;
				}
				++foundTraceCounter;
				foundtraces.add(foundTrace);
				if (foundTrace.isSuccessful()) {
					++successfulCounter;
				}
			}
			boolean majSuccessful = false;
			if ((successfulCounter > foundTraceCounter / 2) || (preferSuccess && successfulCounter > 0)) {
				majSuccessful = true;
			}
			IMutableTrace<T> resultTrace = result.addTrace(traceIdentifier, majSuccessful);
			
			// iterate over all node identifiers and set the involvement in the trace
			for (T nodeIdentifier : allNodeIdentifiers) {
				int involvedCounter = 0;
				for (ITrace<T> foundTrace : foundtraces) {
					if (foundTrace.isInvolved(nodeIdentifier)) {
						++involvedCounter;
					}
				}
				if ((involvedCounter > foundTraceCounter / 2) || (preferInvolved && involvedCounter > 0)) {
					resultTrace.setInvolvement(nodeIdentifier, true);
				}
			}
		}
		
		return result;
	}
	
}


