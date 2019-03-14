/**
 * 
 */
package se.de.hu_berlin.informatik.gen.spectra.jacoco.modules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.cli.Option;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator;

import se.de.hu_berlin.informatik.gen.spectra.AbstractInstrumenter;
import se.de.hu_berlin.informatik.gen.spectra.jacoco.JaCoCoSpectraGenerationFactory;
import se.de.hu_berlin.informatik.utils.miscellaneous.ClassPathParser;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.basics.ExecuteMainClassInNewJVM;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;


/**
 * Instruments given classes with jaCoCo.
 * 
 * @author Simon Heiden
 */
final public class JaCoCoInstrumenter extends AbstractInstrumenter {

	public JaCoCoInstrumenter(Path projectDir, String instrumentedDir, String testClassPath,
			String[] pathsToBinaries) {
		super(projectDir, instrumentedDir, testClassPath, pathsToBinaries);
	}

	@Override
	public int instrumentClasses() {
		/* #====================================================================================
		 * # (offline) instrumentation
		 * #==================================================================================== */
		if (JaCoCoSpectraGenerationFactory.OFFLINE_INSTRUMENTATION) {
			//build arguments for instrumentation
			String[] instrArgs = { 
					Instrument.CmdOptions.OUTPUT.asArg(), Paths.get(instrumentedDir).toAbsolutePath().toString()};

			if (testClassPath != null) {
				instrArgs = Misc.addToArrayAndReturnResult(instrArgs, 
						Instrument.CmdOptions.CLASS_PATH.asArg(), testClassPath);
			}

			if (pathsToBinaries != null) {
				instrArgs = Misc.addToArrayAndReturnResult(instrArgs, Instrument.CmdOptions.INSTRUMENT_CLASSES.asArg());
				instrArgs = Misc.joinArrays(instrArgs, pathsToBinaries);
			}

			String systemClassPath = new ClassPathParser().parseSystemClasspath().getClasspath();
			
			//we need to run the tests in a new jvm that uses the given Java version
			return new ExecuteMainClassInNewJVM(//javaHome,
					null,
					Instrument.class, 
					//classPath,
					systemClassPath + (testClassPath != null ? File.pathSeparator + testClassPath : ""),
					projectDir.toAbsolutePath().toFile())
					.submit(instrArgs)
					.getResult();
		} else {
			return 0;
		}
	}

	public final static class Instrument {

		private Instrument() {
			//disallow instantiation
		}

		public enum CmdOptions implements OptionWrapperInterface {
			/* add options here according to your needs */
			CLASS_PATH("cp", "classPath", true, "An additional class path which may be needed for the execution of tests. "
					+ "Will be appended to the regular class path if this option is set.", false),
			INSTRUMENT_CLASSES(Option.builder("c").longOpt("classes").required()
					.hasArgs().desc("A list of classes/directories to instrument with Cobertura.").build()),
			OUTPUT("o", "output", true, "Path to output directory.", true);

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

		private static Instrumenter instrumenter;

		private static final List<File> source = new ArrayList<>();

		/**
		 * @param args
		 * command line arguments
		 */
		public static void main(final String[] args) {

			final OptionParser options = OptionParser.getOptions("Instrument", false, CmdOptions.class, args);

			final Path instrumentedDir = options.isDirectory(CmdOptions.OUTPUT, false).toAbsolutePath();
			final String[] classesToInstrument = options.getOptionValues(CmdOptions.INSTRUMENT_CLASSES);

			for (String file : classesToInstrument) {
				source.add(new File(file).getAbsoluteFile());
			}

			final File absoluteDest = instrumentedDir.toFile().getAbsoluteFile();
			instrumenter = new Instrumenter(new OfflineInstrumentationAccessGenerator());
			int total = 0;
			for (final File s : source) {
				if (s.isFile()) {
					try {
						total += instrument(s, new File(absoluteDest, s.getName()));
						//						Log.out(Instrument.class, "Instrumented %s.", s);
					} catch (IOException e) {
						Log.err(Instrument.class, e, "Could not instrument '%s' with target '%s'.", s, new File(absoluteDest, s.getName()));
					}
				} else {
					try {
						total += instrumentRecursive(s, absoluteDest);
					} catch (IOException e) {
						Log.err(Instrument.class, e, "Could not instrument folder '%s' with target '%s'.", s, absoluteDest);
					}
				}
			}
			Log.out(Instrument.class, "%s classes instrumented to %s.",
					total, absoluteDest);
		}

		private static int instrumentRecursive(final File src, final File dest)
				throws IOException {
			int total = 0;
			if (src.isDirectory()) {
				for (final File child : Objects.requireNonNull(src.listFiles())) {
					total += instrumentRecursive(child,
							new File(dest, child.getName()));
				}
			} else {
				total += instrument(src, dest);
				//				Log.out(Instrument.class, "Instrumented %s.", src);
			}
			return total;
		}

		private static int instrument(final File src, final File dest) throws IOException {
			dest.getParentFile().mkdirs();
			try (InputStream input = new FileInputStream(src)) {
				try (OutputStream output = new FileOutputStream(dest)) {
					return instrumenter.instrumentAll(input, output,
							src.getAbsolutePath());
				}
			} catch (final IOException e) {
				dest.delete();
				throw e;
			}
		}

	}

}
