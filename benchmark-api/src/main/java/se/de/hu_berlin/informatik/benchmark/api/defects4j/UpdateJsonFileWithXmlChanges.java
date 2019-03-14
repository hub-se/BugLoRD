package se.de.hu_berlin.informatik.benchmark.api.defects4j;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import se.de.hu_berlin.informatik.benchmark.modification.Modification;
import se.de.hu_berlin.informatik.benchmark.modification.Modification.Type;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class UpdateJsonFileWithXmlChanges {
	
	/**
	 * @param args
	 * command line arguments
	 * @throws IOException 
	 * in case of not being able to read or write from/to disk
	 */
	public static void main(String[] args) throws IOException {
		
		Path mainPath = Paths.get("d4j-faults");
		
		Path outputMainPath = mainPath.resolve("json");
		

		
		FileInputStream is = new FileInputStream(Paths.get("defects4j-bugs.json").toFile());
		String fileString = convertStreamToString(is);
		
//		if (fileString.contains("<")) {
//			Log.out(UpdateJsonFileWithXmlChanges.class, "yeah.");
//		}

		JsonArray allBugs = new JsonParser().parse(fileString).getAsJsonArray();

		for (JsonElement bug : allBugs) {
			String project = bug.getAsJsonObject().get("project").getAsString();
			int bugId = bug.getAsJsonObject().get("bugId").getAsInt();

			Map<String, List<Modification>> changes = Defects4JBuggyFixedEntity.getModificationsFromXmlFile(project, String.valueOf(bugId));
			if (changes == null) {
				Log.out(UpdateJsonFileWithXmlChanges.class, "No changes for " + project + "-" + bugId + ".");
				continue;
			}
			
			JsonObject changedFiles = bug.getAsJsonObject().get("changedFiles").getAsJsonObject();
			for (Map.Entry<String, List<Modification>> stringListEntry : changes.entrySet()) {
				JsonElement element = getJSonElementFromXmlFile(stringListEntry.getKey(), stringListEntry.getValue());
				changedFiles.add(stringListEntry.getKey(), element);
			}
		}
		
		outputMainPath.toFile().mkdirs();
		Path outputFilePath = outputMainPath.resolve("defects4j-bugs.json");
		
		Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().disableHtmlEscaping().create();
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(Files.newOutputStream(outputFilePath), StandardCharsets.UTF_8))) {
        	String string = gson.toJson(allBugs);
//        	if (string.contains("<")) {
//    			Log.out(UpdateJsonFileWithXmlChanges.class, "yeah2.");
//    		}
        	writer.append(string);
        }
        
		Log.out(UpdateJsonFileWithXmlChanges.class, "All done!");
		
	}
	
	private static String convertStreamToString(java.io.InputStream is) {
		try (java.util.Scanner s = new java.util.Scanner(is)) {
			return s.useDelimiter("\\A").hasNext() ? s.next() : "";
		}
	}

	private static JsonElement getJSonElementFromXmlFile(String filePath, List<Modification> list) {
		JsonObject modifications = new JsonObject();
		JsonElement changedLines = getLinesForModificationType(filePath, list, Type.CHANGE);
		if (changedLines != null) {
			modifications.add("changes", changedLines);
		}
		JsonElement deletedLines = getLinesForModificationType(filePath, list, Type.DELETE);
		if (deletedLines != null) {
			modifications.add("deletes", deletedLines);
		}
		JsonElement insertedLines = getLinesForModificationType(filePath, list, Type.INSERT);
		if (insertedLines != null) {
			modifications.add("inserts", insertedLines);
		}
		return modifications;
	}
	
	private static JsonElement getLinesForModificationType(final String filePath,
			List<Modification> list, Modification.Type type) {
		boolean modificationExists = Modification.doesModificationTypeExistInList(list, type);
		if (!modificationExists) {
			return null;
		}

		JsonArray outerArray = new JsonArray();
		for (Modification modification : list) {
			if (modification.isOfType(type)) {
				JsonArray innerArray = new JsonArray();
				for (int element : modification.getPossibleLines()) {
					innerArray.add(element);
				}
				outerArray.add(innerArray);
			}
		}
		return outerArray;
	}
	
	
}
