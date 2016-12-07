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
import se.de.hu_berlin.informatik.c2r.modules.FilterSpectraModule;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.util.SpectraUtils;
import se.de.hu_berlin.informatik.utils.fileoperations.ListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.fileoperations.csv.CSVUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturn;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturnFactory;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.AbstractPipe;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.PipeLinker;
import se.de.hu_berlin.informatik.utils.tm.pipes.ThreadedProcessorPipe;

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

										Log.out(GenerateStatistics.class, "Processing file '%s'.", spectraFile);
										ISpectra<SourceCodeBlock> spectra = SpectraUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, spectraFile);

										Map<String, List<ChangeWrapper>> changesMap = input.loadChangesFromFile();
										
										int changeCount = 0;
										int deleteCount = 0;
										int insertCount = 0;
										
										for (INode<SourceCodeBlock> node : spectra.getNodes()) {
											List<ChangeWrapper> changes = getModifications(node.getIdentifier(), changesMap);
											boolean isChange = false;
											boolean isInsert = false;
											boolean isDelete = false;
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
												case UNKNOWN:
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
										}
										
										String[] objectArray = new String[9];

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
										
										manualOutput(objectArray);
										
										
										spectra = new FilterSpectraModule<SourceCodeBlock>().submit(spectra).getResult();
										
										changeCount = 0;
										deleteCount = 0;
										insertCount = 0;
										
										for (INode<SourceCodeBlock> node : spectra.getNodes()) {
											List<ChangeWrapper> changes = getModifications(node.getIdentifier(), changesMap);
											boolean isChange = false;
											boolean isInsert = false;
											boolean isDelete = false;
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
												case UNKNOWN:
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
										}
										
										objectArray = new String[9];

										i = 0;
										objectArray[i++] = bug.getUniqueIdentifier().replace(';','_') + "_filtered";
										
										objectArray[i++] = String.valueOf(spectraFile.toFile().length() / 1024);
										
										objectArray[i++] = String.valueOf(spectra.getNodes().size());
										objectArray[i++] = String.valueOf(spectra.getTraces().size());
										objectArray[i++] = String.valueOf(spectra.getSuccessfulTraces().size());
										objectArray[i++] = String.valueOf(spectra.getFailingTraces().size());
										
										objectArray[i++] = String.valueOf(changeCount);
										objectArray[i++] = String.valueOf(deleteCount);
										objectArray[i++] = String.valueOf(insertCount);
										
										return objectArray;
									}
								};
							}
						}),
				new AbstractPipe<String[], List<String>>(true) {
					Map<String, String> map = new HashMap<>();
					@Override
					public List<String> processItem(String[] item) {
						map.put(item[0], CSVUtils.toCsvLine(item));
						return null;
					}
					@Override
					public List<String> getResultFromCollectedItems() {
						String[] titleArray = { "identifier", "file size (kb)", "#nodes", "#tests", "#succ. tests", "#fail. tests", "#changes", "#deletes", "#inserts" };
						map.put("", CSVUtils.toCsvLine(titleArray));
						return Misc.sortByKeyToValueList(map);
					}
				},
				new ListToFileWriterModule<List<String>>(output, true)
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
