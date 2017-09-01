package se.de.hu_berlin.informatik.junittestutils.testrunner.running;

import org.junit.runners.model.InitializationError;

import junit.framework.JUnit4TestAdapter;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class SeparateClassLoaderTestAdapter extends JUnit4TestAdapter {

	public SeparateClassLoaderTestAdapter(Class<?> clazz, ClassLoader testClassLoader) throws InitializationError {
        super(getFromTestClassloader(clazz, testClassLoader));
    }
	
	public SeparateClassLoaderTestAdapter(String clazz, ClassLoader testClassLoader) throws InitializationError {
        super(getFromTestClassloader(clazz, testClassLoader));
    }

    public static Class<?> getFromTestClassloader(Class<?> clazz, ClassLoader testClassLoader) throws InitializationError {
        return getFromTestClassloader(clazz.getName(), testClassLoader);
    }
    
    public static Class<?> getFromTestClassloader(String clazz, ClassLoader testClassLoader) throws InitializationError {
        try {
            return Class.forName(clazz, true, testClassLoader);
        } catch (ClassNotFoundException e) {
        	Log.err(SeparateClassLoaderTestAdapter.class, "Class '%s' not found.", clazz);
            throw new InitializationError(e);
        }
    }
    
}