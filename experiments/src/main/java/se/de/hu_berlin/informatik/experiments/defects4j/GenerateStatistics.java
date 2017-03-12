/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JBuggyFixedEntity;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.manipulation.FilterSpectraModule;
import se.de.hu_berlin.informatik.stardust.util.SpectraUtils;
import se.de.hu_berlin.informatik.utils.files.csv.CSVUtils;
import se.de.hu_berlin.informatik.utils.files.processors.StringListToFileWriter;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.Producer;
import se.de.hu_berlin.informatik.utils.processors.basics.ThreadedProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.PipeLinker;

/**
 * Stores the generated spectra for future usage.
 * 
 * @author SimHigh
 */
public class GenerateStatistics {

	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		OUTPUT("o", "output", true, "Path to output csv statistics file (e.g. '~/outputDir/project/bugID/data.csv').", true);

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
		 * # load the compressed spectra files and generate a statistics csv file
		 * #==================================================================================== */

		//get the output path (does not need to exist)
		Path output = options.isFile(CmdOptions.OUTPUT, false);
		
		PipeLinker linker = new PipeLinker().append(
				new ThreadedProcessor<BuggyFixedEntity,Object>(
						options.getNumberOfThreads(), 
						new AbstractProcessor<BuggyFixedEntity, Object>() {

							@Override
							public Object processItem(BuggyFixedEntity input, Producer<Object> producer) {
								Log.out(GenerateStatistics.class, "Processing %s.", input);
								Entity bug = input.getBuggyVersion();
								Path spectraFile = bug.getWorkDataDir()
										.resolve(BugLoRDConstants.DIR_NAME_RANKING)
										.resolve(BugLoRDConstants.SPECTRA_FILE_NAME);
								if (!spectraFile.toFile().exists()) {
									Log.err(GenerateStatistics.class, "Spectra file does not exist for %s.", input);
									return null;
								}

								Map<String, List<ChangeWrapper>> changesMap = input.loadChangesFromFile();
								if (changesMap == null) {
									Log.err(GenerateStatistics.class, "Could not load changes for %s.", input);
									return null;
								}
								Log.out(this, "%s: changes count -> %d", input, changesMap.size());

								ISpectra<SourceCodeBlock> spectra = SpectraUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, spectraFile);

								int changeCount = 0;
								int deleteCount = 0;
								int insertCount = 0;
								int unknownCount = 0;

								int changesCount = 0;
								for (INode<SourceCodeBlock> node : spectra.getNodes()) {
									List<ChangeWrapper> changes = getModifications(node.getIdentifier(), changesMap);
									if (!changes.isEmpty()) {
										++changesCount;
									}
									boolean isChange = false;
									boolean isInsert = false;
									boolean isDelete = false;
									boolean isUnknown = false;
									for (ChangeWrapper change : changes) {
										switch (change.getModificationType()) {
										case CHANGE:
											isChange = true;
											break;
										case DELETE:
											isDelete = true;
											break;
										case INSERT:
											isInsert = true;
											break;
										case NO_SEMANTIC_CHANGE:
											isUnknown = true;
											break;
										default:
											break;
										}
									}
									if (isChange) {
										++changeCount;
									}
									if (isInsert) {
										++insertCount;
									}
									if (isDelete) {
										++deleteCount;
									}
									if (isUnknown) {
										++unknownCount;
									}
								}

								Log.out(this, "%s: changed nodes count -> %d", input, changesCount);

								String[] objectArray = new String[10];

								int i = 0;
								objectArray[i++] = bug.getUniqueIdentifier().replace(';','_');

								objectArray[i++] = String.valueOf(spectraFile.toFile().length() / 1024);

								objectArray[i++] = String.valueOf(spectra.getNodes().size());
								objectArray[i++] = String.valueOf(spectra.getTraces().size());
								objectArray[i++] = String.valueOf(spectra.getSuccessfulTraces().size());
								objectArray[i++] = String.valueOf(spectra.getFailingTraces().size());

								objectArray[i++] = String.valueOf(changeCount);
								objectArray[i++] = String.valueOf(deleteCount);
								objectArray[i++] = String.valueOf(insertCount);
								objectArray[i++] = String.valueOf(unknownCount);

								producer.produce(objectArray);



								Path spectraFileFiltered = bug.getWorkDataDir()
										.resolve(BugLoRDConstants.DIR_NAME_RANKING)
										.resolve(BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME);
								if (!spectraFileFiltered.toFile().exists()) {
									Log.warn(GenerateStatistics.class, "Filtered spectra file does not exist for %s.", input);
									spectra = new FilterSpectraModule<SourceCodeBlock>().submit(spectra).getResult();
									spectraFileFiltered = spectraFile;
								} else {
									spectra = SpectraUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, spectraFileFiltered);
								}

								changeCount = 0;
								deleteCount = 0;
								insertCount = 0;
								unknownCount = 0;

								for (INode<SourceCodeBlock> node : spectra.getNodes()) {
									List<ChangeWrapper> changes = getModifications(node.getIdentifier(), changesMap);
									boolean isChange = false;
									boolean isInsert = false;
									boolean isDelete = false;
									boolean isUnknown = false;
									for (ChangeWrapper change : changes) {
										switch (change.getModificationType()) {
										case CHANGE:
											isChange = true;
											break;
										case DELETE:
											isDelete = true;
											break;
										case INSERT:
											isInsert = true;
											break;
										case NO_SEMANTIC_CHANGE:
											isUnknown = true;
											break;
										default:
											break;
										}
									}
									if (isChange) {
										++changeCount;
									}
									if (isInsert) {
										++insertCount;
									}
									if (isDelete) {
										++deleteCount;
									}
									if (isUnknown) {
										++unknownCount;
									}
								}

								objectArray = new String[10];

								i = 0;
								objectArray[i++] = bug.getUniqueIdentifier().replace(';','_') + "_filtered";

								objectArray[i++] = String.valueOf(spectraFileFiltered.toFile().length() / 1024);

								objectArray[i++] = String.valueOf(spectra.getNodes().size());
								objectArray[i++] = String.valueOf(spectra.getTraces().size());
								objectArray[i++] = String.valueOf(spectra.getSuccessfulTraces().size());
								objectArray[i++] = String.valueOf(spectra.getFailingTraces().size());

								objectArray[i++] = String.valueOf(changeCount);
								objectArray[i++] = String.valueOf(deleteCount);
								objectArray[i++] = String.valueOf(insertCount);
								objectArray[i++] = String.valueOf(unknownCount);

								return objectArray;
							}
						}),
				new AbstractProcessor<String[], List<String>>() {
					Map<String, String> map = new HashMap<>();
					@Override
					public List<String> processItem(String[] item) {
						map.put(item[0], CSVUtils.toCsvLine(item));
						return null;
					}
					@Override
					public List<String> getResultFromCollectedItems() {
						String[] titleArray = { "identifier", "file size (kb)", "#nodes", "#tests", "#succ. tests", "#fail. tests", "#changes", "#deletes", "#inserts", "#unknown" };
						map.put("", CSVUtils.toCsvLine(titleArray));
						return Misc.sortByKeyToValueList(map);
					}
				},
				new StringListToFileWriter<List<String>>(output, true)
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

		Log.out(GenerateStatistics.class, "All done!");

	}
	
	
	public static List<ChangeWrapper> getModifications(SourceCodeBlock block, Map<String, List<ChangeWrapper>> changesMap) {
		List<ChangeWrapper> list = Collections.emptyList();
		//see if the respective file was changed
		if (changesMap.containsKey(block.getClassName())) {
			List<ChangeWrapper> changes = changesMap.get(block.getClassName());
			for (ChangeWrapper change : changes) {
				//is the ranked block part of a changed statement?
				if (block.getEndLineNumber() >= change.getStart() && block.getStartLineNumber() <= change.getEnd()) {
					if (list.isEmpty()) {
						list = new ArrayList<>(1);
					}
					list.add(change);
				}
			}
		}
		
		return list;
	}

}
