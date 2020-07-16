package se.de.hu_berlin.informatik.faultlocalizer.cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import se.de.hu_berlin.informatik.spectra.core.cfg.CFG;
import se.de.hu_berlin.informatik.spectra.core.cfg.Node;
import se.de.hu_berlin.informatik.spectra.core.cfg.ScoredDynamicCFG;

public class ASCOS<T> {
	
	private static final int CONVERGENCE_MULTIPLICATOR = 1000000;
	
	/**
	 * Calculates the ASCOS scores for all nodes in the given CFG. This will base scores on *ingoing* edges.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with score arrays for each CFG node index 
	 * @see <a href="https://dl.acm.org/doi/10.1145/2776894">https://dl.acm.org/doi/10.1145/2776894</a>
	 */
	public static Map<Integer, double[]> calculateASCOS(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations) {
		return calculate(cfg, dampingFactor, iterations, false, (i, j) -> {
			// weight of 1 will basically use the basic ASCOS algorithm (not exactly)
			return 1.0;
		});
	}
	
	/**
	 * Calculates the (inverted) ASCOS scores for all nodes in the given CFG. This will base scores on *outgoing* edges.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with score arrays for each CFG node index 
	 * @see <a href="https://dl.acm.org/doi/10.1145/2776894">https://dl.acm.org/doi/10.1145/2776894</a>
	 */
	public static Map<Integer, double[]> calculateInvertedASCOS(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations) {
		return calculate(cfg, dampingFactor, iterations, true, (i, j) -> {
			// weight of 1 will basically use the basic ASCOS algorithm (not exactly)
			return 1.0;
		});
	}
	
	/**
	 * Calculates the ASCOS scores for all nodes in the given CFG. This will base scores on *ingoing* edges.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with score arrays for each CFG node index 
	 * @see <a href="https://dl.acm.org/doi/10.1145/2776894">https://dl.acm.org/doi/10.1145/2776894</a>
	 */
	public static Map<Integer, double[]> calculateASCOSPlusPlus(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations) {
		return calculate(cfg, dampingFactor, iterations, false, (i, j) -> {
			// use average score of both connected nodes as weight
			return (cfg.getScore(i) + cfg.getScore(j)) / 2.0;
		});
	}
	
	/**
	 * Calculates the (inverted) ASCOS scores for all nodes in the given CFG. This will base scores on *outgoing* edges.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with score arrays for each CFG node index 
	 * @see <a href="https://dl.acm.org/doi/10.1145/2776894">https://dl.acm.org/doi/10.1145/2776894</a>
	 */
	public static Map<Integer, double[]> calculateInvertedASCOSPlusPlus(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations) {
		return calculate(cfg, dampingFactor, iterations, true, (i, j) -> {
			// use average score of both connected nodes as weight
			return (cfg.getScore(i) + cfg.getScore(j)) / 2.0;
		});
	}
	
