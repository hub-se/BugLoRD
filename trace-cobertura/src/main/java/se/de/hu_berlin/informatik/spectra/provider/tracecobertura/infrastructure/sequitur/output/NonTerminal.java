/** License information:
 *    Component: sequitur
 *    Package:   de.unisb.cs.st.sequitur.output
 *    Class:     NonTerminal
 *    Filename:  sequitur/src/main/java/de/unisb/cs/st/sequitur/output/NonTerminal.java
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
package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.output;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Queue;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.output.Rule.Dummy;

// package-private
class NonTerminal extends Symbol {

    private final Rule rule;

    public NonTerminal(final Rule rule) {
        super(1);
        assert rule != null;
        this.rule = rule;
        rule.incUseCount();
    }

    @Override
    public void remove() {
        super.remove();
        this.rule.decUseCount();
    }

    public Rule getRule() {
        return this.rule;
    }

    @Override
    protected boolean singleEquals(final Symbol obj) {
        if (obj.getClass() != this.getClass())
            return false;
        final NonTerminal other = (NonTerminal) obj;
        return this.count == other.count && this.rule.equals(other.rule);
    }

    @Override
    protected int singleHashcode() {
        return this.rule.hashCode() + 31*this.count;
    }

    /**
     * replace this non terminal with the contents of its rule;
     * works only if the rule is only used once
     * @param grammar
     */
    public void checkExpand(final Grammar grammar) {
        assert this.count >= 1;
        if (this.count == 1 && this.rule.getUseCount() == 1) {
            grammar.removeDigram(this.prev);
            grammar.removeDigram(this);
            remove();
            linkTogether(this.prev, this.rule.dummy.next);
            linkTogether(this.rule.dummy.prev, this.next);
            grammar.checkDigram(this.prev);
            grammar.checkDigram(this.rule.dummy.prev);
        }
    }

    public boolean checkSubstRule(final Grammar grammar) {
        assert this.count >= 1;
        // only works if rule is of length one...
        if (this.rule.dummy.next.next != this.rule.dummy)
            return false;

        grammar.removeDigram(this.prev);
        grammar.removeDigram(this);
        
        // replace this non-terminal with the right side of the rule
        final Symbol newSymbol = this.rule.dummy.next.clone();
        newSymbol.count *= this.count;
        remove();
        this.next.insertBefore(newSymbol);
        if (!grammar.checkDigram(this.prev))
            grammar.checkDigram(this);
        return true;
    }

    @Override
    public boolean meltDigram(final Grammar grammar) {
    	if (this.next instanceof NonTerminal) {
    		final NonTerminal otherNonT = (NonTerminal) this.next;
    		// check if both non-terminals are the same rule
    		if (otherNonT.rule.equals(this.rule)) {
    			final boolean hasPrev = !(this.prev instanceof Dummy);
    			final boolean hasNextNext = !(otherNonT.next instanceof Dummy);
    			if (hasPrev)
    				grammar.removeDigram(this.prev);
    			if (hasNextNext)
    				grammar.removeDigram(otherNonT);
    			this.count += otherNonT.count;
    			otherNonT.remove();
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
        return this.count == 1 ? 0 : 1;
    }

    @Override
    public void writeOut(final ObjectOutputStream objOut, final Grammar grammar,
            final ObjectWriter objectWriter, final Queue<Rule> queue) throws IOException {
        assert this.count >= 1;
        if (this.count != 1) {
            DataOutput.writeInt(objOut, this.count);
        }
        DataOutput.writeLong(objOut, grammar.getRuleNr(this.rule, queue));
    }

    @Override
    protected NonTerminal clone() {
        final NonTerminal clone = (NonTerminal) super.clone();
        clone.rule.incUseCount();
        return clone;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('R').append(this.rule.hashCode());
        assert this.count >= 1;
        if (this.count > 1) {
            sb.append('^').append(this.count);
        }
        return sb.toString();
    }

}
