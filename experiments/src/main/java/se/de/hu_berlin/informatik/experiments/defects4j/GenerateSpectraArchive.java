/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JBuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.util.SpectraUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturn;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturnFactory;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.PipeLinker;
import se.de.hu_berlin.informatik.utils.tm.pipes.ThreadedProcessorPipe;

/**
 * Stores the generated spectra for future usage.
 * 
 * @author SimHigh
 */
public class GenerateSpectraArchive {
	
	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
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
		
		OptionParser options = OptionParser.getOptions("GenerateSpectraArchive", true, CmdOptions.class, args);

//		AbstractEntity mainEntity = Defects4JEntity.getDummyEntity();
//		
//		File archiveMainDir = mainEntity.getBenchmarkDir(false).toFile();
//		
//		if (!archiveMainDir.exists()) {
//			Log.abort(GenerateSpectraArchive.class, 
//					"Archive main directory doesn't exist: '" + mainEntity.getBenchmarkDir(false) + "'.");
//		}
		
		/* #====================================================================================
		 * # load the compressed spectra files and store them in a separate archive folder for
		 * # further usage in the future
		 * #==================================================================================== */
		
		String spectraArchiveDir = Defects4J.getValueOf(Defects4JProperties.SPECTRA_ARCHIVE_DIR);
		String changesArchiveDir = Defects4J.getValueOf(Defects4JProperties.CHANGES_ARCHIVE_DIR);

		//TODO this is for now. In the future, we may just move the specific files...
		PipeLinker linker = new PipeLinker().append(
				new ThreadedProcessorPipe<BuggyFixedEntity,Object>(
						options.getNumberOfThreads(), 
						new EHWithInputAndReturnFactory<BuggyFixedEntity, Object>() {

					@Override
					public EHWithInputAndReturn<BuggyFixedEntity, Object> newFreshInstance() {
						return new EHWithInputAndReturn<BuggyFixedEntity, Object>() {

							@Override
							public void resetAndInit() {
								// nothing to do here
							}

							@Override
							public Object processInput(BuggyFixedEntity input) {
								Entity bug = input.getBuggyVersion();
								Path spectraFile = bug.getWorkDataDir()
										.resolve(BugLoRDConstants.DIR_NAME_RANKING)
										.resolve(BugLoRDConstants.SPECTRA_FILE_NAME);

								Log.out(GenerateSpectraArchive.class, "Processing '%s'.", input);
								if (spectraFile.toFile().exists()) {
									ISpectra<SourceCodeBlock> spectra = SpectraUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, spectraFile);
									SpectraUtils.saveBlockSpectraToZipFile(spectra, Paths.get(spectraArchiveDir, 
											Misc.replaceWhitespacesInString(bug.getUniqueIdentifier(), "_") + ".zip"), true, true, true);
								} else {
									Log.err(GenerateSpectraArchive.class, "'%s' does not exist.", spectraFile);
								}
								
								Map<String, List<ChangeWrapper>> changes = input.loadChangesFromFile();
								
								if (changes != null) {
									ChangeWrapper.storeChanges(changes, Paths.get(changesArchiveDir, 
											Misc.replaceWhitespacesInString(bug.getUniqueIdentifier(), "_") + ".changes"));
									ChangeWrapper.storeChangesHumanReadable(changes, Paths.get(changesArchiveDir, 
											Misc.replaceWhitespacesInString(bug.getUniqueIdentifier(), "_") + ".changes_human"));
								}
								
//								SpectraUtils.saveSpectraToZipFile(spectra, Paths.get(spectraArchiveDir, 
//										Misc.replaceWhitespacesInString(input.getUniqueIdentifier(), "_") + ".zip"), true);
								
//								SpectraUtils.saveSpectraToBugMinerZipFile(spectra, Paths.get(spectraArchiveDir, filename + "_BugMiner.zip"));
								return null;
							}
						};
					}
				})

				);
		
//		,
//		new AbstractPipe<BuggyFixedEntity,Object>(true) {
//			@Override
//			public Object processItem(BuggyFixedEntity item) {
//				Path spectraFile = item.getWorkDataDir()
//				.resolve(BugLoRDConstants.DIR_NAME_RANKING)
//				.resolve(BugLoRDConstants.SPECTRA_FILE_NAME);
//				
//				Log.out(GenerateSpectraArchive.class, "Processing file '%s'.", spectraFile);
//				int count = spectraFile.getNameCount();
//				String filename = spectraFile.getName(count-4).toString() + "-" + spectraFile.getName(count-3).toString();
//				ISpectra<String> spectra = SpectraUtils.loadSpectraFromZipFile(spectraFile);
//				SpectraUtils.saveSpectraToZipFile(spectra, Paths.get(spectraArchiveDir, filename + ".zip"), true);
//				SpectraUtils.saveSpectraToBugMinerZipFile(spectra, Paths.get(spectraArchiveDir, filename + "_BugMiner.zip"));
//				return null;
//			}
//		}
		
		//iterate over all projects
		for (String project : Defects4J.getAllProjects()) {
			String[] ids = Defects4J.getAllBugIDs(project); 
			for (String id : ids) {
				linker.submit(new Defects4JBuggyFixedEntity(project, id));
			}
		}
		linker.shutdown();
		
		Log.out(GenerateSpectraArchive.class, "All done!");
		
	}
	
}
