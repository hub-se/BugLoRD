/**
 * 
 */
package se.de.hu_berlin.informatik.c2r;

import java.nio.file.Path;
import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.c2r.modules.RankingModule;
import se.de.hu_berlin.informatik.c2r.modules.ReadSpectraModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.IOptions;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.ModuleLinker;


/**
 * Computes SBFL rankings or hit traces from an existing spectra file 
 * with the support of the stardust API.
 * 
 * @author Simon Heiden
 */
public class Spectra2Ranking {

	public static enum CmdOptions implements IOptions {
		/* add options here according to your needs */
		INPUT("i", "input", true, "A compressed spectra file.", true),
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
	 * -i input-file [-l loc1 loc2 ...] -o output
	 */
	public static void main(String[] args) {

		OptionParser options = OptionParser.getOptions("Spectra2Ranking", false, CmdOptions.class, args);

		Path spectraFile = options.isFile(CmdOptions.INPUT, true);
		String outputDir = options.isDirectory(CmdOptions.OUTPUT, false).toString();

		if (spectraFile.toFile().isDirectory()) {
			Log.abort(Spectra2Ranking.class, "Input has to be a file.");
		}
		String[] localizers = null;
		if ((localizers = options.getOptionValues(CmdOptions.LOCALIZERS)) == null) {
			Log.abort(Spectra2Ranking.class, "No localizers given.");
		}
		new ModuleLinker().link(
				new ReadSpectraModule(),
				new RankingModule(outputDir, localizers))
		.submit(spectraFile);
		
	}

	/**
	 * Convenience method for easier use in a special case.
	 * @param spectraFile
	 * a compressed spectra file
	 * @param rankingDir
	 * path to the main ranking directory
	 * @param localizers
	 * an array of String representation of fault localizers
	 * as used by STARDUST
	 */
	public static void generateRankingForDefects4JElement(
			String spectraFile, String rankingDir, String[] localizers) {
		String[] args = { 
				CmdOptions.INPUT.asArg(), spectraFile,
				CmdOptions.OUTPUT.asArg(), rankingDir,
				CmdOptions.LOCALIZERS.asArg()};
		args = Misc.joinArrays(args, localizers);
		
		main(args);
	}

}
