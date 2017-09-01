package se.de.hu_berlin.informatik.sbfl.spectra;

import java.io.File;
import java.util.ArrayList;

import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

public class TestProjects extends TestSettings {

	private TestProjects() {
		// no instantiation
	}
	
	public static class CoberturaTestProject extends AbstractTestProject {

		private static String testCP = "";
		
		private ArrayList<String> failingTests = new ArrayList<>();

		public CoberturaTestProject() {
			super(getStdResourcesDir() + File.separator + "CoberturaTestProject", 
					"src", 
					"bin", 
					"test-bin", 
					testCP,
					getStdResourcesDir() + File.separator + "testclassesSimple.txt");
			failingTests.add("coberturatest.tests.SimpleProgramTest::testAddWrong");
		}

		@Override
		public ArrayList<String> getFailingTests() {
			return failingTests;
		}

	}
	
	public static class Time3b extends AbstractTestProject {

		private static String testCP = getStdResourcesDir() + File.separator + "Time3b/lib/joda-convert-1.2.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Time3b/lib/junit-3.8.2.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Time3b/target/classes" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Time3b/target/test-classes";
		
		private ArrayList<String> failingTests = new ArrayList<>();

		public Time3b() {
			super(getStdResourcesDir() + File.separator + "Time3b", 
					"src", 
					"target" + File.separator + "classes", 
					"target" + File.separator + "test-classes", 
					testCP,
					"testClasses.txt");
			failingTests.add("org.joda.time.TestMutableDateTime_Adds::testAddYears_int_dstOverlapWinter_addZero");
			failingTests.add("org.joda.time.TestMutableDateTime_Adds::testAddDays_int_dstOverlapWinter_addZero");
			failingTests.add("org.joda.time.TestMutableDateTime_Adds::testAddWeeks_int_dstOverlapWinter_addZero");
			failingTests.add("org.joda.time.TestMutableDateTime_Adds::testAdd_DurationFieldType_int_dstOverlapWinter_addZero");
			failingTests.add("org.joda.time.TestMutableDateTime_Adds::testAddMonths_int_dstOverlapWinter_addZero");
		}

		@Override
		public ArrayList<String> getFailingTests() {
			return failingTests;
		}

	}
	
	public static class Lang8b extends AbstractTestProject {

		private static String testCP = getStdResourcesDir() + File.separator + "Lang8b/target/classes" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Lang8b/target/tests" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Lang8b/lib/junit-4.11.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Lang8b/lib/easymock.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Lang8b/lib/commons-io.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Lang8b/lib/cglib.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Lang8b/lib/asm.jar";
		
		private ArrayList<String> failingTests = new ArrayList<>();

		public Lang8b() {
			super(getStdResourcesDir() + File.separator + "Lang8b", 
					"src", 
					"target" + File.separator + "classes", 
					"target" + File.separator + "tests", 
					testCP,
					"testClasses.txt");
			failingTests.add("org.apache.commons.lang3.time.FastDateFormat_PrinterTest::testCalendarTimezoneRespected");
			failingTests.add("org.apache.commons.lang3.time.FastDatePrinterTest::testCalendarTimezoneRespected");
		}

		@Override
		public ArrayList<String> getFailingTests() {
			return failingTests;
		}

	}
	
	public static class Mockito12b  extends AbstractTestProject {

		private static String testCP = getStdResourcesDir() + File.separator + "Mockito12b/lib/junit-4.11.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/target/classes" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/target/test-classes" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/lib/asm-all-5.0.4.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/lib/assertj-core-2.1.0.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/lib/cglib-and-asm-1.0.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/lib/cobertura-2.0.3.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/lib/fest-assert-1.3.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/lib/fest-util-1.1.4.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/lib/hamcrest-all-1.3.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/lib/hamcrest-core-1.1.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/lib/objenesis-2.1.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/lib/objenesis-2.2.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/lib/powermock-reflect-1.2.5.jar";
		
		private ArrayList<String> failingTests = new ArrayList<>();

