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
 * GeometricMean fault localizer
 *
 * @param <T>
 *            type used to identify nodes in the system
 */
public class GeometricMean<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public GeometricMean() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        final double denom1 = node.getEF() + node.getEP();
        final double denom2 = node.getNP() + node.getNF();
        final double denom3 = node.getEF() + node.getNF();
        final double denom4 = node.getEP() + node.getNP();
        
        double numerator = node.getEF() * node.getNP() - node.getNF() * node.getEP();
    	if (numerator == 0) {
    		return 0;
    	}
        return numerator / Math.sqrt(denom1 * denom2 * denom3 * denom4);
    }

    @Override
    public String getName() {
        return "GeometricMean";
    }

}
