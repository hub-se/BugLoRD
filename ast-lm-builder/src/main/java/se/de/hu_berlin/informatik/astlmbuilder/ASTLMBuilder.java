package se.de.hu_berlin.informatik.astlmbuilder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import edu.berkeley.nlp.lm.ConfigOptions;
import edu.berkeley.nlp.lm.StringWordIndexer;
import edu.berkeley.nlp.lm.io.ArpaLmReader;
import edu.berkeley.nlp.lm.io.KneserNeyFileWritingLmReaderCallback;
import edu.berkeley.nlp.lm.io.KneserNeyLmReaderCallback;
import edu.berkeley.nlp.lm.io.LmReaders;
import se.de.hu_berlin.informatik.astlmbuilder.ASTLMBOptions.ASTLMBCmdOptions;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstants;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstantsShort;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.IMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.Node2AbstractionTokenMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.serialization.Node2SerializationMapper;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.files.processors.ThreadedFileWalkerProcessor;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;



/**
 * This is the main class of the AST Language Model Builder which builds a
 * language model based on the tokens retrieved from java source files.
 */
public class ASTLMBuilder {

	Logger log = Logger.getLogger(ASTLMBuilder.class);

	OptionParser options = null;
	private final int THREAD_COUNT;
	private final int MAPPING_DEPTH_VALUE;
	private final int NGRAM_ORDER;

	private static final String VALID_FILES_PATTERN = "**/*.java";
	private static final String VERSION = "1.4";

	/**
	 * Constructor which also reads the arguments
	 * @param args
	 * command line arguments
	 */
	public ASTLMBuilder(String[] args) {
		Logger root = Logger.getRootLogger();
		root.addAppender(new ConsoleAppender(new PatternLayout("%r [%t]: %m%n")));

		options = OptionParser.getOptions("AST LM Builder", false, ASTLMBCmdOptions.class, args);
		// using more than one thread currently creates unstable results
		THREAD_COUNT = 1;
		
		MAPPING_DEPTH_VALUE = Integer.parseInt(options.getOptionValue( ASTLMBCmdOptions.MAPPING_DEPTH, ASTLMBOptions.MAPPING_DEPTH_DEFAULT ));
	
		NGRAM_ORDER = Integer
				.parseInt(options.getOptionValue(ASTLMBCmdOptions.NGRAM_ORDER, ASTLMBOptions.NGRAM_ORDER_DEFAULT));	
	}

	/**
	 * Entry method
	 * @param args
	 * command line arguments
	 */
	public static void main(String[] args) {
		ASTLMBuilder builder = new ASTLMBuilder(args);
		builder.doAction();
	}

