package se.de.hu_berlin.informatik.faultlocalizer.cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.spectra.core.cfg.Node;
import se.de.hu_berlin.informatik.spectra.core.cfg.ScoredDynamicCFG;

public class PageRank<T> {
	
	private static final int CONVERGENCE_MULTIPLICATOR = 1000000;
	
	/**
	 * Calculates the PageRank scores for all nodes in the given CFG. This will favor nodes with many *ingoing* edges.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with scores for each CFG node index 
	 * @see <a href="https://en.wikipedia.org/wiki/PageRank">https://en.wikipedia.org/wiki/PageRank</a>
	 */
	public static Map<Integer, Double> calculatePageRank(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations) {
		return calculate(cfg, dampingFactor, iterations, false);
	}
	
	/**
	 * Calculates the CheiRank scores for all nodes in the given CFG. This will favor nodes with many *outgoing* edges.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with scores for each CFG node index 
	 * @see <a href="https://en.wikipedia.org/wiki/CheiRank">https://en.wikipedia.org/wiki/CheiRank</a>
	 */
	public static Map<Integer, Double> calculateCheiRank(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations) {
		return calculate(cfg, dampingFactor, iterations, true);
	}
	
	/**
	 * Calculates the PageRank and CheiRank scores for all nodes in the given CFG and returns scores based on the 
	 * vector lengths, treating PageRank and CheiRank scores as vector components.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with scores for each CFG node index 
	 */
	public static Map<Integer, Double> calculatePageCheiVectorRank(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations) {
		Map<Integer, Double> pr = calculatePageRank(cfg, dampingFactor, iterations);
		Map<Integer, Double> cr = calculateCheiRank(cfg, dampingFactor, iterations);
		
		Map<Integer, Double> pcr = new HashMap<>();
		for (Entry<Integer, Double> entry : pr.entrySet()) {
			double crScore = cr.get(entry.getKey());
			// simple vector length calculation
			pcr.put(entry.getKey(), Math.sqrt(crScore*crScore + entry.getValue()*entry.getValue()));
		}
		
		return pcr;
	}
	
	private static Map<Integer, Double> calculate(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations, boolean inverted) {
		// initialize the values
		double offset = (1 - dampingFactor) / cfg.getNodes().size();

		Map<Integer, Double> pageRank = new HashMap<>();
		for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
			Node node = entry.getValue();
			double score = cfg.getScore(node.getIndex());
			pageRank.put(node.getIndex(), score);
		}
		
		Map<Integer, Double> old_pageRank;

		// loop until values converge or max iterations reached
		int count = 0;
		do {
			old_pageRank = pageRank;
			//Calculate the page rank
			pageRank = calculateIteration(pageRank, cfg, offset, dampingFactor, inverted);
			++count;
		} while (!didConverge(old_pageRank, pageRank) && (iterations <= 0 || count < iterations));

