/**
 * License information:
 * Component: sequitur
 * Package:   de.unisb.cs.st
 * Class:     SingleIntegrationTest
 * Filename:  sequitur/src/test/java/de/unisb/cs/st/SingleIntegrationTest.java
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


import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;

@RunWith(JUnit4.class)
public class SingleIntegrationTest extends RandomIntegrationTest {

    private static final int length = 10;
    private static final long seed = 8398731863259715411l;
    private static final int ITERATIONS = 1000000000;
    private static final int LOWER_8_BITS = 0xFF;

    public SingleIntegrationTest() {
        super(length, seed);
    }

    private static int toInt1(int b3, int b2, int b1, int b0) {
        return ((((((b3 & LOWER_8_BITS) << 8) | (b2 & LOWER_8_BITS)) << 8) | (b1 & LOWER_8_BITS)) << 8) | (b0 & LOWER_8_BITS);
    }

    private static int toInt2(int b3, int b2, int b1, int b0) {
        return ((b3 & LOWER_8_BITS) << 24) | ((b2 & LOWER_8_BITS) << 16) | ((b1 & LOWER_8_BITS) << 8) | (b0 & LOWER_8_BITS);
    }

    //    @Test
    public void testStuff() {
        int local = 1239675285;
        for (int i = 0; i < 100000; ++i) {
            local += toInt1(local, local, local, local);
            local += toInt2(local, local, local, local);
        }

        for (int j = 0; j < 5; ++j) {
            long startTime = new Date().getTime();

            local = 1239675285 + j;
            for (int i = 0; i < ITERATIONS; ++i) {
                local += toInt1(local, local, local, local);
            }

            long endTime = new Date().getTime();

            System.out.println(String.format("1: %,d ms -> %d", endTime - startTime, local));

            startTime = new Date().getTime();

            local = 1239675285 + j;
            for (int i = 0; i < ITERATIONS; ++i) {
                local += toInt2(local, local, local, local);
            }

            endTime = new Date().getTime();

            System.out.println(String.format("2: %,d ms -> %d", endTime - startTime, local));
        }

    }

}
