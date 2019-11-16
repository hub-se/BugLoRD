package se.de.hu_berlin.informatik.faultlocalizer.ngram;

import se.de.hu_berlin.informatik.faultlocalizer.sbfl.AbstractFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.ranking.NodeRanking;
import se.de.hu_berlin.informatik.spectra.core.ComputationStrategies;
import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;

import java.util.HashMap;

public class Nessa<T> extends AbstractFaultLocalizer<T> {
    private HashMap<Integer, Double> confidence;

    public Nessa() {
        super();
        confidence = new HashMap<>();
    }

    @Override
    public Ranking<INode<T>> localize(final ISpectra<T, ?> spectra, ComputationStrategies strategy) {
        final Ranking<INode<T>> ranking = new NodeRanking<>();
        LinearExecutionHitTrace hitTrace = new LinearExecutionHitTrace((ISpectra<SourceCodeBlock, ?>) spectra);
        NGramSet nGrams = new NGramSet(hitTrace, 3, 0.9);
        confidence = nGrams.getConfidence();
        for (final INode<T> node : spectra.getNodes()) {
            final double suspiciousness = this.suspiciousness(node, strategy);
            ranking.add(node, suspiciousness);
        }
        return Ranking.getRankingWithStrategies(
                ranking, Ranking.RankingValueReplacementStrategy.NEGATIVE_INFINITY, Ranking.RankingValueReplacementStrategy.INFINITY,
                Ranking.RankingValueReplacementStrategy.NEGATIVE_INFINITY);
    }

    @Override
    public double suspiciousness(INode<T> node, ComputationStrategies strategy) {
        if (confidence.get(node.getIndex()) != null) return confidence.get(node.getIndex());
        else return 0;
    }
}
