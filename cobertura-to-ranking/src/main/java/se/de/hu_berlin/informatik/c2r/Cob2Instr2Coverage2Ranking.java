/**
 * 
 */
package se.de.hu_berlin.informatik.c2r;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.cli.Option;

import net.sourceforge.cobertura.instrument.InstrumentMain;
import se.de.hu_berlin.informatik.junittestutils.testlister.UnitTestLister;
import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.ClassPathParser;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.IOptions;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.tm.modules.ExecuteMainClassInNewJVMModule;


/**
 * Computes SBFL rankings or hit traces from a list of tests or a list of test classes
 * with the support of the stardust API.
 * Instruments given classes with Cobertura and may list all tests of given test classes
 * at the beginning for convenience.
 * 
 * @author Simon Heiden
 */
public class Cob2Instr2Coverage2Ranking {

	public static enum CmdOptions implements IOptions {
		/* add options here according to your needs */
		HIT_TRACE("ht", "hitTraceMode", false, "Whether only hit traces should be computed.", false),
		JAVA_HOME_DIR("java", "javaHomeDir", true, "Path to a Java home directory (at least v1.8). Set if you encounter any version problems. "
				+ "If not set, the default JRE is used.", false),
		CLASS_PATH("cp", "classPath", true, "An additional class path which may be needed for the execution of tests. "
				+ "Will be appended to the regular class path if this option is set.", false),
		TEST_LIST("t", "testList", true, "File with all tests to execute.", 0),
		TEST_CLASS_LIST("tc", "testClassList", true, "File with a list of test classes from which all tests shall be executed.", 0),
		INSTRUMENT_CLASSES(Option.builder("c").longOpt("classes").required()
				.hasArgs().desc("A list of classes/directories to instrument with Cobertura.").build()),
		PROJECT_DIR("pd", "projectDir", true, "Path to the directory of the project under test.", true),
		SOURCE_DIR("sd", "sourceDir", true, "Relative path to the main directory containing the sources from the project directory.", true),
		TEST_CLASS_DIR("td", "testClassDir", true, "Relative path to the main directory containing the needed test classes from the project directory.", true),
		OUTPUT("o", "output", true, "Path to output directory.", true),
		LOCALIZERS(Option.builder("l").longOpt("localizers").optionalArg(true)
				.hasArgs().desc("A list of identifiers of Cobertura localizers (e.g. 'Tarantula', 'Jaccard', ...).").build());

		/* the following code blocks should not need to be changed */
		final private OptionWrapper option;

