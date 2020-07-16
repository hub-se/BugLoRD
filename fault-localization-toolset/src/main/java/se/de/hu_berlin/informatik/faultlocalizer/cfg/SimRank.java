package se.de.hu_berlin.informatik.faultlocalizer.cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.spectra.core.cfg.CFG;
import se.de.hu_berlin.informatik.spectra.core.cfg.Node;
import se.de.hu_berlin.informatik.spectra.core.cfg.ScoredDynamicCFG;

public class SimRank<T> {
	
	private static final int CONVERGENCE_MULTIPLICATOR = 1000000;
	
	/**
	 * Calculates the SimRank scores for all nodes in the given CFG. This will base scores on *ingoing* edges.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with score arrays for each CFG node index 
	 * @see <a href="https://en.wikipedia.org/wiki/SimRank">https://en.wikipedia.org/wiki/SimRank</a>
	 */
	public static Map<Integer, double[]> calculateSimRank(CFG<?> cfg, double dampingFactor, int iterations) {
		return calculate(cfg, dampingFactor, iterations, false);
	}
	
	/**
	 * Calculates the (inverted) SimRank scores for all nodes in the given CFG. This will base scores on *outgoing* edges.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with score arrays for each CFG node index 
	 * @see <a href="https://en.wikipedia.org/wiki/SimRank">https://en.wikipedia.org/wiki/SimRank</a>
	 */
	public static Map<Integer, double[]> calculateInvertedSimRank(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations) {
		return calculate(cfg, dampingFactor, iterations, true);
	}
	
	/**
	 * Calculates the SimRank and inverted SimRank scores for all nodes in the given CFG and returns scores based on the 
	 * vector lengths, treating both scores as vector components.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with score arrays for each CFG node index 
	 */
	public static Map<Integer, double[]> calculateSimVectorRank(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations) {
		Map<Integer, double[]> pr = calculateSimRank(cfg, dampingFactor, iterations);
		Map<Integer, double[]> cr = calculateInvertedSimRank(cfg, dampingFactor, iterations);
		
		// max vector length is sqrt(2)
		final double normFactor = Math.sqrt(2);
				
		Map<Integer, double[]> pcr = new HashMap<>();
		for (Entry<Integer, double[]> entry : pr.entrySet()) {
			double[] prScores = pr.get(entry.getKey());
			double[] crScores = cr.get(entry.getKey());
			double[] newScores = new double[prScores.length];
			for (int i = 0; i < prScores.length; i++) {
				// simple vector length calculation
				newScores[i] = Math.sqrt(prScores[i]*prScores[i] + crScores[i]*crScores[i]) / normFactor;
			}
			
			pcr.put(entry.getKey(), newScores);
		}
		
		return pcr;
	}
	
	private static Map<Integer, double[]> calculate(CFG<?> cfg, double dampingFactor, int iterations, boolean inverted) {
		// get highest existing node index to inititalize score arrays;
		// not all node indices may exist, but it's probably better than using thousands of HashMaps...
		int arrayLength = getHighestNodeIndex(cfg) + 1;
		// initialize the values
		double offset = (1 - dampingFactor) / cfg.getNodes().size();

		Map<Integer, double[]> simRank = new HashMap<>();
		for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
			Node node = entry.getValue();
			double[] scores = new double[arrayLength];
			scores[node.getIndex()] = 1;
			simRank.put(node.getIndex(), scores);
		}
		
		Map<Integer, double[]> old_simRank;

		// loop until values converge or max iterations reached
		int count = 0;
		do {
			old_simRank = simRank;
			//Calculate the SimRank
			simRank = calculateIteration(simRank, cfg, offset, dampingFactor, inverted);
			++count;
		} while (!didConverge(old_simRank, simRank) && (iterations <= 0 || count < iterations));

