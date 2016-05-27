/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.tokenize;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import se.de.hu_berlin.informatik.javatokenizer.modules.TokenizerParserModule;
import se.de.hu_berlin.informatik.utils.fileoperations.StringListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.IOutputPathGenerator;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithPaths;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.ModuleLinker;

/**
 * {@link Callable} object that tokenizes the method bodies of the provided Java (1.8) source code file.
 * 
 * @author Simon Heiden
 */
public class TokenizeMethodsCall extends CallableWithPaths<Path,Boolean> {

	/**
	 * States if ends of lines (EOL) should be incorporated.
	 */
	final boolean eol;
	
	/**
	 * Initializes a {@link TokenizeMethodsCall} object with the given parameters.
	 * @param eol
	 * determines if ends of lines (EOL) are relevant
	 * @param outputPathGenerator
	 * a generator to automatically create output paths
	 */
	public TokenizeMethodsCall(boolean eol, IOutputPathGenerator<Path> outputPathGenerator) {
		super(outputPathGenerator);
		this.eol = eol;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() {
		System.out.print(".");
		ModuleLinker linker = new ModuleLinker();
		linker.link(new TokenizerParserModule(true, eol), 
				new StringListToFileWriterModule<List<String>>(getOutputPath(), true))
			.submitAndStart(getInput());
		return true;
	}

}

