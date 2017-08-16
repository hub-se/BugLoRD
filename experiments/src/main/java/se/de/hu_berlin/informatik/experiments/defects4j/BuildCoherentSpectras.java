/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j;

import java.nio.file.Path;
import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JBuggyFixedEntity;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.manipulation.BuildCoherentSpectraModule;
import se.de.hu_berlin.informatik.stardust.spectra.manipulation.FilterSpectraModule;
import se.de.hu_berlin.informatik.stardust.spectra.manipulation.SaveSpectraModule;
import se.de.hu_berlin.informatik.stardust.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.basics.ThreadedProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.ProcessorSocket;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.PipeLinker;

/**
 * Stores the generated spectra for future usage.
 * 
 * @author SimHigh
 */
public class BuildCoherentSpectras {

	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
//		OUTPUT("o", "output", true, "Path to output csv statistics file (e.g. '~/outputDir/project/bugID/data.csv').", true)
		;
		
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
				final boolean hasArg, final String description, int groupId) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(false).
					hasArg(hasArg).desc(description).build(), groupId);
		}

		//adds the given option that will be part of the group with the given id
		CmdOptions(Option option, int groupId) {
			this.option = new OptionWrapper(option, groupId);
		}

		//adds the given option that will be part of no group
		CmdOptions(Option option) {
			this(option, NO_GROUP);
		}

		@Override public String toString() { return option.getOption().getOpt(); }
		@Override public OptionWrapper getOptionWrapper() { return option; }
	}

	/**
	 * @param args
	 * command line arguments
	 */
	public static void main(String[] args) {

		OptionParser options = OptionParser.getOptions("BuildCoherentSpectras", true, CmdOptions.class, args);

		//		AbstractEntity mainEntity = Defects4JEntity.getDummyEntity();
		//		
		//		File archiveMainDir = mainEntity.getBenchmarkDir(false).toFile();
		//		
		//		if (!archiveMainDir.exists()) {
		//			Log.abort(GenerateSpectraArchive.class, 
		//					"Archive main directory doesn't exist: '" + mainEntity.getBenchmarkDir(false) + "'.");
		//		}

		/* #====================================================================================
		 * # load the compressed spectra files and generate/save coherent spectras
		 * #==================================================================================== */

		PipeLinker linker = new PipeLinker().append(
				new ThreadedProcessor<BuggyFixedEntity,Object>(
						options.getNumberOfThreads(), 
						new AbstractProcessor<BuggyFixedEntity, Object>() {

							@Override
							public Object processItem(BuggyFixedEntity input, ProcessorSocket<BuggyFixedEntity, Object> socket) {
								Log.out(BuildCoherentSpectras.class, "Processing %s.", input);
								Entity bug = input.getBuggyVersion();
								Path spectraFile = bug.getWorkDataDir()
										.resolve(BugLoRDConstants.SPECTRA_FILE_NAME);
								if (!spectraFile.toFile().exists()) {
									Log.err(BuildCoherentSpectras.class, "Spectra file does not exist for %s.", input);
									return null;
								}
								Path spectraFileFiltered = bug.getWorkDataDir()
										.resolve(BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME);

								//load the full spectra
								ISpectra<SourceCodeBlock> spectra = SpectraFileUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, spectraFile);
								
								//generate the coherent version
								spectra = new BuildCoherentSpectraModule().submit(spectra).getResult();
								
								//save the coherent full spectra
								new SaveSpectraModule<>(SourceCodeBlock.DUMMY, spectraFile).submit(spectra);

								//generate the filtered coherent spectra
								//(building a coherent spectra from an already filtered spectra may yield wrong results
								//by generating blocks that reach over filtered out nodes...)
								spectra = new FilterSpectraModule<SourceCodeBlock>(INode.CoverageType.EF_EQUALS_ZERO).submit(spectra).getResult();
								
								//save the filtered spectra
								new SaveSpectraModule<>(SourceCodeBlock.DUMMY, spectraFileFiltered).submit(spectra);

								return null;
							}
							
						})
				);

		//iterate over all projects
		for (String project : Defects4J.getAllProjects()) {
			String[] ids = Defects4J.getAllBugIDs(project); 
			for (String id : ids) {
				linker.submit(new Defects4JBuggyFixedEntity(project, id));
			}
		}
		linker.shutdown();

		Log.out(BuildCoherentSpectras.class, "All done!");

	}

}
