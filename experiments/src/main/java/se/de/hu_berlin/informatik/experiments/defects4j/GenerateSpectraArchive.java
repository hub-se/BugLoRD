/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.manipulation.FilterSpectraModule;
import se.de.hu_berlin.informatik.stardust.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.basics.ThreadedProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.PipeLinker;

/**
 * Stores the generated spectra for future usage.
 * 
 * @author SimHigh
 */
public class GenerateSpectraArchive {
	
	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		CREATE_SPECTRA_ARCHIVE("s", "spectra", false, "Whether the spectra archive shall be created/updated.", false),
		CREATE_CHANGES_ARCHIVE("c", "changes", false, "Whether the changes archive shall be created/updated.", false);

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
		
		options.assertAtLeastOneOptionSet(CmdOptions.CREATE_CHANGES_ARCHIVE, CmdOptions.CREATE_SPECTRA_ARCHIVE);
		
		String spectraArchiveDir = Defects4J.getValueOf(Defects4JProperties.SPECTRA_ARCHIVE_DIR);
		String changesArchiveDir = Defects4J.getValueOf(Defects4JProperties.CHANGES_ARCHIVE_DIR);

		PipeLinker linker = new PipeLinker().append(
				new ThreadedProcessor<>(
						options.getNumberOfThreads(), 
						new AbstractProcessor<BuggyFixedEntity<?>, Object>() {

							@Override
							public Object processItem(BuggyFixedEntity<?> input) {
								Log.out(GenerateSpectraArchive.class, "Processing '%s'.", input);
								Entity bug = input.getBuggyVersion();
								
								if (options.hasOption(CmdOptions.CREATE_SPECTRA_ARCHIVE)) {	
									// merged
									copySpecificSpectra(spectraArchiveDir, input, bug, null);
									
									// JaCoCo
									copySpecificSpectra(spectraArchiveDir, input, bug, BugLoRDConstants.DIR_NAME_JACOCO);
									
									// Cobertura
									copySpecificSpectra(spectraArchiveDir, input, bug, BugLoRDConstants.DIR_NAME_COBERTURA);
									
								}
								
								if (options.hasOption(CmdOptions.CREATE_CHANGES_ARCHIVE)) {
									Map<String, List<ChangeWrapper>> changes = input.loadChangesFromFile();

									if (changes != null) {
										ChangeWrapper.storeChanges(changes, Paths.get(changesArchiveDir, 
												Misc.replaceWhitespacesInString(bug.getUniqueIdentifier(), "_") + ".changes"));
										ChangeWrapper.storeChangesHumanReadable(changes, Paths.get(changesArchiveDir, 
												Misc.replaceWhitespacesInString(bug.getUniqueIdentifier(), "_") + ".changes_human"));
									}
								}
								
//								SpectraUtils.saveSpectraToZipFile(spectra, Paths.get(spectraArchiveDir, 
//										Misc.replaceWhitespacesInString(input.getUniqueIdentifier(), "_") + ".zip"), true);
								
//								SpectraUtils.saveSpectraToBugMinerZipFile(spectra, Paths.get(spectraArchiveDir, filename + "_BugMiner.zip"));
								return null;
							}

							private void copySpecificSpectra(String spectraArchiveDir, BuggyFixedEntity<?> input,
									Entity bug, String subDirName) {
								Path spectraDestination;
								Path spectraDestinationFiltered;
								Path spectraFile = BugLoRD.getSpectraFilePath(bug, subDirName);
								Path spectraFileFiltered = BugLoRD.getFilteredSpectraFilePath(bug, subDirName);
								if (subDirName == null) {
									spectraDestination = Paths.get(spectraArchiveDir,
											Misc.replaceWhitespacesInString(bug.getUniqueIdentifier(), "_") + ".zip");
									spectraDestinationFiltered = Paths.get(spectraArchiveDir,
											Misc.replaceWhitespacesInString(bug.getUniqueIdentifier(), "_") + "_filtered.zip");
								} else {
									spectraDestination = Paths.get(spectraArchiveDir, subDirName,
											Misc.replaceWhitespacesInString(bug.getUniqueIdentifier(), "_") + ".zip");
									spectraDestinationFiltered = Paths.get(spectraArchiveDir, subDirName,
											Misc.replaceWhitespacesInString(bug.getUniqueIdentifier(), "_") + "_filtered.zip");
								}

								if (!spectraDestination.toFile().exists() || !spectraDestinationFiltered.toFile().exists()) {
									copySpectra(
											input, spectraFile, spectraFileFiltered, spectraDestination,
											spectraDestinationFiltered);
								}
							}

							private void copySpectra(BuggyFixedEntity<?> input, 
									Path spectraFile, Path spectraFileFiltered,
									Path spectraDestination, Path spectraDestinationFiltered) {
								if (spectraFile.toFile().exists()) {
									try {
										FileUtils.copyFileOrDir(spectraFile.toFile(), 
												spectraDestination.toFile(), StandardCopyOption.REPLACE_EXISTING);
									} catch (IOException e) {
										Log.err(this, "Could not copy spectra for %s.", input);
										ISpectra<SourceCodeBlock> spectra = SpectraFileUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, spectraFile);
										SpectraFileUtils.saveBlockSpectraToZipFile(spectra, spectraDestination, true, true, true);
									}
									if (spectraFileFiltered.toFile().exists()) {
										try {
											FileUtils.copyFileOrDir(spectraFileFiltered.toFile(), 
													spectraDestinationFiltered.toFile(), StandardCopyOption.REPLACE_EXISTING);
										} catch (IOException e) {
											Log.err(this, "Could not copy filtered spectra for %s.", input);
											ISpectra<SourceCodeBlock> spectra = SpectraFileUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, spectraFileFiltered);
											SpectraFileUtils.saveBlockSpectraToZipFile(spectra, spectraDestinationFiltered, true, true, true);
										}
									} else { //generate filtered spectra
										ISpectra<SourceCodeBlock> spectra = SpectraFileUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, spectraFile);
										SpectraFileUtils.saveBlockSpectraToZipFile(
												new FilterSpectraModule<SourceCodeBlock>(INode.CoverageType.EF_EQUALS_ZERO).submit(spectra).getResult(),
												spectraDestinationFiltered, true, true, true);
									}
								} else {
									Log.err(GenerateSpectraArchive.class, "'%s' does not exist.", spectraFile);
								}
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
