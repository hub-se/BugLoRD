/** License information:
 *    Component: sequitur
 *    Package:   de.unisb.cs.st.sequitur.input
 *    Class:     Terminal
 *    Filename:  sequitur/src/main/java/de/unisb/cs/st/sequitur/input/Terminal.java
 *
 * This file is part of the Sequitur library developed by Clemens Hammacher
 * at Saarland University. It has been developed for use in the JavaSlicer
 * tool. See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * Sequitur is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sequitur is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Sequitur. If not, see <http://www.gnu.org/licenses/>.
 */
package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input;

import java.io.IOException;
import java.io.ObjectInputStream;


// package-private
class Terminal extends Symbol {

    private final int value;

    public Terminal(final int value, final int count) {
        super(count);
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    @Override
    public long getLength(final boolean ignoreCount) {
        return ignoreCount ? 1 : this.count;
    }

    @Override
    public String toString() {
        if (this.count == 1)
            return String.valueOf(this.value);

        return new StringBuilder().append(this.value)
            .append('^').append(this.count).toString();
    }

    public static  Terminal readFrom(final ObjectInputStream objIn, final boolean counted,
            final ObjectReader objectReader) throws IOException {
        final int count = counted ? DataInput.readInt(objIn) : 1;
        if (objectReader == null) {
            final int value = DataInput.readInt(objIn);
            return new Terminal(value, count);
        } else {
        	final int value = objectReader.readObject(objIn);
        	return new Terminal(value, count);
        }
    }

}
