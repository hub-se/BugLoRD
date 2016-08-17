
package se.de.hu_berlin.informatik.changechecker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 *  
 * 
 * @author Simon Heiden
 */
public class ChangeChecker {
	
	public static final String SEPARATION_CHAR = ":";
	
	//option constants
	private static final String LEFT_INPUT_OPT = "l";
	private static final String RIGHT_INPUT_OPT = "r";
	
//	private static final String OUTPUT_OPT = "o";
	
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

		for (String element : checkForChanges(left, right)) {
			Log.out(ChangeChecker.class, element);
		}
	}
	
	/**
	 * Compares the given files and returns a String with information about all
	 * discovered changes, including line numbers, types and significance level.
	 * @param left
	 * the first file
	 * @param right
	 * the second file
	 * @return
	 * a String containing information about all discovered changes
	 */
	public static List<String> checkForChanges(File left, File right) {
		
		FileDistiller distiller = ChangeDistiller.createFileDistiller(Language.JAVA);
		try {
		    distiller.extractClassifiedSourceCodeChanges(left, right);
		} catch(Exception e) {
		    /* An exception most likely indicates a bug in ChangeDistiller. Please file a
		       bug report at https://bitbucket.org/sealuzh/tools-changedistiller/issues and
		       attach the full stack trace along with the two files that you tried to distill. */
			Log.err(ChangeChecker.class, "Warning: error while change distilling. " + e.getMessage());
		}

		List<SourceCodeChange> changes = distiller.getSourceCodeChanges();

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		List<String> lines = new ArrayList<>();

	    // Parse the class as a compilation unit.
	    parser.setKind(ASTParser.K_COMPILATION_UNIT);
	    try {
			parser.setSource(Misc.readFile2CharArray(left.toString()));
		} catch (IOException e) {
			Log.err(ChangeChecker.class, e, "Could not parse source file '%s'.", left);
			return lines;
		} // give your java source here as char array
	    parser.setResolveBindings(true);

	    // Return the compiled class as a compilation unit
	    CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
	    
		if(changes != null) {
		    for(SourceCodeChange change : changes) {
		        // see Javadocs for more information
		    	SourceCodeEntity parent = change.getParentEntity();
		    	SourceCodeEntity entity = change.getChangedEntity();
		    	
		    	ChangeType type = change.getChangeType();
		    	
		    	int startPos = entity.getStartPosition();
		    	int endPos = entity.getEndPosition();
		    	
//		    	Log.out(ChangeChecker.class, entity.getType() + SEPARATION_CHAR + 
//		    			change.getChangeType() + ", %d %d, %d %d, %d %d, %d %d", entity.getStartPosition(), entity.getEndPosition(), 
//		    			compilationUnit.getLineNumber(entity.getStartPosition()), compilationUnit.getLineNumber(entity.getEndPosition()),
//		    			parent.getStartPosition(), parent.getEndPosition(), 
//		    			compilationUnit.getLineNumber(parent.getSourceRange().getStart()), compilationUnit.getLineNumber(parent.getEndPosition()));
		    	
		    	switch(type) {
		    	case ADDITIONAL_CLASS:
		    	case ADDITIONAL_FUNCTIONALITY:
		    		//ignore additional methods, since you can't really decide a reasonable range of lines
		    		//that lets the developer decide that something is missing there...
		    		continue;
		    	case ADDITIONAL_OBJECT_STATE:
		    	case COMMENT_INSERT:
		    	case COMMENT_MOVE:
		    	case DOC_INSERT:
		    	case PARAMETER_INSERT:
		    	case PARENT_CLASS_INSERT:
		    	case PARENT_INTERFACE_INSERT:
		    	case RETURN_TYPE_INSERT:
		    	case STATEMENT_INSERT:
		    		if (parentIsAChangeOrInsideOfChange(parent, changes)) {
		    			continue;
		    		}
		    		startPos = parent.getStartPosition();
		    		endPos = parent.getEndPosition();
		    		break;
		    	default:
		    		break;
		    	}
		    	
		    	String modification_type = ChangeWrapper.MOD_UNKNOWN;
		    	switch(type) {
				case ADDITIONAL_CLASS:
				case ADDITIONAL_FUNCTIONALITY:
				case ADDITIONAL_OBJECT_STATE:
				case ALTERNATIVE_PART_INSERT:
				case COMMENT_INSERT:
				case DOC_INSERT:
				case PARAMETER_INSERT:
				case PARENT_CLASS_INSERT:
				case PARENT_INTERFACE_INSERT:
				case RETURN_TYPE_INSERT:
				case STATEMENT_INSERT:
					modification_type = ChangeWrapper.MOD_INSERT;
					break;
					
				case ALTERNATIVE_PART_DELETE:
				case COMMENT_DELETE:
				case DOC_DELETE:
				case PARAMETER_DELETE:
				case PARENT_CLASS_DELETE:
				case PARENT_INTERFACE_DELETE:
				case REMOVED_CLASS:
				case REMOVED_FUNCTIONALITY:
				case REMOVED_OBJECT_STATE:
				case REMOVING_ATTRIBUTE_MODIFIABILITY:
				case REMOVING_CLASS_DERIVABILITY:
				case REMOVING_METHOD_OVERRIDABILITY:
				case RETURN_TYPE_DELETE:
				case STATEMENT_DELETE:
					modification_type = ChangeWrapper.MOD_DELETE;
					break;
					
				case ATTRIBUTE_RENAMING:
				case ATTRIBUTE_TYPE_CHANGE:
				case CLASS_RENAMING:
				case COMMENT_UPDATE:
				case CONDITION_EXPRESSION_CHANGE:
				case DECREASING_ACCESSIBILITY_CHANGE:
				case DOC_UPDATE:
				case INCREASING_ACCESSIBILITY_CHANGE:
				case METHOD_RENAMING:
				case PARAMETER_ORDERING_CHANGE:
				case PARAMETER_RENAMING:
				case PARAMETER_TYPE_CHANGE:
				case PARENT_CLASS_CHANGE:
				case PARENT_INTERFACE_CHANGE:
				case RETURN_TYPE_CHANGE:
				case STATEMENT_ORDERING_CHANGE:
				case STATEMENT_PARENT_CHANGE:
				case STATEMENT_UPDATE:
					modification_type = ChangeWrapper.MOD_CHANGE;
					break;
					
				case UNCLASSIFIED_CHANGE:
				case ADDING_ATTRIBUTE_MODIFIABILITY:
				case ADDING_CLASS_DERIVABILITY:
				case ADDING_METHOD_OVERRIDABILITY:
				case COMMENT_MOVE:
				default:
					break;
		    	}
		    	
		    	
		    	lines.add(
		    			compilationUnit.getLineNumber(startPos) + SEPARATION_CHAR + 
		    			compilationUnit.getLineNumber(endPos) + SEPARATION_CHAR + 
		    			entity.getType() + SEPARATION_CHAR + 
		    			change.getChangeType() + SEPARATION_CHAR + 
		    			change.getSignificanceLevel()+ SEPARATION_CHAR +
		    			modification_type);
		    	
		    }
		}
		
		return lines;
	}

	private static boolean parentIsAChangeOrInsideOfChange(SourceCodeEntity parent, List<SourceCodeChange> changes) {
		int start = parent.getStartPosition();
		int end = parent.getEndPosition();
		for (SourceCodeChange change : changes) {
			if (change.getChangedEntity().getStartPosition() <= start &&
					change.getChangedEntity().getEndPosition() >= end) {
				return true;
			}
		}
		return false;
	}
	
}
