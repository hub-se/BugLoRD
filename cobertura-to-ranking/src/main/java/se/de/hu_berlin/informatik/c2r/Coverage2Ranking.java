/**
 * 
 */
package se.de.hu_berlin.informatik.c2r;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.c2r.modules.AddToProviderAndGenerateSpectraModule;
import se.de.hu_berlin.informatik.c2r.modules.RankingModule;
import se.de.hu_berlin.informatik.c2r.modules.SaveSpectraModule;
import se.de.hu_berlin.informatik.c2r.modules.TraceFileModule;
import se.de.hu_berlin.informatik.c2r.modules.XMLCoverageWrapperModule;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.utils.fileoperations.PathToFileConverterModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.PipeLinker;
import se.de.hu_berlin.informatik.utils.tm.pipes.SearchFileOrDirPipe;


/**
 * Computes SBFL rankings or hit traces from existing Cobertura xml coverage files 
 * with the support of the stardust API.
 * 
 * @author Simon Heiden
 */
final public class Coverage2Ranking {
	
	private Coverage2Ranking() {
		//disallow instantiation
	}

	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		INPUT("i", "input", true, "Cobertura xml file or report directory with Cobertura xml files.", true),
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
	 * -i (input-dir|input-file) (-r [-l loc1 loc2 ...] | -ht) -o output
	 */
	public static void main(final String[] args) {

		final OptionParser options = OptionParser.getOptions("Coverage2Ranking", false, CmdOptions.class, args);

		final Path input = Paths.get(options.getOptionValue(CmdOptions.INPUT));
		final String outputDir = options.isDirectory(CmdOptions.OUTPUT, false).toString();


		//ranking mode
		if (!input.toFile().isDirectory()) {
			Log.abort(Coverage2Ranking.class, "Input has to be a directory.");
		}
		final String[] localizers = options.getOptionValues(CmdOptions.LOCALIZERS);
		if (localizers == null) {
			Log.warn(Coverage2Ranking.class, "No localizers given. Only generating the compressed spectra.");
		}
		new PipeLinker().append(
				new SearchFileOrDirPipe("**/*.{xml}").searchForFiles(),
				new PathToFileConverterModule(),
				new XMLCoverageWrapperModule(),
				new AddToProviderAndGenerateSpectraModule(true, false).enableTracking(50),
				new SaveSpectraModule<SourceCodeBlock>(SourceCodeBlock.DUMMY, Paths.get(outputDir, "spectraCompressed.zip")),
				new TraceFileModule(outputDir),
				new RankingModule(outputDir, localizers))
		.submitAndShutdown(input);
		//if we don't wait here for the pipe to shut down, then 
		//the running pipe threads are just cancelled by the JVM
		//at the end of the scope...

	}


}
