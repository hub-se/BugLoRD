/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.tokenize;

import java.nio.file.Path;
import java.util.List;

import se.de.hu_berlin.informatik.utils.threaded.ADisruptorEventHandlerWMultiplexerFactory;
import se.de.hu_berlin.informatik.utils.threaded.EHWithInputAndReturn;

/**
 * Tokenizes the whole provided (Java source code) file.
 * 
 * @author Simon Heiden
 */
public class SemanticTokenizeEH extends EHWithInputAndReturn<Path,List<String>> {

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

	public static class Factory extends ADisruptorEventHandlerWMultiplexerFactory<Path,List<String>> {

		private final boolean eol;
		private final boolean produceSingleTokens;
		private final int depth;
		private final boolean methodsOnly;
		
		/**
		 * Initializes a {@link Factory} object with the given parameters.
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
		public Factory(boolean methodsOnly, boolean eol, boolean produceSingleTokens, int depth) {
			super(SemanticTokenizeEH.class);
			this.eol = eol;
			this.produceSingleTokens = produceSingleTokens;
			this.depth = depth;
			this.methodsOnly = methodsOnly;
		}

		@Override
		public EHWithInputAndReturn<Path, List<String>> getNewInstance() {
			return new SemanticTokenizeEH(methodsOnly, eol, produceSingleTokens, depth);
		}
	}

	@Override
	public void resetAndInit() {
		//not needed
	}

	@Override
	public List<String> processInput(Path input) {
		return new SemanticTokenizerParser(methodsOnly, eol, produceSingleTokens, depth)
				.asModule()
				.submit(input)
				.getResult();
	}
}

