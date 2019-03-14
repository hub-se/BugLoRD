/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.faultlocalizer.extra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.de.hu_berlin.informatik.faultlocalizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Ample;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Anderberg;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.ArithmeticMean;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Cohen;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Dice;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Euclid;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Fleiss;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.GeometricMean;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Goodman;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Hamann;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Hamming;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.HarmonicMean;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Jaccard;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Kulczynski1;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Kulczynski2;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.M1;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.M2;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Ochiai;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Ochiai2;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Overlap;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.RogersTanimoto;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Rogot1;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Rogot2;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.RussellRao;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Scott;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.SimpleMatching;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Sokal;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.SorensenDice;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Tarantula;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Wong1;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Wong2;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Wong3;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Zoltar;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.ranking.SBFLNormalizedRanking;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.ranking.SBFLRanking;
import se.de.hu_berlin.informatik.spectra.core.ComputationStrategies;
import se.de.hu_berlin.informatik.spectra.core.ILocalizerCache;
import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.utils.experiments.ranking.NormalizedRanking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.NormalizedRanking.NormalizationStrategy;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

/**
 * Implements the Fusing Fault Localizers as proposed by Lucia, David Lo and Xin Xia.
 *
 * @param <T>
 * a type
 */
public class FusingFaultLocalizer<T> implements IFaultLocalizer<T> {

    /** Holds all SBFL to fuse */
    private final List<IFaultLocalizer<T>> sbfl = new ArrayList<>();

    /** Chosen normalization strategy */
    private final NormalizationStrategy normalizationStrategy;
    /** Chosen selection strategy */
    private final SelectionTechnique selectionStrategy;
    /** Chosen data fusion strategy */
    private final DataFusionTechnique fusionStrategy;

    /**
     * Enum representing all available selection techniques
     */
    public enum SelectionTechnique {
        OVERLAP_RATE, BIAS_RATE
    }

    /**
     * Enum representing all available data fusion techniques.
     */
    public enum DataFusionTechnique {
        COMB_SUM, COMB_ANZ, COMB_MNZ
    }

    /**
     * Constructs a fusion fault localizer
     *
     * @param normalization
     *            strategy to use
     * @param selection
     *            strategy to use
     * @param dataFusion
     *            strategy to use
     */
    public FusingFaultLocalizer(final NormalizationStrategy normalization, final SelectionTechnique selection,
            final DataFusionTechnique dataFusion) {
        super();
        this.normalizationStrategy = normalization;
        this.selectionStrategy = selection;
        this.fusionStrategy = dataFusion;

        this.sbfl.add(new Ample<>());
        this.sbfl.add(new Anderberg<>());
        this.sbfl.add(new ArithmeticMean<>());
        this.sbfl.add(new Cohen<>());
        this.sbfl.add(new Dice<>());
        this.sbfl.add(new Euclid<>());
        this.sbfl.add(new Fleiss<>());
        this.sbfl.add(new GeometricMean<>());
        this.sbfl.add(new Goodman<>());
        this.sbfl.add(new Hamann<>());
        this.sbfl.add(new Hamming<>());
        this.sbfl.add(new HarmonicMean<>());
        this.sbfl.add(new Jaccard<>());
        this.sbfl.add(new Kulczynski1<>());
        this.sbfl.add(new Kulczynski2<>());
        this.sbfl.add(new M1<>());
        this.sbfl.add(new M2<>());
        this.sbfl.add(new Ochiai<>());
        this.sbfl.add(new Ochiai2<>());
        this.sbfl.add(new Overlap<>());
        this.sbfl.add(new RogersTanimoto<>());
        this.sbfl.add(new Rogot1<>());
        this.sbfl.add(new Rogot2<>());
        this.sbfl.add(new RussellRao<>());
        this.sbfl.add(new Scott<>());
        this.sbfl.add(new SimpleMatching<>());
        this.sbfl.add(new Sokal<>());
        this.sbfl.add(new SorensenDice<>());
        this.sbfl.add(new Tarantula<>());
        this.sbfl.add(new Wong1<>());
        this.sbfl.add(new Wong2<>());
        this.sbfl.add(new Wong3<>());
        this.sbfl.add(new Zoltar<>());
    }

    @Override
    public String getName() {
        return String.format("F-%s-%s-%s", this.normalizationStrategy.toString(), this.selectionStrategy.toString(),
                this.fusionStrategy.toString());
    }

