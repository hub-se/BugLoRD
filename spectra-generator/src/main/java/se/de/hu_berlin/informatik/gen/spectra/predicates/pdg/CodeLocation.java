package se.de.hu_berlin.informatik.gen.spectra.predicates.pdg;

import soot.SootMethod;
import soot.Unit;

public class CodeLocation {

    public Unit unit;
    public String className;
    public String methodName;
    public SootMethod method;
    public SootConnector sootConnector;

    public CodeLocation(Unit unit, String className, SootMethod method, SootConnector sootConnector)
    {
        this.unit = unit;
        this.className = className;
        this.methodName = method.getName();
        this.method = method;
        this.sootConnector = sootConnector;
    }

    public String getLocationString()
    {
        return this.className + ":" + this.unit.getJavaSourceStartLineNumber();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof CodeLocation) {
            CodeLocation other = (CodeLocation) obj;
            if (this.className.equals(other.className) &&
            this.unit.getJavaSourceStartLineNumber() == other.unit.getJavaSourceStartLineNumber())
                return true;
        }
        return super.equals(obj);
    }
}
