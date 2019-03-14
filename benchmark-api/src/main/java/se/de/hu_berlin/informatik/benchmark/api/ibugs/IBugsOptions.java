package se.de.hu_berlin.informatik.benchmark.api.ibugs;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;


/**
 * The options for the ast language model builder are defined in this class as
 * well as the default values.
 */
public class IBugsOptions {

	public enum IBugsCmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		/* We could split most of the commands in a pre and post fix command if we want to execute them alone */
		FIX_ID( "id", "fixId", true, 
				"The id or a list of ids separated by " + IBugs.LIST_SEPARATOR + " that should be used. " +
				"Use \"" + IBugs.USE_ALL_IDS + "\" to use all known fix ids.", true),
		PROJECT_ROOT_DIR( "root", "rootDir", true,
				"The root directory of the project that contains all the configuration and build files", true ),
		CHECKOUT("co", "checkout", false, 
				"Checkout the pre and post fix version of a repository for a given fixed id", false),
		BUILD("b", "build", false, 
				"Build the pre and post fix version of a repository for a given fixed id", false),
		BUILD_TESTS("bt", "build-tests", false, 
				"Build the test classes of the pre and post fix version of a repository for a given fixed id", false),
		RUN_JUNIT( "rut", "run-unit-tests", false, 
				"Runs the jUnit tests for the pre and post fix version of a repository for a given fixed id", false ),
		RUN_HARNESS( "rht", "run-harness-tests", false, 
				"Runs the harness tests for the pre and post fix version of a repository for a given fixed id", false ),
		GEN_TEST_SCRIPT( "gts", "gen-test-script", false,
				"Generates a test script for the pre and post version of a repository for a given fixed id", false),
		PROJECT( "p", "project", true,
				"It is possible to specify a project. Currently \"" + IBugs.DEFAULT_PROJECT + "\" is always used but others may implemented in future versions", false );
		
		/* the following code blocks should not need to be changed */
		final private OptionWrapper option;
		
		//adds an option that is not part of any group
		IBugsCmdOptions(final String opt, final String longOpt, 
				final boolean hasArg, final String description, final boolean required) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(required).
					hasArg(hasArg).desc(description).build(), NO_GROUP);
		}

		@Override public String toString() { return option.getOption().getOpt(); }
		@Override public OptionWrapper getOptionWrapper() { return option; }
	}
}