		return simRank;
	}
	
	private static Map<Integer, double[]> calculateIteration(Map<Integer, double[]> simRank, CFG<?> cfg, 
			double offset, double dampingFactor, boolean inverted) {
		
		Map<Integer, double[]> newSimRankArray = new HashMap<>();
		
		if (inverted) {
			for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
				Node nodeA = entry.getValue();
				double[] oldScores = simRank.get(nodeA.getIndex());
				double[] newScores = new double[oldScores.length];
				// get out-nodes of node A
				if (nodeA.hasSuccessors()) {
					int[] aSuccessors = nodeA.getSuccessors();
					// iterate over all nodes B
					for (Entry<Integer, Node> entry2 : cfg.getNodes().entrySet()) {
						// calculate s(A,B)
						Node nodeB = entry2.getValue();
						if (nodeA.getIndex() == nodeB.getIndex()) {
							// s(*,*) is always defined as 1
							newScores[nodeA.getIndex()] = 1;
						} else {
							// use SimRank equation to calculate s(A,B)
							// get out-nodes of node B
							if (nodeB.hasSuccessors()) {
								int[] bSuccessors = nodeB.getSuccessors();
								// calculate sums over all successor scores
								double sum = 0;
								for (int i = 0; i < aSuccessors.length; i++) {
									double[] scores = simRank.get(aSuccessors[i]);
									for (int j = 0; j < bSuccessors.length; j++) {
										// get scores for each s(O(A),O(B))
										sum += scores[bSuccessors[j]];
									}
								}
								// full score calculation
								newScores[nodeB.getIndex()] = dampingFactor / (aSuccessors.length * bSuccessors.length) * sum;
							}
						}
					}
				}
				newSimRankArray.put(nodeA.getIndex(), newScores);
			}
		} else {
			for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
				Node nodeA = entry.getValue();
				double[] oldScores = simRank.get(nodeA.getIndex());
				double[] newScores = new double[oldScores.length];
				// get in-nodes of node A
				if (nodeA.hasPredecessors()) {
					int[] aPredecessors = nodeA.getPredecessors();
					// iterate over all nodes B
					for (Entry<Integer, Node> entry2 : cfg.getNodes().entrySet()) {
						// calculate s(A,B)
						Node nodeB = entry2.getValue();
						if (nodeA.getIndex() == nodeB.getIndex()) {
							// s(*,*) is always defined as 1
							newScores[nodeA.getIndex()] = 1;
						} else {
							// use SimRank equation to calculate s(A,B)
							// get in-nodes of node B
							if (nodeB.hasPredecessors()) {
								int[] bPredecessors = nodeB.getPredecessors();
								// calculate sums over all predecessor scores
								double sum = 0;
								for (int i = 0; i < aPredecessors.length; i++) {
									double[] scores = simRank.get(aPredecessors[i]);
									for (int j = 0; j < bPredecessors.length; j++) {
										// get scores for each s(I(A),I(B))
										sum += scores[bPredecessors[j]];
									}
								}
								// full score calculation
								newScores[nodeB.getIndex()] = dampingFactor / (aPredecessors.length * bPredecessors.length) * sum;
							}
						}
					}
				}
				newSimRankArray.put(nodeA.getIndex(), newScores);
			}
		}
		
		return newSimRankArray;
	}
	
	private static int getHighestNodeIndex(CFG<?> cfg) {
		int max = -1;
		for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
			max = Math.max(max, entry.getValue().getIndex());
		}
		return max;
	}

	/**
	 * Check if the values of simRank converged
	 * @param simRank 
	 * @param old_simRank 
	 * @return True if the converge is successful
	 */
	private static boolean didConverge(Map<Integer, double[]> old_simRank, Map<Integer, double[]> simRank) {
		for (Entry<Integer, double[]> entry : simRank.entrySet()) {
			double[] newScores = entry.getValue();
			double[] oldScores = old_simRank.get(entry.getKey());
			for (int i = 0; i < newScores.length; i++) {
				if ((int) Math.floor(newScores[i] * CONVERGENCE_MULTIPLICATOR) != 
						(int) Math.floor(oldScores[i] * CONVERGENCE_MULTIPLICATOR)) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Calculates the SimRank scores for all nodes in the given CFG. This will base scores on *ingoing* edges.
	 * <br><br> Treats each execution as a separate link.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with score arrays for each CFG node index 
	 * @see <a href="https://en.wikipedia.org/wiki/SimRank">https://en.wikipedia.org/wiki/SimRank</a>
	 */
	public static Map<Integer, double[]> calculateHitAwareSimRank(CFG<?> cfg, double dampingFactor, int iterations) {
		return calculateHitAware(cfg, dampingFactor, iterations, false);
	}
	
	/**
	 * Calculates the (inverted) SimRank scores for all nodes in the given CFG. This will base scores on *outgoing* edges.
	 * <br><br> Treats each execution as a separate link.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with score arrays for each CFG node index 
	 * @see <a href="https://en.wikipedia.org/wiki/SimRank">https://en.wikipedia.org/wiki/SimRank</a>
	 */
	public static Map<Integer, double[]> calculateHitAwareInvertedSimRank(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations) {
		return calculateHitAware(cfg, dampingFactor, iterations, true);
	}
	
	/**
	 * Calculates the SimRank and inverted SimRank scores for all nodes in the given CFG and returns scores based on the 
	 * vector lengths, treating both scores as vector components.
	 * <br><br> Treats each execution as a separate link.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with score arrays for each CFG node index 
	 */
	public static Map<Integer, double[]> calculateHitAwareSimVectorRank(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations) {
		Map<Integer, double[]> pr = calculateHitAwareSimRank(cfg, dampingFactor, iterations);
		Map<Integer, double[]> cr = calculateHitAwareInvertedSimRank(cfg, dampingFactor, iterations);
		
		// max vector length is sqrt(2)
		final double normFactor = Math.sqrt(2);
				
		Map<Integer, double[]> pcr = new HashMap<>();
		for (Entry<Integer, double[]> entry : pr.entrySet()) {
			double[] prScores = pr.get(entry.getKey());
			double[] crScores = cr.get(entry.getKey());
			double[] newScores = new double[prScores.length];
			for (int i = 0; i < prScores.length; i++) {
				// simple vector length calculation
				newScores[i] = Math.sqrt(prScores[i]*prScores[i] + crScores[i]*crScores[i]) / normFactor;
			}
			
			pcr.put(entry.getKey(), newScores);
		}
		
		return pcr;
	}
	
	private static Map<Integer, double[]> calculateHitAware(CFG<?> cfg, double dampingFactor, int iterations, boolean inverted) {
		// get highest existing node index to inititalize score arrays;
		// not all node indices may exist, but it's probably better than using thousands of HashMaps...
		int arrayLength = getHighestNodeIndex(cfg) + 1;
		// initialize the values
		double offset = (1 - dampingFactor) / cfg.getNodes().size();

		Map<Integer, double[]> simRank = new HashMap<>();
		for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
			Node node = entry.getValue();
			double[] scores = new double[arrayLength];
			scores[node.getIndex()] = 1;
			simRank.put(node.getIndex(), scores);
		}
		
		Map<Integer, double[]> old_simRank;

		// loop until values converge or max iterations reached
		int count = 0;
		do {
			old_simRank = simRank;
			//Calculate the SimRank
			simRank = calculateHitAwareIteration(simRank, cfg, offset, dampingFactor, inverted);
			++count;
		} while (!didConverge(old_simRank, simRank) && (iterations <= 0 || count < iterations));

		return simRank;
	}
	
	private static Map<Integer, double[]> calculateHitAwareIteration(Map<Integer, double[]> simRank, CFG<?> cfg, 
			double offset, double dampingFactor, boolean inverted) {
		
		Map<Integer, double[]> newSimRankArray = new HashMap<>();
		
		if (inverted) {
			for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
				Node nodeA = entry.getValue();
				double[] oldScores = simRank.get(nodeA.getIndex());
				double[] newScores = new double[oldScores.length];
				// get out-nodes of node A
				if (nodeA.hasSuccessors()) {
					int[] aSuccessors = nodeA.getSuccessors();
					long[] aSuccessorHits = nodeA.getSuccessorHits();
					long aHitsSum = 0;
					for (long l : aSuccessorHits) {
						aHitsSum += l;
					}
					// iterate over all nodes B
					for (Entry<Integer, Node> entry2 : cfg.getNodes().entrySet()) {
						// calculate s(A,B)
						Node nodeB = entry2.getValue();
						if (nodeA.getIndex() == nodeB.getIndex()) {
							// s(*,*) is always defined as 1
							newScores[nodeA.getIndex()] = 1;
						} else {
							// use SimRank equation to calculate s(A,B)
							// get out-nodes of node B
							if (nodeB.hasSuccessors()) {
								int[] bSuccessors = nodeB.getSuccessors();
								long[] bSuccessorHits = nodeB.getSuccessorHits();
								long bHitsSum = 0;
								for (long l : bSuccessorHits) {
									bHitsSum += l;
								}
								// calculate sums over all successor scores
								double sum = 0;
								for (int i = 0; i < aSuccessors.length; i++) {
									double[] scores = simRank.get(aSuccessors[i]);
									for (int j = 0; j < bSuccessors.length; j++) {
										// get scores for each s(O(A),O(B))
										sum += aSuccessorHits[i] * bSuccessorHits[j] * scores[bSuccessors[j]];
									}
								}
								// full score calculation
								newScores[nodeB.getIndex()] = dampingFactor / (aHitsSum * bHitsSum) * sum;
							}
						}
					}
				}
				newSimRankArray.put(nodeA.getIndex(), newScores);
			}
		} else {
			for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
				Node nodeA = entry.getValue();
				double[] oldScores = simRank.get(nodeA.getIndex());
				double[] newScores = new double[oldScores.length];
				// get in-nodes of node A
				if (nodeA.hasPredecessors()) {
					int[] aPredecessors = nodeA.getPredecessors();
					long[] aPredecessorHits = nodeA.getPredecessorHits();
					long aHitsSum = 0;
					for (long l : aPredecessorHits) {
						aHitsSum += l;
					}
					// iterate over all nodes B
					for (Entry<Integer, Node> entry2 : cfg.getNodes().entrySet()) {
						// calculate s(A,B)
						Node nodeB = entry2.getValue();
						if (nodeA.getIndex() == nodeB.getIndex()) {
							// s(*,*) is always defined as 1
							newScores[nodeA.getIndex()] = 1;
						} else {
							// use SimRank equation to calculate s(A,B)
							// get in-nodes of node B
							if (nodeB.hasPredecessors()) {
								int[] bPredecessors = nodeB.getPredecessors();
								long[] bPredecessorHits = nodeB.getPredecessorHits();
								long bHitsSum = 0;
								for (long l : bPredecessorHits) {
									bHitsSum += l;
								}
								// calculate sums over all predecessor scores
								double sum = 0;
								for (int i = 0; i < aPredecessors.length; i++) {
									double[] scores = simRank.get(aPredecessors[i]);
									for (int j = 0; j < bPredecessors.length; j++) {
										// get scores for each s(I(A),I(B))
										sum += aPredecessorHits[i] * bPredecessorHits[j] * scores[bPredecessors[j]];
									}
								}
								// full score calculation
								newScores[nodeB.getIndex()] = dampingFactor / (aHitsSum * bHitsSum) * sum;
							}
						}
					}
				}
				newSimRankArray.put(nodeA.getIndex(), newScores);
			}
		}
		
		return newSimRankArray;
	}
	
}