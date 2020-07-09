/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.faultlocalizer.sbfl;

import se.de.hu_berlin.informatik.faultlocalizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.ranking.NodeRanking;
import se.de.hu_berlin.informatik.spectra.core.ComputationStrategies;
import se.de.hu_berlin.informatik.spectra.core.ILocalizerCache;
import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking.RankingValueReplacementStrategy;

/**
 * Class is used to simplify the creation of (spectrum based) fault localizers.
 *
 * @param <T> type used to identify nodes in the system
 */
public abstract class AbstractFaultLocalizer<T> implements IFaultLocalizer<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Ranking<INode<T>> localize(final ISpectra<T, ? extends ITrace<T>> spectra, ComputationStrategies strategy) {
        final Ranking<INode<T>> ranking = new NodeRanking<>();
        for (final INode<T> node : spectra.getNodes()) {
            final double suspiciousness = this.suspiciousness(node, strategy);
            ranking.add(node, suspiciousness);
        }

        // treats NaN values as being negative infinity
        return Ranking.getRankingWithStrategies(
                ranking, RankingValueReplacementStrategy.NEGATIVE_INFINITY, RankingValueReplacementStrategy.INFINITY,
                RankingValueReplacementStrategy.NEGATIVE_INFINITY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Ranking<INode<T>> localize(final ILocalizerCache<T> localizer, ComputationStrategies strategy) {
        final Ranking<INode<T>> ranking = new NodeRanking<>();
        for (final INode<T> node : localizer.getNodes()) {
            final double suspiciousness = this.suspiciousness(node, strategy);
            ranking.add(node, suspiciousness);
        }

        // treats NaN values as being negative infinity
        return Ranking.getRankingWithStrategies(
                ranking, RankingValueReplacementStrategy.NEGATIVE_INFINITY, RankingValueReplacementStrategy.INFINITY,
                RankingValueReplacementStrategy.NEGATIVE_INFINITY);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

}