    @Override
    public Ranking<INode<T>> localize(final ILocalizerCache<T> localizer, ComputationStrategies strategy) {
        final Map<IFaultLocalizer<T>, NormalizedRanking<INode<T>>> sbflRankings = new HashMap<>();
        // create ordinary rankings
        for (final IFaultLocalizer<T> fl : this.sbfl) {
            final Ranking<INode<T>> ranking = fl.localize(localizer);
            sbflRankings.put(fl, new SBFLNormalizedRanking<>(ranking, this.normalizationStrategy));
        }

        // compute top-K nodes per ranking metric
        final int k = new Double(localizer.getNodes().size() * 0.1 < 10 ? 10 : localizer.getNodes().size() * 0.1)
        .intValue();
        final Map<IFaultLocalizer<T>, Set<INode<T>>> topK = this.topK(sbflRankings, k);


        // select techniques
        final List<IFaultLocalizer<T>> selected;
        switch (this.selectionStrategy) {
        case OVERLAP_RATE:
            selected = this.selectOverlapBased(sbflRankings, topK);
            break;
        case BIAS_RATE:
            selected = this.selectBiasBased(sbflRankings, topK);
            break;
        default:
            throw new RuntimeException("Selection strategy " + this.selectionStrategy.toString()
                    + " not implemented yet");
        }
        assert selected != null && selected.size() > 1;
        Log.out(this, "Selected " + selected.size());

        // combine
        switch (this.fusionStrategy) {
        case COMB_ANZ:
            return this.fuseCombAnz(localizer, selected, sbflRankings);

        case COMB_SUM:
            return this.fuseCombSum(localizer, selected, sbflRankings);

        default:
            throw new RuntimeException("Data fusion strategy " + this.fusionStrategy.toString()
                    + " not implemented yet");
        }
    }

    /**
     * Extracts the top K ranked nodes from all rankings and puts them in separate sets for each FL.
     *
     * @param rankings
     *            the rankings to extract the top nodes from
     * @param k
     *            number of nodes to extract
     * @return top k
     */
    protected Map<IFaultLocalizer<T>, Set<INode<T>>> topK(final Map<IFaultLocalizer<T>, NormalizedRanking<INode<T>>> rankings,
            final int k) {
        final Map<IFaultLocalizer<T>, Set<INode<T>>> topK = new HashMap<>();
        for (final Map.Entry<IFaultLocalizer<T>, NormalizedRanking<INode<T>>> iFaultLocalizerNormalizedRankingEntry : rankings.entrySet()) {
            final Set<INode<T>> top = new HashSet<>();
            final NormalizedRanking<INode<T>> ranking = iFaultLocalizerNormalizedRankingEntry.getValue();
            for (final INode<T> node : ranking) {
                top.add(node);
                if (top.size() >= k) {
                    break;
                }
            }
            topK.put(iFaultLocalizerNormalizedRankingEntry.getKey(), top);
        }
        return topK;
    }

    /**
     * Selects half of the input techniques that have as little overlap as possible
     *
     * @param rankings
     *            rankings of different FL algorithms
     * @param topK
     *            all sets of topK nodes for each FL
     * @return selected algorithms based on ranking node overlap
     */
    protected List<IFaultLocalizer<T>> selectOverlapBased(
            final Map<IFaultLocalizer<T>, NormalizedRanking<INode<T>>> rankings, final Map<IFaultLocalizer<T>, Set<INode<T>>> topK) {
        // add set containing all
        final Set<INode<T>> all = new HashSet<>();
        for (final Set<INode<T>> specific : topK.values()) {
            all.addAll(specific);
        }

        // add to sorted map
        final Map<IFaultLocalizer<T>, Double> sortby = new HashMap<>();
        for (final IFaultLocalizer<T> fl : rankings.keySet()) {
            // score
            final double oRate = (double) (all.size() - topK.get(fl).size()) / (double) all.size();
            sortby.put(fl, oRate);
        }

        // select'em
        return this.selectUsingMap(sortby, 0.5, true);
    }

    /**
     * Selects half of the input techniques that are less similar towards the norm.
     *
     * @param rankings
     *            rankings of different FL algorithms
     * @param topK
     *            all sets of topK nodes for each FL
     * @return selected algorithms based on ranking node overlap
     */
    protected List<IFaultLocalizer<T>> selectBiasBased(
            final Map<IFaultLocalizer<T>, NormalizedRanking<INode<T>>> rankings, final Map<IFaultLocalizer<T>, Set<INode<T>>> topK) {

        // Create L_ALL
        final Map<INode<T>, Integer> lAll = new HashMap<>();
        for (final Set<INode<T>> specific : topK.values()) {
            for (final INode<T> curNode : specific) {
                if (!lAll.containsKey(curNode)) {
                    lAll.put(curNode, 0);
                }
                lAll.put(curNode, lAll.get(curNode) + 1);
            }
        }

        // calculate similarity
        final Map<IFaultLocalizer<T>, Double> bias = new HashMap<>();
        int lAllSum = 0;
        for (final Integer rankedIn : lAll.values()) {
            lAllSum += rankedIn * rankedIn;
        }
        for (final IFaultLocalizer<T> fl : rankings.keySet()) {
            int numSum = 0;
            final int lSum = topK.get(fl).size();
            for (final INode<T> curNode : topK.get(fl)) {
                numSum += lAll.get(curNode);
            }

            bias.put(fl, 1.0d - (double) numSum / (Math.sqrt((double) lSum) * Math.sqrt((double) lAllSum)));
        }

        // select'em
        return this.selectUsingMap(bias, 0.5, false);
    }

