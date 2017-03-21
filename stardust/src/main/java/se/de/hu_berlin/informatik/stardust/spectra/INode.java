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
 * Represents a node in the system.
 *
 * @param <T>
 *            type used to identify nodes in the system
 */
public interface INode<T> {

	public enum CoverageType {
		/** EP + EF == 0 */
		NOT_EXECUTED,
		/** EP + EF &gt; 0 */
		EXECUTED,
		/** EP == 0 */
		EP_EQUALS_ZERO,
		/** EP &gt; 0 */
		EP_GT_ZERO,
		/** EF == 0 */
		EF_EQUALS_ZERO,
		/** EF &gt; 0 */
		EF_GT_ZERO,
		/** NP == 0 */
		NP_EQUALS_ZERO,
		/** NP &gt; 0 */
		NP_GT_ZERO,
		/** NF == 0 */
		NF_EQUALS_ZERO,
		/** NF &gt; 0 */
		NF_GT_ZERO;
		
		@Override
		public String toString() {
			switch(this) {
			case EF_EQUALS_ZERO:
				return "ef_eq_zero";
			case EF_GT_ZERO:
				return "ef_gt_zero";
			case EP_EQUALS_ZERO:
				return "ep_eq_zero";
			case EP_GT_ZERO:
				return "ep_gt_zero";
			case EXECUTED:
				return "executed";
			case NF_EQUALS_ZERO:
				return "nf_eq_zero";
			case NF_GT_ZERO:
				return "nf_gt_zero";
			case NOT_EXECUTED:
				return "not_executed";
			case NP_EQUALS_ZERO:
				return "np_eq_zero";
			case NP_GT_ZERO:
				return "np_gt_zero";
			default:
				throw new UnsupportedOperationException("Not implemented.");
			}
		}
	}

    /**
     * Returns the identifier for this node
     *
     * @return the identifier
     */
    public abstract T getIdentifier();

    /**
     * Returns the spectra this node belongs to
     *
     * @return spectra
     */
    public abstract ISpectra<T> getSpectra();

    /**
     * Returns the amount of traces this node was not involved in, but passed.
     *
     * @return amount of traces in spectra
     */
    public abstract int getNP();

    /**
     * Returns the amount of traces this node was not involved in and failed.
     *
     * @return amount of traces in spectra
     */
    public abstract int getNF();

    /**
     * Returns the amount of traces where this node was executed and which passed.
     *
     * @return amount of traces in spectra
     */
    public abstract int getEP();

    /**
     * Returns the amount of traces where this node was executed and which failed.
     *
     * @return amount of traces in spectra
     */
    public abstract int getEF();

    /**
     * Display node identifier as string
     *
     * @return identifying string for this node
     */
    @Override
    public abstract String toString();

}