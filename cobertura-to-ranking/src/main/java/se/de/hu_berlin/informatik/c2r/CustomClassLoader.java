package se.de.hu_berlin.informatik.c2r;

import java.net.URL;

import java.net.URLClassLoader;
import java.util.List;

import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class CustomClassLoader extends URLClassLoader {

    private ChildClassLoader childClassLoader;
    private boolean debug = false;

    public CustomClassLoader(List<URL> classpath, boolean debug) {
        super(new URL[0], Thread.currentThread().getContextClassLoader());
        URL[] urls = classpath.toArray(new URL[classpath.size()]);
        this.debug = debug;
        if (debug) {
        	childClassLoader = new DebugChildClassLoader( urls, new DetectClass(this.getParent()) );
        } else {
        	childClassLoader = new ChildClassLoader( urls, new DetectClass(this.getParent()) );
        }
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
        	if (debug) {
        		Log.out(this, "Loading class: '%s'...", name);
        	}
            return childClassLoader.findClass(name);
        } catch( ClassNotFoundException e ) {
        	if (debug) {
        		Log.out(this, "Loading class from super: '%s'.", name);
        	}
            return super.loadClass(name, resolve);
        }
    }
    

	private static class DebugChildClassLoader extends ChildClassLoader {

        private DetectClass realParent;

        public DebugChildClassLoader( URL[] urls, DetectClass realParent ) {
            super(urls, null);
            this.realParent = realParent;
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
        	Class<?> loaded = findloadedClassinSuper(name);
            if( loaded != null ) {
            	Log.out(this, "Found loaded class: '%s'.", name);
            	Log.out(this, "Found loaded class path: '%s'.", loaded.getResource(loaded.getSimpleName() + ".class"));
                return loaded;
            }
            
            try {
                loaded = findClassinSuper(name);
                Log.out(this, "Found class in given URLs: '%s'.", name);
            	Log.out(this, "Found class path in given URLs: '%s'.", loaded.getResource(loaded.getSimpleName() + ".class"));
            	return loaded;
            } catch( ClassNotFoundException e ) {
            	loaded = realParent.loadClass(name);
                Log.out(this, "Loaded class from parent: '%s'.", name);
            	Log.out(this, "Loaded class path from parent: '%s'.", loaded.getResource(loaded.getSimpleName() + ".class"));
            	return loaded;
            }
        }
        
    }
	
	private static class ChildClassLoader extends URLClassLoader {

        private DetectClass realParent;

        public ChildClassLoader( URL[] urls, DetectClass realParent ) {
            super(urls, null);
            this.realParent = realParent;
        }

        public Class<?> findClassinSuper(String name) throws ClassNotFoundException {
        	return super.findClass(name);
        }
        
        public Class<?> findloadedClassinSuper(String name) throws ClassNotFoundException {
        	return super.findLoadedClass(name);
        }
        
        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
        	Class<?> loaded = findloadedClassinSuper(name);
            if( loaded != null ) {
                return loaded;
            }
            
            try {
            	return findClassinSuper(name);
            } catch( ClassNotFoundException e ) {
            	return realParent.loadClass(name);
            }
        }
        
    }

    private static class DetectClass extends ClassLoader {

        public DetectClass(ClassLoader parent) {
            super(parent);
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            return super.findClass(name);
        }

    }

}
