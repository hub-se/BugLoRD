package se.de.hu_berlin.informatik.astlmbuilder.tests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import se.de.hu_berlin.informatik.astlmbuilder.ASTTokenReader;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.AdvancedNode2StringMapping;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.ITokenMapper;

public class GenerateTokenFilesFromSourceFiles {

	public static void main(String[] args) {
		GenerateTokenFilesFromSourceFiles gen = new GenerateTokenFilesFromSourceFiles();
		gen.doAction( args );
	}
	
	public void doAction( String[] args ) {
		// this has to be the same object for all token reader threads
	
		ITokenMapper<String, Integer> mapper = new AdvancedNode2StringMapping();
		int abstractionDepth = 0;
		int seriDepth = 0;
		ASTTokenReader<String> reader = new ASTTokenReader<String>(mapper, null, null, true, true, abstractionDepth, seriDepth);
		List<List<String>> allTokenSequences = reader.getAllTokenSequences( new File( args[0] ));
		
		// write all sequences to the output file
		File outputF = new File( args[1] );
		FileWriter writer = null;
		try {
			writer = new FileWriter( outputF );
			
			for( List<String> seq : allTokenSequences ) {
				StringBuilder oneLine = new StringBuilder();
				for( String s : seq ) {
					oneLine.append( s + " " );
				}
				oneLine.append( "\n" );
				
				writer.write( oneLine.toString() );
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if ( writer != null ) {
				try {
					writer.close();
				} catch (IOException e) {
					// nothing to do
				}
			}
		}
		
		
	}

}
