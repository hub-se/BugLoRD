/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.tokenize;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import se.de.hu_berlin.informatik.javatokenizer.modules.SemanticTokenizerParserModule;
import se.de.hu_berlin.informatik.utils.threaded.ADisruptorEventHandlerWMultiplexerFactory;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithInputAndReturn;

/**
 * {@link Callable} object that tokenizes the provided (Java source code) file,
 * taking only the included methods into account.
 * 
 * @author Simon Heiden
 */
public class SemanticTokenizeMethodsCall extends CallableWithInputAndReturn<Path,List<String>> {

	private final boolean eol;
	private boolean produceSingleTokens;
	private int depth;
	
	/**
	 * Initializes a {@link SemanticTokenizeMethodsCall} object with the given parameters.
	 * @param eol
	 * determines if ends of lines (EOL) are relevant
	 * @param produceSingleTokens
	 * sets whether for each AST node a single token should be produced
	 * @param depth
	 * the maximum depth of constructing the tokens, where 0 equals
	 * total abstraction and -1 means unlimited depth
	 */
	public SemanticTokenizeMethodsCall(boolean eol, boolean produceSingleTokens, int depth) {
		super();
		this.eol = eol;
		this.produceSingleTokens = produceSingleTokens;
		this.depth = depth;
	}
	
	@Override
	public List<String> processInput(Path input) {
		return new SemanticTokenizerParserModule(true, eol, produceSingleTokens, depth)
				.submit(input)
				.getResult();
	}

	public static class Factory extends ADisruptorEventHandlerWMultiplexerFactory<Path,List<String>> {

		private final boolean eol;
		private final boolean produceSingleTokens;
		private final int depth;
		
		/**
		 * Initializes a {@link Factory} object with the given parameters.
		 * @param eol
		 * determines if ends of lines (EOL) are relevant
		 * @param produceSingleTokens
		 * sets whether for each AST node a single token should be produced
		 * @param depth
		 * the maximum depth of constructing the tokens, where 0 equals
		 * total abstraction and -1 means unlimited depth
		 */
		public Factory(boolean eol, boolean produceSingleTokens, int depth) {
			super(SemanticTokenizeMethodsCall.class);
			this.eol = eol;
			this.produceSingleTokens = produceSingleTokens;
			this.depth = depth;
		}

		@Override
		public CallableWithInputAndReturn<Path, List<String>> getNewInstance() {
			return new SemanticTokenizeMethodsCall(eol, produceSingleTokens, depth);
		}
	}

	@Override
	public void resetAndInit() {
		//not needed
	}
	
}

