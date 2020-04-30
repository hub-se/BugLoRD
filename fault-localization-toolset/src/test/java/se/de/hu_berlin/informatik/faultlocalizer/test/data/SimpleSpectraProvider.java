/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.faultlocalizer.test.data;

import se.de.hu_berlin.informatik.spectra.core.hit.HitSpectra;
import se.de.hu_berlin.informatik.spectra.core.hit.HitTrace;
import se.de.hu_berlin.informatik.spectra.provider.IHitSpectraProvider;

/**
 * Provides a small and simple spectra for testing.
 * <p>
 * Test data taken from Table 1 from:
 * <p>
 * Lee Naish, Hua Jie Lee, and Kotagiri Ramamohanarao. 2011. A model for spectra-based software diagnosis. ACM
 * Trans. Softw. Eng. Methodol. 20, 3, Article 11 (August 2011), 32 pages. DOI=10.1145/2000791.2000795
 * http://doi.acm.org/10.1145/2000791.2000795
 *
 * @author Fabian Keller 'dev@fabian-keller.de'
 * @see {@linktourl http://dl.acm.org/citation.cfm?id=2000795}
 */
public class SimpleSpectraProvider implements IHitSpectraProvider<String> {

    @Override
    public HitSpectra<String> loadHitSpectra() throws IllegalStateException {
        final HitSpectra<String> s = new HitSpectra<>(null);

        final HitTrace<String> t1 = s.addTrace("t1", 1, false);
        t1.setInvolvement("S1", true);
        t1.setInvolvement("S2", true);

        final HitTrace<String> t2 = s.addTrace("t2", 2, false);
        t2.setInvolvement("S2", true);
        t2.setInvolvement("S3", true);

        final HitTrace<String> t3 = s.addTrace("t3", 3, true);
        t3.setInvolvement("S1", true);

        final HitTrace<String> t4 = s.addTrace("t4", 4, true);
        t4.setInvolvement("S1", true);
        t4.setInvolvement("S2", true);
        t4.setInvolvement("S3", true);

        final HitTrace<String> t5 = s.addTrace("t5", 5, true);
        t5.setInvolvement("S1", true);
        t5.setInvolvement("S3", true);

        return s;
    }

}
