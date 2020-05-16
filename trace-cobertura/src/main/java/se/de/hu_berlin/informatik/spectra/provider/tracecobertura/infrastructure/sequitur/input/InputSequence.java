/**
 * License information:
 * Component: sequitur
 * Package:   de.unisb.cs.st.sequitur.input
 * Class:     InputSequence
 * Filename:  sequitur/src/main/java/de/unisb/cs/st/sequitur/input/InputSequence.java
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

import de.hammacher.util.LongHolder;
import gnu.trove.map.TObjectLongMap;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

@CoverageIgnore
public class InputSequence {

    public static class TraceIterator {

        private long pos;
        private final long seqLength;
        private final List<Rule> ruleStack = new ArrayList<Rule>(2);
        private int[] rulePos;
        private int[] count;

        public TraceIterator(final long position, final Rule firstRule) {
            this.pos = position;
            this.seqLength = firstRule.getLength();
            if (position == 0) {
                if (this.seqLength > 0) {
                    Rule rule = firstRule;
                    while (true) {
                        this.ruleStack.add(rule);
                        final Symbol sym = rule.symbols[0];
                        if (sym instanceof Terminal)
                            break;
                        rule = ((NonTerminal) sym).getRule();
                    }
                    final int depth = Math.max(Integer.highestOneBit(this.ruleStack.size() - 1) * 2, 2);
                    this.rulePos = new int[depth];
                    this.count = new int[depth];
                }
            } else if (position == firstRule.getLength()) {
                Rule rule = firstRule;
                this.rulePos = new int[2];
                this.count = new int[2];
                int i = 0;
                while (true) {
                    this.ruleStack.add(rule);
                    final int ruleSymLength = rule.symbols.length;
                    final Symbol sym = rule.symbols[ruleSymLength - 1];
                    if (this.rulePos.length == i) {
                        int[] newRulePos = new int[2 * i];
                        System.arraycopy(this.rulePos, 0, newRulePos, 0, i);
                        this.rulePos = newRulePos;
                        int[] newCount = new int[2 * i];
                        System.arraycopy(this.count, 0, newCount, 0, i);
                        this.count = newCount;
                    }
                    if (sym instanceof Terminal) {
                        // move behind the last symbol:
                        this.rulePos[i] = ruleSymLength;
                        break;
                    }
                    this.rulePos[i] = ruleSymLength - 1;
                    this.count[i++] = sym.count - 1;
                    rule = ((NonTerminal) sym).getRule();
                }
            } else {
                Rule rule = firstRule;
                this.rulePos = new int[2];
                this.count = new int[2];
                long after = 0;
                int i = 0;
                while (true) {
                    this.ruleStack.add(rule);
                    final LongHolder afterHolder = new LongHolder(0);
                    final int ruleOffset = position == after ? 0 : rule.findOffset(position - after, afterHolder);
                    after += afterHolder.longValue();
                    if (this.rulePos.length == i) {
                        int[] newRulePos = new int[2 * i];
                        System.arraycopy(this.rulePos, 0, newRulePos, 0, i);
                        this.rulePos = newRulePos;
                        int[] newCount = new int[2 * i];
                        System.arraycopy(this.count, 0, newCount, 0, i);
                        this.count = newCount;
                    }
                    this.rulePos[i] = ruleOffset;
                    final Symbol sym = rule.symbols[ruleOffset];
                    if (sym.count > 1) {
                        final long oneLength = sym.getLength(true);
                        this.count[i] = (int) ((position - after) / oneLength);
                        if (this.count[i] > 0)
                            after += this.count[i] * oneLength;
                        assert this.count[i] >= 0 && this.count[i] < sym.count;
                    }
                    if (sym instanceof Terminal)
                        break;
                    rule = ((NonTerminal) sym).getRule();
                    ++i;
                }
                assert after == position;
            }
        }

        public boolean hasNext() {
            return this.pos != this.seqLength;
        }

        public boolean hasPrevious() {
            return this.pos != 0;
        }

        public int next() {
            if (!hasNext())
                throw new NoSuchElementException();
            int depth = this.ruleStack.size() - 1;
            Symbol[] ruleSymbols = this.ruleStack.get(depth).symbols;
            Symbol sym = ruleSymbols[this.rulePos[depth]];
            final int value = ((Terminal) sym).getValue();

            while (true) {
                if (this.count[depth] + 1 < sym.count) {
                    ++this.count[depth];
                    break;
                }
                if (this.rulePos[depth] != ruleSymbols.length - 1) {
                    sym = ruleSymbols[++this.rulePos[depth]];
                    this.count[depth] = 0;
                    break;
                }
                if (depth == 0) {
                    assert this.pos == this.seqLength - 1;
                    ++this.rulePos[0];
                    this.count[0] = 0;
                    ++this.pos;
                    return value;
                }
                this.ruleStack.remove(depth);
                ruleSymbols = this.ruleStack.get(--depth).symbols;
                sym = ruleSymbols[this.rulePos[depth]];
            }
            while (sym instanceof NonTerminal) {
                final Rule rule = ((NonTerminal) sym).getRule();
                this.ruleStack.add(rule);
                if (this.rulePos.length == ++depth) {
                    int[] newRulePos = new int[2 * depth];
                    System.arraycopy(this.rulePos, 0, newRulePos, 0, depth);
                    this.rulePos = newRulePos;
                    int[] newCount = new int[2 * depth];
                    System.arraycopy(this.count, 0, newCount, 0, depth);
                    this.count = newCount;
                }
                this.rulePos[depth] = 0;
                this.count[depth] = 0;
                sym = rule.symbols[0];
            }
            ++this.pos;
            return value;
        }

        public int nextIndex() {
            if (this.pos >= Integer.MAX_VALUE)
                return Integer.MAX_VALUE;
            return (int) this.pos;
        }

        public int previous() {
            if (!hasPrevious())
                throw new NoSuchElementException();

            int depth = this.ruleStack.size() - 1;

            Symbol sym;
            while (true) {
                if (this.count[depth] != 0) {
                    --this.count[depth];
                    sym = this.ruleStack.get(depth).symbols[this.rulePos[depth]];
                    break;
                }
                if (this.rulePos[depth] != 0) {
                    sym = this.ruleStack.get(depth).symbols[--this.rulePos[depth]];
                    this.count[depth] = sym.count - 1;
                    break;
                }
                this.ruleStack.remove(depth--);
            }
            while (sym instanceof NonTerminal) {
                final Rule rule = ((NonTerminal) sym).getRule();
                this.ruleStack.add(rule);
                if (this.rulePos.length == ++depth) {
                    int[] newRulePos = new int[2 * depth];
                    System.arraycopy(this.rulePos, 0, newRulePos, 0, depth);
                    this.rulePos = newRulePos;
                    int[] newCount = new int[2 * depth];
                    System.arraycopy(this.count, 0, newCount, 0, depth);
                    this.count = newCount;
                }
                this.rulePos[depth] = rule.symbols.length - 1;
                sym = rule.symbols[this.rulePos[depth]];
                this.count[depth] = sym.count - 1;
            }
            --this.pos;
            return ((Terminal) sym).getValue();
        }

        public int previousIndex() {
            if (this.pos > Integer.MAX_VALUE)
                return Integer.MAX_VALUE;
            return (int) (this.pos - 1);
        }

    }

    private final Rule firstRule;

    private InputSequence(final Rule firstRule) {
        this.firstRule = firstRule;
    }

    public InputSequence(final long startRuleNumber, final SharedInputGrammar grammar) {
        this(getStartRule(startRuleNumber, grammar));
    }

    private static Rule getStartRule(final long startRuleNumber, final SharedInputGrammar grammar) {
        final Rule rule = grammar.grammar.getRule(startRuleNumber);
        if (rule == null)
            throw new IllegalArgumentException("Unknown start rule number");
        return rule;
    }

    public TraceIterator iterator() {
        return iterator(0);
    }

    public TraceIterator iterator(final long position) {
        return new TraceIterator(position, this.firstRule);
    }

    public long getLength() {
        return this.firstRule.getLength();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (this.firstRule.symbols.length > 0) {
            sb.append(this.firstRule.symbols[0]);
            for (int i = 1; i < this.firstRule.symbols.length; ++i)
                sb.append(' ').append(this.firstRule.symbols[i]);
        }

        final TObjectLongMap<Rule> rules = this.firstRule.getUsedRules();

        Rule[] keys = rules.keys((Rule[]) new Rule[rules.size()]);
        // sort by number of uses descending, then length (in symbols) descending
        Arrays.sort(keys, new Comparator<Rule>() {
            public int compare(Rule r1, Rule r2) {
                long numUses1 = rules.get(r1);
                long numUses2 = rules.get(r2);
                if (numUses1 != numUses2)
                    return numUses1 > numUses2 ? -1 : 1;
                long length1 = r1.symbols.length;
                long length2 = r2.symbols.length;
                return length1 == length2 ? 0 : length1 > length2 ? -1 : 1;
            }
        });
        final String newline = System.getProperty("line.separator");
        char[] blanks = new char[9];
        Arrays.fill(blanks, ' ');
        for (Rule rule : keys) {
            long numUses = rules.get(rule);
            assert numUses >= 1;
            byte fillBlanks = numUses < 10 ? 7
                    : numUses < 100 ? 6
                    : numUses < 1000 ? 5
                    : numUses < 10000 ? 4
                    : numUses < 100000 ? 3
                    : numUses < 1000000 ? 2
                    : numUses < 10000000 ? 1
                    : (byte) 0;
            sb.append(newline).append(numUses).append('x').append(blanks, 0, fillBlanks + 2).append(rule);
            /* print the whole expansion of the rule: */
            /*
            sb.append(newline).append("==> ");
            Itr itr = new Itr(0, rule);
            while (itr.hasNext())
                sb.append(itr.next());
            */
        }

        return sb.toString();
    }

    public static InputSequence readFrom(final ObjectInputStream objIn)
            throws IOException {
        return readFrom(objIn, SharedInputGrammar.readFrom(objIn));
    }

    public static InputSequence readFrom(final ObjectInputStream objIn,
                                         final ObjectReader objectReader) throws IOException {
        return readFrom(objIn, SharedInputGrammar.readFrom(objIn, objectReader));
    }

    public static InputSequence readFrom(final ObjectInputStream objIn, final SharedInputGrammar sharedGrammar) throws IOException {
        if (sharedGrammar == null)
            throw new NullPointerException();
        final long startRuleNr = DataInput.readLong(objIn);
        final Rule rule = sharedGrammar.grammar.getRule(startRuleNr);
        if (rule == null)
            throw new IOException("Unknown rule number");
        return new InputSequence(rule);
    }

    public Set<Integer> computeTerminals() {
        Set<Integer> result = new HashSet<>();
        final TObjectLongMap<Rule> rules = this.firstRule.getUsedRules();
        Rule[] keys = rules.keys((Rule[]) new Rule[rules.size()]);
        for (Rule rule : keys) {
            for (Symbol symbol : rule.symbols) {
                if (symbol instanceof Terminal) {
                    result.add(((Terminal) symbol).getValue());
                }
            }
        }
        return result;
    }

}
