package se.de.hu_berlin.informatik.gen.spectra.predicates.pdg;

import jdk.nashorn.internal.runtime.regexp.joni.encoding.ObjPtr;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SootConnector {

    //private static final String JDK_PATH = "/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/jre/lib/rt.jar";
    //private static final String JDK_PATH = "/Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home/jre/lib/rt.jar";
    //private static final String JDK_PATH = Defects4J.Defects4JProperties.JAVA8_JRE.getValue() + "/lib/rt.jar;";
    //private static final String JDK_PATH = "C:\\Users\\Yuof\\.jdks\\adopt-openjdk-1.8.0_275\\jre\\lib\\rt.jar;";

    //private static final String JCE_PATH = Defects4J.Defects4JProperties.JAVA8_JRE.getValue() + "/lib/jce.jar;";

    //private static final String JDK_8_PATH = "/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/rt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/jce.jar";

    private static final Map<String, SootConnector> cache = new HashMap<>();

    public static SootConnector getInstance(String packageName, String className, String classPathExtension) {
        System.out.println("-------Initializing SootConnection------");
        return getInstance(packageName, className, classPathExtension, false);
    }

    public static SootConnector getInstance(String packageName, String className, String classPathExtension, boolean forceReload) {
        classPathExtension = classPathExtension.replace(',', ':');
        String qualifiedName = packageName + "." + className + ";" + classPathExtension;
        if (!cache.containsKey(qualifiedName) || forceReload) {
            SootConnector instance = new SootConnector(packageName, className, classPathExtension);
            cache.put(qualifiedName, instance);
        }
        return cache.get(qualifiedName);
    }

    private final String packageName;
    private final String className;



    private SootConnector(String packageName, String className, String classPathExtension) {
        this.packageName = packageName;
        this.className = className;
        init(classPathExtension, packageName, className);
    }

    @SuppressWarnings("static-access")
    private static synchronized void init(String classPathExtension, String packageName, String className) {
        long startTime = System.nanoTime();
        //System.out.println("classpath is: " + classPathExtension);
        //soot.G.v().reset(); // TODO really necessary? think about performance
        String classpath =  Defects4J.Defects4JProperties.JAVA8_JRE.getValue() + "/lib/rt.jar" + ":"
                + Defects4J.Defects4JProperties.JAVA8_JRE.getValue() + "/lib/jce.jar" + ":"
                + classPathExtension + ":";
        Options.v().set_prepend_classpath(true);
        Options.v().set_soot_classpath(classpath);
        Options.v().set_src_prec(Options.src_prec_only_class);
        Options.v().set_keep_line_number(true);
        //Options.v().set_whole_shimple(true);
        Options.v().set_whole_program(true); // important for interprocedural analysis
        //PhaseOptions.v().setPhaseOption("cg", "off");
        //Options.v().set_full_resolver(true);
        //Options.v().set_no_writeout_body_releasing(true);
        //Options.v().set_drop_bodies_after_load(false);
        //Options.v().set_allow_phantom_refs(true);
         //Options.v().set_verbose(true);
        Options.v().set_output_format(Options.output_format_none);

        Scene.v().setSootClassPath(classpath);
        Scene.v().loadBasicClasses();
        Scene.v().loadNecessaryClasses();
        SootClass c = Scene.v().forceResolve(packageName + "." + className, SootClass.BODIES);
        //SootClass c = Scene.v().loadClassAndSupport(packageName + "." + className);
        c.setApplicationClass();
        Scene.v().loadNecessaryClasses();
        //Scene.v().setMainClass(c);

        PackManager.v().getPack("cg").apply(); // builds whole CallGraph
        PackManager.v().runPacks();
        long endTime = System.nanoTime();
        System.out.println("Loading costs " + (endTime - startTime)/1000000000 + " s.");
    }

    public UnitGraph getCFGForMethodName(String methodName) {
        SootClass c = Scene.v().getSootClass(packageName + "." + className);
//        c.setApplicationClass();
//        Scene.v().loadNecessaryClasses();
        SootMethod targetMethod = c.getMethodByName(methodName);
        UnitGraph unitGraph = new ExceptionalUnitGraph(targetMethod.retrieveActiveBody());
        return unitGraph;
    }

    public UnitGraph getCFGForMethod(SootMethod method) { //newly added
//        SootClass c = Scene.v().getSootClass(packageName + "." + className);
//        c.setApplicationClass();
//        Scene.v().loadNecessaryClasses();
        UnitGraph unitGraph = null;
        if (method.getSource() != null)
            unitGraph = new ExceptionalUnitGraph(method.retrieveActiveBody());
        return unitGraph;
    }

    public Iterator<Edge> getIteratorOnCallersForMethodName(String methodName) {
        CallGraph callGraph = Scene.v().getCallGraph();
        SootClass c = Scene.v().getSootClass(packageName + "." + className);
        c.setApplicationClass();
        Scene.v().loadNecessaryClasses();
        Iterator<Edge> i = callGraph.edgesInto(c.getMethodByName(methodName));
        return i;
    }

    public Iterator<Edge> getIteratorOnCallersForMethod(SootMethod method){ //newly added
        CallGraph callGraph = Scene.v().getCallGraph();
        SootClass c = Scene.v().getSootClass(packageName + "." + className);
        c.setApplicationClass();
        Scene.v().loadNecessaryClasses();
        Iterator<Edge> i = callGraph.edgesInto(method);
        return i;
    }

    public Iterator<Edge> getIteratorOnCallsOutOfMethodName(String methodName) {
        CallGraph callGraph = Scene.v().getCallGraph();
        SootClass c = Scene.v().getSootClass(packageName + "." + className);
        c.setApplicationClass();
        Scene.v().loadNecessaryClasses();
        Iterator<Edge> i = callGraph.edgesOutOf(c.getMethodByName(methodName));
        return i;
    }

    public SootMethod loadMethod(String methodName) {  //newly added
        SootClass c = Scene.v().getSootClass(packageName + "." + className);
        c.setApplicationClass();
        Scene.v().loadNecessaryClasses();
        SootMethod targetMethod = c.getMethodByName(methodName);
        return targetMethod;
    }

    public List<SootMethod> getAllMethods() {
        SootClass c = Scene.v().getSootClass(packageName + "." + className);
        //SootClass c = Scene.v().getSootClass(packageName + "." + className);
//        c.setApplicationClass();
//        Scene.v().loadNecessaryClasses();
        return c.getMethods();
    }
	/*
	public static void main(String args[]) {
		String packageName = "debug";
		String className = "TestAfter";
		String pathExtensions = "/Users/minxing/Documents/eclipse-workspace/jpf-shadow-plus/build/tests/";
		String methodName = "test";

		SootConnector sc =SootConnector.getInstance(packageName, className, pathExtensions);
	}*/
}

