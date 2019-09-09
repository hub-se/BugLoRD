/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers;

import se.de.hu_berlin.informatik.faultlocalizer.sbfl.AbstractFaultLocalizer;
import se.de.hu_berlin.informatik.spectra.core.ComputationStrategies;
import se.de.hu_berlin.informatik.spectra.core.INode;

/**
 * Ochiai2 fault localizer $\frac{\EF\NP}{\sqrt{(\EF+\EP)\cdot(\NP+\NF)\cdot(\EF+\NF)\cdot(\EP+\NP)}}$
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Ochiai2<T> extends AbstractFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Ochiai2() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
        final double denom1 = node.getEF(strategy) + node.getEP(strategy);
        final double denom2 = node.getNP(strategy) + node.getNF(strategy);
        final double denom3 = node.getEF(strategy) + node.getNF(strategy);
        final double denom4 = node.getEP(strategy) + node.getNP(strategy);
        
        double numerator = node.getEF(strategy) * node.getNP(strategy);
        if (numerator == 0) {
    		return 0;
    	}
        return numerator / Math.sqrt(denom1 * denom2 * denom3 * denom4);
    }

}
