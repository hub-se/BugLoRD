/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.core.traces;

import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace.TraceIterator;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;


/**
 * @author Simon
 *
 */
public class RawTraceCollectorTest extends TestSettings {

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
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.core.traces.RawTraceCollector#addRawTraceToPool(java.lang.String, java.util.List)}.
	 */
	@Test
	public void testAddRawTraceToPool() throws Exception {
		RawTraceCollector collector = new RawTraceCollector(Paths.get(getStdTestDir()).resolve("test1"));
		collector.addRawTraceToPool(1, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8});
		collector.addRawTraceToPool(2, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8});
		
		//                                            0    1    2    4    4    1    2    4    4    1    2    1      3
		collector.addRawTraceToPool(3, 0, new int[] {1,2, 3,4, 5,6, 5,7, 5,7, 3,4, 5,6, 5,7, 5,7, 3,4, 5,6, 3,4, 5,6,7,8});
		
		System.out.println(collector.getGsTree());
		
		ExecutionTrace executionTrace = collector.getExecutionTraces(3).get(0);
		
		System.out.println(Arrays.toString(collector.getRawTraces(3).get(0)));
		System.out.println(Arrays.toString(executionTrace.reconstructFullTrace(collector.getIndexer())));
		System.out.println(Arrays.toString(executionTrace.reconstructFullIndexedTrace()));
		for (TraceIterator iterator = executionTrace.iterator(); iterator.hasNext();) {
			Integer integer = iterator.next();
			System.out.print(integer + ", ");
		}
		System.out.println();
		
		System.out.println(Arrays.toString(executionTrace.getCompressedTrace()));
	}
	
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.core.traces.RawTraceCollector#addRawTraceToPool(java.lang.String, java.util.List)}.
	 */
	@Test
	public void testAddRawTraceToPool2() throws Exception {
		RawTraceCollector collector = new RawTraceCollector(Paths.get(getStdTestDir()).resolve("test2"));
		collector.addRawTraceToPool(1, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8});
		collector.addRawTraceToPool(2, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8});
		
		collector.addRawTraceToPool(3, 0, new int[] {1,2, 3,4, 1,2, 3,4, 1,2, 3,4, 3,4, 5,6, 5,6, 5,7, 3,4, 5,6, 3,4, 5,6,7,8});
		
		System.out.println(collector.getGsTree());
		
		ExecutionTrace executionTrace = collector.getExecutionTraces(3).get(0);
		
		System.out.println(Arrays.toString(collector.getRawTraces(3).get(0)));
		System.out.println(Arrays.toString(executionTrace.reconstructFullTrace(collector.getIndexer())));
		System.out.println(Arrays.toString(executionTrace.reconstructFullIndexedTrace()));
		
		System.out.println(Arrays.toString(executionTrace.getCompressedTrace()));
		
		try {
			collector.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.core.traces.RawTraceCollector#addRawTraceToPool(java.lang.String, java.util.List)}.
	 */
	@Test
	public void testAddRawTraceToPool3() throws Exception {
		RawTraceCollector collector = new RawTraceCollector(Paths.get(getStdTestDir()).resolve("test3"));
		collector.addRawTraceToPool(1, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8});
		collector.addRawTraceToPool(2, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8});
		
		collector.addRawTraceToPool(3, 0, new int[] {1,2, 3,4, 5,6, 5,7, 3,4, 5,6, 3,4, 5,6,7,8, 5,6,7,8, 5,6,7,8, 5,6,7,8, 5,6,7,8});
		
		System.out.println(collector.getGsTree());
		
		ExecutionTrace executionTrace = collector.getExecutionTraces(3).get(0);
		
		System.out.println(Arrays.toString(collector.getRawTraces(3).get(0)));
		System.out.println(Arrays.toString(executionTrace.reconstructFullTrace(collector.getIndexer())));
		System.out.println(Arrays.toString(executionTrace.reconstructFullIndexedTrace()));
		
		System.out.println(Arrays.toString(executionTrace.getCompressedTrace()));
		
		try {
			collector.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
