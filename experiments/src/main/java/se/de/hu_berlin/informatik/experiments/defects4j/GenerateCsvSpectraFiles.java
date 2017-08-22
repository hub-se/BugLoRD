/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JBuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper.ModificationType;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.manipulation.FilterSpectraModule;
import se.de.hu_berlin.informatik.stardust.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.files.csv.CSVUtils;
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
public class GenerateCsvSpectraFiles {

	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		USE_SPECIAL_BICLUSTER_FORMAT("b", "bicluster", false, "Whether to use the special bicluster format.", false),
		USE_SHORT_IDENTIFIERS("s", "short", false, "Whether to use short identifiers.", false);

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
	 * command line arguments
	 */
	public static void main(String[] args) {

		OptionParser options = OptionParser.getOptions("GenerateBiClusterSpectraFiles", true, CmdOptions.class, args);

		// AbstractEntity mainEntity = Defects4JEntity.getDummyEntity();
		//
		// File archiveMainDir = mainEntity.getBenchmarkDir(false).toFile();
		//
		// if (!archiveMainDir.exists()) {
		// Log.abort(GenerateSpectraArchive.class,
		// "Archive main directory doesn't exist: '" +
		// mainEntity.getBenchmarkDir(false) + "'.");
		// }

		/*
		 * #====================================================================
		 * ================ # load the compressed spectra files and store them
		 * in a separate archive folder for # further usage in the future
		 * #====================================================================
		 * ================
		 */

		String spectraArchiveDir = Defects4J.getValueOf(Defects4JProperties.SPECTRA_ARCHIVE_DIR) + File.separator
				+ "biclusterFiles";
		String changesArchiveDir = spectraArchiveDir;

		PipeLinker linker = new PipeLinker().append(
				new ThreadedProcessor<BuggyFixedEntity, Object>(options.getNumberOfThreads(),
						new AbstractProcessor<BuggyFixedEntity, Object>() {

							@Override
							public Object processItem(BuggyFixedEntity input) {
								Log.out(GenerateCsvSpectraFiles.class, "Processing '%s'.", input);
								Entity bug = input.getBuggyVersion();

								// create spectra csv files
								Path spectraFile = bug.getWorkDataDir().resolve(BugLoRDConstants.SPECTRA_FILE_NAME);
								Path spectraFileFiltered = bug.getWorkDataDir()
										.resolve(BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME);
								Path spectraDestination = Paths.get(
										spectraArchiveDir,
										Misc.replaceWhitespacesInString(bug.getUniqueIdentifier(), "_") + ".csv");
								Path spectraDestinationFiltered = Paths.get(
										spectraArchiveDir,
										Misc.replaceWhitespacesInString(bug.getUniqueIdentifier(), "_")
												+ "_filtered.csv");

								if (spectraFile.toFile().exists()) {
									if (spectraFileFiltered.toFile().exists()) {
										ISpectra<SourceCodeBlock> spectra = SpectraFileUtils
												.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, spectraFile);

										SpectraFileUtils.saveBlockSpectraToCsvFile(
												spectra, spectraDestination,
												options.hasOption(CmdOptions.USE_SPECIAL_BICLUSTER_FORMAT),
												options.hasOption(CmdOptions.USE_SHORT_IDENTIFIERS));

										ISpectra<SourceCodeBlock> spectraFiltered = SpectraFileUtils
												.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, spectraFileFiltered);
										SpectraFileUtils.saveBlockSpectraToCsvFile(
												spectraFiltered, spectraDestinationFiltered,
												options.hasOption(CmdOptions.USE_SPECIAL_BICLUSTER_FORMAT),
												options.hasOption(CmdOptions.USE_SHORT_IDENTIFIERS));

									} else { // generate filtered spectra
										ISpectra<SourceCodeBlock> spectra = SpectraFileUtils
												.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, spectraFile);
										SpectraFileUtils.saveBlockSpectraToCsvFile(
												spectra, spectraDestination,
												options.hasOption(CmdOptions.USE_SPECIAL_BICLUSTER_FORMAT),
												options.hasOption(CmdOptions.USE_SHORT_IDENTIFIERS));
										SpectraFileUtils.saveBlockSpectraToCsvFile(
												new FilterSpectraModule<SourceCodeBlock>(
														INode.CoverageType.EF_EQUALS_ZERO).submit(spectra).getResult(),
												spectraDestinationFiltered,
												options.hasOption(CmdOptions.USE_SPECIAL_BICLUSTER_FORMAT),
												options.hasOption(CmdOptions.USE_SHORT_IDENTIFIERS));
									}
								} else {
									Log.err(GenerateCsvSpectraFiles.class, "'%s' does not exist.", spectraFile);
								}

								// create changes csv files
								Map<String, List<ChangeWrapper>> changes = input.loadChangesFromFile();

								if (changes != null) {
									saveChangesToCsvFile(changes, Paths.get(
											changesArchiveDir,
											Misc.replaceWhitespacesInString(bug.getUniqueIdentifier(), "_")
													+ "_changes.csv"));
								}

								return null;
							}
						})

		);

		// iterate over all projects
		for (String project : Defects4J.getAllProjects()) {
			String[] ids = Defects4J.getAllBugIDs(project);
			for (String id : ids) {
				linker.submit(new Defects4JBuggyFixedEntity(project, id));
			}
		}
		linker.shutdown();

		Log.out(GenerateCsvSpectraFiles.class, "All done!");

	}

	private static void saveChangesToCsvFile(Map<String, List<ChangeWrapper>> changes, Path output) {
		List<String[]> listOfRows = new ArrayList<>();

		for (Entry<String, List<ChangeWrapper>> entry : changes.entrySet()) {
			for (ChangeWrapper change : entry.getValue()) {
				ModificationType changeType = change.getModificationType();
				if (changeType != ModificationType.NO_SEMANTIC_CHANGE && changeType != ModificationType.NO_CHANGE) {
					for (int changedLine : change.getIncludedDeltas()) {
						listOfRows.add(new String[] { entry.getKey() + ":" + changedLine, changeType.toString() });
					}
				}
			}
		}

		CSVUtils.toCsvFile(listOfRows, false, output);
	}

}
