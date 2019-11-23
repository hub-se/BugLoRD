/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import java.util.HashSet;
import java.util.Set;

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
	
	private static Set<Long> values;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		values = new HashSet<>();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		values = null;
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
//		testEncodingAndDecoding(0, 0, 1034, 0);
//		testEncodingAndDecoding(0, 17, 0, 0);
		
		for (int classId = 0; classId < Math.pow(2,CoberturaStatementEncoding.CLASS_ID_BITS); classId += 11) {
			for (int counterId = 0; counterId < Math.pow(2,CoberturaStatementEncoding.COUNTER_ID_BITS); counterId += 17) {
				testEncodingAndDecoding(classId, counterId);
				for (int classId2 = 0; classId2 < Math.pow(2,CoberturaStatementEncoding.CLASS_ID_BITS); classId2 += 1121) {
					for (int counterId2 = 0; counterId2 < Math.pow(2,CoberturaStatementEncoding.COUNTER_ID_BITS); counterId2 += 11127) {
						testEncodingAndDecoding(classId, counterId, classId2, counterId2);
					}
				}
			}
		}
		testEncodingAndDecoding(0, 0);
		testEncodingAndDecoding((int)Math.pow(2,CoberturaStatementEncoding.CLASS_ID_BITS)-1,
				(int)Math.pow(2,CoberturaStatementEncoding.COUNTER_ID_BITS)-1);
	}

	private void testEncodingAndDecoding(int classId, int counterId) {
		testEncodingAndDecoding(classId, counterId, CoberturaStatementEncoding.NORMAL_ID);
		testEncodingAndDecoding(classId, counterId, CoberturaStatementEncoding.JUMP_ID);
		testEncodingAndDecoding(classId, counterId, 12345678);
	}
	
	private void testEncodingAndDecoding(int classId, int counterId, int classId2, int counterId2) {
		long encoded = CoberturaStatementEncoding.generateUniqueRepresentationForTwoStatements(
				CoberturaStatementEncoding.generateUniqueRepresentationForStatement(classId, counterId), 
				CoberturaStatementEncoding.generateUniqueRepresentationForStatement(classId2, counterId2));
//		Assert.assertTrue(
//				String.format("%d, %d, %d, %d, %d, %d, %d", classId, counterId, classId2, counterId2, encoded, 
//						CoberturaStatementEncoding.generateUniqueRepresentationForStatement(classId, counterId),
//						CoberturaStatementEncoding.generateUniqueRepresentationForStatement(classId2, counterId2)), 
//				!values.contains(encoded));
//		values.add(encoded);
		Assert.assertEquals(
//				String.format("%d, %d, %d, %d, %d, %d, %d", classId, counterId, classId2, counterId2, encoded, 
//				CoberturaStatementEncoding.generateUniqueRepresentationForStatement(classId, counterId),
//				CoberturaStatementEncoding.generateUniqueRepresentationForStatement(classId2, counterId2)), 
				classId, CoberturaStatementEncoding.getFirstClassId(encoded));
		Assert.assertEquals(
//				String.format("%d, %d, %d, %d, %d, %d, %d", classId, counterId, classId2, counterId2, encoded, 
//				CoberturaStatementEncoding.generateUniqueRepresentationForStatement(classId, counterId),
//				CoberturaStatementEncoding.generateUniqueRepresentationForStatement(classId2, counterId2)), 
				counterId, CoberturaStatementEncoding.getFirstCounterId(encoded));
		Assert.assertEquals(
//				String.format("%d, %d, %d, %d, %d, %d, %d", classId, counterId, classId2, counterId2, encoded, 
//				CoberturaStatementEncoding.generateUniqueRepresentationForStatement(classId, counterId),
//				CoberturaStatementEncoding.generateUniqueRepresentationForStatement(classId2, counterId2)), 
				classId2, CoberturaStatementEncoding.getLastClassId(encoded));
		Assert.assertEquals(
//				String.format("%d, %d, %d, %d, %d, %d, %d", classId, counterId, classId2, counterId2, encoded, 
//				CoberturaStatementEncoding.generateUniqueRepresentationForStatement(classId, counterId),
//				CoberturaStatementEncoding.generateUniqueRepresentationForStatement(classId2, counterId2)), 
				counterId2, CoberturaStatementEncoding.getLastCounterId(encoded));
	}

	private void testEncodingAndDecoding(int classId, int counterId, int specialIndicatorId) {
		int encoded = CoberturaStatementEncoding.generateUniqueRepresentationForStatement(classId, counterId);
		Assert.assertEquals(classId, CoberturaStatementEncoding.getClassId(encoded));
		Assert.assertEquals(counterId, CoberturaStatementEncoding.getCounterId(encoded));
//		Assert.assertEquals(specialIndicatorId, CoberturaStatementEncoding.getSpecialIndicatorId(encoded));
	}

}
