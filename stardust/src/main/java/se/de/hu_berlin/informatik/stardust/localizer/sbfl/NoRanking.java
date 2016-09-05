/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.localizer.sbfl;

import se.de.hu_berlin.informatik.stardust.localizer.HitRanking;
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;

/**
 * Simple Matching fault localizer
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class NoRanking<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public NoRanking() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        return 0;
    }

    @Override
    public String getName() {
        return "NoRanking";
    }
    
    /**
     * Creates a fault location ranking for all nodes in the given spectra.
     * 
     * @param spectra
     *            the spectra to perform the fault localization on
     * @return nodes ranked by suspiciousness of actually causing the failure
     */
    public HitRanking<T> localizeHit(final ISpectra<T> spectra) {
        final HitRanking<T> ranking = new HitRanking<>();
        for (final INode<T> node : spectra.getNodes()) {
            final double suspiciousness = this.suspiciousness(node);
            ranking.rank(node, suspiciousness);
        }
        return ranking;
    }
}
