/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.provider;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;

/**
 * Interface used by classes that load or provide spectra objects. Can be used
 * in experiments for automation.
 * 
 * @param <T>
 * type used to identify nodes in the system.
 * @param <K>
 * type of traces in the spectra
 */
public interface ISpectraProvider<T, K extends ITrace<T>> {

	/**
	 * Provides a spectra object.
	 * 
	 * @return spectra with traces and nodes
	 * @throws IllegalStateException
	 * in case providing the spectra fails
	 */
	public ISpectra<T, ? super K> loadSpectra() throws IllegalStateException;

}
