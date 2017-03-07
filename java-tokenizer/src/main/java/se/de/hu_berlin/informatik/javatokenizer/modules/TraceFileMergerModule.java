/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.modules;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.tm.AbstractProcessor;

/**
 * Merges all trace files in the submitted directory and returns a list of
 * all lines without repetitions (i.e. all items are unique).
 * 
 * @author Simon Heiden
 * 
 */
public class TraceFileMergerModule extends AbstractProcessor<Path, List<String>> {

	/**
	 * Creates a new {@link TraceFileMergerModule} object.
	 */
	public TraceFileMergerModule() {
		super();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public List<String> processItem(Path inputDir) {
		HashSet<String> set = new HashSet<>();
		for (final File file : inputDir.toFile().listFiles()) {
	        if (!file.isDirectory()) {
	        	if (file.getName().endsWith(".trc")) {
	        		try (BufferedReader reader = Files.newBufferedReader(file.toPath() , StandardCharsets.UTF_8)) {
	        			String line;
	        			while ((line = reader.readLine()) != null) {
	        				set.add(line);
	        			}
	        		} catch (IOException x) {
	        			Log.abort(this, x, "Not able to open/read file %s.", file.toString());
	        		}
	        	}
	        }
	    }
		return new ArrayList<String>(set);
	}

}