	/**
	 * Calculates the ASCOS and inverted ASCOS scores for all nodes in the given CFG and returns scores based on the 
	 * vector lengths, treating both scores as vector components.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with score arrays for each CFG node index 
	 */
	public static Map<Integer, double[]> calculateSimVectorRank(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations) {
		Map<Integer, double[]> pr = calculateASCOS(cfg, dampingFactor, iterations);
		Map<Integer, double[]> cr = calculateInvertedASCOS(cfg, dampingFactor, iterations);
		
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
	
	/**
	 * Calculates the ASCOS and inverted ASCOS scores for all nodes in the given CFG and returns scores based on the 
	 * vector lengths, treating both scores as vector components.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with score arrays for each CFG node index 
	 */
	public static Map<Integer, double[]> calculateSimVectorRankPlusPlus(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations) {
		Map<Integer, double[]> pr = calculateASCOSPlusPlus(cfg, dampingFactor, iterations);
		Map<Integer, double[]> cr = calculateInvertedASCOSPlusPlus(cfg, dampingFactor, iterations);
		
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
	
	private static Map<Integer, double[]> calculate(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations, 
			boolean inverted, BiFunction<Integer, Integer, Double> weightFunction) {
		// get highest existing node index to inititalize score arrays;
		// not all node indices may exist, but it's probably better than using thousands of HashMaps...
		int arrayLength = getHighestNodeIndex(cfg) + 1;
		// initialize the values
		double offset = (1 - dampingFactor) / cfg.getNodes().size();

		Map<Integer, double[]> ASCOS = new HashMap<>();
		for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
			Node node = entry.getValue();
			double[] scores = new double[arrayLength];
			scores[node.getIndex()] = 1;
			ASCOS.put(node.getIndex(), scores);
		}
		
		Map<Integer, double[]> old_ASCOS;

		// loop until values converge or max iterations reached
		int count = 0;
		do {
			old_ASCOS = ASCOS;
			//Calculate the ASCOS
			ASCOS = calculateIteration(ASCOS, cfg, offset, dampingFactor, inverted, weightFunction);
			++count;
		} while (!didConverge(old_ASCOS, ASCOS) && (iterations <= 0 || count < iterations));

		System.out.println("iterations: " + count);
		return ASCOS;
	}
	
	private static Map<Integer, double[]> calculateIteration(Map<Integer, double[]> ASCOS, ScoredDynamicCFG<?> cfg, 
			double offset, double dampingFactor, boolean inverted, BiFunction<Integer, Integer, Double> weightFunction) {
		
		Map<Integer, double[]> newASCOSArray = new HashMap<>();
		
		if (inverted) {
			for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
				Node nodeA = entry.getValue();
				double[] oldScores = ASCOS.get(nodeA.getIndex());
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
							// use ASCOS equation to calculate s(A,B)
							// calculate sums over all successor scores
							double sum = 0;
							double weightSum = 0;
							for (int i = 0; i < aSuccessors.length; i++) {
								double weight = weightFunction.apply(nodeA.getIndex(), aSuccessors[i]);
								sum += weight * Math.abs(1 - Math.exp(weight)) * ASCOS.get(aSuccessors[i])[nodeB.getIndex()];
								weightSum += weight;
							}
							// full score calculation
							newScores[nodeB.getIndex()] = dampingFactor / weightSum * sum;
						}
					}
				}
				newASCOSArray.put(nodeA.getIndex(), newScores);
			}
		} else {
			for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
				Node nodeA = entry.getValue();
				double[] oldScores = ASCOS.get(nodeA.getIndex());
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
							// use ASCOS equation to calculate s(A,B)
							// calculate sums over all predecessor scores
							double sum = 0;
							double weightSum = 0;
							for (int i = 0; i < aPredecessors.length; i++) {
								double weight = weightFunction.apply(nodeA.getIndex(), aPredecessors[i]);
								sum += weight * Math.abs(1 - Math.exp(weight)) * ASCOS.get(aPredecessors[i])[nodeB.getIndex()];
								weightSum += weight;
							}
							// full score calculation
							newScores[nodeB.getIndex()] = dampingFactor / weightSum * sum;
						}
					}
				}
				newASCOSArray.put(nodeA.getIndex(), newScores);
			}
		}
		
		return newASCOSArray;
	}
	
	private static int getHighestNodeIndex(CFG<?> cfg) {
		int max = -1;
		for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
			max = Math.max(max, entry.getValue().getIndex());
		}
		return max;
	}

	/**
	 * Check if the values of ASCOS converged
	 * @param ASCOS 
	 * @param old_ASCOS 
	 * @return True if the converge is successful
	 */
	private static boolean didConverge(Map<Integer, double[]> old_ASCOS, Map<Integer, double[]> ASCOS) {
		for (Entry<Integer, double[]> entry : ASCOS.entrySet()) {
			double[] newScores = entry.getValue();
			double[] oldScores = old_ASCOS.get(entry.getKey());
			for (int i = 0; i < newScores.length; i++) {
				if ((int) Math.floor(newScores[i] * CONVERGENCE_MULTIPLICATOR) != 
						(int) Math.floor(oldScores[i] * CONVERGENCE_MULTIPLICATOR)) {
//					System.out.println(i + " -> " + newScores[i] + ", " + oldScores[i]);
					return false;
				}
			}
		}
//		System.out.println();
		return true;
	}
	
	/**
	 * Calculates the ASCOS scores for all nodes in the given CFG. This will base scores on *ingoing* edges.
	 * <br><br> Treats each execution as a separate link.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with score arrays for each CFG node index 
	 * @see <a href="https://dl.acm.org/doi/10.1145/2776894">https://dl.acm.org/doi/10.1145/2776894</a>
	 */
	public static Map<Integer, double[]> calculateHitAwareASCOS(CFG<?> cfg, double dampingFactor, int iterations) {
		return calculateHitAware(cfg, dampingFactor, iterations, false, (i, j) -> {
			// weight of 1 will basically use the basic ASCOS algorithm (not exactly)
			return 1.0;
		});
	}
	
	/**
	 * Calculates the (inverted) ASCOS scores for all nodes in the given CFG. This will base scores on *outgoing* edges.
	 * <br><br> Treats each execution as a separate link.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with score arrays for each CFG node index 
	 * @see <a href="https://dl.acm.org/doi/10.1145/2776894">https://dl.acm.org/doi/10.1145/2776894</a>
	 */
	public static Map<Integer, double[]> calculateHitAwareInvertedASCOS(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations) {
		return calculateHitAware(cfg, dampingFactor, iterations, true, (i, j) -> {
			// weight of 1 will basically use the basic ASCOS algorithm (not exactly)
			return 1.0;
		});
	}
	
	/**
	 * Calculates the ASCOS scores for all nodes in the given CFG. This will base scores on *ingoing* edges.
	 * <br><br> Treats each execution as a separate link.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with score arrays for each CFG node index 
	 * @see <a href="https://dl.acm.org/doi/10.1145/2776894">https://dl.acm.org/doi/10.1145/2776894</a>
	 */
	public static Map<Integer, double[]> calculateHitAwareASCOSPlusPlus(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations) {
		return calculateHitAware(cfg, dampingFactor, iterations, false, (i, j) -> {
			// use average score of both connected nodes as weight
			return (cfg.getScore(i) + cfg.getScore(j)) / 2.0;
		});
	}
	
	/**
	 * Calculates the (inverted) ASCOS scores for all nodes in the given CFG. This will base scores on *outgoing* edges.
	 * <br><br> Treats each execution as a separate link.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with score arrays for each CFG node index 
	 * @see <a href="https://dl.acm.org/doi/10.1145/2776894">https://dl.acm.org/doi/10.1145/2776894</a>
	 */
	public static Map<Integer, double[]> calculateHitAwareInvertedASCOSPlusPlus(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations) {
		return calculateHitAware(cfg, dampingFactor, iterations, true, (i, j) -> {
			// use average score of both connected nodes as weight
			return (cfg.getScore(i) + cfg.getScore(j)) / 2.0;
		});
	}
	
	/**
	 * Calculates the ASCOS and inverted ASCOS scores for all nodes in the given CFG and returns scores based on the 
	 * vector lengths, treating both scores as vector components.
	 * <br><br> Treats each execution as a separate link.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with score arrays for each CFG node index 
	 */
	public static Map<Integer, double[]> calculateHitAwareSimVectorRank(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations) {
		Map<Integer, double[]> pr = calculateHitAwareASCOS(cfg, dampingFactor, iterations);
		Map<Integer, double[]> cr = calculateHitAwareInvertedASCOS(cfg, dampingFactor, iterations);
		
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
	
	/**
	 * Calculates the ASCOS and inverted ASCOS scores for all nodes in the given CFG and returns scores based on the 
	 * vector lengths, treating both scores as vector components.
	 * <br><br> Treats each execution as a separate link.
	 * @param cfg a CFG
	 * @param dampingFactor the damping factor to use
	 * @param iterations the maximum number of iterations; if this is 0, then the algorithm runs until it converges
	 * @return a map with score arrays for each CFG node index 
	 */
	public static Map<Integer, double[]> calculateHitAwareSimVectorRankPlusPlus(ScoredDynamicCFG<?> cfg, double dampingFactor, int iterations) {
		Map<Integer, double[]> pr = calculateHitAwareASCOSPlusPlus(cfg, dampingFactor, iterations);
		Map<Integer, double[]> cr = calculateHitAwareInvertedASCOSPlusPlus(cfg, dampingFactor, iterations);
		
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
	
	private static Map<Integer, double[]> calculateHitAware(CFG<?> cfg, double dampingFactor, int iterations, 
			boolean inverted, BiFunction<Integer, Integer, Double> weightFunction) {
		// get highest existing node index to inititalize score arrays;
		// not all node indices may exist, but it's probably better than using thousands of HashMaps...
		int arrayLength = getHighestNodeIndex(cfg) + 1;
		// initialize the values
		double offset = (1 - dampingFactor) / cfg.getNodes().size();

		Map<Integer, double[]> ASCOS = new HashMap<>();
		for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
			Node node = entry.getValue();
			double[] scores = new double[arrayLength];
			scores[node.getIndex()] = 1;
			ASCOS.put(node.getIndex(), scores);
		}
		
		Map<Integer, double[]> old_ASCOS;

		// loop until values converge or max iterations reached
		int count = 0;
		do {
			old_ASCOS = ASCOS;
			//Calculate the ASCOS
			ASCOS = calculateHitAwareIteration(ASCOS, cfg, offset, dampingFactor, inverted, weightFunction);
			++count;
		} while (!didConverge(old_ASCOS, ASCOS) && (iterations <= 0 || count < iterations));

		return ASCOS;
	}
	
	private static Map<Integer, double[]> calculateHitAwareIteration(Map<Integer, double[]> ASCOS, CFG<?> cfg, 
			double offset, double dampingFactor, boolean inverted, BiFunction<Integer, Integer, Double> weightFunction) {
		
		Map<Integer, double[]> newASCOSArray = new HashMap<>();
		
		if (inverted) {
			for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
				Node nodeA = entry.getValue();
				double[] oldScores = ASCOS.get(nodeA.getIndex());
				double[] newScores = new double[oldScores.length];
				// get out-nodes of node A
				if (nodeA.hasSuccessors()) {
					int[] aSuccessors = nodeA.getSuccessors();
					long[] aSuccessorHits = nodeA.getSuccessorHits();
					// iterate over all nodes B
					for (Entry<Integer, Node> entry2 : cfg.getNodes().entrySet()) {
						// calculate s(A,B)
						Node nodeB = entry2.getValue();
						if (nodeA.getIndex() == nodeB.getIndex()) {
							// s(*,*) is always defined as 1
							newScores[nodeA.getIndex()] = 1;
						} else {
							// use ASCOS equation to calculate s(A,B)
							// calculate sums over all successor scores
							double sum = 0;
							double weightSum = 0;
							for (int i = 0; i < aSuccessors.length; i++) {
								double weight = weightFunction.apply(nodeA.getIndex(), aSuccessors[i]);
								sum += aSuccessorHits[i] * weight * Math.abs(1 - Math.exp(weight)) * ASCOS.get(aSuccessors[i])[nodeB.getIndex()];
								weightSum += aSuccessorHits[i] * weight;
							}
							// full score calculation
							newScores[nodeB.getIndex()] = dampingFactor / weightSum * sum;
						}
					}
				}
				newASCOSArray.put(nodeA.getIndex(), newScores);
			}
		} else {
			for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
				Node nodeA = entry.getValue();
				double[] oldScores = ASCOS.get(nodeA.getIndex());
				double[] newScores = new double[oldScores.length];
				// get in-nodes of node A
				if (nodeA.hasPredecessors()) {
					int[] aPredecessors = nodeA.getPredecessors();
					long[] aPredecessorHits = nodeA.getPredecessorHits();
					// iterate over all nodes B
					for (Entry<Integer, Node> entry2 : cfg.getNodes().entrySet()) {
						// calculate s(A,B)
						Node nodeB = entry2.getValue();
						if (nodeA.getIndex() == nodeB.getIndex()) {
							// s(*,*) is always defined as 1
							newScores[nodeA.getIndex()] = 1;
						} else {
							// use ASCOS equation to calculate s(A,B)
							// calculate sums over all predecessor scores
							double sum = 0;
							double weightSum = 0;
							for (int i = 0; i < aPredecessors.length; i++) {
								double weight = weightFunction.apply(nodeA.getIndex(), aPredecessors[i]);
								sum += aPredecessorHits[i] * weight * Math.abs(1 - Math.exp(weight)) * ASCOS.get(aPredecessors[i])[nodeB.getIndex()];
								weightSum += aPredecessorHits[i] * weight;
							}
							// full score calculation
							newScores[nodeB.getIndex()] = dampingFactor / weightSum * sum;
						}
					}
				}
				newASCOSArray.put(nodeA.getIndex(), newScores);
			}
		}
		
		return newASCOSArray;
	}
	
}