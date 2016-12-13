/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.spectra;

import java.util.Map;


/**
 * A basic execution trace that additionally provides write access.
 *
 * @param <T>
 *            type used to identify nodes in the system.
 */
public interface IMutableTrace<T> extends ITrace<T> {

    /**
     * Set the involvement of a single node. If no node
     * with the given identifier exists, it will be created.
     *
     * @param identifier
     *            the node to set the involvement for
     * @param involved
     *            true if the node was involved, false otherwise
     */
    public abstract void setInvolvement(T identifier, boolean involved);

    /**
     * Set the involvement of a single node.
     *
     * @param node
     *            the node to set the involvement for
     * @param involved
     *            true if the node was involved, false otherwise
     */
    public abstract void setInvolvement(INode<T> node, boolean involved);

    /**
     * Sets the involvement of multiple nodes belonging to the given set of identifiers
     *
     * @param involvement
     *            a map of node-involvement pairs
     */
    public abstract void setInvolvementForIdentifiers(Map<T, Boolean> involvement);

    /**
     * Sets the involvement of multiple node
     *
     * @param involvement
     *            a map of node-involvement pairs
     */
    public abstract void setInvolvementForNodes(Map<INode<T>, Boolean> involvement);

}