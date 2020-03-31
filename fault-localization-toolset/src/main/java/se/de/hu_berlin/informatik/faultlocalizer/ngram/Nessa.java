package se.de.hu_berlin.informatik.faultlocalizer.ngram;

import se.de.hu_berlin.informatik.faultlocalizer.sbfl.AbstractFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.ranking.NodeRanking;
import se.de.hu_berlin.informatik.spectra.core.*;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class Nessa<T> extends AbstractFaultLocalizer<T> {
    private HashMap<Integer, Double> confidence;
    private double minSup = 0.9;
    private int maxN = 3;
    private boolean dynSup;

    public Nessa() {
        super();
        confidence = new LinkedHashMap<>();
    }

    public Nessa(int maxN, double minSup) {
        super();
        confidence = new LinkedHashMap<>();
        this.maxN = maxN;
        this.minSup = minSup;
    }

    public Nessa(int maxN, double minSup, boolean dynSup) {
        super();
        confidence = new LinkedHashMap<>();
        this.maxN = maxN;
        this.minSup = minSup;
        this.dynSup = dynSup;
    }

    @Override
    public Ranking<INode<T>> localize(final ISpectra<T, ?> spectra, ComputationStrategies strategy) {
        final Ranking<INode<T>> ranking = new NodeRanking<>();
        LinearExecutionHitTrace hitTrace = new LinearExecutionHitTrace((ISpectra<SourceCodeBlock, ?>) spectra);
        NGramSet nGrams = dynSup ?
                new NGramSet(hitTrace, maxN, minSup, true) : new NGramSet(hitTrace, maxN, minSup);
        confidence = nGrams.getConfidence();
        confidence.forEach((key, value) -> {
                    ranking.add(spectra.getNode(key), value);
                }
        );
        for (final INode<T> node : spectra.getNodes()) {
            if (confidence.get(node) != null) continue;
            ranking.add(node, 0.0);
        }

        return Ranking.getRankingWithStrategies(
                ranking, Ranking.RankingValueReplacementStrategy.ZERO, Ranking.RankingValueReplacementStrategy.ZERO,
                Ranking.RankingValueReplacementStrategy.ZERO);
    }

    @Override
    public Ranking<INode<T>> localize(final ILocalizerCache<T> localizer, ComputationStrategies strategy) {
        return localize((ISpectra<T, ?>) localizer, strategy);

    }

    @Override
    public double suspiciousness(INode<T> node, ComputationStrategies strategy) {
        return 0;
    }
}