		public Mockito12b() {
			super(getStdResourcesDir() + File.separator + "Mockito12b", 
					"src", 
					"target" + File.separator + "classes", 
					"target" + File.separator + "test-classes", 
					testCP,
					"testClasses.txt");
			failingTests.add("org.mockito.internal.util.reflection.GenericMasterTest::shouldDealWithNestedGenerics");
			failingTests.add("org.mockitousage.annotation.CaptorAnnotationBasicTest::shouldUseAnnotatedCaptor");
			failingTests.add("org.mockitousage.annotation.CaptorAnnotationBasicTest::shouldUseCaptorInOrdinaryWay");
			failingTests.add("org.mockitousage.annotation.CaptorAnnotationBasicTest::shouldCaptureGenericList");
			failingTests.add("org.mockitousage.annotation.CaptorAnnotationBasicTest::shouldUseGenericlessAnnotatedCaptor");
			failingTests.add("org.mockitousage.annotation.CaptorAnnotationTest::shouldScreamWhenWrongTypeForCaptor");
			failingTests.add("org.mockitousage.annotation.CaptorAnnotationTest::testNormalUsage");
			failingTests.add("org.mockitousage.annotation.CaptorAnnotationTest::shouldScreamWhenMoreThanOneMockitoAnnotaton");
			failingTests.add("org.mockitousage.annotation.CaptorAnnotationTest::shouldScreamWhenInitializingCaptorsForNullClass");
			failingTests.add("org.mockitousage.annotation.CaptorAnnotationTest::shouldLookForAnnotatedCaptorsInSuperClasses");
		}

		@Override
		public ArrayList<String> getFailingTests() {
			return failingTests;
		}

	}

	public static class Closure101b extends AbstractTestProject {

		private static String testCP = getStdResourcesDir() + File.separator + "Closure101b/build/classes" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Closure101b/lib/ant_deploy.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Closure101b/lib/args4j_deploy.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Closure101b/lib/google_common_deploy.jar"
				+ File.pathSeparator + getStdResourcesDir() + File.separator + "Closure101b/lib/hamcrest-core-1.1.jar"
				+ File.pathSeparator + getStdResourcesDir() + File.separator + "Closure101b/lib/junit.jar"
				+ File.pathSeparator + getStdResourcesDir() + File.separator
				+ "Closure101b/lib/libtrunk_rhino_parser_jarjared.jar" + File.pathSeparator + getStdResourcesDir()
				+ File.separator + "Closure101b/lib/protobuf_deploy.jar" + File.pathSeparator + getStdResourcesDir()
				+ File.separator + "Closure101b/lib/ant.jar" + File.pathSeparator + getStdResourcesDir()
				+ File.separator + "Closure101b/build/test";

		private static ArrayList<String> failingTests = new ArrayList<>();

		public Closure101b() {
			super(getStdResourcesDir() + File.separator + "Closure101b", 
					"src", 
					"build" + File.separator + "classes", 
					"build" + File.separator + "test", 
					testCP,
					"testClasses.txt");
			failingTests.add("com.google.javascript.jscomp.CommandLineRunnerTest::testProcessClosurePrimitives");
		}

		@Override
		public ArrayList<String> getFailingTests() {
			return failingTests;
		}

	}
	
	private static abstract class AbstractTestProject implements TestProject {
		
		private String mainProjectDir;
		private String srcDir;
		private String mainBinDir;
		private String mainTestBinDir;
		private String testClassListPath;
		private String testCP;
		
		public AbstractTestProject(String mainDir, String srcDir, String mainBinDir, String mainTestBinDir,
				String testCP, String testClassListPath) {
			super();
			this.mainProjectDir = mainDir;
			this.srcDir = srcDir;
			this.mainBinDir = mainBinDir;
			this.mainTestBinDir = mainTestBinDir;
			this.testClassListPath = testClassListPath;
			this.testCP = testCP;
		}

		@Override
		public String getProjectMainDir() {
			return mainProjectDir;
		}

		@Override
		public String getSrcDir() {
			return srcDir;
		}

		@Override
		public String getBinDir() {
			return mainBinDir;
		}

		@Override
		public String getBinTestDir() {
			return mainTestBinDir;
		}

		@Override
		public String getTestCP() {
			return testCP;
		}

		@Override
		public String getTestClassListPath() {
			return testClassListPath;
		}
		
	}

}
