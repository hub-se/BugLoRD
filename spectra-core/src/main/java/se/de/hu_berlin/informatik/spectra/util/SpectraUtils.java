package se.de.hu_berlin.informatik.spectra.util;

import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.Node.NodeType;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.branch.ProgramBranch;
import se.de.hu_berlin.informatik.spectra.core.branch.ProgramBranch.BranchIterator;
import se.de.hu_berlin.informatik.spectra.core.branch.ProgramBranchSpectra;
import se.de.hu_berlin.informatik.spectra.core.count.CountSpectra;
import se.de.hu_berlin.informatik.spectra.core.count.CountTrace;
import se.de.hu_berlin.informatik.spectra.core.hit.HitSpectra;
import se.de.hu_berlin.informatik.spectra.core.hit.HitTrace;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

import java.util.*;

/**
 * Helper class that handles spectra objects.
 *
 * @author Simon
 */
public class SpectraUtils {

    //suppress default constructor (class should not be instantiated)
    private SpectraUtils() {
        throw new AssertionError();
    }

    /**
     * Removes all nodes from this spectra that are of the specified type (at this moment).
     *
     * @param spectra      the spectra
     * @param coverageType the type of the nodes to remove
     * @param <T>          the type of node identifiers
     */
    public static <T> void removeNodesWithCoverageType(ISpectra<T, ?> spectra, INode.CoverageType coverageType) {
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
     *
     * @param spectra the spectra
     * @param <T>     the type of node identifiers
     */
    private static <T> void removeCoveredNodes(ISpectra<T, ?> spectra) {
        Collection<? extends ITrace<T>> traces = spectra.getTraces();
        removeNodesInvolvedInATrace(spectra, traces);
    }

    /**
     * Removes all nodes from the given spectra that were not
     * executed by any trace. (EP + EF == 0)
     *
     * @param spectra the spectra
     * @param <T>     the type of node identifiers
     */
    private static <T> void removeUncoveredNodes(ISpectra<T, ?> spectra) {
        Collection<? extends ITrace<T>> traces = spectra.getTraces();
        removeNodesNotInvolvedInAllTraces(spectra, traces);
    }

    /**
     * Removes all nodes from the given spectra that were not
     * executed by any failing trace. (EF == 0)
     *
     * @param spectra the spectra
     * @param <T>     the type of node identifiers
     */
    private static <T> void removePurelySuccessfulNodes(ISpectra<T, ?> spectra) {
        Collection<? extends ITrace<T>> failedTraces = spectra.getFailingTraces();
        removeNodesNotInvolvedInAllTraces(spectra, failedTraces);
    }

    /**
     * Removes all nodes from the given spectra that were
     * executed by at least one failing trace. (EF &gt; 0)
     *
     * @param spectra the spectra
     * @param <T>     the type of node identifiers
     */
    private static <T> void removeFailingNodes(ISpectra<T, ?> spectra) {
        Collection<? extends ITrace<T>> failedTraces = spectra.getFailingTraces();
        removeNodesInvolvedInATrace(spectra, failedTraces);
    }

    /**
     * Removes all nodes from the given spectra that were not
     * executed by any successful trace. (EP == 0)
     *
     * @param spectra the spectra
     * @param <T>     the type of node identifiers
     */
    private static <T> void removePurelyFailingNodes(ISpectra<T, ?> spectra) {
        Collection<? extends ITrace<T>> successfulTraces = spectra.getSuccessfulTraces();
        removeNodesNotInvolvedInAllTraces(spectra, successfulTraces);
    }

    /**
     * Removes all nodes from the given spectra that were
     * executed by at least one successful trace. (EP &gt; 0)
     *
     * @param spectra the spectra
     * @param <T>     the type of node identifiers
     */
    private static <T> void removeSuccessfulNodes(ISpectra<T, ?> spectra) {
        Collection<? extends ITrace<T>> successfulTraces = spectra.getSuccessfulTraces();
        removeNodesInvolvedInATrace(spectra, successfulTraces);
    }

    /**
     * Removes all nodes from the given spectra that were
     * executed by all failing traces. (NF == 0 &lt;=&gt; EF == F)
     *
     * @param spectra the spectra
     * @param <T>     the type of node identifiers
     */
    private static <T> void removeAllFailingNodes(ISpectra<T, ?> spectra) {
        Collection<? extends ITrace<T>> failedTraces = spectra.getFailingTraces();
        removeNodesInvolvedInAllTraces(spectra, failedTraces);
    }

    /**
     * Removes all nodes from the given spectra that were not
     * executed by at least one failing trace. (NF &gt; 0 &lt;=&gt; EF &lt; F)
     *
     * @param spectra the spectra
     * @param <T>     the type of node identifiers
     */
    private static <T> void removeNotAllFailingNodes(ISpectra<T, ?> spectra) {
        Collection<? extends ITrace<T>> failedTraces = spectra.getFailingTraces();
        removeNodesNotInvolvedInATrace(spectra, failedTraces);
    }

    /**
     * Removes all nodes from the given spectra that were
     * executed by all successful traces.  (NP == 0 &lt;=&gt; EP == P)
     *
     * @param spectra the spectra
     * @param <T>     the type of node identifiers
     */
    private static <T> void removeAllSuccessfulNodes(ISpectra<T, ?> spectra) {
        Collection<? extends ITrace<T>> successfulTraces = spectra.getSuccessfulTraces();
        removeNodesInvolvedInAllTraces(spectra, successfulTraces);
    }

    /**
     * Removes all nodes from the given spectra that were not
     * executed by at least one successful trace. (NP &gt; 0 &lt;=&gt; EP &lt; P)
     *
     * @param spectra the spectra
     * @param <T>     the type of node identifiers
     */
    private static <T> void removeNotAllSuccessfulNodes(ISpectra<T, ?> spectra) {
        Collection<? extends ITrace<T>> successfulTraces = spectra.getSuccessfulTraces();
        removeNodesNotInvolvedInATrace(spectra, successfulTraces);
    }


    private static <T> void removeNodesInvolvedInATrace(ISpectra<T, ?> spectra, Collection<? extends ITrace<T>> traces) {
        Collection<Integer> nodesToRemove = new HashSet<>();
        for (INode<T> node : spectra.getNodes()) {
            boolean isInvolvedInTrace = isNodeInvolvedInATrace(traces, node);
            if (isInvolvedInTrace) {
                nodesToRemove.add(node.getIndex());
            }
        }
        spectra.removeNodesByIndex(nodesToRemove);
    }

    private static <T> void removeNodesInvolvedInAllTraces(ISpectra<T, ?> spectra, Collection<? extends ITrace<T>> traces) {
        Collection<Integer> nodesToRemove = new HashSet<>();
        for (INode<T> node : spectra.getNodes()) {
            boolean isNotInvolvedInTrace = isNodeNotInvolvedInATrace(traces, node);
            if (!isNotInvolvedInTrace) {
                nodesToRemove.add(node.getIndex());
            }
        }
        spectra.removeNodesByIndex(nodesToRemove);
    }

    private static <T> void removeNodesNotInvolvedInATrace(ISpectra<T, ?> spectra, Collection<? extends ITrace<T>> traces) {
        Collection<Integer> nodesToRemove = new HashSet<>();
        for (INode<T> node : spectra.getNodes()) {
            boolean isNotInvolvedInTrace = isNodeNotInvolvedInATrace(traces, node);
            if (isNotInvolvedInTrace) {
                nodesToRemove.add(node.getIndex());
            }
        }
        spectra.removeNodesByIndex(nodesToRemove);
    }

    private static <T> void removeNodesNotInvolvedInAllTraces(ISpectra<T, ?> spectra, Collection<? extends ITrace<T>> traces) {
        Collection<Integer> nodesToRemove = new HashSet<>();
        for (INode<T> node : spectra.getNodes()) {
            boolean isInvolvedInTrace = isNodeInvolvedInATrace(traces, node);
            if (!isInvolvedInTrace) {
                nodesToRemove.add(node.getIndex());
            }
        }
        spectra.removeNodesByIndex(nodesToRemove);
    }


    private static <T> boolean isNodeInvolvedInATrace(Collection<? extends ITrace<T>> traces, INode<T> node) {
        for (ITrace<T> trace : traces) {
            if (trace.isInvolved(node)) {
                trace.sleep();
                return true;
            }
            trace.sleep();
        }
        return false;
    }

    private static <T> boolean isNodeNotInvolvedInATrace(Collection<? extends ITrace<T>> traces, INode<T> node) {
        for (ITrace<T> trace : traces) {
            if (!trace.isInvolved(node)) {
                trace.sleep();
                return true;
            }
            trace.sleep();
        }
        return false;
    }


    /**
     * Inverts involvements of nodes for successful and/or
     * failing traces to the respective opposite.
     * Returns a new Spectra object that has the required properties.
     * The given spectra is left unmodified. Node identifiers are shared
     * between the two spectra objects, though.
     *
     * @param iSpectra               the spectra for which to invert the traces
     * @param invertSuccessfulTraces whether to invert involvements of nodes in successful traces
     * @param invertFailedTraces     whether to invert involvements of nodes in failed traces
     * @param <T>                    the type of node identifiers
     * @return a new spectra with inverted involvements
     */
    public static <T> HitSpectra<T> createInvertedSpectrum(
            ISpectra<T, ? super HitTrace<T>> iSpectra, boolean invertSuccessfulTraces, boolean invertFailedTraces) {
        HitSpectra<T> spectra = new HitSpectra<>(iSpectra.getPathToSpectraZipFile());

        //populate new spectra with nodes from input spectra
        for (INode<T> node : iSpectra.getNodes()) {
            spectra.getOrCreateNode(node.getIdentifier());
        }

        //iterate over all traces of the spectra to invert
        for (ITrace<T> inputTrace : iSpectra.getTraces()) {
            //check whether the trace is successful
            boolean successful = inputTrace.isSuccessful();
            //create a new trace in the new spectra
            ITrace<T> addedTrace = spectra.addTrace(inputTrace.getIdentifier(), inputTrace.getIndex(), successful);
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
            inputTrace.sleep();
            addedTrace.sleep();
        }

        return spectra;
    }

    /**
     * Creates a method level spectra based on the given input spectra on statement level.
     *
     * @param iSpectra the input spectra
     * @return a new spectra with method level program elements
     */
    public static HitSpectra<SourceCodeBlock> createMethodLevelSpectrum(ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> iSpectra) {
        HitSpectra<SourceCodeBlock> spectra = new HitSpectra<>(iSpectra.getPathToSpectraZipFile());
        Map<Integer, SourceCodeBlock> nodesToMethodMap = new HashMap<>();
        Map<SourceCodeBlock, List<Integer>> methodToNodesMap = new HashMap<>();

        //populate new spectra with nodes from input spectra
        for (INode<SourceCodeBlock> node : iSpectra.getNodes()) {
            spectra.getOrCreateNode(getMethodNode(nodesToMethodMap, methodToNodesMap, node));
        }

        //iterate over all traces of the spectra to invert
        for (ITrace<SourceCodeBlock> inputTrace : iSpectra.getTraces()) {
            //check whether the trace is successful
            boolean successful = inputTrace.isSuccessful();
            //create a new trace in the new spectra
            ITrace<SourceCodeBlock> addedTrace = spectra.addTrace(inputTrace.getIdentifier(), inputTrace.getIndex(), successful);
            //iterate over all (method level) nodes
            for (INode<SourceCodeBlock> methodNode : spectra.getNodes()) {
                //check for the involvement of the node in the input spectra
                List<Integer> nodes = methodToNodesMap.get(methodNode.getIdentifier());
                for (Integer index : nodes) {
                    // if any of the statement level nodes is involved, then the entire method is involved
                    if (inputTrace.isInvolved(index)) {
                        addedTrace.setInvolvement(methodNode, true);
                        break;
                    }
                }
            }
            inputTrace.sleep();
            addedTrace.sleep();
        }

        return spectra;
    }

    private static SourceCodeBlock getMethodNode(Map<Integer, SourceCodeBlock> nodesToMethodMap,
                                                 Map<SourceCodeBlock, List<Integer>> methodToNodesMap, INode<SourceCodeBlock> node) {
        SourceCodeBlock methodBlock = nodesToMethodMap.get(node.getIndex());
        if (methodBlock == null) {
            List<INode<SourceCodeBlock>> nodes = new ArrayList<>();
            SourceCodeBlock identifier = node.getIdentifier();
            for (INode<SourceCodeBlock> iNode : node.getSpectra().getNodes()) {
                if (identifier.getMethodName().equals(iNode.getIdentifier().getMethodName()) &&
                        identifier.getFilePath().equals(iNode.getIdentifier().getFilePath())) {
                    nodes.add(iNode);
                }
            }
            Collections.sort(nodes, new Comparator<INode<SourceCodeBlock>>() {
                @Override
                public int compare(INode<SourceCodeBlock> o1, INode<SourceCodeBlock> o2) {
                    return Integer.compare(o1.getIdentifier().getStartLineNumber(), o2.getIdentifier().getStartLineNumber());
                }
            });

            methodBlock = new SourceCodeBlock(node.getIdentifier().getPackageName(),
                    node.getIdentifier().getFilePath(),
                    node.getIdentifier().getMethodName(),
                    nodes.get(0).getIdentifier().getStartLineNumber(),
                    nodes.get(nodes.size() - 1).getIdentifier().getEndLineNumber(),
                    NodeType.NORMAL);

            for (INode<SourceCodeBlock> nodeInMethod : nodes) {
                nodesToMethodMap.put(nodeInMethod.getIndex(), methodBlock);
            }

            List<Integer> indexList = new ArrayList<>(nodes.size());
            for (INode<SourceCodeBlock> nodeInList : nodes) {
                indexList.add(nodeInList.getIndex());
            }
            methodToNodesMap.put(methodBlock, indexList);
        }
        return methodBlock;
    }

    /**
     * Merges the given spectra into one single spectra, based on majority decisions.
     *
     * @param <T>            the type of nodes in the spectra
     * @param spectra        the list of spectra to merge
     * @param preferSuccess  whether to declare a trace successful if only one original trace is successful (opposed to majority voting)
     * @param preferInvolved whether to declare a node involved in a trace if only one original node is involved (opposed to majority voting)
     * @return the merged spectra
     */
    public static <T> ISpectra<T, ? extends CountTrace<T>> mergeCountSpectra(List<ISpectra<T, ?>> spectra, boolean preferSuccess, boolean preferInvolved) {
        ISpectra<T, CountTrace<T>> result = new CountSpectra<>(null);
        if (spectra.isEmpty()) {
            Log.warn(SpectraUtils.class, "No spectra given.");
            return result;
        }

        // collect all trace identifiers
        Set<String> allTraceIdentifiers = new HashSet<>();
        for (ISpectra<T, ?> spectrum : spectra) {
            for (ITrace<T> trace : spectrum.getTraces()) {
                allTraceIdentifiers.add(trace.getIdentifier());
                trace.sleep();
            }
        }

        // collect all node identifiers
        Set<T> allNodeIdentifiers = new HashSet<>();
        for (ISpectra<T, ?> spectrum : spectra) {
            for (INode<T> node : spectrum.getNodes()) {
                allNodeIdentifiers.add(node.getIdentifier());
            }
        }

        // iterate over all nodes and add them to the result spectra
        for (T nodeIdentifier : allNodeIdentifiers) {
            result.getOrCreateNode(nodeIdentifier);
        }

        int traceCounter = 0;
        // iterate over all traces
        for (String traceIdentifier : allTraceIdentifiers) {
            int foundTraceCounter = 0;
            int successfulCounter = 0;
            List<ITrace<T>> foundtraces = new ArrayList<>(spectra.size());
            for (ISpectra<T, ?> spectrum : spectra) {
                ITrace<T> foundTrace = spectrum.getTrace(traceIdentifier);
                if (foundTrace == null) {
                    Log.warn(SpectraUtils.class, "Trace '%s' not found in spectra.", traceIdentifier);
                    continue;
                }
                ++foundTraceCounter;
                foundtraces.add(foundTrace);
                if (foundTrace.isSuccessful()) {
                    ++successfulCounter;
                }
                foundTrace.sleep();
            }
            boolean majSuccessful = false;
            if ((successfulCounter > foundTraceCounter / 2) || (preferSuccess && successfulCounter > 0)) {
                majSuccessful = true;
            }
            CountTrace<T> resultTrace = result.addTrace(traceIdentifier, ++traceCounter, majSuccessful);

            // iterate over all node identifiers and set the involvement in the trace
            for (T nodeIdentifier : allNodeIdentifiers) {
                int involvedCounter = 0;
                int countTraces = 0;
                long hits = 0;
                for (ITrace<T> foundTrace : foundtraces) {
                    if (foundTrace instanceof CountTrace) {
                        ++countTraces;
                        hits += ((CountTrace<T>) foundTrace).getHits(nodeIdentifier);
                    }
                    if (foundTrace.isInvolved(nodeIdentifier)) {
                        ++involvedCounter;
                    }
                    foundTrace.sleep();
                }
                if ((involvedCounter > foundTraceCounter / 2) || (preferInvolved && involvedCounter > 0)) {
                    setInvolvement(resultTrace, nodeIdentifier, countTraces, hits);
                }
            }
        }

        return result;
    }

    private static <T> void setInvolvement(CountTrace<T> resultTrace, T nodeIdentifier, int countTraces, long hits) {
        resultTrace.setInvolvement(nodeIdentifier, true);
        if (countTraces > 0) {
            resultTrace.setHits(nodeIdentifier, Math.round(hits / (double) countTraces));
        }
        resultTrace.sleep();
    }

    /**
     * Merges the given spectra into one single spectra, based on majority decisions.
     *
     * @param <T>            the type of nodes in the spectra
     * @param spectra        the list of spectra to merge
     * @param preferSuccess  whether to declare a trace successful if only one original trace is successful (opposed to majority voting)
     * @param preferInvolved whether to declare a node involved in a trace if only one original node is involved (opposed to majority voting)
     * @return the merged spectra
     */
    public static <T> ISpectra<T, ?> mergeSpectra(List<ISpectra<T, ?>> spectra, boolean preferSuccess, boolean preferInvolved) {
        ISpectra<T, ?> result = new HitSpectra<>(null);
        if (spectra.isEmpty()) {
            Log.warn(SpectraUtils.class, "No spectra given.");
            return result;
        } else if (spectra.size() == 1) {
            return spectra.get(0);
        }

        // collect all trace identifiers
        Set<String> allTraceIdentifiers = new HashSet<>();
        for (ISpectra<T, ?> spectrum : spectra) {
            for (ITrace<T> trace : spectrum.getTraces()) {
                allTraceIdentifiers.add(trace.getIdentifier());
                trace.sleep();
            }
        }

        // collect all node identifiers
        Set<T> allNodeIdentifiers = new HashSet<>();
        for (ISpectra<T, ?> spectrum : spectra) {
            for (INode<T> node : spectrum.getNodes()) {
                allNodeIdentifiers.add(node.getIdentifier());
            }
        }

        // iterate over all nodes and add them to the result spectra
        for (T nodeIdentifier : allNodeIdentifiers) {
            result.getOrCreateNode(nodeIdentifier);
        }

        int traceCounter = 0;
        // iterate over all traces
        for (String traceIdentifier : allTraceIdentifiers) {
            int foundTraceCounter = 0;
            int successfulCounter = 0;
            List<ITrace<T>> foundtraces = new ArrayList<>(spectra.size());
            for (ISpectra<T, ?> spectrum : spectra) {
                ITrace<T> foundTrace = spectrum.getTrace(traceIdentifier);
                if (foundTrace == null) {
                    Log.warn(SpectraUtils.class, "Trace '%s' not found in spectra.", traceIdentifier);
                    continue;
                }
                ++foundTraceCounter;
                foundtraces.add(foundTrace);
                if (foundTrace.isSuccessful()) {
                    ++successfulCounter;
                }
                foundTrace.sleep();
            }
            boolean majSuccessful = false;
            if ((successfulCounter > foundTraceCounter / 2) || (preferSuccess && successfulCounter > 0)) {
                majSuccessful = true;
            }
            ITrace<T> resultTrace = result.addTrace(traceIdentifier, ++traceCounter, majSuccessful);

            // iterate over all node identifiers and set the involvement in the trace
            for (T nodeIdentifier : allNodeIdentifiers) {
                int involvedCounter = 0;
                for (ITrace<T> foundTrace : foundtraces) {
                    if (foundTrace.isInvolved(nodeIdentifier)) {
                        ++involvedCounter;
                    }
                    foundTrace.sleep();
                }
                if ((involvedCounter > foundTraceCounter / 2) || (preferInvolved && involvedCounter > 0)) {
                    resultTrace.setInvolvement(nodeIdentifier, true);
                }
            }
            resultTrace.sleep();
        }

        return result;
    }

	public static <T> void removeTestClassNodes(T dummy, ISpectra<T, ?> spectra) {
		Collection<String> testClassNames = new HashSet<>();
		Collection<? extends ITrace<?>> traces = (Collection<? extends ITrace<?>>) spectra.getTraces();
		for (ITrace<?> trace : traces) {
			String testClassName = getTestClassName(trace.getIdentifier());
			if (testClassName != null) {
				testClassNames.add(testClassName);
			}
		}
		
		if (dummy instanceof SourceCodeBlock) {
			Collection<Integer> nodesToRemove = new HashSet<>();
			for (INode<T> node : spectra.getNodes()) {
				// is the node part of a test class?
				if (testClassNames.contains(getClassName((SourceCodeBlock) node.getIdentifier()))) {
					nodesToRemove.add(node.getIndex());
				}
			}
			spectra.removeNodesByIndex(nodesToRemove);
		} else if (dummy instanceof ProgramBranch) {
			Collection<Integer> nodesToRemove = new HashSet<>();
			for (INode<T> node : spectra.getNodes()) {
				// iterate over the branch
				ProgramBranch branch = (ProgramBranch) node.getIdentifier();
				for (BranchIterator iterator = branch.branchIterator(); iterator.hasNext();) {
					SourceCodeBlock sourceCodeBlock = iterator.next();
					// is the statement part of a test class?
					if (testClassNames.contains(getClassName(sourceCodeBlock))) {
						nodesToRemove.add(iterator.getCurrentStatementId());
					}
				}
			}
			@SuppressWarnings("unchecked")
			ProgramBranchSpectra<ProgramBranch> programBranchSpectra = (ProgramBranchSpectra<ProgramBranch>) spectra;
			
			programBranchSpectra.removeNodesFromNodeIdSequencesByIndex(nodesToRemove);
		} else {
			throw new UnsupportedOperationException("Spectra node type not supported.");
		}
	}
	
	private static String getClassName(SourceCodeBlock node) {
		// file name is something like 'class/Name.java' TODO: lower case correct/necessary?
		return node.getFilePath().replace(".java", "").replace('/', '.');
	}

	private static String getTestClassName(String testIdentifier) {
		// test name should be 'class.name::testName' TODO: lower case correct/necessary?
		int index = testIdentifier.indexOf(':');
		if (index < 0) {
			return null;
		} else {
			return testIdentifier.substring(0, index);
		}
	}
}


