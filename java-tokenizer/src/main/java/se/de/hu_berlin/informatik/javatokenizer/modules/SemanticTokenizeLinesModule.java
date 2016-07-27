/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.modules;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import se.de.hu_berlin.informatik.astlmbuilder.ASTTokenReader;
import se.de.hu_berlin.informatik.astlmbuilder.TokenWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AModule;

/**
 * Module that tokenizes lines of files that are given by a provided {@link Map}
 * that links file paths given as {@link String}s with a {@link Set} of line
 * numbers and populates a given map that links trace file lines to tokenized 
 * sentences.
 * 
 * @author Simon Heiden
 * 
 */
public class SemanticTokenizeLinesModule extends AModule<Map<String, Set<Integer>>, Path> {

	private String src_path;
	private Path lineFile;
	private boolean use_context;
	private boolean startFromMethods; 
	private int order;
	private boolean use_lookahead;
	
	//maps trace file lines to sentences
	private Map<String,String> sentenceMap;
	
	/**
	 * Creates a new {@link SemanticTokenizeLinesModule} object with the given parameters.
	 * @param sentenceMap 
	 * map that links trace file lines to tokenized sentences
	 * @param src_path
	 * is the path to the source folder
	 * @param lineFile
	 * file with file names and line numbers (format: relative/path/To/File:line#)
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
	public SemanticTokenizeLinesModule(Map<String, String> sentenceMap, String src_path, Path lineFile, boolean use_context, boolean startFromMethods, 
			int order, boolean use_lookahead) {
		super(true);
		this.sentenceMap = sentenceMap;
		this.src_path = src_path;
		this.lineFile = lineFile;
		this.use_context = use_context;
		this.startFromMethods = startFromMethods;
		this.order = order;
		this.use_lookahead = use_lookahead;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public Path processItem(Map<String, Set<Integer>> map) {
		createTokenizedLinesOutput(map, use_context, startFromMethods, order, use_lookahead);
		
		return lineFile;
	}

	/**
	 * Tokenizes the specified lines in all files provided in the given {@link HashMap} 
	 * and updates the sentence map.
	 * @param map
	 * holds source code file paths, each associated with an {@link Set} of line numbers.  
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
	private void createTokenizedLinesOutput(
			final Map<String, Set<Integer>> map, 
			final boolean use_context, final boolean startFromMethods, 
			final int order, final boolean use_lookahead) {
		
		for (Entry<String, Set<Integer>> i : map.entrySet()) {
			createTokenizedLinesOutput(i.getKey(), Paths.get(src_path + File.separator + i.getKey()), i.getValue(), use_context, startFromMethods, order, use_lookahead);
		}
	}
	
	/**
	 * Tokenizes the specified lines in the given file and updates the sentence map.
	 * @param prefixForMap
	 * a prefix (path) that completes a trace file line together with a line number
	 * @param inputFile
	 * holds the {@link Path} to a Java source code file
	 * @param lineNumbersSet
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
	 */
	private void createTokenizedLinesOutput(
			String prefixForMap,
			final Path inputFile, final Set<Integer> lineNumbersSet, 
			final boolean use_context, boolean startFromMethods, 
			final int order, final boolean use_lookahead) {
		//sort the line numbers
		List<Integer> lineNumbers = asSortedList(lineNumbersSet);
		
		//try opening the file
		try {
			
			ASTTokenReader reader = new ASTTokenReader(null, null, startFromMethods, true);

			List<List<TokenWrapper>> sentences = reader.getAllTokenSequencesWithLineNumbers(inputFile.toFile());	
			
			final int contextLength = order - 1;
			final String contextToken = "<_con_end_>";

			List<String> context = new ArrayList<>();
			List<String> nextContext = new ArrayList<>();
			StringBuilder line = new StringBuilder();
			StringBuilder contextLine = new StringBuilder();
			StringBuilder lookAhead = new StringBuilder();
			
			//parse the first line number
			int parsedLineNumber = 0;
			int lineNumber_index = 0;
			try {
				parsedLineNumber = lineNumbers.get(lineNumber_index);		
			} catch (Exception e) {
				System.err.println("not able to parse line number " + lineNumber_index);
				parsedLineNumber = 0;
			}
			
			boolean lastLineNeedsUpdate = false;

			int lastReadLine = 0;
			Iterator<List<TokenWrapper>> sentencesIterator = sentences.iterator();
			while (sentencesIterator.hasNext()) {
				Iterator<TokenWrapper> tokenIterator = sentencesIterator.next().iterator();
				
				//only use the context starting at the beginning of a method
				if (startFromMethods) {
					context.clear();
				}
				
				while (tokenIterator.hasNext()) {
					TokenWrapper token = tokenIterator.next();
					
					nextContext.add(token.getToken());
					if (token.getStartLineNumber() <= parsedLineNumber 
							&& token.getEndLineNumber() >= parsedLineNumber) {
						line.append(token.getToken() + " ");
					}
					lookAhead.append(token.getToken() + " ");

					boolean eol = lastReadLine != token.getStartLineNumber();
					//if it's the start of another line or if no more tokens do exist
					if (eol || (!sentencesIterator.hasNext() && !tokenIterator.hasNext())) {
						if (use_lookahead && lastLineNeedsUpdate) {
							if (lookAhead.length() > 0) {
								lookAhead.deleteCharAt(lookAhead.length()-1);
								String temp = sentenceMap.get(prefixForMap + ":" + String.valueOf(lineNumbers.get(lineNumber_index-1)));
								temp += " " + lookAhead.toString();
								sentenceMap.put(prefixForMap + ":" + String.valueOf(lineNumbers.get(lineNumber_index-1)), temp);
								lastLineNeedsUpdate = false;
							}
						}
						if (parsedLineNumber <= lastReadLine && parsedLineNumber >= 0) {
							if (line.length() != 0) {
								//delete the last space
								line.deleteCharAt(line.length()-1);
							}
							
							if (use_context) {
								int index = context.size() - contextLength;

								for (ListIterator<String> i = context.listIterator(index < 0 ? 0 : index); i.hasNext();) {
									contextLine.append(i.next() + " ");
								}
								contextLine.append(contextToken + " ");
							}
							contextLine.append(line);
							
							//add the line to the map
							sentenceMap.put(prefixForMap + ":" + String.valueOf(lineNumbers.get(lineNumber_index)), contextLine.toString());
//							Misc.out(prefixForMap + ":" + String.valueOf(lineNumbers.get(lineNumber_index)) + " -> " + contextLine.toString());
							
							lastLineNeedsUpdate = true;
							//reuse the StringBuilders
							contextLine.setLength(0);
							line.setLength(0);
							
							try {
								parsedLineNumber = lineNumbers.get(++lineNumber_index);
							} catch (Exception e) {
								parsedLineNumber = -1;
							}
						}
						context.addAll(nextContext);
						nextContext.clear();
						lookAhead.setLength(0);
						
						lastReadLine = token.getStartLineNumber();
					}
				}
			}
			
			while (parsedLineNumber > lastReadLine) {
				if (line.length() != 0) {
					//delete the last space
					line.deleteCharAt(line.length()-1);
				}
				
				if (use_context) {
					int index = context.size() - contextLength;

					for (ListIterator<String> i = context.listIterator(index < 0 ? 0 : index); i.hasNext();) {
						contextLine.append(i.next() + " ");
					}
					contextLine.append(contextToken + " ");
				}
				contextLine.append(line);
				
				//add the line to the map
				sentenceMap.put(prefixForMap + ":" + String.valueOf(lineNumbers.get(lineNumber_index)), contextLine.toString());
//				Misc.out(prefixForMap + ":" + String.valueOf(lineNumbers.get(lineNumber_index)) + " -> " + contextLine.toString());
				
				//reuse the StringBuilders
				contextLine.setLength(0);
				line.setLength(0);
				
				try {
					parsedLineNumber = lineNumbers.get(++lineNumber_index);
				} catch (Exception e) {
					parsedLineNumber = 0;
				}
			}
		} catch (Exception x) {
			Misc.err(this, x, "Exception on file %s. Adding empty strings for corresponding lines.", inputFile);
			for (int lineNo : lineNumbers) {
				sentenceMap.put(prefixForMap + ":" + String.valueOf(lineNo), "");
			}
		}
	}
	
	public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
	  List<T> list = new ArrayList<T>(c);
	  java.util.Collections.sort(list);
	  return list;
	}
}
