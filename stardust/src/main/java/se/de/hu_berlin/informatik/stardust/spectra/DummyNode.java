package se.de.hu_berlin.informatik.stardust.spectra;

import se.de.hu_berlin.informatik.stardust.localizer.sbfl.AbstractSpectrumBasedFaultLocalizer.ComputationStrategies;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.ILocalizer;

/**
 * Represents a single node in a system.
 *
 * @param <T>
 * type used to identify nodes in the system.
 */
public class DummyNode<T> implements INode<T> {

	/** The identifier of this node */
	private final T identifier;
	private ILocalizer<T> localizer;

	/**
	 * Constructs the node
	 *
	 * @param identifier
	 * the identifier of this node
	 * @param localizer
	 * the localizer to use
	 */
	public DummyNode(final T identifier, ILocalizer<T> localizer) {
		this.identifier = identifier;
		this.localizer = localizer;
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
	 * @see fk.stardust.traces.INode#getNS()
	 */
	@Override
	public double getNP(ComputationStrategies strategy) {
		return localizer.getNP(this, strategy);
	}

	/*
	 * (non-Javadoc)
	 * @see fk.stardust.traces.INode#getNF()
	 */
	@Override
	public double getNF(ComputationStrategies strategy) {
		return localizer.getNF(this, strategy);
	}

	/*
	 * (non-Javadoc)
	 * @see fk.stardust.traces.INode#getIS()
	 */
	@Override
	public double getEP(ComputationStrategies strategy) {
		return localizer.getEP(this, strategy);
	}

	/*
	 * (non-Javadoc)
	 * @see fk.stardust.traces.INode#getIF()
	 */
	@Override
	public double getEF(ComputationStrategies strategy) {
		return localizer.getEF(this, strategy);
	}

	@Override
	public void invalidateCachedValues() {
		localizer.invalidateCachedValues();
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
		if (obj instanceof DummyNode) {
			DummyNode<?> oNode = (DummyNode<?>) obj;
			if (!this.getIdentifier().equals(oNode.getIdentifier())) {
				return false;
			}
			return true;
		}
		return false;
	}

}
