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
 * HarmonicMean fault localizer 
 * $\frac{(\EF\NP-\NF\EP)\left((\EF+\EP)(\NP+\NF) + (\EF+\NF)(\EP+\NP)\right)}%
 * 		{(\EF+\EP)\cdot(\NP+\NF)\cdot(\EF+\NF)\cdot(\EP+\NP)}$
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class HarmonicMean<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public HarmonicMean() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        final double enu1 = node.getEF() * node.getNP() - node.getNF() * node.getEP();
        final double enu21 = (node.getEF() + node.getEP()) * (node.getNP() + node.getNF());
        final double enu22 = (node.getEF() + node.getNF()) * (node.getEP() + node.getNP());
        final double enu = enu1 * (enu21 + enu22);

        final double denom1 = node.getEF() + node.getEP();
        final double denom2 = node.getNP() + node.getNF();
        final double denom3 = node.getEF() + node.getNF();
        final double denom4 = node.getEP() + node.getNP();
        final double denom = denom1 * denom2 * denom3 * denom4;

        if (enu == 0) {
    		return 0;
    	}
        return enu / denom;
    }

    @Override
    public String getName() {
        return "HarmonicMean";
    }

}
