/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.sandbox;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;

import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AModule;

/**
 * Parser module that tokenizes a given input file and outputs a 
 * {@link List} of tokenized lines as {@link String}s. Uses the 
 * "com.github.javaparser" framework for parsing.
 * 
 * @author Simon Heiden
 * 
 */
public class JavaParserModule extends AModule<Path,List<String>> {

	// TODO create needed fields
	
	public JavaParserModule() {
		super(true);
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.miscellaneous.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public List<String> processItem(Path inputPath) {
		CompilationUnit cu = null;
		try (FileInputStream in = new FileInputStream(inputPath.toFile())) {
	        // parse the file
	        cu = JavaParser.parse(in);
	        	        
	    } catch (FileNotFoundException e) {
			Misc.err(this, e, "File not found...");
		} catch (IOException e) {
			Misc.err(this, e, "IO Exception...");
		} catch (ParseException e) {
			Misc.err(this, e, "Parser Exception...");
		}
		
		
		// visit and change the methods names and parameters
        TokenizeVisitor visitor = new TokenizeVisitor();
        visitor.visit(cu, null);

        // prints the changed compilation unit
        System.out.println(visitor.getSource());
        
        List<String> list = new ArrayList<>(1);
        list.add(cu.toStringWithoutComments());
		return list;
	}

}
