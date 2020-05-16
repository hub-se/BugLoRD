/**
 * License information:
 * Component: sequitur
 * Package:   de.unisb.cs.st.sequitur.output
 * Class:     SharedOutputGrammar
 * Filename:  sequitur/src/main/java/de/unisb/cs/st/sequitur/output/SharedOutputGrammar.java
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

import java.io.IOException;
import java.io.ObjectOutputStream;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;

@CoverageIgnore
public class SharedOutputGrammar {

    protected final Grammar grammar;
    protected final ObjectWriter objectWriter;

    public SharedOutputGrammar() {
        this(null);
    }

    public SharedOutputGrammar(final ObjectWriter objectWriter) {
        this(new Grammar(), objectWriter);
    }

    protected SharedOutputGrammar(final Grammar grammar, final ObjectWriter objectWriter) {
        if (grammar == null)
            throw new NullPointerException();
        this.grammar = grammar;
        this.objectWriter = objectWriter;
    }

    public void writeOut(final ObjectOutputStream objOut) throws IOException {
        this.grammar.writeOut(objOut, this.objectWriter);
    }

    @Override
    public String toString() {
        return this.grammar.toString();
    }

}
