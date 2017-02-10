package se.de.hu_berlin.informatik.stardust.localizer;

import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.utils.experiments.ranking.SimpleRankingMetric;

/**
 * Holds all ranking information for a node.
 * @param <T>
 * the type of the node
 */
public class SBFLRankingMetric<T> extends SimpleRankingMetric<INode<T>> {

    protected SBFLRankingMetric(INode<T> node, int bestRanking, int ranking, int worstRanking, double suspiciousness,
			int numberOfElements) {
		super(node, bestRanking, ranking, worstRanking, suspiciousness, numberOfElements);
	}

}
