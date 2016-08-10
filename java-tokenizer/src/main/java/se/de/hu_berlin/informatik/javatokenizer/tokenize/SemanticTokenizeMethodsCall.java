/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.tokenize;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import se.de.hu_berlin.informatik.javatokenizer.modules.SemanticTokenizerParserModule;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithPaths;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.PipeLinker;

/**
 * {@link Callable} object that tokenizes the provided (Java source code) file,
 * taking only the included methods into account.
 * 
 * @author Simon Heiden
 */
public class SemanticTokenizeMethodsCall extends CallableWithPaths<Path,Boolean> {

	private final boolean eol;
	private boolean produceSingleTokens;
	private int depth;
	
	/**
	 * Initializes a {@link SemanticTokenizeMethodsCall} object with the given parameters.
	 * @param eol
	 * determines if ends of lines (EOL) are relevant
	 * @param produceSingleTokens
	 * sets whether for each AST node a single token should be produced
	 * @param callback
	 * a PipeLinker callback object that expects lists of Strings as input objects
	 * @param depth
	 * the maximum depth of constructing the tokens, where 0 equals
	 * total abstraction and -1 means unlimited depth
	 */
	public SemanticTokenizeMethodsCall(boolean eol, boolean produceSingleTokens, 
			PipeLinker callback, int depth) {
		super(callback);
		this.eol = eol;
		this.produceSingleTokens = produceSingleTokens;
		this.depth = depth;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() {
		System.out.print(".");
		
		getCallback().submit(
				new SemanticTokenizerParserModule(true, eol, produceSingleTokens, depth)
				.submit(getInput())
				.getResult());
		
		return true;
	}

}

