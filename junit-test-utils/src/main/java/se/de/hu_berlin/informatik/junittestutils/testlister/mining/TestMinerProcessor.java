package se.de.hu_berlin.informatik.junittestutils.testlister.mining;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.tools.ant.taskdefs.optional.junit.CustomJUnit4TestAdapterCache;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import se.de.hu_berlin.informatik.java7.testrunner.TestWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.ProcessorSocket;

public class TestMinerProcessor extends AbstractProcessor<String, TestWrapper> {

	private static final String JUNIT_4_TEST_ADAPTER = "junit.framework.JUnit4TestAdapter";
	
	private ClassLoader testClassLoader;

	private boolean skipNonTests;

	public TestMinerProcessor(ClassLoader testClassLoader, boolean skipNonTests) {
		super(testClassLoader);
		this.testClassLoader = testClassLoader;
		this.skipNonTests = skipNonTests;
	}

	@Override
	public TestWrapper processItem(String className, ProcessorSocket<String, TestWrapper> socket) {
		boolean junit4;
	    
		try {
			Class<?> testClass = null;
			if (testClassLoader == null) {
				testClass = Class.forName(className);
			} else {
				testClass = Class.forName(className, true,
						testClassLoader);
			}

			Class<?> junit4TestAdapterClass = null;
			Class<?> junit4TestAdapterCacheClass = null;

			if (junit.framework.TestCase.class.isAssignableFrom(testClass)) {
				// Do not use JUnit 4 API for running JUnit 3.x
				// tests - it is not able to run individual test
				// methods.
				//
				// Technical details:
				// org.junit.runner.Request.method(Class, String).getRunner()
				// would return a runner which always executes all
				// test methods. The reason is that the Runner would be
				// an instance of class
				// org.junit.internal.runners.OldTestClassRunner
				// that does not implement interface Filterable - so it
				// is unable to filter out test methods not matching
				// the requested name.
			} else {
				// Check for JDK 5 first. Will *not* help on JDK 1.4
				// if only junit-4.0.jar in CP because in that case
				// linkage of whole task will already have failed! But
				// will help if CP has junit-3.8.2.jar:junit-4.0.jar.

				// In that case first C.fN will fail with CNFE and we
				// will avoid UnsupportedClassVersionError.

				try {
					Class.forName("java.lang.annotation.Annotation");
					junit4TestAdapterCacheClass = Class.forName("org.apache.tools.ant.taskdefs.optional.junit.CustomJUnit4TestAdapterCache");
					if (testClassLoader == null) {
						junit4TestAdapterClass =
								Class.forName(JUNIT_4_TEST_ADAPTER);
					} else {
						JUnit4TestAdapter cache = new JUnit4TestAdapter(testClass, CustomJUnit4TestAdapterCache.getInstance());
						cache.getTests();
						junit4TestAdapterClass =
								Class.forName(JUNIT_4_TEST_ADAPTER,
										true, testClassLoader);
					}
				} catch (final ClassNotFoundException e) {
					Log.err(this, e, "class not found...");
				}
			}
			junit4 = junit4TestAdapterClass != null;

			if (skipNonTests) {
				if (!containsTests(testClass, junit4)) {
					return null;
				}
			}

			List<Pair<String, String>> methods = null;
			if (junit4) {
				// Let's use it!
				methods = getJUnit4TestMethods(testClass, junit4TestAdapterClass, junit4TestAdapterCacheClass, true);
			} else {
				// Use JUnit 3.
				methods = getJUnit3TestMethods(testClass, true);
			}

			for (Pair<String,String> method : methods) {
				socket.produce(new TestWrapper(method.first(), method.second(), testClassLoader));
			}

		} catch (final Throwable e) {
			Log.err(this, e, "Exception while mining tests...");
		}

		return null;

	}
	
