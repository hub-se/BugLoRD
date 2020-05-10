/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.provider;

import se.de.hu_berlin.informatik.spectra.core.hit.HitSpectra;

/**
 * Interface used by classes that load or provide spectra objects. Can be used
 * in experiments for automation.
 *
 * @param <T> type used to identify nodes in the system.
 */
public interface IHitSpectraProvider<T> {

    /**
     * Provides a spectra object.
     *
     * @return spectra with traces and nodes
     * @throws IllegalStateException in case providing the spectra fails
     */
    public HitSpectra<T> loadHitSpectra() throws IllegalStateException;

}
