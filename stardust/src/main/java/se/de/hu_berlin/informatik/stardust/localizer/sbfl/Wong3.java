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
 * Wong3 fault localizer
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Wong3<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Wong3() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        if (node.getEP() <= 2) {
            return node.getEF() - node.getEP();
        } else if (node.getEP() <= 10) {
            return (double)node.getEF() - (2.0d + 0.1d * ((double)node.getEP() - 2.0d));
        } else {
            return (double)node.getEF() - (2.8d + 0.001d * ((double)node.getEP() - 10.0d));
        }
    }

    @Override
    public String getName() {
        return "Wong3";
    }

}
