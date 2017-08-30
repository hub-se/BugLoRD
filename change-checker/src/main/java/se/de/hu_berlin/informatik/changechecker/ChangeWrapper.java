package se.de.hu_berlin.informatik.changechecker;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.SignificanceLevel;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.files.processors.ListToFileWriter;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;

public class ChangeWrapper implements Serializable, Comparable<ChangeWrapper> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2681796538051406576L;

	public static final String SEPARATION_CHAR = ":";
	public static final String PATH_MARK = "#";

	public static enum ModificationType {
		CHANGE("CHANGE"), DELETE("DELETE"), INSERT("INSERT"), 
		NO_SEMANTIC_CHANGE("NOSEMANTICCHANGE"), NO_CHANGE("NONE");

		String arg;

		ModificationType(String arg) {
			this.arg = arg;
		}

		@Override
		public String toString() {
			return arg;
		}
	}

//	private SourceCodeEntity entity;
	
	private final int parentStart;
	private final int parentEnd;
	private final int start;
	private final int end;

	private final EntityType entityType;
	private final ChangeType changeType;
	private final SignificanceLevel significance;
	private final ModificationType modificationType;

	private final String className;

	private List<Integer> includedDeltas;

	public ChangeWrapper(String className, int parentStart, int parentEnd, int start, int end, EntityType entityType,
			ChangeType changeType, SignificanceLevel significanceLevel, ModificationType modification_type) {
		super();
		this.className = className;
		this.parentStart = parentStart;
		this.parentEnd = parentEnd;
		this.start = start;
		this.end = end;
		this.entityType = entityType;
		this.changeType = changeType;
		this.modificationType = modification_type;
		this.significance = significanceLevel;
	}

	public ChangeWrapper(String className, int parentStart, int parentEnd, int start, int end,
			List<Integer> includedDeltas, EntityType entityType, ChangeType changeType,
			SignificanceLevel significanceLevel, ModificationType modification_type) {
		this(className, parentStart, parentEnd, start, end, entityType, changeType, significanceLevel,
				modification_type);
		this.includedDeltas = includedDeltas;
	}
	
//	public SourceCodeEntity getEntity() {
//		return entity;
//	}

	public int getParentStart() {
		return parentStart;
	}

	public int getParentEnd() {
		return parentEnd;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public void setDeltas(List<Integer> includedDeltas) {
		this.includedDeltas = includedDeltas;
	}
	
	public void addDelta(int deltaLine) {
		if (this.includedDeltas == null) {
			this.includedDeltas = new ArrayList<>(1);
		}
		this.includedDeltas.add(deltaLine);
	}

	public List<Integer> getIncludedDeltas() {
		return includedDeltas;
	}

	public EntityType getEntityType() {
		return entityType;
	}

	public ChangeType getChangeType() {
		return changeType;
	}

	public ModificationType getModificationType() {
		return modificationType;
	}

	public SignificanceLevel getSignificance() {
		return significance;
	}

	@Override
	public String toString() {
		return className + SEPARATION_CHAR + "(" + parentStart + "-" + parentEnd + ")" + SEPARATION_CHAR + "(" + start
				+ "-" + end + ")" + SEPARATION_CHAR + getLinesFromDeltas() + SEPARATION_CHAR + entityType
				+ SEPARATION_CHAR + changeType + SEPARATION_CHAR + significance + SEPARATION_CHAR + modificationType;
	}

	private String getLinesFromDeltas() {
		if (includedDeltas != null) {
			return Misc.listToString(includedDeltas, ",", "<", ">");
		} else {
			return "<>";
		}
	}

	public static boolean storeChanges(Map<String, List<ChangeWrapper>> changesMap, Path changesFile) {
		FileUtils.ensureParentDir(changesFile.toFile());
		FileUtils.delete(changesFile);
		try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(
				new FileOutputStream(changesFile.toFile()))) {
			objectOutputStream.writeObject(changesMap);
			return true;
		} catch (FileNotFoundException e) {
			Log.err(ChangeWrapper.class, e, "File '%s' could not be accessed.", changesFile);
			return false;
		} catch (IOException e) {
			Log.err(ChangeWrapper.class, e, "Could not write to file '%s'.", changesFile);
			return false;
		}
	}

	public static void storeChangesHumanReadable(Map<String, List<ChangeWrapper>> changesMap, Path changesFile) {
		FileUtils.ensureParentDir(changesFile.toFile());
		// iterate over all modified source files
		List<String> result = new ArrayList<>();
		for (Entry<String, List<ChangeWrapper>> changes : changesMap.entrySet()) {
			// add the name of the modified class
			result.add(PATH_MARK + changes.getKey());
			// add the changes
			for (ChangeWrapper change : changes.getValue()) {
				result.add(change.toString());
			}
		}

		// save the gathered information about modified lines in a file
		new ListToFileWriter<List<String>>(changesFile, true).submit(result);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, List<ChangeWrapper>> readChangesFromFile(Path changesFile) {
		Map<String, List<ChangeWrapper>> changeMap = null;
		try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(changesFile.toFile()))) {
			Object obj = objectInputStream.readObject();
			if (obj instanceof Map) {
				changeMap = (Map<String, List<ChangeWrapper>>) obj;
			}
		} catch (FileNotFoundException e) {
			Log.err(ChangeWrapper.class, e, "File '%s' could not be found.", changesFile);
		} catch (IOException e) {
			Log.err(ChangeWrapper.class, e, "Could not read file '%s'.", changesFile);
		} catch (ClassNotFoundException e) {
			Log.err(ChangeWrapper.class, e, "Could not find class of object in file '%s'.", changesFile);
		}

		return changeMap;
	}

	public String getClassName() {
		return className;
	}

	@Override
	public int compareTo(ChangeWrapper o) {
		if (o.getClassName().equals(this.getClassName())) {
			return Integer.compare(this.getEnd(), o.getEnd());
		} else {
			return this.getClassName().compareTo(o.getClassName());
		}
	}

}
