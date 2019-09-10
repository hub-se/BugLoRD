/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.faultlocalizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.AbstractFaultLocalizer;
import se.de.hu_berlin.informatik.spectra.core.ComputationStrategies;
import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;

/**
 * calculates the suspiciousness with the given fault localizer and adds the average suspiciousness 
 * of all nodes in the neighborhood of the given node (within the same method).
 */
public class NeighborhoodFocusFL<T> extends AbstractFaultLocalizer<T> {

	public static enum Direction {
		FORWARD,
		BACKWARD,
		BOTH
	}
	
	private IFaultLocalizer<SourceCodeBlock> localizer;
	private Map<Integer, Double> suspCache = new HashMap<>();
	private Map<Integer, List<INode<SourceCodeBlock>>> methodMap = new HashMap<>();
	private int neighborHoodSize;
	private Direction direction;

	/**
	 * Create fault localizer
	 */
	public NeighborhoodFocusFL(IFaultLocalizer<SourceCodeBlock> localizer, int neighborHoodSize, Direction direction) {
		super();
		this.localizer = localizer;
		this.neighborHoodSize = neighborHoodSize;
		this.direction = direction;
	}

	@Override
	public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
		@SuppressWarnings("unchecked")
		INode<SourceCodeBlock> castedNode = (INode<SourceCodeBlock>) node;
		List<INode<SourceCodeBlock>> nodes = getNodesInSameMethod(castedNode);
		Collections.sort(nodes, new Comparator<INode<SourceCodeBlock>>() {
			@Override
			public int compare(INode<SourceCodeBlock> o1, INode<SourceCodeBlock> o2) {
				return Integer.compare(o1.getIdentifier().getStartLineNumber(), o2.getIdentifier().getStartLineNumber());
			}
		});
		int index = nodes.indexOf(castedNode);
		if (index >= 0) {
			int count = 0;
			double sum = 0;
			for (int i = 0; i < nodes.size(); ++i) {
				switch (direction) {
				case BOTH:
					if (i >= index - neighborHoodSize && i <= index + neighborHoodSize) {
						sum += getSuspiciousness(strategy, nodes.get(i));
						++count;
					}
					break;
				case FORWARD:
					if (i >= index && i <= index + neighborHoodSize) {
						sum += getSuspiciousness(strategy, nodes.get(i));
						++count;
					}
					break;
				case BACKWARD:
					if (i >= index - neighborHoodSize && i <= index) {
						sum += getSuspiciousness(strategy, nodes.get(i));
						++count;
					}
					break;
				default:
					throw new UnsupportedOperationException("Neighborhood direction unknown!");
				}
					
			}
			return getSuspiciousness(strategy, castedNode) + (sum / count);
		} else {
			return Double.NaN;
		}
	}

	private double getSuspiciousness(ComputationStrategies strategy, INode<SourceCodeBlock> nodeInMethod) {
		Double suspiciousness = suspCache.get(nodeInMethod.getIndex());
		if (suspiciousness == null) {
			suspiciousness = localizer.suspiciousness(nodeInMethod, strategy);
			suspCache.put(nodeInMethod.getIndex(), suspiciousness);
		}
		return suspiciousness;
	}

	private List<INode<SourceCodeBlock>> getNodesInSameMethod(INode<SourceCodeBlock> node) {
		List<INode<SourceCodeBlock>> nodes = methodMap.get(node.getIndex());
		if (nodes == null) {
			nodes = new ArrayList<>();
			SourceCodeBlock identifier = node.getIdentifier();
			for (INode<SourceCodeBlock> iNode : node.getSpectra().getNodes()) {
				if (identifier.getMethodName().equals(iNode.getIdentifier().getMethodName()) &&
						identifier.getFilePath().equals(iNode.getIdentifier().getFilePath())) {
					nodes.add(iNode);
				}
			}
			for (INode<SourceCodeBlock> nodeInMethod : nodes) {
				methodMap.put(nodeInMethod.getIndex(), nodes);
			}
		}
		return nodes;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName() + "-" + direction.toString() + neighborHoodSize + "+" + localizer.getName();
	}
	
}
