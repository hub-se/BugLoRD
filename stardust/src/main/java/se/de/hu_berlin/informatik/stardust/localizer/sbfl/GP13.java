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
 * GP13 (genetic algorithm derived formula) fault localizer
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class GP13<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public GP13() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        return new Double(node.getEF()) * (1.0 + 1.0 / new Double(2*node.getEP() + node.getEF()));
    }

    @Override
    public String getName() {
        return "gp13";
    }

}