	private List<Pair<String, String>> getJUnit3TestMethods(Class<?> testClass, boolean debug) {
		List<Pair<String,String>> testMethods = new ArrayList<>();
        String testClassName = testClass.getCanonicalName();

        // check if we have any inner classes that contain suitable test methods
        for (final Class<?> innerClass : testClass.getDeclaredClasses()) {
        	testMethods.addAll(getJUnit3TestMethods(innerClass, false));
        }
        
        if (Modifier.isAbstract(testClass.getModifiers()) || Modifier.isInterface(testClass.getModifiers())) {
            // can't instantiate class (inner classes may have returned tests, though, I guess?)
            return testMethods;
        }
        
    	if (!TestCase.class.isAssignableFrom(testClass)) {
    		//a test we think is JUnit3 but does not extend TestCase. Can't really be a test.
    		return Collections.emptyList();
    	}

    	for (final Method m : testClass.getMethods()) {
    		// check if JUnit3 class have public or protected no-args methods starting with names starting with test
    		if (m.getName().startsWith("test") && m.getParameterTypes().length == 0
    				&& (Modifier.isProtected(m.getModifiers()) || Modifier.isPublic(m.getModifiers()))) {
    			testMethods.add(new Pair<>(testClassName, m.getName()));
    		}
//    		// check if JUnit3 or JUnit4 test have a public or protected, static,
//    		// no-args 'suite' method
//    		if (m.getName().equals("suite") && m.getParameterTypes().length == 0
//    				&& (Modifier.isProtected(m.getModifiers()) || Modifier.isPublic(m.getModifiers()))
//    				&& Modifier.isStatic(m.getModifiers())) {
//    			// ignore test suite methods?!
//    			//testMethods.add(new Pair<>(testClassName, m.getName()));
//    		}
    	}
    	
    	if (debug) {
    		if (testMethods.isEmpty()) {
    			Log.warn(this, "No Tests in class '%s'.", testClassName);
    		}
    	}

        return testMethods;
	}

