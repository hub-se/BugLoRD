
package se.de.hu_berlin.informatik.changechecker;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;

import org.apache.commons.cli.Option;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class ChangeChecker {

	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		LEFT_INPUT_OPT("l", "left", true, "Path to the left file (previous).", true),
		RIGHT_INPUT_OPT("r", "right", true, "Path to the right file (changed).", true),

		COMPRESS_AST_CHANGES("c", "compress", false, "Only keep changes with \"real\" changed lines.", false);

		// options.add(OUTPUT_OPT, "output", true, "Path to output file.",
		// true);

		/* the following code blocks should not need to be changed */
		final private OptionWrapper option;

		// adds an option that is not part of any group
		CmdOptions(final String opt, final String longOpt, final boolean hasArg, final String description,
				final boolean required) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(required).hasArg(hasArg).desc(description).build(),
					NO_GROUP);
		}

		// adds an option that is part of the group with the specified index
		// (positive integer)
		// a negative index means that this option is part of no group
		// this option will not be required, however, the group itself will be
		CmdOptions(final String opt, final String longOpt, final boolean hasArg, final String description,
				int groupId) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(false).hasArg(hasArg).desc(description).build(),
					groupId);
		}

		// adds the given option that will be part of the group with the given
		// id
		CmdOptions(Option option, int groupId) {
			this.option = new OptionWrapper(option, groupId);
		}

		// adds the given option that will be part of no group
		CmdOptions(Option option) {
			this(option, NO_GROUP);
		}

		@Override
		public String toString() {
			return option.getOption().getOpt();
		}

		@Override
		public OptionWrapper getOptionWrapper() {
			return option;
		}
	}

	/**
	 * @param args
	 * -s spectra-zip-file [-r ranked-lines-file] [-u unranked-lines-file] -o
	 * output-file
	 */
	public static void main(String[] args) {

		OptionParser options = OptionParser.getOptions("ChangeChecker", false, CmdOptions.class, args);

		File left = options.isFile(CmdOptions.LEFT_INPUT_OPT, true).toFile();
		File right = options.isFile(CmdOptions.RIGHT_INPUT_OPT, true).toFile();

		
//		List<Action> actions = Collections.emptyList();
//		Run.initGenerators();
//		try {
//			ITree src = Generators.getInstance().getTree(left.toString()).getRoot();
//			ITree dst = Generators.getInstance().getTree(right.toString()).getRoot();
//			Matcher m = Matchers.getInstance().getMatcher(src, dst); // retrieve the default matcher
//			m.match();
//			ActionGenerator g = new ActionGenerator(src, dst, m.getMappings());
//			g.generate();
//			actions = g.getActions(); // return the actions
//		} catch (UnsupportedOperationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		for (Action action : actions) {
//			Log.out(ChangeChecker.class, "'%s'", action);
//		}
		
		
		List<ChangeWrapper> changes = ChangeCheckerUtils.checkForChanges(left, right);

		if (options.hasOption(CmdOptions.COMPRESS_AST_CHANGES)) {
			removeNoiseInChanges(changes);
		}

		for (ChangeWrapper element : changes) {
			Log.out(ChangeChecker.class, element.toString());
		}
	}

	public static void removeNoiseInChanges(List<ChangeWrapper> changes) {
		for (Iterator<ChangeWrapper> iterator = changes.iterator(); iterator.hasNext();) {
			ChangeWrapper element = iterator.next();
			if (element.getIncludedDeltas() == null || element.getIncludedDeltas().isEmpty()) {
				iterator.remove();
			}
		}
	}

	

}
