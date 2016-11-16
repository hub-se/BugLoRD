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
 * Scott fault localizer $\frac{4\EF\NP - 4\NF\EP - (\NF-\EP)^2}{(2\EF+\NF+\EP)\cdot(2\NP+\NF+\EP)}$
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Scott<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Scott() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        final double enu1 = 4.0d * node.getEF() * node.getNP();
        final double enu2 = 4.0d * node.getNF() * node.getEP();
        final double enu3 = node.getNF() - node.getEP();
        final double enu = enu1 - enu2 - (enu3 * enu3);

        final double denom1 = 2.0d * node.getEF() + node.getNF() + node.getEP();
        final double denom2 = 2.0d * node.getNP() + node.getNF() + node.getEP();
        final double denom = denom1 * denom2;

        if (enu == 0) {
    		return 0;
    	}
        return enu / denom;
    }

    @Override
    public String getName() {
        return "Scott";
    }

}