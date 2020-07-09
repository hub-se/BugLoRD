package se.de.hu_berlin.informatik.faultlocalizer.cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.spectra.core.cfg.Node;
import se.de.hu_berlin.informatik.spectra.core.cfg.ScoredDynamicCFG;

public class PageRank<T> {
	
	private static final int CONVERGENCE_MULTIPLICATOR = 1000000;

	private final ScoredDynamicCFG<T> cfg;
	private final double dampingFactor;
	private final double offset;

	private Map<Integer, Double> pageRank = new HashMap<>();
	private Map<Integer, Double> old_pageRank;

	private int iterations;

	private boolean reverse;

	public PageRank(ScoredDynamicCFG<T> cfg, double dampingFactor, int iterations, boolean reverse) {

		this.cfg = cfg;
		this.dampingFactor = dampingFactor;
		this.iterations = iterations;
		this.reverse = reverse;

		// initialize the values
		this.offset = (1 - dampingFactor) / cfg.getNodes().size();
		
		for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
			Node node = entry.getValue();
			Double score = cfg.getScore(node.getIndex());
			this.pageRank.put(node.getIndex(), score);
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
					for (Node successor : node.getSuccessors()) {
						sum += pageRank.get(successor.getIndex()) / successor.getPredecessorCount();
					}
				}
			} else {
				if (node.hasPredecessors()) {
					for (Node predecessor : node.getPredecessors()) {
						sum += pageRank.get(predecessor.getIndex()) / predecessor.getSuccessorCount();
					}
				}
			}
			newPageRankArray.put(node.getIndex(), offset + dampingFactor * sum);
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