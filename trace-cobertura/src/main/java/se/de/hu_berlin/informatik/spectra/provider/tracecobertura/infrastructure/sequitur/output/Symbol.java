/**
 * License information:
 * Component: sequitur
 * Package:   de.unisb.cs.st.sequitur.output
 * Class:     Symbol
 * Filename:  sequitur/src/main/java/de/unisb/cs/st/sequitur/output/Symbol.java
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
import java.util.Queue;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;

// package-private
@CoverageIgnore
abstract class Symbol implements Cloneable {

    protected static final int NULL_VALUE = -1;

    public Symbol next = null;
    public Symbol prev = null;
    protected int count;

    protected Symbol(final int count) {
        this.count = count;
    }

    public int getCount() {
        return this.count;
    }

    /**
     * Inserts the given Symbol before this Symbol in the implicit linked list.
     * Does <b>not</b> check any invariants or manipulate the grammar.
     *
     * @param toInsert the new Symbol to insert
     */
    public void insertBefore(final Symbol toInsert) {
        linkTogether(this.prev, toInsert);
        linkTogether(toInsert, this);
    }

    protected static void linkTogether(final Symbol first, final Symbol second) {
        first.next = second;
        second.prev = first;
    }

    /**
     * Replace this symbol with a non-terminal representing the given rule.
     */
    public void substituteDigram(final Rule rule, final Grammar grammar) {
        grammar.removeDigram(this.prev);
        grammar.removeDigram(this);
        grammar.removeDigram(this.next);
        this.remove(); // this.next is still intact
        this.next.remove(); // this.next.next is still intact
        final NonTerminal newSymbol = new NonTerminal(rule);
        this.next.next.insertBefore(newSymbol);

        // if the digram starting at the preceeding symbol is substituted, then
        // the digram starting at this symbol is already checked
        if (!grammar.checkDigram(newSymbol.prev))
            grammar.checkDigram(newSymbol);
    }

    /**
     * Removes this symbol from the implicit linked list.
     * Does no checking of digrams or something else.
     */
    public void remove() {
        linkTogether(this.prev, this.next);
        this.count = 0;
    }

    /**
     * Tries to melt this symbol with its successor. Only possible if the successor is equal
     * to this symbol.
     * In that case, the count for this symbol is increased by the count of the successor and
     * the successor is removed.
     *
     * @return whether this symbol could be melted with it's successor
     */
    public abstract boolean meltDigram(final Grammar grammar);

    // return a 2-bit header for this symbol
    public abstract int getHeader();

    private int digramHashcode() {
        return this.next == this ? 32 * singleHashcode()
                : (singleHashcode() + 31 * this.next.singleHashcode());
    }

    @Override
    protected Symbol clone() {
        try {
            return (Symbol) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new RuntimeException("Symbol should be clonable", e);
        }
    }

    protected abstract int singleHashcode();

    private boolean digramEquals(final Symbol obj) {
        return singleEquals(obj) && this.next.singleEquals(obj.next);
    }

    protected abstract boolean singleEquals(Symbol obj);

    /*
     * WARNING: hashCode() returns a hashCode not only for this symbol, but for the
     * digram of this and the next symbol!
     */
    @Override
    public int hashCode() {
        return digramHashcode();
    }

    /*
     * WARNING: equals() does not only check for equality of the two symbols, but also
     * for the two successor symbols!
     */
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Symbol ? digramEquals((Symbol) obj) : false;
    }

    public abstract void writeOut(final ObjectOutputStream objOut, Grammar grammar,
                                  ObjectWriter objectWriter, Queue<Rule> queue)
            throws IOException;

}
