package se.de.hu_berlin.informatik.ngram;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

class NGramTest {


    @Test
    void compareTo() {
        NGram dummy = new NGram(1, 2.0, 3.0, new int[]{3});
        NGram dummy2 = new NGram(2, 2.0, 3.0, new int[]{3});
        System.out.println(dummy.equals(dummy2));
        System.out.println(dummy.hashCode());
        System.out.println(dummy2.hashCode());
        HashSet<NGram> set = new HashSet<>();
        set.add(dummy);
        System.out.println(set.contains(dummy2));
        int[] a = new int[1];
        int[] b = new int[1];
        a[0] = 2;
        b[0] = 2;
        System.out.println(Arrays.equals(a, b));
        HashSet<int[]> set2 = new HashSet<>();
        set2.add(a);
        System.out.println(set2.contains(b));
    }
}