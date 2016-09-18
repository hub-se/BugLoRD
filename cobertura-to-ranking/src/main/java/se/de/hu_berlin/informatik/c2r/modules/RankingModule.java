/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import se.de.hu_berlin.informatik.stardust.localizer.HitRanking;
import se.de.hu_berlin.informatik.stardust.localizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.stardust.localizer.Ranking;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.FaultLocalizerFactory;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.NoRanking;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AModule;
import se.de.hu_berlin.informatik.utils.tracking.ProgressBarTracker;

/**
 * Computes rankings for all coverage data stored in the 
 * input spectra and saves multiple ranking files for
 * various SBFL formulae to the hard drive.
 * 
 * @author Simon Heiden
 */
public class RankingModule extends AModule<ISpectra<String>, Object> {

	private String outputdir;
	private List<IFaultLocalizer<String>> localizers;
	
	/**
	 * @param outputdir
	 * path to the output directory
	 * @param localizers
	 * a list of Cobertura localizer identifiers
	 */
	public RankingModule(String outputdir, String... localizers) {
		super(true, true);
		this.outputdir = outputdir;
		if (localizers != null) {
			this.localizers = new ArrayList<>(localizers.length);

			//check if the given localizers can be found and abort in the negative case
			for (int i = 0; i < localizers.length; ++i) {
				try {
					this.localizers.add(FaultLocalizerFactory.newInstance(localizers[i]));
				} catch (IllegalArgumentException e) {
					Log.abort(this, e, "Could not find localizer '%s'.", localizers[i]);
				}
			}
		} else {
			this.localizers = new ArrayList<>(0);
		}
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public Object processItem(ISpectra<String> spectra) {
		//save a trace file that contains all executed lines
		try {
			HitRanking<String> ranking = new NoRanking<String>().localizeHit(spectra);
			Paths.get(outputdir).toFile().mkdirs();
			ranking.save(outputdir + File.separator + "ranking.trc");
		} catch (Exception e1) {
			Log.err(this, e1, "Could not save hit trace for spectra in '%s'.%n", 
					outputdir + File.separator + "ranking.trc");
		}
		
		ProgressBarTracker tracker = new ProgressBarTracker(1, localizers.size());
		//calculate the SBFL rankings, if any localizers are given
		for (IFaultLocalizer<String> localizer : localizers) {
			String className = localizer.getClass().getSimpleName();
			tracker.track("...calculating " + className + " ranking.");
//			Log.out(this, "...calculating " + className + " ranking.");
			generateRanking(spectra, localizer, className.toLowerCase());
		}
		return null;
	}
	
	/**
	 * Generates and saves a specific SBFL ranking. 
	 * @param spectra
	 * Cobertura line spectra
	 * @param localizer
	 * provides specific SBFL formulae
	 * @param subfolder
	 * name of a subfolder to be used
	 */
	private void generateRanking(ISpectra<String> spectra, final IFaultLocalizer<String> localizer, final String subfolder) {
		try {
			final Ranking<String> ranking = localizer.localize(spectra);
			Paths.get(outputdir + File.separator + subfolder).toFile().mkdirs();
			ranking.save(outputdir + File.separator + subfolder + File.separator + "ranking.rnk");
		} catch (Exception e) {
			Log.err(this, e, "Could not save ranking in '%s'.", outputdir + File.separator + subfolder + File.separator + "ranking.rnk");
		}
	}

}
