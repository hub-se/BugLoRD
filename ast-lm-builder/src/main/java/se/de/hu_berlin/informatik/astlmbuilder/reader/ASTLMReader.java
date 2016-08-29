package se.de.hu_berlin.informatik.astlmbuilder.reader;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import edu.berkeley.nlp.lm.ArrayEncodedProbBackoffLm;
import edu.berkeley.nlp.lm.io.LmReaders;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;

/**
 * This class is supposed to read a previously build token language model
 * and provide access to it.
 *
 */
public class ASTLMReader {

	private OptionParser options;
	Logger log = Logger.getLogger( ASTLMReader.class );
	
	/**
	 * Constructor which also reads the arguments
	 */
	public ASTLMReader(String[] args) {
		Logger root = Logger.getRootLogger();
		root.addAppender(new ConsoleAppender(new PatternLayout("%r [%t]: %m%n")));

		options = ASTLMROptions.getOptions(args);
	}
	
	/**
	 * The main method
	 */
	public static void main(String[] args) {
		ASTLMReader reader = new ASTLMReader( args );
		reader.doAction();
	}
	
	/**
	 * The non static main method
	 */
	public void doAction() {
		String srcFile = options.getOptionValue( ASTLMROptions.INPUT_DIR );
		// I know that the type of the file but the api does not let me specify it
		@SuppressWarnings({ "rawtypes", "unchecked" })
		ArrayEncodedProbBackoffLm<String> lm = (ArrayEncodedProbBackoffLm) LmReaders.readLmBinary( srcFile );
		log.info("Found lm of order: " + lm.getLmOrder());
//		WordIndexer<String> wi = lm.getWordIndexer();

	}

}
