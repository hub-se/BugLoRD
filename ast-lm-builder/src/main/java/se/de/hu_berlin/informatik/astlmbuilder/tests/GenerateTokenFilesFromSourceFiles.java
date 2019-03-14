package se.de.hu_berlin.informatik.astlmbuilder.tests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import edu.berkeley.nlp.lm.StringWordIndexer;
import edu.berkeley.nlp.lm.io.LmReaderCallback;
import edu.berkeley.nlp.lm.util.LongRef;
import se.de.hu_berlin.informatik.astlmbuilder.ASTTokenReader;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstants;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.IMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.Node2AbstractionMapper;

public class GenerateTokenFilesFromSourceFiles {

	public static void main(String[] args) {
		GenerateTokenFilesFromSourceFiles gen = new GenerateTokenFilesFromSourceFiles();
		gen.doAction( args );
	}
	
	public void doAction( String[] args ) {
		// this has to be the same object for all token reader threads
	
		int maxListMembers = -1;
		IMapper<String> mapper = new Node2AbstractionMapper.Builder(new KeyWordConstants())
				.setMaxListMembers(maxListMembers)
				.usesStringAbstraction()
				.usesVariableNameAbstraction()
				.usesPrivateMethodAbstraction()
				.usesClassNameAbstraction()
				.usesMethodNameAbstraction()
				.build();
		StringWordIndexer swi = null; // not needed for testing
		LmReaderCallback<LongRef> cb = null; // not needed for testing
		boolean onlyMethods = false; // use everything for testing
		boolean filterNodes = false; // do not filter
		int depth = 0;
		
		ASTTokenReader<String> reader = new ASTTokenReader<>(mapper, swi, cb, onlyMethods, filterNodes, depth, false);
		List<List<String>> allTokenSequences = reader.getAllTokenSequences( new File( args[0] ));
		
		// write all sequences to the output file
		File outputF = new File( args[1] );
		try (FileWriter writer = new FileWriter(outputF)) {

			for (List<String> seq : allTokenSequences) {
				StringBuilder oneLine = new StringBuilder();
				for (String s : seq) {
					oneLine.append(s).append(" ");
				}
				oneLine.append("\n");

				writer.write(oneLine.toString());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		// nothing to do


	}

}
