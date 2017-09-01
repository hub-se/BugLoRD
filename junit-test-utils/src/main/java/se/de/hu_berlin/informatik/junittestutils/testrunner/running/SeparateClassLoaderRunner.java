package se.de.hu_berlin.informatik.junittestutils.testrunner.running;

import java.util.ArrayList;
import java.util.List;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class SeparateClassLoaderRunner extends BlockJUnit4ClassRunner {

    private List<FrameworkMethod> methods;

	public SeparateClassLoaderRunner(Class<?> clazz, FrameworkMethod method, ClassLoader testClassLoader) throws InitializationError {
        super(getFromTestClassloader(clazz, testClassLoader));
        if (methods == null) {
    		methods = new ArrayList<>(1);
    	}
        this.methods.add(method);
    }

    private static Class<?> getFromTestClassloader(Class<?> clazz, ClassLoader testClassLoader) throws InitializationError {
        try {
            return Class.forName(clazz.getName(), true, testClassLoader);
        } catch (ClassNotFoundException e) {
        	Log.err(SeparateClassLoaderRunner.class, "Class '%s' not found.", clazz.getName());
            throw new InitializationError(e);
        }
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
    	if (methods == null) {
    		return super.computeTestMethods();
    	} else {
    		return methods;
    	}
    }
    
}