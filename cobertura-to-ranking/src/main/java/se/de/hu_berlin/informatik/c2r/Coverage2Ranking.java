/**
 * 
 */
package se.de.hu_berlin.informatik.c2r;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.c2r.modules.AddToProviderAndGenerateSpectraModule;
import se.de.hu_berlin.informatik.c2r.modules.HitTraceModule;
import se.de.hu_berlin.informatik.c2r.modules.RankingModule;
import se.de.hu_berlin.informatik.c2r.modules.SaveSpectraModule;
import se.de.hu_berlin.informatik.c2r.modules.XMLCoverageWrapperModule;
import se.de.hu_berlin.informatik.utils.fileoperations.PathToFileConverterModule;
import se.de.hu_berlin.informatik.utils.fileoperations.SearchForFilesOrDirsModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.ModuleLinker;
import se.de.hu_berlin.informatik.utils.tm.modules.ListSequencerModule;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.PipeLinker;
import se.de.hu_berlin.informatik.utils.tm.pipes.ListSequencerPipe;


/**
 * Computes SBFL rankings or hit traces from existing Cobertura xml coverage files 
 * with the support of the stardust API.
 * 
 * @author Simon Heiden
 */
public class Coverage2Ranking {

	/**
	 * Parses the options from the command line.
	 * @param args
	 * the application's arguments
	 * @return
	 * an {@link OptionParser} object that provides access to all parsed options and their values
	 */
	private static OptionParser getOptions(String[] args) {
//		final String tool_usage = "Coverage2Ranking -i (input-dir|input-file) (-r [-l loc1 loc2 ...] | -ht) -o output"; 
		final String tool_usage = "Coverage2Ranking";
		final OptionParser options = new OptionParser(tool_usage, args);

		options.add("i", "input", true, "Cobertura xml file or report directory with Cobertura xml files.", true);
		options.add(Option.builder("o").longOpt("output").hasArg().required()
				.desc("Path to output directory.").build());       

		options.addGroup("r", "ranking", true, "Compute rankings (directory with failed traces "
				+ "(named \"...fail...\") has to be included in the report directory).", 
				"ht", "trace", false, "Compute trace(s).", true);

		options.add(Option.builder("l").longOpt("localizers")
				.hasArgs().desc("A list of identifiers of Cobertura localizers (e.g. 'Tarantula', 'Jaccard', ...).")
				.build());

		options.parseCommandLine();

		return options;
	}

	/**
	 * @param args
	 * -i (input-dir|input-file) (-r [-l loc1 loc2 ...] | -ht) -o output
	 */
	public static void main(String[] args) {

		OptionParser options = getOptions(args);

		Path input = Paths.get(options.getOptionValue('i'));
		String outputDir = options.isDirectory('o', false).toString();

		if (options.hasOption("ht")) { 
			//hit trace mode
			if (input.toFile().isDirectory()) {
				new ModuleLinker().link(
						new SearchForFilesOrDirsModule("**/*.{xml}", false, true, true),
						new ListSequencerModule<List<Path>,Path>(),
						new PathToFileConverterModule(),
						new XMLCoverageWrapperModule(),
						new HitTraceModule(outputDir, false).enableTracking(50))
				.submit(input);
			} else {
				new ModuleLinker().link(
						new XMLCoverageWrapperModule(),
						new HitTraceModule(outputDir, false))
				.submit(input.toFile());
			}
		} else {
			//ranking mode
			if (!input.toFile().isDirectory()) {
				Log.abort(Coverage2Ranking.class, "Input has to be a directory.");
			}
			String[] localizers = null;
			if ((localizers = options.getOptionValues('l')) == null) {
				Log.err(Coverage2Ranking.class, "No localizers given. Only generating the compressed spectra.");
			}
			new PipeLinker().link(
					new SearchForFilesOrDirsModule("**/*.{xml}", false, true, true),
					new ListSequencerPipe<List<Path>,Path>(),
					new PathToFileConverterModule(),
					new XMLCoverageWrapperModule(),
					new AddToProviderAndGenerateSpectraModule(true, false).enableTracking(50),
					new SaveSpectraModule(Paths.get(outputDir, "spectraCompressed.zip"), true),
					new RankingModule(outputDir, localizers))
			.submit(input)
			.waitForShutdown();
			//if we don't wait here for the pipe to shut down, then 
			//the running pipe threads are just cancelled by the JVM
			//at the end of the scope...
		}

	}


}
