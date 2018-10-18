/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.faultlocalizer.hierarchical;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import se.de.hu_berlin.informatik.faultlocalizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.ranking.SBFLRanking;
import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.hit.HierarchicalHitSpectra;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class LevelLocalizer<P, C> implements IHierarchicalFaultLocalizer<P, C> {

    /** Holds the fault localizers to use for each level. */
    private final List<IFaultLocalizer<?>> levelLocalizers = new ArrayList<>();

    public LevelLocalizer() {
        super();
    }

    /**
     * Adds a specific localizer for a single level.
     *
     * Level indexing starts at 0 (top level, also passed to localize()) and rises by 1 for each child level.
     *
     * @param level
     *            the level to specify the localizer for
     * @param localizer
     *            the actual localizer
     */
    public void setLevelLocalizer(final int level, final IFaultLocalizer<?> localizer) {
        this.levelLocalizers.add(level, localizer);
    }

    //TODO: this is certainly not correct...
	@SuppressWarnings("unchecked")
	@Override
    public Ranking<? super INode<?>> localize(final HierarchicalHitSpectra<P, C> spectra) {
        int level = 0;
        ISpectra<?,?> cur = spectra;
        final List<Ranking<? super INode<?>>> levelRankings = new ArrayList<>();
        while (cur != null) {
        	Log.out(this, String.format("Lvl: %d, Hash: %d", level, cur.hashCode()));

            // try to create ranking of parent and child levels
            Ranking<? super INode<?>> curRanking;
            try {
                curRanking =  (Ranking<? super INode<?>>) this.localize(this.levelLocalizers.get(level), cur);
                levelRankings.add(curRanking);
            } catch (final IndexOutOfBoundsException e) {
                throw new RuntimeException(String.format(
                        "No fault localizer set for level %d of hierarchical spectra.", level), e);
            }

            // go hierarchical
            if (cur instanceof HierarchicalHitSpectra) {
                cur = ((HierarchicalHitSpectra<?, ?>) cur).getChildSpectra();
            } else {
                cur = null;
            }
            level++;
        }

        // create ranking
        @SuppressWarnings("rawtypes")
		final Ranking<? super INode<?>> ranking = new SBFLRanking();
        this.addRecursive(ranking, spectra, new HashSet<INode<?>>(spectra.getNodes()), levelRankings, 0.0d);
        return ranking;
    }

    private <L> double getSuspiciousness(final Ranking<? super INode<L>> ranking, final INode<?> node) {
        @SuppressWarnings("unchecked")
        final INode<L> real = (INode<L>) node;
        return ranking.getRankingValue(real);
    }

    private <L, M> Set<INode<M>> getChildrenof(final HierarchicalHitSpectra<L, M> children, final INode<?> node) {
        @SuppressWarnings("unchecked")
        final INode<L> real = (INode<L>) node;
        return children.getChildrenOf(real);
    }

    private void rank(final Ranking<? super INode<?>> ranking, final INode<?> node, final double suspiciousness) {
        ranking.add(node, suspiciousness);
    }

    private void addRecursive(final Ranking<? super INode<?>> finalRanking, final ISpectra<?,?> spectra, final Set<INode<?>> curNodes,
            final List<Ranking<? super INode<?>>> rankings, final double score) {
        if (spectra instanceof HierarchicalHitSpectra) {
            // recurse branch - apply this for all children
            final HierarchicalHitSpectra<?, ?> hSpectra = (HierarchicalHitSpectra<?, ?>) spectra;
            for (final INode<?> curNode : curNodes) {
                this.addRecursive(finalRanking, hSpectra.getChildSpectra(),
                        new HashSet<INode<?>>(this.getChildrenof(hSpectra, curNode)),
                        rankings.subList(1, rankings.size()), score + this.getSuspiciousness(rankings.get(0), curNode));
            }
        } else {
            // abort branch - add all nodes with score added to their suspiciousness
            for (final INode<?> node : curNodes) {
                this.rank(finalRanking, node, score + this.getSuspiciousness(rankings.get(0), node));
            }
        }
    }

    /**
     * Hope that given fault localizer of level matches with generic type of level spectra.
     *
     * @param localizer
     * @param spectra
     * @return ranking of specific level
     */
    private <L> Ranking<? super INode<L>> localize(final IFaultLocalizer<L> localizer, final ISpectra<?,?> spectra) {
        @SuppressWarnings("unchecked")
        final ISpectra<L,?> real = (ISpectra<L,?>) spectra;
        return localizer.localize(real);
    }

//    /**
//     * Merges two rankings by hoping that data types fit together.
//     *
//     * @param one
//     * @param two
//     * @return merged rankings
//     */
//    private <M> Ranking<M> merge(final Ranking<M> one, final Ranking<?> two) {
//        @SuppressWarnings("unchecked")
//        final Ranking<M> real = (Ranking<M>) two;
//        return one.merge(real);
//    }

}
