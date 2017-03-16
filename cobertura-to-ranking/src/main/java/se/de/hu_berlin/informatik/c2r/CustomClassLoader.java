package se.de.hu_berlin.informatik.c2r;

import java.net.URL;

import java.net.URLClassLoader;

import java.util.List;

import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class CustomClassLoader extends ClassLoader {

    private ChildClassLoader childClassLoader;

    public CustomClassLoader(List<URL> classpath) {
        super(Thread.currentThread().getContextClassLoader());
        URL[] urls = classpath.toArray(new URL[classpath.size()]);
        childClassLoader = new ChildClassLoader( urls, new DetectClass(this.getParent()) );
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
        	Log.out(this, "Loading class: '%s'...", name);
            return childClassLoader.findClass(name);
        } catch( ClassNotFoundException e ) {
        	Log.out(this, "Loading class from super: '%s'...", name);
            return super.loadClass(name, resolve);
        }
    }

	private static class ChildClassLoader extends URLClassLoader {

        private DetectClass realParent;

        public ChildClassLoader( URL[] urls, DetectClass realParent ) {
            super(urls, null);
            this.realParent = realParent;
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
        	Log.out(this, "Finding class: '%s'...", name);
        	Class<?> loaded = super.findLoadedClass(name);
            if( loaded != null ) {
            	Log.out(this, "Found class: '%s'...", name);
            	Log.out(this, "Found class path: '%s'...", loaded.getResource(loaded.getSimpleName() + ".class"));
                return loaded;
            }
            
            try {
                Log.out(this, "Finding class from super: '%s'...", name);
                loaded = super.findClass(name);
                Log.out(this, "Found class from super: '%s'...", name);
            	Log.out(this, "Found class path: '%s'...", loaded.getResource(loaded.getSimpleName() + ".class"));
            	return loaded;
            } catch( ClassNotFoundException e ) {
            	Log.out(this, "Loading class from super: '%s'...", name);
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
        	Log.out(this, "Finding class: '%s'...", name);
            return super.findClass(name);
        }

    }

}
