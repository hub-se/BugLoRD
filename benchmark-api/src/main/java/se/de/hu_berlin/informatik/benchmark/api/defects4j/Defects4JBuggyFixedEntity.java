package se.de.hu_berlin.informatik.benchmark.api.defects4j;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import se.de.hu_berlin.informatik.benchmark.api.AbstractBuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JBase.Defects4JProject;
import se.de.hu_berlin.informatik.benchmark.modification.Change;
import se.de.hu_berlin.informatik.benchmark.modification.Delete;
import se.de.hu_berlin.informatik.benchmark.modification.Insert;
import se.de.hu_berlin.informatik.benchmark.modification.Modification;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.*;

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
    
    public Defects4JBuggyFixedEntity(Defects4JProject project, String bugId) {
        super(Defects4JEntity.getBuggyDefects4JEntity(project.getId(), bugId),
                Defects4JEntity.getFixedDefects4JEntity(project.getId(), bugId));
        this.project = project.getId();
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
     *
     * @param executionMode whether the execution directory should be used to make the necessary system call
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
     *
     * @param info the info string
     * @return modified source files, separated by new lines
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

    @Override
    public Map<String, List<Modification>> loadChangesFromFile() {
        // just load the changes from the XML files in the jar
        return getAllChanges(true, false, false, true, false, false);
    }

    @Override
    public Map<String, List<Modification>> getAllChanges(boolean executionModeBug, boolean resetBug,
                                                         boolean deleteBugAfterwards, boolean executionModeFix, 
                                                         boolean resetFix, boolean deleteFixAfterwards) {
        if (changesMap == null) {
            changesMap = getModificationsFromXmlFile(project, bugID);
        }
        return changesMap;
    }

    public static Map<String, List<Modification>> getModificationsFromXmlFile(String d4jProject, String id) throws NullPointerException {
        InputStream xmlPath = getBugXmlFileAsInputStream(d4jProject, id);
        if (xmlPath == null) {
            return null;
        }

        Document doc = null;
        try {
            doc = new SAXBuilder().build(xmlPath);
        } catch (JDOMException e) {
            Log.err(ConvertChangesToPythonFormat.class, e, "JDOMException in xml file '%s'.", xmlPath);
            return null;
        } catch (IOException e) {
            Log.err(ConvertChangesToPythonFormat.class, e, "Could not parse xml file '%s'.", xmlPath);
            return null;
        }

        Element project = doc.getRootElement().getChild("project");
        Objects.requireNonNull(project, "Project item not found.");

        String projectID = project.getAttributeValue("projectid");
        Objects.requireNonNull(projectID, "Project ID not found.");

        Element bug = project.getChild("bug");
        Objects.requireNonNull(bug, "bug item not found.");

//		String bugID = project.getAttributeValue("bugid");
//		Objects.requireNonNull(projectID, "Bug ID not found.");

        Element fixLocations = bug.getChild("fixlocations");
        Objects.requireNonNull(fixLocations, "Fix locations item not found.");

        Map<String, List<Modification>> map = new HashMap<>();

        // loop over all files with changes
        for (final Object fileObj : fixLocations.getChildren()) {
            final Element file = (Element) fileObj;
            final String filePath = file.getAttributeValue("path");
            Objects.requireNonNull(filePath, "Path attribute not found in file item.");

            List<List<Integer>> changesList = new ArrayList<>();
            List<List<Integer>> deletesList = new ArrayList<>();
            List<List<Integer>> insertsList = new ArrayList<>();
            // loop over all changed lines and the "bugtypes" item
            for (final Object elementObj : file.getChildren()) {
                final Element element = (Element) elementObj;

                String name = element.getName();

                switch (name) {
                    case "change":
                        // can be numbers, divided by ',' and ':', and may also contain ranges marked by '-'
                        // ranges have to be unfolded, since they have other meaning than in the case of an insert
                        List<Integer> changeContent = unfoldRanges(element.getText());
                        parseInsertElementContents(changesList, Misc.listToString(changeContent, ",", "", ""));
                        break;
                    case "delete":
                        // can be numbers, divided by ',' and ':', and may also contain ranges marked by '-'
                        // ranges have to be unfolded, since they have other meaning than in the case of an insert
                        List<Integer> deleteContent = unfoldRanges(element.getText());
                        parseInsertElementContents(deletesList, Misc.listToString(deleteContent, ",", "", ""));
                        break;
                    case "insert":
                        // can be numbers, divided by ',' and ':', and may also contain ranges marked by '-' 
                        String insertContent = element.getText();
                        parseInsertElementContents(insertsList, insertContent);
                        break;
                    case "bugtypes":
                        break;
                    default:
                        Objects.requireNonNull(null, "Unknown change item.");
                }
            }

            List<Modification> modifications = new ArrayList<>();

            for (List<Integer> possibleLines : changesList) {
                if (possibleLines.isEmpty()) {
                    continue;
                } else if (possibleLines.size() == 1) {
                    modifications.add(new Change(possibleLines.get(0), filePath));
                } else {
                    int[] array = new int[possibleLines.size()];
                    int i = 0;
                    for (int line : possibleLines) {
                        array[i] = line;
                        ++i;
                    }
                    modifications.add(new Change(array, filePath));
                }
            }

            for (List<Integer> possibleLines : deletesList) {
                if (possibleLines.isEmpty()) {
                    continue;
                } else if (possibleLines.size() == 1) {
                    modifications.add(new Delete(possibleLines.get(0), filePath));
                } else {
                    int[] array = new int[possibleLines.size()];
                    int i = 0;
                    for (int line : possibleLines) {
                        array[i] = line;
                        ++i;
                    }
                    modifications.add(new Delete(array, filePath));
                }
            }

            for (List<Integer> possibleLines : insertsList) {
                if (possibleLines.isEmpty()) {
                    continue;
                } else if (possibleLines.size() == 1) {
                    modifications.add(new Insert(possibleLines.get(0), filePath));
                } else {
                    int[] array = new int[possibleLines.size()];
                    int i = 0;
                    for (int line : possibleLines) {
                        array[i] = line;
                        ++i;
                    }
                    modifications.add(new Insert(array, filePath));
                }
            }

            map.put(filePath, modifications);
        }

        return map;
    }

    private static InputStream getBugXmlFileAsInputStream(String project, String id) {
        String bugFile = "/d4j-faults/" + project + "/bugdiagnosis_" + project + "-" + id + ".xml";
        InputStream xmlPath = Defects4JBuggyFixedEntity.class.getResourceAsStream(bugFile);
        if (xmlPath == null) {
            Log.out(Defects4JBuggyFixedEntity.class, "%s does not exist!", bugFile);
        }
        return xmlPath;
    }

    private static List<Integer> unfoldRanges(String changeContent) {
        if (changeContent.contains(":")) {
            Objects.requireNonNull(null, "Content may not contain ':'.");
        }
        List<Integer> list = new ArrayList<>();
        String[] numbers1 = changeContent.split(",");
        for (String number1 : numbers1) {
            String[] numbers2 = number1.split("-");
            if (numbers2.length == 1) {
                // empty?
                if (numbers2[0].isEmpty()) {
                    // marks the whole class...?!
                    list.add(-1);
                }
                // single number
                Integer number = Integer.valueOf(numbers2[0]);
                list.add(number);
            } else if (numbers2.length == 2) {
                // range
                Integer numberStart = Integer.valueOf(numbers2[0]);
                Integer numberEnd = Integer.valueOf(numbers2[1]);
                if (numberEnd < numberStart) {
                    Objects.requireNonNull(null, "Ending number greater than starting number in range.");
                }
                for (int i = numberStart; i <= numberEnd; ++i) {
                    list.add(i);
                }
            } else {
                Objects.requireNonNull(null, "Number format wrong.");
            }
        }
        return list;
    }

//	private static void parseElementContents(List<Integer> list, final String content) {
//		String[] numbers1 = content.split(",");
//		for (String number1 : numbers1) {
//			String[] numbers2 = number1.split(":");
//			for (String number2 : numbers2) {
//				String[] numbers3 = number2.split("-");
//				if (numbers3.length == 1) {
//					// empty?
//					if (numbers3[0].equals("")) {
//						continue;
//					}
//					// single number
//					Integer number = Integer.valueOf(numbers3[0]);
//					list.add(number);
//				} else if (numbers3.length == 2) {
//					// range
//					Integer numberStart = Integer.valueOf(numbers3[0]);
//					Integer numberEnd = Integer.valueOf(numbers3[1]);
//					if (numberEnd < numberStart) {
//						Objects.requireNonNull(null, "Ending number greater than starting number in range.");
//					}
//					for (int i = numberStart; i <= numberEnd; ++i) {
//						list.add(i);
//					}
//				} else {
//					Objects.requireNonNull(null, "Number format wrong.");
//				}
//			}
//		}
//	}

    private static void parseInsertElementContents(List<List<Integer>> list, final String content) {
        String[] numbers1 = content.split(",");
        for (String number1 : numbers1) {
            String[] numbers2 = number1.split(":");
            List<Integer> numList = new ArrayList<>();
            for (String number2 : numbers2) {
                String[] numbers3 = number2.split("-");
                if (numbers3.length == 1) {
                    // empty?
                    if (numbers3[0].isEmpty()) {
                        // marks the whole class...?!
                        numList.add(-1);
                    } else {
                        // single number
                        Integer number = Integer.valueOf(numbers3[0]);
                        numList.add(number);
                    }
                } else if (numbers3.length == 2) {
                    // range
                    Integer numberStart = Integer.valueOf(numbers3[0]);
                    Integer numberEnd = Integer.valueOf(numbers3[1]);
                    if (numberEnd < numberStart) {
                        Objects.requireNonNull(null, "Ending number greater than starting number in range.");
                    }
                    for (int i = numberStart; i <= numberEnd; ++i) {
                        numList.add(i);
                    }
                } else {
                    Objects.requireNonNull(null, "Number format wrong.");
                }
            }
            if (!numList.isEmpty()) {
                list.add(numList);
            }
        }
    }

}
