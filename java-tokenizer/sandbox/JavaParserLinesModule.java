/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.sandbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;

import se.de.hu_berlin.informatik.javatokenizer.modules.TokenizeLinesModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AModule;

/**
 * Parser module that tokenizes a given input file and outputs a 
 * {@link List} of tokenized lines as {@link String}s. Uses the 
 * "com.github.javaparser" framework for parsing.
 * 
 * @author Simon Heiden
 * 
 */
public class JavaParserLinesModule extends AModule<Map<String, List<Integer>>, List<List<String>>> {

	private boolean use_context;
	private boolean startFromMethods; 
	private int order;
	private boolean use_lookahead;
	private String src_path;
	
	/**
	 * Creates a new {@link TokenizeLinesModule} object with the given parameters.
	 * @param src_path 
	 * @param use_context
	 * sets if each sentence should contain a context of previous tokens
	 * @param startFromMethods
	 * sets if the context (if used) should only go back to the start of a method. (Currently, the implementation
	 * only goes back to the last opening curly bracket, which doesn't have to be the start of a method.)
	 * @param order
	 * the n-gram order (only important for the length of the context)
	 * @param use_lookahead
	 * sets if for each line, the next line should also be appended to the sentence
	 */
	public JavaParserLinesModule(String src_path, boolean use_context, boolean startFromMethods, 
			int order, boolean use_lookahead) {
		super(true);
		this.src_path = src_path;
		this.use_context = use_context;
		this.startFromMethods = startFromMethods;
		this.order = order;
		this.use_lookahead = use_lookahead;
	}
	
	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.miscellaneous.ITransmitter#processItem(java.lang.Object)
	 */
	public List<List<String>> processItem(Map<String, List<Integer>> map) {
		return createTokenizedLinesOutput(map, use_context, startFromMethods, order, use_lookahead);
	}

	/**
	 * Tokenizes the specified lines in all files provided in the given {@link HashMap} and writes the
	 * tokenized lines (sentences) and the according file paths with line numbers to the given output files.
	 * @param map
	 * holds source code file paths, each associated with an {@link ArrayList} of line numbers.  
	 * @param use_context
	 * sets if each sentence should contain a context of previous tokens
	 * @param startFromMethods
	 * sets if the context (if used) should only go back to the start of a method. (Currently, the implementation
	 * only goes back to the last opening curly bracket, which doesn't have to be the start of a method.)
	 * @param order
	 * the n-gram order (only important for the length of the context)
	 * @param use_lookahead
	 * sets if for each line, the next line should also be appended to the sentence
	 * @return
	 * a list of list of Strings that contain the contents of two files. The first file
	 * contains paths to Java files with corresponding line numbers. The second file
	 * contains the corresponding lines (maybe with a context, depending on the options).
	 */
	private List<List<String>> createTokenizedLinesOutput(
			final Map<String, List<Integer>> map, 
			final boolean use_context, final boolean startFromMethods, 
			final int order, final boolean use_lookahead) {
		
		List<String> path_line_lines = new ArrayList<>();
		List<String> sentence_lines = new ArrayList<>();
		
		for (Entry<String, List<Integer>> i : map.entrySet()) {
			List<String> new_lines = createTokenizedLinesOutput(Paths.get(src_path + File.separator + i.getKey()), i.getValue(), use_context, startFromMethods, order, use_lookahead);
			if (new_lines != null ) {
				for (int j = 0; j < new_lines.size(); ++j) {
					path_line_lines.add(i.getKey() + ":" + i.getValue().get(j));
					sentence_lines.add(new_lines.get(j));
				}
			}
		}
		List<List<String>> fileList = new ArrayList<>(2);
		
		fileList.add(path_line_lines);
		fileList.add(sentence_lines);
		
		return fileList;
	}
	
	/**
	 * Tokenizes the specified lines in the given file and returns the tokenized lines (sentences).
	 * @param inputFile
	 * holds the {@link Path} to a Java source code file
	 * @param lineNumbers
	 * holds the line numbers of the lines to be tokenized
	 * @param use_context
	 * sets if each sentence should contain a context of previous tokens
	 * @param startFromMethods
	 * sets if the context (if used) should only go back to the start of a method. (Currently, the implementation
	 * only goes back to the last opening curly bracket, which doesn't have to be the start of a method.)
	 * @param order
	 * the n-gram order (only important for the length of the context)
	 * @param use_lookahead
	 * sets if for each line, the next line should also be appended to the sentence
	 * @return
	 * the tokenized lines as a {@link List}
	 */
	private List<String> createTokenizedLinesOutput(
			final Path inputFile, final List<Integer> lineNumbers, 
			final boolean use_context, boolean startFromMethods, 
			final int order, final boolean use_lookahead) {
		//sort the line numbers
		lineNumbers.sort(null);
		
		CompilationUnit cu = null;
		try (FileInputStream in = new FileInputStream(inputFile.toFile())) {
			// parse the file
			cu = JavaParser.parse(in);

		} catch (FileNotFoundException e) {
			Misc.err(this, e, "File not found...");
		} catch (IOException e) {
			Misc.err(this, e, "IO Exception...");
		} catch (ParseException e) {
			Misc.err(this, e, "Parser Exception...");
		}


		// visit and change the methods names and parameters
		TokenizeVisitor visitor = new TokenizeVisitor(lineNumbers);
		visitor.visit(cu, null);

		// prints the changed compilation unit
		System.out.println(visitor.getSource());

		List<String> list = new ArrayList<>(1);
		list.add(cu.toStringWithoutComments());
		return list;
	}

}
