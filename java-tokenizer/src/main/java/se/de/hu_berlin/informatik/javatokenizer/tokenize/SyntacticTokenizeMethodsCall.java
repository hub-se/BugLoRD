/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.tokenize;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import se.de.hu_berlin.informatik.javatokenizer.modules.SyntacticTokenizerParserModule;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithPaths;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.PipeLinker;

/**
 * {@link Callable} object that tokenizes the provided (Java source code) file,
 * taking only the included methods into account.
 * 
 * @author Simon Heiden
 */
public class SyntacticTokenizeMethodsCall extends CallableWithPaths<Path,Boolean> {

	/**
	 * States if ends of lines (EOL) should be incorporated.
	 */
	private final boolean eol;
	
	/**
	 * Initializes a {@link SyntacticTokenizeMethodsCall} object with the given parameters.
	 * @param eol
	 * determines if ends of lines (EOL) are relevant
	 * @param callback
	 * a PipeLinker callback object that expects lists of Strings as input objects
	 */
	public SyntacticTokenizeMethodsCall(boolean eol, PipeLinker callback) {
		super(callback);
		this.eol = eol;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() {
		getCallback().submit(
				new SyntacticTokenizerParserModule(true, eol)
				.submit(getInput())
				.getResult());
		
		return true;
	}

}

