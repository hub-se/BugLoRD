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
 * Sokal fault localizer
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Sokal<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Sokal() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
    	double numerator = 2.0d * (node.getEF() + node.getNP());
    	if (numerator == 0) {
    		return 0;
    	}
        return numerator / (double)(2.0d * (node.getEF() + node.getNP()) + node.getNF() + node.getEP());
    }

    @Override
    public String getName() {
        return "Sokal";
    }

}
