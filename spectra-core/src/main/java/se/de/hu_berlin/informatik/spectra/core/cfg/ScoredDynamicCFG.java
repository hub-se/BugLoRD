package se.de.hu_berlin.informatik.spectra.core.cfg;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;

public class ScoredDynamicCFG<T> implements CFG<T> {
	
	private Map<Integer, Double> scores = new HashMap<>();
	private CFG<T> cfg;

	public ScoredDynamicCFG(CFG<T> cfg) {
		this.cfg = cfg;
	}

	public void assignScore(int nodeIndex, double score) {
		if (containsNode(nodeIndex)) {
			scores.put(nodeIndex, score);
		}
	}
	
	public Double getScore(int nodeIndex) {
		return scores.get(nodeIndex);
	}
	
	@Override
	public Map<Integer, Node> getNodes() {
		return cfg.getNodes();
	}

	@Override
	public Node getOrCreateNode(int index) {
		return cfg.getOrCreateNode(index);
	}

	@Override
	public Node getNode(int index) {
		return cfg.getNode(index);
	}

	@Override
	public boolean containsNode(int index) {
		return cfg.containsNode(index);
	}

	@Override
	public void addExecutionTrace(ExecutionTrace executionTrace) {
		cfg.addExecutionTrace(executionTrace);
	}

	@Override
	public void generateCompleteCFG() {
		cfg.generateCompleteCFG();
	}

	@Override
	public void mergeLinearSequeces() {
		cfg.mergeLinearSequeces();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Entry<Integer, Node> entry : getNodes().entrySet()) {
			Node node = entry.getValue();
//			if (node.getIndex() != entry.getKey()) {
//				continue;
//			}
			
			sb.append(node.toString());
			Double score = getScore(node.getIndex());
			if (score != null) {
				sb.append("\tscore: ").append(score).append(System.lineSeparator());
			}
			sb.append("--------------").append(System.lineSeparator());
		}
		return sb.toString();
	}

	@Override
	public void save(File outputFile) {
		cfg.save(outputFile); // doesn't save scores
	}

	@Override
	public Collection<T> getIdentifiersForNode(int index) {
		return cfg.getIdentifiersForNode(index);
	}
	
}
