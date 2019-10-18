/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.core.traces;

import java.nio.file.Paths;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.IntTraceIterator;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;


/**
 * @author Simon
 *
 */
public class ExecutionTraceTest extends TestSettings {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace#ExecutionTrace(se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedIntArrayQueue, boolean)}.
	 */
	@Test
	public void testExecutionTrace1() throws Exception {
		BufferedIntArrayQueue rawTrace = new BufferedIntArrayQueue(
				Paths.get(getStdTestDir()).resolve("execTraceTest").toFile(), "t1", false);
		rawTrace.add(1);
		rawTrace.add(1);
		ExecutionTrace shortTrace = new ExecutionTrace(rawTrace, true);
		
		IntTraceIterator traceIterator = shortTrace.iterator();
		int count = 0;
		while (traceIterator.hasNext()) {
			Assert.assertEquals(1, traceIterator.next());
			++count;
		}
		Assert.assertEquals(shortTrace.size(), count);
	}

	private static final int REPETITIONS = 10;

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace#ExecutionTrace(se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedIntArrayQueue, boolean)}.
	 */
	@Test
	public void testExecutionTrace2() throws Exception {
		BufferedIntArrayQueue rawTrace = new BufferedIntArrayQueue(
				Paths.get(getStdTestDir()).resolve("execTraceTest").toFile(), "t2", false);
		for (int repCount = 0; repCount < REPETITIONS; ++repCount) {
			for (int i = 0; i < 10; ++i) {
				rawTrace.add(i);	
			}
		}

		ExecutionTrace eTrace = new ExecutionTrace(rawTrace, true);
		
		IntTraceIterator traceIterator = eTrace.iterator();
		int count = 0;
		for (int repCount = 0; repCount < REPETITIONS; ++repCount) {
			for (int i = 0; i < 10; ++i) {
				Assert.assertTrue(traceIterator.hasNext());
				Assert.assertEquals(i, traceIterator.next());
				++count;
			}
		}
		Assert.assertFalse(traceIterator.hasNext());

		Assert.assertEquals(count, eTrace.size());
	}
	
}
