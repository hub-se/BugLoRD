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
 * Rogot2 fault localizer
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Rogot2<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Rogot2() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        final double frac1 = new Double(node.getEF()) / new Double(node.getEF() + node.getEP());
        final double frac2 = new Double(node.getEF()) / new Double(node.getEF() + node.getNF());
        final double frac3 = new Double(node.getNP()) / new Double(node.getNP() + node.getEP());
        final double frac4 = new Double(node.getNP()) / new Double(node.getNP() + node.getNF());
        return 0.25d * (frac1 + frac2 + frac3 + frac4);
    }

    @Override
    public String getName() {
        return "Rogot2";
    }

}
