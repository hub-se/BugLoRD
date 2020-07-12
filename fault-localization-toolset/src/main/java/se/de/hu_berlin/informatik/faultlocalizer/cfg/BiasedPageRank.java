package se.de.hu_berlin.informatik.faultlocalizer.cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.spectra.core.cfg.Node;
import se.de.hu_berlin.informatik.spectra.core.cfg.ScoredDynamicCFG;

public class BiasedPageRank<T> {
	
	private static final int CONVERGENCE_MULTIPLICATOR = 1000000;

	private final ScoredDynamicCFG<T> cfg;
	private final double dampingFactor;
	private final double offset;

	final private Map<Integer, Double> originalPageRank = new HashMap<>();
	private Map<Integer, Double> pageRank = new HashMap<>();
	private Map<Integer, Double> old_pageRank;

	private int iterations;

	private boolean reverse;

	public BiasedPageRank(ScoredDynamicCFG<T> cfg, double dampingFactor, int iterations, boolean reverse) {

		this.cfg = cfg;
		this.dampingFactor = dampingFactor;
		this.iterations = iterations;
		this.reverse = reverse;

		// initialize the values
		int nodeCount = cfg.getNodes().size();
		this.offset = (1 - dampingFactor) / nodeCount;
		
		for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
			Node node = entry.getValue();
			Double score = cfg.getScore(node.getIndex());
			this.pageRank.put(node.getIndex(), score);
			this.originalPageRank.put(node.getIndex(), score / nodeCount);
		}

	}
	
	public Map<Integer, Double> calculate() {
		// loop until values converge or max iterations reached
		int count = 0;
		do {
			//Calculate the page rank
			calculatePageRank();
			++count;
		} while (!didConverge() && (iterations <= 0 || count < iterations));

		return pageRank;
	}
	
	/**
	 * Calculate the page rank
	 */
	private void calculatePageRank() {
		Map<Integer, Double> newPageRankArray = new HashMap<>();
		
		for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
			Node node = entry.getValue();
			double sum = 0;
			if (reverse) {
				if (node.hasSuccessors()) {
					for (int successor : node.getSuccessors()) {
						sum += pageRank.get(successor) / cfg.getNode(successor).getPredecessorCount();
					}
				}
			} else {
				if (node.hasPredecessors()) {
					for (int predecessor : node.getPredecessors()) {
						sum += pageRank.get(predecessor) / cfg.getNode(predecessor).getSuccessorCount();
					}
				}
			}
			newPageRankArray.put(node.getIndex(), originalPageRank.get(node.getIndex()) + offset + dampingFactor * sum);
		}
		
		old_pageRank = pageRank;
		pageRank = newPageRankArray;
	}
	
	/**
	 * Check if the values of page rank converged
	 * @return True if the converge is successful
	 */
	public boolean didConverge() {
		for (Entry<Integer, Double> entry : pageRank.entrySet()) {
			if ((int) Math.floor(entry.getValue() * CONVERGENCE_MULTIPLICATOR) != 
					(int) Math.floor(old_pageRank.get(entry.getKey()) * CONVERGENCE_MULTIPLICATOR)) {
				return false;
			}
		}
		return true;
	}
	
}