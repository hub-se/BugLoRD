/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers;

import se.de.hu_berlin.informatik.faultlocalizer.sbfl.AbstractSpectrumBasedFaultLocalizer;
import se.de.hu_berlin.informatik.spectra.core.ComputationStrategies;
import se.de.hu_berlin.informatik.spectra.core.INode;

/**
 * Simple Matching fault localizer $\frac{\EF+\NP}{\EF+\NF+\EP+\NP}$
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class SimpleMatching<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public SimpleMatching() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
    	double numerator = node.getEF(strategy) + node.getNP(strategy);
    	if (numerator == 0) {
    		return 0;
    	}
        return numerator / (node.getEF(strategy) + node.getNF(strategy) + node.getEP(strategy) + node.getNP(strategy));
    }

    @Override
    public String getName() {
        return "SimpleMatching";
    }

}
