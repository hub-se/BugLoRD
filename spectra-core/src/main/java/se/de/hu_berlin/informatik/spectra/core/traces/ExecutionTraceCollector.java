package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExecutionTraceCollector {
	
	private Map<String,int[]> rawTracePool = new HashMap<>();
	
	private GeneralizedSuffixTree gsTree = new GeneralizedSuffixTree();
	private Map<String,ExecutionTrace> executionTracePool = new HashMap<>();
	
	public boolean addRawTraceToPool(String testID, List<Integer> trace) {
		if (rawTracePool.get(testID) != null) {
			return false;
		}
		// collect raw trace
		int[] traceArray = trace.stream().mapToInt(i->i).toArray();
		rawTracePool.put(testID, traceArray);
		// new input may or may not invalidate previously generated execution traces
		// (generally, the execution traces should only be generated at the end of trace collection)
		executionTracePool.clear();
		// we need to extract repetitions in the trace and add them to the GS tree
		extractRepetitions(traceArray);
		return true;
	}
	
	private void extractRepetitions(int[] traceArray) {
		// TODO extract repetitions in the trace and add them to the GS tree
		
	}

	public ExecutionTrace getExecutionTrace(String testID) {
		if (executionTracePool.get(testID) == null) {
			generateExecutiontraceFromPool(testID);
		}
		// may still be null
		return executionTracePool.get(testID);
	}

	private void generateExecutiontraceFromPool(String testID) {
		// TODO only generate each trace if necessary
		
	}

}
