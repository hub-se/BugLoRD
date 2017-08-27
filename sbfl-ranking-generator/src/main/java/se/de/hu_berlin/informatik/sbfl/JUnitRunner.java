package se.de.hu_berlin.informatik.sbfl;

import static org.junit.internal.runners.rules.RuleMemberValidator.CLASS_RULE_METHOD_VALIDATOR;
import static org.junit.internal.runners.rules.RuleMemberValidator.CLASS_RULE_VALIDATOR;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.evosuite.shaded.org.mockito.internal.debugging.WarningsCollector;
import org.evosuite.shaded.org.mockito.internal.runners.RunnerFactory;
import org.evosuite.shaded.org.mockito.internal.runners.RunnerImpl;
import org.evosuite.shaded.org.mockito.internal.util.junit.JUnitFailureHacker;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;
import org.junit.validator.AnnotationsValidator;
import org.junit.validator.PublicClassValidator;
import org.junit.validator.TestClassValidator;

public class JUnitRunner extends Runner implements Filterable {

	private static final List<TestClassValidator> VALIDATORS = Arrays.asList(
            new AnnotationsValidator(), new PublicClassValidator());

	private static final boolean VERBOSE = false;
	
	private final RunnerImpl runner;
	private final TestClass testClass;

    public JUnitRunner(Class<?> klass) throws InvocationTargetException, InitializationError {
        runner = new RunnerFactory().create(klass);
        this.testClass = createTestClass(klass);
        validate();
    }
    
    protected TestClass createTestClass(Class<?> testClass) {
        return new TestClass(testClass);
    }

    @Override
    public void run(final RunNotifier notifier) {
    	System.out.println("running....");
    	if (VERBOSE) {
    		//a listener that changes the failure's exception in a very hacky way...
            RunListener listener = new RunListener() {
                
                WarningsCollector warningsCollector;
                           
                @Override
                public void testStarted(Description description) throws Exception {
                    warningsCollector = new WarningsCollector();
                }
                
                @Override 
                public void testFailure(final Failure failure) throws Exception {       
                    String warnings = warningsCollector.getWarnings();
                    new JUnitFailureHacker().appendWarnings(failure, warnings);                              
                }
            };

            notifier.addFirstListener(listener);
    	}
    	
    	runner.run(notifier);
    }

    @Override
    public Description getDescription() {
        return runner.getDescription();
    }

	public void filter(Filter filter) throws NoTestsRemainException {
        //filter is required because without it UnrootedTests show up in Eclipse
		runner.filter(filter);
	}
	
	/**
     * Returns a {@link TestClass} object wrapping the class to be executed.
     */
    public final TestClass getTestClass() {
        return testClass;
    }
	
	private void validate() throws InitializationError {
        List<Throwable> errors = new ArrayList<Throwable>();
        collectInitializationErrors(errors);
        if (!errors.isEmpty()) {
            throw new InitializationError(errors);
        }
    }
	
	/**
     * Adds to {@code errors} a throwable for each problem noted with the test class (available from {@link #getTestClass()}).
     * Default implementation adds an error for each method annotated with
     * {@code @BeforeClass} or {@code @AfterClass} that is not
     * {@code public static void} with no arguments.
     */
    protected void collectInitializationErrors(List<Throwable> errors) {
        validatePublicVoidNoArgMethods(BeforeClass.class, true, errors);
        validatePublicVoidNoArgMethods(AfterClass.class, true, errors);
        validateClassRules(errors);
        applyValidators(errors);
    }
    
    private void applyValidators(List<Throwable> errors) {
        if (getTestClass().getJavaClass() != null) {
            for (TestClassValidator each : VALIDATORS) {
                errors.addAll(each.validateTestClass(getTestClass()));
            }
        }
    }
    
    /**
     * Adds to {@code errors} if any method in this class is annotated with
     * {@code annotation}, but:
     * <ul>
     * <li>is not public, or
     * <li>takes parameters, or
     * <li>returns something other than void, or
     * <li>is static (given {@code isStatic is false}), or
     * <li>is not static (given {@code isStatic is true}).
     * </ul>
     */
    protected void validatePublicVoidNoArgMethods(Class<? extends Annotation> annotation,
            boolean isStatic, List<Throwable> errors) {
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(annotation);

        for (FrameworkMethod eachTestMethod : methods) {
            eachTestMethod.validatePublicVoidNoArg(isStatic, errors);
        }
    }
    
    private void validateClassRules(List<Throwable> errors) {
        CLASS_RULE_VALIDATOR.validate(getTestClass(), errors);
        CLASS_RULE_METHOD_VALIDATOR.validate(getTestClass(), errors);
    }
    
}