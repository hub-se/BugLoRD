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
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.optionparser.IOptions;
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

	public static enum CmdOptions implements IOptions {
		/* add options here according to your needs */
		HIT_TRACE("ht", "hitTraceMode", false, "Whether only hit traces should be computed.", false),
		TEST_LIST("t", "testList", true, "File with all tests to execute.", true),
		PROJECT_DIR("pd", "projectDir", true, "Path to the directory of the project under test.", true),
		SOURCE_DIR("sd", "sourceDir", true, "Relative path to the main directory containing the sources from the project directory.", true),
		OUTPUT("o", "output", true, "Path to output directory.", true),
		LOCALIZERS(Option.builder("l").longOpt("localizers").optionalArg(true)
				.hasArgs().desc("A list of identifiers of Cobertura localizers (e.g. 'Tarantula', 'Jaccard', ...).").build());
		
		/* the following code blocks should not need to be changed */
		final private Option option;
		final private int groupId;

		//adds an option that is not part of any group
		CmdOptions(final String opt, final String longOpt, final boolean hasArg, final String description, final boolean required) {
			this.option = Option.builder(opt).longOpt(longOpt).required(required).hasArg(hasArg).desc(description).build();
			this.groupId = NO_GROUP;
		}
		
		//adds an option that is part of the group with the specified index (positive integer)
		//a negative index means that this option is part of no group
		//this option will not be required, however, the group itself will be
		CmdOptions(final String opt, final String longOpt, final boolean hasArg, final String description, int groupId) {
			this.option = Option.builder(opt).longOpt(longOpt).required(false).hasArg(hasArg).desc(description).build();
			this.groupId = groupId;
		}
		
		//adds the given option that will be part of the group with the given id
		CmdOptions(Option option, int groupId) {
			this.option = option;
			this.groupId = groupId;
		}
		
		//adds the given option that will be part of no group
		CmdOptions(Option option) {
			this(option, NO_GROUP);
		}
		
		@Override public Option option() { return option; }
		@Override public int groupId() { return groupId; }
		@Override public String toString() { return option.getOpt(); }
	}

	/**
	 * @param args
	 * command line arguments
	 */
	public static void main(String[] args) {
		
		if (System.getProperty("net.sourceforge.cobertura.datafile") == null) {
			Log.abort(Instr2Coverage2Ranking.class, "Please include property '-Dnet.sourceforge.cobertura.datafile=.../cobertura.ser' in the application's call.");
		}

		OptionParser options = OptionParser.getOptions("Instr2Coverage2Ranking", false, CmdOptions.class, args);

		Path testFile = options.isFile(CmdOptions.TEST_LIST, true);
		Path projectDir = options.isDirectory(CmdOptions.PROJECT_DIR, true);
		Path srcDir = options.isDirectory(projectDir, CmdOptions.SOURCE_DIR, true);
		String outputDir = options.isDirectory(CmdOptions.OUTPUT, false).toString();
		Path coberturaDataFile = Paths.get(System.getProperty("net.sourceforge.cobertura.datafile"));

		if (options.hasOption(CmdOptions.HIT_TRACE)) {
			//hit trace mode
			new PipeLinker().link(
					new FileLineProcessorModule<List<String>>(new TestLineProcessor()),
					new ListSequencerPipe<List<String>,String>(),
					new TestRunAndReportModule(coberturaDataFile, outputDir, srcDir.toString(), false),
					new HitTraceModule(outputDir, true))
			.submitAndShutdown(testFile);
		} else {
			//ranking mode
			String[] localizers = null;
			if ((localizers = options.getOptionValues(CmdOptions.LOCALIZERS)) == null) {
				Log.warn(Instr2Coverage2Ranking.class, "No localizers given. Only generating the compressed spectra.");
			}
			new PipeLinker().link(
					new FileLineProcessorModule<List<String>>(new TestLineProcessor()),
					new ListSequencerPipe<List<String>,String>(),
					new TestRunAndReportModule(coberturaDataFile, outputDir, srcDir.toString(), false),
					new AddToProviderAndGenerateSpectraModule(true, true, outputDir + File.separator + "fail"),
					new SaveSpectraModule(Paths.get(outputDir, "spectraCompressed.zip"), true),
					new RankingModule(outputDir, localizers))
			.submitAndShutdown(testFile);
		}
	}
	
}
