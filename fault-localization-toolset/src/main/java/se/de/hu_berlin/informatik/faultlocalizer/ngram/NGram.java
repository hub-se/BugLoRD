package se.de.hu_berlin.informatik.faultlocalizer.ngram;

import java.util.Arrays;

public class NGram implements Comparable<NGram> {
    final int length;
    private int[] blockIDs;
    private double EF;
    private double ET;
    private double confidence;


    public NGram(int length) {
        this.length = length;
        blockIDs = new int[length];
    }

    public NGram(int length, double EF, double ET, int[] blockIDs) {
        this.length = length;
        this.blockIDs = blockIDs;
        this.EF = EF;
        this.ET = ET;
        confidence = (ET > 0) ? EF / ET : 0.0;
    }

    public double getConfidence() {
        return confidence;
    }


    public int[] getBlockIDs() {
        return blockIDs;
    }


    public double getEF() {
        return EF;
    }


    public double getET() {
        return ET;
    }


    // the algorithm begins with the relevant 1-gram item sets with maximal support.
    // there 2 scenarios: conf of such 1-grams is 1 or less.
    // in case of conf = 1 this node most probably a inner node of an linear execution trace.
    // therefore it is not clearly decidable if it is a faulty node.
    // 1-gram with conf less than 1 is how ever more interesting because they are branch-nodes.
    // that mean there is a context-change at this position so that in some cases it will be executed
    // in a successful test or vice versa. By computing the confidence of 2-grams that contain such node
    // we can determine the probability of a fault at this position in a more intuitive or meaningful way.
    // that why the ranking of n-gram give two n-grams with identical confidence and block length a different score
    // depends on their support. Node with smaller support but with same or higher confidence  has more entropy.
    @Override
    public int compareTo(NGram o) {
        int out = 0;
        if (confidence > o.getConfidence()) out = 1;
        if (confidence < o.getConfidence()) out = -1;
        if (confidence == o.getConfidence()) {
            if (blockIDs.length > o.blockIDs.length) out = 1;
            if (blockIDs.length < o.blockIDs.length) out = -1;
            if (blockIDs.length == o.blockIDs.length) {
                if (EF > o.EF) out = -1;
                if (EF < o.EF) out = 1;
                if (EF == o.EF) out = 0;
            }
        }
        return out;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof NGram)) {
            return false;
        }
        NGram dummy = (NGram) obj;
        return Arrays.equals(blockIDs, dummy.blockIDs);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + length;
        result = 31 * result + Arrays.hashCode(blockIDs);
        return result;
    }

}
