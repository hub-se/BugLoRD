/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.provider;

import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;

/**
 * Interface used by classes that load or provide spectra objects. Can be used in experiments for automation.
 * 
 * @param <T>
 *            type used to identify nodes in the system.
 */
public interface ISpectraProvider<T> {
    /**
     * Provides a spectra object.
     * 
     * @return spectra with traces and nodes
     * @throws Exception
     *             in case providing the spectra fails
     */
    public ISpectra<T> loadSpectra() throws Exception;
}
