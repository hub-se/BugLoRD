/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JBuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.changechecker.ChangeCheckerUtils;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper.ModificationType;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.ToolSpecific;
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
import se.de.hu_berlin.informatik.utils.threaded.SemaphoreThreadLimit;
import se.de.hu_berlin.informatik.utils.threaded.ThreadLimit;

/**
 * Stores the generated spectra for future usage.
 * 
 * @author SimHigh
 */
public class GenerateCsvSpectraFiles {

	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		USE_SPECIAL_BICLUSTER_FORMAT("b", "bicluster", false, "Whether to use the special bicluster format.", false),
		USE_SHORT_IDENTIFIERS("s", "short", false, "Whether to use short identifiers.", false),
		SPECTRA_TOOL("st", "spectraTool", ToolSpecific.class, ToolSpecific.MERGED, 
				"Which spectra should be used?.", false);

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

		//adds an option that may have arguments from a given set (Enum)
		<T extends Enum<T>> CmdOptions(final String opt, final String longOpt, 
				Class<T> valueSet, T defaultValue, final String description, final boolean required) {
			if (defaultValue == null) {
				this.option = new OptionWrapper(
						Option.builder(opt).longOpt(longOpt).required(required).
						hasArgs().desc(description + " Possible arguments: " +
								Misc.enumToString(valueSet) + ".").build(), NO_GROUP);
			} else {
				this.option = new OptionWrapper(
						Option.builder(opt).longOpt(longOpt).required(required).
						hasArg(true).desc(description + " Possible arguments: " +
								Misc.enumToString(valueSet) + ". Default: " + 
								defaultValue.toString() + ".").build(), NO_GROUP);
			}
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

		String spectraArchiveDir = Defects4J.getValueOf(Defects4JProperties.SPECTRA_ARCHIVE_DIR) + File.separator
				+ "biclusterFiles";
		String changesArchiveDir = spectraArchiveDir;
		
		ToolSpecific toolSpecific = options.getOptionValue(CmdOptions.SPECTRA_TOOL, 
				ToolSpecific.class, ToolSpecific.MERGED, true);
		final String subDirName = BugLoRD.getSubDirName(toolSpecific);
		
		int numberOfThreads = options.getNumberOfThreads();
		ThreadLimit limit = new SemaphoreThreadLimit(numberOfThreads);

		PipeLinker linker = new PipeLinker().append(
				new ThreadedProcessor<>(numberOfThreads, limit,
						new GenerateCsvSpectraProcessor(spectraArchiveDir, subDirName,
								options.hasOption(CmdOptions.USE_SPECIAL_BICLUSTER_FORMAT), 
								options.hasOption(CmdOptions.USE_SHORT_IDENTIFIERS))),
				new ThreadedProcessor<>(numberOfThreads, limit,
						new GenerateCsvChangesProcessor(changesArchiveDir))
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

	private static class GenerateCsvSpectraProcessor extends AbstractProcessor<BuggyFixedEntity<?>, BuggyFixedEntity<?>> {

		private String spectraArchiveDir;
		private String subDirName;
		private boolean useBiClusterFormat;
		private boolean useShortIdentifiers;
		
		public GenerateCsvSpectraProcessor(String spectraArchiveDir, String subDirName, 
				boolean useBiClusterFormat, boolean useShortIdentifiers) {
			this.spectraArchiveDir = spectraArchiveDir;
			this.subDirName = subDirName;
			this.useBiClusterFormat = useBiClusterFormat;
			this.useShortIdentifiers = useShortIdentifiers;
		}

		@Override
		public BuggyFixedEntity<?> processItem(BuggyFixedEntity<?> input) {
			Log.out(BuildCoherentSpectras.class, "Processing %s with sub directory '%s'.", 
					input, subDirName == null ? "<none>" : subDirName);
			Entity bug = input.getBuggyVersion();

			// create spectra csv files
			Path spectraFile = BugLoRD.getSpectraFilePath(bug, subDirName);
			Path spectraFileFiltered = BugLoRD.getFilteredSpectraFilePath(bug, subDirName);
			
			Path spectraDestination;
			Path spectraDestinationFiltered;
			if (subDirName == null) {
				spectraDestination = Paths.get(
						spectraArchiveDir,
						Misc.replaceWhitespacesInString(bug.getUniqueIdentifier(), "_") + ".csv");
				spectraDestinationFiltered = Paths.get(
						spectraArchiveDir,
						Misc.replaceWhitespacesInString(bug.getUniqueIdentifier(), "_")
						+ "_filtered.csv");
			} else {
				spectraDestination = Paths.get(
						spectraArchiveDir,
						subDirName,
						Misc.replaceWhitespacesInString(bug.getUniqueIdentifier(), "_") + ".csv");
				spectraDestinationFiltered = Paths.get(
						spectraArchiveDir,
						subDirName,
						Misc.replaceWhitespacesInString(bug.getUniqueIdentifier(), "_")
						+ "_filtered.csv");
			}

			if (spectraFile.toFile().exists()) {
				if (spectraFileFiltered.toFile().exists()) {
					ISpectra<SourceCodeBlock> spectra = SpectraFileUtils
							.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, spectraFile);

					SpectraFileUtils.saveBlockSpectraToCsvFile(
							spectra, spectraDestination,
							useBiClusterFormat,
							useShortIdentifiers);

					ISpectra<SourceCodeBlock> spectraFiltered = SpectraFileUtils
							.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, spectraFileFiltered);
					SpectraFileUtils.saveBlockSpectraToCsvFile(
							spectraFiltered, spectraDestinationFiltered,
							useBiClusterFormat,
							useShortIdentifiers);

				} else { // generate filtered spectra
					ISpectra<SourceCodeBlock> spectra = SpectraFileUtils
							.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, spectraFile);
					SpectraFileUtils.saveBlockSpectraToCsvFile(
							spectra, spectraDestination,
							useBiClusterFormat,
							useShortIdentifiers);
					SpectraFileUtils.saveBlockSpectraToCsvFile(
							new FilterSpectraModule<SourceCodeBlock>(
									INode.CoverageType.EF_EQUALS_ZERO).submit(spectra).getResult(),
							spectraDestinationFiltered,
							useBiClusterFormat,
							useShortIdentifiers);
				}
			} else {
				Log.err(GenerateCsvSpectraFiles.class, "'%s' does not exist.", spectraFile);
			}

			return input;
		}
	}
	
	private static class GenerateCsvChangesProcessor extends AbstractProcessor<BuggyFixedEntity<?>, BuggyFixedEntity<?>> {

		private String changesArchiveDir;
		
		public GenerateCsvChangesProcessor(String changesArchiveDir) {
			this.changesArchiveDir = changesArchiveDir;
		}

		@Override
		public BuggyFixedEntity<?> processItem(BuggyFixedEntity<?> input) {
			Log.out(this, "Processing '%s'.", input);
			Entity bug = input.getBuggyVersion();
			
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
	}
	
	private static void saveChangesToCsvFile(Map<String, List<ChangeWrapper>> changes, Path output) {
		List<String[]> listOfRows = new ArrayList<>();

		for (Entry<String, List<ChangeWrapper>> entry : changes.entrySet()) {
			List<ChangeWrapper> list = entry.getValue();
			ChangeCheckerUtils.removeChangesWithType(list, ModificationType.NO_CHANGE);
			ChangeCheckerUtils.removeChangesWithType(list, ModificationType.NO_SEMANTIC_CHANGE);
			List<Integer> deltas = new ArrayList<>(ChangeCheckerUtils.getAllChangeDeltas(list));
			Collections.sort(deltas);
			for (int changedLine : deltas) {
				List<ChangeWrapper> modifications = ChangeCheckerUtils.getModifications(changedLine, changedLine, true, list);
				ModificationType changeType = ChangeCheckerUtils.getMostImportantType(modifications);
				listOfRows.add(new String[] { entry.getKey() + ":" + changedLine, changeType.toString() });
			}
		}

		CSVUtils.toCsvFile(listOfRows, false, output);
	}

}
