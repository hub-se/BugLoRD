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
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.PipeLinker;
import se.de.hu_berlin.informatik.utils.tm.pipes.ListSequencerPipe;

/**
 * Computes SBFL rankings or hit traces from a list of tests with the support of the stardust API.
 * Needs the classes instrumented by Cobertura at the beginning of the class path and needs the
 * system property "net.sourceforge.cobertura.datafile" set to the path to a cobertura data file.
 * 
 * @author Simon Heiden
 */
final public class Instr2Coverage2Ranking {
	
	private Instr2Coverage2Ranking() {
		//disallow instantiation
	}

	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		HIT_TRACE("ht", "hitTraceMode", false, "Whether only hit traces should be computed.", false),
		TEST_LIST("t", "testList", true, "File with all tests to execute.", true),
		PROJECT_DIR("pd", "projectDir", true, "Path to the directory of the project under test.", true),
		SOURCE_DIR("sd", "sourceDir", true, "Relative path to the main directory containing the sources from the project directory.", true),
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
				final boolean hasArg, final String description, final int groupId) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(false).
					hasArg(hasArg).desc(description).build(), groupId);
		}
		
		//adds the given option that will be part of the group with the given id
		CmdOptions(final Option option, final int groupId) {
			this.option = new OptionWrapper(option, groupId);
		}
		
		//adds the given option that will be part of no group
		CmdOptions(final Option option) {
			this(option, NO_GROUP);
		}

		@Override public String toString() { return option.getOption().getOpt(); }
		@Override public OptionWrapper getOptionWrapper() { return option; }
	}

	/**
	 * @param args
	 * command line arguments
	 */
	public static void main(final String[] args) {
		
		if (System.getProperty("net.sourceforge.cobertura.datafile") == null) {
			Log.abort(Instr2Coverage2Ranking.class, "Please include property '-Dnet.sourceforge.cobertura.datafile=.../cobertura.ser' in the application's call.");
		}

		final OptionParser options = OptionParser.getOptions("Instr2Coverage2Ranking", false, CmdOptions.class, args);

		final Path testFile = options.isFile(CmdOptions.TEST_LIST, true);
		final Path projectDir = options.isDirectory(CmdOptions.PROJECT_DIR, true);
		final Path srcDir = options.isDirectory(projectDir, CmdOptions.SOURCE_DIR, true);
		final String outputDir = options.isDirectory(CmdOptions.OUTPUT, false).toString();
		final Path coberturaDataFile = Paths.get(System.getProperty("net.sourceforge.cobertura.datafile"));

		if (options.hasOption(CmdOptions.HIT_TRACE)) {
			//hit trace mode
			new PipeLinker().append(
					new FileLineProcessorModule<List<String>>(new TestLineProcessor()),
					new ListSequencerPipe<List<String>,String>(),
					new TestRunAndReportModule(coberturaDataFile, outputDir, srcDir.toString(), false),
					new HitTraceModule(outputDir, true))
			.submitAndShutdown(testFile);
		} else {
			//ranking mode
			final String[] localizers = options.getOptionValues(CmdOptions.LOCALIZERS);
			if (localizers == null) {
				Log.warn(Instr2Coverage2Ranking.class, "No localizers given. Only generating the compressed spectra.");
			}
			new PipeLinker().append(
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
