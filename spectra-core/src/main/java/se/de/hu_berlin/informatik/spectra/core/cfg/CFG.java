package se.de.hu_berlin.informatik.spectra.core.cfg;

import java.io.File;
import java.util.Collection;
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

	public Collection<T> getIdentifiersForNode(int index);
	
	default public String getIdentifierString(int index) {
		Collection<T> identifiers = this.getIdentifiersForNode(index);
		StringBuilder sb = new StringBuilder();
		sb.append("node ").append(index).append(" =====>").append(System.lineSeparator());
		for (T t : identifiers) {
			sb.append(t).append(System.lineSeparator());
		}
		sb.append("<=====");
		return sb.toString();
	}
}
