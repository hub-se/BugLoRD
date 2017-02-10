/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.tokenize;

import java.nio.file.Path;
import java.util.List;

import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturn;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturnMethodProvider;

/**
 * Tokenizes the whole provided (Java source code) file.
 * 
 * @author Simon Heiden
 */
public class SyntacticTokenizeEH extends EHWithInputAndReturnMethodProvider<Path,List<String>> {

	/**
	 * States if ends of lines (EOL) should be incorporated.
	 */
	private final boolean eol;
	private final boolean methodsOnly;
	
	/**
	 * Initializes a {@link SyntacticTokenizeEH} object with the given parameters.
	 * @param methodsOnly
	 * determines if only method bodies should be tokenized
	 * @param eol
	 * determines if ends of lines (EOL) are relevant
	 */
	public SyntacticTokenizeEH(boolean methodsOnly, boolean eol) {
		super();
		this.eol = eol;
		this.methodsOnly = methodsOnly;
	}

	@Override
	public List<String> processInput(Path input, EHWithInputAndReturn<Path, List<String>> executingHandler) {
		return new SyntacticTokenizerParser(methodsOnly, eol)
				.asModule()
				.submit(input)
				.getResult();
	}
	
}

