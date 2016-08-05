/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.tokenize;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import se.de.hu_berlin.informatik.javatokenizer.modules.SemanticTokenizerParserModule;
import se.de.hu_berlin.informatik.utils.fileoperations.ListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.IOutputPathGenerator;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithPaths;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.ModuleLinker;

/**
 * {@link Callable} object that tokenizes the provided (Java source code) file.
 * 
 * @author Simon Heiden
 */
public class SemanticTokenizeCall extends CallableWithPaths<Path,Boolean> {

	private final boolean eol;
	private final boolean methodsOnly;
	private boolean produceSingleTokens;
	
	/**
	 * Initializes a {@link SemanticTokenizeCall} object with the given parameters.
	 * @param methodsOnly 
	 * whether only methods shall be tokenized
	 * @param eol
	 * determines if ends of lines (EOL) are relevant
	 * @param produce_single_tokens
	 * sets whether for each AST node a single token should be produced
	 * @param outputPathGenerator
	 * a generator to automatically create output paths
	 */
	public SemanticTokenizeCall(boolean methodsOnly, boolean eol, boolean produceSingleTokens, IOutputPathGenerator<Path> outputPathGenerator) {
		super(outputPathGenerator);
		this.eol = eol;
		this.methodsOnly = methodsOnly;
		this.produceSingleTokens = produceSingleTokens;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() {
		System.out.print(".");
		
		new ModuleLinker()
		.link(new SemanticTokenizerParserModule(methodsOnly, eol, produceSingleTokens),
				new ListToFileWriterModule<List<String>>(getOutputPath(), true))
		.submit(getInput());
		
		return true;
	}

}

