/** License information:
 *    Component: sequitur
 *    Package:   de.unisb.cs.st.sequitur.input
 *    Class:     Grammar
 *    Filename:  sequitur/src/main/java/de/unisb/cs/st/sequitur/input/Grammar.java
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

import de.hammacher.util.LongArrayList;

// package-private
class Grammar {

    private final LongArrayList<Rule> rules;

    protected Grammar(final LongArrayList<Rule> rules) {
        this.rules = rules;
    }

    public static  Grammar readFrom(final ObjectInputStream objIn, final ObjectReader objectReader) 
    		throws IOException {
        final LongArrayList<Rule> rules = Rule.readAll(objIn, objectReader);
        final Grammar grammar = new Grammar(rules);
        for (final Rule rule: rules)
            rule.substituteRealRules(grammar);
        boolean ready = false;
        while (!ready) {
            ready = true;
            for (final Rule rule: rules)
                if (!rule.computeLength())
                    ready = false;
        }
        return grammar;
    }

    public Rule getRule(final long ruleNr) {
        if (ruleNr < 0 || ruleNr >= this.rules.longSize())
            return null;
        return this.rules.get(ruleNr);
    }

}
