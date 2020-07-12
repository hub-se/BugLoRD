package se.de.hu_berlin.informatik.spectra.core.cfg;

import java.io.File;
import java.util.Map;

import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;

public interface CFG<T> {
	
	public Map<Integer, Node> getNodes();
	
	public Node getOrCreateNode(int index);

	public Node getNode(int index);
	
	public boolean containsNode(int index);

	public void addExecutionTrace(ExecutionTrace executionTrace);

	public void generateCompleteCFG();
	
	public void mergeLinearSequeces();
	
	public void save(File outputFile);
	
}
