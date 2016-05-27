/**
 * 
 */
package se.de.hu_berlin.informatik.c2r;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.cli.Option;

import net.sourceforge.cobertura.instrument.InstrumentMain;
import se.de.hu_berlin.informatik.utils.miscellaneous.ClassPathParser;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
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

	/**
	 * Parses the options from the command line.
	 * @param args
	 * the application's arguments
	 * @return
	 * an {@link OptionParser} object that provides access to all parsed options and their values
	 */
	private static OptionParser getOptions(String[] args) {
		//		final String tool_usage = "Cobertura2Ranking -i (input-dir|input-file) (-r failed-traces-dir -l loc1 loc2 ... | -t) [-o output]"; 
		final String tool_usage = "Cobertura2Ranking";
		final OptionParser options = new OptionParser(tool_usage, args);

		options.add("ht", "hitTraceMode", false, "Whether only hit traces should be computed.");
		
		options.add("java", "javaHomeDir", true, "Path to a Java home directory (at least v1.8). Set if you encounter any version problems.");
		options.add("cp", "classPath", true, "An additional class path which may be needed for the execution of tests. "
				+ "Will be appended to the regular class path if this option is set.");
		
		options.addGroup("t", "testList", true, "File with all tests to execute.",
				"tc", "testClassList", true, "File with a list of test classes from which all tests shall be executed.",true);
		options.add(Option.builder("c").longOpt("classes").required()
				.hasArgs().desc("A list of classes/directories to instrument with Cobertura.")
				.build());
		options.add("pd", "projectDir", true, "Path to the directory of the project under test.", true);
		options.add("sd", "sourceDir", true, "Relative path to the main directory containing the sources from the project directory.", true);
		options.add("td", "testClassDir", true, "Relative path to the main directory containing the needed test classes from the project directory.", true);
		
		options.add(Option.builder("o").longOpt("output").hasArg().required()
				.desc("Path to output directory.").build());        

		options.add(Option.builder("l").longOpt("localizers").optionalArg(true)
				.hasArgs().desc("A list of identifiers of Cobertura localizers (e.g. 'tarantula', 'jaccard', ...).")
				.build());

		options.parseCommandLine();

		return options;
	}

	/**
	 * @param args
	 * -i (input-dir|input-file) (-r failed-traces-dir -l loc1 loc2 ... | -t) [-o output]
	 */
	public static void main(String[] args) {

		OptionParser options = getOptions(args);
		
		Path projectDir = options.isDirectory("pd", true);
		options.isDirectory(projectDir, "sd", true);
		Path testClassDir = options.isDirectory(projectDir, "td", true);
		String outputDir = options.isDirectory('o', false).toString();
		
		if (!options.hasOption("ht") && options.getOptionValues('l') == null) {
			Misc.abort("No localizers given.");
		}

		Path instrumentedDir = Paths.get(outputDir, "instrumented").toAbsolutePath();
		File coberturaDataFile = Paths.get(outputDir, "cobertura.ser").toAbsolutePath().toFile();
		
		String[] classesToInstrument = options.getOptionValues("c");
		
		String javaHome = options.getOptionValue("java", null);
		
		String[] instrArgs = { 
				"--datafile", coberturaDataFile.toString(),
				"--destination", instrumentedDir.toString(), 
				//"--auxClasspath" $COBERTURADIR/cobertura-2.1.1.jar, //not needed since already in class path
		};
		
		//add class path for files that can't be found during instrumentation
		if (options.hasOption("cp")) {
			String[] auxCP = { "--auxClasspath", options.getOptionValue("cp") };
			instrArgs = Misc.joinArrays(instrArgs, auxCP);
		}

		//setting this property is not really necessary, but it should avoid writing to a non-existent data file
		System.setProperty("net.sourceforge.cobertura.datafile", coberturaDataFile.toString());
		
		//add the classes (or dirs of classes) to instrument to the end of the argument array
		instrArgs = Misc.joinArrays(instrArgs, classesToInstrument);

		//instrument the classes
		int returnValue = InstrumentMain.instrument(instrArgs);
		if ( returnValue != 0 ) {
			Misc.abort("Error while instrumenting class files.");
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
		classPath += options.hasOption("cp") ? File.pathSeparator + options.getOptionValue("cp") : "";

		String allTestsFile = null;
		if (options.hasOption("tc")) {
			//mine all tests from test classes given in input file
			allTestsFile = Paths.get(outputDir + File.separator + "all_tests.txt").toAbsolutePath().toString();
			String[] testlisterArgs = {
					"-i", options.getOptionValue("tc"),
					"-o", allTestsFile
			};
			//we need the test classes in the class path, so start a new java process
			int result = new ExecuteMainClassInNewJVMModule(javaHome, null, 
					"se.de.hu_berlin.informatik.junittestutils.testlister.UnitTestLister", classPath, "-XX:+UseNUMA")
			.submit(testlisterArgs).getResult();
			
			if (result != 0) {
				Misc.abort("Error while mining tests from test class file.");
			}
		} else { //has option "t"
			allTestsFile = Paths.get(options.getOptionValue('t')).toAbsolutePath().toString();
		}

		//build arguments for the next application
		String[] newArgs = { 
				"-pd", options.getOptionValue("pd"), 
				"-sd", options.getOptionValue("sd"),
				"-t", allTestsFile,
				"-o", Paths.get(outputDir).toAbsolutePath().toString(),
				"-l"};
		newArgs = Misc.joinArrays(newArgs, options.getOptionValues('l'));
		
		//hit trace mode?
		if (options.hasOption("ht")) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, "-ht");
		}
		
		//sadly, we have no other choice but to start a new java process with the updated class path and the cobertura data file...
		//the reason is that I didn't manage to update the class path on the fly, no matter what I tried to do...
		new ExecuteMainClassInNewJVMModule(javaHome, projectDir.toFile(), "se.de.hu_berlin.informatik.c2r.Instr2Coverage2Ranking", classPath, 
				"-Dnet.sourceforge.cobertura.datafile=" + coberturaDataFile.getAbsolutePath().toString(), "-XX:+UseNUMA")
		.submit(newArgs);

	}
	
	
}
