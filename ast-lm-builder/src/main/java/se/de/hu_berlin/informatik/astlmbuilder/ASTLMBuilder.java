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
import se.de.hu_berlin.informatik.astlmbuilder.mapping.ExperimentalAdvancedNode2StringMapping;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.ITokenMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.Multiple2SingleTokenMapping;
import se.de.hu_berlin.informatik.utils.fileoperations.ThreadedFileWalkerModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithPaths;

/**
 * This is the main class of the AST Language Model Builder which builds a
 * language model based on the tokens retrieved from java source files.
 */
public class ASTLMBuilder {

	Logger log = Logger.getLogger(ASTLMBuilder.class);

	private OptionParser options = null;
	private final int THREAD_COUNT;
	private final int MAPPING_DEPTH_VALUE;

	private static final String VALID_FILES_PATTERN = "**/*.java";
	private static final String VERSION = "1.1";

	/**
	 * Constructor which also reads the arguments
	 * @param args
	 * command line arguments
	 */
	public ASTLMBuilder(String[] args) {
		Logger root = Logger.getRootLogger();
		root.addAppender(new ConsoleAppender(new PatternLayout("%r [%t]: %m%n")));

		options = ASTLMBOptions.getOptions(args);
		// using more than one thread currently creates unstable results
		THREAD_COUNT = 1;
//		THREAD_COUNT = Integer
//				.parseInt(options.getOptionValue(ASTLMBOptions.THREAD_COUNT, ASTLMBOptions.THREAD_COUNT_DEFAULT));
		
		MAPPING_DEPTH_VALUE = Integer
		.parseInt(options.getOptionValue(ASTLMBOptions.MAPPING_DEPTH, ASTLMBOptions.MAPPING_DEPTH_DEFAULT));
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
	@SuppressWarnings("unchecked")
	public void doAction() {		
		log.info("Started the AST Language Model Builder (v." + VERSION + ")");

		// this has to be the same object for all token reader threads
		StringWordIndexer wordIndexer = getNewWordIndexer();
		int ngramOrder = Integer
				.parseInt(options.getOptionValue(ASTLMBOptions.NGRAM_ORDER, ASTLMBOptions.NGRAM_ORDER_DEFAULT));

		ConfigOptions defOpt = new ConfigOptions();

		// all token readers will put their sequences in the same callback object
		KneserNeyLmReaderCallback<String> callback = 
				new KneserNeyLmReaderCallback<String>(wordIndexer, ngramOrder, defOpt);

		boolean ignoreRootDir = true;
		boolean searchDirectories = false;
		boolean searchFiles = true;
		String inputDir = options.getOptionValue(ASTLMBOptions.INPUT_DIR);
		Path inputPath = Paths.get(inputDir);

		boolean filterNodes = options.getOptionValue(ASTLMBOptions.GRANULARITY, ASTLMBOptions.GRAN_NORMAL)
				.equalsIgnoreCase(ASTLMBOptions.GRAN_NORMAL);
		boolean onlyMethods = options.getOptionValue(ASTLMBOptions.ENTRY_POINT, ASTLMBOptions.ENTRY_METHOD)
				.equalsIgnoreCase(ASTLMBOptions.ENTRY_METHOD);

		//you can configure the token mapper here at this point
		ITokenMapper<String,Integer> mapper = new ExperimentalAdvancedNode2StringMapping();
		
		if (options.hasOption(ASTLMBOptions.SINGLE_TOKENS)) {
			mapper = new Multiple2SingleTokenMapping<>(mapper);
		}
		
		@SuppressWarnings("rawtypes")
		Class<CallableWithPaths<Path, Boolean>> TokenReaderClass = (Class) ASTTokenReader.class;
				
		// create the thread pool for the file parsing
		new ThreadedFileWalkerModule(ignoreRootDir, searchDirectories, searchFiles, VALID_FILES_PATTERN, THREAD_COUNT,
				TokenReaderClass, mapper, wordIndexer, callback, onlyMethods, filterNodes, MAPPING_DEPTH_VALUE)
		.submit(inputPath);

		log.info("Finished training the language model. Writing it to disk...");
		
		// write lm to file
		String outputFile = options.getOptionValue(ASTLMBOptions.OUTPUT_FILE);
		Misc.ensureParentDir(new File(outputFile));

		if (options.hasOption(ASTLMBOptions.CREATE_ARPA_TEXT)) {

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
		// this is somehow the start of a relevant sequence like the start of a
		// method
		wordIndexer.setStartSymbol(ArpaLmReader.START_SYMBOL);
		// not sure how the end should be indicated. End of method block?
		wordIndexer.setEndSymbol(ArpaLmReader.END_SYMBOL);
		// what is this anyway?
		wordIndexer.setUnkSymbol(ArpaLmReader.UNK_SYMBOL);
		return wordIndexer;
	}

}
