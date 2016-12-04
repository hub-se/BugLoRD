package se.de.hu_berlin.informatik.astlmbuilder.reader;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import edu.berkeley.nlp.lm.ArrayEncodedProbBackoffLm;
import edu.berkeley.nlp.lm.ConfigOptions;
import edu.berkeley.nlp.lm.WordIndexer;
import edu.berkeley.nlp.lm.io.LmReaders;
import se.de.hu_berlin.informatik.astlmbuilder.ASTLMBuilder;
import se.de.hu_berlin.informatik.astlmbuilder.reader.ASTLMROptions.ASTLMRCmdOptions;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;

/**
 * This class is supposed to read a previously build token language model
 * and provide access to it.
 *
 */
public class ASTLMReader {

	public OptionParser options;
	Logger log = Logger.getLogger( ASTLMReader.class );
	
	/**
	 * Constructor which also reads the arguments
	 * @param args
	 * command line arguments
	 */
	public ASTLMReader(String[] args) {
		Logger root = Logger.getRootLogger();
		root.addAppender(new ConsoleAppender(new PatternLayout("%r [%t]: %m%n")));

		options = OptionParser.getOptions("AST LM Reader", false, ASTLMRCmdOptions.class, args);
	}
	
	/**
	 * The main method
	 * @param args
	 * command line arguments
	 */
	public static void main(String[] args) {
		ASTLMReader reader = new ASTLMReader( args );
		reader.doAction();
	}
	
	/**
	 * The non static main method
	 */
	public void doAction() {
		String srcFile = options.getOptionValue( ASTLMRCmdOptions.INPUT );
		int n = Integer.parseInt( options.getOptionValue( ASTLMRCmdOptions.LM_ORDER, ASTLMROptions.LM_ORDER_DEFAULT ));
		
		ArrayEncodedProbBackoffLm<String> lm = readLMFromFile( srcFile, n );

		log.info("Found lm of order: " + lm.getLmOrder());
	}
	
	
	public ArrayEncodedProbBackoffLm<String> readLMFromFile( String aLMFile ) {
		return readLMFromFile( aLMFile, -1 );
	}
	
	/**
	 * Reads the given file and create a language model object from it
	 * @param aLMFile The lm file as arpa or binary
	 * @param aLmOrder
	 * the order of the language model
	 * @return The lm object as ArrayEncodedProbBackoffLm
	 */
	public ArrayEncodedProbBackoffLm<String> readLMFromFile( String aLMFile, int aLmOrder ) {
		ArrayEncodedProbBackoffLm<String> lm;
		WordIndexer<String> wi = ASTLMBuilder.getNewWordIndexer();
		
//		ConfigOptions co = new ConfigOptions(); 
		
		if ( aLMFile.endsWith( ASTLMROptions.BINARY_SUFFIX ) ) {
			lm = (ArrayEncodedProbBackoffLm<String>) LmReaders.<String>readLmBinary( aLMFile );
		} else {
			if( aLmOrder == -1 ) {
				lm = (ArrayEncodedProbBackoffLm<String>) LmReaders.readArrayEncodedLmFromArpa(aLMFile, false, wi);
			} else {
				lm = (ArrayEncodedProbBackoffLm<String>) LmReaders.readArrayEncodedLmFromArpa(aLMFile, false, wi, new ConfigOptions(), aLmOrder);
			}	
		}
		
		return lm;
	}

}