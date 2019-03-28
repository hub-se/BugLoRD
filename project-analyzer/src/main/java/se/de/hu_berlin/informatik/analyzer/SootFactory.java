package se.de.hu_berlin.informatik.analyzer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class SootFactory
{

    private static final String JDK_PATH = ".;C:/Tools/jdk/jdk1.7.0_55/jre/lib/rt.jar";
    private static final String JDK_8_PATH = "/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/rt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/jce.jar";

    private final String packageName;
    private final String className;
    private static Map<String, SootFactory> cache = new HashMap<>();

    private SootFactory(final String packageName, final String className, final String classPathExtension)
    {
        this.packageName = packageName;
        this.className = className;
        init(classPathExtension);
    }

    public static SootFactory getInstance(final String packageName, final String className, final String classPathExtension)
    {
        return getInstance(packageName, className, classPathExtension, false);
    }

    public static SootFactory getInstance(final String packageName, final String className, String classPathExtension, final boolean forceReload)
    {
        System.out.println("-------Initializing Soot------");
        classPathExtension = classPathExtension.replace(',', ';');
        final String qualifiedName = packageName + "." + className;
        if (!cache.containsKey(qualifiedName) || forceReload)
        {
            final SootFactory instance = new SootFactory(packageName, className, classPathExtension);
            cache.put(qualifiedName, instance);
        }
        return cache.get(qualifiedName);
    }

    public String getPackageName()
    {
        return packageName;
    }

    public String getClassName()
    {
        return className;
    }

    private String getPackageAndClassName()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(getPackageName());
        builder.append(".");
        builder.append(getClassName());
        return builder.toString();
    }

    /**
     * Initialize Soot and set the Soot Options
     *
     * @param classPathExtension
     */
    private void init(final String classPathExtension)
    {
        final long startTime = System.nanoTime();

        // set Soot Options
        // TODO: define JDK in ini file
        // Set Soot's internal classpath
        Options.v().set_soot_classpath(JDK_PATH + ";C:/Tools/jdk/jdk1.7.0_55/jre/lib/jce.jar" + ";" + classPathExtension);
        Options.v().set_src_prec(Options.src_prec_only_class);

        // Enable line number
        Options.v().set_keep_line_number(true);

        // Enable whole-program mode
        Options.v().set_whole_program(true);
        final SootClass c = Scene.v().loadClassAndSupport(getPackageAndClassName());
        Scene.v().loadNecessaryClasses();
        c.setApplicationClass();

        // Set the main class of the application to be analysed
        Scene.v().setMainClass(c);

        // cg = the call graph pack
        PackManager.v().getPack("cg").apply();

        final long endTime = System.nanoTime();
        System.out.println("Loading costs " + (endTime - startTime) + " ns, equal to " + (endTime - startTime) / 1000000000 + " s.");
    }

    /**
     * Get control flow graph of the SootClass by method name
     *
     * @param methodName
     * @return UnitGraph of Soot
     */
    public UnitGraph getCFGForMethodName(final String methodName)
    {
        UnitGraph unitGraph = null;
        SootClass c = null;
        SootMethod targetMethod = null;

        if (methodName != null)
        {
            c = Scene.v().loadClassAndSupport(getPackageAndClassName());
            if (c != null)
            {
                Scene.v().loadNecessaryClasses();
                c.setApplicationClass();
                targetMethod = c.getMethodByName(methodName);
                if (targetMethod != null)
                {
                    unitGraph = new ExceptionalUnitGraph(targetMethod.retrieveActiveBody());
                }
            }
        }

        return unitGraph;
    }

    /**
     * Get control flow graph of the SootClass by SootMethod
     *
     * @param method
     * @return UnitGraph of Soot
     */
    public UnitGraph getCFGForMethod(final SootMethod method)
    {
        UnitGraph unitGraph = null;

        if (method != null)
        {
            unitGraph = new ExceptionalUnitGraph(method.retrieveActiveBody());
        }

        return unitGraph;
    }

    /**
     * Get edges of call graph of the SootClass by method name
     *
     * @param methodName
     * @return Iterator<Edge> of Soot
     */
    public Iterator<Edge> getIteratorOnCallersForMethodName(final String methodName)
    {
        final CallGraph callGraph = Scene.v().getCallGraph();
        Iterator<Edge> i = null;
        SootClass c = null;

        if (callGraph != null && methodName != null)
        {
            c = Scene.v().loadClassAndSupport(getPackageAndClassName());
            if (c != null)
            {
                i = callGraph.edgesInto(c.getMethodByName(methodName));
            }
        }

        return i;
    }

    /**
     * Get edges of call graph of the SootClass by SootMethod
     *
     * @param method
     * @return Iterator<Edge> of Soot
     */
    public Iterator<Edge> getIteratorOnCallersForMethod(final SootMethod method)
    {
        final CallGraph callGraph = Scene.v().getCallGraph();
        Iterator<Edge> i = null;

        if (callGraph != null && method != null)
        {
            i = callGraph.edgesInto(method);
        }

        return i;
    }

    /**
     * Get edges of call graph of the SootClass by method name
     *
     * @param methodName
     * @return Iterator<Edge> of Soot
     */
    public Iterator<Edge> getIteratorOnCallsOutOfMethodName(final String methodName)
    {
        final CallGraph callGraph = Scene.v().getCallGraph();
        SootClass c = null;
        Iterator<Edge> i = null;

        if (callGraph != null && methodName != null)
        {
            c = Scene.v().loadClassAndSupport(getPackageAndClassName());
            if (c != null)
            {
                i = callGraph.edgesOutOf(c.getMethodByName(methodName));
            }
        }

        return i;
    }

    /**
     * Load SootMethod by method name
     *
     * @param methodName
     * @return
     */
    public SootMethod loadMethod(final String methodName)
    {
        final SootClass c = Scene.v().loadClassAndSupport(getPackageAndClassName());
        SootMethod targetMethod = null;
        if (c != null && methodName != null)
        {
            Scene.v().loadNecessaryClasses();
            c.setApplicationClass();
            targetMethod = c.getMethodByName(methodName);
        }
        return targetMethod;
    }

    /**
     * Print control flow graph of the SootClass by method name
     *
     * @param c
     *            is the SootClass
     * @param methodName
     *            is the method name
     * @return an UnitGraph
     */
    public static UnitGraph printCFGForMethodName(final SootClass c, final String methodName)
    {
        UnitGraph unitGraph = null;
        SootMethod method = null;
        Iterator<Unit> unitIt = null;

        if (c != null && methodName != null)
        {
            method = c.getMethodByName(methodName);
            if (method != null)
            {
                unitGraph = new ExceptionalUnitGraph(method.retrieveActiveBody());
                if (unitGraph != null)
                {
                    unitIt = unitGraph.iterator();
                    if (unitIt != null)
                    {
                        while (unitIt.hasNext())
                        {
                            System.out.println(unitIt.next());
                        }
                    }
                }
            }
        }

        return unitGraph;
    }
}
