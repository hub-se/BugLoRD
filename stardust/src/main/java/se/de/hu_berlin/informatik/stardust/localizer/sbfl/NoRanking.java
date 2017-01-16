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

	final private boolean onlyIncludeExecutedElements;
    /**
     * Create fault localizer
     * @param onlyIncludeExecutedElements
     * whether to include only executed nodes in the ranking
     */
    public NoRanking(boolean onlyIncludeExecutedElements) {
        super();
        this.onlyIncludeExecutedElements = onlyIncludeExecutedElements;
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        if (onlyIncludeExecutedElements) {
        	return node.getEF() + node.getEP();
        } else {
        	return 1;
        }
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
            if (suspiciousness > 0) {
            	ranking.add(node, suspiciousness);
            }
        }
        return ranking;
    }
}
