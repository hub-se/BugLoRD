
package se.de.hu_berlin.informatik.changechecker;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.SignificanceLevel;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;

/**
 *  
 * 
 * @author Simon Heiden
 */
public class ChangeChecker {
	
	//option constants
	private static final String LEFT_INPUT_OPT = "l";
	private static final String RIGHT_INPUT_OPT = "r";
	
	private static final String OUTPUT_OPT = "o";
	
	/**
	 * Parses the options from the command line.
	 * @param args
	 * the application's arguments
	 * @return
	 * an {@link OptionParser} object that provides access to all parsed options and their values
	 */
	private static OptionParser getOptions(String[] args) {
//		final String tool_usage = "ChangeChecker -s spectra-zip-file [-r ranked-lines-file] [-u unranked-lines-file] -o output-file"; 
		final String tool_usage = "ChangeChecker";
		final OptionParser options = new OptionParser(tool_usage, args);

		options.add(LEFT_INPUT_OPT, "left", true, "Path to the left file (previous).", true);
		options.add(RIGHT_INPUT_OPT, "right", true, "Path to the right file (changed).", true);
		
//		options.add(OUTPUT_OPT, "output", true, "Path to output file.", true);
        
        options.parseCommandLine();
        
        return options;
	}
	
	/**
	 * @param args
	 * -s spectra-zip-file [-r ranked-lines-file] [-u unranked-lines-file] -o output-file
	 */
	public static void main(String[] args) {

		OptionParser options = getOptions(args);

		File left = options.isFile(LEFT_INPUT_OPT, true).toFile();
		File right = options.isFile(RIGHT_INPUT_OPT, true).toFile();

		FileDistiller distiller = ChangeDistiller.createFileDistiller(Language.JAVA);
		try {
		    distiller.extractClassifiedSourceCodeChanges(left, right);
		} catch(Exception e) {
		    /* An exception most likely indicates a bug in ChangeDistiller. Please file a
		       bug report at https://bitbucket.org/sealuzh/tools-changedistiller/issues and
		       attach the full stack trace along with the two files that you tried to distill. */
		    System.err.println("Warning: error while change distilling. " + e.getMessage());
		}

		List<SourceCodeChange> changes = distiller.getSourceCodeChanges();
		if(changes != null) {
		    for(SourceCodeChange change : changes) {
		        // see Javadocs for more information
		    	SourceCodeEntity entity = change.getChangedEntity();
		    	Misc.out(entity.toString());
		    	Misc.out("start: " + entity.getStartPosition() + ", end: " + entity.getEndPosition() + ", modifiers: " + entity.getModifiers());
		    	Misc.out(entity.getSourceRange().toString());
		    	
		    	ChangeType type = change.getChangeType();
		    	Misc.out(type.toString());
		    	String label = change.getLabel();
		    	Misc.out(label);
		    	SignificanceLevel level = change.getSignificanceLevel();
		    	Misc.out(level.toString());
		    	
		    	SourceCodeEntity parent = change.getParentEntity();
		    	Misc.out("parent: " + parent.toString());
		    	StructureEntityVersion root = change.getRootEntity();
		    	Misc.out("");
		    }
		}
		
	}
	
}
