package se.de.hu_berlin.informatik.benchmark.modification;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.changechecker.ChangeCheckerUtils;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.files.processors.ListToFileWriter;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public abstract class Modification implements Serializable, Comparable<Modification> {

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Modification)) return false;
        return compareTo((Modification) o) == 0;
    }

    public enum Type {
		CHANGE, DELETE, INSERT
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 660633009597273711L;
	
	public static final String SEPARATION_CHAR = ":";
	public static final String PATH_MARK = "#";

	private int[] possibleLines;
	private String classPath;

	public Modification(int[] possibleLines, String classPath) {
		super();
		checkLinesArray(possibleLines, classPath);
		this.possibleLines = possibleLines;
		Arrays.sort(possibleLines);
		this.classPath = classPath;
	}

	private void checkLinesArray(int[] possibleLines, String classPath) {
		Objects.requireNonNull(possibleLines, "Possible lines array in modification is null! -> " + classPath);
		if (possibleLines.length == 0) {
			throw new IllegalStateException("Possible lines array in modification is empty! -> " + classPath);
		}
	}
	
	public Modification(int line, String classPath) {
		super();
		this.possibleLines = new int[] { line };
		this.classPath = classPath;
	}

	public int[] getPossibleLines() {
		return possibleLines;
	}

	public void setPossibleLines(int[] possibleLines) {
		checkLinesArray(possibleLines, classPath);
		this.possibleLines = possibleLines;
		Arrays.sort(possibleLines);
	}

	public String getClassPath() {
		return classPath;
	}

	public void setClassPath(String classPath) {
		this.classPath = classPath;
	}
	
	@Override
	public String toString() {
		StringBuilder lines = new StringBuilder();
		for (int i = 0; i < possibleLines.length; i++) {
			if (i == possibleLines.length - 1) {
				lines.append(possibleLines[i]);
			} else {
				lines.append(possibleLines[i]).append("+");
			}
		}
		return classPath + ":" + lines;
	}
	
	@Override
	public int compareTo(Modification o) {
		if (o.getClassPath().equals(this.getClassPath())) {
			return Integer.compare(this.getPossibleLines()[0], o.getPossibleLines()[0]);
		} else {
			return this.getClassPath().compareTo(o.getClassPath());
		}
	}
	
	
	public static boolean storeChanges(Map<String, List<Modification>> changesMap, Path changesFile) {
		FileUtils.ensureParentDir(changesFile.toFile());
		FileUtils.delete(changesFile);
		try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(
				new FileOutputStream(changesFile.toFile()))) {
			objectOutputStream.writeObject(changesMap);
			return true;
		} catch (FileNotFoundException e) {
			Log.err(Modification.class, e, "File '%s' could not be accessed.", changesFile);
			return false;
		} catch (IOException e) {
			Log.err(Modification.class, e, "Could not write to file '%s'.", changesFile);
			return false;
		}
	}

	public static void storeChangesHumanReadable(Map<String, List<Modification>> changesMap, Path changesFile) {
		FileUtils.ensureParentDir(changesFile.toFile());
		// iterate over all modified source files
		List<String> result = new ArrayList<>();
		for (Entry<String, List<Modification>> changes : changesMap.entrySet()) {
			// add the name of the modified class
			result.add(PATH_MARK + changes.getKey());
			// add the changes
			for (Modification change : changes.getValue()) {
				result.add(change.toString());
			}
		}

		// save the gathered information about modified lines in a file
		new ListToFileWriter<List<String>>(changesFile, true).submit(result);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, List<Modification>> readChangesFromFile(Path changesFile) {
		Map<String, List<Modification>> changeMap = null;
		try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(changesFile.toFile()))) {
			Object obj = objectInputStream.readObject();
			if (obj instanceof Map) {
				changeMap = (Map<String, List<Modification>>) obj;
			}
		} catch (FileNotFoundException e) {
			Log.err(Modification.class, e, "File '%s' could not be found.", changesFile);
		} catch (IOException e) {
			Log.err(Modification.class, e, "Could not read file '%s'.", changesFile);
		} catch (ClassNotFoundException e) {
			Log.err(Modification.class, e, "Could not find class of object in file '%s'.", changesFile);
		}

		return changeMap;
	}
	
	public static Set<Integer> getAllPossiblyModifiedLines(List<Modification> modifications) {
		Set<Integer> result = new HashSet<>();
		for (Modification change : modifications) {
			for (int changedLine : change.getPossibleLines()) {
				result.add(changedLine);
			}
		}
		return result;
	}
	
	public Modification.Type getModificationType() {
		if (this instanceof Change) {
			return Type.CHANGE;
		} else if (this instanceof Delete) {
			return Type.DELETE;
		} else if (this instanceof Insert) {
			return Type.INSERT;
		}
		return null;
	}
	
	public boolean isOfType(Modification.Type type) {
		return isOfType(this, type);
	}
	
	public static boolean isOfType(Modification modification, Modification.Type type) {
		boolean rightType = false;
		switch (type) {
		case CHANGE:
			if (modification instanceof Change) {
				rightType = true;
			}
			break;
		case DELETE:
			if (modification instanceof Delete) {
				rightType = true;
			}
			break;
		case INSERT:
			if (modification instanceof Insert) {
				rightType = true;
			}
			break;
		default:
			break;
		}
		return rightType;
	}

	public static boolean doesModificationTypeExistInList(List<Modification> list, Modification.Type type) {
		boolean modificationExists = false;
		for (Modification modification : list) {
			switch (type) {
			case CHANGE:
				if (modification instanceof Change) {
					modificationExists = true;
				}
				break;
			case DELETE:
				if (modification instanceof Delete) {
					modificationExists = true;
				}
				break;
			case INSERT:
				if (modification instanceof Insert) {
					modificationExists = true;
				}
				break;
			default:
				break;
			}
			
			if (modificationExists) {
				break;
			}
		}
		return modificationExists;
	}
	
	public static Type getMostImportantType(List<Modification> changes) {
		EnumSet<Type> types = getModificationTypes(changes);
		if (types.contains(Type.CHANGE)) {
			return Type.CHANGE;
		} else if (types.contains(Type.DELETE)) {
			return Type.DELETE;
		} else if (types.contains(Type.INSERT)) {
			return Type.INSERT;
		} else {
			return null;
		}
	}
	
	public static EnumSet<Type> getModificationTypes(List<Modification> changes) {
		EnumSet<Type> set = EnumSet.noneOf(Type.class);
		for (Modification change : changes) {
			if (change.getModificationType() == Type.INSERT) {
				set.add(Type.INSERT);
				break;
			}
		}
		for (Modification change : changes) {
			if (change.getModificationType() == Type.CHANGE) {
				set.add(Type.CHANGE);
				break;
			}
		}
		for (Modification change : changes) {
			if (change.getModificationType() == Type.DELETE) {
				set.add(Type.DELETE);
				break;
			}
		}
		return set;
	}
	
	public static List<Modification> convertChangeWrappersToModifications(String classPath,
			List<ChangeWrapper> changes) {
		if (changes == null) {
			return null;
		}
		List<Modification> modifications = new ArrayList<>();
		ChangeCheckerUtils.removeChangesWithNoDeltaLines(changes);
		if (!changes.isEmpty()) {
			for (ChangeWrapper changeWrapper : changes) {
				List<Integer> deltas = changeWrapper.getIncludedDeltas();
				if (deltas == null || deltas.isEmpty()) {
					continue;
				}
				int[] possibleLines = new int[deltas.size()];
				int i = 0;
				for (int line : deltas) {
					possibleLines[i] = line;
					++i;
				}
				switch (changeWrapper.getModificationType()) {
				case CHANGE:
					modifications.add(new Change(possibleLines, classPath));
					break;
				case DELETE:
					modifications.add(new Delete(possibleLines, classPath));
					break;
				case INSERT:
					modifications.add(new Insert(possibleLines, classPath));
					break;
				case NO_CHANGE:
					break;
				case NO_SEMANTIC_CHANGE:
					break;
				default:
					break;
				}
			}
		}
		return modifications;
	}
	
	public static Map<String, List<Modification>> convertChangeWrappersToModifications(String classPath,
			Map<String, List<ChangeWrapper>> changes) {
		if (changes == null) {
			return null;
		}
		Map<String, List<Modification>> modifications = new HashMap<>();
		for (Entry<String, List<ChangeWrapper>> entry : changes.entrySet()) {
			List<Modification> mod = Modification.convertChangeWrappersToModifications(entry.getKey(), entry.getValue());
			if (mod != null) {
				modifications.put(entry.getKey(), mod);
			}
		}
		return modifications;
	}
	
	
	/**
	 * Returns the list of changes relevant to the given source code range.
	 * @param filePath
	 * the file under consideration
	 * @param start
	 * the beginning line
	 * @param end
	 * the ending line
	 * @param ignoreRefactorings
	 * whether to ignore changes that are refactorings
	 * @param changesMap
	 * the map of all existing changes
	 * @param ignoreList
	 * modifications that have already been seen/used and should not be returned again
	 * @return
	 * list of changes relevant to the given range; {@code null} if no changes match
	 */
	public static List<Modification> getModifications(String filePath, int start, int end, 
			boolean ignoreRefactorings, Map<String, List<Modification>> changesMap, List<Modification> ignoreList) {
		//see if the respective file was changed
		List<Modification> changes = changesMap.get(filePath);
		List<Modification> list = null;
		if (changes != null) {
            for (Modification change : changes) {
                //is the ranked block part of a changed statement?
                for (int deltaLine : change.getPossibleLines()) {
                    if (start <= deltaLine && deltaLine <= end) {
                        if (ignoreList == null || !ignoreList.contains(change)) {
                            if (list == null) {
                                list = new ArrayList<>(1);
                            }
                            list.add(change);
                            if (ignoreList != null) {
                                ignoreList.add(change);
                            }
                        }
                        break;
                    }
                }
            }
		}
		return list;
	}
	
	/**
	 * Returns the list of changes relevant to the given source code range.
	 * @param start
	 * the beginning line
	 * @param end
	 * the ending line
	 * @param ignoreRefactorings
	 * whether to ignore changes that are refactorings
	 * @param changes
	 * a list of changes
	 * @return
	 * list of changes relevant to the given block; {@code null} if no changes match
	 */
	public static List<Modification> getModifications(int start, int end, 
			boolean ignoreRefactorings, List<Modification> changes) {
		List<Modification> list = null;
		if (changes != null) {
			for (Modification change : changes) {
				//is the ranked block part of a changed statement?
				for (int deltaLine : change.getPossibleLines()) {
					if (start <= deltaLine && deltaLine <= end) {
						if (list == null) {
							list = new ArrayList<>(1);
						}
						list.add(change);
						
						break;
					}
				}
			}
		}
		return list;
	}
}
