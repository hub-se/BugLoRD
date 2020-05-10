package se.de.hu_berlin.informatik.spectra.core;

/**
 * Represents a single node in a system.
 *
 * @param <T> type used to identify nodes in the system.
 */
public class DummyNode<T> implements INode<T> {

    /**
     * The index of this node
     */
    private final int index;
    /**
     * The identifier of this node
     */
    private final T identifier;
    private final ILocalizerCache<T> localizer;

    /**
     * Constructs the node
     *
     * @param index      the integer index of this node
     * @param identifier the identifier of this node
     * @param localizer  the localizer to use
     */
    public DummyNode(final int index, final T identifier, ILocalizerCache<T> localizer) {
        this.index = index;
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
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DummyNode) {
            DummyNode<?> oNode = (DummyNode<?>) obj;
            return this.getIdentifier().equals(oNode.getIdentifier());
        }
        return false;
    }

    @Override
    public ISpectra<T, ? extends ITrace<T>> getSpectra() {
        throw new UnsupportedOperationException();
    }

}
