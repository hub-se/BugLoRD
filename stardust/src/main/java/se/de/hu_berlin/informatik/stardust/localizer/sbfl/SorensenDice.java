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
 * Sorensen-Dice fault localizer $\frac{2\EF}{2\EF+\NF+\EP}$
 *
 * @param <T>
 *            type used to identify nodes in the system
 */
public class SorensenDice<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public SorensenDice() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
    	double numerator = 2.0d * node.getEF();
    	if (numerator == 0) {
    		return 0;
    	}
        return numerator / (double)(2.0d * node.getEF() + node.getNF() + node.getEP());
    }

    @Override
    public String getName() {
        return "SorensenDice";
    }

}
