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
 * Euclid fault localizer
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Euclid<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Euclid() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        return Math.sqrt(new Double(node.getIF() + node.getNS()));
    }

    @Override
    public String getName() {
        return "Euclid";
    }

}