	/**
	 * The non static main method
	 */
	public void doAction() {		
		log.info("Started the AST Language Model Builder (v." + VERSION + ")");
		
		// this has to be the same object for all token reader threads
		StringWordIndexer wordIndexer = getNewWordIndexer();
		ConfigOptions defOpt = new ConfigOptions();

		// all token readers will put their sequences in the same callback object
		KneserNeyLmReaderCallback<String> callback = 
				new KneserNeyLmReaderCallback<String>(wordIndexer, NGRAM_ORDER, defOpt);

		String inputDir = options.getOptionValue(ASTLMBCmdOptions.INPUT);
		Path inputPath = Paths.get(inputDir);

		// only filter with normal granularity
		boolean filterNodes = options.getOptionValue(ASTLMBCmdOptions.GRANULARITY, ASTLMBOptions.GRAN_DEFAULT)
				.equalsIgnoreCase(ASTLMBOptions.GRAN_NORMAL);
		// mark if only nodes below a method declaration should be used
		boolean onlyMethods = options.getOptionValue(ASTLMBCmdOptions.ENTRY_POINT, ASTLMBOptions.ENTRY_DEFAULT)
				.equalsIgnoreCase(ASTLMBOptions.ENTRY_METHOD);

		//you can configure the token mapper here at this point
		IMapper<String> mapper = null;
		int seriDepth = Integer.parseInt( options.getOptionValue( ASTLMBCmdOptions.SERIALIZATION_DEPTH, ASTLMBOptions.SERIALIZATION_DEPTH_DEFAULT ));
		int seriMaxChildren = Integer.parseInt( options.getOptionValue( ASTLMBCmdOptions.SERIALIZATION_MAX_CHILDREN, ASTLMBOptions.SERIALIZATION_MAX_CHILDREN_DEFAULT ));
		boolean hrkwMode = options.hasOption( ASTLMBCmdOptions.HUMAN_READABLE_KEYWORDS );
		
		if ( seriDepth != 0 ) {
			// basically the same as the other mapper but with serialization enabled
			if ( hrkwMode ) {
				mapper = new Node2SerializationMapper(new KeyWordConstants(), seriMaxChildren );
			}else {
				mapper = new Node2SerializationMapper(new KeyWordConstantsShort(), seriMaxChildren );
			}
		} else {
			// adding abstraction depended informations to the tokens
			if ( hrkwMode ) {
				mapper = new Node2AbstractionTokenMapper(new KeyWordConstants());
			} else {
				mapper = new Node2AbstractionTokenMapper(new KeyWordConstantsShort());
			}	
		}
				
		ThreadedFileWalkerProcessor tfwm = new ThreadedFileWalkerProcessor(VALID_FILES_PATTERN, THREAD_COUNT);
		tfwm.includeRootDir(); // currently this sets the root directory in use variable to false
		tfwm.searchForFiles(); // enables the search for files which is the main purpose of this module
		
		tfwm.call(new ASTTokenReader<String>(mapper, wordIndexer, callback, onlyMethods, filterNodes, MAPPING_DEPTH_VALUE));
		
		tfwm.enableTracking(50);
		tfwm.submit(inputPath);
		

		log.info("Finished training the language model. Writing it to disk...");
		
		// write lm to file
		String outputFile = options.getOptionValue(ASTLMBCmdOptions.OUTPUT);
		FileUtils.ensureParentDir(new File(outputFile));

		if (options.hasOption(ASTLMBCmdOptions.CREATE_ARPA_TEXT)) {

			try {
				// create a text file
				String textOutput = outputFile + ".arpa";
				log.info("Start writing language model to text file...");
				// sometimes this fails on some random null pointer and corrupts
				// the bin file aswell
				// I dont know why and when it happens... seems to happen with multiple threads only
				callback.parse(new KneserNeyFileWritingLmReaderCallback<String>(new File(textOutput), wordIndexer));
			} catch (NullPointerException npe) {
				// this is kind of strange and I dont know why this happens
				log.info("Could not create the text version of the language model", npe);
			}
		}

		log.info("Start writing language model to binary file...");
		// create a binary file even if the text file was created too
		LmReaders.writeLmBinary(callback, outputFile + ".bin");

		log.info("Finished the AST Language Model Builder");
		log.info("Processed around " + ASTTokenReader.stats_files_processed + " files.");
		log.info( "Successfully parsed and added to language model " + ASTTokenReader.stats_files_successfully_parsed + " files.");
		log.info("Overview of exceptions and errors: ");
		log.info("\tFile not found exceptions: " + ASTTokenReader.stats_fnf_e);
		log.info("\tParsing the AST exceptions: " + ASTTokenReader.stats_parse_e);
		log.info("\tRuntime exceptions: " + ASTTokenReader.stats_runtime_e);
		log.info("\tOther exceptions: " + ASTTokenReader.stats_general_e);
		log.info("\tToken Manager erros: " + ASTTokenReader.stats_token_err);
		log.info("\tOther errors: " + ASTTokenReader.stats_general_err);
		
		// because this should not happen we print all not found file names at the end
		log.info( "List of files that were not found: " );
		for( String s : ASTTokenReader.fnf_list ) {
			log.info( s );
		}
	}

	/**
	 * Creates a new word indexer with default arpa symbols.
	 * @return
	 * a word indexer
	 */
	public static StringWordIndexer getNewWordIndexer() {
		StringWordIndexer wordIndexer = new StringWordIndexer();
		// this marks the start of a sentence
		wordIndexer.setStartSymbol(ArpaLmReader.START_SYMBOL);
		// this marks the end of a sentence
		wordIndexer.setEndSymbol(ArpaLmReader.END_SYMBOL);
		// this marks tokens that do not exist in the vocabulary
		// (only needed for querying, I think...)
		wordIndexer.setUnkSymbol(ArpaLmReader.UNK_SYMBOL);
		return wordIndexer;
	}

}
