package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.*;
import se.de.hu_berlin.informatik.spectra.core.hit.HitSpectra;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedIntArrayQueue;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

import java.util.*;


public class BuildLinearExecutionBlockModule extends AbstractProcessor<ISpectra<SourceCodeBlock, ?>, ISpectra<SourceCodeBlock, ?>> {
    public BuildLinearExecutionBlockModule() {
        super();
    }

    @Override
    public ISpectra<SourceCodeBlock, ?> processItem(final ISpectra<SourceCodeBlock, ?> input) {

        LinearExecutionHitTrace hitTrace = new LinearExecutionHitTrace(input);


        //In order to integrate the linear execution block/trace in Buglord
        //we map the result to sourcecode block structure by removing all node of a LE-Block
        //except the first one.
        //this has no effect on the metric calculation, since all node within a block have the same properties like
        // involvement, EF, EP etc.
        //Only the node type data are not the same but we do not use it at all, so there is no problem.
        //TODO: how should blockmap (block -> sequence of nodes) and lineToMethodMap (original node id -> new node id) be stored?


        ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>> LEBSpectra = new HitSpectra<>(null);
        Map<Integer, Integer> lineToMethodMap = new HashMap<>();
        int currentBlockNodeIndex = -1;
        SourceCodeBlock currentBlockLine = new SourceCodeBlock("", "", "", -1, Node.NodeType.NORMAL);

        for (Integer nodeIndex : hitTrace.getBlockMap().keySet()) {

            SourceCodeBlock line = input.getNode(nodeIndex).getIdentifier();
//            System.out.println(line.getShortIdentifier());
//            System.out.print("copy node: "+nodeIndex);
            INode<SourceCodeBlock> node = input.getNode(line);
            currentBlockLine = line.clone();
            INode<SourceCodeBlock> methodNode = LEBSpectra.getOrCreateNode(currentBlockLine);
            currentBlockNodeIndex = methodNode.getIndex();
            lineToMethodMap.put(node.getIndex(), currentBlockNodeIndex);
            currentBlockLine.setLineNumberEnd(line.getEndLineNumber());
//            System.out.println(" new index: "+currentBlockNodeIndex
//                    +" start line: "+methodNode.getIdentifier().getStartLineNumber()
//                    +" end line: "+methodNode.getIdentifier().getEndLineNumber()
//                    +" node type: "+methodNode.getIdentifier().getNodeType()
//            );
        }

        Collection<? extends ITrace<SourceCodeBlock>> traces = input.getTraces();

        int traceCounter = 0;
        // iterate over all traces
        for (ITrace<SourceCodeBlock> trace : traces) {
            ITrace<?> LEBTestTrace = LEBSpectra.addTrace(
                    trace.getIdentifier(), ++traceCounter, trace.isSuccessful());
            // set the involvement, if at least one node of the method was executed
            LinearExecutionTestTrace testTrace = hitTrace.getTrace(traceCounter - 1);
            for (int nodeIndex : testTrace.getInvolvedBlocks()) {

                LEBTestTrace.setInvolvement(lineToMethodMap.get(nodeIndex), true);
            }

            // iterate over all execution traces
            int i = 0;
            for (ExecutionTrace executiontrace : trace.getExecutionTraces()) {
                BufferedIntArrayQueue LEBTrace =
                        new BufferedIntArrayQueue(executiontrace.getCompressedTrace().getOutputDir(),
                                "m_cpr_trace_" + UUID.randomUUID().toString(), 50000);
                LinearBlockSequence t = testTrace.getTrace(i++);
                for (Iterator<LinearExecutionBlock> iterator = t.getBlockSeq().iterator(); iterator.hasNext(); ) {
                    int nodeIndex = lineToMethodMap.get(iterator.next().getIndex());
                    LEBTrace.add(nodeIndex);
                }
                // add method level execution trace
                LEBTestTrace.addExecutionTrace(
                        new ExecutionTrace(LEBTrace, false));
            }
        }


        return LEBSpectra;
    }

}
