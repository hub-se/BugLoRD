/**
 * License information:
 * Component: sequitur
 * Package:   de.unisb.cs.st.sequitur.output
 * Class:     Terminal
 * Filename:  sequitur/src/main/java/de/unisb/cs/st/sequitur/output/Terminal.java
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
package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.output;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.output.Rule.Dummy;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Queue;

// package-private
class Terminal extends Symbol {

    protected final int value;

    public Terminal(final int value) {
        this(value, 1);
    }

    public Terminal(final int value, final int count) {
        super(count);
        this.value = value;
    }

    @Override
    public boolean meltDigram(final Grammar grammar) {
        if (this.next instanceof Terminal) {
            final Terminal otherT = (Terminal) this.next;
            // check if both terminals are the same value
            if (this.value == Symbol.NULL_VALUE ? otherT.value == Symbol.NULL_VALUE : this.value == otherT.value) {
                final boolean hasPrev = !(this.prev instanceof Dummy);
                final boolean hasNextNext = !(otherT.next instanceof Dummy);
                if (hasPrev)
                    grammar.removeDigram(this.prev);
                if (hasNextNext)
                    grammar.removeDigram(otherT);
                this.count += otherT.count;
                otherT.remove();
                if (hasPrev)
                    grammar.checkDigram(this.prev);
                if (hasNextNext)
                    grammar.checkDigram(this);
                return true;
            }
        }
        return false;
    }

    @Override
    public int getHeader() {
        assert this.count >= 1;
        return this.count == 1 ? 2 : 3;
    }

    @Override
    public void writeOut(final ObjectOutputStream objOut, final Grammar grammar,
                         final ObjectWriter objectWriter,
                         final Queue<Rule> queue) throws IOException {
        assert this.count >= 1;
        if (this.count != 1) {
            DataOutput.writeInt(objOut, this.count);
        }
        if (objectWriter == null)
            DataOutput.writeInt(objOut, this.value);
        else
            objectWriter.writeObject(this.value, objOut);
    }

    @Override
    protected boolean singleEquals(final Symbol obj) {
        if (obj.getClass() != this.getClass())
            return false;
        final Terminal other = (Terminal) obj;
        return this.count == other.count
                && (this.value == Symbol.NULL_VALUE ? other.value == Symbol.NULL_VALUE : this.value == other.value);
    }

    @Override
    protected int singleHashcode() {
        return (this.value == Symbol.NULL_VALUE ? 0 : this.value) + 31 * this.count;
    }

    @Override
    public String toString() {
        assert this.count >= 1;
        if (this.count == 1)
            return String.valueOf(this.value);

        return new StringBuilder().append(this.value)
                .append('^').append(this.count).toString();
    }

}
