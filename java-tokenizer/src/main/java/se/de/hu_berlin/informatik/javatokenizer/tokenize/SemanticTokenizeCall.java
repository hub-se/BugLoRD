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
import se.de.hu_berlin.informatik.utils.threaded.DisruptorEventHandler;
import se.de.hu_berlin.informatik.utils.threaded.IDisruptorEventHandlerFactory;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.ModuleLinker;

/**
 * {@link Callable} object that tokenizes the whole provided (Java source code) file.
 * 
 * @author Simon Heiden
 */
public class SemanticTokenizeCall extends CallableWithPaths<Path,Boolean> {

	private final boolean eol;
	private final boolean produceSingleTokens;
	private final int depth;
	
	/**
	 * Initializes a {@link SemanticTokenizeCall} object with the given parameters.
	 * @param eol
	 * determines if ends of lines (EOL) are relevant
	 * @param produceSingleTokens
	 * sets whether for each AST node a single token should be produced
	 * @param outputPathGenerator
	 * a generator to automatically create output paths
	 * @param depth
	 * the maximum depth of constructing the tokens, where 0 equals
	 * total abstraction and -1 means unlimited depth
	 */
	public SemanticTokenizeCall(boolean eol, boolean produceSingleTokens, 
			IOutputPathGenerator<Path> outputPathGenerator, int depth) {
		super(outputPathGenerator);
		this.eol = eol;
		this.produceSingleTokens = produceSingleTokens;
		this.depth = depth;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() {
		new ModuleLinker()
		.link(new SemanticTokenizerParserModule(false, eol, produceSingleTokens, depth),
				new ListToFileWriterModule<List<String>>(getOutputPath(), true))
		.submit(getInput());
		
		return true;
	}

	public static class Factory implements IDisruptorEventHandlerFactory<Path> {

		private final boolean eol;
		private final boolean produceSingleTokens;
		private final int depth;
		private final IOutputPathGenerator<Path> outputPathGenerator;
		
		/**
		 * Initializes a {@link Factory} object with the given parameters.
		 * @param eol
		 * determines if ends of lines (EOL) are relevant
		 * @param produceSingleTokens
		 * sets whether for each AST node a single token should be produced
		 * @param outputPathGenerator
		 * a generator to automatically create output paths
		 * @param depth
		 * the maximum depth of constructing the tokens, where 0 equals
		 * total abstraction and -1 means unlimited depth
		 */
		public Factory(boolean eol, boolean produceSingleTokens, 
				IOutputPathGenerator<Path> outputPathGenerator, int depth) {
			this.eol = eol;
			this.produceSingleTokens = produceSingleTokens;
			this.depth = depth;
			this.outputPathGenerator = outputPathGenerator;
		}
		
		@Override
		public Class<? extends DisruptorEventHandler<Path>> getEventHandlerClass() {
			return SemanticTokenizeCall.class;
		}

		@Override
		public DisruptorEventHandler<Path> newInstance() {
			return new SemanticTokenizeCall(eol, produceSingleTokens, outputPathGenerator, depth);
		}
	}

	@Override
	public void resetAndInit() {
		//not needed
	}
}

