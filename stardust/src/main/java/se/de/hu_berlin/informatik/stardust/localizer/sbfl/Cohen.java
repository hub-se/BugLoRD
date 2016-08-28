/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.localizer.sbfl;

import se.de.hu_berlin.informatik.stardust.spectra.INode;

/**
 * Cohen fault localizer
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Cohen<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Cohen() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        final double enu1 = 2 * node.getEF() * node.getNP();
        final double enu2 = 2 * node.getNF() * node.getEP();
        final double enu = enu1 - enu2;

        final double denom1 = (node.getEF() + node.getEP()) * (node.getNP() + node.getEP());
        final double denom2 = (node.getEF() + node.getNF()) * (node.getNF() + node.getNP());
        final double denom = denom1 + denom2;
        
        if (enu == 0) {
    		return 0;
    	}
        return enu / denom;
    }

    @Override
    public String getName() {
        return "Cohen";
    }

}
