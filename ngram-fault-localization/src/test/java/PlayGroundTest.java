import org.junit.*;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemErrRule;
import se.de.hu_berlin.informatik.spectra.core.*;
import se.de.hu_berlin.informatik.spectra.core.count.CountSpectra;
import se.de.hu_berlin.informatik.spectra.core.count.CountTrace;
import se.de.hu_berlin.informatik.spectra.core.hit.HierarchicalHitTrace;
import se.de.hu_berlin.informatik.spectra.core.hit.HitTrace;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;
import se.de.hu_berlin.informatik.spectra.core.traces.RawIntTraceCollector;
import se.de.hu_berlin.informatik.spectra.core.traces.SimpleIntIndexerCompressed;
import se.de.hu_berlin.informatik.spectra.provider.cobertura.CoberturaSpectraProviderFactory;
import se.de.hu_berlin.informatik.spectra.provider.cobertura.xml.CoberturaCountXMLProvider;
import se.de.hu_berlin.informatik.spectra.provider.cobertura.xml.CoberturaXMLProvider;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedLongArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CoberturaStatementEncoding;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.TraceIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.CompressedIntegerTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.longs.CompressedLongTrace;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.ZipException;

import static org.junit.Assert.*;

/**
 * @author tdh
 *
 */
public class PlayGroundTest extends TestSettings {

	/**
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
	}

	/**
	 */
	@AfterClass
	public static void tearDownAfterClass() {
//		deleteTestOutputs();
	}

	/**
	 */
	@Before
	public void setUp() {
//		Log.off();
	}

	/**
	 */
	@After
	public void tearDown() {
//		deleteTestOutputs();
	}

	@Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
	
	@Rule
	public final SystemErrRule systemErrRule = new SystemErrRule().enableLog();

	private int[] s(int... numbers) {
		return numbers;
	}
	
	private int[][] rt(int... numbers) {
		int[][] result = new int[numbers.length][];
		for (int i = 0; i < numbers.length; ++i) {
			result[i] = new int[] {0,numbers[i],0};
		}
		return result;
	}
	
	private CompressedLongTrace asList(Path outputDir, int[][] rt) {
		BufferedLongArrayQueue list = new BufferedLongArrayQueue(
				outputDir.toFile(), String.valueOf(UUID.randomUUID()), rt.length);
		for (int[] statement : rt) {
			list.add(CoberturaStatementEncoding.generateUniqueRepresentationForStatement(statement[0], statement[1], statement[2]));
		}
		return new CompressedLongTrace(list, true);
	}
	
	private CompressedIntegerTrace c(Path outputDir, int... numbers) {
		BufferedIntArrayQueue list = new BufferedIntArrayQueue(
				outputDir.toFile(), String.valueOf(UUID.randomUUID()), numbers.length);
		for (int id : numbers) {
			list.add(id);
		}
		return new CompressedIntegerTrace(list, true);
	}
	public static <T> Stream<List<T>> sliding(List<T> list, int size) {
		if(size > list.size())
			return Stream.empty();
		return IntStream.range(0, list.size()-size+1)
				.mapToObj(start -> list.subList(start, start+size));
	}
	

	
	@Test
	public void testBlockSpectraReadingAndWriting2() throws ZipException {
		Path output1 = Paths.get(getStdResourcesDir(), "spectraCompressed_filtered.zip");

		ISpectra<SourceCodeBlock, ?> spectra2 = SpectraFileUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, output1);
		Log.out(this, "loaded...");
		assertNotNull(spectra2.getIndexer());
		Collection<? extends ITrace<SourceCodeBlock>> failingTraces = spectra2.getFailingTraces();
		assertNotNull(failingTraces);
		assertEquals(2, failingTraces.size());
		for(ITrace<SourceCodeBlock> trace : failingTraces){
			Log.out(this,"trace identifier= "+ trace.getIdentifier());
		}
		// check the correct execution trace
		//assertFalse(trace.getExecutionTraces().isEmpty());
		//ExecutionTrace executionTrace1 = trace.getExecutionTraces().iterator().next();
	}

}
