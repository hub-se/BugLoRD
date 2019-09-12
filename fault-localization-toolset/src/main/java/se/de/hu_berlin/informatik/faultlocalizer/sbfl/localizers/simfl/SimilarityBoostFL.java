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
public class SimilarityBoostFL<T> extends AbstractFaultLocalizer<T> {

	private IFaultLocalizer<T> localizer1;
	private int divisor1;
	private IFaultLocalizer<T> localizer2;
	private double power;
	private int divisor2;

	/**
	 * Create fault localizer
	 */
	public SimilarityBoostFL(IFaultLocalizer<T> localizer1, int divisor1, double power, int divisor2) {
		super();
		this.localizer1 = localizer1;
		this.divisor1 = divisor1;
		this.divisor2 = divisor2;
		this.localizer2 = new PwrExtSimilarityFL<>(0.1);
		this.power = power;

	}

	@Override
	public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
		double score1 = localizer1.suspiciousness(node, strategy);
		double score2 = localizer2.suspiciousness(node, strategy); // range between -1 and 1!
		if (Double.isNaN(score1) || Double.isNaN(score2)) {
			return Double.NaN;
		} else {
			return (score1 / divisor1) * (1 + Math.pow(score2 + 1, power) / divisor2);
		}
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName() + 
				"(" + localizer1.getName() + (divisor1 != 1 ? "-" + divisor1 : "") + "_" + 
				String.format("pwr0-1<%d-%d>-%d", (int)power, ((int)(power*10)) % 10, divisor2) + ")";
	}
	
}
