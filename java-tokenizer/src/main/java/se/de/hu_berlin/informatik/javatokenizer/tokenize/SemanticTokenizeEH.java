/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.tokenize;

import java.nio.file.Path;
import java.util.List;

import se.de.hu_berlin.informatik.utils.tm.AbstractProcessorUser;

/**
 * Tokenizes the whole provided (Java source code) file.
 * 
 * @author Simon Heiden
 */
public class SemanticTokenizeEH extends AbstractProcessorUser<Path,List<String>> {

	private final boolean eol;
	private final boolean produceSingleTokens;
	private final int depth;
	private final boolean methodsOnly;
	
	/**
	 * Initializes a {@link SemanticTokenizeEH} object with the given parameters.
	 * @param methodsOnly
	 * determines if only method bodies should be tokenized
	 * @param eol
	 * determines if ends of lines (EOL) are relevant
	 * @param produceSingleTokens
	 * sets whether for each AST node a single token should be produced
	 * @param depth
	 * the maximum depth of constructing the tokens, where 0 equals
	 * total abstraction and -1 means unlimited depth
	 */
	public SemanticTokenizeEH(boolean methodsOnly, boolean eol, boolean produceSingleTokens, int depth) {
		super();
		this.eol = eol;
		this.produceSingleTokens = produceSingleTokens;
		this.depth = depth;
		this.methodsOnly = methodsOnly;
	}

	@Override
	public List<String> processItem(Path input) {
		return new SemanticTokenizerParser(methodsOnly, eol, produceSingleTokens, depth)
				.asModule()
				.submit(input)
				.getResult();
	}
}

