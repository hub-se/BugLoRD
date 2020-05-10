/**
 * License information:
 * Component: sequitur
 * Package:   de.unisb.cs.st
 * Class:     RandomIntegrationTest
 * Filename:  sequitur/src/test/java/de/unisb/cs/st/RandomIntegrationTest.java
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


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.InputSequence;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.InputSequence.TraceIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.SharedInputGrammar;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.output.OutputSequence;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.output.SharedOutputGrammar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Random;

import static org.junit.Assert.*;


@RunWith(Parameterized.class)
public class RandomIntegrationTest {

    private final static int numTests = 4000;

    private final int length;
    private final long seed;


    public RandomIntegrationTest(int length, long seed) {
        this.length = length;
        this.seed = seed;
    }

    @Parameters
    public static Collection<Object[]> parameters() {
        Collection<Object[]> params = new AbstractList<Object[]>() {

            private final Random random = new Random();

            @Override
            public Object[] get(int index) {
                return new Object[]{this.random.nextInt(1 + index / 10), this.random.nextLong()};
            }

            @Override
            public int size() {
                return numTests;
            }

        };
        return params;
    }

    @Test
    public void privateGrammar() {
        try {
            Random rand = new Random(this.seed);
            OutputSequence outSeq = new OutputSequence();
            int[] ints = new int[this.length];
            for (int i = 0; i < this.length; ++i) {
                ints[i] = rand.nextInt(1 + this.length / 20);
                outSeq.append(ints[i]);
            }
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
            outSeq.writeOut(objOut, true);
            objOut.close();
            byte[] bytes = byteOut.toByteArray();

            ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
            ObjectInputStream objIn = new ObjectInputStream(byteIn);
            InputSequence inSeq = InputSequence.readFrom(objIn);

            assertEquals(this.length, inSeq.getLength());
            TraceIterator inIt = inSeq.iterator();
            for (int i = 0; i < this.length; ++i) {
                assertTrue(inIt.hasNext());
                assertEquals(ints[i], inIt.next());
            }
            assertFalse(inIt.hasNext());

        } catch (Throwable e) {
            throw new RuntimeException("Exception in sequitur test for length " + this.length + "; seed " + this.seed, e);
        }
    }

    @Test
    public void sharedGrammar() {
        try {
            Random rand = new Random(this.seed);
            int numSequences = this.length / 10;
            SharedOutputGrammar sharedGrammar = new SharedOutputGrammar();

            OutputSequence[] outSeqs = (OutputSequence[]) new OutputSequence[numSequences];
            for (int k = 0; k < numSequences; ++k) {
                outSeqs[k] = new OutputSequence(sharedGrammar);
            }
            int[][] ints = new int[numSequences][this.length];

            long overall = numSequences * this.length;
            int[] written = new int[numSequences];

            for (long i = 0; i < overall; ) {
                int seq = rand.nextInt(numSequences);
                if (written[seq] < this.length) {
                    ints[seq][written[seq]] = rand.nextInt(this.length / 5);
                    outSeqs[seq].append(ints[seq][written[seq]]);
                    ++written[seq];
                    ++i;
                }
            }
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
            sharedGrammar.writeOut(objOut);
            for (int k = 0; k < numSequences; ++k)
                outSeqs[k].writeOut(objOut, false);
            objOut.close();
            byte[] bytes = byteOut.toByteArray();

            ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
            ObjectInputStream objIn = new ObjectInputStream(byteIn);
            InputSequence[] inSeqs = (InputSequence[]) new InputSequence[numSequences];
            SharedInputGrammar inGrammar = (SharedInputGrammar) SharedInputGrammar.readFrom(objIn);
            for (int k = 0; k < numSequences; ++k) {
                inSeqs[k] = InputSequence.readFrom(objIn, inGrammar);
            }

            assertTrue("expected EOF", objIn.read() == -1);

            for (int k = 0; k < numSequences; ++k) {
                assertEquals("(internal check) sequence length", this.length, written[k]);
                assertEquals("sequence length", this.length, inSeqs[k].getLength());
                TraceIterator inIt = inSeqs[k].iterator();
                for (int i = 0; i < this.length; ++i) {
                    assertTrue("iterator should have more elements", inIt.hasNext());
                    assertEquals("value in sequence", ints[k][i], inIt.next());
                }
                assertFalse(inIt.hasNext());
            }

        } catch (Throwable e) {
            throw new RuntimeException("Exception in sequitur test with shared grammar for length " + this.length + "; seed " + this.seed, e);
        }
    }

}
