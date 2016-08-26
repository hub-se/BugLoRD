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
 * Simple Matching fault localizer
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
    public double suspiciousness(final INode<T> node) {
        return (double)(node.getEF() + node.getNP())
                / (double)(node.getEF() + node.getNF() + node.getEP() + node.getNP());
    }

    @Override
    public String getName() {
        return "SimpleMatching";
    }

}
