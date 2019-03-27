package se.de.hu_berlin.informatik.experiments.defects4j;

import se.de.hu_berlin.informatik.analyzer.SootFactory;

public class SootRunner {

    public static void main(final String[] args) {

        final SootFactory soot = new SootFactory("se.de.hu_berlin.informatik.experiments.test.Hello");
        soot.generateCFGFormMethod("test2");
    }

}
