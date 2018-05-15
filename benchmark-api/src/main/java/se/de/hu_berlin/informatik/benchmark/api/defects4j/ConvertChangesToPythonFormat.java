/**
 * 
 */
package se.de.hu_berlin.informatik.benchmark.api.defects4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import se.de.hu_berlin.informatik.benchmark.modification.Modification;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class ConvertChangesToPythonFormat {
	
	/**
	 * @param args
	 * command line arguments
	 * @throws IOException 
	 * in case of not being able to read or write from/to disk
	 */
	public static void main(String[] args) throws IOException {
		
		Path mainPath = Paths.get("d4j-faults");
		
		Path outputMainPath = mainPath.resolve("python-changefiles");
		
		//iterate over all projects
		for (String project : Defects4JBase.getAllProjects()) {
			Path outputProjectPath = outputMainPath.resolve(project);
			outputProjectPath.toFile().mkdirs();
			
			String[] ids = Defects4JBase.getAllBugIDs(project); 
			for (String id : ids) {
				Map<String, List<Modification>> changes = Defects4JBuggyFixedEntity.getModificationsFromXmlFile(project, id);
				if (changes == null) {
					continue;
				}
				
				generatePythonScriptChangeFiles(changes, outputProjectPath, project, id);
				
			}
		}
		
		Log.out(ConvertChangesToPythonFormat.class, "All done!");
		
	}
	
	private static void generatePythonScriptChangeFiles(Map<String, List<Modification>> changes, Path outputProjectPath, String project, String id) throws IOException {
		String content = generatePythonChangeFileContent(changes);
		
		Path outputFilePath = outputProjectPath.resolve(project + "-" + id + "b.chng");
		
		OutputStream out = Files.newOutputStream(outputFilePath);
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
			writer.append(content);
		}
		out.close();
	}
	
	
	private static String generatePythonChangeFileContent(Map<String, List<Modification>> map) {
		StringBuilder builder = new StringBuilder();

		for (Entry<String, List<Modification>> entry : map.entrySet()) {

			String filePath = entry.getKey();

			boolean worked = buildChangeStringPart(builder, filePath , entry.getValue(), Modification.Type.CHANGE);
			if (worked) {
				builder.append(":CHANGE").append(System.lineSeparator());
			}

			worked = buildChangeStringPart(builder, filePath, entry.getValue(), Modification.Type.DELETE);
			if (worked) {
				builder.append(":DELETE").append(System.lineSeparator());
			}

			worked = buildChangeStringPart(builder, filePath, entry.getValue(), Modification.Type.INSERT);
			if (worked) {
				builder.append(":INSERT").append(System.lineSeparator());
			}
		}

		return builder.toString();
	}
	
	private static boolean buildChangeStringPart(StringBuilder builder, final String filePath,
			List<Modification> list, Modification.Type type) {
		boolean modificationExists = Modification.doesModificationTypeExistInList(list, type);
		if (!modificationExists) {
			return false;
		}
		
		builder.append(filePath).append(':');
		boolean first = true;
		for (Modification modification : list) {
			boolean rightType = modification.isOfType(type);
			if (rightType) {
				if (first) {
					first = false;
				} else {
					builder.append(',');
				}
				boolean innerFirst = true;
				for (int element : modification.getPossibleLines()) {
					if (innerFirst) {
						innerFirst = false;
					} else {
						builder.append("+");
					}
					builder.append(element);
				}
			}
		}
		return true;
	}

	
	
	
	
}