		//adds an option that is not part of any group
		CmdOptions(final String opt, final String longOpt, 
				final boolean hasArg, final String description, final boolean required) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(required).
					hasArg(hasArg).desc(description).build(), NO_GROUP);
		}
		
		//adds an option that is part of the group with the specified index (positive integer)
		//a negative index means that this option is part of no group
		//this option will not be required, however, the group itself will be
		CmdOptions(final String opt, final String longOpt, 
				final boolean hasArg, final String description, int groupId) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(false).
					hasArg(hasArg).desc(description).build(), groupId);
		}
		
		//adds the given option that will be part of the group with the given id
		CmdOptions(Option option, int groupId) {
			this.option = new OptionWrapper(option, groupId);
		}
		
		//adds the given option that will be part of no group
		CmdOptions(Option option) {
			this(option, NO_GROUP);
		}

		@Override public String toString() { return option.getOption().getOpt(); }
		@Override public OptionWrapper getOptionWrapper() { return option; }
	}

	/**
	 * @param args
	 * command line arguments
	 */
	public static void main(String[] args) {

		OptionParser options = OptionParser.getOptions("Cob2Instr2Coverage2Ranking", false, CmdOptions.class, args);
		
		Path projectDir = options.isDirectory(CmdOptions.PROJECT_DIR, true);
		options.isDirectory(projectDir, CmdOptions.SOURCE_DIR, true);
		Path testClassDir = options.isDirectory(projectDir, CmdOptions.TEST_CLASS_DIR, true);
		String outputDir = options.isDirectory(CmdOptions.OUTPUT, false).toString();
		
//		if (!options.hasOption("ht") && options.getOptionValues('l') == null) {
//			Misc.err("No localizers given. Only generating the compressed spectra.");
//		}

		Path instrumentedDir = Paths.get(outputDir, "instrumented").toAbsolutePath();
		File coberturaDataFile = Paths.get(outputDir, "cobertura.ser").toAbsolutePath().toFile();
		
		String[] classesToInstrument = options.getOptionValues(CmdOptions.INSTRUMENT_CLASSES);
		
		String javaHome = options.getOptionValue(CmdOptions.JAVA_HOME_DIR, null);
		
		String[] instrArgs = { 
				"--datafile", coberturaDataFile.toString(),
				"--destination", instrumentedDir.toString(), 
				//"--auxClasspath" $COBERTURADIR/cobertura-2.1.1.jar, //not needed since already in class path
		};
		
		//add class path for files that can't be found during instrumentation
		if (options.hasOption(CmdOptions.CLASS_PATH)) {
			String[] auxCP = { "--auxClasspath", options.getOptionValue(CmdOptions.CLASS_PATH) };
			instrArgs = Misc.joinArrays(instrArgs, auxCP);
		}

		//setting this property is not really necessary, but it should avoid writing to a non-existent data file
		System.setProperty("net.sourceforge.cobertura.datafile", coberturaDataFile.toString());
		
		//add the classes (or dirs of classes) to instrument to the end of the argument array
		instrArgs = Misc.joinArrays(instrArgs, classesToInstrument);

		//instrument the classes
		int returnValue = InstrumentMain.instrument(instrArgs);
		if ( returnValue != 0 ) {
			Log.abort(Cob2Instr2Coverage2Ranking.class, "Error while instrumenting class files.");
		}

		//generate modified class path with instrumented classes at the beginning
		ClassPathParser cpParser = new ClassPathParser()
				.parseSystemClasspath()
				.addElementAtStartOfClassPath(testClassDir.toAbsolutePath().toFile());
		for (String item : classesToInstrument) {
			cpParser.addElementAtStartOfClassPath(Paths.get(item).toAbsolutePath().toFile());
		}
		cpParser.addElementAtStartOfClassPath(instrumentedDir.toAbsolutePath().toFile());
		String classPath = cpParser.getClasspath();
		
		//append a given class path for any files that are needed to run the tests
		classPath += options.hasOption(CmdOptions.CLASS_PATH) ? File.pathSeparator + options.getOptionValue(CmdOptions.CLASS_PATH) : "";

		String allTestsFile = null;
		if (options.hasOption(CmdOptions.TEST_CLASS_LIST)) {
			//mine all tests from test classes given in input file
			allTestsFile = Paths.get(outputDir + File.separator + "all_tests.txt").toAbsolutePath().toString();
			String[] testlisterArgs = {
					UnitTestLister.CmdOptions.INPUT.asArg(), options.getOptionValue(CmdOptions.TEST_CLASS_LIST),
					UnitTestLister.CmdOptions.OUTPUT.asArg(), allTestsFile
			};
			//we need the test classes in the class path, so start a new java process
			int result = new ExecuteMainClassInNewJVMModule(javaHome, 
					"se.de.hu_berlin.informatik.junittestutils.testlister.UnitTestLister",
					classPath, null,
					"-XX:+UseNUMA")
			.submit(testlisterArgs).getResult();
			
			if (result != 0) {
				Log.abort(Cob2Instr2Coverage2Ranking.class, "Error while mining tests from test class file.");
			}
		} else { //has option "t"
			allTestsFile = options.isFile(CmdOptions.TEST_LIST, true).toAbsolutePath().toString();
		}

		//build arguments for the next application
		String[] newArgs = { 
				Instr2Coverage2Ranking.CmdOptions.PROJECT_DIR.asArg(), options.getOptionValue(CmdOptions.PROJECT_DIR), 
				Instr2Coverage2Ranking.CmdOptions.SOURCE_DIR.asArg(), options.getOptionValue(CmdOptions.SOURCE_DIR),
				Instr2Coverage2Ranking.CmdOptions.TEST_LIST.asArg(), allTestsFile,
				Instr2Coverage2Ranking.CmdOptions.OUTPUT.asArg(), Paths.get(outputDir).toAbsolutePath().toString()};
		
		if (options.getOptionValues(CmdOptions.LOCALIZERS) != null) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, Instr2Coverage2Ranking.CmdOptions.LOCALIZERS.asArg());
			newArgs = Misc.joinArrays(newArgs, options.getOptionValues(CmdOptions.LOCALIZERS));
		}
		
		//hit trace mode?
		if (options.hasOption(CmdOptions.HIT_TRACE)) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, Instr2Coverage2Ranking.CmdOptions.HIT_TRACE.asArg());
		}
		
		//sadly, we have no other choice but to start a new java process with the updated class path and the cobertura data file...
		//the reason is that I didn't manage to update the class path on the fly, no matter what I tried to do...
		new ExecuteMainClassInNewJVMModule(javaHome, 
				"se.de.hu_berlin.informatik.c2r.Instr2Coverage2Ranking", classPath, projectDir.toFile(), 
				"-Dnet.sourceforge.cobertura.datafile=" + coberturaDataFile.getAbsolutePath().toString(), 
				"-XX:+UseNUMA", "-XX:+UseConcMarkSweepGC")
		.submit(newArgs);
		
		FileUtils.delete(instrumentedDir);

	}
	
	
	/**
	 * Convenience method for easier use in a special case.
	 * @param workDir
	 * directory of a buggy Defects4J project version
	 * @param mainSrcDir
	 * path to main source directory
	 * @param testBinDir
	 * path to main directory of binary test classes
	 * @param testCP
	 * class path needed to execute tests
	 * @param mainBinDir
	 * path to main directory of binary program classes
	 * @param testClassesFile
	 * path to a file that contains a list of all test classes to consider
	 * @param rankingDir
	 * output path of generated rankings
	 * @param localizers
	 * an array of String representation of fault localizers
	 * as used by STARDUST
	 */
	public static void generateRankingForDefects4JElement(
			String workDir, String mainSrcDir, String testBinDir, 
			String testCP, String mainBinDir, String testClassesFile, 
			String rankingDir, String[] localizers) {
		String[] args = { 
				CmdOptions.PROJECT_DIR.asArg(), workDir, 
				CmdOptions.SOURCE_DIR.asArg(), mainSrcDir,
				CmdOptions.TEST_CLASS_DIR.asArg(), testBinDir,
				CmdOptions.CLASS_PATH.asArg(), testCP,
				CmdOptions.INSTRUMENT_CLASSES.asArg(), mainBinDir,
				CmdOptions.TEST_CLASS_LIST.asArg(), testClassesFile,
				CmdOptions.OUTPUT.asArg(), rankingDir};
		
		if (localizers != null) {
			args = Misc.addToArrayAndReturnResult(args, CmdOptions.LOCALIZERS.asArg());
			args = Misc.joinArrays(args, localizers);
		}
		
		main(args);
	}
	
}
