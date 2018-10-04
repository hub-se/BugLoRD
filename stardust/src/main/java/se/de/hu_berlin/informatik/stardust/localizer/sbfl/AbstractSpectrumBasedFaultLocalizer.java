/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.localizer.sbfl;

import se.de.hu_berlin.informatik.stardust.localizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.stardust.localizer.SBFLRanking;
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking.RankingValueReplacementStrategy;

/**
 * Class is used to simplify the creation of spectrum based fault localizers.
 *
 * @param <T>
 * type used to identify nodes in the system
 */
public abstract class AbstractSpectrumBasedFaultLocalizer<T> implements IFaultLocalizer<T> {

	public static enum ComputationStrategies {
		STANDARD_SBFL, SIMILARITY_SBFL
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Ranking<INode<T>> localize(final ISpectra<T,?> spectra, ComputationStrategies strategy) {
		final Ranking<INode<T>> ranking = new SBFLRanking<>();
		for (final INode<T> node : spectra.getNodes()) {
			final double suspiciousness = this.suspiciousness(node, strategy);
			ranking.add(node, suspiciousness);
		}

		// treats NaN values as being negative infinity
		return Ranking.getRankingWithStrategies(
				ranking, RankingValueReplacementStrategy.NEGATIVE_INFINITY, RankingValueReplacementStrategy.INFINITY,
				RankingValueReplacementStrategy.NEGATIVE_INFINITY);
	}

}
