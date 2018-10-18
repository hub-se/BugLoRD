package se.de.hu_berlin.informatik.aspectj.frontend.evaluation;

import se.de.hu_berlin.informatik.spectra.core.hit.HitTrace;
import se.de.hu_berlin.informatik.spectra.provider.ISpectraProvider;

/**
 * Factories a spectra provider
 *
 * @param <T>
 *            node identifier type
 */
public interface ISpectraProviderFactory<T> {

    /**
     * Create spectra provider
     *
     * @param bugId
     *            the bug ID to load
     * @return provider
     */
    public ISpectraProvider<T, HitTrace<T>> factory(int bugId);
}