		return pageRank;
	}
	
	private static Map<Integer, Double> calculateIteration(Map<Integer, Double> pageRank, ScoredDynamicCFG<?> cfg, 
			double offset, double dampingFactor, boolean reverse) {
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
						sum += pageRank.get(predecessor) /  cfg.getNode(predecessor).getSuccessorCount();
					}
				}
			}
			newPageRankArray.put(node.getIndex(), offset + dampingFactor * sum);
		}
		
		return newPageRankArray;
	}
	
	/**
	 * Check if the values of page rank converged
	 * @param pageRank 
	 * @param old_pageRank 
	 * @return True if the converge is successful
	 */
	private static boolean didConverge(Map<Integer, Double> old_pageRank, Map<Integer, Double> pageRank) {
		for (Entry<Integer, Double> entry : pageRank.entrySet()) {
			if ((int) Math.floor(entry.getValue() * CONVERGENCE_MULTIPLICATOR) != 
					(int) Math.floor(old_pageRank.get(entry.getKey()) * CONVERGENCE_MULTIPLICATOR)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Calculates the PageRank scores for all nodes in the given CFG. This will favor nodes with many *ingoing* edges.
	 * <br><br> Treats each execution as a separate link.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with scores for each CFG node index 
	 * @see <a href="https://en.wikipedia.org/wiki/PageRank">https://en.wikipedia.org/wiki/PageRank</a>
	 */
	public static Map<Integer, Double> calculateHitAwarePageRank(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations) {
		return calculateHitAware(cfg, dampingFactor, iterations, false);
	}
	
	/**
	 * Calculates the CheiRank scores for all nodes in the given CFG. This will favor nodes with many *outgoing* edges.
	 * <br><br> Treats each execution as a separate link.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with scores for each CFG node index 
	 * @see <a href="https://en.wikipedia.org/wiki/CheiRank">https://en.wikipedia.org/wiki/CheiRank</a>
	 */
	public static Map<Integer, Double> calculateHitAwareCheiRank(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations) {
		return calculateHitAware(cfg, dampingFactor, iterations, true);
	}
	
	/**
	 * Calculates the PageRank and CheiRank scores for all nodes in the given CFG and returns scores based on the 
	 * vector lengths, treating PageRank and CheiRank scores as vector components.
	 * <br><br> Treats each execution as a separate link.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with scores for each CFG node index 
	 */
	public static Map<Integer, Double> calculateHitAwarePageCheiVectorRank(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations) {
		Map<Integer, Double> pr = calculateHitAwarePageRank(cfg, dampingFactor, iterations);
		Map<Integer, Double> cr = calculateHitAwareCheiRank(cfg, dampingFactor, iterations);
		
		Map<Integer, Double> pcr = new HashMap<>();
		for (Entry<Integer, Double> entry : pr.entrySet()) {
			double crScore = cr.get(entry.getKey());
			// simple vector length calculation
			pcr.put(entry.getKey(), Math.sqrt(crScore*crScore + entry.getValue()*entry.getValue()));
		}
		
		return pcr;
	}
	
	private static Map<Integer, Double> calculateHitAware(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations, boolean inverted) {
		// initialize the values
		double offset = (1 - dampingFactor) / cfg.getNodes().size();

		Map<Integer, Double> pageRank = new HashMap<>();
		for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
			Node node = entry.getValue();
			double score = cfg.getScore(node.getIndex());
			pageRank.put(node.getIndex(), score);
		}
		
		Map<Integer, Double> old_pageRank;

		// loop until values converge or max iterations reached
		int count = 0;
		do {
			old_pageRank = pageRank;
			//Calculate the page rank
			pageRank = calculateHitAwareIteration(pageRank, cfg, offset, dampingFactor, inverted);
			++count;
		} while (!didConverge(old_pageRank, pageRank) && (iterations <= 0 || count < iterations));

		return pageRank;
	}
	
	private static Map<Integer, Double> calculateHitAwareIteration(Map<Integer, Double> pageRank, ScoredDynamicCFG<?> cfg, 
			double offset, double dampingFactor, boolean reverse) {
		Map<Integer, Double> newPageRankArray = new HashMap<>();
		
		for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
			Node node = entry.getValue();
			double sum = 0;
			if (reverse) {
				if (node.hasSuccessors()) {
					int[] successors = node.getSuccessors();
					for (int i = 0; i < successors.length; i++) {
						Node succNode = cfg.getNode(successors[i]);
						int[] predecessors = succNode.getPredecessors();
						long[] predecessorHits = succNode.getPredecessorHits();
						long hits = 0;
						long totalHits = 0;
						for (int j = 0; j < predecessors.length; j++) {
							if (predecessors[j] == node.getIndex()) {
								// how many times did the successor get hit after the current node
								hits = predecessorHits[j];
							}
							// sum up the total number of times the successor got hit
							totalHits += predecessorHits[j];
						}
						// use ratio of outgoing hits to the successor from the current node
						// to all executions of the successor
						sum += pageRank.get(successors[i]) * hits / totalHits;
					}
				}
			} else {
				if (node.hasPredecessors()) {
					int[] predecessors = node.getPredecessors();
					for (int i = 0; i < predecessors.length; i++) {
						Node prevNode = cfg.getNode(predecessors[i]);
						int[] successors = prevNode.getSuccessors();
						long[] successorHits = prevNode.getSuccessorHits();
						long hits = 0;
						long totalHits = 0;
						for (int j = 0; j < successors.length; j++) {
							if (successors[j] == node.getIndex()) {
								// how many times did the current node get hit after the predecessor
								hits = successorHits[j];
							}
							// sum up the total number of times the predecessor got hit
							totalHits += successorHits[j];
						}
						// use ratio of ingoing hits from the predecessor to the current node
						// to all executions of the predecessor
						sum += pageRank.get(predecessors[i]) * hits / totalHits;
					}
				}
			}
			newPageRankArray.put(node.getIndex(), offset + dampingFactor * sum);
		}
		
		return newPageRankArray;
	}
	
}