/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl;

import se.de.hu_berlin.informatik.faultlocalizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.AbstractFaultLocalizer;
import se.de.hu_berlin.informatik.spectra.core.ComputationStrategies;
import se.de.hu_berlin.informatik.spectra.core.INode;

/**
 * calculates the suspiciousness with the given fault localizers.
 */
public class ScoreCombinationFL<T> extends AbstractFaultLocalizer<T> {

    private IFaultLocalizer<T> localizer1;
    private int divisor1;
    private IFaultLocalizer<T> localizer2;
    private int divisor2;

    /**
     * Create fault localizer
     */
    public ScoreCombinationFL(IFaultLocalizer<T> localizer1, int divisor1,
                              IFaultLocalizer<T> localizer2, int divisor2) {
        super();
        this.localizer1 = localizer1;
        this.divisor1 = divisor1;
        this.localizer2 = localizer2;
        this.divisor2 = divisor2;

    }

    @Override
    public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
        double score1 = localizer1.suspiciousness(node, strategy);
        double score2 = localizer2.suspiciousness(node, strategy);
        if (Double.isNaN(score1) || Double.isNaN(score2)) {
            return Double.NaN;
        } else {
            return score1 / divisor1 + score2 / divisor2;
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName() +
                "(" + localizer1.getName() + (divisor1 != 1 ? "-" + divisor1 : "") + "_" +
                localizer2.getName() + (divisor2 != 1 ? "-" + divisor2 : "") + ")";
    }

}
