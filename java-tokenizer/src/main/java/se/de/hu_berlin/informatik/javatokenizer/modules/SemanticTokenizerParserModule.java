/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.modules;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import se.de.hu_berlin.informatik.astlmbuilder.ASTTokenReader;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.AdvancedNode2StringMapping;
import se.de.hu_berlin.informatik.javatokenizer.tokenizer.Tokenizer;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AModule;

/**
 * Parser module that tokenizes a given input file and outputs a 
 * {@link List} of tokenized lines as {@link String}s. Uses an
 * original implementation of a Java tokenizer. For the tokenization
 * of only methods, the "com.github.javaparser" framework is used
 * to obtain the method bodies up front.
 * 
 * @author Simon Heiden
 * 
 * @see Tokenizer
 */
public class SemanticTokenizerParserModule extends AModule<Path,List<String>> {

	private boolean eol = false;
	
	private ASTTokenReader<String> reader; 

	/**
	 * Creates a new {@link SemanticTokenizerParserModule} object with the given parameters.
	 * @param methodsOnly
	 * determines if only method bodies should be tokenized
	 * @param eol
	 * determines if ends of lines (EOL) are relevant
	 */
	public SemanticTokenizerParserModule(boolean methodsOnly, boolean eol) {
		super(true);
		this.eol = eol;
		
		reader = new ASTTokenReader<>(
				new AdvancedNode2StringMapping(), 
				null, null, methodsOnly, true);
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public List<String> processItem(Path inputPath) {
		List<List<String>> list = reader.getAllTokenSequences(inputPath);
		
		List<String> result = new ArrayList<>(list.size());
		for (List<String> element : list) {
//			Misc.out(Misc.arrayToString(element.toArray(new String[0])));
			result.add(String.join(" ", element));
		}
		
		if (!eol) {
			String temp = String.join(" ", result);
			result = new ArrayList<>(1);
			result.add(temp);
		}
		
		return result;
	}

}
