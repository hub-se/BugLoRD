package se.de.hu_berlin.informatik.gen.spectra.predicates.modules;

import org.apache.commons.cli.Option;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import se.de.hu_berlin.informatik.gen.spectra.AbstractInstrumenter;
import se.de.hu_berlin.informatik.utils.miscellaneous.ClassPathParser;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.basics.ExecuteMainClassInNewJVM;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final public class PredicateInstrumenter  extends AbstractInstrumenter {

    private final String joinStrategy;

    public PredicateInstrumenter(Path projectDir, String instrumentedDir, String testClassPath, String joinstrategy, String... pathsToBinaries) {
        super(projectDir, instrumentedDir, testClassPath, pathsToBinaries);
        joinStrategy = joinstrategy;
    }

    @Override
    public int instrumentClasses() {
        String[] instrArgs = {
                PredicateInstrumenter.Instrument.CmdOptions.OUTPUT.asArg(), Paths.get(instrumentedDir).toAbsolutePath().toString()};

        if (testClassPath != null) {
            instrArgs = Misc.addToArrayAndReturnResult(instrArgs,
                    PredicateInstrumenter.Instrument.CmdOptions.CLASS_PATH.asArg(), testClassPath);
        }

        if (pathsToBinaries != null) {
            instrArgs = Misc.addToArrayAndReturnResult(instrArgs, PredicateInstrumenter.Instrument.CmdOptions.INSTRUMENT_CLASSES.asArg());
            instrArgs = Misc.joinArrays(instrArgs, pathsToBinaries);
        }

        if (this.joinStrategy != null) {
            instrArgs = Misc.addToArrayAndReturnResult(instrArgs, Instrument.CmdOptions.JOINSTRATEGY.asArg(), this.joinStrategy);
        }

        String systemClassPath = new ClassPathParser().parseSystemClasspath().getClasspath();

        //we need to run the tests in a new jvm that uses the given Java version
        return new ExecuteMainClassInNewJVM(//javaHome,
                null,
                PredicateInstrumenter.Instrument.class,
                //classPath,
                systemClassPath + (testClassPath != null ? File.pathSeparator + testClassPath : ""),
                projectDir.toAbsolutePath().toFile()
                //,"-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5009"
                )
                .submit(instrArgs)
                .getResult();
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
                    .hasArgs().desc("A list of classes/directories to instrument.").build()),
            OUTPUT("o", "output", true, "Path to output directory.", true),
            JOINSTRATEGY("j", "joinStrategy", true, "Strategy used to construct joint Predicates.", false);

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

            @Override
            public String toString() {
                return option.getOption().getOpt();
            }

            @Override
            public OptionWrapper getOptionWrapper() {
                return option;
            }
        }


        private static final List<File> source = new ArrayList<>();

        /**
         * @param args
         * command line arguments
         */
        public static void main(final String[] args) throws Exception {

            final OptionParser options = OptionParser.getOptions("Instrument", false, PredicateInstrumenter.Instrument.CmdOptions.class, args);

            final Path instrumentedDir = options.isDirectory(PredicateInstrumenter.Instrument.CmdOptions.OUTPUT, false).toAbsolutePath();
            final String[] classesToInstrument = options.getOptionValues(PredicateInstrumenter.Instrument.CmdOptions.INSTRUMENT_CLASSES);

            for (String file : classesToInstrument) {
                source.add(new File(file).getAbsoluteFile());
            }

            if (options.getOptionValue(CmdOptions.JOINSTRATEGY) != null)
                Output.joinStrategy = Output.JOINSTRATEGY.valueOf(options.getOptionValue(CmdOptions.JOINSTRATEGY));

            final File absoluteDest = instrumentedDir.toFile().getAbsoluteFile();
            int total = 0;
            for (final File s : source) {
                if (s.isFile()) {
                    try {
                        total += instrument(s, new File(absoluteDest, s.getName()));
                        //						Log.out(Instrument.class, "Instrumented %s.", s);
                    } catch (IOException e) {
                        Log.err(PredicateInstrumenter.Instrument.class, e, "Could not instrument '%s' with target '%s'.", s, new File(absoluteDest, s.getName()));
                    }
                } else {
                    try {
                        total += instrumentRecursive(s, absoluteDest);
                    } catch (IOException e) {
                        Log.err(PredicateInstrumenter.Instrument.class, e, "Could not instrument folder '%s' with target '%s'.", s, absoluteDest);
                    }
                }
            }
            Log.out(PredicateInstrumenter.Instrument.class, "%s classes instrumented to %s.",
                    total, absoluteDest);

            Output.writeToFile(new File(absoluteDest.getParent()),"Predicates.db", true);
            //Output.writeToHumanFile(absoluteDest);
//            //adds static Output class
//            FileOutputStream fos = new FileOutputStream(absoluteDest + "/Output.class");
//            ClassReader cr = new ClassReader(Output.class.getName());
//            ClassWriter cw = new ClassWriter(0);
//            cr.accept(cw, 0);
//            fos.write(cw.toByteArray());
//            fos.close();
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
                if (!src.getName().endsWith(".class"))
                    return 0;
                total += instrument(src, dest);
                				//Log.out(Instrument.class, "Instrumented %s.", src);
            }
            return total;
        }

        private static int instrument(final File src, final File dest) throws IOException {
            dest.getParentFile().mkdirs();
            //Log.out(Instrument.class, "Instrumenting %s.", src.getName());
            //System.out.println("File: " + src.getName());
            try (InputStream input = new FileInputStream(src)) {
                try (OutputStream output = new FileOutputStream(dest)) {
                    byte[] b;

                    ClassReader cr = new ClassReader(input);
                    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                    ClassVisitor cv = new PredicateClassVisitor(cw);
                    cr.accept(cv, ClassReader.EXPAND_FRAMES);
                    b = cw.toByteArray();

                    output.write(b);
                    output.close();
                    return 1;
                }
            } catch (final IOException e) {
                dest.delete();
                throw e;
            }
        }

    }
}
