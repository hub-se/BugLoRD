/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author Simon
 *
 */
public class CoberturaStatementEncodingTest {

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
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CoberturaStatementEncoding#generateUniqueRepresentationForStatement(int, int, int)}.
	 */
	@Test
	public void testGenerateUniqueRepresentationForStatement() throws Exception {
		for (int classId = 0; classId < Math.pow(2,CoberturaStatementEncoding.CLASS_ID_BITS); classId += 11) {
			for (int counterId = 0; counterId < Math.pow(2,CoberturaStatementEncoding.COUNTER_ID_BITS); counterId += 17) {
				testEncodingAndDecoding(classId, counterId, CoberturaStatementEncoding.NORMAL_ID);
				testEncodingAndDecoding(classId, counterId, CoberturaStatementEncoding.JUMP_ID);
				testEncodingAndDecoding(classId, counterId, 12345678);
			}
		}
	}

	private void testEncodingAndDecoding(int classId, int counterId, int specialIndicatorId) {
		long encoded = CoberturaStatementEncoding.generateUniqueRepresentationForStatement(classId, counterId, specialIndicatorId);
		Assert.assertEquals(classId, CoberturaStatementEncoding.getClassId(encoded));
		Assert.assertEquals(counterId, CoberturaStatementEncoding.getCounterId(encoded));
		Assert.assertEquals(specialIndicatorId, CoberturaStatementEncoding.getSpecialIndicatorId(encoded));
	}

}
