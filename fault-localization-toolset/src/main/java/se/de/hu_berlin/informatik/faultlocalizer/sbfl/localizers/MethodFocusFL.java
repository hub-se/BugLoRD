/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import se.de.hu_berlin.informatik.faultlocalizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.AbstractFaultLocalizer;
import se.de.hu_berlin.informatik.spectra.core.ComputationStrategies;
import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;

/**
 * calculates the suspiciousness with the given fault localizer and adds the average suspiciousness of all nodes in the same method.
 */
public class MethodFocusFL<T> extends AbstractFaultLocalizer<T> {

	private IFaultLocalizer<SourceCodeBlock> localizer;
	private Map<Integer, Double> suspCache = new HashMap<>();

	/**
	 * Create fault localizer
	 */
	public MethodFocusFL(IFaultLocalizer<SourceCodeBlock> localizer) {
		super();
		this.localizer = localizer;
	}

	@Override
	public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
		@SuppressWarnings("unchecked")
		INode<SourceCodeBlock> castedNode = (INode<SourceCodeBlock>) node;
		Collection<INode<SourceCodeBlock>> nodes = getNodesInSameMethod(castedNode);
		double sum = 0;
		for (INode<SourceCodeBlock> nodeInMethod : nodes) {
			sum += getSuspiciousness(strategy, nodeInMethod);
		}
		return getSuspiciousness(strategy, castedNode) + (sum / nodes.size());
	}

	private double getSuspiciousness(ComputationStrategies strategy, INode<SourceCodeBlock> nodeInMethod) {
		Double suspiciousness = suspCache.get(nodeInMethod.getIndex());
		if (suspiciousness == null) {
			suspiciousness = localizer.suspiciousness(nodeInMethod, strategy);
			suspCache.put(nodeInMethod.getIndex(), suspiciousness);
		}
		return suspiciousness;
	}

	private Collection<INode<SourceCodeBlock>> getNodesInSameMethod(INode<SourceCodeBlock> node) {
		Collection<INode<SourceCodeBlock>> nodes = new ArrayList<>();
		SourceCodeBlock identifier = node.getIdentifier();
		for (INode<SourceCodeBlock> iNode : node.getSpectra().getNodes()) {
			if (identifier.getMethodName().equals(iNode.getIdentifier().getMethodName()) &&
					identifier.getFilePath().equals(iNode.getIdentifier().getFilePath())) {
				nodes.add(iNode);
			}
		}
		return nodes;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName() + "+" + localizer.getName();
	}
	
}
