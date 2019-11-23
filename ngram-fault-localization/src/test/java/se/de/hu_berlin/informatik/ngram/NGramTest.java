package se.de.hu_berlin.informatik.ngram;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

class NGramTest {


    @Test
    void compareTo() {
        ArrayList<Integer> a1 = new ArrayList<>(1);
        ArrayList<Integer> a2 = new ArrayList<>(1);
        a1.add(3);
        a2.add(3);
        NGram dummy = new NGram(1, 2.0, 3.0, a1);
        NGram dummy2 = new NGram(2, 2.0, 3.0, a2);
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