/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.core;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 *
 * @param <P>
 *            parent node identifier type
 * @param <C>
 *            child node identifier type
 * @param <K>
 * type of traces
 */
public abstract class AbstractHierarchicalSpectra<P, C, K extends ITrace<P>> extends AbstractSpectra<P,K> {

    /** Holds the child spectra information */
    private final ISpectra<C,?> childSpectra;

    /** Holds the parent->child node relation */
    Map<INode<P>, Set<INode<C>>> relation = new HashMap<>();

    /** Holds a map of all child traces that are mapped to hierarchical traces of this spectra. */
    Map<ITrace<C>, K> traceMap = new HashMap<>();

//    /**
//     * Creates a new parent spectra object.
//     *
//     * @param childSpectra
//     *            the child spectra to fetch involvement information from
//     */
//    public AbstractHierarchicalSpectra(final ISpectra<C,?> childSpectra) {
//        super(null);
//        this.childSpectra = childSpectra;
//    }
    
    public AbstractHierarchicalSpectra(final ISpectra<C,?> childSpectra, Path spectraZipFile) {
    	super(spectraZipFile);
        this.childSpectra = childSpectra;
    }

    @Override
	public INode<P> getOrCreateNode(P identifier) {
    	INode<P> node = super.getOrCreateNode(identifier);
    	//need to invalidate cached values, possibly
    	node.invalidateCachedValues();
		return node;
	}

	/**
     * Adds childNode as child of parentNode to this hierarchical spectra.
     *
     * @param parentIdentifier
     *            the parent node
     * @param childIdentifier
     *            the child node to be added under the parent node
     */
    public void setParent(final P parentIdentifier, final C childIdentifier) {
        this.setParent(this.getOrCreateNode(parentIdentifier), this.childSpectra.getOrCreateNode(childIdentifier));
    }

    /**
     * Adds childNode as child of parentNode to this hierarchical spectra.
     *
     * @param parentNode
     *            the parent node
     * @param childNode
     *            the child node to be added under the parent node
     */
    public void setParent(final INode<P> parentNode, final INode<C> childNode) {
        this.childrenOf(parentNode).add(childNode);
    }

    /**
     * Returns all children of the parent node.
     *
     * @param parent
     *            the parent node to fetch the children of
     * @return all children of the parent
     */
    private Set<INode<C>> childrenOf(final INode<P> parent) {
        if (!this.relation.containsKey(parent)) {
            this.relation.put(parent, new HashSet<INode<C>>());
        }
        return this.relation.get(parent);
    }

    /**
     * Returns all children of the given parent node.
     *
     * The returned set of children is not modifiable.
     *
     * @param parent
     *            the parent node to fetch the children of
     * @return all children of the parent
     */
    public Set<INode<C>> getChildrenOf(final INode<P> parent) {
        return Collections.unmodifiableSet(this.childrenOf(parent));
    }

    @Override
    public Collection<K> getTraces() {
        // if not yet stored add hierarchical traces for all available child traces to this spectra
        if (this.traceMap.size() != this.childSpectra.getTraces().size()) {
            for (final ITrace<C> childTrace : this.childSpectra.getTraces()) {
                if (!this.traceMap.containsKey(childTrace)) {
                    this.traceMap.put(childTrace, createNewHierarchicalTrace(this, childTrace));
                }
            }
        }

        // Due to some reason no direct cast from Collection<HierarchicalTrace> to Collection<ITrace<P>> is possible
        final Collection<K> hierarchicalTraces = new ArrayList<>();
        for (final K trace : this.traceMap.values()) {
            hierarchicalTraces.add(trace);
        }
        return hierarchicalTraces;
    }

    protected abstract K createNewHierarchicalTrace(AbstractHierarchicalSpectra<P, C, K> abstractHierarchicalSpectra,
			ITrace<C> childTrace);

	/**
     * Returns the child spectra of this hierarchical spectra.
     *
     * @return child spectra
     */
    public ISpectra<C,?> getChildSpectra() {
        return this.childSpectra;
    }

	@Override
	public K createNewTrace(String identifier, int traceIndex, boolean successful) {
		throw new IllegalStateException("Cannot add new trace in hierarchical spectra");
	}
}
