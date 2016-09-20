/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.tokenize;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import se.de.hu_berlin.informatik.javatokenizer.modules.SyntacticTokenizerParserModule;
import se.de.hu_berlin.informatik.utils.threaded.ADisruptorEventHandlerFactoryWMultiplexer;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithInputAndReturn;

/**
 * {@link Callable} object that tokenizes the provided (Java source code) file,
 * taking only the included methods into account.
 * 
 * @author Simon Heiden
 */
public class SyntacticTokenizeMethodsCall extends CallableWithInputAndReturn<Path,List<String>> {

	/**
	 * States if ends of lines (EOL) should be incorporated.
	 */
	private final boolean eol;
	
	/**
	 * Initializes a {@link SyntacticTokenizeMethodsCall} object with the given parameters.
	 * @param eol
	 * determines if ends of lines (EOL) are relevant
	 */
	public SyntacticTokenizeMethodsCall(boolean eol) {
		super();
		this.eol = eol;
	}

	public static class Factory extends ADisruptorEventHandlerFactoryWMultiplexer<Path,List<String>> {

		final private boolean eol;

		/**
		 * Initializes a {@link Factory} object with the given parameters.
		 * @param eol
		 * determines if ends of lines (EOL) are relevant
		 */
		public Factory(boolean eol) {
			super(SyntacticTokenizeMethodsCall.class);
			this.eol = eol;
		}

		@Override
		public CallableWithInputAndReturn<Path, List<String>> getNewInstance() {
			return new SyntacticTokenizeMethodsCall(eol);
		}

	}

	@Override
	public void resetAndInit() {
		//not needed
	}

	@Override
	public List<String> processInput(Path input) {
		return new SyntacticTokenizerParserModule(true, eol)
				.submit(input)
				.getResult();
	}
	
}

