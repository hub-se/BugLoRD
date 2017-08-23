package se.de.hu_berlin.informatik.benchmark.api.defects4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import se.de.hu_berlin.informatik.benchmark.api.AbstractBuggyFixedEntity;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class Defects4JBuggyFixedEntity extends AbstractBuggyFixedEntity<Defects4JEntity> {
		
	final private String project;
	final private String bugID;
	
	public static final String SEPARATOR_CHAR = ":";
	
	public Defects4JBuggyFixedEntity(String project, String bugId) {
		super(Defects4JEntity.getBuggyDefects4JEntity(project, bugId), 
				Defects4JEntity.getFixedDefects4JEntity(project, bugId));
		this.project = project;
		this.bugID = bugId;
	}
	
	@Override
	public String getUniqueIdentifier() {
		return project + SEPARATOR_CHAR + bugID;
	}

	@Override
	public List<String> getModifiedClasses(boolean executionMode) {
		return getModifiedClassesFromInfo(executionMode);
	}
	
	/**
	 * Returns a String which contains all modified source files with one file per line.
	 * <p> line format: {@code qualified.class.name}
	 * <p> example: {@code com.google.javascript.jscomp.FlowSensitiveInlineVariables}
	 * @param executionMode
	 * whether the execution directory should be used to make the necessary system call
	 */
	private List<String> getModifiedClassesFromInfo(boolean executionMode) {
		/* #====================================================================================
		 * # collect bug info
		 * #==================================================================================== */
		String infoOutput = getBuggyVersion().getInfo(executionMode);
		
		return parseInfoStringForModifiedClasses(infoOutput);
	}
	
	/**
	 * Parses a Defects4J info string and returns a String which contains all modified
	 * source files with one file per line.
	 * <p> line format: {@code qualified.class.name}
	 * <p> example: {@code com.google.javascript.jscomp.FlowSensitiveInlineVariables}
	 * @param info
	 * the info string
	 * @return
	 * modified source files, separated by new lines
	 */
	private List<String> parseInfoStringForModifiedClasses(String info) {
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new StringReader(info))) {
			String line = null;
			boolean modifiedSourceLine = false;
			while ((line = reader.readLine()) != null) {
				if (line.equals("List of modified sources:")) {
					modifiedSourceLine = true;
					continue;
				}
				if (modifiedSourceLine && line.startsWith(" - ")) {
					lines.add(line.substring(3));
				} else {
					modifiedSourceLine = false;
				}
			}
		} catch (IOException e) {
			Log.abort(this, "IOException while reading info string.");
		}
		
		return lines;
	}

}
