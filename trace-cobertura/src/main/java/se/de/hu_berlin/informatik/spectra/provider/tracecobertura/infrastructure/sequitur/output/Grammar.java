/**
 * License information:
 * Component: sequitur
 * Package:   de.unisb.cs.st.sequitur.output
 * Class:     Grammar
 * Filename:  sequitur/src/main/java/de/unisb/cs/st/sequitur/output/Grammar.java
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

import de.hammacher.util.LongArrayList;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.output.Rule.Dummy;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// package-private
@CoverageIgnore
class Grammar {
	
	Lock lock = new ReentrantLock();

    static final int prime = 2265539;
    private final Map<Symbol, Symbol> digrams = new HashMap<Symbol, Symbol>(prime);

    private final List<OutputSequence> usingSequences = new ArrayList<OutputSequence>(3);

    // in writeOut, this map is filled
    private Map<Rule, Long> ruleNumbers = new IdentityHashMap<Rule, Long>();
    private long nextRuleNumber = 0;

    /**
     * Checks a new digram. If it appears
     * elsewhere, deals with it by calling
     * match(), otherwise inserts it into the
     * hash map.
     *
     * @return whether or not there was a substitution
     */
    public boolean checkDigram(final Symbol first) {
        // dummy objects are assigned count 0, for example
        if (first.count == 0 || first.next.count == 0)
            return false;

        if (first.meltDigram(this))
            return true;

        // the equals() method of symbol actually checks if both this symbol and its successor are the same
        final Symbol oldDigram = this.digrams.get(first);
        if (oldDigram == null) {
            this.digrams.put(first, first);
            return false;
        } else if (oldDigram == first) // this is necessary, but it should not be...
            return false;

        assert oldDigram.next != first && oldDigram != first;

        // 'oldDigram' is the actual object in the digram map; 'first' is not! 
        // we found an existing entry!
        match(first, oldDigram);
        return true;
    }

    public void removeDigram(final Symbol sym) {
        if (sym instanceof Dummy || sym.next instanceof Dummy)
            return;
        // only remove the digram if the stored symbol is the same object!
        if (this.digrams.get(sym) == sym)
            this.digrams.remove(sym);
    }

    private void match(final Symbol newDigram, final Symbol oldDigram) {
        Rule rule;

//		System.out.println(oldDigram + ", " + oldDigram.next + "; " + newDigram + ", " + newDigram.next);
//		if (oldDigram.prev  instanceof Dummy && 
//				oldDigram.next.next instanceof Dummy){
//
//			// reuse an existing rule
//
//			rule = ((Dummy)oldDigram.prev).getRule();
//			newDigram.substituteDigram(rule, this);
//		}else{
//
//			// create a new rule
//
//			final Symbol clone = newDigram.clone();
//			rule = new Rule(clone, newDigram.next.clone());
//
//			digrams.put(clone,clone);
//
//			newDigram.substituteDigram(rule, this);
//			oldDigram.substituteDigram(rule, this);
//		}
//
//		// Check for an underused rule.
//
//		Symbol first = rule.dummy.next;
//		if (first instanceof NonTerminal)
//			((NonTerminal)first).checkExpand(this);


        // newGram is a non-repeated non-terminal/rule which is used twice;
        // this means, we have to replace the rule with its right side, since it...
        if (newDigram instanceof NonTerminal && ((NonTerminal) newDigram).getCount() == 1
                && (rule = ((NonTerminal) newDigram).getRule()).getUseCount() == 2) {
//			System.out.println("check1");
            assert oldDigram instanceof NonTerminal && ((NonTerminal) oldDigram).getRule() == rule;

            // remove the successor in both digrams and add it to the non-terminal's rule
            final Symbol next = newDigram.next;
            // newGram is not the object in the map!
            //            removeDigram(newDigram);
            removeDigram(next);
            Symbol.linkTogether(newDigram, next.next);
            removeDigram(oldDigram);
            removeDigram(oldDigram.next);
            oldDigram.next.remove();
            rule.append(next, this);

            // check if the length of the rule's right side is 1 after appending the new symbol
            // and, if so, substitute the non-terminal with the right side
            if (((NonTerminal) newDigram).count != 0 &&
                    ((NonTerminal) newDigram).checkSubstRule(this)) {
                System.out.println("check1");
                return;
            }
            if (((NonTerminal) oldDigram).count != 0 &&
                    ((NonTerminal) oldDigram).checkSubstRule(this)) {
                System.out.println("check2");
                return;
            }

            // newGram is the only symbol in its rule
            if (newDigram.prev == newDigram.next && ((Dummy) newDigram.next).getRule().mayBeReused()) {
                final Rule otherRule = ((Dummy) newDigram.next).getRule();
                // rule is expanded inside the otherRule, since otherRule consisted of only this one nonterminal
                removeDigram(oldDigram.prev);
                removeDigram(oldDigram);
                // newDigram.remove(); // not needed, because it is overwritten by these instructions:
                Symbol.linkTogether(newDigram.next, rule.dummy.next);
                Symbol.linkTogether(rule.dummy.prev, newDigram.next);
                oldDigram.remove();
                oldDigram.next.insertBefore(new NonTerminal(otherRule));
                checkDigram(oldDigram.prev);
                checkDigram(oldDigram.next);
            } else

                // oldGram is the only symbol in its rule
                if (oldDigram.prev == oldDigram.next && ((Dummy) oldDigram.next).getRule().mayBeReused()) {
                    final Rule otherRule = ((Dummy) oldDigram.next).getRule();
                    // rule is expanded inside the otherRule, since otherRule consisted of only this one nonterminal
                    removeDigram(newDigram.prev);
                    removeDigram(newDigram);
                    Symbol firstSubst = rule.dummy.next;
                    Symbol lastSubst = rule.dummy.prev;
                    // if rule is used more than twice (in newDigram and oldDigram), we have to clone it
                    if (rule.getUseCount() > 2) {
                        firstSubst = rule.dummy.next.clone();
                        Symbol s = firstSubst;
                        while (s.next != lastSubst) {
                            (s.next = s.next.clone()).prev = s;
                            s = s.next;
                        }
                        (s.next = lastSubst = lastSubst.clone()).prev = s;
                    }
                    // oldDigram.remove(); // not needed, because it is overwritten by these instructions:
                    Symbol.linkTogether(oldDigram.next, firstSubst);
                    Symbol.linkTogether(lastSubst, oldDigram.next);
                    newDigram.remove();
                    newDigram.next.insertBefore(new NonTerminal(otherRule));
                    checkDigram(newDigram.prev);
                    checkDigram(newDigram.next);
                } else {
                    checkDigram(newDigram);
                    checkDigram(oldDigram);
                }
        } else

            // the old digram's rule consists of only 2 (non-dummy) symbols
            if (oldDigram.prev == oldDigram.next.next
                    && (rule = ((Dummy) oldDigram.prev).getRule()).mayBeReused()) {
//				System.out.println("check2");
                // substitute the new digram with the old digram's rule
                newDigram.substituteDigram(rule, this);
                if (rule.getUseCount() > 0) {
                    Symbol first = rule.dummy.next;
                    if (first instanceof NonTerminal)
                        ((NonTerminal) first).checkExpand(this);
//				final Dummy dummy = rule.dummy;
//				for (Symbol s = dummy.next; s != dummy; s = s.next) {
//					if (s instanceof NonTerminal)
//						((NonTerminal)s).checkExpand(this);
//				}
                }
            } else

                // TODO check if this case really is new... underlying rules should be the same!
                // the new digram's rule consists of only 2 (non-dummy) symbols
                if (newDigram.prev == newDigram.next.next
                        && (rule = ((Dummy) newDigram.prev).getRule()).mayBeReused()) {
//				System.out.println("check3");
                    // substitute the old digram with new digram's rule
                    // (idk if this case really makes sense)
                    oldDigram.substituteDigram(rule, this);
                    if (rule.getUseCount() > 0) {
                        Symbol first = rule.dummy.next;
                        if (first instanceof NonTerminal)
                            ((NonTerminal) first).checkExpand(this);
//				final Dummy dummy = rule.dummy;
//				for (Symbol s = dummy.next; s != dummy; s = s.next) {
//					if (s instanceof NonTerminal)
//						((NonTerminal)s).checkExpand(this);
//				}
                    }
                } else

                // we are either dealing with terminal symbols,
                // (melted) non-terminal symbols,
                // non-terminal symbols that have been used more than twice,
                // symbols whose rules contain multiple symbols
                {
                    // create a new rule that consists of the new digram (and the next element?)
                    final Symbol clone = newDigram.clone();

                    rule = new Rule(clone, newDigram.next.clone());
                    // explicit removal is necessary, since we care for the actual key object that is inserted
                    this.digrams.remove(clone);
                    this.digrams.put(clone, clone);
                    newDigram.substituteDigram(rule, this);
                    oldDigram.substituteDigram(rule, this);
                    if (rule.getUseCount() > 0) {
                        Symbol first = rule.dummy.next;
                        if (first instanceof NonTerminal)
                            ((NonTerminal) first).checkExpand(this);
//				final Dummy dummy = rule.dummy;
//				for (Symbol s = dummy.next; s != dummy; s = s.next) {
//					if (s instanceof NonTerminal)
//						((NonTerminal)s).checkExpand(this);
//				}
                    }

                }
    }

    protected long getRuleNr(final Rule rule) {
        return getRuleNr(rule, null);
    }

    protected long getRuleNr(final Rule rule, final Queue<Rule> queue) {
        Long nr = this.ruleNumbers.get(rule);
        if (nr == null) {
            if (queue != null)
                queue.add(rule);
            nr = this.nextRuleNumber++;
            // this rule must not be removed!!
            rule.incUseCount();
            try {
                this.ruleNumbers.put(rule, nr);
            } catch (final IllegalStateException e) {
                if (this.ruleNumbers.getClass().equals(TreeMap.class))
                    throw e;
                // capacity exceeded: switch to treemap
                this.ruleNumbers = new TreeMap<Rule, Long>(this.ruleNumbers);
                this.ruleNumbers.put(rule, nr);
            }
        }
        return nr;
    }

    public void writeOut(final ObjectOutputStream objOut, final ObjectWriter objectWriter)
            throws IOException {
        final Queue<Rule> ruleQueue = new LinkedList<Rule>();
        // first, fill in already written rules
        // take care of the order!
        assert (TreeMap.class.equals(this.ruleNumbers.getClass()) ||
                IdentityHashMap.class.equals(this.ruleNumbers.getClass()));
        if (TreeMap.class.equals(this.ruleNumbers.getClass())) {
            // on a TreeMap, we cannot rely on the size, because it is
            // stored in an int
            final LongArrayList<Rule> rules = new LongArrayList<Rule>();
            long numRules = 0;
            for (final Entry<Rule, Long> e : this.ruleNumbers.entrySet()) {
                ++numRules;
                rules.ensureCapacity(e.getValue());
                while (rules.longSize() <= e.getValue())
                    rules.add(null);
                assert rules.get(e.getValue()) == null;
                rules.set(e.getValue(), e.getKey());
                
                if (numRules % 100000 == 0) {
                    System.out.print('.');
                    if (numRules % 10000000 == 0)
                        System.out.println(String.format("%,d", numRules));
                }
            }
            assert numRules == rules.size();
            ruleQueue.addAll(rules);
        } else if (this.ruleNumbers.size() > 0) {
            final Rule[] ruleArr = newRuleArray(this.ruleNumbers.size());
            for (final Entry<Rule, Long> e : this.ruleNumbers.entrySet())
                ruleArr[e.getValue().intValue()] = e.getKey();
            ruleQueue.addAll(Arrays.asList(ruleArr));
        }
        // then, fill in the first rule of sequences that use this grammar
        for (final OutputSequence seq : this.usingSequences) {
            seq.flush();
            getRuleNr(seq.firstRule, ruleQueue);
        }
        for (final Rule rule : ruleQueue)
            rule.ensureInvariants(this);

        long ruleNr = 0;
        while (!ruleQueue.isEmpty()) {
            final Rule rule = ruleQueue.poll();
            assert getRuleNr(rule) == ruleNr;
            ++ruleNr;
            rule.writeOut(objOut, this, objectWriter, ruleQueue);
            
            if (ruleNr % 100000 == 0) {
                System.out.print(':');
                if (ruleNr % 10000000 == 0)
                    System.out.println(String.format("%,d", ruleNr));
            }
        }
        objOut.write(0); // mark end of rules
    }

    private Rule[] newRuleArray(final int dim) {
        return (Rule[]) new Rule[dim];
    }

    protected void newSequence(final OutputSequence seq) {
        this.usingSequences.add(seq);
    }

    // TODO remove
    @Override
    public String toString() {
        return this.digrams.toString() +
                System.getProperty("line.separator") +
                this.usingSequences.toString();
    }

}
