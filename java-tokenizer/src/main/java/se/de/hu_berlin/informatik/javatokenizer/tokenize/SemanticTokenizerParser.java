/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.tokenize;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import se.de.hu_berlin.informatik.astlmbuilder.ASTTokenReader;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.ITokenMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.shortKW.Node2AbstractionTokenMapperShort;
import se.de.hu_berlin.informatik.javatokenizer.tokenizer.Tokenizer;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

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
public class SemanticTokenizerParser extends AbstractProcessor<Path,List<String>> {

	private boolean eol = false;
	
	private ASTTokenReader<String> reader; 

	/**
	 * Creates a new {@link SemanticTokenizerParser} object with the given parameters.
	 * @param methodsOnly
	 * determines if only method bodies should be tokenized
	 * @param eol
	 * determines if ends of lines (EOL) are relevant
	 * @param produce_single_tokens
	 * sets whether for each AST node a single token should be produced
	 * @param depth
	 * the maximum depth of constructing the tokens, where 0 equals
	 * total abstraction and -1 means unlimited depth
	 */
	public SemanticTokenizerParser(boolean methodsOnly, boolean eol, boolean produce_single_tokens, int depth) {
		this.eol = eol;
		
		ITokenMapper<String> mapper = new Node2AbstractionTokenMapperShort();
		
		reader = new ASTTokenReader<>(
				mapper, null, null, methodsOnly, true, depth);
	}

	@Override
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
