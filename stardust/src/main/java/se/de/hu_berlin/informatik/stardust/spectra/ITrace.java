/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.spectra;


/**
 * A basic execution trace that provides read-only access.
 *
 * @param <T>
 *            type used to identify nodes in the system.
 */
public interface ITrace<T> {

    /**
     * Returns true if the actual execution of the trace was successful and false if an error occured during execution.
     *
     * @return successful
     */
    public boolean isSuccessful();

//    /**
//     * Returns the spectra this trace belongs to.
//     *
//     * @return spectra
//     */
//    public abstract ISpectra<T> getSpectra();

    /**
     * Checks whether the given node is involved in the current trace.
     *
     * @param node
     *            the node to check
     * @return true if it was involved, false otherwise
     */
    public boolean isInvolved(INode<T> node);
    
    /**
     * @return
     * the identifier (usually the test case name) of the trace
     */
    public String getIdentifier();

}