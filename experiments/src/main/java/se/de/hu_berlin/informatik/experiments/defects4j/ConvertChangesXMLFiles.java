/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;

public class ConvertChangesXMLFiles {
	
	private final static String[] projects = { "Closure", "Time", "Math", "Lang", "Chart", "Mockito" };

	public static String[] getAllBugIDs(String project) {
		int maxID = getMaxBugID(project);
		String[] result = new String[maxID];
		for (int i = 0; i < maxID; ++i) {
			result[i] = String.valueOf(i + 1);
		}
		return result;
	}

	public static int getMaxBugID(String project) {
		int maxID;
		switch (project) {
		case "Lang":
			maxID = 65;
			break;
		case "Math":
			maxID = 106;
			break;
		case "Chart":
			maxID = 26;
			break;
		case "Time":
			maxID = 27;
			break;
		case "Closure":
			maxID = 133;
			break;
		case "Mockito":
			maxID = 38;
			break;
		default:
			maxID = 0;
			break;
		}
		return maxID;
	}
	
	/**
	 * @param args
	 * command line arguments
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		Path mainPath = Paths.get("d4j-faults");
		
		Path outputMainPath = mainPath.resolve("python-changefiles");
		
		//iterate over all projects
		for (String project : projects) {
			Path projectPath = mainPath.resolve(project);
			if (!projectPath.toFile().exists()) {
				Log.out(ConvertChangesXMLFiles.class, "%s does not exist!", projectPath);
				continue;
			}
			
			Path outputProjectPath = outputMainPath.resolve(project);
			outputProjectPath.toFile().mkdirs();
			
			String[] ids = getAllBugIDs(project); 
			for (String id : ids) {
				Path xmlPath = projectPath.resolve("bugdiagnosis_" + project + "-" + id + ".xml");
				if (!xmlPath.toFile().exists()) {
					Log.out(ConvertChangesXMLFiles.class, "%s does not exist!", projectPath);
					continue;
				}
				
				String changes = parseXmlFile(xmlPath);
				
				Path outputFilePath = outputProjectPath.resolve(project + "-" + id + "b.chng");
				
				OutputStream out = Files.newOutputStream(outputFilePath);
				try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
					writer.append(changes);
				}
				out.close();
			}
		}
		
		Log.out(ConvertChangesXMLFiles.class, "All done!");
		
	}
	
	private static String parseXmlFile(Path xmlPath) throws NullPointerException {
		Document doc = null;
		try {
			doc = new SAXBuilder().build(new FileReader(xmlPath.toFile()));
		} catch (JDOMException e) {
			Log.err(ConvertChangesXMLFiles.class, e, "JDOMException in xml file '%s'.", xmlPath);
			return null;
		} catch (IOException e) {
			Log.err(ConvertChangesXMLFiles.class, e, "Could not parse xml file '%s'.", xmlPath);
			return null;
		}
		
		Element project = doc.getRootElement().getChild("project");
		Objects.requireNonNull(project, "Project item not found.");
		
		String projectID = project.getAttributeValue("projectid");
		Objects.requireNonNull(projectID, "Project ID not found.");
		
		Element bug = project.getChild("bug");
		Objects.requireNonNull(bug, "bug item not found.");
		
		String bugID = project.getAttributeValue("bugid");
		Objects.requireNonNull(projectID, "Bug ID not found.");
		
		Element fixLocations = bug.getChild("fixlocations");
		Objects.requireNonNull(fixLocations, "Fix locations item not found.");
		
		StringBuilder builder = new StringBuilder();
		
		// loop over all files with changes
		for (final Object fileObj : fixLocations.getChildren()) {
			final Element file = (Element) fileObj;
			final String filePath = file.getAttributeValue("path");
			Objects.requireNonNull(filePath, "Path attribute not found in file item.");

			List<Integer> changesList = new ArrayList<>();
			List<Integer> deletesList = new ArrayList<>();
			List<List<Integer>> insertsList = new ArrayList<>();
			// loop over all changed lines and the "bugtypes" item
			for (final Object elementObj : file.getChildren()) {
				final Element element = (Element) elementObj;
				
				String name = element.getName();
				
				switch (name) {
				case "change":
					parseElementContents(changesList, element);
					break;
				case "delete":
					parseElementContents(deletesList, element);
					break;
				case "insert":
					parseInsertElementContents(insertsList, element);
					break;
				case "bugtypes":
					break;
				default:
					Objects.requireNonNull(null, "Unknown change item.");
				}
			}
			
			if (!changesList.isEmpty()) {
				builder.append(filePath).append(":")
				.append(Misc.listToString(changesList, ",", "", ""))
				.append(":CHANGE").append(System.lineSeparator());
			}
			
			if (!deletesList.isEmpty()) {
				builder.append(filePath).append(":")
				.append(Misc.listToString(deletesList, ",", "", ""))
				.append(":DELETE").append(System.lineSeparator());
			}
			
			if (!insertsList.isEmpty()) {
				for (List<Integer> innerList : insertsList) {
					builder.append(filePath).append(":")
					.append(Misc.listToString(innerList, "+", "", ""))
					.append(":INSERT").append(System.lineSeparator());
				}
			}
		}
		
		return builder.toString();
	}

	private static void parseElementContents(List<Integer> list, final Element element) {
		// can be numbers, divided by ',' and ':', and may also contain ranges marked by '-' 
		String content = element.getText();
		String[] numbers1 = content.split(",");
		for (String number1 : numbers1) {
			String[] numbers2 = number1.split(":");
			for (String number2 : numbers2) {
				String[] numbers3 = number2.split("-");
				if (numbers3.length == 1) {
					// empty?
					if (numbers3[0].equals("")) {
						continue;
					}
					// single number
					Integer number = Integer.valueOf(numbers3[0]);
					list.add(number);
				} else if (numbers3.length == 2) {
					// range
					Integer numberStart = Integer.valueOf(numbers3[0]);
					Integer numberEnd = Integer.valueOf(numbers3[1]);
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
		}
	}
	
	private static void parseInsertElementContents(List<List<Integer>> list, final Element element) {
		// can be numbers, divided by ',' and ':', and may also contain ranges marked by '-' 
		String content = element.getText();
		String[] numbers1 = content.split(",");
		for (String number1 : numbers1) {
			String[] numbers2 = number1.split(":");
			List<Integer> numList = new ArrayList<>();
			for (String number2 : numbers2) {
				String[] numbers3 = number2.split("-");
				if (numbers3.length == 1) {
					// empty?
					if (numbers3[0].equals("")) {
						continue;
					}
					// single number
					Integer number = Integer.valueOf(numbers3[0]);
					numList.add(number);
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
			list.add(numList);
		}
	}
	
}
