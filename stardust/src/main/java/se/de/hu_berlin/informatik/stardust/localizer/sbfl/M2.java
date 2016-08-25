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
 * M2 fault localizer
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class M2<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public M2() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        return new Double(node.getEF())
                / new Double(node.getEF() + node.getNP() + 2.0d * (node.getNF() + node.getEP()));
    }

    @Override
    public String getName() {
        return "M2";
    }

}
