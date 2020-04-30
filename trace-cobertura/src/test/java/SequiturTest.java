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
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.InputSequence;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.InputSequence.TraceIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.output.OutputSequence;

import java.io.*;

import static org.junit.Assert.*;


public class SequiturTest {

    private int[] concatenateArrays(int[]... arrays) {
        int length = 0;
        for (int[] a : arrays) {
            length += a.length;
        }
        int[] result = new int[length];
        int index = 0;
        for (int[] a : arrays) {
            System.arraycopy(a, 0, result, index, a.length);
            index += a.length;
        }
        return result;
    }

    private int[] a = {1, 2, 3, 4, 5};
    private int[] b = {1, 5, 4, 5, 4, 5, 4, 5};
    private int[] c = {3, 3, 3, 3, 3, 3, 3};
    private int[] d = {9, 9, 9};
    private int[] e = {1, 2, 3};

    @Test
    public void privateGrammar() throws IOException {
        OutputSequence outSeq = new OutputSequence();
        int[] ints = concatenateArrays(a, b, e, c, d, d, d, a, a, c, b, b, a, e, c, c, d, d, d, a, d, a, b, e, a, b, c, a, b, c, e);
        for (int i = 0; i < ints.length; ++i) {
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

        assertEquals(ints.length, inSeq.getLength());
        TraceIterator inIt = inSeq.iterator();
        for (int i = 0; i < ints.length; ++i) {
            assertTrue(inIt.hasNext());
            assertEquals(ints[i], inIt.next());
        }
        assertFalse(inIt.hasNext());

        System.out.println(inSeq);

        assertEquals(74, bytes.length);
    }

//    @Test
//    public void sharedGrammar() {
//        try {
//            Random rand = new Random(this.seed);
//            int numSequences = this.length / 10;
//            SharedOutputGrammar sharedGrammar = new SharedOutputGrammar();
//
//            OutputSequence[] outSeqs = (OutputSequence[]) new OutputSequence[numSequences];
//            for (int k = 0; k < numSequences; ++k) {
//                outSeqs[k] = new OutputSequence(sharedGrammar);
//            }
//            int[][] ints = new int[numSequences][this.length];
//
//            long overall = numSequences * this.length;
//            int[] written = new int[numSequences];
//
//            for (long i = 0; i < overall; ) {
//            	int seq = rand.nextInt(numSequences);
//            	if (written[seq] < this.length) {
//                    ints[seq][written[seq]] = rand.nextInt(this.length/5);
//                    outSeqs[seq].append(ints[seq][written[seq]]);
//                    ++written[seq];
//                    ++i;
//            	}
//            }
//            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
//            ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
//            sharedGrammar.writeOut(objOut);
//            for (int k = 0; k < numSequences; ++k)
//                outSeqs[k].writeOut(objOut, false);
//            objOut.close();
//            byte[] bytes = byteOut.toByteArray();
//
//            ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
//            ObjectInputStream objIn = new ObjectInputStream(byteIn);
//            InputSequence[] inSeqs = (InputSequence[]) new InputSequence[numSequences];
//            SharedInputGrammar inGrammar = (SharedInputGrammar)SharedInputGrammar.readFrom(objIn);
//            for (int k = 0; k < numSequences; ++k) {
//                inSeqs[k] = InputSequence.readFrom(objIn, inGrammar);
//            }
//
//            assertTrue("expected EOF", objIn.read() == -1);
//
//            for (int k = 0; k < numSequences; ++k) {
//                assertEquals("(internal check) sequence length", this.length, written[k]);
//                assertEquals("sequence length", this.length, inSeqs[k].getLength());
//                TraceIterator inIt = inSeqs[k].iterator();
//                for (int i = 0; i < this.length; ++i) {
//                    assertTrue("iterator should have more elements", inIt.hasNext());
//                    assertEquals("value in sequence", ints[k][i], inIt.next());
//                }
//                assertFalse(inIt.hasNext());
//            }
//
//        } catch (Throwable e) {
//            throw new RuntimeException("Exception in sequitur test with shared grammar for length " + this.length + "; seed " + this.seed, e);
//        }
//    }

}