	private List<Pair<String, String>> getJUnit4TestMethods(Class<?> testClass, Class<?> junit4TestAdapterClass,
			Class<?> junit4TestAdapterCacheClass, boolean debug) throws IllegalAccessException, IllegalArgumentException, 
	InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException {
		List<Pair<String,String>> testMethods = new ArrayList<>();
        String testClassName = testClass.getCanonicalName();

        // check if we have any inner classes that contain suitable test methods
        for (final Class<?> innerClass : testClass.getDeclaredClasses()) {
        	testMethods.addAll(getJUnit4TestMethods(innerClass, junit4TestAdapterClass, junit4TestAdapterCacheClass, false));
        	testMethods.addAll(getJUnit3TestMethods(innerClass, false));
        }
        
        if (Modifier.isAbstract(testClass.getModifiers()) || Modifier.isInterface(testClass.getModifiers())) {
            // can't instantiate class (inner classes may have returned tests, though, I guess?)
            return testMethods;
        }
        
        try {
    		Class.forName("org.junit.Test");
    	} catch (final ClassNotFoundException e) {
    		// odd - we think we're JUnit4 but don't support the test annotation. We therefore can't have any tests!
    		return Collections.emptyList();
    	}

        // get Tests...
        Class<?>[] formalParams;
        Object[] actualParams;

        formalParams = new Class[] {Class.class, Class.forName("junit.framework.JUnit4TestAdapterCache")};
        actualParams = new Object[] {testClass, junit4TestAdapterCacheClass.getMethod("getInstance").invoke(null)};

        JUnit4TestAdapter cache =
        		(JUnit4TestAdapter) junit4TestAdapterClass
        		.getConstructor(formalParams).
        		newInstance(actualParams);

        for (Test test : cache.getTests()) {
        	if (test.toString().equals("No Tests")) {
        		if (debug) {
        			Log.warn(this, "No Tests in class '%s'.", testClassName);
        		}
        		continue;
        	}
        	if (test.toString().startsWith("initializationError(")) {
        		if (debug) {
        			Log.err(this, "Test could not be initialized: %s", test.toString());
        		}
        		continue;
        	}

        	String temp = test.toString();
        	if (temp.contains("(")) {
        		temp = temp.substring(0, temp.indexOf('('));
        	} else {
        		if (debug) {
        			Log.err(this, "Test '%s' in class '%s' not parseable.", temp, testClassName);
        		}
        		continue;
        	}

        	testMethods.add(new Pair<>(testClassName, temp));
        }
        return testMethods;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static boolean containsTests(final Class<?> testClass, final boolean isJUnit4) {
        Class testAnnotation = null;
        Class suiteAnnotation = null;
        Class runWithAnnotation = null;

        try {
            testAnnotation = Class.forName("org.junit.Test");
        } catch (final ClassNotFoundException e) {
            if (isJUnit4) {
                // odd - we think we're JUnit4 but don't support the test annotation. We therefore can't have any tests!
                return false;
            }
            // else... we're a JUnit3 test and don't need the annotation
        }

        try {
            suiteAnnotation = Class.forName("org.junit.Suite.SuiteClasses");
        } catch(final ClassNotFoundException ex) {
            // ignore - we don't have this annotation so make sure we don't check for it
        }
        try {
            runWithAnnotation = Class.forName("org.junit.runner.RunWith");
        } catch(final ClassNotFoundException ex) {
            // also ignore as this annotation doesn't exist so tests can't use it
        }


        if (!isJUnit4 && !TestCase.class.isAssignableFrom(testClass)) {
            //a test we think is JUnit3 but does not extend TestCase. Can't really be a test.
            return false;
        }

        // check if we have any inner classes that contain suitable test methods
        for (final Class<?> innerClass : testClass.getDeclaredClasses()) {
            if (containsTests(innerClass, isJUnit4) || containsTests(innerClass, !isJUnit4)) {
                return true;
            }
        }

        if (Modifier.isAbstract(testClass.getModifiers()) || Modifier.isInterface(testClass.getModifiers())) {
            // can't instantiate class and no inner classes are tests either
            return false;
        }

        if (isJUnit4) {
             if (suiteAnnotation != null && testClass.getAnnotation(suiteAnnotation) != null) {
                // class is marked as a suite. Let JUnit try and work its magic on it.
                return true;
             }
            if (runWithAnnotation != null && testClass.getAnnotation(runWithAnnotation) != null) {
                /* Class is marked with @RunWith. If this class is badly written (no test methods, multiple
                 * constructors, private constructor etc) then the class is automatically run and fails in the
                 * IDEs I've tried... so I'm happy handing the class to JUnit to try and run, and let JUnit
                 * report a failure if a bad test case is provided. Trying to do anything else is likely to
                 * result in us filtering out cases that could be valid for future versions of JUnit so would
                 * just increase future maintenance work.
                 */
                return true;
            }
        }

        for (final Method m : testClass.getMethods()) {
            if (isJUnit4) {
                // check if suspected JUnit4 classes have methods with @Test annotation
                if (m.getAnnotation(testAnnotation) != null) {
                    return true;
                }
            } else {
                // check if JUnit3 class have public or protected no-args methods starting with names starting with test
                if (m.getName().startsWith("test") && m.getParameterTypes().length == 0
                        && (Modifier.isProtected(m.getModifiers()) || Modifier.isPublic(m.getModifiers()))) {
                    return true;
                }
            }
            // check if JUnit3 or JUnit4 test have a public or protected, static,
            // no-args 'suite' method
            if (m.getName().equals("suite") && m.getParameterTypes().length == 0
                    && (Modifier.isProtected(m.getModifiers()) || Modifier.isPublic(m.getModifiers()))
                    && Modifier.isStatic(m.getModifiers())) {
                return true;
            }
        }

        // no test methods found
        return false;
    }
	
}
