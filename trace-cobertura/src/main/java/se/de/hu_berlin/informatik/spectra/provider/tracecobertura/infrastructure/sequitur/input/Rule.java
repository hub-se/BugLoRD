/** License information:
 *    Component: sequitur
 *    Package:   de.unisb.cs.st.sequitur.input
 *    Class:     Rule
 *    Filename:  sequitur/src/main/java/de/unisb/cs/st/sequitur/input/Rule.java
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

import gnu.trove.impl.Constants;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import de.hammacher.util.LongArrayList;
import de.hammacher.util.LongHolder;
import de.hammacher.util.streams.MyByteArrayInputStream;

// package-private
class Rule {

    protected final Symbol[] symbols;
    private long length;
    private long[] positionAfter = null;

    protected Rule(Symbol[] symbols) {
        this.symbols = symbols;
    }

    public void substituteRealRules(final Grammar grammar) {
        for (int i = this.symbols.length - 1; i >= 0; --i) {
            Symbol sym = this.symbols[i];
            if (sym instanceof NonTerminal)
                this.symbols[i] = ((NonTerminal)sym).substituteRealRules(grammar);
        }
    }

    public long getLength() {
        return this.length;
    }

    protected boolean computeLength() {
        if (this.length != 0)
            return true;
        long len = 0;
        for (final Symbol sym: this.symbols) {
            final long symLen = sym.getLength(false);
            if (symLen == 0)
                return false;
            len += symLen;
        }
        this.length = len;
        return true;
    }

    public TObjectLongMap<Rule> getUsedRules() {
        TObjectLongMap<Rule> rules = new TObjectLongHashMap<Rule>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);

        List<Rule> iteratedList = new ArrayList<Rule>(32);
        List<Rule> newList = new ArrayList<Rule>(32);

        iteratedList.add(this);

        do {
            for (Rule rule : iteratedList) {
                for (Symbol sym: rule.symbols) {
                    if (sym instanceof NonTerminal) {
                        Rule newRule = ((NonTerminal)sym).getRule();
                        if (rules.adjustOrPutValue(newRule, 1, 1) == 1) // increase counter. new rule?
                            newList.add(newRule);
                    }
                }
            }
            List<Rule> nextNewList = iteratedList;
            iteratedList = newList;
            newList = nextNewList;
            newList.clear();
        } while (!iteratedList.isEmpty());

        return rules;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("R").append(hashCode()).append(" -->");
        for (final Symbol sym: this.symbols)
            sb.append(' ').append(sym);
        return sb.toString();
    }

    public static  LongArrayList<Rule> readAll(final ObjectInputStream objIn,
            final ObjectReader objectReader) throws IOException, ClassNotFoundException {

        final LongArrayList<Rule> rules = new LongArrayList<Rule>();
        readRules:
        while (true) {
            int header = objIn.read();
            int length;
            switch (header >> 6) {
            case 0:
                break readRules;
            case 1:
                length = DataInput.readInt(objIn);
                break;
            case 2:
                length = 2;
                break;
            case 3:
                length = 3;
                break;
            default:
                if (header == -1)
                    throw new IOException("Unexpected EOF");
                throw new IOException("Corrupted data");
            }
            final int additionalHeaderBytes = length / 4;
            final MyByteArrayInputStream headerInputStream;
            if (additionalHeaderBytes == 0) {
                headerInputStream = null;
            } else {
                // maximum rule length == 1 << 30, to fit in arraylist
                if (additionalHeaderBytes > 1 << 28)
                    throw new IOException("Rule longer than 1<<30??");
                final byte[] headerBuf = new byte[additionalHeaderBytes];
                objIn.readFully(headerBuf);
                headerInputStream = new MyByteArrayInputStream(headerBuf);
            }

            Symbol[] symbols = (Symbol[]) new Symbol[length];
            int pos = 3;
            for (int i = 0; i < length; ++i) {
                if (pos-- == 0) {
                    assert headerInputStream != null;
                    header = headerInputStream.read();
                    pos = 3;
                }
                switch ((header >> (2*pos)) & 3) {
                case 0:
                    symbols[i] = NonTerminal.readFrom(objIn, false);
                    break;
                case 1:
                    symbols[i] = NonTerminal.readFrom(objIn, true);
                    break;
                case 2:
                    symbols[i] = Terminal.readFrom(objIn, false, objectReader);
                    break;
                case 3:
                    symbols[i] = Terminal.readFrom(objIn, true, objectReader);
                    break;
                default:
                    throw new InternalError();
                }
            }
            rules.add(new Rule(symbols));
        }

        rules.trimToSize();
        return rules;
    }

    /**
     * Returns the maximum offset (= index of a symbol in this rule), s.t. the number of
     * symbols before this offset is smaller or equal to the given position.
     *
     * @param position the position to determine the offset for
     * @param positionHolder an object where the position of the symbol at the returned
     *                    offset is stored (may be <code>null</code>)
     * @return the maximum offset whose position is smaller or equal to the given position
     */
    public int findOffset(final long position, final LongHolder positionHolder) {
        if (this.symbols.length < 10) {
            // simply search for the position from the beginning
            int offset = 0;
            long after = 0;
            long newLength;
            while (after + (newLength = this.symbols[offset].getLength(false)) <= position) {
                after += newLength;
                ++offset;
            }
            if (positionHolder != null)
                positionHolder.set(after);
            return offset;
        }

        // initialize the position cache if necessary
        if (this.positionAfter == null) {
            this.positionAfter = new long[this.symbols.length - 1];
            long after = 0;
            for (int i = 0; i < this.symbols.length - 1; ++i) {
                this.positionAfter[i] = after += this.symbols[i].getLength(false);
            }
        }

        // now do a binary search
        int left = 0;
        int right = this.symbols.length - 1;
        int mid;

        while ((mid = (left + right) / 2) != left) {
            final long midVal = this.positionAfter[mid];
            if (midVal <= position)
                left = mid;
            else
                right = mid;
        }

        if (positionHolder != null)
            positionHolder.set(this.positionAfter[left]);
        return left+1;
    }

}
