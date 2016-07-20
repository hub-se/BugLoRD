/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithPaths;

/**
 * {@link Callable} object that runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class PlotAverageCall extends CallableWithPaths<String, Boolean> {

	private final static String SEP = File.separator;
	
	ParserStrategy strategy;
	String[] localizers;
	
	/**
	 * Initializes a {@link PlotAverageCall} object with the given parameters.
	 * @param strategy
	 * the strategy to use when encountering equal-rank data points
	 * @param localizers
	 * the SBFL localizers to use
	 */
	public PlotAverageCall(ParserStrategy strategy, String[] localizers) {
		super();
		this.strategy = strategy;
		this.localizers = localizers;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() {
		String project = getInput();
		
		if (!Prop.validateProjectAndBugID(project, 1, false)) {
			Misc.err("Project '" + project + "is not valid. Skipping...");
			return false;
		}

		//this is important!!
		Prop prop = new Prop().loadProperties(project, "1b", "1f");

		File projectDir = Paths.get(prop.archiveProjectDir).toFile();
		
		if (!projectDir.exists()) {
			Misc.abort("Archive project directory doesn't exist: '" + prop.archiveProjectDir + "'.");
		}
			
		/* #====================================================================================
		 * # plot averaged rankings for given project
		 * #==================================================================================== */
		String plotOutputDir = prop.plotMainDir + SEP + "average" + SEP + project;
		
		String height = "120";
		
		Plotter.plotAverageDefects4JProject(projectDir.toString(), plotOutputDir, strategy, height, localizers);
		
		return true;
	}

}

