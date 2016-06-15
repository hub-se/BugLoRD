/**
 * 
 */
package se.de.hu_berlin.informatik.c2r;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.cli.Option;
import se.de.hu_berlin.informatik.c2r.modules.AddToProviderAndGenerateSpectraModule;
import se.de.hu_berlin.informatik.c2r.modules.HitTraceModule;
import se.de.hu_berlin.informatik.c2r.modules.RankingModule;
import se.de.hu_berlin.informatik.c2r.modules.SaveSpectraModule;
import se.de.hu_berlin.informatik.c2r.modules.TestRunAndReportModule;
import se.de.hu_berlin.informatik.utils.fileoperations.FileLineProcessorModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.PipeLinker;
import se.de.hu_berlin.informatik.utils.tm.pipes.ListSequencerPipe;

/**
 * Computes SBFL rankings or hit traces from a list of tests with the support of the stardust API.
 * Needs the classes instrumented by Cobertura at the beginning of the class path and needs the
 * system property "net.sourceforge.cobertura.datafile" set to the path to a cobertura data file.
 * 
 * @author Simon Heiden
 */
public class Instr2Coverage2Ranking {

	/**
	 * Parses the options from the command line.
	 * @param args
	 * the application's arguments
	 * @return
	 * an {@link OptionParser} object that provides access to all parsed options and their values
	 */
	private static OptionParser getOptions(String[] args) {
//		final String tool_usage = "Instr2Coverage2Ranking -i (input-dir|input-file) (-r failed-traces-dir -l loc1 loc2 ... | -t) [-o output]"; 
		final String tool_usage = "Instr2Coverage2Ranking";
		final OptionParser options = new OptionParser(tool_usage, args);

		options.add("ht", "hitTraceMode", false, "Whether only hit traces should be computed.");
		
		options.add("t", "testList", true, "File with all tests to execute.", true);
		options.add("pd", "projectDir", true, "Path to the directory of the project under test.", true);
		options.add("sd", "sourceDir", true, "Relative path to the main directory containing the sources from the project directory.", true);
		
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
		
		if (System.getProperty("net.sourceforge.cobertura.datafile") == null) {
			Misc.abort("Please include property '-Dnet.sourceforge.cobertura.datafile=.../cobertura.ser' in the application's call.");
		}

		OptionParser options = getOptions(args);

		Path testFile = options.isFile('t', true);
		Path projectDir = options.isDirectory("pd", true);
		Path srcDir = options.isDirectory(projectDir, "sd", true);
		String outputDir = options.isDirectory('o', false).toString();
		Path coberturaDataFile = Paths.get(System.getProperty("net.sourceforge.cobertura.datafile"));

		if (options.hasOption("ht")) {
			//hit trace mode
			new PipeLinker().link(
					new FileLineProcessorModule<List<String>>(new TestLineProcessor()),
					new ListSequencerPipe<List<String>,String>(),
					new TestRunAndReportModule(coberturaDataFile, outputDir, srcDir.toString(), false),
					new HitTraceModule(outputDir, true))
			.submit(testFile)
			.waitForShutdown();
		} else {
			//ranking mode
			String[] localizers = null;
			if ((localizers = options.getOptionValues('l')) == null) {
				Misc.abort("No localizers given.");
			}
			new PipeLinker().link(
					new FileLineProcessorModule<List<String>>(new TestLineProcessor()),
					new ListSequencerPipe<List<String>,String>(),
					new TestRunAndReportModule(coberturaDataFile, outputDir, srcDir.toString(), false),
					new AddToProviderAndGenerateSpectraModule(true, true, outputDir + File.separator + "fail"),
					new SaveSpectraModule(Paths.get(outputDir, "spectraCompressed.zip"), true),
					new RankingModule(outputDir, localizers))
			.submit(testFile)
			.waitForShutdown();
		}
	}
	
}
