/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.localizer.sbfl.localizers;

import se.de.hu_berlin.informatik.stardust.localizer.sbfl.AbstractSpectrumBasedFaultLocalizer;
import se.de.hu_berlin.informatik.stardust.spectra.INode;

/**
 * Jaccard fault localizer $\frac{\EF}{\EF+\NF+\EP}$
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Jaccard<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Jaccard() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
    	if (node.getEF(strategy) == 0) {
    		return 0;
    	}
        return (double)node.getEF(strategy) / (double)(node.getEF(strategy) + node.getNF(strategy) + node.getEP(strategy));
    }

    @Override
    public String getName() {
        return "Jaccard";
    }

}
