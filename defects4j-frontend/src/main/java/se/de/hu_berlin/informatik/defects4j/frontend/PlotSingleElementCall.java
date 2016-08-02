/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithPaths;

/**
 * {@link Callable} object that runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class PlotSingleElementCall extends CallableWithPaths<String, Boolean> {

	private final static String SEP = File.separator;
	
	final String project;
	String[] localizers;
	
	/**
	 * Initializes a {@link PlotSingleElementCall} object with the given parameters.
	 * @param project
	 * the id of the project under consideration
	 * @param localizers
	 * the SBFL localizers to use
	 */
	public PlotSingleElementCall(String project, String[] localizers) {
		super();
		this.project = project;
		this.localizers = localizers;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() {
		String id = getInput();
		
		if (!Prop.validateProjectAndBugID(project, Integer.parseInt(id), false)) {
			Misc.err(this, "Combination of project '" + project + "' and bug '" + id + "' "
					+ "is not valid. Skipping...");
			return false;
		}
		String buggyID = id + "b";
		String fixedID = id + "f";
		
		//this is important!!
		Prop prop = new Prop().loadProperties(project, buggyID, fixedID);

		File archiveBuggyWorkDir = Paths.get(prop.archiveBuggyWorkDir).toFile();
		
		if (!archiveBuggyWorkDir.exists()) {
			Misc.abort(this, "Archive buggy project version directory doesn't exist: '" + prop.archiveBuggyWorkDir + "'.");
		}
			
		/* #====================================================================================
		 * # plot a single Defects4J element
		 * #==================================================================================== */
		String rankingDir = prop.archiveBuggyWorkDir + SEP + "ranking";
		
		String plotOutputDir = prop.plotMainDir + SEP + project;
		
		String range = "200";
		String height = "120";
		
		Plotter.plotSingleDefects4JElement(project, id, rankingDir, plotOutputDir, range, height, localizers);
		
		return true;
	}

}

