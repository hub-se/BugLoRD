/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend.plot;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import se.de.hu_berlin.informatik.defects4j.frontend.Prop;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithPaths;

/**
 * {@link Callable} object that runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class PlotAverageIgnoreZeroCall extends CallableWithPaths<String, Boolean> {

	private final static String SEP = File.separator;
	
	private final ParserStrategy strategy;
	private final String[] localizers;
	private String outputDir;
	
	/**
	 * Initializes a {@link PlotAverageIgnoreZeroCall} object with the given parameters.
	 * @param strategy
	 * the strategy to use when encountering equal-rank data points
	 * @param localizers
	 * the SBFL localizers to use
	 * @param outputDir
	 * the main plot output directory
	 */
	public PlotAverageIgnoreZeroCall(ParserStrategy strategy, String[] localizers, String outputDir) {
		super();
		this.strategy = strategy;
		this.localizers = localizers;
		this.outputDir = outputDir;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() {
		String project = getInput();
		
//		if (!Prop.validateProjectAndBugID(project, 1, false)) {
//			Misc.err("Project '" + project + "is not valid. Skipping...");
//			return false;
//		}

		//this is important!!
		Prop prop = new Prop().loadProperties(project, "1b", "1f");

		File projectDir = Paths.get(prop.archiveProjectDir).toFile();
		
		if (outputDir == null) {
			outputDir = prop.plotMainDir;
		}
		
		if (!projectDir.exists()) {
			if (new File(project).exists()) {
				/* #====================================================================================
				 * # plot averaged rankings for given path
				 * #==================================================================================== */
				String plotOutputDir = outputDir + SEP + "average" + SEP + project.replaceAll(SEP, "_");
				
				String height = "120";
				
				Plotter.plotAverageDefects4JProjectIgnoreZeroAndNegativeRankings(
						project, plotOutputDir, strategy, height, localizers);
				
				return true;
			} else {
				Log.abort(this, "Archive project directory doesn't exist: '" + prop.archiveProjectDir + "'.");
			}
		}
			
		/* #====================================================================================
		 * # plot averaged rankings for given project
		 * #==================================================================================== */
		String plotOutputDir = outputDir + SEP + "averageNoZero" + SEP + project;
		
		String height = "120";
		
		Plotter.plotAverageDefects4JProjectIgnoreZeroAndNegativeRankings(
				projectDir.toString(), plotOutputDir, strategy, height, localizers);
		
		return true;
	}

}

