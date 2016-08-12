/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.tokenize;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import se.de.hu_berlin.informatik.javatokenizer.modules.SyntacticTokenizerParserModule;
import se.de.hu_berlin.informatik.utils.fileoperations.ListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.IOutputPathGenerator;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithPaths;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.ModuleLinker;

/**
 * {@link Callable} object that tokenizes the whole provided (Java source code) file.
 * 
 * @author Simon Heiden
 */
public class SyntacticTokenizeCall extends CallableWithPaths<Path,Boolean> {

	/**
	 * States if ends of lines (EOL) should be incorporated.
	 */
	private final boolean eol;
	
	/**
	 * Initializes a {@link SyntacticTokenizeCall} object with the given parameters.
	 * @param eol
	 * determines if ends of lines (EOL) are relevant
	 * @param outputPathGenerator
	 * a generator to automatically create output paths
	 */
	public SyntacticTokenizeCall(boolean eol, IOutputPathGenerator<Path> outputPathGenerator) {
		super(outputPathGenerator);
		this.eol = eol;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() {
		new ModuleLinker()
		.link(new SyntacticTokenizerParserModule(false, eol),
				new ListToFileWriterModule<List<String>>(getOutputPath(), true))
		.submit(getInput());
		
		return true;
	}

}