    /**
     * Selects a number of techniques. Sorts the techniques in ascending order by the double values provided in sortby
     * if the asc parameter is set to true
     *
     * @param sortby
     *            the score to sort the techniques by
     * @param n
     *            the percentage of techniques to select
     * @param asc
     *            true to sort ascending, false to sort descending
     * @return selected techniques
     */
    private List<IFaultLocalizer<T>> selectUsingMap(final Map<IFaultLocalizer<T>, Double> sortby, final double n,
            final boolean asc) {
        final List<Map.Entry<IFaultLocalizer<T>, Double>> list = new ArrayList<>(sortby.entrySet());
        list.sort((o1, o2) -> {
            if (asc) {
                return o1.getValue().compareTo(o2.getValue());
            } else {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        final Map<IFaultLocalizer<T>, Double> sorted = new LinkedHashMap<>();
        for (final Map.Entry<IFaultLocalizer<T>, Double> entry : list) {
            sorted.put(entry.getKey(), entry.getValue());
        }

        // select
        final List<IFaultLocalizer<T>> selected = new ArrayList<>();
        for (final IFaultLocalizer<T> fl : sorted.keySet()) {
            selected.add(fl);
            if (selected.size() >= sorted.size() * n) {
                break;
            }
        }
        return selected;
    }

    /**
     * Combine scores to final ranking using CombANZ technique
     *
     * @param localizer
     *            spectra ranking is backed on
     * @param selected
     *            selected techniques
     * @param rankings
     *            rankings of all techniques
     * @return new ranking
     */
    protected SBFLRanking<T> fuseCombAnz(final ILocalizerCache<T> localizer, final List<IFaultLocalizer<T>> selected,
            final Map<IFaultLocalizer<T>, NormalizedRanking<INode<T>>> rankings) {
        final SBFLRanking<T> finalRanking = new SBFLRanking<>();
        for (final INode<T> node : localizer.getNodes()) {
            double sum = 0;
            int nonZero = 0;
            for (final IFaultLocalizer<T> fl : selected) {
                final double score = rankings.get(fl).getRankingValue(node);
                sum += score;
                if (score != 0) {
                    nonZero++;
                }
            }
            double finalScore;
            if (nonZero == 0) {
                finalScore = 0.0d;
            } else {
                finalScore = 1.0d / nonZero * sum;
            }

            finalRanking.add(node, finalScore);
        }
        return finalRanking;
    }

    /**
     * Combine scores to final ranking using CombSUM technique
     *
     * @param localizer
     *            spectra ranking is backed on
     * @param selected
     *            selected techniques
     * @param rankings
     *            rankings of all techniques
     * @return new ranking
     */
    protected SBFLRanking<T> fuseCombSum(final ILocalizerCache<T> localizer, final List<IFaultLocalizer<T>> selected,
            final Map<IFaultLocalizer<T>, NormalizedRanking<INode<T>>> rankings) {
        final SBFLRanking<T> finalRanking = new SBFLRanking<>();
        for (final INode<T> node : localizer.getNodes()) {
            double finalScore = 0;
            for (final IFaultLocalizer<T> fl : selected) {
                finalScore += rankings.get(fl).getRankingValue(node);
            }
            finalRanking.add(node, finalScore);
        }
        return finalRanking;
    }


    protected SBFLRanking<T> fuseCorrB(final ISpectra<T,?> spectra, final List<IFaultLocalizer<T>> selected,
            final Map<IFaultLocalizer<T>, SBFLRanking<T>> rankings) {
        return null;
    }

	@Override
	public double suspiciousness(INode<T> node, ComputationStrategies strategy) {
		throw new UnsupportedOperationException();
	}

//    /**
//     * Used to calculate the weighted sum of a set of fault localization techniques to create a new ranking.
//     *
//     * To be used by correlation based methods.
//     *
//     * @param spectra
//     *            pectra ranking is backed on
//     * @param weights
//     *            selected techniques and their ranking
//     * @param rankings
//     *            rankings of all techniques
//     * @return new ranking
//     */
//    private Ranking<T> fuseWeightedSum(final ISpectra<T> spectra, final Map<IFaultLocalizer<T>, Double> weights,
//            final Map<IFaultLocalizer<T>, Ranking<T>> rankings) {
//        final Ranking<T> ranking = new Ranking<>();
//        for (final INode<T> node : spectra.getNodes()) {
//            double score = 0;
//            for (final IFaultLocalizer<T> fl : weights.keySet()) {
//                score += weights.get(fl) + rankings.get(fl).getSuspiciousness(node);
//            }
//            ranking.rank(node, score);
//        }
//        return ranking;
//    }

}
