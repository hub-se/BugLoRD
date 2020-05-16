/**
 * License information:
 * Component: sequitur
 * Package:   de.unisb.cs.st.sequitur.input
 * Class:     Symbol
 * Filename:  sequitur/src/main/java/de/unisb/cs/st/sequitur/input/Symbol.java
 * <p>
 * This file is part of the Sequitur library developed by Clemens Hammacher
 * at Saarland University. It has been developed for use in the JavaSlicer
 * tool. See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 * <p>
 * Sequitur is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Sequitur is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Sequitur. If not, see <http://www.gnu.org/licenses/>.
 */
package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;

// package-private
@CoverageIgnore
abstract class Symbol {

    protected final int count;

    protected Symbol(final int count) {
        assert count > 0;
        this.count = count;
    }

    public abstract long getLength(boolean ignoreCount);

}
