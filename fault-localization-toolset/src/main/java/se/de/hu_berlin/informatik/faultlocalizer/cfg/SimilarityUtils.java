package se.de.hu_berlin.informatik.faultlocalizer.cfg;

import java.util.Map;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.spectra.core.cfg.Node;
import se.de.hu_berlin.informatik.spectra.core.cfg.ScoredDynamicCFG;

public class SimilarityUtils {

	public static enum CalculationStrategy {
		MAX_SIMILARITY("max"),
		AVERAGE_SIMILARITY("avg");
		
		String id;
	
		private CalculationStrategy(String id) {
			this.id = id;
		}
	}

	public static <T> double calculateScore(ScoredDynamicCFG<T> cfg, Map<Integer, double[]> simRank, int index, CalculationStrategy strategy) {
			double baseScore = cfg.getScore(index);
			double[] simRankScores = simRank.get(index);
			double score = baseScore;
			
			switch (strategy) {
			case AVERAGE_SIMILARITY:
				score = 0;
				for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
					int oNodeIndex = entry.getValue().getIndex();
					// add up all node's base scores in relation to the nodes' similarity to this node;
					// this way, a node with many similar nodes that are highly suspicious gets a higher score
					score += simRankScores[oNodeIndex] * cfg.getScore(oNodeIndex);
				}
	//			score /= cfg.getNodes().size();
				break;
			case MAX_SIMILARITY:
				score = baseScore;
				for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
					int oNodeIndex = entry.getValue().getIndex();
	//				double oldScore = score;
					// if there exists a very similar node with a high base suspiciousness score, then increase this node's score...
					score = Math.max(score, simRankScores[oNodeIndex] * cfg.getScore(oNodeIndex));
	//				if (score != oldScore) {
	//					System.out.println(String.format("%s%n%s", 
	//							cfg.getIdentifierString(index), cfg.getIdentifierString(oNodeIndex)));
	//					System.out.println(String.format("similarity (%.3f) of nodes (%d, %d) increased score from %.3f to %.3f", 
	//							simRankScores[oNodeIndex], index, oNodeIndex, oldScore, score));
	//				}
				}
				break;
			}
	
			return score;
		}

}
