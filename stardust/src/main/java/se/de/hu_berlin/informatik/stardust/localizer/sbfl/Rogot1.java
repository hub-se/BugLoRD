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
 * Rogot1 fault localizer
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Rogot1<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Rogot1() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        final double left = (double)node.getEF() / (double)(2.0d * node.getEF() + node.getNF() + node.getEP());
        final double right = (double)node.getNP() / (double)(2.0d * node.getNP() + node.getNF() + node.getEP());
        return 0.5d * (left + right);
    }

    @Override
    public String getName() {
        return "Rogot1";
    }

}
