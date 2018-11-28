/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.core.manipulation;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.hit.HitSpectra;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.BufferedArrayQueue;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Reads a Spectra object and combines sequences of nodes to larger blocks based
 * on whether they are within the same method.
 * 
 * @author Simon Heiden
 */
public class StatementToMethodSpectraModule extends AbstractProcessor<ISpectra<SourceCodeBlock,?>, ISpectra<SourceCodeBlock,?>> {

	public StatementToMethodSpectraModule() {
		super();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public ISpectra<SourceCodeBlock,?> processItem(final ISpectra<SourceCodeBlock,?> input) {
		
		//get lines in the spectra and sort them
		Collection<INode<SourceCodeBlock>> nodes = input.getNodes();
		SourceCodeBlock[] nodeArray = new SourceCodeBlock[nodes.size()];
		int counter = -1;
		for (INode<SourceCodeBlock> node : nodes) {
			nodeArray[++counter] = node.getIdentifier();
		}
		Arrays.sort(nodeArray);
		
		ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> methodSpectra = new HitSpectra<>(null);
		Map<Integer, Integer> lineToMethodMap = new HashMap<>();
		
		
		SourceCodeBlock currentMethodLine = new SourceCodeBlock("", "", "", -1);
		int currentMethodNodeIndex = -1;
		//iterate over all lines
		for (SourceCodeBlock line : nodeArray) {
			INode<SourceCodeBlock> node = input.getOrCreateNode(line);
			//see if we are inside the same method in the same package
			if (!line.getMethodName().equals(currentMethodLine.getMethodName())
					|| !line.getPackageName().equals(currentMethodLine.getPackageName())) {
				//if we change into another method or package, add a new node to the new spectra
				currentMethodLine = line.clone();
				INode<SourceCodeBlock> methodNode = methodSpectra.getOrCreateNode(currentMethodLine);
				currentMethodNodeIndex = methodNode.getIndex();
			}
			
			lineToMethodMap.put(node.getIndex(), currentMethodNodeIndex);
			currentMethodLine.setLineNumberEnd(line.getEndLineNumber());
		}
		
		Collection<? extends ITrace<SourceCodeBlock>> traces = input.getTraces();
		int traceCounter = 0;
		// iterate over all traces
		for (ITrace<SourceCodeBlock> trace : traces) {
			ITrace<?> methodSpectraTrace = methodSpectra.addTrace(
					trace.getIdentifier(), ++traceCounter, trace.isSuccessful());
			// set the involvement, if at least one node of the method was executed
			for (int nodeIndex : trace.getInvolvedNodes()) {
				methodSpectraTrace.setInvolvement(lineToMethodMap.get(nodeIndex), true);
			}
			
			// iterate over all execution traces
			for (ExecutionTrace executiontrace : trace.getExecutionTraces()) {
				BufferedArrayQueue<Integer> methodExecutionTrace = 
						new BufferedArrayQueue<>(executiontrace.getCompressedTrace().getOutputDir(), UUID.randomUUID().toString(), 50000);
				int lastNodeIndex = -1;
				for (Iterator<Integer> iterator = executiontrace.mappedIterator(input.getIndexer()); iterator.hasNext();) {
					int nodeIndex = lineToMethodMap.get(iterator.next());
					// add index to execution trace without repetitions
					if (nodeIndex != lastNodeIndex) {
						methodExecutionTrace.add(nodeIndex);
						lastNodeIndex = nodeIndex;
					}
				}
				// add method level execution trace
				methodSpectraTrace.addExecutionTrace(
						new ExecutionTrace(methodExecutionTrace, false));
			}
		}
		
		return methodSpectra;
	}

}
