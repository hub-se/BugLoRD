package se.de.hu_berlin.informatik.astlmbuilder.tests;

import edu.berkeley.nlp.lm.ArrayEncodedProbBackoffLm;
import junit.framework.TestCase;
import se.de.hu_berlin.informatik.astlmbuilder.reader.ASTLMROptions;
import se.de.hu_berlin.informatik.astlmbuilder.reader.ASTLMReader;

/**
 * Only for private testings and therefore little to no checks for invalid arguments 
 *
 */
public class ReaderTest extends TestCase {

	private ASTLMReader reader;
	
	public ReaderTest( String[] args ) {
		reader = new ASTLMReader( args );
	}
	
	public static void main(String[] args) {
		ReaderTest rt = new ReaderTest( args );
		rt.doAction();

	}
	
	/**
	 * Non static entry method
	 */
	public void doAction() {
		ArrayEncodedProbBackoffLm<String> lm = reader.readLMFromFile( reader.options.getOptionValue( ASTLMROptions.INPUT_DIR) );
		System.out.println( "Order: " + lm.getLmOrder() );
		
		// TODO implement tests to ask the language model for probabilities
	}

}
