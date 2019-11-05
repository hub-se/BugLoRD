package se.de.hu_berlin.informatik.ngram;

import java.util.HashSet;

public class ExcGraphNode {
    private HashSet<Integer> InNodes = new HashSet<>();
    private HashSet<Integer> OutNodes = new HashSet<>();
    private int nodeId;

    public ExcGraphNode(int nodeId) {
        this.nodeId = nodeId;
    }

    public HashSet<Integer> getInNodes() {
        return InNodes;
    }

    public HashSet<Integer> getOutNodes() {
        return OutNodes;
    }

    public int getNodeId() {
        return nodeId;
    }


//    public HashSet<Integer> getInNodes() {
//        return InNodes;
//    }
//
//    public HashSet<Integer> getOutNodes() {
//        return OutNodes;
//    }

    public boolean checkInNode(Integer n) {
        return InNodes.contains(n);
    }

    public boolean addInNode(Integer n) {
        return InNodes.add(n);
    }

    public int getInDegree() {
        return InNodes.size();
    }

    public boolean checkOutNode(Integer n) {
        return OutNodes.contains(n);
    }

    public boolean addOutNode(Integer n) {
        return OutNodes.add(n);
    }

    public int getOutDegree() {
        return OutNodes.size();
    }

}
