package se.de.hu_berlin.informatik.spectra.core.manipulation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import de.unisb.cs.st.sequitur.output.OutputSequence;
import de.unisb.cs.st.sequitur.output.SharedOutputGrammar;
import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.Node.NodeType;
import se.de.hu_berlin.informatik.spectra.core.hit.HitSpectra;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;
import se.de.hu_berlin.informatik.spectra.core.traces.SequenceIndexerCompressed;
import se.de.hu_berlin.informatik.spectra.core.traces.SimpleIntIndexerCompressed;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
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
		
		
		SourceCodeBlock currentMethodLine = new SourceCodeBlock("", "", "", -1, NodeType.NORMAL);
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
		
		if (currentMethodNodeIndex >= 0) {
			int[][] nodeIdSequences = new int[currentMethodNodeIndex+1][];
			for (int i = 0; i < nodeIdSequences.length; ++i) {
				// one to one mapping... (still ok for easy future removal of nodes from traces)
				nodeIdSequences[i] = new int[] {i};
			}
			
			SharedOutputGrammar<Integer> methodExecutionTraceGrammar = new SharedOutputGrammar<>();
			SequenceIndexerCompressed methodSpectraIndexer = null;
			try {
				methodSpectraIndexer = new SimpleIntIndexerCompressed(methodExecutionTraceGrammar, nodeIdSequences);
			} catch (ClassNotFoundException | IOException e1) {
				Log.abort(this, e1, "Cannot set up indexer.");
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

					OutputSequence<Integer> outSeq = new OutputSequence<>(methodExecutionTraceGrammar);
					int lastNodeIndex = -1;
					for (Iterator<Integer> iterator = executiontrace.mappedIterator(input.getIndexer()); iterator.hasNext();) {
						int nodeIndex = lineToMethodMap.get(iterator.next());
						// add index to execution trace without repetitions
						if (nodeIndex != lastNodeIndex) {
							outSeq.append(nodeIndex);
							lastNodeIndex = nodeIndex;
						}
					}

					try {
						byte[] byteArray = SpectraFileUtils.convertToByteArray(outSeq, false);

						ExecutionTrace methodExecutionTrace = 
								new ExecutionTrace(byteArray, methodSpectraIndexer);

						// add method level execution trace
						methodSpectraTrace.addExecutionTrace(methodExecutionTrace);
					} catch (IOException e) {
						Log.abort(this, e, "Could not add method level execution trace");
					}
					
				}
				trace.sleep();
			}

			// don't forget to set the indexer
			methodSpectra.setIndexer(methodSpectraIndexer);
		}
		
		return methodSpectra;
	}

}
