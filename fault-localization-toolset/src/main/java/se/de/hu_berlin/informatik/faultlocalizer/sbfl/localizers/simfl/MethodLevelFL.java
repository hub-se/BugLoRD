/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl;

import java.util.HashMap;
import java.util.Map;

import se.de.hu_berlin.informatik.faultlocalizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.AbstractFaultLocalizer;
import se.de.hu_berlin.informatik.spectra.core.ComputationStrategies;
import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.hit.HitTrace;
import se.de.hu_berlin.informatik.spectra.util.SpectraUtils;

/**
 * calculates the suspiciousness with the given fault localizer on method level 
 * (but assigns the suspiciousness score to all statements of the method).
 */
public class MethodLevelFL<T> extends AbstractFaultLocalizer<T> {

	private IFaultLocalizer<SourceCodeBlock> localizer;
	private Map<Integer, Double> suspCache = new HashMap<>();
	
	private ISpectra<SourceCodeBlock, ? super HitTrace<SourceCodeBlock>> methodLevelSpectra;

	/**
	 * Create fault localizer
	 */
	public MethodLevelFL(IFaultLocalizer<SourceCodeBlock> localizer) {
		super();
		this.localizer = localizer;
	}

	@Override
	public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
		@SuppressWarnings("unchecked")
		INode<SourceCodeBlock> castedNode = (INode<SourceCodeBlock>) node;
		
		if (methodLevelSpectra == null) {
			methodLevelSpectra = SpectraUtils.createMethodLevelSpectrum(castedNode.getSpectra());
		}
		
		INode<SourceCodeBlock> methodNode = getCorrespondingMethodNode(castedNode);
		if (methodNode != null) {
			return getSuspiciousness(strategy, methodNode);
		} else {
			return Double.NaN;
		}
	}

	private double getSuspiciousness(ComputationStrategies strategy, INode<SourceCodeBlock> methodNode) {
		Double suspiciousness = suspCache.get(methodNode.getIndex());
		if (suspiciousness == null) {
			suspiciousness = localizer.suspiciousness(methodNode, strategy);
			suspCache.put(methodNode.getIndex(), suspiciousness);
		}
		return suspiciousness;
	}

	private INode<SourceCodeBlock> getCorrespondingMethodNode(INode<SourceCodeBlock> node) {
		SourceCodeBlock identifier = node.getIdentifier();
		for (INode<SourceCodeBlock> methodNode : methodLevelSpectra.getNodes()) {
			if (identifier.getMethodName().equals(methodNode.getIdentifier().getMethodName()) &&
					identifier.getFilePath().equals(methodNode.getIdentifier().getFilePath())) {
				return methodNode;
			}
		}

		return null;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName() + "+" + localizer.getName();
	}
	
}
