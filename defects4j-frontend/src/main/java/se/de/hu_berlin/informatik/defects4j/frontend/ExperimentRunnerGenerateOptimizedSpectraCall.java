/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.io.Files;

import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.util.SpectraUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithPaths;

/**
 * {@link Callable} object that runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ExperimentRunnerGenerateOptimizedSpectraCall extends CallableWithPaths<String, Boolean> {

	private final String project;
	private final static String SEP = File.separator;
	
	public static Charset[] charsets = { 
			StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1, 
			StandardCharsets.US_ASCII, StandardCharsets.UTF_16,
			StandardCharsets.UTF_16BE, StandardCharsets.UTF_16LE};
	
	/**
	 * Initializes a {@link ExperimentRunnerGenerateOptimizedSpectraCall} object with the given parameters.
	 * @param project
	 * the id of the project under consideration
	 */
	public ExperimentRunnerGenerateOptimizedSpectraCall(String project) {
		super();
		this.project = project;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() {
		String id = getInput();
		
		if (!Prop.validateProjectAndBugID(project, Integer.parseInt(id), false)) {
			Log.err(this, "Combination of project '" + project + "' and bug '" + id + "' "
					+ "is not valid. Skipping...");
			return false;
		}
		String buggyID = id + "b";
		String fixedID = id + "f";
		
		//this is important!!
		Prop prop = new Prop().loadProperties(project, buggyID, fixedID);
		
//		//make sure that the current experiment hasn't been run yet
//		Path progressFile = Paths.get(prop.progressFile);
//		try {
//			String progress = Misc.readFile2String(progressFile);
//			if (progress.contains(project + id)) {
//				//experiment in progress or finished
//				return true;
//			} else {
//				//new experiment -> make a new entry in the file
//				Misc.appendString2File(project + id, progressFile.toFile());
//			}
//		} catch (IOException e) {
//			//error while reading or writing file
//			Misc.err(this, "Could not read from or write to '%s'.", progressFile);
//		}
		
		File archiveBuggyWorkDir = Paths.get(prop.archiveBuggyWorkDir).toFile();
		
		String buggyMainSrcDir = prop.executeCommandWithOutput(archiveBuggyWorkDir, false, 
				prop.defects4jExecutable, "export", "-p", "dir.src.classes");
		Log.out(this, "main source directory: <" + buggyMainSrcDir + ">");
		
		String rankingDir = prop.archiveBuggyWorkDir + SEP + "ranking";
		
		ISpectra<String> spectra = SpectraUtils.loadSpectraFromZipFile(Paths.get(rankingDir, "spectraCompressed.zip"));
		
		Map<String, Set<Integer>> map = new HashMap<>();
		
		List<String> nodesToBeRemoved = new ArrayList<>();
		//iterate over all nodes in the spectra
		for (INode<String> node : spectra.getNodes()) {
			String[] nodeParts = node.getIdentifier().split(":");
			assert nodeParts.length == 2;
			
			if (map.containsKey(nodeParts[0])) {
				map.get(nodeParts[0]).add(Integer.parseInt(nodeParts[1]));
			} else {
				map.put(nodeParts[0], new HashSet<Integer>());
				map.get(nodeParts[0]).add(Integer.parseInt(nodeParts[1]));
			}
		}
		
		//iterate over all file paths
		for (Entry<String, Set<Integer>> entry : map.entrySet()) {
			String path = entry.getKey();
			List<String> lines = getLinesFromFile(
					Paths.get(prop.archiveBuggyWorkDir, buggyMainSrcDir, path));
			
			if (lines != null) {
				//iterate over all line numbers
				for (Integer lineNo : entry.getValue()) {
					//if the line only contains whitespace characters and closing brackets, 
					//then remove the respecting node...
					if (lines.get(lineNo-1).matches("[\\s}{]*")) {
						nodesToBeRemoved.add(path + ":" + lineNo);
//						Log.out(this, path + ":" + lineNo + " -> " + lines.get(lineNo-1) + ".");
					}
				}
			} else {
				Log.err(this, "Could not process '%s'.", path);
			}
		}
		
		for (String identifier : nodesToBeRemoved) {
			spectra.removeNode(identifier);
		}
		
		SpectraUtils.saveSpectraToZipFile(spectra, Paths.get(rankingDir, "spectraCompressed_opt.zip"), true);
		
		return true;
	}

	private List<String> getLinesFromFile(Path file) {
		//try opening the file with different charsets
		for (Charset charset : charsets) {
			try {
				return Files.readLines(file.toFile(), charset);
			} catch (IOException x) {
				//try next charset
			}
		}
		Log.err(this, "unknown charset!");
		return null;
	}
	
}



