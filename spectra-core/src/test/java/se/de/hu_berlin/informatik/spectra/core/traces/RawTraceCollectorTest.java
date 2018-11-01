/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author Simon
 *
 */
public class RawTraceCollectorTest {

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
		RawTraceCollector collector = new RawTraceCollector();
		collector.addRawTraceToPool("test1", 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8});
		collector.addRawTraceToPool("test2", 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8});
		
		//                                               0    1    2    1    2    4    1    2    1      3
		collector.addRawTraceToPool("test3", 0, new int[] {1,2, 3,4, 5,6, 3,4, 5,6, 5,7, 3,4, 5,6, 3,4, 5,6,7,8});
		
		System.out.println(collector.getGsTree());
		
		ExecutionTrace executionTrace = collector.getExecutionTraces("test3").get(0);
		
		System.out.println(Arrays.toString(collector.getRawTraces("test3").get(0)));
		System.out.println(Arrays.toString(executionTrace.reconstructFullTrace(collector.getIndexer())));
		System.out.println(Arrays.toString(executionTrace.reconstructFullIndexedTrace()));
		
		System.out.println(Arrays.toString(executionTrace.getCompressedTrace()));
	}
	
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.core.traces.RawTraceCollector#addRawTraceToPool(java.lang.String, java.util.List)}.
	 */
	@Test
	public void testAddRawTraceToPool2() throws Exception {
		RawTraceCollector collector = new RawTraceCollector();
		collector.addRawTraceToPool("test1", 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8});
		collector.addRawTraceToPool("test2", 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8});
		
		collector.addRawTraceToPool("test3", 0, new int[] {1,2, 3,4, 3,4, 3,4, 5,6, 5,6, 5,7, 3,4, 5,6, 3,4, 5,6,7,8});
		
		System.out.println(collector.getGsTree());
		
		ExecutionTrace executionTrace = collector.getExecutionTraces("test3").get(0);
		
		System.out.println(Arrays.toString(collector.getRawTraces("test3").get(0)));
		System.out.println(Arrays.toString(executionTrace.reconstructFullTrace(collector.getIndexer())));
		System.out.println(Arrays.toString(executionTrace.reconstructFullIndexedTrace()));
		
		System.out.println(Arrays.toString(executionTrace.getCompressedTrace()));
	}
	
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.core.traces.RawTraceCollector#addRawTraceToPool(java.lang.String, java.util.List)}.
	 */
	@Test
	public void testAddRawTraceToPool3() throws Exception {
		RawTraceCollector collector = new RawTraceCollector();
		collector.addRawTraceToPool("test1", 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8});
		collector.addRawTraceToPool("test2", 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8});
		
		collector.addRawTraceToPool("test3", 0, new int[] {1,2, 3,4, 5,6, 5,7, 3,4, 5,6, 3,4, 5,6,7,8, 5,6,7,8});
		
		System.out.println(collector.getGsTree());
		
		ExecutionTrace executionTrace = collector.getExecutionTraces("test3").get(0);
		
		System.out.println(Arrays.toString(collector.getRawTraces("test3").get(0)));
		System.out.println(Arrays.toString(executionTrace.reconstructFullTrace(collector.getIndexer())));
		System.out.println(Arrays.toString(executionTrace.reconstructFullIndexedTrace()));
		
		System.out.println(Arrays.toString(executionTrace.getCompressedTrace()));
	}

}
