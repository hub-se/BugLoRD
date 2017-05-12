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
import difflib.Delta;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.files.processors.StringListToFileWriter;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class ChangeWrapper implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2681796538051406576L;

	public static final String SEPARATION_CHAR = ":";
	public static final String PATH_MARK = "#";

	public static enum ModificationType {
		CHANGE("CHANGE"), DELETE("DELETE"), INSERT("INSERT"), NO_SEMANTIC_CHANGE("NOSEMANTICCHANGE");

		String arg;

		ModificationType(String arg) {
			this.arg = arg;
		}

		@Override
		public String toString() {
			return arg;
		}
	}

	private final int start;
	private final int end;

	private final EntityType entityType;
	private final ChangeType changeType;
	private final SignificanceLevel significance;
	private final ModificationType modificationType;

	private final String className;

	private List<Delta<String>> includedDeltas;

	public ChangeWrapper(String className, int start, int end, EntityType entityType, ChangeType changeType,
			SignificanceLevel significanceLevel, ModificationType modification_type) {
		super();
		this.className = className;
		this.start = start;
		this.end = end;
		this.entityType = entityType;
		this.changeType = changeType;
		this.modificationType = modification_type;
		this.significance = significanceLevel;
	}

	public ChangeWrapper(String className, int parentStart, int parentEnd, List<Delta<String>> includedDeltas,
			EntityType entityType, ChangeType changeType, SignificanceLevel significanceLevel,
			ModificationType modification_type) {
		this(className, parentStart, parentEnd, entityType, changeType, significanceLevel, modification_type);
		this.includedDeltas = includedDeltas;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}
	
	public List<Delta<String>> getIncludedDeltas() {
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
		return className + SEPARATION_CHAR + start + SEPARATION_CHAR + end + SEPARATION_CHAR + getLinesFromDeltas() + SEPARATION_CHAR + entityType
				+ SEPARATION_CHAR + changeType + SEPARATION_CHAR + significance + SEPARATION_CHAR + modificationType;
	}

	private String getLinesFromDeltas() {
		StringBuilder sb = new StringBuilder("<");
		if (includedDeltas != null) {
			boolean isFirst = true;
			for (Delta<String> delta : includedDeltas) {
				if (isFirst) {
					isFirst = false;
				} else {
					sb.append(',');
				}
				sb.append(delta.getOriginal().getPosition()+1);
			}
		}
		sb.append(">");
		return sb.toString();
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
		new StringListToFileWriter<List<String>>(changesFile, true).submit(result);
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

}
