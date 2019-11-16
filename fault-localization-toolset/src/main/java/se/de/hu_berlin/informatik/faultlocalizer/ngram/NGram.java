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


    @Override
    public int compareTo(NGram o) {
        int out = 0;
        if (confidence > o.getConfidence()) out = 1;
        if (confidence < o.getConfidence()) out = -1;
        if (confidence == o.getConfidence()) {
            if (blockIDs.length < o.blockIDs.length) out = 1;
            if (blockIDs.length > o.blockIDs.length) out = -1;
            if (blockIDs.length == o.blockIDs.length) out = 0;
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
