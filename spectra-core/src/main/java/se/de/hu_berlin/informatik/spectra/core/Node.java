/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.core;

/**
 * Represents a single node in a system.
 *
 * @param <T>
 * type used to identify nodes in the system.
 */
public class Node<T> implements INode<T> {
	
	public static enum NodeType {
		NORMAL("N"),
		TRUE_BRANCH("T"),
		FALSE_BRANCH("F"),
		SWITCH_BRANCH("S");
//		SWITCH_DEFAULT_BRANCH("D");
		
		
		private String identifier;
		private NodeType(String identifier) {
			this.identifier = identifier;
		}
		
		@Override
		public String toString() {
			return identifier;
		}
	}

	/** The index of this node */
	private final int index;
	
	/** The identifier of this node */
	private final T identifier;

	/** The spectra this node belongs to */
	private final ISpectra<T,? extends ITrace<T>> spectra;

	/**
	 * Constructs the node
	 *
	 * @param index
	 * the integer index of this node
	 * @param identifier
	 * the identifier of this node
	 * @param spectra
	 * the spectra this node belongs to
	 */
	protected Node(final int index, final T identifier, final ISpectra<T,? extends ITrace<T>> spectra) {
		this.index = index;
		this.identifier = identifier;
		this.spectra = spectra;
	}

	/*
	 * (non-Javadoc)
	 * @see fk.stardust.traces.INode#getIdentifier()
	 */
	@Override
	public T getIdentifier() {
		return this.identifier;
	}
	
	/*
	 * (non-Javadoc)
	 * @see fk.stardust.traces.INode#getIndex()
	 */
	@Override
	public int getIndex() {
		return this.index;
	}

	/*
	 * (non-Javadoc)
	 * @see fk.stardust.traces.INode#getNS()
	 */
	@Override
	public double getNP(ComputationStrategies strategy) {
		return spectra.getLocalizer().getNP(this, strategy);
	}

	/*
	 * (non-Javadoc)
	 * @see fk.stardust.traces.INode#getNF()
	 */
	@Override
	public double getNF(ComputationStrategies strategy) {
		return spectra.getLocalizer().getNF(this, strategy);
	}

	/*
	 * (non-Javadoc)
	 * @see fk.stardust.traces.INode#getIS()
	 */
	@Override
	public double getEP(ComputationStrategies strategy) {
		return spectra.getLocalizer().getEP(this, strategy);
	}

	/*
	 * (non-Javadoc)
	 * @see fk.stardust.traces.INode#getIF()
	 */
	@Override
	public double getEF(ComputationStrategies strategy) {
		return spectra.getLocalizer().getEF(this, strategy);
	}

	@Override
	public void invalidateCachedValues() {
		spectra.getLocalizer().invalidateCachedValues();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return this.identifier.toString();
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + getIdentifier().hashCode();
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Node) {
			Node<?> oNode = (Node<?>) obj;
			return this.getIdentifier().equals(oNode.getIdentifier());
		}
		return false;
	}

	@Override
	public ISpectra<T, ? extends ITrace<T>> getSpectra() {
		return spectra;
	}

}
