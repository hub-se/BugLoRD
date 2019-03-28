package se.de.hu_berlin.informatik.junit;

import static org.junit.Assert.assertNotNull;

import java.util.Iterator;

import org.junit.Test;

import se.de.hu_berlin.informatik.analyzer.SootFactory;
import soot.Unit;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.UnitGraph;

public class SootConnector
{
    final String packageName = "se.de.hu_berlin.informatik.test";
    final String className = "Hello";
    final String pathExtensions = "C:/Users/yerlikayaa13/Downloads/HU/BugLoRD-master/BugLoRD-master/project-analyzer/target/test-classes";
    final String methodName = "test2";

    @Test
    public void testCFGByMethodName()
    {
        Iterator<Unit> unitIt = null;
        UnitGraph unitGraph = null;
        SootFactory sc = null;

        // get Soot Instance
        sc = SootFactory.getInstance(packageName, className, pathExtensions);
        assertNotNull("Soot can not initialise", sc);

        // Get control flow graph of the SootClass by method name
        unitGraph = sc.getCFGForMethodName(methodName);
        assertNotNull("UnitGraph is null", unitGraph);

        unitIt = unitGraph.iterator();

        while (unitIt.hasNext())
        {
            System.out.println(unitIt.next());
        }
    }

    @Test
    public void testIteratorOnCallsOutOfMethodName()
    {
        Iterator<Edge> itEdge = null;
        SootFactory sc = null;

        // get Soot Instance
        sc = SootFactory.getInstance(packageName, className, pathExtensions);
        assertNotNull("Soot can not initialise", sc);

        // Get control flow graph of the SootClass by method name
        itEdge = sc.getIteratorOnCallsOutOfMethodName(methodName);
        assertNotNull("UnitGraph is null", itEdge);

        while (itEdge.hasNext())
        {
            System.out.println(itEdge.next());
        }
    }

}
