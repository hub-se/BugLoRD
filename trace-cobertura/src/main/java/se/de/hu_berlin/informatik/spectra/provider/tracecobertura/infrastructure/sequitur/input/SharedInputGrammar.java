/** License information:
 *    Component: sequitur
 *    Package:   de.unisb.cs.st.sequitur.input
 *    Class:     SharedInputGrammar
 *    Filename:  sequitur/src/main/java/de/unisb/cs/st/sequitur/input/SharedInputGrammar.java
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

public class SharedInputGrammar {

    protected final Grammar grammar;

    protected SharedInputGrammar(final Grammar grammar) {
        if (grammar == null)
            throw new NullPointerException();
        this.grammar = grammar;
    }

    public static SharedInputGrammar readFrom(final ObjectInputStream objIn) throws IOException {
        return new SharedInputGrammar(Grammar.readFrom(objIn, null));
    }

    public static  SharedInputGrammar readFrom(final ObjectInputStream objIn,
            final ObjectReader objectReader) throws IOException {
        return new SharedInputGrammar(Grammar.readFrom(objIn, objectReader));
    }

}
